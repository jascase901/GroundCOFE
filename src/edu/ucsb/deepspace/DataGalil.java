package edu.ucsb.deepspace;

import edu.ucsb.deepspace.ActInterface.axisType;

public class DataGalil implements DataInterface {
	
	private GalilStatus az = new GalilStatus(0, 0, 0, 0), el = new GalilStatus(0, 0, 0, 0);
	
	public DataGalil() {
		
	}
	
	void make (String pos, String vel, String jg, String ac, axisType axis) {
		if (pos.equals("")) {
			pos = "0";
		}
		if (vel.equals("")) {
			vel = "0";
		}
		if (jg.equals("")) {
			jg = "0";
		}
		if (ac.equals("")) {
			ac = "0";
		}
		double dPos = Double.parseDouble(pos);
		double dVel = Double.parseDouble(vel);
		double dJg = Double.parseDouble(jg);
		double dAc = Double.parseDouble(ac);
		switch (axis) {
			case AZ:
				az = new GalilStatus(dPos, dVel, dJg, dAc); break;
			case EL:
				el = new GalilStatus(dPos, dVel, dJg, dAc); break;
		}
	}

	@Override
	public String info() {
		String out = "";
		out += "Az Position:  " + az.pos + "\n";
		out += "Az Velocity:  " + az.vel + "\n";
		out += "Az Jog Speed: " + az.jg + "\n";
		out += "Az Accel:     " + az.ac + "\n";
		out += "\n";
		out += "El Position:  " + el.pos + "\n";
		out += "El Velocity:  " + el.vel + "\n";
		out += "El Jog Speed: " + el.jg + "\n";
		out += "El Accel:     " + el.ac + "\n";
		out += "\n";
		
		return out;
	}
	
	@Override
	public boolean moving() {
		boolean azMoving = (Math.abs(az.vel) >= 10);
		//boolean elMoving = (el.vel != 0);
		return azMoving;
		//return azMoving && elMoving;
	}

	@Override
	public double azPos() {
		return az.pos;
	}

	@Override
	public double elPos() {
		return el.pos;
	}
	
	public double azVel() {
		return az.vel;
	}
	
	public double elVel() {
		return el.vel;
	}
	
	class GalilStatus {
		double pos, vel, jg, ac;
		private GalilStatus(double pos, double vel, double jg, double ac) {
			this.pos = pos;
			this.vel = vel;
			this.jg = jg;
			this.ac = ac;
		}
		
	}

}
