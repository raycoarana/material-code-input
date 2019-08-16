package com.raycoarana.codeinputview

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.UiThreadTestRule
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VisualizationTest {

    private lateinit var viewHelper: ViewHelpers
    private lateinit var view: CodeInputView
    private lateinit var context: Context

    @get:Rule
    var uiThreadRule = UiThreadTestRule()

    @Before
    fun prepareContext() {
        context = getInstrumentation().targetContext
    }

    @Test
    fun drawDigitSections() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        Screenshot.snap(view).record()
    }

    @Test
    fun drawErrorText() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.error = "An error occurs after checking your code"
        viewHelper.layout()

        Screenshot.snap(view).record()
    }

    @Test
    fun drawLongErrorText() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.error = "This is a test of long error text that should expand over several lines of errors and the view should layout it correctly"
        viewHelper.layout()

        Screenshot.snap(view).record()
    }

    @Test
    fun drawNumericDigits() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.code = "1234"

        Screenshot.snap(view).record()
    }

    @Test
    fun drawNumericDigitsInPasswordMode() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.inPasswordMode = true
        view.code = "1234"

        Screenshot.snap(view).record()
    }

    @Test
    fun drawAlphaNumericDigits() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.inputType = CodeInputView.INPUT_TYPE_TEXT
        view.code = "abc34"

        Screenshot.snap(view).record()
    }

    @Test
    fun drawAlphaNumericDigitsInPasswordMode() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.inputType = CodeInputView.INPUT_TYPE_TEXT
        view.inPasswordMode = true
        view.code = "acb34"

        Screenshot.snap(view).record()
    }

    @Test
    fun changeNormalStateColors() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()
        givenAllColorsAreChanged()

        view.code = "134"

        Screenshot.snap(view).record()
    }

    @Test
    fun changeNormalStateColorsInPasswordMode() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()
        givenAllColorsAreChanged()

        view.inPasswordMode = true
        view.code = "134"

        Screenshot.snap(view).record()
    }

    @Test
    fun changeErrorStateColors() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()
        givenAllColorsAreChanged()

        view.code = "134"
        view.error = "This is an error"

        Screenshot.snap(view).record()
    }

    @Test
    fun changePasswordCharacter() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.inPasswordMode = true
        view.code = "134"
        view.passwordCharacter = '$'

        Screenshot.snap(view).record()
    }

    @Test
    fun changeCodeLength() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.code = "134"
        view.lengthOfCode = 3

        viewHelper.layout()

        Screenshot.snap(view).record()
    }

    @Test
    fun changeGravityToLeft() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.code = "134"
        view.gravity = Gravity.LEFT

        viewHelper.layout()

        Screenshot.snap(view).record()
    }

    @Test
    fun changeGravityToRight() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.code = "134"
        view.gravity = Gravity.RIGHT

        viewHelper.layout()

        Screenshot.snap(view).record()
    }

    @Test
    fun changeErrorTextGravityToLeft() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.code = "134"
        view.error = "This is an error"
        view.errorTextGravity = Gravity.LEFT

        viewHelper.layout()

        Screenshot.snap(view).record()
    }

    @Test
    fun changeErrorTextGravityToRight() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.code = "134"
        view.error = "This is an error"
        view.errorTextGravity = Gravity.RIGHT

        viewHelper.layout()

        Screenshot.snap(view).record()
    }

    @Test
    fun changeTextSize() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.code = "134"
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f)

        viewHelper.layout()

        Screenshot.snap(view).record()
    }

    @Test
    fun changeErrorTextSize() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.code = "134"
        view.error = "This is an error"
        view.setErrorTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f)

        viewHelper.layout()

        Screenshot.snap(view).record()
    }

    @Test
    fun changeTextMargin() {
        givenViewInitializedWithDefaultAttributes()
        givenViewIsMeasuredWithExactWidth()

        view.code = "1234"
        view.error = "This is an error quite large that needs margins"
        view.setTextMarginBottom(TypedValue.COMPLEX_UNIT_DIP, 32.0f)
        view.setErrorTextMarginTop(TypedValue.COMPLEX_UNIT_DIP, 32.0f)
        view.setErrorTextMarginLeft(TypedValue.COMPLEX_UNIT_DIP, 32.0f)
        view.setErrorTextMarginRight(TypedValue.COMPLEX_UNIT_DIP, 32.0f)

        viewHelper.layout()

        Screenshot.snap(view).record()
    }

    @Test
    fun delChar() {
        givenViewInitializedWithDefaultAttributes()
        view.code = "134"
        givenViewIsMeasuredWithExactWidth()

        Screenshot.snap(view).setName("delChar_beforeDelete").record()
        view.onKeyDown(KeyEvent.KEYCODE_DEL, null)

        Screenshot.snap(view).setName("delChar_afterDelete").record()
    }

    private fun givenAllColorsAreChanged() {
        view.textColor = Color.BLUE
        view.underlineColor = Color.GREEN
        view.underlineSelectedColor = Color.MAGENTA
        view.errorColor = Color.YELLOW
        view.errorTextColor = Color.CYAN
    }

    private fun givenViewInitializedWithDefaultAttributes() {
        view = CodeInputView(context)
        view.setAnimateOnComplete(false)
    }

    private fun givenViewIsMeasuredWithExactWidth() {
        viewHelper = ViewHelpers.setupView(view)
                .setExactWidthDp(300)
        viewHelper.layout()
    }
}
