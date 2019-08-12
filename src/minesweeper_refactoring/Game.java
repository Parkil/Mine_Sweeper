package minesweeper_refactoring;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.sql.Date;
import javafx.util.Pair;
import minesweeper_refactoring.Score.Time;
import minesweeper_refactoring.db.DBUtil;
import minesweeper_refactoring.ui.CellBtn;
import minesweeper_refactoring.ui.UIWindow;

/**
 * @author alkain77
 * Mine Sweeper Main Class
 */
public class Game {
	public boolean playing;
	
	public DBUtil util;

	public UIWindow gui;

	public Score score;
	
	private int rows = 9;
	private int cols = 9;
	private int totMineCnt = 10;

	public Game() {
		new Game(rows, cols, totMineCnt);
	}
	
	public Game(int rows, int cols, int totMineCnt) {
		this.rows = rows;
		this.cols = cols;
		this.totMineCnt = totMineCnt;

		gui = new UIWindow(rows, cols, totMineCnt);
		gui.setLook("Nimbus");

		this.playing = false;

		gui.setVisible(true);

		gui.setIcon();
		gui.hideAll();
		
		util = new DBUtil(this);
		
		score = new Score();
		util.populate(score);
		
		GameEventExec exec = new GameEventExec(score, gui, this);
		this.gui.setButtonListeners(exec);
		
		resumeGame();
	}

	/**
	 * resume game if save game exists
	 */
	@SuppressWarnings("rawtypes")
	public void resumeGame() {
		if (util.checkSave()) {
			int option = gui.getUIDialog().getOptionYNDialog("Do you want to continue your saved game?", "Saved Game Found", null, null);
			
			switch (option) {
				case JOptionPane.YES_OPTION:
					Pair p = util.loadSaveGame();
	
					gui.setButtonImages();
					gui.setTimePassed((int) p.getKey());
					gui.setMines((int) p.getValue());
					gui.startTimer();
	
					playing = true;
					break;
				case JOptionPane.NO_OPTION:
					util.deleteSavedGame();
					break;
				case JOptionPane.CLOSED_OPTION:
					util.deleteSavedGame();
					break;
			}
		}
	}

	/**
	 * initalize game status 
	 */
	public void resetGame() {
		this.playing = false;
		
		gui.resetBtn();
		gui.interruptTimer();
		gui.resetTimer();
		gui.initGame();
		gui.setMines(totMineCnt);
	}
	
	/**
	 * Releated Actions on Game Won
	 */
	public void gameWon() {
		score.incCurrentStreak();
		score.incCurrentWinningStreak();
		score.incGamesWon();
		score.incGamesPlayed();

		gui.interruptTimer();
		
		playing = false;
		showAll();

		util.saveScore(score);
		
		ArrayList<Time> bTimes = score.getBestTimes();
		score.addTime(gui.getTimePassed(), new Date(System.currentTimeMillis()));
		
		HashMap<String,Object> retMap = gui.getUIDialog().gameWonDialog();
		
		JDialog dialog = (JDialog)retMap.get("dialog");
		
		JLabel bestTimeAnn = (JLabel)retMap.get("bestTimeAnn");
		JLabel time = (JLabel)retMap.get("time");
		JLabel bestTime = (JLabel)retMap.get("bestTime");
		JLabel gPlayed = (JLabel)retMap.get("gPlayed");
		JLabel gWon = (JLabel)retMap.get("gWon");
		JLabel gPercentage = (JLabel)retMap.get("gPercentage");
		
		if (bTimes.isEmpty() || (bTimes.get(0).getTimeValue() > gui.getTimePassed())) {
			bestTimeAnn.setText("    You have the fastest time for this difficulty level!    ");
		}else {
			bestTimeAnn.setText("        ");
			
		}
		
		time.setText("  Time:  " + Integer.toString(gui.getTimePassed()) + " seconds            Date:  "
				+ new Date(System.currentTimeMillis()));
		
		if (bTimes.isEmpty()) {
			bestTime.setText("  Best Time:  ---                  Date:  ---");
		} else {
			bestTime.setText("  Best Time:  " + bTimes.get(0).getTimeValue() + " seconds            Date:  "
					+ bTimes.get(0).getDateValue());
		}
		
		gPlayed.setText("  Games Played:  " + score.getGamesPlayed());
		gWon.setText("  Games Won:  " + score.getGamesWon());
		gPercentage.setText("  Win Percentage:  " + score.getWinPercentage() + "%");
		
		JButton exitBtn = (JButton)retMap.get("exitBtn");
		JButton playAgainBtn = (JButton)retMap.get("playAgainBtn");
		
		exitBtn.addActionListener((ActionEvent e) -> {
			dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
		});
		playAgainBtn.addActionListener((ActionEvent e) -> {
			dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
		});
		
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				dialog.dispose();
				resetGame();
			}
		});
		
		/*
		 * dialog.pack() and dialog.setVisible(true) must be declared in same place to code work normally
		 */
		dialog.pack();
		dialog.setVisible(true);
	}
	
	/**
	 * Releated Actions on Game Over
	 */
	public void gameLost() {
		score.decCurrentStreak();
		score.incCurrentLosingStreak();
		score.incGamesPlayed();

		gui.interruptTimer();

		playing = false;
		showAll();

		util.saveScore(score);
		
		ArrayList<Time> bTimes = score.getBestTimes();
		
		HashMap<String,Object> retMap = gui.getUIDialog().gameLostDialog();
		
		JDialog dialog = (JDialog)retMap.get("dialog");
		
		JLabel time = (JLabel)retMap.get("time");
		JLabel bestTime = (JLabel)retMap.get("bestTime");
		JLabel gPlayed = (JLabel)retMap.get("gPlayed");
		JLabel gWon = (JLabel)retMap.get("gWon");
		JLabel gPercentage = (JLabel)retMap.get("gPercentage");
		
		time.setText("  Time:  " + Integer.toString(gui.getTimePassed()) + " seconds");
		
		if(!bTimes.isEmpty()) {
			bestTime.setText("  Best Time:  " + bTimes.get(0).getTimeValue() + " seconds            Date:  "
					+ bTimes.get(0).getDateValue());
		}else {
			bestTime.setText("                        ");
		}
		
		gPlayed.setText("  Games Played:  " + score.getGamesPlayed());
		gWon.setText("  Games Won:  " + score.getGamesWon());
		gPercentage.setText("  Win Percentage:  " + score.getWinPercentage() + "%");
		
		
		JButton exitBtn = (JButton)retMap.get("exitBtn");
		JButton restartBtn = (JButton)retMap.get("restartBtn");
		JButton playAgainBtn = (JButton)retMap.get("playAgainBtn");
		
		exitBtn.addActionListener((ActionEvent e) -> {
			dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
		});
		restartBtn.addActionListener((ActionEvent e) -> {
			dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
		});
		playAgainBtn.addActionListener((ActionEvent e) -> {
			dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
		});
		
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				dialog.dispose();
				resetGame();
			}
		});
		
		/*
		 * dialog.pack() and dialog.setVisible(true) must be declared in same place to code work normally
		 */
		dialog.pack();
		dialog.setVisible(true);
	}

	/**
	 * show best records
	 */
	public void showScore() {
		ArrayList<Time> bTimes = score.getBestTimes();
		
		HashMap<String,Object> retMap = gui.getUIDialog().showScoreDialog();
		
		JDialog dialog = (JDialog)retMap.get("dialog");
		
		if (bTimes.isEmpty()) {
			for(int i=1 ; i<=5 ; i++) {
				JLabel bestTime = (JLabel)retMap.get("bestTime"+i);
				bestTime.setText("                               ");
			}
		} else {
			for(int i=0 ; i<bTimes.size() ; i++) {
				JLabel bestTime = (JLabel)retMap.get("bestTime"+(i+1));
				bestTime.setText("  " + bTimes.get(i).getTimeValue() + "           " + bTimes.get(i).getDateValue());
			}
			
			for(int j=bTimes.size()+1 ; j <= 5; j++) {
				JLabel bestTime = (JLabel)retMap.get("bestTime"+j);
				bestTime.setText("                               ");
			}
		}
		
		JLabel gPlayed = (JLabel)retMap.get("gPlayed");
		JLabel gWon = (JLabel)retMap.get("gWon");
		JLabel gPercentage = (JLabel)retMap.get("gPercentage");
		JLabel lWin = (JLabel)retMap.get("lWin");
		JLabel lLose = (JLabel)retMap.get("lLose");
		JLabel currentStreak = (JLabel)retMap.get("currentStreak");
		
		gPlayed.setText("  Games Played:  " + score.getGamesPlayed());
		gWon.setText("  Games Won:  " + score.getGamesWon());
		gPercentage.setText("  Win Percentage:  " + score.getWinPercentage() + "%");
		lWin.setText("  Longest Winning Streak:  " + score.getLongestWinningStreak());
		lLose.setText("  Longest Losing Streak:  " + score.getLongestLosingStreak());
		currentStreak.setText("  Current Streak:  " + score.getCurrentStreak());
		
		JButton closeBtn = (JButton)retMap.get("closeBtn");
		JButton resetBtn = (JButton)retMap.get("resetBtn");
		
		if (score.getGamesPlayed() == 0) {
			resetBtn.setEnabled(false);
		}
		
		closeBtn.addActionListener((ActionEvent e) -> {
			dialog.dispose();
		});
		
		resetBtn.addActionListener((ActionEvent e) -> {
			int option = gui.getUIDialog().getOptionYNDialog("Do you want to reset all your statistics to zero?", "Reset Statistics", null, null);

			switch (option) {
			case JOptionPane.YES_OPTION:
				score.resetScore();
				util.saveScore(score);
				dialog.dispose();
				showScore();
				break;
			case JOptionPane.NO_OPTION:
				break;
			}
		});
		
		dialog.pack();
		dialog.setVisible(true);
	}

	/**
	 * show all cells in board
	 */
	private void showAll() {
		String cellContent;

		CellBtn cellBtnArr[][] = gui.getCellBtnArr();

		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cellContent = cellBtnArr[x][y].getContent();

				// Is the cell still unrevealed
				if (cellContent.equals("")) {
					cellBtnArr[x][y].setIcon(null);

					// Get Neighbours
					cellContent = Integer.toString(cellBtnArr[x][y].getSurroundingMineCnt());

					// Is it a mine?
					if (cellBtnArr[x][y].isMineBuried()) {
						cellContent = "M";

						// mine
						cellBtnArr[x][y].setIcon(gui.getIconMine());
						cellBtnArr[x][y].setBackground(Color.white);
					} else {
						if (cellContent.equals("0")) {
							cellBtnArr[x][y].setText("");
							cellBtnArr[x][y].setBackground(Color.white);
						} else {
							cellBtnArr[x][y].setBackground(Color.white);
							cellBtnArr[x][y].setText(cellContent);
							gui.setTextColor(cellBtnArr[x][y]);
						}
					}
				}

				// This cell is already flagged!
				else if (cellContent.equals("F")) {
					// Is it correctly flagged?
					if (!cellBtnArr[x][y].isMineBuried()) {
						cellBtnArr[x][y].setBackground(Color.orange);
					} else
						cellBtnArr[x][y].setBackground(Color.green);
				}

			}
		}
	}
	
	/** check game won condition
	 * @return is game Won
	 */
	public boolean isFinished() {
		boolean isFinished = true;
		String cellSolution;
		
		CellBtn cellBtnArr[][] = gui.getCellBtnArr();

		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cellSolution = Integer.toString(cellBtnArr[x][y].getSurroundingMineCnt());

				if (cellBtnArr[x][y].isMineBuried())
					cellSolution = "F";

				if (!cellBtnArr[x][y].getContent().equals(cellSolution)) {
					isFinished = false;
					break;
				}
			}
		}

		return isFinished;
	}
	
	/**return mine sweeper game info
	 * @return
	 */
	public HashMap<String,Object> getGameInfo() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("rows", rows);
		map.put("cols", cols);
		map.put("totMineCnt", totMineCnt);
		map.put("cellBtn", gui.getCellBtnArr().clone());
		
		return map;
	}
	
	public DBUtil getDBUtil() {
		return util;
	}
}
