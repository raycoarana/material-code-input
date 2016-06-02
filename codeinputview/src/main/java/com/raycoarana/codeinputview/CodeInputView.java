package com.raycoarana.codeinputview;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.raycoarana.codeinputview.data.FixedStack;
import com.raycoarana.codeinputview.model.Underline;

public class CodeInputView extends View {

	private static final int DEFAULT_CODES = 6;

	private static final Pattern KEYCODE_NUMERIC_PATTERN = Pattern.compile("KEYCODE_(\\d)");
	private static final Pattern KEYCODE_TEXT_PATTERN = Pattern.compile("KEYCODE_(\\w)");

	private static final int TEXT_INPUT_TYPE = 1;
	private static final int NUMERIC_INPUT_TYPE = 2;

	private static final long ANIMATION_DURATION = 500;
	private static final long DISPATCH_COMPLETE_EVENT_DELAY = 200;

	private FixedStack<Character> mCharacters;
	private Underline mUnderlines[];
	private Paint mUnderlinePaint;
	private Paint mUnderlineSelectedPaint;
	private Paint mTextPaint;
	private float mUnderlineReduction;
	private float mUnderlineStrokeWidth;
	private float mUnderlineSelectedStrokeWidth;
	private float mUnderlineErrorStrokeWidth;
	private float mUnderlineWidth;
	private float mReduction;
	private float mTextSize;
	private float mTextMarginBottom;
	private int mHeight;
	private int mUnderlineAmount;
	private int mUnderlineColor;
	private int mUnderlineSelectedColor;
	private int mTextColor;
	private List<InputCodeCompletedListener> mInputCompletedListeners = new ArrayList<>();
	private boolean mIsEditable = true;
	private int mInputType = NUMERIC_INPUT_TYPE;
	private int mUnderLineY;
	private ValueAnimator mReductionAnimator;
	private ValueAnimator mHideCharactersAnimator;
	private ValueAnimator mErrorColorAnimator;
	private ValueAnimator mErrorTextAnimator;
	private float mCharactersBaseline;
	private String mErrorMessage;
	private int mErrorColor;
	private float mErrorTextSize;
	private float mErrorTextMarginTop;
	private Paint mErrorTextPaint;
	private int mXOffset;
	private boolean mAnimateOnComplete;

	public CodeInputView(Context context) {
		super(context);
		init(null);
	}

	public CodeInputView(Context context, AttributeSet attributeset) {
		super(context, attributeset);
		init(attributeset);
	}

	public CodeInputView(Context context, AttributeSet attributeset, int defStyledAttrs) {
		super(context, attributeset, defStyledAttrs);
		init(attributeset);
	}

	private void init(AttributeSet attributeset) {
		initDefaultAttributes();
		initCustomAttributes(attributeset);
		initDataStructures();
		initPaint();
		initAnimator();
		initViewOptions();
	}

	private void initDefaultAttributes() {
		mUnderlineColor = getColor(R.color.underline_default_color);
		mUnderlineWidth = getContext().getResources().getDimension(R.dimen.underline_width);
		mUnderlineStrokeWidth = getContext().getResources().getDimension(R.dimen.underline_stroke_width);
		mUnderlineSelectedColor = getColor(R.color.underline_selected_color);
		mUnderlineSelectedStrokeWidth = getContext().getResources().getDimension(R.dimen.underline_selected_stroke_width);
		mUnderlineErrorStrokeWidth = getContext().getResources().getDimension(R.dimen.underline_error_stroke_width);
		mUnderlineReduction = getContext().getResources().getDimension(R.dimen.section_reduction);
		mUnderlineAmount = DEFAULT_CODES;
		mTextColor = getColor(R.color.text_color);
		mTextSize = getContext().getResources().getDimension(R.dimen.text_size);
		mTextMarginBottom = getContext().getResources().getDimension(R.dimen.text_margin_bottom);
		mErrorColor = getColor(R.color.error_color);
		mErrorTextSize = getContext().getResources().getDimension(R.dimen.error_text_size);
		mErrorTextMarginTop = getContext().getResources().getDimension(R.dimen.error_text_margin_top);
		mReduction = mUnderlineReduction;
	}

	@SuppressWarnings("deprecation")
	private int getColor(@ColorRes int resId) {
		if (VERSION.SDK_INT < VERSION_CODES.M) {
			return getContext().getResources().getColor(resId);
		} else {
			return getContext().getResources().getColor(resId, null);
		}
	}

	private void initCustomAttributes(AttributeSet attributeset) {

		TypedArray attributes = getContext().obtainStyledAttributes(attributeset, R.styleable.code_input_view);

		mUnderlineColor = attributes.getColor(R.styleable.code_input_view_underline_color, mUnderlineColor);
		mUnderlineWidth = attributes.getDimension(R.styleable.code_input_view_underline_width, mUnderlineWidth);
		mUnderlineStrokeWidth = attributes.getDimension(R.styleable.code_input_view_underline_stroke_width, mUnderlineStrokeWidth);
		mUnderlineSelectedColor = attributes.getColor(R.styleable.code_input_view_underline_selected_color, mUnderlineSelectedColor);
		mUnderlineSelectedStrokeWidth = attributes.getDimension(R.styleable.code_input_view_underline_selected_stroke_width, mUnderlineSelectedStrokeWidth);
		mUnderlineErrorStrokeWidth = attributes.getDimension(R.styleable.code_input_view_underline_error_stroke_width, mUnderlineErrorStrokeWidth);
		mUnderlineReduction = attributes.getDimension(R.styleable.code_input_view_underline_section_reduction, mUnderlineReduction);
		mUnderlineAmount = attributes.getInt(R.styleable.code_input_view_codes, mUnderlineAmount);
		mInputType = attributes.getInt(R.styleable.code_input_view_input_type, mInputType);
		mTextColor = attributes.getInt(R.styleable.code_input_view_text_color, mTextColor);
		mTextSize = attributes.getDimension(R.styleable.code_input_view_text_size, mTextSize);
		mTextMarginBottom = attributes.getDimension(R.styleable.code_input_view_text_margin_bottom, mTextMarginBottom);
		mErrorColor = attributes.getInt(R.styleable.code_input_view_error_color, mErrorColor);
		mErrorTextSize = attributes.getDimension(R.styleable.code_input_view_error_text_size, mErrorTextSize);
		mErrorTextMarginTop = attributes.getDimension(R.styleable.code_input_view_error_text_margin_top, mErrorTextMarginTop);
		mAnimateOnComplete = attributes.getBoolean(R.styleable.code_input_view_animate_on_complete, true);
		attributes.recycle();
	}

	private void initDataStructures() {
		mUnderlines = new Underline[mUnderlineAmount];
		mCharacters = new FixedStack<>();
		mCharacters.setMaxSize(mUnderlineAmount);
	}

	private void initPaint() {
		mUnderlinePaint = new Paint();
		mUnderlinePaint.setColor(mUnderlineColor);
		mUnderlinePaint.setStrokeWidth(mUnderlineStrokeWidth);
		mUnderlinePaint.setStyle(android.graphics.Paint.Style.STROKE);
		mUnderlineSelectedPaint = new Paint();
		mUnderlineSelectedPaint.setColor(mUnderlineSelectedColor);
		mUnderlineSelectedPaint.setStrokeWidth(mUnderlineSelectedStrokeWidth);
		mUnderlineSelectedPaint.setStyle(android.graphics.Paint.Style.STROKE);
		mTextPaint = new Paint();
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setColor(mTextColor);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mErrorTextPaint = new Paint();
		mErrorTextPaint.setTextSize(mErrorTextSize);
		mErrorTextPaint.setColor(mErrorColor);
		mErrorTextPaint.setAntiAlias(true);
		mErrorTextPaint.setTextAlign(Paint.Align.CENTER);

	}

	private void initAnimator() {
		mReductionAnimator = ValueAnimator.ofFloat(mUnderlineReduction, mUnderlineWidth / 2);
		mReductionAnimator.setDuration(ANIMATION_DURATION);
		mReductionAnimator.addUpdateListener(new ReductionAnimatorListener());
		mReductionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		mHideCharactersAnimator = ValueAnimator.ofFloat(0, mTextPaint.getFontSpacing() + mTextMarginBottom);
		mHideCharactersAnimator.setDuration(ANIMATION_DURATION);
		mHideCharactersAnimator.addUpdateListener(new HideCharactersAnimatorListener());
		mHideCharactersAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		mErrorColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mUnderlineColor, mErrorColor);
		mErrorColorAnimator.setDuration(ANIMATION_DURATION);
		mErrorColorAnimator.addUpdateListener(new ErrorColorAnimatorListener());
		mErrorColorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		mErrorTextAnimator = ValueAnimator.ofInt(0, 255);
		mErrorTextAnimator.setDuration(ANIMATION_DURATION);
		mErrorTextAnimator.addUpdateListener(new ErrorTextAnimatorListener());
		mErrorTextAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
	}

	private void initViewOptions() {
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mUnderLineY = (int) (mTextMarginBottom + mTextPaint.getFontSpacing());
		mHeight = h;
		mXOffset = (int) Math.abs(w - (mUnderlineAmount * mUnderlineWidth)) / 2;
		initUnderline();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
			float desiredWidth = mUnderlineAmount * mUnderlineWidth;
			width = Math.min((int) desiredWidth, width);
		}
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
			float desiredHeight = mErrorTextPaint.getFontSpacing() + mErrorTextMarginTop + mTextPaint.getFontSpacing() + mTextMarginBottom;
			height = Math.min((int) desiredHeight, height);
		}
		setMeasuredDimension(width, height);
	}

	private void initUnderline() {
		for (int i = 0; i < mUnderlineAmount; i++) {
			mUnderlines[i] = createPath(i, mUnderlineWidth);
		}
	}

	private Underline createPath(int position, float sectionWidth) {
		float fromX = mXOffset + sectionWidth * (float) position;
		return new Underline(fromX, mUnderLineY, fromX + sectionWidth, mUnderLineY);
	}

	private void showKeyboard() {
		InputMethodManager inputmethodmanager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputmethodmanager.showSoftInput(this, InputMethodManager.RESULT_UNCHANGED_SHOWN);
		inputmethodmanager.viewClicked(this);
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		switch (mInputType) {
			case NUMERIC_INPUT_TYPE:
				outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
				break;
			case TEXT_INPUT_TYPE:
				outAttrs.inputType = InputType.TYPE_CLASS_TEXT;
				break;
		}
		return new BaseInputConnection(this, false) {
			@Override
			public boolean deleteSurroundingText(int beforeLength, int afterLength) {
				return deleteCharacter();
			}
		};
	}

	/**
	 * Detects the del key and delete the numbers
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
		if (keyCode == KeyEvent.KEYCODE_DEL) {
			deleteCharacter();
		}
		return super.onKeyDown(keyCode, keyevent);
	}

	private boolean deleteCharacter() {
		boolean canDelete = mCharacters.size() > 0 && mIsEditable;
		if (canDelete) {
			restoreState();
			mCharacters.pop();
			clearError();
		}
		return canDelete;
	}

	private void restoreState() {
		if (mCharacters.size() == mUnderlineAmount &&
			!mReductionAnimator.getAnimatedValue().equals(mUnderlineReduction) &&
				mAnimateOnComplete) {
			mReductionAnimator.reverse();
			mHideCharactersAnimator.reverse();
		}
	}

	/**
	 * Capture the keyboard events but only if are A-Z 0-9
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent keyevent) {
		String text = KeyEvent.keyCodeToString(keyCode);
		Pattern pattern;
		if (mInputType == NUMERIC_INPUT_TYPE) {
			pattern = KEYCODE_NUMERIC_PATTERN;
		} else {
			pattern = KEYCODE_TEXT_PATTERN;
		}
		Matcher matcher = pattern.matcher(text);
		if (mIsEditable && matcher.matches()) {
			char character = matcher.group(1).charAt(0);
			if (mCharacters.size() < mUnderlineAmount) {
				mCharacters.push(character);
				if (mCharacters.size() == mUnderlineAmount) {
					dispatchComplete();
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private void dispatchComplete() {
		mIsEditable = false;
		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mAnimateOnComplete) {
					mReductionAnimator.start();
					mHideCharactersAnimator.start();
				}
				notifyCompleted();
			}
		}, DISPATCH_COMPLETE_EVENT_DELAY);
	}

	private void notifyCompleted() {
		String code = getCode();
		for (InputCodeCompletedListener listener : mInputCompletedListeners) {
			listener.onCompleted(code);
		}
	}

	/**
	 * Enables that the user can edit the code or not
	 * @param value true will let the user change the code in the input, false will ignore any action from the user
	 */
	public void setEditable(boolean value) {
		mIsEditable = value;
		invalidate();
	}

	/**
	 * Adds a listener that will be fired once the user complete all the code characters
	 *
	 * @param listener listener to add
	 */
	public void addInputCodeCompletedListener(InputCodeCompletedListener listener) {
		mInputCompletedListeners.add(listener);
	}

	/**
	 * When a touch is detected the view need to focus and animate if is necessary
	 */
	@Override
	public boolean onTouchEvent(MotionEvent motionevent) {
		if (motionevent.getAction() == 0) {
			requestFocus();
			showKeyboard();
		}
		return super.onTouchEvent(motionevent);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		for (int i = 0; i < mUnderlines.length; i++) {
			Underline sectionpath = mUnderlines[i];
			float fromX = sectionpath.getFromX() + mReduction;
			float fromY = sectionpath.getFromY();
			float toX = sectionpath.getToX() - mReduction;
			float toY = sectionpath.getToY();
			if (mCharacters.size() > i && mCharacters.size() != 0) {
				canvas.save();
				canvas.clipRect(0, 0, toX, toY);
				drawCharacter(fromX, toX, mCharacters.get(i), canvas);
				canvas.restore();
			}
			drawSection(i, fromX, fromY, toX, toY, canvas);
		}
		drawErrorMessage(canvas);
		invalidate();
	}

	private void drawErrorMessage(Canvas canvas) {
		if (mErrorMessage == null) {
			return;
		}

		int x = getWidth() / 2;
		int y = (int) (mUnderLineY + mErrorTextMarginTop + mErrorTextPaint.getFontSpacing());
		canvas.drawText(mErrorMessage, x, y, mErrorTextPaint);
	}

	private void drawSection(int position, float fromX, float fromY, float toX, float toY,
			Canvas canvas) {
		Paint paint = mUnderlinePaint;
		if (position == mCharacters.size() && hasFocus()) {
			paint = mUnderlineSelectedPaint;
		}
		canvas.drawLine(fromX, fromY, toX, toY, paint);
	}

	private void drawCharacter(float fromX, float toX, Character character, Canvas canvas) {
		float actualWidth = toX - fromX;
		float centerWidth = actualWidth / 2;
		float centerX = fromX + centerWidth;
		canvas.drawText(character.toString(), centerX, (mHeight - mTextMarginBottom - mErrorTextMarginTop -
						mErrorTextPaint.getFontSpacing()) +
						mCharactersBaseline,
				mTextPaint);
	}

	/**
	 * Gets the current code as an String
	 *
	 * @return the code
	 */
	public String getCode() {
		StringBuilder builder = new StringBuilder();
		for (Character item : mCharacters) {
			builder.append(item.charValue());
		}
		return builder.toString();
	}

	/**
	 * Changes the current code with the provided characters from the string. No complete event will be fired.
	 *
	 * @param code string where to extract the code
	 */
	public void setCode(String code) {
		if (code.length() > mUnderlineAmount) {
			throw new IllegalArgumentException("Code length is bigger that codes count");
		}

		mCharacters.clear();
		for (char item : code.toCharArray()) {
			mCharacters.add(item);
		}
	}

	/**
	 * Changes the state of the view to show an error message, changing the color or all inputs to error color
	 *
	 * @param errorMessage the message to show
	 */
	public void setError(@StringRes int errorMessage) {
		mErrorMessage = getContext().getString(errorMessage);
		setError(mErrorMessage);
	}

	/**
	 * Changes the state of the view to show an error message, changing the color or all inputs to error color
	 *
	 * @param errorMessage the message to show
	 */
	public void setError(String errorMessage) {
		if (mErrorMessage == null) {
			if (mAnimateOnComplete) {
				mErrorColorAnimator.start();
				mErrorTextAnimator.start();
			} else {
				mUnderlinePaint.setColor(mErrorColor);
			}
			restoreState();
		}
		mUnderlinePaint.setStrokeWidth(mUnderlineErrorStrokeWidth);
		mErrorMessage = errorMessage;
		invalidate();
	}

	/**
	 * Removes the error message and clears the state of the view
	 */
	public void clearError() {
		if (mErrorMessage != null) {
			if (mAnimateOnComplete) {
				mErrorColorAnimator.reverse();
				mErrorTextAnimator.reverse();
			} else {
				mUnderlinePaint.setColor(mUnderlineColor);
			}
		}
		mUnderlinePaint.setStrokeWidth(mUnderlineStrokeWidth);
		mErrorMessage = null;
		invalidate();
	}

	/**
	 * Listener to update the mReduction of the underline bars
	 */
	private class ReductionAnimatorListener implements ValueAnimator.AnimatorUpdateListener {

		public void onAnimationUpdate(ValueAnimator valueanimator) {
			mReduction = (Float) valueanimator.getAnimatedValue();
		}
	}

	/**
	 * Listener to update the mCharacters
	 */
	private class HideCharactersAnimatorListener implements ValueAnimator.AnimatorUpdateListener {

		public void onAnimationUpdate(ValueAnimator valueanimator) {
			mCharactersBaseline = (Float) valueanimator.getAnimatedValue();
		}
	}

	/**
	 * Listener to update color to error color
	 */
	private class ErrorColorAnimatorListener implements AnimatorUpdateListener {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			int color = (Integer) animation.getAnimatedValue();
			mUnderlinePaint.setColor(color);
		}
	}

	/**
	 * Listener to update error text alpha
	 */
	private class ErrorTextAnimatorListener implements AnimatorUpdateListener {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			mErrorTextPaint.setAlpha((Integer) animation.getAnimatedValue());
		}
	}
}
