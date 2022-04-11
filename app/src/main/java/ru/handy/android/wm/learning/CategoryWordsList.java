package ru.handy.android.wm.learning;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yodo1.mas.banner.Yodo1MasBannerAdView;

import java.util.ArrayList;
import java.util.Collections;

import ru.handy.android.wm.About;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;
import ru.handy.android.wm.setting.Utils;

/**
 * класс с со словами категории из уровка (все, отгаданные и не отгаданные)
 */
public class CategoryWordsList extends AppCompatActivity implements View.OnClickListener {

    private boolean isUpdatedLesson = false; // была ли нажат пункт меню по обновлению урока
    private GlobApp app;
    private ListView lvCatWordsList;
    private Button bAllWordsCWL;
    private Button bRightWordsCWL;
    private Button bWrongWordsCWL;
    private ArrayList<Word> allWords; // кол-во слов
    private ArrayList<Word> rightWords; // кол-во отгаданных слов
    private ArrayList<Word> wrongWords; // кол-во не отгаданных слов
    private WordsAdapter wAdapter;
    private LinearLayout llAdMobCatWordList;
    private Yodo1MasBannerAdView avBottomBannerCatWordList;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
        }

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        int amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        avBottomBannerCatWordList = findViewById(R.id.avBottomBannerCatWordList);
        llAdMobCatWordList = findViewById(R.id.llAdMobCatWordList);
        // загружаем баннерную рекламу yodo1
        avBottomBannerCatWordList.loadAd();
        ViewGroup.LayoutParams params = llAdMobCatWordList.getLayoutParams();
        if (amountDonate > 0) {
            params.height = 0;
            Log.i("myLogs", "загружена баннерная реклама в CategoryWordsList без отображения");
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Log.i("myLogs", "загружена баннерная реклама в CategoryWordsList");
        }
        llAdMobCatWordList.setLayoutParams(params);

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

        initWordList();
    }

    /**
     * инициирование урока данными, в т.ч. и после нажаитя кнопки "Обновить урок"
     */
    private void initWordList() {
        allWords = db.getAllWordsInCurLesson();
        rightWords = db.getRightWordsInCurLesson();
        wrongWords = db.getAllWrongWordsInCurLesson();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.setGroupVisible(R.id.group_renew_lesson, true);
        menu.setGroupVisible(R.id.group_about, true);
        return true;
    }

    // обрабатываем кнопку "назад" в ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        if (item.getItemId() == android.R.id.home) {
            createIntent();
            finish();
            return true;
        } else if (item.getItemId() == R.id.renew_lesson) { // загружаем урок заново
            new AlertDialog.Builder(this)
                    .setMessage(s(R.string.renew_current_lesson))
                    .setPositiveButton(s(R.string.yes), (dialog, which) -> {
                        new Fixing(db, db.getCategoryCurLesson(), true, false);
                        initWordList();
                        isUpdatedLesson = true;
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
        } else if (item.getItemId() == R.id.about) { // вызов страницы О программе
            startActivity(new Intent(this, About.class));
        }
        return super.onOptionsItemSelected(item);
    }

    // обработка кнопки назад
    @Override
    public void onBackPressed() {
        createIntent();
        super.onBackPressed();
    }

    private void createIntent() {
        Intent intent = new Intent();
        intent.putExtra("isUpdatedLesson", isUpdatedLesson);
        setResult(RESULT_OK, intent);
    }

    private String s(int res) {
        return getResources().getString(res);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d("myLogs", "onDestroy CategoryWordsList");
        super.onDestroy();
    }
}