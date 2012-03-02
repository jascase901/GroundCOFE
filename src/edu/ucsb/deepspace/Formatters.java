package edu.ucsb.deepspace;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Formatters {
	public static DecimalFormat INT = new DecimalFormat("####");
	public static DecimalFormat TWO_POINTS_FORCE = new DecimalFormat("##.00");
	public static DecimalFormat TWO_POINTS = new DecimalFormat("##.##");
	public static DecimalFormat FOUR_POINTS = new DecimalFormat("##.####");
	public static DateFormat HOUR_MIN_SEC = new SimpleDateFormat("HH:mm:ss");
	public static DecimalFormat TWO_DIGITS = new DecimalFormat("00");
	
	public static String lstFormatter(double hour, double min, double sec) {
		String out = TWO_DIGITS.format(hour) + ":" + TWO_DIGITS.format(Math.floor(min)) + ":" + TWO_DIGITS.format(Math.floor(sec));
		return out;
	}
}