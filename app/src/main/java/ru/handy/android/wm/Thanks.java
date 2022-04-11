package ru.handy.android.wm;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

import ru.handy.android.wm.setting.Pay;
import ru.handy.android.wm.setting.Utils;

/**
 * Класс для возможности добровольного взноса или оценки в Google Play
 */
public class Thanks extends AppCompatActivity implements View.OnClickListener {

    Button bDonate;
    TextView tvEvaluate;
    SeekBar sbDonate;
    TextView tv0;
    TextView tv1;
    TextView tv2;
    Button bEvaluate;
    private GlobApp app;
    // класс для обработки платежей
    private Pay pay;
    private String itemSKU = Pay.ITEM_SKU_249rub;
    // показывает сумму, которую пользователь пожертвовал разработчику
    private int amountDonate = 0;
    private DB db;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thanks);

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

        sbDonate = findViewById(R.id.sbDonate);
        tv0 = findViewById(R.id.tv0);
        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        bDonate = findViewById(R.id.bDonate);
        tvEvaluate = findViewById(R.id.tvEvaluate);
        bEvaluate = findViewById(R.id.bEvaluate);
        bEvaluate.setOnClickListener(this);

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        // обрабатываем изменение значения SpinnerBar
        sbDonate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    itemSKU = Pay.ITEM_SKU_249rub;
                    tv0.setTextColor(Color.parseColor("#EC407A"));
                    tv1.setTextColor(Color.parseColor("#76000000"));
                    tv2.setTextColor(Color.parseColor("#76000000"));
                } else if (progress == 1) {
                    itemSKU = Pay.ITEM_SKU_499rub;
                    tv0.setTextColor(Color.parseColor("#76000000"));
                    tv1.setTextColor(Color.parseColor("#EC407A"));
                    tv2.setTextColor(Color.parseColor("#76000000"));
                } else if (progress == 2) {
                    itemSKU = Pay.ITEM_SKU_999rub;
                    tv0.setTextColor(Color.parseColor("#76000000"));
                    tv1.setTextColor(Color.parseColor("#76000000"));
                    tv2.setTextColor(Color.parseColor("#EC407A"));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        bDonate.setOnClickListener(this);
        tvEvaluate.setText(R.string.text_for_evaluate2);
        if (Build.VERSION.SDK_INT < 23) {
            tvEvaluate.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        } else {
            tvEvaluate.setTextAppearance(android.R.style.TextAppearance_Medium);
        }
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
        // обработка нажатия кнопки с добровольным взносом
        if (v.getId() == bDonate.getId()) {
            int purchaseRes = pay.purchase(this, itemSKU, 1001);
            int i = 0;
            while (purchaseRes != 0) {
                i++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                purchaseRes = pay.purchase(this, itemSKU, 1001);
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
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d("myLogs", "onDestroy Thanks");
        super.onDestroy();
    }
}
