package edu.ucsb.deepspace.gui;



import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.ucsb.deepspace.LatLongAlt;
import edu.ucsb.deepspace.Stage;

public class LatLongAltWindow {
	private Shell shell;
	private Button action;
	
	private Text latDeg, latMin, latSec;
	private Text longDeg, longMin, longSec;
	private Text altText;
	private Label lat, lon, deg, min, sec, alt;

	
	private boolean minsec;
	private LatLongAlt previous;
	private final String type;
	private final Stage stage;

	public LatLongAltWindow(boolean minsec, String type, Stage stage) {
		shell = new Shell(Display.getDefault(), SWT.CLOSE | SWT.TITLE | SWT.MIN);
		shell.setSize(228, 259);
		this.minsec = minsec;
		if (type.equals("baseloc")) {
			this.previous = stage.getBaseLocation();
		}
		else {
			this.previous = stage.getBalloonLocation();
		}
		this.type = type;
		this.stage = stage;
		
		initGUI();
		shell.open();
        while (!shell.isDisposed()) {
            if (!Display.getDefault().readAndDispatch())
            	Display.getDefault().sleep();
        }
	}
	
	public void close() {
		stage.buttonEnabler(type);
		shell.close();
	}
	
	private void initGUI() {
		shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				stage.buttonEnabler(type);
			}
		});
		
		latDeg = new Text(shell, SWT.BORDER);
		latDeg.setBounds(53, 112, 50, 18);
		latDeg.setText(String.valueOf(previous.getLatitude()));
		latDeg.setMessage("String.valueOf(previous.getLatitude())");
		latDeg.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, latDeg,minsec);
			}});
		
	
		
		latMin = new Text(shell, SWT.BORDER);
		latMin.setBounds(107, 112, 50, 18);
		latMin.setMessage("");
		latMin.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, latMin,minsec);
			}});
		
		
		latSec = new Text(shell, SWT.BORDER);
		latSec.setBounds(160, 112, 50, 18);
		latSec.setMessage("");
		latSec.setVisible(minsec);
		latSec.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, latSec,minsec);
			}});
		
		
		longDeg = new Text(shell, SWT.BORDER);
		longDeg.setBounds(53, 133, 50, 18);
		longDeg.setText(String.valueOf(previous.getLongitude()));
		longDeg.setMessage(String.valueOf(previous.getLongitude()));
		longDeg.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, longDeg,minsec);
			}});

		
		longMin = new Text(shell, SWT.BORDER);
		longMin.setBounds(107, 133, 50, 18);
		longMin.setMessage("");
		
		longMin.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, longMin,minsec);
			}});
		
		
		
		longSec = new Text(shell, SWT.BORDER);
		longSec.setBounds(160, 133, 50, 18);
		longSec.setMessage("");
		longSec.setVisible(minsec);
		longSec.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, latSec,minsec);
			}});
		
		
		alt = new Label(shell, SWT.NONE);
		alt.setBounds(20, 174, 16, 13);
		alt.setText("alt:");
		
		
		altText = new Text(shell, SWT.BORDER);
		altText.setBounds(53, 171, 75, 18);
		altText.setText("0.0");
		altText.setMessage("0.0");
		altText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, altText,minsec);
			}});
		
		action = new Button(shell, SWT.PUSH | SWT.CENTER);
		action.setText("Set Balloon Position");
		if (type.equals("baseloc")) {
			action.setText("Set Base Position");
		}
		else if (type.equals("balloonloc")) {
			action.setText("Set Balloon Position");
		}
		action.setBounds(51, 195, 103, 30);
		action.setEnabled(false);
		action.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent evt) {
				LatLongAlt pos = null;
				
				double latDegVal = Double.parseDouble(latDeg.getText());
				double latMinVal = Double.parseDouble(latMin.getText());
				double longDegVal = Double.parseDouble(longDeg.getText());
				double longMinVal = Double.parseDouble(longMin.getText());
				double altVal = Double.parseDouble(altText.getText());
				
				double latMod = Math.signum(latDegVal);
				double lonMod = Math.signum(longDegVal);
				
				double lat = latDegVal + latMod*latMinVal/60d;
				double lon = longDegVal + lonMod*longMinVal/60d;
				
				if (minsec) {
					double latSecVal = Double.parseDouble(latSec.getText());
					double longSecVal = Double.parseDouble(longSec.getText());
					lat += latMod*latSecVal/3600d;
					lon += lonMod*longSecVal/3600d;
				}
				pos = new LatLongAlt(lat, lon, altVal);
				
				
				if (type.equals("baseloc")) {
					stage.setBaseLocation(pos);
				}
				else if (type.equals("balloonloc")) {
					stage.setBalloonLocation(pos);
				}
				close();
			}
		});	
		
		deg = new Label(shell, SWT.NONE);
		deg.setText("deg");
		deg.setBounds(63, 93, 37, 13);
		
		sec = new Label(shell, SWT.NONE);
		sec.setText("sec");
		sec.setBounds(170, 93, 37, 13);
		sec.setVisible(minsec);
		
		min = new Label(shell, SWT.NONE);
		min.setText("min");
		min.setBounds(117, 93, 37, 13);
		
		lat = new Label(shell, SWT.NONE);
		lat.setText("lat:");
		lat.setBounds(20, 112, 37, 13);
		
		lon = new Label(shell, SWT.NONE);
		lon.setText("long:");
		lon.setBounds(20, 133, 37, 13);
		
	
		
		Label lblNewLabel = new Label(shell, SWT.WRAP);
		lblNewLabel.setBounds(10, 10, 188, 77);
		lblNewLabel.setText("latitude is between -90 and 90 degrees.  longitude is between -180 and 180.  east is + and west is -    press enter to move to the next field");
		
		Label lblkm = new Label(shell, SWT.NONE);
		lblkm.setBounds(134, 174, 23, 13);
		lblkm.setText("(km)");
		
	}
	//Enables calibrate if all the fields are filled.
		public void checkHandler(ModifyEvent evt, Text text, boolean minOrSecFlag){
			
		
			
			boolean min_sec_check = false;
			if (minOrSecFlag){
				if(latSec.getText().equals("")||longSec.getText().equals("")){
					min_sec_check = true;
				}
		
			}
			if (latDeg.getText().equals("") || latMin.getText().equals("") || longDeg.getText().equals("")
					||altText.getText().equals("") || longMin.getText().equals("") || longDeg.getText().equals("") || min_sec_check){
				action.setEnabled(false);
				
			}
			else{
				action.setEnabled(true);
			}
			
			
		}
	@SuppressWarnings("unused")
	public void handler(KeyEvent evt, Text text, boolean minOrSecFlag) {
		double rangeMin = 0;
		double rangeMax = 0;
		if (minOrSecFlag) {
			rangeMin = 0;
			rangeMax = 60;
		}
		else {
			rangeMin = -180;
			rangeMax = 180;
		}
	
		
	}
}
