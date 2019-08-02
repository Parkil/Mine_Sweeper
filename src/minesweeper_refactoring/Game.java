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

//����ã�� ���� Ŭ����
/*
 * Game�� ���� ���ӽ���� ��ư event�� ȥ��Ǿ� �ְ� Game Class ������ UI class�� �Ѱܼ� �̺�Ʈ�� �����ϵ��� �Ǿ� �ִ�
 * �̸� �и��� �ʿ䰡 �ִ� 
 * 
 * [�и����]
 * 1.GUI ���� ���� : UI���� ����
 * 2.GUI ��ҿ� ���õ� �̺�Ʈ : �̺�Ʈ�󼼷����� ���õ� class�� ���� ����
 * 
 * [cell-board �� jbutton-UI�� �����Ұ������� ����]
 * 1.Cell - Jbutton�� ������ ������ �ִ�. Cell�� �� UI�� JButton�̰� Cell�� ��ǥ�� Jbutton�� ��ǥ��
 * 		-> Cell�ȿ� JButton��ü�� ���Խ�Ű�� �������� ó��
 * 
 */
public class Game {
	public boolean playing;
	
	public DBUtil util;

	public UIWindow gui;

	public Score score;
	
	//���μ��� ũ�� �� ��ü ���� ���� �ʱ�ȭ�� ������ �⺻���� ��
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
		util.populate(score); // ���� ��������ȸ�� + bestŬ���� �ð� ��ȯ
		
		GameEventExec exec = new GameEventExec(score, gui, this);
		this.gui.setButtonListeners(exec);
		
		resumeGame();
	}

	// -----------------Load Save Game (if any)--------------------------//
	
	/*
	 * ������ ����� ������ ������ ��� �������� continue ���� ���� �� �׿� ���� ó������
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

	public void newGame() {
		this.playing = false;
		
		gui.resetBtn();
		gui.interruptTimer();
		gui.resetTimer();
		gui.initGame();
		gui.setMines(totMineCnt);
	}

	public void restartGame() {
		this.playing = false;
		
		gui.resetBtn();
		gui.interruptTimer();
		gui.resetTimer();
		gui.initGame();
		gui.setMines(totMineCnt);
	}
	
	private void endGame() {
		playing = false;
		showAll();

		util.saveScore(score);
	}
	
	/**
	 * ���� Ŭ����� ���÷���
	 */
	public void gameWon() {
		score.incCurrentStreak();
		score.incCurrentWinningStreak();
		score.incGamesWon();
		score.incGamesPlayed();

		gui.interruptTimer();
		endGame();
		
		ArrayList<Time> bTimes = score.getBestTimes();
		score.addTime(gui.getTimePassed(), new Date(System.currentTimeMillis()));
		
		HashMap<String,Object> retMap = gui.getUIDialog().gameWonDialog();
		
		JDialog dialog = (JDialog)retMap.get("dialog");
		
		//JDialog�� ������Ʈ�� �����Ϳ� ���� ���� �Է�
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
			dialog.dispose();
			dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
		});
		playAgainBtn.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			newGame();
		});
		
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				dialog.dispose();
				newGame();
			}
		});
		
		/*
		 * dialog.pack()�� dialog.setVisible�� ������ �ҽ����� �����ؾ� ���������� �۵���
		 * java doc������ �ش� window�� display�Ǳ����� size����� �ǰ� ���߿� display�� �Ǹ�
		 * pack�� ȣ���Ҷ����� �����۾��� �Ѵٰ� ��������
		 */
		dialog.pack();
		dialog.setVisible(true);
	}
	
	/**
	 * Game Over�� ���� ���� 
	 */
	public void gameLost() {
		score.decCurrentStreak();
		score.incCurrentLosingStreak();
		score.incGamesPlayed();

		gui.interruptTimer();

		endGame();
		
		ArrayList<Time> bTimes = score.getBestTimes();
		
		HashMap<String,Object> retMap = gui.getUIDialog().gameLostDialog();
		
		JDialog dialog = (JDialog)retMap.get("dialog");
		
		//JDialog�� ������Ʈ�� �����Ϳ� ���� ���� �Է�
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
			dialog.dispose();
			dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
		});
		restartBtn.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			restartGame();
		});
		playAgainBtn.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			newGame();
		});
		
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				dialog.dispose();
				newGame();
			}
		});
		
		dialog.pack();
		dialog.setVisible(true);
	}

	/**
	 * ����Ʈ ��� ǥ��
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

	// Shows the "solution" of the game.
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
	
	/*
	 * ���� Ŭ���� ���� üũ
	 * �̰� �̷��� üũ�� �ʿ䰡 �ֳ�?
	 * ����ã�� Ŭ���� ���� : ���ڰ� �ִ� ������ ���� ���� Ŭ���ϴ°�
	 * -> �ش������δ� ���� üũ�� �ʿ� ���� Ŭ������ ���� ������ = ���ڰ��� �� ����Ŭ����� �Ǵ��ص� �ȴ� �� �̰��� ���ӿ������ǰ� ���� �ǴܵǾ�� ���������� �۵��Ҽ� �ִ�
	 */
	public boolean isFinished() {
		boolean isFinished = true;
		String cellSolution;
		
		CellBtn cellBtnArr[][] = gui.getCellBtnArr();

		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cellSolution = Integer.toString(cellBtnArr[x][y].getSurroundingMineCnt()); //�ش� �� �ֺ� ������ ����

				if (cellBtnArr[x][y].isMineBuried()) //�ش� ���� ���ڰ� ������ "F"�� ��ȯ
					cellSolution = "F";

				if (!cellBtnArr[x][y].getContent().equals(cellSolution)) { //�ش缿�� ������ "F"�� �ƴѰ��� ������ ������ Ŭ���� ���� ����
					isFinished = false;
					break;
				}
			}
		}

		return isFinished;
	}

	// Check the game to see if its finished or not
	public void checkGame() {
		if (isFinished()) {
			gameWon();
		}
	}
	
	//���� �⺻ ���� ��ȯ
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
