package ru.handy.android.wm.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;
import ru.handy.android.wm.downloads.EditData;
import ru.handy.android.wm.learning.Learning;
import ru.handy.android.wm.statistics.Statistics;

/**
 * вспомогательный класс для изменения стилей
 * Created by Андрей on 27.01.2016.
 */
public class Utils {

    public final static int THEME_1 = 1;
    public final static int THEME_2 = 2;
    public final static int THEME_3 = 3;
    public final static int THEME_4 = 4;
    public final static int THEME_5 = 5;
    public final static int THEME_6 = 6;
    public final static int THEME_7 = 7;
    public final static int THEME_8 = 8;
    public final static int THEME_9 = 9;
    public final static int THEME_10 = 10;
    public final static int THEME_11 = 11;
    public final static int THEME_12 = 12;
    public final static int THEME_13 = 13;
    public final static int THEME_14 = 14;
    public final static int THEME_15 = 15;
    private static int mainColor; // цвет фона для приложения
    private static int toolbarIconsColor; // цвет фона для иконок toolbar
    private static int fontColorToolbar; // цвет шрифта в toolbar
    private static int colorForIcon; // цвет для иконки Статистики
    private static int fabColor; // цвет для FloatingActionButton
    private static int sTheme = 100;
    private static boolean fadeAnim = false; // показывает, нужно ли делать плавный переход activity

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(AppCompatActivity act, int theme) {
        fadeAnim = true;
        sTheme = theme;
        act.finish();
        Intent intent = new Intent(act, act.getClass());
        intent.putExtra("idsetting", 2);
        act.startActivity(intent);
    }

    /**
     * Set the theme of the activity, according to the configuration.
     */
    public static void onActivityCreateSetTheme(AppCompatActivity act) {
        DB db;
        GlobApp app = (GlobApp) act.getApplication(); // получаем доступ к приложению
        db = app.getDb(); // открываем подключение к БД
        if (sTheme == 100) {
            String strColor = db.getValueByVariable(DB.BG_COLOR);
            sTheme = strColor == null ? 1 : Integer.parseInt(strColor);
        }
        //проверяем не прошел ли бесплатный 7 дневный период по типу обучения. Если да, то возвращаем в базовый тип обучения
        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        int amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        //  с 32 версии не актуальный функционал
        /*String strStartDate = db.getValueByVariable(DB.DATE_BG_COLOR);
        Date startDate = strStartDate == null || strStartDate.equals("") ? null : Date.valueOf(strStartDate);
        if (amountDonate == 0 && startDate != null && sTheme != 1) {
            long dif = (System.currentTimeMillis() - startDate.getTime()) / (1000 * 60 * 60 * 24);
            Log.d("myLogs", "dif = " + dif);
            if (dif > 7) { // и если закончились бесплатные 7 дней
                db.updateRecExitState(DB.BG_COLOR, "1");
                sTheme = 1;
            }
        }*/
        switch (sTheme) {
            default:
            case THEME_1:
                act.setTheme(fadeAnim ? R.style.AppThemeBase1_AppThemeAnim1 : R.style.AppThemeBase1);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary1);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark1);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar1);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon1);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground1);
                break;
            case THEME_2:
                act.setTheme(fadeAnim ? R.style.AppThemeBase2_AppThemeAnim2 : R.style.AppThemeBase2);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary2);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark2);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar2);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon2);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground2);
                break;
            case THEME_3:
                act.setTheme(fadeAnim ? R.style.AppThemeBase3_AppThemeAnim3 : R.style.AppThemeBase3);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary3);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark3);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar3);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon3);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground3);
                break;
            case THEME_4:
                act.setTheme(fadeAnim ? R.style.AppThemeBase4_AppThemeAnim4 : R.style.AppThemeBase4);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary4);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark4);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar4);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon4);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground4);
                break;
            case THEME_5:
                act.setTheme(fadeAnim ? R.style.AppThemeBase5_AppThemeAnim5 : R.style.AppThemeBase5);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary5);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark5);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar5);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon5);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground5);
                break;
            case THEME_6:
                act.setTheme(fadeAnim ? R.style.AppThemeBase6_AppThemeAnim6 : R.style.AppThemeBase6);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary6);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark6);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar6);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon6);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground6);
                break;
            case THEME_7:
                act.setTheme(fadeAnim ? R.style.AppThemeBase7_AppThemeAnim7 : R.style.AppThemeBase7);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary7);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark7);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar7);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon7);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground7);
                break;
            case THEME_8:
                act.setTheme(fadeAnim ? R.style.AppThemeBase8_AppThemeAnim8 : R.style.AppThemeBase8);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary8);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark8);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar8);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon8);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground8);
                break;
            case THEME_9:
                act.setTheme(fadeAnim ? R.style.AppThemeBase9_AppThemeAnim9 : R.style.AppThemeBase9);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary9);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark9);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar9);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon9);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground9);
                break;
            case THEME_10:
                act.setTheme(fadeAnim ? R.style.AppThemeBase10_AppThemeAnim10 : R.style.AppThemeBase10);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary10);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark10);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar10);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon10);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground10);
                break;
            case THEME_11:
                act.setTheme(fadeAnim ? R.style.AppThemeBase11_AppThemeAnim11 : R.style.AppThemeBase11);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary11);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark11);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar11);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon11);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground11);
                break;
            case THEME_12:
                act.setTheme(fadeAnim ? R.style.AppThemeBase12_AppThemeAnim12 : R.style.AppThemeBase12);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary12);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark12);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar12);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon12);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground12);
                break;
            case THEME_13:
                act.setTheme(fadeAnim ? R.style.AppThemeBase13_AppThemeAnim13 : R.style.AppThemeBase13);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary13);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark13);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar13);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon13);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground13);
                break;
            case THEME_14:
                act.setTheme(fadeAnim ? R.style.AppThemeBase14_AppThemeAnim14 : R.style.AppThemeBase14);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary14);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark14);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar14);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon14);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground14);
                break;
            case THEME_15:
                act.setTheme(fadeAnim ? R.style.AppThemeBase15_AppThemeAnim15 : R.style.AppThemeBase15);
                mainColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimary15);
                toolbarIconsColor = ContextCompat.getColor(act.getBaseContext(), R.color.colorPrimaryDark15);
                fontColorToolbar = ContextCompat.getColor(act.getBaseContext(), R.color.fontColorToolbar15);
                colorForIcon = ContextCompat.getColor(act.getBaseContext(), R.color.colorForIcon15);
                fabColor = ContextCompat.getColor(act.getBaseContext(), R.color.fabBackground15);
                break;
        }
        fadeAnim = false;
    }

    /**
     * изменение цвета фона toolbar
     *
     * @param act         активити
     * @param toolbarView бар
     */
    public static void colorizeToolbar(AppCompatActivity act, Toolbar toolbarView) {
        if (sTheme == 100) {
            DB db;
            GlobApp app = (GlobApp) act.getApplication(); // получаем доступ к приложению
            db = app.getDb(); // открываем подключение к БД
            String strColor = db.getValueByVariable(DB.BG_COLOR);
            sTheme = strColor == null ? 1 : Integer.parseInt(strColor);
        }
        toolbarView.setBackgroundColor(toolbarIconsColor); // цвет фона
        toolbarView.setTitleTextColor(fontColorToolbar); // цвет шрифта
        for (int i = 0; i < toolbarView.getChildCount(); i++) {
            final View v = toolbarView.getChildAt(i);
            v.setBackgroundColor(toolbarIconsColor);
            //Step 1 : Changing the color of back button (or open drawer button).
            if (v instanceof ImageButton) {
                //Action Bar back button
                v.setBackgroundColor(toolbarIconsColor);
            }
            //Step 2: Changing the color of title and subtitle.
            if (v instanceof TextView) {
                v.setBackgroundColor(toolbarIconsColor);
            }
            if (v instanceof ActionMenuView) {
                for (int j = 0; j < ((ActionMenuView) v).getChildCount(); j++) {
                    //Step 3: Changing the color of any ActionMenuViews - icons that
                    //are not back button, nor text, nor overflow menu icon.
                    v.setBackgroundColor(toolbarIconsColor);
                    final View innerView = ((ActionMenuView) v).getChildAt(j);
                    if (innerView instanceof ActionMenuItemView) {
                        innerView.setBackgroundColor(toolbarIconsColor);
                    }
                }
            }
            //Step 4: Changing the color of the Overflow Menu icon.
            setOverflowButtonColor(act, toolbarIconsColor);
        }
    }

    /**
     * It's important to set overflowDescription atribute in styles, so we can grab the reference
     * to the overflow icon. Check: res/values/styles.xml
     *
     * @param act активити
     */
    private static void setOverflowButtonColor(final AppCompatActivity act, final int toolbarIconsColor) {
        final String overflowDescription = act.getString(R.string.accessibility_overflow);
        final ViewGroup decorView = (ViewGroup) act.getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<>();
                decorView.findViewsWithText(outViews, overflowDescription,
                        View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (outViews.isEmpty()) {
                    return;
                }
                Drawable dr = ((AppCompatImageView) outViews.get(0)).getDrawable();
                dr.setColorFilter(colorForIcon, PorterDuff.Mode.SRC_ATOP);
                ActionMenuView overflowViewParent = (ActionMenuView) outViews.get(0).getParent();
                overflowViewParent.setBackgroundColor(toolbarIconsColor);
                // удаляем слушатель
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    decorView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    decorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    /**
     * получение текущего цвета фона
     *
     * @return возвращает текущий цвет
     */
    public static int getTheme() {
        return sTheme;
    }

    /**
     * получение цвета фона для приложения
     *
     * @return цвет фона
     */
    public static int getMainColor() {
        return mainColor;
    }

    /**
     * получение цвета шрифта для toolbar
     *
     * @return цвет шрифта для toolbar
     */
    public static int getFontColorToolbar() {
        return fontColorToolbar;
    }

    /**
     * получение цвета для иконки со Статистикой
     *
     * @return цвет для иконки со Статистикой
     */
    public static int getColorForIcon() {
        return colorForIcon;
    }

    /**
     * получение цвета фона для FloatingActionButton
     *
     * @return цвет фона для FloatingActionButton
     */
    public static int getFabColor() {
        return fabColor;
    }

    /**
     * главный метод, в котором вызываются диалоговые окно, оповещающее о платных функциях (уже не актуально, платные функции убраны)
     *
     * @param kindOfSetting вид настройки, у которой сохраняется дата начала бесплатного периода (если пусто, то значит у этой настройки не сохраняется дата начала):
     *                      DATE_TRIAL_STATS, DATE_BG_COLOR, DATE_LEARNING_METHOD, DATE_LANGUAGE, DATE_LANG_WORD_AMOUNT
     * @param act           activity или fragmentActivity, из которого запускается диалоговое окно
     * @param pay           класс, через который идут платежи
     * @param db            ссылка на класс с базой данных
     * @param learningType  если меняется метод обучения, то нужно указать, какой именно метод устанавливается
     */
    public static void mainAlertForPay(final String kindOfSetting, final Object act, final Pay pay, final DB db, final int learningType) {
        final FragmentActivity fragAct;
        if (act instanceof Learning || act instanceof EditData) {
            fragAct = (FragmentActivity) act;
        } else if (act instanceof OtherSetting) {
            fragAct = ((OtherSetting) act).getActivity();
        } else if (act instanceof LearningSetting) {
            fragAct = ((LearningSetting) act).getActivity();
        } else {
            fragAct = null;
        }
        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        int amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        String strStartDate = db.getValueByVariable(kindOfSetting);
        final Date startDate = strStartDate == null || strStartDate.equals("") ? null : Date.valueOf(strStartDate);
        Log.d("myLogs", "amountDonate = " + amountDonate);
        Log.d("myLogs", "startDate = " + startDate);
        if (amountDonate == 0 && startDate == null) { //если приложение еще не оплачено и еще не начался бесплатный месяц
            Utils.alertForPay(kindOfSetting, act, pay, db, true, learningType);
            return;
        } else if (amountDonate == 0 && startDate != null) { // если приложение не оплачены и стоит дата начала тестового периода
            long dif = (System.currentTimeMillis() - startDate.getTime()) / (1000 * 60 * 60 * 24);
            Log.d("myLogs", "dif = " + dif);
            if (dif > 7) { // и если закончились бесплатные 7 дней
                Utils.alertForPay(kindOfSetting, act, pay, db, false);
                return;
            }
        }
        // с 32 версии не актуальный функционал и вся эта функция
        /*if (kindOfSetting.equals(DB.DATE_TRIAL_STATS)) {
            // 3 означает класс Statistics
            fragAct.startActivityForResult(new Intent(fragAct, Statistics.class), 3); // это уже не актуально, так как это убрал из платных функций
        } else if (kindOfSetting.equals(DB.DATE_BG_COLOR)) {
            ((OtherSetting) act).changeColor(""); // это уже не актуально, так как это убрал из платных функций
        } else if (kindOfSetting.equals(DB.DATE_LEARNING_METHOD)) {
            ((LearningSetting) act).setLearningType(learningType); // это уже не актуально, так как это убрал из платных функций
        } else if (kindOfSetting.equals(DB.DATE_LANGUAGE)) {
            ((LearningSetting) act).setEng(false); // это уже не актуально, так как это убрал из платных функций
        } else if (kindOfSetting.equals(DB.DATE_LANG_WORD_AMOUNT)) {
            ((LearningSetting) act).setAmountWords(); // это уже не актуально, так как это убрал из платных функций
        }*/
    }

    /**
     * главный метод, в котором вызываются диалоговые окно, оповещающее о платных функциях (перегруженный метод) (уже не актуально, платные функции убраны)
     *
     * @param kindOfSetting вид настройки, у которой сохраняется дата начала бесплатного периода (если пусто, то значит у этой настройки не сохраняется дата начала):
     *                      DATE_TRIAL_STATS, DATE_BG_COLOR, DATE_LEARNING_METHOD, DATE_LANGUAGE, DATE_LANG_WORD_AMOUNT
     * @param act           activity или fragmentActivity, из которого запускается диалоговое окно
     * @param pay           класс, через который идут платежи
     * @param db            ссылка на класс с базой данных
     */
    public static void mainAlertForPay(final String kindOfSetting, final Object act, final Pay pay, final DB db) {
        mainAlertForPay(kindOfSetting, act, pay, db, 0);
    }

    /**
     * универсальное диалоговое окно, оповещающее о платных функциях (уже не актуально, платные функции убраны)
     *
     * @param kindOfSetting      вид настройки, у которой сохраняется дата начала бесплатного периода (если пусто, то значит у этой настройки не сохраняется дата начала
     *                           DATE_TRIAL_STATS, DATE_BG_COLOR, DATE_LEARNING_METHOD, DATE_LANGUAGE, DATE_LANG_WORD_AMOUNT
     * @param act                activity или fragmentActivity, из которого запускается диалоговое окно
     * @param pay                класс, через который идут платежи
     * @param db                 ссылка на класс с базой данных
     * @param isButtonFreePeriod показывать ли кнопку с 7 дневным бесплатным периодом
     * @param learningType       если меняется метод обучения, то нужно указать, какой именно метод устанавливается
     */
    public static void alertForPay(final String kindOfSetting, final Object act, final Pay pay, final DB db, final boolean isButtonFreePeriod, final int learningType) {
        /*final FragmentActivity fragAct;
        if (act instanceof Learning || act instanceof EditData) {
            fragAct = (FragmentActivity) act;
        } else if (act instanceof OtherSetting) {
            fragAct = ((OtherSetting) act).getActivity();
        } else if (act instanceof LearningSetting) {
            fragAct = ((LearningSetting) act).getActivity();
        } else {
            fragAct = null;
        }
        final int reqCode = kindOfSetting.equals(DB.DATE_TRIAL_STATS) ? 1002 : (kindOfSetting.equals("") ? 1003 :
                (kindOfSetting.equals(DB.DATE_LEARNING_METHOD) ? 1004 :
                        (kindOfSetting.equals(DB.DATE_LANGUAGE) ? 1005 :
                                (kindOfSetting.equals(DB.DATE_LANG_WORD_AMOUNT) ? 1006 :
                                        (kindOfSetting.equals(DB.DATE_BG_COLOR) ? 1007 : 9999)))));
        AlertDialog.Builder builder = new AlertDialog.Builder(fragAct);
        LayoutInflater inflater = fragAct.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_pay, null));
        builder.setPositiveButton(R.string.buy, (dialog, which) -> {
            int purchaseRes = pay.purchase(fragAct, Pay.ITEM_SKU_249rub_noad, reqCode);
            int i = 0;
            while (purchaseRes != 0) {
                i++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                purchaseRes = pay.purchase(fragAct, Pay.ITEM_SKU_249rub_noad, reqCode);
                if (i == 10) {
                    if (purchaseRes == 7) {
                        Toast.makeText(fragAct, R.string.aready_purchased, Toast.LENGTH_LONG).show();
                    } else if (purchaseRes == -1) {
                        Toast.makeText(fragAct, R.string.pay_service_disconnected, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(fragAct, R.string.purchase_error, Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
        });
        if (isButtonFreePeriod && !kindOfSetting.equals("")) {
            builder.setNeutralButton(R.string.try_7day_free, (dialog, which) -> {
                db.updateRecExitState(kindOfSetting, new Date(System.currentTimeMillis()).toString());
                if (kindOfSetting.equals(DB.DATE_TRIAL_STATS)) {
                    fragAct.startActivityForResult(new Intent(fragAct, Statistics.class), 3);
                } else if (kindOfSetting.equals(DB.DATE_BG_COLOR)) {
                    ((OtherSetting) act).changeColor("");
                } else if (kindOfSetting.equals(DB.DATE_LEARNING_METHOD)) {
                    ((LearningSetting) act).setLearningType(learningType);
                } else if (kindOfSetting.equals(DB.DATE_LANGUAGE)) {
                    ((LearningSetting) act).setEng(false);
                } else if (kindOfSetting.equals(DB.DATE_LANG_WORD_AMOUNT)) {
                    ((LearningSetting) act).setAmountWords();
                }
            });
        }
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (kindOfSetting.equals(DB.DATE_LEARNING_METHOD)) {
                    ((LearningSetting) act).setLearningType(0, true);
                } else if (kindOfSetting.equals(DB.DATE_LANGUAGE)) {
                    ((LearningSetting) act).setEng(true, true);
                } else if (kindOfSetting.equals(DB.DATE_LANG_WORD_AMOUNT)) {
                    int amountWords = db.getValueByVariable(DB.LEARNING_AMOUNT_WORDS) == null ? 8 :
                            Integer.parseInt(db.getValueByVariable(DB.LEARNING_AMOUNT_WORDS));
                    ((LearningSetting) act).getSAmountWords().setSelection(amountWords - 2);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();*/
    }

    /**
     * универсальное диалоговое окно, оповещающее о платных функциях (перегруженная функция)  (уже не актуально, платные функции убраны)
     *
     * @param kindOfSetting      вид настройки, у которой сохраняется дата начала бесплатного периода (если пусто, то значит у этой настройки не сохраняется дата начала
     *                           DATE_TRIAL_STATS, DATE_BG_COLOR, DATE_LEARNING_METHOD, DATE_LANGUAGE, DATE_LANG_WORD_AMOUNT
     * @param act                activity или fragmentActivity, из которого запускается диалоговое окно
     * @param pay                класс, через который идут платежи
     * @param db                 ссылка на класс с базой данных
     * @param isButtonFreePeriod показывать ли кнопку с 7 дневным бесплатным периодом
     */
    public static void alertForPay(final String kindOfSetting, final Object act, final Pay pay, final DB db, final boolean isButtonFreePeriod) {
        Utils.alertForPay(kindOfSetting, act, pay, db, isButtonFreePeriod, 0);
    }

    /**
     * разделяет сроку на элементы на основе данного разделителя
     *
     * @param str         строка, котороая преобразуется в ArrayList
     * @param delimiter   разделитель, по которому строка делится на элементы
     * @param deleteBlank удалять пробелы в начале и конце каждого элемента или нет
     * @return список List
     */
    public static List<String> strToList(String str, String delimiter, boolean deleteBlank) {
        String[] arr = str.split(",");
        List<String> list = new ArrayList<>(); //список со всеми категориями в данном слове
        for (String s : arr) {
            list.add(deleteBlank ? s.trim() : s);
        }
        return list;
    }

    /**
     * объединяет список в одну строку на основе разделителя
     *
     * @param list      список, который преобразуется в строку я использованием разделителя
     * @param delimiter разделитель, по которому строка делится на элементы
     * @return строка
     */
    public static String listToStr(List<String> list, String delimiter) {
        String str = "";
        for (int i = 0; i < list.size(); i++) {
            str = str + list.get(i) + ((i == list.size() - 1) ? "" : ", ");
        }
        return str;
    }

}