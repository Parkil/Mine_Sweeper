package minesweeper_refactoring.event;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;
import minesweeper_refactoring.GameEventExec;

public class EventWindow extends WindowAdapter {
	private GameEventExec exec;

	public EventWindow(GameEventExec exec) {
		this.exec = exec;
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		if (exec.getIsGamePlaying()) {
			int quit = exec.windowClosingOptionDialog();
			
			switch (quit) {
				// save
				case JOptionPane.YES_OPTION:
					exec.windowEventQuitSave();
					System.exit(0);
					break;
	
				// dont save
				case JOptionPane.NO_OPTION:
					exec.windowEventQuitNoSave();
					System.exit(0);
					break;
	
				case JOptionPane.CANCEL_OPTION:
					break;
			}
		} else {
			System.exit(0);
		}
	}
}
