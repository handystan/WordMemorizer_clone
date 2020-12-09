/**
 * 
 */
package ru.handy.android.wm.learning;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * @author Администратор
 * класс в котором инкапсулирована информация по слову
 */
public class Word implements Parcelable, Comparable<Word> {

	int id;
	private String engWord;
	private String transcription;
	private String rusTranslate;
	private String category;

	public Word(int id, String engWord, String transcription,
				String rusTranslate) {
		super();
		this.id = id;
		this.engWord = engWord;
		this.transcription = transcription;
		this.rusTranslate = rusTranslate;
	}

	public Word(int id, String engWord, String transcription,
				String rusTranslate, String category) {
		super();
		this.id = id;
		this.engWord = engWord;
		this.transcription = transcription;
		this.rusTranslate = rusTranslate;
		this.category = category;
	}

	protected Word(Parcel in) {
		id = in.readInt();
		engWord = in.readString();
		transcription = in.readString();
		rusTranslate = in.readString();
		category = in.readString();
	}

	/**
	 * Describe the kinds of special objects contained in this Parcelable's
	 * marshalled representation.
	 *
	 * @return a bitmask indicating the set of special object types marshalled
	 * by the Parcelable.
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Flatten this object in to a Parcel.
	 *
	 * @param dest  The Parcel in which the object should be written.
	 * @param flags Additional flags about how the object should be written.
	 *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(engWord);
		dest.writeString(transcription);
		dest.writeString(rusTranslate);
		dest.writeString(category);
	}

	public static final Creator<Word> CREATOR = new Creator<Word>() {
		@Override
		public Word createFromParcel(Parcel in) {
			return new Word(in);
		}

		@Override
		public Word[] newArray(int size) {
			return new Word[size];
		}
	};

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getEngWord() {
		return engWord;
	}
	public void setEngWord(String engWord) {
		this.engWord = engWord;
	}
	public String getTranscription() {
		return transcription;
	}
	public void setTranscription(String transcription) {
		this.transcription = transcription;
	}
	public String getRusTranslate() {
		return rusTranslate;
	}
	public void setRusTranslate(String rusTranslate) {
		this.rusTranslate = rusTranslate;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "Word{" + "id=" + id + ", engWord='" + engWord + '\'' + ", transcription='" +
				transcription + '\'' + ", rusTranslate='" + rusTranslate + '\'' +
				", category='" + category + '\'' + '}';
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Word other = (Word) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/**
	 * Compares this object to the specified object to determine their relative
	 * order.
	 *
	 * @param another the object to compare to this instance.
	 * @return a negative integer if this instance is less than {@code another};
	 * a positive integer if this instance is greater than
	 * {@code another}; 0 if this instance has the same order as
	 * {@code another}.
	 * @throws ClassCastException if {@code another} cannot be converted into something
	 *                            comparable to {@code this} instance.
	 */
	@Override
	public int compareTo(Word another) {
		return this.engWord.compareToIgnoreCase(another.engWord);
	}
}
