package minesweeper_refactoring;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.sql.Date;
import javafx.util.Pair;
import javax.swing.border.TitledBorder;
import minesweeper_refactoring.Score.Time;
import minesweeper_refactoring.db.DBUtil;
import minesweeper_refactoring.ui.CellBtn;

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
	public static String dbPath;
	// "playing" indicates whether a game is running (true) or not (false).
	public boolean playing;

	
	public DBUtil util;

	public UI gui;

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
		
		// set db path
		String p = "";

		try {
			p = new File(Game.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath()
					+ "\\db.accdb";
		} catch (URISyntaxException ex) {
			System.out.println("Error loading database file.");
		}

		dbPath = "jdbc:ucanaccess://" + p;

		score = new Score();
		score.populate(); // ���� ��������ȸ�� + bestŬ���� �ð� ��ȯ

		UI.setLook("Nimbus");
		
		//swing�� �̿��Ͽ� GUI����
		this.gui = new UI(9, 9, 10); //���߿� �ܺμ������� �����Ҽ� �ֵ��� ó��
		
		GameEventExec exec = new GameEventExec(score, gui, this);
		
		this.gui.setButtonListeners(exec); //new

		this.playing = false;

		gui.setVisible(true);

		//gui.setIcon();
		gui.hideAll();
		
		util = new DBUtil(this); //lazy init�ʿ�
		
		resumeGame();
	}

	// -----------------Load Save Game (if any)--------------------------//
	
	/*
	 * ������ ����� ������ ������ ��� �������� continue ���� ���� �� �׿� ���� ó������
	 */
	public void resumeGame() {
		if (util.checkSave()) {
			ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));

			int option = JOptionPane.showOptionDialog(null, "Do you want to continue your saved game?",
					"Saved Game Found", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, question, null, null);

			switch (option) {
			case JOptionPane.YES_OPTION:

				// load board's state
				Pair p = util.loadSaveGame();

				// set button's images
				// private ���������� ������� �ʰ� �Ķ���͸� �̿��Ͽ� �����ϴ°� ���� ������?
				setButtonImages();

				// load timer's value
				gui.setTimePassed((int) p.getKey());

				// load mines value
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

	// -------------------------------------------------//
	public void setButtonImages() {
		CellBtn cellBtnArr[][] = gui.getCellBtnArr();

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				cellBtnArr[x][y].setIcon(null);
				
				if ("".equals(cellBtnArr[x][y].getContent())) {
					cellBtnArr[x][y].setIcon(gui.getIconTile());
				} else if ("F".equals(cellBtnArr[x][y].getContent())) {
					cellBtnArr[x][y].setIcon(gui.getIconFlag());
					cellBtnArr[x][y].setBackground(Color.blue);
				} else if ("0".equals(cellBtnArr[x][y].getContent())) {
					cellBtnArr[x][y].setBackground(Color.lightGray);
				} else {
					cellBtnArr[x][y].setBackground(Color.lightGray);
					cellBtnArr[x][y].setText(cellBtnArr[x][y].getContent());
					gui.setTextColor(cellBtnArr[x][y]);
				}
			}
		}
	}
	/*
	 * newGame�̳� restartGame�̳� ����� ������ �ڵ尡 �ߺ��Ǿ� ǥ��Ǿ� ����.
	 */
	public void newGame() {
		this.playing = false;

		gui.interruptTimer();
		gui.resetTimer();
		gui.initGame();
		gui.setMines(totMineCnt);
	}
	// ------------------------------------------------------------------------------//

	public void restartGame() {
		this.playing = false;
		
		gui.resetBtn();
		gui.interruptTimer();
		gui.resetTimer();
		gui.initGame();
		gui.setMines(totMineCnt);
	}

	// ------------------------------------------------------------------------------//
	private void endGame() {
		playing = false;
		showAll();

		score.save();
	}

	// -------------------------GAME WON AND GAME LOST
	// ---------------------------------//
	
	/*
	 * ������ Ŭ���� ������ ���� ����
	 * UI�� ����ϴ� Ŭ������ ���� �������� Ŭ����� UI�� �����ϴ� ������ ���� �� ���� �̸� UI.java�� ������ �����ϰ� ����Ŭ����� ������ ȣ���ϴ� ������� ������ �ʿ䰡 �ִ�.
	 */
	public void gameWon() {
		score.incCurrentStreak();
		score.incCurrentWinningStreak();
		score.incGamesWon();
		score.incGamesPlayed();

		gui.interruptTimer();
		endGame();
		// ----------------------------------------------------------------//

		JDialog dialog = new JDialog(gui, Dialog.ModalityType.DOCUMENT_MODAL);

		// ------MESSAGE-----------//
		JLabel message = new JLabel("Congratulations, you won the game!", SwingConstants.CENTER);

		// -----STATISTICS-----------//
		JPanel statistics = new JPanel();
		statistics.setLayout(new GridLayout(6, 1, 0, 10));

		ArrayList<Time> bTimes = score.getBestTimes();

		if (bTimes.isEmpty() || (bTimes.get(0).getTimeValue() > gui.getTimePassed())) {
			statistics.add(new JLabel("    You have the fastest time for this difficulty level!    "));
		}

		score.addTime(gui.getTimePassed(), new Date(System.currentTimeMillis()));

		JLabel time = new JLabel("  Time:  " + Integer.toString(gui.getTimePassed()) + " seconds            Date:  "
				+ new Date(System.currentTimeMillis()));

		JLabel bestTime = new JLabel();

		if (bTimes.isEmpty()) {
			bestTime.setText("  Best Time:  ---                  Date:  ---");
		} else {
			bestTime.setText("  Best Time:  " + bTimes.get(0).getTimeValue() + " seconds            Date:  "
					+ bTimes.get(0).getDateValue());
		}

		JLabel gPlayed = new JLabel("  Games Played:  " + score.getGamesPlayed());
		JLabel gWon = new JLabel("  Games Won:  " + score.getGamesWon());
		JLabel gPercentage = new JLabel("  Win Percentage:  " + score.getWinPercentage() + "%");

		statistics.add(time);
		statistics.add(bestTime);
		statistics.add(gPlayed);
		statistics.add(gWon);
		statistics.add(gPercentage);

		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		statistics.setBorder(loweredetched);

		// --------BUTTONS----------//
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2, 10, 0));

		JButton exit = new JButton("Exit");
		JButton playAgain = new JButton("Play Again");

		exit.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			//windowClosing(null); //����ó���� ���� �ӽ÷� �ּ� ó��
		});
		playAgain.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			newGame();
		});

		buttons.add(exit);
		buttons.add(playAgain);

		// --------DIALOG-------------//

		JPanel c = new JPanel();
		c.setLayout(new BorderLayout(20, 20));
		c.add(message, BorderLayout.NORTH);
		c.add(statistics, BorderLayout.CENTER);
		c.add(buttons, BorderLayout.SOUTH);

		c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				dialog.dispose();
				newGame();
			}
		});

		dialog.setTitle("Game Won");
		dialog.add(c);
		dialog.pack();
		dialog.setLocationRelativeTo(gui);
		dialog.setVisible(true);
	}
	
	/*
	 * GameOver�� ��������
	 * UI�� ����ϴ� Ŭ������ ���� �������� Ŭ����� UI�� �����ϴ� ������ ���� �� ���� �̸� UI.java�� ������ �����ϰ� ����Ŭ����� ������ ȣ���ϴ� ������� ������ �ʿ䰡 �ִ�.
	 * �ش��ڵ嵵 gameWon�� ���������� gameWon�޼ҵ�� ������ ���� �Ķ���� ������ �����ϰ�� ���� ������ �����ϴ� 
	 */
	public void gameLost() {
		score.decCurrentStreak();
		score.incCurrentLosingStreak();
		score.incGamesPlayed();

		gui.interruptTimer();

		endGame();

		// ----------------------------------------------------------------//

		JDialog dialog = new JDialog(gui, Dialog.ModalityType.DOCUMENT_MODAL);

		// ------MESSAGE-----------//
		JLabel message = new JLabel("Sorry, you lost this game. Better luck next time!", SwingConstants.CENTER);

		// -----STATISTICS-----------//
		JPanel statistics = new JPanel();
		statistics.setLayout(new GridLayout(5, 1, 0, 10));

		JLabel time = new JLabel("  Time:  " + Integer.toString(gui.getTimePassed()) + " seconds");

		JLabel bestTime = new JLabel();

		ArrayList<Time> bTimes = score.getBestTimes();

		if (bTimes.isEmpty()) {
			bestTime.setText("                        ");
		} else {
			bestTime.setText("  Best Time:  " + bTimes.get(0).getTimeValue() + " seconds            Date:  "
					+ bTimes.get(0).getDateValue());
		}

		JLabel gPlayed = new JLabel("  Games Played:  " + score.getGamesPlayed());
		JLabel gWon = new JLabel("  Games Won:  " + score.getGamesWon());
		JLabel gPercentage = new JLabel("  Win Percentage:  " + score.getWinPercentage() + "%");

		statistics.add(time);
		statistics.add(bestTime);
		statistics.add(gPlayed);
		statistics.add(gWon);
		statistics.add(gPercentage);

		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		statistics.setBorder(loweredetched);

		// --------BUTTONS----------//
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 3, 2, 0));

		JButton exit = new JButton("Exit");
		JButton restart = new JButton("Restart");
		JButton playAgain = new JButton("Play Again");

		exit.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			//windowClosing(null); //����ó���� ���� �ӽ÷� �ּ�ó��
		});
		restart.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			restartGame();
		});
		playAgain.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			newGame();
		});

		buttons.add(exit);
		buttons.add(restart);
		buttons.add(playAgain);

		// --------DIALOG-------------//

		JPanel c = new JPanel();
		c.setLayout(new BorderLayout(20, 20));
		c.add(message, BorderLayout.NORTH);
		c.add(statistics, BorderLayout.CENTER);
		c.add(buttons, BorderLayout.SOUTH);

		c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				dialog.dispose();
				newGame();
			}
		});

		dialog.setTitle("Game Lost");
		dialog.add(c);
		dialog.pack();
		dialog.setLocationRelativeTo(gui);
		dialog.setVisible(true);
	}

	// --------------------------------SCORE
	// BOARD--------------------------------------//
	/*
	 * ����ǥ��
	 * gameLost�� �����ϰ� swing�κ��� UI class�� �̰��� �ʿ�
	 */
	public void showScore() {
		// ----------------------------------------------------------------//

		JDialog dialog = new JDialog(gui, Dialog.ModalityType.DOCUMENT_MODAL);

		// -----BEST TIMES--------//

		JPanel bestTimes = new JPanel();
		bestTimes.setLayout(new GridLayout(5, 1));

		ArrayList<Time> bTimes = score.getBestTimes();

		for (int i = 0; i < bTimes.size(); i++) {
			JLabel t = new JLabel("  " + bTimes.get(i).getTimeValue() + "           " + bTimes.get(i).getDateValue());
			bestTimes.add(t);
		}

		if (bTimes.isEmpty()) {
			JLabel t = new JLabel("                               ");
			bestTimes.add(t);
		}

		TitledBorder b = BorderFactory.createTitledBorder("Best Times");
		b.setTitleJustification(TitledBorder.LEFT);

		bestTimes.setBorder(b);

		// -----STATISTICS-----------//
		JPanel statistics = new JPanel();

		statistics.setLayout(new GridLayout(6, 1, 0, 10));

		JLabel gPlayed = new JLabel("  Games Played:  " + score.getGamesPlayed());
		JLabel gWon = new JLabel("  Games Won:  " + score.getGamesWon());
		JLabel gPercentage = new JLabel("  Win Percentage:  " + score.getWinPercentage() + "%");
		JLabel lWin = new JLabel("  Longest Winning Streak:  " + score.getLongestWinningStreak());
		JLabel lLose = new JLabel("  Longest Losing Streak:  " + score.getLongestLosingStreak());
		JLabel currentStreak = new JLabel("  Current Streak:  " + score.getCurrentStreak());

		statistics.add(gPlayed);
		statistics.add(gWon);
		statistics.add(gPercentage);
		statistics.add(lWin);
		statistics.add(lLose);
		statistics.add(currentStreak);

		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		statistics.setBorder(loweredetched);

		// --------BUTTONS----------//
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2, 10, 0));

		JButton close = new JButton("Close");
		JButton reset = new JButton("Reset");

		close.addActionListener((ActionEvent e) -> {
			dialog.dispose();
		});
		reset.addActionListener((ActionEvent e) -> {
			ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));

			int option = JOptionPane.showOptionDialog(null, "Do you want to reset all your statistics to zero?",
					"Reset Statistics", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, question, null, null);

			switch (option) {
			case JOptionPane.YES_OPTION:

				score.resetScore();
				score.save();
				dialog.dispose();
				showScore();
				break;

			case JOptionPane.NO_OPTION:
				break;
			}
		});

		buttons.add(close);
		buttons.add(reset);

		if (score.getGamesPlayed() == 0)
			reset.setEnabled(false);

		// --------DIALOG-------------//

		JPanel c = new JPanel();
		c.setLayout(new BorderLayout(20, 20));
		c.add(bestTimes, BorderLayout.WEST);
		c.add(statistics, BorderLayout.CENTER);
		c.add(buttons, BorderLayout.SOUTH);

		c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		dialog.setTitle("Minesweeper Statistics - Haris Muneer");
		dialog.add(c);
		dialog.pack();
		dialog.setLocationRelativeTo(gui);
		dialog.setVisible(true);
	}

	// ------------------------------------------------------------------------------//

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
						cellBtnArr[x][y].setBackground(Color.lightGray);
					} else {
						if (cellContent.equals("0")) {
							cellBtnArr[x][y].setText("");
							cellBtnArr[x][y].setBackground(Color.lightGray);
						} else {
							cellBtnArr[x][y].setBackground(Color.lightGray);
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

	// -------------------------------------------------------------------------//

	// -------------------------------------------------------------------------//

	// -------------------------------------------------------------------------//

	// --------------------------------------------------------------------------//
	
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

	// ----------------------------------------------------------------------/
}
