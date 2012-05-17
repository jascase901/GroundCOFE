package edu.ucsb.deepspace;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ucsb.deepspace.ActInterface.axisType;
import edu.ucsb.deepspace.MoveCommand.MoveMode;
import edu.ucsb.deepspace.MoveCommand.MoveType;
import edu.ucsb.deepspace.gui.MainWindow;

public class Stage {

	private static final Stage INSTANCE = new Stage();
	public static Stage getInstance() {return INSTANCE;}

	public static enum stageType {
		Galil, FTDI;
	}
	private stageType type = stageType.Galil;
	public stageType getType() {
		return this.type;
	}

	private double minAz, maxAz, minEl, maxEl;
	//TODO private double maxMoveRel = 360;
	private int encTol = 10;

	private Timer raDecTracker, lstUpdater;
	private final ExecutorService exec = Executors.newFixedThreadPool(2);
	private ActInterface az, el;
	private MainWindow window;
	private boolean commStatus = false;
	private ReaderInterface reader;
	private DataInterface position;
	private final Properties actSettings = new Properties();
	private final Properties settings = new Properties();
	private LatLongAlt baseLocation, balloonLocation;
	private double azToBalloon = 0, elToBalloon = 0;
	private CommGalil protocol, protocolTest;
	/**
	 * Creates a new stage object and loads all the preset settings into it.
	 */
	public Stage() {
		try {
			loadSettings();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Creates all the CommGalil, ActGalil, etc. objects and starts the connection. 
	 * @param window
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void initialize(MainWindow window) throws FileNotFoundException, IOException {
		this.window = window;
		switch (type) {
		case Galil:
			protocol = new CommGalil(1337);
			protocolTest = new CommGalil(1338);
			az = new ActGalil(axisType.AZ, protocol);
			el = new ActGalil(axisType.EL, protocolTest);
			reader = new ReaderGalil(this);
			loadGalil();
			az.registerStage(this);
			el.registerStage(this);
			break;
		case FTDI:
			//These are commented out because I made the ActFTDI class abstract.
			//I got tired of adding new functionality to the ActInterface and having to "implement" the method
			//in ActFTDI.  Once abstract, I could no longer instantiate them.  Hence they are now commented out.
			//Reed, 5/5/2012
			
			//az = new ActFTDI();
			//el = new ActFTDI();
			reader = new ReaderFTDI(this);
			loadFTDI();
			break;
		}
		updateLst();
		if (commStatus) {
			System.out.println("reader started");
			reader.start();
		}
	}
	/**
	 * Returns the axis name: A for azimuth and B for elevation.
	 * @param axis
	 * @return A or B
	 */
	public String axisName(axisType axis) {
		switch (axis) {
		case AZ:
			return "A";
		case EL:
			return "B";
		default:
			System.out.println("this should never happen.  Stage.axisName()");
		}
		return "error Stage.axisName()";
	}
	/**
	 * Loads the settings.ini file and saves all its variables.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadSettings() throws FileNotFoundException, IOException {
		//read settings from file
		settings.load(new FileInputStream("Settings.ini"));

		//populate fields with information from file
		azToBalloon = Double.parseDouble(settings.getProperty("azToBalloon", "0"));
		elToBalloon = Double.parseDouble(settings.getProperty("elToBalloon", "0"));

		double baseLatitude = Double.parseDouble(settings.getProperty("baseLatitude", "0"));
		double baseLongitude = Double.parseDouble(settings.getProperty("baseLongitude", "0"));
		double baseAltitude = Double.parseDouble(settings.getProperty("baseAltitude", "0"));
		baseLocation =  new LatLongAlt(baseLatitude, baseLongitude, baseAltitude);

		double balloonLatitude = Double.parseDouble(settings.getProperty("balloonLatitude", "0"));
		double balloonLongitude = Double.parseDouble(settings.getProperty("balloonLongitude", "0"));
		double balloonAltitude = Double.parseDouble(settings.getProperty("balloonAltitude", "0"));
		balloonLocation =  new LatLongAlt(balloonLatitude, balloonLongitude, balloonAltitude);
	}
	/**
	 * Saves new settings to settings.ini.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void saveSettings() throws FileNotFoundException, IOException {
		//prep settings to be stored
		settings.setProperty("baseLatitude", String.valueOf(baseLocation.getLatitude()));
		settings.setProperty("baseLongitude", String.valueOf(baseLocation.getLongitude()));
		settings.setProperty("baseAltitude", String.valueOf(baseLocation.getAltitude()));

		settings.setProperty("balloonLatitude", String.valueOf(balloonLocation.getLatitude()));
		settings.setProperty("balloonLongitude", String.valueOf(balloonLocation.getLongitude()));
		settings.setProperty("balloonAltitude", String.valueOf(balloonLocation.getAltitude()));

		settings.setProperty("azToBalloon", String.valueOf(azToBalloon));
		settings.setProperty("elToBalloon", String.valueOf(elToBalloon));

		//store settings
		settings.store(new FileOutputStream("Settings.ini"), "");
	}
	/**
	 * Loads the galil.ini file and reads all its variables.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadGalil() throws FileNotFoundException, IOException {
		actSettings.load(new FileInputStream("Galil.ini"));
		double azOffset = Double.parseDouble(actSettings.getProperty("azOffset"));
		double elOffset = Double.parseDouble(actSettings.getProperty("elOffset"));
		minAz = Double.parseDouble(actSettings.getProperty("minAz"));
		maxAz = Double.parseDouble(actSettings.getProperty("maxAz"));
		minEl = Double.parseDouble(actSettings.getProperty("minEl"));
		maxEl = Double.parseDouble(actSettings.getProperty("maxEl"));
		encTol = Integer.parseInt(actSettings.getProperty("encTol"));
		az.setOffset(azOffset);
		el.setOffset(elOffset);
		window.setMinMaxAzEl(minAz, maxAz, minEl, maxEl);
	}
	/**
	 * Saves the settings of Galil back into galil.ini.
	 */
	private void closeGalil() {
		actSettings.setProperty("azOffset", String.valueOf(az.getOffset()));
		actSettings.setProperty("elOffset", String.valueOf(el.getOffset()));
		actSettings.setProperty("minAz", String.valueOf(minAz));
		actSettings.setProperty("maxAz", String.valueOf(maxAz));
		actSettings.setProperty("minEl", String.valueOf(minEl));
		actSettings.setProperty("maxEl", String.valueOf(maxEl));
		actSettings.setProperty("encTol", String.valueOf(encTol));
		try {
			actSettings.store(new FileOutputStream("Galil.ini"), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadFTDI() throws FileNotFoundException, IOException {
		actSettings.load(new FileInputStream("FTDI.ini"));
	}

	public void confirmCommConnection() {
		commStatus = true;
	}

	//TODO When this method is called from MainWindow constructor, az and el have not
	//been instantiated.  They are instantiated in Stage.initialize, which is called after MainWindow
	// constructor.  Obviously this is a problem.  (Reed, 4/12/2012)
	public String stageInfo() {
		String out = "";
		//String out = az.info() + "\n" + el.info();
		//System.out.println(out);
		return out;
	}
	/**
	 * Points to a ra dec position every amount of time specified until told to stop.
	 * @param ra
	 * @param dec
	 */
	public void startRaDecTracking(final double ra, final double dec) {
		long period = 10000;
		raDecTracker = new Timer("RA/Dec Tracker", true);
		raDecTracker.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				double az = baseLocation.radecToAz(ra, dec);
				double el = baseLocation.radecToEl(ra, dec);
				System.out.println("az:  " + az);
				System.out.println("el:  " + el);
				System.out.println();
				//for FTDI only: velocity of 0 means no motion has occurred
				//vel=1 currently at rest, but was moving forward
				//vel=-1 currently at rest, but was moving backwards
//				if (Math.abs(velocity) == 1 || velocity == 0) {
//					moveAbsolute(az, el);
//				}
				moveAbsolute(az, el);
			}
		}, 0, period);
	}
	/**
	 * Stops ra dec tracking.
	 */
	public void stopRaDecTracking() {
		raDecTracker.cancel();
	}
	/**
	 * Updates information on the Galil position, times, and ra dec coordinates every second.
	 */
	private void updateLst() {
		lstUpdater = new Timer("LST Updater", true);
		lstUpdater.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {		
				Calendar local = Calendar.getInstance();

				double azPos = 0, elPos = 0;
				if (position != null ) {
					azPos = az.userPos();
					elPos = el.userPos();
				}
				
				double ra = baseLocation.azelToRa(azPos, elPos);
				double dec = baseLocation.azelToDec(azPos, elPos);
				double lst = baseLocation.lst();
				String gmt = baseLocation.gmt();

				String sLst = Formatters.formatLst(lst);
				
				String out = "Az:  " + Formatters.DEGREE_POS.format(azPos);

				out += "\nEl:  " + Formatters.DEGREE_POS.format(elPos);
				out += "\nRA:  " + Formatters.FOUR_POINTS.format(ra);
				out += "\nDec:  " + Formatters.THREE_POINTS.format(dec);
				out += "\nLST:  " + sLst;
				out += "\nUTC:  " + gmt;
				out += "\nLocal:  " + Formatters.HOUR_MIN_SEC.format(local.getTime());
				
				window.updateTxtAzElRaDec(out);
			}
		}, 0, 1000);
	}
	/**
	 * Moves over a specified min and max az and el a specified or continuous number of times. 
	 * 
	 * @param azSc
	 * @param elSc
	 */
	public void startScanning(final ScanCommand azSc, final ScanCommand elSc) {
		if (azSc == null && elSc == null) {
			window.updateStatusArea("Fatal error.  The ScanCommands associated with Az and El are both null.\n");
			return;
		}
		//I changed the size of the thread pool executor to 2.  This may have unintended consequences
		//for other pieces of the code.  However, this is the only way I could get both to scan at
		//the same time AND have the scanning done from inside the ActGalil class.
		exec.submit(new Runnable() {
			@Override
			public void run() {
				az.scan(azSc);
			}
		});
		exec.submit(new Runnable() {
			@Override
			public void run() {
				el.scan(elSc);
			}
		});
		//TODO is this correct?
		if (elSc == null) {
			window.setScanEnabled(axisType.AZ);
		}
		else if (azSc == null) {
			window.setScanEnabled(axisType.EL);
		}
		window.setScanEnabled(axisType.BOTH);
	}
	/**
	 * Stops movement caused by scanning.
	 * 
	 */
	public void stopScanning() {
		az.stopScanning();
		el.stopScanning();
	}
	/**
	 * Moves the axes to the minimum of the ScanCommands is two separate threads.
	 * 
	 * @param azSc
	 * @param elSc
	 */
	public void raster(ScanCommand azSc, ScanCommand elSc) {
		moveAbsolute(azSc.getMin(), elSc.getMin());
		double deltaAz = azSc.getMax() - azSc.getMin();
		double deltaEl = elSc.getMax() - elSc.getMin();
		double reps = azSc.getReps();
		
		
//		moveAbsolute(minAz, minEl)
//		int i = 1
//		int mask = 0
//		int parity = 1
//		while (i<2*reps) {
//		  moveRelative(parity*deltaAz, mask*deltaEl/reps)
//		  parity = -1*parity
//		  mask++
//		  mask%2
		
	}
	/**
	 * Determines if a move is valid and if so executes the correct move command based on mc.
	 * @param mc the move command to be executed
	 */
	public void move(MoveCommand mc) {
		ActInterface act = null;
		double min = 0, max = 0;
		
		switch (mc.getAxis()) {
			case AZ:
				act = az; min = minAz; max = maxAz; break;
			case EL:
				act = el; min = minEl; max = maxEl; break;
			default:
				System.out.println("error Stage.move");
		}
		
		if (!act.validMove(mc, min, max)) {
			System.out.println("this is an invalid move");
			window.controlMoveButtons(true);
			return;
		}
		
		switch (mc.getMode()) {
			case RELATIVE:
				act.moveRelative(mc); break;
			case ABSOLUTE:
				act.moveAbsolute(mc); break;
			default:
				System.out.println("error Stage.move");
		}
		window.controlMoveButtons(true);
	}
	
	/**
	 * Convenience method that moves the az and el axis to an absolute position in degrees in two separate 
	 * threads.
	 * @param azDeg
	 * @param elDeg
	 */
	private void moveAbsolute(double azDeg, double elDeg) {
		final MoveCommand mcAz = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, axisType.AZ, azDeg);
		final MoveCommand mcEl = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, axisType.EL, elDeg);
		exec.submit(new Runnable() {
			public void run() {
				move(mcAz);
			}
		});
		exec.submit(new Runnable() {
			public void run() {
				move(mcEl);
			}
		});
	}
	/**
	 * Moves an axis to its default position.
	 * @param type of axis
	 */
	public void index(final axisType type) {
		exec.submit(new Runnable() {
			@Override
			public void run() {
				switch (type) {
				case AZ:
					az.index();
					buttonEnabler("indexAz");
					break;
				case EL:
					el.index();
					buttonEnabler("indexEl");
					break;
				}
			}
		});
	}

	/**
	 * Returns true if something is moving, false if not.
	 * @return boolean
	 */
	public boolean isMoving() {
		switch (type) {
		case FTDI:
			return true; //don't care about FTDI right now (5/5/2012, reed)
			//return Math.abs(velocity) <= 1;
		case Galil:
			return position.moving();
		default:
			return true;
		}
	}

	/**
	 * While isMoving() is true, sleeps for 100ms.
	 */
	public void waitWhileMoving() {
		while (isMoving()) {
			pause(100);
		}
	}
	/**
	 * Sleeps for the waitTime.
	 * @param waitTimeInMS
	 */
	private void pause(long waitTimeInMS) {
		try {
			Thread.sleep(waitTimeInMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Sets Galil's min and max az value.
	 * @param minAz
	 * @param maxAz
	 */
	public void setMinMaxAz(double minAz, double maxAz) {
		this.minAz = minAz;
		this.maxAz = maxAz;
	}
	/**
	 * Sets Galil's min and max el value.
	 * @param minEl
	 * @param maxEl
	 */
	public void setMinMaxEl(double minEl, double maxEl) {
		this.minEl = minEl;
		this.maxEl = maxEl;
	}

	public int getEncTol() {return encTol;}
	/**
	 * Sets some value of Galil, I'm not sure which.
	 * @param encTol
	 */
	public void setEncTol(int encTol) {
		this.encTol = encTol;
	}
	/** 
	 * Gets and updates the velocity and position of the az axis.
	 */
	public void status() {
		String tellPos = "TP";
		String tellVel = "TV";
		String azAxis = "A";
		DataGalil data;

		String azPos = protocol.sendRead(tellPos + azAxis);
		String azVel = protocol.sendRead(tellVel + azAxis);

		data = new DataGalil();
		data.makeAz(azPos, azVel);
		position = data;
		window.updateTxtPosInfo(position.info());
	}
	/**
	 * Sends a message to Galil and prints its reply.
	 * @param command to send
	 */
	public void sendCommand(String command) {
		System.out.println(protocol.sendRead(command));
	}
	/**
	 * Gets and prints the amount of bytes waiting to be read from Galil.
	 */
	public void queueSize() {
		System.out.println(protocol.queueSize());
	}
	/**
	 * Reads off what Galil is waiting to send.
	 */
	public void readQueue() {
		protocol.test();
	}
	/**
	 * Gets the position of a certain axis.
	 * @param axisType
	 * @return
	 */
	public double encPos(axisType axisType) {
		if (position == null) return 0;
		switch (axisType) {
		case AZ:
			return position.azPos();
		case EL:
			return position.elPos();
		default:
			return 0;
		}
	}
	/**
	 * Tells the user and everything else that indexing is done.
	 * @param type
	 */
	public void indexingDone(axisType type) {
		System.out.println("indexing done");
		switch (type) {
		case AZ:
			az.setIndexing(false); break;
		case EL:
			el.setIndexing(false); break;
		}
	}
	/**
	 * Moves Galil to a specified coordinate.
	 * @param c coordinate
	 */
	public void goToPos(Coordinate c) {
		moveAbsolute(c.getAz(), c.getEl());
	}
	/**
	 * Sets the coordinate for ra dec tracking.
	 * @param ra
	 * @param dec
	 */
	public void setRaDecTracking(double ra, double dec) {
		window.setRaDec(ra, dec);
	}
	/**
	 * Sets the base location.
	 * @param pos in latitude, longitude, and position
	 */
	public void setBaseLocation(LatLongAlt pos) {
		baseLocation = pos;
		calcAzElToBalloon();
		window.updateBaseBalloonLoc();
	}
	/**
	 * Sets the balloon location.
	 * @param pos in latitude, longitude, and position
	 */
	public void setBalloonLocation(LatLongAlt pos) {
		balloonLocation = pos;
		calcAzElToBalloon();
		window.updateBaseBalloonLoc();
	}
	/**
	 * Converts the longitude and latitude position of the balloon to an az and el coordinate.
	 */
	private void calcAzElToBalloon() {
		Coordinate base = new Coordinate(baseLocation);
		Coordinate balloon = new Coordinate(balloonLocation);

		double deltaX = balloon.getX() - base.getX();
		double deltaY = balloon.getY() - base.getY();
		double deltaZ = balloon.getZ() - base.getZ();

		Coordinate R = new Coordinate(deltaX, deltaY, deltaZ, true);

		Coordinate rHat = base.rHat();
		Coordinate thetaHat = base.thetaHat();
		Coordinate phiHat = base.phiHat();
		thetaHat = thetaHat.negate();

		double xRel = R.dot(phiHat);
		double yRel = R.dot(thetaHat);
		double zRel = R.dot(rHat);
		double xyRel = Math.sqrt(xRel*xRel + yRel*yRel);

		elToBalloon = Math.toDegrees(Math.atan2(zRel, xyRel));
		azToBalloon = Math.toDegrees(Math.atan2(xRel, yRel));
		if (azToBalloon < 0) azToBalloon = azToBalloon + 360;
	}
	/**
	 * Returns the balloon location.
	 * @return
	 */
	public LatLongAlt getBalloonLocation() {
		return balloonLocation;
	}
	/**
	 * Return the base location.
	 * @return
	 */
	public LatLongAlt getBaseLocation() {
		return baseLocation;
	}
	/**
	 * The string that displays the base location in the program.
	 * @return
	 */
	public String baseLocDisplay() {
		return "Base Location\n" + baseLocation.guiString();
	}
	/**
	 * The string that displays the balloon location in the program.
	 * @return
	 */
	public String balloonLocDisplay() {
		String out = "BalloonLocation\n" + balloonLocation.guiString();
		out += "\nAz to balloon:  " + Formatters.TWO_POINTS_FORCE.format(azToBalloon) + "\n";
		out += "El to balloon:  " + Formatters.TWO_POINTS_FORCE.format(elToBalloon);
		return out;
	}
	/**
	 * Moves Galil to the balloon position.
	 */
	public void goToBalloon() {
		moveAbsolute(azToBalloon, elToBalloon);
	}
	/**
	 * Sets a new coordinate as the relative (0,0).
	 * @param c coordinate
	 */
	public void calibrate(Coordinate c) {
		az.calibrate(c.getAz());
		el.calibrate(c.getEl());
	}
	/**
	 * Convenience method for enabling a method.
	 * @param name
	 */
	public void buttonEnabler(String name) {
		window.buttonEnabler(name);
	}
	/**
	 * Updates Galil's position for the user.
	 * @param data to update with
	 */
	void updatePosition(DataInterface data) {
		position = data;
		String info = position.info();
		if (window.debug) {
			info += "Az AbsPos: " + Formatters.TWO_POINTS.format(az.absolutePos()) + "\n";
			info += "El AbsPos: " + Formatters.TWO_POINTS.format(el.absolutePos()) + "\n";
		}
		window.updateTxtPosInfo(info);
		
	}
	/**
	 * Updates the axes velocity and acceleration for the user.
	 * @param azVel
	 * @param azAcc
	 * @param elVel
	 * @param elAcc
	 */
	void updateVelAcc(String azVel, String azAcc, String elVel, String elAcc) {
		window.updateVelAcc(azVel, azAcc, elVel, elAcc);
	}
	/**
	 * Allows data to be read from Galil to update the velocity, acceleration and position for the user.
	 */
	void toggleReader() {
		reader.togglePauseFlag();
	}

//	void setGoalAz(double goalAz) {
//		window.setGoalAz(goalAz);
//	}
//
//	void setGoalEl(double goalEl) {
//		window.setGoalEl(goalEl);
//	}
	/**
	 * Sets where an axis wants to go if it were to complete a successful move.
	 * @param deg absolute position it will end up
	 * @param axis
	 */
	void setGoalPos(double deg, axisType axis) {
		window.setGoalPos(Formatters.TWO_POINTS.format(deg), axis);
	}
	/**
	 * Closes the program and saves all settings.
	 */
	public void shutdown() {
		exec.shutdown();
		reader.stop2();
		try {
			saveSettings();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		switch (type) {
		case Galil:
			closeGalil();
			protocol.close();
			break;
		case FTDI:
			break;
		}
	}

}