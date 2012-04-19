package edu.ucsb.deepspace.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.ucsb.deepspace.Coordinate;
import edu.ucsb.deepspace.LatLongAlt;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import edu.ucsb.deepspace.Stage;

public class RaDecWindow {
	
	private Shell shell;
	private Button action, btnGO;
	
	private Text raHour, raMin, raSec;
	private Text decDeg, decMin, decSec;
	private Label dec, min, ra, sec, hourdeg, lblDirections;
	private Control current;
	private List<Control> order = new ArrayList<Control>();
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
		raHour.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, raHour, false);
			}
		});
		raHour.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = raHour;
			}
		});
		
		raMin = new Text(shell, SWT.BORDER);
		raMin.setBounds(96, 89, 37, 18);
		raMin.setEnabled(false);
		raMin.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, raMin, true);
			}
		});
		raMin.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = raMin;
			}
		});
		
		raSec = new Text(shell, SWT.BORDER);
		raSec.setBounds(139, 89, 37, 18);
		raSec.setEnabled(false);
		raSec.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, raSec, true);
			}
		});
		raSec.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = raSec;
			}
		});
		
		decDeg = new Text(shell, SWT.BORDER);
		decDeg.setBounds(53, 110, 37, 18);
		decDeg.setEnabled(false);
		decDeg.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, decDeg, false);
			}
		});
		decDeg.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = decDeg;
			}
		});
		
		decMin = new Text(shell, SWT.BORDER);
		decMin.setBounds(96, 110, 37, 18);
		decMin.setEnabled(false);
		decMin.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, decMin, true);
			}
		});
		decMin.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = decMin;
			}
		});
		
		decSec = new Text(shell, SWT.BORDER);
		decSec.setBounds(139, 110, 37, 18);
		decSec.setEnabled(false);
		decSec.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, decSec, true);
			}
		});
		decSec.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = decSec;
			}
		});
		
		action = new Button(shell, SWT.PUSH | SWT.CENTER);
		action.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		action.setText("Convert to az/el");
		action.setBounds(49, 134, 84, 30);
		action.setEnabled(false);
		action.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent evt) {
				double raHourVal = Double.parseDouble(raHour.getText());
				double raMinVal = Double.parseDouble(raMin.getText());
				double raSecVal;
				double decDegVal = Double.parseDouble(decDeg.getText());
				double decMinVal = Double.parseDouble(decMin.getText());
				double decSecVal;
				double ra = raHourVal + raMinVal/60;
				double dec = decDegVal + decMinVal/60;
				if (minsec) {
					raSecVal = Double.parseDouble(raSec.getText());
					decSecVal = Double.parseDouble(decSec.getText());
					ra += raSecVal/3600;
					dec += decSecVal/3600;
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
		
		order.add(raHour);
		order.add(raMin);
		if (minsec) {
			order.add(raSec);
		}
		order.add(decDeg);
		order.add(decMin);
		if (minsec) {
			order.add(decSec);
		}
		
		order.add(action);
		current = raHour;
		
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
		int pos = order.indexOf(current);
		final Control next = order.get(pos+1);
		if (evt.keyCode == 16777296 || evt.keyCode == 13) {
			try {
				Double val = Double.parseDouble(text.getText());
				//if ((val >= rangeMin) && (val <= rangeMax)) {
					Display.getCurrent().asyncExec(new Runnable() {
						public void run() {
							next.setEnabled(true);
							next.setFocus();
						}
					});
					current = next;
					return;
				//}
			} catch (NumberFormatException e) {
				if (text.getText()== null) {
					
				}
				else if (text.getText().equals("")) {
					
				}
				System.out.println("enter an integer between -180 and 180");
			} 
		}
	}
}