package ru.handy.android.wm;
/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Application;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

import ru.handy.android.wm.learning.Learning;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app
 */
public class GlobApp extends Application {
    public static final String OPEN_ACT = "open_act"; // событие - создание Activity
    public static final String ALL_LEARNING = "all_learning"; // событие со всеми настройками с оплаченным и неоплаченным обучением
    public static final String PAID_LEARNING = "paid_learning"; // событие со всеми настройками и только с оплаченным обучением
    public static final String DICTIONARY_SETTINGS = "dictionary_settings"; // событие открытия словаря со всеми его настройками
    public static final String OTHER_SETTINGS = "other_settings"; // событие открытия приложения с прочими настройками
    public static final String READ_FILE = "read_file"; // событие по чтению и загрузке файла со словами
    public static final String WRITE_FILE = "write_file"; // событие по записи в файл слова
    public static final String PURCHASE_MOTIVES = "purchase_motives"; // событие по покупке приложения и мотивам этой покупки
    private DB db;
    private TextToSpeech tts;
    private FirebaseAnalytics mFBAnalytics;
    private Learning learning;

    /**
     * Gets the default {@link FirebaseAnalytics} for this {@link Application}.
     *
     * @return FirebaseAnalytics
     */
    synchronized public FirebaseAnalytics getFBAnalytics() {
        if (mFBAnalytics == null) {
            mFBAnalytics = FirebaseAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.FA VERBOSE
        }
        // при разработке нужно ставить false, при выкате в бой - true
        mFBAnalytics.setAnalyticsCollectionEnabled(false);
        return mFBAnalytics;
    }

    /**
     * Регистрирует событие созданию (OPEN_ACT) Activity
     *
     * @param actName имя Activity, которое открывается
     */
    synchronized public void openActEvent(String actName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, actName);
        getFBAnalytics().logEvent(OPEN_ACT, bundle);
    }

    /**
     * Регистрирует событие обучения со всеми настройками обучения для оплаченных (PAID_LEARNING) и не оплаченных приложений (ALL_LEARNING)
     *
     * @param paidApp               оплачено ли приложение ('Оплачено', т.е. DB.AMOUNT_DONATE > 0 или DB.OLD_FREE_DB == 1, 'Не оплачено')
     * @param categories            категория(и) слов для данного урока
     * @param learningType          тип обучения ('Выбор верного варианта', 'Написание слова', 'Комплексное обучение')
     * @param learningSpeak         озвучивать английское слово после отгадывания или нет: 'Озвучивать слово', 'Не озвучивать слово'
     * @param learningShowTranscr   показывать транскрипцию в обучалке или нет: 'Показывать транскрипцию', 'Не показывать транскрипцию'
     * @param learningShowDontKnow  показывать кнопку "не знаю" в обучалке: 'Показывать кнопку \'Не знаю\'', 'Не показывать кнопку \'Не знаю\''
     * @param learningLanguage      какие слова отгадыватся в обучалке: 'Отгадывание английских слов', 'Отгадывание русских слов'
     * @param learningAmountWords   сколько вариантов слов для отгадывания будет предложено: от 2 до 12
     * @param learningRepeatsAmount кол-во повторений последовательностей в комплексном обучении: от 1 до 10 (по умолчанию - 2)
     * @param answer                'Верный ответ' или 'Неверный ответ'
     */
    synchronized public void learningEvent(String paidApp, String categories, String learningType,
                                           String learningSpeak, String learningShowTranscr,
                                           String learningShowDontKnow, String learningLanguage,
                                           String learningAmountWords, String learningRepeatsAmount,
                                           String answer) {
        Bundle bundle = new Bundle();
        bundle.putString("paid_app", paidApp);
        bundle.putString("categories", categories);
        bundle.putString("learning_type", learningType);
        bundle.putString("learning_speak", learningSpeak);
        bundle.putString("learning_show_transcr", learningShowTranscr);
        bundle.putString("learning_show_dont_know", learningShowDontKnow);
        bundle.putString("learning_language", learningLanguage);
        bundle.putString("learning_amount_words", learningAmountWords);
        bundle.putString("learning_repeats_amount", learningRepeatsAmount);
        bundle.putString("answer", answer);
        getFBAnalytics().logEvent(ALL_LEARNING, bundle);
        if (paidApp.equals(s(R.string.paid))) {
            bundle = new Bundle();
            bundle.putString("paid_app", paidApp);
            bundle.putString("categories", categories);
            bundle.putString("learning_type", learningType);
            bundle.putString("learning_speak", learningSpeak);
            bundle.putString("learning_show_transcr", learningShowTranscr);
            bundle.putString("learning_show_dont_know", learningShowDontKnow);
            bundle.putString("learning_language", learningLanguage);
            bundle.putString("learning_amount_words", learningAmountWords);
            bundle.putString("learning_repeats_amount", learningRepeatsAmount);
            bundle.putString("answer", answer);
            getFBAnalytics().logEvent(PAID_LEARNING, bundle);
        }
    }

    /**
     * Регистрирует событие открытия словаря со всеми его настройками
     *
     * @param translationDirection направление перевода: 'Англо-рус', 'Русско-англ'
     * @param filterType           как использовать фильтр в словаре: 'по начальным буквам', 'по буквам в любой части слова'
     * @param typeOfStartDict      что показывать при пустой строке поиска: 'Все слова', 'Последние просмотренные слова'
     */
    synchronized public void dictEvent(String translationDirection, String filterType, String typeOfStartDict) {
        Bundle bundle = new Bundle();
        bundle.putString("translation_direction", translationDirection);
        bundle.putString("filter_type", filterType);
        bundle.putString("type_of_start_dict", typeOfStartDict);
        getFBAnalytics().logEvent(DICTIONARY_SETTINGS, bundle);
    }

    /**
     * Регистрирует событие открытия приложения с прочими настройками
     *
     * @param backgroundColor цвет фона: 'THEME_1'... 'THEME_15'
     * @param speakType       тип озвучки англ. слов: 'Амер. английский', 'Брит. английский'
     */
    synchronized public void otherEvent(String backgroundColor, String speakType) {
        Bundle bundle = new Bundle();
        bundle.putString("background_color", backgroundColor);
        bundle.putString("speak_type", speakType);
        getFBAnalytics().logEvent(OTHER_SETTINGS, bundle);
    }

    /**
     * Регистрирует событие по чтению (READ_FILE) или записи (WRITE_FILE) файла со словами
     *
     * @param readOrWrite регистрируется событие по чтению или записи файла
     * @param xlsOrTxt    чтение экселевского или текстового файла: 'xls', 'txt'
     * @param wordsAmount кол-во загружаемых слов
     */
    synchronized public void readWriteFileEvent(String readOrWrite, String xlsOrTxt, String wordsAmount) {
        Bundle bundle = new Bundle();
        bundle.putString("xls_or_txt", xlsOrTxt);
        bundle.putString("words_amount", wordsAmount);
        if (readOrWrite.equals(READ_FILE)) {
            getFBAnalytics().logEvent(READ_FILE, bundle);
        } else if (readOrWrite.equals(WRITE_FILE)) {
            getFBAnalytics().logEvent(WRITE_FILE, bundle);
        } else {
            Log.w("myLogs", "для регистрации события Firebase нужно выбрать READ_FILE или WRITE_FILE");
        }
    }

    /**
     * Регистрирует событие покупки с сопутствуюей инфой, откуда сделана покука и какие пробные периоды были открыты
     *
     * @param motiveOfPurchase      что послужило мотивом покупки: 'Из Thanks', 'Статистика', 'Данные из файла', 'Тип обучения', 'Язык обучения', 'Количество слов для выбора', 'Цвет фона'
     * @param dateTrialStats        дата начала пробного периода по статистике
     * @param dateTrialLearningType дата начала пробного периода по другим методам обучения
     * @param dateTrialLanguage     дата начала пробного периода по смене языка обучения
     * @param dateTrialWordAmount   дата начала пробного периода по кол-ву слов для выбора
     * @param dateTrialBgColor      дата начала пробного периода по цвету фона
     */
    synchronized public void purchaseEvent(String motiveOfPurchase, String dateTrialStats,
                                           String dateTrialLearningType, String dateTrialLanguage,
                                           String dateTrialWordAmount, String dateTrialBgColor) {
        Bundle bundle = new Bundle();
        bundle.putString("motive_of_purchase", motiveOfPurchase);
        bundle.putString("date_trial_stats", dateTrialStats);
        bundle.putString("date_trial_learning_type", dateTrialLearningType);
        bundle.putString("date_trial_language", dateTrialLanguage);
        bundle.putString("date_trial_word_amount", dateTrialWordAmount);
        bundle.putString("date_trial_bgcolor", dateTrialBgColor);
        getFBAnalytics().logEvent(PURCHASE_MOTIVES, bundle);
    }

    /**
     * Gets open {@link DB}
     *
     * @return DB
     */
    synchronized public DB getDb() {
        if (db == null || !db.isOpen()) {
            db = new DB(this.getApplicationContext());
            db.open();
            Log.d("myLogs", "create new DB");
        }
        return db;
    }

    /**
     * Close {@link DB}
     */
    synchronized public void closeDb() {
        if (db != null) db.close();
    }

    /**
     * Gets the default {@link TextToSpeech} for this {@link Application}.
     *
     * @return TextToSpeech
     */
    synchronized public TextToSpeech speak(final String text) {
        if (tts == null || tts.speak(text, TextToSpeech.QUEUE_ADD, null) == TextToSpeech.ERROR) {
            tts = new TextToSpeech(getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    String pronun = db.getValueByVariable(DB.PRONUNCIATION_USUK);
                    int pronunc = (pronun == null || pronun.equals("0") ? 0 : 1);
                    int result = tts.setLanguage(pronunc == 0 ? Locale.US : Locale.UK);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("myLogs", "This Language Locale.US is not supported");
                    } else
                        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
                    Log.d("myLogs", "TextToSpeech initialization was successed!");
                } else {
                    Log.e("myLogs", "TextToSpeech initialization was failed!");
                }
            });
        }
        return tts;
    }

    synchronized public void shutdownTTS() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    /**
     * Sets {@link Learning} for this {@link Application}.
     */
    synchronized public void setLearning(Learning learning) {
        this.learning = learning;
    }

    /**
     * Gets {@link Learning} for this {@link Application}.
     *
     * @return Learning
     */
    synchronized public Learning getLearning() {
        return learning;
    }

    private String s(int res) {
        return getResources().getString(res);
    }
}