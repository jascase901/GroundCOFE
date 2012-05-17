package edu.ucsb.deepspace;

/**
 * Thread that continually monitors the receive queue of the FTDI device.  <P>
 * Purpose is to read the messages that the az/el controller sends back.  Primarily used to parse the status string
 * that is sent at 1Hz.
 *
 */
public class ReaderFTDI extends Thread implements ReaderInterface {
	/** Thread runs while flag is true.*/
	boolean flag = true;
	/** Instance of FTDI so that FTDIReader can get access to the receive queue. */
	private final CommFTDI protocol;
	private Stage stage = null;
	
	String message = "";
	
	public ReaderFTDI(Stage stage) {
		this.setDaemon(true);
		this.protocol = CommFTDI.getInstance();
		this.stage = stage;
	}
	private boolean indexElStart = false, indexAzStart = false;
	
	@SuppressWarnings("unused")
	@Override
	public void run() {
		while (flag) { //keep looping while flag is true (runs until stop2() is called)
			if (protocol.queueSize() > 0) { //only continue if there is stuff in the receive queue
				String messageFromFTDI = protocol.read(); //read from queue
				if (messageFromFTDI.equals("ft232 is null.  Impossible to read.\n")) { //if ft232 is null, nothing will ever
					flag = false;                                          //be read from the queue, so break the loop now
					break;
				}
				message = message + messageFromFTDI; //message is previous message + the new stuff
				
				/*if message doesn't have the string "eol", then we've only read a fragment of a complete message
				 *  in this case, don't do anything else and continue the loop
				 */
				if (message.contains("eol")) { 
					/*split message up, delimited by "eol"
					 * example:   message1eolmessage2 would be split into "message1" and "message2"
					 */
					String[] messages = message.split("eol");
					
					String[] splitStatus = new String[0]; //initialize splitStatus to a 0 length array
					
					/*
					 * loop through messages, try to find one that starts with "GSt".  this is the status message that we need
					 */
					for (String s : messages) {
						if (s.startsWith("GSt")) {
							splitStatus = s.split(" ");
						}
						else if (s.contains("Indexing Axis 1 (el)")) { //el indexing procedure has been started
							indexElStart = true;
						}
						else if (s.contains("Indexing Axis 0 (az)")) { //az indexing procedure has been started
							indexAzStart = true;
						}
						else if (s.contains("Index Found, Loop Exited")) { //indexing has ended
							if (indexElStart) {
								stage.indexingDone(ActInterface.axisType.EL);
								indexElStart = false;
							}
							else if (indexAzStart) {
								stage.indexingDone(ActInterface.axisType.AZ);
								indexAzStart = false;
							}
						}
					}
					
					/*
					 * if length is 1, then there is only one message (the status message)
					 * OR if it ends in eol, we have no message fragments (message1eolmessage2eol)
					 * we don't need to store anything for the next iteration of the loop
					 */
					if (messages.length == 1 || message.endsWith("eol")) {
						message = "";
					}
					/*
					 * in this case, we have something like this: message1eolmessage2eolFRAGMENT
					 * we want to store FRAGMENT for next iteration, in the hope that the next protocol.read()
					 * will complete the message
					 */
					else if (messages.length > 1) {
						message = messages[messages.length - 1];
					}
					
					/*
					 * Parse the status message.  Here is an example status message.
					 * This could be changed in future versions of Connor's code.
					 * GSt AzGoal 89 ElGoal 1654 AzNow 89 ElNow 1654 AzEncPosition -3 ElEncPosition 139 AzEncoderInd 0 stepperAtInd -6 ElEncoderInd 148 stepperAtIndex 1634 Velocity 1
					 */
					if (splitStatus.length == 23) {
						try {							
							int azStepperGoal = Integer.parseInt(splitStatus[2]);
							int azStepperNow = Integer.parseInt(splitStatus[6]);
							int azEncoderPos = Integer.parseInt(splitStatus[10]);
							int azEncoderAtIndex = Integer.parseInt(splitStatus[14]);
							int azStepperAtIndex = Integer.parseInt(splitStatus[16]);
							//TODO ActStatus azStatus = new ActStatus(azStepperGoal, azStepperNow, azEncoderPos, azEncoderAtIndex, azStepperAtIndex);
							
							int elStepperGoal = Integer.parseInt(splitStatus[4]);
							int elStepperNow = Integer.parseInt(splitStatus[8]);
							int elEncoderPos = Integer.parseInt(splitStatus[12]);
							//System.out.println(splitStatus[12]);
							int elEncoderAtIndex = Integer.parseInt(splitStatus[18]);
							int elStepperAtIndex = Integer.parseInt(splitStatus[20]);
							//TODO ActStatus elStatus = new ActStatus(elStepperGoal, elStepperNow, elEncoderPos, elEncoderAtIndex, elStepperAtIndex);
							
							int velocity = Integer.parseInt(splitStatus[22]);
							if (stage == null) System.out.println("stage is null");
							//TODO stage.setStatuses(azStatus, elStatus, velocity);
							
							//TODO BalloonTrackingWindow.updateTxtPosInfo(azStatus, elStatus, velocity); //so that the GUI will update
							
						} catch (NumberFormatException e) {
							System.out.println(messageFromFTDI);
							System.out.println(message);
							
							//TODO BalloonTrackingWindow.updateTxtStatusArea("Error parsing status string from sate controller.");
							//BalloonTrackingWindow.updateTxtStatusArea("Specifically, expected a number but got something else instead.");
							//BalloonTrackingWindow.updateTxtStatusArea("Error from:  FTDIReader.java");
							e.printStackTrace();
						}
					}
				}
			}
			/*
			 * sleep for 100ms because we don't want to unnecessarily check the queue
			 * this wouldn't be bad, it'd just waste CPU
			 */
			//TODO BalloonTrackingWindow.updateTxtAzElRaDec(stage.currentAzDeg(), stage.currentElDeg());
			pause(100);
		}
	}
	private void pause(int msToPause) {
		try {
			Thread.sleep(msToPause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * break the while loop to shut down the thread
	 */
	public void stop2() {
		flag = false;
	}
	@Override
	public void togglePauseFlag() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void readerOnOff(boolean onOff) {
		// TODO Auto-generated method stub
		
	}

}