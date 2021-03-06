package ru.handy.android.wm.dictionary;

import static ru.handy.android.wm.setting.Utils.strToList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yodo1.mas.banner.Yodo1MasBannerAdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.handy.android.wm.CustomKeyboard;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
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
    private RelativeLayout rlWordEdit;
    private LinearLayout llAdMobWordEdit;
    private Yodo1MasBannerAdView avBottomBannerWordEdit;
    private DB db;
    private long id = 0; // не 0 - редактирование, 0 - добавление записи
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordedit);

        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mFBAnalytics = app.getFBAnalytics(); // получение экземпляра FirebaseAnalytics
        if (mFBAnalytics != null) {
            String[] arrClName = this.getClass().toString().split("\\.");
            app.openActEvent(arrClName[arrClName.length - 1]);
        }
        db = app.getDb(); // открываем подключение к БД

        // устанавливаем отдельную клавиатуру для поля с транскрипцией
        keyboard = new CustomKeyboard(this, R.id.etTrascrip, R.id.keyboardView,
                R.xml.mykeyboard);

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
        rlWordEdit = findViewById(R.id.rlWordEdit);
        llAdMobWordEdit = findViewById(R.id.llAdMobWordEdit);
        avBottomBannerWordEdit = findViewById(R.id.avBottomBannerWordEdit);
        // загружаем баннерную рекламу yodo1
        avBottomBannerWordEdit.loadAd();
        ViewGroup.LayoutParams params = llAdMobWordEdit.getLayoutParams();
        if (amountDonate > 0) {
            params.height = 0;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName() + " без отображения");
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName());
        }
        llAdMobWordEdit.setLayoutParams(params);
        if (amountDonate <= 0) {
            //если приложение еще не оплачено, то ставим Listener, чтобы показывать рекламу, когда нет клавиатуры и не показывать, когда клавитура есть
            rlWordEdit.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                Rect rect = new Rect();
                v.getWindowVisibleDisplayFrame(rect);
                int screenHeight = v.getRootView().getHeight();
                int keypadHeight = screenHeight - rect.bottom;
                ViewGroup.LayoutParams params1 = llAdMobWordEdit.getLayoutParams();
                params1.height = (keypadHeight > screenHeight * 0.15) ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
                llAdMobWordEdit.setLayoutParams(params1);
            });
        }

        // устанавливаем цвет фона и шрифта для toolbar
        Utils.colorizeToolbar(this, toolbar);
        // устанавливаем цвет стрелки "назад" в toolbar
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        if (upArrow != null && bar != null) {
            upArrow.setColorFilter(Utils.getFontColorToolbar(), PorterDuff.Mode.SRC_ATOP);
            bar.setHomeAsUpIndicator(upArrow);
        }

        // получаем поля
        etEngWord = findViewById(R.id.etEngWord);
        etTrascrip = findViewById(R.id.etTrascrip);
        etTranslate = findViewById(R.id.etTranslate);
        bSave = findViewById(R.id.bSave);
        etTrascrip.setOnTouchListener((v, event) -> {
            InputMethodManager imm = (InputMethodManager) WordEdit.this.getSystemService(
                    WordEdit.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            return false;
        });
        Intent intent = getIntent();
        if (intent.getStringExtra("c_ew_id") != null)
            id = Long.parseLong(Objects.requireNonNull(intent.getStringExtra("c_ew_id")));

        // массив категорий и программно добавляем поле
        // MultiAutoCompleteTextView
        categories = db.getCategories();
        lastCategories = new ArrayList<>();
        lastCategories.addAll(categories);
        LinearLayout llMACTV = findViewById(R.id.llMACTV);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lParams.leftMargin = 20;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, categories);
        mactvCategory = new MyMultiAutoCompleteTextView(this);
        mactvCategory.setAdapter(adapter);
        mactvCategory
                .setTokenizer(new AppCompatMultiAutoCompleteTextView.CommaTokenizer());
        mactvCategory.addTextChangedListener(new MyWatcher());
        mactvCategory.setHint(R.string.categories);
        llMACTV.addView(mactvCategory, lParams);
        mactvCategory.setOnClickListener(v -> {
            mactvCategory.setText(mactvCategory.getText());
            mactvCategory.setSelection(mactvCategory.getText().length());
        });

        // устанавливаем начальные данные для полей
        if (id != 0) {
            etEngWord.setText(intent.getStringExtra("c_ew_engword"));
            String transcr = Objects.requireNonNull(intent.getStringExtra("c_ew_transcription")).trim();
            if (!transcr.equals("") && transcr.endsWith("]"))
                transcr = transcr.substring(0, transcr.length() - 1);
            if (!transcr.equals("") && transcr.startsWith("["))
                transcr = transcr.substring(1);
            etTrascrip.setText(transcr);
            etTranslate.setText(intent.getStringExtra("c_ew_rustranslate"));
            String mmactv = Objects.requireNonNull(intent.getStringExtra("c_ew_category")).trim();
            if (!mmactv.equals("") && !mmactv.endsWith(","))
                mmactv = mmactv.concat(", ");
            mactvCategory.setText(mmactv);
            changeAdapter();
        }

        // обработчик для кнопки сохранения
        bSave.setOnClickListener(v -> {
            onClickSave();
            finish();
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
        if (id != 0) { // редактируем слово
            // для редактирования сначала удаляем слово
            if (db.delRecEngWord(id)) {
                app.getLearning().updateLesson(db.getCategoryCurLesson(), false, false, 0);
            }
            // а потом его добавляем
            db.addRecEngWord(id, etEngWord.getText().toString(), transcr,
                    etTranslate.getText().toString(), mmactv);
            if (db.addWordInLessons(id, etEngWord.getText().toString(), transcr,
                    etTranslate.getText().toString(), mmactv)) {
                app.getLearning().updateLesson(db.getCategoryCurLesson(), false, false, 0);
            }
            Toast.makeText(getApplicationContext(), s(R.string.word_is_changed), Toast.LENGTH_LONG).show();
        } else { // добавляем слово
            id = db.addRecEngWord(null, etEngWord.getText().toString(), transcr,
                    etTranslate.getText().toString(), mmactv);
            if (db.addWordInLessons(id, etEngWord.getText().toString(), transcr,
                    etTranslate.getText().toString(), mmactv)) {
                app.getLearning().updateLesson(db.getCategoryCurLesson(), false, false, 0);
            }
            Toast.makeText(getApplicationContext(), s(R.string.word_is_added), Toast.LENGTH_LONG).show();
        }
        if (keyboard != null && keyboard.isCustomKeyboardVisible()) {
            keyboard.hideCustomKeyboard();
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mactvCategory.getWindowToken(), 0);
        }
        Intent intent = new Intent();
        List<String> cats = strToList(db.getCategoryCurLesson(), ",", true);
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
        if (item.getItemId() == android.R.id.home) {
            if (keyboard != null && keyboard.isCustomKeyboardVisible()) {
                keyboard.hideCustomKeyboard();
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void changeAdapter() {
        ArrayList<String> newCategories = new ArrayList<>(categories);
        String[] arr = mactvCategory.getText().toString().split(",");
        for (String category : arr) {
            category = category.trim();
            newCategories.remove(category);
        }
        if (lastCategories.size() != newCategories.size()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, newCategories);
            mactvCategory.setAdapter(adapter);
            lastCategories = new ArrayList<>();
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
