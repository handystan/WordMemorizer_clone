package ru.handy.android.wm;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import com.google.firebase.analytics.FirebaseAnalytics;

import ru.handy.android.wm.setting.Pay;
import ru.handy.android.wm.setting.Utils;

public class NoAd extends AppCompatActivity implements View.OnClickListener {

    Button bNoAd;
    private GlobApp app;
    // класс для обработки платежей
    private Pay pay;
    private String itemSKU = Pay.ITEM_SKU_249rub_noad;
    // показывает сумму, которую пользователь пожертвовал разработчику
    private int amountDonate = 0;
    private DB db;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_ad);

        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mFBAnalytics = app.getFBAnalytics(); // получение экземпляра FirebaseAnalytics
        if (mFBAnalytics != null) {
            String[] arrClName = this.getClass().toString().split("\\.");
            app.openActEvent(arrClName[arrClName.length - 1]);
        }
        db = app.getDb(); // открываем подключение к БД

        // устанавливаем toolbar и actionbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
        }
        // устанавливаем цвет фона и шрифта для toolbar
        Utils.colorizeToolbar(this, toolbar);
        // устанавливаем цвет стрелки "назад" в toolbar
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        if (upArrow != null && bar != null) {
            upArrow.setColorFilter(Utils.getFontColorToolbar(), PorterDuff.Mode.SRC_ATOP);
            bar.setHomeAsUpIndicator(upArrow);
        }

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        bNoAd = findViewById(R.id.bNoAd);
        bNoAd.setOnClickListener(this);
        pay = app.getPay(app);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        if (item.getItemId() == android.R.id.home) {
            createIntent();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        // обработка нажатия кнопки со снятием рекламы
        if (v.getId() == bNoAd.getId()) {
            int purchaseRes = pay.purchase(this, itemSKU, 1008);
            int i = 0;
            while (purchaseRes != 0) {
                i++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                purchaseRes = pay.purchase(this, itemSKU, 1008);
                if (i == 10) {
                    if (purchaseRes == 7) {
                        Toast.makeText(NoAd.this.getApplicationContext(), "Товар уже приобретен. Повторная покупка не возможна", Toast.LENGTH_LONG).show();
                    } else if (purchaseRes == -1) {
                        Toast.makeText(NoAd.this.getApplicationContext(), "Нет связи с сервисом покупки", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(NoAd.this.getApplicationContext(), "Ошибка покупки товара", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d("myLogs", "onDestroy NoAd");
        super.onDestroy();
    }
}