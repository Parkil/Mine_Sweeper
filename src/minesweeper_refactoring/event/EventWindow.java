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
			int dialogOption = exec.windowClosingOptionDialog();
			
			switch (dialogOption) {
				// save
				case JOptionPane.YES_OPTION:
					exec.windowEventQuitSave();
					break;
	
				// dont save
				case JOptionPane.NO_OPTION:
					exec.windowEventQuitNoSave();
					break;
	
				case JOptionPane.CANCEL_OPTION:
					return; //cancel 선택시 System.exit(0)가 실행되지 않도록 처리
			}
		}
		
		System.exit(0);
	}
}
