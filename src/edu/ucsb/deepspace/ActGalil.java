package edu.ucsb.deepspace;

import edu.ucsb.deepspace.MoveCommand.MoveMode;
import edu.ucsb.deepspace.MoveCommand.MoveType;
/**
 * All methods that convert between encoder pulses and degrees and move Galil.
 * 
 *
 */
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
	/**
	 * Creates an ActGalil object to control each axis.
	 * @param axis
	 * @param protocol to talk to Galil
	 */
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
	/**
	 * Creates a stage object with the correct instance variables.
	 */
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
//				double userPos = userPos();
//				double amount = mc.getAmount();
//				//goal = userPos() + mc.getAmount(); break;
//				goal = userPos + amount; break;
//			case ABSOLUTE:
//				goal = mc.getAmount(); break;
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
//		double absPos = absolutePos();
//		double userPos = absPos % 360;
//		return userPos;
		return absDegToUserDeg(absolutePos());
		//return absolutePos() % 360;
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
		protocol.send("PA" + axisName + "=" + value);
		protocol.sendRead("BG" + axisName);
	}
	
	/**
	 * Moves the actuator relative to the current position by the specified amount.
	 * @param value
	 */
	private void encoderRelative(double value) {
		protocol.send("PR" + axisName + "=" + value);
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
	public void setVelocity(double vel) {
		String out = "JG" + axisName + "=" + vel;
		protocol.sendRead(out);
	}
	
	public boolean indexing() {return indexing;}
	public void setIndexing(boolean indexing) {this.indexing = indexing;}
	
	/**
	 * Sets a new coordinate as the relative (0,0).
	 */
	public void calibrate(double degVal) {
		offset = degVal - convEncToDeg(stage.encPos(axis));
	}
	/**
	 * Moves everything to its default position.
	 */
	public void index() {
		switch (axis) {
			case AZ:
				indexGalilAz(); break;
			case EL:
				indexGalilEl(); break;
		}
	}
	/**
	 * Indexes the AZ axis.
	 */
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
	/**
	 * Indexes the EL axis.
	 */
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