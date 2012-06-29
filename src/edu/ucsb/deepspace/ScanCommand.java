package edu.ucsb.deepspace;
/**
 * Lumps everything needed to run a scan into one object for convenience.
 * 
 *
 */
public class ScanCommand {
	
	private final double min, max, time, reps;
	
	public ScanCommand(double min, double max, double time, int reps) {
		this.min = min;
		this.max = max;
		this.time = time;
		this.reps = reps;
		
	}
	
	public ScanCommand(double min, double max) {
		this.min = min;
		this.max = max;
		this.time = 0;
		this.reps = 0;
	}
	
	
	public double getMin() {return min;}
	public double getMax() {return max;}
	public double getTime() {return time;}
	public double getReps() {return reps;}
	
	
}