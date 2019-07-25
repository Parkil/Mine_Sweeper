package minesweeper_refactoring.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javafx.util.Pair;
import minesweeper_refactoring.Game;
import minesweeper_refactoring.ui.CellBtn;

public class DBUtil {
	Game game;
	
	private int rows;
	private int cols;
//	private int totMineCnt;
	private CellBtn[][] cellBtnArr;
	
	public DBUtil(Game game) {
		this.game = game;
		Map<String,Object> infoMap = game.getGameInfo();
		
		rows = ((Integer)infoMap.get("rows")).intValue();
		cols = ((Integer)infoMap.get("cols")).intValue();
//		totMineCnt = ((Integer)infoMap.get("totMineCnt")).intValue();
		cellBtnArr = (CellBtn[][])infoMap.get("cellBtn");
	}
	
	public boolean checkSave() {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		boolean saveExists = false;

		try {
			String dbURL = Game.dbPath;

			connection = DriverManager.getConnection(dbURL);
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM GAME_STATE");

			while (resultSet.next()) {
				saveExists = true;
			}

			// cleanup resources, once after processing
			resultSet.close();
			statement.close();

			// and then finally close connection
			connection.close();

			return saveExists;
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
			return false;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair loadSaveGame() {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			String dbURL = Game.dbPath;

			connection = DriverManager.getConnection(dbURL);

			// 저장된 상태반환
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

			// 남아있는 지뢰, timer반환
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

	// ------------------------------------------------------------------------//
	/*
	 * 기존에 저장된 게임을 삭제
	 */
	public void deleteSavedGame() {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			String dbURL = Game.dbPath;

			connection = DriverManager.getConnection(dbURL);

			// ----------EMPTY GAME_STATE TABLE------//
			String template = "DELETE FROM GAME_STATE";
			statement = connection.prepareStatement(template);
			statement.executeUpdate();

			// ----------EMPTY CELL TABLE------//
			template = "DELETE FROM CELL";
			statement = connection.prepareStatement(template);
			statement.executeUpdate();

			statement.close();

			// and then finally close connection
			connection.close();
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
		}
	}

	// --------------SAVE GAME IN DATABASE-----------//
	/*
	 * 게임저장
	 */
	public void saveGame(int timer, int mines) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			String dbURL = Game.dbPath;

			connection = DriverManager.getConnection(dbURL);

			// --------------INSERT DATA INTO CELL TABLE-----------//
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
			// --------------------------------------------------//

			// --------------------SAVE GAME STATE----------------------//
			template = "INSERT INTO GAME_STATE (TIMER,MINES) values (?,?)";
			statement = connection.prepareStatement(template);

			statement.setInt(1, timer);
			statement.setInt(2, mines);

			statement.executeUpdate();

			// ---------------------------------------------------------//

			statement.close();

			// and then finally close connection
			connection.close();
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
		}
	}
}
