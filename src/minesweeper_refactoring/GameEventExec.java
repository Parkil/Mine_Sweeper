package minesweeper_refactoring;

import java.awt.Color;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.SwingWorker;

import minesweeper_refactoring.ui.CellBtn;
import minesweeper_refactoring.ui.UIWindow;

/**
 * @author alkain77
 * Define the behavior used swing events
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
	
	public void windowEventQuitSave() {
		gui.interruptTimer();
		game.getDBUtil().saveScore(score);
		windowClosingSaveDialog();
	}
	
	public void windowEventQuitNoSave() {
		score.incGamesPlayed();
		game.getDBUtil().saveScore(score);
	}
	
	public void execReset() {
		game.resetGame();
		score.incGamesPlayed();
		game.getDBUtil().saveScore(score);
	}
	
	public void menuEventStatistics() {
		game.showScore();
	}
	
	public void fireWindowClosingEvent() {
		gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
	}
	
	public void setGamePlaying() {
		gui.startTimer();
		playing = true;
	}
	
	public boolean getIsGamePlaying() {
		return playing;
	}
	
	public void checkGame() {
		if (game.isFinished()) {
			game.gameWon();
		}
	}
	
	public void leftClick(CellBtn button) {
		//Execute left click only when CellBtn Contents "F"(Flagged) or ""(Unclicked)
		if (!"F".equals(button.getContent()) && "".equals(button.getContent().trim())) {
			String[] coord = button.getName().split(",");

			int x = Integer.parseInt(coord[0]);
			int y = Integer.parseInt(coord[1]);

			boolean isMine = button.isMineBuried();
			int surroundMineCnt = button.getSurroundingMineCnt();
			
			button.setIcon(null);

			// Mine is clicked.
			if (isMine) {
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
	
	public int windowNewGameDialog() {
		String title = "New Game";
		String dialogContent = "What do you want to do with the game in progress?";
		Object[] options = { "Quit and Start a New Game", "Restart", "Keep Playing" };
		Object initValue = options[2];
		
		return gui.getUIDialog().getOptionYNCancelDialog(title, dialogContent, options, initValue);
	}
	
	public int windowClosingOptionDialog() {
		String title = "Game Exit";
		String dialogContent = "What do you want to do with the game in progress?";
		Object[] options = { "Save", "Don't Save", "Cancel" };
		Object initValue = options[2];
		
		return gui.getUIDialog().getOptionYNCancelDialog(title, dialogContent, options, initValue);
	}
	
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
		dialog.pack();
		dialog.setVisible(true);
	}
}
