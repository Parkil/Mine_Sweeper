package minesweeper_refactoring.event;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import minesweeper_refactoring.GameEventExec;

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
			// Get the button's name
			JButton button = (JButton) e.getSource();

			String[] co = button.getName().split(","); //�̰� Name���� x,y��ǥ�� �������� ���� Cell�ʱ�ȭ�� JButton�� ���� �ʱ�ȭ�ϰ� JButton��ü�� Cell��ü�� �ҷ��ͼ� Cell���� ǥ�� �������� ó��

			int x = Integer.parseInt(co[0]);
			int y = Integer.parseInt(co[1]);

			// Get cell information.
			boolean isMine = exec.getMineExists(x, y);
			int neighbours = exec.getSurroundMineCnt(x, y);

			// Left Click
			if (SwingUtilities.isLeftMouseButton(e)) {
				if (!exec.getCellContent(x, y).equals("F")) { // ��߼����� �� �������� Ŭ���� ���� �ʵ��� ó��
					button.setIcon(null);

					// Mine is clicked.
					if (isMine) {
						// red mine
						button.setIcon(exec.getGui().getIconRedMine());
						button.setBackground(Color.red);
						exec.setCellContent(x, y, "M");
						exec.fireGameLost();
					} else {
						// The player has clicked on a number.
						exec.setCellContent(x, y, Integer.toString(neighbours));
						button.setText(Integer.toString(neighbours));
						exec.getGui().setTextColor(button); 

						if (neighbours == 0) {
							// Show all surrounding cells.
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
				
				if (exec.getCellContent(x, y).equals("F")) {
					exec.setCellContent(x, y, "");
					button.setText("");
					button.setBackground(new Color(0, 110, 140));

					// simple blue

					button.setIcon(exec.getGui().getIconTile());
					exec.getGui().incMines(); // --
				} else if (exec.getCellContent(x, y).equals("")) {
					exec.setCellContent(x, y, "F");
					button.setBackground(Color.blue);

					button.setIcon(exec.getGui().getIconFlag());
					exec.getGui().decMines(); //--
				}
			}
			
			exec.checkGame();
		}
	}
}
