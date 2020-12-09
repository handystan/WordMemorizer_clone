package ru.handy.android.wm.learning;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Date;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ru.handy.android.wm.About;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.Help;
import ru.handy.android.wm.R;
import ru.handy.android.wm.Thanks;
import ru.handy.android.wm.dictionary.Dictionary;
import ru.handy.android.wm.downloads.EditData;
import ru.handy.android.wm.setting.LearningSetting;
import ru.handy.android.wm.setting.Pay;
import ru.handy.android.wm.setting.Settings;
import ru.handy.android.wm.setting.Utils;
import ru.handy.android.wm.statistics.Statistics;

public class Learning extends AppCompatActivity implements OnClickListener, OnTouchListener {

    private static int GET_CATEGORIES = 1;
    private GlobApp app;
    private LinearLayout llDownLearning;
    private LinearLayout llCenterLearning;
    private LinearLayout llThanks;
    private Button bCategory;
    private TextView tvAmountWords;
    private TextView tvCheckedWord;
    private TextView tvThanks;
    private ImageView ivSound;
    private ScrollView svChoice;
    private ScrollView svWriteWord;
    private EditText etAnswerWord;
    private TextView tvMessage;
    private TextView tvRightAnswer;
    private Button bDontKnow;
    private Button bKnow;
    private FloatingActionButton fab;
    private Menu menu;
    private ArrayList<Button> buttons = new ArrayList<Button>();
    private DB db;
    private Fixing fixing; // класс, в котором хранится информация по текущему уроку с данной категорией
    private ArrayList<Word> words; // список слов, которые проставляются на кнопках
    private Word curWord; // текущее отгадываемое слово
    private Word wrongWord = null; // неверное выбранное слово
    private CountDownLatch latch = null;
    private String checkedEngWord = ""; //проверяемое аглийское слово
    // тип обучения: 0-отгадывание из набора слов, 1-написание слова, 2-комлексное обучение
    private int learningType = 0;
    // озвучивать ли слова: true-озвучивать, false-не озвучивать
    private boolean isSpeak = true;
    // какие слова отгадываются в "закреплении": true-английские, false-русские
    private boolean isEngFixing = true;
    // показывать ли транскрипцию в "закреплении": true-да, false-нет
    private boolean isShowTrancr = true;
    private int amountWords = 6;
    // показывать ли кнопку "Не знаю": true-показывать, false-не показывать
    private boolean isShowDontKnow = true;
    // показывает в каком состоянии находится обучалка: true-выбран неверный ответ, ждут прикосновения, false-обычный режим (нужно для сохранения состояния при повороте экрана)
    private boolean isWaitTouch = false;
    // только для комплексного обучения (для случая, когда learningType=2)
    private int repeatsAmountCompl = 2; // кол-во повторений
    //    private int curRepeatNumberCompl = 0; // номер текущего повторение данного слова
    private int curLearningTypeCompl = 0; // текущий тип обучения в комплексном обучении: 0-отгадывание из из русс. перевода, 1-отгадывание из англ. перевода, 2-написание англ. слова
    // диалоговое окно с сообщением
    private DialogLearning dl = null;
    // сообщение либо о полном окончании урока, либо об окончании с ошибками
    private Word message = null;
    // показывает сумму, которую пользователь пожертвовал разработчику
    private int amountDonate = 0;
    private int lastBGColor = 100; // цвет фона для запоминания
    private Pay pay; // класс для обработки платежей
    private Tracker mTracker; // трекер для Google analitics, чтобы отслеживать активности пользователей

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        GoogleAnalytics.getInstance(this).setDryRun(true); // отказ от GoogleAnalytics при тестировании (при выпуске в бой нужно закоментить)
        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mTracker = app.getDefaultTracker(); // Obtain the shared Tracker instance.
        db = app.getDb(); // открываем подключение к БД
        app.setLearning(this); // в application указываем экземпляр learning, чтобы с ним можно было работать из любых мест приложения

        // в отдельном потоке инициализируем TTS
        new Thread(new Runnable() {
            @Override
            public void run() {
                app.speak("");
            }
        }).start();

        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.learning_md);
        Log.d("myLogs", "onCreate Learning");
        String fromOldDB = db.getValueByVariable(DB.OLD_FREE_DB);
        boolean isFromOldDB = !(fromOldDB == null || fromOldDB.equals("0"));
        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);

        // устанавливаем toolbar и actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // устанавливаем цвет фона и шрифта для toolbar
        Utils.colorizeToolbar(this, toolbar);
        // устанавливаем цвет иконки со статистикой и overflow
        Drawable stat = ContextCompat.getDrawable(this, R.drawable.statistics);
        stat.setColorFilter(Utils.getColorForIcon(), PorterDuff.Mode.SRC_ATOP);
        // устанавливаем плавающию кнопку с перечнем слов в категории
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        fab.setBackgroundTintList(ColorStateList.valueOf(Utils.getFabColor()));

        String lt = db.getValueByVariable(DB.LEARNING_TYPE);
        learningType = lt == null ? 0 : Integer.parseInt(lt);
        //проверяем не прошел ли бесплатный 30 дневный период по типу обучения. Если да, то возвращаем в базовый тип обучения
        String strStartDate = db.getValueByVariable(DB.DATE_LEARNING_METHOD);
        Date startDate = strStartDate == null || strStartDate.equals("") ? null : Date.valueOf(strStartDate);
        if (!isFromOldDB && amountDonate == 0 && startDate != null && learningType != 0) {
            long dif = (System.currentTimeMillis() - startDate.getTime()) / (1000 * 60 * 60 * 24);
            Log.d("myLogs", "dif = " + dif);
            if (dif > 30) { // и если закончился бесплатный месяц
                db.updateRecExitState(DB.LEARNING_TYPE, "0");
                learningType = 0;
            }
        }

        String speak = db.getValueByVariable(DB.LEARNING_SPEAK);
        isSpeak = (speak == null || speak.equals("1"));
        String trancr = db.getValueByVariable(DB.LEARNING_SHOW_TRANSCR);
        isShowTrancr = (trancr == null || trancr.equals("1"));
        amountWords = db.getValueByVariable(DB.LEARNING_AMOUNT_WORDS) == null ? 8 :
                Integer.parseInt(db.getValueByVariable(DB.LEARNING_AMOUNT_WORDS));
        //проверяем не прошел ли бесплатный 30 дневный период по кол-ву слов в обучении. Если да, то возвращаем в базовый
        strStartDate = db.getValueByVariable(DB.DATE_LANG_WORD_AMOUNT);
        startDate = strStartDate == null || strStartDate.equals("") ? null : Date.valueOf(strStartDate);
        if (!isFromOldDB && amountDonate == 0 && startDate != null && amountWords != 8) {
            long dif = (System.currentTimeMillis() - startDate.getTime()) / (1000 * 60 * 60 * 24);
            Log.d("myLogs", "dif = " + dif);
            if (dif > 30) { // и если закончился бесплатный месяц
                db.updateRecExitState(DB.LEARNING_AMOUNT_WORDS, "8");
                amountWords = 8;
            }
        }
        String showTrancr = db.getValueByVariable(DB.LEARNING_SHOW_DONTKNOW);
        isShowDontKnow = (showTrancr == null || showTrancr.equals("1"));

        svChoice = (ScrollView) findViewById(R.id.svChoice);
        svWriteWord = (ScrollView) findViewById(R.id.svWriteWord);
        svChoice.setVisibility(learningType == 0 ? View.VISIBLE : View.GONE);
        svWriteWord.setVisibility(learningType == 0 ? View.GONE : View.VISIBLE);

        llCenterLearning = (LinearLayout) findViewById(R.id.llCenterLearning);
        llCenterLearning.setOnTouchListener(this);
        tvCheckedWord = (TextView) findViewById(R.id.tvCheckedWord);
        buttons.add((Button) findViewById(R.id.bChoice1));
        buttons.add((Button) findViewById(R.id.bChoice2));
        buttons.add((Button) findViewById(R.id.bChoice3));
        buttons.add((Button) findViewById(R.id.bChoice4));
        buttons.add((Button) findViewById(R.id.bChoice5));
        buttons.add((Button) findViewById(R.id.bChoice6));
        buttons.add((Button) findViewById(R.id.bChoice7));
        buttons.add((Button) findViewById(R.id.bChoice8));
        buttons.add((Button) findViewById(R.id.bChoice9));
        buttons.add((Button) findViewById(R.id.bChoice10));
        buttons.add((Button) findViewById(R.id.bChoice11));
        buttons.add((Button) findViewById(R.id.bChoice12));
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setOnClickListener(this);
            buttons.get(i).setOnTouchListener(this);
            buttons.get(i).setTextColor(Color.parseColor("black"));
        }
        setAmountWords(amountWords);
        bCategory = (Button) findViewById(R.id.bCategory);
        llDownLearning = (LinearLayout) findViewById(R.id.llDownLearning);
        tvAmountWords = (TextView) findViewById(R.id.tvAmountWords);

        ivSound = (ImageView) findViewById(R.id.ivSound);
        ivSound.setOnClickListener(this);
        final Drawable defBackground = ivSound.getBackground(); // фон по умолчанию для кнопки с озвучки
        ivSound.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_MOVE) {
                    ivSound.setBackgroundResource(R.color.bright_blue);
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= 16) {
                        ivSound.setBackground(defBackground);
                    } else {
                        ivSound.setBackgroundDrawable(defBackground);
                    }
                }
                return false;
            }
        });
        etAnswerWord = (EditText) findViewById(R.id.etAnswerWord);
        etAnswerWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // при написании слова меняем название кнопки
                bDontKnow.setText(isWaitTouch ? R.string.further : (s.toString().equals("") ? R.string.dont_know : R.string.check));
            }
        });
        etAnswerWord.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (bDontKnow.isClickable()) {
                        if (fixing.getCategories().equals("")) {
                            return false;
                        }
                        nextWord(new Word(0, etAnswerWord.getText().toString(), "", etAnswerWord.getText().toString()));
                    } else {
                        afterOnTouch();
                    }
                    return true;
                }
                return false;
            }
        });
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvRightAnswer = (TextView) findViewById(R.id.tvRightAnswer);
        tvMessage.setVisibility(View.GONE);
        tvRightAnswer.setVisibility(View.GONE);
        bDontKnow = (Button) findViewById(R.id.bDontKnow);
        bDontKnow.setOnClickListener(this);
        bDontKnow.setOnTouchListener(this);
        bDontKnow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), s(R.string.hint_dont_know), Toast.LENGTH_LONG).show();
                return false;
            }
        });
        bDontKnow.setVisibility(isShowDontKnow ? View.VISIBLE : View.GONE);
        bKnow = (Button) findViewById(R.id.bKnow);
        bKnow.setOnClickListener(this);
        bKnow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), s(R.string.hint_know), Toast.LENGTH_LONG).show();
                return false;
            }
        });
        bKnow.setVisibility(isShowDontKnow && learningType == 2 ? View.VISIBLE : View.GONE);
        etAnswerWord.setTextColor(Color.parseColor("black"));
        etAnswerWord.setText("");

        String lang = db.getValueByVariable(DB.LEARNING_LANGUAGE);
        // если программа загружается первый раз, то открываем урок с ягодами
        if (lang == null) {
            db.updateRecExitState(DB.LEARNING_LANGUAGE, "0");
            isEngFixing = true;
            fixing = new Fixing(db, s(R.string.berries), 0);
            // если не в первый раз, то урок берется из БД
        } else {
            isEngFixing = lang.equals("0");
            //проверяем не прошел ли бесплатный 30 дневный период по языку обучения. Если да, то возвращаем в базовый
            strStartDate = db.getValueByVariable(DB.DATE_LANGUAGE);
            startDate = strStartDate == null || strStartDate.equals("") ? null : Date.valueOf(strStartDate);
            if (!isFromOldDB && amountDonate == 0 && startDate != null && !isEngFixing) {
                long dif = (System.currentTimeMillis() - startDate.getTime()) / (1000 * 60 * 60 * 24);
                Log.d("myLogs", "dif = " + dif);
                if (dif > 30) { // и если закончился бесплатный месяц
                    db.updateRecExitState(DB.LEARNING_LANGUAGE, "0");
                    isEngFixing = true;
                }
            }
            ivSound.setVisibility(isEngFixing ? View.VISIBLE : View.GONE);
            // загружаем урок из БД
            fixing = new Fixing(db, null, 1);
        }

        bCategory.setOnClickListener(this);
        llDownLearning.setOnClickListener(this);
        tvAmountWords.setOnClickListener(this);

        curWord = fixing.getCurWord();
        if (curWord == null) {
            bCategory.setTextColor(Color.parseColor("darkgray"));
            bCategory.setText(R.string.choose_category);
        } else {
            bCategory.setTextColor(Color.parseColor("black"));
            Spannable span = new SpannableString(s(R.string.category)
                    + "\n" + fixing.getCategories());
            int fontSize = (int) (bCategory.getTextSize() * 0.7);
            span.setSpan(new AbsoluteSizeSpan(fontSize, false), 0, 10,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            fontSize = (int) (bCategory.getTextSize() * 1.1);
            span.setSpan(new AbsoluteSizeSpan(fontSize, false), 11, span.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            bCategory.setText(span);
        }

        // если был поворот экрана
        if (savedInstanceState != null) {
            isWaitTouch = savedInstanceState.getBoolean("isWaitTouch");
            words = savedInstanceState.getParcelableArrayList("buttonWords");
            curWord = savedInstanceState.getParcelable("curWord");
            wrongWord = savedInstanceState.getParcelable("wrongWord");
            showWords(savedInstanceState.getBoolean("isShowMessage"));
        } else {
            showWords();
        }
        if (isWaitTouch) {
            nextWord(wrongWord);
        }
        setTextAmountWords();

        // показываем или скрываем строку с предложением поддержки разработчика
        llThanks = (LinearLayout) findViewById(R.id.llThanks);
        tvThanks = (TextView) findViewById(R.id.tvThanks);
        llThanks.setOnClickListener(this);
        tvThanks.setOnClickListener(this);
        pay = new Pay(this);
        if (amountDonate == 0) { //если в БД нет инфы о покупках, на всякий случай смотрим в Google play
            try {
                new Thread() {
                    public void run() {
                        pay = new Pay(Learning.this);
                        int amount = pay.amountOfPurchased();
                        for (int i = 0; i < 100; i++) {
                            if (amount != -1) {
                                Log.i("myLogs", "i = " + i + ", amountDonate = " + amount);
                                break;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            amount = pay.amountOfPurchased();
                        }
                        if (amount != -1) {
                            amountDonate = amount;
                            db.updateRecExitState(DB.AMOUNT_DONATE, amountDonate + "");
                            runOnUiThread(new Runnable() { //заморочиться с runOnUiThread пришлось из-за того Learning может обновляться из About

                                @Override
                                public void run() {
                                    ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) llThanks.getLayoutParams();
                                    params.height = amountDonate == 0 ? LinearLayout.LayoutParams.WRAP_CONTENT : 0;
                                    llThanks.setLayoutParams(params);
                                }
                            });
                        }
                    }
                }.start();
            } catch (Exception e) {
                Log.e("myLogs", "e = " + e.getMessage());
            }
        }
        Log.i("myLogs", "amountDonate = " + amountDonate);
        if (!isFromOldDB || amountDonate > 0) {
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) llThanks.getLayoutParams();
            params.height = 0;
            llThanks.setLayoutParams(params);
        }
    }

    /**
     * метод для сохранения состояния при повороте экрана
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isWaitTouch", isWaitTouch);
        outState.putParcelableArrayList("buttonWords", words);
        outState.putParcelable("curWord", curWord);
        outState.putParcelable("wrongWord", wrongWord);
        outState.putBoolean("isShowMessage", dl != null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.setGroupVisible(R.id.group_addrec, false);
        menu.setGroupVisible(R.id.group_clear_hist, false);
        menu.setGroupVisible(R.id.group_resetStat, false);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.idictionary:
                startActivity(new Intent(this, Dictionary.class));
                return true;
            case R.id.statistics:
                Utils.mainAlertForPay(DB.DATE_TRIAL_STATS, this, pay, db);
                return true;
            case R.id.action_settings:
                Intent intent1 = new Intent(this, Settings.class);
                intent1.putExtra("idsetting", 0);
                // 0 означает класс Settings
                startActivityForResult(intent1, 0);
                return true;
            case R.id.ihelp:
                Intent intent2 = new Intent(this, Help.class);
                intent2.putExtra("idhelp", 0);
                startActivity(intent2);
                return true;
            case R.id.idata:
                startActivity(new Intent(this, EditData.class));
                return true;
            case R.id.donate:
                // 2 означает класс Thanks
                startActivityForResult(new Intent(this, Thanks.class), 2);
                return true;
            case R.id.about:
                startActivity(new Intent(this, About.class));
                return true;
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * отображает загадываемое слово и варианты ответов
     */
    public void showWords() {
        showWords(true);
    }

    /**
     * отображает загадываемое слово и варианты ответов
     *
     * @param isRefreshWords true - обновляем список слов для кнопок, false - не обновляем
     */
    public void showWords(boolean isRefreshWords) {
        if (!db.isOpen()) db = app.getDb();
        if (fixing != null) {
            // текущее слово не определяем, если был поворот экрана и оно у нас сохранено
            if (!isWaitTouch) curWord = fixing.getCurWord();
            initComplexLearning(); // инициация данных для комплексного обучения
            if (curWord != null) {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    tvCheckedWord.setTextAppearance(android.R.style.TextAppearance_Large);
                } else {
                    tvCheckedWord.setTextAppearance(this, android.R.style.TextAppearance_Large);
                }
                tvCheckedWord.setText(isEngFixing ? curWord.getEngWord() + (isShowTrancr
                        ? " " + curWord.getTranscription() : "") : curWord.getRusTranslate());
                checkedEngWord = curWord.getEngWord();
                tvRightAnswer.setText(isEngFixing ? curWord.getRusTranslate() :
                        curWord.getEngWord() + (isShowTrancr ? " " + curWord.getTranscription() : ""));
                // список слов не обновляем, если только что был поворот экрана или вернулся экран с настройками
                if (isRefreshWords) {
                    words = db.getRandomWords(fixing.getCategories(), curWord, amountWords, false);
                }
            } else {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    tvCheckedWord.setTextAppearance(android.R.style.TextAppearance_Small);
                } else {
                    tvCheckedWord.setTextAppearance(this, android.R.style.TextAppearance_Small);
                }
                tvCheckedWord.setText(R.string.no_category);
                bKnow.setVisibility(View.GONE);
                words = db.getRandomWords("", curWord, amountWords, false);
                checkedEngWord = "";
            }
            for (int i = 0; i < Math.min(amountWords, words.size()); i++) {
                String textWord = isEngFixing ? words.get(i).getRusTranslate() : words.get(i).getEngWord()
                        + (isShowTrancr ? " " + words.get(i).getTranscription() : "");
                buttons.get(i).setText(textWord);
            }
            if (isRefreshWords) {
                etAnswerWord.setText("");
                etAnswerWord.setTextColor(Color.parseColor("black"));
                tvMessage.setVisibility(View.GONE);
                tvRightAnswer.setVisibility(View.GONE);
            }
            if ((learningType == 1 || (learningType == 2 && curLearningTypeCompl == 2)) && !isWaitTouch) {
                etAnswerWord.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etAnswerWord, InputMethodManager.SHOW_IMPLICIT);
            } else {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etAnswerWord.getWindowToken(), 0);
            }
        }
    }

    public void showWordsSynchr() {
        if (latch != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Learning.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                latch.await();
                                showWords();
                                if (message != null) {
                                    dl = null;
                                    onActivityResult(GET_CATEGORIES, AppCompatActivity.RESULT_OK,
                                            Learning.this.getIntent().putExtra(Categories.NEW_CATEGORIES, ""));
                                    startActivityForResult(new Intent(Learning.this, Categories.class), GET_CATEGORIES);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).start();
        } else {
            showWords();
        }
    }

    @Override
    public void onClick(View v) {
        // обработка нажатия строки с количеством слов
        if (v.getId() == tvAmountWords.getId() || v.getId() == llDownLearning.getId()) {
            startActivity(new Intent(this, WrongWords.class));
            return;
        }
        // обработка нажатия нижней строки с предложением поддержать разработчика
        if (v.getId() == tvThanks.getId() || v.getId() == llThanks.getId()) {
            // 2 означает класс Thanks
            startActivityForResult(new Intent(this, Thanks.class), 2);
            return;
        }
        // обработка нажатия кнопки со списком всех слов категории
        if (v.getId() == fab.getId()) {
            String categories = fixing.getCategories();
            if (categories.equals("")) {
                Toast.makeText(getApplicationContext(), s(R.string.no_category), Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, WordListCategory.class));
            }
            return;
        }
        // обработка нажатия кнопки с категориями
        if (v.getId() == R.id.bCategory) {
            startActivityForResult(new Intent(this, Categories.class), GET_CATEGORIES);
            return;
        }
        // обработка нажатия озвучки слова
        if (v.getId() == R.id.ivSound) {
            if (!checkedEngWord.equals("") && isEngFixing)
                app.speak(checkedEngWord);
            return;
        }
        // обработка нажатия кнопок, если они пустые
        if (((Button) v).getText().toString().equals(""))
            return;
        // обработка нажатия кнопок по выбору правильного слова или нажатия кнопки "Не знаю"
        Word selectedWord = null;
        switch (v.getId()) {
            case R.id.bChoice1:
                selectedWord = words.get(0);
                break;
            case R.id.bChoice2:
                selectedWord = words.get(1);
                break;
            case R.id.bChoice3:
                selectedWord = words.get(2);
                break;
            case R.id.bChoice4:
                selectedWord = words.get(3);
                break;
            case R.id.bChoice5:
                selectedWord = words.get(4);
                break;
            case R.id.bChoice6:
                selectedWord = words.get(5);
                break;
            case R.id.bChoice7:
                selectedWord = words.get(6);
                break;
            case R.id.bChoice8:
                selectedWord = words.get(7);
                break;
            case R.id.bChoice9:
                selectedWord = words.get(8);
                break;
            case R.id.bChoice10:
                selectedWord = words.get(9);
                break;
            case R.id.bChoice11:
                selectedWord = words.get(10);
                break;
            case R.id.bChoice12:
                selectedWord = words.get(11);
                break;
            case R.id.bDontKnow:
                selectedWord = new Word(0, etAnswerWord.getText().toString(), "", etAnswerWord.getText().toString());
                if (fixing.getCategories().equals(""))
                    return;
                break;
            case R.id.bKnow:
                selectedWord = curWord;
                db.setCurRepeatNumber(curWord, repeatsAmountCompl - 1);
                db.setCurLearningType(curWord, 2);
                nextWord(selectedWord, true);
                return;
        }
        nextWord(selectedWord);
    }

    /**
     * обработка события после выбора слова
     *
     * @param selectedWord выбранное слово
     */
    public void nextWord(Word selectedWord) {
        nextWord(selectedWord, false);
    }

    /**
     * обработка события после выбора слова
     *
     * @param selectedWord выбранное слово
     * @param isForceTrue  true - для комплекного обучение нажата кнопка "Уже знаю", т.е. засчитать слово как известное, false - для остальных случаев
     */
    private void nextWord(Word selectedWord, boolean isForceTrue) {
        Word rightWord = null;
        // текущее слово не определяем, если был поворот экрана и оно у нас сохранено
        if (!isWaitTouch) {
            ArrayList<Word> result = fixing.nextWord(selectedWord, isForceTrue);
            rightWord = result.get(0);
            message = result.get(2);
            // обновляем статистику по отгаданным / не отгаданным словам
            final Word finalRightWord = rightWord;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    db.updateStat(curWord, fixing.getCategories(), finalRightWord == null);
                }
            }).start();
        } else {
            rightWord = curWord;
        }
        // если слово не отгадано, правильное ставиться зеленым, не правильное - красным
        if (rightWord != null) {
            // неверное слово
            String textWW = isEngFixing ? selectedWord.getRusTranslate()
                    : selectedWord.getEngWord()
                    + (isShowTrancr ? " "
                    + selectedWord.getTranscription() : "");
            // правильное слово
            String textRW = isEngFixing ? rightWord.getRusTranslate()
                    : rightWord.getEngWord()
                    + (isShowTrancr ? " "
                    + rightWord.getTranscription() : "");
            for (int i = 0; i < buttons.size(); i++) {
                buttons.get(i).setClickable(false);
                if (buttons.get(i).getText().toString().equals(textWW))
                    buttons.get(i).setTextColor(Color.parseColor("red"));
                if (buttons.get(i).getText().toString().equals(textRW))
                    buttons.get(i).setTextColor(Color.parseColor("#00BB00"));
            }
            etAnswerWord.setTextColor(Color.parseColor("red"));
            bDontKnow.setClickable(false);
            bDontKnow.setText(R.string.further);
            tvMessage.setVisibility(View.VISIBLE);
            tvRightAnswer.setVisibility(View.VISIBLE);
            tvRightAnswer.setText(textRW);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etAnswerWord.getWindowToken(), 0);
            isWaitTouch = true;
            wrongWord = selectedWord;
            setTextAmountWords();
            if (isSpeak) app.speak(rightWord.getEngWord());
            bKnow.setVisibility(View.GONE);
        } else { // если слово отгадано
            if (message != null) { // выводится сообщение либо о полном окончании урока
                showDialog("", message.getEngWord());
                if (fixing.getAmountWrongWords() != 0) message = null;
            }
            if (!isForceTrue) {
                Toast.makeText(getApplicationContext(), s(R.string.right), Toast.LENGTH_SHORT).show();
            }
            isWaitTouch = false;
            wrongWord = null;
            setTextAmountWords();
            if (isSpeak) app.speak(curWord.getEngWord());
            showWordsSynchr();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // обрабатываем нажатие после неправильно отгаданного слова
        if ((v.getId() == R.id.bChoice1 || v.getId() == R.id.bChoice2
                || v.getId() == R.id.bChoice3 || v.getId() == R.id.bChoice4
                || v.getId() == R.id.bChoice5 || v.getId() == R.id.bChoice6
                || v.getId() == R.id.bChoice7 || v.getId() == R.id.bChoice8
                || v.getId() == R.id.bChoice9 || v.getId() == R.id.bChoice10
                || v.getId() == R.id.bChoice11 || v.getId() == R.id.bChoice12
                || v.getId() == R.id.bDontKnow || v.getId() == R.id.llCenterLearning)
                && !buttons.get(0).isClickable()
                && v.getId() != R.id.tvCheckedWord
                && event.getAction() == MotionEvent.ACTION_DOWN) {
            afterOnTouch();
            return true;
        }
        return false;
    }

    /**
     * действия, которые нужно сделать после ознаколения с верным ответом
     */
    private void afterOnTouch() {
        // выводится сообщение об окончании урока с ошибками
        if (message != null) {
            showDialog("", message.getEngWord());
            message = null;
        }
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setClickable(true);
            buttons.get(i).setTextColor(Color.parseColor("black"));
        }
        bDontKnow.setClickable(true);
        bDontKnow.setText(R.string.dont_know);
        if (learningType == 2) bKnow.setVisibility(View.VISIBLE);
        isWaitTouch = false;
        wrongWord = null;
        showWordsSynchr();
    }

    /**
     * инициация данных для комплексного обучения
     */
    private void initComplexLearning() {
        if (curWord != null && learningType == 2) {
            String ra = db.getValueByVariable(DB.LEARNING_REPEATS_AMOUNT);
            repeatsAmountCompl = ra == null ? 2 : Integer.parseInt(ra);
            curLearningTypeCompl = db.getCurLearningType(curWord);
            svChoice.setVisibility(curLearningTypeCompl != 2 ? View.VISIBLE : View.GONE);
            svWriteWord.setVisibility(curLearningTypeCompl != 2 ? View.GONE : View.VISIBLE);
            isEngFixing = curLearningTypeCompl == 0;
            ivSound.setVisibility(isEngFixing ? View.VISIBLE : View.GONE);
            bKnow.setVisibility(View.VISIBLE);
        }

    }

    private void showDialog(String title, String message) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("message", message);
        dl = new DialogLearning();
        dl.setArguments(bundle);
        latch = new CountDownLatch(1);
        dl.setLatch(latch);
        dl.setFragmentManager(getSupportFragmentManager());
        dl.setTag("DialogLearning");
        new Thread(dl).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == GET_CATEGORIES || requestCode == 3) { // если выбрана категория (3-статистика)
                boolean isOnlyMistakes = data.getBooleanExtra("isOnlyMistakes", false);
                String categories = data.getStringExtra(Categories.NEW_CATEGORIES);
                for (int i = 0; i < buttons.size(); i++) {
                    buttons.get(i).setTextColor(Color.parseColor("black"));
                }
                String lang = db.getValueByVariable(DB.LEARNING_LANGUAGE);
                isEngFixing = (lang == null || lang.equals("0"));
                ivSound.setVisibility(isEngFixing ? View.VISIBLE : View.GONE);
                String trancr = db.getValueByVariable(DB.LEARNING_SHOW_TRANSCR);
                isShowTrancr = (trancr == null || trancr.equals("1"));
                fixing = new Fixing(db, categories, 0, isOnlyMistakes);
                showWords();
                setTextAmountWords();
                if (fixing.getCurWord() == null) {
                    bCategory.setTextColor(Color.parseColor("darkgray"));
                    bCategory.setText(R.string.choose_category);
                } else {
                    bCategory.setTextColor(Color.parseColor("black"));
                    Spannable span = new SpannableString(s(R.string.category)
                            + "\n" + fixing.getCategories());
                    int fontSize = (int) (bCategory.getTextSize() * 0.7);
                    span.setSpan(new AbsoluteSizeSpan(fontSize, false), 0, 10,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    fontSize = (int) (bCategory.getTextSize() * 1.2);
                    span.setSpan(new AbsoluteSizeSpan(fontSize, false), 11, span.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    bCategory.setText(span);
                }
                InputMethodManager imm = (InputMethodManager) this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tvCheckedWord.getWindowToken(), 0);
            } else if (requestCode == 0) { // если вернулся результат с настройками
                setLearningType(data.getIntExtra("learningType", 0));
                setRepeatsAmount(data.getIntExtra("repeatsAmount", 2));
                setSpeak(data.getBooleanExtra("isSpeakLearning", true));
                setEngFixing(data.getBooleanExtra("engOrRusLearning", true));
                setShowTrancr(data.getBooleanExtra("isShowTranscr", true));
                setAmountWords(data.getIntExtra("amountWords", 8));
                setShowDontKnow(data.getBooleanExtra("isShowDontKnow", true));
                showWords(false);
            } else if (requestCode == 2) { // если вернулся результат с оплатой из Thanks
                amountDonate = data.getIntExtra("amountDonate", 0);
                if (amountDonate > 0) {
                    ViewGroup.LayoutParams params = llThanks.getLayoutParams();
                    params.height = 0;
                    llThanks.setLayoutParams(params);
                }
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            // do nothing
        }
    }

    /**
     * установление параметра
     * (0-отгадывание из набора слов, 1-написание слова, 2-комлексное обучение)
     *
     * @param learningType 0-отгадывание из набора слов, 1-написание слова, 2-комлексное обучение
     */
    public void setLearningType(int learningType) {
        this.learningType = learningType;
        svChoice.setVisibility(learningType == 0 ? View.VISIBLE : View.GONE);
        svWriteWord.setVisibility(learningType == 0 ? View.GONE : View.VISIBLE);
        bKnow.setVisibility(learningType == 2 ? View.VISIBLE : View.GONE);
    }

    /**
     * устанвоелине кол-ва повторений для комплексного обучения
     *
     * @param repeatsAmount кол-во повторений для комплексного обучения
     */
    public void setRepeatsAmount(int repeatsAmount) {
        repeatsAmountCompl = repeatsAmount;
    }

    /**
     * установление параметра isSpeak - тип обучения
     * (true-озвучивать слова, false-не озвучивать слова)
     *
     * @param isSpeakLearning true-озвучивать слова, false-не озвучивать слова
     */
    public void setSpeak(boolean isSpeakLearning) {
        isSpeak = isSpeakLearning;
    }

    /**
     * установление параметра isEngFix - какие слова отгадываются
     * (true-английские, false-русские)
     *
     * @param isEngFix true-английские, false-русские
     */
    public void setEngFixing(boolean isEngFix) {
        isEngFixing = isEngFix;
        ivSound.setVisibility(isEngFixing ? View.VISIBLE : View.GONE);
    }

    /**
     * установление параметра isShowTran - показывать транкрипцию в обучалке или нет
     *
     * @param isShowTran true-показывать транскрипцию, false-не показывать
     */
    public void setShowTrancr(boolean isShowTran) {
        isShowTrancr = isShowTran;
    }

    /**
     * установление параметра amountWords - кол-ва слов при отгадывании
     *
     * @param amountWords кол-во слов при отгадывании
     */
    public void setAmountWords(int amountWords) {
        this.amountWords = amountWords;
        for (int i = 0; i < amountWords; i++) {
            buttons.get(i).setVisibility(View.VISIBLE);
        }
        for (int i = amountWords; i < buttons.size(); i++) {
            buttons.get(i).setVisibility(View.GONE);
        }
    }

    /**
     * установление параметра: показывать кнопку "Не знаю" или нет
     *
     * @param isShowDontKnow true-показывать кнопку "Не знаю", false-не показывать
     */
    public void setShowDontKnow(boolean isShowDontKnow) {
        this.isShowDontKnow = isShowDontKnow;
        bDontKnow.setVisibility(isShowDontKnow ? View.VISIBLE : View.GONE);
        bKnow.setVisibility(isShowDontKnow && learningType == 2 ? View.VISIBLE : View.GONE);
    }

    /**
     * написание в нижней панели кол-ва правильно и не правлиьно отгаданных слов
     */
    private void setTextAmountWords() {
        String text1 = "  " + s(R.string.ofWords) + " "
                + fixing.getAmountWords();
        String text2 = text1 + "     " + s(R.string.right) + " "
                + fixing.getAmountRightWords();
        String allText = text2 + "     " + s(R.string.wrong) + " "
                + fixing.getAmountWrongWords();
        Spannable span = new SpannableString(allText);
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#00BB00")), text1.length(),
                text2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(Color.RED), text2.length(),
                allText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvAmountWords.setText(span);
    }

    /**
     * установление суммы оплаченных покупок
     */
    public void setAmountDonate(int amountDonate) {
        this.amountDonate = amountDonate;
    }

    /*
     * делаем возможность получать llThanks извне, чтобы из Pay иметь возможность его скрывать
     */
    public LinearLayout getLlThanks() {
        return llThanks;
    }

    @Override
    public void onResume() {
        String strColor = db.getValueByVariable(DB.BG_COLOR);
        int bGColor = strColor == null ? 0 : Integer.parseInt(strColor);
        if (lastBGColor != 100 && lastBGColor != bGColor) {
            lastBGColor = bGColor;
            recreate();
            Log.d("myLogs", "onResume Learning recreate");
        }
        if (mTracker != null) {
            Log.i("myLogs", "Setting screen name: " + this.getLocalClassName());
            mTracker.setScreenName("Activity " + this.getLocalClassName() + (learningType == 0
                    ? " - Choices" : (learningType == 1 ? " - Write" : " - Complex")));
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        String strColor = db.getValueByVariable(DB.BG_COLOR);
        lastBGColor = strColor == null ? 0 : Integer.parseInt(strColor);
        super.onStop();
        Log.d("myLogs", "onStop Learning");
    }

    @Override
    protected void onDestroy() {
        Log.d("myLogs", "onDestroy Learning");
        // закрываем подключение при выходе
        app.closeDb();
        if (pay != null) pay.close();
        super.onDestroy();
    }

    private String s(int res) {
        return getResources().getString(res);
    }
}
