package ru.handy.android.wm.dictionary;

import android.content.Context;
import android.content.Intent;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.CustomKeyboard;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.R;
import ru.handy.android.wm.learning.Word;
import ru.handy.android.wm.setting.Utils;

public class WordEdit extends AppCompatActivity {

    private GlobApp app;
    private EditText etEngWord;
    private EditText etTrascrip;
    private EditText etTranslate;
    private MyMultiAutoCompleteTextView mactvCategory;
    private Button bSave;
    private ArrayList<String> categories;
    private ArrayList<String> lastCategories;
    private CustomKeyboard keyboard;
    private DB db;
    private long id = 0; // не 0 - редактирование, 0 - добавление записи
    private Tracker mTracker; // трекер для Google analitics, чтобы отслеживать активности пользователей

    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordedit);

        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mTracker = app.getDefaultTracker(); // Obtain the shared Tracker instance.
        db = app.getDb(); // открываем подключение к БД

        // устанавливаем отдельную клавиатуру для поля с транскрипцией
        keyboard = new CustomKeyboard(this, R.id.etTrascrip, R.id.keyboardView,
                R.xml.mykeyboard);

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

        // получаем поля
        etEngWord = (EditText) findViewById(R.id.etEngWord);
        etTrascrip = (EditText) findViewById(R.id.etTrascrip);
        etTranslate = (EditText) findViewById(R.id.etTranslate);
        bSave = (Button) findViewById(R.id.bSave);
        etTrascrip.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) WordEdit.this.getSystemService(
                        WordEdit.this.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });
        Intent intent = getIntent();
        if (intent.getStringExtra("c_ew_id") != null)
            id = Long.parseLong(intent.getStringExtra("c_ew_id"));

        // массив категорий и программно добавляем поле
        // MultiAutoCompleteTextView
        categories = db.getCategories();
        lastCategories = new ArrayList<String>();
        lastCategories.addAll(categories);
        LinearLayout llMACTV = (LinearLayout) findViewById(R.id.llMACTV);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lParams.leftMargin = 20;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, categories);
        mactvCategory = new MyMultiAutoCompleteTextView(this);
        mactvCategory.setAdapter(adapter);
        mactvCategory
                .setTokenizer(new AppCompatMultiAutoCompleteTextView.CommaTokenizer());
        mactvCategory.addTextChangedListener(new MyWatcher());
        mactvCategory.setHint(R.string.categories);
        llMACTV.addView(mactvCategory, lParams);
        mactvCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mactvCategory.setText(mactvCategory.getText());
                mactvCategory.setSelection(mactvCategory.getText().length());
            }
        });

        // устанавливаем начальные данные для полей
        if (id != 0) {
            etEngWord.setText(intent.getStringExtra("c_ew_engword"));
            String transcr = intent.getStringExtra("c_ew_transcription").trim();
            if (!transcr.equals("") && transcr.endsWith("]"))
                transcr = transcr.substring(0, transcr.length() - 1);
            if (!transcr.equals("") && transcr.startsWith("["))
                transcr = transcr.substring(1, transcr.length());
            etTrascrip.setText(transcr);
            etTranslate.setText(intent.getStringExtra("c_ew_rustranslate"));
            String mmactv = intent.getStringExtra("c_ew_category").trim();
            if (!mmactv.equals("") && !mmactv.endsWith(","))
                mmactv = mmactv.concat(", ");
            mactvCategory.setText(mmactv);
            changeAdapter();
        }

        // обработчик для кнопки сохранения
        bSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onClickSave();
                finish();
            }
        });
    }

    // действия при нажатии кнопки сохранения
    private void onClickSave() {
        String mmactv = mactvCategory.getText().toString().trim();
        if (mmactv.endsWith(","))
            mmactv = mmactv.substring(0, mmactv.length() - 1);
        String transcr = etTrascrip.getText().toString().trim();
        if (!transcr.equals("") && !transcr.endsWith("]"))
            transcr = transcr.concat("]");
        if (!transcr.equals("") && !transcr.startsWith("["))
            transcr = "[".concat(transcr);
        if (id != 0) {
            db.updateRecEngWord(id, etEngWord.getText().toString(), transcr,
                    etTranslate.getText().toString(), mmactv);
            Toast.makeText(getApplicationContext(), s(R.string.word_is_changed), Toast.LENGTH_LONG).show();
        } else {
            id = db.addRecEngWord(etEngWord.getText().toString(), transcr,
                    etTranslate.getText().toString(), mmactv);
            Toast.makeText(getApplicationContext(), s(R.string.word_is_added), Toast.LENGTH_LONG).show();
        }
        if (keyboard != null && keyboard.isCustomKeyboardVisible()) {
            keyboard.hideCustomKeyboard();
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mactvCategory.getWindowToken(), 0);
        }
        Intent intent = new Intent();
        String strCats = db.getCategoryLesson(); // выбранные категории для текущего урока
        String[] arr = strCats.split(",");
        ArrayList<String> cats = new ArrayList<>();
        for (String cat : arr) {
            cats.add(cat.trim());
        }
        boolean inLesson = false; // переменная, показывающая входит слово в урок или нет
        for (String cat : cats) {
            if (mmactv.contains(cat)) {
                inLesson = true;
                break;
            }
        }
        if (inLesson) {
            intent.putExtra("editWord", new Word((int) id, etEngWord.getText().toString(), transcr,
                    etTranslate.getText().toString(), mmactv));
        }
        setResult(RESULT_OK, intent);
    }

    // обрабатываем кнопку "назад" в ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        switch (item.getItemId()) {
            case android.R.id.home:
                if (keyboard != null && keyboard.isCustomKeyboardVisible()) {
                    keyboard.hideCustomKeyboard();
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
        if (keyboard != null && keyboard.isCustomKeyboardVisible()) {
            keyboard.hideCustomKeyboard();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void changeAdapter() {
        ArrayList<String> newCategories = new ArrayList<String>();
        newCategories.addAll(categories);
        String[] arr = mactvCategory.getText().toString().split(",");
        for (String category : arr) {
            category = category.trim();
            if (newCategories.contains(category)) {
                newCategories.remove(newCategories.indexOf(category));
            }
        }
        if (lastCategories.size() != newCategories.size()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, newCategories);
            mactvCategory.setAdapter(adapter);
            lastCategories = new ArrayList<String>();
            lastCategories.addAll(newCategories);
        }
    }

    /**
     * This is used to watch for edits to the text view. Note that we call to
     * methods on the auto complete text view class so that we can access
     * private vars without going through thunks.
     */
    private class MyWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            changeAdapter();
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }
    }
}
