package ru.handy.android.wm;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawableWrapper;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.sql.Date;

import ru.handy.android.wm.learning.Learning;
import ru.handy.android.wm.setting.Utils;

public class About extends AppCompatActivity implements View.OnClickListener {

    private GlobApp app;
    private TextView mTextView;
    private int amountClick = 0; // количество нажатий на текстовое поле
    private DB db;
    private Tracker mTracker; // трекер для Google analitics, чтобы отслеживать активности пользователей


    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mTracker = app.getDefaultTracker(); // Obtain the shared Tracker instance
        db = app.getDb(); // открываем подключение к БД

        // устанавливаем toolbar и actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        // устанавливаем цвет фона и шрифта для toolbar
        Utils.colorizeToolbar(this, toolbar);
        // устанавливаем цвет стрелки "назад" в toolbar
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Utils.getFontColorToolbar(), PorterDuff.Mode.SRC_ATOP);
        bar.setHomeAsUpIndicator(upArrow);

        mTextView = (TextView) findViewById(R.id.tvAbout);
        mTextView.setText(Html.fromHtml(s(R.string.about_desc)));
        mTextView.setOnClickListener(this);
    }

    // обрабатываем кнопку "назад" в ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 10-кратное нажатие на поле позволяет скрыть или показать "Поддержку разработчика" в Learning
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == mTextView.getId()) {
            amountClick++;
            if (amountClick == 10) {
                String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
                int amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
                amountDonate = amountDonate == 0 ? 1 : 0;
                db.updateRecExitState(DB.AMOUNT_DONATE, amountDonate + "");
                if (amountDonate == 0) {
                    db.updateRecExitState(DB.DATE_TRIAL_STATS, "");
                    db.updateRecExitState(DB.DATE_BG_COLOR, "");
                    db.updateRecExitState(DB.DATE_LEARNING_METHOD, "");
                    db.updateRecExitState(DB.DATE_LANGUAGE, "");
                    db.updateRecExitState(DB.DATE_LANG_WORD_AMOUNT, "");
                }
                Learning learning = app.getLearning();
                learning.setAmountDonate(amountDonate);
                LinearLayout llThanks = learning.getLlThanks();
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) llThanks.getLayoutParams();
                params.height = amountDonate == 0 ? LinearLayout.LayoutParams.WRAP_CONTENT : 0;
                llThanks.setLayoutParams(params);
                amountClick = 0;
            }
        }
    }

    private String s(int res) {
        return getResources().getString(res);
    }

    @Override
    public void onResume() {
        if (mTracker != null) {
            Log.i("myLogs", "Setting screen name: " + this.getLocalClassName());
            mTracker.setScreenName("Activity " + this.getLocalClassName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d("myLogs", "onDestroy Thanks");
        super.onDestroy();
    }
}
