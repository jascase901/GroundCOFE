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
	private double maxMoveRel = 360;
	private int encTol = 10;
	
	private int velocity;
	
	private Timer raDecTracker, lstUpdater;
	private final ExecutorService exec = Executors.newFixedThreadPool(1);
	private boolean scanning = false;
	private ActInterface az, el;
	private MainWindow window;
	private boolean commStatus = false;
	private ReaderInterface reader;
	private DataInterface position;
	private final Properties actSettings = new Properties();
	private final Properties settings = new Properties();
	private LatLongAlt baseLocation, balloonLocation;
	private double azToBalloon = 0, elToBalloon = 0;
	private CommGalil protocol;
	
	public Stage() {
		try {
			loadSettings();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void initialize(MainWindow window) throws FileNotFoundException, IOException {
		this.window = window;
		switch (type) {
			case Galil:
				protocol = new CommGalil(1337);
				az = new ActGalil(axisType.AZ, protocol);
				el = new ActGalil(axisType.EL, protocol);
				reader = new ReaderGalil(this);
				loadGalil();
				az.registerStage(this);
				el.registerStage(this);
				break;
			case FTDI:
				az = new ActFTDI();
				el = new ActFTDI();
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
				//for FTDI only: velocity of 0 means no motion has occured
				//vel=1 currently at rest, but was moving forward
				//vel=-1 currently at rest, but was moving backwards
				if (Math.abs(velocity) == 1 || velocity == 0) {
					moveAbsolute(az, el);
				}
			}
		}, 0, period);
	}
	
	public void stopRaDecTracking() {
		raDecTracker.cancel();
	}
	
	private void updateLst() {
		lstUpdater = new Timer("LST Updater", true);
		lstUpdater.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {		
				Calendar local = Calendar.getInstance();
				
				double azPos = 0;
				if (position != null ) {
					azPos = az.encValToDeg(position.azPos());
				}
				double elPos = 0;
				if (position != null ) {
					elPos = el.encValToDeg(position.elPos());
				}
				
				double ra = baseLocation.azelToRa(azPos, elPos);
				double dec = baseLocation.azelToDec(azPos, elPos);
				double lst = baseLocation.lst();
				String gmt = baseLocation.gmt();
				
				//double hourLst = (int) lst;
				//double minLst = (lst - hourLst)*60;
				//double secLst = (minLst - (int)minLst)*60;
				//String sLst = Formatters.lstFormatter(hourLst, minLst, secLst);
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
	
	//Unknown functionality at the moment.  (2/13/2012)
	public void startScanning(final double minScan, final double maxScan, double time, final int reps, final axisType type) {
		System.out.println("min angle:  " + minScan);
		System.out.println("max angle:  " + maxScan);
		System.out.println("time:  " + time);
		System.out.println("axis:  " + type.toString());
		
		scanning = true;
		
		exec.submit(new Runnable() {
			@Override
			public void run() {
				double min = 0, max = 0;
				ActInterface axis = null;
				switch (type) {
					case AZ:
						axis = az; min = minAz; max = maxAz; break;
					case EL:
						axis = el; min = minEl; max = maxEl; break;
				}
				System.out.println(min +" "+ max);
				if (minScan < min || maxScan > max) {
					System.out.println("invalid minscan or maxscan");
					return;
				}
				
				for (int i = 1; i <= reps; i++) {
					if (scanning == false) break;
						axis.moveAbsolute(minScan);
						pauseWhileMoving();
						axis.moveAbsolute(maxScan);
						pauseWhileMoving();
					}
				window.toggleAzScan();
			}
		});
	
	}
	
	public void pauseWhileMoving(){
		while (isMoving()){
			pause(500);
		}
		pause(1000);

	}
	public void stopScanning() {
		scanning = false;
	}
	
	public void moveAbsolute(final double azDeg, final double elDeg) {
		exec.submit(new Runnable() {
			@Override
			public void run() {	
				if (az.allowedMove("absolute", minAz, maxAz, azDeg)) {
					System.out.println("allowed az");
					az.moveAbsolute(azDeg);
				}
				else {
					System.out.println("az not in range");
				}
				if (el.allowedMove("absolute", minEl, maxEl, elDeg)) {
					System.out.println("allowed el");
					el.moveAbsolute(elDeg);
				}
				else {
					System.out.println("el not in range");
				}
				
				
//				System.out.println("should wait - stage");
//				while (!isAtRest()) {
//					pause(100);
//				}
//				System.out.println("done waiting - stage");
//				if (elDeg >= minEl && elDeg <= maxEl) {
//					el.moveAbsolute(elDeg);
//					System.out.println("allowed el");
//				}
//				else {
//					System.out.println("el not in range");
//				}
				window.enableMoveButtons();
			}
		});
	}
	
	public void relative(final axisType type, final String moveType, final double amount) {
		exec.submit(new Runnable() {
			@Override
			public void run() {
				double min = 0, max = 0;
				ActInterface axis = null;
				switch (type) {
					case AZ:
						axis = az; min = minAz; max = maxAz; break;
					case EL:
						axis = el; min = minEl; max = maxEl; break;
				}
				if(axis.allowedMove(moveType, min, max, amount)){
					if (moveType.equals("steps")) {
						axis.moveRelative(amount, "steps");
						//System.out.println(amount);
					}
					else if (moveType.equals("degrees")) {
						if (amount <= maxMoveRel) {
							axis.moveRelative(amount, "degrees");
						}
						else {
							//message to user saying that moving that much isn't allowed
							
						}
					}
					else if (moveType.equals("encoder")) {
						//axis.moveRelative(amount, "encoder");
						System.out.println("hi");
					}
				}
				else{
					window.displayErrorBox("Not allowed to move here");
				}
				window.enableMoveButtons();
			}
		});
	}
	
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
	
	//probably not needed for galil
	//NOTE!!! this isn't used by the calibrate popup
	//that method is calibrate(Coordinate c)
	public void calibrate(double azDeg, double elDeg) {
		System.out.println("wtf");
		System.out.println("azDeg: " + azDeg);
		System.out.println("elDeg: " + elDeg);
		az.calibrate(azDeg);
		el.calibrate(elDeg);
	}
	
	//TODO probably not needed with Galil
	public void previousCalibrate(final double previousAz, final double previousEl) {
		exec.submit(new Runnable() {
			@Override
			public void run() {
				pause(2000);
//				ActStatus azStatus = az.getStatus();
//				ActStatus elStatus = el.getStatus();
//				if (azStatus.allZero() && elStatus.allZero()) {
//					az.setOffset(previousAz);
//					el.setOffset(previousEl);
//				}
			}
		});
	}
	
	public void initializeValues(double azOffset, double elOffset, double azEncInd, double elEncInd) {
		az.setOffset(azOffset);
		az.setEncInd(azEncInd);
		el.setOffset(elOffset);
		el.setEncInd(elEncInd);
	}
	
	//should return true if something is moving, false if not
	//FTDI functionality unknown (reed, 4/19/2012)
	public boolean isMoving() {
		switch (type) {
			case FTDI:
				return Math.abs(velocity) <= 1;
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
	
	public boolean ftdiRest() {return Math.abs(velocity) <= 1;}
	
	private void pause(long waitTimeInMS) {
		try {
			Thread.sleep(waitTimeInMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int getVelocity() {
		return velocity;
	}
	
//	public void setStatuses(ActStatus azStatus, ActStatus elStatus, int velocity) {
//		az.setStatus(azStatus);
//		el.setStatus(elStatus);
//		this.velocity = velocity;
//		//System.out.println("velocity:  " + this.velocity);
//	}
	
	//TODO unneeded?
	//public double getMinAz() {return minAz;}
	//public double getMaxAz() {return maxAz;}	
	public void setMinMaxAz(double minAz, double maxAz) {
		this.minAz = minAz;
		this.maxAz = maxAz;
		
	}
	
	//TODO unneeded?
	//public double getMinEl() {return minEl;}
	//public double getMaxEl() {return maxEl;}	
	public void setMinMaxEl(double minEl, double maxEl) {
		this.minEl = minEl;
		this.maxEl = maxEl;
	}

	public int getEncTol() {return encTol;}
	public void setEncTol(int encTol) {
		this.encTol = encTol;
	}
	
	public void status() {
		String tellPos = "TP";
		String tellVel = "TV";
		String azAxis = "A";
		DataGalil data;
		//TODO
		//CommGalil protocol = CommGalil.getInstance();
		
		String azPos = protocol.sendRead(tellPos + azAxis);
		String azVel = protocol.sendRead(tellVel + azAxis);

		data = new DataGalil();
		data.makeAz(azPos, azVel);
		position = data;
		window.updateTxtPosInfo(position.info());
	}
	
	public void sendCommand(String command) {
		//CommGalil protocol = CommGalil.getInstance();
		System.out.println(protocol.sendRead(command));
	}
	
	public void queueSize() {
		//CommGalil protocol = CommGalil.getInstance();
		System.out.println(protocol.queueSize());
	}
	
	public void readQueue() {
		//CommGalil protocol = CommGalil.getInstance();
		protocol.test();
	}
	
	//TODO unneeded?
	//probably don't need these since the settings files are written from stage
//	public double getAzOffset() {return az.getOffset();}
//	public double getElOffset() {return el.getOffset();}
//	public double getAzEncInd() {return az.getEncInd();}
//	public double getElEncInd() {return el.getEncInd();}
	
	public double currentAzDeg() {
//		double azDeg = az.currentDegPos();
//		return azDeg;
		if (position == null) return 0;
		double azDeg = position.azPos();
		return azDeg;
	}
	
	//this doesn't give the position in degrees...
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
	
	public double currentElDeg() {
//		double elDeg = el.currentDegPos();
//		return elDeg;
		if (position == null) return 0;
		double azDeg = position.elPos();
		return azDeg;
	}
	
	public void indexingDone(axisType type) {
		System.out.println("indexing done");
		switch (type) {
			case AZ:
				az.setIndexing(false); break;
			case EL:
				el.setIndexing(false); break;
		}
	}
	
	public void goToPos(Coordinate c) {
		moveAbsolute(c.getAz(), c.getEl());
	}
	
	public void setRaDecTracking(double ra, double dec) {
		
	}
	
	public void setBalloonLocation(LatLongAlt pos) {
		balloonLocation = pos;
		window.updateBaseBalloonLoc();
		
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
	
	public void setBaseLocation(LatLongAlt pos) {
		baseLocation = pos;
		window.updateBaseBalloonLoc();
	}
	
	public LatLongAlt getBalloonLocation() {
		return balloonLocation;
	}
	
	public LatLongAlt getBaseLocation() {
		return baseLocation;
	}
	
	public String baseLocDisplay() {
		return "Base Location\n" + baseLocation.guiString();
	}
	
	public String balloonLocDisplay() {
		String out = "BalloonLocation\n" + balloonLocation.guiString();
		out += "\nAz to balloon:  " + Formatters.TWO_POINTS_FORCE.format(azToBalloon) + "\n";
    	out += "El to balloon:  " + Formatters.TWO_POINTS_FORCE.format(elToBalloon);
    	return out;
	}
	
	public void goToBalloon() {
		moveAbsolute(azToBalloon, elToBalloon);
	}
	
	public void calibrate(Coordinate c) {
		az.calibrate(c.getAz());
		el.calibrate(c.getEl());
	}
	
	public void buttonEnabler(String name) {
		window.buttonEnabler(name);
	}
	
	void updatePosition(DataInterface data) {
		position = data;
		window.updateTxtPosInfo(position.info());
	}
	
	void toggleReader() {
		reader.togglePauseFlag();
	}
	
	void setGoalAz(double goalAz) {
		window.setGoalAz(goalAz);
	}
	
	void setGoalEl(double goalEl) {
		window.setGoalEl(goalEl);
	}
	
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
				//CommGalil.getInstance().close();
				protocol.close();
				break;
			case FTDI:
				break;
		}
	}

}