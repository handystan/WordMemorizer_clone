package ru.handy.android.wm.dictionary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;

public class MyMultiAutoCompleteTextView extends AppCompatMultiAutoCompleteTextView {

	public MyMultiAutoCompleteTextView(Context context) {
		super(context);
		start();
	}

	public MyMultiAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		start();
	}

	public MyMultiAutoCompleteTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		start();
	}

	private void start() {
		setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showDropDown();
				}
			}
		});
	}

	@Override
	public boolean enoughToFilter() {
		return true;
	}
	
	@Override
	public void replaceText(CharSequence text) {
        super.replaceText(text);
        showDropDown();
    }
}
