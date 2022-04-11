package ru.handy.android.wm.dictionary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yodo1.mas.banner.Yodo1MasBannerAdView;

import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;
import ru.handy.android.wm.setting.Utils;

public class WordDescription extends AppCompatActivity implements OnClickListener {

    private GlobApp app;
    private DB db;
    private TextView tvDictEngWord;
    private TextView tvTrascrip;
    private TextView tvRusTrasclate;
    private TextView tvCategory;
    private ImageView ivSound;
    private LinearLayout llAdMobWordDescr;
    private Yodo1MasBannerAdView avBottomBannerWordDescr;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word);

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

        // Obtain the shared Tracker instance.
        app = (GlobApp) getApplication();
        mFBAnalytics = app.getFBAnalytics(); // получение экземпляра FirebaseAnalytics
        if (mFBAnalytics != null) {
            String[] arrClName = this.getClass().toString().split("\\.");
            app.openActEvent(arrClName[arrClName.length - 1]);
        }
        db = app.getDb(); // открываем подключение к БД

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        int amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        avBottomBannerWordDescr = findViewById(R.id.avBottomBannerWordDescr);
        llAdMobWordDescr = findViewById(R.id.llAdMobWordDescr);
        // загружаем баннерную рекламу yodo1
        avBottomBannerWordDescr.loadAd();
        ViewGroup.LayoutParams params = llAdMobWordDescr.getLayoutParams();
        if (amountDonate > 0) {
            params.height = 0;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName() + " без отображения");
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName());
        }
        llAdMobWordDescr.setLayoutParams(params);

        tvDictEngWord = findViewById(R.id.tvDictEngWord);
        tvTrascrip = findViewById(R.id.tvTrascrip);
        tvRusTrasclate = findViewById(R.id.tvRusTrasclate);
        tvCategory = findViewById(R.id.tvCategory);
        ivSound = findViewById(R.id.ivSound);
        ivSound.setOnClickListener(this);
        final Drawable defBackground = ivSound.getBackground(); // фон по умолчанию для кнопки с озвучки
        ivSound.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                ivSound.setBackgroundResource(R.color.bright_blue);
            } else {
                ivSound.setBackground(defBackground);
            }
            return false;
        });
        Intent intent = getIntent();
        tvDictEngWord.setText(intent.getStringExtra("c_ew_engword"));
        tvTrascrip.setText(intent.getStringExtra("c_ew_transcription"));
        tvRusTrasclate.setText(intent.getStringExtra("c_ew_rustranslate"));
        tvCategory.setText(intent.getStringExtra("c_ew_category"));
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivSound) {
            app.speak(tvDictEngWord.getText().toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d("myLogs", "onDestroy WordDictionary");
        super.onDestroy();
    }
}
