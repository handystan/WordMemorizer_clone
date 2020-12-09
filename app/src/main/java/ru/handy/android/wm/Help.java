package ru.handy.android.wm;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.handy.android.wm.setting.Utils;

public class Help extends AppCompatActivity {

    private int idHelp; // id ActionBar, который выбран на момент вызова помощи
    private String linkWord;
    private TextView mTextView;
    private Tracker mTracker; // трекер для Google analitics, чтобы отслеживать активности пользователей

    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        Intent intent = getIntent();
        idHelp = intent.getIntExtra("idhelp", 0);

        // Obtain the shared Tracker instance.
        GlobApp app = (GlobApp) getApplication();
        mTracker = app.getDefaultTracker();

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

        mTextView = (TextView) findViewById(R.id.tvHelp);
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
        String[] devDevFull = new String[2];

        for (int i = 1; i < devFull.length; i++) {
            // obtaining 'clear' link
            devDevFull = devFull[i].split("</a>");
            link[i - 1] = new SpannableString(devDevFull[0]);
            linkWord = devDevFull[0];
            cs[i - 1] = new ClickableSpan() {
                String w = linkWord;

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
                    } else if (w.equals("Данные")) {
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
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mTextView.getText().toString().contains("На главную")) {
                    mTextView.setText(Html.fromHtml(""));
                    setTVText(R.string.helpForLearning);
                } else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        if (mTracker != null) {
            Log.i("myLogs", "Setting screen name: " + this.getLocalClassName());
            mTracker.setScreenName("Activity " + this.getLocalClassName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        super.onResume();
    }
}
