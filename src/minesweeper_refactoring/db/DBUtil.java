package minesweeper_refactoring.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.util.Pair;
import minesweeper_refactoring.Game;

public class DBUtil {
	// to check whether there is a save game or not
	/*
	 * MS Access내에 저장되어 있는 테이블의 데이터를 읽어서 기존에 저장된 게임이 존재하는지 확인 Access를 대체하려면 1.파일 기반
	 * DB가 필요함(== 해당 게임 외부에 DB를 띄워서 통신하는 방식은 안되고 게임내부에서 저장소와 통신을 하는 방법만 가능)
	 * 2.상용라이센스는 안됨 3.SQL기반으로 데이터를 저장/변경/삭제가 가능해야 함
	 */
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

	// --------------LOAD SAVED GAME-----------------//

	/*
	 * 기존에 저장된 게임 load DB에 저장된 셀정보를 가져와서 저장된 상태로 복원
	 * 
	 * 파일기반 DB - Game이 1:1로 통신을 하고 복잡한 SQL이 존재하지 않기 때문에 Connection Pool이나 Mybatis같은
	 * tool을 사용하는건 적절치 않아 보인다
	 */
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
			
			/*
			for (int x = 0; x < cols; x++) {
				for (int y = 0; y < rows; y++) {
					resultSet.next();

					cells[x][y].setContent(resultSet.getString("CONTENT"));
					cells[x][y].setMine(resultSet.getBoolean("MINE"));
					cells[x][y].setSurroundingMines(resultSet.getInt("SURROUNDING_MINES"));
				}
			}
			*/
			statement.close();
			resultSet.close();
			// ----------------------------------------------------//

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
			
			/*
			for (int x = 0; x < cols; x++) {
				for (int y = 0; y < rows; y++) {
					statement.setString(1, cells[x][y].getContent());
					statement.setBoolean(2, cells[x][y].getMine());
					statement.setInt(3, (int) cells[x][y].getSurroundingMines());

					statement.executeUpdate();
				}
			}
			*/
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
