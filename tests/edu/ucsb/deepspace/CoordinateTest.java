package edu.ucsb.deepspace;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.junit.Test;

@SuppressWarnings("unused")
public class CoordinateTest extends TestCase {
	private Coordinate one;
	private Coordinate aboveNorthPole = new Coordinate(1, 0, 0);
	private Coordinate primeMeridianEquator = new Coordinate(1, 90, 0);
	private RandomData rand;
	
	@Override
	protected void setUp() {
		one = new Coordinate(1, 0, 0);
		rand = new RandomDataImpl();
	}

//	@Test
//	public void testCoordinateDoubleDoubleDouble() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCoordinateDoubleDoubleDoubleBoolean() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testCoordinateDoubleDouble() {
		double az = 0;
		double el = 0;
		Coordinate c = new Coordinate(el, az);
		double expectedTheta = 90;
		double expectedPhi = 90;
		assertEquals(expectedTheta, c.getTheta());
		assertEquals(expectedPhi, c.getPhi());
		
		az = 90;
		el = 90;
		c = new Coordinate(el, az);
		expectedTheta = 0;
		expectedPhi = 0;
		assertEquals(expectedTheta, c.getTheta());
		assertEquals(expectedPhi, c.getPhi());
		
		az = 180;
		el = -45;
		c = new Coordinate(el, az);
		expectedTheta = 135;
		expectedPhi = -90;
		assertEquals(expectedTheta, c.getTheta());
		assertEquals(expectedPhi, c.getPhi());
	}

	@Test
	public void testCoordinateLatLongAlt() {
		double latitude = 90;
		double longitude = 30;
		double altitude = rand.nextUniform(0, 1000000000);
		LatLongAlt asdf = new LatLongAlt(latitude, longitude, altitude);
		Coordinate c = new Coordinate(asdf);
		double expectedTheta = 0;
		double expectedPhi = 30;
		assertEquals(expectedTheta, c.getTheta());
		assertEquals(expectedPhi, c.getPhi());
		
		latitude = 30;
		longitude = -30;
		asdf = new LatLongAlt(latitude, longitude, altitude);
		c = new Coordinate(asdf);
		expectedTheta = 60;
		expectedPhi = 330;
		assertEquals(expectedTheta, c.getTheta());
		assertEquals(expectedPhi, c.getPhi());
	}

	@Test
	public void testGetR() {
		double x = 4d;
		double y = 5;
		double z = -3.2;
		Coordinate asdf = new Coordinate(x, y, z, true);
		double expectedR = Math.sqrt(x*x + y*y + z*z);
		assertEquals(expectedR, asdf.getR());
		
		x = Math.random();
		y = Math.random();
		z = Math.random();
		asdf = new Coordinate(x, y, z, true);
		expectedR = Math.sqrt(x*x + y*y + z*z);
		assertEquals(expectedR, asdf.getR());
	}

	@Test
	public void testGetTheta() {
		double x = 4d;
		double y = 5;
		double z = -3.2;
		Coordinate asdf = new Coordinate(x, y, z, true);
		double XY = Math.sqrt(x*x + y*y);
		double expectedTheta = Math.atan2(XY, z);
		assertEquals(expectedTheta, asdf.getTheta(), .00001);
		
		x = Math.random();
		y = Math.random();
		z = Math.random();
		asdf = new Coordinate(x, y, z, true);
		XY = Math.sqrt(x*x + y*y);
		expectedTheta = Math.atan2(XY, z);
		assertEquals(expectedTheta, asdf.getTheta(), .00001);
	}

	@Test
	public void testGetPhi() {
		double x = 4d;
		double y = 5;
		double z = -3.2;
		Coordinate asdf = new Coordinate(x, y, z, true);
		double expectedPhi = Math.atan2(y, x);
		assertEquals(expectedPhi, asdf.getPhi());
		
		x = Math.random();
		y = Math.random();
		z = Math.random();
		asdf = new Coordinate(x, y, z, true);
		expectedPhi = Math.atan2(y, x);
		assertEquals(expectedPhi, asdf.getPhi());
	}

	@Test
	public void testGetX() {
		double r = 3.4;
		double theta = Math.PI/3;
		double phi = Math.PI + 1;
		Coordinate asdf = new Coordinate(r, theta, phi);
		double expectedX = r*Math.sin(Math.toRadians(theta))*Math.cos(Math.toRadians(phi));
		assertEquals(expectedX, asdf.getX());
	}

	@Test
	public void testGetY() {
		double r = 3.4;
		double theta = Math.PI/3;
		double phi = Math.PI + 1;
		Coordinate asdf = new Coordinate(r, theta, phi);
		double expectedY = r*Math.sin(Math.toRadians(theta))*Math.sin(Math.toRadians(phi));
		assertEquals(expectedY, asdf.getY());
	}

	@Test
	public void testGetZ() {
		double r = 3.4;
		double theta = Math.PI/3;
		double phi = Math.PI + 1;
		Coordinate asdf = new Coordinate(r, theta, phi);
		double expectedZ = r*Math.cos(Math.toRadians(theta));
		assertEquals(expectedZ, asdf.getZ());
	}

	@Test
	public void testRHat() {
		Coordinate c = new Coordinate(1, 0, 0, true);
		Coordinate rHat = primeMeridianEquator.rHat();
		assertEquals(c.getX(), rHat.getX(), .00000001);
		assertEquals(c.getY(), rHat.getY(), .00000001);
		assertEquals(c.getZ(), rHat.getZ(), .00000001);
	}

	@Test
	public void testThetaHat() {
		Coordinate c = new Coordinate(0, 0, -1, true);
		Coordinate thetaHat = primeMeridianEquator.thetaHat();
		assertEquals(c.getX(), thetaHat.getX(), .00000001);
		assertEquals(c.getY(), thetaHat.getY(), .00000001);
		assertEquals(c.getZ(), thetaHat.getZ(), .00000001);
	}

	@Test
	public void testPhiHat() {
		Coordinate c = new Coordinate(0, 1, 0, true);
		Coordinate phiHat = primeMeridianEquator.phiHat();
		assertEquals(c.getX(), phiHat.getX(), .00000001);
		assertEquals(c.getY(), phiHat.getY(), .00000001);
		assertEquals(c.getZ(), phiHat.getZ(), .00000001);
		
		//TODO gives incorrect output
		//phiHat = aboveNorthPole.phiHat();
		//System.out.println(phiHat.toString());
	}

	@Test
	public void testDot() {
		double x1 = 2;
		double y1 = 3;
		double z1 = 5;
		Coordinate one = new Coordinate(x1, y1, z1, true);
		double x2 = 6;
		double y2 = 8;
		double z2 = 46;
		Coordinate two = new Coordinate(x2, y2, z2, true);
		double expectedDot = x1*x2 + y1*y2 + z1*z2;
		assertEquals(expectedDot, one.dot(two));
		assertEquals(expectedDot, two.dot(one));
		
		x1 = Math.random();
		y1 = Math.random();
		z1 = Math.random();
		one = new Coordinate(x1, y1, z1, true);
		x2 = Math.random();
		y2 = Math.random();
		z2 = Math.random();
		two = new Coordinate(x2, y2, z2, true);
		expectedDot = x1*x2 + y1*y2 + z1*z2;
		assertEquals(expectedDot, one.dot(two));
		assertEquals(expectedDot, two.dot(one));
	}

	@Test
	public void testNegate() {
		double x = 4d;
		double y = 5;
		double z = -3.2;
		Coordinate asdf = new Coordinate(x, y, z, true);
		Coordinate negated = asdf.negate();
		assertEquals(-1*asdf.getX(), negated.getX());
		assertEquals(-1*asdf.getY(), negated.getY());
		assertEquals(-1*asdf.getZ(), negated.getZ());
		
		x = Math.random();
		y = Math.random();
		z = Math.random();
		asdf = new Coordinate(x, y, z, true);
		negated = asdf.negate();
		assertEquals(-1*asdf.getX(), negated.getX());
		assertEquals(-1*asdf.getY(), negated.getY());
		assertEquals(-1*asdf.getZ(), negated.getZ());
	}

	@Test
	public void testGetAz() {
		double r = 1d;
		double theta = 0;
		double phi = 0;
		Coordinate one = new Coordinate(r, theta, phi);
		double expectedAz = -1*phi + 90;
		assertEquals(expectedAz, one.getAz());
		
		phi = 90;
		one = new Coordinate(r, theta, phi);
		expectedAz = -1*phi + 90;
		assertEquals(expectedAz, one.getAz());
		
		r = Math.random();
		theta = Math.random();
		phi = Math.random();
		one = new Coordinate(r, theta, phi);
		expectedAz = -1*phi + 90;
		assertEquals(expectedAz, one.getAz());
	}

	@Test
	public void testGetEl() {
		double r = 1d;
		double theta = 0;
		double phi = 0;
		Coordinate one = new Coordinate(r, theta, phi);
		double expectedEl = -1*theta + 90;
		assertEquals(expectedEl, one.getEl());
		
		theta = 90;
		one = new Coordinate(r, theta, phi);
		expectedEl = -1*theta + 90;
		assertEquals(expectedEl, one.getEl());
		
		r = Math.random();
		theta = Math.random();
		phi = Math.random();
		one = new Coordinate(r, theta, phi);
		expectedEl = -1*theta + 90;
		assertEquals(expectedEl, one.getEl());
	}

}