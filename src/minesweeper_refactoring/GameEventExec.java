package minesweeper_refactoring;

import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.SwingWorker;

import minesweeper_refactoring.ui.UIDialog;

/*
 * Event Listener에 전달할 객체 Lister에서 실제 지뢰찾기에서 수행되어야 하는 동작을 정의
 * 
 */
public class GameEventExec {
	private boolean playing = false;

	private Board board;

	private Score score;
	
	private UI gui;
	
	private Game game;
	
	private UIDialog uiDialog;
	
	public GameEventExec(Board board, Score score, UI gui, Game game) {
		this.board = board;
		this.score = score;
		this.gui = gui;
		this.game = game;
		this.uiDialog = new UIDialog(gui);
	}
	
	//window 종료시 저장
	public void windowEventQuitSave() {
		gui.interruptTimer();
		score.save();
		windowClosingSaveDialog();
	}
	
	//window 종료시 저장안함
	public void windowEventQuitNoSave() {
		score.incGamesPlayed();
		score.save();
	}
	
	//menu > Start New Game
	public void menuEventNewGame() {
		game.newGame(); //--
		score.incGamesPlayed(); // --
		score.save(); // --
	}
	
	//menu > restart
	public void menuEventRestartGame() {
		score.incGamesPlayed(); // --
		score.save(); // --
		game.restartGame(); // --
	}
	
	//menu > statistics
	public void menuEventStatistics() {
		game.showScore(); // --
	}
	
	//window closing event 발생
	public void fireWindowClosingEvent() {
		gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING)); // --
	}
	
	//set game playing
	public void setGamePlaying() {
		playing = true;
	}
	
	//set game end
	public void setGameEnd() {
		playing = false;
	}
	
	//get current game status
	public boolean getIsGamePlaying() {
		return playing;
	}
	
	//return mine exists by coordinates 
	public boolean getMineExists(int x, int y) {
		return board.getCells()[x][y].getMine();
	}
	
	//return surround mine count by coordinates
	public int getSurroundMineCnt(int x, int y) {
		return board.getCells()[x][y].getSurroundingMines();
	}
	
	//get cell contents
	public String getCellContent(int x, int y) {
		return board.getCells()[x][y].getContent();
	}
	
	//set cell contents
	public void setCellContent(int x, int y, String content) {
		board.getCells()[x][y].setContent(content);
	}
	
	public void fireGameLost() {
		setGameEnd();
		game.gameLost();
	}
	
	public void fireFindZeros(int x, int y) {
		game.findZeroes(x, y);
	}
	
	public void incMines() {
		gui.incMines();
	}
	
	public void decMines() {
		gui.decMines();
	}
	
	public void checkGame() {
		game.checkGame();
	}
	
	public UI getGui() {
		return gui;
	}
	
	public void startTimer() {
		gui.startTimer();
	}
	
	//New Game 선택시 단순 dialog 표시
	public int windowNewGameDialog() {
		String title = "New Game";
		String dialogContent = "What do you want to do with the game in progress?";
		Object[] options = { "Quit and Start a New Game", "Restart", "Keep Playing" };
		Object initValue = options[2];
		
		return uiDialog.getOptionDialog(title, dialogContent, options, initValue);
	}
	
	//window closing시 선택 단순 dialog 표시
	public int windowClosingOptionDialog() {
		String title = "Game Exit";
		String dialogContent = "What do you want to do with the game in progress?";
		Object[] options = { "Save", "Don't Save", "Cancel" };
		Object initValue = options[2];
		
		return uiDialog.getOptionDialog(title, dialogContent, options, initValue);
	}
	
	//window closing시 저장상태를 표시하는 dialog 표시
	public void windowClosingSaveDialog() {
		JDialog dialog = uiDialog.windowClosingSaveDialog();
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				System.out.println("상태 저장중....");
				board.saveGame(gui.getTimePassed(), gui.getMines());
				return null;
			}

			@Override
			protected void done() {
				System.out.println("상태저장 완료");
				dialog.dispose();
			}
		};

		worker.execute();
		dialog.setVisible(true);
	}
}
