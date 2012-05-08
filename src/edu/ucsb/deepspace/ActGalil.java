package edu.ucsb.deepspace;

public class ActGalil implements ActInterface {
	
	private axisType axis;
	private Stage stage;
	private CommGalil protocol;
	private double encPulsePerRev;
	private double encPulsePerDeg;
	private boolean indexing;
	private String axisName = "";
	private double offset = 0;
	private double azEncPerRev=1000*1024;
	private boolean scanning = false;

	public ActGalil(axisType axis, CommGalil protocol) {
		this.axis = axis;
		this.protocol = protocol;
		if (axis == axisType.AZ) {
			encPulsePerRev = 1000*1024;
		}
		else {
			encPulsePerRev = 4000;
		}
		encPulsePerDeg = ((double) encPulsePerRev) / 360d;
	}
	
	public void registerStage(Stage stage) {
		this.stage = stage;
		axisName = stage.axisName(axis);
	}
	
	public void configure() {
		
	}
	
	public String info() {
		return "info: blank";
	}
	
	public void moveAbsolute(double goalPosInDeg) {
	    if (axis == axisType.AZ) stage.setGoalAz(goalPosInDeg);
		else stage.setGoalEl(goalPosInDeg);
	    double goalEnc = degToEncVal(goalPosInDeg);
		moveEncVal(goalEnc);
	}
	
	public void moveRelative(double numDeg, String moveType) {
		double goalDeg = goalPos(moveType, numDeg);
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
		out += "=" + numEncPulses;
		//System.out.println(out);
		protocol.sendRead(out);
		protocol.sendRead("BG"+axisName);
	}
	
	private double currentPosDeg() {
		return encValToDeg(stage.encPos(axis));
	}
	
	public double encValToDeg(double encVal) {
		return offset + encVal / encPulsePerDeg;
	}
	
	//Inverse of encValToDeg
	private double  degToEncVal(double degVal) {
		return (degVal - offset)*encPulsePerDeg;
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
			return true;
		}
		return false;
	}
	
	public void calibrate(double degVal) {
		offset = degVal - stage.encPos(axis) / encPulsePerDeg;
	}
	
	public void index() {
		switch (axis) {
			case AZ:
				indexGalilAz(); break;
			case EL:
				indexGalilEl(); break;
		}
	}
	
	private void indexGalilAz() {
		// Save acceleration and jog speed values
		protocol.sendRead("T1 = _JG" + axisName);
		protocol.sendRead("T2 = _AC" + axisName);

		// then overwrite them
		protocol.sendRead("MG \"Homing\", T1");
		double jg = 150000d;
		jg = jg * encPulsePerRev / azEncPerRev;
		System.out.println(jg);
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
	
	private void indexGalilEl() {
		// Save acceleration and jog speed values
		//protocol.sendRead("T1 = _JG" + axisName);
		//protocol.sendRead("T2 = _AC" + axisName);
		
		double jg = 500d;
		protocol.sendRead("JG" + axisName + "=" + jg);
		
		// Do the index search ("FI")
		protocol.sendRead("FI" + axisName);
		protocol.sendRead("BG" + axisName);
		
		// Finally, restore accel and jog speeds from before routine was run
		//protocol.sendRead("JG" + axisName + "=T1");
		//protocol.sendRead("AC" + axisName + "=T2");
	}
	
	public void setOffset(double indexOffset) {
		offset = indexOffset;
	}
	
	public double getOffset() {return offset;}

	public void scan(ScanCommand sc) {
		if (sc == null) return;
		
		double firstMoveDelta = Math.abs(sc.getMin() - currentPosDeg());
		firstMoveDelta *= encPulsePerDeg;
		
		double delta = sc.getMax() - sc.getMin();
		delta *= encPulsePerDeg;
		
		double vel = delta / sc.getTime();
		setVelocity(vel);
		
		scanning = true;
		
		moveAbsolute(sc.getMin());
		pause(1000*(firstMoveDelta/vel + .1));
		moveAbsolute(sc.getMax());
		pause(1000*(sc.getTime()+ .1));
		
		int i = 1;
		while (scanning) {
			moveAbsolute(sc.getMin());
			pause(1000*(sc.getTime()+ .1));
			
			moveAbsolute(sc.getMax());
			pause(1000*(sc.getTime()+ .1));
			
			i++;
			if (i >= sc.getReps() && !sc.getContinuous()) scanning = false;
		}
	}
	
	public void stopScanning() {
		this.scanning = false;
	}
	
	private void pause(double timeInMS) {
		try {
			Thread.sleep((long) timeInMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}