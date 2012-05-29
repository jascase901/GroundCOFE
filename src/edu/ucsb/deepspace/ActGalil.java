package edu.ucsb.deepspace;

<<<<<<< HEAD

=======
import edu.ucsb.deepspace.MoveCommand.MoveMode;
import edu.ucsb.deepspace.MoveCommand.MoveType;
/**
 * All methods that convert between encoder pulses and degrees and move Galil.
 * 
 *
 */
>>>>>>> ccd8e4f4271c63dbf1ae70e44821f934eb385664
public class ActGalil implements ActInterface {
	
	private Axis axis;
	private Stage stage;
	private CommGalil protocol;
	private double encPulsePerRev;
	private double encPulsePerDeg;
	private boolean indexing = false;
	private String axisAbbrev = "";
	private double offset = 0;
	private boolean scanning = false;
<<<<<<< HEAD
	private boolean motorState = false;

	public ActGalil(Axis axis, CommGalil protocol) {
=======
	/**
	 * Creates an ActGalil object to control each axis.
	 * @param axis
	 * @param protocol to talk to Galil
	 */
	public ActGalil(axisType axis, CommGalil protocol) {
>>>>>>> ccd8e4f4271c63dbf1ae70e44821f934eb385664
		this.axis = axis;
		this.protocol = protocol;
		if (axis == Axis.AZ) {
			encPulsePerRev = 1000*1024;
		}
		else {
			encPulsePerRev = 4000;
		}
		encPulsePerDeg = ((double) encPulsePerRev) / 360d;
	}
	/**
	 * Creates a stage object with the correct instance variables.
	 */
	public void registerStage(Stage stage) {
		this.stage = stage;
		axisAbbrev = axis.getAbbrev();
	}
	
	public void configure() {
		
	}
	
	public String info() {
		return "info: blank";
	}

	/**
	 * Returns true if the move is valid. <P>
	 * A move is valid if the goal position falls between min and max.
	 */
	public boolean validMove(MoveCommand mc, double min, double max) {
		double goal = goalUserDeg(mc);
		if (min <= goal && goal <= max) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private double goalAbsDeg(MoveCommand mc) {
		return userDegToAbsDeg(goalUserDeg(mc));
	}
	
	/**
	 * Calculates where the motion will end, assuming the current move succeeds.
	 * @param mc
	 * @return
	 */
	//TODO this works, but it's really ugly
	private double goalUserDeg(MoveCommand mc) {
		double goal = 0;
		
//		if (mc.getMode() == MoveMode.RELATIVE) {
//			if (mc.getType() == MoveType.ENCODER) {
//				goal = userPos() + convEncToDeg(mc.getAmount());
//			}
//			else if (mc.getType() == MoveType.DEGREE) {
//				goal = userPos() + mc.getAmount();
//			}
//		}
//		else if (mc.getMode() == MoveMode.ABSOLUTE) {
//			if (mc.getType() == MoveType.ENCODER) {
//				goal = convEncToDeg(mc.getAmount());
//			}
//			else if (mc.getType() == MoveType.DEGREE) {
//				goal = mc.getAmount();
//			}
		//}
		
//		switch (mc.getMode()) {
//			case RELATIVE:
//				switch (mc.getType()) {
//					case ENCODER:
//						goal = userPos() + convEncToDeg(mc.getAmount()); break;
//					case DEGREE:
//						goal = userPos() + mc.getAmount(); break;
//				}
//			case ABSOLUTE:
//				switch (mc.getType()) {
//					case ENCODER:
//						goal = convEncToDeg(mc.getAmount()); break;
//					case DEGREE:
//						goal = mc.getAmount(); break;
//				}
//		}
		
		return absDegToUserDeg(goal);
	}

	/**
	 * 
	 * @return the absolute position in degrees
	 */
	public double absolutePos() {
		double encPos = stage.encPos(axis);
		double absDeg = encToAbsDeg(encPos);
		return absDeg;
		//return encToAbsDeg(stage.encPos(axis));
	}
	
	/**
	 * The position of the actuator in degrees. <P>
	 * Prevents the user from ever seeing a position like 540 degrees.
	 * @return absolutePos() % 360
	 */
	public double userPos() {
		return absDegToUserDeg(absolutePos());
	}
	
	/**
	 * Converts an encoder value into the absolute position. <P>
	 * Inverse of absDegToEnc()
	 * @param enc value of encoder
	 * @return absolute degree position
	 */
	private double encToAbsDeg(double enc) {
		return offset + convEncToDeg(enc);
	}
	
	/**
	 * Converts an absolute degree position into an encoder value. <P>
	 * Inverse of encToAbsDeg
	 * @param deg position in degrees
	 * @return encoder value
	 */
	private double absDegToEnc(double deg) {
		return convDegToEnc(deg - offset);
	}
	
	/**
	 * Converts user degrees into encoder value. <P>
	 * First converts user degrees into absolute degrees.
	 * @param deg
	 * @return
	 */
	@SuppressWarnings("unused")
	private double userDegToEnc(double deg) {
		double absDeg = userDegToAbsDeg(deg);
		return absDegToEnc(absDeg);
	}
	
	/**
	 * Converts user degrees into absolute degrees.
	 * @param deg
	 * @return 
	 */
	private double userDegToAbsDeg(double deg) {
		return absolutePos() - userPos() + deg;
	}
	
	/**
	 * Converts absolute degrees into user degrees.
	 * @param deg
	 * @return
	 */
	private double absDegToUserDeg(double deg) {
		return deg % 360;
	}
	
	/**
	 * Converts a number of encoder pulses into degrees.
	 * @param enc
	 * @return
	 */
	private double convEncToDeg(double enc) {
		return enc / encPulsePerDeg;
	}
	
	/**
	 * Converts degrees into encoder pulses.
	 * @param deg
	 * @return
	 */
	private double convDegToEnc(double deg) {
		return deg * encPulsePerDeg;
	}
	
	/**
	 * Moves the actuator to the specified absolute position.
	 * @param value
	 */
	@SuppressWarnings("unused")
	private void encoderAbsolute(double value) {
		protocol.sendRead("PA" + axisAbbrev + "=" + value);
		protocol.sendRead("BG" + axisAbbrev);
		waitWhileMoving();
	}
	
	/**
	 * Moves the actuator relative to the current position by the specified amount.
	 * @param value
	 */
	@SuppressWarnings("unused")
	private void encoderRelative(double value) {
		protocol.sendRead("PR" + axisAbbrev + "=" + value);
		//System.out.println("send bg");
		protocol.sendRead("BG" + axisAbbrev);
		waitWhileMoving();
	}
	
	/**
	 * Moves relative to the current position. <P>
	 * If type is is encoder, simply moves by amount. <BR>
	 * If type is degree, first converts amount to encoder pulses and then moves.
	 */
	public void moveRelative(MoveCommand mc) {
		stage.setGoalPos(goalUserDeg(mc), axis);
//		switch (mc.getType()) {
//			case ENCODER:
//				encoderRelative(mc.getAmount()); break;
//			case DEGREE:
//				encoderRelative(convDegToEnc(mc.getAmount())); break;
//		}
	}
	
	/**
	 * Moves to the absolute position specified by the mc. <P>
	 */
	public void moveAbsolute(MoveCommand mc) {
		stage.setGoalPos(goalUserDeg(mc), axis);
//		switch (mc.getType()) {
//			case ENCODER:
//				encoderAbsolute(mc.getAmount()); break;
//			case DEGREE:
//				double enc = userDegToEnc(mc.getAmount());
//				encoderAbsolute(enc); break;
//		}
	}
	
	/**
	 * 
	 * @return true if motor is on, false if not
	 */
	public boolean motorState() {
		String response = protocol.sendRead("MG _MO" + axisAbbrev);
		double state = 0;
		try {
			state = Double.parseDouble(response);
		} catch (NumberFormatException e) {
			System.out.println();
			System.out.println(axis);
			System.out.println(response);
			System.out.println("ActGalil.motorState numberformatexception sigh");
		}
		if (state == 1) {
			motorState = false;
			return false;
		}
		else if (state == 0) {
			motorState = true;
			return true;
		}
		System.out.println("error ActGalil.motorState");
		return false;
	}
<<<<<<< HEAD
	
	private void motorOn() {
		protocol.sendRead("SH" + axisAbbrev);
		motorState();
	}
	
	private void motorOff() {
		protocol.sendRead("MO" + axisAbbrev);
		motorState();
	}
	
	/**
	 * Toggles the motor state. <P>
	 * By default, set to false.  When stage is initialized, motorState() is called.<BR>
	 * This updates motorState to the current state.  True for on, false for off. <BR>
	 * 
	 */
	public boolean motorControl() {
		if (motorState) {
			motorOff();
		}
		else {
			motorOn();
		}
		return motorState;
	}
	
	/**
	 * Stops the current motion.
	 */
	public void stop() {
		protocol.sendRead("ST" + axisAbbrev);
	}
	
=======
	/**
	 * Turns an axis motor on.
	 */
	void motorOn() {
		protocol.send("SH" + axisName);
	}
	/**
	 * Turns an axis motor off.
	 */
	void motorOff() {
		protocol.send("MO" + axisName);
	}
	
	/**
	 * Sets the velocity of az or el movement.
	 * @param vel in encoder pulses per second
	 */
>>>>>>> ccd8e4f4271c63dbf1ae70e44821f934eb385664
	public void setVelocity(double vel) {
		String out = "JG" + axisAbbrev + "=" + vel;
		protocol.sendRead(out);
	}
	
	/**
	 * Returns true if this axis is currently indexing.
	 */
	public boolean indexing() {return indexing;}
	public void setIndexing(boolean indexing) {this.indexing = indexing;}
	
<<<<<<< HEAD
=======
	/**
	 * Sets a new coordinate as the relative (0,0).
	 */
>>>>>>> ccd8e4f4271c63dbf1ae70e44821f934eb385664
	public void calibrate(double degVal) {
		offset = degVal - convEncToDeg(stage.encPos(axis));
	}
	/**
	 * Moves everything to its default position.
	 */
	public void index() {
		if (!motorState()) {
			stage.statusArea(axis.getFullName() + " motor is not on.  Please turn on motor before proceeding.\n");
			return;
		}
		indexing = true;
		switch (axis) {
			case AZ:
				indexGalilAz(); break;
			case EL:
				indexGalilEl(); break;
		}
		indexing = false;
		//protocol.read(); //there's an erroneous : that pops up at the end of indexing. this gets rid of it
		//protocol.read();
	}
	/**
	 * Indexes the AZ axis.
	 */
	private void indexGalilAz() {
//		// Save acceleration and jog speed values
//		protocol.sendRead("T1 = _JG" + axisName);
//		protocol.sendRead("T2 = _AC" + axisName);
//
//		// then overwrite them
//		protocol.sendRead("MG \"Homing\", T1");
//		double jg = 150000d;
//		//System.out.println(jg);
//		protocol.sendRead("JG" + axisName + "=" + jg);
//		double ac = 50000d;
//		protocol.sendRead("AC" + axisName + "=" + ac);
//
//		// "FE" - find the opto-edge
//		protocol.sendRead("FE" + axisName);
//		protocol.sendRead("BG" + axisName);
//		//waitWhileMoving();
//		protocol.sendRead("AM" + axisName);
//		protocol.sendRead("MG \"Found Opto-Index\"; TP" + axisName);
//
//		// Turn the jog speed WAAAY down when searching for the index
//		jg = 500d;
//		protocol.sendRead("JG" + axisName + "=" + jg);
//
//		// Do the index search ("FI")
//		protocol.sendRead("FI" + axisName);
//		protocol.sendRead("BG" + axisName);
//		
//		waitWhileMoving();
//
//		// Finally, restore accel and jog speeds from before routine was run
//		protocol.sendRead("JG" + axisName + "=T1");
//		protocol.sendRead("AC" + axisName + "=T2");
		protocol.sendRead("XQ #HOMEA,0");
	}
	/**
	 * Indexes the EL axis.
	 */
	private void indexGalilEl() {
//		// Save acceleration and jog speed values
//		protocol.sendRead("T1 = _JG" + axisName);
//		protocol.sendRead("T2 = _AC" + axisName);
//		
//		double jg = 1000d;
//		protocol.sendRead("JG" + axisName + "=" + jg);
//		
//		// Do the index search ("FI")
//		protocol.sendRead("FI" + axisName);
//		protocol.sendRead("BG" + axisName);
//		protocol.sendRead("AM" + axisName);
//		encoderRelative(3900);
//		jg = 50d;
//		protocol.sendRead("AM" + axisName);
//		protocol.sendRead("JG" + axisName + "=" + jg);
//		protocol.sendRead("FI" + axisName);
//		protocol.sendRead("BG" + axisName);
//		//protocol.sendRead("AM" + axisName);
//		//protocol.sendRead("MG \"Motion Done\";");
//		
//		waitWhileMoving();
//		
//		// Finally, restore accel and jog speeds from before routine was run
//		protocol.sendRead("JG" + axisName + "=T1");
//		protocol.sendRead("AC" + axisName + "=T2");
		protocol.sendRead("XQ #HOMEB,0");
	}
	
	private void waitWhileMoving() {
//		boolean flag = true;
//		CommGalil hatlife = new CommGalil(55555);
//		while (flag) {
//			String temp = protocol.sendRead("MG _BG" + axisName);
//			if (temp.contains("0.0000")) {
//				flag = false;
//			}
//			pause(250);
//		}
//		hatlife.close();
		//System.out.println("done wait while moving");
		
		while (moving()) {
			pause(250);
		}
	}
	
	/**
	 * Returns true if moving.
	 */
	public boolean moving() {
		boolean flag = true;
		String temp = protocol.sendRead("MG _BG" + axisAbbrev);
		if (temp.contains("0.0000")) {
			flag =  false;
		}
		protocol.read();
		return flag;
	}
	/**
	 * Sets the offset of an axis.
	 */
	public void setOffset(double indexOffset) {
		offset = indexOffset;
	}
	/**
	 * @return offset
	 */
	public double getOffset() {return offset;}
	/**
	 * Moves an axis between a min and max multiple times.
	 * @param time to move between min and max
	 * @param repitions to be completed before stopping
	 * @param continuous if true never stops until told to
	 */
	public void scan(ScanCommand sc) {
		if (sc == null) return;
		
		//TODO
		double firstMoveDelta = 0;//Math.abs(sc.getMin() - currentPosDeg());
		firstMoveDelta *= encPulsePerDeg;
		
		double delta = sc.getMax() - sc.getMin();
		delta *= encPulsePerDeg;
		
		double vel = delta / sc.getTime();
		setVelocity(vel);
		
		scanning = true;
		
		//moveAbsolute(sc.getMin());
		pause(1000*(firstMoveDelta/vel + .1));
		//moveAbsolute(sc.getMax());
		pause(1000*(sc.getTime()+ .1));
		
		int i = 1;
		while (scanning) {
			//moveAbsolute(sc.getMin());
			pause(1000*(sc.getTime()+ .1));
			
			//moveAbsolute(sc.getMax());
			pause(1000*(sc.getTime()+ .1));
			
			i++;
			if (i >= sc.getReps() && !sc.getContinuous()) scanning = false;
		}
	}
	/**
	 * Stops the scanning loop.
	 */
	public void stopScanning() {
		this.scanning = false;
	}
	/**
	 * Stops the thread from doing anything.
	 * @param timeInMS
	 */
	private void pause(double timeInMS) {
		try {
			Thread.sleep((long) timeInMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}