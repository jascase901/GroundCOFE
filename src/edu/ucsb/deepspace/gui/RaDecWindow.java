package edu.ucsb.deepspace.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.ucsb.deepspace.Coordinate;
import edu.ucsb.deepspace.LatLongAlt;
import edu.ucsb.deepspace.Stage;

public class RaDecWindow {
	
	private Shell shell;
	private Button action, btnGO;
	
	private Text raHour, raMin, raSec;
	private Text decDeg, decMin, decSec;
	private Label dec, min, ra, sec, hourdeg, lblDirections;
	private boolean minsec;
	private LatLongAlt baseLocation;
	private double az = 0, el = 0, raVal = 0, decVal = 0;
	private Text txtAz;
	private Text txtEl;
	private String type = "";
	private final Stage stage;

	public RaDecWindow(boolean minsec, String type, Stage stage) {
		shell = new Shell(Display.getDefault(), SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX);
		shell.setSize(213, 294);
		this.minsec = minsec;
		this.baseLocation = stage.getBaseLocation();
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
		
		raHour = new Text(shell, SWT.BORDER);
		raHour.setBounds(53, 89, 37, 18);
		raHour.setMessage("");
		raHour.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, raHour,minsec);
			}
		});
		
		raMin = new Text(shell, SWT.BORDER);
		raMin.setBounds(96, 89, 37, 18);
		raMin.setMessage("");
		raMin.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, raMin,minsec);
			}
		});
		
		raSec = new Text(shell, SWT.BORDER);
		raSec.setBounds(139, 89, 37, 18);
		raSec.setMessage("");
		raSec.setVisible(minsec);
		raSec.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, raSec,minsec);
			}
		});
		
		decDeg = new Text(shell, SWT.BORDER);
		decDeg.setBounds(53, 110, 37, 18);
		decDeg.setMessage("");
		decDeg.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, decDeg,minsec);
			}
		});
		
		decMin = new Text(shell, SWT.BORDER);
		decMin.setBounds(96, 110, 37, 18);
		decMin.setMessage("");
		decMin.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, decMin,minsec);
			}
		});
		
		decSec = new Text(shell, SWT.BORDER);
		decSec.setBounds(139, 110, 37, 18);
		decSec.setMessage("");
		decSec.setVisible(minsec);
		decSec.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent evt1) {
				checkHandler(evt1, decSec,minsec);
			}
		});
		
		action = new Button(shell, SWT.PUSH | SWT.CENTER);
		action.setText("Convert to az/el");
		action.setBounds(49, 134, 84, 30);
		action.setEnabled(false);
		action.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent evt) {
				double raHourVal = Double.parseDouble(raHour.getText());
				double raMinVal = Double.parseDouble(raMin.getText());
				double raMod = Math.signum(raHourVal);
				double raSecVal;
				
				double decDegVal = Double.parseDouble(decDeg.getText());
				double decMinVal = Double.parseDouble(decMin.getText());
				double decMod = Math.signum(decDegVal);
				double decSecVal;
				double ra = raHourVal + raMod*raMinVal/60;
				double dec = decDegVal + decMod*decMinVal/60;
				if (minsec) {
					raSecVal = Double.parseDouble(raSec.getText());
					decSecVal = Double.parseDouble(decSec.getText());
					ra += raMod*raSecVal/3600;
					dec += decMod*decSecVal/3600;
				}
				raVal = ra;
				decVal = dec;
				az = baseLocation.radecToAz(ra, dec);
				el = baseLocation.radecToEl(ra, dec);
				txtAz.setText(String.valueOf(az));
				txtEl.setText(String.valueOf(el));
				btnGO.setEnabled(true);
			}
		});
		
		hourdeg = new Label(shell, SWT.NONE);
		hourdeg.setText("hour / deg");
		hourdeg.setBounds(40, 70, 50, 13);
		
		sec = new Label(shell, SWT.NONE);
		sec.setText("sec");
		sec.setVisible(minsec);
		sec.setBounds(139, 70, 37, 13);
		
		min = new Label(shell, SWT.NONE);
		min.setText("min");
		min.setBounds(96, 70, 37, 13);
		
		ra = new Label(shell, SWT.NONE);
		ra.setText("ra:");
		ra.setBounds(10, 89, 37, 13);
		
		dec = new Label(shell, SWT.NONE);
		dec.setText("dec:");
		dec.setBounds(10, 110, 37, 13);
		
		lblDirections = new Label(shell, SWT.WRAP);
		lblDirections.setBounds(27, 27, 149, 80);
		lblDirections.setText("ra in hours, dec in degrees");
		
		Label lblAz = new Label(shell, SWT.NONE);
		lblAz.setBounds(10, 193, 18, 18);
		lblAz.setText("az");
		
		Label lblEl = new Label(shell, SWT.NONE);
		lblEl.setBounds(10, 216, 18, 18);
		lblEl.setText("el");
		
		txtAz = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
		txtAz.setBounds(34, 192, 76, 18);
		
		txtEl = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
		txtEl.setBounds(34, 215, 76, 19);
		
		btnGO = new Button(shell, SWT.NONE);
		btnGO.setBounds(129, 193, 68, 41);
		btnGO.setEnabled(false);
		if (type.equals("gotoradec")) btnGO.setText("GO");
		else if (type.equals("tracking")) btnGO.setText("Set RA/Dec");
		btnGO.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				Coordinate c = new Coordinate(el, az);
				if (type.equals("gotoradec")) stage.goToPos(c);
				else if (type.equals("tracking")) stage.setRaDecTracking(raVal, decVal);
				close();
			}
		});
	}
	
	//Enables calibrate if all the fields are filled.
	public void checkHandler(ModifyEvent evt, Text text, boolean minOrSecFlag){
		boolean min_sec_check = false;
		if (minOrSecFlag){
			if(raSec.getText().equals("")||decSec.getText().equals("")){
				min_sec_check = true;
			}
		}
		if (decDeg.getText().equals("") || decMin.getText().equals("") || raHour.getText().equals("")
				|| raMin.getText().equals("") || min_sec_check){
			action.setEnabled(false);
		}
		else{
			action.setEnabled(true);
		}
	}
	
}