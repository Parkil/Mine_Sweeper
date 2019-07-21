package minesweeper_refactoring;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.sql.Date;
import javafx.util.Pair;
import javax.swing.border.TitledBorder;
import minesweeper_refactoring.Score.Time;

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
public class Game {
	public static String dbPath;
	// "playing" indicates whether a game is running (true) or not (false).
	public boolean playing;

	public Board board;

	public UI gui;

	public Score score;

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
		
		/*
		 * JButton에 Cell관련정보를 넣으면서 createBoard에서 처리하는 부분을 UI에서 전부 입력하도록 처리
		 */
		createBoard(); // 지뢰찾기 게임정보 생성
		
		//swing을 이용하여 GUI설정
		this.gui = new UI(9, 9, 10); //나중에 외부설정으로 구현할수 있도록 처리
		
		GameEventExec exec = new GameEventExec(board, score, gui, this);
		
		this.gui.setButtonListeners(exec); //new

		this.playing = false;

		gui.setVisible(true);

		//gui.setIcon();
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
		JButton buttons[][] = gui.getCellBtnArr();

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
		
		gui.resetBtn();
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
			//windowClosing(null); //에러처리를 위해 임시로 주석 처리
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
			//windowClosing(null); //에러처리를 위해 임시로 주석처리
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
		JButton buttons[][] = gui.getCellBtnArr();

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
	public void checkGame() {
		if (isFinished()) {
			gameWon();
		}
	}

	// ----------------------------------------------------------------------/
}
