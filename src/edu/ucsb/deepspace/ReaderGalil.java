package edu.ucsb.deepspace;


public class ReaderGalil extends Thread implements ReaderInterface {
	
	private boolean flag = true, flag2 = false;
	private CommGalil protocol;
	private final Stage stage;
	private DataGalil data;
	
	public ReaderGalil(Stage stage, CommGalil protocol) {
		this.setDaemon(true);
		this.stage = stage;
		this.setName("ReaderGalil");
		this.protocol = protocol;
	}
	
	public void readerOnOff(boolean onOff) {
		this.flag2 = onOff;
	}
	
	//TODO make use of motor data and motion data
	public void run() {
		while (flag) {
			if (flag2) {		
				protocol.initialize();
				protocol.sendRead("XQ #READERI,3");
				String info = protocol.read();
				//System.out.println(info);
				String[] temp = info.split(" ");
				if (temp.length>1){
				String azPos = temp[1];
				String azVel = temp[2];
				String azJg = temp[3];
				String azAc = temp[4];
				
				String elPos = temp[5];
				String elVel = temp[6];
				String elJg = temp[7];
				String elAc = temp[8];
				
				String azMotor = temp[9];
				String elMotor = temp[10];
				
				String azMoving = temp[11];
				String elMoving = temp[12];
				
				String hqThread0 = temp[13];
				String hqThread1 = temp[14];
				String hqThread2 = temp[15];
				
				
				data = new DataGalil();
				data.make(azPos, azVel, azJg, azAc, azMotor, azMoving, Axis.AZ);
				data.make(elPos, elVel, elJg, elAc, elMotor, elMoving, Axis.EL);
				data.make(hqThread0, hqThread1, hqThread2);
				
				stage.updatePosition(data);
				}
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