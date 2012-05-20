package edu.ucsb.deepspace;

public class TelescopeGalil implements TelescopeInterface {
	
	@SuppressWarnings("unused")
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
	public void moveSingle(double amount, Axis axis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void motorControl(boolean azOnOff, boolean elOnOff) {
		// TODO Auto-generated method stub

	}

	@Override
	public void index(Axis axis) {
		switch (axis) {
			case AZ:
				protocol.sendRead("XQ #HOMEAZ,0"); break;
			case EL:
				protocol.sendRead("XQ #HOMEB,1"); break;
		}
	}

	@Override
	public void setVelocity(double azVel, double elVel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOffsets(double azOffset, double elOffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getOffset(Axis axis) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getUserPos(Axis axis) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAbsolutePos(Axis axis) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void scan(ScanCommand azSc, ScanCommand elSc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopScanning() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean validMove(MoveCommand mc, double min, double max) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean motorState(Axis axis) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void move(MoveCommand mc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isMoving() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void calibrate(Coordinate c) {
		// TODO Auto-generated method stub
		
	}

}
