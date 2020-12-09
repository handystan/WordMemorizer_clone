package ru.handy.android.wm;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.handy.android.wm.learning.Learning;
import ru.handy.android.wm.setting.Pay;
import ru.handy.android.wm.setting.Utils;

/**
 * Класс для возможности добровольного взноса или оценки в Google Play
 */
public class Thanks extends AppCompatActivity implements View.OnClickListener {

    LinearLayout llDonate;
    Spinner sDonate;
    Button bDonate;
    TextView tvEvaluate;
    Button bEvaluate;
    private GlobApp app;
    // класс для обработки платежей
    private Pay pay;
    private String itemSKU = Pay.ITEM_SKU_2dol;
    // показывает сумму, которую пользователь пожертвовал разработчику
    private int amountDonate = 0;
    private DB db;
    private Tracker mTracker; // трекер для Google analitics, чтобы отслеживать активности пользователей

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thanks);

        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mTracker = app.getDefaultTracker(); // Obtain the shared Tracker instance.
        db = app.getDb(); // открываем подключение к БД

        // устанавливаем toolbar и actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        // устанавливаем цвет фона и шрифта для toolbar
        Utils.colorizeToolbar(this, toolbar);
        // устанавливаем цвет стрелки "назад" в toolbar
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Utils.getFontColorToolbar(), PorterDuff.Mode.SRC_ATOP);
        bar.setHomeAsUpIndicator(upArrow);

        llDonate = (LinearLayout) findViewById(R.id.llDonate);
        sDonate = (Spinner) findViewById(R.id.sDonate);
        bDonate = (Button) findViewById(R.id.bDonate);
        tvEvaluate = (TextView) findViewById(R.id.tvEvaluate);
        bEvaluate = (Button) findViewById(R.id.bEvaluate);
        bEvaluate.setOnClickListener(this);

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        // адаптер для спиннера размера взноса
        String[] spinnerData = {(amountDonate == 0 ? "99 " : "100 ") + s(R.string.rub)
                , "250 " + s(R.string.rub), "500 " + s(R.string.rub)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinnerData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sDonate.setAdapter(adapter);
        sDonate.setSelection(0);
        sDonate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    itemSKU = amountDonate == 0 ? Pay.ITEM_SKU_99rub : Pay.ITEM_SKU_2dol;
                } else if (position == 1) {
                    itemSKU = Pay.ITEM_SKU_5dol;
                } else if (position == 2) {
                    itemSKU = Pay.ITEM_SKU_10dol;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        bDonate.setOnClickListener(this);
        tvEvaluate.setText(R.string.text_for_evaluate2);
        if (Build.VERSION.SDK_INT < 23) {
            tvEvaluate.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        } else {
            tvEvaluate.setTextAppearance(android.R.style.TextAppearance_Medium);
        }
        pay = new Pay(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        switch (item.getItemId()) {
            case android.R.id.home:
                createIntent();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // обработка кнопки назад
    @Override
    public void onBackPressed() {
        createIntent();
        super.onBackPressed();
    }

    private void createIntent() {
        Intent intent = new Intent();
        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        intent.putExtra("amountDonate", amountDonate);
        setResult(RESULT_OK, intent);
    }

    private String s(int res) {
        return getResources().getString(res);
    }

    @Override
    public void onClick(View v) {
        // обработка нажатия кнопки с добровольным взносом
        if (v.getId() == bDonate.getId()) {
            int purchaseRes = pay.purchase(itemSKU, 1001);
            int i = 0;
            while (purchaseRes != 0) {
                i++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                purchaseRes = pay.purchase(itemSKU, 1001);
                if (i == 10) {
                    if (purchaseRes == 7) {
                        Toast.makeText(Thanks.this.getApplicationContext(), "Товар уже приобретен. Повторная покупка не возможна", Toast.LENGTH_LONG).show();
                    } else if (purchaseRes == -1) {
                        Toast.makeText(Thanks.this.getApplicationContext(), "Нет связи с сервисом покупки", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Thanks.this.getApplicationContext(), "Ошибка покупки товара", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
        } else if (v.getId() == bEvaluate.getId()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=ru.handy.android.wm"));
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 1001 - поддержка разработчика из этого Activity
/*        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), s(R.string.thank_you), Toast.LENGTH_LONG).show();
                Log.d("myLogs", "Оплата произведена успешно!");
                amountDonate += itemSKU.equals(Pay.ITEM_SKU_1dol) ? 50 :
                        (itemSKU.equals(Pay.ITEM_SKU_2dol) ? 100 :
                                (itemSKU.equals(Pay.ITEM_SKU_5dol) ? 250 :
                                        (itemSKU.equals(Pay.ITEM_SKU_10dol) ? 500 :
                                                (itemSKU.equals(Pay.ITEM_SKU_99rub) ? 99 : 0))));
                db.updateRecExitState(DB.AMOUNT_DONATE, amountDonate + "");
                Log.d("myLogs", "Всего оплачено пользователем " + db.getValueByVariable(DB.AMOUNT_DONATE));
                // это нужно для того, что иметь возможность делать покупку несколько раз.
                // Но я в итоге от этого отказался, так как в этом случае на сервере не сохраняется информация о покупке
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String purchaseToken = jo.getString("purchaseToken");
                    Toast.makeText(this.getApplicationContext(), "purchaseToken = " + purchaseToken, Toast.LENGTH_LONG).show();
                    pay.consume(itemSKU, purchaseToken);
                } catch (JSONException e) {
                    Log.e("myLogs", "e: " + e);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "consume " + itemSKU + " is failed: " + e, Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }*/
    }

    @Override
    public void onResume() {
        if (mTracker != null) {
            Log.i("myLogs", "Setting screen name: " + this.getLocalClassName());
            mTracker.setScreenName("Activity " + this.getLocalClassName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d("myLogs", "onDestroy Thanks");
        if (pay != null) pay.close();
        super.onDestroy();
    }
}
