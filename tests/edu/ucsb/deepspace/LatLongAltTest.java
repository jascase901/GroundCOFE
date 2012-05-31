package edu.ucsb.deepspace;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.junit.Test;

public class LatLongAltTest extends TestCase {
	
	LatLongAlt asdf = new LatLongAlt(30, 50, 10);
	private final static double DTR = Math.PI / 180d;
	private final static double RTD = 180d / Math.PI;
	
	private final static double RTH = 12d / Math.PI;
	private final static double HTR = Math.PI / 12d;
	
	@Override
	protected void setUp() {
		
	}

	@Test
	public void testGetLatitude() {
		double az = 0;
		double el = 0;
		Coordinate c = new Coordinate(el, az);
		LatLongAlt l = new LatLongAlt(c);
		double expectedLatitude = 0;
		assertEquals(expectedLatitude, l.getLatitude());
		
		az = 90;
		el = 90;
		c = new Coordinate(el, az);
		l = new LatLongAlt(c);
		expectedLatitude = 90;
		assertEquals(expectedLatitude, l.getLatitude());
		
		az = 180;
		el = -45;
		c = new Coordinate(el, az);
		l = new LatLongAlt(c);
		expectedLatitude = -45;
		assertEquals(expectedLatitude, l.getLatitude());
	}

	@Test
	public void testGetLongitude() {
		double az = 0;
		double el = 0;
		Coordinate c = new Coordinate(el, az);
		LatLongAlt l = new LatLongAlt(c);
		double expectedLongitude = 90;
		assertEquals(expectedLongitude, l.getLongitude());
		
		az = 90;
		el = 90;
		c = new Coordinate(el, az);
		l = new LatLongAlt(c);
		expectedLongitude = 0;
		assertEquals(expectedLongitude, l.getLongitude());
		
		az = 180;
		el = -45;
		c = new Coordinate(el, az);
		l = new LatLongAlt(c);
		expectedLongitude = -90;
		assertEquals(expectedLongitude, l.getLongitude());
	}

	@Test
	public void testGetAltitude() {
		double az = 0;
		double el = 0;
		Coordinate c = new Coordinate(el, az);
		LatLongAlt l = new LatLongAlt(c);
		double expectedAltitude = -6737.1;
		assertEquals(expectedAltitude, l.getAltitude());
		
		az = 90;
		el = 90;
		c = new Coordinate(el, az);
		l = new LatLongAlt(c);
		expectedAltitude = -6737.1;
		assertEquals(expectedAltitude, l.getAltitude());
		
		az = 180;
		el = -45;
		c = new Coordinate(el, az);
		l = new LatLongAlt(c);
		expectedAltitude = -6737.1;
		assertEquals(expectedAltitude, l.getAltitude());
	}

	@Test
	public void testAzelToRa() {
		double az = 0;
		double el = 0;
		Coordinate c = new Coordinate(el, az);
		LatLongAlt l = new LatLongAlt(c);
		double expectedRa = ((l.lst()) + 24) % 24;
		assertEquals(expectedRa, l.azelToRa(az,el), .00000001);
		
		az = 90;
		el = 90;
		c = new Coordinate(el, az);
		l = new LatLongAlt(c);
		double newaz = az * DTR;
		double newel = el * DTR;
		double lst = l.lst() * HTR;
		double lat = l.getLatitude() * DTR;
		
		double h = calcXY(newel, newaz, lat);
		double ra = lst - h;
		
		ra = ra * RTH;
		ra = (ra + 24)%24;
		expectedRa = ra;
		assertEquals(expectedRa, l.azelToRa(az,el), .00000001);
		
		az = 180;
		el = -45;
		c = new Coordinate(el, az);
		l = new LatLongAlt(c);
		newaz = az * DTR;
		newel = el * DTR;
		lst = l.lst() * HTR;
		lat = l.getLatitude() * DTR;
		
		h = calcXY(newel, newaz, lat);
		ra = lst - h;
		
		ra = ra * RTH;
		ra = (ra + 24)%24;
		expectedRa = ra;
		assertEquals(expectedRa, l.azelToRa(az,el), .00000001);
		
		
	}
	
	public double calcXY(double arg1, double arg2, double arg3) {
		double y = -Math.cos(arg1) * Math.sin(arg2);
		double x = Math.sin(arg1) * Math.cos(arg3) - Math.cos(arg1) * Math.cos(arg2) * Math.sin(arg3);
		return Math.atan2(y, x);
	}
	public double calcZ(double arg1, double arg2, double arg3) {
		double z = Math.sin(arg1) * Math.sin(arg2) + Math.cos(arg1) * Math.cos(arg3) * Math.cos(arg2);
		return Math.asin(z) * RTD;
	}

	@Test
	public void testRadecToAz() {
		double lat = 30;
		double lon = 50;
		double alt = 90;
		double ra = 0;
		double dec = 0;
		LatLongAlt l = new LatLongAlt(lat, lon, alt);
		double newdec = dec * DTR;
		double newlat = lat * DTR;
		double H = (l.lst() - ra) * HTR;
		
		double az = calcXY(newdec, H, newlat);
		
		az = az * RTD;
		if (az < 0) az = az + 360;
		double expectedAz = az;
		assertEquals(expectedAz, l.radecToAz(ra, dec), .00000001);
		
		ra = 20;
		dec = 1;
		newdec = dec * DTR;
		newlat = lat * DTR;
		H = (l.lst() - ra) * HTR;
		
		az = calcXY(newdec, H, newlat);
		
		az = az * RTD;
		if (az < 0) az = az + 360;
		expectedAz = az;
		assertEquals(expectedAz, l.radecToAz(ra, dec), .00000001);
		
		ra = 240;
		dec = -100;
		newdec = dec * DTR;
		newlat = lat * DTR;
		H = (l.lst() - ra) * HTR;
		
		az = calcXY(newdec, H, newlat);
		
		az = az * RTD;
		if (az < 0) az = az + 360;
		expectedAz = az;
		assertEquals(expectedAz, l.radecToAz(ra, dec), .00000001);
	}

	@Test
	public void testAzelToDec() {
		double az = 0;
		double el = 0;
		Coordinate c = new Coordinate(el, az);
		LatLongAlt l = new LatLongAlt(c);
		double newaz = az * DTR;
		double newel = el * DTR;
		double lat = l.getLatitude() * DTR;
		
		double expectedDec = calcZ(newel, lat, newaz);
		assertEquals(expectedDec, l.azelToDec(az,el), .00000001);
		
		az = 90;
		el = 90;
		c = new Coordinate(el, az);
		l = new LatLongAlt(c);
		newaz = az * DTR;
		newel = el * DTR;
		lat = l.getLatitude() * DTR;
		
		expectedDec = calcZ(newel, lat, newaz);
		assertEquals(expectedDec, l.azelToDec(az,el), .00000001);
		
		az = 180;
		el = -45;
		c = new Coordinate(el, az);
		l = new LatLongAlt(c);
		newaz = az * DTR;
		newel = el * DTR;
		lat = l.getLatitude() * DTR;
		
		expectedDec = calcZ(newel, lat, newaz);
		assertEquals(expectedDec, l.azelToDec(az,el), .00000001);
	}

	@Test
	public void testRadecToEl() {
		double lat = 30;
		double lon = 50;
		double alt = 90;
		double ra = 0;
		double dec = 0;
		LatLongAlt l = new LatLongAlt(lat, lon, alt);
		double newdec = dec * DTR;
		double newlat = lat * DTR;
		double H = (l.lst() - ra) * HTR;
		
		double expectedEl = calcZ(newdec, newlat, H);
		assertEquals(expectedEl, l.radecToEl(ra, dec), .00000001);
		
		ra = 80;
		dec = -80;
		newdec = dec * DTR;
		newlat = lat * DTR;
		H = (l.lst() - ra) * HTR;
		
		expectedEl = calcZ(newdec, newlat, H);
		assertEquals(expectedEl, l.radecToEl(ra, dec), .00000001);
		
		ra = 25;
		dec = 240;
		newdec = dec * DTR;
		newlat = lat * DTR;
		H = (l.lst() - ra) * HTR;
		
		expectedEl = calcZ(newdec, newlat, H);
		assertEquals(expectedEl, l.radecToEl(ra, dec), .00000001);
	}

	@Test
	public void testGmt() {
		Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		DateFormat a = new SimpleDateFormat("HH:mm:ss");
		a.setTimeZone(TimeZone.getTimeZone("GMT"));
		String expectedGmt =  a.format(gmt.getTime());
		assertEquals(expectedGmt, asdf.gmt());
	}

	@Test
	public void testLst() {
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
		
		double lon = asdf.getLongitude() * (12d/180d);
		double lst = gmst + st + lon;
		if (lst > 24) lst = lst - 24;
		if (lst < 0) lst = lst + 24;
		
		double expectedlst = lst;
		assertEquals(expectedlst, asdf.lst(), .00000001);
	}

}
