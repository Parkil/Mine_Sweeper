package minesweeper;

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
import minesweeper.Score.Time;

//지뢰찾기 메인 클래스
/*
 * Game에 실제 게임실행과 버튼 event가 혼재되어 있고 Game Class 참조를 UI class로 넘겨서 이벤트를 설정하도록 되어 있다
 * 이를 분리할 필요가 있다 
 * 
 * [분리방안]
 * 1.GUI 구조 생성 : UI에서 전담
 * 2.GUI 요소에 관련된 이벤트 : 이벤트상세로직에 관련된 class를 따로 생성
 * 
 * [cell-board 와 jbutton-UI를 통합할것인지의 여부]
 * 1.Cell - Jbutton은 밀접한 연관이 있다. Cell이 곧 UI의 JButton이고 Cell의 좌표가 Jbutton의 좌표임
 * 		-> Cell안에 JButton객체를 포함시키는 방향으로 처리
 * 
 */
public class Game implements MouseListener, ActionListener, WindowListener {
	public static String dbPath;
	// "playing" indicates whether a game is running (true) or not (false).
	private boolean playing;

	private Board board;

	private UI gui;

	private Score score;

	// ------------------------------------------------------------------//
	
	/*
	 * 1.access db에서 기존 게임 플레이 정보(게임 실행 회수, best기록)를 가져온다 
	 * 2.지뢰찾기 각 셀의 정보를 설정
	 * 3.2번에서 설정한 정보를 기준으로 GUI를 생성
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
		score.populate(); // 기존 게임진행회수 + best클리어 시간 반환

		UI.setLook("Nimbus");

		createBoard(); // 지뢰찾기 게임정보 생성
		
		//swing을 이용하여 GUI설정
		this.gui = new UI(board.getRows(), board.getCols(), board.getNumberOfMines());
		this.gui.setButtonListeners(this);

		this.playing = false;

		gui.setVisible(true);

		gui.setIcons();
		gui.hideAll();

		resumeGame();
	}

	// -----------------Load Save Game (if any)--------------------------//
	
	/*
	 * 기존에 저장된 게임이 존재할 경우 기존게임 continue 여부 질문 및 그에 따른 처리수행
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
				// private 전역변수를 사용하지 않고 파라메터를 이용하여 설정하는게 낫지 않을까?
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
	 * newGame이나 restartGame이나 기능은 동일한 코드가 중복되어 표기되어 있음.
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
	 * 게임을 클리어 했을때 동작 정의
	 * UI를 담당하는 클래스가 따로 있음에도 클리어시 UI를 동작하는 로직이 같이 들어가 있음 이를 UI.java에 동작을 정의하고 게임클리어시 동작을 호출하는 방식으로 변경할 필요가 있다.
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
			windowClosing(null);
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
	 * GameOver시 동작정의
	 * UI를 담당하는 클래스가 따로 있음에도 클리어시 UI를 동작하는 로직이 같이 들어가 있음 이를 UI.java에 동작을 정의하고 게임클리어시 동작을 호출하는 방식으로 변경할 필요가 있다.
	 * 해당코드도 gameWon과 마찬가지로 gameWon메소드와 문구및 내부 파라메터 변경을 제외하고는 거의 로직이 동일하다 
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
			windowClosing(null);
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
	 * 점수표시
	 * gameLost와 동일하게 swing부분을 UI class로 이관이 필요
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
	 * 게임 클리어 여부 체크
	 * 이걸 이렇게 체크할 필요가 있나?
	 * 지뢰찾기 클리어 조건 : 지뢰가 있는 영역을 빼고 전부 클릭하는것
	 * -> 극단적으로는 지뢰 체크할 필요 없이 클릭되지 않은 셀개수 = 지뢰개수 면 게임클리어로 판단해도 된다 단 이경우는 게임오버조건과 같이 판단되어야 정상적으로 작동할수 있다
	 */
	public boolean isFinished() {
		boolean isFinished = true;
		String cellSolution;

		Cell cells[][] = board.getCells();

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				// If a game is solved, the content of each Cell should match the value of its
				// surrounding mines
				cellSolution = Integer.toString(cells[x][y].getSurroundingMines()); //해당 셀 주변 지뢰의 숫자

				if (cells[x][y].getMine()) //해당 셀에 지뢰가 있으면 "F"로 변환
					cellSolution = "F";

				// Compare the player's "answer" to the solution.
				if (!cells[x][y].getContent().equals(cellSolution)) { //해당셀의 내용이 "F"가 아닌건이 있으면 게임이 클리어 되지 않음
					// This cell is not solved yet
					isFinished = false;
					break;
				}
			}
		}

		return isFinished;
	}

	// Check the game to see if its finished or not
	private void checkGame() {
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
	 * 클릭한 셀이 주변에 지뢰가 없는 셀일 경우 주변셀을 전부 개방
	 */
	public void findZeroes(int xCo, int yCo) {
		int neighbours;

		Cell cells[][] = board.getCells();
		JButton buttons[][] = gui.getButtons();
		
		/*
		 * 1.인자로 주어진 좌표의 주변셀을 전부 검사
		 * 2.검사하는 대상셀에 숫자가 존재하지 않으면(주변에 지뢰가 없음) 대상셀 좌표를 기준으로 1번을 재귀호출로 다시 수행
		 * 3.1,2번을 대상셀에 숫자가 존재하지 않을때까지 반복
		 * 
		 * -- 좌표를 찍는 대상이 Cell과 JButton으로 이원화되어 있다. 이를 1개로 통합하여 움직여야 할듯
		 * Cell = JButton인 만큼 Cell안에 JButton을 내부변수로 이용하는 방법이 효율적일듯
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

	// -----------------------------------------------------------------------------//
	// This function is called when clicked on closed button or exit
	/*
	 * window close 이벤트
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		if (playing) {
			ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));

			Object[] options = { "Save", "Don't Save", "Cancel" };

			int quit = JOptionPane.showOptionDialog(null, "What do you want to do with the game in progress?",
					"New Game", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, question, options,
					options[2]);

			switch (quit) {
			// save
			case JOptionPane.YES_OPTION:

				gui.interruptTimer();
				score.save();

				JDialog dialog = new JDialog(gui, Dialog.ModalityType.DOCUMENT_MODAL);
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
				panel.add(new JLabel("Saving.... Please Wait", SwingConstants.CENTER));
				dialog.add(panel);
				dialog.setTitle("Saving Game...");
				dialog.pack();
				dialog.setLocationRelativeTo(gui);
				dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						board.saveGame(gui.getTimePassed(), gui.getMines());
						return null;
					}

					@Override
					protected void done() {
						dialog.dispose();
					}
				};

				worker.execute();
				dialog.setVisible(true);

				System.exit(0);
				break;

			// dont save
			case JOptionPane.NO_OPTION:
				score.incGamesPlayed();
				score.save();
				System.exit(0);
				break;

			case JOptionPane.CANCEL_OPTION:
				break;
			}
		} else
			System.exit(0);
	}

	// -----------------------------------------------------------------------//
	/*
	 * 상단메뉴 action event
	 * 지금까지 분석하면서 지속적으로 나오는 문제점
	 * 1.JDialog관련 로직이 중복되어 표시된다.
	 * 		->UI class에 dialog를 만드는 로직을 따로 두던가, 아니면 dialog interface를 만들고 이를 구현하는 방식으로 세부로직을 두는것도 고려해볼 필요가 있다.
	 * 2.event관련 로직이 중복으로 표시된다.
	 * 		->event는 Swing Component별로 붙는것이니 만큼 Swing Compoent를 class별로 분리하고 해당 class내에서 event를 처리하도록 수정
	 * 		->분류
	 * 			Window
	 * 			Dialog
	 * 			Button -> Cell class와 연계
	 * 			Board(Cell집합) -> 해당 UI와연계처리 또는 내부참조로 처리할 필요가 있음
	 * 			
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem menuItem = (JMenuItem) e.getSource();

		if (menuItem.getName().equals("New Game")) {
			if (playing) {
				ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));

				Object[] options = { "Quit and Start a New Game", "Restart", "Keep Playing" };

				int startNew = JOptionPane.showOptionDialog(null, "What do you want to do with the game in progress?",
						"New Game", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, question, options,
						options[2]);

				switch (startNew) {
				case JOptionPane.YES_OPTION:

					// Initialize the new game.
					newGame();
					score.incGamesPlayed();
					score.save();
					break;

				case JOptionPane.NO_OPTION:
					score.incGamesPlayed();
					score.save();
					restartGame();
					break;

				case JOptionPane.CANCEL_OPTION:
					break;
				}
			}
		}

		else if (menuItem.getName().equals("Exit")) {
			windowClosing(null);
		}

		// Statistics
		else {
			showScore();
		}
	}

	// --------------------------------------------------------------------------//

	// Mouse Click Listener
	/*
	 * 각 셀 클릭 이벤트
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	// -------------------------RELATED TO SCORES----------------------//

	// ---------------------EMPTY FUNCTIONS-------------------------------//
	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		//플레이 중이 아닐때 
		if (!playing) {
			gui.startTimer();
			playing = true;
		}
		
		System.out.println("mouse Click : "+playing);
		//플레이 중
		if (playing) {
			// Get the button's name
			JButton button = (JButton) e.getSource();

			// Get coordinates (button.getName().equals("x,y")).
			String[] co = button.getName().split(","); //이걸 Name으로 x,y좌표를 설정하지 말고 Cell초기화시 JButton을 같이 초기화하고 JButton객체로 Cell객체를 불러와서 Cell안의 표를 가져오게 처리

			int x = Integer.parseInt(co[0]);
			int y = Integer.parseInt(co[1]);

			// Get cell information.
			boolean isMine = board.getCells()[x][y].getMine();
			int neighbours = board.getCells()[x][y].getSurroundingMines();

			// Left Click
			if (SwingUtilities.isLeftMouseButton(e)) {
				System.out.println("left click : "+board.getCells()[x][y].getContent());
				if (!board.getCells()[x][y].getContent().equals("F")) {
					button.setIcon(null);

					// Mine is clicked.
					if (isMine) {
						// red mine
						button.setIcon(gui.getIconRedMine());
						button.setBackground(Color.red);
						board.getCells()[x][y].setContent("M");

						gameLost(); //이 로직때문에 반응속도가 느리게 나옴
					} else {
						// The player has clicked on a number.
						board.getCells()[x][y].setContent(Integer.toString(neighbours));
						button.setText(Integer.toString(neighbours));
						gui.setTextColor(button);

						if (neighbours == 0) {
							// Show all surrounding cells.
							button.setBackground(Color.lightGray);
							button.setText("");
							findZeroes(x, y);
						} else {
							button.setBackground(Color.lightGray);
						}
					}
				}
			}
			// Right Click - node.js포팅시 가장문제가 되는 부분, 우측클릭을 막을수는 있어도 우측클릭에 깃발표시 이벤트를 먹이는게 쉽지 않을듯
			else if (SwingUtilities.isRightMouseButton(e)) {
				if (board.getCells()[x][y].getContent().equals("F")) {
					board.getCells()[x][y].setContent("");
					button.setText("");
					button.setBackground(new Color(0, 110, 140));

					// simple blue

					button.setIcon(gui.getIconTile());
					gui.incMines();
				} else if (board.getCells()[x][y].getContent().equals("")) {
					board.getCells()[x][y].setContent("F");
					button.setBackground(Color.blue);

					button.setIcon(gui.getIconFlag());
					gui.decMines();
				}
			}

			checkGame();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}
