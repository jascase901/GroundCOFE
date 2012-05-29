package edu.ucsb.deepspace;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Formatters {
	public static DecimalFormat INT = new DecimalFormat("####");
	public static DecimalFormat TWO_POINTS_FORCE = new DecimalFormat("##.00");
	public static DecimalFormat TWO_POINTS = new DecimalFormat("##.##");
	public static DecimalFormat THREE_POINTS = new DecimalFormat("##.###");
	public static DecimalFormat FOUR_POINTS = new DecimalFormat("##.####");
	public static DateFormat HOUR_MIN_SEC = new SimpleDateFormat("HH:mm:ss");
	public static DecimalFormat TWO_DIGITS = new DecimalFormat("00");
	public static DecimalFormat DEGREE_POS = new DecimalFormat("###.##");
	public static String ACTINFO_FORMAT = "%1$-10s %2$9.2f %3$9.2f";
	
	public static String lstFormatter(double hour, double min, double sec) {
		String out = TWO_DIGITS.format(hour) + ":" + TWO_DIGITS.format(Math.floor(min)) + ":" + TWO_DIGITS.format(Math.floor(sec));
		return out;
	}
	
	public static String formatLst(double lst) {
		String out = "";
		double hourLst = (int) lst;
		double minLst = (lst - hourLst)*60;
		double secLst = (minLst - (int)minLst)*60;
		out = lstFormatter(hourLst, minLst, secLst);
		return out;
	}
	
}