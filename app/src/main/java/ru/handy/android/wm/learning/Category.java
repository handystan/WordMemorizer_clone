package ru.handy.android.wm.learning;

import java.util.Comparator;
import java.util.Locale;

/**
 * Класс в котором записываются параметры категории
 * Created by Андрей on 22.03.2015.
 */
public class Category implements Comparable, Comparator {

    private String name; // имя категории
    private int amount = 0; // кол-во слов в категории
    private boolean checked = false; // показывает выбрана категория в layout через checkBox или нет
    private int amountRight = 0; // кол-во правильных ответов
    private int amountWrong = 0; // кол-во неправильных ответов
    private boolean isShow = true; // показывать или нет данную категорию в layout (через фильтр текстового поля)

    /**
     * используется для вывода всех категорий
     * @param name имя категории
     * @param amount кол-во слов в категории
     * @param checked показывает выбрана категория в layout или нет
     */
    public Category(String name, int amount, boolean checked) {
        this.name = name;
        this.amount = amount;
        this.checked = checked;
    }

    /**
     * используется для вывода всех категорий
     * @param name имя категории
     * @param amount кол-во слов в категории
     */
    public Category(String name, int amount) {
        this(name, amount, false);
    }

    /**
     * используется для получения статистики по категориям
     * @param name имя категории
     * @param amountRight кол-во правильных ответов
     * @param amountWrong кол-во неправильных ответов
     */
    public Category(String name, int amountRight, int amountWrong) {
        this.name = name;
        this.amountRight = amountRight;
        this.amountWrong = amountWrong;
    }

    /**
     * используется для получения статистики по категориям
     * @param name имя категории
     * @param amount кол-во слов в категории
     * @param amountRight кол-во слов, которые отгаданы хоть 1 раз
     * @param amountWrong кол-во слов, которые ни разу не отгадали и хоть 1 раз отгадали не верно
     */
    public Category(String name, int amount, int amountRight, int amountWrong) {
        this(name, amountRight, amountWrong);
        this.amount = amount;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmountRight() {
        return amountRight;
    }

    public void setAmountRight(int amountRight) {
        this.amountRight = amountRight;
    }

    public int getAmountWrong() {
        return amountWrong;
    }

    public void setAmountWrong(int amountWrong) {
        this.amountWrong = amountWrong;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    /**
     * сравнивает 2 строки по правилам сортировки категорий
     *
     * @param str1 строка 1
     * @param str2 строка 2
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    public static int compare2Strings (String str1, String str2) {
        String s1 = str1.toLowerCase(new Locale("ru"));
        String s2 = str2.toLowerCase(new Locale("ru"));
        String s1p = s1;
        String s2p = s2;
        int n1 = 0;
        for (int i = 1; i < s1.length(); i++) {
            try {
                n1 = Integer.parseInt(s1.substring(s1.length()
                        - i));
                s1p = s1.substring(0, s1.length() - i);
            } catch (Exception e) {
                break;
            }
        }
        int n2 = 0;
        for (int i = 1; i < s2.length(); i++) {
            try {
                n2 = Integer.parseInt(s2.substring(s2.length()
                        - i));
                s2p = s2.substring(0, s2.length() - i);
            } catch (Exception e) {
                break;
            }
        }
        if (s2.equals("")) {
            return -1;
        } else if (s1.equals("")) {
            return 1;
        } else if (!s1.startsWith("прочее") && s2.startsWith("прочее")) {
            return -1;
        } else if (s1.startsWith("прочее") && !s2.startsWith("прочее")) {
            return 1;
        } else if (n1 != 0 && n2 != 0 && s1p.equals(s2p)) {
            return n1 - n2;
        }
        return s1.compareToIgnoreCase(s2);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Object o) {
        return compare2Strings(this.name, ((Category) o).name);
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the
     * second.
     * @throws NullPointerException if an argument is null and this
     *                              comparator does not permit null arguments
     * @throws ClassCastException   if the arguments' types prevent them from
     *                              being compared by this comparator.
     */
    @Override
    public int compare(Object o1, Object o2) {
        return compare2Strings(((Category) o1).name, (((Category) o2).name));
    }
}
