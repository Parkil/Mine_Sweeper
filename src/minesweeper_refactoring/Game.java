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
	public boolean playing;
	
	public DBUtil util;

	public UIWindow gui;

	public Score score;
	
	//가로세로 크기 및 전체 지뢰 개수 초기화된 설정은 기본설정 값
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
		util.populate(score); // 기존 게임진행회수 + best클리어 시간 반환
		
		GameEventExec exec = new GameEventExec(score, gui, this);
		this.gui.setButtonListeners(exec);
		
		resumeGame();
	}

	// -----------------Load Save Game (if any)--------------------------//
	
	/*
	 * 기존에 저장된 게임이 존재할 경우 기존게임 continue 여부 질문 및 그에 따른 처리수행
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
	 * 게임 클리어시 관련로직
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
		
		//JDialog의 컴포넌트에 데이터에 따른 값을 입력
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
		 * dialog.pack()과 dialog.setVisible은 동일한 소스에서 선언해야 정상적으로 작동함
		 * java doc에서는 해당 window가 display되기전에 size계산이 되고 나중에 display가 되면
		 * pack을 호출할때에는 검증작업만 한다고 나와있음
		 */
		dialog.pack();
		dialog.setVisible(true);
	}
	
	/**
	 * Game Over시 동작 정의 
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
		
		//JDialog의 컴포넌트에 데이터에 따른 값을 입력
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
	 * 베스트 기록 표시
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
	 * 게임 클리어 여부 체크
	 * 이걸 이렇게 체크할 필요가 있나?
	 * 지뢰찾기 클리어 조건 : 지뢰가 있는 영역을 빼고 전부 클릭하는것
	 * -> 극단적으로는 지뢰 체크할 필요 없이 클릭되지 않은 셀개수 = 지뢰개수 면 게임클리어로 판단해도 된다 단 이경우는 게임오버조건과 같이 판단되어야 정상적으로 작동할수 있다
	 */
	public boolean isFinished() {
		boolean isFinished = true;
		String cellSolution;
		
		CellBtn cellBtnArr[][] = gui.getCellBtnArr();

		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cellSolution = Integer.toString(cellBtnArr[x][y].getSurroundingMineCnt()); //해당 셀 주변 지뢰의 숫자

				if (cellBtnArr[x][y].isMineBuried()) //해당 셀에 지뢰가 있으면 "F"로 변환
					cellSolution = "F";

				if (!cellBtnArr[x][y].getContent().equals(cellSolution)) { //해당셀의 내용이 "F"가 아닌건이 있으면 게임이 클리어 되지 않음
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
	
	//게임 기본 정보 반환
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
