package ru.handy.android.wm.dictionary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;
import ru.handy.android.wm.setting.Utils;

public class WordDescription extends AppCompatActivity implements OnClickListener {

    private GlobApp app;
    private TextView tvDictEngWord;
    private TextView tvTrascrip;
    private TextView tvRusTrasclate;
    private TextView tvCategory;
    private ImageView ivSound;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word);

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

        // Obtain the shared Tracker instance.
        app = (GlobApp) getApplication();
        mFBAnalytics = app.getFBAnalytics(); // получение экземпляра FirebaseAnalytics
        if (mFBAnalytics != null) {
            String[] arrClName = this.getClass().toString().split("\\.");
            app.openActEvent(arrClName[arrClName.length - 1]);
        }

        tvDictEngWord = (TextView) findViewById(R.id.tvDictEngWord);
        tvTrascrip = (TextView) findViewById(R.id.tvTrascrip);
        tvRusTrasclate = (TextView) findViewById(R.id.tvRusTrasclate);
        tvCategory = (TextView) findViewById(R.id.tvCategory);
        ivSound = (ImageView) findViewById(R.id.ivSound);
        ivSound.setOnClickListener(this);
        final Drawable defBackground = ivSound.getBackground(); // фон по умолчанию для кнопки с озвучки
        ivSound.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_MOVE) {
                    ivSound.setBackgroundResource(R.color.bright_blue);
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= 16) {
                        ivSound.setBackground(defBackground);
                    } else {
                        ivSound.setBackgroundDrawable(defBackground);
                    }
                }
                return false;
            }
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
