package minesweeper_refactoring.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import minesweeper_refactoring.GameEventExec;

public class EventMenu implements ActionListener {
	
	private GameEventExec exec;

	public EventMenu(GameEventExec exec) {
		this.exec = exec;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem menuItem = (JMenuItem) e.getSource();
		System.out.println("menu이벤트 분리");

		if (menuItem.getName().equals("New Game")) {
			if (exec.getIsGamePlaying()) {
				int startNew = exec.windowNewGameDialog();
				
				switch (startNew) {
				case JOptionPane.YES_OPTION:

					exec.menuEventNewGame();
					break;

				case JOptionPane.NO_OPTION:
					exec.menuEventRestartGame();
					break;

				case JOptionPane.CANCEL_OPTION:
					break;
				}
			}
		}

		else if (menuItem.getName().equals("Exit")) {
			/*
			 * dispose와 ui.dispatchEvent(new WindowEvent(ui, WindowEvent.WINDOW_CLOSING));의 차이
			 * 둘다 해당 window를 closing한다는 점에서는 동일하지만 차이점이 존재한다
			 * dispose - window closing event를 발생시키지 않고 모든자원을 강제로 OS에 반환한다
			 * ui.dispatchEvent(new WindowEvent(ui, WindowEvent.WINDOW_CLOSING)) - window closing이벤트를 발생시켜 closing을 수행한다
			 */
			
			/*
			 * Event에서 가져와야 하는정보
			 * 1.TopFrame정보 - window closing이벤트 fire를 위해 필요
			 */
			exec.fireWindowClosingEvent();
		}

		// Statistics
		else {
			exec.menuEventStatistics();
		}
	}
}
