package ru.handy.android.wm.learning;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.handy.android.wm.R;

/**
 * Адаптер для заполнения данных для списка категорий
 * Created by Андрей on 22.03.2015.
 */
public class CategoryAdapter extends BaseAdapter {

    public static String NEW_CATEGORIES = "NEW_CATEGORIES";
    private final Context ctx;
    private final LayoutInflater lInflater;
    private final ArrayList<Category> objects;
    private boolean isStatistics; // true - статистика, false - все категории
    // список для регистрации слушателей про изменению галок на категориях
    private List<OnCatCheckedListener> listeners = new ArrayList<OnCatCheckedListener>();

    public CategoryAdapter(Context context, ArrayList<Category> categories, boolean isStatistics) {
        ctx = context;
        objects = categories;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isStatistics = isStatistics;
    }

    /**
     * получение списка категорий
     *
     * @return спиок категорий
     */
    public ArrayList<Category> getCategories() {
        return objects;
    }

    /**
     * получение индекса в списке категорий по данной категории
     *
     * @param category искомая категория
     * @return индекс искомой категории. Возвращает -1, если категория не найдена
     */
    public int getIdByCategory(Category category) {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getName().equals(category.getName()))
                return i;
        }
        return -1;
    }

    /**
     * получение индекса в списке категорий по имени данной категории
     *
     * @param catName имя искомой категории
     * @return индекс искомой категории. Возвращает -1, если категория не найдена
     */
    public int getIdByCatName(String catName) {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getName().equals(catName))
                return i;
        }
        return -1;
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
        tvAmount.setOnTouchListener((v, event) -> {
            setBackground(cbCategory, tvAmount, event, defBackground);
            return false;
        });
        cbCategory.setOnTouchListener((v, event) -> {
            setBackground(cbCategory, tvAmount, event, defBackground);
            return false;
        });
        tvAmount.setOnClickListener(v -> {
            Category c = (Category) getItem((Integer) v.getTag());
            c.setChecked(!c.isChecked());
            cbCategory.setChecked(c.isChecked());
        });
        //без установки здесь слушателя контекстного меню на кажоде поле не получается устновить его на саму listView
        cbCategory.setOnCreateContextMenuListener(null);
        tvAmount.setOnCreateContextMenuListener(null);
        // пишем позицию
        cbCategory.setTag(position);
        tvAmount.setTag(position);
        cbCategory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int pos = (Integer) buttonView.getTag();
            // ставим галку в категории такую же как и в чек-боксе
            ((Category) CategoryAdapter.this.getItem(pos)).setChecked(isChecked);
            // передаем информацию в слушатели
            for (OnCatCheckedListener listener : listeners)
                listener.onCatChecked(objects, pos, isChecked);
        });
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
            tvAmount.setTypeface(null, Typeface.BOLD);

            tvAmount.setText(span);
        } else {
            if (cat.getAmount() == 0) {
                tvAmount.setText("");
            } else {
                String text = s(R.string.learned) + "\n" + cat.getAmountRight() + " " + s(R.string.from) + " " + cat.getAmount();
                Spannable span = new SpannableString(text);
                if (cat.getAmountRight() > 0) {
                    span.setSpan(new StyleSpan(Typeface.BOLD), 8, 8 + (cat.getAmountRight() + "").length()
                            , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                span.setSpan(new StyleSpan(Typeface.BOLD), text.length() - (cat.getAmount() + "").length()
                        , text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvAmount.setTypeface(null, Typeface.NORMAL);
                tvAmount.setText(span);
            }
        }
        return view;
    }

    /**
     * добавление слушателей по изменению галок на категориях
     *
     * @param listenerToAdd слушатель к добавлению
     */
    public void addListener(OnCatCheckedListener listenerToAdd) {
        listeners.add(listenerToAdd);
    }

    private void setBackground(CheckBox cbCategory, TextView tvAmount, MotionEvent event, Drawable defBackground) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            cbCategory.setBackgroundResource(R.color.bright_blue);
            tvAmount.setBackgroundResource(R.color.bright_blue);
        } else {
            cbCategory.setBackground(defBackground);
            tvAmount.setBackground(defBackground);
        }
    }

    // содержимое корзины
    public ArrayList<Category> getCheckedCategories() {
        ArrayList<Category> cats = new ArrayList<>();
        for (Category c : objects) {
            // если выбрана
            if (c.isChecked())
                cats.add(c);
        }
        return cats;
    }

    private String s(int res) {
        return ctx.getResources().getString(res);
    }
}