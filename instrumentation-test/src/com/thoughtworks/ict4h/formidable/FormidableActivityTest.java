package com.thoughtworks.ict4h.formidable;

import com.thoughtworks.ict4h.formidable.FormidableActivity;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.thoughtworks.ict4h.formidable.FormidableActivityTest \
 * com.thoughtworks.ict4h.formidable.tests/android.test.InstrumentationTestRunner
 */
public class FormidableActivityTest extends ActivityInstrumentationTestCase2<FormidableActivity> {

    public FormidableActivityTest() {
        super("com.thoughtworks.ict4h.formidable", FormidableActivity.class);
    }

}
