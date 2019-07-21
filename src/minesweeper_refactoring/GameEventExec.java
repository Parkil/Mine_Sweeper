package minesweeper_refactoring;

import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingWorker;

import minesweeper_refactoring.ui.UIDialog;

/*
 * Event Listener�� ������ ��ü Lister���� ���� ����ã�⿡�� ����Ǿ�� �ϴ� ������ ����
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
	
	//window ����� ����
	public void windowEventQuitSave() {
		gui.interruptTimer();
		score.save();
		windowClosingSaveDialog();
	}
	
	//window ����� �������
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
		startTimer();
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
		gui.findZeroes(x, y);
	}
	
	public void incMines() {
		gui.incMines();
	}
	
	public void decMines() {
		gui.decMines();
	}
	
	public void checkGame() {
		//game.checkGame();
	}
	
	public UI getGui() {
		return gui;
	}
	
	public void startTimer() {
		gui.startTimer();
	}
	
	public Cell getCellByJButton(JButton btn) {
		return board.getCellByJButton(btn);
	}
	
	//New Game ���ý� �ܼ� dialog ǥ��
	public int windowNewGameDialog() {
		String title = "New Game";
		String dialogContent = "What do you want to do with the game in progress?";
		Object[] options = { "Quit and Start a New Game", "Restart", "Keep Playing" };
		Object initValue = options[2];
		
		return uiDialog.getOptionDialog(title, dialogContent, options, initValue);
	}
	
	//window closing�� ���� �ܼ� dialog ǥ��
	public int windowClosingOptionDialog() {
		String title = "Game Exit";
		String dialogContent = "What do you want to do with the game in progress?";
		Object[] options = { "Save", "Don't Save", "Cancel" };
		Object initValue = options[2];
		
		return uiDialog.getOptionDialog(title, dialogContent, options, initValue);
	}
	
	//window closing�� ������¸� ǥ���ϴ� dialog ǥ��
	public void windowClosingSaveDialog() {
		JDialog dialog = uiDialog.windowClosingSaveDialog();
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				//board.saveGame(gui.getTimePassed(), gui.getMines());
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
