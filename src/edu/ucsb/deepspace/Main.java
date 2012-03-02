package edu.ucsb.deepspace;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.ucsb.deepspace.gui.MainWindow;

public class Main {
	private static MainWindow window;
	private static Stage stage;
	
	public static void main(String[] args) {
		stage = Stage.getInstance();
		
		Shell shell = new Shell(Display.getDefault(), SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		window = new MainWindow(shell, SWT.NULL, stage);
		try {
			stage.initialize(window);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		window.alive();
	}
}