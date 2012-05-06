package edu.ucsb.deepspace;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import edu.ucsb.deepspace.Formatters;

public class LatLongAlt {
	private final double latitude;
	private final double longitude;
	private final double altitude;
	
	private final static double DTR = Math.PI / 180d;
	private final static double RTD = 180d / Math.PI;
	
	private final static double RTH = 12d / Math.PI;
	private final static double HTR = Math.PI / 12d;
	
	public LatLongAlt(double latitude, double longitude, double alt) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = alt;
	}
	
	public LatLongAlt(Coordinate c) {
		this.latitude = 90 - c.getTheta();
		this.altitude = c.getR() - Coordinate.RADIUS_EARTH;
		double lon = c.getPhi();
		if (lon > 180) lon = lon-360;
		this.longitude = lon;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public double getAltitude() {
		return altitude;
	}
	
	public double azelToRa(double az, double el) {	
		az = az * DTR;
		el = el * DTR;
		double lst = lst() * HTR;
		double lat = latitude * DTR;
		
		double h = calcXY(el, az, lat);
		double ra = lst - h;
		
		ra = ra * RTH;
		ra = (ra + 24)%24;
		return ra;
	}
	
	public double radecToAz(double ra, double dec) {
		dec = dec * DTR;
		double lat = latitude * DTR;
		double H = (lst() - ra) * HTR;
		
		double az = calcXY(dec, H, lat);
		
		az = az * RTD;
		if (az < 0) az = az + 360;
		return az;
	}
	
	private double calcXY(double arg1, double arg2, double arg3) {
		double y = -Math.cos(arg1) * Math.sin(arg2);
		double x = Math.sin(arg1) * Math.cos(arg3) - Math.cos(arg1) * Math.cos(arg2) * Math.sin(arg3);
		return Math.atan2(y, x);
	}
	
	public double azelToDec(double az, double el) {
		az = az * DTR;
		el = el * DTR;
		double lat = latitude * DTR;
		
		return calcZ(el, lat, az);
	}
	
	public double radecToEl(double ra, double dec) {
		dec = dec * DTR;
		double lat = latitude * DTR;
		double H = (lst() - ra) * HTR;
		
		return calcZ(dec, lat, H);
	}
	
	private double calcZ(double arg1, double arg2, double arg3) {
		double z = Math.sin(arg1) * Math.sin(arg2) + Math.cos(arg1) * Math.cos(arg3) * Math.cos(arg2);
		return Math.asin(z) * RTD;
	}
	
	public String gmt() {
		Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		DateFormat a = new SimpleDateFormat("HH:mm:ss");
		a.setTimeZone(TimeZone.getTimeZone("GMT"));
		return a.format(gmt.getTime());
	}
	
	public double lst() {
		Calendar old = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		old.set(1858, Calendar.NOVEMBER, 16, 12, 0, 0);
		Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		double GMT = gmt.get(Calendar.HOUR_OF_DAY) + gmt.get(Calendar.MINUTE)/60d + gmt.get(Calendar.SECOND)/3600d;
		
		double oldTime = old.getTimeInMillis();
		double newTime = gmt.getTimeInMillis();
		
		double myJD = Math.floor((newTime - oldTime)/(1000 * 60 * 60 * 24)) - 15020.5;
		
		double tu = myJD / 36525d;
		
		double gmst, st;
		gmst = 6.6460656 + 2400.051262 * tu + .0000258 * tu * tu;
		gmst = gmst - (int)gmst + (int)(gmst % 24);
		if (gmst > 24) gmst = gmst - 24;
		if (gmst < 0) gmst = gmst + 24;
		st = (1.002737909 + tu * .589 * Math.pow(10, -10)) * GMT;
		
		double lon = longitude * (12d/180d);
		double lst = gmst + st + lon;
		if (lst > 24) lst = lst - 24;
		if (lst < 0) lst = lst + 24;
		
		return lst;
	}
	
	public String toString() {
		return "lat:  " + latitude + "  lon:  " + longitude + "  alt:  " + altitude;
	}

	public String guiString() {
		String out = "";
		out += "Latitude:  " + Formatters.FOUR_POINTS.format(latitude) + "\n";
		out += "Longitude: " + Formatters.FOUR_POINTS.format(longitude) + "\n"; 
		out += "Altitude:  " + altitude + " (km)";
		return out;
	}
	
}