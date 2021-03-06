package ru.handy.android.wm;

import static ru.handy.android.wm.setting.Utils.listToStr;
import static ru.handy.android.wm.setting.Utils.strToList;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import ru.handy.android.wm.learning.Category;
import ru.handy.android.wm.learning.Word;

public class DB {

    // главная таблица со словами
    public static final String T_ENGWORDS = "engwords";
    public static final String C_EW_ID = "_id";
    public static final String C_EW_ENGWORD = "ew_engword";
    public static final String C_EW_TRANSCRIPTION = "ew_transcription";
    public static final String C_EW_RUSTRANSLATE = "ew_rustranslate";
    public static final String C_EW_CATEGORY = "ew_category";
    public static final String C_EW_HISTORY = "ew_history"; // порядковый номер в истории последных запросов в Словаре
    // таблица с тек. уроком и всеми прошлыми уроками (если стоит соответсвующая галка в настройках)
    public static final String T_LESSON = "lesson";
    public static final String C_L_ID = "_id"; // id записи в таблице
    public static final String C_L_LESSON_ID = "l_lesson_id"; // id урока (у тек. урока самый большой id). В секундах от 01.01.1970, чтобы можно было перевести в дату
    public static final String C_L_ENGWORD_ID = "l_engword_id"; // id слова из таблицы T_ENGWORDS
    public static final String C_L_ENGWORD = "l_engword";
    public static final String C_L_TRANSCRIPTION = "l_transcription";
    public static final String C_L_RUSTRANSLATE = "l_rustranslate";
    public static final String C_L_CATEGORY = "l_category"; // категория(и) (для всех слов урока один перечень категорий)
    public static final String C_L_LEARNING_TYPE_COPML = "l_learning_type_compl"; // для компл. обучения: 0-отгадывание из из русс. перевода, 1-отгадывание из англ. перевода, 2-написание англ. слова
    public static final String C_L_REPEAT_NUMBER_COPML = "l_repeat_number_compl"; // для компл. обучения порядковый номер повторения (начинается с 0)
    public static final String C_L_RESULT = "l_result"; // отгадано ли слово: 1 - отгадано, 0 - нет, null - еще не отгадывалось
    public static final String C_L_ORDINAL = "l_ordinal";// порядковый номер слова в уроке
    // таблица для записи статистики по отгаданным и не отгаданным словам
    public static final String T_STATISTICS = "statistics";
    public static final String C_S_ID = "_id";
    public static final String C_S_ID_WORD = "l_id_word"; // id слова
    public static final String C_S_CATEGORY = "l_category"; // категория
    public static final String C_S_AMOUNT_RIGHT = "l_amount_right"; // кол-во правильных ответов
    public static final String C_S_AMOUNT_WRONG = "l_amount_wrong"; // кол-во неправильных ответов
    // таблица для запоминания состояния программы при выходе
    public static final String T_EXITSTATE = "exitstate";
    public static final String C_ES_ID = "_id";
    public static final String C_ES_VARIABLE = "es_variable"; // переменная
    public static final String C_ES_VALUE = "es_value"; // значение переменной
    /* ---------------------описание переменных------------------------- */
    // тип обучения: 0-выбор между словами, 1-написание слова, 2- комплексное обучение
    public static final String LEARNING_TYPE = "learningType";
    // кол-во повторений последовательностей в комплексном обучении: от 1 до 10 (по умолчанию - 2)
    public static final String LEARNING_REPEATS_AMOUNT = "learningRepeatsAmount";
    // озвучивать английское слово после отгадывания или нет: 0-не озвучивать, 1-озвучивать
    public static final String LEARNING_SPEAK = "learningSpeak";
    // какие слова отгадыватся в обучалке: 0-английские, 1-русские
    public static final String LEARNING_LANGUAGE = "learningLanguage";
    // показывать транскрипцию в обучалке или нет: 0-нет, 1-да
    public static final String LEARNING_SHOW_TRANSCR = "learningShowTranscr";
    // сколько вариантов слов для отгадывания будет предложено: от 2 до 12
    public static final String LEARNING_AMOUNT_WORDS = "learningAmountWords";
    // показывать кнопку "не знаю" в обучалке: 0-не показывать, 1-показывать
    public static final String LEARNING_SHOW_DONTKNOW = "learningShowDontKnow";
    // сохранять или нет историю по всем незаконченным урокам: 0-нет, 1-да
    public static final String LEARNING_LESSONS_HISTORY = "learningLessonsHistory";
    // что было введено в поле поиска в словаре
    public static final String SEARCH_WORD = "searchWord";
    // английские слова будут произноситься по американски или британски (0-американский, 1-британский)
    public static final String PRONUNCIATION_USUK = "pronunciationUsUk";
    // какие слова в словаре переводятся: 0-английские, 1-русские
    public static final String DICT_TRASL_DIRECT = "dictTranslDirect";
    // тип поиска слов: 0-по начальным буквам, 1-по буквам в любой части слова
    public static final String DICT_SEARCH_TYPE = "dictSearchType";
    // показывать в словаре последние выбранные слова: 0-нет, 1-да
    public static final String DICT_SHOW_HISTORY = "dictShowHistory";
    // сколько пользователь перечислил ознаграждения
    public static final String AMOUNT_DONATE = "amount_donate";
    // какой фон выбран? по умолчанию - 1. Предложено 15 цветов
    public static final String BG_COLOR = "bgColor";
    // дата начала бесплатного месячного периода для статистики (с 32 версии не актуальный функционал
    /*public static final String DATE_TRIAL_STATS = "dateTrialStats";
    // дата начала бесплатного месячного периода для смены цвета фона
    public static final String DATE_BG_COLOR = "dateBgColor";
    // дата начала бесплатного месячного периода для смены метода обучения
    public static final String DATE_LEARNING_METHOD = "dateLearningMethod";
    // дата начала бесплатного месячного периода для смены языка обучения
    public static final String DATE_LANGUAGE = "dateLanguage";
    // дата начала бесплатного месячного периода для кол-ва слов, показываемых при обучении
    public static final String DATE_LANG_WORD_AMOUNT = "dateLangWordAmount";*/
    // скрипт для создания основной таблицы-словаря со всеми словами
    public static final String T_ENGWORDS_CREATE = "create table " + T_ENGWORDS
            + " (" + C_EW_ID + " integer primary key autoincrement, "
            + C_EW_ENGWORD + " text, " + C_EW_TRANSCRIPTION + " text, "
            + C_EW_RUSTRANSLATE + " text, " + C_EW_CATEGORY + " text, "
            + C_EW_HISTORY + " integer);";
    // скрипт для создания таблицы, в которой храняться данные по текущему уроку
    public static final String T_LESSON_CREATE = "create table " + T_LESSON
            + " (" + C_L_ID + " integer primary key autoincrement, " + C_L_LESSON_ID + " integer, "
            + C_L_ENGWORD_ID + " integer, " + C_L_ENGWORD + " text, "
            + C_L_TRANSCRIPTION + " text, " + C_L_RUSTRANSLATE + " text, "
            + C_L_CATEGORY + " text, " + C_L_LEARNING_TYPE_COPML + " integer default 0, "
            + C_L_REPEAT_NUMBER_COPML + " integer default 0, " + C_L_RESULT + " integer, "
            + C_L_ORDINAL + " integer default 0);";
    // скрипт для создания таблицы со статистикой
    public static final String T_STATISTICS_CREATE = "create table " + T_STATISTICS
            + " (" + C_S_ID + " integer primary key autoincrement, "
            + C_S_ID_WORD + " integer, " + C_S_CATEGORY + " text, "
            + C_S_AMOUNT_RIGHT + " integer default 0, " + C_S_AMOUNT_WRONG + " integer default 0);";
    // скрипт для создания таблицы, в которой храниться состояние программы
    public static final String T_EXITSTATE_CREATE = "create table "
            + T_EXITSTATE + " (" + C_ES_ID
            + " integer primary key autoincrement, " + C_ES_VARIABLE
            + " text, " + C_ES_VALUE + " text);";

    private static final String DB_NAME = "ewdb";
    private static final int DB_VERSION = 35; // 13 - первая версия с платными настройками, 32 - версия, где убрал isFromOldDB
    private final Context mCtx;

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        if (mDBHelper != null)
            mDBHelper.close();
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        if (mDB != null)
            mDB.close();
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper != null)
            mDBHelper.close();
        if (mDB != null)
            mDB.close();
    }

    public boolean isOpen() {
        return mDB.isOpen();
    }

    public void setTransactionSuccessful() {
        mDB.setTransactionSuccessful();
    }

    public void beginTransaction() {
        mDB.beginTransaction();
    }

    public void endTransaction() {
        mDB.endTransaction();
    }

    public String getDbName() {
        return DB_NAME;
    }

    /**
     * Получение всех категорий
     *
     * @return ArrayList<String> с перечнем категорий
     */
    @SuppressLint("Range")
    public ArrayList<String> getCategories() {
        Cursor cursor = mDB.query(true, T_ENGWORDS, new String[]{C_EW_CATEGORY}, null,
                null, null, null, C_EW_CATEGORY, null);
        if (cursor != null) {
            ArrayList<String> list = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    String str = "";
                    if (!cursor.isNull(cursor.getColumnIndex(C_EW_CATEGORY))) {
                        str = cursor.getString(cursor.getColumnIndex(C_EW_CATEGORY)).trim();
                    }
                    String[] arr = str.split(",");
                    for (String category : arr) {
                        category = category.trim();
                        if (!list.contains(category))
                            list.add(category);
                    }
                } while (cursor.moveToNext());
                cursor.close();
                Collections.sort(list, Category::compare2Strings);
            }
            return list;
        }
        return null;
    }

    /**
     * Получение всех классов категорий (названий и кол-во слов в каждой категории)
     *
     * @return ArrayList<Category> массив с перечнем классов категорий (названия и кол-во слов в каждой категории)
     */
    @SuppressLint("Range")
    public ArrayList<Category> getClassCategories() {
        ArrayList<Category> classCats = new ArrayList<>();
        ArrayList<String> cats = getCategories();
        Cursor c = null;
        try {
            for (int i = 0; i < cats.size(); i++) {
                String sqlQuery = "SELECT COUNT(" + C_EW_ID + ") AS countwords FROM " + T_ENGWORDS
                        + " WHERE " + getWhereClauseForCategory(cats.get(i));
                c = mDB.rawQuery(sqlQuery, null);
                int countWords = 0;
                if (c.moveToFirst()) {
                    countWords = c.getInt(c.getColumnIndex("countwords"));
                }
                classCats.add(new Category(cats.get(i), countWords));
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) c.close();
        }
        return classCats;
    }

    /**
     * Получение всех слов из таблицы engwords
     *
     * @return Cursor с данными
     */
    public Cursor getAllWords() {
        return mDB.query(T_ENGWORDS, null, null, null, null, null, C_EW_ENGWORD);
    }

    /**
     * получение списка всех слов в словаре в HasMap, где ключом является англ. слово
     *
     * @return список слов HashMap<String, Word>, где ключ англ. слово, причем если англ. слово
     * встречается несколько раз, то берется более позднее
     */
    @SuppressLint("Range")
    public HashMap<String, Word> getAllWordsInHashMap() {
        HashMap<String, Word> hashWords = new HashMap<>();
        Cursor c = mDB.query(T_ENGWORDS, new String[]{C_EW_ID, C_EW_ENGWORD, C_EW_TRANSCRIPTION, C_EW_RUSTRANSLATE
                , C_EW_CATEGORY}, null, null, null, null, C_EW_ID);
        if (c.moveToFirst()) {
            do {
                Word word = new Word(c.getInt(0), c.getString(1)
                        , c.getString(2), c.getString(3), c.getString(4));
                hashWords.put(c.getString(1), word);
            } while (c.moveToNext());
        }
        c.close();
        return hashWords;
    }

    /**
     * Получение слова из таблицы engwords по его id
     *
     * @param id - id слова
     * @return Cursor с данными
     */
    public Cursor getWordById(long id) {
        return mDB.query(T_ENGWORDS, null, C_EW_ID + " = " + id, null, null,
                null, C_EW_ENGWORD);
    }

    /**
     * Получает данные из таблицы engwords по части слова
     *
     * @param engOrRus    какие слова нужно искать: true-английские, false-русские
     * @param isStartWord правило поиска: true-по началу слова, false-в любой части слова
     * @param partOfWord  части слова, по которой происходит поиск слов
     * @return Cursor с данными
     */
    public Cursor getDataWithWord(boolean engOrRus, boolean isStartWord,
                                  String partOfWord) {
        String whereClause = null;
        String column = engOrRus ? C_EW_ENGWORD : C_EW_RUSTRANSLATE;
        if (partOfWord != null && !partOfWord.equals("")) {
            partOfWord = partOfWord.replace("'", "''");
            whereClause = C_EW_ENGWORD + " LIKE " + (isStartWord ? "'" : "'%") + partOfWord + "%' OR "
                    + C_EW_RUSTRANSLATE + " LIKE " + (isStartWord ? "'" : "'%") + partOfWord + "%'";
        }
        return mDB.query(T_ENGWORDS, null, whereClause, null, null, null,
                "LOWER(" + column + ")");
    }

    /**
     * Получает данные из таблицы engwords, основываясь на столбце C_EW_HISTORY,
     * то есть показываются только те слова, которые уже просматривались
     *
     * @return Cursor с данными
     */
    public Cursor getDataFromHistory() {
        return mDB.query(T_ENGWORDS, null, C_EW_HISTORY + " IS NOT NULL", null,
                null, null, C_EW_HISTORY + " DESC");
    }

    /**
     * Получает данные из таблицы T_ENGWORDS или T_LESSON по категории
     *
     * @param table          - таблица, по которой получаем курсор
     * @param categories     - категория(и), к которой может относится слово
     * @param isOnlyMistakes - показывать только слова с ошибками (true) или все (false)
     * @return Cursor с данными
     */
    public Cursor getDataByCategory(String categories, String table, boolean isOnlyMistakes) {
        String colId = table.equals(T_ENGWORDS) ? C_EW_ID : C_L_ENGWORD_ID;
        String colEngWord = table.equals(T_ENGWORDS) ? C_EW_ENGWORD : C_L_ENGWORD;
        String colCat = table.equals(T_ENGWORDS) ? C_EW_CATEGORY : C_L_CATEGORY;
        String sqlQuery;
        if (isOnlyMistakes) {
            sqlQuery = "SELECT t1.* FROM (SELECT * FROM " + table + " WHERE " +
                    getWhereClauseForCategory(categories, colCat) + ") t1, " + T_STATISTICS + " t2 WHERE t1." +
                    colId + "=t2." + C_S_ID_WORD + " AND t2." + C_S_AMOUNT_WRONG + ">0 ORDER BY t1." + colEngWord;
        } else {
            sqlQuery = "SELECT * FROM " + table + " WHERE " + getWhereClauseForCategory(categories, colCat)
                    + " ORDER BY " + colEngWord;
        }
        return mDB.rawQuery(sqlQuery, null);
    }

    /**
     * Получает данные из таблицы T_ENGWORDS по категории
     *
     * @param categories     - категория(и), к которой может относится слово
     * @param isOnlyMistakes - показывать только слова с ошибками (true) или все (false)
     * @return Cursor с данными
     */
    public Cursor getDataByCategory(String categories, boolean isOnlyMistakes) {
        return getDataByCategory(categories, T_ENGWORDS, isOnlyMistakes);
    }

    /**
     * Получает данные из таблицы T_ENGWORDS или T_LESSON по категории
     *
     * @param table      - таблица, по которой получаем курсор
     * @param categories - категория(и), к которой может относится слово
     * @return Cursor с данными
     */
    public Cursor getDataByCategory(String categories, String table) {
        return getDataByCategory(categories, table, false);
    }

    /**
     * Получает данные из таблицы T_ENGWORDS по категории
     *
     * @param categories - категория(и), к которой может относится слово
     * @return Cursor с данными
     */
    public Cursor getDataByCategory(String categories) {
        return getDataByCategory(categories, T_ENGWORDS, false);
    }

    /**
     * Получает слова из таблицы engwords по категории
     *
     * @param categories - категория(и), к которой может относится слово
     * @return ArrayList<Word> - список слов с данной категорией
     */
    public ArrayList<Word> getWordsByCategory(String categories) {
        return getWordList("SELECT * FROM " + T_ENGWORDS + " WHERE " + getWhereClauseForCategory(categories) + " ORDER BY LOWER(" + C_EW_ENGWORD + ")");
    }

    private String getWhereClauseForCategory(String categories) {
        return getWhereClauseForCategory(categories, C_EW_CATEGORY);
    }

    private String getWhereClauseForCategory(String categories, String column) {
        StringBuilder whereClause = new StringBuilder();
        if (categories != null) {
            String[] arr = categories.split(",");
            for (String cat : arr) {
                String c = cat.trim().replace("'", "''");
                if (c.equals("")) {
                    whereClause.append(column).append("='' OR ");
                } else {
                    whereClause.append(column).append("='").append(c).append("' OR ")
                            .append(column).append(" LIKE '").append(c).append(",%' OR ")
                            .append(column).append(" LIKE '").append(c).append(" ,%' OR ")
                            .append(column).append(" LIKE '").append(c).append("  ,%' OR ")
                            .append(column).append(" LIKE '%,").append(c).append("' OR ")
                            .append(column).append(" LIKE '%, ").append(c).append("' OR ")
                            .append(column).append(" LIKE '%,  ").append(c).append("' OR ")
                            .append(column).append(" LIKE '%,").append(c).append(",%' OR ")
                            .append(column).append(" LIKE '%, ").append(c).append(",%' OR ")
                            .append(column).append(" LIKE '%,  ").append(c).append(",%' OR ")
                            .append(column).append(" LIKE '%,").append(c).append(" ,%' OR ")
                            .append(column).append(" LIKE '%,").append(c).append("  ,%' OR ")
                            .append(column).append(" LIKE '%, ").append(c).append(" ,%' OR ")
                            .append(column).append(" LIKE '%, ").append(c).append("  ,%' OR ")
                            .append(column).append(" LIKE '%,  ").append(c).append(" ,%' OR ")
                            .append(column).append(" LIKE '%,  ").append(c).append("  ,%' OR ");
                }
            }
            whereClause = new StringBuilder(whereClause
                    .substring(0, whereClause.length() - 4));
        }
        return whereClause.toString();
    }

    /**
     * Добавляем новое слово в главной таблице engwords английских слов
     *
     * @param id            id слова (если null, значит слово добавляется с новым id)
     * @param engWord       английское слово
     * @param transcription транскрипция
     * @param rusTranslate  русский перевод
     * @param category      категория
     * @return возвращает id нового слова в таблице T_ENGWORDS
     */
    public long addRecEngWord(Long id, String engWord, String transcription, String rusTranslate, String category) {
        ContentValues cv = new ContentValues();
        if (id != null) cv.put(C_EW_ID, id);
        cv.put(C_EW_ENGWORD, engWord);
        cv.put(C_EW_TRANSCRIPTION, transcription);
        cv.put(C_EW_RUSTRANSLATE, rusTranslate);
        cv.put(C_EW_CATEGORY, category);
        return mDB.insert(T_ENGWORDS, null, cv);
    }

    /**
     * Обновляем запись в главной таблице engwords английских слов
     *
     * @param id            - id записи
     * @param engWord       - англиское слово
     * @param transcription - транскрипция англйского слова
     * @param rusTranslate  - русский перевод
     * @param category      - категория слова
     * @return int - кол-во обновленных строк
     */
    public int updateRecEngWord(long id, String engWord, String transcription,
                                String rusTranslate, String category) {
        ContentValues cv = new ContentValues();
        cv.put(C_EW_ENGWORD, engWord);
        cv.put(C_EW_TRANSCRIPTION, transcription);
        cv.put(C_EW_RUSTRANSLATE, rusTranslate);
        cv.put(C_EW_CATEGORY, category);
        return mDB.update(T_ENGWORDS, cv, C_EW_ID + " = " + id, null);
    }

    /**
     * удалить запись из T_ENGWORDS и из T_LESSON
     *
     * @param id слова
     * @return true - если слово добавляется в текущий урок, false - если в текущий урок не добавляется
     */
    public boolean delRecEngWord(long id) {
        mDB.delete(T_ENGWORDS, C_EW_ID + " = " + id, null); // удаляем из таблицы T_ENGWORDS
        // узнаем есть ли удаляемое слово в текущем уроке
        String sqlQuery = "SELECT * FROM " + T_LESSON + " AS l1 WHERE l1." + C_L_LESSON_ID
                + "=(SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON + ") AND " + C_L_ENGWORD_ID + "=" + id;
        Cursor c = mDB.rawQuery(sqlQuery, null);
        boolean inCurLesson = c.moveToFirst();
        c.close();
        mDB.delete(T_LESSON, C_L_ENGWORD_ID + " = " + id, null);  // удаляем из таблицы T_ENGWORDS
        mDB.delete(T_STATISTICS, C_S_ID_WORD + " = " + id, null);  // удаляем из таблицы T_STATISTICS
        return inCurLesson;
    }

    /**
     * удалить все записи из какой-либо таблицы
     */
    public int delAll(String table) {
        return mDB.delete(table, null, null);
    }

    /**
     * Формирование урока путем рандомного формирования слов данной категории(й)
     *
     * @param categories     категории, по которым формируется урок
     * @param isOnlyMistakes брать только слова с ошибками (true) или все (false)
     * @return ArrayList<Word> массив слов
     */
    @SuppressLint("Range")
    public ArrayList<Word> createLessonForCat(String categories, boolean isOnlyMistakes) {
        ArrayList<Word> allWords = new ArrayList<>();
        Cursor c;
        if (categories != null) {
            c = getDataByCategory(categories, isOnlyMistakes);
        } else {
            c = getAllWords();
        }
        if (c.moveToFirst()) {
            do {
                Word word = new Word(c.getInt(c.getColumnIndex(C_EW_ID)),
                        c.getString(c.getColumnIndex(C_EW_ENGWORD)),
                        c.getString(c.getColumnIndex(C_EW_TRANSCRIPTION)),
                        c.getString(c.getColumnIndex(C_EW_RUSTRANSLATE)));
                allWords.add(word);
            } while (c.moveToNext());
            c.close();
        }
        ArrayList<Word> resWords = new ArrayList<>();
        Random randGen = new Random();
        while (allWords.size() > 0) {
            int rand = randGen.nextInt(allWords.size());
            resWords.add(allWords.get(rand));
            allWords.remove(rand);
        }
        return resWords;
    }

    /**
     * Получение случайных наборов слов в данном количестве из базы из урока
     *
     * @param categories    категории, по которым нужно получить случайные слова. Если =
     *                      null, то берет по всем категориям
     * @param rightWord     правильное слово, которое должен отгадать пользователь (может
     *                      быть null)
     * @param countOfChoice количество получаемых слов
     * @return ArrayList<Word> массив слов
     */
    @SuppressLint("Range")
    public ArrayList<Word> getRandomWords(String categories, Word rightWord, int countOfChoice) {
        ArrayList<Word> resWords = new ArrayList<>();
        if (rightWord != null) {
            ArrayList<Word> allWords = getAllWordsInCurLesson();
            if (allWords.size() < countOfChoice) {
                allWords = getWordList("SELECT * FROM " + T_ENGWORDS);
            }
            for (int i = 0; i < allWords.size(); i++) {
                if (rightWord.equals(allWords.get(i)) ||
                        (rightWord.getEngWord().equals(allWords.get(i).getEngWord()) &&
                                rightWord.getRusTranslate().equals(allWords.get(i).getRusTranslate()))) {
                    allWords.remove(i);
                    break;
                }
            }
            Random randGen = new Random();
            while (resWords.size() < countOfChoice - 1) {
                if (allWords.size() > 0) {
                    int rand = randGen.nextInt(allWords.size());
                    resWords.add(allWords.get(rand));
                    allWords.remove(rand);
                } else {
                    resWords.add(new Word(0, "", "", ""));
                }
            }
            resWords.add(randGen.nextInt(countOfChoice), rightWord);
        } else {
            while (resWords.size() < countOfChoice) {
                resWords.add(new Word(0, "", "", ""));
            }
        }
        return resWords;
    }

    /**
     * Удаление всех слов данных категорий из таблицы T_ENGWORDS или T_LESSON
     *
     * @param table      - таблица из которой удаляется категория
     * @param categories - категория, к которой может относится слово
     * @return массив из трех чисел (кол-во удаленных категорий, кол-во удаленных слов этих категорий, кол-во проапдейченных слов с категориями)
     */
    @SuppressLint("Range")
    public int[] deleteCategories(String table, String categories) {
        String colId = table.equals(T_ENGWORDS) ? C_EW_ID : C_L_ID;
        String colCat = table.equals(T_ENGWORDS) ? C_EW_CATEGORY : C_L_CATEGORY;
        //список со всеми категориями
        List<String> cats = strToList(categories, ",", true);
        mDB.beginTransaction();
        Cursor c = getDataByCategory(categories, table); // курсор со списком всех слов данных категорий
        int deletedWords = 0; // счетчик для удаленных слов
        int updatedWords = 0; //счетчик для проапдейченных слов
        try {
            String delArgs = colId + " IN (";
            if (c.moveToFirst()) {
                do {
                    int wordId = c.getInt(c.getColumnIndex(colId));
                    String catInWord = c.getString(c.getColumnIndex(colCat));
                    //список со всеми категориями в данном слове
                    List<String> catsInWord = strToList(catInWord, ",", true);
                    catsInWord.removeAll(cats);
                    //если в слове только удаляемые категории, то удаляем его
                    if (catsInWord.size() == 0) {
                        delArgs = delArgs + wordId + ", ";
                        deletedWords++;
                    } else { //если в слове есть не удаляемые категории, то апдейтим его категорию
                        String newCat = listToStr(catsInWord, ", ");
                        ContentValues cv = new ContentValues();
                        cv.put(colCat, newCat);
                        int upd = mDB.update(table, cv, colId + "=" + wordId, null);
                        updatedWords += upd;
                    }
                } while (c.moveToNext());
                c.close();
                if (deletedWords > 0) {
                    delArgs = delArgs.substring(0, delArgs.length() - 2) + ")";
                    mDB.delete(table, delArgs, null);
                }
            }
            mDB.setTransactionSuccessful();
            return new int[]{cats.size(), deletedWords, updatedWords};
        } finally {
            mDB.endTransaction();
        }
    }

    /**
     * устанавливает порядковый номер столбца C_EW_HISTORY. Если id = null, то
     * значение этого столбца во всех словах становится = null
     *
     * @param id - id слова в таблице C_EW_HISTORY
     */
    @SuppressLint("Range")
    public void setHistory(Long id) {
        ContentValues cv = new ContentValues();
        if (id != null) {
            String sqlQuery = "SELECT MAX(" + C_EW_HISTORY
                    + ") AS maxhist FROM " + T_ENGWORDS;
            Cursor c = mDB.rawQuery(sqlQuery, null);
            int prevHist = 0;
            if (c.moveToFirst()) {
                prevHist = c.getInt(c.getColumnIndex("maxhist"));
            }
            c.close();
            cv.put(C_EW_HISTORY, ++prevHist);
            mDB.update(T_ENGWORDS, cv, C_EW_ID + " = " + id, null);
        } else {
            cv.putNull(C_EW_HISTORY);
            mDB.update(T_ENGWORDS, cv, null, null);
        }
    }

    /**
     * в столбце категорий таблицы со словами одна категория заменяется на другую (просто ищутся совпадения)
     *
     * @param table   в какой таблице изменяется название категорий (T_ENGWORDS или T_LESSON)
     * @param oldName старое название катгории
     * @param newName новое название катгории
     */
    public void categoryRename(String table, String oldName, String newName) {
        String column = table.equals(T_ENGWORDS) ? C_EW_CATEGORY : C_L_CATEGORY;
        // если меняется категория на пустую, то нужно удалить возможные запятые перед и после нее
        // (если в слове было несколько категорий)
        if (!oldName.equals("") && newName.equals("")) {
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column + ", ', " + oldName + ",', ',')");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column + ", '," + oldName + ",', ',')");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column + ", ', " + oldName + " ,', ',')");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column + ", '," + oldName + " ,', ',')");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column
                    + ", ', " + oldName + "', '') WHERE " + column + " LIKE '%, " + oldName + "'");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column
                    + ", '," + oldName + "', '') WHERE " + column + " LIKE '%," + oldName + "'");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column
                    + ", '" + oldName + ",', '') WHERE " + column + " LIKE '" + oldName + ",%'");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column
                    + ", '" + oldName + " ,', '') WHERE " + column + " LIKE '" + oldName + " ,%'");
            ContentValues cv = new ContentValues();
            cv.put(column, "");
            mDB.update(table, cv, column + "='" + oldName + "'", null);
        } else if (oldName.equals("") && !newName.equals("")) { // если пустая категория меняется на нормальную
            ContentValues cv = new ContentValues();
            cv.put(column, newName);
            mDB.update(table, cv, column + " = ''", null);
        } else if (!oldName.equals(newName)) { // делаем апдейт только, если старое и нове имя разные
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column + ", ', " + oldName + ",', ', " + newName + ",')");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column + ", '," + oldName + ",', ', " + newName + ",')");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column + ", ', " + oldName + " ,', ', " + newName + ",')");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column + ", '," + oldName + " ,', ', " + newName + ",')");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column
                    + ", ', " + oldName + "', ', " + newName + "') WHERE " + column + " LIKE '%, " + oldName + "'");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column
                    + ", '," + oldName + "', ', " + newName + "') WHERE " + column + " LIKE '%," + oldName + "'");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column
                    + ", '" + oldName + ",', '" + newName + ",') WHERE " + column + " LIKE '" + oldName + ",%'");
            mDB.execSQL("UPDATE " + table + " SET " + column + " = REPLACE(" + column
                    + ", '" + oldName + " ,', '" + newName + ",') WHERE " + column + " LIKE '" + oldName + " ,%'");
            ContentValues cv = new ContentValues();
            cv.put(column, newName);
            mDB.update(table, cv, column + "='" + oldName + "'", null);
        }
    }

    /*
     * Отсюда методы по таблице Lesson
     **********************************************************************
     **********************************************************************
     **********************************************************************
     */

    /**
     * получение последнего сохраненного урока из БД (все, что еще не пройдено)
     *
     * @return список слов из урока
     */
    public ArrayList<Word> getCurLesson() {
        String sqlQuery = "SELECT * FROM " + T_LESSON + " AS l1 WHERE (l1."
                + C_L_RESULT + " = 0 OR l1." + C_L_RESULT + " IS NULL OR l1."
                + C_L_LEARNING_TYPE_COPML + ">0 OR l1." + C_L_REPEAT_NUMBER_COPML + ">0) AND l1."
                + C_L_ORDINAL + " <> 0 AND l1." + C_L_LESSON_ID + "=(SELECT MAX("
                + C_L_LESSON_ID + ") FROM " + T_LESSON + ") ORDER BY l1." + C_L_ORDINAL;
        return getWordList(sqlQuery);
    }

    /**
     * получение всех слов из сохраненного последнего урока в БД
     *
     * @return список всех слов из урока
     */
    public ArrayList<Word> getAllWordsInCurLesson() {
        String sqlQuery = "SELECT * FROM " + T_LESSON + " AS l1 WHERE l1." + C_L_LESSON_ID
                + "=(SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON + ") ORDER BY l1." + C_L_ID;
        return getWordList(sqlQuery);
    }

    /**
     * получение всех отгаданных слов из последнего сохраненного урока в БД
     *
     * @return список всех отгаданных слов из урока
     */
    public ArrayList<Word> getRightWordsInCurLesson() {
        String sqlQuery = "SELECT * FROM " + T_LESSON + " AS l1 WHERE l1."
                + C_L_RESULT + " = 1 AND l1." + C_L_LESSON_ID + "=(SELECT MAX("
                + C_L_LESSON_ID + ") FROM " + T_LESSON + ") ORDER BY l1." + C_L_ID;
        return getWordList(sqlQuery);
    }

    /**
     * получение всех не правильно отгаданных слов из последнего сохраненного урока в БД
     *
     * @return список всех не правильно отгаданных слов из урока
     */
    public ArrayList<Word> getAllWrongWordsInCurLesson() {
        String sqlQuery = "SELECT * FROM " + T_LESSON + " AS l1 WHERE l1."
                + C_L_RESULT + " = 0 AND l1." + C_L_LESSON_ID + "=(SELECT MAX("
                + C_L_LESSON_ID + ") FROM " + T_LESSON + ") ORDER BY l1." + C_L_ID;
        return getWordList(sqlQuery);
    }

    /**
     * получение не правильно отгаданных слов до тек. слова из последнего сохр. урока в БД
     *
     * @return список не правильно отгаданных слов до тек. слова из урока
     */
    @SuppressLint("Range")
    public ArrayList<Word> getCurWrongWords() {
        String sqlQuery = "SELECT * FROM " + T_LESSON + " AS l1 WHERE l1."
                + C_L_RESULT + " = 0 AND l1." + C_L_ORDINAL + " = 0 AND l1."
                + C_L_LESSON_ID + "=(SELECT MAX(" + C_L_LESSON_ID + ") FROM "
                + T_LESSON + ") ORDER BY l1." + C_L_ID;
        return getWordList(sqlQuery);
    }

    /**
     * получение всех слов из урока с данной категорией(ями)
     *
     * @param categories - категория, к которой может относится слово
     * @return список всех слов из урока с данной категорией(ями)
     */
    @SuppressLint("Range")
    public ArrayList<Word> getWordsFromLessonByCats(String categories) {
        String sqlQuery = "SELECT * FROM " + T_LESSON + " WHERE " + C_L_CATEGORY + "='" + categories + "'";
        return getWordList(sqlQuery);
    }

    @SuppressLint("Range")
    private ArrayList<Word> getWordList(String sqlQuery) {
        String id = C_L_ENGWORD_ID;
        String engWord = C_L_ENGWORD;
        String transcr = C_L_TRANSCRIPTION;
        String rusTranslate = C_L_RUSTRANSLATE;
        String category = C_L_CATEGORY;
        if (sqlQuery.contains("FROM " + T_ENGWORDS)) {
            id = C_EW_ID;
            engWord = C_EW_ENGWORD;
            transcr = C_EW_TRANSCRIPTION;
            rusTranslate = C_EW_RUSTRANSLATE;
            category = C_EW_CATEGORY;
        }
        ArrayList<Word> wordList = new ArrayList<>();
        Cursor c = mDB.rawQuery(sqlQuery, null);
        if (c.moveToFirst()) {
            do {
                Word word = new Word(
                        c.getInt(c.getColumnIndex(id)),
                        c.getString(c.getColumnIndex(engWord)),
                        c.getString(c.getColumnIndex(transcr)),
                        c.getString(c.getColumnIndex(rusTranslate)),
                        c.getString(c.getColumnIndex(category)),
                        sqlQuery.contains("FROM " + T_LESSON) ? c.getString(c.getColumnIndex(C_L_RESULT)) : null);
                wordList.add(word);
            } while (c.moveToNext());
        }
        c.close();
        return wordList;
    }

    /**
     * получение категории(й), по текущему уроку в T_LESSON
     *
     * @return категория
     */
    @SuppressLint("Range")
    public String getCategoryCurLesson() {
        String cat = "";
        Cursor c = mDB.query(T_LESSON, null
                , C_L_LESSON_ID + "=(SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON + ")"
                , null, null, null, null);
        if (c.moveToFirst()) {
            cat = c.getString(c.getColumnIndex(C_L_CATEGORY));
        }
        c.close();
        if (!cat.equals("") && cat.endsWith(", ")) {
            cat = cat.substring(0, cat.length() - 2);
        }
        return cat;
    }

    /**
     * получение номера текущего повторения данного слова в текущем уроке Lesson при комплексном обучении
     *
     * @param curWord - текущее слово, у которого нужно узнать номер текущей повторения слова
     * @return - номер текущей повторения слова
     */
    public int getCurRepeatNumber(Word curWord) {
        if (curWord == null) return 0;
        int curRep = 0;
        Cursor c = mDB.query(T_LESSON, new String[]{DB.C_L_REPEAT_NUMBER_COPML}
                , DB.C_L_ENGWORD_ID + "=" + curWord.getId() + " AND "
                        + C_L_LESSON_ID + "=(SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON + ")"
                , null, null, null, null);
        if (c.moveToFirst()) {
            curRep = c.getInt(0);
        }
        c.close();
        return curRep;
    }

    /**
     * получение текущего типа обучения для данного слова в уроке Lesson при комплексном обучении
     *
     * @param curWord - текущее слово, у которого нужно узнать тип текущего обучения
     * @return - тип текущего обучения: 0-отгадывание из русс. перевода, 1-отгадывание из англ. перевода, 2-написание англ. слова
     */
    public int getCurLearningType(Word curWord) {
        if (curWord == null) return 0;
        int curType = 0;
        Cursor c = mDB.query(T_LESSON, new String[]{DB.C_L_LEARNING_TYPE_COPML}
                , DB.C_L_ENGWORD_ID + "=" + curWord.getId() + " AND "
                        + C_L_LESSON_ID + "=(SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON + ")"
                , null, null, null, null);
        if (c.moveToFirst()) {
            curType = c.getInt(0);
        }
        c.close();
        return curType;
    }

    /**
     * получение результата отгадывания данного слова в текущем уроке Lesson
     *
     * @param word - слово, у которого нужно посмотреть результат
     * @return 1 - слово было отгадано, 0 - слово было не правильно отгадано, null - слово еще не отгадывалось
     */
    @SuppressLint("Range")
    public Integer getResultInCurLesson(Word word) {
        Integer res = null;
        Cursor c = mDB.query(T_LESSON, null,
                C_L_ENGWORD_ID + "=" + word.getId() + " AND "
                        + C_L_LESSON_ID + "=(SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON + ")"
                , null, null, null, null);
        if (c.moveToFirst()) {
            try {
                res = Integer.valueOf(c.getString(c.getColumnIndex(C_L_RESULT)));
            } catch (Exception ex) {
            }
        }
        c.close();
        return res;
    }

    /**
     * удаление из таблицы T_LESSON всех слов данной категории(й)
     *
     * @param category - категория(и) словая которой удаляются
     * @return - количество удаленных записей
     */
    public int delCatLesson(String category) {
        return mDB.delete(T_LESSON, C_L_CATEGORY + "='" + category + "'", null);
    }

    /**
     * удаление всех уроков кроме текущего
     */
    public void delLessonWithoutCur() {
        mDB.delete(T_LESSON, C_L_LESSON_ID + "<(SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON + ")", null);
        ContentValues cv = new ContentValues();
        cv.put(C_L_LESSON_ID, System.currentTimeMillis() / 1000);
        mDB.update(T_LESSON, cv, null, null);
    }


    /**
     * Добавление слова в один урок таблицы T_LESSON
     *
     * @param isLessonsHistory - сохраняется в T_LESSON все незавершенные уроки (true) или только последний урок (false)
     * @param idEngWord        - id англиского слова
     * @param engWord          - англиское слово
     * @param transcription    - транскрипция англйского слова
     * @param rusTranslate     - русский перевод
     * @param category         - категория слова
     * @param result           - результат урока: null - слово еще не отгадывалось, 0 - не отгадал, 1 - отгадал
     * @param current          - текущее слово (1), все остальные слова - 0
     */
    @SuppressLint("Range")
    public void addWordInLesson(boolean isLessonsHistory, int idEngWord, String engWord,
                                String transcription, String rusTranslate, String category,
                                Integer result, int current) {
        // если созраняется только послед. урок, то C_L_LESSON_ID = System.currentTimeMillis()/1000, т.к. предварительно вся таблица очищается
        long lessonId = System.currentTimeMillis() / 1000;
        // если сохраняются все незавершенные уроки, то определяется, какой C_L_LESSON_ID нужно ставить
        if (isLessonsHistory) {
            Cursor c = mDB.rawQuery("SELECT " + C_L_LESSON_ID + " FROM " + T_LESSON
                    + " WHERE " + C_L_CATEGORY + "='" + category + "'", null);
            if (c.moveToFirst()) {
                lessonId = c.getInt(0);
            }
            c.close();
        }
        ContentValues cv = new ContentValues();
        cv.put(C_L_LESSON_ID, lessonId);
        cv.put(C_L_ENGWORD_ID, idEngWord);
        cv.put(C_L_ENGWORD, engWord);
        cv.put(C_L_TRANSCRIPTION, transcription);
        cv.put(C_L_RUSTRANSLATE, rusTranslate);
        cv.put(C_L_CATEGORY, category);
        cv.put(C_L_RESULT, result);
        cv.put(C_L_ORDINAL, current);
        mDB.insert(T_LESSON, null, cv);
    }

    /**
     * Находит id урока по категории, если он есть, и -1, если не найден
     *
     * @param categories - категория(и) по которой ищется урок
     * @return - id урока или -1, если урок не найден
     */
    public int getLessonIdByCategory(String categories) {
        int lessonId = -1;
        if (categories != null) {
            Cursor c = mDB.rawQuery("SELECT " + C_L_LESSON_ID + " FROM " + T_LESSON
                    + " WHERE " + C_L_CATEGORY + "='" + categories + "'", null);
            if (c.moveToFirst()) {
                lessonId = c.getInt(0);
            }
            c.close();
        }
        return lessonId;
    }

    /**
     * получаем список категорий во всех уроках
     *
     * @return TreeMap<Long, String> с id урока и категорией(ями) урока(ов)
     */
    public TreeMap<Long, String> getCatsInAllLessons() {
        TreeMap<Long, String> treeCats = new TreeMap<>();
        Cursor c = mDB.rawQuery("SELECT " + C_L_LESSON_ID + ", " + C_L_CATEGORY + " FROM " + T_LESSON
                + " GROUP BY " + C_L_LESSON_ID + ", " + C_L_CATEGORY + " ORDER BY " + C_L_LESSON_ID, null);
        if (c.moveToFirst()) {
            do {
                treeCats.put(c.getLong(0), c.getString(1));
            } while (c.moveToNext());
        }
        c.close();
        return treeCats;
    }

    /**
     * @param idWord        - id слова
     * @param engWord       - англиское слово
     * @param transcription - транскрипция англйского слова
     * @param rusTranslate  - русский перевод
     * @param category      - категория слова
     * @return true - если слово добавляется в текущий урок, false - если в текущий урок не добавляется
     */
    public boolean addWordInLessons(long idWord, String engWord, String transcription, String rusTranslate, String category) {
        boolean changeInCurLesson = false;
        List<String> wordCats = strToList(category, ",", true); // список категорий которым принадлежит добавляемое слово
        TreeMap<Long, String> catsInLessons = getCatsInAllLessons(); // категории во всех уроках
        for (Map.Entry<Long, String> entry : catsInLessons.entrySet()) {
            List<String> lessonCats = strToList(entry.getValue(), ",", true); // список категорий данного урока
            for (String cat : wordCats) {
                if (lessonCats.contains(cat)) {
                    int ordinal = 0; // порядковй номер слова в уроке
                    Cursor c = mDB.rawQuery("SELECT MAX(" + C_L_ORDINAL + ") FROM " + T_LESSON
                            + " WHERE " + C_L_LESSON_ID + "=" + entry.getKey(), null);
                    if (c.moveToFirst()) {
                        ordinal = c.getInt(0);
                    }
                    c.close();
                    ContentValues cv = new ContentValues();
                    cv.put(C_L_LESSON_ID, entry.getKey());
                    cv.put(C_L_ENGWORD_ID, idWord);
                    cv.put(C_L_ENGWORD, engWord);
                    cv.put(C_L_TRANSCRIPTION, transcription);
                    cv.put(C_L_RUSTRANSLATE, rusTranslate);
                    cv.put(C_L_CATEGORY, entry.getValue());
                    cv.put(C_L_ORDINAL, ++ordinal);
                    mDB.insert(T_LESSON, null, cv);
                    if (entry.getKey().equals(catsInLessons.lastKey())) {
                        changeInCurLesson = true;
                    }
                    break;
                }
            }
        }
        return changeInCurLesson;
    }

    /**
     * делает урок данной категории текущим, т.е. C_L_LESSON_ID делает максимальным
     *
     * @param categories - категория(и), которая становится текущим уроком
     * @return - кол-во обновленных записей
     */
    public int changeLessonIdByCategory(String categories) {
        long lessonId = getLessonIdByCategory(categories);
        long newLessonId = System.currentTimeMillis() / 1000;
        ContentValues cv = new ContentValues();
        cv.put(C_L_LESSON_ID, newLessonId);
        return mDB.update(T_LESSON, cv, C_L_LESSON_ID + "=" + lessonId, null);
    }

    /**
     * установление результата отгадывания слова в текущем уроке Lesson
     *
     * @param word   - слово, на котором нужно отразить результат урока
     * @param result - результат (1 - слово отгадано, 0 - нет)
     */
    public void setResultCurLesson(Word word, int result) {
        ContentValues cv = new ContentValues();
        cv.put(C_L_RESULT, result);
        mDB.update(T_LESSON, cv, C_L_ENGWORD_ID + "=" + word.getId() + " AND "
                + C_L_LESSON_ID + "=(SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON + ")", null);
    }

    /**
     * установление номера текущей попытки по данному слову в текущем уроке Lesson
     *
     * @param word            - слово, на котором нужно установить текущую попытку
     * @param curRepeatNumber - текущая попытка для установления
     */
    public void setCurRepeatNumber(Word word, int curRepeatNumber) {
        ContentValues cv = new ContentValues();
        cv.put(C_L_REPEAT_NUMBER_COPML, curRepeatNumber);
        mDB.update(T_LESSON, cv, C_L_ENGWORD_ID + " = " + word.getId() + " AND "
                + C_L_LESSON_ID + "=(SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON + ")", null);
    }

    /**
     * установление текущего типа обучения в комплексном обучении в текущем уроке Lesson
     *
     * @param word            - слово, на котором нужно установить текущий тип обучения
     * @param curLearningType - текущий тип обучения в комплексном обучении: 0-отгадывание из из русс. перевода, 1-отгадывание из англ. перевода, 2-написание англ. слова
     */
    public void setCurLearningType(Word word, int curLearningType) {
        ContentValues cv = new ContentValues();
        cv.put(C_L_LEARNING_TYPE_COPML, curLearningType);
        mDB.update(T_LESSON, cv, C_L_ENGWORD_ID + " = " + word.getId() + " AND "
                + C_L_LESSON_ID + "=(SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON + ")", null);
    }

    /**
     * установление порядкового номера слова в текущем уроке Lesson
     *
     * @param word   - слово, для которого устанавливается порядковый номер (если null, то устанавливается для всех слов урока)
     * @param number - порядковый номер урока (если 0, значит исключается из урока)
     */
    public void setNumberInLesson(Word word, int number) {
        ContentValues cv = new ContentValues();
        cv.put(C_L_ORDINAL, number);
        mDB.update(T_LESSON, cv, (word == null ? "" : C_L_ENGWORD_ID + " = " + word.getId() + " AND ")
                + C_L_LESSON_ID + "=(SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON + ")", null);
    }

    /**
     * удаляет все уроки кроме текущего урока и кроме уроков, которые младше определенного кол-ва дней
     *
     * @param delDays - кол-во дней старше которых уроки удаляются
     * @return - кол0во удаленных записей
     */
    public int delOldLessons(int delDays) {
        // id урока, меньше которого все нужно удалять
        long idMinLesson = System.currentTimeMillis() / 1000 - ((long) delDays * 24 * 60 * 60);
        long idCurLesson = 0; // id текущего урока
        Cursor c = mDB.rawQuery("SELECT MAX(" + C_L_LESSON_ID + ") FROM " + T_LESSON, null);
        if (c.moveToFirst()) {
            idCurLesson = c.getInt(0);
        }
        c.close();
        return mDB.delete(T_LESSON, C_L_LESSON_ID + "<>" + idCurLesson
                + " AND " + C_L_LESSON_ID + "<" + idMinLesson, null);
    }

    /*
     * Отсюда методы по таблице Statistics
     * *********************************************************************
     * *********************************************************************
     * *********************************************************************
     */

    /**
     * получение списка категорий с количеством правильных и неправильных ответов из таблицы Statistics
     * Причем последним набором в списке является агрегированное количество правильных и неправильных ответов
     *
     * @return получение списка категорий с количеством правильных и неправильных ответов из таблицы Statistics
     */
    @SuppressLint("Range")
    public ArrayList<Category> getCategoriesStats() {
        ArrayList<Category> categoryStats = new ArrayList<>();
        String sqlQuery = "SELECT " + C_S_CATEGORY + ", SUM(" + C_S_AMOUNT_RIGHT + ") AS right1, SUM(" +
                C_S_AMOUNT_WRONG + ") AS wrong FROM " + T_STATISTICS + " GROUP BY " + C_S_CATEGORY +
                " ORDER BY wrong DESC, right1 ";
        Cursor c = mDB.rawQuery(sqlQuery, null);
        if (c.moveToFirst()) {
            int amountRight = 0;
            int amountWrong = 0;
            do {
                amountRight += c.getInt(c.getColumnIndex("right1"));
                amountWrong += c.getInt(c.getColumnIndex("wrong"));
                categoryStats.add(new Category(c.getString(c.getColumnIndex(C_S_CATEGORY)),
                        c.getInt(c.getColumnIndex("right1")), c.getInt(c.getColumnIndex("wrong"))));
            } while (c.moveToNext());
            categoryStats.add(new Category("все категории", amountRight, amountWrong));
        }
        c.close();
        return categoryStats;
    }

    /**
     * получение списка категорий с указанием сколько слов хотя бы один раз отгадано и сколько ни разу не отгадано (из таблицы Statistics)
     *
     * @return получение списка категорий с указанием сколько слов хотя бы один раз отгадано и сколько ни разу не отгадано (из таблицы Statistics)
     */
    @SuppressLint("Range")
    public HashMap<String, Category> getRightWordsInCats() {
        HashMap<String, Category> rightWordsInCats = new HashMap<>();
        String sqlQuery = "SELECT " + C_S_CATEGORY + ", SUM(CASE WHEN " + C_S_AMOUNT_RIGHT +
                ">0 THEN 1 ELSE 0 END) AS right1, SUM(CASE WHEN " + C_S_AMOUNT_RIGHT +
                ">0 THEN 0 ELSE 1 END) AS wrong FROM " + T_STATISTICS + " GROUP BY " + C_S_CATEGORY +
                " ORDER BY wrong DESC, right1 ";
        Cursor c = mDB.rawQuery(sqlQuery, null);
        if (c.moveToFirst()) {
            do {
                rightWordsInCats.put(c.getString(c.getColumnIndex(C_S_CATEGORY)),
                        new Category(c.getString(c.getColumnIndex(C_S_CATEGORY)),
                                c.getInt(c.getColumnIndex("right1")),
                                c.getInt(c.getColumnIndex("wrong"))));
            } while (c.moveToNext());
        }
        c.close();
        return rightWordsInCats;
    }

    /**
     * обновление статистики по правильным / неправильным ответам
     *
     * @param word     слово, по которому дан ответ
     * @param category категория
     * @param is_right правильно или нет дан ответ
     * @return кол-во обновленных записей
     */
    @SuppressLint("Range")
    public int updateStat(Word word, String category, boolean is_right) {
        int amountUpdate = 0;
        ArrayList<String> cats = new ArrayList<>();
        String[] arr = category.split(",");
        for (String cat : arr) {
            cats.add(cat.trim());
        }
        for (int i = 0; i < cats.size(); i++) {
            Cursor c = mDB.query(T_ENGWORDS, null, C_EW_ID + "=" + word.getId() +
                    " AND (" + getWhereClauseForCategory(cats.get(i)) + ")", null, null, null, null);
            if (c.moveToFirst()) {
                c.close();
                c = mDB.query(T_STATISTICS, null, C_S_ID_WORD + "=" + word.getId() + " AND " +
                        C_S_CATEGORY + "='" + cats.get(i) + "'", null, null, null, null);
                if (c.moveToFirst()) {
                    int oldAmount = c.getInt(c.getColumnIndex(is_right ? C_S_AMOUNT_RIGHT : C_S_AMOUNT_WRONG));
                    ContentValues cv = new ContentValues();
                    cv.put(is_right ? C_S_AMOUNT_RIGHT : C_S_AMOUNT_WRONG, oldAmount + 1);
                    mDB.update(T_STATISTICS, cv, C_S_ID_WORD + "=" + word.getId() + " AND " +
                            C_S_CATEGORY + "='" + cats.get(i) + "'", null);
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put(C_S_ID_WORD, word.getId());
                    cv.put(C_S_CATEGORY, cats.get(i));
                    cv.put(is_right ? C_S_AMOUNT_RIGHT : C_S_AMOUNT_WRONG, 1);
                    mDB.insert(T_STATISTICS, null, cv);
                }
                c.close();
                amountUpdate++;
            }
        }
        return amountUpdate;
    }

    /**
     * удаление всей статистики по определенным категориям по отгаданным / неотгаданным словам
     *
     * @param cats строка с категориями, где категории идут в одну строку через запятую
     * @return кол-во удаленных строк
     */
    public int removeStats(String cats) {
        String[] arr = cats.split(",");
        String catsForIn = ""; // строка, в которой будет выражение для IN в SQL-запросе
        for (String s : arr) {
            catsForIn = catsForIn + "'" + s.trim() + "', ";
        }
        catsForIn = catsForIn.substring(0, catsForIn.length() - 2);
        return mDB.delete(T_STATISTICS, C_S_CATEGORY + " IN (" + catsForIn + ")", null);
    }

    /**
     * Обновляем запись в главной таблице engwords английских слов и в таблице с действующим уроком
     *
     * @param oldName старое название категории
     * @param newName старое название категории
     * @return int - кол-во обновленных строк
     */
    public int updateCatInStats(String oldName, String newName) {
        ContentValues cv = new ContentValues();
        cv.put(C_S_CATEGORY, newName);
        return mDB.update(T_STATISTICS, cv, C_S_CATEGORY + "='" + oldName + "'", null);
    }

    /*
     * Отсюда методы по таблице exitstate
     **********************************************************************
     **********************************************************************
     **********************************************************************
     */

    /**
     * получение из таблицы exitstate значения заданной переменной
     *
     * @param variable название переменной, значение которой нужно вернуть
     * @return значение переменной
     */
    @SuppressLint("Range")
    public String getValueByVariable(String variable) {
        Cursor c = mDB.query(T_EXITSTATE, null, C_ES_VARIABLE + "=?",
                new String[]{variable}, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex(C_ES_VALUE));
            }
            c.close();
        }
        return null;
    }

    /**
     * Обновляем запись в таблице exitstate
     *
     * @param variable - переменная, которую нужно обновить
     * @param value    - новое значение переменной
     * @return int - кол-во обновленных записей
     */
    public int updateRecExitState(String variable, String value) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(C_ES_VALUE, value);
            int a = mDB.update(T_EXITSTATE, cv, C_ES_VARIABLE + "=?",
                    new String[]{variable});
            if (a != 1) {
                cv.put(C_ES_VARIABLE, variable);
                mDB.delete(T_EXITSTATE, C_ES_VARIABLE + "=?",
                        new String[]{variable});
                mDB.insert(T_EXITSTATE, null, cv);
            }
            return 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * выполняет не SELECT выражения, которые не возвращают данные
     *
     * @param strSql SQL-выражение, которое должны быполнено
     */
    public void execSQL(String strSql, Object[] bindArgs) {
        if (bindArgs == null) {
            mDB.execSQL(strSql);
        } else {
            mDB.execSQL(strSql, bindArgs);
        }
    }
}