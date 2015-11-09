package com.sregg.android.tv.spotifyPlayer.testUtils.viewAction;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import com.squareup.spoon.Spoon;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Created by simonreggiani on 15-06-30.
 * Source: https://github.com/square/spoon/issues/214#issuecomment-81979248
 */
public final class SpoonScreenshotAction implements ViewAction {
    private final String tag;
    private final String testClass;
    private final String testMethod;

    public SpoonScreenshotAction(String tag, String testClass, String testMethod) {
        this.tag = tag;
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    @Override
    public Matcher<View> getConstraints() {
        return instanceOf(View.class);
    }

    @Override
    public String getDescription() {
        return "Taking a screenshot using spoon.";
    }

    @Override
    public void perform(UiController uiController, View view) {
        Spoon.screenshot(getActivity(view), tag, testClass, testMethod);
    }

    private static Activity getActivity(View view) {
        Context context = view.getContext();
        while (!(context instanceof Activity)) {
            if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            } else {
                throw new IllegalStateException("Got a context of class "
                        + context.getClass()
                        + " and I don't know how to get the Activity from it");
            }
        }
        return (Activity) context;
    }

    public static void perform(String tag, String className, String methodName) {
        onView(isRoot()).perform(new SpoonScreenshotAction(tag, className, methodName));
    }
}
