package ru.handy.android.wm.setting;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBar.Tab;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.R;

public class Settings extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private GlobApp app;
    private DictSetting dictSetting;
    private LearningSetting learningSetting;
    private OtherSetting otherSetting;
    private DB db;
    private ActionBar bar;
    // что было выбрано при вызове настроек: 0-обучение, 1-словарь, 2- прочее
    private int idSetting;
    private Tracker mTracker; // трекер для Google analitics, чтобы отслеживать активности пользователей

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
        mTracker = app.getDefaultTracker(); // Obtain the shared Tracker instance.
        db = app.getDb(); // открываем подключение к БД

        // устанавливаем toolbar и actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        // устанавливаем цвет фона и шрифта для toolbar
        Utils.colorizeToolbar(this, toolbar);
        // устанавливаем цвет стрелки "назад" в toolbar
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Utils.getFontColorToolbar(), PorterDuff.Mode.SRC_ATOP);
        bar.setHomeAsUpIndicator(upArrow);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
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

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        Log.d("myLogs", "onCreate Setting");
    }

    /**
     * метод для сохранения состояния при повороте экрана
     *
     * @param outState
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
        /*intent.putExtra("learningType", db.getValueByVariable(DB.LEARNING_TYPE) == null ? 0 :
                Integer.parseInt(db.getValueByVariable(DB.LEARNING_TYPE)));
        intent.putExtra("repeatsAmount", db.getValueByVariable(DB.LEARNING_REPEATS_AMOUNT) == null ? 2 :
                Integer.parseInt(db.getValueByVariable(DB.LEARNING_REPEATS_AMOUNT)));
        intent.putExtra("isSpeakLearning", db.getValueByVariable(DB.LEARNING_SPEAK) == null ? true :
                Boolean.parseBoolean(db.getValueByVariable(DB.LEARNING_SPEAK)));
        intent.putExtra("engOrRusLearning", db.getValueByVariable(DB.LEARNING_LANGUAGE) == null ? true :
                !Boolean.parseBoolean(db.getValueByVariable(DB.LEARNING_LANGUAGE)));
        intent.putExtra("isShowTranscr", db.getValueByVariable(DB.LEARNING_SHOW_TRANSCR) == null ? true :
                Boolean.parseBoolean(db.getValueByVariable(DB.LEARNING_SHOW_TRANSCR)));
        intent.putExtra("amountWords", db.getValueByVariable(DB.LEARNING_AMOUNT_WORDS) == null ? 8 :
                Integer.parseInt(db.getValueByVariable(DB.LEARNING_AMOUNT_WORDS)));
        intent.putExtra("isShowDontKnow", db.getValueByVariable(DB.LEARNING_SHOW_DONTKNOW) == null ? true :
                Boolean.parseBoolean(db.getValueByVariable(DB.LEARNING_SHOW_DONTKNOW)));*/
        intent.putExtra("learningType", learningSetting.getLearningType());
        intent.putExtra("repeatsAmount", learningSetting.getRepeatsAmount());
        intent.putExtra("isSpeakLearning", learningSetting.isSpeakLearning());
        intent.putExtra("engOrRusLearning", learningSetting.isEng());
        intent.putExtra("isShowTranscr", learningSetting.isShowTranscr());
        intent.putExtra("amountWords", learningSetting.getAmountWords());
        intent.putExtra("isShowDontKnow", learningSetting.isShowDontKnow());
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
        if (mTracker != null) {
            Log.i("myLogs", "Setting screen name: " + this.getLocalClassName());
            mTracker.setScreenName("Activity " + this.getLocalClassName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
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