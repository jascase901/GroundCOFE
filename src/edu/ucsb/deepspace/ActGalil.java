package edu.ucsb.deepspace;

import edu.ucsb.deepspace.MoveCommand.MoveMode;
import edu.ucsb.deepspace.MoveCommand.MoveType;

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
	private boolean motorState = false;

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
		
		if (mc.getMode() == MoveMode.RELATIVE) {
			if (mc.getType() == MoveType.ENCODER) {
				goal = userPos() + convEncToDeg(mc.getAmount());
			}
			else if (mc.getType() == MoveType.DEGREE) {
				goal = userPos() + mc.getAmount();
			}
		}
		else if (mc.getMode() == MoveMode.ABSOLUTE) {
			if (mc.getType() == MoveType.ENCODER) {
				goal = convEncToDeg(mc.getAmount());
			}
			else if (mc.getType() == MoveType.DEGREE) {
				goal = mc.getAmount();
			}
		}
		
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
	private void encoderAbsolute(double value) {
		protocol.sendRead("PA" + axisName + "=" + value);
		protocol.sendRead("BG" + axisName);
	}
	
	/**
	 * Moves the actuator relative to the current position by the specified amount.
	 * @param value
	 */
	private void encoderRelative(double value) {
		protocol.sendRead("PR" + axisName + "=" + value);
		protocol.sendRead("BG" + axisName);
	}
	
	/**
	 * Moves relative to the current position. <P>
	 * If type is is encoder, simply moves by amount. <BR>
	 * If type is degree, first converts amount to encoder pulses and then moves.
	 */
	public void moveRelative(MoveCommand mc) {
		stage.setGoalPos(goalUserDeg(mc), axis);
		switch (mc.getType()) {
			case ENCODER:
				encoderRelative(mc.getAmount()); break;
			case DEGREE:
				encoderRelative(convDegToEnc(mc.getAmount())); break;
		}
	}
	
	/**
	 * Moves to the absolute position specified by the mc. <P>
	 */
	public void moveAbsolute(MoveCommand mc) {
		stage.setGoalPos(goalUserDeg(mc), axis);
		switch (mc.getType()) {
			case ENCODER:
				encoderAbsolute(mc.getAmount()); break;
			case DEGREE:
				double enc = userDegToEnc(mc.getAmount());
				encoderAbsolute(enc); break;
		}
	}
	
	/**
	 * 
	 * @return true if motor is on, false if not
	 */
	public boolean motorState() {
		String response = protocol.sendRead("MG _MO" + axisName);
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
	
	private void motorOn() {
		protocol.sendRead("SH" + axisName);
		motorState();
	}
	
	private void motorOff() {
		protocol.sendRead("MO" + axisName);
		motorState();
	}
	
	/**
	 * Toggles the motor state. <P>
	 * By default, set to false.  When stage is initialized, motorState() is called.<BR>
	 * This updates motorState to the current state.  True for on, false for off. <BR>
	 * 
	 */
	public void motorControl() {
		if (motorState) {
			motorOff();
		}
		else {
			motorOn();
		}
	}
	
	/**
	 * Stops the current motion.
	 */
	public void stop() {
		protocol.sendRead("ST" + axisName);
	}
	
	public void setVelocity(double vel) {
		String out = "JG" + axisName + "=" + vel;
		protocol.sendRead(out);
	}
	
	public boolean indexing() {return indexing;}
	public void setIndexing(boolean indexing) {this.indexing = indexing;}
	
	public void calibrate(double degVal) {
		offset = degVal - convEncToDeg(stage.encPos(axis));
	}
	
	public void index() {
		if (!motorState()) {
			stage.statusArea(stage.axisNameLong(axis) + " motor is not on.  Please turn on motor before proceeding.\n");
			return;
		}
		switch (axis) {
			case AZ:
				indexGalilAz(); break;
			case EL:
				indexGalilEl(); break;
		}
		protocol.read(); //there's an erroneous : that pops up at the end of indexing. this gets rid of it
	}
	
	private void indexGalilAz() {
		boolean flag = true;
		// Save acceleration and jog speed values
		protocol.sendRead("T1 = _JG" + axisName);
		protocol.sendRead("T2 = _AC" + axisName);

		// then overwrite them
		protocol.sendRead("MG \"Homing\", T1");
		double jg = 150000d;
		//System.out.println(jg);
		protocol.sendRead("JG" + axisName + "=" + jg);
		double ac = 50000d;
		protocol.sendRead("AC" + axisName + "=" + ac);

		// "FE" - find the opto-edge
		protocol.sendRead("FE" + axisName);
		protocol.sendRead("BG" + axisName);
		protocol.sendRead("AM" + axisName);
		protocol.sendRead("MG \"Found Opto-Index\"; TP" + axisName);

		// Turn the jog speed WAAAY down when searching for the index
		jg = 500d;
		protocol.sendRead("JG" + axisName + "=" + jg);

		// Do the index search ("FI")
		protocol.sendRead("FI" + axisName);
		protocol.sendRead("BG" + axisName);
		protocol.sendRead("AM" + axisName);
		protocol.sendRead("MG \"Motion Done\";TP" + axisName);
		while (flag) {
			String temp = protocol.read();
			if (temp.contains("one")) {
				flag = false;
			}
			pause(100);
		}

		// Finally, restore accel and jog speeds from before routine was run
		protocol.sendRead("JG" + axisName + "=T1");
		protocol.sendRead("AC" + axisName + "=T2");
	}
	
	private void indexGalilEl() {
		boolean flag = true;
		// Save acceleration and jog speed values
		protocol.sendRead("T1 = _JG" + axisName);
		protocol.sendRead("T2 = _AC" + axisName);
		
		double jg = 1000d;
		protocol.sendRead("JG" + axisName + "=" + jg);
		
		// Do the index search ("FI")
		protocol.sendRead("FI" + axisName);
		protocol.sendRead("BG" + axisName);
		protocol.sendRead("AM" + axisName);
		encoderRelative(3900);
		jg = 50d;
		protocol.sendRead("AM" + axisName);
		protocol.sendRead("JG" + axisName + "=" + jg);
		protocol.sendRead("FI" + axisName);
		protocol.sendRead("BG" + axisName);
		protocol.sendRead("AM" + axisName);
		protocol.sendRead("MG \"Motion Done\";");
		while (flag) {
			String temp = protocol.read();
			if (temp.contains("one")) {
				flag = false;
			}
			pause(100);
		}
		
		
		// Finally, restore accel and jog speeds from before routine was run
		protocol.sendRead("JG" + axisName + "=T1");
		protocol.sendRead("AC" + axisName + "=T2");
	}
	
	public void setOffset(double indexOffset) {
		offset = indexOffset;
	}
	
	public double getOffset() {return offset;}

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