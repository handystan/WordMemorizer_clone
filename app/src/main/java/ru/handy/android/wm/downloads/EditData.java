package ru.handy.android.wm.downloads;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.mozilla.universalchardet.UniversalDetector;

import ru.handy.android.wm.About;
import ru.handy.android.wm.CustomKeyboard;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.Help;
import ru.handy.android.wm.R;
import ru.handy.android.wm.Thanks;
import ru.handy.android.wm.learning.Learning;
import ru.handy.android.wm.setting.Pay;
import ru.handy.android.wm.setting.Utils;

public class EditData extends AppCompatActivity {

    private static final int REQUEST_LOAD = 0;
    private static final int REQUEST_SAVE = 1;
    LinearLayout llPayInformation;
    Button bPay;
    Button bOpenDialog;
    Button bSaveDialog;
    Button bDownload;
    Button bUpload;
    EditText etOpenFileName;
    EditText etSaveFileName;
    CheckBox cbDelete;
    Spinner sFileType;
    EditText etSemicolon;
    TextView tvSemicolon;
    ImageView ivShare1;
    ImageView ivShare2;
    CustomKeyboard keyboard;
    DB db;
    Learning learning = null;
    private Uri uriDownloadFile; // uri загружаемого файла
    private Uri uriUploadFile; // uri выгружаемого файла
    private GlobApp app;
    private Pay pay;
    private int amountDonate = 0;
    private boolean isFromOldDB = false;
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
        ivShare1 = findViewById(R.id.ivShare1);
        ivShare2 = findViewById(R.id.ivShare2);

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        String fromOldDB = db.getValueByVariable(DB.OLD_FREE_DB);
        isFromOldDB = fromOldDB != null && !fromOldDB.equals("0");
        if (isFromOldDB || amountDonate > 0) { // если старая БД или приложение оплачено, то скрываем layout с предложением оплаты
            llPayInformation.getLayoutParams().height = 0;
        } else { // если новая БД и приложение не оплачено, то показываем layout с предложением оплаты
            pay = new Pay(this);
            bPay.setOnClickListener(v -> Utils.alertForPay("", EditData.this, pay, db, false));
        }

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
                    etSemicolon.setVisibility(View.INVISIBLE);
                    tvSemicolon.setVisibility(View.INVISIBLE);
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
                readXlsFile(uriDownloadFile);
            } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xlsm") || fileName.endsWith(".xlsb")) {
                Toast.makeText(getApplicationContext(), s(R.string.should_be_xls), Toast.LENGTH_LONG).show();
            } else {
                readTxtFile(uriDownloadFile);
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
                        writeXlsFile(uriUploadFile);
                    }
                } else if (sFileType.getSelectedItemPosition() == 1) { // выгружается txt-файл
                    if (fileName.endsWith(".xls")) { // если пользователь выбрал не то расширение файла
                        Toast.makeText(getApplicationContext(), s(R.string.shouldnt_be_xls), Toast.LENGTH_LONG).show();
                    } else if (delimiter.equals("")) {
                        Toast.makeText(getApplicationContext(), s(R.string.empty_delimiter),
                                Toast.LENGTH_LONG).show();
                    } else {
                        writeTxtFile(uriUploadFile);
                    }
                }
                // в зависимости от того новый это файл или нет, устанавливаем доступность кнопки по отправке файла
                AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().
                        openAssetFileDescriptor(uriUploadFile, "r");
                ivShare2.setVisibility(fileDescriptor.getLength() > 0 ? View.VISIBLE : View.GONE);
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
                            ivShare2.setVisibility(fileDescriptor.getLength() > 0 ? View.VISIBLE : View.GONE);
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
     * Метод для чтения данных из экселевского файла, в котором создается строка
     * вставки строки типа такой: insert into myTable (col1,col2) select aValue
     * ,anotherValue union select moreValue,evenMoreValue union select...
     *
     * @param fileUri uri файла
     * @return количество загруженных строк
     */
    private int readXlsFile(Uri fileUri) {
        Workbook w = null;
        try {
            db.beginTransaction();
            if (cbDelete.isChecked())
                db.delAll(DB.T_ENGWORDS);
            WorkbookSettings wbs = new WorkbookSettings();
            wbs.setEncoding("ISO-8859-1");
            w = Workbook.getWorkbook(getContentResolver().openInputStream(fileUri), wbs);
            Sheet sheet = w.getSheet(0);
            StringBuilder sb = new StringBuilder();
            ArrayList<String> args = new ArrayList<>();
            int r = 1;
            for (int j = 0; j < sheet.getRows(); j++) {
                String engWord = sheet.getCell(0, j).getContents();
                if (engWord != null && !engWord.equals("")) {
                    if (sb.indexOf("INSERT") == -1) {
                        sb.append("INSERT INTO ").append(DB.T_ENGWORDS)
                                .append(" (").append(DB.C_EW_ENGWORD)
                                .append(", ").append(DB.C_EW_TRANSCRIPTION)
                                .append(", ").append(DB.C_EW_RUSTRANSLATE)
                                .append(", ").append(DB.C_EW_CATEGORY)
                                .append(") SELECT ?, ?, ?, ? ");
                    } else {
                        sb.append(" UNION SELECT ?, ?, ?, ? ");
                    }
                    for (int i = 0; i < 4; i++) {
                        args.add(sheet.getCell(i, j).getContents());
                        r++;
                    }
                }
                // такая порционность вставки строк нужна из-за того, что кол-во
                // вставляемых параметров не должно быть больше 999
                if (r % 995 >= 0 && r % 995 <= 3 && sb.indexOf("INSERT") != -1) {
                    db.execSQL(sb.toString(), args.toArray());
                    sb = new StringBuilder();
                    args = new ArrayList<>();
                }
                // если приложение не оплачено, то позволяем вставить только 10 строк
                if (!isFromOldDB && amountDonate == 0 && j == 9 && sb.indexOf("INSERT") != -1) {
                    break;
                }
            }
            if (sb.indexOf("INSERT") != -1) {
                db.execSQL(sb.toString(), args.toArray());
            }
            if (r > 1) {
                db.setTransactionSuccessful();
                Toast.makeText(getApplicationContext(),
                        s(R.string.amount_load_rows) + ": " + (r / 4),
                        Toast.LENGTH_LONG).show();
            }
            // отправляем в Firebase инфу по чтению слов из файла
            if (mFBAnalytics != null) {
                app.readWriteFileEvent(GlobApp.READ_FILE, "xls", (r / 4) + "");
            }
            return r / 4;
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), s(R.string.file_read_error) + "\n" +
                    e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            db.endTransaction();
            if (w != null)
                w.close();
        }
        return 0;
    }

    /**
     * Выгрузка словаря в экселевский файл с расширением xls
     *
     * @param fileUri uri файла
     * @return количество выгруженных строк
     */
    private int writeXlsFile(Uri fileUri) {
        Cursor c = null;
        WritableWorkbook wb = null;
        int i = 0;
        try {
            c = db.getAllWords();
            if (c.moveToFirst()) {
                wb = Workbook.createWorkbook(getContentResolver().openOutputStream(fileUri));
                WritableSheet sheet = wb.createSheet("WM", 0);
                do {
                    sheet.addCell(new Label(0, i, c.getString(c.getColumnIndex(DB.C_EW_ENGWORD))));
                    sheet.addCell(new Label(1, i, c.getString(c.getColumnIndex(DB.C_EW_TRANSCRIPTION))));
                    sheet.addCell(new Label(2, i, c.getString(c.getColumnIndex(DB.C_EW_RUSTRANSLATE))));
                    sheet.addCell(new Label(3, i, c.getString(c.getColumnIndex(DB.C_EW_CATEGORY))));
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
     * Метод для чтения данных из текстового файла, в котором создается строка
     * вставки строки типа такой: insert into myTable (col1,col2) select aValue
     * ,anotherValue union select moreValue,evenMoreValue union select...
     *
     * @param fileUri uri файла
     * @return количество загруженных строк
     */
    private int readTxtFile(Uri fileUri) {
        String row;
        BufferedReader br = null;
        try {
            db.beginTransaction();
            if (cbDelete.isChecked()) {
                db.delAll(DB.T_ENGWORDS);
            }
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
                StringBuilder sb = new StringBuilder();
                ArrayList<String> args = new ArrayList<>();
                StringBuilder sbRegex = new StringBuilder();
                sbRegex.append("^([^").append(delimiter).append("]*)([").append(delimiter).append("])([^").append(delimiter).
                        append("]*)(\\2)([^").append(delimiter).append("]*)((\\2)([^").append(delimiter).append("]*))?$");
                patt = Pattern
                        .compile(sbRegex.toString());
                StringBuilder wrongRow = new StringBuilder();
                int intWrongRow = 0;
                int r = 1;
                while ((row = br.readLine()) != null) {
                    Matcher match = patt.matcher(row);
                    if (match.find()) {
                        if (sb.indexOf("INSERT") == -1) {
                            sb.append("INSERT INTO ").append(DB.T_ENGWORDS)
                                    .append(" (").append(DB.C_EW_ENGWORD)
                                    .append(", ").append(DB.C_EW_TRANSCRIPTION)
                                    .append(", ").append(DB.C_EW_RUSTRANSLATE)
                                    .append(", ").append(DB.C_EW_CATEGORY)
                                    .append(") SELECT ?, ?, ?, ? ");
                        } else {
                            sb.append(" UNION SELECT ?, ?, ?, ? ");
                        }
                        args.add(match.group(1));
                        args.add(match.group(3));
                        args.add(match.group(5));
                        args.add(match.group(8));
                    } else {
                        wrongRow.append(wrongRow.toString().equals("") ? "" : ", ").append(r);
                        intWrongRow++;
                    }
                    r++;
                    // такая порционность вставки строк нужна из-за того, что кол-во
                    // вставляемых параметров не должно быть больше 999
                    if ((r - intWrongRow) % 249 == 0 && sb.indexOf("INSERT") != -1) {
                        db.execSQL(sb.toString(), args.toArray());
                        sb = new StringBuilder();
                        args = new ArrayList<>();
                    }
                    // если приложение не оплачено, то позволяем вставить только 10 строк
                    if (!isFromOldDB && amountDonate == 0 && (r - intWrongRow) == 11 && sb.indexOf("INSERT") != -1) {
                        break;
                    }
                }
                if (sb.indexOf("INSERT") != -1)
                    db.execSQL(sb.toString(), args.toArray());
                if (intWrongRow < r - intWrongRow) {
                    db.setTransactionSuccessful();
                    if (wrongRow.toString().equals("")) {
                        Toast.makeText(getApplicationContext(),
                                s(R.string.amount_load_rows) + ": " + (r - intWrongRow - 1)
                                        + adviceEncoding,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                s(R.string.amount_load_rows) + ": " + (r - intWrongRow - 1)
                                        + "\r\n" + s(R.string.not_load_rows)
                                        + wrongRow + ".\r\n"
                                        + s(R.string.template_in_help),
                                Toast.LENGTH_LONG).show();
                    }
                    // отправляем в Firebase инфу по чтению слов из файла
                    if (mFBAnalytics != null) {
                        app.readWriteFileEvent(GlobApp.READ_FILE, "txt", (r - intWrongRow - 1) + "");
                    }
                    return r - 1;
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
     * @param fileUri uri файла
     * @return количество выгруженных строк
     */
    private int writeTxtFile(Uri fileUri) {
        Cursor c = null;
        BufferedWriter bw = null;
        // разделитель строк
        String d = etSemicolon.getText().toString();
        int i = 0;
        try {
            StringBuilder sb = new StringBuilder();
            c = db.getAllWords();
            if (c.moveToFirst()) {
                do {
                    sb.append(c.getString(c.getColumnIndex(DB.C_EW_ENGWORD)))
                            .append(d)
                            .append(c.getString(c
                                    .getColumnIndex(DB.C_EW_TRANSCRIPTION)))
                            .append(d)
                            .append(c.getString(c
                                    .getColumnIndex(DB.C_EW_RUSTRANSLATE)))
                            .append(d)
                            .append(c.getString(c
                                    .getColumnIndex(DB.C_EW_CATEGORY)));
                    i++;
                    if (c.moveToNext()) {
                        sb.append("\r\n");
                        c.moveToPrevious();
                    } else
                        break;
                } while (c.moveToNext());
                bw = new BufferedWriter(new OutputStreamWriter(
                        getContentResolver().openOutputStream(fileUri), StandardCharsets.UTF_8));
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
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            // do nothing
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.setGroupVisible(R.id.group_addrec, false);
        menu.setGroupVisible(R.id.group_dictionary, false);
        menu.setGroupVisible(R.id.group_clear_hist, false);
        menu.setGroupVisible(R.id.group_statistics, false);
        menu.setGroupVisible(R.id.group_idata, false);
        menu.setGroupVisible(R.id.group_resetStat, false);
        menu.setGroupVisible(R.id.group_action_settings, false);
        menu.setGroupVisible(R.id.group_exit, false);
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
        } else if (item.getItemId() == R.id.ihelp) { // вызов помощи
            Intent intent = new Intent(this, Help.class);
            intent.putExtra("idhelp", 2);
            startActivity(intent);
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
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        try {
            if (uriUploadFile != null) {
                AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().
                        openAssetFileDescriptor(uriUploadFile, "r");
                if (fileDescriptor.getLength() == 0) {
                    DocumentsContract.deleteDocument(getApplicationContext().getContentResolver(), uriUploadFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("myLogs", "onDestroy EditData");
        super.onDestroy();
        if (pay != null) pay.close();
    }
}