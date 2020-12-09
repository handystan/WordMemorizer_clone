package ru.handy.android.wm.learning;

import java.util.ArrayList;
import java.util.Random;

import ru.handy.android.wm.DB;

/**
 * @author handy Класс, в котором генерится обрабатывается обучение по
 *         закреплению слов
 */
public class Fixing {

    private DB db;
    private String categories = ""; // массив категорий

    // массив слов, участвующий в обучении
    private ArrayList<Word> lesson = new ArrayList<Word>();
    // массив слов, с неправильными ответами
    private ArrayList<Word> wrongAnswers = new ArrayList<Word>();
    private int cur = 0; // текущая позиция в массиве слов
    private int amountWords = 0; // кол-во слов в уроке
    private int amountWrongWords = 0; // кол-во не отгаданных слов в уроке
    private int amountRightWords = 0; // кол-во отгаданных слов в уроке

    /**
     * @param _db            - база данных
     * @param cat            - категория слов, по которой будет проходить урок
     * @param fromDB         - 1 - подкачиваем из БД, 0 - формируем урок заново
     * @param isOnlyMistakes - показывать только слова с ошибками (true) или все (false)
     */
    public Fixing(DB _db, String cat, int fromDB, boolean isOnlyMistakes) {
        db = _db;
        cur = 0;
        if (fromDB == 0) {
            categories = cat;
            lesson = db.getRandomWords(categories, isOnlyMistakes); // список слов
            wrongAnswers = new ArrayList<Word>();
            amountWords = lesson.size();
            amountWrongWords = 0;
            amountRightWords = 0;
            // записываем слова урока в БД
            db.beginTransaction();
            try {
                db.delAll(DB.T_LESSON);
                for (int i = 0; i < lesson.size(); i++) {
                    db.addRecLesson(lesson.get(i).getId(), lesson.get(i)
                            .getEngWord(), lesson.get(i).getTranscription(), lesson
                            .get(i).getRusTranslate(), categories, null, i + 1);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        } else {
            lesson = db.getLesson();
            if (lesson.size() != 0) {
                wrongAnswers = db.getWrongWords();
                categories = db.getCategoryLesson();
                amountWords = db.getAllWordsInLesson().size();
                amountWrongWords = db.getAllWrongWordsInLesson().size();
                amountRightWords = db.getRightWordsInLesson().size();
            }
        }
    }

    /**
     * @param _db    - база данных
     * @param cat    - категория слов, по которой будет проходить урок
     * @param fromDB - 1 - подкачиваем из БД, 0 - формируем урок заново
     */
    public Fixing(DB _db, String cat, int fromDB) {
        this(_db, cat, fromDB, false);
    }

    public Word getCurWord() {
        if (lesson.size() == 0)
            return null;
        else
            return lesson.get(cur);
    }

    public ArrayList<Word> nextWord(Word selectedWord) {
        return nextWord(selectedWord, false);
    }

    /**
     * получение следующего слова в массиве слов
     *
     * @param selectedWord выбранное слово
     * @param isForceTrue  true - для комплекного обучение нажата кнопка "Уже знаю", т.е. засчитать слово как известное, false - для остальных случаев
     * @return ArrayList<Word> ответ в виде массиве, где 1-ый элемент - верный
     * ответ (если была ошибка) или null (если все верно), 2-ой -
     * следующее слово или null, если список слов в категории
     * закончился,3-ий - информационное сообщение об окончании урока или
     * о промежуточным окончании урока с ошибками, либо null (если это не
     * окончание урока)
     */
    public ArrayList<Word> nextWord(Word selectedWord, boolean isForceTrue) {
        ArrayList<Word> answer = new ArrayList<Word>();
        answer.add(null); // 0 - записывается верное слово (если была ошибка) или null (если все верно)
        answer.add(null); // 1 - следующее слово или null, если список слов в категории закончился
        answer.add(null); // 2 - записываются раличные сообщения или null, если это неокончание урока
        db.beginTransaction();
        try {
            Integer prevRes = db.getResult(lesson.get(cur));
            // сначала определяем отгадано слово или нет в зависимости о типа обучения (отгадывания или написания слова
            String lt = db.getValueByVariable(DB.LEARNING_TYPE);
            int learningType = lt == null ? 0 : Integer.parseInt(lt); // тип обучения (0, 1 или 2)
            String ra = db.getValueByVariable(DB.LEARNING_REPEATS_AMOUNT);
            int repeatsAmountCompl = ra == null ? 2 : Integer.parseInt(ra); // кол-во повторений для комплексного обучения
            int curLearningTypeCompl = db.getCurLearningType(lesson.get(cur)); // текущий тип обучения в комплексном обучении: 0-отгадывание из из русс. перевода, 1-отгадывание из англ. перевода, 2-написание англ. слова
            int curRepeatNumberCompl = db.getCurRepeatNumber(lesson.get(cur)); // номер текущего повторение данного слова
            boolean isRight = (learningType == 0 || (learningType == 2 && curLearningTypeCompl != 2))
                    ? selectedWord.equals(lesson.get(cur)) : isRightAnswer(selectedWord); // отгадано ли слово (пока не обрабатывается 2)
            // если ошибка, то ставится правильное слово
            if (!isRight) {
                if (prevRes == null || prevRes == 1) {
                    amountWrongWords++;
                    db.setResult(lesson.get(cur), 0);
                }
                if ((curRepeatNumberCompl == 0 && curLearningTypeCompl == 0)
                        || ((!(curRepeatNumberCompl == 0) || !(curLearningTypeCompl == 0)) && prevRes == 1)) {
                    wrongAnswers.add(lesson.get(cur));
                }
                if (prevRes != null && prevRes == 1) amountRightWords--;
                answer.set(0, lesson.get(cur));
            } else { // если слово отгадано
                if (prevRes != null && prevRes == 0 && (isForceTrue || curRepeatNumberCompl == 0 && curLearningTypeCompl == 0)) {
                    amountWrongWords--;
                }
                if ((isForceTrue && (prevRes == null || prevRes == 0)) || (curLearningTypeCompl == 0 && curRepeatNumberCompl == 0)) {
                    db.setResult(lesson.get(cur), 1);
                    amountRightWords++;
                }
                if (isForceTrue && wrongAnswers.size() > 0 && selectedWord.equals(wrongAnswers.get(wrongAnswers.size() - 1))) {
                    wrongAnswers.remove(wrongAnswers.size() - 1);
                }
            }
            // если урок еще не окончился
            if (cur != lesson.size() - 1 || learningType == 2 && !(cur == lesson.size() - 1
                    && curRepeatNumberCompl == repeatsAmountCompl - 1 && curLearningTypeCompl == 2)) {
                if (learningType != 2 || (learningType == 2 && curRepeatNumberCompl == repeatsAmountCompl - 1
                        && curLearningTypeCompl == 2)) {
                    db.setNumberInLesson(lesson.get(cur), 0);
                    db.setCurRepeatNumber(lesson.get(cur), 0);
                    db.setCurLearningType(lesson.get(cur), 0);
                    cur++;
                } else {
                    if (curLearningTypeCompl == 2) {
                        db.setCurLearningType(lesson.get(cur), 0);
                        db.setCurRepeatNumber(lesson.get(cur), ++curRepeatNumberCompl);
                    } else {
                        db.setCurLearningType(lesson.get(cur), ++curLearningTypeCompl);
                    }
                }
                // следующее слово (или остается текущее при комплексном обучении и не прохождении всех стадий)
                answer.set(1, lesson.get(cur));
            } else { // окончание урока (с ошибками или без)
                db.setNumberInLesson(null, 0);
                db.setCurRepeatNumber(lesson.get(cur), 0);
                db.setCurLearningType(lesson.get(cur), 0);
                lesson = new ArrayList<Word>();
                if (wrongAnswers.size() == 0) { // окончание урока без ошибок
                    // сообщение
                    answer.set(2, new Word(0, "Урок успешно пройден. Можно выбрать другую категорию.", "", ""));
                    db.delAll(DB.T_LESSON);
                } else { // окончание урока с ошибками
                    Random randGen = new Random();
                    while (wrongAnswers.size() > 0) {
                        int rand = randGen.nextInt(wrongAnswers.size());
                        lesson.add(wrongAnswers.get(rand));
                        db.setNumberInLesson(wrongAnswers.get(rand), lesson.size());
                        wrongAnswers.remove(rand);
                    }
                    wrongAnswers = new ArrayList<Word>();
                    cur = 0;
                    answer.set(1, lesson.get(cur)); // следующее слово
                    // сообщение
                    answer.set(2, new Word(0, lesson.size()
                            + " - количество ошибок. Их нужно пройти еще раз.",
                            "", ""));
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return answer;
    }

    /**
     * для типа обучения "написание слова" определяет, правильно ли написанное слово
     *
     * @param selectedWord - написанное слово
     * @return
     */
    private boolean isRightAnswer(Word selectedWord) {
        boolean isEngFixing = db.getValueByVariable(DB.LEARNING_LANGUAGE).equals("0");
        String lt = db.getValueByVariable(DB.LEARNING_TYPE);
        int learningType = lt == null ? 0 : Integer.parseInt(lt); // тип обучения (0, 1 или 2)
        String ra = db.getValueByVariable(DB.LEARNING_REPEATS_AMOUNT);
        int curLearningTypeCompl = db.getCurLearningType(lesson.get(cur)); // тип обучения для комплексного обучения
        if (learningType == 2 && curLearningTypeCompl == 2) isEngFixing = false;
        String curWord = !isEngFixing ? lesson.get(cur).getEngWord() : lesson.get(cur).getRusTranslate();
        String answerWord = !isEngFixing ? selectedWord.getEngWord() : selectedWord.getRusTranslate();
        String[] arrCurWord = curWord.split("(,|;)");
        ArrayList<String> alCurWord = new ArrayList<>();
        for (int i = 0; i < arrCurWord.length; i++) {
            String item = arrCurWord[i].trim();
            alCurWord.add(item);
            String item2 = item.replaceAll("(\\(|\\))", "");
            if (!item.equals(item2))
                alCurWord.add(item2);
        }
        String[] arrAnswerWord = answerWord.split("(,|;)");
        ArrayList<String> alAnswerWord = new ArrayList<>();
        for (int i = 0; i < arrAnswerWord.length; i++) {
            String item = arrAnswerWord[i].trim();
            alAnswerWord.add(item);
            String item2 = item.replaceAll("(\\(|\\))", "");
            if (!item.equals(item2))
                alAnswerWord.add(item2);
        }
        for (int i = 0; i < alCurWord.size(); i++) {
            for (int j = 0; j < alAnswerWord.size(); j++) {
                if (alCurWord.get(i).equalsIgnoreCase(alAnswerWord.get(j)))
                    return true;
            }
        }
        return false;
    }

    /**
     * получение категории для данного урока
     *
     * @return категория(и)
     */
    public String getCategories() {
        return categories;
    }

    public int getAmountWords() {
        return amountWords;
    }

    public int getAmountWrongWords() {
        return amountWrongWords;
    }

    public int getAmountRightWords() {
        return amountRightWords;
    }

}
