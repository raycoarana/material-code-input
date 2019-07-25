package com.raycoarana.codeinputview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.xmlpull.v1.XmlPullParserException;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.XmlRes;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputType;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.raycoarana.codeinputview.model.Underline;

public class CodeInputView extends View {

    private static final int DEFAULT_CODES = 6;

    public static final int INPUT_TYPE_TEXT = 1;
    public static final int INPUT_TYPE_NUMERIC = 2;

    private static final int ANIMATION_DURATION = 500;
    private static final int DISPATCH_COMPLETE_EVENT_DELAY = 200;
    private static final int DEFAULT_TIME_CHARACTER_IS_SHOWN_WHILE_TYPING = 200;

    private static final String TAG = "CodeInputView";

    private Underline[] mUnderlines;
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
    private float mErrorTextMarginLeft;
    private float mErrorTextMarginRight;
    private int mLengthOfCode;
    private int mUnderlineColor;
    private int mUnderlineSelectedColor;
    private int mTextColor;
    private List<OnCodeCompleteListener> mInputCompletedListeners = new ArrayList<>();
    private List<OnDigitInputListener> mDigitInputListeners = new ArrayList<>();
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
    private int mErrorTextColor;
    private float mErrorTextSize;
    private float mErrorTextMarginTop;
    private Paint mErrorTextPaint;
    private int mXOffset;
    private boolean mAnimateOnComplete;
    private int mOnCompleteEventDelay;
    private boolean mInPasswordMode;
    private boolean mShowPasswordWhileTyping;
    private char mPasswordCharacter = '\u2022';
    private boolean mShowKeyboard = true;
    private long mTimeCharacterIsShownWhileTypingInNano;
    private int mTimeCharacterIsShownWhileTypingInMillis = DEFAULT_TIME_CHARACTER_IS_SHOWN_WHILE_TYPING;
    private long mLastTimeTypedInNano;
    private int mGravity;
    private int mErrorTextGravity;
    private StaticLayout mErrorTextLayout;
    private InputContentType mInputContentType = new InputContentType();
    private SpannableStringBuilder mSpannableSupportBuilder = new SpannableStringBuilder();

    static class InputContentType {
        int imeOptions = EditorInfo.IME_NULL;
        String privateImeOptions;
        CharSequence imeActionLabel;
        int imeActionId;
        Bundle extras;
        OnEditorActionListener onEditorActionListener;
    }

    /**
     * Interface definition for a callback to be invoked when an action is
     * performed on the editor.
     */
    public interface OnEditorActionListener {
        /**
         * Called when an action is being performed.
         *
         * @param v        The view that was clicked.
         * @param actionId Identifier of the action.  This will be either the
         *                 identifier you supplied, or {@link EditorInfo#IME_NULL
         *                 EditorInfo.IME_NULL} if being called due to the enter key
         *                 being pressed.
         * @param event    If triggered by an enter key, this is the event;
         *                 otherwise, this is null.
         * @return Return true if you have consumed the action, else false.
         */
        boolean onEditorAction(CodeInputView v, int actionId, KeyEvent event);
    }

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
        mErrorTextMarginLeft = 0;
        mErrorTextMarginRight = 0;
        mErrorColor = getColor(R.color.error_color);
        mErrorTextColor = getColor(R.color.error_color);
        mErrorTextSize = getContext().getResources().getDimension(R.dimen.error_text_size);
        mErrorTextMarginTop = getContext().getResources().getDimension(R.dimen.error_text_margin_top);
        mReduction = mUnderlineReduction;
        mGravity = Gravity.CENTER;
        mErrorTextGravity = Gravity.CENTER;
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
        mErrorTextColor = attributes.getInt(R.styleable.CodeInputView_error_text_color, mErrorTextColor);
        mErrorTextSize = attributes.getDimension(R.styleable.CodeInputView_error_text_size, mErrorTextSize);
        mErrorTextMarginTop = attributes.getDimension(R.styleable.CodeInputView_error_text_margin_top, mErrorTextMarginTop);
        mErrorTextMarginLeft = attributes.getDimension(R.styleable.CodeInputView_error_text_margin_left, mErrorTextMarginLeft);
        mErrorTextMarginRight = attributes.getDimension(R.styleable.CodeInputView_error_text_margin_right, mErrorTextMarginRight);
        mAnimateOnComplete = attributes.getBoolean(R.styleable.CodeInputView_animate_on_complete, true);
        mOnCompleteEventDelay = attributes.getInteger(R.styleable.CodeInputView_on_complete_delay, DISPATCH_COMPLETE_EVENT_DELAY);
        mShowKeyboard = attributes.getBoolean(R.styleable.CodeInputView_show_keyboard, mShowKeyboard);
        mInPasswordMode = attributes.getBoolean(R.styleable.CodeInputView_password_mode, mInPasswordMode);
        mShowPasswordWhileTyping = attributes.getBoolean(R.styleable.CodeInputView_show_password_while_typing, mShowPasswordWhileTyping);
        mTimeCharacterIsShownWhileTypingInMillis = attributes.getInt(R.styleable.CodeInputView_time_character_is_shown_while_typing, mTimeCharacterIsShownWhileTypingInMillis);
        mTimeCharacterIsShownWhileTypingInNano = TimeUnit.MILLISECONDS.toNanos(mTimeCharacterIsShownWhileTypingInMillis);
        mGravity = attributes.getInteger(R.styleable.CodeInputView_gravity, mGravity);
        mErrorTextGravity = attributes.getInteger(R.styleable.CodeInputView_error_text_gravity, mErrorTextGravity);
        mInputContentType.imeOptions = attributes.getInteger(R.styleable.CodeInputView_imeOptions, mInputContentType.imeOptions);
        mInputContentType.imeActionId = attributes.getInteger(R.styleable.CodeInputView_imeActionId, mInputContentType.imeActionId);
        mInputContentType.imeActionLabel = attributes.getString(R.styleable.CodeInputView_imeActionLabel);
        mInputContentType.privateImeOptions = attributes.getString(R.styleable.CodeInputView_privateImeOptions);
        int inputExtrasResId = attributes.getInteger(R.styleable.CodeInputView_editorExtras, 0);
        if (inputExtrasResId != 0) {
            try {
                setInputExtras(inputExtrasResId);
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Failure reading input extras", e);
            } catch (IOException e) {
                Log.w(TAG, "Failure reading input extras", e);
            }
        }

        String passwordChar = attributes.getString(R.styleable.CodeInputView_password_character);
        if (passwordChar != null && passwordChar.length() == 1) {
            mPasswordCharacter = passwordChar.charAt(0);
        }
        attributes.recycle();
    }

    private void initDataStructures() {
        mUnderlines = new Underline[mLengthOfCode];
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
        mErrorTextPaint.setColor(mErrorTextColor);
        mErrorTextPaint.setAntiAlias(true);
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
                    if (mIsEditable) {
                        showKeyboard();
                    } else {
                        hideKeyboard();
                    }
                }
            }, 100);
        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Rect container = new Rect(0, 0, w, h);
        Rect destinationRect = new Rect();

        mErrorTextLayout = buildErrorTextLayout(w);
        Gravity.apply(mGravity, getDesiredWidth(), getDesiredHeight(mErrorTextLayout), container, destinationRect);

        mXOffset = destinationRect.left;
        mUnderLineY = (int) (destinationRect.top + mTextMarginBottom + mTextPaint.getFontSpacing());
        initUnderline();
    }

    private StaticLayout buildErrorTextLayout(int width) {
        int textWidth = (int) (width - mErrorTextMarginLeft - mErrorTextMarginRight);
        StaticLayout staticLayout;
        String errorMessage = mErrorMessage != null ? mErrorMessage : " ";
        TextPaint textPaint = new TextPaint(mErrorTextPaint);
        Alignment alignment = getAlignment();
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            staticLayout = StaticLayout.Builder.obtain(errorMessage, 0, errorMessage.length(), textPaint, textWidth)
                    .setAlignment(alignment)
                    .build();
        } else {
            staticLayout = new StaticLayout(
                    mErrorMessage,
                    textPaint,
                    textWidth,
                    alignment,
                    1.0f,
                    0,
                    true
            );
        }
        return staticLayout;
    }

    private Alignment getAlignment() {
        switch (mErrorTextGravity) {
            case Gravity.CENTER:
                return Alignment.ALIGN_CENTER;
            case Gravity.RIGHT:
                return Alignment.ALIGN_OPPOSITE;
            default:
                return Alignment.ALIGN_NORMAL;
        }
    }

    private void initUnderline() {
        for (int i = 0; i < mLengthOfCode; i++) {
            mUnderlines[i] = createPath(i, mUnderlineWidth);
        }
    }

    private Underline createPath(int position, float sectionWidth) {
        float fromX = mXOffset + sectionWidth * (float) position;
        return new Underline(fromX, mUnderLineY, fromX + sectionWidth, mUnderLineY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            width = Math.min(getDesiredWidth(), width);
        }

        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            int desiredHeight = getDesiredHeight(buildErrorTextLayout(width));
            height = Math.min(desiredHeight, height);
        }
        setMeasuredDimension(width, height);
    }

    private int getDesiredWidth() {
        return (int) (mLengthOfCode * mUnderlineWidth);
    }

    private int getDesiredHeight(StaticLayout errorTextLayout) {
        return (int) (errorTextLayout.getHeight() + mErrorTextMarginTop + mTextPaint.getFontSpacing() + mTextMarginBottom);
    }

    private void showKeyboard() {
        if (!mShowKeyboard) {
            return;
        }

        InputMethodManager inputmethodmanager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputmethodmanager != null) {
            inputmethodmanager.showSoftInput(this, InputMethodManager.RESULT_UNCHANGED_SHOWN);
            inputmethodmanager.viewClicked(this);
        }
    }

    private void hideKeyboard() {
        if (!mShowKeyboard) {
            return;
        }

        InputMethodManager inputmethodmanager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputmethodmanager != null) {
            inputmethodmanager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        switch (mInputType) {
            case INPUT_TYPE_NUMERIC:
                outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
                break;
            case INPUT_TYPE_TEXT:
                outAttrs.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                break;
        }

        outAttrs.imeOptions = mInputContentType.imeOptions;
        outAttrs.privateImeOptions = mInputContentType.privateImeOptions;
        outAttrs.actionLabel = mInputContentType.imeActionLabel;
        outAttrs.actionId = mInputContentType.imeActionId;
        outAttrs.extras = mInputContentType.extras;

        return new BaseInputConnection(this, false) {

            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                return deleteCharacter();
            }

            @Override
            public boolean performEditorAction(int actionCode) {
                if (CodeInputView.this.onEditorAction(actionCode)) {
                    super.performEditorAction(actionCode);
                }
                return true;
            }
        };
    }

    /**
     * Called when an attached input method calls
     * {@link InputConnection#performEditorAction(int)
     * InputConnection.performEditorAction()}
     * for this mCode view.  The default implementation will call your action
     * listener supplied to {@link #setOnEditorActionListener}, or perform
     * a standard operation for {@link EditorInfo#IME_ACTION_NEXT
     * EditorInfo.IME_ACTION_NEXT}, {@link EditorInfo#IME_ACTION_PREVIOUS
     * EditorInfo.IME_ACTION_PREVIOUS}, or {@link EditorInfo#IME_ACTION_DONE
     * EditorInfo.IME_ACTION_DONE}.
     *
     * <p>For backwards compatibility, if no IME options have been set and the
     * mCode view would not normally advance focus on enter, then
     * the NEXT and DONE actions received here will be turned into an enter
     * key down/up pair to go through the normal key handling.
     *
     * @param actionCode The code of the action being performed.
     * @see #setOnEditorActionListener
     */
    @SuppressLint("WrongConstant")
    private boolean onEditorAction(int actionCode) {
        if (mInputContentType.onEditorActionListener != null) {
            int actionId = mInputContentType.imeActionId;
            if (actionId == 0) {
                actionId = actionCode;
            }
            if (mInputContentType.onEditorActionListener.onEditorAction(this, actionId, null)) {
                return false;
            }
        }

        if (actionCode == EditorInfo.IME_ACTION_NEXT) {
            View v = focusSearch(FOCUS_FORWARD);
            if (v != null) {
                if (!v.requestFocus(FOCUS_FORWARD)) {
                    throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                }
            }
        } else if (actionCode == EditorInfo.IME_ACTION_PREVIOUS) {
            View v = focusSearch(FOCUS_BACKWARD);
            if (v != null) {
                if (!v.requestFocus(FOCUS_BACKWARD)) {
                    throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                }
            }
        } else if (actionCode == EditorInfo.IME_ACTION_DONE) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && inputMethodManager.isActive(this)) {
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
        return true;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.mCode = getCode();
        ss.mError = getError();
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        setError(savedState.mError);
        setCode(savedState.mCode);
    }

    /**
     * User interface state that is stored by CodeInputView for implementing
     * {@link View#onSaveInstanceState}.
     */
    public static class SavedState extends BaseSavedState {
        String mCode;
        String mError;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(mCode);

            if (mError == null) {
                out.writeInt(0);
            } else {
                out.writeInt(1);
                out.writeString(mError);
            }
        }

        @Override
        public String toString() {
            String str = "CodeInputView.SavedState{" + Integer.toHexString(System.identityHashCode(this));
            if (mCode != null) {
                str += " mCode=" + mCode;
            }
            if (mError != null) {
                str += " mError=" + mError;
            }
            return str + "}";
        }

        public static final Parcelable.Creator<CodeInputView.SavedState> CREATOR =
                new Parcelable.Creator<CodeInputView.SavedState>() {
                    public CodeInputView.SavedState createFromParcel(Parcel in) {
                        return new CodeInputView.SavedState(in);
                    }

                    public CodeInputView.SavedState[] newArray(int size) {
                        return new CodeInputView.SavedState[size];
                    }
                };

        private SavedState(Parcel in) {
            super(in);
            mCode = in.readString();

            if (in.readInt() != 0) {
                mError = in.readString();
            }
        }
    }

    /**
     * Change the editor type integer associated with the mCode view, which
     * is reported to an Input Method Editor (IME) with {@link EditorInfo#imeOptions}
     * when it has focus.
     *
     * Ref android.R.styleable#TextView_imeOptions
     * @see #getImeOptions
     * @see android.view.inputmethod.EditorInfo
     */
    public void setImeOptions(int imeOptions) {
        mInputContentType.imeOptions = imeOptions;
    }

    /**
     * Get the type of the Input Method Editor (IME).
     *
     * @return the type of the IME
     * @see #setImeOptions(int)
     * @see android.view.inputmethod.EditorInfo
     */
    public int getImeOptions() {
        return mInputContentType.imeOptions;
    }

    /**
     * Change the custom IME action associated with the mCode view, which
     * will be reported to an IME with {@link EditorInfo#actionLabel}
     * and {@link EditorInfo#actionId} when it has focus.
     *
     * Ref android.R.styleable#TextView_imeActionLabel
     * Ref android.R.styleable#TextView_imeActionId
     * @see #getImeActionLabel
     * @see #getImeActionId
     * @see android.view.inputmethod.EditorInfo
     */
    public void setImeActionLabel(CharSequence label, int actionId) {
        mInputContentType.imeActionLabel = label;
        mInputContentType.imeActionId = actionId;
    }

    /**
     * Get the IME action label previous set with {@link #setImeActionLabel}.
     *
     * @see #setImeActionLabel
     * @see android.view.inputmethod.EditorInfo
     */
    public CharSequence getImeActionLabel() {
        return mInputContentType.imeActionLabel;
    }

    /**
     * Get the IME action ID previous set with {@link #setImeActionLabel}.
     *
     * @see #setImeActionLabel
     * @see android.view.inputmethod.EditorInfo
     */
    public int getImeActionId() {
        return mInputContentType.imeActionId;
    }

    /**
     * Set a special listener to be called when an action is performed
     * on the mCode view.  This will be called when the enter key is pressed,
     * or when an action supplied to the IME is selected by the user.  Setting
     * this means that the normal hard key event will not insert a newline
     * into the mCode view, even if it is multi-line; holding down the ALT
     * modifier will, however, allow the user to insert a newline character.
     */
    public void setOnEditorActionListener(OnEditorActionListener l) {
        mInputContentType.onEditorActionListener = l;
    }

    /**
     * Set the private content type of the mCode, which is the
     * {@link EditorInfo#privateImeOptions EditorInfo.privateImeOptions}
     * field that will be filled in when creating an input connection.
     *
     * Ref android.R.styleable#TextView_privateImeOptions
     * @see #getPrivateImeOptions()
     * @see EditorInfo#privateImeOptions
     */
    public void setPrivateImeOptions(String type) {
        mInputContentType.privateImeOptions = type;
    }

    /**
     * Get the private type of the content.
     *
     * @see #setPrivateImeOptions(String)
     * @see EditorInfo#privateImeOptions
     */
    public String getPrivateImeOptions() {
        return mInputContentType.privateImeOptions;
    }

    /**
     * Set the extra input data of the mCode, which is the
     * {@link EditorInfo#extras TextBoxAttribute.extras}
     * Bundle that will be filled in when creating an input connection.  The
     * given integer is the resource identifier of an XML resource holding an
     * {android.R.styleable#InputExtras &lt;input-extras&gt;} XML tree.
     *
     * Ref android.R.styleable#TextView_editorExtras
     * @see #getInputExtras(boolean)
     * @see EditorInfo#extras
     */
    public void setInputExtras(@XmlRes int xmlResId) throws XmlPullParserException, IOException {
        XmlResourceParser parser = getResources().getXml(xmlResId);
        mInputContentType.extras = new Bundle();
        getResources().parseBundleExtras(parser, mInputContentType.extras);
    }

    /**
     * Retrieve the input extras currently associated with the mCode view, which
     * can be viewed as well as modified.
     *
     * @param create If true, the extras will be created if they don't already
     *               exist.  Otherwise, null will be returned if none have been created.
     * Ref android.R.styleable#TextView_editorExtras
     * @see #setInputExtras(int)
     * @see EditorInfo#extras
     */
    public Bundle getInputExtras(boolean create) {
        if (mInputContentType.extras == null && create) {
            mInputContentType.extras = new Bundle();
        }
        return mInputContentType.extras;
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
        boolean canDelete = prepareForDelete(mSpannableSupportBuilder.length());
        if (canDelete) {
            int length = mSpannableSupportBuilder.length();
            mSpannableSupportBuilder.delete(length - 1, length);
        }
        return canDelete;
    }

    private boolean prepareForDelete(int currentLength) {
        mLastTimeTypedInNano = 0;
        boolean canDelete = currentLength > 0 && mIsEditable;
        if (canDelete) {
            restoreState();
            notifyDeleteDigit();
            clearError();
        }
        return canDelete;
    }

    private void restoreState() {
        if (mSpannableSupportBuilder.length() == mLengthOfCode &&
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

        if (mIsEditable && isValid && mSpannableSupportBuilder.length() < mLengthOfCode) {
            mSpannableSupportBuilder.append(typedChar);
            mLastTimeTypedInNano = System.nanoTime();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            }, mTimeCharacterIsShownWhileTypingInMillis);
            invalidate();
            notifyInputDigit(typedChar);
            if (mSpannableSupportBuilder.length() == mLengthOfCode) {
                dispatchComplete();
            }
            return true;
        } else {
            return false;
        }
    }

    private void dispatchComplete() {
        mIsEditable = false;
        hideKeyboard();
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

    private void notifyInputDigit(char newDigit) {
        for (OnDigitInputListener listener : mDigitInputListeners) {
            listener.onInput(newDigit);
        }
    }

    private void notifyDeleteDigit() {
        for (OnDigitInputListener listener : mDigitInputListeners) {
            listener.onDelete();
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
        if (mIsEditable && hasFocus()) {
            showKeyboard();
        }
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
     * Removes a listener that will be fired once the user complete all the code characters
     *
     * @param listener listener to remove
     */
    public void removeOnCompleteListener(OnCodeCompleteListener listener) {
        mInputCompletedListeners.remove(listener);
    }

    /**
     * Adds a listener that will be fired every time a digit is added or removed
     *
     * @param listener listener to add
     */
    public void addOnDigitInputListener(OnDigitInputListener listener) {
        mDigitInputListeners.add(listener);
    }


    /**
     * Removes a listener that will be fired every time a digit is added or removed
     *
     * @param listener listener to remove
     */
    public void removeOnDigitInputListener(OnDigitInputListener listener) {
        mDigitInputListeners.remove(listener);
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
        if (mIsEditable) {
            showKeyboard();
        } else {
            hideKeyboard();
        }
        return super.performClick();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mUnderlines.length; i++) {
            Underline sectionPath = mUnderlines[i];
            float fromX = sectionPath.getFromX() + mReduction;
            float fromY = sectionPath.getFromY();
            float toX = sectionPath.getToX() - mReduction;
            float toY = sectionPath.getToY();
            int charactersCount = mSpannableSupportBuilder.length();
            if (charactersCount > i) {
                canvas.save();
                canvas.clipRect(0, 0, toX, toY);
                boolean canBeShown = charactersCount - 1 == i && mShowPasswordWhileTyping && System.nanoTime() - mLastTimeTypedInNano < mTimeCharacterIsShownWhileTypingInNano;
                char charToDraw = (mInPasswordMode && !canBeShown) ? mPasswordCharacter : mSpannableSupportBuilder.charAt(i);
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

        canvas.save();
        canvas.translate(mErrorTextMarginLeft, mUnderLineY + mErrorTextMarginTop);
        mErrorTextLayout.draw(canvas);
        canvas.restore();
    }

    private void drawSection(int position, float fromX, float fromY, float toX, float toY, Canvas canvas) {
        Paint paint = mUnderlinePaint;
        if (position == mSpannableSupportBuilder.length() && hasFocus()) {
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
        return mSpannableSupportBuilder.toString();
    }

    /**
     * Changes the current code with the provided characters from the string. No complete event will be fired.
     *
     * @param code string where to extract the code
     */
    @SuppressWarnings("SameParameterValue")
    public void setCode(@Nullable String code) {
        if (code != null && code.length() > mLengthOfCode) {
            Log.e(TAG, "Code length is bigger that codes count");
            return;
        }

        mSpannableSupportBuilder.clear();
        mSpannableSupportBuilder.append(code);
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
        if (errorMessage != null && errorMessage.isEmpty()) {
            errorMessage = null;
        }

        if ((mErrorMessage == null || mErrorMessage.isEmpty()) && errorMessage != null) {
            if (mAnimateOnComplete) {
                mErrorColorAnimator.start();
                mErrorTextAnimator.start();
            } else {
                mUnderlinePaint.setColor(mErrorColor);
            }
            restoreState();
            mUnderlinePaint.setStrokeWidth(mUnderlineErrorStrokeWidth);
        } else if (mErrorMessage != null && errorMessage == null) {
            if (mAnimateOnComplete) {
                mErrorColorAnimator.reverse();
                mErrorTextAnimator.reverse();
            } else {
                mUnderlinePaint.setColor(mUnderlineColor);
            }
            mUnderlinePaint.setStrokeWidth(mUnderlineStrokeWidth);
        }

        mErrorMessage = errorMessage;
        int width = getWidth();
        if (width > 0) {
            mErrorTextLayout = buildErrorTextLayout(width);
        }
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
    public boolean getInPasswordMode() {
        return mInPasswordMode;
    }

    /**
     * Enables or disables the password mode. In password mode the code is not shown in the view
     * instead of that a big dot is shown.
     * PasswordMode is disabled by default.
     *
     * @param enabled true to hide the code or false the display it.
     */
    @SuppressWarnings("SameParameterValue")
    public void setInPasswordMode(boolean enabled) {
        mInPasswordMode = enabled;

        invalidate();
    }

    /**
     * Get if the password is shown while the user is typing for some time or until the next
     * character type
     *
     * @return true when it is showing the password while typing or false if not
     */
    @SuppressWarnings("unused")
    public boolean getShowPasswordWhileTyping() {
        return mShowPasswordWhileTyping;
    }

    /**
     * Enables or disables showing the last typed character while typing for some time or until
     * the next character is typed
     * ShowPasswordWhileTyping is disabled by default.
     *
     * @param enabled true to show password while typing or false to not show never
     */
    @SuppressWarnings("SameParameterValue")
    public void setShowPasswordWhileTyping(boolean enabled) {
        mShowPasswordWhileTyping = enabled;

        invalidate();
    }

    /**
     * Get time in milliseconds that the last character is shown when showPasswordWhileTyping is
     * enabled
     *
     * @return the time in milliseconds
     */
    @SuppressWarnings("unused")
    public int getTimeCharacterIsShownWhileTyping() {
        return mTimeCharacterIsShownWhileTypingInMillis;
    }

    /**
     * Set time in milliseconds that the last character is shown when showPasswordWhileTyping is
     * enabled.
     *
     * @param timeInMillis true to hide the code or false the display it.
     */
    @SuppressWarnings("SameParameterValue")
    public void setTimeCharacterIsShownWhileTyping(int timeInMillis) {
        mTimeCharacterIsShownWhileTypingInMillis = timeInMillis;
        mTimeCharacterIsShownWhileTypingInNano = TimeUnit.MILLISECONDS.toNanos(timeInMillis);

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
     * Return if the OS keyboard should be shown when the view gain focus or the user tap on it
     *
     * @return true if the OS keyboard should be shown or false in case it is not
     */
    @SuppressWarnings("unused")
    public boolean getShowKeyBoard() {
        return mShowKeyboard;
    }

    /**
     * Set if the OS keyboard should be shown when the view gain focus or the user tap on it
     *
     * @param value true to show the keyboard when focus gained or tapped, false to not show it
     */
    @SuppressWarnings("SameParameterValue")
    public void setShowKeyboard(boolean value) {
        mShowKeyboard = value;
        invalidate();
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
            Integer animatedValue = (Integer) animation.getAnimatedValue();
            mErrorTextPaint.setAlpha(animatedValue);
            if (mErrorTextLayout != null) {
                mErrorTextLayout.getPaint().setAlpha(animatedValue);
            }
            invalidate();
        }
    }
}
