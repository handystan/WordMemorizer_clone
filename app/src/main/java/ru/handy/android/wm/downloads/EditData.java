package ru.handy.android.wm.downloads;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import ru.handy.android.wm.CustomKeyboard;
import ru.handy.android.wm.DB;
import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;
import ru.handy.android.wm.learning.Learning;
import ru.handy.android.wm.setting.Pay;
import ru.handy.android.wm.setting.Utils;

public class EditData extends AppCompatActivity {

    private static int REQUEST_LOAD = 0;
    private static int REQUEST_SAVE = 1;
    final private int PERMISSION_READ = 11; // разрешение для чтения файла
    final private int PERMISSION_WRITE = 12; // разрешение для записи файла
    final private int PERMISSION_READ2 = 13;
    final private int PERMISSION_WRITE2 = 14;
    LinearLayout llPayInformation;
    Button bPay;
    Button bOpenDialog;
    Button bSaveDialog;
    Button bUpload;
    Button bDownload;
    EditText etOpenFileName;
    EditText etSaveFileName;
    CheckBox cbDelete;
    Spinner sFileType;
    EditText etSemicolon;
    TextView tvSemicolon;
    CustomKeyboard keyboard;
    DB db;
    Learning learning = null;
    private GlobApp app;
    private String fileName; // имя файла, который будет считываться или записываться
    private Menu menu;
    private Pay pay;
    private int amountDonate = 0;
    private boolean isFromOldDB = false;
    private Tracker mTracker; // трекер для Google analitics, чтобы отслеживать активности пользователей

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_data);

        app = (GlobApp) getApplication(); // получаем доступ к приложению
        mTracker = app.getDefaultTracker(); // Obtain the shared Tracker instance.
        db = app.getDb(); // открываем подключение к БД

        // устанавливаем отдельную клавиатуру для поля с транскрипцией
        keyboard = new CustomKeyboard(this, R.id.etSemicolon,
                R.id.specKeyboardView, R.xml.specialkeyboard);

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

        llPayInformation = (LinearLayout) findViewById(R.id.llPayInformation);
        bPay = (Button) findViewById(R.id.bPay);
        bOpenDialog = (Button) findViewById(R.id.bDialogUpload);
        bSaveDialog = (Button) findViewById(R.id.bDialogDownload);
        bUpload = (Button) findViewById(R.id.bUpload);
        bDownload = (Button) findViewById(R.id.bDownload);
        etOpenFileName = (EditText) findViewById(R.id.etFileUpload);
        etSaveFileName = (EditText) findViewById(R.id.etFileDownload);
        cbDelete = (CheckBox) findViewById(R.id.cbDelete);
        sFileType = (Spinner) findViewById(R.id.sFileType);
        etSemicolon = (EditText) findViewById(R.id.etSemicolon);
        tvSemicolon = (TextView) findViewById(R.id.tvSemicolon);

        String amountDonateStr = db.getValueByVariable(DB.AMOUNT_DONATE);
        amountDonate = amountDonateStr == null ? 0 : Integer.parseInt(amountDonateStr);
        String fromOldDB = db.getValueByVariable(DB.OLD_FREE_DB);
        isFromOldDB = (fromOldDB == null || fromOldDB.equals("0")) ? false : true;
        if (isFromOldDB || amountDonate > 0) { // если старая БД или приложение оплачено, то скрываем layout с предложением оплаты
            llPayInformation.getLayoutParams().height = 0;
        } else { // если новая БД и приложение не оплачено, то показываем layout с предложением оплаты
            pay = new Pay(this);
            bPay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.alertForPay("", EditData.this, pay, db, false);
                }
            });
        }

        // адаптер для типа файла
        String[] spinnerData = {s(R.string.spinner_xls),
                s(R.string.spinner_txt)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
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
                        new InputFilter() {
                            public CharSequence filter(CharSequence source, int start, int end,
                                                       Spanned dest, int dstart, int dend) {
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
                }
        );

        bOpenDialog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // проверка на наличие разрешения на чтение из файла
                if (isGrantedPermission(Manifest.permission.READ_EXTERNAL_STORAGE
                        , s(R.string.attention), s(R.string.rationale_for_file_system), PERMISSION_READ2)) {
                    Intent intent = new Intent(EditData.this, FileDialog.class);
                    intent.putExtra(FileDialog.START_PATH, Environment
                            .getExternalStorageDirectory().getPath());
                    intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
                    intent.putExtra(FileDialog.CAN_CREATE_FILE, false);
                    startActivityForResult(intent, REQUEST_LOAD);
                }
            }
        });

        bSaveDialog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // проверка на наличие разрешения на запись в файл
                if (isGrantedPermission(Manifest.permission.READ_EXTERNAL_STORAGE
                        , s(R.string.attention), s(R.string.rationale_for_file_system), PERMISSION_WRITE2)) {
                    Intent intent = new Intent(EditData.this, FileDialog.class);
                    intent.putExtra(FileDialog.START_PATH, Environment
                            .getExternalStorageDirectory().getPath());
                    intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
                    startActivityForResult(intent, REQUEST_SAVE);
                }
            }
        });

        bUpload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fileName = etOpenFileName.getText().toString();
                if (fileName == null || fileName.equals("")) {
                    Toast.makeText(getApplicationContext(), s(R.string.choose_file), Toast.LENGTH_LONG).show();
                    return;
                }
                // проверка на наличие разрешения на чтение из файла
                if (isGrantedPermission(Manifest.permission.READ_EXTERNAL_STORAGE
                        , s(R.string.attention), s(R.string.rationale_for_read), PERMISSION_READ)) {
                    if (fileName.endsWith(".xls"))
                        readXlsFile(fileName);
                    else
                        readTxtFile(fileName);
                }
            }
        });

        bDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fileName = etSaveFileName.getText().toString();
                String delimiter = etSemicolon.getText().toString();
                if (fileName == null || fileName.equals("")) {
                    Toast.makeText(getApplicationContext(), s(R.string.choose_file), Toast.LENGTH_LONG).show();
                    return;
                }
                // проверка на наличие разрешения на запись в файл
                if (isGrantedPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , s(R.string.attention), s(R.string.rationale_for_write), PERMISSION_WRITE)) {
                    if (sFileType.getSelectedItemPosition() == 0) {
                        if (!fileName.endsWith(".xls")) {
                            Toast.makeText(getApplicationContext(), s(R.string.should_be_xls),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        writeXlsFile(fileName);
                    } else {
                        if (fileName.endsWith(".xls")) {
                            Toast.makeText(getApplicationContext(), s(R.string.shouldnt_be_xls),
                                    Toast.LENGTH_LONG).show();
                            return;
                        } else if (delimiter == null || delimiter.equals("")) {
                            Toast.makeText(getApplicationContext(), s(R.string.empty_delimiter),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        writeTxtFile(fileName);
                    }
                }
            }
        });
    }

    /**
     * проверка на наличие разрешения на определенное действие
     *
     * @param permission     - разрешение, наличие которого проверяется
     * @param title          - надпись в тексте сообщения
     * @param message        - текст самого сообщения
     * @param codePermission - индентификатор, сообщающий дальнейшие действия
     * @return true - разрешние уже есть, false - разрешения нет
     */
    private boolean isGrantedPermission(final String permission, String title, String message, final int codePermission) {
        if (ActivityCompat.checkSelfPermission(EditData.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(EditData.this, permission)) {
                new AlertDialog.Builder(EditData.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(s(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(EditData.this
                                        , new String[]{permission}, codePermission);
                            }
                        })
                        .setNegativeButton(s(R.string.cancel), null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(EditData.this
                        , new String[]{permission}, codePermission);
            }
            return false;
        }
        return true;
    }

    // метод, который выполняется при предоставлении / не предоставлении разрешния (permission)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (fileName.endsWith(".xls"))
                        readXlsFile(fileName);
                    else
                        readTxtFile(fileName);
                } else {
                    Toast.makeText(getApplicationContext(), s(R.string.unabled_permissions), Toast.LENGTH_LONG).show();
                }
                break;
            case PERMISSION_READ2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(EditData.this, FileDialog.class);
                    intent.putExtra(FileDialog.START_PATH, Environment
                            .getExternalStorageDirectory().getPath());
                    intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
                    intent.putExtra(FileDialog.CAN_CREATE_FILE, false);
                    startActivityForResult(intent, REQUEST_LOAD);
                } else {
                    Toast.makeText(getApplicationContext(), s(R.string.unabled_permissions), Toast.LENGTH_LONG).show();
                }
                break;
            case PERMISSION_WRITE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String delimiter = etSemicolon.getText().toString();
                    if (sFileType.getSelectedItemPosition() == 0) {
                        if (!fileName.endsWith(".xls")) {
                            Toast.makeText(getApplicationContext(), s(R.string.should_be_xls),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        writeXlsFile(fileName);
                    } else {
                        if (fileName.endsWith(".xls")) {
                            Toast.makeText(getApplicationContext(), s(R.string.shouldnt_be_xls),
                                    Toast.LENGTH_LONG).show();
                            return;
                        } else if (delimiter == null || delimiter.equals("")) {
                            Toast.makeText(getApplicationContext(), s(R.string.empty_delimiter),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        writeTxtFile(fileName);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), s(R.string.unabled_permissions), Toast.LENGTH_LONG).show();
                }
                break;
            case PERMISSION_WRITE2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(EditData.this, FileDialog.class);
                    intent.putExtra(FileDialog.START_PATH, Environment
                            .getExternalStorageDirectory().getPath());
                    intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
                    startActivityForResult(intent, REQUEST_SAVE);
                } else {
                    Toast.makeText(getApplicationContext(), s(R.string.unabled_permissions), Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void setLearning(Learning learning) {
        this.learning = learning;
    }

    /**
     * Метод для чтения данных из экселевского файла, в котором создается строка
     * вставки строки типа такой: insert into myTable (col1,col2) select aValue
     * ,anotherValue union select moreValue,evenMoreValue union select...
     */
    private int readXlsFile(String fileName) {
        Workbook w = null;
        try {
            db.beginTransaction();
            if (cbDelete.isChecked())
                db.delAll(DB.T_ENGWORDS);
            WorkbookSettings wbs = new WorkbookSettings();
            wbs.setEncoding("ISO-8859-1");
            w = Workbook.getWorkbook(new File(fileName), wbs);
            Sheet sheet = w.getSheet(0);
            StringBuilder sb = new StringBuilder();
            ArrayList<String> args = new ArrayList<String>();
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
                    args = new ArrayList<String>();
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
            return r / 4;
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (BiffException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            db.endTransaction();
            if (w != null)
                w.close();
        }
        return 0;
    }

    // запись БД в экселевский файл
    private int writeXlsFile(String fileName) {
        Cursor c = null;
        WritableWorkbook wb = null;
        int i = 0;
        try {
            c = db.getAllWords();
            if (c.moveToFirst()) {
                wb = Workbook.createWorkbook(new File(fileName));
                WritableSheet sheet = wb.createSheet("WM", 0);
                do {
                    sheet.addCell(new Label(0, i, c.getString(c
                            .getColumnIndex(DB.C_EW_ENGWORD))));
                    sheet.addCell(new Label(1, i, c.getString(c
                            .getColumnIndex(DB.C_EW_TRANSCRIPTION))));
                    sheet.addCell(new Label(2, i, c.getString(c
                            .getColumnIndex(DB.C_EW_RUSTRANSLATE))));
                    sheet.addCell(new Label(3, i, c.getString(c
                            .getColumnIndex(DB.C_EW_CATEGORY))));
                    i++;
                } while (c.moveToNext());
                wb.write();
                wb.close();
                Toast.makeText(getApplicationContext(), s(R.string.amount_save_rows) + ": " + i,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), s(R.string.no_data_for_download),
                        Toast.LENGTH_LONG).show();
            }
            return i;
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), s(R.string.android_version_rescription), Toast.LENGTH_LONG).show();
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
     */
    private int readTxtFile(String fileName) {
        String row = "";
        BufferedReader br = null;
        try {
            db.beginTransaction();
            if (cbDelete.isChecked()) {
                db.delAll(DB.T_ENGWORDS);
            }
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(fileName)), "UTF8"));
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
            // после определения разделителя получаем данные
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(fileName)), "UTF8"));
            StringBuilder sb = new StringBuilder();
            ArrayList<String> args = new ArrayList<String>();
            patt = Pattern
                    .compile("^([^" + delimiter + "]*)([" + delimiter + "])([^" + delimiter + "]*)(\\2)([^" + delimiter + "]*)((\\2)([^" + delimiter + "]*))?$");
            String wrongRow = "";
            int intWrongRow = 0;
            int r = 1;
            while ((row = br.readLine()) != null) {
                Matcher match = patt.matcher(row);
                if (match.find()) {
                    if (delimiter.equals(""))
                        delimiter = match.group(2);
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
                    wrongRow = wrongRow + (wrongRow.equals("") ? "" : ", ") + r;
                    intWrongRow++;
                }
                r++;
                // такая порционность вставки строк нужна из-за того, что кол-во
                // вставляемых параметров не должно быть больше 999
                if ((r - intWrongRow) % 249 == 0 && sb.indexOf("INSERT") != -1) {
                    db.execSQL(sb.toString(), args.toArray());
                    sb = new StringBuilder();
                    args = new ArrayList<String>();
                }
                // если приложение не оплачено, то позволяем вставить только 10 строк
                if (!isFromOldDB && amountDonate == 0 && (r - intWrongRow) == 11 && sb.indexOf("INSERT") != -1) {
                    break;
                }
            }
            if (sb.indexOf("INSERT") != -1)
                db.execSQL(sb.toString(), args.toArray());
            if (!delimiter.equals("")) {
                db.setTransactionSuccessful();
                if (wrongRow.equals(""))
                    Toast.makeText(getApplicationContext(),
                            s(R.string.amount_load_rows) + ": " + (r - intWrongRow - 1)
                                    + ".\r\n" + s(R.string.must_be_utf8),
                            Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(),
                            s(R.string.amount_load_rows) + ": " + (r - intWrongRow - 1)
                                    + "\r\n" + s(R.string.not_load_rows)
                                    + wrongRow + ".\r\n"
                                    + s(R.string.template_in_help),
                            Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        s(R.string.file_not_fit_template) + " "
                                + s(R.string.template_in_help),
                        Toast.LENGTH_LONG).show();
            }
            return r - 1;
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            db.endTransaction();
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
            }
        }
        return 0;
    }

    // запись БД в текстовый файл
    private int writeTxtFile(String fileName) {
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
                        new FileOutputStream(fileName), "UTF8"));
                bw.write(sb.toString());
                bw.close();
                Toast.makeText(getApplicationContext(), s(R.string.amount_save_rows) + ": " + i,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), s(R.string.no_data_for_download),
                        Toast.LENGTH_LONG).show();
            }
            return i;
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), s(R.string.android_version_rescription), Toast.LENGTH_LONG).show();
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
            if (requestCode == REQUEST_LOAD) {
                etOpenFileName.setText(data
                        .getStringExtra(FileDialog.RESULT_PATH));
            } else if (requestCode == REQUEST_SAVE) {
                etSaveFileName.setText(data
                        .getStringExtra(FileDialog.RESULT_PATH));
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            // do nothing
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Операции для выбранного пункта меню
        switch (item.getItemId()) {
            case android.R.id.home: // обрабатываем кнопку "назад" в ActionBar
                Log.d("myLogs", "keyboard = " + keyboard);
                if (keyboard != null && keyboard.isCustomKeyboardVisible()) {
                    keyboard.hideCustomKeyboard();
                    Log.d("myLogs", "keyboard.isCustomKeyboardVisible() = " + keyboard.isCustomKeyboardVisible());
                } else {
                    super.onBackPressed();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        if (mTracker != null) {
            Log.i("myLogs", "Setting screen name: " + this.getLocalClassName());
            mTracker.setScreenName("Activity " + this.getLocalClassName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d("myLogs", "onDestroy EditData");
        super.onDestroy();
        if (pay != null) pay.close();
    }
}