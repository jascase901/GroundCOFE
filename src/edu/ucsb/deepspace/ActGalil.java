package edu.ucsb.deepspace;

public class ActGalil implements ActInterface {
	
	private axisType axis;
	private Stage stage;
	private CommGalil protocol;
	private int encPulsePerRev;
	private double encPulsePerDeg = ((double) encPulsePerRev) / 360d;
	private boolean indexing;
	private String axisName = "";
	private double offset = 0;
	private double azEncPerRev = 1000*1024;

	public ActGalil(axisType axis, CommGalil protocol) {
		this.axis = axis;
		//this.protocol = CommGalil.getInstance();
		this.protocol = protocol;
		if (axis == axisType.AZ) {
			axisName = "A";
			encPulsePerRev = 1000*1024;
		}
		else {
			axisName = "B";
			encPulsePerRev = 4000;
		}
	}
	
	//I haven't found a need for this yet.  (Reed, 2/15/2012)
	//The FTDI actuator does make use of stage, however.
	//Might as well leave it here for now.
	public void registerStage(Stage stage) {
		this.stage = stage;
	}
	
	public void configure() {
		
	}
	
//	public void update(DataGalil.GalilStatus status) {
//		this.position = status.pos;
//		this.velocity = status.vel;
//	}
	
	public String info() {
		return "info: blank";
	}
	
	//TODO
	//Galil is really precise.  Probably don't need any external tolerance checking.
	// Reed, 4/12/2012
	public void moveAbsolute(double goalPosInDeg) {
	    if (axis == axisType.AZ) stage.setGoalAz(goalPosInDeg);
		else stage.setGoalEl(goalPosInDeg);
		
		//long encPulses = encPulseToMove(goalPosInDeg);
	    double goalEnc = degToEncVal(goalPosInDeg);
		moveEncVal(goalEnc);
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
	
	public void moveRelative(double numDeg, String moveType) {
		double goalDeg = goalPos(moveType, numDeg);
		//System.out.println("goalDeg=" + goalDeg);
		//System.out.println(numDeg);
		moveAbsolute(goalDeg);
	}
	
	//public void set
	
	public void moveEncoder(double numEncPulse) {
		double goalDeg = goalPos("encoder", numEncPulse);
		moveAbsolute(goalDeg);
	}
	
	//Lowest level move command
	//This is the method that makes use of the GalilComm instance, "protocol".
	public void moveEncVal(double numEncPulses) {
		String out = "PA" + axisName;
		out += "=" + numEncPulses;
		//System.out.println(out);
		protocol.sendRead(out);
		protocol.sendRead("BG"+axisName);
	}
	
	private double currentPosDeg() {
		return encValToDeg(stage.degPos(axis));
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
	
	public void setVelocity(double vel) {
		String out = "JG" + axisName + "=" + vel;
		protocol.sendRead(out);
	}
	
	public boolean indexing() {return indexing;}
	public void setIndexing(boolean indexing) {this.indexing = indexing;}
	
	//This method will be used to calculate the final position as a result of a move command.
	//Used so that the user knows where it will end up pointing, AND to verify that the move is valid.
	private double goalPos(String moveType, double amount) {
		double goalPos = currentPosDeg();
		//System.out.println("currentDegPos: " + goalPos);
		//System.out.println("moveType: " + moveType);
		//System.out.println("current encoder count: " + degToEncVal(goalPos));
		
		
		//System.out.println("current pos=" + goalPos);
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
		//System.out.println("goalPos: " + goalPos);
		//System.out.println("goalEnc: " + degToEncVal(goalPos));
		//System.out.println();
		return goalPos;
	}
	
	//Check to see if the move is allowed.
	public boolean allowedMove(String moveType, double min, double max, double amount) {
		double goalPos = goalPos(moveType, amount);
		if (goalPos >= min && goalPos <= max) {
			//System.out.println("Allowed Move");
			return true;
		}
		//System.out.println("Not Allowed move:golpos="+goalPos+" min="+min+" max="+max);
		return false;
	}
	
	public void calibrate(double degVal) {
		offset = degVal - currentPosDeg() / encPulsePerDeg;
	}
	
	public void index() {
		// Save acceleration and jog speed values
		protocol.sendRead("T1 = _JG" + axisName);
		protocol.sendRead("T2 = _AC" + axisName);

		// then overwrite them
		protocol.sendRead("MG \"Homing\", T1");
		double jg = 150000d;
		jg = jg * encPulsePerRev / azEncPerRev;
		protocol.sendRead("JG" + axisName + "=" + jg);
		double ac = 50000d;
		ac = ac * encPulsePerRev / azEncPerRev;
		protocol.sendRead("AC" + axisName + "=" + ac);

		// "FE" - find the opto-edge
		protocol.sendRead("FE" + axisName);
		protocol.sendRead("BG" + axisName);
		protocol.sendRead("AM" + axisName);
		protocol.sendRead("MG \"Found Opto-Index\"; TP" + axisName);

		// Turn the jog speed WAAAY down when searching for the index
		jg = 500d;
		jg = jg * encPulsePerRev / azEncPerRev;
		protocol.sendRead("JG" + axisName + "=" + jg);

		// Do the index search ("FI")
		protocol.sendRead("FI" + axisName);
		protocol.sendRead("BG" + axisName);
		protocol.sendRead("AM" + axisName);
		protocol.sendRead("MG \"Motion Done\";TP" + axisName);
		
		// Finally, restore accel and jog speeds from before routine was run
		protocol.sendRead("JG" + axisName + "=T1");
		protocol.sendRead("AC" + axisName + "=T2");
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
