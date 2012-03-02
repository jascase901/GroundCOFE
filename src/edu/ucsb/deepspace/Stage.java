package edu.ucsb.deepspace;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
	
	private double minAz, maxAz, minEl, maxEl;
	private double maxMoveRel = 90;
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
				az = new ActGalil(axisType.AZ);
				el = new ActGalil(axisType.EL);
				reader = new ReaderGalil(this);
				loadGalil();
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
			//System.out.println("reader started");
			//reader.start();
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
	}
	
	private void closeGalil() {
		actSettings.setProperty("azOffset", String.valueOf(az.getOffset()));
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
	
	public String stageInfo() {
		String out = "";
		//String out = az.info() + "\n" + el.info();
		System.out.println(out);
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
				if (Math.abs(velocity) == 1 || velocity == 0) {
					moveAbsolute(az, el);
				}
			}
		}, 0, period);
	}
	
	private void updateLst() {
		lstUpdater = new Timer("LST Updater", true);
		lstUpdater.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {		
				Calendar local = Calendar.getInstance();
				DateFormat a = new SimpleDateFormat("HH:mm:ss");
				
				double azPos = 4;
				double elPos = 3;
				
				double ra = baseLocation.azelToRa(azPos, elPos);
				double dec = baseLocation.azelToDec(azPos, elPos);
				double lst = baseLocation.lst();
				String gmt = baseLocation.gmt();
				
				double hourLst = (int) lst;
				double minLst = (lst - hourLst)*60;
				double secLst = (minLst - (int)minLst)*60;
				String sLst = Formatters.lstFormatter(hourLst, minLst, secLst);
				
				
				DecimalFormat formatter = new DecimalFormat("###.##");
				
				String out = "Az:  " + formatter.format(azPos);;
				out += "\nEl:  " + formatter.format(elPos);
				formatter = new DecimalFormat("##.####");
				out += "\nRA:  " + formatter.format(ra);
				formatter = new DecimalFormat("##.###");
				out += "\nDec:  " + formatter.format(dec);
				out += "\nLST:  " + sLst;
				out += "\nUTC:  " + gmt;
				out += "\nLocal:  " + a.format(local.getTime());
				
				window.updateTxtAzElRaDec(out);
			}
		}, 0, 1000);
	}
	
	
	public void stopRaDecTracking() {
		raDecTracker.cancel();
	}
	
	public void stopScanning() {
		scanning = false;
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
				
				if (minScan < min || maxScan > max) {
					System.out.println("invalid minscan or maxscan");
					return;
				}
				
				for (int i = 1; i <= reps; i++) {
					if (scanning == false) break;
					if (i%2 == 0) {
						axis.moveAbsolute(minScan);
					}
					else if (i%2 == 1) {
						axis.moveAbsolute(maxScan);
					}
				}
			}
		});
		
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
				System.out.println("should wait - stage");
				while (!isAtRest()) {
					pause(100);
				}
				System.out.println("done waiting - stage");
				if (elDeg >= minEl && elDeg <= maxEl) {
					el.moveAbsolute(elDeg);
					System.out.println("allowed el");
				}
				else {
					System.out.println("el not in range");
				}
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
				axis.allowedMove(moveType, min, max, amount);
				if (moveType.equals("steps")) {
					axis.moveEncVal((long) amount);
				}
				else if (moveType.equals("degrees")) {
					if (amount <= maxMoveRel) {
						axis.moveRelative(amount);
					}
					else {
						//message to user saying that moving that much isn't allowed
					}
				}
				else if (moveType.equals("encoder")) {
					axis.moveEncoder(amount);
				}
				window.enableMoveButtons();
			}
		});
	}
	
	public void index(axisType type) {
		switch (type) {
			case AZ:
				az.index(); break;
			case EL:
				el.index(); break;
		}
	}
	
	public void calibrate(double azDeg, double elDeg) {
		az.calibrate(azDeg);
		el.calibrate(elDeg);
	}
	
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
	
	public boolean isAtRest() {
		switch (type) {
			case FTDI:
				return Math.abs(velocity) <= 1;
			case Galil:
				return az.isMoving() && el.isMoving();
			default:
				return true;
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
	
	public double getMinAz() {return minAz;}
	public double getMaxAz() {return maxAz;}	
	public void setMinMaxAz(double minAz, double maxAz) {
		this.minAz = minAz;
		this.maxAz = maxAz;
	}
	
	public double getMinEl() {return minEl;}
	public double getMaxEl() {return maxEl;}	
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
		
		CommGalil protocol = CommGalil.getInstance();
		
		String azPos = protocol.sendRead(tellPos + azAxis);
		String azVel = protocol.sendRead(tellVel + azAxis);

		data = new DataGalil();
		data.makeAz(azPos, azVel);
		position = data;
		window.updateTxtPosInfo(position.info());
	}
	
	public void sendCommand(String command) {
		CommGalil protocol = CommGalil.getInstance();
		System.out.println(protocol.sendRead(command));
	}
	
	public void queueSize() {
		CommGalil protocol = CommGalil.getInstance();
		System.out.println(protocol.queueSize());
	}
	
	public void readQueue() {
		CommGalil protocol = CommGalil.getInstance();
		protocol.test();
	}
	
	public double getAzOffset() {return az.getOffset();}
	public double getElOffset() {return el.getOffset();}
	public double getAzEncInd() {return az.getEncInd();}
	public double getElEncInd() {return el.getEncInd();}
	
	public double currentAzDeg() {
		double azDeg = az.currentDegPos();
		//azDeg = 270.07;
		//if (azDeg == null) azDeg = 0;
		return azDeg;
	}
	public double currentElDeg() {
		double elDeg = el.currentDegPos();
		//elDeg = 78.61;
		return elDeg;
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
		
	}
	
	public void setRaDecTracking(double ra, double dec) {
		
	}
	
	public void setBalloonLocation(LatLongAlt pos) {
		balloonLocation = pos;
		window.updateBaseBalloonLoc();
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
	}
	
	public void buttonEnabler(String name) {
		window.buttonEnabler(name);
	}
	
	void updatePosition(DataInterface data) {
		position = data;
		window.updateTxtPosInfo(position.info());
		
	}
	
	public void shutdown() {
		exec.shutdown();
		reader.stop2();
		try {
			saveSettings();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch (type) {
			case Galil:
				closeGalil();
				CommGalil.getInstance().close();
				break;
			case FTDI:
				break;
		}
	}

}