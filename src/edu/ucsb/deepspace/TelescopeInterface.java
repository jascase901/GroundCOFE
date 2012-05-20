package edu.ucsb.deepspace;

public interface TelescopeInterface {
	
	public void moveAbsolute(MoveCommand az, MoveCommand el);
	
	public void moveRelative(MoveCommand az, MoveCommand el);
	
	public void moveSingle(double amount, Axis axis);
	
	public void motorControl(boolean azOnOff, boolean elOnOff);
	
	public void index(Axis axis);
	
	public void setVelocity(double azVel, double elVel);
	
	public void setOffsets(double azOffset, double elOffset);
	
	public double getOffset(Axis axis);
	
	public double getUserPos(Axis axis);
	
	public double getAbsolutePos(Axis axis);
	
	public void scan(ScanCommand azSc, ScanCommand elSc);
	
	public void stopScanning();
	
	public boolean validMove(MoveCommand mc, double min, double max);
	
	public boolean motorState(Axis axis);
	
	public void move(MoveCommand mc);
	
	public boolean isMoving();
	
	public void calibrate(Coordinate c);
	
}