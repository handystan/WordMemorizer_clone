package ru.handy.android.wm;

import java.util.ArrayList;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class CustomKeyboard {

	/** A link to the KeyboardView that is used to render this CustomKeyboard. */
	private KeyboardView mKeyboardView;
	/** A link to the activity that hosts the {@link #mKeyboardView}. */
	private Activity mHostActivity;
	private View fragment;
	private EditText edittext;

	/** The key (code) handler. */

	private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {

		public final static int CodeDelete = -5; // Keyboard.KEYCODE_DELETE
		public final static int CodeCancel = -3; // Keyboard.KEYCODE_CANCEL

		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			// NOTE We can say ?<Key android:codes=”49,50″ … >’ in the xml
			// file; all codes come in keyCodes, the first in this list in
			// primaryCode
			// Get the EditText and its Editable
			Editable editable = edittext.getText();
			int start = edittext.getSelectionStart();
			int end = edittext.getSelectionEnd();
			if (primaryCode == CodeCancel) {
				hideCustomKeyboard();
			} else if (primaryCode == CodeDelete) {
				if (editable != null && start > 0) {
					if (start == end) {
						editable.delete(start - 1, end);
					} else {
						editable.delete(start, end);
					}
				}
			} else { // insert character
				if (editable != null) {
					editable.replace(start, end,
							Character.toString((char) primaryCode));
				}
			}
		}

		@Override
		public void onPress(int arg0) {
		}

		@Override
		public void onRelease(int primaryCode) {
		}

		@Override
		public void onText(CharSequence text) {
		}

		@Override
		public void swipeDown() {
			hideCustomKeyboard();
		}

		@Override
		public void swipeLeft() {
		}

		@Override
		public void swipeRight() {
		}

		@Override
		public void swipeUp() {

		}
	};

	/**
	 * Create a custom keyboard, that uses the KeyboardView (with resource id
	 * <var>viewid</var>) of the <var>host</var> activity, and load the keyboard
	 * layout from xml file <var>layoutid</var> (see {@link Keyboard} for
	 * description). Note that the <var>host</var> activity must have a
	 * <var>KeyboardView</var> in its layout (typically aligned with the bottom
	 * of the activity). Note that the keyboard layout xml file may include key
	 * codes for navigation; see the constants in this class for their values.
	 * Note that to enable EditText’s to use this custom keyboard, call the
	 * {@link #registerEditText(int)}.
	 * 
	 * @param host
	 *            The hosting activity.
	 * @param resid
	 *            The id of the EditText.
	 * @param viewid
	 *            The id of the KeyboardView.
	 * @param layoutid
	 *            The id of the xml file containing the keyboard layout.
	 */
	public CustomKeyboard(Activity host, int resid, int viewid, int layoutid) {
		mHostActivity = host;
		mKeyboardView = (KeyboardView) mHostActivity.findViewById(viewid);
		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutid));
		mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the
		registerEditText(resid);
//		edittext.setKeyListener(null);
		// preview balloons
		mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
		// Hide the standard keyboard initially
//		mHostActivity.getWindow().setSoftInputMode(
//				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	/**
	 * Вспомогательный контруктор. �?спользуется, если клавиатура вызывается из
	 * фрагмента, а не из Activity
	 * 
	 * @param host
	 *            The hosting activity.
	 * @param resid
	 *            The id of the EditText.
	 * @param viewid
	 *            The id of the KeyboardView.
	 * @param layoutid
	 *            The id of the xml file containing the keyboard layout.
	 * @param fragment
	 *            фрагмент, в котором располагается клавиатура
	 */
	public CustomKeyboard(Activity host, int resid, int viewid, int layoutid,
			View fragment) {
		mHostActivity = host;
		this.fragment = fragment;
		mKeyboardView = (KeyboardView) fragment.findViewById(viewid);
		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutid));
		mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the
		registerEditText(resid);
		edittext.setKeyListener(null);
		// preview balloons
		mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
		// Hide the standard keyboard initially
		mHostActivity.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	/** Returns whether the CustomKeyboard is visible. */
	public boolean isCustomKeyboardVisible() {
		return mKeyboardView.getVisibility() == View.VISIBLE;
	}

	/**
	 * Make the CustomKeyboard visible, and hide the system keyboard for view v.
	 */
	public void showCustomKeyboard(View v) {
		mKeyboardView.setVisibility(View.VISIBLE);
		mKeyboardView.setEnabled(true);
		if (v != null)
			((InputMethodManager) mHostActivity
					.getSystemService(Activity.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	/** Make the CustomKeyboard invisible. */
	public void hideCustomKeyboard() {
		mKeyboardView.setVisibility(View.GONE);
		mKeyboardView.setEnabled(false);
	}

	/**
	 * Register <var>EditText<var> with resource id <var>resid</var> (on the
	 * hosting activity) for using this custom keyboard.
	 * 
	 * @param resid
	 *            The resource id of the EditText that registers to the custom
	 *            keyboard.
	 */
	private void registerEditText(int resid) {
		// Find the EditText �?resid’
		ArrayList<View> views; // список элементов
		if (fragment == null) {
			edittext = (EditText) mHostActivity.findViewById(resid);
			views = getAllChildren(mHostActivity
					.findViewById(android.R.id.content));
		} else {
			edittext = (EditText) fragment.findViewById(resid);
			views = getAllChildren(fragment);
		}
		// Make the custom keyboard appear

		edittext.setOnFocusChangeListener(new OnFocusChangeListener() {

			// NOTE By setting the on focus listener, we can show the custom
			// keyboard when the edit box gets focus, but also hide it when
			// the edit box loses focus
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					showCustomKeyboard(v);
				else
					hideCustomKeyboard();

			}
		});

		for (View view : views) {
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v == edittext)
						showCustomKeyboard(v);
					else
						hideCustomKeyboard();

				}
			});
		}
	}

	// получение всех элементов. находящихся в Activity или Fragment
	private ArrayList<View> getAllChildren(View v) {
		if (!(v instanceof ViewGroup)) {
			ArrayList<View> viewArrayList = new ArrayList<View>();
			viewArrayList.add(v);
			return viewArrayList;
		}
		ArrayList<View> result = new ArrayList<View>();
		ViewGroup viewGroup = (ViewGroup) v;
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			View child = viewGroup.getChildAt(i);
			ArrayList<View> viewArrayList = new ArrayList<View>();
			viewArrayList.add(v);
			viewArrayList.addAll(getAllChildren(child));
			result.addAll(viewArrayList);
		}
		return result;
	}
}