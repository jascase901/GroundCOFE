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

import edu.ucsb.deepspace.MoveCommand.MoveMode;
import edu.ucsb.deepspace.MoveCommand.MoveType;
import edu.ucsb.deepspace.gui.MainWindow;

public class Stage implements StageInterface {
	
	private static final StageInterface INSTANCE = new Stage();
	public static StageInterface getInstance() {return INSTANCE;}
	
	public static enum StageTypes {
		GALIL, FTDI;
	}
	private StageTypes stageType = StageTypes.GALIL;
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getType()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getType()
	 */
	
	
	public StageTypes getType() {
		return this.stageType;
	}

	private double minAz, maxAz, minEl, maxEl;
	private double maxVelAz, maxAccAz, maxVelEl, maxAccEl;
	private double maxMoveRelAz, maxMoveRelEl;
	private int encTol = 10;
	
	private TelescopeInterface scope;
	private String txtPosInfo;
	private Timer raDecTracker, lstUpdater;
	private final ExecutorService exec = Executors.newFixedThreadPool(1);
	private Ui window;
	private boolean commStatus = false;
	private ReaderInterface reader;
	private boolean readerState = false;
	private DataInterface position = DataGalil.blank();
	private final Properties actSettings = new Properties();
	private final Properties settings = new Properties();
	private LatLongAlt baseLocation, balloonLocation;
	private double azToBalloon = 0, elToBalloon = 0;
	private CommGalil stageProtocol, scopeProtocol, readerProtocol;
	private Boolean continousScanOn = false;
	private Boolean raOn = false;

	ScriptLoader sl;
	private int maxAzVel;
	private int maxElVel;

	


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
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#initialize(edu.ucsb.deepspace.Ui)
	 */
	
	
	public void initialize(Ui window) throws FileNotFoundException, IOException {
		this.window = window;
		switch (stageType) {
		case GALIL:
			stageProtocol = new CommGalil(2222);
			scopeProtocol = new CommGalil(23);
			readerProtocol = new CommGalil(4444);
			loadScripts();
			scope = new TelescopeGalil(this, scopeProtocol);
			reader = new ReaderGalil(this, readerProtocol);
			
			loadGalil();
			loadSafety(minAz, maxAz, minEl, maxEl);
			break;
		case FTDI:
			//Really tired of FTDI stuff.
			//Reed, 5/19/2012
			break;
		}
		updateLst();
		if (commStatus) {
			refreshScripts();
			reader.start();
			if (sl.readerReady()) {
				reader.readerOnOff(true);
			}
		}
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
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#loadSafety(double, double, double, double)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#loadSafety(double, double, double, double)
	 */
	
	
	public void loadSafety(double minAz, double maxAz, double minEl, double maxEl){
		scope.safety(minAz, maxAz, minEl, maxEl);
		
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
		double azOffset = Double.parseDouble(actSettings.getProperty("azOffset", "0"));
		double elOffset = Double.parseDouble(actSettings.getProperty("elOffset", "0"));
		minAz = Double.parseDouble(actSettings.getProperty("minAz", "0"));
		maxAz = Double.parseDouble(actSettings.getProperty("maxAz", "0"));
		minEl = Double.parseDouble(actSettings.getProperty("minEl", "0"));
		maxEl = Double.parseDouble(actSettings.getProperty("maxEl", "0"));
		maxVelAz = Double.parseDouble(actSettings.getProperty("maxVelAz", "0"));
		maxAccAz = Double.parseDouble(actSettings.getProperty("maxAccAz", "0"));
		maxVelEl = Double.parseDouble(actSettings.getProperty("maxVelEl", "0"));
		maxAccEl = Double.parseDouble(actSettings.getProperty("maxAccEl", "0"));
		maxMoveRelAz = Double.parseDouble(actSettings.getProperty("maxMoveRelAz", "360"));
		maxMoveRelEl = Double.parseDouble(actSettings.getProperty("maxMoveRelEl", "360"));
		encTol = Integer.parseInt(actSettings.getProperty("encTol", "10"));
		scope.setOffsets(azOffset, elOffset);

		window.setMinMaxAzEl(minAz, maxAz, minEl, maxEl);
		window.setVelAccAzEl(maxVelAz, maxAccAz, maxVelEl, maxAccEl);
		window.setMaxMoveRel(maxMoveRelAz, maxMoveRelEl);
		
	}
	
	/**
	 * Saves the settings of Galil back into galil.ini.
	 */
	private void closeGalil() {
		actSettings.setProperty("azOffset", String.valueOf(scope.getOffset(Axis.AZ)));
		actSettings.setProperty("elOffset", String.valueOf(scope.getOffset(Axis.EL)));
		actSettings.setProperty("minAz", String.valueOf(minAz));
		actSettings.setProperty("maxAz", String.valueOf(maxAz));
		actSettings.setProperty("minEl", String.valueOf(minEl));
		actSettings.setProperty("maxEl", String.valueOf(maxEl));
		actSettings.setProperty("maxVelAz", String.valueOf(maxVelAz));
		actSettings.setProperty("maxAccAz", String.valueOf(maxAccAz));
		actSettings.setProperty("maxVelEl", String.valueOf(maxVelEl));
		actSettings.setProperty("maxAccEl", String.valueOf(maxAccEl));
		actSettings.setProperty("maxMoveRelAz", String.valueOf(maxMoveRelAz));
		actSettings.setProperty("maxMoveRelEl", String.valueOf(maxMoveRelEl));
		actSettings.setProperty("encTol", String.valueOf(encTol));
		try {
			actSettings.store(new FileOutputStream("Galil.ini"), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void loadFTDI() throws FileNotFoundException, IOException {
		actSettings.load(new FileInputStream("FTDI.ini"));
	}

	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#confirmCommConnection()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#confirmCommConnection()
	 */
	
	
	public void confirmCommConnection() {
		commStatus = true;
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#toggleReader()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#toggleReader()
	 */
	
	
	public void toggleReader() {
		readerState = !readerState;
		reader.readerOnOff(readerState);
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#loadScripts()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#loadScripts()
	 */
	
	
	public void loadScripts() {
		sl = new ScriptLoader();
		sl.load();
		sl.findLoaded();
		sl.close();
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#refreshScripts()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#refreshScripts()
	 */
	
	
	public void refreshScripts() {
		sl = new ScriptLoader();
		sl.findLoaded();
		sl.close();
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#refreshScriptWindow()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#refreshScriptWindow()
	 */
	
	
	public void refreshScriptWindow() {
		sl = new ScriptLoader();
		window.updateScriptArea("expected", sl.findExpected());
		window.updateScriptArea("loaded", sl.findLoaded());
		sl.close();
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#startRaDecTracking(double, double)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#startRaDecTracking(double, double)
	 */
	
	
	public void startRaDecTracking(final double ra, final double dec) {
		if (!motorCheck(Axis.AZ)) {
			window.raDecTrackingButtonUpdater(true, false);
			return;
		}
		if (!motorCheck(Axis.EL)) {
			window.raDecTrackingButtonUpdater(true, false);
			return;
		}
		long period = 10000;
		raDecTracker = new Timer("RA/Dec Tracker", true);
		raDecTracker.scheduleAtFixedRate(new TimerTask() {
			
			public void run() {
				double az = baseLocation.radecToAz(ra, dec);
				double el = baseLocation.radecToEl(ra, dec);
				System.out.println("az:  " + az);
				System.out.println("el:  " + el);
				System.out.println();
				//for FTDI only: velocity of 0 means no motion has occurred
				//vel=1 currently at rest, but was moving forward
				//vel=-1 currently at rest, but was moving backwards
				moveAbsolute(az, el);
			}
		}, 0, period);
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#stopRaDecTracking()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#stopRaDecTracking()
	 */
	
	
	public void stopRaDecTracking() {
		if (raDecTracker == null) return;
		raDecTracker.cancel();
	}
	/**
	 * Updates information on the Galil position, times, and ra dec coordinates every second.
	 */
	private void updateLst() {
		lstUpdater = new Timer("LST Updater", true);
		lstUpdater.scheduleAtFixedRate(new TimerTask() {
			
			public void run() {		
				Calendar local = Calendar.getInstance();

				double azPos = 0, elPos = 0;
				if (position != null ) {
					azPos = scope.getUserPos(Axis.AZ);
					elPos = scope.getUserPos(Axis.EL);
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
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#updateScanCommand(edu.ucsb.deepspace.ScanCommand, edu.ucsb.deepspace.Axis)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#updateScanCommand(edu.ucsb.deepspace.ScanCommand, edu.ucsb.deepspace.Axis)
	 */
	
	
	public void updateScanCommand(ScanCommand sc, Axis axis) {
		
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#canScan(edu.ucsb.deepspace.ScanCommand, edu.ucsb.deepspace.ScanCommand, double)
	 */
	
	//TODO FIGURE OUT A WAY TO MAKE THIS PRETTY
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#canScan(edu.ucsb.deepspace.ScanCommand, edu.ucsb.deepspace.ScanCommand, double)
	 */
	
	
	public boolean canScan(ScanCommand azSc, ScanCommand elSc, double time){
			
		double axAz;
		double axEl;
		boolean moveable = true;
		
	if (azSc == null){
			

			double acVelEl = 2*scope.getDistance(elSc.getMin(), elSc.getMax(), Axis.EL)/time;
			double acTimeEl =acVelEl/maxAccEl;

			 
			axAz = 0;
			axEl = .5*maxAccEl*acTimeEl*acTimeEl;
		
			axEl = scope.convEncToDeg(axEl, Axis.EL);
		
			MoveCommand mcMinEl = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, null ,elSc.getMin()-axEl);
			MoveCommand mcMaxEl = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, null ,elSc.getMax()+axEl);
			moveable =  canMove(mcMaxEl, false) && canMove(mcMinEl,false);
			if (GalilCalc.round(axEl, 2) == 0){
				statusArea("scan in dead range");
				return true;
			}
		
			
		}
		else{
			double acVelAz = 2*scope.getDistance(azSc.getMin(), azSc.getMax(), Axis.AZ)/time;
			double acTimeAz =acVelAz/maxAccAz;
			
			

			axAz = .5*maxAccAz*acTimeAz*acTimeAz;
			axEl = 0;
			axAz = scope.convEncToDeg(axAz, Axis.AZ);

			MoveCommand mcMinAz = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, azSc.getMin()-axAz, null);
			MoveCommand mcMaxAz = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, azSc.getMax()+axAz, null);
			moveable = canMove(mcMaxAz,false) && canMove(mcMinAz,false);
			
			if (GalilCalc.round(axAz, 2) == 0){
				statusArea("scan in dead range");
				return true;

			}
				
			
		}
		
		
		
		
		
		
		


			return moveable;
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#test()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#test()
	 */
	
	
	public boolean test(){
		return true;
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#Spin()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#Spin()
	 */
	
	
	public void Spin(){
		exec.submit(new Runnable(){
			
			public void run(){
				scope.Spin();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#startScanning(edu.ucsb.deepspace.ScanCommand, edu.ucsb.deepspace.ScanCommand, boolean)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#startScanning(edu.ucsb.deepspace.ScanCommand, edu.ucsb.deepspace.ScanCommand, boolean)
	 */
	
	
	public void startScanning(final ScanCommand azSc, final ScanCommand elSc, final boolean fraster) {
		if (azSc == null && elSc == null) {
			window.updateStatusArea("Fatal error.  The ScanCommands associated with Az and El are both null.\n");
			return;
		}
		//I changed the size of the thread pool executor to 2.  This may have unintended consequences
		//for other pieces of the code.  However, this is the only way I could get both to scan at
		//the same time AND have the scanning done from inside the ActGalil class.
		exec.submit(new Runnable() {
			
			public void run() {
				int azSpeedMax= (int) maxVelAz;
	
				double reps = 0;
				double time = 0;
				if (elSc!=null){
						time=elSc.getTime();
						reps=elSc.getReps();
				}
				else{
						time = azSc.getTime();
						reps = azSc.getReps();
				}
				//If user presses scan both
				if (azSc != null && elSc!=null && raOn) {
					double minAz = azSc.getMin();
					double maxAz = azSc.getMax();
					double minEl = azSc.getMin();
					double maxEl = azSc.getMax();
					double roundPlace = 1000;
					//convert cords to time dependent RADEC
					double minDec = baseLocation.azelToDec(azSc.getMin(), elSc.getMin());
					double maxDec =baseLocation.azelToDec(azSc.getMax(), elSc.getMax());
					double minRa = baseLocation.azelToRa(azSc.getMin(), elSc.getMin());
					double maxRa = baseLocation.azelToRa(azSc.getMax(), elSc.getMax());
					double maxScanTime = 0;

					while(continousScanOn || reps>0){
						reps = reps -1;

						waitWhileExecuting(1);
						//scan uses Az/El coord not ra dec so make scan commands with az/el
						ScanCommand aSc= new ScanCommand(minAz, maxAz, time, (int)azSc.getReps());
						ScanCommand eSc =new ScanCommand(minEl, maxEl, time, (int)elSc.getReps());
						if(canScan(azSc, eSc, elSc.getTime()))
							scope.scan(aSc,eSc, fraster);		
						waitWhileExecuting(1);
						//convert ra coords, to az el this changes with base location time
						minAz = GalilCalc.round(roundPlace*baseLocation.radecToAz(minRa, minDec), 4);
						minEl = GalilCalc.round(roundPlace*baseLocation.radecToEl(minRa, minDec),4);
						maxAz = GalilCalc.round(roundPlace*baseLocation.radecToAz(maxRa, maxDec),4);
						maxEl = GalilCalc.round(roundPlace*baseLocation.radecToEl(maxRa, maxDec),4);
						
						
					}
				}

				else {
					while(continousScanOn || reps>0){
						reps = reps-1;
						waitWhileExecuting(1);
						if (canScan(azSc, elSc, time))
							scope.scan(azSc, elSc, fraster);
						waitWhileExecuting(1);
						window.updateReps(reps);
					}
					
					
				}
				
				window.enableScanButtons();
				statusArea("I AM DONE SCANNING");
				

				
				
				
				
				

				
				
			}});
		
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#stopScanning()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#stopScanning()
	 */
	
	
	public void stopScanning() {
		scope.stopScanning();
	}
	
	
	//TODO Can Infinate loop
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getScanTime(edu.ucsb.deepspace.ScanCommand, edu.ucsb.deepspace.Axis)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getScanTime(edu.ucsb.deepspace.ScanCommand, edu.ucsb.deepspace.Axis)
	 */
	
	
	public double getScanTime(ScanCommand sc, Axis axis){
		double d=0, dx =0;
		double time = 0;
		d = Math.abs(scope.getDistance(maxAz, minAz, axis));
		
		
		switch(axis){
		case AZ:
			while (!canScan(sc, null, time)){
				if (maxAz<=sc.getMax() || minAz>=sc.getMin())
					return time;
				time=time+.5;
				//pause(12);
				
				
			}
			
			d = Math.abs(scope.getDistance(maxAz, minAz, axis));
	
			return time;
			
		case EL:
			while (!canScan(null, sc, time)){
				if (maxEl<=sc.getMax() || minEl>=sc.getMin())
					return time;
				time=time+1;
			}
			d=Math.abs(scope.getDistance(maxEl, minEl, axis));
			return time;

		default:
			return 0;
			
		}
		
	}
	
	boolean canMove( final MoveCommand mc, final boolean displayStatus){
		if (mc.getAzAmount() != null) {
			if (!motorCheck(Axis.AZ) && !motorCheck(Axis.EL)) {
				window.controlMoveButtons(true);
				statusArea("The motors must be on before moving.\n");
				return false;
			}
		}
		
		if (mc.getMode() == MoveMode.RELATIVE && mc.getType() == MoveType.DEGREE) {
			if (mc.getAmount(Axis.AZ) == null) {
				if (Math.abs(mc.getAmount(Axis.EL)) > maxMoveRelEl) {
					window.controlMoveButtons(true);
					if (displayStatus)
						statusArea("Maximum allowed relative move limit exceeded.\n");
					return false;
				}
			}
			else if (mc.getAmount(Axis.EL) == null) {
				if (Math.abs(mc.getAmount(Axis.AZ)) > maxMoveRelAz) {
					window.controlMoveButtons(true);
					if (displayStatus)
						statusArea("Maximum allowed relative move limit exceeded.\n");
					return false;
				}
			}
		}
		
	
		
		if (!scope.validMove(mc, minAz, maxAz, minEl, maxEl)) {
			System.out.println("this is an invalid move");
			if(displayStatus)
				statusArea("The desired position falls outside the allowed moving angles.\n");
			window.controlMoveButtons(true);
			return false;
		}
		
		return true;
		
	}
	boolean canMove(final MoveCommand mc){
		return  canMove(mc, true);
		
	}

	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#move(edu.ucsb.deepspace.MoveCommand)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#move(edu.ucsb.deepspace.MoveCommand)
	 */
	
	
	public void move(final MoveCommand mc) {
		exec.submit(new Runnable() {
			public void run() {
				if (!canMove(mc))
					return;
				scope.move(mc);
				
				System.out.println("done stage.move\n");
				window.controlMoveButtons(true);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setVelocity(double, edu.ucsb.deepspace.Axis)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setVelocity(double, edu.ucsb.deepspace.Axis)
	 */
	
	
	public void setVelocity(double vel, Axis axis) {
		scope.setVelocity(vel, axis);
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#moveRelative(java.lang.Double, edu.ucsb.deepspace.Axis, edu.ucsb.deepspace.MoveCommand.MoveType)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#moveRelative(java.lang.Double, edu.ucsb.deepspace.Axis, edu.ucsb.deepspace.MoveCommand.MoveType)
	 */
	
	
	public void moveRelative(Double amount, Axis axis, MoveType type) {
		MoveCommand mc = new MoveCommand(MoveMode.RELATIVE, type, null, null);
		
		switch (axis) {
			case AZ:
				mc = new MoveCommand(MoveMode.RELATIVE, type, amount, null);
				setVelocity( this.maxVelAz, axis);
				break;
			case EL:
				mc = new MoveCommand(MoveMode.RELATIVE, type, null, amount);
				setVelocity(this.maxVelEl, axis);
				break;
		}
		move(mc);
	}
	
	/**
	 * Convenience method that moves the az and el axis to an absolute position in degrees in two separate 
	 * threads.
	 * @param azDeg
	 * @param elDeg
	 */
	private void moveAbsolute(double azDeg, double elDeg) {
		MoveCommand mc = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, azDeg, elDeg);
		move(mc);
	}
	
	

	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#index(edu.ucsb.deepspace.Axis)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#index(edu.ucsb.deepspace.Axis)
	 */
	
	
	public void index(final Axis axis) {
		if (scope.isIndexing(axis)) {
			buttonEnabler("indexAz");
			buttonEnabler("indexEl");
			return;
		}
		
		if (!motorCheck(axis)) {
			buttonEnabler("indexAz");
			buttonEnabler("indexEl");
			return;
		}
		exec.submit(new Runnable() {
			
			public void run() {
				reader.readerOnOff(false);
				scope.index(axis);
				buttonEnabler("indexAz");
				buttonEnabler("indexEl");
				reader.readerOnOff(true);
			}
		});
		window.controlMoveButtons(true);
	}

	
	


	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#isMoving()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#isMoving()
	 */
	
	
	public boolean isMoving() {
		switch (stageType) {
			case FTDI:
				return true; //don't care about FTDI right now (5/5/2012, reed)
				//return Math.abs(velocity) <= 1;
			case GALIL:
				return scope.isMoving();
			default:
				return true;
		}
	}

	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#waitWhileExecuting(int)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#waitWhileExecuting(int)
	 */
	
	
	public void waitWhileExecuting(int thread) {
		
		while (position.isThreadEx(thread)) {
			//statusArea("thread" +thread + "is executing\n");
			;
		}
		//statusArea("Thread" + thread +" is  done executing\n");
		//pause(50);
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
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setMinMaxAz(double, double)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setMinMaxAz(double, double)
	 */
	
	
	public void setMinMaxAz(double minAz, double maxAz) {
		this.minAz = minAz;
		this.maxAz = maxAz;
		scope.safety(minAz, maxAz, minEl, maxEl);
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setMinMaxEl(double, double)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setMinMaxEl(double, double)
	 */
	
	
	public void setMinMaxEl(double minEl, double maxEl) {
		this.minEl = minEl;
		this.maxEl = maxEl;
		scope.safety(minAz, maxAz, minEl, maxEl);
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setMaxVelAccAz(double, double)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setMaxVelAccAz(double, double)
	 */
	
	
	public void setMaxVelAccAz(double maxVelAz, double maxAccAz) {
		this.maxVelAz = maxVelAz;
		this.maxAccAz = maxAccAz;
		scope.setAccel(maxAccAz, Axis.AZ);
		scope.setVelocity(maxVelAz, Axis.AZ);
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setMaxVelAccEl(double, double)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setMaxVelAccEl(double, double)
	 */
	
	
	public void setMaxVelAccEl(double maxVelEl, double maxAccEl) {
		this.maxVelEl = maxVelEl;
		this.maxAccEl = maxAccEl;
		scope.setAccel(this.maxEl, Axis.EL);
		scope.setVelocity(maxVelEl, Axis.EL);
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setMaxMoveRel(double, double)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setMaxMoveRel(double, double)
	 */
	
	
	public void setMaxMoveRel(double maxMoveRelAz, double maxMoveRelEl) {
		this.maxMoveRelAz = maxMoveRelAz;
		this.maxMoveRelEl = maxMoveRelEl;
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setRpm(double, double)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setRpm(double, double)
	 */
	
	
	public void setRpm(double azRpm, double elRpm) {
		scope.setSpeedByRpm(azRpm, Axis.AZ);
		scope.setSpeedByRpm(elRpm, Axis.EL);
	}

	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getEncTol()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getEncTol()
	 */
	
	
	public int getEncTol() {return encTol;}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setEncTol(int)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setEncTol(int)
	 */
	
	
	public void setEncTol(int encTol) {
		this.encTol = encTol;
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#status()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#status()
	 */
	
	
	public void status() {
		stageProtocol.initialize();
		System.out.println(stageProtocol.sendRead("XQ #READERI"));
		System.out.println(stageProtocol.read());
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#sendCommand(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#sendCommand(java.lang.String)
	 */
	
	
	public void sendCommand(String command) {
		System.out.println(stageProtocol.sendRead(command));
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#queueSize()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#queueSize()
	 */
	
	
	public void queueSize() {
		System.out.println("stage protocol queue size: " + stageProtocol.queueSize());
		System.out.println("scope protocol queue size: " + scopeProtocol.queueSize());
		System.out.println(readerProtocol.port + " reader protocol queue size: " + readerProtocol.queueSize());
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#readQueue()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#readQueue()
	 */
	
	
	public void readQueue() {
		stageProtocol.test();
		System.out.println("--------");
		scopeProtocol.test();
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#stop(edu.ucsb.deepspace.Axis)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#stop(edu.ucsb.deepspace.Axis)
	 */
	
	
	public void stop(Axis axis) {
		scope.stop(axis);
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#motorToggle(edu.ucsb.deepspace.Axis)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#motorToggle(edu.ucsb.deepspace.Axis)
	 */
	
	
	public void motorToggle(final Axis axis) {
		exec.submit(new Runnable() {
			public void run() {
				scope.motorToggle(axis);
			}
		});
	}

	/**
	 * Gets the position of a certain axis.
	 * @param axisType
	 * @return
	 */
	double encPos(Axis axis) {
		if (position == null) return 0;
		switch (axis) {
			case AZ:
				return position.azPos();
			case EL:
				return position.elPos();
			default:
				return 0;
		}
	}

	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#goToPos(edu.ucsb.deepspace.Coordinate)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#goToPos(edu.ucsb.deepspace.Coordinate)
	 */
	
	
	public void goToPos(Coordinate c) {
		moveAbsolute(c.getAz(), c.getEl());
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setRaDecTracking(double, double)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setRaDecTracking(double, double)
	 */
	
	
	public void setRaDecTracking(double ra, double dec) {
		window.setRaDec(ra, dec);
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setBaseLocation(edu.ucsb.deepspace.LatLongAlt)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setBaseLocation(edu.ucsb.deepspace.LatLongAlt)
	 */
	
	
	public void setBaseLocation(LatLongAlt pos) {
		baseLocation = pos;
		calcAzElToBalloon();
		window.updateBaseBalloonLoc();
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setBalloonLocation(edu.ucsb.deepspace.LatLongAlt)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setBalloonLocation(edu.ucsb.deepspace.LatLongAlt)
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
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getBalloonLocation()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getBalloonLocation()
	 */
	
	
	public LatLongAlt getBalloonLocation() {
		return balloonLocation;
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getBaseLocation()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getBaseLocation()
	 */
	
	
	public LatLongAlt getBaseLocation() {
		return baseLocation;
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#baseLocDisplay()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#baseLocDisplay()
	 */
	
	
	public String baseLocDisplay() {
		return "Base Location\n" + baseLocation.guiString();
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#balloonLocDisplay()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#balloonLocDisplay()
	 */
	
	
	public String balloonLocDisplay() {
		String out = "BalloonLocation\n" + balloonLocation.guiString();
		out += "\nAz to balloon:  " + Formatters.TWO_POINTS_FORCE.format(azToBalloon) + "\n";
		out += "El to balloon:  " + Formatters.TWO_POINTS_FORCE.format(elToBalloon);
		return out;
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#goToBalloon()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#goToBalloon()
	 */
	
	
	public void goToBalloon() {
		moveAbsolute(azToBalloon, elToBalloon);
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#calibrate(edu.ucsb.deepspace.Coordinate)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#calibrate(edu.ucsb.deepspace.Coordinate)
	 */
	
	
	public void calibrate(Coordinate c) {
		scope.calibrate(c);
		System.out.println("az: " + c.getAz());
		System.out.println("el: " + c.getEl());
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#buttonEnabler(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#buttonEnabler(java.lang.String)
	 */
	
	
	public void buttonEnabler(String name) {
		window.buttonEnabler(name);
	}
	/**
	 * Updates Galil's position for the user.
	 * @param data to update with
	 */
	void updatePosition(DataInterface data) {
		if (data.motorState(Axis.AZ) != position.motorState(Axis.AZ) ||
		data.motorState(Axis.EL) != position.motorState(Axis.EL)) {
			window.updateMotorButton(data.motorState(Axis.AZ), Axis.AZ);
			window.updateMotorButton(data.motorState(Axis.EL), Axis.EL);
			
		}
		
		position = data;
		String info = position.info();
		double azRpm = scope.rpm(data.azMaxVel(), Axis.AZ);
		double elRpm = scope.rpm(data.elMaxVel(), Axis.EL);
		info += String.format(Formatters.ACTINFO_FORMAT, "RPM", azRpm, elRpm) + "\n";
		if (window.debug) {
			double azAbsPos = scope.getAbsolutePos(Axis.AZ);
			double elAbsPos = scope.getAbsolutePos(Axis.EL);
			info += String.format(Formatters.ACTINFO_FORMAT, "Abs Pos", azAbsPos, elAbsPos);
		}
		txtPosInfo = info;
		window.updateTxtPosInfo(info);
	}
	
//	void updateVelAcc(String azVel, String azAcc, String elVel, String elAcc) {
//		window.updateVelAcc(azVel, azAcc, elVel, elAcc);
//	}
	

	/**
	 * Sets where an axis wants to go if it were to complete a successful move.
	 * @param deg absolute position it will end up
	 * @param axis
	 */
	void setGoalPos(double deg, Axis axis) {
		window.setGoalPos(Formatters.TWO_POINTS.format(deg), axis);
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#shutdown()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#shutdown()
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
		switch (stageType) {
			case GALIL:
				closeGalil();
				stageProtocol.close();
				break;
			case FTDI:
				break;
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#statusArea(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#statusArea(java.lang.String)
	 */
	
	
	public void statusArea(String message) {
		window.updateStatusArea(message);
	}
	
	/**
	 * Returns true if the motor is on.
	 * @param axis
	 * @return
	 */
	private boolean motorCheck(Axis axis) {
		if (!position.motorState(axis)) {
			statusArea(axis.getFullName() + " motor is off.  Please turn motor on before proceeding.\n");
			return false;
		}
		return true;
	}
	
	boolean validateSpeed(double proposedVel, Axis axis) {
		switch (axis) {
			case AZ:
				if (Math.abs(proposedVel) <= maxVelAz) {
					return true;
				}
				break;
			case EL:
				if (Math.abs(proposedVel) <= maxVelEl) {
					return true;
				}
				break;
			default:
				assert false; //This is only reached if a new axis is added.
		}
		return false;
	}
	
	boolean validateAccel(double proposedAcc, Axis axis) {
		switch (axis) {
			case AZ:
				if (Math.abs(proposedAcc) <= maxAccAz) {
					return true;
				}
				break;
			case EL:
				if (Math.abs(proposedAcc) <= maxAccEl) {
					return true;
				}
				break;
			default:
				assert false; //This is only reached if a new axis is added.
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getContinousScanOn()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getContinousScanOn()
	 */
	
	
	public Boolean getContinousScanOn() {
		return continousScanOn;
	}

	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setContinousScanOn(java.lang.Boolean)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setContinousScanOn(java.lang.Boolean)
	 */
	
	
	public void setContinousScanOn(Boolean continousScanOn) {
		this.continousScanOn = continousScanOn;
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#RaOn()
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#RaOn()
	 */
	
	
	public Boolean RaOn() {
		return raOn;
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setRaOn(java.lang.Boolean)
	 */
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#setRaOn(java.lang.Boolean)
	 */
	
	
	public void setRaOn(Boolean raOn){
		this.raOn = raOn;
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getTxtPosInfo()
	 */
	
	public String getTxtPosInfo(){
		
		return txtPosInfo;
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getMinAz()
	 */
	
	public double getMinAz(){
		return this.minAz;
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getMaxAz()
	 */
	
	public double getMaxAz(){
		return this.maxAz;
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getMinEl()
	 */
	
	public double getMinEl(){
		return this.getMinEl();
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.deepspace.StageInterface#getMaxEl()
	 */
	
	public double getMaxEl(){
		return this.getMaxEl();
	}
	public int getMaxVel(Axis axis){
		switch (axis){
		case AZ:
			return maxAzVel;
			
		case EL:
			return maxElVel;
		default:
			statusArea("SOMETHING WENT HORRIBLY WRONG");
			return 0;
			
		}
	}
	
	
	
	



}