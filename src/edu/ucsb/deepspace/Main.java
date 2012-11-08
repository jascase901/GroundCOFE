package edu.ucsb.deepspace;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.ucsb.deepspace.gui.MainWindow;
import edu.ucsb.deepspace.tcp.CommandLine;

public class Main {
	private static MainWindow window;
	private static CommandLine cmd;
	private static StageInterface stage;
	private static Ui ui;
	
	public static void main(String[] args) {
		stage = Stage.getInstance();
		
		Stage.StageTypes StageTypes = stage.getType();
		Shell shell = new Shell(Display.getDefault(), SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		//ui =  new CommandLine(stage, StageTypes);
		ui = new MainWindow(shell, SWT.NULL, stage, StageTypes);
		try {
			stage.initialize(ui);
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ui.alive();



	}
}