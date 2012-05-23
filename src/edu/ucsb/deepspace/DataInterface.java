package edu.ucsb.deepspace;

public interface DataInterface {
	
	public String info();
	
	public boolean moving();
	
	public double azPos();
	
	public double elPos();
	
	public double azMaxVel();
	
	public double elMaxVel();
	
}