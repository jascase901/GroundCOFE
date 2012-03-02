package edu.ucsb.deepspace;

public class ReaderGalil extends Thread implements ReaderInterface {
	
	private boolean flag = true;
	private CommGalil protocol;
	private final Stage stage;
	private String azAxis = "";
	private String elAxis = "";
	
	public ReaderGalil(Stage stage) {
		this.setDaemon(true);
		protocol = CommGalil.getInstance();
		this.stage = stage;
		
		//TODO methods that fetch these from stage
		//for now just default them
		//azAxis = stage.getAzAxis();
		//elAxis = stage.getElAxis();
		azAxis = "A";
		elAxis = "B";
	}
	
	private String tellPos = "TP";
	private String tellVel = "TV";
	DataGalil data;
	
	public void run() {
		while (flag) {
			String azPos = protocol.sendRead(tellPos + azAxis);
			String azVel = protocol.sendRead(tellVel + azAxis);

			data = new DataGalil();
			data.makeAz(azPos, azVel);
			stage.updatePosition(data);


			//String elPos = protocol.sendRead(tellPos + elAxis);
			
			pause(1000);
		}
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