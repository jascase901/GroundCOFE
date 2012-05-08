package edu.ucsb.deepspace;


public abstract class ActFTDI implements ActInterface {

	public void registerStage(Stage stage) {
		
	}

	public void configure() {
		
	}

	public String info() {
		return null;
	}

	public void moveAbsolute(double goalPosInDeg) {
		
	}

	public void moveRelative(double numDeg) {
		
	}

	public void moveEncoder(double numEncPulse) {
		
	}

	public void moveEncVal(double numSteps) {
		
	}

//	@Override
//	public double currentDegPos() {
//		return 0;
//	}

	public boolean allowedMove(String moveType, double min, double max, double amount) {
		return false;
	}

	public void calibrate(double degVal) {
		
	}

	public void index() {
		
	}

	public void setEncInd(double encInd) {
		
	}

	public double getEncInd() {
		return 0;
	}

	public void setOffset(double indexOffset) {
		
	}

	public double getOffset() {
		return 0;
	}

	public boolean indexing() {
		return false;
	}

	public void setIndexing(boolean indexing) {
		
	}

//	@Override
//	public boolean isMoving() {
//		return false;
//	}

	public double encValToDeg(double encVal) {
		return 0;
	}

//	@Override
//	public void update(GalilStatus status) {
//		
//	}

	public void moveRelative(double numDeg, String moveType) {
		
	}

}
