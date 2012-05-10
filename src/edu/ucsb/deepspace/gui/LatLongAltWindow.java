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

import edu.ucsb.deepspace.LatLongAlt;
import edu.ucsb.deepspace.Stage;

public class LatLongAltWindow {
	private Shell shell;
	private Button action;
	
	private Text latDeg, latMin, latSec;
	private Text longDeg, longMin, longSec;
	private Text altText;
	private Label lat, lon, deg, min, sec, alt;
	private Control current;
	private List<Control> order = new ArrayList<Control>();
	
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
		latDeg.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, latDeg, false);
			}
		});
		latDeg.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = latDeg;
			}
		});
		
		latMin = new Text(shell, SWT.BORDER);
		latMin.setBounds(107, 112, 50, 18);
		latMin.setEnabled(false);
		latMin.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, latMin, true);
			}
		});
		latMin.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = latMin;
			}
		});
		
		latSec = new Text(shell, SWT.BORDER);
		latSec.setBounds(160, 112, 50, 18);
		latSec.setEnabled(false);
		latSec.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, latSec, true);
			}
		});
		latSec.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = latSec;
			}
		});
		
		longDeg = new Text(shell, SWT.BORDER);
		longDeg.setBounds(53, 133, 50, 18);
		longDeg.setText(String.valueOf(previous.getLongitude()));
		longDeg.setEnabled(false);
		longDeg.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, longDeg, false);
			}
		});
		longDeg.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = longDeg;
			}
		});
		
		longMin = new Text(shell, SWT.BORDER);
		longMin.setBounds(107, 133, 50, 18);
		longMin.setEnabled(false);
		longMin.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, longMin, true);
			}
		});
		longMin.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = longMin;
			}
		});
		
		longSec = new Text(shell, SWT.BORDER);
		longSec.setBounds(160, 133, 50, 18);
		longSec.setEnabled(false);
		longSec.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				handler(evt, longSec, true);
			}
		});
		longSec.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = longSec;
			}
		});
		
		alt = new Label(shell, SWT.NONE);
		alt.setBounds(20, 174, 16, 13);
		alt.setText("alt:");
		
		altText = new Text(shell, SWT.BORDER);
		altText.setBounds(53, 171, 75, 18);
		altText.setText(String.valueOf(previous.getAltitude()));
		altText.setEnabled(false);
		altText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				if (evt.keyCode == 16777296 || evt.keyCode == 13) {
					try {
						double altVal = Double.parseDouble(altText.getText());
						//System.out.println(altVal);
						if (altVal >= 0) {
							action.setEnabled(true);
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
		});
		altText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				current = altText;
			}
		});
		
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
		
		min = new Label(shell, SWT.NONE);
		min.setText("min");
		min.setBounds(117, 93, 37, 13);
		
		lat = new Label(shell, SWT.NONE);
		lat.setText("lat:");
		lat.setBounds(20, 112, 37, 13);
		
		lon = new Label(shell, SWT.NONE);
		lon.setText("long:");
		lon.setBounds(20, 133, 37, 13);
		
		order.add(latDeg);
		order.add(latMin);
		if (minsec) {
			order.add(latSec);
		}
		order.add(longDeg);
		order.add(longMin);
		if (minsec) {
			order.add(longSec);
		}
		order.add(altText);
		order.add(action);
		current = latDeg;
		
		Label lblNewLabel = new Label(shell, SWT.WRAP);
		lblNewLabel.setBounds(10, 10, 188, 77);
		lblNewLabel.setText("latitude is between -90 and 90 degrees.  longitude is between -180 and 180.  east is + and west is -    press enter to move to the next field");
		
		Label lblkm = new Label(shell, SWT.NONE);
		lblkm.setBounds(134, 174, 23, 13);
		lblkm.setText("(km)");
		
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
