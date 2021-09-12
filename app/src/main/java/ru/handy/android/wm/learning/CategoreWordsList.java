package ru.handy.android.wm.learning;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;

import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;
import ru.handy.android.wm.setting.Utils;

/**
 * класс с со словами категории из уровка (все, отгаданные и не отгаданные)
 */
public class CategoreWordsList extends AppCompatActivity implements View.OnClickListener {

    private GlobApp app;
    private ListView lvCatWordsList;
    private Button bAllWordsCWL;
    private Button bRightWordsCWL;
    private Button bWrongWordsCWL;
    private ArrayList<Word> allWords; // кол-во слов
    private ArrayList<Word> rightWords; // кол-во отгаданных слов
    private ArrayList<Word> wrongWords; // кол-во не отгаданных слов
    private WordsAdapter wAdapter;
    private DB db;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_words_list);

        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mFBAnalytics = app.getFBAnalytics(); // получение экземпляра FirebaseAnalytics
        if (mFBAnalytics != null) {
            String[] arrClName = this.getClass().toString().split("\\.");
            app.openActEvent(arrClName[arrClName.length - 1]);
        }
        db = app.getDb(); // открываем подключение к БД

        // устанавливаем toolbar и actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        lvCatWordsList = findViewById(R.id.lvCatWordsList);
        bAllWordsCWL = findViewById(R.id.bAllWordsCWL);
        bAllWordsCWL.setOnClickListener(this);
        bRightWordsCWL = findViewById(R.id.bRightWordsCWL);
        bRightWordsCWL.setOnClickListener(this);
        bWrongWordsCWL = findViewById(R.id.bWrongWordsCWL);
        bWrongWordsCWL.setOnClickListener(this);

        allWords = db.getAllWordsInLesson();
        rightWords = db.getRightWordsInLesson();
        wrongWords = db.getAllWrongWordsInLesson();
        Collections.sort(allWords, (word1, word2) -> word1.getEngWord().compareToIgnoreCase(word2.getEngWord()));
        Collections.sort(rightWords, (word1, word2) -> word1.getEngWord().compareToIgnoreCase(word2.getEngWord()));
        Collections.sort(wrongWords, (word1, word2) -> word1.getEngWord().compareToIgnoreCase(word2.getEngWord()));
        wAdapter = new WordsAdapter(app, allWords);
        lvCatWordsList.setAdapter(wAdapter);
        bAllWordsCWL.setText(getResources().getString(R.string.ofWords) + " " + allWords.size());
        bRightWordsCWL.setText(getResources().getString(R.string.right) + " " + rightWords.size());
        bWrongWordsCWL.setText(getResources().getString(R.string.wrong) + " " + wrongWords.size());
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        // обработка нажатия строки с количеством слов
        Log.d("myLogs", "onClick");
        if (v.getId() == bAllWordsCWL.getId()) {
            wAdapter = new WordsAdapter(app, allWords);
            lvCatWordsList.setAdapter(wAdapter);
            setTitle(getResources().getString(R.string.category_words_list));
        } else if (v.getId() == bRightWordsCWL.getId()) {
            Log.d("myLogs", "onClick tvRightWordsWLC");
            wAdapter = new WordsAdapter(app, rightWords);
            lvCatWordsList.setAdapter(wAdapter);
            setTitle(getResources().getString(R.string.right_words));
        } else if (v.getId() == bWrongWordsCWL.getId()) {
            wAdapter = new WordsAdapter(app, wrongWords);
            lvCatWordsList.setAdapter(wAdapter);
            setTitle(getResources().getString(R.string.wrong_words));
        }
    }

    // обрабатываем кнопку "назад" в ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d("myLogs", "onDestroy WrongWords");
        super.onDestroy();
    }
}