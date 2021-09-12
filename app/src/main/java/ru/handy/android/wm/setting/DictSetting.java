package ru.handy.android.wm.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;

public class DictSetting extends Fragment implements OnClickListener {

    private GlobApp app;
    private View dictTab;
    private ImageButton ibChangeDir;
    private TextView tvFirstDir;
    private TextView tvSecondDir;
    private RadioButton rbSearchRule1;
    private RadioButton rbSearchRule2;
    private CheckBox cbShowHistory;
    private DB db;
    private boolean isEngTransl = true;
    private boolean isSearchRule1 = true;
    private boolean isShowHistory = false;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dictTab = inflater.inflate(R.layout.dict_setting, container, false);

        app = (GlobApp) getActivity().getApplication(); // получаем доступ к приложению
        db = app.getDb(); // открываем подключение к БД

        ibChangeDir = (ImageButton) dictTab.findViewById(R.id.ibChangeDirTrans);
        tvFirstDir = (TextView) dictTab.findViewById(R.id.tvFirstDir);
        tvSecondDir = (TextView) dictTab.findViewById(R.id.tvSecondDir);
        rbSearchRule1 = (RadioButton) dictTab.findViewById(R.id.rbSearchRule1);
        rbSearchRule2 = (RadioButton) dictTab.findViewById(R.id.rbSearchRule2);
        cbShowHistory = (CheckBox) dictTab.findViewById(R.id.cbShowHistory);
        String transDir = db.getValueByVariable(DB.DICT_TRASL_DIRECT);
        if (transDir == null || transDir.equals("0")) {
            tvFirstDir.setText(s(R.string.english));
            tvSecondDir.setText(s(R.string.russian));
            isEngTransl = true;
        } else {
            tvFirstDir.setText(s(R.string.russian));
            tvSecondDir.setText(s(R.string.english));
            isEngTransl = false;
        }
        ibChangeDir.setOnClickListener(this);
        String searchType = db.getValueByVariable(DB.DICT_SEARCH_TYPE);
        isSearchRule1 = (searchType == null || searchType.equals("0"));
        rbSearchRule1.setChecked(isSearchRule1);
        rbSearchRule2.setChecked(!isSearchRule1);
        rbSearchRule1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isSearchRule1 = isChecked;
            db.updateRecExitState(DB.DICT_SEARCH_TYPE, isSearchRule1 ? "0"
                    : "1");
        });
        String showHist = db.getValueByVariable(DB.DICT_SHOW_HISTORY);
        isShowHistory = !(showHist == null || showHist.equals("0"));
        cbShowHistory.setChecked(isShowHistory);
        cbShowHistory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isShowHistory = isChecked;
            db.updateRecExitState(DB.DICT_SHOW_HISTORY, isShowHistory ? "1"
                    : "0");
        });

        return dictTab;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ibChangeDirTrans) {
            isEngTransl = !isEngTransl;
            db.updateRecExitState(DB.DICT_TRASL_DIRECT, isEngTransl ? "0" : "1");
            tvFirstDir.setText(isEngTransl ? s(R.string.english)
                    : s(R.string.russian));
            tvSecondDir.setText(isEngTransl ? s(R.string.russian)
                    : s(R.string.english));
        }
    }

    /**
     * какие слова переводятся: true-английские, false-русские
     *
     * @return boolean
     */
    public boolean isEngTransl() {
        return isEngTransl;
    }

    /**
     * способ поиска слов: true-по начальным буквам слова, false-по буквам с
     * любой части слова
     *
     * @return boolean
     */
    public boolean isSearchRule1() {
        return isSearchRule1;
    }

    /**
     * показывать историю, когда строка поиска пустая: true-да, false-нет
     *
     * @return boolean
     */
    public boolean isShowHistory() {
        return isShowHistory;
    }

    private String s(int res) {
        return getResources().getString(res);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("myLogs", "onDestroy DictSetting");
    }
}
