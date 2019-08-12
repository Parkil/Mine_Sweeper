package minesweeper_refactoring.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import minesweeper_refactoring.GameEventExec;

public class EventMenu implements ActionListener {
	
	private GameEventExec exec;

	public EventMenu(GameEventExec exec) {
		this.exec = exec;
	}
	
	private void execNewGameMenu() {
		if (exec.getIsGamePlaying()) {
			int startNew = exec.windowNewGameDialog();
			
			switch (startNew) {
			case JOptionPane.YES_OPTION:
				exec.execReset();
				break;

			case JOptionPane.NO_OPTION:
				exec.execReset();
				break;

			case JOptionPane.CANCEL_OPTION:
				break;
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem menuItem = (JMenuItem) e.getSource();
		
		switch(menuItem.getName()) {
			case "New Game" : execNewGameMenu(); //New Game
			break;
			
			case "Exit" : exec.fireWindowClosingEvent(); //Exit
			break;
			
			default : exec.menuEventStatistics(); //Statistics
			break;
		}
	}
}
