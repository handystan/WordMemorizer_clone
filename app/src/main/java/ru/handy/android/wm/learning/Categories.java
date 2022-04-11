package ru.handy.android.wm.learning;

import static ru.handy.android.wm.setting.Utils.strToList;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yodo1.mas.Yodo1Mas;
import com.yodo1.mas.banner.Yodo1MasBannerAdView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;
import ru.handy.android.wm.setting.Utils;

/**
 * класс позволяющий выбирать категорию(и) для обучения
 */
public class Categories extends AppCompatActivity implements OnClickListener, OnCatCheckedListener {

    public static String NEW_CATEGORIES = "NEW_CATEGORIES";
    private static final int CM_EDIT_ID = 0; // идентификатор пункта контекстного меню по редактированию названия категории
    private int fromAct; // из какого activity вызывается (0 - Learning, 1 - Dictionary, 2 - EditData
    private String message; // сообщение об окончании урока
    private GlobApp app;
    private EditText etInputWord;
    private ListView lvCategories;
    private Button bChooseCat;
    private CheckBox cbSelectAll;
    private CategoryAdapter cAdapter;
    private RelativeLayout rlCategories;
    private LinearLayout llAdMobCategories;
    private Yodo1MasBannerAdView avBottomBannerCategories;
    private DB db;
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories);
        fromAct = getIntent().getIntExtra("fromAct", 0);
        message = getIntent().getStringExtra("message");

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

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        int amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        rlCategories = findViewById(R.id.rlCategories);
        llAdMobCategories = findViewById(R.id.llAdMobCategories);
        avBottomBannerCategories = findViewById(R.id.avBottomBannerCategories);
        // загружаем баннерную рекламу yodo1
        avBottomBannerCategories.loadAd();
        ViewGroup.LayoutParams params = llAdMobCategories.getLayoutParams();
        if (amountDonate > 0) {
            params.height = 0;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName() + " без отображения");
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName());
        }
        llAdMobCategories.setLayoutParams(params);
        if (amountDonate <= 0) {
            //если приложение еще не оплачено, то ставим Listener, чтобы показывать рекламу, когда нет клавиатуры и не показывать, когда клавитура есть
            rlCategories.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                Rect rect = new Rect();
                v.getWindowVisibleDisplayFrame(rect);
                int screenHeight = v.getRootView().getHeight();
                int keypadHeight = screenHeight - rect.bottom;
                ViewGroup.LayoutParams params1 = llAdMobCategories.getLayoutParams();
                params1.height = (keypadHeight > screenHeight * 0.15) ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
                llAdMobCategories.setLayoutParams(params1);
            });
        }

        // устанавливаем цвет фона и шрифта для toolbar
        Utils.colorizeToolbar(this, toolbar);
        // устанавливаем цвет стрелки "назад" в toolbar
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        if (upArrow != null && bar != null) {
            upArrow.setColorFilter(Utils.getFontColorToolbar(), PorterDuff.Mode.SRC_ATOP);
            bar.setHomeAsUpIndicator(upArrow);
        }

        // обраотка кнопки Назад
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (message != null && message.equals(s(R.string.success))) {
                    Toast.makeText(getApplicationContext(), s(R.string.need_category), Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        });

        lvCategories = findViewById(R.id.lvCategories);
        cbSelectAll = findViewById(R.id.cbSelectAll);
        bChooseCat = findViewById(R.id.bChooseCat);
        etInputWord = findViewById(R.id.etInputWord);
        bChooseCat.setOnClickListener(this);
        lvCategories.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // если категории вызываются из обучения, то кнопку для выбора всех категорий делаем не видимой
        cbSelectAll.setVisibility(fromAct == 0 ? View.GONE : View.VISIBLE);
        bChooseCat.setText(fromAct == 1 ? R.string.delete_category : fromAct == 2 ? R.string.choose_category_and_upload_file
                : R.string.choose_category);
        // для ускорения отображения сначала показываем категории без кол-ва слов
        ArrayList<String> cats = db.getCategories();
        final ArrayList<Category> categories = new ArrayList<>();
        for (int i = 0; i < cats.size(); i++) {
            categories.add(new Category(cats.get(i), 0));
        }
        cAdapter = new CategoryAdapter(this, categories, false);
        lvCategories.setAdapter(cAdapter);
        cAdapter.addListener(this);

        // добавляем контекстное меню к списку
        registerForContextMenu(lvCategories);

        etInputWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // здесь ничего не делаем
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // здесь ничего не делаем
            }

            @Override
            public void afterTextChanged(Editable s) {
                int remainChecked = 0; //оставшиеся выбранные категории
                for (int i = 0; i < categories.size(); i++) {
                    String catName = categories.get(i).getName();
                    boolean shouldShow = s.toString().equals("")
                            || catName.toLowerCase().contains(s.toString().toLowerCase());
                    categories.get(i).setShow(shouldShow);
                    if (!shouldShow)
                        categories.get(i).setChecked(false);
                    if (shouldShow && categories.get(i).isChecked())
                        remainChecked++;
                }
                cAdapter.notifyDataSetChanged(); // обновляем адаптер
                cbSelectAll.setChecked(remainChecked == categories.size());
                cbSelectAll.setText(remainChecked == 0 ? s(R.string.choose_all_categories) :
                        s(R.string.chose) + " " + remainChecked);
                bChooseCat.setEnabled(!(remainChecked == 0));
            }
        });
        // затем в отдельном потоке отображаем и кол-во слов в каждой категории
        new Thread(() -> {
            //категории с указанем кол-ва отгаданных и неотгаданных слов (из T_STATISTICS)
            HashMap<String, Category> rightWordsInCats = db.getRightWordsInCats();
            final ArrayList<Category> newCategories = db.getClassCategories();
            if (newCategories != null) {
                for (int i = 0; i < categories.size(); i++) {
                    String cat = categories.get(i).getName();
                    categories.get(i).setAmount(newCategories.get(i).getAmount());
                    categories.get(i).setAmountRight(rightWordsInCats.get(cat) == null ? 0 : rightWordsInCats.get(cat).getAmountRight());
                    categories.get(i).setAmountWrong(rightWordsInCats.get(cat) == null ? 0 : rightWordsInCats.get(cat).getAmountWrong());
                }
                runOnUiThread(() -> {
                    cAdapter.notifyDataSetChanged(); // обновляем адаптер
                });
            }
        }).start();

        cbSelectAll.setOnClickListener(v -> {
            boolean isAllChecked = ((CheckBox) v).isChecked();
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).isShow())
                    categories.get(i).setChecked(isAllChecked);
            }
            cAdapter.notifyDataSetChanged(); // обновляем адаптер
        });
        //показываем полноэкранную рекламу, если необходимо
        if (getIntent().getBooleanExtra("showInterstitialAd", false)) {
            Yodo1Mas.getInstance().showInterstitialAd(this);
        }
    }

    @Override
    public void onClick(View v) {
        StringBuilder sb = new StringBuilder();
        for (Category c : cAdapter.getCheckedCategories()) {
            if (c.isChecked())
                sb.append(c.getName()).append(", ");
        }
        if (sb.toString().equals("")) {
            Toast.makeText(getApplicationContext(), s(R.string.need_category), Toast.LENGTH_SHORT).show();
            return;
        }
        String catsStr = sb.substring(0, sb.length() - 2);
        Intent intent = new Intent();
        // если выбраны категории для обучения
        if (fromAct == 0) {
            intent.putExtra(NEW_CATEGORIES, catsStr);
            setResult(RESULT_OK, intent);
            finish();
        } else if (fromAct == 1) { //если выбраны категории для удаления
            new AlertDialog.Builder(this)
                    .setMessage(s(R.string.delete_category_words))
                    .setPositiveButton(s(R.string.yes), (dialog, which) -> {
                        try {
                            // проверяем, нужно ли обновлять интерфейс в тек. уроке (есть ли в тек. уроке удаляемые категории)
                            List<String> cats = strToList(catsStr, ",", true);
                            List<String> catsInCurLesson = strToList(db.getCategoryCurLesson(), ",", true);
                            boolean inCurLesson = false;
                            for (String cat : cats) {
                                inCurLesson = catsInCurLesson.contains(cat);
                            }
                            db.removeStats(catsStr); // удаляем катгории из T_STATISTICS
                            db.deleteCategories(DB.T_LESSON, catsStr); // удаляем катгории из T_LESSON
                            // удаляем катгории из T_ENGWORDS
                            int[] res = db.deleteCategories(DB.T_ENGWORDS, catsStr);
                            intent.putExtra("categoriesAmount", res[0]);
                            intent.putExtra("deletedWordsAmount", res[1]);
                            intent.putExtra("updateWordsAmount", res[2]);
                            if (inCurLesson) { // нужно ли обновлять интерфейс текущеш-го урока
                                app.getLearning().updateLesson(db.getCategoryCurLesson(), true, false, 0);
                            }
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (SQLiteException e) {
                            //обработка ошибки, когда выбрано слишком много категорий
                            if (Objects.requireNonNull(e.getMessage()).startsWith("Expression tree is too large")) {
                                Toast.makeText(getApplicationContext(), s(R.string.to_much_categories), Toast.LENGTH_LONG).show();
                            } else {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
        } else if (fromAct == 2) { //если выбраны категории для загрузки/выгрузки данных
            intent.putExtra("categories", catsStr);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    /**
     * @param categories список категорий
     * @param position   позиция категории, по которой изменилась галка
     * @param isChecked  новое значение галки
     */
    @Override
    public void onCatChecked(ArrayList<Category> categories, int position, boolean isChecked) {
        int amountOfChecked = 0;
        int amountofShowed = 0;
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).isChecked()) amountOfChecked++;
            if (categories.get(i).isShow()) amountofShowed++;
        }
        cbSelectAll.setChecked(amountOfChecked == amountofShowed);
        cbSelectAll.setText(amountOfChecked == 0 ? s(R.string.choose_all_categories) :
                s(R.string.chose) + " " + amountOfChecked);
        bChooseCat.setEnabled(!(amountOfChecked == 0));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_EDIT_ID, 0, R.string.rename_category);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // получаем из пункта контекстного меню данные по пункту списка
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        // извлекаем id записи
        long idItem = acmi.id;
        if (item.getItemId() == CM_EDIT_ID) { // изменяем наименование
            String catName = ((Category) cAdapter.getItem((int) idItem)).getName();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View viewDialog = getLayoutInflater().inflate(R.layout.dialog_category_rename, null);
            EditText etCatRename = viewDialog.findViewById(R.id.etCatRename);
            etCatRename.setText(catName);
            etCatRename.selectAll();
            builder.setView(viewDialog);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                // запускаем изменение имени категории только, если старое и новое имя отличаются
                if (!catName.equals(etCatRename.getText().toString())) {
                    db.categoryRename(DB.T_ENGWORDS, catName, etCatRename.getText().toString()); // обновляем категории в T_ENGWORDS
                    db.categoryRename(DB.T_LESSON, catName, etCatRename.getText().toString()); // обновляем категории в T_LESSON
                    db.updateCatInStats(catName, etCatRename.getText().toString()); // обновляем категории в T_STATISTICS
                    ArrayList<Category> cats = cAdapter.getCategories();
                    // если новое имя совпадает с каким сущствующим, то находится его id в списке
                    int idExist = cAdapter.getIdByCatName(etCatRename.getText().toString());
                    int id = cAdapter.getIdByCatName(catName); //id категории, имя которой изменяется
                    // если новое имя не совпадает ни с какой другой категории, то просто обновляем его имя
                    if (idExist == -1) {
                        cats.get(id).setName(etCatRename.getText().toString());
                    } else { // если новое имя совпадает с именем уже существующей другой категории
                        Category newExist = cats.get(idExist);
                        newExist.setAmount(cats.get(idExist).getAmount() + cats.get(id).getAmount());
                        cats.set(idExist, newExist);
                        cats.remove(id);
                    }
                    Collections.sort(cats); // сортируем обновленный список категорий
                    cAdapter.notifyDataSetChanged();
                    // если изменилась наименование категории в тек. уроке, то в интерфейсе обновляем кнопку с категорией
                    if (db.getCategoryCurLesson().contains(etCatRename.getText().toString())) {
                        app.getLearning().updateLesson(null, false, false, 2);
                    }
                    Toast.makeText(getApplicationContext(), s(R.string.success_of_rename_category), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), s(R.string.no_rename_category), Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).
                    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    // обрабатываем кнопку "назад" в ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        if (item.getItemId() == android.R.id.home) {
            if (message != null && message.equals(s(R.string.success))) {
                Toast.makeText(getApplicationContext(), s(R.string.need_category), Toast.LENGTH_SHORT).show();
                return false;
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d("myLogs", "onDestroy Categories");
        super.onDestroy();
    }

    private String s(int res) {
        return this.getResources().getString(res);
    }
}
