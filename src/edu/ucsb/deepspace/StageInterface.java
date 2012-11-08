package edu.ucsb.deepspace;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.ucsb.deepspace.MoveCommand.MoveType;
import edu.ucsb.deepspace.Stage.StageTypes;

public interface StageInterface {

	public abstract StageTypes getType();

	/**
	 * Creates all the CommGalil, ActGalil, etc. objects and starts the connection. 
	 * @param window
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public abstract void initialize(Ui window) throws FileNotFoundException,
			IOException;

	public abstract void loadSafety(double minAz, double maxAz, double minEl,
			double maxEl);

	public abstract void confirmCommConnection();

	/**
	 * Toggles the reader on or off.
	 */
	public abstract void toggleReader();

	public abstract void loadScripts();

	public abstract void refreshScripts();

	public abstract void refreshScriptWindow();

	/**
	 * Points to a ra dec position every amount of time specified until told to stop.
	 * @param ra
	 * @param dec
	 */
	public abstract void startRaDecTracking(double ra, double dec);

	/**
	 * Stops ra dec tracking.
	 */
	public abstract void stopRaDecTracking();

	/**
	 * updates scan command min and max using ra/dec conversions
	 * @param sc
	 * @param axis
	 */
	public abstract void updateScanCommand(ScanCommand sc, Axis axis);

	/**
	 * Moves over a specified min and max az and el a specified or continuous number of times. 
	 * 
	 * @param azSc
	 * @param elSc
	 */

	//TODO FIGURE OUT A WAY TO MAKE THIS PRETTY
	public abstract boolean canScan(ScanCommand azSc, ScanCommand elSc,
			double time);

	public abstract boolean test();

	public abstract void Spin();

	public abstract void startScanning(ScanCommand azSc, ScanCommand elSc,
			boolean fraster);

	/**
	 * Stops movement caused by scanning.
	 * 
	 */
	public abstract void stopScanning();

	//TODO Can Infinate loop
	public abstract double getScanTime(ScanCommand sc, Axis axis);

	public abstract void move(MoveCommand mc);

	public abstract void setVelocity(double vel, Axis axis);

	public abstract void moveRelative(Double amount, Axis axis, MoveType type);

	/**
	 * Moves an axis to its default position.
	 * @param type of axis
	 */
	public abstract void index(Axis axis);

	/**
	 * Returns true if something is moving, false if not.
	 * @return boolean
	 */
	public abstract boolean isMoving();

	/**
	 * While isMoving() is true, sleeps for 100ms.
	 */
	public abstract void waitWhileExecuting(int thread);

	/**
	 * Sets Galil's min and max az value.
	 * @param minAz
	 * @param maxAz
	 */
	public abstract void setMinMaxAz(double minAz, double maxAz);

	/**
	 * Sets Galil's min and max el value.
	 * @param minEl
	 * @param maxEl
	 */
	public abstract void setMinMaxEl(double minEl, double maxEl);

	public abstract void setMaxVelAccAz(double maxVelAz, double maxAccAz);

	public abstract void setMaxVelAccEl(double maxVelEl, double maxAccEl);

	public abstract void setMaxMoveRel(double maxMoveRelAz, double maxMoveRelEl);

	public abstract void setRpm(double azRpm, double elRpm);

	public abstract int getEncTol();

	/**
	 * Sets some value of Galil, I'm not sure which.
	 * @param encTol
	 */
	public abstract void setEncTol(int encTol);

	/** 
	 * Gets and updates the velocity and position of the az axis.
	 */
	public abstract void status();

	/**
	 * Sends a message to Galil and prints its reply.
	 * @param command to send
	 */
	public abstract void sendCommand(String command);

	/**
	 * Gets and prints the amount of bytes waiting to be read from Galil.
	 */
	public abstract void queueSize();

	/**
	 * Reads off what Galil is waiting to send.
	 */
	public abstract void readQueue();

	/**
	 * Stops the desired axis.
	 * @param axis az or el
	 */
	public abstract void stop(Axis axis);

	/**
	 * Toggles the motor on or off.
	 * @param axis az or el
	 */
	public abstract void motorToggle(Axis axis);

	/**
	 * Moves Galil to a specified coordinate.
	 * @param c coordinate
	 */
	public abstract void goToPos(Coordinate c);

	/**
	 * Sets the coordinate for ra dec tracking.
	 * @param ra
	 * @param dec
	 */
	public abstract void setRaDecTracking(double ra, double dec);

	/**
	 * Sets the base location.
	 * @param pos in latitude, longitude, and position
	 */
	public abstract void setBaseLocation(LatLongAlt pos);

	/**
	 * Sets the balloon location.
	 * @param pos in latitude, longitude, and position
	 */
	public abstract void setBalloonLocation(LatLongAlt pos);

	/**
	 * Returns the balloon location.
	 * @return
	 */
	public abstract LatLongAlt getBalloonLocation();

	/**
	 * Return the base location.
	 * @return
	 */
	public abstract LatLongAlt getBaseLocation();

	/**
	 * The string that displays the base location in the program.
	 * @return
	 */
	public abstract String baseLocDisplay();

	/**
	 * The string that displays the balloon location in the program.
	 * @return
	 */
	public abstract String balloonLocDisplay();

	/**
	 * Moves Galil to the balloon position.
	 */
	public abstract void goToBalloon();

	/**
	 * Sets a new coordinate as the relative (0,0).
	 * @param c coordinate
	 */
	public abstract void calibrate(Coordinate c);

	/**
	 * Convenience method for enabling a method.
	 * @param name
	 */
	public abstract void buttonEnabler(String name);

	/**
	 * Closes the program and saves all settings.
	 */
	public abstract void shutdown();

	public abstract void statusArea(String message);

	public abstract Boolean getContinousScanOn();

	public abstract void setContinousScanOn(Boolean continousScanOn);

	public abstract Boolean RaOn();

	public abstract void setRaOn(Boolean raOn);

	public abstract String getTxtPosInfo();

	public abstract double getMinAz();

	public abstract double getMaxAz();

	public abstract double getMaxEl();

	public abstract double getMinEl();
	public abstract int getMaxVel(Axis axis);

	

}