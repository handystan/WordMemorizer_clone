package ru.handy.android.wm.dictionary;

import android.annotation.SuppressLint;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.handy.android.wm.About;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.Help;
import ru.handy.android.wm.R;
import ru.handy.android.wm.Thanks;
import ru.handy.android.wm.setting.Settings;
import ru.handy.android.wm.setting.Utils;

public class Dictionary extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private GlobApp app;
    private static final int CM_SHOW_ID = 0;
    private static final int CM_EDIT_ID = 1;
    private static final int CM_ADD_ID = 2;
    private static final int CM_DELETE_ID = 3;
    private ListView lvWord;
    private EditText etInputWord;
    private TextView tvNoWords;
    private FloatingActionButton fab;
    private DB db;
    private SimpleCursorAdapter scAdapter;
    // какие слова будут в поиске: true-английские, false-русские
    private boolean isEnglSearch = true;
    // правила поиска: true-по начальным буквам слова, false-по буквам с любой части слова
    private boolean isSearchRule1 = true;
    // показывать историю, когда строка поиска пустая: true-да, false-нет
    private boolean isShowHistory = false;
    private int lastBGColor = 100; // цвет фона для запоминания
    private Tracker mTracker; // трекер для Google analitics, чтобы отслеживать активности пользователей

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("InflateParams")
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dictionary);
        Log.d("myLogs", "onCreate Dictionary");

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

        // формируем столбцы сопоставления
        String engl = db.getValueByVariable(DB.DICT_TRASL_DIRECT);
        isEnglSearch = (engl == null || engl.equals("0"));
        String search = db.getValueByVariable(DB.DICT_SEARCH_TYPE);
        isSearchRule1 = (search == null || search.equals("0"));
        String showHist = db.getValueByVariable(DB.DICT_SHOW_HISTORY);
        isShowHistory = !(showHist == null || showHist.equals("0"));
        lvWord = (ListView) findViewById(R.id.lvWords);
        setAdapter();
        lvWord.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                record(id, WordDescription.class);
            }
        });

        etInputWord = (EditText) findViewById(R.id.inputWord);
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
                // получаем новый курсор с данными
                Dictionary.this.getLoaderManager().getLoader(0).forceLoad();
            }
        });
        tvNoWords = (TextView) findViewById(R.id.tvNoWords);

        // добавляем контекстное меню к списку
        registerForContextMenu(lvWord);

        String searchWord = db.getValueByVariable(DB.SEARCH_WORD);
        etInputWord.setText(searchWord == null ? "" : searchWord);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_SHOW_ID, 0, R.string.show_record);
        menu.add(0, CM_EDIT_ID, 1, R.string.edit_record);
        menu.add(0, CM_ADD_ID, 2, R.string.add_record);
        menu.add(0, CM_DELETE_ID, 3, R.string.delete_record);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // получаем из пункта контекстного меню данные по пункту списка
        AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item
                .getMenuInfo();
        // извлекаем id записи
        long idItem = acmi.id;
        switch (item.getItemId()) {
            case CM_SHOW_ID: // смотрим запись
                record(idItem, WordDescription.class);
                return true;
            case CM_EDIT_ID: // редактируем запись
                record(idItem, WordEdit.class);
                return true;
            case CM_ADD_ID: // добавляем запись
                record(0, WordEdit.class);
                return true;
            case CM_DELETE_ID: // удаляем запись
                db.delRecEngWord(idItem);
                // получаем новый курсор с данными
                getLoaderManager().getLoader(0).forceLoad();
                Toast.makeText(getApplicationContext(), s(R.string.word_is_deleted), Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            // получаем данные из Settings
            if (requestCode == 0) {
                setEnglSearch(data.getBooleanExtra("translDirection", true));
                setSearchRule1(data.getBooleanExtra("searchRule", true));
                setShowHistory(data.getBooleanExtra("showHistory", false));
                setAdapter();
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            // do nothing
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.setGroupVisible(R.id.group_addrec, false);
        menu.setGroupVisible(R.id.group_dictionary, false);
        menu.setGroupVisible(R.id.group_clear_hist, false);
        menu.setGroupVisible(R.id.group_statistics, false);
        menu.setGroupVisible(R.id.group_idata, false);
        menu.setGroupVisible(R.id.group_resetStat, false);
        menu.setGroupVisible(R.id.group_exit, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        switch (item.getItemId()) {
            case android.R.id.home: // обрабатываем кнопку "назад" в ActionBar
                super.onBackPressed();
                return true;
            case R.id.action_settings: // вызов настроек
                Intent intent1 = new Intent(this, Settings.class);
                intent1.putExtra("idsetting", 1);
                // 0 означает класс Settings
                startActivityForResult(intent1, 0);
                return true;
            case R.id.ihelp: // вызов помощи
                Intent intent2 = new Intent(this, Help.class);
                intent2.putExtra("idhelp", 1);
                startActivity(intent2);
                return true;
            case R.id.donate: // вызов страницы с благодарностью
                startActivity(new Intent(this, Thanks.class));
                return true;
            case R.id.about: // вызов страницы О программе
                startActivity(new Intent(this, About.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        startActivity(intent);
        db.setHistory(id);
    }

    /**
     * создаем адаптер для списка
     */
    public void setAdapter() {
        // формируем столбцы сопоставления
        String[] from = new String[]{isEnglSearch ? DB.C_EW_ENGWORD
                : DB.C_EW_RUSTRANSLATE};
        int[] to = new int[]{R.id.tvDictWord};
        // а вот и сам адаптер
        scAdapter = new SimpleCursorAdapter(this, R.layout.dict_item_word,
                null, from, to, 0);
        lvWord.setAdapter(scAdapter);
        getLoaderManager().destroyLoader(0);
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * установление параметра isEnglSearch - искать английские слова или русские
     *
     * @param isEnglSearch
     */
    public void setEnglSearch(boolean isEnglSearch) {
        this.isEnglSearch = isEnglSearch;
    }

    /**
     * установление правила поиска isSearchRule1 - true-по начальным буквам
     * слова, false-по буквам с любой части слова
     *
     * @param isSearchRule1
     */
    public void setSearchRule1(boolean isSearchRule1) {
        this.isSearchRule1 = isSearchRule1;
    }

    /**
     * показывается история или нет
     */
    public boolean isShowHistory() {
        return this.isShowHistory;
    }

    /**
     * показывать историю, когда строка поиска пустая: true-да, false-нет
     *
     * @param isShowHistory
     */
    public void setShowHistory(boolean isShowHistory) {
        this.isShowHistory = isShowHistory;
    }

    private String s(int res) {
        return getResources().getString(res);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        MyCursorLoader myCursorLoader = new MyCursorLoader(this, db,
                isEnglSearch, isSearchRule1, isShowHistory);
        return myCursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        scAdapter.swapCursor(cursor);
        if (isShowHistory && etInputWord.getEditableText().toString().equals("")) {
            if (scAdapter.getCount() == 0)
                tvNoWords.setText(s(R.string.no_last_query));
            else
                tvNoWords.setText(s(R.string.last_query));
            tvNoWords.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        } else {
            tvNoWords.setText("");
            tvNoWords.setLayoutParams(new LayoutParams(0, 0));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onResume() {
        Log.d("myLogs", "onResume Dictionary");
        if (db != null && db.isOpen()) {
            InputMethodManager imm = (InputMethodManager) this
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etInputWord, 0);
            if (etInputWord.requestFocus())
                getWindow()
                        .setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            etInputWord.selectAll();
            getLoaderManager().getLoader(0).forceLoad();
        }
        String strColor = db.getValueByVariable(DB.BG_COLOR);
        int bGColor = strColor == null ? 1 : Integer.parseInt(strColor);
        if (lastBGColor != 100 && lastBGColor != bGColor) {
            lastBGColor = bGColor;
            recreate();
            Log.d("myLogs", "onResume Learning recreate");
        }
        if (mTracker != null) {
            Log.i("myLogs", "Setting screen name: " + this.getLocalClassName());
            mTracker.setScreenName("Activity " + this.getLocalClassName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

        super.onResume();
    }

    @Override
    public void onStop() {
        String strColor = db.getValueByVariable(DB.BG_COLOR);
        lastBGColor = strColor == null ? 1 : Integer.parseInt(strColor);
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etInputWord.getWindowToken(), 0);
        super.onStop();
        Log.d("myLogs", "onStop Dictionary");
    }

    @Override
    public void onDestroy() {
        Log.d("myLogs", "onDestroy Dictionary");
        db.updateRecExitState(DB.SEARCH_WORD, etInputWord.getText().toString());
        getLoaderManager().destroyLoader(0);
        super.onDestroy();
    }

    static class MyCursorLoader extends CursorLoader {

        DB db;
        EditText inputWord;
        boolean isEngl;
        boolean isStartWord;
        boolean isShowHistory;

        public MyCursorLoader(Context context, DB db, boolean isEngl,
                              boolean isStartWord, boolean isShowHistory) {
            super(context);
            this.db = db;
            inputWord = (EditText) ((AppCompatActivity) context).findViewById(R.id.inputWord);
            this.isEngl = isEngl;
            this.isStartWord = isStartWord;
            this.isShowHistory = isShowHistory;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = null;
            String wordPart = inputWord.getEditableText().toString();
            if (db != null && db.isOpen())
                if (wordPart.equals("") && isShowHistory) {
                    cursor = db.getDataFromHistory();
                } else {
                    cursor = db.getDataWithWord(isEngl, isStartWord, wordPart);
                }
            return cursor;
        }
    }
}
