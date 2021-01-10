package ru.handy.android.wm.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;

public class LearningSetting extends Fragment {

    private GlobApp app;
    private Pay pay; // класс для обработки платежей
    private View learningTab;
    private RadioButton rbChoiceLearning;
    private RadioButton rbWritingLearning;
    private RadioButton rbComplexLearning;
    private LinearLayout llRepeatsAmount;
    private Spinner sRepeatsAmount;
    private CheckBox cbSpeak;
    private LinearLayout llEngOrRus;
    private RadioButton rbEng;
    private RadioButton rbRus;
    private CheckBox cbShowTranscr;
    private LinearLayout llAmountWords;
    private Spinner sAmountWords;
    private CheckBox cbShowDontKnow;
    private DB db;
    private int learningType = 0;
    private int repeatsAmount = 2;
    private boolean isSpeak = true; // нужно ли после каждого отгадывания озвучивать английское слово
    private boolean isEngl = true;
    private boolean isShowTranscr = true;
    private int amountWords = 8;
    private boolean isShowDontKnow = true;

    public LearningSetting() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        learningTab = inflater.inflate(R.layout.learning_setting, container,
                false);

        app = (GlobApp) getActivity().getApplication(); // получаем доступ к приложению
        db = app.getDb(); // открываем подключение к БД

        rbChoiceLearning = (RadioButton) learningTab.findViewById(R.id.rbChoiceLearning);
        rbWritingLearning = (RadioButton) learningTab.findViewById(R.id.rbWritingLearning);
        rbComplexLearning = (RadioButton) learningTab.findViewById(R.id.rbComplexLearning);
        llRepeatsAmount = (LinearLayout) learningTab.findViewById(R.id.llRepeatsAmount);
        sRepeatsAmount = (Spinner) learningTab.findViewById(R.id.sRepeatsAmount);
        cbSpeak = (CheckBox) learningTab.findViewById(R.id.cbSpeak);
        llEngOrRus = (LinearLayout) learningTab.findViewById(R.id.llEngOrRus);
        rbEng = (RadioButton) learningTab.findViewById(R.id.rbEng);
        rbRus = (RadioButton) learningTab.findViewById(R.id.rbRus);
        cbShowTranscr = (CheckBox) learningTab.findViewById(R.id.cbShowTranscr);
        llAmountWords = (LinearLayout) learningTab.findViewById(R.id.llAmountWords);
        sAmountWords = (Spinner) learningTab.findViewById(R.id.sAmountWords);
        cbShowDontKnow = (CheckBox) learningTab.findViewById(R.id.cbShowDontKnow);
        // установка типа обучения
        if (db.getValueByVariable(DB.LEARNING_TYPE) == null
                || db.getValueByVariable(DB.LEARNING_TYPE).equals("0")) {
            rbChoiceLearning.setChecked(true);
            learningType = 0;
            llRepeatsAmount.setVisibility(View.GONE);
        } else if (db.getValueByVariable(DB.LEARNING_TYPE).equals("1")) {
            rbWritingLearning.setChecked(true);
            learningType = 1;
            llRepeatsAmount.setVisibility(View.GONE);
            llAmountWords.setVisibility(View.GONE);
        } else {
            rbComplexLearning.setChecked(true);
            learningType = 2;
            llEngOrRus.setVisibility(View.GONE);
        }
        Log.d("myLogs", "learningType in LearningSetting= " + learningType);
        Log.d("myLogs", "learningSetting in LearningSetting= " + this);
        Spannable span = new SpannableString(s(R.string.complex_learning) + s(R.string.complex_description));
        int fontSize = (int) (rbComplexLearning.getTextSize() * 0.65);
        span.setSpan(new AbsoluteSizeSpan(fontSize, false), s(R.string.complex_learning).length()
                , span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        rbComplexLearning.setText(span);

        rbChoiceLearning.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    setLearningType(0);
                }
            }
        });
        rbWritingLearning.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    Utils.mainAlertForPay(DB.DATE_LEARNING_METHOD, LearningSetting.this, pay, db, 1);
                }
            }
        });
        rbComplexLearning.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    Utils.mainAlertForPay(DB.DATE_LEARNING_METHOD, LearningSetting.this, pay, db, 2);
                }
            }
        });
        // раскрывающийся список для кол-ва повторений в комплексном обучении
        Integer[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sRepeatsAmount.setAdapter(adapter);
        repeatsAmount = db.getValueByVariable(DB.LEARNING_REPEATS_AMOUNT) == null ? 2 :
                Integer.parseInt(db.getValueByVariable(DB.LEARNING_REPEATS_AMOUNT));
        sRepeatsAmount.setSelection(repeatsAmount - 1);
        sRepeatsAmount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                repeatsAmount = Integer.parseInt(sRepeatsAmount.getSelectedItem().toString());
                db.updateRecExitState(DB.LEARNING_REPEATS_AMOUNT, String.valueOf(repeatsAmount));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        // нужно после каждого отгадывания озвучивать английское слово
        if (db.getValueByVariable(DB.LEARNING_SPEAK) == null
                || db.getValueByVariable(DB.LEARNING_SPEAK).equals("1")) {
            cbSpeak.setChecked(true);
            isSpeak = true;
        } else {
            cbSpeak.setChecked(false);
            isSpeak = false;
        }
        cbSpeak.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                isSpeak = isChecked;
                db.updateRecExitState(DB.LEARNING_SPEAK,
                        isSpeak ? "1" : "0");
            }
        });
        // установка языка для заучивания
        if (db.getValueByVariable(DB.LEARNING_LANGUAGE) == null
                || db.getValueByVariable(DB.LEARNING_LANGUAGE).equals("0")) {
            rbEng.setChecked(true);
            rbRus.setChecked(false);
            isEngl = true;
        } else {
            rbEng.setChecked(false);
            rbRus.setChecked(true);
            isEngl = false;
        }
        rbEng.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    setEng(true);
                }
            }
        });
        rbRus.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    Utils.mainAlertForPay(DB.DATE_LANGUAGE, LearningSetting.this, pay, db);
                }
            }
        });
        // показывать транскрипцию или нет
        if (db.getValueByVariable(DB.LEARNING_SHOW_TRANSCR) == null
                || db.getValueByVariable(DB.LEARNING_SHOW_TRANSCR).equals("1")) {
            cbShowTranscr.setChecked(true);
            isShowTranscr = true;
        } else {
            cbShowTranscr.setChecked(false);
            isShowTranscr = false;
        }
        cbShowTranscr.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                isShowTranscr = isChecked;
                db.updateRecExitState(DB.LEARNING_SHOW_TRANSCR,
                        isShowTranscr ? "1" : "0");
            }
        });
        // раскрывающийся список для кол-ва слов для выбора
        Integer[] data2 = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        ArrayAdapter<Integer> adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, data2);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sAmountWords.setAdapter(adapter2);
        amountWords = db.getValueByVariable(DB.LEARNING_AMOUNT_WORDS) == null ? 8 :
                Integer.parseInt(db.getValueByVariable(DB.LEARNING_AMOUNT_WORDS));
        sAmountWords.setSelection(amountWords - 2);
        sAmountWords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position + 2) != amountWords) {
                    Utils.mainAlertForPay(DB.DATE_LANG_WORD_AMOUNT, LearningSetting.this, pay, db);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        // показывать кнопку "Не знаю" или нет
        if (db.getValueByVariable(DB.LEARNING_SHOW_DONTKNOW) == null
                || db.getValueByVariable(DB.LEARNING_SHOW_DONTKNOW).equals("1")) {
            cbShowDontKnow.setChecked(true);
            isShowDontKnow = true;
        } else {
            cbShowDontKnow.setChecked(false);
            isShowDontKnow = false;
        }
        cbShowDontKnow.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                isShowDontKnow = isChecked;
                db.updateRecExitState(DB.LEARNING_SHOW_DONTKNOW,
                        isShowDontKnow ? "1" : "0");
            }
        });

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        int amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        String fromOldDB = db.getValueByVariable(DB.OLD_FREE_DB);
        boolean isFromOldDB = !(fromOldDB == null || fromOldDB.equals("0"));
        if (!isFromOldDB && amountDonate == 0) pay = new Pay(getActivity());
        return learningTab;
    }

    /**
     * получение информации о типе обучения
     *
     * @return learningType тип обучения
     */
    public int getLearningType() {
        return learningType;
    }

    /**
     * установление типа обучения
     *
     * @param learningType тип обучения
     * @param isSetRB      нужно ли устанавливать значения на самих radioButton
     */
    public void setLearningType(int learningType, boolean isSetRB) {
        db.updateRecExitState(DB.LEARNING_TYPE, String.valueOf(learningType));
        this.learningType = learningType;
        if (learningType == 0) {
            if (isSetRB) {
                rbChoiceLearning.setChecked(true);
                rbWritingLearning.setChecked(false);
                rbComplexLearning.setChecked(false);
            }
            llRepeatsAmount.setVisibility(View.GONE);
            llEngOrRus.setVisibility(View.VISIBLE);
            llAmountWords.setVisibility(View.VISIBLE);
        } else if (learningType == 1) {
            if (isSetRB) {
                rbChoiceLearning.setChecked(false);
                rbWritingLearning.setChecked(true);
                rbComplexLearning.setChecked(false);
            }
            llRepeatsAmount.setVisibility(View.GONE);
            llEngOrRus.setVisibility(View.VISIBLE);
            llAmountWords.setVisibility(View.GONE);
        } else if (learningType == 2) {
            if (isSetRB) {
                rbChoiceLearning.setChecked(false);
                rbWritingLearning.setChecked(false);
                rbComplexLearning.setChecked(true);
            }
            llRepeatsAmount.setVisibility(View.VISIBLE);
            llEngOrRus.setVisibility(View.GONE);
            llAmountWords.setVisibility(View.VISIBLE);
        }
    }

    /**
     * установление типа обучения
     *
     * @param learningType тип обучения
     */
    public void setLearningType(int learningType) {
        setLearningType(learningType, false);
    }

    /**
     * получение кол-ва повторений для комплексного обучения
     *
     * @return кол-во повторений для комплексного обучения
     */
    public int getRepeatsAmount() {
        return repeatsAmount;
    }

    /**
     * получение информации о том, нужно ли озвучивать слова после отгадывания
     *
     * @return true - нужно озвучивать, false - не нужно озвучивать
     */
    public boolean isSpeakLearning() {
        return isSpeak;
    }

    /**
     * получение информации о том, какие слова учаться: true-английские, false-русские
     *
     * @return true-английские, false-русские
     */
    public boolean isEng() {
        return isEngl;
    }

    /**
     * установка языка для обучения: true-английские, false-русские
     *
     * @param isEngl  true-английские, false-русские
     * @param isSetRB нужно ли устанавливать значения на самих radioButton
     */
    public void setEng(boolean isEngl, boolean isSetRB) {
        if (isSetRB) {
            rbEng.setChecked(isEngl);
            rbRus.setChecked(!isEngl);
        }
        db.updateRecExitState(DB.LEARNING_LANGUAGE, isEngl ? "0" : "1");
        this.isEngl = isEngl;
    }

    /**
     * установка языка для обучения: true-английские, false-русские (перегруженный метод)
     *
     * @param isEngl true-английские, false-русские
     */
    public void setEng(boolean isEngl) {
        setEng(isEngl, false);
    }

    /**
     * получение информации о том, нужно ли показывать транскрипцию в обучалке
     *
     * @return true - можно показывать, false - нельзя
     */
    public boolean isShowTranscr() {
        return isShowTranscr;
    }

    /**
     * получение информации о кол-ве слов используемых при отгадывании
     *
     * @return кол-во слов используемых при отгадывании
     */
    public int getAmountWords() {
        return amountWords;
    }

    /**
     * получение информации о том, нужно ли показывать кнопку "Не знаю" в обучалке
     *
     * @return true - нужно показывать, false - не нужно
     */
    public boolean isShowDontKnow() {
        return isShowDontKnow;
    }

    /**
     * меняет кол-во слов для выбора
     */
    public void setAmountWords() {
        amountWords = Integer.parseInt(sAmountWords.getSelectedItem().toString());
        db.updateRecExitState(DB.LEARNING_AMOUNT_WORDS, String.valueOf(amountWords));
    }

    /**
     * получаем Spinner с кол-вом слов для отгадывания, чтобы извне можно было устанавливать его значение
     *
     * @return sAmountWords
     */
    public Spinner getSAmountWords() {
        return sAmountWords;
    }

    private String s(int res) {
        return getResources().getString(res);
    }

    @Override
    public void onDestroyView() {
        if (pay != null) pay.close();
        super.onDestroyView();
        Log.d("myLogs", "onDestroyView LearningSetting");
    }
}
