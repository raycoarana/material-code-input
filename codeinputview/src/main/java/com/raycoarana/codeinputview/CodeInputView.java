package com.raycoarana.codeinputview;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

public class CodeInputView extends View {

    private static final int DEFAULT_CODES = 6;

    public static final int INPUT_TYPE_TEXT = 1;
    public static final int INPUT_TYPE_NUMERIC = 2;

    private static final int ANIMATION_DURATION = 500;
    private static final int DISPATCH_COMPLETE_EVENT_DELAY = 200;

    private static final String TAG = "CodeInputView";

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
    private int mLengthOfCode;
    private int mUnderlineColor;
    private int mUnderlineSelectedColor;
    private int mTextColor;
    private List<OnCodeCompleteListener> mInputCompletedListeners = new ArrayList<>();
    private boolean mIsEditable = true;
    private int mInputType = INPUT_TYPE_NUMERIC;
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
    private int mOnCompleteEventDelay;
    private boolean mPasswordMode;
    private char mPasswordCharacter = '\u2022';

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
        initViewOptions();
        initDefaultAttributes();
        initCustomAttributes(attributeset);
        initDataStructures();
        initPaint();
        initAnimator();
    }

    private void initDefaultAttributes() {
        mUnderlineColor = getColor(R.color.underline_default_color);
        mUnderlineWidth = getContext().getResources().getDimension(R.dimen.underline_width);
        mUnderlineStrokeWidth = getContext().getResources().getDimension(R.dimen.underline_stroke_width);
        mUnderlineSelectedColor = getColor(R.color.underline_selected_color);
        mUnderlineSelectedStrokeWidth = getContext().getResources().getDimension(R.dimen.underline_selected_stroke_width);
        mUnderlineErrorStrokeWidth = getContext().getResources().getDimension(R.dimen.underline_error_stroke_width);
        mUnderlineReduction = getContext().getResources().getDimension(R.dimen.section_reduction);
        mLengthOfCode = DEFAULT_CODES;
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

        TypedArray attributes = getContext().obtainStyledAttributes(attributeset, R.styleable.CodeInputView);

        mUnderlineColor = attributes.getColor(R.styleable.CodeInputView_underline_color, mUnderlineColor);
        mUnderlineWidth = attributes.getDimension(R.styleable.CodeInputView_underline_width, mUnderlineWidth);
        mUnderlineStrokeWidth = attributes.getDimension(R.styleable.CodeInputView_underline_stroke_width, mUnderlineStrokeWidth);
        mUnderlineSelectedColor = attributes.getColor(R.styleable.CodeInputView_underline_selected_color, mUnderlineSelectedColor);
        mUnderlineSelectedStrokeWidth = attributes.getDimension(R.styleable.CodeInputView_underline_selected_stroke_width, mUnderlineSelectedStrokeWidth);
        mUnderlineErrorStrokeWidth = attributes.getDimension(R.styleable.CodeInputView_underline_error_stroke_width, mUnderlineErrorStrokeWidth);
        mUnderlineReduction = attributes.getDimension(R.styleable.CodeInputView_underline_section_reduction, mUnderlineReduction);
        mLengthOfCode = attributes.getInt(R.styleable.CodeInputView_length_of_code, mLengthOfCode);
        mInputType = attributes.getInt(R.styleable.CodeInputView_input_type, mInputType);
        mTextColor = attributes.getInt(R.styleable.CodeInputView_code_text_color, mTextColor);
        mTextSize = attributes.getDimension(R.styleable.CodeInputView_code_text_size, mTextSize);
        mTextMarginBottom = attributes.getDimension(R.styleable.CodeInputView_code_text_margin_bottom, mTextMarginBottom);
        mErrorColor = attributes.getInt(R.styleable.CodeInputView_error_color, mErrorColor);
        mErrorTextSize = attributes.getDimension(R.styleable.CodeInputView_error_text_size, mErrorTextSize);
        mErrorTextMarginTop = attributes.getDimension(R.styleable.CodeInputView_error_text_margin_top, mErrorTextMarginTop);
        mAnimateOnComplete = attributes.getBoolean(R.styleable.CodeInputView_animate_on_complete, true);
        mOnCompleteEventDelay = attributes.getInteger(R.styleable.CodeInputView_on_complete_delay, DISPATCH_COMPLETE_EVENT_DELAY);
        attributes.recycle();
    }

    private void initDataStructures() {
        mUnderlines = new Underline[mLengthOfCode];
        mCharacters = new FixedStack<>();
        mCharacters.setMaxSize(mLengthOfCode);
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
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        if (gainFocus) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    showKeyboard();
                }
            }, 100);
        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mUnderLineY = (int) (mTextMarginBottom + mTextPaint.getFontSpacing());
        initUnderline();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            float desiredWidth = mLengthOfCode * mUnderlineWidth;
            width = Math.min((int) desiredWidth, width);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            float desiredHeight = mErrorTextPaint.getFontSpacing() + mErrorTextPaint.getFontMetrics().bottom +
                    mErrorTextMarginTop + mTextPaint.getFontSpacing() + mTextMarginBottom;
            height = Math.min((int) desiredHeight, height);
        }
        setMeasuredDimension(width, height);
    }

    private void initUnderline() {
        mXOffset = (int) Math.abs(getWidth() - (mLengthOfCode * mUnderlineWidth)) / 2;
        for (int i = 0; i < mLengthOfCode; i++) {
            mUnderlines[i] = createPath(i, mUnderlineWidth);
        }
    }

    private Underline createPath(int position, float sectionWidth) {
        float fromX = mXOffset + sectionWidth * (float) position;
        return new Underline(fromX, mUnderLineY, fromX + sectionWidth, mUnderLineY);
    }

    private void showKeyboard() {
        InputMethodManager inputmethodmanager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputmethodmanager != null) {
            inputmethodmanager.showSoftInput(this, InputMethodManager.RESULT_UNCHANGED_SHOWN);
            inputmethodmanager.viewClicked(this);
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        switch (mInputType) {
            case INPUT_TYPE_NUMERIC:
                outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
                break;
            case INPUT_TYPE_TEXT:
                outAttrs.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
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
        if (mCharacters.size() == mLengthOfCode &&
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
        char typedChar = (char) keyevent.getUnicodeChar();
        boolean isValid;
        if (mInputType == INPUT_TYPE_NUMERIC) {
            isValid = Character.isDigit(typedChar);
        } else {
            isValid = Character.isLetterOrDigit(typedChar);
        }

        if (mIsEditable && isValid && mCharacters.size() < mLengthOfCode) {
            mCharacters.push(typedChar);
            invalidate();
            if (mCharacters.size() == mLengthOfCode) {
                dispatchComplete();
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
        }, mOnCompleteEventDelay);
    }

    private void notifyCompleted() {
        String code = getCode();
        for (OnCodeCompleteListener listener : mInputCompletedListeners) {
            listener.onCompleted(code);
        }
    }

    /**
     * Enables that the user can edit the code or not
     *
     * @param value true will let the user change the code in the input, false will ignore any action from the user
     */
    @SuppressWarnings("SameParameterValue")
    public void setEditable(boolean value) {
        mIsEditable = value;
        invalidate();
    }

    /**
     * Adds a listener that will be fired once the user complete all the code characters
     *
     * @param listener listener to add
     */
    public void addOnCompleteListener(OnCodeCompleteListener listener) {
        mInputCompletedListeners.add(listener);
    }

    /**
     * Delay time after the user completes the code before OnCodeCompleteListener is fired
     *
     * @param delay delay in millis
     */
    @SuppressWarnings("SameParameterValue")
    public void setOnCompleteEventDelay(int delay) {
        mOnCompleteEventDelay = delay;
    }

    /**
     * Gets the delay time after the user completes the code before OnCodeCompleteListener is fired
     *
     * @return delay in millis
     */
    @SuppressWarnings("unused")
    public int getOnCompleteEventDelay() {
        return mOnCompleteEventDelay;
    }

    /**
     * When a touch is detected the view need to focus and animate if is necessary
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionevent) {
        if (motionevent.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
        }
        return super.onTouchEvent(motionevent);
    }

    @Override
    public boolean performClick() {
        requestFocus();
        showKeyboard();
        return super.performClick();
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
                char charToDraw = mPasswordMode ? mPasswordCharacter : mCharacters.get(i);
                drawCharacter(fromX, toX, charToDraw, canvas);
                canvas.restore();
            }
            drawSection(i, fromX, fromY, toX, toY, canvas);
        }
        drawErrorMessage(canvas);
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
        canvas.drawText(character.toString(), centerX, (mUnderLineY - mTextMarginBottom) + mCharactersBaseline, mTextPaint);
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
    @SuppressWarnings("SameParameterValue")
    public void setCode(String code) {
        if (code.length() > mLengthOfCode) {
            Log.e(TAG, "Code length is bigger that codes count");
            return;
        }

        mCharacters.clear();
        for (char item : code.toCharArray()) {
            mCharacters.add(item);
        }
        invalidate();
    }

    /**
     * Changes the state of the view to show an error message, changing the color or all inputs to error color
     *
     * @param errorMessage the message to show
     */
    @SuppressWarnings("unused")
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
        if ((mErrorMessage == null || mErrorMessage.isEmpty()) && errorMessage != null) {
            if (mAnimateOnComplete) {
                mErrorColorAnimator.start();
                mErrorTextAnimator.start();
            } else {
                mUnderlinePaint.setColor(mErrorColor);
            }
            restoreState();
            mUnderlinePaint.setStrokeWidth(mUnderlineErrorStrokeWidth);
        } else if (mErrorMessage != null && (errorMessage == null || errorMessage.isEmpty())) {
            if (mAnimateOnComplete) {
                mErrorColorAnimator.reverse();
                mErrorTextAnimator.reverse();
            } else {
                mUnderlinePaint.setColor(mUnderlineColor);
            }
            mUnderlinePaint.setStrokeWidth(mUnderlineStrokeWidth);
        }
        mErrorMessage = errorMessage;
        invalidate();
    }


    /**
     * Return the current error message, if any
     *
     * @return return the current message
     */
    @Nullable
    @SuppressWarnings("unused")
    public String getError() {
        return mErrorMessage;
    }

    /**
     * Removes the error message and clears the state of the view
     */
    public void clearError() {
        setError(null);
    }

    /**
     * Get the current input type, could be one of: INPUT_TYPE_TEXT, INPUT_TYPE_NUMERIC
     *
     * @return INPUT_TYPE_TEXT or INPUT_TYPE_NUMERIC
     */
    @SuppressWarnings("unused")
    public int getInputType() {
        return mInputType;
    }

    /**
     * Set the current input type, could be one of: INPUT_TYPE_TEXT, INPUT_TYPE_NUMERIC
     *
     * @param inputType INPUT_TYPE_TEXT or INPUT_TYPE_NUMERIC
     */
    public void setInputType(int inputType) {
        mInputType = inputType;

        invalidate();
    }

    /**
     * Get the actual password mode
     *
     * @return true when it is hiding the code or false when it is displaying it.
     */
    @SuppressWarnings("unused")
    public boolean isInPasswordMode() {
        return mPasswordMode;
    }

    /**
     * Enables or disables the password mode. In password mode the code is not shown in the view
     * instead of that a big dot is shown.
     * PasswordMode is disabled by default.
     *
     * @param enabled true to hide the code or false the display it.
     */
    @SuppressWarnings("SameParameterValue")
    public void setPasswordMode(boolean enabled) {
        mPasswordMode = enabled;

        invalidate();
    }

    /**
     * Get the current password mode character that is used to draw the code when the view
     * is in password mode
     *
     * @return the current character in password mode
     */
    @SuppressWarnings("unused")
    public char getPasswordCharacter() {
        return mPasswordCharacter;
    }

    /**
     * Set the character to use when the view is in password mode
     *
     * @param passwordCharacter the new character for password mode
     */
    @SuppressWarnings("unused")
    public void setPasswordCharacter(char passwordCharacter) {
        this.mPasswordCharacter = passwordCharacter;
    }

    /**
     * Get the current length of the code
     *
     * @return number of total characters of the code
     */
    @SuppressWarnings("unused")
    public int getLengthOfCode() {
        return mLengthOfCode;
    }

    /**
     * Set the length of the code required. Current code will get reset.
     *
     * @param value new length of code
     */
    @SuppressWarnings("SameParameterValue")
    public void setLengthOfCode(int value) {
        mLengthOfCode = value;
        initDataStructures();
        initUnderline();
        invalidate();
    }

    /**
     * Listener to update the mReduction of the underline bars
     */
    private class ReductionAnimatorListener implements ValueAnimator.AnimatorUpdateListener {

        public void onAnimationUpdate(ValueAnimator valueanimator) {
            mReduction = (Float) valueanimator.getAnimatedValue();
            invalidate();
        }
    }

    /**
     * Listener to update the mCharacters
     */
    private class HideCharactersAnimatorListener implements ValueAnimator.AnimatorUpdateListener {

        public void onAnimationUpdate(ValueAnimator valueanimator) {
            mCharactersBaseline = (Float) valueanimator.getAnimatedValue();
            invalidate();
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
            invalidate();
        }
    }

    /**
     * Listener to update error text alpha
     */
    private class ErrorTextAnimatorListener implements AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mErrorTextPaint.setAlpha((Integer) animation.getAnimatedValue());
            invalidate();
        }
    }
}
