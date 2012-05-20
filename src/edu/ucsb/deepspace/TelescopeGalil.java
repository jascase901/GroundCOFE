package edu.ucsb.deepspace;

public class TelescopeGalil implements TelescopeInterface {
	
	@SuppressWarnings("unused")
	private Stage stage;
	private CommGalil protocol;
	
	private GalilAxis az, el;
	
	public TelescopeGalil(Stage stage) {
		this.stage = stage;
		protocol = new CommGalil(55555);
		az = new GalilAxis(Axis.AZ);
		el = new GalilAxis(Axis.EL);
	}

	@Override
	public void moveAbsolute(MoveCommand az, MoveCommand el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveRelative(MoveCommand az, MoveCommand el) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOffsets(double azOffset, double elOffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getOffset(Axis axis) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void calibrate(Coordinate c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getUserPos(Axis axis) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAbsolutePos(Axis axis) {
		// TODO Auto-generated method stub
		return 0;
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
	public boolean motorState(Axis axis, boolean query) {
		GalilAxis temp = picker(axis);
		if (query) {
			return temp.queryMotorState();
		}
		else {
			return temp.motorState;
		}
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
		}
		throw new Error("This should never happen.  TelescopeGalil.index reached the end.");
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
		private boolean motorState = false;
		private String abbrev;
		
		private GalilAxis(Axis axis) {
			this.axis = axis;
			abbrev = axis.getAbbrev();
		}
		
		void motorOn() {
			protocol.sendRead("SH" + abbrev);
			motorState = true;
		}
		
		void motorOff() {
			protocol.sendRead("MO" + abbrev);
			motorState = false;
		}
		
		boolean queryMotorState() {
			String response = protocol.sendRead("MG _MO" + abbrev);
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
			System.out.println("error TelescopeGalil.GalilAxis.queryMotorState");
			return false;
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
	}


}