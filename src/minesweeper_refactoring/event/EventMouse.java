package minesweeper_refactoring.event;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import minesweeper_refactoring.Cell;
import minesweeper_refactoring.GameEventExec;
import minesweeper_refactoring.ui.CellBtn;

public class EventMouse extends MouseAdapter {
	
	private GameEventExec exec;

	public EventMouse(GameEventExec exec) {
		this.exec = exec;
	}
	
	/*
	 * ���� Event���� �������
	 * 1.����ã�⿡ ���õ� ���� : �÷��̿���,Cell Ŭ��������,Board Ŭ��������...
	 * 		->�����ڿ��� �������� ó��
	 * 2.���ӿ� ���õ� ���� : ����Ŭ���� Gameover, ������ Ŭ���� ���ǥ��
	 * 		->�ϴ� Game������ �����ڿ��� �޾Ƽ� ó���ϰ� ���߿� �и�
	 * 
	 * ���� Event���� UIŬ������ ������ �޾Ƽ� ����ϴ� �κ��� �ִµ� �̸� ���߿���
	 * Event <-> GameEventExec <-> ���� ����(���η���,UI �۵�) ������ ����
	 * 	Event������ GameEventExec�� �޼ҵ常 ȣ���ϰ� UI�����̳� ���η����� GameEventExec���� �� Ŭ������ ȣ���ϴ� ������� ����
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		//�÷��� ���� �ƴҶ� 
		if (!exec.getIsGamePlaying()) {
			exec.setGamePlaying();
		}
		
		//�÷��� ��
		if (exec.getIsGamePlaying()) {
			CellBtn button = (CellBtn) e.getSource();

			String[] coord = button.getName().split(",");

			int x = Integer.parseInt(coord[0]);
			int y = Integer.parseInt(coord[1]);

			// Get cell information.
			//boolean isMine = exec.getMineExists(x, y);
			//int surroundMineCnt = exec.getSurroundMineCnt(x, y);
			
			boolean isMine = button.isMineBuried();
			int surroundMineCnt = button.getSurroundingMineCnt();
			
			//System.out.println(x+","+y);
			//System.out.println(surroundMineCnt);
			
			// Left Click
			if (SwingUtilities.isLeftMouseButton(e)) {
				if (!"F".equals(button.getContent())) { // ��߼����� �� �������� Ŭ���� ���� �ʵ��� ó��
					button.setIcon(null);

					// Mine is clicked.
					if (isMine) {
						// red mine
						button.setIcon(exec.getGui().getIconRedMine());
						button.setBackground(Color.red);
						button.setContent("M");
						exec.fireGameLost();
					} else {
						// The player has clicked on a number
						button.setContent(Integer.toString(surroundMineCnt));
						//exec.setCellContent(x, y, Integer.toString(surroundMineCnt));
						button.setText(Integer.toString(surroundMineCnt));
						exec.getGui().setTextColor(button); 

						if (surroundMineCnt == 0) {
							button.setBackground(Color.lightGray);
							button.setText("");
							exec.fireFindZeros(x, y);
						} else {
							button.setBackground(Color.lightGray);
						}
					}
				}
			}
			// Right Click - node.js���ý� ���幮���� �Ǵ� �κ�, ����Ŭ���� �������� �־ ����Ŭ���� ���ǥ�� �̺�Ʈ�� ���̴°� ���� ������
			else if (SwingUtilities.isRightMouseButton(e)) {
				if ("F".equals(button.getContent())) {
					button.setContent("");
					button.setText("");
					button.setBackground(new Color(0, 110, 140));

					button.setIcon(exec.getGui().getIconTile());
					exec.getGui().incMines();
				} else if (exec.getCellContent(x, y).equals("")) {
					button.setContent("F");;
					button.setBackground(Color.blue);

					button.setIcon(exec.getGui().getIconFlag());
					exec.getGui().decMines();
				}
			}
			
			exec.checkGame();
		}
	}
}
