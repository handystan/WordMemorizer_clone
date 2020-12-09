package ru.handy.android.wm.learning;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.R;
import ru.handy.android.wm.setting.Utils;

public class WrongWords extends AppCompatActivity {

    private GlobApp app;
    private TextView tvNoWrongWords;
    private ListView lvWrongWords;
    private WordsAdapter wAdapter;
    private DB db;
    private Tracker mTracker; // трекер для Google analitics, чтобы отслеживать активности пользователей

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrong_words);

        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mTracker = app.getDefaultTracker(); // Obtain the shared Tracker instance.
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

        tvNoWrongWords = (TextView) findViewById(R.id.tvNoWrongWords);
        lvWrongWords = (ListView) findViewById(R.id.lvWrongWords);
        ArrayList<Word> words = db.getAllWrongWordsInLesson();
        Collections.sort(words, new Comparator<Word>() {
            @Override
            public int compare(Word word1, Word word2) {
                return word1.getEngWord().compareTo(word2.getEngWord());
            }
        });
        if (words.size() == 0) {
            tvNoWrongWords.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        } else {
            tvNoWrongWords.setLayoutParams(new LayoutParams(0, 0));
        }
        wAdapter = new WordsAdapter(app, words);
        lvWrongWords.setAdapter(wAdapter);
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
    public void onDestroy() {
        Log.d("myLogs", "onDestroy WrongWords");
        super.onDestroy();
    }
}