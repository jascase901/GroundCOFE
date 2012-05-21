package edu.ucsb.deepspace;

import edu.ucsb.deepspace.ActInterface.axisType;

public class ReaderGalil extends Thread implements ReaderInterface {
	
	private boolean flag = true;
	private CommGalil protocol;
	private final Stage stage;
	
	public ReaderGalil(Stage stage) {
		this.setDaemon(true);
		this.stage = stage;
		this.setName("ReaderGalil");
		protocol = new CommGalil(4444);
	}
	
	DataGalil data;
	private boolean flag2 = true;
	
//	public void togglePauseFlag() {
//		this.flag2 = !flag2;
//	}
	
	public void readerOnOff(boolean onOff) {
		this.flag2 = onOff;
	}
	
	public void run() {
		while (flag) {
			if (flag2) {		
				protocol.initialize();
				protocol.sendRead("XQ #READERI,3");
				String info = protocol.read();
				System.out.println(info);
				String[] temp = info.split(" ");
				
				String azPos = temp[1];
				String azVel = temp[2];
				String azJg = temp[3];
				String azAc = temp[4];
				
				String elPos = temp[5];
				String elVel = temp[6];
				String elJg = temp[7];
				String elAc = temp[8];
				
				
				
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