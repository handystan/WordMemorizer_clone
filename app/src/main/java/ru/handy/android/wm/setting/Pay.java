package ru.handy.android.wm.setting;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;
import ru.handy.android.wm.downloads.EditData;
import ru.handy.android.wm.learning.Learning;
import ru.handy.android.wm.statistics.Statistics;

/**
 * класс помогающий осуществлять покупки в приложении через billingClient
 * Created by Андрей on 09.02.2016, modified by Андрей on 16.09.20
 */
public class Pay implements PurchasesUpdatedListener {

    // идентификаторы продукта, которые покупается с помощью InAppBilling
    public static final String ITEM_SKU_249rub = "donate_wm249rub";
    public static final String ITEM_SKU_499rub = "donate_wm499rub";
    public static final String ITEM_SKU_999rub = "donate_wm999rub";
    public static final String ITEM_SKU_249rub_noad = "ru.handy.android.wm.249rub";
    /*public static final String ITEM_SKU_249rub = "android.test.purchased";
    public static final String ITEM_SKU_499rub = "android.test.canceled";
    public static final String ITEM_SKU_999rub = "android.test.item_unavailable";
    public static final String ITEM_SKU_249rub_noad = "android.test.purchased";*/
    private BillingClient billingClient;
    private Activity act;
    private List<SkuDetails> skuDetList = new ArrayList<>(); //список с идентификаторами возможных покупок
    private int reqCode; //код запроса при покупке, который помогает определить дальнейшие действия после покупки
    private GlobApp app;
    private int amountDonate = 0; // показывает сумму, которую пользователь пожертвовал разработчику
    private DB db;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    public Pay(GlobApp globApp) {
        app = globApp; // получаем доступ к приложению
        billingClient = BillingClient.newBuilder(app.getApplicationContext()).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i("myLogs", "Billing client successfully set up");
                    mFBAnalytics = app.getFBAnalytics(); // получение экземпляра FirebaseAnalytics
                    db = app.getDb(); // открываем подключение к БД
                    String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
                    amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
                    //заполняем список с идентификаторами возможных покупок
                    List<String> skuList = new ArrayList<>();
                    skuList.add(ITEM_SKU_249rub);
                    skuList.add(ITEM_SKU_499rub);
                    skuList.add(ITEM_SKU_999rub);
                    skuList.add(ITEM_SKU_249rub_noad);
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(),
                            (billingResult1, skuDetailsList) -> skuDetList = skuDetailsList);
                } else {
                    Log.i("myLogs", "Billing client set up with responseCode = " + billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                billingClient = null;
                Log.i("myLogs", "Billing service disconnected");
            }
        });
    }

    /**
     * проведение платежа с consumable и non-consumable Item
     *
     * @param itemSKU     - идентификатор продукта, который оплачивается
     * @param requestCode - идентификатор запроса на оплату
     * @return код ответа: 0 - удачная покупка, остальные ответа - не удачная покупка
     */
    public int purchase(Activity activity, String itemSKU, int requestCode) {
        act = activity;
        if (billingClient == null || !billingClient.isReady()) return -1;
        reqCode = requestCode;
        SkuDetails skuDetails = null; // искомый продукт, который хотят купить
        for (SkuDetails skuDets : skuDetList) {
            if (skuDets.getSku().equals(itemSKU)) {
                skuDetails = skuDets;
                break;
            }
        }
        if (skuDetails == null) return -1;
        BillingFlowParams purchaseParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build();
        return billingClient.launchBillingFlow(act, purchaseParams).getResponseCode();
    }

    /**
     * слушатель, который обрабатывает покупку
     *
     * @param billingResult результат покупки
     * @param purchases     перечень покупок
     */
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                String itemSKU = purchase.getSkus().get(0);
                Toast.makeText(app.getApplicationContext(), s(R.string.thank_you), Toast.LENGTH_LONG).show();
                Log.i("myLogs", "Оплата произведена успешно!");
                final int thisSKU = itemSKU.equals(ITEM_SKU_249rub) ? 249 :
                        (itemSKU.equals(ITEM_SKU_499rub) ? 499 :
                                (itemSKU.equals(ITEM_SKU_999rub) ? 999 :
                                        (itemSKU.equals(ITEM_SKU_249rub_noad) ? 249 : 0)));
                int oldAmountDonate = amountDonate;
                amountDonate += thisSKU;
                db.updateRecExitState(DB.AMOUNT_DONATE, amountDonate + "");
                Log.i("myLogs", "Всего оплачено пользователем " + db.getValueByVariable(DB.AMOUNT_DONATE) + " (пока без подтверждения)");
                String purchaseMotive = "";
                // признаем покупку (иначе через 3 дня пользователю деньги вернуться обратно
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // 1001 - ответ пришел от Thanks
                    // если это первая покупка, то делаем невозобновляемую покупку, а если не первая, то возобновляемую через consume
                    if (reqCode == 1001) {
                        consume(purchase.getSkus().get(0), purchase.getPurchaseToken());
                        purchaseMotive = s(R.string.from_thanks);
                    } else if (reqCode == 1002) { // 1002 - открываем статистику после оплаты (3 - это код для статистики)
                        ((Learning) act).setAmountDonate(amountDonate);
                        LinearLayout llAdMob = ((Learning) act).getLlAdMob();
                        ViewGroup.LayoutParams params = llAdMob.getLayoutParams();
                        params.height = 0;
                        llAdMob.setLayoutParams(params);
                        act.startActivityForResult(new Intent(act, Statistics.class), 3);
                        purchaseMotive = s(R.string.statistics);
                    } else if (reqCode == 1003) { // 1003 - ответ пришел от EditData (возможность загружать из файла не ограниченное число слов)
                        ((EditData) act).setAmountDonate(amountDonate);
                        LinearLayout llPayInformation = ((EditData) act).getLlPayInformation();
                        ViewGroup.LayoutParams params = llPayInformation.getLayoutParams();
                        params.height = 0;
                        llPayInformation.setLayoutParams(params);
                        purchaseMotive = s(R.string.data_from_file);
                    } else if (reqCode == 1004) { // 1004 - ответ пришел от фрагмента LearningSetting (меняется метод обучения)
                        ((Settings) act).getLearningSetting().setLearningType(1, true);
                        purchaseMotive = s(R.string.learning_type);
                    } else if (reqCode == 1005) { // 1005 - ответ пришел от фрагмента LearningSetting (меняется язык обучения)
                        ((Settings) act).getLearningSetting().setEng(false);
                        purchaseMotive = s(R.string.learning_lang);
                    } else if (reqCode == 1006) { // 1006 - ответ пришел от фрагмента LearningSetting (меняется кол-во слов для выбора)
                        ((Settings) act).getLearningSetting().setAmountWords();
                        purchaseMotive = s(R.string.amount_words);
                    } else if (reqCode == 1007) { // 1007 - ответ пришел от фрагмента OtherSetting по смене цвета фона
                        ((Settings) act).getOtherSetting().changeColor(itemSKU);
                        purchaseMotive = s(R.string.bg_color);
                    } else if (reqCode == 1008) { // 1008 - ответ пришел от NoAd
                        consume(purchase.getSkus().get(0), purchase.getPurchaseToken());
                        purchaseMotive = s(R.string.from_noad);
                    }
                    // отправляем в Firebase инфу с настройками по словарю (с 32 версии не актуальный функционал
                    /*if (mFBAnalytics != null) {
                        app.purchaseEvent(purchaseMotive, db.getValueByVariable(DB.DATE_TRIAL_STATS),
                                db.getValueByVariable(DB.DATE_LEARNING_METHOD), db.getValueByVariable(DB.DATE_LANGUAGE),
                                db.getValueByVariable(DB.DATE_LANG_WORD_AMOUNT), db.getValueByVariable(DB.DATE_BG_COLOR));
                    }*/

                    if (!purchase.isAcknowledged()) {
                        AcknowledgePurchaseParams acknowledgePurchaseParams =
                                AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.getPurchaseToken())
                                        .build();
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult1 -> {
                            if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                Log.i("myLogs", "Покупка подтверждена");
                            } else {
                                amountDonate = Math.max(0, amountDonate - thisSKU);
                                db.updateRecExitState(DB.AMOUNT_DONATE, amountDonate + "");
                                Log.w("myLogs", "Внимание! Покупка не подтверждена!");
                                Log.w("myLogs", "Всего оплачено пользователем " + db.getValueByVariable(DB.AMOUNT_DONATE) + " (после не подтверждения)");
                            }
                        });
                    }
                }
            }
        } else {
            Log.i("myLogs", "Покупка не прошла. BillingResponseCode = " + billingResult.getResponseCode());
            if (reqCode == 1004) { // 1004 - ответ пришел от фрагмента LearningSetting (меняется метод обучения)
                ((Settings) act).getLearningSetting().setLearningType(0, true);
            } else if (reqCode == 1005) { // 1005 - ответ пришел от фрагмента LearningSetting (меняется язык обучения)
                ((Settings) act).getLearningSetting().setEng(true, true);
            } else if (reqCode == 1006) { // 1006 - ответ пришел от фрагмента LearningSetting (меняется кол-во слов для выбора)
                int amountWords = db.getValueByVariable(DB.LEARNING_AMOUNT_WORDS) == null ? 8 :
                        Integer.parseInt(db.getValueByVariable(DB.LEARNING_AMOUNT_WORDS));
                ((Settings) act).getLearningSetting().getSAmountWords().setSelection(amountWords - 2);
            }
        }
    }

    /**
     * есть ли сохраненная информация в Google Play о покупаках данного клиента
     *
     * @return оплаченная сумма, сохраненная в Google Play. -1 означает сервисы не готовы
     */
    public int amountOfPurchased() {
        int amountDonate = 0;
        if (billingClient == null || !billingClient.isReady()) return -1;
        try {
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
            for (Purchase purchase : purchasesResult.getPurchasesList()) {
                String sku = purchase.getSkus().get(0);
                if (sku.equals(ITEM_SKU_249rub)) amountDonate += 249;
                if (sku.equals(ITEM_SKU_499rub)) amountDonate += 499;
                if (sku.equals(ITEM_SKU_999rub)) amountDonate += 999;
                if (sku.equals(ITEM_SKU_249rub_noad)) amountDonate += 249;
            }
        } catch (final Exception e) {
            Log.e("myLogs", "e = " + e.getMessage());
            return -1;
        }
        return amountDonate;
    }

    /**
     * делаем продукт доступным для многоразовой оплаты.
     *
     * @param itemSKU       - идентификатор продукта, который оплачен
     * @param purchaseToken - идентификатор покупки
     */
    public void consume(final String itemSKU, String purchaseToken) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build();
        billingClient.consumeAsync(consumeParams, (billingResult, purchaseToken1) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.i("myLogs", "Продукт " + itemSKU + " оплачен и снова доступен для покупки");
            } else {
                int thisSKU = itemSKU.equals(ITEM_SKU_249rub) ? 249 :
                        (itemSKU.equals(ITEM_SKU_499rub) ? 499 :
                                (itemSKU.equals(ITEM_SKU_999rub) ? 999 :
                                        (itemSKU.equals(ITEM_SKU_249rub_noad) ? 249 : 0)));
                amountDonate = Math.max(0, amountDonate - thisSKU);
                db.updateRecExitState(DB.AMOUNT_DONATE, amountDonate + "");
                Log.w("myLogs", "Внимание! Не сработал метод Consume (подтвеждение и возможность повторного перевода)!");
                Log.w("myLogs", "Всего оплачено пользователем " + db.getValueByVariable(DB.AMOUNT_DONATE) + " (после не подтверждения)");
            }
        });
    }

    private String s(int res) {
        return app.getResources().getString(res);
    }

    public void close() {
        if (billingClient != null) {
            billingClient = null;
        }
    }
}
