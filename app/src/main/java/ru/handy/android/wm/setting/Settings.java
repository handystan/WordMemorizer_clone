package ru.handy.android.wm.setting;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yodo1.mas.banner.Yodo1MasBannerAdView;

import java.util.ArrayList;
import java.util.List;

import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;

public class Settings extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private GlobApp app;
    private DictSetting dictSetting;
    private LearningSetting learningSetting;
    private OtherSetting otherSetting;
    private LinearLayout llAdMobSettings;
    private Yodo1MasBannerAdView avBottomBannerSettings;
    private DB db;
    private ActionBar bar;
    // что было выбрано при вызове настроек: 0-обучение, 1-словарь, 2- прочее
    private int idSetting;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_md);
        Intent intent = getIntent();
        idSetting = intent.getIntExtra("idsetting", 0);

        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mFBAnalytics = app.getFBAnalytics(); // получение экземпляра FirebaseAnalytics
        if (mFBAnalytics != null) {
            String[] arrClName = this.getClass().toString().split("\\.");
            app.openActEvent(arrClName[arrClName.length - 1]);
        }
        db = app.getDb(); // открываем подключение к БД

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        int amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        llAdMobSettings = findViewById(R.id.llAdMobSettings);
        avBottomBannerSettings = findViewById(R.id.avBottomBannerSettings);
        // загружаем баннерную рекламу yodo1
        avBottomBannerSettings.loadAd();
        ViewGroup.LayoutParams params = llAdMobSettings.getLayoutParams();
        if (amountDonate > 0) {
            params.height = 0;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName() + " без отображения");
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName());
        }
        llAdMobSettings.setLayoutParams(params);

        // устанавливаем toolbar и actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bar = getSupportActionBar();
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

        viewPager = findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if (savedInstanceState != null) {
            learningSetting = (LearningSetting) getSupportFragmentManager().getFragment(savedInstanceState, "learningSetting");
            dictSetting = (DictSetting) getSupportFragmentManager().getFragment(savedInstanceState, "dictSetting");
            otherSetting = (OtherSetting) getSupportFragmentManager().getFragment(savedInstanceState, "otherSetting");
        } else {
            learningSetting = new LearningSetting();
            dictSetting = new DictSetting();
            otherSetting = new OtherSetting();
        }
        adapter.addFragment(learningSetting, s(R.string.learning));
        adapter.addFragment(dictSetting, s(R.string.dictionary));
        adapter.addFragment(otherSetting, s(R.string.other));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(idSetting);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        Log.d("myLogs", "onCreate Setting");
    }

    /**
     * метод для сохранения состояния при повороте экрана
     *
     * @param outState ориентация экрана
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            getSupportFragmentManager().putFragment(outState, "learningSetting", learningSetting);
        } catch (Exception e) {
        }
        try {
            getSupportFragmentManager().putFragment(outState, "dictSetting", dictSetting);
        } catch (Exception e) {
        }
        try {
            getSupportFragmentManager().putFragment(outState, "otherSetting", otherSetting);
        } catch (Exception e) {
        }
    }

    // обрабатываем кнопку "назад" в ActionBar
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
        intent.putExtra("learningType", learningSetting.getLearningType());
        intent.putExtra("repeatsAmount", learningSetting.getRepeatsAmount());
        intent.putExtra("isSpeakLearning", learningSetting.isSpeakLearning());
        intent.putExtra("engOrRusLearning", learningSetting.isEng());
        intent.putExtra("isShowTranscr", learningSetting.isShowTranscr());
        intent.putExtra("amountWords", learningSetting.getAmountWords());
        intent.putExtra("isShowDontKnow", learningSetting.isShowDontKnow());
        intent.putExtra("isLessonsHistory", learningSetting.isLessonsHistory());
        intent.putExtra("translDirection", dictSetting.isEngTransl());
        intent.putExtra("searchRule", dictSetting.isSearchRule1());
        intent.putExtra("showHistory", dictSetting.isShowHistory());
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public DictSetting getDictSetting() {
        return dictSetting;
    }

    public LearningSetting getLearningSetting() {
        return learningSetting;
    }

    public OtherSetting getOtherSetting() {
        return otherSetting;
    }

    private String s(int res) {
        return getResources().getString(res);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d("myLogs", "onDestroy Setting");
        super.onDestroy();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}