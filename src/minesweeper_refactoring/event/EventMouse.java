package minesweeper_refactoring.event;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import minesweeper_refactoring.GameEventExec;
import minesweeper_refactoring.ui.CellBtn;

public class EventMouse extends MouseAdapter {
	
	private GameEventExec exec;

	public EventMouse(GameEventExec exec) {
		this.exec = exec;
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (!exec.getIsGamePlaying()) {
			exec.setGamePlaying();
		}
		
		//플레이 중
		if (exec.getIsGamePlaying()) {
			CellBtn button = (CellBtn) e.getSource();
			
			if (SwingUtilities.isLeftMouseButton(e)) { //left click
				exec.leftClick(button);
			} else if (SwingUtilities.isRightMouseButton(e)) { //right click
				exec.rightClick(button);
			}
			
			exec.checkGame();
		}
	}
}
