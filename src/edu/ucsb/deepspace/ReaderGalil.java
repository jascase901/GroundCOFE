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
		protocol = new CommGalil(4444);
	}
	
	private String tellPos = "TP";
	private String tellVel = "TV";
	DataGalil data;
	private boolean flag2 = true;
	
	public void togglePauseFlag() {
		this.flag2 = !flag2;
	}
	
	public void readerOnOff(boolean onOff) {
		this.flag2 = onOff;
	}
	
	public void run() {
		while (flag) {
			if (flag2) {
				//System.out.println("\n\n");
				String azPos = protocol.sendRead(tellPos + azAxis);
				String azVel = protocol.sendRead(tellVel + azAxis);
				String azJg = protocol.sendRead("JG?");
				String azAc = protocol.sendRead("AC?");
				
				String elPos = protocol.sendRead(tellPos + elAxis);
				String elVel = protocol.sendRead(tellVel + elAxis);
				String elJg = protocol.sendRead("JG,?");
				String elAc = protocol.sendRead("AC,?");
				//System.out.println("\n\n");
				
				data = new DataGalil();
				data.make(azPos, azVel, azJg, azAc, axisType.AZ);
				data.make(elPos, elVel, elJg, elAc, axisType.EL);
				stage.updatePosition(data);
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