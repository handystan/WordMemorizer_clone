package ru.handy.android.wm.learning;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import java.util.ArrayList;

import ru.handy.android.wm.R;
import ru.handy.android.wm.statistics.Statistics;

/**
 * Адаптер для заполнения данных для списка категорий
 * Created by Андрей on 22.03.2015.
 */
public class CategoryAdapter extends BaseAdapter {

    public static String NEW_CATEGORIES = "NEW_CATEGORIES";
    private Context ctx;
    private LayoutInflater lInflater;
    private ArrayList<Category> objects;
    // обработчик для чекбоксов
    OnCheckedChangeListener checkChangList = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            // меняем галочку в чек-боксе
            ((Category) getItem((Integer) buttonView.getTag())).setChecked(isChecked);
        }
    };
    private boolean isStatistics; // true - статистика, false - все категории

    public CategoryAdapter(Context context, ArrayList<Category> categories, boolean isStatistics) {
        ctx = context;
        objects = categories;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isStatistics = isStatistics;
    }

    // кол-во элементов
    @Override
    public int getCount() {
        int j = 0;
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).isShow()) j++;
        }
        return j;
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        int j = -1;
        int i;
        for (i = 0; i < objects.size(); i++) {
            if (objects.get(i).isShow()) {
                j++;
                if (j == position) break;
            }
        }
        return objects.get(i);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        int j = -1;
        int i;
        for (i = 0; i < objects.size(); i++) {
            if (objects.get(i).isShow()) {
                j++;
                if (j == position) break;
            }
        }
        return i;
    }

    // пункт списка
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.category, parent, false);
        }

        final Category cat = ((Category) getItem(position));
        final Drawable defBackground = view.getBackground();

        // заполняем View в пункте списка данными из категорий: текст чек-бокса и кол-во
        final TextView tvAmount = (TextView) view.findViewById(R.id.tvAmount);
        final CheckBox cbCategory = (CheckBox) view.findViewById(R.id.cbCategory);
        cbCategory.setText(cat.getName());
        // присваиваем чекбоксу обработчик
        tvAmount.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setBackground(cbCategory, tvAmount, event, defBackground);
                return false;
            }
        });
        cbCategory.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setBackground(cbCategory, tvAmount, event, defBackground);
                return false;
            }
        });
        tvAmount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Category c = (Category) getItem((Integer) v.getTag());
                    c.setChecked(!c.isChecked());
                    cbCategory.setChecked(c.isChecked());
            }
        });
        // пишем позицию
        cbCategory.setTag(position);
        tvAmount.setTag(position);
        cbCategory.setOnCheckedChangeListener(checkChangList);
        // проставляем начальные данные чек-бокса
        cbCategory.setChecked(cat.isChecked());
        if (isStatistics) {
            String text1 = cat.getAmountWrong() + "";
            String allText = text1 + (cat.getAmountRight() < 10 ? "      " : cat.getAmountRight() < 100 ? "    " : "  ") +
                    cat.getAmountRight();
            Spannable span = new SpannableString(allText);
            span.setSpan(new ForegroundColorSpan(Color.RED), 0,
                    text1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.parseColor("#00BB00")), text1.length(),
                    allText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvAmount.setText(span);
        } else {
            tvAmount.setText(s(R.string.ofWords) + " " + (cat.getAmount() == 0 ? "  " : cat.getAmount()));
        }
        return view;
    }

    private void setBackground(CheckBox cbCategory, TextView tvAmount, MotionEvent event, Drawable defBackground) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            cbCategory.setBackgroundResource(R.color.bright_blue);
            tvAmount.setBackgroundResource(R.color.bright_blue);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                cbCategory.setBackground(defBackground);
                tvAmount.setBackground(defBackground);
            } else {
                cbCategory.setBackgroundDrawable(defBackground);
                tvAmount.setBackgroundDrawable(defBackground);
            }
        }
    }

    // содержимое корзины
    public ArrayList<Category> getCheckedCategories() {
        ArrayList<Category> cats = new ArrayList<Category>();
        for (Category c : objects) {
            // если выбрана
            if (c.isChecked())
                cats.add(c);
        }
        return cats;
    }

    private void chooseCategory(String catName) {
        ((Statistics) ctx).getIntent().putExtra(NEW_CATEGORIES, catName);
        ((Statistics) ctx).setResult(Activity.RESULT_OK, ((Statistics) ctx).getIntent());
        ((Statistics) ctx).finish();
    }

    private String s(int res) {
        return ctx.getResources().getString(res);
    }
}