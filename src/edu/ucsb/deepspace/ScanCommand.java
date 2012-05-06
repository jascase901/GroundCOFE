package edu.ucsb.deepspace;

public class ScanCommand {
	
	private final double min, max, time, reps;
	private final boolean continuous;
	
	public ScanCommand(double min, double max, double time, int reps, boolean continuous) {
		this.min = min;
		this.max = max;
		this.time = time;
		this.reps = reps;
		this.continuous = continuous;
	}
	
	public double getMin() {return min;}
	public double getMax() {return max;}
	public double getTime() {return time;}
	public double getReps() {return reps;}
	public boolean getContinuous() {return continuous;}
}