package com.polimi.jaj.roarify;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;


import com.polimi.jaj.roarify.activity.HomeActivity;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.contrib.DrawerMatchers.isOpen;


/**
 * Created by Jorge on 13/02/2017.
 */

public class UploadMessageTest {

    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<>(
            HomeActivity.class);

    @Test
    public void uploadMessage() {

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withText(R.string.home)).perform(click());
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.new_message)).perform(typeText("uploadMessageText"));
        onView(withText("Roar!")).perform(click());
    }


}