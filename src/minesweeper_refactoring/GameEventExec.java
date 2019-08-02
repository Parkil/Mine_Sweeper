package minesweeper_refactoring;

import java.awt.Color;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.SwingWorker;

import minesweeper_refactoring.ui.CellBtn;
import minesweeper_refactoring.ui.UIWindow;

/*
 * Event Listener�� ������ ��ü Lister���� ���� ����ã�⿡�� ����Ǿ�� �ϴ� ������ ����
 * 
 */
public class GameEventExec {
	private boolean playing = false;

	private Score score;
	
	private UIWindow gui;
	
	private Game game;
	
	public GameEventExec(Score score, UIWindow gui, Game game) {
		this.score = score;
		this.gui = gui;
		this.game = game;
	}
	
	//window ����� ����
	public void windowEventQuitSave() {
		gui.interruptTimer();
		game.getDBUtil().saveScore(score);
		windowClosingSaveDialog();
	}
	
	//window ����� �������
	public void windowEventQuitNoSave() {
		score.incGamesPlayed();
		game.getDBUtil().saveScore(score);
	}
	
	//menu > Start New Game
	public void menuEventNewGame() {
		game.newGame();
		score.incGamesPlayed();
		game.getDBUtil().saveScore(score);
	}
	
	//menu > restart
	public void menuEventRestartGame() {
		score.incGamesPlayed();
		game.getDBUtil().saveScore(score);
		game.restartGame();
	}
	
	//menu > statistics
	public void menuEventStatistics() {
		game.showScore();
	}
	
	//window closing event �߻�
	public void fireWindowClosingEvent() {
		/*
		 * dispose�� ui.dispatchEvent(new WindowEvent(ui, WindowEvent.WINDOW_CLOSING));�� ����
		 * �Ѵ� �ش� window�� closing�Ѵٴ� �������� ���������� �������� �����Ѵ�
		 * dispose - window closing event�� �߻���Ű�� �ʰ� ����ڿ��� ������ OS�� ��ȯ�Ѵ�
		 * ui.dispatchEvent(new WindowEvent(ui, WindowEvent.WINDOW_CLOSING)) - window closing�̺�Ʈ�� �߻����� closing�� �����Ѵ�
		 */
		gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
	}
	
	//set game playing
	public void setGamePlaying() {
		gui.startTimer();
		playing = true;
	}
	
	//get current game status
	public boolean getIsGamePlaying() {
		return playing;
	}
	
	
	public void checkGame() {
		game.checkGame();
	}
	
	//����Ŭ�� ó��
	public void leftClick(CellBtn button) {
		//F(���ǥ��) �Ǵ� ""(���� Ŭ������ ����) �����϶��� left click�� ����
		if (!"F".equals(button.getContent()) && "".equals(button.getContent().trim())) {
			String[] coord = button.getName().split(",");

			int x = Integer.parseInt(coord[0]);
			int y = Integer.parseInt(coord[1]);

			boolean isMine = button.isMineBuried();
			int surroundMineCnt = button.getSurroundingMineCnt();
			
			button.setIcon(null);

			// Mine is clicked.
			if (isMine) {
				// red mine
				button.setIcon(gui.getIconRedMine());
				button.setBackground(Color.red);
				button.setContent("M");
				playing = false;
				game.gameLost();
			} else {
				button.setContent(Integer.toString(surroundMineCnt));
				button.setText(Integer.toString(surroundMineCnt));
				gui.setTextColor(button); 

				if (surroundMineCnt == 0) {
					button.setBackground(Color.white);
					button.setText("");
					gui.findZeroes(x, y);
				} else {
					button.setBackground(Color.white);
				}
			}
		}
	}
	
	//������ Ŭ�� ó��
	public void rightClick(CellBtn button) {
		if ("F".equals(button.getContent())) {
			button.setContent("");
			button.setText("");
			button.setBackground(new Color(0, 110, 140));

			button.setIcon(gui.getIconTile());
			gui.incMines();
		} else if ("".equals(button.getContent())) {
			button.setContent("F");;
			button.setBackground(Color.blue);

			button.setIcon(gui.getIconFlag());
			gui.decMines();
		}
	}
	
	//New Game ���ý� �ܼ� dialog ǥ��
	public int windowNewGameDialog() {
		String title = "New Game";
		String dialogContent = "What do you want to do with the game in progress?";
		Object[] options = { "Quit and Start a New Game", "Restart", "Keep Playing" };
		Object initValue = options[2];
		
		return gui.getUIDialog().getOptionYNCancelDialog(title, dialogContent, options, initValue);
	}
	
	//window closing�� ���� �ܼ� dialog ǥ��
	public int windowClosingOptionDialog() {
		String title = "Game Exit";
		String dialogContent = "What do you want to do with the game in progress?";
		Object[] options = { "Save", "Don't Save", "Cancel" };
		Object initValue = options[2];
		
		return gui.getUIDialog().getOptionYNCancelDialog(title, dialogContent, options, initValue);
	}
	
	//window closing�� ������¸� ǥ���ϴ� dialog ǥ��
	public void windowClosingSaveDialog() {
		JDialog dialog = gui.getUIDialog().windowClosingSaveDialog();
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				game.getDBUtil().saveGame(gui.getTimePassed(), gui.getMines());
				return null;
			}

			@Override
			protected void done() {
				dialog.dispose();
			}
		};

		worker.execute();
		dialog.setVisible(true);
	}
}
