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
	 * 현재 Event내의 구성요소
	 * 1.지뢰찾기에 관련된 정보 : 플레이여부,Cell 클래스정보,Board 클래스정보...
	 * 		->생성자에서 가져오게 처리
	 * 2.게임에 관련된 동작 : 지뢰클릭시 Gameover, 오른쪽 클릭시 깃발표시
	 * 		->일단 Game참조를 생성자에서 받아서 처리하고 나중에 분리
	 * 
	 * 현재 Event에서 UI클래스의 참조를 받아서 사용하는 부분이 있는데 이를 나중에는
	 * Event <-> GameEventExec <-> 실제 동작(내부로직,UI 작동) 구조로 변경
	 * 	Event에서는 GameEventExec의 메소드만 호출하고 UI동작이나 내부로직은 GameEventExec에서 각 클래스를 호출하는 방식으로 구현
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		//플레이 중이 아닐때 
		if (!exec.getIsGamePlaying()) {
			exec.setGamePlaying();
		}
		
		//플레이 중
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
				if (!"F".equals(button.getContent())) { // 깃발설정이 된 곳에서는 클릭이 되지 않도록 처리
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
			// Right Click - node.js포팅시 가장문제가 되는 부분, 우측클릭을 막을수는 있어도 우측클릭에 깃발표시 이벤트를 먹이는게 쉽지 않을듯
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
