package uk.gov.saas.dsa.web.helper;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class DateHelper {

    public static Date addMinutesToDate(Timestamp ts, int minutes) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ts.getTime());
        calendar.add(Calendar.MINUTE, minutes);

        return new Date(calendar.getTimeInMillis());
    }

    public static Date addDaysToDate(Timestamp ts, int days) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ts.getTime());
        calendar.add(Calendar.DATE, days);

        return new Date(calendar.getTimeInMillis());
    }

    public static Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return new Date(calendar.getTimeInMillis());
    }
}
