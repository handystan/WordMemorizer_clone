package ru.handy.android.wm.downloads;

import static ru.handy.android.wm.setting.Utils.listToStr;
import static ru.handy.android.wm.setting.Utils.strToList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yodo1.mas.banner.Yodo1MasBannerAdView;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import ru.handy.android.wm.About;
import ru.handy.android.wm.CustomKeyboard;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.Help;
import ru.handy.android.wm.NoAd;
import ru.handy.android.wm.R;
import ru.handy.android.wm.Thanks;
import ru.handy.android.wm.learning.Categories;
import ru.handy.android.wm.learning.Learning;
import ru.handy.android.wm.learning.Word;
import ru.handy.android.wm.setting.Utils;

public class EditData extends AppCompatActivity {

    private static final int REQUEST_LOAD = 0;
    private static final int REQUEST_SAVE = 1;
    private static final int GET_CAT_UPLOAD_XLS = 2;
    private static final int GET_CAT_UPLOAD_TXT = 3;
    private LinearLayout llPayInformation;
    private Button bPay;
    private Button bOpenDialog;
    private Button bSaveDialog;
    private Button bDownload;
    private Button bUpload;
    private EditText etOpenFileName;
    private EditText etSaveFileName;
    private CheckBox cbDelete;
    private CheckBox cbOnlyChoseCat;
    private Spinner sFileType;
    private EditText etSemicolon;
    private TextView tvSemicolon;
    private ImageView ivShare1;
    private ImageView ivShare2;
    private LinearLayout llAdMobData;
    private Yodo1MasBannerAdView avBottomBannerData;
    private CustomKeyboard keyboard;
    private Menu menu;
    private DB db;
    private Learning learning = null;
    private Uri uriDownloadFile; // uri загружаемого файла
    private Uri uriUploadFile; // uri выгружаемого файла
    private GlobApp app;
    private int amountDonate = 0; // показывает сумму, которую пользователь пожертвовал разработчику
    private FirebaseAnalytics mFBAnalytics; // переменная для регистрации событий в FirebaseAnalytics

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_data);

        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mFBAnalytics = app.getFBAnalytics(); // получение экземпляра FirebaseAnalytics
        if (mFBAnalytics != null) {
            String[] arrClName = this.getClass().toString().split("\\.");
            app.openActEvent(arrClName[arrClName.length - 1]);
        }
        db = app.getDb(); // открываем подключение к БД

        // устанавливаем отдельную клавиатуру для поля с транскрипцией
        keyboard = new CustomKeyboard(this, R.id.etSemicolon,
                R.id.specKeyboardView, R.xml.specialkeyboard);

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

        llPayInformation = findViewById(R.id.llPayInformation);
        bPay = findViewById(R.id.bPay);
        bOpenDialog = findViewById(R.id.bDialogDownload);
        bSaveDialog = findViewById(R.id.bDialogUpload);
        bDownload = findViewById(R.id.bDownload);
        bUpload = findViewById(R.id.bUpload);
        etOpenFileName = findViewById(R.id.etFileDownload);
        etSaveFileName = findViewById(R.id.etFileUpload);
        cbDelete = findViewById(R.id.cbDelete);
        sFileType = findViewById(R.id.sFileType);
        etSemicolon = findViewById(R.id.etSemicolon);
        tvSemicolon = findViewById(R.id.tvSemicolon);
        cbOnlyChoseCat = findViewById(R.id.cbOnlyChoseCat);
        ivShare1 = findViewById(R.id.ivShare1);
        ivShare2 = findViewById(R.id.ivShare2);

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);

        llAdMobData = findViewById(R.id.llAdMobData);
        avBottomBannerData = findViewById(R.id.avBottomBannerData);
        // загружаем баннерную рекламу yodo1
        avBottomBannerData.loadAd();

        // это уже не актуально, так как это убрал из платных функций
        llPayInformation.getLayoutParams().height = 0;
        /*if (amountDonate > 0) { // если старая БД или приложение оплачено, то скрываем layout с предложением оплаты
            llPayInformation.getLayoutParams().height = 0;
        } else { // если новая БД и приложение не оплачено, то показываем layout с предложением оплаты
            pay = app.getPay(app);
            bPay.setOnClickListener(v -> Utils.alertForPay("", EditData.this, pay, db, false));
        }*/

        // адаптер для типа файла
        String[] spinnerData = {s(R.string.spinner_xls),
                s(R.string.spinner_txt)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sFileType.setAdapter(adapter);
        sFileType.setPrompt(s(R.string.spinner_title));
        sFileType.setSelection(0);
        sFileType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (position == 0) {
                    etSemicolon.setVisibility(View.GONE);
                    tvSemicolon.setVisibility(View.GONE);
                } else {
                    etSemicolon.setVisibility(View.VISIBLE);
                    tvSemicolon.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        // устанавливаем фильтр на текстовое поле с разделителем для текстового файла
        etSemicolon.setFilters(
                new InputFilter[]{
                        (source, start, end, dest, dstart, dend) -> {
                            if (source.length() + etSemicolon.length() > 1) return "";
                            for (int i = start; i < end; i++) {
                                if (source.charAt(i) == ';' || source.charAt(i) == '!'
                                        || source.charAt(i) == '#' || source.charAt(i) == '&'
                                        || source.charAt(i) == '|') {
                                    return null;
                                }
                            }
                            return "";
                        }
                }
        );

        bOpenDialog.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] mimetypes = {"application/vnd.ms-excel", "text/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            startActivityForResult(intent, REQUEST_LOAD);
        });

        bSaveDialog.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (sFileType.getSelectedItemPosition() == 0) {
                intent.setType("application/vnd.ms-excel");
                intent.putExtra(Intent.EXTRA_TITLE, "Dictionary.xls");
            } else {
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, "Dictionary.txt");
            }
            startActivityForResult(intent, REQUEST_SAVE);
        });

        bDownload.setOnClickListener(v -> {
            String fileName = etOpenFileName.getText().toString();
            if (fileName.equals("") || uriDownloadFile == null) {
                Toast.makeText(getApplicationContext(), s(R.string.choose_file), Toast.LENGTH_LONG).show();
                return;
            }
            if (fileName.endsWith(".xls")) {
                bDownload.setText(s(R.string.downloading));
                new Thread() {
                    public void run() {
                        runOnUiThread(() -> {
                            readXlsFile(uriDownloadFile);
                            bDownload.setText(s(R.string.download_data));
                        });
                    }
                }.start();
            } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xlsm") || fileName.endsWith(".xlsb")) {
                Toast.makeText(getApplicationContext(), s(R.string.should_be_xls), Toast.LENGTH_LONG).show();
            } else {
                bDownload.setText(s(R.string.downloading));
                new Thread() {
                    public void run() {
                        runOnUiThread(() -> {
                            readTxtFile(uriDownloadFile);
                            bDownload.setText(s(R.string.download_data));
                        });
                    }
                }.start();
            }
        });

        bUpload.setOnClickListener(v -> {
            String fileName = etSaveFileName.getText().toString();
            String delimiter = etSemicolon.getText().toString();
            if (fileName.equals("") || uriUploadFile == null) {
                Toast.makeText(getApplicationContext(), s(R.string.choose_file), Toast.LENGTH_LONG).show();
                return;
            }
            try {
                //boolean isAllRight = true; // если что-то пошло не так, то это запишется в эту переменную
                if (sFileType.getSelectedItemPosition() == 0) { // выгружается xls-файл
                    if (!fileName.endsWith(".xls")) { // если пользователь выбрал не то расширение файла
                        Toast.makeText(getApplicationContext(), s(R.string.should_be_xls), Toast.LENGTH_LONG).show();
                    } else {
                        if (cbOnlyChoseCat.isChecked()) {
                            Intent intent = new Intent(this, Categories.class);
                            intent.putExtra("fromAct", 2); // 2 - запуск из EditData
                            startActivityForResult(intent, GET_CAT_UPLOAD_XLS);
                        } else {
                            bUpload.setText(s(R.string.uploading));
                            new Thread() {
                                public void run() {
                                    runOnUiThread(() -> {
                                        writeXlsFile(uriUploadFile, null);
                                        bUpload.setText(s(R.string.upload_data));
                                    });
                                }
                            }.start();
                        }
                    }
                } else if (sFileType.getSelectedItemPosition() == 1) { // выгружается txt-файл
                    if (fileName.endsWith(".xls")) { // если пользователь выбрал не то расширение файла
                        Toast.makeText(getApplicationContext(), s(R.string.shouldnt_be_xls), Toast.LENGTH_LONG).show();
                    } else if (delimiter.equals("")) {
                        Toast.makeText(getApplicationContext(), s(R.string.empty_delimiter),
                                Toast.LENGTH_LONG).show();
                    } else {
                        if (cbOnlyChoseCat.isChecked()) {
                            Intent intent = new Intent(this, Categories.class);
                            intent.putExtra("fromAct", 2); // 2 - запуск из EditData
                            startActivityForResult(intent, GET_CAT_UPLOAD_TXT);
                        } else {
                            bUpload.setText(s(R.string.uploading));
                            new Thread() {
                                public void run() {
                                    runOnUiThread(() -> {
                                        writeTxtFile(uriUploadFile, null);
                                        bUpload.setText(s(R.string.upload_data));
                                    });
                                }
                            }.start();
                        }
                    }
                }
                // в зависимости от того новый это файл или нет, устанавливаем доступность кнопки по отправке файла
                AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().
                        openAssetFileDescriptor(uriUploadFile, "r");
                ivShare2.setVisibility(fileDescriptor != null && fileDescriptor.getLength() > 0 ? View.VISIBLE : View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //показываем или скрываем кнопку по отправке файла из телефона в зависимости от заполненности поля с путем файла
        addTextChangedListener(etOpenFileName, 0);
        addTextChangedListener(etSaveFileName, 1);
        // отправляем файл с телефона в другое приложение
        ivShare1.setOnClickListener(v -> shareFile(uriDownloadFile));
        // отправляем файл, созданный приложением в другое приложение
        ivShare2.setOnClickListener(v -> shareFile(uriUploadFile));
    }

    /**
     * отправка файла-словаря через другие программы
     *
     * @param uriFile uri оптравляемого файла
     */
    private void shareFile(Uri uriFile) {
        if (uriFile != null) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, uriFile);
            sendIntent.setType("application/*");
            //это делаем для того, чтобы не возникала ошибка java.lang.SecurityException
            Intent chooser = Intent.createChooser(sendIntent, "Share File");
            @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> resInfoList = EditData.this.getPackageManager().
                    queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                EditData.this.grantUriPermission(resolveInfo.activityInfo.packageName,
                        uriFile, Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivity(chooser);
        }
    }

    /**
     * показываем или скрываем кнопку по отправке файла из телефона в зависимости от заполненности поля с путем файла
     *
     * @param etFilePath текстовое поле, в котором указывается путь
     * @param type       тип текстового поля (0 - файл из телефона, 1 - файл, сохраненный из приложения)
     */
    private void addTextChangedListener(EditText etFilePath, int type) {
        etFilePath.addTextChangedListener(new TextWatcher() {
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
                if (type == 0) {
                    ivShare1.setVisibility(s != null && !s.toString().equals("") ? View.VISIBLE : View.GONE);
                } else if (type == 1) {
                    if (s == null || s.toString().equals("") || uriUploadFile == null) {
                        ivShare2.setVisibility(View.GONE);
                    } else {
                        try {
                            AssetFileDescriptor fileDescriptor = getApplicationContext().
                                    getContentResolver().openAssetFileDescriptor(uriUploadFile, "r");
                            ivShare2.setVisibility(fileDescriptor != null && fileDescriptor.getLength() > 0 ? View.VISIBLE : View.GONE);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void setLearning(Learning learning) {
        this.learning = learning;
    }

    /**
     * Метод для чтения данных из экселевского файла со следующей логикой:
     * - если идет полное совпадение (англ. и рус. слово, транскрипция и категория), то слово остается без изменений)
     * - если совпадает только англ. слово, то оно сначала удаляется из словаря, а потом добавляется из файла
     * - если в словаре нет англ. слова из файла и стоит галка по удалению, то оно удаляется
     *
     * @param fileUri uri файла
     * @return количество загруженных строк
     */
    private int readXlsFile(Uri fileUri) {
        Workbook wb = null;
        HashMap<String, Word> words = db.getAllWordsInHashMap(); // список всех слов из словаря
        boolean refreshLearning = false; // переменная, показывающая, нужно ли обновлять интерфейс тек. урока
        try {
            db.beginTransaction();
            WorkbookSettings wbs = new WorkbookSettings();
            wbs.setEncoding("ISO-8859-1");
            wb = Workbook.getWorkbook(getContentResolver().openInputStream(fileUri), wbs);
            Sheet sheet = wb.getSheet(0);
            int r = 0; // кол-во записанных из файла слов
            for (int j = 0; j < sheet.getRows(); j++) {
                // слово из файла
                Word wFile = new Word(0, sheet.getCell(0, j).getContents(), sheet.getCell(1, j).getContents()
                        , sheet.getCell(2, j).getContents(), sheet.getCell(3, j).getContents());
                if (wFile.getEngWord() != null && !wFile.getEngWord().equals("")) {
                    r++;
                    // непосредственно добавление слова в словарь
                    refreshLearning = addRecFromFile(words, wFile) || refreshLearning;
                }
            }
            Log.d("myLogs", "words.size() = " + words.size());
            if (cbDelete.isChecked()) {
                for (Word w : words.values()) {
                    refreshLearning = db.delRecEngWord(w.getId()) || refreshLearning;
                }
            }
            if (refreshLearning) {
                app.getLearning().updateLesson(db.getCategoryCurLesson(), false, false, 0);
            }
            if (r > 0) {
                db.setTransactionSuccessful();
                Toast.makeText(getApplicationContext(),
                        s(R.string.amount_load_rows) + ": " + r, Toast.LENGTH_LONG).show();
            }
            // отправляем в Firebase инфу по чтению слов из файла
            if (mFBAnalytics != null) {
                app.readWriteFileEvent(GlobApp.READ_FILE, "xls", r + "");
            }
            return r;
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), s(R.string.file_read_error) + "\n" +
                    e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            db.endTransaction();
            if (wb != null)
                wb.close();
        }
        return 0;
    }

    /**
     * добавление одного слова в словарь
     *
     * @param words список всех слов из словаря
     * @param wFile слово из файла
     * @return нужно ли обновлять интерфейс Learning
     */
    private boolean addRecFromFile(HashMap<String, Word> words, Word wFile) {
        boolean refreshLearning = false; // переменная, показывающая, нужно ли обновлять интерфейс тек. урока
        // если англ. слово из файла и словаря совпадают, но не совпадает транскрипция или рус. перевод или категория
        if (words.containsKey(wFile.getEngWord())) {
            Word wDict = words.get(wFile.getEngWord());
            if (wDict != null && (!wDict.getTranscription().equalsIgnoreCase(wFile.getTranscription())
                    || !wDict.getRusTranslate().equalsIgnoreCase(wFile.getRusTranslate())
                    || !wDict.getCategory().equalsIgnoreCase(wFile.getCategory()))) {
                // сначала удаляем слово из словаря
                refreshLearning = db.delRecEngWord(wDict.getId());
                // и добавляем слово из файла
                db.addRecEngWord((long) wDict.getId(), wFile.getEngWord(), wFile.getTranscription()
                        , wFile.getRusTranslate(), wFile.getCategory());
                refreshLearning = db.addWordInLessons(wDict.getId(), wFile.getEngWord(), wFile.getTranscription()
                        , wFile.getRusTranslate(), wFile.getCategory()) || refreshLearning;
            }
            words.remove(wFile.getEngWord());
        } else {
            long id = db.addRecEngWord(null, wFile.getEngWord(), wFile.getTranscription()
                    , wFile.getRusTranslate(), wFile.getCategory());
            refreshLearning = db.addWordInLessons(id, wFile.getEngWord(), wFile.getTranscription()
                    , wFile.getRusTranslate(), wFile.getCategory());
        }
        return refreshLearning;
    }

    /**
     * Выгрузка словаря в экселевский файл с расширением xls
     *
     * @param fileUri    uri файла
     * @param categories перечень категорий, которые нужно выгружать в файл (если null, то выгружаются все категории)
     * @return количество выгруженных строк
     */
    @SuppressLint("Range")
    private int writeXlsFile(Uri fileUri, String categories) {
        Cursor c = null;
        WritableWorkbook wb = null;
        List<String> catsToWrite = null; // список категорий, переносимых в файл
        int i = 0;
        try {
            if (categories == null) {
                c = db.getAllWords();
            } else {
                c = db.getDataByCategory(categories);
                catsToWrite = strToList(categories, ",", true);
            }
            if (c.moveToFirst()) {
                wb = Workbook.createWorkbook(getContentResolver().openOutputStream(fileUri));
                WritableSheet sheet = wb.createSheet("WM", 0);
                do {
                    sheet.addCell(new Label(0, i, c.getString(c.getColumnIndex(DB.C_EW_ENGWORD))));
                    sheet.addCell(new Label(1, i, c.getString(c.getColumnIndex(DB.C_EW_TRANSCRIPTION))));
                    sheet.addCell(new Label(2, i, c.getString(c.getColumnIndex(DB.C_EW_RUSTRANSLATE))));
                    if (categories == null) {
                        sheet.addCell(new Label(3, i, c.getString(c.getColumnIndex(DB.C_EW_CATEGORY))));
                    } else {
                        List<String> catsInWord = strToList(c.getString(c.getColumnIndex(DB.C_EW_CATEGORY)), ",", true);
                        catsInWord.retainAll(catsToWrite);
                        sheet.addCell(new Label(3, i, listToStr(catsInWord, ", ")));
                    }
                    i++;
                } while (c.moveToNext());
                wb.write();
                wb.close();
                Toast.makeText(getApplicationContext(), s(R.string.amount_save_rows) + ": " + i,
                        Toast.LENGTH_LONG).show();
                // отправляем в Firebase инфу по записи слов в файл
                if (mFBAnalytics != null) {
                    app.readWriteFileEvent(GlobApp.WRITE_FILE, "xls", i + "");
                }
            } else {
                Toast.makeText(getApplicationContext(), s(R.string.no_data_for_download),
                        Toast.LENGTH_LONG).show();
            }
            return i;
        } catch (IOException e) {
            // сообщение о то, что в текущей версии Android нельзя созранить файл
            String message = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ?
                    s(R.string.android_version30_rescription) :
                    s(R.string.android_version23_rescription);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    s(R.string.failed_download) + "\r\n" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            try {
                if (wb != null)
                    wb.close();
                if (c != null)
                    c.close();
            } catch (Exception e) {
            }
        }
        return 0;
    }

    /**
     * Метод для чтения данных из экселевского файла со следующей логикой:
     * - если идет полное совпадение (англ. и рус. слово, транскрипция и категория), то слово остается без изменений)
     * - если совпадает только англ. слово, то оно сначала удаляется из словаря, а потом добавляется из файла
     * - если в словаре нет англ. слова из файла и стоит галка по удалению, то оно удаляется
     *
     * @param fileUri uri файла
     * @return количество загруженных строк
     */
    private int readTxtFile(Uri fileUri) {
        String row;
        BufferedReader br = null;
        HashMap<String, Word> words = db.getAllWordsInHashMap(); // список всех слов из словаря
        boolean refreshLearning = false; // переменная, показывающая, нужно ли обновлять интерфейс тек. урока
        try {
            db.beginTransaction();
            //определяем кодировку текстового файла
            String encoding = UniversalDetector.detectCharset(getContentResolver().openInputStream(fileUri));
            String adviceEncoding = "";
            Charset charset = StandardCharsets.UTF_8;
            if (encoding != null) {
                switch (encoding) {
                    case "UTF-8":
                        charset = StandardCharsets.UTF_8;
                        break;
                    case "US-ASCII":
                        charset = StandardCharsets.US_ASCII;
                        break;
                    case "UTF-16BE":
                        charset = StandardCharsets.UTF_16BE;
                        break;
                    case "UTF-16LE":
                        charset = StandardCharsets.UTF_16LE;
                        break;
                    default:
                        adviceEncoding = ".\r\n" + s(R.string.must_be_utf8);
                }
            }
            br = new BufferedReader(new InputStreamReader(
                    getContentResolver().openInputStream(fileUri), charset));
            // сначала определяем какой разделитель используется для столбцов
            String delimiter = "";
            Pattern patt = Pattern
                    .compile("^([^;!#&|]*)([;!#&|])([^;!#&|]*)(\\2)([^;!#&|]*)((\\2)([^;!#&|]*))?$");
            while ((row = br.readLine()) != null) {
                Matcher match = patt.matcher(row);
                if (match.find()) {
                    delimiter = match.group(2);
                    break;
                }
            }
            Log.d("myLogs", "delimiter = " + delimiter);
            if (delimiter != null && !delimiter.equals("")) {
                // после определения разделителя получаем данные
                br = new BufferedReader(new InputStreamReader(
                        getContentResolver().openInputStream(fileUri), charset));
                StringBuilder sbRegex = new StringBuilder();
                sbRegex.append("^([^").append(delimiter).append("]*)([").append(delimiter).append("])([^").append(delimiter).
                        append("]*)(\\2)([^").append(delimiter).append("]*)((\\2)([^").append(delimiter).append("]*))?$");
                patt = Pattern.compile(sbRegex.toString());
                StringBuilder wrongRow = new StringBuilder();
                int intWrongRow = 0;
                int r = 0;
                while ((row = br.readLine()) != null) {
                    r++;
                    Matcher match = patt.matcher(row);
                    if ((match.find() || !Objects.equals(match.group(1), "")) && match.group(8) != null) {
                        // слово из файла
                        Word wFile = new Word(0, match.group(1), match.group(3), match.group(5), match.group(8));
                        // непосредственно добавление слова в словарь
                        refreshLearning = addRecFromFile(words, wFile) || refreshLearning;
                    } else {
                        wrongRow.append(wrongRow.toString().equals("") ? "" : ", ").append(r);
                        intWrongRow++;
                    }
                }
                Log.d("myLogs", "words.size() = " + words.size());
                if (cbDelete.isChecked()) {
                    for (Word w : words.values()) {
                        refreshLearning = db.delRecEngWord(w.getId()) || refreshLearning;
                    }
                }
                if (refreshLearning) {
                    app.getLearning().updateLesson(db.getCategoryCurLesson(), false, false, 0);
                }
                if (intWrongRow < (r - intWrongRow)) {
                    db.setTransactionSuccessful();
                    if (intWrongRow == 0) {
                        Toast.makeText(getApplicationContext(), s(R.string.amount_load_rows)
                                + ": " + (r - intWrongRow) + adviceEncoding, Toast.LENGTH_LONG).show();
                    } else {
                        String notLoad = intWrongRow < 10 ? s(R.string.not_load_rows) + wrongRow
                                : s(R.string.amount_not_load_rows) + ": " + intWrongRow;
                        Toast.makeText(getApplicationContext(),
                                s(R.string.amount_load_rows) + ": " + (r - intWrongRow) + "\r\n" + notLoad
                                        + ".\r\n" + s(R.string.template_in_help),
                                Toast.LENGTH_LONG).show();
                    }
                    // отправляем в Firebase инфу по чтению слов из файла
                    if (mFBAnalytics != null) {
                        app.readWriteFileEvent(GlobApp.READ_FILE, "txt", (r - intWrongRow) + "");
                    }
                    return r;
                } else {
                    Toast.makeText(getApplicationContext(),
                            s(R.string.file_not_fit_template) + " " + "\r\n"
                                    + s(R.string.template_in_help), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        s(R.string.file_not_fit_template) + " " + "\r\n"
                                + s(R.string.template_in_help), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            db.endTransaction();
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * Выгрузка словаря в экселевский текстовый файл
     *
     * @param fileUri    uri файла
     * @param categories перечень категорий, которые нужно выгружать в файл (если null, то выгружаются все категории)
     * @return количество выгруженных строк
     */
    @SuppressLint("Range")
    private int writeTxtFile(Uri fileUri, String categories) {
        Cursor c = null;
        BufferedWriter bw = null;
        List<String> catsToWrite = null; // список категорий, переносимых в файл
        // разделитель строк
        String d = etSemicolon.getText().toString();
        int i = 0;
        try {
            StringBuilder sb = new StringBuilder();
            if (categories == null) {
                c = db.getAllWords();
            } else {
                c = db.getDataByCategory(categories);
                catsToWrite = strToList(categories, ",", true);
            }
            if (c.moveToFirst()) {
                do {
                    sb.append(c.getString(c.getColumnIndex(DB.C_EW_ENGWORD)))
                            .append(d)
                            .append(c.getString(c.getColumnIndex(DB.C_EW_TRANSCRIPTION)))
                            .append(d)
                            .append(c.getString(c.getColumnIndex(DB.C_EW_RUSTRANSLATE)))
                            .append(d);
                    if (categories == null) {
                        sb.append(c.getString(c.getColumnIndex(DB.C_EW_CATEGORY)));
                    } else {
                        List<String> catsInWord = strToList(c.getString(c.getColumnIndex(DB.C_EW_CATEGORY)), ",", true);
                        catsInWord.retainAll(catsToWrite);
                        sb.append(listToStr(catsInWord, ", "));
                    }
                    i++;
                    if (c.moveToNext()) {
                        sb.append("\r\n");
                        c.moveToPrevious();
                    } else
                        break;
                } while (c.moveToNext());
                bw = new BufferedWriter(new OutputStreamWriter(
                        getContentResolver().openOutputStream(fileUri, "rwt"), StandardCharsets.UTF_8));
                bw.write(sb.toString());
                bw.close();
                Toast.makeText(getApplicationContext(), s(R.string.amount_save_rows) + ": " + i,
                        Toast.LENGTH_LONG).show();
                // отправляем в Firebase инфу по записи слов в файл
                if (mFBAnalytics != null) {
                    app.readWriteFileEvent(GlobApp.WRITE_FILE, "txt", i + "");
                }
            } else {
                Toast.makeText(getApplicationContext(), s(R.string.no_data_for_download),
                        Toast.LENGTH_LONG).show();
            }
            return i;
        } catch (IOException e) {
            // сообщение о то, что в текущей версии Android нельзя созранить файл
            String message = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ?
                    s(R.string.android_version30_rescription) :
                    s(R.string.android_version23_rescription);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    s(R.string.failed_download) + "\r\n" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (c != null)
                    c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * установление суммы оплаченных покупок
     */
    public void setAmountDonate(int amountDonate) {
        this.amountDonate = amountDonate;
    }

    private String s(int res) {
        return getResources().getString(res);
    }

    /**
     * Получение клаватуры
     *
     * @return keyboard - клавиатура
     */
    public CustomKeyboard getKeyboard() {
        return keyboard;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            // если выгружаем данные в файл
            if (requestCode == REQUEST_LOAD) {
                uriDownloadFile = data.getData();
                String fileName = DocumentFile.fromSingleUri(this, uriDownloadFile).getName(); //имя файла без пути
                String[] pathArr = uriDownloadFile.getPath().split(":");
                String path = pathArr[pathArr.length - 1];
                fileName = path.endsWith(fileName) ? path : fileName; // а теперь имя файла с путем
                etOpenFileName.setText(fileName);
            } else if (requestCode == REQUEST_SAVE) { // если загружаем данные в файл
                uriUploadFile = data.getData();
                String fileName = DocumentFile.fromSingleUri(this, uriUploadFile).getName(); //имя файла без пути
                String[] pathArr = uriUploadFile.getPath().split(":");
                String path = pathArr[pathArr.length - 1];
                fileName = path.endsWith(fileName) ? path : fileName; // а теперь имя файла с путем
                etSaveFileName.setText(fileName);
            } else if (requestCode == GET_CAT_UPLOAD_XLS) {
                writeXlsFile(uriUploadFile, data.getStringExtra("categories"));
            } else if (requestCode == GET_CAT_UPLOAD_TXT) {
                writeTxtFile(uriUploadFile, data.getStringExtra("categories"));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.setGroupVisible(R.id.group_help, true);
        menu.setGroupVisible(R.id.group_no_ad, amountDonate <= 0);
        menu.setGroupVisible(R.id.group_donate, true);
        menu.setGroupVisible(R.id.group_about, true);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        if (item.getItemId() == android.R.id.home) { // обрабатываем кнопку "назад" в ActionBar
            Log.d("myLogs", "keyboard = " + keyboard);
            if (keyboard != null && keyboard.isCustomKeyboardVisible()) {
                keyboard.hideCustomKeyboard();
                Log.d("myLogs", "keyboard.isCustomKeyboardVisible() = " + keyboard.isCustomKeyboardVisible());
            } else {
                super.onBackPressed();
            }
            return true;
        } else if (item.getItemId() == R.id.help) { // вызов помощи
            Intent intent = new Intent(this, Help.class);
            intent.putExtra("idhelp", 2);
            startActivity(intent);
        } else if (item.getItemId() == R.id.no_ad) { // вызов страницы с отключением рекламы
            startActivity(new Intent(this, NoAd.class));
        } else if (item.getItemId() == R.id.donate) { // вызов страницы с благодарностью
            startActivity(new Intent(this, Thanks.class));
        } else if (item.getItemId() == R.id.about) { // вызов страницы О программе
            startActivity(new Intent(this, About.class));
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * делаем возможность получать llPayInformation извне, чтобы из Pay иметь возможность его скрывать
     */
    public LinearLayout getLlPayInformation() {
        return llPayInformation;
    }

    // обработка кнопки назад
    @Override
    public void onBackPressed() {
        if (keyboard != null && keyboard.isCustomKeyboardVisible()) {
            keyboard.hideCustomKeyboard();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        //показываем рекламу или нет
        String amountDonateStr = db != null ? db.getValueByVariable(DB.AMOUNT_DONATE) : null;
        amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        ViewGroup.LayoutParams params = llAdMobData.getLayoutParams();
        if (amountDonate > 0) {
            params.height = 0;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName() + " без отображения");
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Log.i("myLogs", "загружена баннерная реклама в " + getClass().getSimpleName());
        }
        llAdMobData.setLayoutParams(params);

        if (menu != null) {
            menu.setGroupVisible(R.id.group_no_ad, amountDonate <= 0);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        try {
            if (uriUploadFile != null) {
                AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().
                        openAssetFileDescriptor(uriUploadFile, "r");
                if (fileDescriptor != null && fileDescriptor.getLength() == 0) {
                    DocumentsContract.deleteDocument(getApplicationContext().getContentResolver(), uriUploadFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("myLogs", "onDestroy EditData");
        super.onDestroy();
    }
}