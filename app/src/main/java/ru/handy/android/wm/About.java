package ru.handy.android.wm;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import ru.handy.android.wm.learning.Learning;
import ru.handy.android.wm.setting.Utils;

public class About extends AppCompatActivity implements View.OnClickListener {

    private GlobApp app;
    private TextView mTextView;
    private int amountClick = 0; // количество нажатий на текстовое поле
    private LinearLayout llAdMobAbout;
    private AdView avBottomBannerAbout;
    private DB db;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics


    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mFBAnalytics = app.getFBAnalytics(); // получение экземпляра FirebaseAnalytics
        if (mFBAnalytics != null) {
            String[] arrClName = this.getClass().toString().split("\\.");
            app.openActEvent(arrClName[arrClName.length - 1]);
        }
        db = app.getDb(); // открываем подключение к БД

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        int amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        avBottomBannerAbout = findViewById(R.id.avBottomBannerAbout);
        llAdMobAbout = findViewById(R.id.llAdMobAbout);
        // инициализация AdMob для рекламы
        MobileAds.initialize(this, initializationStatus ->
                Log.d("myLogs", "AdMob in " + getClass().getSimpleName() + " is initialized"));
        AdRequest adRequest = new AdRequest.Builder().build();
        // загружаем баннерную рекламу
        avBottomBannerAbout.loadAd(adRequest);
        ViewGroup.LayoutParams params = llAdMobAbout.getLayoutParams();
        if (amountDonate > 0) {
            params.height = 0;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName() + " без отображения");
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName());
        }
        llAdMobAbout.setLayoutParams(params);

        // устанавливаем toolbar и actionbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
        }
        // устанавливаем цвет фона и шрифта для toolbar
        Utils.colorizeToolbar(this, toolbar);
        // устанавливаем цвет стрелки "назад" в toolbar
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        if (upArrow != null && bar != null) {
            upArrow.setColorFilter(Utils.getFontColorToolbar(), PorterDuff.Mode.SRC_ATOP);
            bar.setHomeAsUpIndicator(upArrow);
        }

        mTextView = findViewById(R.id.tvAbout);
        mTextView.setText(Html.fromHtml(s(R.string.about_desc)));
        mTextView.setOnClickListener(this);
    }

    // обрабатываем кнопку "назад" в ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                /*if (amountDonate == 0) { //  (с 32 версии не актуальный функционал
                    db.updateRecExitState(DB.DATE_TRIAL_STATS, "");
                    db.updateRecExitState(DB.DATE_BG_COLOR, "");
                    db.updateRecExitState(DB.DATE_LEARNING_METHOD, "");
                    db.updateRecExitState(DB.DATE_LANGUAGE, "");
                    db.updateRecExitState(DB.DATE_LANG_WORD_AMOUNT, "");
                }*/
                ViewGroup.LayoutParams params = llAdMobAbout.getLayoutParams();
                if (amountDonate > 0) {
                    params.height = 0;
                    Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName() + " без отображения");
                } else {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName());
                }
                llAdMobAbout.setLayoutParams(params);
                amountClick = 0;
            }
        }
    }

    private String s(int res) {
        return getResources().getString(res);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d("myLogs", "onDestroy Thanks");
        super.onDestroy();
    }
}
