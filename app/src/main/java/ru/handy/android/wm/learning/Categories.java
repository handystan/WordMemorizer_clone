package ru.handy.android.wm.learning;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;
import ru.handy.android.wm.setting.Utils;

/**
 * класс позволяющий выбирать категорию(и) для обучения
 */
public class Categories extends AppCompatActivity implements OnClickListener {

    public static String NEW_CATEGORIES = "NEW_CATEGORIES";
    private GlobApp app;
    private EditText etInputWord;
    private ListView lvCategories;
    private Button bChooseCat;
    private CategoryAdapter cAdapter;
    private DB db;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories);

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
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        // устанавливаем цвет фона и шрифта для toolbar
        Utils.colorizeToolbar(this, toolbar);
        // устанавливаем цвет стрелки "назад" в toolbar
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Utils.getFontColorToolbar(), PorterDuff.Mode.SRC_ATOP);
        bar.setHomeAsUpIndicator(upArrow);

        lvCategories = (ListView) findViewById(R.id.lvCategories);
        bChooseCat = (Button) findViewById(R.id.bChooseCat);
        etInputWord = (EditText) findViewById(R.id.etInputWord);
        bChooseCat.setOnClickListener(this);
        lvCategories.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // для ускорения отображения сначала показываем категории без кол-ва слов
        ArrayList<String> cats = db.getCategories();
        final ArrayList<Category> categories = new ArrayList<>();
        for (int i = 0; i < cats.size(); i++) {
            categories.add(new Category(cats.get(i), 0));
        }
        cAdapter = new CategoryAdapter(this, categories, false);
        lvCategories.setAdapter(cAdapter);
/*
        // в actionBar делаем доступной кнопку "назад"
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
*/

        etInputWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // здесь ничего не делаем
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // здесь ничего не делаем
            }

            @Override
            public void afterTextChanged(Editable s) {
                for (int i = 0; i < categories.size(); i++) {
                    String catName = categories.get(i).getName();
                    categories.get(i).setShow(s.toString().equals("")
                            || catName.toLowerCase().contains(s.toString().toLowerCase()));
                }
                cAdapter.notifyDataSetChanged(); // обновляем адаптер
            }
        });
        // затем в отдельном потоке отображаем и кол-во слов в каждой категории
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<Category> newCategories = db.getClassCategories();
                if (newCategories != null) {
                    for (int i = 0; i < categories.size(); i++) {
                        categories.get(i).setAmount(newCategories.get(i).getAmount());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cAdapter.notifyDataSetChanged(); // обновляем адаптер
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
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
        setResult(RESULT_OK, getIntent());
        finish();
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d("myLogs", "onDestroy Categories");
        super.onDestroy();
    }
}
