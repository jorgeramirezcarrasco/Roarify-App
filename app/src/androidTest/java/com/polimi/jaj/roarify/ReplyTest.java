package com.polimi.jaj.roarify;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;

import com.polimi.jaj.roarify.activity.HomeActivity;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isOpen;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

/**
 * Created by Jorge on 15/02/2017.
 */
/*This test creates a message and a reply to it, the it deletes both of them*/

public class ReplyTest {

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
        onView(withId(R.id.new_message)).perform(typeText("Luke, I'm your father"));
        onView(withText("Roar!")).perform(click());
        //Click on the message and make it favourite
        onData(anything()).inAdapterView(withId(R.id.comments)).atPosition(0).perform(click());
        onView(withId(R.id.replyButton)).perform(click());
        onView(withId(R.id.response)).perform(typeText("I'm Luke"));
        onView(withText("Reply")).perform(click());
        onData(anything()).inAdapterView(withId(R.id.comments)).atPosition(0).perform(click());
        //Delete the message(reply)
        onView(withId(R.id.deleteButton)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());
        //Delete the message(father)
        onView(withId(R.id.deleteButton)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());



    }
}
