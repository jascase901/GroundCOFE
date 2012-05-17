package edu.ucsb.deepspace;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Coordinate implements Comparable<Coordinate> {

	private double r, theta, phi;
	private double x, y, z;
	static double RADIUS_EARTH = 6738.1;
	
	public Coordinate (double r, double theta, double phi) {
		this.r = r;
		this.theta = theta;
		this.phi = phi;
		this.x = r*Math.sin(Math.toRadians(theta))*Math.cos(Math.toRadians(phi));
		this.y = r*Math.sin(Math.toRadians(theta))*Math.sin(Math.toRadians(phi));
		this.z = r*Math.cos(Math.toRadians(theta));
	}
	
	public Coordinate (double x, double y, double z, boolean cartesian) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.r = Math.sqrt(x*x + y*y + z*z);
		this.theta = Math.acos(z/r);
		this.phi = Math.atan(y/x);
	}
	
	public Coordinate (double el, double az) {
		this.r = 1;
		this.theta = -1*el + 90;
		this.phi = -1*az + 90;
		this.x = r*Math.sin(Math.toRadians(theta))*Math.cos(Math.toRadians(phi));
		this.y = r*Math.sin(Math.toRadians(theta))*Math.sin(Math.toRadians(phi));
		this.z = r*Math.cos(Math.toRadians(theta));
	}
	
	public Coordinate (LatLongAlt loc) {
		this.r = RADIUS_EARTH + loc.getAltitude();
		this.theta = 90 - loc.getLatitude();
		if (loc.getLongitude() > 0) {
			this.phi = loc.getLongitude();
		}
		else {
			this.phi = 360 + loc.getLongitude();
		}
		this.x = r*Math.sin(Math.toRadians(theta))*Math.cos(Math.toRadians(phi));
		this.y = r*Math.sin(Math.toRadians(theta))*Math.sin(Math.toRadians(phi));
		this.z = r*Math.cos(Math.toRadians(theta));
	}
	
	public double getR() {return r;}
	public double getTheta() {return theta;}
	public double getPhi() {return phi;}
	public double getX() {return x;}
	public double getY() {return y;}
	public double getZ() {return z;}

	public Coordinate rHat() {
		double xCom = x / r;
		double yCom = y / r;
		double zCom = z / r;
		return new Coordinate(xCom, yCom, zCom, true);
	}

	//TODO breaks if x and y are 0
	public Coordinate thetaHat() {
		double xCom = x*z / (r*Math.sqrt(x*x+y*y));
		double yCom = y*z / (r*Math.sqrt(x*x+y*y));
		double zCom = -Math.sqrt(x*x+y*y) / r;
		return new Coordinate(xCom, yCom, zCom, true);
	}
	
	//TODO breaks if x and y are 0
	public Coordinate phiHat() {
		double xCom = -y / Math.sqrt(x*x+y*y);
		double yCom = x / Math.sqrt(x*x+y*y);
		return new Coordinate(xCom, yCom, 0, true);
	}

	public double dot(Coordinate other) {
		return x*other.getX() + y*other.getY() + z*other.getZ();
	}
	
	public Coordinate negate() {
		return new Coordinate(-x, -y, -z, true);
	}
	
	public double getAz() {
		return -1*phi + 90;
	}
	
	public double getEl() {
		return -1*theta + 90;
	}
	
	public String toString() {
		return "phi:  " + phi + "  theta:  " + theta + "  r:  " + r;
	}
	
	@Override
	public boolean equals (Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != this.getClass()) return false;
		Coordinate rhs = (Coordinate) o;
		return new EqualsBuilder().append(x, rhs.x).append(y, rhs.y).append(z, rhs.z).isEquals(); 
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 13).append(x).append(y).append(z).toHashCode();
	}

	@Override
	public int compareTo(Coordinate o) {
		return new CompareToBuilder().append(this.x, o.x).append(this.y, o.y).append(this.z, o.z).toComparison();
	}
}
