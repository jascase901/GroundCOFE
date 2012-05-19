package edu.ucsb.deepspace;

public interface TelescopeInterface {
	
	public void moveAbsolute(MoveCommand az, MoveCommand el);
	
	public void moveRelative(MoveCommand az, MoveCommand el);
	
	public void moveSingle(double amount, ActInterface.axisType axis);
	
	public void motorControl(boolean azOnOff, boolean elOnOff);
	
	public void index(ActInterface.axisType axis);
	
	public void setVelocity(double azVel, double elVel);
	
}