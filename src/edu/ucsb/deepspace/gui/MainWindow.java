package edu.ucsb.deepspace.gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import edu.ucsb.deepspace.ActInterface.axisType;
import edu.ucsb.deepspace.Formatters;
import edu.ucsb.deepspace.MoveCommand;
import edu.ucsb.deepspace.MoveCommand.MoveMode;
import edu.ucsb.deepspace.MoveCommand.MoveType;
import edu.ucsb.deepspace.ScanCommand;
import edu.ucsb.deepspace.Stage;

public class MainWindow extends org.eclipse.swt.widgets.Composite {
	public static enum buttonGroups {
		SCAN, RELATIVE;
	}
	
	public boolean debug = true;
	private String debugAxis = "A";
	
	private Shell shell;
	private final Properties windowSettings = new Properties();
	private Stage stage;
	
	private String moveType = "degrees";
	private long moveAmountVal = 0;
	private boolean minsec = false;
	private boolean continuousScanOn = false;
	private boolean rasterScan = false;
	private double minAz = 0, maxAz = 0, minEl = 0, maxEl = 0;
	private int encTol;
	private boolean radecOn = false;
	private String actInfo = "";
	private Text moveAmount;
	private Button azMinus;
	private Button azPlus;
	private Button elMinus;
	private Button elPlus;
	private Button btnRadioSteps;
	private Button btnRadioDegrees;
	private Button btnEncoderSteps;
	private Button status;
	private Button indexAz;
	private Button indexEl;
	private Button btnCalibrate;
	private Button btnGoToPosition;
	private Button btnGoToRaDec;
	private Button btnBaseLocation;
	private Button btnBalloonLocation;
	private Button btnDecimalMinutes;
	private Button btnMinutesSeconds;
	private Button btnLock;
	private Button btnUnlock;
	private Button btnRaDecOn;
	private Button btnRaDecOff;
	private Button btnChangeRadec;
	private Button btnQuit;
	private Button btnGoToBalloon;
	private Button btnSetMinMaxAz;
	private Button btnSetMinMaxEl;
	private Button btnScanAz;
	private Button btnScanEl;
	private Button btnScanBoth;
	private Button btnContinuousScan;
	private Text txtEncTol;
	private Text txtRa;
	private Text txtDec;
	private Text txtMinAz;
	private Text txtMaxAz;
	private Text txtMinEl;
	private Text txtMaxEl;
	private Text txtMinAzScan;
	private Text txtMaxAzScan;
	private Text txtTimeAzScan;
	private Text txtMinElScan;
	private Text txtMaxElScan;
	private Text txtTimeElScan;
	private Text txtRepScan;
	private static Map<String, Button> popupWindowButtons = new HashMap<String, Button>();
	private Text txtPosInfo;
	private Button btnStop;
	private Button btnMotorOn;
	private Button btnMotorOff;
	private Button btnBegin;
	private Text txtAzElRaDec;
	private Text txtBaseLocation;
	private Text txtBalloonLocation;
	private Text txtGoalAz;
	private Text txtGoalEl;
	private Stage.stageType stageType;

	private Button btnDebugAz;

	private Button btnDebugEl;
	private Text txtDebugVel;
	private Text txtStatusArea;
	private List<Text> scanAzTexts = new ArrayList<Text>();
	private List<Text> scanElTexts = new ArrayList<Text>();
	private Text txtVelAz;
	private Text txtAccAz;
	private Text txtVelEl;
	private Text txtAccEl;
	private Button btnStopEl;
	private Button btnSetMaxVelAccAz;
	private Button btnSetMaxVelAccEl;
	private Button btnMotorEl;
	private Button btnMotorAz;
	private Button btnStopAz;
	private Group area;
	private Group grpLatitutdeLongitudeControl;
	private Text text;

	public MainWindow(Composite parent, int style, Stage stage, Stage.stageType stageType) {
		super(parent, style);
		this.stage = stage;
		this.stageType = stageType;
		actInfo = stage.stageInfo();
		shell = parent.getShell();
		setPreferences();
		initGUI();
		
		shell.setLayout(new FillLayout());
        shell.setText("Ground COFE");
        shell.layout();
		shell.open();
	}
	
	public void alive() {
		while (!shell.isDisposed()) {
            if (!Display.getDefault().readAndDispatch())
            	Display.getDefault().sleep();
        }
	}
	
	private void initGUI() {
		shell.addDisposeListener(new DisposeListener() {
        	public void widgetDisposed(DisposeEvent evt) {shellWidgetDisposed();}
        });

        shell.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent evt) {saveShellBounds();}
            public void controlMoved(ControlEvent evt) {saveShellBounds();}
        });
        
        area = new Group(this, SWT.NONE);
        area.setText("");
        area.setLayout(null);
        area.setBounds(10, 0, 728, 876);
        
        guiJoystick();
        guiRaDec();
        guiScanning();
        guiAxisControl();
        guiDebug();
    	
    	status = new Button(area, SWT.PUSH | SWT.CENTER);
    	status.setText("status");
    	status.setBounds(10, 17, 60, 30);
    	status.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			stage.status();
    		}
    	});
    	
    	btnCalibrate = new Button(area, SWT.PUSH | SWT.CENTER);
    	btnCalibrate.setText("Calibrate");
    	btnCalibrate.setBounds(387, 437, 87, 31);
    	popupWindowButtons.put("calibrate", btnCalibrate);
    	btnCalibrate.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			btnCalibrate.setEnabled(false);
    			new AzElWindow(minsec, "calibrate", stage);
    		}
    	});
    	
    	btnGoToPosition = new Button(area, SWT.NONE);
    	btnGoToPosition.setBounds(290, 471, 87, 31);
    	btnGoToPosition.setText("Go to Position");
    	popupWindowButtons.put("gotopos", btnGoToPosition);
    	btnGoToPosition.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			btnGoToPosition.setEnabled(false);
    			new AzElWindow(minsec, "gotopos", stage);
    		}
    	});
    	
    	btnGoToRaDec = new Button(area, SWT.NONE);
    	btnGoToRaDec.setBounds(290, 504, 87, 31);
    	btnGoToRaDec.setText("Go to RA / Dec");
    	popupWindowButtons.put("gotoradec", btnGoToRaDec);
    	btnGoToRaDec.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			btnGoToRaDec.setEnabled(false);
    			new RaDecWindow(minsec, "gotoradec", stage);
    		}
    	});
    	
    	btnDecimalMinutes = new Button(area, SWT.RADIO);
    	btnDecimalMinutes.setSelection(true);
    	btnDecimalMinutes.setBounds(378, 541, 96, 16);
    	btnDecimalMinutes.setText("decimal minutes");
    	btnDecimalMinutes.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			minsec = false;
    		}
    	});
    	 	
    	btnMinutesSeconds = new Button(area, SWT.RADIO);
    	btnMinutesSeconds.setBounds(378, 563, 96, 16);
    	btnMinutesSeconds.setText("minutes seconds");
    	btnMinutesSeconds.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			minsec = true;
    		}
    	});
    	
    	txtPosInfo = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
    	txtPosInfo.setBounds(290, 17, 181, 181);
    	txtPosInfo.setText("Actuator Information\r\n");
    	
    	txtEncTol = new Text(area, SWT.BORDER);
    	txtEncTol.setBounds(11, 89, 49, 19);
    	txtEncTol.setText(String.valueOf(encTol));
    	txtEncTol.addModifyListener(new ModifyListener() {
    		public void modifyText(ModifyEvent e) {
    			String textVal = txtEncTol.getText();
    			int encTol = 0;
    			try {
    				encTol = Integer.parseInt(textVal);
    				if (encTol < 1) {
    					throw new NumberFormatException("encoder tolerance must be an integer larger than 1");
    				}
    			} catch (NumberFormatException e1) {
    				encTol = 2;
    				System.out.println("default to 2");
    				return;
    			}
    			stage.setEncTol(encTol);
    		}
    	});
    	
    	Label lblEncoderTolerance = new Label(area, SWT.WRAP);
    	lblEncoderTolerance.setBounds(11, 53, 49, 30);
    	lblEncoderTolerance.setText("encoder tolerance");
    	
    	btnLock = new Button(area, SWT.NONE);
    	btnLock.setBounds(10, 114, 50, 23);
    	btnLock.setText("Lock");
    	btnLock.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			disableButtons();
    			btnUnlock.setEnabled(true);
    			btnLock.setEnabled(false);
    		}
    	});
    	
    	btnUnlock = new Button(area, SWT.NONE);
    	btnUnlock.setBounds(10, 142, 50, 23);
    	btnUnlock.setText("Unlock");
    	btnUnlock.setEnabled(false);
    	btnUnlock.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			enableButtons();
    			btnUnlock.setEnabled(false);
    			btnLock.setEnabled(true);
    		}
    	});
    	
    	txtAzElRaDec = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
    	txtAzElRaDec.setText("Az:  \nEl:  \nRA:  \nDec:  \nUTC:  \nLST:  \nLocal:");
    	txtAzElRaDec.setFont(SWTResourceManager.getFont("Tahoma", 16, SWT.NORMAL));
    	txtAzElRaDec.setBounds(477, 17, 237, 181);
    	
    	btnQuit = new Button(area, SWT.NONE);
    	btnQuit.setBounds(2, 171, 68, 23);
    	btnQuit.setText("QUIT");
    	btnQuit.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			shell.close();
    		}
    	});
    	
    	Text txtStaticActInfo = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
    	txtStaticActInfo.setBounds(10, 590, 137, 110);
    	txtStaticActInfo.setText(actInfo);
    		
    	btnGoToBalloon = new Button(area, SWT.NONE);
    	btnGoToBalloon.setBounds(290, 437, 87, 31);
    	btnGoToBalloon.setText("Go to Balloon");
    	btnGoToBalloon.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			stage.goToBalloon();
    		}
    	});
    	
    	txtGoalAz = new Text(area, SWT.BORDER | SWT.READ_ONLY);
    	txtGoalAz.setBounds(620, 204, 94, 17);
    	
    	txtGoalEl = new Text(area, SWT.BORDER | SWT.READ_ONLY);
    	txtGoalEl.setBounds(620, 231, 94, 17);
    	
    	txtStatusArea = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
    	txtStatusArea.setBounds(10, 729, 704, 133);
    	txtStatusArea.setText("StatusArea\n\n");
    	
    	grpLatitutdeLongitudeControl = new Group(area, SWT.NONE);
    	grpLatitutdeLongitudeControl.setText("Latitutde Longitude Control");
    	grpLatitutdeLongitudeControl.setBounds(290, 204, 200, 227);
    	//--------------------------------------------------------------------------------------------------------------------
    	    	
    	    	txtBaseLocation = new Text(grpLatitutdeLongitudeControl, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
    	    	txtBaseLocation.setBounds(10, 20, 181, 60);
    	    	txtBaseLocation.setText("Base Location");
    	    	txtBaseLocation.setText(stage.baseLocDisplay());
    	    	
    	    	txtBalloonLocation = new Text(grpLatitutdeLongitudeControl, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
    	    	txtBalloonLocation.setBounds(10, 86, 181, 95);
    	    	txtBalloonLocation.setText("Balloon Location");
    	    	txtBalloonLocation.setText(stage.balloonLocDisplay());
    	    	
    	    	btnBaseLocation = new Button(grpLatitutdeLongitudeControl, SWT.NONE);
    	    	btnBaseLocation.setBounds(10, 187, 87, 31);
    	    	btnBaseLocation.setText("Base Location");
    	    	popupWindowButtons.put("baseloc", btnBaseLocation);
    	    	
    	    	btnBalloonLocation = new Button(grpLatitutdeLongitudeControl, SWT.NONE);
    	    	btnBalloonLocation.setBounds(99, 187, 87, 31);
    	    	btnBalloonLocation.setText("Balloon Location");
    	    	popupWindowButtons.put("balloonloc", btnBalloonLocation);
    	    	
    	    	Button btnNewButton = new Button(area, SWT.NONE);
    	    	btnNewButton.setBounds(387, 474, 87, 30);
    	    	btnNewButton.setText("New Button");
    	    	btnBalloonLocation.addMouseListener(new MouseAdapter() {
    	    		@Override
    	    		public void mouseDown(MouseEvent e) {
    	    			btnBalloonLocation.setEnabled(false);
    	    			new LatLongAltWindow(minsec, "balloonloc", stage);
    	    		}
    	    	});
    	    	btnBaseLocation.addMouseListener(new MouseAdapter() {
    	    		public void mouseDown(MouseEvent evt) {
    	    			btnBaseLocation.setEnabled(false);
    	    			new LatLongAltWindow(minsec, "baseloc", stage);
    	    		}
    	    	});
    	
    	

    	//TODO hide the debug stuff
    	if (debug) {
    	}
    	
	}
	
	private void guiJoystick() {
    	Group grpJoystick = new Group(area, SWT.NONE);
    	grpJoystick.setText("Joystick");
    	grpJoystick.setBounds(76, 17, 208, 267);
    	
    	moveAmount = new Text(grpJoystick, SWT.BORDER);
    	moveAmount.setBounds(10, 28, 90, 16);
    	moveAmount.setText("amount to move");
    	moveAmount.addModifyListener(new ModifyListener() {
    		public void modifyText(ModifyEvent e) {
    			Long temp = moveAmountVal;
    			try {
    				moveAmountVal = Long.parseLong(moveAmount.getText());
    			} catch (NumberFormatException e1) {
    				moveAmountVal = temp;
    			}
    		}
    	});
    	
    	azMinus = new Button(grpJoystick, SWT.PUSH | SWT.CENTER);
    	azMinus.setBounds(19, 106, 67, 31);
    	azMinus.setText("azMinus");
    	azMinus.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			controlMoveButtons(false);
    			//stage.relative(axisType.AZ, moveType, -moveAmountVal);
    			stage.move(makeRelativeMC(axisType.AZ, -moveAmountVal));
    		}
    	});
    	
    	azPlus = new Button(grpJoystick, SWT.PUSH | SWT.CENTER);
    	azPlus.setBounds(79, 106, 67, 31);
    	azPlus.setText("azPlus");
    	azPlus.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			controlMoveButtons(false);
    			//stage.relative(axisType.AZ, moveType, moveAmountVal);
    			stage.move(makeRelativeMC(axisType.AZ, moveAmountVal));
    		}
    	});
    	
    	elMinus = new Button(grpJoystick, SWT.PUSH | SWT.CENTER);
    	elMinus.setBounds(49, 137, 67, 31);
    	elMinus.setText("elMinus");
    	elMinus.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			controlMoveButtons(false);
    			//stage.relative(axisType.EL, moveType, -moveAmountVal);
    			stage.move(makeRelativeMC(axisType.EL, -moveAmountVal));
    		}
    	});
    	
    	elPlus = new Button(grpJoystick, SWT.CENTER);
    	elPlus.setBounds(49, 72, 67, 31);
    	elPlus.setText("elPlus");
    	elPlus.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			controlMoveButtons(false);
    			//stage.relative(axisType.EL, moveType, moveAmountVal);
    			stage.move(makeRelativeMC(axisType.EL, moveAmountVal));
    		}
    	});
    	
    	btnRadioSteps = new Button(grpJoystick, SWT.RADIO);
    	btnRadioSteps.setBounds(106, 10, 47, 16);
    	btnRadioSteps.setText("steps");
    	btnRadioSteps.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			moveType = "steps";
    		}
    	});
    	
    	if (stageType.equals(Stage.stageType.Galil)) {
	    	btnRadioSteps.setVisible(false);
    	}
    	
    	btnRadioDegrees = new Button(grpJoystick, SWT.RADIO);
    	btnRadioDegrees.setSelection(true);
    	btnRadioDegrees.setBounds(106, 29, 55, 16);
    	btnRadioDegrees.setText("degrees");
    	btnRadioDegrees.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			moveType = "degrees";
    		}
    	});
    	
    	btnEncoderSteps = new Button(grpJoystick, SWT.RADIO);
    	btnEncoderSteps.setBounds(106, 50, 83, 16);
    	btnEncoderSteps.setText("encoder steps");
    	
    	text = new Text(grpJoystick, SWT.BORDER);
    	text.setBounds(10, 188, 76, 19);
    	btnEncoderSteps.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			moveType = "encoder";
    		}
    	});
	}
	
	private void guiRaDec() {
    	Group grpRaDec = new Group(area, SWT.NONE);
    	grpRaDec.setText("RA / Dec Tracking");
    	grpRaDec.setBounds(497, 394, 217, 108);
    	
    	txtRa = new Text(grpRaDec, SWT.BORDER);
    	txtRa.setEditable(false);
    	txtRa.setBounds(35, 62, 76, 15);
    	
    	txtDec = new Text(grpRaDec, SWT.BORDER);
    	txtDec.setEditable(false);
    	txtDec.setBounds(35, 81, 76, 15);
    	
    	btnRaDecOn = new Button(grpRaDec, SWT.NONE);
    	btnRaDecOn.setBounds(21, 23, 46, 23);
    	btnRaDecOn.setText("On");
    	btnRaDecOn.setEnabled(false);
    	btnRaDecOn.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			btnRaDecOn.setEnabled(false);
    			btnRaDecOff.setEnabled(true);
    			double ra = Double.parseDouble(txtRa.getText());
    			double dec = Double.parseDouble(txtDec.getText());
    			radecOn = true;
    			stage.startRaDecTracking(ra, dec);
    		}
    	});
    	
    	btnRaDecOff = new Button(grpRaDec, SWT.NONE);
    	btnRaDecOff.setBounds(79, 23, 46, 23);
    	btnRaDecOff.setText("Off");
    	btnRaDecOff.setEnabled(false);
    	btnRaDecOff.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			btnRaDecOn.setEnabled(true);
    			btnRaDecOff.setEnabled(false);
    			radecOn = false;
    			stage.stopRaDecTracking();
    		}
    	});
    	
    	Label lblRa = new Label(grpRaDec, SWT.NONE);
    	lblRa.setBounds(10, 62, 14, 15);
    	lblRa.setText("RA");
    	
    	Label lblDec = new Label(grpRaDec, SWT.NONE);
    	lblDec.setBounds(10, 81, 24, 15);
    	lblDec.setText("Dec");
    	
    	
    	
    	btnChangeRadec = new Button(grpRaDec, SWT.NONE);
    	btnChangeRadec.setBounds(120, 62, 87, 23);
    	btnChangeRadec.setText("Change RA/Dec");
    	btnChangeRadec.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			new RaDecWindow(minsec, "tracking", stage);
    		}
    	});
	}
	
	private void guiAxisControl() {
    	Group grpAxisControl = new Group(area, SWT.NONE);
    	grpAxisControl.setText("Axis Control");
    	grpAxisControl.setBounds(10, 290, 274, 294);
    	
    	txtMinEl = new Text(grpAxisControl, SWT.BORDER);
    	txtMinEl.setBounds(215, 42, 49, 19);
    	txtMinEl.setText(String.valueOf(minEl));
    	
    	txtMaxEl = new Text(grpAxisControl, SWT.BORDER);
    	txtMaxEl.setBounds(215, 67, 49, 19);
    	txtMaxEl.setText(String.valueOf(maxEl));
    	
    	txtMaxAz = new Text(grpAxisControl, SWT.BORDER);
    	txtMaxAz.setBounds(110, 67, 49, 19);
    	txtMaxAz.setText(String.valueOf(maxAz));
    	
    	txtMinAz = new Text(grpAxisControl, SWT.BORDER);
    	txtMinAz.setBounds(110, 42, 49, 19);
    	txtMinAz.setText(String.valueOf(minAz));
    	
    	btnSetMinMaxEl = new Button(grpAxisControl, SWT.NONE);
    	btnSetMinMaxEl.setBounds(181, 92, 83, 23);
    	btnSetMinMaxEl.setText("Set Min/Max El");
    	btnSetMinMaxEl.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			try {
    				double minEl = Double.parseDouble(txtMinEl.getText());
    				double maxEl = Double.parseDouble(txtMaxEl.getText());
    				stage.setMinMaxEl(minEl, maxEl);
    			} catch (NumberFormatException e1) {
    				txtStatusArea.append("Must input a number.\n");
    			}
    		}
    	});
    	
    	btnSetMinMaxAz = new Button(grpAxisControl, SWT.NONE);
    	btnSetMinMaxAz.setBounds(94, 92, 83, 23);
    	btnSetMinMaxAz.setText("Set Min/Max Az");
    	btnSetMinMaxAz.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			try {
    				double minAz = Double.parseDouble(txtMinAz.getText());
    				double maxAz = Double.parseDouble(txtMaxAz.getText());
    				stage.setMinMaxAz(minAz, maxAz);
    				
    			} catch (NumberFormatException e1) {
    				txtStatusArea.append("Must input a number.\n");
    			}
    		}
    	});
    	
    	txtAccAz = new Text(grpAxisControl, SWT.BORDER);
    	txtAccAz.setBounds(110, 146, 60, 19);
    	
    	txtVelAz = new Text(grpAxisControl, SWT.BORDER);
    	txtVelAz.setBounds(110, 121, 60, 19);
    	
    	txtVelEl = new Text(grpAxisControl, SWT.BORDER);
    	txtVelEl.setBounds(204, 121, 60, 19);
    	
    	txtAccEl = new Text(grpAxisControl, SWT.BORDER);
    	txtAccEl.setBounds(204, 146, 60, 19);
    	
    	Label lblAzimuth = new Label(grpAxisControl, SWT.NONE);
    	lblAzimuth.setBounds(110, 23, 49, 13);
    	lblAzimuth.setText("Azimuth");
    	
    	Label lblElevation = new Label(grpAxisControl, SWT.NONE);
    	lblElevation.setBounds(215, 23, 49, 13);
    	lblElevation.setText("Elevation");
    	
    	Label lblDescription = new Label(grpAxisControl, SWT.NONE);
    	lblDescription.setBounds(10, 23, 60, 13);
    	lblDescription.setText("Description");
    	
    	Label lblMinAngle = new Label(grpAxisControl, SWT.NONE);
    	lblMinAngle.setBounds(10, 42, 49, 13);
    	lblMinAngle.setText("Min Angle");
    	
    	Label lblMaxAngle = new Label(grpAxisControl, SWT.NONE);
    	lblMaxAngle.setBounds(10, 67, 49, 13);
    	lblMaxAngle.setText("Max Angle");
    	
    	Label lblMaxVelocity = new Label(grpAxisControl, SWT.NONE);
    	lblMaxVelocity.setBounds(10, 121, 72, 13);
    	lblMaxVelocity.setText("Max Velocity");
    	
    	Label lblMaxAcceleration = new Label(grpAxisControl, SWT.NONE);
    	lblMaxAcceleration.setBounds(10, 146, 83, 13);
    	lblMaxAcceleration.setText("Max Acceleration");
    	
    	indexAz = new Button(grpAxisControl, SWT.PUSH | SWT.CENTER);
    	indexAz.setBounds(110, 235, 65, 25);
    	indexAz.setText("Index Az");
    	popupWindowButtons.put("indexAz", indexAz);
    	indexAz.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			indexAz.setEnabled(false);
    			//indexEl.setEnabled(false);
    			stage.index(axisType.AZ);
    		}
    	});
    	
    	indexEl = new Button(grpAxisControl, SWT.PUSH | SWT.CENTER);
    	indexEl.setBounds(199, 235, 65, 25);
    	indexEl.setText("Index El");
    	popupWindowButtons.put("indexEl", indexEl);
    	indexEl.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			//indexAz.setEnabled(false);
    			indexEl.setEnabled(false);
    			stage.index(axisType.EL);
    		}
    	});
    	
    	btnMotorAz = new Button(grpAxisControl, SWT.NONE);
    	btnMotorAz.setBounds(110, 204, 65, 25);
    	btnMotorAz.setText("MotorAzOnOff");
    	btnMotorAz.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			stage.motorControl(axisType.AZ);
    		}
    	});
    	
    	btnMotorEl = new Button(grpAxisControl, SWT.NONE);
    	btnMotorEl.setBounds(199, 204, 65, 25);
    	btnMotorEl.setText("MotorElOnOff");
    	btnMotorEl.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			stage.motorControl(axisType.EL);
    		}
    	});
    	
    	btnStopAz = new Button(grpAxisControl, SWT.NONE);
    	btnStopAz.setBounds(110, 266, 65, 25);
    	btnStopAz.setText("Stop Az");
    	btnStopAz.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			stage.stop(axisType.EL);
    		}
    	});
    	
    	btnStopEl = new Button(grpAxisControl, SWT.NONE);
    	btnStopEl.setBounds(199, 266, 65, 25);
    	btnStopEl.setText("Stop El");
    	btnStopEl.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			stage.stop(axisType.EL);
    		}
    	});
    	
    	btnSetMaxVelAccAz = new Button(grpAxisControl, SWT.NONE);
    	btnSetMaxVelAccAz.setBounds(52, 171, 104, 23);
    	btnSetMaxVelAccAz.setText("Set Max Vel/Acc Az");
    	btnSetMaxVelAccAz.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			try {
    				double velAz = Double.parseDouble(txtVelAz.getText());
    				double accAz = Double.parseDouble(txtAccAz.getText());
    				stage.setVelAccAz(velAz, accAz);
    			} catch (NumberFormatException e1) {
    				txtStatusArea.append("Must input a number.\n");
    			}
    		}
    	});
    	
    	btnSetMaxVelAccEl = new Button(grpAxisControl, SWT.NONE);
    	btnSetMaxVelAccEl.setBounds(162, 171, 102, 23);
    	btnSetMaxVelAccEl.setText("Set Max Vel/Acc El");
    	btnSetMaxVelAccEl.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent evt) {
    			try {
    				double velEl = Double.parseDouble(txtVelEl.getText());
    				double accEl = Double.parseDouble(txtAccEl.getText());
    				stage.setVelAccEl(velEl, accEl);
    			} catch (NumberFormatException e1) {
    				txtStatusArea.append("Must input a number.\n");
    			}
    		}
    	});
	}
	
	private void guiScanning() {
    	Group grpScanning = new Group(area, SWT.NONE);
    	grpScanning.setText("Scanning");
    	grpScanning.setBounds(489, 508, 225, 215);
    	
    	Label lblMinAzScan = new Label(grpScanning, SWT.NONE);
    	lblMinAzScan.setBounds(13, 16, 40, 19);
    	lblMinAzScan.setText("min az");
    	
    	Label lblMaxAzScan = new Label(grpScanning, SWT.NONE);
    	lblMaxAzScan.setBounds(13, 41, 40, 19);
    	lblMaxAzScan.setText("max az");
    	
    	Label lblMinElScan = new Label(grpScanning, SWT.NONE);
    	lblMinElScan.setBounds(108, 16, 40, 19);
    	lblMinElScan.setText("min el");
    	
    	Label lblMaxElScan = new Label(grpScanning, SWT.NONE);
    	lblMaxElScan.setBounds(108, 41, 40, 19);
    	lblMaxElScan.setText("max el");
    	
    	Label lblAzTimeScan = new Label(grpScanning, SWT.NONE);
    	lblAzTimeScan.setBounds(13, 66, 40, 19);
    	lblAzTimeScan.setText("az time");
    	
    	Label lblElTimeScan = new Label(grpScanning, SWT.NONE);
    	lblElTimeScan.setBounds(108, 66, 40, 19);
    	lblElTimeScan.setText("el time");
    	
    	txtMinAzScan = new Text(grpScanning, SWT.BORDER);
    	txtMinAzScan.setBounds(59, 16, 43, 19);
    	scanAzTexts.add(txtMinAzScan);
    	
    	txtMaxAzScan = new Text(grpScanning, SWT.BORDER);
    	txtMaxAzScan.setBounds(59, 41, 43, 19);
    	scanAzTexts.add(txtMaxAzScan);
    	
    	txtTimeAzScan = new Text(grpScanning, SWT.BORDER);
    	txtTimeAzScan.setBounds(59, 66, 43, 19);
    	scanAzTexts.add(txtTimeAzScan);
    	
    	txtMinElScan = new Text(grpScanning, SWT.BORDER);
    	txtMinElScan.setBounds(154, 16, 43, 19);
    	scanElTexts.add(txtMinElScan);
    	
    	txtMaxElScan = new Text(grpScanning, SWT.BORDER);
    	txtMaxElScan.setBounds(154, 41, 43, 19);
    	scanElTexts.add(txtMaxElScan);
    	
    	txtTimeElScan = new Text(grpScanning, SWT.BORDER);
    	txtTimeElScan.setBounds(154, 66, 43, 19);
    	scanElTexts.add(txtTimeElScan);
    	
    	Label lblRepetitionsScan = new Label(grpScanning, SWT.NONE);
    	lblRepetitionsScan.setBounds(13, 96, 58, 19);
    	lblRepetitionsScan.setText("repetitions");
    	
    	txtRepScan = new Text(grpScanning, SWT.BORDER);
    	txtRepScan.setBounds(72, 96, 43, 19);
    	scanAzTexts.add(txtRepScan);
    	scanElTexts.add(txtRepScan);
    	
    	
    	btnScanAz = new Button(grpScanning, SWT.NONE);
    	btnScanAz.setBounds(10, 121, 68, 23);
    	btnScanAz.setText("Scan Az");
    	btnScanAz.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			if (btnScanAz.getText().equals("Stop Scan")) {
    				setScanEnabled(axisType.AZ);
    				stage.stopScanning();
    			}
    			else {
    				if (!validateScanInput(scanAzTexts)) {
    					return;
    				}
    				
    				btnScanAz.setText("Stop Scan");
    				btnScanEl.setEnabled(false);
    				btnScanBoth.setEnabled(false);
    				
    				double min = Double.parseDouble(txtMinAzScan.getText());
    				double max = Double.parseDouble(txtMaxAzScan.getText());
    				double time = Double.parseDouble(txtTimeAzScan.getText());
    				int reps = Integer.parseInt(txtRepScan.getText());
    				//stage.startScanning(min, max, time, reps, axisType.AZ, continuousScanOn);
    				
    				ScanCommand azSc = new ScanCommand(min, max, time, reps, continuousScanOn);
    				stage.startScanning(azSc, null);
    			}
    		}
    	});
    	
    	btnScanEl = new Button(grpScanning, SWT.NONE);
    	btnScanEl.setBounds(80, 121, 68, 23);
    	btnScanEl.setText("Scan El");
    	btnScanEl.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			if (btnScanEl.getText().equals("Stop Scan")) {
    				setScanEnabled(axisType.EL);
    				stage.stopScanning();
    			}
    			else {
    				if (!validateScanInput(scanElTexts)) {
    					return;
    				}
    				
    				btnScanEl.setText("Stop Scan");
    				btnScanBoth.setEnabled(false);
        			btnScanAz.setEnabled(false);
        			
        			double min = Double.parseDouble(txtMinElScan.getText());
        			double max = Double.parseDouble(txtMaxElScan.getText());
        			double time = Double.parseDouble(txtTimeElScan.getText());
        			int reps = Integer.parseInt(txtRepScan.getText());
        			//stage.startScanning(min, max, time, reps, axisType.EL,continuousScanOn);
        			
        			ScanCommand elSc = new ScanCommand(min, max, time, reps, continuousScanOn);
        			stage.startScanning(null, elSc);
    			}
    		}
    	});
    	
    	btnScanBoth = new Button(grpScanning, SWT.NONE);
    	btnScanBoth.setBounds(154, 121, 68, 23);
    	btnScanBoth.setText("Scan Both");
    	btnScanBoth.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			if (btnScanBoth.getText().equals("Stop Scan")) {
    				enableScanButtons();
    				btnScanBoth.setText("Scan Both");
    				stage.stopScanning();
    			}
    			else {
    				if (!validateScanInput(scanAzTexts) && !validateScanInput(scanElTexts)) {
    					return;
    				}
    				
    				double minAz = Double.parseDouble(txtMinAzScan.getText());
    				double maxAz = Double.parseDouble(txtMaxAzScan.getText());
    				double timeAz = Double.parseDouble(txtTimeAzScan.getText());
    				double minEl = Double.parseDouble(txtMinElScan.getText());
        			double maxEl = Double.parseDouble(txtMaxElScan.getText());
        			double timeEl = Double.parseDouble(txtTimeElScan.getText());
    				int reps = Integer.parseInt(txtRepScan.getText());
    				
    				ScanCommand azSc = new ScanCommand(minAz, maxAz, timeAz, reps, continuousScanOn);
    				ScanCommand elSc = new ScanCommand(minEl, maxEl, timeEl, reps, continuousScanOn);
    				stage.startScanning(azSc, elSc);
    				
    				btnScanBoth.setText("Stop Scan");
    				btnScanEl.setEnabled(false);
        			btnScanAz.setEnabled(false);
    			}
    		}
    	});
    	
    	btnContinuousScan = new Button(grpScanning, SWT.CHECK);
    	btnContinuousScan.setBounds(13, 150, 100, 24);
    	btnContinuousScan.setText("Continuous Scan");
    	btnContinuousScan.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			continuousScanOn = !continuousScanOn;
    		}
    	});
    	
    	Button btnScnRaster = new Button(grpScanning, SWT.CHECK);
    	btnScnRaster.setBounds(119, 154, 85, 16);
    	btnScnRaster.setText("Raster Scan");
    	btnScnRaster.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			rasterScan = !rasterScan;
    		}
    	});
	}

	private void guiDebug() {
    	Group grpDebug = new Group(area, SWT.NONE);
    	grpDebug.setText("Debug");
    	grpDebug.setBounds(234, 590, 249, 133);
    	
    	Button btnQueueSize = new Button(grpDebug, SWT.NONE);
    	btnQueueSize.setBounds(10, 78, 68, 23);
    	btnQueueSize.setText("queue size");
    	btnQueueSize.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			stage.queueSize();
    		}
    	});
    	
    	btnStop = new Button(grpDebug, SWT.NONE);
    	btnStop.setBounds(10, 20, 68, 23);
    	btnStop.setText("Stop");
    	btnStop.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			stage.sendCommand("ST" + debugAxis);
    		}
    	});
    	
    	btnBegin = new Button(grpDebug, SWT.NONE);
    	btnBegin.setBounds(84, 20, 68, 23);
    	btnBegin.setText("Begin");
    	btnBegin.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			stage.sendCommand("BG" + debugAxis);
    		}
    	});
    	
    	btnMotorOff = new Button(grpDebug, SWT.NONE);
    	btnMotorOff.setBounds(84, 49, 68, 23);
    	btnMotorOff.setText("Motor Off");
    	btnMotorOff.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			stage.sendCommand("MO" + debugAxis);
    		}
    	});
    	
    	
    	btnMotorOn = new Button(grpDebug, SWT.NONE);
    	btnMotorOn.setBounds(10, 49, 68, 23);
    	btnMotorOn.setText("Motor On");
    	btnMotorOn.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			stage.sendCommand("SH" + debugAxis);
    		}
    	});
    	
    	Button btnReadQueue = new Button(grpDebug, SWT.NONE);
    	btnReadQueue.setBounds(10, 107, 68, 23);
    	btnReadQueue.setText("read queue");
    	btnReadQueue.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			stage.readQueue();
    		}
    	});
    	
    	btnDebugAz = new Button(grpDebug, SWT.RADIO);
    	btnDebugAz.setSelection(true);
    	btnDebugAz.setBounds(161, 22, 85, 16);
    	btnDebugAz.setText("Azimuth");
    	btnDebugAz.addSelectionListener(new SelectionAdapter() {
    		@Override
    		public void widgetSelected(SelectionEvent e) {
    			debugAxis = "A";
    		}
    	});
    	
    	btnDebugEl = new Button(grpDebug, SWT.RADIO);
    	btnDebugEl.setBounds(161, 43, 85, 16);
    	btnDebugEl.setText("Elevation");
    	btnDebugEl.addSelectionListener(new SelectionAdapter() {
    		@Override
    		public void widgetSelected(SelectionEvent e) {
    			debugAxis = "B";
    		}
    	});
    	
    	txtDebugVel = new Text(grpDebug, SWT.BORDER);
    	txtDebugVel.setText("vel");
    	txtDebugVel.setBounds(94, 82, 76, 19);
    	
    	Button btnSetVelocity = new Button(grpDebug, SWT.NONE);
    	btnSetVelocity.setBounds(176, 78, 68, 23);
    	btnSetVelocity.setText("Set Velocity");
    	btnSetVelocity.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseDown(MouseEvent e) {
    			
    			double vel = 0;
    			try {
    				vel = Double.parseDouble(txtDebugVel.getText());
    			} catch (NumberFormatException f) {
    				f.printStackTrace();
    			}
    			String out = "JG" + debugAxis + "=" + vel;
    			stage.sendCommand(out);
    		}
    	});
	}
	
	private void shellWidgetDisposed() {
        try {
        	saveShellBounds();
            windowSettings.store(new FileOutputStream("WindowSettings.ini"), "");
            stage.shutdown();
        } catch (FileNotFoundException ignored) {
        } catch (IOException ignored) {
        }
    }
	
	private void saveShellBounds() {
		Rectangle bounds = shell.getBounds();
        windowSettings.setProperty("top", String.valueOf(bounds.y));
        windowSettings.setProperty("left", String.valueOf(bounds.x));
        windowSettings.setProperty("width", String.valueOf(bounds.width));
        windowSettings.setProperty("height", String.valueOf(bounds.height));
	}
	
	private void setPreferences() {
        try {
            windowSettings.load(new FileInputStream("WindowSettings.ini"));
        } catch (FileNotFoundException ignored) {
        } catch (IOException ignored) {
        }
        
        int width = Integer.parseInt(windowSettings.getProperty("width", "800"));
        int height = Integer.parseInt(windowSettings.getProperty("height", "600"));
        Rectangle screenBounds = getDisplay().getBounds();
        int defaultTop = (screenBounds.height - height) / 2;
        int defaultLeft = (screenBounds.width - width) / 2;
        int top = Integer.parseInt(windowSettings.getProperty("top", String.valueOf(defaultTop)));
        int left = Integer.parseInt(windowSettings.getProperty("left", String.valueOf(defaultLeft)));
        shell.setSize(width, height);
        shell.setLocation(left, top);
        saveShellBounds();
    }
	
	public void buttonEnabler(String name) {
		final Button btn = popupWindowButtons.get(name);
		if (btn == null) {
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				btn.setEnabled(true);
			}
		});
	}
	
	public void controlMoveButtons(final boolean trueFalse) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				azPlus.setEnabled(trueFalse);
				azMinus.setEnabled(trueFalse);
				elPlus.setEnabled(trueFalse);
				elMinus.setEnabled(trueFalse);
			}
		});
	}
	
	public void enableButtons() {
		btnGoToPosition.setEnabled(true);
    	btnBalloonLocation.setEnabled(true);
    	btnCalibrate.setEnabled(true);
    	btnBaseLocation.setEnabled(true);
    	azPlus.setEnabled(true);
    	azMinus.setEnabled(true);
    	elPlus.setEnabled(true);
    	elMinus.setEnabled(true);
    	status.setEnabled(true);
    	indexAz.setEnabled(true);
    	indexEl.setEnabled(true);
    	btnMinutesSeconds.setEnabled(true);
    	btnDecimalMinutes.setEnabled(true);
    	btnRadioSteps.setEnabled(true);
    	btnRadioDegrees.setEnabled(true);
    	//btnEncoderSteps.setEnabled(true);
    	txtEncTol.setEnabled(true);
    	if (radecOn) btnRaDecOff.setEnabled(true);
    	else if (txtRa.getText().equals("") && txtDec.getText().equals("")) {}
    	else btnRaDecOn.setEnabled(true);
    	btnGoToRaDec.setEnabled(true);
    	btnChangeRadec.setEnabled(true);
    	btnQuit.setEnabled(true);
    	btnSetMinMaxEl.setEnabled(true);
    	txtMaxEl.setEnabled(true);
    	txtMinEl.setEnabled(true);
    	txtMaxAz.setEnabled(true);
    	txtMinAz.setEnabled(true);
    	moveAmount.setEnabled(true);
    	btnGoToBalloon.setEnabled(true);
    	btnSetMinMaxAz.setEnabled(true);
    	
    	btnScanAz.setEnabled(true);
    	btnScanEl.setEnabled(true);
    	btnScanBoth.setEnabled(true);
    	txtMinAzScan.setEnabled(true);
    	txtMaxAzScan.setEnabled(true);
    	txtTimeAzScan.setEnabled(true);
    	txtMinElScan.setEnabled(true);
    	txtMaxElScan.setEnabled(true);
    	txtTimeElScan.setEnabled(true);
    	txtRepScan.setEnabled(true);
	}
	
	public void disableButtons() {
		btnGoToPosition.setEnabled(false);
    	btnBalloonLocation.setEnabled(false);
    	btnCalibrate.setEnabled(false);
    	btnBaseLocation.setEnabled(false);
    	azPlus.setEnabled(false);
    	azMinus.setEnabled(false);
    	elPlus.setEnabled(false);
    	elMinus.setEnabled(false);
    	status.setEnabled(false);
    	indexAz.setEnabled(false);
    	indexEl.setEnabled(false);
    	btnMinutesSeconds.setEnabled(false);
    	btnRaDecOff.setEnabled(false);
    	btnDecimalMinutes.setEnabled(false);
    	btnRadioSteps.setEnabled(false);
    	btnRadioDegrees.setEnabled(false);
    	//btnEncoderSteps.setEnabled(false);
    	txtEncTol.setEnabled(false);
    	btnRaDecOn.setEnabled(false);
    	btnGoToRaDec.setEnabled(false);
    	btnChangeRadec.setEnabled(false);
    	btnQuit.setEnabled(false);
    	btnSetMinMaxEl.setEnabled(false);
    	txtMaxEl.setEnabled(false);
    	txtMinEl.setEnabled(false);
    	txtMaxAz.setEnabled(false);
    	txtMinAz.setEnabled(false);
    	moveAmount.setEnabled(false);
    	btnGoToBalloon.setEnabled(false);
    	btnSetMinMaxAz.setEnabled(false);
    	
    	btnScanAz.setEnabled(false);
    	btnScanEl.setEnabled(false);
    	btnScanBoth.setEnabled(false);
    	txtMinAzScan.setEnabled(false);
    	txtMaxAzScan.setEnabled(false);
    	txtTimeAzScan.setEnabled(false);
    	txtMinElScan.setEnabled(false);
    	txtMaxElScan.setEnabled(false);
    	txtTimeElScan.setEnabled(false);
    	txtRepScan.setEnabled(false);
	}
	
	public void enableScanButtons() {
		btnScanAz.setEnabled(true);
		btnScanEl.setEnabled(true);
		btnScanBoth.setEnabled(true);
	}
	
	public void updateTxtPosInfo(final String info) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				txtPosInfo.setText(info);
			}
		});
	}
	
	public void updateTxtAzElRaDec(final String info) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				txtAzElRaDec.setText(info);
			}
		});
	}
	
	public void updateBaseBalloonLoc() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				txtBaseLocation.setText(stage.baseLocDisplay());
				txtBalloonLocation.setText(stage.balloonLocDisplay());
			}
		});
	}
	
	public void setMinMaxAzEl(final double minAz, final double maxAz, final double minEl, final double maxEl) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				txtMinAz.setText(String.valueOf(minAz));
				txtMaxAz.setText(String.valueOf(maxAz));
				txtMinEl.setText(String.valueOf(minEl));
				txtMaxEl.setText(String.valueOf(maxEl));
			}
		});
	}
	
	public void setVelAccAzEl(final double velAz, final double accAz, final double velEl, final double accEl) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				txtVelAz.setText(String.valueOf(velAz));
				txtAccAz.setText(String.valueOf(accAz));
				txtVelEl.setText(String.valueOf(velEl));
				txtAccEl.setText(String.valueOf(accEl));
			}
		});
	}
	
	public void setGoalPos(final String goalDeg, final axisType axis) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				switch (axis) {
					case AZ:
						txtGoalAz.setText(goalDeg); break;
					case EL:
						txtGoalEl.setText(goalDeg); break;
					default:
						System.out.println("error MainWindow.setGoalPos");
				}
			}
		});
	}
	
	public void setScanEnabled(final axisType type){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				enableScanButtons();
				switch (type) {
					case AZ:
						btnScanAz.setText("Scan Az" ); break;
					case EL:
						btnScanEl.setText("Scan El"); break;
					case BOTH:
						btnScanBoth.setText("Scan Both"); break;
				}
			}
		});
	}
	
	public void setRaDec(final double ra, final double dec) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				txtRa.setText(Formatters.TWO_POINTS.format(ra));
				txtDec.setText(Formatters.TWO_POINTS.format(dec));
				btnRaDecOn.setEnabled(true);
			}
		});
	}
	
	public void updateStatusArea(final String message) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				txtStatusArea.append(message);
			}
		});
	}
	
	public boolean isDouble(String input) {
		try {
			Double.parseDouble(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	private boolean validateScanInput(List<Text> texts) {
		for (Text t : texts) {
			if (!isDouble(t.getText())) {
				updateStatusArea("This is not a valid number.\n");
				t.setFocus();
				return false;
			}
		}
		return true;
	}
	
	public void updateVelAcc(final String azVel, final String azAcc, final String elVel, final String elAcc) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				txtVelAz.setText(azVel);
				txtAccAz.setText(azAcc);
				txtVelEl.setText(elVel);
				txtAccEl.setText(elAcc);
			}
		});
	}
	
	/**
	 * Utility method to generate a MoveCommand for relative motion. <P>
	 * Used by the joystick buttons.
	 * @param axis az or el
	 * @param amount to move in degrees
	 * @return
	 */
	private MoveCommand makeRelativeMC(axisType axis, double amount) {
		MoveType type = null;
		switch (moveType) {
			case "degrees":
				type = MoveType.DEGREE; break;
			case "encoder":
				type = MoveType.ENCODER; break;
		}
		MoveCommand mc = new MoveCommand(MoveMode.RELATIVE, type, axis, amount);
		return mc;
	}
	
	/**
	 * Used to initialize the text on btnMotorAz and btnMotorEl
	 * @param az state of az motor
	 * @param el state of el motor
	 */
	public void updateMotorState(final boolean az, final boolean el) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				String textAz = "Motor On";
				String textEl = "Motor on";
				if (az) textAz = "Motor Off";
				if (el) textEl = "Motor Off";
				btnMotorAz.setText(textAz);
				btnMotorEl.setText(textEl);
			}
		});
	}
	
	public void updateMotorButton(final boolean onOff, final axisType axis) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				String text = "Motor On";
				if (onOff) {
					text = "Motor Off";
				}
				switch (axis) {
					case AZ:
						btnMotorAz.setText(text); break;
					case EL:
						btnMotorEl.setText(text); break;
				}
			}
		});
	}
}