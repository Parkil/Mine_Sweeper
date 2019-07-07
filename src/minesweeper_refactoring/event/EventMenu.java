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
		System.out.println("menu�̺�Ʈ �и�");

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
			 * dispose�� ui.dispatchEvent(new WindowEvent(ui, WindowEvent.WINDOW_CLOSING));�� ����
			 * �Ѵ� �ش� window�� closing�Ѵٴ� �������� ���������� �������� �����Ѵ�
			 * dispose - window closing event�� �߻���Ű�� �ʰ� ����ڿ��� ������ OS�� ��ȯ�Ѵ�
			 * ui.dispatchEvent(new WindowEvent(ui, WindowEvent.WINDOW_CLOSING)) - window closing�̺�Ʈ�� �߻����� closing�� �����Ѵ�
			 */
			
			/*
			 * Event���� �����;� �ϴ�����
			 * 1.TopFrame���� - window closing�̺�Ʈ fire�� ���� �ʿ�
			 */
			exec.fireWindowClosingEvent();
		}

		// Statistics
		else {
			exec.menuEventStatistics();
		}
	}
}
