package edu.ucsb.deepspace;

public interface DataInterface {
	
	public String info();
	
	public boolean moving();
	
	public double azPos();
	
	public double elPos();
	
	public double azMaxVel();
	
	public double elMaxVel();

	/**
	 * Returns true if axis is done moving.
	 * @param axis
	 * @return
	 */
	boolean motionState(Axis axis);

	/**
	 * Returns true if motor is on.
	 * @param axis
	 * @return
	 */
	boolean motorState(Axis axis);
	
	/**
	 * returns true if thread 0 is executing
	 * @return
	 */
	public boolean isThreadEx(int thread);
	

	
}