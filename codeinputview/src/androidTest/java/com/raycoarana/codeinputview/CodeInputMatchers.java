package com.raycoarana.codeinputview;

import android.view.View;

import com.raycoarana.codeinputview.CodeInputView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CodeInputMatchers {

    public static Matcher<View> withCode(final String code) {
        return new CodeInputViewMatcher() {

            @Override
            public void describeTo(Description description) {
                description.appendValue(code);
            }

            @Override
            protected boolean matchesWith(CodeInputView item) {
                return code.equals(item.getCode());
            }
        };
    }

    static abstract class CodeInputViewMatcher extends TypeSafeMatcher<View> {

        @Override
        protected boolean matchesSafely(View item) {
            return item instanceof CodeInputView && matchesWith((CodeInputView) item);
        }

        protected abstract boolean matchesWith(CodeInputView item);

    }

}
