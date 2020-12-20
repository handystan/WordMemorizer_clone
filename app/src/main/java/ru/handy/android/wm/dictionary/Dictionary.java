package ru.handy.android.wm.dictionary;

import android.annotation.SuppressLint;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;

import ru.handy.android.wm.About;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.Help;
import ru.handy.android.wm.R;
import ru.handy.android.wm.Thanks;
import ru.handy.android.wm.learning.Word;
import ru.handy.android.wm.setting.Settings;
import ru.handy.android.wm.setting.Utils;

public class Dictionary extends AppCompatActivity implements LoaderCallbacks<Cursor>, View.OnClickListener {

    private GlobApp app;
    private static final int CM_SHOW_ID = 0;
    private static final int CM_EDIT_ID = 1;
    private static final int CM_ADD_ID = 2;
    private static final int CM_DELETE_ID = 3;
    private ListView lvWords;
    private EditText etInputWord;
    private TextView tvNoWords;
    private LinearLayout llLetters;
    private ArrayList<Button> bLetters = new ArrayList<>();
    private FloatingActionButton fab;
    private DB db;
    private SimpleCursorAdapter scAdapter;
    // перечен англ. букв, с помощью которых будет делаться перемотка списка
    String[] engLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    // перечен русских букв, с помощью которых будет делаться перемотка списка
    String[] rusLetters = {"А", "Б", "В", "Г", "Д", "Е", "Ж", "З", "И", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц", "Ч", "Ш", "Щ", "Э", "Ю", "Я"};
    private int[] engLettersIndexes = null; // индексы списка, с которых начинаются английские слова в lvWords
    private int[] rusLettersIndexes = null; // индексы списка, с которых начинаются английские слова в lvWords
    private boolean isEnglSearch = true; // какие слова будут в поиске: true-английские, false-русские
    private boolean isSearchRule1 = true; // правила поиска: true-по начальным буквам слова, false-по буквам с любой части слова
    private boolean isShowHistory = false; // показывать историю, когда строка поиска пустая: true-да, false-нет
    private int llLettersHeight = 0; // высота layout, в которой будут кнопки с буквами
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
        lvWords = findViewById(R.id.lvWords);
        llLetters = findViewById(R.id.llLetters);
        etInputWord = findViewById(R.id.inputWord);
        tvNoWords = findViewById(R.id.tvNoWords);
        bLetters.add((Button) findViewById(R.id.bLetter0));
        bLetters.add((Button) findViewById(R.id.bLetter1));
        bLetters.add((Button) findViewById(R.id.bLetter2));
        bLetters.add((Button) findViewById(R.id.bLetter3));
        bLetters.add((Button) findViewById(R.id.bLetter4));
        bLetters.add((Button) findViewById(R.id.bLetter5));
        bLetters.add((Button) findViewById(R.id.bLetter6));
        bLetters.add((Button) findViewById(R.id.bLetter7));
        bLetters.add((Button) findViewById(R.id.bLetter8));
        bLetters.add((Button) findViewById(R.id.bLetter9));
        bLetters.add((Button) findViewById(R.id.bLetter10));
        bLetters.add((Button) findViewById(R.id.bLetter11));
        bLetters.add((Button) findViewById(R.id.bLetter12));
        bLetters.add((Button) findViewById(R.id.bLetter13));
        bLetters.add((Button) findViewById(R.id.bLetter14));
        bLetters.add((Button) findViewById(R.id.bLetter15));
        bLetters.add((Button) findViewById(R.id.bLetter16));
        bLetters.add((Button) findViewById(R.id.bLetter17));
        bLetters.add((Button) findViewById(R.id.bLetter18));
        bLetters.add((Button) findViewById(R.id.bLetter19));
        bLetters.add((Button) findViewById(R.id.bLetter20));
        bLetters.add((Button) findViewById(R.id.bLetter21));
        bLetters.add((Button) findViewById(R.id.bLetter22));
        bLetters.add((Button) findViewById(R.id.bLetter23));
        bLetters.add((Button) findViewById(R.id.bLetter24));
        bLetters.add((Button) findViewById(R.id.bLetter25));
        bLetters.add((Button) findViewById(R.id.bLetter26));
        bLetters.add((Button) findViewById(R.id.bLetter27));

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

        for (int i = 0; i < bLetters.size(); i++) {
            bLetters.get(i).setOnClickListener(this);
        }
        setLetters();

        setAdapter();
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
                llLetters.setVisibility(etInputWord.getText().length() == 0 ? View.VISIBLE : View.INVISIBLE);
            }
        });

        // добавляем контекстное меню к списку
        registerForContextMenu(lvWords);

        String searchWord = db.getValueByVariable(DB.SEARCH_WORD);
        etInputWord.setText(searchWord == null ? "" : searchWord);

        lvWords.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                record(id, WordDescription.class);
            }
        });

        // определяем высоту layout, в которой будут кнопки с буквами и показываем из только, если экран вертикально расположен и слова показываются не из истории
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && !isShowHistory) {
            lvWords.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (v.getHeight() > (oldBottom - oldTop)) {
                        llLettersHeight = v.getHeight();
                    } else {
                        lvWords.removeOnLayoutChangeListener(this);
                        if (etInputWord.getText().length() == 0) {
                            llLetters.setVisibility(View.VISIBLE);
                            LayoutParams params = (LayoutParams) bLetters.get(0).getLayoutParams();
                            params.height = (int) (llLettersHeight / (isEnglSearch ? 26 : 28));
                            for (int i = 0; i < bLetters.size(); i++) {
                                bLetters.get(i).setLayoutParams(params);
                            }
                        }
                    }
                }
            });
        }
        // при скроллинге отмечаем букву, на которой сейчас находится listView
        lvWords.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Cursor c = (Cursor) scAdapter.getItem(firstVisibleItem);
                if (c != null) {
                    String letter = c.getString(c.getColumnIndex(isEnglSearch ? DB.C_EW_ENGWORD : DB.C_EW_RUSTRANSLATE)).substring(0, 1).toUpperCase();
                    int index = Arrays.binarySearch(isEnglSearch ? engLetters : rusLetters, letter);
                    index = index == -1 ? 0 : index;
                    for (int i = 0; i < bLetters.size(); i++) {
                        bLetters.get(i).setTextColor(Color.parseColor("#55000000"));
                    }
                    bLetters.get(index).setTextColor(Color.BLUE);
                }
            }
        });
    }

    /**
     * пишет на кнопка буквы в зависимости от того, русский или английский словарь
     */
    private void setLetters() {
        int firstLetterInUnicode = isEnglSearch ? 0x0041 : 0x0410;
        for (int i = 0; i < bLetters.size(); i++) {
            if (isEnglSearch) {
                if (i < 26) {
                    bLetters.get(i).setText(engLetters[i]);
                } else {
                    bLetters.get(i).setText("");
                    bLetters.get(i).setVisibility(View.INVISIBLE);
                }
            } else if (!isEnglSearch) {
                bLetters.get(i).setText(rusLetters[i]);
                bLetters.get(i).setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            // определяем индексы списка, с которых начинаются английские/русские слова в lvWords
            if (engLettersIndexes == null && isEnglSearch) {
                engLettersIndexes = getLetterIndexes();
            } else if (rusLettersIndexes == null && !isEnglSearch) {
                rusLettersIndexes = getLetterIndexes();
            }
            int ind = Integer.parseInt(v.getTag().toString()); // определяем порядковый номер буквы, которая была нажата
            lvWords.setSelection(isEnglSearch ? engLettersIndexes[ind] : rusLettersIndexes[ind]);
            for (int i = 0; i < bLetters.size(); i++) {
                bLetters.get(i).setTextColor(Color.parseColor("#55000000"));
            }
            bLetters.get(ind).setTextColor(Color.BLUE);
        }
    }

    private int[] getLetterIndexes() {
        String[] letters = isEnglSearch ? engLetters : rusLetters;
        int[] lettersIndexes = new int[letters.length];
        Cursor c = ((SimpleCursorAdapter) lvWords.getAdapter()).getCursor();
        int i = 0; //счетчик, проходящий по всему курсору
        int letterI = 0; // счетчик, проходящий по массиву букв
        String prevLetter = ""; // начальная буква слова в предшесвующем курсоре
        if (c.moveToFirst()) {
            do {
                String thisLetter = c.getString(c.getColumnIndex(isEnglSearch ? DB.C_EW_ENGWORD : DB.C_EW_RUSTRANSLATE)).substring(0, 1).toUpperCase();
                if (!thisLetter.equals(prevLetter)) {
                    for (int j = letterI; j < letters.length; j++) {
                        lettersIndexes[j] = i;
                        if (thisLetter.equals(letters[j])) {
                            letterI = letterI == letters.length - 1 ? j : j + 1;
                            prevLetter = thisLetter;
                            break;
                        }
                    }
                }
                i++;
            } while (c.moveToNext());
        }
        return lettersIndexes;
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
        if (item.getItemId() == android.R.id.home) { // обрабатываем кнопку "назад" в ActionBar
            super.onBackPressed();
        } else if (item.getItemId() == R.id.action_settings) { // вызов настроек
            Intent intent1 = new Intent(this, Settings.class);
            intent1.putExtra("idsetting", 1);
            // 0 означает класс Settings
            startActivityForResult(intent1, 0);
        } else if (item.getItemId() == R.id.ihelp) { // вызов помощи
            Intent intent2 = new Intent(this, Help.class);
            intent2.putExtra("idhelp", 1);
            startActivity(intent2);
        } else if (item.getItemId() == R.id.donate) { // вызов страницы с благодарностью
            startActivity(new Intent(this, Thanks.class));
        } else if (item.getItemId() == R.id.about) { // вызов страницы О программе
            startActivity(new Intent(this, About.class));
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
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
        lvWords.setAdapter(scAdapter);
        getLoaderManager().destroyLoader(0);
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * установление параметра isEnglSearch - искать английские слова или русские
     *
     * @param isEnglSearch true - английские слова, false - русские слова
     */
    public void setEnglSearch(boolean isEnglSearch) {
        this.isEnglSearch = isEnglSearch;
    }

    /**
     * установление правила поиска isSearchRule1
     *
     * @param isSearchRule1 true-по начальным буквам
     *      * слова, false-по буквам с любой части слова
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
     * @param isShowHistory true-да, false-нет
     */
    public void setShowHistory(boolean isShowHistory) {
        this.isShowHistory = isShowHistory;
    }

    private String s(int res) {
        return getResources().getString(res);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        return new MyCursorLoader(this, db, isEnglSearch, isSearchRule1, isShowHistory);
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
        // определяем высоту layout, в которой будут кнопки с буквами и показываем из только, если экран вертикально расположен и слова показываются не из истории
        // если еще не была вычислена высота LinearLayout llLettersHeight
        if (llLettersHeight == 0 && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && !isShowHistory) {
            lvWords.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (v.getHeight() > (oldBottom - oldTop)) {
                        llLettersHeight = v.getHeight();
                    } else {
                        lvWords.removeOnLayoutChangeListener(this);
                        if (etInputWord.getText().length() == 0) {
                            llLetters.setVisibility(View.VISIBLE);
                            LayoutParams params = (LayoutParams) bLetters.get(0).getLayoutParams();
                            params.height = (int) (llLettersHeight / (isEnglSearch ? 26 : 28));
                            for (int i = 0; i < bLetters.size(); i++) {
                                bLetters.get(i).setLayoutParams(params);
                            }
                        }
                    }
                }
            });
        } else if (llLettersHeight > 0 && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && !isShowHistory) {
            LayoutParams params = (LayoutParams) bLetters.get(0).getLayoutParams();
            params.height = (int) (llLettersHeight / (isEnglSearch ? 26 : 28));
            for (int i = 0; i < bLetters.size(); i++) {
                bLetters.get(i).setLayoutParams(params);
            }
            llLetters.setVisibility(etInputWord.getText().length() == 0 ? View.VISIBLE : View.INVISIBLE);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE || isShowHistory) {
            llLetters.setVisibility(View.INVISIBLE);
        }
        setLetters(); //устанавливаем русские или английские в зависимости от настройки
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
        @SuppressLint("StaticFieldLeak")
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
