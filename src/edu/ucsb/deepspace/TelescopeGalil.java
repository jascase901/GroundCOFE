package edu.ucsb.deepspace;

import edu.ucsb.deepspace.ActInterface.axisType;

public class TelescopeGalil implements TelescopeInterface {
	
	private Stage stage;
	private CommGalil protocol;
	
	public TelescopeGalil(Stage stage) {
		this.stage = stage;
		protocol = new CommGalil(55555);
	}

	@Override
	public void moveAbsolute(MoveCommand az, MoveCommand el) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveRelative(MoveCommand az, MoveCommand el) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveSingle(double amount, axisType axis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void motorControl(boolean azOnOff, boolean elOnOff) {
		// TODO Auto-generated method stub

	}

	@Override
	public void index(axisType axis) {
		// TODO Auto-generated method stub
		switch (axis) {
		case AZ:
			protocol.sendRead("#XHOMEA"); break;
		case EL:
			protocol.sendRead("#XHOMEB"); break;
		}
	}

	@Override
	public void setVelocity(double azVel, double elVel) {
		// TODO Auto-generated method stub

	}

}
