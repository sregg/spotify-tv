package com.sregg.android.tv.spotify.testUtils.rule;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.FailureHandler;
import android.support.test.espresso.base.DefaultFailureHandler;
import android.view.View;

import com.sregg.android.tv.spotify.testUtils.viewAction.SpoonScreenshotAction;

import org.hamcrest.Matcher;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Created by simonreggiani on 15-07-28.
 */
public class FailureScreenshotRule implements TestRule {
    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setupFailureHandler(description);
                base.evaluate();
            }
        };
    }

    public void setupFailureHandler(Description description) {
        final String testClassName = description.getClassName();
        final String testMethodName = description.getMethodName();
        final Context context = InstrumentationRegistry.getTargetContext();
        Espresso.setFailureHandler(new FailureHandler() {
            @Override
            public void handle(Throwable throwable, Matcher<View> matcher) {
                SpoonScreenshotAction.perform("espresso_assertion_failed", testClassName, testMethodName);
                new DefaultFailureHandler(context).handle(throwable, matcher);
            }
        });
    }
}
