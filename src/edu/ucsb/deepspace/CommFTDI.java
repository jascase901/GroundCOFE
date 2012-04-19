package edu.ucsb.deepspace;

import java.util.Arrays;
import java.util.List;

import com.ftdi.FTD2XXException;
import com.ftdi.FTDevice;
import com.ftdi.Parity;
import com.ftdi.StopBits;
import com.ftdi.WordLength;


@SuppressWarnings("unused")
public class CommFTDI implements CommInterface {
	
	private FTDevice ft232; //object representing the FTDI chip
	public boolean errorFlag = false; //if an error is thrown during initialization, set this to true
	public boolean nullFlag = false;  //set to true if ft232 is null
	
	/** Serial number of FTDI device */
	private String serialNumber = "A6008nKa";
	
	/** Singleton design pattern.  Only need one FTDI object. */
	private static final CommFTDI INSTANCE = new CommFTDI();
	
	/** Return our singleton, rather than exposing the constructor to the public. */
	public static CommFTDI getInstance() {return INSTANCE;}
	
	/** Private constructor prevents more than one instance of this class from being created. */
	private CommFTDI() {
//		List<FTDevice> fTDevices = null;
//		try {
//			fTDevices = FTDevice.getDevices();
//		} catch (FTD2XXException e) {
//			e.printStackTrace();
//		}
//		for (FTDevice fTDevice : fTDevices) {
//		    if (fTDevice.getDevSerialNumber().equals(serialNumber)) {
//		        ft232 = fTDevice;
//		    }
//		 }
		
		try {
			ft232 = FTDevice.getDevicesBySerialNumber(serialNumber).get(0);
			status("Found device with serial number = " + serialNumber);
		} catch (FTD2XXException e) {
			dealWithFTDIexception(e, true);
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) { //occurs if no devices were found, resulting in a 0 length array
			status(e.toString());
			status("No devices with serial number: " + serialNumber + " were found.\n");
		}
		
		if (ft232 == null) { //this will happen if no device was found in the try/catch block
			nullFlag = true; //now everyone knows that ft232 is null
			status("ft232 is null.");
			status("This means a connection could not be established to the FTDI device.\n");	
		}
		
		try {
			if (!nullFlag) { //if device isn't null, open it and configure it
				ft232.open();
				status("Device opened.");
				ft232.setBaudRate(9600); //ask Connor why 9600
				status("Baud rate set.");
				ft232.setDataCharacteristics(WordLength.BITS_8, StopBits.STOP_BITS_1, Parity.PARITY_NONE); //ask Connor why these values
				status("Word length set.");
				status("FTDI device ready for use.\n");
			}
		} catch (FTD2XXException e) {
			errorFlag = true; //if an error occurs when opening/configuring the device
			dealWithFTDIexception(e, true);
			e.printStackTrace();
		}
	}
	
	/**
	 * closes the device
	 * @return a string stating the device was closed or that ft232 is null
	 */
	public void close() {
		if (nullFlag) return; //if device is null, stop method now
		try {
			ft232.close();
			System.out.println("FTDI connection closed.");
		} catch (FTD2XXException e) {
			errorFlag = true;
			dealWithFTDIexception(e, false);
			status("Error from:  FTDI.close()\n");
			e.printStackTrace();
		}
	}
	
	/**
	 * write data to the device
	 * @param data byte array to write to the device
	 * @return the number of bytes written
	 */
	public void write(byte[] data) {
		if (nullFlag) return; //if device is null, stop method now
		try {
			//System.out.println(Arrays.toString(data));
			ft232.write(data); //write bytes to device
		} catch (FTD2XXException e) {
			errorFlag = true;
			dealWithFTDIexception(e, false);
			status("Error from:  FTDI.write()\n");
			e.printStackTrace();
		}
	}
	
	/**
	 * read data from the FTDI device
	 * @return
	 */
	public String read() {
		if (nullFlag) return "ft232 is null.  Impossible to read.\n"; //if device is null, stop method now
		byte[] read = new byte[0];
		try {
			if (ft232.getQueueStatus() > 0) //check to make sure there is data in the queue
				read = ft232.read(ft232.getQueueStatus()); //read X number of bytes into the array, 
		} catch (FTD2XXException e) {                      //where X is the size of the queue
			errorFlag = true;
			dealWithFTDIexception(e, false);
			status("Error from:  FTDI.read()\n");
			e.printStackTrace();
		}
		return new String(read); //convert the byte array into a string
	}
	
	/**
	 * to see how large the receive queue is
	 * @return number of bytes in receive queue
	 */
	public int queueSize() {
		if (nullFlag) return 0; //if device is null, stop method now
		if (errorFlag) return 0;
		int queueStatus = 0;
		try {
			queueStatus = ft232.getQueueStatus();
		} catch (FTD2XXException e) {
			errorFlag = true;
			dealWithFTDIexception(e, false);
			status("Error from:  FTDI.queueStatus()\n");
			e.printStackTrace();
		}
		return queueStatus;
	}
	
	private void dealWithFTDIexception(FTD2XXException e, boolean noMore) {
		String extra = "\n";
		if (noMore) extra = "";
		status(e.toString());
		status("Something went wrong while trying to talk to the FTDI chip.");
		status("This could mean that the device was never properly initialized, something " +
				"physically went wrong after the program started, or that something exceptional happened." + extra);
	}
	
	private void status(String s) {
		//TODO BalloonTrackingWindow.updateTxtStatusArea(s);
	}
}