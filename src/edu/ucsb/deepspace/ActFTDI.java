package edu.ucsb.deepspace;

public class ActFTDI implements ActInterface {

	@Override
	public void registerStage(Stage stage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configure() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String info() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveAbsolute(double goalPosInDeg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveRelative(double numDeg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveEncoder(double numEncPulse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveEncVal(double numSteps) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double currentDegPos() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean allowedMove(String moveType, double min, double max,
			double amount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void calibrate(double degVal) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void index() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEncInd(double encInd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getEncInd() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setOffset(double indexOffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean indexing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIndexing(boolean indexing) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isMoving() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double encValToDeg(double encVal) {
		// TODO Auto-generated method stub
		return 0;
	}

}
