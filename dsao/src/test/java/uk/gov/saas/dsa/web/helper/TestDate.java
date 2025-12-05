package uk.gov.saas.dsa.web.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TestDate {

	public static void main(String[] args) {
		String date1 = "2024-01-30";
		String date2 = "2024-02-29";
		System.out.println("Chrono Days: " + daysBetweenDates(date1, date2));
		String date3 = "2023-01-30";
		String date4 = "2023-02-28";
		System.out.println("Chrono Days: " + daysBetweenDates(date3, date4));

	}

	public static int daysBetweenDates(String date1, String date2) {
		LocalDate dt1 = LocalDate.parse(date1);
		LocalDate dt2 = LocalDate.parse(date2);
		long diffDays = ChronoUnit.DAYS.between(dt1, dt2);
		return Math.abs((int) diffDays);
	}

	public static int daysBetweenDatesw(Date date1, Date date2) {
		SimpleDateFormat dates = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr1 = dates.format(date1);
		String dateStr2 = dates.format(date2);
		LocalDate dt1 = LocalDate.parse(dateStr1);
		LocalDate dt2 = LocalDate.parse(dateStr2);
		long diffDays = ChronoUnit.DAYS.between(dt1, dt2);
		return Math.abs((int) diffDays);
	}
}