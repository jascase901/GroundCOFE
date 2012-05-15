package edu.ucsb.deepspace;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.junit.Test;

@SuppressWarnings("unused")
public class CoordinateTest extends TestCase {
	private Coordinate one;
	
	@Override
	protected void setUp() {
		one = new Coordinate(1, 0, 0);
	}

	@Test
	public void testCoordinateDoubleDoubleDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testCoordinateDoubleDoubleDoubleBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testCoordinateDoubleDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testCoordinateLatLongAlt() {
		fail("Not yet implemented");
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
		fail("Not yet implemented");
	}

	@Test
	public void testThetaHat() {
		fail("Not yet implemented");
	}

	@Test
	public void testPhiHat() {
		fail("Not yet implemented");
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

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

}
