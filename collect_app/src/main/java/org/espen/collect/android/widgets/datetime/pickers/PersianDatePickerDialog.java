/*
 * Copyright 2019 Nafundi
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

package org.espen.collect.android.widgets.datetime.pickers;

import org.joda.time.LocalDateTime;
import org.joda.time.chrono.PersianChronologyKhayyamBorkowski;
import org.espen.collect.android.R;
import org.espen.collect.android.widgets.datetime.DateTimeUtils;

import java.util.Arrays;

public class PersianDatePickerDialog extends CustomDatePickerDialog {
    private static final int MIN_SUPPORTED_YEAR = 1278; //1900 in Gregorian calendar
    private static final int MAX_SUPPORTED_YEAR = 1478; //2100 in Gregorian calendar

    private String[] monthsArray;

    @Override
    public void onResume() {
        super.onResume();
        monthsArray = getResources().getStringArray(R.array.persian_months);
        setUpValues();
    }

    @Override
    protected void updateDays() {
        LocalDateTime localDateTime = getCurrentPersianDate();
        setUpDayPicker(localDateTime.getDayOfMonth(), localDateTime.dayOfMonth().getMaximumValue());
    }

    @Override
    protected LocalDateTime getOriginalDate() {
        return getCurrentPersianDate();
    }

    private void setUpDatePicker() {
        LocalDateTime persianDate = DateTimeUtils
                .skipDaylightSavingGapIfExists(getDate())
                .toDateTime()
                .withChronology(PersianChronologyKhayyamBorkowski.getInstance())
                .toLocalDateTime();
        setUpDayPicker(persianDate.getDayOfMonth(), persianDate.dayOfMonth().getMaximumValue());
        setUpMonthPicker(persianDate.getMonthOfYear(), monthsArray);
        setUpYearPicker(persianDate.getYear(), MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR);
    }

    private void setUpValues() {
        setUpDatePicker();
        updateGregorianDateLabel();
    }

    private LocalDateTime getCurrentPersianDate() {
        int persianDay = getDay();
        int persianMonth = Arrays.asList(monthsArray).indexOf(getMonth());
        int persianYear = getYear();

        LocalDateTime persianDate = new LocalDateTime(persianYear, persianMonth + 1, 1, 0, 0, 0, 0, PersianChronologyKhayyamBorkowski.getInstance());
        if (persianDay > persianDate.dayOfMonth().getMaximumValue()) {
            persianDay = persianDate.dayOfMonth().getMaximumValue();
        }
        if (persianDay < persianDate.dayOfMonth().getMinimumValue()) {
            persianDay = persianDate.dayOfMonth().getMinimumValue();
        }

        return new LocalDateTime(persianYear, persianMonth + 1, persianDay, 0, 0, 0, 0, PersianChronologyKhayyamBorkowski.getInstance());
    }
}
