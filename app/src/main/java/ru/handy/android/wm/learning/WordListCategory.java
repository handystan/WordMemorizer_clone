package ru.handy.android.wm.learning;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;

import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.R;
import ru.handy.android.wm.dictionary.WordDescription;
import ru.handy.android.wm.dictionary.WordEdit;
import ru.handy.android.wm.setting.Utils;

public class WordListCategory extends AppCompatActivity {

    private GlobApp app;
    private static final int CM_SHOW_ID = 0;
    private static final int CM_EDIT_ID = 1;
    private static final int CM_ADD_ID = 2;
    private static final int CM_DELETE_ID = 3;
    private ListView lvWords;
    private TextView tvAmountWordsWLC;
    private FloatingActionButton fab;
    private DB db;
    private WordsAdapter wAdapter;
    private Tracker mTracker; // трекер для Google analitics, чтобы отслеживать активности пользователей

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_list_category);
        Log.d("myLogs", "onCreate WordListCategory");

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
        // устанавливаем плавающию кнопку с перечнем слов в категории
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_new_word), Toast.LENGTH_LONG).show();
                return false;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record(0, WordEdit.class); // добавление слова
            }
        });
        fab.setBackgroundTintList(ColorStateList.valueOf(Utils.getFabColor()));

        lvWords = (ListView) findViewById(R.id.lvWordListCat);
        tvAmountWordsWLC = (TextView) findViewById(R.id.tvAmountWordsWLC);

        ArrayList<Word> words = db.getAllWordsInLesson();
        Collections.sort(words);
        wAdapter = new WordsAdapter(app, this, words);
        lvWords.setAdapter(wAdapter);
        lvWords.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                record(id, WordDescription.class);
            }
        });
        // добавляем контекстное меню к списку
        registerForContextMenu(lvWords);

        tvAmountWordsWLC.setText("  " + getResources().getString(R.string.ofWords) + " "
                + words.size());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_SHOW_ID, 0, R.string.show_record);
        menu.add(0, CM_EDIT_ID, 1, R.string.edit_record);
        menu.add(0, CM_ADD_ID, 2, R.string.add_record);
        menu.add(0, CM_DELETE_ID, 3, R.string.delete_record);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // получаем из пункта контекстного меню данные по пункту списка
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        // извлекаем id записи
        int id = ((Word) wAdapter.getItem(acmi.position)).getId();
        switch (item.getItemId()) {
            case CM_SHOW_ID: // смотрим запись
                record(id, WordDescription.class);
                return true;
            case CM_EDIT_ID: // редактируем запись
                record(id, WordEdit.class);
                return true;
            case CM_ADD_ID: // добавляем запись
                record(0, WordEdit.class);
                return true;
            case CM_DELETE_ID: // удаляем запись
                db.delRecEngWord(id);
                wAdapter.deleteWord(id);
                wAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.word_is_deleted), Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * показывает всю информацию по английскому слову
     *
     * @param id            - идентификатор в таблице английских слов
     * @param activityClass - класс для вызываемого Activity
     */
    private void record(long id, Class<? extends AppCompatActivity> activityClass) {
        Cursor cursor = db.getWordById(id);
        Intent intent = new Intent(this, activityClass);
        if (cursor != null && cursor.moveToFirst()) {
            intent.putExtra("c_ew_id",
                    cursor.getString(cursor.getColumnIndex(DB.C_EW_ID)));
            intent.putExtra("c_ew_engword",
                    cursor.getString(cursor.getColumnIndex(DB.C_EW_ENGWORD)));
            intent.putExtra("c_ew_transcription", cursor.getString(cursor
                    .getColumnIndex(DB.C_EW_TRANSCRIPTION)));
            intent.putExtra("c_ew_rustranslate", cursor.getString(cursor
                    .getColumnIndex(DB.C_EW_RUSTRANSLATE)));
            intent.putExtra("c_ew_category",
                    cursor.getString(cursor.getColumnIndex(DB.C_EW_CATEGORY)));
            cursor.close();
        }
        if (activityClass == WordEdit.class) {
            startActivityForResult(intent, 0);
        } else {
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            // если вернулось отредактирвоанное слово
            if (requestCode == 0) {
                Word word = data.getParcelableExtra("editWord");
                if (word != null) {
                    wAdapter.updateWord(word);
                    wAdapter.notifyDataSetChanged();
                }
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            // do nothing
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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
    protected void onDestroy() {
        Log.d("myLogs", "onDestroy WordListCategory");
        super.onDestroy();
    }

}
