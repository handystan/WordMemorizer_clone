package ru.handy.android.wm.learning;

/**
 * Класс в котором записываются параметры категории
 * Created by Андрей on 22.03.2015.
 */
public class Category {

    private String name; // имя категории
    private int amount = 0; // кол-во слов в категории
    private boolean checked = false; // показывает выбрана категория в layout или нет
    private int amountRight = 0; // кол-во правильных ответов
    private int amountWrong = 0; // кол-во неправильных ответов
    private boolean isShow = true; // показывать или нет данную категорию в

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
     * испоьзуется для получения статистики по категориям
     * @param name имя категории
     * @param amountRight кол-во правильных ответов
     * @param amountWrong кол-во неправильных ответов
     */
    public Category(String name, int amountRight, int amountWrong) {
        this.name = name;
        this.amountRight = amountRight;
        this.amountWrong = amountWrong;
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

}
