/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.espen.collect.android.instrumented.utilities;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.espen.collect.android.application.Collect;
import org.espen.collect.android.widgets.datetime.DatePickerDetails;
import org.espen.collect.android.widgets.utilities.DateTimeWidgetUtils;
import org.odk.collect.testshared.TimeZoneSetter;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DateTimeUtilsTest {

    private DatePickerDetails gregorianDatePickerDetails;
    private DatePickerDetails ethiopianDatePickerDetails;
    private DatePickerDetails copticDatePickerDetails;
    private DatePickerDetails islamicDatePickerDetails;
    private DatePickerDetails bikramSambatDatePickerDetails;
    private DatePickerDetails myanmarDatePickerDetails;
    private DatePickerDetails persianDatePickerDetails;
    private DatePickerDetails buddhistDatePickerDetails;

    private Context context;
    private Locale defaultLocale;
    private TimeZone defaultTimezone;

    @Before
    public void setUp() {
        gregorianDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.CALENDAR);
        ethiopianDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.ETHIOPIAN, DatePickerDetails.DatePickerMode.SPINNERS);
        copticDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.COPTIC, DatePickerDetails.DatePickerMode.SPINNERS);
        islamicDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.ISLAMIC, DatePickerDetails.DatePickerMode.SPINNERS);
        bikramSambatDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.BIKRAM_SAMBAT, DatePickerDetails.DatePickerMode.SPINNERS);
        myanmarDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.MYANMAR, DatePickerDetails.DatePickerMode.SPINNERS);
        persianDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.PERSIAN, DatePickerDetails.DatePickerMode.SPINNERS);
        buddhistDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.BUDDHIST, DatePickerDetails.DatePickerMode.SPINNERS);

        context = Collect.getInstance();
        defaultLocale = Locale.getDefault();
        defaultTimezone = TimeZone.getDefault();
    }

    @Test
    public void getDateTimeLabelTest() {
        Locale.setDefault(Locale.ENGLISH);
        TimeZoneSetter.setTimezone(TimeZone.getTimeZone("GMT"));

        // 20 Oct 1991 14:00 GMT
        Calendar calendar = Calendar.getInstance();
        calendar.set(1991, 9, 20, 14, 0, 0);
        Date date = calendar.getTime();

        Assert.assertEquals("Oct 20, 1991", DateTimeWidgetUtils.getDateTimeLabel(date, gregorianDatePickerDetails, false, context));
        assertEquals("Oct 20, 1991, 14:00", DateTimeWidgetUtils.getDateTimeLabel(date, gregorianDatePickerDetails, true, context));

        assertEquals("9 Tikimt 1984 (Oct 20, 1991)", DateTimeWidgetUtils.getDateTimeLabel(date, ethiopianDatePickerDetails, false, context));
        assertEquals("9 Tikimt 1984, 14:00 (Oct 20, 1991, 14:00)", DateTimeWidgetUtils.getDateTimeLabel(date, ethiopianDatePickerDetails, true, context));

        assertEquals("9 Paopi 1708 (Oct 20, 1991)", DateTimeWidgetUtils.getDateTimeLabel(date, copticDatePickerDetails, false, context));
        assertEquals("9 Paopi 1708, 14:00 (Oct 20, 1991, 14:00)", DateTimeWidgetUtils.getDateTimeLabel(date, copticDatePickerDetails, true, context));

        assertEquals("11 Rabi' al-thani 1412 (Oct 20, 1991)", DateTimeWidgetUtils.getDateTimeLabel(date, islamicDatePickerDetails, false, context));
        assertEquals("11 Rabi' al-thani 1412, 14:00 (Oct 20, 1991, 14:00)", DateTimeWidgetUtils.getDateTimeLabel(date, islamicDatePickerDetails, true, context));

        assertEquals("3 कार्तिक 2048 (Oct 20, 1991)", DateTimeWidgetUtils.getDateTimeLabel(date, bikramSambatDatePickerDetails, false, context));
        assertEquals("3 कार्तिक 2048, 14:00 (Oct 20, 1991, 14:00)", DateTimeWidgetUtils.getDateTimeLabel(date, bikramSambatDatePickerDetails, true, context));

        assertEquals("12 သီတင်းကျွတ် 1353 (Oct 20, 1991)", DateTimeWidgetUtils.getDateTimeLabel(date, myanmarDatePickerDetails, false, context));
        assertEquals("12 သီတင်းကျွတ် 1353, 14:00 (Oct 20, 1991, 14:00)", DateTimeWidgetUtils.getDateTimeLabel(date, myanmarDatePickerDetails, true, context));

        assertEquals("28 Mehr 1370 (Oct 20, 1991)", DateTimeWidgetUtils.getDateTimeLabel(date, persianDatePickerDetails, false, context));
        assertEquals("28 Mehr 1370, 14:00 (Oct 20, 1991, 14:00)", DateTimeWidgetUtils.getDateTimeLabel(date, persianDatePickerDetails, true, context));

        assertEquals("20 ตุลาคม 2534 (Oct 20, 1991)", DateTimeWidgetUtils.getDateTimeLabel(date, buddhistDatePickerDetails, false, context));
        assertEquals("20 ตุลาคม 2534, 14:00 (Oct 20, 1991, 14:00)", DateTimeWidgetUtils.getDateTimeLabel(date, buddhistDatePickerDetails, true, context));
    }

    @After
    public void resetTimeZone() {
        Locale.setDefault(defaultLocale);
        TimeZoneSetter.setTimezone(defaultTimezone);
    }
}
