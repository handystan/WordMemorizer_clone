package ru.handy.android.wm.learning;

import java.util.ArrayList;

/**
 * интерфейс регистрирующий события по выбору или снятию галок в категориях в layout
 */
public interface OnCatCheckedListener {
    /**
     *
     * @param categories список категорий
     * @param position позиция категории, по которой изменилась галка
     * @param isChecked новое значение галки
     */
    void onCatChecked(ArrayList<Category> categories, int position, boolean isChecked);
}
