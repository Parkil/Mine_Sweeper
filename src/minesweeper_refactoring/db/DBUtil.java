package minesweeper_refactoring.db;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javafx.util.Pair;
import minesweeper_refactoring.Game;
import minesweeper_refactoring.Score;
import minesweeper_refactoring.ui.CellBtn;

public class DBUtil {
	Game game;
	
	private int rows;
	private int cols;
	private CellBtn[][] cellBtnArr;
	
	public final static String dbURL;
	
	//access db파일 경로를 검색하여 변수에 저장
	static {
		String p = "";

		try {
			p = new File(Game.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath()
					+ "\\db.accdb";
		} catch (URISyntaxException ex) {
			System.out.println("Error loading database file.");
		}

		dbURL = "jdbc:ucanaccess://" + p;
	}
	
	public DBUtil(Game game) {
		this.game = game;
		Map<String,Object> infoMap = game.getGameInfo();
		
		rows = ((Integer)infoMap.get("rows")).intValue();
		cols = ((Integer)infoMap.get("cols")).intValue();
		cellBtnArr = (CellBtn[][])infoMap.get("cellBtn");
	}
	
	/** check save game 
	 * @return save game exists
	 */
	public boolean checkSave() {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		boolean saveExists = false;

		try {
			connection = DriverManager.getConnection(dbURL);
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM GAME_STATE");

			while (resultSet.next()) {
				saveExists = true;
			}

			resultSet.close();
			statement.close();

			connection.close();

			return saveExists;
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
			return false;
		}
	}
	
	/** load save
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair loadSaveGame() {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			connection = DriverManager.getConnection(dbURL);

			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM CELL");

			for (int x = 0; x < cols; x++) {
				for (int y = 0; y < rows; y++) {
					resultSet.next();

					cellBtnArr[x][y].setContent(resultSet.getString("CONTENT"));
					cellBtnArr[x][y].setMineBuried(resultSet.getBoolean("MINE"));
					cellBtnArr[x][y].setSurroundingMineCnt(resultSet.getInt("SURROUNDING_MINES"));
				}
			}

			statement.close();
			resultSet.close();

			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM GAME_STATE");

			resultSet.next();

			Pair p = new Pair(resultSet.getInt("TIMER"), resultSet.getInt("MINES"));

			// After loading, delete the saved game
			deleteSavedGame();

			// cleanup resources, once after processing
			resultSet.close();
			statement.close();

			// and then finally close connection
			connection.close();

			return p;
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
			return null;
		}
	}

	/**
	 * delete save game
	 */
	public void deleteSavedGame() {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DriverManager.getConnection(dbURL);

			String template = "DELETE FROM GAME_STATE";
			statement = connection.prepareStatement(template);
			statement.executeUpdate();

			template = "DELETE FROM CELL";
			statement = connection.prepareStatement(template);
			statement.executeUpdate();

			statement.close();

			connection.close();
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
		}
	}

	/** save sweeper cell state
	 * @param timer
	 * @param mines
	 */
	public void saveGame(int timer, int mines) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DriverManager.getConnection(dbURL);

			String template = "INSERT INTO CELL (CONTENT, MINE, SURROUNDING_MINES) values (?,?,?)";
			statement = connection.prepareStatement(template);

			for (int x = 0; x < cols; x++) {
				for (int y = 0; y < rows; y++) {
					statement.setString(1, cellBtnArr[x][y].getContent());
					statement.setBoolean(2, cellBtnArr[x][y].isMineBuried());
					statement.setInt(3, (int) cellBtnArr[x][y].getSurroundingMineCnt());

					statement.executeUpdate();
				}
			}
			
			template = "INSERT INTO GAME_STATE (TIMER,MINES) values (?,?)";
			statement = connection.prepareStatement(template);

			statement.setInt(1, timer);
			statement.setInt(2, mines);

			statement.executeUpdate();
			statement.close();

			// and then finally close connection
			connection.close();
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
		}
	}
	
	/** get score and play count
	 * @return
	 */
	public boolean populate(Score score) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			String dbURL = DBUtil.dbURL;

			connection = DriverManager.getConnection(dbURL);
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM SCORE");

			while (resultSet.next()) {
				score.setGamesPlayed(resultSet.getInt("GAMES_PLAYED"));
				score.setGamesWon(resultSet.getInt("GAMES_WON"));
				
				score.setLongestWinningStreak(resultSet.getInt("LWSTREAK"));
				score.setLongestLosingStreak(resultSet.getInt("LLSTREAK"));
				
				score.setCurrentStreak(resultSet.getInt("CSTREAK"));
				
				score.setCurrentWinningStreak(resultSet.getInt("CWSTREAK"));
				score.setCurrentLosingStreak(resultSet.getInt("CLSTREAK"));
			}

			// cleanup resources, once after processing
			resultSet.close();
			statement.close();

			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM TIME");

			while (resultSet.next()) {
				int time = resultSet.getInt("TIME_VALUE");
				Date date = resultSet.getDate("DATE_VALUE");

				score.addTime(time, date);
			}

			// cleanup resources, once after processing
			resultSet.close();
			statement.close();

			// and then finally close connection
			connection.close();

			return true;
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * save score
	 */
	public void saveScore(Score score) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			String dbURL = DBUtil.dbURL;

			connection = DriverManager.getConnection(dbURL);

			// ----------EMPTY SCORE TABLE------//
			String template = "DELETE FROM SCORE";
			statement = connection.prepareStatement(template);
			statement.executeUpdate();

			// ----------EMPTY TIME TABLE------//
			template = "DELETE FROM TIME";
			statement = connection.prepareStatement(template);
			statement.executeUpdate();

			// --------------INSERT DATA INTO SCORE TABLE-----------//
			template = "INSERT INTO SCORE (GAMES_PLAYED,GAMES_WON, LWSTREAK, LLSTREAK, CSTREAK, CWSTREAK, CLSTREAK) values (?,?,?,?,?,?,?)";
			statement = connection.prepareStatement(template);
			
			statement.setInt(1, score.getGamesPlayed());
			statement.setInt(2, score.getGamesWon());
			statement.setInt(3, score.getLongestWinningStreak());
			statement.setInt(4, score.getLongestLosingStreak());
			statement.setInt(5, score.getCurrentStreak());
			statement.setInt(6, score.getCurrentWinningStreak());
			statement.setInt(7, score.getCurrentLosingStreak());

			statement.executeUpdate();

			/*
			 * access가 다중 insert나 batch를 지원하지 않음
			 */
			template = "INSERT INTO TIME (TIME_VALUE, DATE_VALUE) values (?,?)";
			statement = connection.prepareStatement(template);
			
			for (int i = 0; i < score.getBestTimes().size(); i++) {
				statement.setInt(1, score.getBestTimes().get(i).getTimeValue());
				statement.setDate(2, score.getBestTimes().get(i).getDateValue());

				statement.executeUpdate();
			}

			statement.close();
			connection.close();
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
		}
	}
}
