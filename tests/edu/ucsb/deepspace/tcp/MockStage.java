package edu.ucsb.deepspace.tcp;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.ucsb.deepspace.Axis;
import edu.ucsb.deepspace.Coordinate;
import edu.ucsb.deepspace.LatLongAlt;
import edu.ucsb.deepspace.MoveCommand;
import edu.ucsb.deepspace.MoveCommand.MoveType;
import edu.ucsb.deepspace.ScanCommand;
import edu.ucsb.deepspace.Stage.StageTypes;
import edu.ucsb.deepspace.StageInterface;
import edu.ucsb.deepspace.Ui;

public class MockStage implements StageInterface{

	@Override
	public StageTypes getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(Ui window) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadSafety(double minAz, double maxAz, double minEl,
			double maxEl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void confirmCommConnection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toggleReader() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadScripts() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshScripts() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshScriptWindow() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startRaDecTracking(double ra, double dec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopRaDecTracking() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateScanCommand(ScanCommand sc, Axis axis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canScan(ScanCommand azSc, ScanCommand elSc, double time) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean test() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void Spin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startScanning(ScanCommand azSc, ScanCommand elSc,
			boolean fraster) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopScanning() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getScanTime(ScanCommand sc, Axis axis) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void move(MoveCommand mc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVelocity(double vel, Axis axis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveRelative(Double amount, Axis axis, MoveType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void index(Axis axis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isMoving() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void waitWhileExecuting(int thread) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMinMaxAz(double minAz, double maxAz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMinMaxEl(double minEl, double maxEl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaxVelAccAz(double maxVelAz, double maxAccAz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaxVelAccEl(double maxVelEl, double maxAccEl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaxMoveRel(double maxMoveRelAz, double maxMoveRelEl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRpm(double azRpm, double elRpm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEncTol() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setEncTol(int encTol) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void status() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendCommand(String command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queueSize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readQueue() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop(Axis axis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void motorToggle(Axis axis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void goToPos(Coordinate c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRaDecTracking(double ra, double dec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBaseLocation(LatLongAlt pos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBalloonLocation(LatLongAlt pos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LatLongAlt getBalloonLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LatLongAlt getBaseLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String baseLocDisplay() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String balloonLocDisplay() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void goToBalloon() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void calibrate(Coordinate c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buttonEnabler(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void statusArea(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean getContinousScanOn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContinousScanOn(Boolean continousScanOn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean RaOn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRaOn(Boolean raOn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTxtPosInfo() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public double getMinAz() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaxAz() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaxEl() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinEl() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxVel(Axis axis) {
		// TODO Auto-generated method stub
		return 0;
	}

}
