package edu.ucsb.deepspace;

import edu.ucsb.deepspace.ActInterface.axisType;

public class ReaderGalil extends Thread implements ReaderInterface {
	
	private boolean flag = true;
	private CommGalil protocol;
	private final Stage stage;
	private String azAxis = "";
	private String elAxis = "";
	
	public ReaderGalil(Stage stage) {
		this.setDaemon(true);
		this.stage = stage;
		this.setName("ReaderGalil");
		azAxis = stage.axisName(axisType.AZ);
		elAxis = stage.axisName(axisType.EL);
		protocol = new CommGalil(13358);
	}
	
	private String tellPos = "TP";
	private String tellVel = "TV";
	DataGalil data;
	private boolean pauseFlag = false;
	
	public void togglePauseFlag() {
		System.out.println("hi");
		this.pauseFlag = !pauseFlag;
	}
	
	public void run() {
		pause(1000);
		while (flag) {
			if (!pauseFlag) {
				String azPos = protocol.sendRead(tellPos + azAxis);
				String azVel = protocol.sendRead(tellVel + azAxis);
				String elPos = protocol.sendRead(tellPos + elAxis);
				String elVel = protocol.sendRead(tellVel + elAxis);
				
				//if (elPos == null || elPos == "") {elPos="0";}
				
				data = new DataGalil();
				data.makeAz(azPos, azVel);
				data.makeEl(elPos, elVel);
				stage.updatePosition(data);
				
				azVel = protocol.sendRead("JG?");
				String azAcc = protocol.sendRead("AC?");
				elVel = protocol.sendRead("JG,?");
				String elAcc = protocol.sendRead("AC,?");
				
				stage.updateVelAcc(azVel, azAcc, elVel, elAcc);
			}
			pause(1000);
		}
		protocol.close();
	}
	
	public void stop2() {
		flag = false;
	}
	
	private void pause(int msToPause) {
		try {
			Thread.sleep(msToPause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}