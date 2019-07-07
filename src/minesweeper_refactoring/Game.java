package minesweeper_refactoring;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.sql.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import javax.swing.border.TitledBorder;
import minesweeper_refactoring.Score.Time;

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

	public Board board;

	public UI gui;

	public Score score;

	// ------------------------------------------------------------------//
	
	/*
	 * 1.access db���� ���� ���� �÷��� ����(���� ���� ȸ��, best���)�� �����´� 
	 * 2.����ã�� �� ���� ������ ����
	 * 3.2������ ������ ������ �������� GUI�� ����
	 * 
	 */
	public Game() {
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

		createBoard(); // ����ã�� �������� ����
		
		//swing�� �̿��Ͽ� GUI����
		this.gui = new UI(board.getRows(), board.getCols(), board.getNumberOfMines());
		
		GameEventExec exec = new GameEventExec(board, score, gui, this);
		
		//this.gui.setButtonListeners(this); //old
		this.gui.setButtonListeners(exec); //new

		this.playing = false;

		gui.setVisible(true);

		gui.setIcons();
		gui.hideAll();

		resumeGame();
	}

	// -----------------Load Save Game (if any)--------------------------//
	
	/*
	 * ������ ����� ������ ������ ��� �������� continue ���� ���� �� �׿� ���� ó������
	 */
	public void resumeGame() {
		if (board.checkSave()) {
			ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));

			int option = JOptionPane.showOptionDialog(null, "Do you want to continue your saved game?",
					"Saved Game Found", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, question, null, null);

			switch (option) {
			case JOptionPane.YES_OPTION:

				// load board's state
				Pair p = board.loadSaveGame();

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
				board.deleteSavedGame();
				break;

			case JOptionPane.CLOSED_OPTION:
				board.deleteSavedGame();
				break;
			}
		}
	}

	// -------------------------------------------------//
	public void setButtonImages() {
		Cell cells[][] = board.getCells();
		JButton buttons[][] = gui.getButtons();

		for (int y = 0; y < board.getRows(); y++) {
			for (int x = 0; x < board.getCols(); x++) {
				buttons[x][y].setIcon(null);

				if (cells[x][y].getContent().equals("")) {
					buttons[x][y].setIcon(gui.getIconTile());
				} else if (cells[x][y].getContent().equals("F")) {
					buttons[x][y].setIcon(gui.getIconFlag());
					buttons[x][y].setBackground(Color.blue);
				} else if (cells[x][y].getContent().equals("0")) {
					buttons[x][y].setBackground(Color.lightGray);
				} else {
					buttons[x][y].setBackground(Color.lightGray);
					buttons[x][y].setText(cells[x][y].getContent());
					gui.setTextColor(buttons[x][y]);
				}
			}
		}
	}

	// ------------------------------------------------------------//

	public void createBoard() {
		// Create a new board
		int mines = 10;

		int r = 9;
		int c = 9;

		this.board = new Board(mines, r, c);
	}

	// ---------------------------------------------------------------//
	/*
	 * newGame�̳� restartGame�̳� ����� ������ �ڵ尡 �ߺ��Ǿ� ǥ��Ǿ� ����.
	 */
	public void newGame() {
		this.playing = false;

		createBoard();

		gui.interruptTimer();
		gui.resetTimer();
		gui.initGame();
		gui.setMines(board.getNumberOfMines());
	}
	// ------------------------------------------------------------------------------//

	public void restartGame() {
		this.playing = false;

		board.resetBoard();

		gui.interruptTimer();
		gui.resetTimer();
		gui.initGame();
		gui.setMines(board.getNumberOfMines());
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
		String cellSolution;

		Cell cells[][] = board.getCells();
		JButton buttons[][] = gui.getButtons();

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				cellSolution = cells[x][y].getContent();

				// Is the cell still unrevealed
				if (cellSolution.equals("")) {
					buttons[x][y].setIcon(null);

					// Get Neighbours
					cellSolution = Integer.toString(cells[x][y].getSurroundingMines());

					// Is it a mine?
					if (cells[x][y].getMine()) {
						cellSolution = "M";

						// mine
						buttons[x][y].setIcon(gui.getIconMine());
						buttons[x][y].setBackground(Color.lightGray);
					} else {
						if (cellSolution.equals("0")) {
							buttons[x][y].setText("");
							buttons[x][y].setBackground(Color.lightGray);
						} else {
							buttons[x][y].setBackground(Color.lightGray);
							buttons[x][y].setText(cellSolution);
							gui.setTextColor(buttons[x][y]);
						}
					}
				}

				// This cell is already flagged!
				else if (cellSolution.equals("F")) {
					// Is it correctly flagged?
					if (!cells[x][y].getMine()) {
						buttons[x][y].setBackground(Color.orange);
					} else
						buttons[x][y].setBackground(Color.green);
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

		Cell cells[][] = board.getCells();

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				// If a game is solved, the content of each Cell should match the value of its
				// surrounding mines
				cellSolution = Integer.toString(cells[x][y].getSurroundingMines()); //�ش� �� �ֺ� ������ ����

				if (cells[x][y].getMine()) //�ش� ���� ���ڰ� ������ "F"�� ��ȯ
					cellSolution = "F";

				// Compare the player's "answer" to the solution.
				if (!cells[x][y].getContent().equals(cellSolution)) { //�ش缿�� ������ "F"�� �ƴѰ��� ������ ������ Ŭ���� ���� ����
					// This cell is not solved yet
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

	// ----------------------------------------------------------------------/

	/*
	 * If a player clicks on a zero, all surrounding cells ("neighbours") must
	 * revealed. This method is recursive: if a neighbour is also a zero, his
	 * neighbours must also be revealed.
	 */
	
	/*
	 * Ŭ���� ���� �ֺ��� ���ڰ� ���� ���� ��� �ֺ����� ���� ����
	 */
	public void findZeroes(int xCo, int yCo) {
		int neighbours;

		Cell cells[][] = board.getCells();
		JButton buttons[][] = gui.getButtons();
		
		/*
		 * 1.���ڷ� �־��� ��ǥ�� �ֺ����� ���� �˻�
		 * 2.�˻��ϴ� ��󼿿� ���ڰ� �������� ������(�ֺ��� ���ڰ� ����) ��� ��ǥ�� �������� 1���� ���ȣ��� �ٽ� ����
		 * 3.1,2���� ��󼿿� ���ڰ� �������� ���������� �ݺ�
		 * 
		 * -- ��ǥ�� ��� ����� Cell�� JButton���� �̿�ȭ�Ǿ� �ִ�. �̸� 1���� �����Ͽ� �������� �ҵ�
		 * Cell = JButton�� ��ŭ Cell�ȿ� JButton�� ���κ����� �̿��ϴ� ����� ȿ�����ϵ�
		 */
		// Columns
		for (int x = board.makeValidCoordinateX(xCo - 1); x <= board.makeValidCoordinateX(xCo + 1); x++) {
			// Rows
			for (int y = board.makeValidCoordinateY(yCo - 1); y <= board.makeValidCoordinateY(yCo + 1); y++) {
				// Only unrevealed cells need to be revealed.
				if (cells[x][y].getContent().equals("")) {
					// Get the neighbours of the current (neighbouring) cell.
					neighbours = cells[x][y].getSurroundingMines();

					// Reveal the neighbours of the current (neighbouring) cell
					cells[x][y].setContent(Integer.toString(neighbours));

					if (!cells[x][y].getMine())
						buttons[x][y].setIcon(null);

					// Is this (neighbouring) cell a "zero" cell itself?
					if (neighbours == 0) {
						// Yes, give it a special color and recurse!
						buttons[x][y].setBackground(Color.lightGray);
						buttons[x][y].setText("");
						findZeroes(x, y);
					} else {
						// No, give it a boring gray color.
						buttons[x][y].setBackground(Color.lightGray);
						buttons[x][y].setText(Integer.toString(neighbours));
						gui.setTextColor(buttons[x][y]);
					}
				}
			}
		}
	}
}
