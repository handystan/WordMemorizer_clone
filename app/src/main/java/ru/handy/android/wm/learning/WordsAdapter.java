package ru.handy.android.wm.learning;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import ru.handy.android.wm.GlobApp;
import ru.handy.android.wm.R;
import ru.handy.android.wm.dictionary.WordDescription;

/**
 * Адаптер для заполнения данных для списка слов
 * Created by Андрей on 26.04.2015.
 */
public class WordsAdapter extends BaseAdapter {

    private GlobApp app;
    private AppCompatActivity act;
    private Context ctx;
    private LayoutInflater lInflater;
    private ArrayList<Word> objects;

    WordsAdapter(GlobApp app, ArrayList<Word> words) {
        this.app = app;
        ctx = app.getApplicationContext();
        objects = words;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    WordsAdapter(GlobApp app, AppCompatActivity act, ArrayList<Word> words) {
        this.app = app;
        this.act = act;
        ctx = app.getApplicationContext();
        objects = words;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_word, parent, false);
        }

        final Word word = ((Word) getItem(position));
        final Drawable defBackground = view.getBackground();

        // заполняем View в пункте списка данными из категорий: текст чек-бокса и кол-во
        final TextView tvEngWord = (TextView) view.findViewById(R.id.tvEngWord);
        final TextView tvTransc = (TextView) view.findViewById(R.id.tvTransc);
        final TextView tvRusWord = (TextView) view.findViewById(R.id.tvRusWord);
        final ImageView ivSound = (ImageView) view.findViewById(R.id.ivSound);
        tvEngWord.setText(word.getEngWord());
        tvTransc.setText(word.getTranscription());
        tvRusWord.setText(word.getRusTranslate());
        if (act != null) {
            // чтобы контекстное меню корректно работало
            act.registerForContextMenu(tvEngWord);
            act.unregisterForContextMenu(tvEngWord);
            act.registerForContextMenu(tvTransc);
            act.unregisterForContextMenu(tvTransc);
            act.registerForContextMenu(tvRusWord);
            act.unregisterForContextMenu(tvRusWord);
            act.registerForContextMenu(ivSound);
            act.unregisterForContextMenu(ivSound);

            tvEngWord.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    record(word, WordDescription.class);
                }
            });
            tvTransc.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    record(word, WordDescription.class);
                }
            });
            tvRusWord.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    record(word, WordDescription.class);
                }
            });
        }
        ivSound.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                app.speak(word.getEngWord());
            }
        });
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WordsAdapter.this.setBackground(tvEngWord, tvTransc, tvRusWord, ivSound, event, defBackground);
                return true;
            }
        });
        tvEngWord.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WordsAdapter.this.setBackground(tvEngWord, tvTransc, tvRusWord, ivSound, event, defBackground);
                return false;
            }
        });
        tvTransc.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WordsAdapter.this.setBackground(tvEngWord, tvTransc, tvRusWord, ivSound, event, defBackground);
                return false;
            }
        });
        tvRusWord.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WordsAdapter.this.setBackground(tvEngWord, tvTransc, tvRusWord, ivSound, event, defBackground);
                return false;
            }
        });
        ivSound.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WordsAdapter.this.setBackground(tvEngWord, tvTransc, tvRusWord, ivSound, event, defBackground);
                return false;
            }
        });
        // пишем позицию
        tvEngWord.setTag(position);
        tvTransc.setTag(position);
        tvRusWord.setTag(position);
        ivSound.setTag(position);
        return view;
    }

    /**
     * показывает всю информацию по английскому слову
     *
     * @param word          - слово, которое нужно показать (отредактировать, добавить)
     * @param activityClass - класс для вызываемого Activity
     */
    private void record(Word word, Class<? extends AppCompatActivity> activityClass) {
        Intent intent = new Intent(act, activityClass);
        if (word != null) {
            intent.putExtra("c_ew_id", word.getId());
            intent.putExtra("c_ew_engword", word.getEngWord());
            intent.putExtra("c_ew_transcription", word.getTranscription());
            intent.putExtra("c_ew_rustranslate", word.getRusTranslate());
            intent.putExtra("c_ew_category", word.getCategory());
        }
        act.startActivity(intent);
    }

    private void setBackground(TextView tvEngWord, TextView tvTransc, TextView tvRusWord
            , ImageView ivSound, MotionEvent event, Drawable defBackground) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            tvEngWord.setBackgroundResource(R.color.bright_blue);
            tvTransc.setBackgroundResource(R.color.bright_blue);
            tvRusWord.setBackgroundResource(R.color.bright_blue);
            ivSound.setBackgroundResource(R.color.bright_blue);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                tvEngWord.setBackground(defBackground);
                tvTransc.setBackground(defBackground);
                tvRusWord.setBackground(defBackground);
                ivSound.setBackground(defBackground);
            } else {
                tvEngWord.setBackgroundDrawable(defBackground);
                tvTransc.setBackgroundDrawable(defBackground);
                tvRusWord.setBackgroundDrawable(defBackground);
                ivSound.setBackgroundDrawable(defBackground);
            }
        }
    }

    /**
     * обновление или добавление слова в адаптере
     * @param word
     */
    public void updateWord (Word word) {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).equals(word)) {
                objects.set(i, word);
                return;
            }
        }
        objects.add(word);
        Collections.sort(objects);
    }

    /**
     * удаление слова из адаптере
     * @param idWord - id удаляемого слова
     */
    public void deleteWord (int idWord) {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getId() == idWord) {
                objects.remove(i);
                return;
            }
        }
    }
}
