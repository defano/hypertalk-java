/*
 * DateUtils
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:11 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypertalk.utils;

import com.defano.hypertalk.ast.common.DateFormat;
import com.defano.hypertalk.ast.common.Value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public final static SimpleDateFormat LONG_TIME = new SimpleDateFormat("h:mm:ss a");
    public final static SimpleDateFormat SHORT_TIME = new SimpleDateFormat("h:mm a");

    public final static SimpleDateFormat LONG_DATE = new SimpleDateFormat("EEEEE, MMMMM dd, yyyy");
    public final static SimpleDateFormat SHORT_DATE = new SimpleDateFormat("MM/dd/yy");
    public final static SimpleDateFormat ABBREV_DATE = new SimpleDateFormat("EEE, MMM dd, yyyy");

    public static Value valueOf(Date d, DateFormat format) {
        switch (format) {
            case LONG:
                return new Value(LONG_DATE.format(d));
            case SHORT:
                return new Value(SHORT_DATE.format(d));
            case ABBREVIATED:
                return new Value(ABBREV_DATE.format(d));

            default:
                throw new IllegalArgumentException("Bug! Unimplemented date format.");
        }
    }

    public static Date dateOf(Value value) {
        String text = value.stringValue();

        try {
            return LONG_TIME.parse(text);
        } catch (ParseException e) {}

        try {
            return SHORT_TIME.parse(text);
        } catch (ParseException e) {}

        try {
            return LONG_DATE.parse(text);
        } catch (ParseException e) {}

        try {
            return SHORT_DATE.parse(text);
        } catch (ParseException e) {}

        try {
            return ABBREV_DATE.parse(text);
        } catch (ParseException e) {}

        return null;
    }

}
