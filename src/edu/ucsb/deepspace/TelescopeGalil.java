package edu.ucsb.deepspace;

public class TelescopeGalil implements TelescopeInterface {
	
	private Stage stage;
	private CommGalil protocol;
	
	private GalilAxis az, el;
	
	public TelescopeGalil(Stage stage) {
		this.stage = stage;
		protocol = new CommGalil(55555);
		az = new GalilAxis(Axis.AZ, 1000*1024);
		el = new GalilAxis(Axis.EL, 4000);
	}

	@Override
	public void moveAbsolute(MoveCommand azMc, MoveCommand elMc) {
		int azEncGoal = 0;
		int elEncGoal = 0;
		azEncGoal = (int) az.encGoal(azMc);
		elEncGoal = (int) el.encGoal(elMc);
		String out = "PA " + azEncGoal + "," + elEncGoal;
		System.out.println("azEncGoal: " + azEncGoal);
		System.out.println("elEncGoal: " + elEncGoal);
		System.out.println(out);
		//protocol.sendRead(out);
	}

	@Override
	public void moveRelative(MoveCommand azMc, MoveCommand elMc) {
		int azEncRel = 0;
		int elEncRel = 0;
		azEncRel = (int) az.encGoal(azMc);
		elEncRel = (int) el.encGoal(elMc);
		String out = "PR " + azEncRel + "," + elEncRel;
		System.out.println("azEncRel: " + azEncRel);
		System.out.println("elEncRel: " + elEncRel);
		System.out.println(out);
		//protocol.sendRead(out);
	}

	@Override
	public void moveSingle(double amount, Axis axis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void move(MoveCommand mc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean validMove(MoveCommand mc, double min, double max) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVelocity(double azVel, double elVel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isMoving() {
		boolean azMoving = az.isMoving();
		boolean elMoving = el.isMoving();
		return azMoving || elMoving;
	}

	@Override
	public void stop(Axis axis) {
		GalilAxis temp = picker(axis);
		temp.stop();
	}

	@Override
	public void setOffsets(double azOffset, double elOffset) {
		az.offset = azOffset;
		el.offset = elOffset;
	}

	@Override
	public double getOffset(Axis axis) {
		GalilAxis temp = picker(axis);
		return temp.offset;
	}

	@Override
	public void calibrate(Coordinate c) {
		az.calibrate(c.getAz(), stage.encPos(Axis.AZ));
		el.calibrate(c.getEl(), stage.encPos(Axis.EL));
	}

	@Override
	public double getUserPos(Axis axis) {
		return getAbsolutePos(axis) % 360;
	}

	@Override
	public double getAbsolutePos(Axis axis) {
		GalilAxis temp = picker(axis);
		double currentEncPos = stage.encPos(axis);
		return temp.encToAbsDeg(currentEncPos);
	}

	@Override
	public void scan(ScanCommand azSc, ScanCommand elSc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopScanning() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void motorControl(boolean azOnOff, boolean elOnOff) {
		if (azOnOff) {
			az.motorOn();
		}
		else {
			az.motorOff();
		}
		if (elOnOff) {
			el.motorOn();
		}
		else {
			el.motorOff();
		}
	}

	@Override
	public boolean motorState(Axis axis) {
		GalilAxis temp = picker(axis);
		return temp.motorState;
	}
	
	@Override
	public void queryMotorState() {
		String response = protocol.sendRead("MG _MOA, _MOB");
		String[] temp = response.split(" ");
		az.motorState = (0 == Double.parseDouble(temp[1]));
		el.motorState = (0 == Double.parseDouble(temp[2]));
	}

	@Override
	public void motorToggle(Axis axis) {
		GalilAxis temp = picker(axis);
		if (temp.motorState) {
			temp.motorOff();
		}
		else {
			temp.motorOn();
		}
	}

	@Override
	public boolean isIndexing(Axis axis) {
		GalilAxis temp = picker(axis);
		return temp.indexing;
	}

	@Override
	public void index(Axis axis) {
		switch (axis) {
			case AZ:
				az.indexing = true;
				protocol.sendRead("XQ #HOMEAZ,0");
				waitWhileMoving(axis);
				az.indexing = false;
				break;
			case EL:
				el.indexing = true;
				protocol.sendRead("XQ #HOMEB,1");
				waitWhileMoving(axis);
				az.indexing = false;
				break;
			default:
				System.out.println("TelescopeGalil.index error. reached end of switch statement");
		}
	}
	
	private GalilAxis picker(Axis axis) {
		switch (axis) {
			case AZ:
				return az;
			case EL:
				return el;
		}
		throw new Error("This should never happen.  TelescopeGalil.picker reached the end.");
	}
	
	private void waitWhileMoving(Axis axis) {
		GalilAxis temp = picker(axis);
		sleep(500);
		while (temp.isMoving()) {
			sleep(500);
		}
	}
	
	private void sleep(long timeInMS) {
		try {
			Thread.sleep(timeInMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private class GalilAxis {
		
		private Axis axis;
		private boolean indexing = false;
		private boolean motorState = true;
		private String abbrev;
		private double encPerDeg;
		private double offset;
		
		private GalilAxis(Axis axis, double encPerRev) {
			this.axis = axis;
			abbrev = axis.getAbbrev();
			encPerDeg = encPerRev / 360d;
		}
		
		void motorOn() {
			protocol.sendRead("SH" + abbrev);
			motorState = true;
		}
		
		void motorOff() {
			protocol.sendRead("MO" + abbrev);
			motorState = false;
		}
		
		void stop() {
			protocol.sendRead("ST" + abbrev);
		}
		
		boolean isMoving() {
			boolean flag = true;
			String temp = protocol.sendRead("MG _BG" + abbrev);
			if (temp.contains("0.0000")) {
				flag =  false;
			}
			protocol.read();
			return flag;
		}
		
		/**
		 * Converts a number of encoder pulses into degrees.
		 * @param enc
		 * @return
		 */
		private double convEncToDeg(double enc) {
			return enc / encPerDeg;
		}
		
		/**
		 * Converts degrees into encoder pulses.
		 * @param deg
		 * @return
		 */
		private double convDegToEnc(double deg) {
			return deg * encPerDeg;
		}
		
		/**
		 * Converts an encoder value into the absolute position. <P>
		 * Inverse of absDegToEnc()
		 * @param enc value of encoder
		 * @return absolute degree position
		 */
		double encToAbsDeg(double enc) {
			return offset + convEncToDeg(enc);
		}
		
		/**
		 * Converts an absolute degree position into an encoder value. <P>
		 * Inverse of encToAbsDeg
		 * @param deg position in degrees
		 * @return encoder value
		 */
		double absDegToEnc(double deg) {
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
		 * Converts absolute degrees into user degrees.
		 * @param deg
		 * @return
		 */
		private double absDegToUserDeg(double deg) {
			return deg % 360;
		}
		
		void calibrate(double degVal, double encPos) {
			offset = degVal - convEncToDeg(encPos);
		}
		
		double encGoal(MoveCommand mc) {
			switch (mc.getMode()) {
				case RELATIVE:
					switch (mc.getType()) {
						case ENCODER:
							return mc.getAmount();
						case DEGREE:
							return convDegToEnc(mc.getAmount());
					}
				case ABSOLUTE:
					switch (mc.getType()) {
						case ENCODER:
							return mc.getAmount();
						case DEGREE:
							return absDegToEnc(mc.getAmount());
					}
			}
			System.out.println("TelescopeGaill.GalilAxis.encGoal reached end");
			throw new Error("TelescopeGaill.GalilAxis.encGoal reached end");
		}
		
	}


}