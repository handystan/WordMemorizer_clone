package ru.handy.android.wm.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Objects;

import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;

/**
 * класс с прочими настройками в частности со сменой фона
 * Created by Андрей on 24.01.2016.
 */
public class OtherSetting extends Fragment implements OnClickListener {

    private GlobApp app;
    //private Pay pay; // класс для обработки платежей
    private FragmentActivity act;
    private View otherTab;
    private RadioButton rbUS;
    private RadioButton rbUK;
    private ArrayList<Button> buttons = new ArrayList<>();
    private int bgColor = 1;
    private int pronunc = 0; // 0-американский, 1-британский
    private DB db;
    // показывает сумму, которую пользователь пожертвовал разработчику
    private int amountDonate = 0;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        otherTab = inflater.inflate(R.layout.other_setting, container, false);

        act = getActivity();
        app = (GlobApp) act.getApplication(); // получаем доступ к приложению
        db = app.getDb(); // открываем подключение к БД

        buttons = new ArrayList<>();
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor1));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor2));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor3));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor4));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor5));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor6));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor7));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor8));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor9));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor10));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor11));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor12));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor13));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor14));
        buttons.add((Button) otherTab.findViewById(R.id.bBGColor15));
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setOnClickListener(this);
        }
        String strColor = db.getValueByVariable(DB.BG_COLOR);
        bgColor = strColor == null ? 1 : Integer.parseInt(strColor);
        buttons.get(bgColor - 1).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_check_on_disable, 0, 0);
        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        //if (amountDonate == 0) pay = app.getPay(app);
        rbUS = (RadioButton) otherTab.findViewById(R.id.rbUS);
        rbUK = (RadioButton) otherTab.findViewById(R.id.rbUK);
        String pronun = db.getValueByVariable(DB.PRONUNCIATION_USUK);
        pronunc = (pronun == null || pronun.equals("0") ? 0 : 1);
        rbUS.setChecked(pronunc == 0);
        rbUK.setChecked(pronunc != 0);
        rbUS.setOnCheckedChangeListener((buttonView, isChecked) -> {
            pronunc = isChecked ? 0 : 1;
            db.updateRecExitState(DB.PRONUNCIATION_USUK, pronunc + "");
            app.shutdownTTS();
            new Thread(() -> app.speak("")).start();

        });
        Log.d("myLogs", "onCreateView OtherSetting");

        return otherTab;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.bBGColor1:
                bgColor = 1;
                break;
            case R.id.bBGColor2:
                bgColor = 2;
                break;
            case R.id.bBGColor3:
                bgColor = 3;
                break;
            case R.id.bBGColor4:
                bgColor = 4;
                break;
            case R.id.bBGColor5:
                bgColor = 5;
                break;
            case R.id.bBGColor6:
                bgColor = 6;
                break;
            case R.id.bBGColor7:
                bgColor = 7;
                break;
            case R.id.bBGColor8:
                bgColor = 8;
                break;
            case R.id.bBGColor9:
                bgColor = 9;
                break;
            case R.id.bBGColor10:
                bgColor = 10;
                break;
            case R.id.bBGColor11:
                bgColor = 11;
                break;
            case R.id.bBGColor12:
                bgColor = 12;
                break;
            case R.id.bBGColor13:
                bgColor = 13;
                break;
            case R.id.bBGColor14:
                bgColor = 14;
                break;
            case R.id.bBGColor15:
                bgColor = 15;
                break;
        }
        changeColor("");
        // убираем из платных функций изменение метода обучения
        //Utils.mainAlertForPay(DB.DATE_BG_COLOR, this, pay, db);
    }

    /**
     * замена цвета фона
     *
     * @param itemSKU идентификатор продукта
     */
    public void changeColor(String itemSKU) {
        try {
            Utils.changeToTheme((AppCompatActivity) act, bgColor);
            for (int i = 0; i < buttons.size(); i++) {
                buttons.get(i).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            buttons.get(bgColor - 1).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_check_on_disable, 0, 0);
            db.updateRecExitState(DB.BG_COLOR, bgColor + "");
            if (!itemSKU.equals("")) {
                amountDonate += itemSKU.equals(Pay.ITEM_SKU_249rub) ? 249 :
                        (itemSKU.equals(Pay.ITEM_SKU_499rub) ? 499 :
                                (itemSKU.equals(Pay.ITEM_SKU_999rub) ? 999 :
                                        (itemSKU.equals(Pay.ITEM_SKU_249rub_noad) ? 249 : 0)));
                db.updateRecExitState(DB.AMOUNT_DONATE, amountDonate + "");
                Log.d("myLogs", "Всего оплачено пользователем " + db.getValueByVariable(DB.AMOUNT_DONATE));
            }
        } catch (Exception e) {
            Log.e("myLogs", Objects.requireNonNull(e.getMessage()));
        }
    }

    /**
     * @return порядковый номер выбранного цвета
     */
    public int getBGColor() {
        return bgColor;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("myLogs", "onDestroyView OtherSetting");
    }
}
