package ru.handy.android.wm.downloads;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import ru.handy.android.wm.R;
import ru.handy.android.wm.setting.Utils;

/**
 * Activity для выбора файла/директории.
 *
 * @author android
 */
@SuppressLint("DefaultLocale")
public class FileDialog extends AppCompatActivity {

    /**
     * Параметр для ввода Activity: начальный путь. По умолчанию: ROOT.
     */
    public static final String START_PATH = "START_PATH";
    /**
     * Параметр для ввода Activity: фильр форматов файлов. По умолчанию: null.
     */
    public static final String FORMAT_FILTER = "FORMAT_FILTER";
    /**
     * Параметр для ввода Activity: выбранный путь. По умолчанию: null.
     */
    public static final String RESULT_PATH = "RESULT_PATH";
    /**
     * Параметр, разрешающий создавать файл, а не только выбирать из имеющихся.
     * По умолчанию true
     */
    public static final String CAN_CREATE_FILE = "CAN_CREATE_FILE";
    /**
     * Параметр для ввода Activity: допустимо ли выбирать директорию. По
     * умолчанию: false.
     */
    public static final String CAN_SELECT_DIR = "CAN_SELECT_DIR";
    /**
     * Ключ в списке элементов пути
     */
    private static final String ITEM_KEY = "key";
    /**
     * Картинка для списка (папка или файл).
     */
    private static final String ITEM_IMAGE = "image";
    /**
     * корневая папка
     */
    private static final String ROOT = "/";
    private List<String> path = null;
    private TextView myPath;
    private EditText mFileName;
    private ListView list;
    private ArrayList<HashMap<String, Object>> mList;

    private Button selectButton;

    private LinearLayout layoutSelect;
    private LinearLayout layoutCreate;
    private InputMethodManager inputManager;
    private String parentPath;
    private String currentPath = ROOT;

    private boolean canCreateFile = true;

    private String[] formatFilter = null;

    private boolean canSelectDir = false;

    private File selectedFile;
    private HashMap<String, Integer> lastPositions = new HashMap<>();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED, getIntent());

        setContentView(R.layout.file_dialog_main);
        myPath = findViewById(R.id.path);
        mFileName = findViewById(R.id.fdEditTextFile);
        list = findViewById(R.id.list);

        inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);


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
        if (upArrow != null) {
            upArrow.setColorFilter(Utils.getFontColorToolbar(), PorterDuff.Mode.SRC_ATOP);
        }
        if (bar != null) {
            bar.setHomeAsUpIndicator(upArrow);
        }

        selectButton = findViewById(R.id.fdButtonSelect);
        selectButton.setEnabled(false);
        selectButton.setOnClickListener(v -> {
            if (selectedFile != null) {
                getIntent().putExtra(RESULT_PATH, selectedFile.getPath());
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });

        final Button newButton = findViewById(R.id.fdButtonNew);
        newButton.setOnClickListener(v -> {
            mFileName.setText(selectedFile == null || !selectButton.isEnabled() ? "" : selectedFile.getName());
            setCreateVisible(v);
            mFileName.requestFocus();
        });

        canCreateFile = getIntent().getBooleanExtra(CAN_CREATE_FILE, true);

        formatFilter = getIntent().getStringArrayExtra(FORMAT_FILTER);

        canSelectDir = getIntent().getBooleanExtra(CAN_SELECT_DIR, false);

        if (!canCreateFile) {
            newButton.setEnabled(false);
        }

        layoutSelect = findViewById(R.id.fdLinearLayoutSelect);
        layoutCreate = findViewById(R.id.fdLinearLayoutCreate);
        layoutCreate.setVisibility(View.GONE);

        final Button cancelButton = findViewById(R.id.fdButtonCancel);
        cancelButton.setOnClickListener(this::setSelectVisible);
        final Button createButton = findViewById(R.id.fdButtonCreate);
        createButton.setOnClickListener(v -> {
            if (mFileName.getText().length() > 0) {
                getIntent().putExtra(RESULT_PATH,
                        currentPath + "/" + mFileName.getText());
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });

        String startPath = getIntent().getStringExtra(START_PATH);
        startPath = startPath != null ? startPath : ROOT;
        if (canSelectDir) {
            selectedFile = new File(startPath);
            selectButton.setEnabled(true);
        }
        getDir(startPath);
    }

    private void getDir(String dirPath) {

        boolean useAutoSelection = dirPath.length() < currentPath.length();

        Integer position = lastPositions.get(parentPath);

        getDirImpl(dirPath);

        if (position != null && useAutoSelection) {
            list.setSelection(position);
        }

    }

    /**
     * Собирает структуру файлов и папок данной родительской папки.
     *
     * @param dirPath родительская папка
     */
    private void getDirImpl(final String dirPath) {

        currentPath = dirPath;

        final List<String> item = new ArrayList<>();
        path = new ArrayList<>();
        mList = new ArrayList<>();

        File f = new File(currentPath);
        File[] files = f.listFiles();
        if (files == null) {
            currentPath = ROOT;
            f = new File(currentPath);
            files = f.listFiles();
            if (files == null) {
                finish();
                return;
            }
        }
        myPath.setText(String.format("%s: %s", getText(R.string.location), currentPath));

        if (!currentPath.equals(ROOT)) {
            item.add(ROOT);
            addItem(ROOT, R.drawable.folder);
            path.add(ROOT);

            item.add("../");
            addItem("../", R.drawable.folder);
            path.add(f.getParent());
            parentPath = f.getParent();
        }

        TreeMap<String, String> dirsMap = new TreeMap<>();
        TreeMap<String, String> dirsPathMap = new TreeMap<>();
        TreeMap<String, String> filesMap = new TreeMap<>();
        TreeMap<String, String> filesPathMap = new TreeMap<>();
        for (File file : files) {
            if (file.isDirectory()) {
                String dirName = file.getName();
                dirsMap.put(dirName, dirName);
                dirsPathMap.put(dirName, file.getPath());
            } else {
                final String fileName = file.getName();
                final String fileNameLwr = fileName.toLowerCase();
                // se ha um filtro de formatos, utiliza-o
                if (formatFilter != null) {
                    boolean contains = false;
                    for (String s : formatFilter) {
                        final String formatLwr = s.toLowerCase();
                        if (fileNameLwr.endsWith(formatLwr)) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) {
                        filesMap.put(fileName, fileName);
                        filesPathMap.put(fileName, file.getPath());
                    }
                    // senao, adiciona todos os arquivos
                } else {
                    filesMap.put(fileName, fileName);
                    filesPathMap.put(fileName, file.getPath());
                }
            }
        }
        item.addAll(dirsMap.tailMap("").values());
        item.addAll(filesMap.tailMap("").values());
        path.addAll(dirsPathMap.tailMap("").values());
        path.addAll(filesPathMap.tailMap("").values());

        SimpleAdapter fileList = new SimpleAdapter(this, mList,
                R.layout.file_dialog_row,
                new String[]{ITEM_KEY, ITEM_IMAGE}, new int[]{
                R.id.fdrowtext, R.id.fdrowimage});

        for (String dir : dirsMap.tailMap("").values()) {
            addItem(dir, R.drawable.folder);
        }

        for (String file : filesMap.tailMap("").values()) {
            addItem(file, R.drawable.file);
        }

        fileList.notifyDataSetChanged();

        list.setAdapter(fileList);

        /*
          При выборе элемента списка необходимо: 1) Если в директории, открывает
          файлы детей; 2) Если можно выбрать каталог, определям его как выбранный
          путь 3) Если файл, устанавливам его как выбранный путь 4) Включить кнопку
          выбора.
         */
        list.setOnItemClickListener((parent, v, position, id) -> {
            File file = new File(path.get(position));
            if (file.isDirectory()) {
                setSelectVisible(v);
                selectButton.setEnabled(false);
                if (file.canRead()) {
                    lastPositions.put(currentPath, position);
                    getDir(path.get(position));
                    if (canSelectDir) {
                        selectedFile = file;
                        v.setSelected(true);
                        selectButton.setEnabled(true);
                    }
                } else {
                    new AlertDialog.Builder(FileDialog.this)
                            .setIcon(R.drawable.icon)
                            .setTitle(
                                    "[" + file.getName() + "] "
                                            + getText(R.string.cant_read_folder))
                            .setPositiveButton("OK", (dialog, which) -> {
                            }).show();
                }
            } else {
                if (layoutCreate.getVisibility() == View.VISIBLE) {
                    mFileName.setText(file.getName());
                    mFileName.requestFocus();
                } else {
                    setSelectVisible(v);
                    selectedFile = file;
                    v.setSelected(true);
                    selectButton.setEnabled(true);
                }
            }
        });
    }

    private void addItem(String fileName, int imageId) {
        HashMap<String, Object> item = new HashMap<>();
        item.put(ITEM_KEY, fileName);
        item.put(ITEM_IMAGE, imageId);
        mList.add(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            selectButton.setEnabled(false);

            if (layoutCreate.getVisibility() == View.VISIBLE) {
                layoutCreate.setVisibility(View.GONE);
                layoutSelect.setVisibility(View.VISIBLE);
            } else {
                if (!currentPath.equals(ROOT)) {
                    getDir(parentPath);
                } else {
                    return super.onKeyDown(keyCode, event);
                }
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Устанавливаем кнопку CREATE видимой.
     *
     * @param v view
     */
    private void setCreateVisible(View v) {
        layoutCreate.setVisibility(View.VISIBLE);
        layoutSelect.setVisibility(View.GONE);

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        selectButton.setEnabled(false);
    }

    /**
     * Устанавливаем кнопку SELECT видимой.
     *
     * @param v view
     */
    private void setSelectVisible(View v) {
        layoutCreate.setVisibility(View.GONE);
        layoutSelect.setVisibility(View.VISIBLE);

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        selectButton.setEnabled(false);
    }

    // обрабатываем кнопку "назад" в ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}