package com.polimi.jaj.roarify;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;


import com.polimi.jaj.roarify.activity.HomeActivity;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
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
import static android.support.test.espresso.Espresso.onData;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.CoreMatchers.equalTo;


/**
 * Created by Jorge on 13/02/2017.
 */

public class UploadMessageTest {

    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<>(
            HomeActivity.class);

    @Test
    public void uploadMessage() {


        //Create the message
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withText(R.string.home)).perform(click());
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.new_message)).perform(typeText("uploadMessageText"));
        onView(withText("Roar!")).perform(click());
        //Click on the message and make it favourite
        onData(anything()).inAdapterView(withId(R.id.comments)).atPosition(0).perform(click());
        onView(withId(R.id.favButton)).perform(click());
        //Check that it is in Favourites and click on it again.
        Espresso.pressBack();
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withText(R.string.favorites)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.favorites)).atPosition(0).perform(click());
        //Uncheck the favourite option
        onView(withId(R.id.favButton)).perform(click());
        //Delete the message
        onView(withId(R.id.deleteButton)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

    }


}