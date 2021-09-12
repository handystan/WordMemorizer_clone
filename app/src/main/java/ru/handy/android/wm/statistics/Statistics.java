package ru.handy.android.wm.statistics;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import ru.handy.android.wm.About;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.Help;
import ru.handy.android.wm.R;
import ru.handy.android.wm.learning.Category;
import ru.handy.android.wm.learning.CategoryAdapter;
import ru.handy.android.wm.setting.Utils;

public class Statistics extends AppCompatActivity implements View.OnClickListener {

    public static String NEW_CATEGORIES = "NEW_CATEGORIES";
    private GlobApp app;
    private ArrayList<Category> categoryStats;
    private Menu menu;
    private ListView lvChooseCat;
    private TextView tvRightAnswers;
    private TextView tvWrongAnswers;
    private Button bLearningMistakes;
    private Button bLearningAll;
    private CategoryAdapter cAdapter;
    private DB db;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

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

        lvChooseCat = (ListView) findViewById(R.id.lvChooseCat);
        tvRightAnswers = (TextView) findViewById(R.id.tvRightAnswers);
        tvWrongAnswers = (TextView) findViewById(R.id.tvWrongAnswers);
        bLearningMistakes = (Button) findViewById(R.id.bLearningMistakes);
        bLearningAll = (Button) findViewById(R.id.bLearningAll);

        // получаем из БД список категорий с ошибками
        categoryStats = db.getCategoryStats();

        //считаем кол-во правильных и неправильных ответов
        if (categoryStats.size() > 0) {
            int amountRight = categoryStats.get(categoryStats.size() - 1).getAmountRight();
            int amountWrong = categoryStats.get(categoryStats.size() - 1).getAmountWrong();
            int percentRight = Math.round(((float) amountRight) / (amountRight + amountWrong) * 100);
            int percentWrong = Math.round(((float) amountWrong) / (amountRight + amountWrong) * 100);
            tvRightAnswers.setText(String.format("%s%d (%d%%)", s(R.string.right_answers), amountRight, percentRight));
            tvWrongAnswers.setText(String.format("%s%d (%d%%)", s(R.string.wrong_answers), amountWrong, percentWrong));
        } else {
            tvRightAnswers.setText(String.format("%s0 (0%%)", s(R.string.right_answers)));
            tvWrongAnswers.setText(String.format("%s0 (0%%)", s(R.string.wrong_answers)));
            bLearningMistakes.setText(s(R.string.no_wrong_answers));
            bLearningAll.setVisibility(View.GONE);
        }
        if (categoryStats.size() > 0) categoryStats.remove(categoryStats.size() - 1);
        cAdapter = new CategoryAdapter(this, categoryStats, true);
        lvChooseCat.setAdapter(cAdapter);
        lvChooseCat.setItemsCanFocus(false);
        lvChooseCat.setOnItemClickListener((parent, view, position, id) -> {
            getIntent().putExtra(NEW_CATEGORIES, categoryStats.get(position).getName());
            setResult(RESULT_OK, getIntent());
            finish();
        });
        bLearningMistakes.setOnClickListener(this);
        bLearningAll.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        // обработка нажатия кнопки с добровольным взносом
        if (v.getId() == bLearningMistakes.getId() || v.getId() == bLearningAll.getId()) {
            StringBuilder s = new StringBuilder();
            for (Category c : cAdapter.getCheckedCategories()) {
                if (c.isChecked())
                    s.append(c.getName()).append(", ");
            }
            if (s.toString().equals("")) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.need_category), Toast.LENGTH_SHORT).show();
                return;
            }
            s = new StringBuilder(s.substring(0, s.length() - 2));
            getIntent().putExtra(NEW_CATEGORIES, s.toString());
            getIntent().putExtra("isOnlyMistakes", v.getId() == bLearningMistakes.getId());
            setResult(RESULT_OK, getIntent());
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.setGroupVisible(R.id.group_addrec, false);
        menu.setGroupVisible(R.id.group_dictionary, false);
        menu.setGroupVisible(R.id.group_statistics, false);
        menu.setGroupVisible(R.id.group_action_settings, false);
        menu.setGroupVisible(R.id.group_idata, false);
        menu.setGroupVisible(R.id.group_donate, false);
        menu.setGroupVisible(R.id.group_clear_hist, false);
        menu.setGroupVisible(R.id.group_exit, false);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        switch (item.getItemId()) {
            case android.R.id.home: // обрабатываем кнопку "назад" в ActionBar
                super.onBackPressed();
                return true;
            case R.id.resetStat: // сбрасываем всю статистику
                new AlertDialog.Builder(this)
                        .setMessage(s(R.string.you_want_reset_stat))
                        .setPositiveButton(s(R.string.yes), (dialog, which) -> {
                            //удаляем статистику из БД и обновляем интерфейс
                            new Thread(() -> db.removeStats()).start();
                            tvRightAnswers.setText(String.format("%s0 (0%%)", s(R.string.right_answers)));
                            tvWrongAnswers.setText(String.format("%s0 (0%%)", s(R.string.wrong_answers)));
                            bLearningMistakes.setText(s(R.string.no_wrong_answers));
                            lvChooseCat.setVisibility(View.GONE);
                            bLearningAll.setVisibility(View.GONE);
                        })
                        .setNegativeButton(R.string.no, null)
                        .create()
                        .show();
                return true;
            case R.id.ihelp:
                Intent intent = new Intent(this, Help.class);
                intent.putExtra("idhelp", 0);
                startActivity(intent);
                return true;
            case R.id.about:
                startActivity(new Intent(this, About.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        super.onDestroy();
    }
}
