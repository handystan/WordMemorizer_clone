package ru.handy.android.wm;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yodo1.mas.banner.Yodo1MasBannerAdView;

import ru.handy.android.wm.setting.Utils;

public class Help extends AppCompatActivity {

    private GlobApp app;
    private int idHelp; // id ActionBar, который выбран на момент вызова помощи
    private String linkWord;
    private TextView mTextView;
    private LinearLayout llAdMobHelp;
    private Yodo1MasBannerAdView avBottomBannerHelp;
    private DB db;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        idHelp = getIntent().getIntExtra("idhelp", 0);

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
        llAdMobHelp = findViewById(R.id.llAdMobHelp);
        avBottomBannerHelp = findViewById(R.id.avBottomBannerHelp);
        // загружаем баннерную рекламу yodo1
        avBottomBannerHelp.loadAd();
        ViewGroup.LayoutParams params = llAdMobHelp.getLayoutParams();
        if (amountDonate > 0) {
            params.height = 0;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName() + " без отображения");
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName());
        }
        llAdMobHelp.setLayoutParams(params);

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

        mTextView = findViewById(R.id.tvHelp);
        int idTvHelp = idHelp == 0 ? R.string.helpForLearning
                : (idHelp == 1 ? R.string.helpForDict : R.string.helpForData);
        setTVText(idTvHelp);
        mTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setTVText(int idTvHelp) {
        // преобразуем html-текст в текст реальными ссылками
        String html = s(idTvHelp);
        // Split string to parts:
        String[] devFull = html.split("<a href='#'>");
        // Adding first part:
        mTextView.append(Html.fromHtml(devFull[0]));
        // Creating array for parts with links (they amount always will
        // devFull.length-1):
        SpannableString[] link = new SpannableString[devFull.length - 1];
        // local vars:
        ClickableSpan[] cs = new ClickableSpan[devFull.length - 1];
        String[] devDevFull;

        for (int i = 1; i < devFull.length; i++) {
            // obtaining 'clear' link
            devDevFull = devFull[i].split("</a>");
            link[i - 1] = new SpannableString(devDevFull[0]);
            linkWord = devDevFull[0];
            cs[i - 1] = new ClickableSpan() {
                final String w = linkWord;

                @Override
                public void onClick(View widget) {
                    if (w.equals("На главную")) {
                        mTextView.setText(Html.fromHtml(""));
                        setTVText(R.string.helpForLearning);
                    } else if (w.equals("Статистика")) {
                        mTextView.setText(Html.fromHtml(""));
                        setTVText(R.string.helpForStat);
                    } else if (w.equals("Словарь")) {
                        mTextView.setText(Html.fromHtml(""));
                        setTVText(R.string.helpForDict);
                    } else if (w.equals("Загрузка/выгрузка слов")) {
                        mTextView.setText(Html.fromHtml(""));
                        setTVText(R.string.helpForData);
                    } else if (w.equals("категорию") || w.equals("категории")
                            || w.equals("категории(й)")
                            || w.equals("категорию(и)")
                            || w.equals("категория")) {
                        mTextView.setText(Html.fromHtml(""));
                        setTVText(R.string.helpCategory);
                    }
                }
            };
            link[i - 1].setSpan(cs[i - 1], 0, linkWord.length(), 0);
            mTextView.append(link[i - 1]);
            try {
                mTextView.append(Html.fromHtml(devDevFull[1]));
            } catch (Exception e) {
            }
        }
    }

    // обрабатываем кнопку "назад" в ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        if (item.getItemId() == android.R.id.home) {
            if (mTextView.getText().toString().contains("На главную")) {
                mTextView.setText(Html.fromHtml(""));
                setTVText(R.string.helpForLearning);
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // обработка кнопки назад
    @Override
    public void onBackPressed() {
        if (mTextView.getText().toString().contains("На главную")) {
            mTextView.setText(Html.fromHtml(""));
            setTVText(R.string.helpForLearning);
        } else {
            super.onBackPressed();
        }
    }

    private String s(int res) {
        return getResources().getString(res);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
