package minesweeper_refactoring;

import java.awt.Color;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.SwingWorker;

import minesweeper_refactoring.ui.CellBtn;
import minesweeper_refactoring.ui.UIWindow;

/*
 * Event Listener에 전달할 객체 Lister에서 실제 지뢰찾기에서 수행되어야 하는 동작을 정의
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
	
	//window 종료시 저장
	public void windowEventQuitSave() {
		gui.interruptTimer();
		game.getDBUtil().saveScore(score);
		windowClosingSaveDialog();
	}
	
	//window 종료시 저장안함
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
	
	//window closing event 발생
	public void fireWindowClosingEvent() {
		/*
		 * dispose와 ui.dispatchEvent(new WindowEvent(ui, WindowEvent.WINDOW_CLOSING));의 차이
		 * 둘다 해당 window를 closing한다는 점에서는 동일하지만 차이점이 존재한다
		 * dispose - window closing event를 발생시키지 않고 모든자원을 강제로 OS에 반환한다
		 * ui.dispatchEvent(new WindowEvent(ui, WindowEvent.WINDOW_CLOSING)) - window closing이벤트를 발생시켜 closing을 수행한다
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
	
	//왼쪽클릭 처리
	public void leftClick(CellBtn button) {
		//F(깃발표시) 또는 ""(아직 클릭되지 않음) 상태일때만 left click을 실행
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
	
	//오른쪽 클릭 처리
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
	
	//New Game 선택시 단순 dialog 표시
	public int windowNewGameDialog() {
		String title = "New Game";
		String dialogContent = "What do you want to do with the game in progress?";
		Object[] options = { "Quit and Start a New Game", "Restart", "Keep Playing" };
		Object initValue = options[2];
		
		return gui.getUIDialog().getOptionYNCancelDialog(title, dialogContent, options, initValue);
	}
	
	//window closing시 선택 단순 dialog 표시
	public int windowClosingOptionDialog() {
		String title = "Game Exit";
		String dialogContent = "What do you want to do with the game in progress?";
		Object[] options = { "Save", "Don't Save", "Cancel" };
		Object initValue = options[2];
		
		return gui.getUIDialog().getOptionYNCancelDialog(title, dialogContent, options, initValue);
	}
	
	//window closing시 저장상태를 표시하는 dialog 표시
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
