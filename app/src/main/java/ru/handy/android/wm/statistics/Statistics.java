package ru.handy.android.wm.statistics;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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
    private Tracker mTracker; // трекер для Google analitics, чтобы отслеживать активности пользователей

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

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
            tvRightAnswers.setText(s(R.string.right_answers) + amountRight + " (" + percentRight + "%)");
            tvWrongAnswers.setText(s(R.string.wrong_answers) + amountWrong + " (" + percentWrong + "%)");
        } else {
            tvRightAnswers.setText(s(R.string.right_answers) + "0 (0%)");
            tvWrongAnswers.setText(s(R.string.wrong_answers) + "0 (0%)");
            bLearningMistakes.setText(s(R.string.no_wrong_answers));
            bLearningAll.setVisibility(View.GONE);
        }
        if (categoryStats.size() > 0) categoryStats.remove(categoryStats.size() - 1);
        cAdapter = new CategoryAdapter(this, categoryStats, true);
        lvChooseCat.setAdapter(cAdapter);
        lvChooseCat.setItemsCanFocus(false);
        lvChooseCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getIntent().putExtra(NEW_CATEGORIES, categoryStats.get(position).getName());
                setResult(RESULT_OK, getIntent());
                finish();
            }
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
            String s = "";
            for (Category c : cAdapter.getCheckedCategories()) {
                if (c.isChecked())
                    s += c.getName() + ", ";
            }
            if (s.equals("")) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.need_category), Toast.LENGTH_SHORT).show();
                return;
            }
            s = s.substring(0, s.length() - 2);
            getIntent().putExtra(NEW_CATEGORIES, s);
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
                        .setPositiveButton(s(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //удаляем статистику из БД и обновляем интерфейс
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        db.removeStats();
                                    }
                                }).start();
                                tvRightAnswers.setText(s(R.string.right_answers) + "0 (0%)");
                                tvWrongAnswers.setText(s(R.string.wrong_answers) + "0 (0%)");
                                bLearningMistakes.setText(s(R.string.no_wrong_answers));
                                lvChooseCat.setVisibility(View.GONE);
                                bLearningAll.setVisibility(View.GONE);
                            }
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
        if (mTracker != null) {
            Log.i("myLogs", "Setting screen name: " + this.getLocalClassName());
            mTracker.setScreenName("Activity " + this.getLocalClassName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
