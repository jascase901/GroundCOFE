package edu.ucsb.deepspace;

public class DataGalil implements DataInterface {
	
	private GalilStatus az = new GalilStatus(0, 0, 0, 0,  false, false), el = new GalilStatus(0, 0, 0, 0, false, false);
	
	public DataGalil() {
		
	}
	static DataGalil asdf = new DataGalil();
	static {
		asdf.make("0", "0", "0", "0", "1", "0", Axis.AZ);
		asdf.make("0", "0", "0", "0", "1", "0", Axis.EL);
	}
	
	static DataGalil blank() {
		return asdf;
	}
	
	void make (String pos, String vel, String jg, String ac, String motor, String motion, Axis axis) {
//		if (pos.equals("")) {
//			pos = "0";
//		}
//		if (vel.equals("")) {
//			vel = "0";
//		}
//		if (jg.equals("")) {
//			jg = "0";
//		}
//		if (ac.equals("")) {
//			ac = "0";
//		}
		double dPos = Double.parseDouble(pos);
		double dVel = Double.parseDouble(vel);
		double dJg = Double.parseDouble(jg);
		double dAc = Double.parseDouble(ac);
		
		boolean motorState = (0 == Double.parseDouble(motor));
		boolean motionState = (1 == Double.parseDouble(motion));
		switch (axis) {
			case AZ:
				az = new GalilStatus(dPos*Axis.AZ.getPolarity(), dVel, dJg, dAc, motorState, motionState); break;
			case EL:
				el = new GalilStatus(dPos*Axis.EL.getPolarity(), dVel, dJg, dAc, motorState, motionState); break;
		}
	}

	@Override
	public String info() {
		String out2 = String.format("%1$-10s %2$-9s %3$-9s", "Value", "Azimuth", "Elevation") + "\n\n";
		out2 += String.format(Formatters.ACTINFO_FORMAT, "Position", az.pos, el.pos) + "\n";
		out2 += String.format(Formatters.ACTINFO_FORMAT, "Velocity", az.vel, el.vel) + "\n";
		out2 += String.format(Formatters.ACTINFO_FORMAT, "Max Speed", az.jg, el.jg) + "\n";
		out2 += String.format(Formatters.ACTINFO_FORMAT, "Max Accel", az.ac, el.ac) + "\n";
		
		return out2;
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
	
	class GalilStatus {
		double pos, vel, jg, ac;
	
		boolean motorState, motionState;
		
		private GalilStatus(double pos, double vel, double jg, double ac, boolean motorState, boolean motionState) {
			this.pos = pos;
			this.vel = vel;
			this.jg = jg;
			this.ac = ac;
			this.motorState = motorState;
			this.motionState = motionState;
			
		}
		
	}

	@Override
	public double azMaxVel() {
		return az.jg;
	}

	@Override
	public double elMaxVel() {
		return el.jg;
	}
	
	@Override
	public boolean motorState(Axis axis) {
		switch (axis) {
			case AZ:
				return az.motorState;
			case EL:
				return el.motorState;
			default:
				assert false; //This can only be reached if a new axis is added.
		}
		return false;
	}
	
	@Override
	public boolean motionState(Axis axis) {
		switch (axis) {
			case AZ:
				return az.motionState;
			case EL:
				return el.motionState;
			default:
				assert false; //This can only be reached if a new axis is added.
		}
		return false;
	}
	
	

}
