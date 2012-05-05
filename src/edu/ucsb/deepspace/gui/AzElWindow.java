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
import edu.ucsb.deepspace.Stage;

public class AzElWindow {
	
	private Shell shell;
	private Button action;
	
	private Text azDeg, azMin, azSec;
	private Text elDeg, elMin, elSec;
	private Label el, min, az, sec, deg, lblDirections;

	
	private boolean minsec;
	private final String type;
	private final Stage stage;

	public AzElWindow(boolean minsec, String type, Stage stage) {
		shell = new Shell(Display.getDefault(), SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX);
		shell.setSize(213, 294);
		this.minsec = minsec;
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
		
		azDeg = new Text(shell, SWT.BORDER);
		azDeg.setBounds(53, 147, 37, 18);
		azDeg.setMessage("");
		azDeg.addModifyListener(new ModifyListener(){

		      public void modifyText(ModifyEvent evt1) {
		      
		        checkHandler(evt1, azDeg,minsec );
		        
		      }});
		

		
		azMin = new Text(shell, SWT.BORDER);
		azMin.setBounds(96, 147, 37, 18);
		//azMin.setEnabled(false);
		azMin.setMessage("");
		azMin.addModifyListener(new ModifyListener(){

		      public void modifyText(ModifyEvent evt1) {
		      
		        checkHandler(evt1, azDeg,minsec );
		        
		      }});
		

		azSec = new Text(shell, SWT.BORDER);
		azSec.setBounds(139, 147, 37, 18);
		
		azSec.setMessage("");
		azSec.addModifyListener(new ModifyListener(){

		      public void modifyText(ModifyEvent evt1) {
		      
		        checkHandler(evt1, azDeg,minsec );
		        
		      }});

		
		elDeg = new Text(shell, SWT.BORDER);
		elDeg.setBounds(53, 168, 37, 18);
		//elDeg.setEnabled(false);
		elDeg.setMessage("");
		elDeg.addModifyListener(new ModifyListener(){

		      public void modifyText(ModifyEvent evt1) {
		      
		        checkHandler(evt1, azDeg,minsec );
		        
		      }});
		

		elMin = new Text(shell, SWT.BORDER);
		elMin.setBounds(96, 168, 37, 18);
		elMin.setMessage("");
		elMin.addModifyListener(new ModifyListener(){

		      public void modifyText(ModifyEvent evt1) {
		      
		        checkHandler(evt1, azDeg,minsec );
		        
		      }});
		

		
		elSec = new Text(shell, SWT.BORDER);
		elSec.setBounds(139, 168, 37, 18);
		
		elSec.addModifyListener(new ModifyListener(){

		      public void modifyText(ModifyEvent evt1) {
		      
		        checkHandler(evt1, azDeg,minsec );
		        
		      }});
		

		action = new Button(shell, SWT.PUSH | SWT.CENTER);
		if (type.equals("calibrate")) {
			action.setText("Calibrate");
		}
		else if (type.equals("gotopos")) {
			action.setText("Go to Position");
		}
		action.setBounds(49, 193, 100, 30);
		action.setEnabled(false);
		action.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent evt) {
				double azDegVal = Double.parseDouble(azDeg.getText());
				double azMinVal = Double.parseDouble(azMin.getText());
				double azSecVal;
				double elDegVal = Double.parseDouble(elDeg.getText());
				double elMinVal = Double.parseDouble(elMin.getText());
				double elSecVal;
				double az = azDegVal + azMinVal/60;
				double el = elDegVal + elMinVal/60;
				if (minsec) {
					azSecVal = Double.parseDouble(azSec.getText());
					elSecVal = Double.parseDouble(elSec.getText());
					az += azSecVal/3600;
					el += elSecVal/3600;
				}
				Coordinate c = new Coordinate(el, az);
				
				if (type.equals("calibrate")) {
					System.out.println(type);
					stage.calibrate(c);
				}
				else if (type.equals("gotopos")) {
					stage.goToPos(c);
				}
				close();
			}
		});
		
		
		deg = new Label(shell, SWT.NONE);
		deg.setText("deg");
		deg.setBounds(53, 128, 37, 13);
		
		sec = new Label(shell, SWT.NONE);
		sec.setText("sec");
		sec.setBounds(139, 128, 37, 13);
		
		min = new Label(shell, SWT.NONE);
		min.setText("min");
		min.setBounds(96, 128, 37, 13);
		
		az = new Label(shell, SWT.NONE);
		az.setText("az:");
		az.setBounds(10, 147, 37, 13);
		
		el = new Label(shell, SWT.NONE);
		el.setText("el:");
		el.setBounds(10, 168, 37, 13);
		

		if (!minsec) {
			
			sec.setVisible(false);
			azSec.setVisible(false);	
			elSec.setVisible(false);
		}
		
;
		
		lblDirections = new Label(shell, SWT.WRAP);
		lblDirections.setBounds(27, 27, 149, 80);
		//lblDirections.setText("azimuth is from 0 to 360.  positive is counter-clockwise.  el is currently 0 straight up.  positive is downwards.");
	}
	
	
	//Enables calibrate if all the fields are filled.
	public void checkHandler(ModifyEvent evt, Text text, boolean minOrSecFlag){
		
		boolean min_sec_check = false;
		if(minOrSecFlag){
			if(azSec.getText().equals("")||elSec.getText().equals("")){
				min_sec_check = true;
			}
		}
		if((elDeg.getText().equals("") || azDeg.getText().equals("")||azDeg.getText().equals("")
				||azMin.getText().equals("")||elMin.getText().equals("")||min_sec_check)){
			action.setEnabled(false);
			System.out.println("gah");

		}
		else{
			action.setEnabled(true);
		}


	}
		
}