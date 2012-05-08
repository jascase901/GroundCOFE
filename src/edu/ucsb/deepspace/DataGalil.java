package edu.ucsb.deepspace;

public class DataGalil implements DataInterface {
	
	private GalilStatus az = new GalilStatus(0, 0), el = new GalilStatus(0, 0);
	
	public DataGalil() {
		
	}
	
	void makeAz(String pos, String vel) {
		if (pos.equals("")) {
			pos = "0";
		}
		if (vel.equals("")) {
			vel = "0";
		}
		double dPos = Double.parseDouble(pos);
		double dVel = Double.parseDouble(vel); 
		az = new GalilStatus(dPos, dVel);
	}
	
	void makeEl(String pos, String vel) {
		if (pos.equals("")) {
			pos = "0";
		}
		if (vel.equals("")) {
			vel = "0";
		}
		double dPos = Double.parseDouble(pos);
		double dVel = Double.parseDouble(vel); 
		el = new GalilStatus(dPos, dVel);
	}

	@Override
	public String info() {
		String out = "";
		out += "Az Position: " + az.pos + "\n";
		out += "Az Velocity: " + az.vel + "\n";
		out += "El Position: " + el.pos + "\n";
		out += "El Velocity: " + el.vel;
		
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
		double pos, vel;
		private GalilStatus(double pos, double vel) {
			this.pos = pos;
			this.vel = vel;
		}
		
	}

}
