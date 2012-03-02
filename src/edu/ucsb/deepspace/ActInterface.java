package edu.ucsb.deepspace;

public interface ActInterface {
	
	public static enum axisType {
		AZ, EL;
	}
	
	//Associate the stage instance with the axes so that they can send information to the stage.
	//If there's a better way to implement this bi-directional communication between objects,
	//please let me know.
	public void registerStage(Stage stage);
	
	public void configure();
	
	public String info();
	
	public void moveAbsolute(double goalPosInDeg);
	
	public void moveRelative(double numDeg);
	
	public void moveEncoder(double numEncPulse);
	
	public void moveEncVal(double numSteps);
	
	public double currentDegPos();
	
	public boolean allowedMove(String moveType, double min, double max, double amount);
	
	public void calibrate(double degVal);
	
	public void index();
	
	public void setEncInd(double encInd);
	public double getEncInd();
	
	public void setOffset(double indexOffset);
	public double getOffset();
	
	public boolean indexing();
	
	public void setIndexing(boolean indexing);
	
	public boolean isMoving();

}