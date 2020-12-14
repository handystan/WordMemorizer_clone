package ru.handy.android.wm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

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
    public static final String C_EW_HISTORY = "ew_history";
    // таблица с тек. уроком
    public static final String T_LESSON = "lesson";
    public static final String C_L_ID = "_id";
    public static final String C_L_ENGWORD_ID = "l_engword_id"; // id слова
    public static final String C_L_ENGWORD = "l_engword";
    public static final String C_L_TRANSCRIPTION = "l_transcription";
    public static final String C_L_RUSTRANSLATE = "l_rustranslate";
    public static final String C_L_CATEGORY = "l_category"; // категория(и) (для всех слов один перечень категорий
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
    // что было введено в поле поиска в словаре
    public static final String SEARCH_WORD = "searchWord";
    // английские слова будут произноситься по американски или британски (0-американский, 1-британский)
    public static final String PRONUNCIATION_USUK= "pronunciationUsUk";
    // какие слова в словаре переводятся: 0-английские, 1-русские
    public static final String DICT_TRASL_DIRECT = "dictTranslDirect";
    // тип поиска слов: 0-по начальным буквам, 1-по буквам в любой части слова
    public static final String DICT_SEARCH_TYPE = "dictSearchType";
    // показывать в словаре последние выбранные слова: 0-нет, 1-да
    public static final String DICT_SHOW_HISTORY = "dictShowHistory";
    // сколько пользователь перечислил ознаграждения
    public static final String AMOUNT_DONATE = "0";
    // какой фон выбран? по умолчанию - 1. Предложено 15 цветов
    public static final String BG_COLOR = "bgColor";
    // признак старой бесплатной БД: 1 - старая бесплатная БД, 0 - новая БД с платными настройками
    public static final String OLD_FREE_DB = "oldFreeDb";
    // дата начала бесплатного месячного периода для статистики
    public static final String DATE_TRIAL_STATS = "dateTrialStats";
    // дата начала бесплатного месячного периода для смены цвета фона
    public static final String DATE_BG_COLOR = "dateBgColor";
    // дата начала бесплатного месячного периода для смены метода обучения
    public static final String DATE_LEARNING_METHOD = "dateLearningMethod";
    // дата начала бесплатного месячного периода для смены языка обучения
    public static final String DATE_LANGUAGE = "dateLanguage";
    // дата начала бесплатного месячного периода для кол-ва слов, показываемых при обучении
    public static final String DATE_LANG_WORD_AMOUNT = "dateLangWordAmount";
    // скрипт для создания основной таблицы-словаря со всеми словами
    public static final String T_ENGWORDS_CREATE = "create table " + T_ENGWORDS
            + " (" + C_EW_ID + " integer primary key autoincrement, "
            + C_EW_ENGWORD + " text, " + C_EW_TRANSCRIPTION + " text, "
            + C_EW_RUSTRANSLATE + " text, " + C_EW_CATEGORY + " text, "
            + C_EW_HISTORY + " integer);";
    // скрипт для создания таблицы, в которой храняться данные по текущему уроку
    public static final String T_LESSON_CREATE = "create table " + T_LESSON
            + " (" + C_L_ID + " integer primary key autoincrement, "
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
    private static final int DB_VERSION = 26; // 13 - первая версия с платными настройками
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

    /**
     * Получение всех категорий
     *
     * @return ArrayList<String> с перечнем категорий
     */
    public ArrayList<String> getCategories() {
        Cursor cursor = mDB.query(true, T_ENGWORDS, new String[]{C_EW_CATEGORY}, C_EW_CATEGORY
                + " IS NOT NULL AND " + C_EW_CATEGORY + " <> ''", null, null, null, C_EW_CATEGORY, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                ArrayList<String> list = new ArrayList<>();
                do {
                    String str = cursor.getString(
                            cursor.getColumnIndex(C_EW_CATEGORY)).trim();
                    String[] arr = str.split(",");
                    for (String category : arr) {
                        category = category.trim();
                        if (!list.contains(category))
                            list.add(category);
                    }
                } while (cursor.moveToNext());
                cursor.close();
                Collections.sort(list, new Comparator<String>() {
                    @Override
                    public int compare(String str1, String str2) {
                        String s1 = str1.toLowerCase(new Locale("ru"));
                        String s2 = str2.toLowerCase(new Locale("ru"));
                        String s1p = s1;
                        String s2p = s2;
                        int n1 = 0;
                        for (int i = 1; i < s1.length(); i++) {
                            try {
                                n1 = Integer.parseInt(s1.substring(s1.length()
                                        - i));
                                s1p = s1.substring(0, s1.length() - i);
                            } catch (Exception e) {
                                break;
                            }
                        }
                        int n2 = 0;
                        for (int i = 1; i < s2.length(); i++) {
                            try {
                                n2 = Integer.parseInt(s2.substring(s2.length()
                                        - i));
                                s2p = s2.substring(0, s2.length() - i);
                            } catch (Exception e) {
                                break;
                            }
                        }
                        if (!s1.startsWith("прочее") && s2.startsWith("прочее")) {
                            return -1;
                        } else if (s1.startsWith("прочее")
                                && !s2.startsWith("прочее")) {
                            return 1;
                        } else if (n1 != 0 && n2 != 0 && s1p.equals(s2p)) {
                            return n1 - n2;
                        }
                        return s1.compareToIgnoreCase(s2);
                    }
                });
                return list;
            }
        }
        return null;
    }

    /**
     * Получение всех классов категорий (названий и кол-во слов в каждой категории
     *
     * @return ArrayList<Category> массив с перечнем классов категорий (названия и кол-во)
     */
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
        return mDB
                .query(T_ENGWORDS, null, null, null, null, null, C_EW_ENGWORD);
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
            partOfWord = partOfWord.replace("'", "");
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
     * Получает данные из таблицы engwords по категории
     *
     * @param category       - категория, к которой может относится слово
     * @param isOnlyMistakes - показывать только слова с ошибками (true) или все (false)
     * @return Cursor с данными
     */
    public Cursor getDataByCategory(String category, boolean isOnlyMistakes) {
        String sqlQuery;
        if (isOnlyMistakes) {
            sqlQuery = "SELECT t1.* FROM (SELECT * FROM " + T_ENGWORDS + " WHERE " +
                    getWhereClauseForCategory(category) + ") t1, " + T_STATISTICS + " t2 WHERE t1." +
                    C_EW_ID + "=t2." + C_S_ID_WORD + " AND t2." + C_S_AMOUNT_WRONG + ">0 ORDER BY t1." + C_EW_ENGWORD;
        } else {
            sqlQuery = "SELECT * FROM " + T_ENGWORDS + " WHERE " + getWhereClauseForCategory(category)
                    + " ORDER BY " + C_EW_ENGWORD;
        }
        return mDB.rawQuery(sqlQuery, null);
    }

    public Cursor getDataByCategory(String category) {
        return getDataByCategory(category, false);
    }

    /**
     * Получает слова из таблицы engwords по категории
     *
     * @param category - категория, к которой может относится слово
     * @return ArrayList<Word> - список слов с данной категорией
     */
    public ArrayList<Word> getWordsByCategory(String category) {
        return getWordList("SELECT * FROM " + T_ENGWORDS + " WHERE " + getWhereClauseForCategory(category) + " ORDER BY LOWER(" + C_EW_ENGWORD + ")");
    }

    private String getWhereClauseForCategory(String category) {
        StringBuilder whereClause = new StringBuilder();
        if (category != null) {
            String[] arr = category.split(",");
            for (String cat : arr) {
                String c = cat.trim().replace("'", "''");
                if (!c.equals(""))
                    whereClause.append(C_EW_CATEGORY).append("='").append(c).append("' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '").append(c).append(",%' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '").append(c).append(" ,%' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '").append(c).append("  ,%' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%,").append(c).append("' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%, ").append(c).append("' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%,  ").append(c).append("' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%,").append(c).append(",%' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%, ").append(c).append(",%' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%,  ").append(c).append(",%' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%,").append(c).append(" ,%' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%,").append(c).append("  ,%' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%, ").append(c).append(" ,%' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%, ").append(c).append("  ,%' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%,  ").append(c).append(" ,%' OR ")
                            .append(C_EW_CATEGORY).append(" LIKE '%,  ").append(c).append("  ,%' OR ");
            }
            if (whereClause.toString().equals(""))
                whereClause = new StringBuilder(C_EW_CATEGORY + "=''");
            else
                whereClause = new StringBuilder(whereClause
                        .substring(0, whereClause.length() - 4));
        }
        return whereClause.toString();
    }

    /**
     * Добавляем новое слово в главной таблице engwords английских слов
     * и в таблице с действующим уроком (если слово нужной категории)
     *
     * @param engWord английское слово
     * @param transcription транскрипция
     * @param rusTranslate русский перевод
     * @param category категория
     * @return возвращает id нового слова в таблице T_ENGWORDS
     */
    public long addRecEngWord(String engWord, String transcription,
                              String rusTranslate, String category) {
        ContentValues cv = new ContentValues();
        cv.put(C_EW_ENGWORD, engWord);
        cv.put(C_EW_TRANSCRIPTION, transcription);
        cv.put(C_EW_RUSTRANSLATE, rusTranslate);
        cv.put(C_EW_CATEGORY, category);
        long idWord = mDB.insert(T_ENGWORDS, null, cv);
        String strCats = getCategoryLesson(); // выбранные категории для текущего урока
        String[] arr = strCats.split(",");
        ArrayList<String> cats = new ArrayList<>();
        for (String cat : arr) {
            cats.add(cat.trim());
        }
        boolean inLesson = false; // переменная, показывающая входит слово в урок или нет
        for (String cat : cats) {
            if (category.contains(cat)) {
                inLesson = true;
                break;
            }
        }
        cv = new ContentValues();
        if (inLesson) { // добавляем слово в урок только, если его категория совпадате с категорией урока
            cv.put(C_L_ENGWORD_ID, idWord);
            cv.put(C_L_ENGWORD, engWord);
            cv.put(C_L_TRANSCRIPTION, transcription);
            cv.put(C_L_RUSTRANSLATE, rusTranslate);
            cv.put(C_L_CATEGORY, strCats);
            mDB.insert(T_LESSON, null, cv);
        }
        return idWord;
    }

    /**
     * Обновляем запись в главной таблице engwords английских слов и в таблице с действующим уроком
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
        cv.put(C_L_ENGWORD, engWord);
        cv.put(C_L_TRANSCRIPTION, transcription);
        cv.put(C_L_RUSTRANSLATE, rusTranslate);
        cv.put(C_L_CATEGORY, category);
        mDB.update(T_LESSON, cv, C_L_ENGWORD_ID + " = " + id, null);
        cv = new ContentValues();
        cv.put(C_EW_ENGWORD, engWord);
        cv.put(C_EW_TRANSCRIPTION, transcription);
        cv.put(C_EW_RUSTRANSLATE, rusTranslate);
        cv.put(C_EW_CATEGORY, category);
        return mDB.update(T_ENGWORDS, cv, C_EW_ID + " = " + id, null);
    }

    /**
     * удалить запись из T_ENGWORDS и из T_LESSON
     *
     * @param id записи
     */
    public int delRecEngWord(long id) {
        int a = mDB.delete(T_LESSON, C_L_ENGWORD_ID + " = " + id, null);
        Log.d("myLogs", "amount delete T_LESSON = " + a);
        int b = mDB.delete(T_ENGWORDS, C_EW_ID + " = " + id, null);
        Log.d("myLogs", "amount delete T_ENGWORDS = " + b);
        return b;
    }

    /**
     * удалить все записи из T_ENGWORDS
     */
    public int delAll(String table) {
        return mDB.delete(table, null, null);
    }

    /**
     * Получение случайных наборов слов в данном количестве из базы с данной
     * категорией
     *
     * @param categories     категории, по которым нужно получить случайные слова. Если =
     *                       null, то берет по всем категориям
     * @param rightWord      правильное слово, которое должен отгадать пользователь (может
     *                       быть null)
     * @param countOfChoice  количество получаемых слов
     * @param isOnlyMistakes брать только слова с ошибками (true) или все (false)
     * @return ArrayList<Word> массив слов
     */
    public ArrayList<Word> getRandomWords(String categories, Word rightWord, int countOfChoice, boolean isOnlyMistakes) {
        ArrayList<Word> allWords = new ArrayList<>();
        Cursor c;
        if (categories != null) {
            c = getDataByCategory(categories, isOnlyMistakes);
            if (c.getCount() < countOfChoice && c.getCount() != 0) {
                c.close();
                c = getAllWords();
            }
        } else {
            c = getAllWords();
        }
        if (c.moveToFirst()) {
            do {
                Word word = new Word(c.getInt(c.getColumnIndex(C_EW_ID)),
                        c.getString(c.getColumnIndex(C_EW_ENGWORD)),
                        c.getString(c.getColumnIndex(C_EW_TRANSCRIPTION)),
                        c.getString(c.getColumnIndex(C_EW_RUSTRANSLATE)));
                if (rightWord == null || !rightWord.equals(word)
                        || !rightWord.getEngWord().equals(word.getEngWord())
                        || !rightWord.getRusTranslate().equals(word.getRusTranslate()))
                    allWords.add(word);
            } while (c.moveToNext());
            c.close();
        }
        ArrayList<Word> resWords = new ArrayList<>();
        Random randGen = new Random();
        int countAllWords = allWords.size();
        int count = rightWord == null ? countOfChoice : countOfChoice - 1;
        while (resWords.size() < count) {
            if (allWords.size() > 0) {
                int rand = randGen.nextInt(allWords.size());
                resWords.add(allWords.get(rand));
                allWords.remove(rand);
            } else {
                resWords.add(new Word(0, "", "", ""));
            }
        }
        if (rightWord != null) {
            int rand = randGen.nextInt(Math.max(1, Math.min(countAllWords, countOfChoice)));
            resWords.add(rand, rightWord);
        }
        return resWords;
    }

    /**
     * Получение всех случайных наборов слов из базы с данной категорией
     *
     * @param categories     категория, по кот. нужно получить слова в случайном порядке
     * @param isOnlyMistakes - брать только слова с ошибками (true) или все (false)
     * @return ArrayList<Word> массив слов
     */
    public ArrayList<Word> getRandomWords(String categories, boolean isOnlyMistakes) {
        return getRandomWords(categories, null, getDataByCategory(categories, isOnlyMistakes).getCount(), isOnlyMistakes);
    }

    /**
     * Получение всех случайных наборов слов из базы с данной категорией
     *
     * @param categories категория, по кот. нужно получить слова в случайном порядке
     * @return ArrayList<Word> массив слов
     */
    public ArrayList<Word> getRandomWords(String categories) {
        return getRandomWords(categories, null, getDataByCategory(categories).getCount(), false);
    }

    /**
     * устанавливает порядковый номер столбца C_EW_HISTORY. Если id = null, то
     * значение этого столбца во всех словах становится = null
     *
     * @param id - id слова в таблице C_EW_HISTORY
     */
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

    /*
     * Отсюда методы по таблице Lesson
     **********************************************************************
     **********************************************************************
     **********************************************************************
     */

    /**
     * получение сохраненного урока из БД (все, что еще не пройдено)
     *
     * @return список слов из урока
     */
    public ArrayList<Word> getLesson() {
        String sqlQuery = "SELECT * FROM " + T_LESSON + " AS l1 WHERE (l1."
                + C_L_RESULT + " = 0 OR l1." + C_L_RESULT + " IS NULL OR l1."
                + C_L_LEARNING_TYPE_COPML + ">0 OR l1." + C_L_REPEAT_NUMBER_COPML + ">0) AND l1."
                + C_L_ORDINAL + " <> 0 ORDER BY l1." + C_L_ORDINAL;
        return getWordList(sqlQuery);
    }

    /**
     * получение всех слов из сохраненного урока в БД
     *
     * @return список всех слов из урока
     */
    public ArrayList<Word> getAllWordsInLesson() {
        String sqlQuery = "SELECT * FROM " + T_LESSON + " AS l1 ORDER BY l1."
                + C_L_ID;
        return getWordList(sqlQuery);
    }

    /**
     * получение всех отгаданных слов из сохраненного урока в БД
     *
     * @return список всех отгаданных слов из урока
     */
    public ArrayList<Word> getRightWordsInLesson() {
        String sqlQuery = "SELECT * FROM " + T_LESSON + " AS l1 WHERE l1."
                + C_L_RESULT + " = 1 ORDER BY l1." + C_L_ID;
        return getWordList(sqlQuery);
    }

    /**
     * получение всех не правильно отгаданных слов из сохраненного урока в БД
     *
     * @return список всех не правильно отгаданных слов из урока
     */
    public ArrayList<Word> getAllWrongWordsInLesson() {
        String sqlQuery = "SELECT * FROM " + T_LESSON + " AS l1 WHERE l1."
                + C_L_RESULT + " = 0 ORDER BY l1." + C_L_ID;
        return getWordList(sqlQuery);
    }

    /**
     * получение не правильно отгаданных слов до тек. слова из сохр. урока в БД
     *
     * @return список не правильно отгаданных слов до тек. слова из урока
     */
    public ArrayList<Word> getWrongWords() {
        String sqlQuery = "SELECT * FROM " + T_LESSON + " AS l1 WHERE l1."
                + C_L_RESULT + " = 0 AND l1." + C_L_ORDINAL + " = 0 ORDER BY l1." + C_L_ID;
        return getWordList(sqlQuery);
    }

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
     * получение категории, по которой строился урок Lesson
     *
     * @return категория
     */
    public String getCategoryLesson() {
        String cat = "";
        Cursor c = mDB.query(T_LESSON, null, null, null, null, null, null);
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
     * получение номера текущего повторения данного слова в уроке Lesson при комплексном обучении
     *
     * @param curWord - текущее слово, у которого нужно узнать номер текущей повторения слова
     * @return - номер текущей повторения слова
     */
    public int getCurRepeatNumber(Word curWord) {
        if (curWord == null) return 0;
        int curRep = 0;
        Cursor c = mDB.query(T_LESSON, new String[]{DB.C_L_REPEAT_NUMBER_COPML}
                , DB.C_L_ENGWORD_ID + "=" + curWord.getId(), null, null, null, null);
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
                , DB.C_L_ENGWORD_ID + "=" + curWord.getId(), null, null, null, null);
        if (c.moveToFirst()) {
            curType = c.getInt(0);
        }
        c.close();
        return curType;
    }

    /**
     * получение результата отгадывания данного слова в уроке Lesson
     *
     * @param word - слово, у которого нужно посмотреть результат
     * @return 1 - слово было отгадано, 0 - слово было не правильно отгадано, null - слово еще не отгадывалось
     */
    public Integer getResult(Word word) {
        Integer res = null;
        Cursor c = mDB.query(T_LESSON, null,
                C_L_ENGWORD_ID + "=" + word.getId(), null, null, null, null);
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
     * Добавление записи в таблицу действующего урока Lesson
     *
     * @param idEngWord     - id англиского слова
     * @param engWord       - англиское слово
     * @param transcription - транскрипция англйского слова
     * @param rusTranslate  - русский перевод
     * @param category      - категория слова
     * @param result        - результат урока: null - слово еще не отгадывалось, 0 - не
     *                      отгадал, 1 - отгадал
     * @param current       - текущее слово (1), все остальные слова - 0
     */
    public void addRecLesson(int idEngWord, String engWord,
                             String transcription, String rusTranslate, String category,
                             Integer result, int current) {
        ContentValues cv = new ContentValues();
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
     * установление результата отгадывания слова в уроке Lesson
     *
     * @param word   - слово, на котором нужно отразить результат урока
     * @param result - результат (1 - слово отгадано, 0 - нет)
     */
    public void setResult(Word word, int result) {
        ContentValues cv = new ContentValues();
        cv.put(C_L_RESULT, result);
        mDB.update(T_LESSON, cv, C_L_ENGWORD_ID + " = " + word.getId(), null);
    }

    /**
     * установление номера текущей попытки по данному слову в уроке Lesson
     *
     * @param word            - слово, на котором нужно установить текущую попытку
     * @param curRepeatNumber - текущая попытка для установления
     */
    public void setCurRepeatNumber(Word word, int curRepeatNumber) {
        ContentValues cv = new ContentValues();
        cv.put(C_L_REPEAT_NUMBER_COPML, curRepeatNumber);
        mDB.update(T_LESSON, cv, C_L_ENGWORD_ID + " = " + word.getId(), null);
    }

    /**
     * установление текущего типа обучения в комплексном обучении в уроке Lesson
     *
     * @param word            - слово, на котором нужно установить текущий тип обучения
     * @param curLearningType - текущий тип обучения в комплексном обучении: 0-отгадывание из из русс. перевода, 1-отгадывание из англ. перевода, 2-написание англ. слова
     */
    public void setCurLearningType(Word word, int curLearningType) {
        ContentValues cv = new ContentValues();
        cv.put(C_L_LEARNING_TYPE_COPML, curLearningType);
        mDB.update(T_LESSON, cv, C_L_ENGWORD_ID + " = " + word.getId(), null);
    }

    /**
     * установление установление порядкового номера слова в Lesson
     *
     * @param word   - слово, для которого устанавливается порядковый номер (если null, то устанавливается для всех слов урока)
     * @param number - порядковый номер урока (если 0, значит исключается из урока)
     */
    public void setNumberInLesson(Word word, int number) {
        ContentValues cv = new ContentValues();
        cv.put(C_L_ORDINAL, number);
        mDB.update(T_LESSON, cv, word == null ? null : C_L_ENGWORD_ID + " = " + word.getId(), null);
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
    public ArrayList<Category> getCategoryStats() {
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
     * обновление статистики по правильным / неправильным ответам
     *
     * @param word     слово, по которому дан ответ
     * @param category категория
     * @param is_right правильно или нет дан ответ
     * @return кол-во обновленных записей
     */
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
     * удаление всей статистики по отгаданным / неотгаданным словам
     *
     * @return кол-во удаленных строк
     */
    public int removeStats() {
        return mDB.delete(T_STATISTICS, null, null);
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
    public String getValueByVariable(String variable) {
        try {
            Cursor c = mDB.query(T_EXITSTATE, null, C_ES_VARIABLE + "=?",
                    new String[]{variable}, null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    return c.getString(c.getColumnIndex(C_ES_VALUE));
                }
                c.close();
            }
        } catch (Exception ex) {
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