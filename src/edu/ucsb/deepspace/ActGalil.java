package edu.ucsb.deepspace;

public class ActGalil implements ActInterface {
	
	private axisType axis;
	@SuppressWarnings("unused")
	private Stage stage;
	private CommGalil protocol;
	private int encPulsePerRev = 1000*1024;
	private double encPulsePerDeg = ((double) encPulsePerRev) / 360d;
	private boolean indexing;
	private int encTol = 2;
	private String axisName = "";
	private double position = 0, velocity;
	private DataGalil.GalilStatus status;
	private double offset = 0;

	public ActGalil(axisType axis, CommGalil protocol) {
		this.axis = axis;
		//this.protocol = CommGalil.getInstance();
		this.protocol = protocol;
		if (axis == axisType.AZ) axisName = "A";
		else axisName = "B";
	}
	
	//I haven't found a need for this yet.  (Reed, 2/15/2012)
	//The FTDI actuator does make use of stage, however.
	//Might as well leave it here for now.
	public void registerStage(Stage stage) {
		this.stage = stage;
	}
	
	public void configure() {
		
	}
	
	void update(DataGalil.GalilStatus status) {
		this.position = status.pos;
		this.velocity = status.vel;
	}
	
	public String info() {
		return "info: blank";
	}
	
	public void moveAbsolute(double goalPosInDeg) {
	    if (axis == axisType.AZ) stage.setGoalAz(goalPosInDeg);
		else stage.setGoalEl(goalPosInDeg);
		
		long encPulses = encPulseToMove(goalPosInDeg);
		moveEncVal(encPulses);
//		pause(2000);
//		waitWhileMoving();
//		pause(1000);
		
//		double deltaEnc = degToEncVal(goalPosInDeg) - getPos();
//		System.out.println(deltaEnc);
//		if (Math.abs(deltaEnc) <= encTol) {
//			System.out.println("finished moving");
//		}
//		else {
//			System.out.println("recursion");
//			moveAbsolute(goalPosInDeg);
//		}
	}
	
	public void moveRelative(double numDeg) {
		double goalDeg = goalPos("degrees", numDeg);
		System.out.println("goalDeg=" + goalDeg);
		moveAbsolute(goalDeg);
	}
	
	public void moveEncoder(double numEncPulse) {
		double goalDeg = goalPos("encoder", numEncPulse);
		moveAbsolute(goalDeg);
	}
	
	//Lowest level move command
	//This is the method that makes use of the GalilComm instance, "protocol".
	public void moveEncVal(double numEncPulses) {
		String out = "PA" + axisName;
		out += "=" + (long) numEncPulses;
		System.out.println(out);
		protocol.sendRead(out);
		protocol.sendRead("BG");
	}
	
	//method that returns current position in degrees
	public double currentDegPos() {
		return encValToDeg(getPos());
	}
	
	//Not sure if this is even relevant to the Galil, but this should convert a desired encoder
	public double encValToDeg(double encVal) {
		return offset + encVal / encPulsePerDeg;
	}
	
	//Inverse of encValToDeg
	private double  degToEncVal(double degVal) {
		return (degVal - offset)*encPulsePerDeg;
	}
	
	private long encPulseToMove(double goalPosInDeg) {
		return (long) ((goalPosInDeg - offset)*encPulsePerDeg);
	}
	
	public boolean indexing() {return indexing;}
	public void setIndexing(boolean indexing) {this.indexing = indexing;}
	
	//This method will be used to calculate the final position as a result of a move command.
	//Used so that the user knows where it will end up pointing, AND to verify that the move is valid.
	private double goalPos(String moveType, double amount) {
		double goalPos = currentDegPos();
		System.out.println("current pos=" + goalPos);
		switch (moveType) {
			case "steps": //Galil: steps = enc pulses
				goalPos += amount / encPulsePerDeg; break;
			case "degrees": //If degrees, add the move amount to current position.
				goalPos += amount; break;
			case "encoder": //Galil: encoder pulses = steps
				goalPos += amount / encPulsePerDeg; break;
			case "absolute": //If absolute, final position is the absolute position; do nothing.
				goalPos = amount; break;
			default :
				System.out.println("Error in Galil.goalPos"); break;
		}
		return goalPos;
	}
	
	//Check to see if the move is allowed.
	public boolean allowedMove(String moveType, double min, double max, double amount) {
		double goalPos = goalPos(moveType, amount);
		if (goalPos >= min && goalPos <= max) {
			System.out.println("Allowed Move");
			return true;
		}
		System.out.println("Not Allowed move:golpos="+goalPos+" min="+min+" max="+max);
		return false;
	}
	
	//Waits while moving.
	private void waitWhileMoving() {
		while (!isMoving()) {
			pause(100);
		}
	}
	
	public boolean isMoving() {
		String vel = protocol.sendRead("TV" + axisName);
		double dVel = Double.parseDouble(vel);
		return (dVel == 0);
	}
	
	private double getPos() {
		if(protocol.queueSize() != 0) {
			protocol.read();
		}
		String pos = protocol.sendRead("TP" + axisName);
		return Double.parseDouble(pos);
	}
	
	public void calibrate(double degVal) {
		offset = degVal - getPos() / encPulsePerDeg;
	}
	
	public void index() {
//		System.out.println(stage == null);
//		stage.toggleReader();
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// Save acceleration and jog speed values
		protocol.sendRead("T1 = _JG" + axisName);
		protocol.sendRead("T2 = _AC" + axisName);

		// then overwrite them
		protocol.sendRead("MG \"Homing\", T1");
		protocol.sendRead("JG" + axisName + "=150000");
		protocol.sendRead("AC" + axisName + "=50000");

		// "FE" - find the opto-edge
		protocol.sendRead("FE" + axisName);
		protocol.sendRead("BG" + axisName);
		protocol.sendRead("AM" + axisName);
		protocol.sendRead("MG \"Found Opto-Index\"; TP" + axisName);

		// Turn the jog speed WAAAY down when searching for the index
		protocol.sendRead("JG" + axisName + "=500");

		// Do the index search ("FI")
		protocol.sendRead("FI" + axisName);
		protocol.sendRead("BG" + axisName);
		protocol.sendRead("AM" + axisName);
		protocol.sendRead("MG \"Motion Done\";TP" + axisName);
		
		// Finally, restore accel and jog speeds from before routine was run
		protocol.sendRead("JG" + axisName + "=T1");
		protocol.sendRead("AC" + axisName + "=T2");
		
		
		
		//protocol.test();
//		System.out.println("shouldn't send this");
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("shouldn't send this");
//		stage.toggleReader();
	}
	
	public void setEncInd(double encInd) {
		
	}
	public double getEncInd() {return 0;}
	
	public void setOffset(double indexOffset) {
		offset = indexOffset;
	}
	public double getOffset() {return offset;}
	
//	public void setStatus(ActStatus status) {
//		
//	}
	
	private void pause(long waitTimeInMS) {
		try {
			Thread.sleep(waitTimeInMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
