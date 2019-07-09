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
	 * MS Access���� ����Ǿ� �ִ� ���̺��� �����͸� �о ������ ����� ������ �����ϴ��� Ȯ�� Access�� ��ü�Ϸ��� 1.���� ���
	 * DB�� �ʿ���(== �ش� ���� �ܺο� DB�� ����� ����ϴ� ����� �ȵǰ� ���ӳ��ο��� ����ҿ� ����� �ϴ� ����� ����)
	 * 2.�����̼����� �ȵ� 3.SQL������� �����͸� ����/����/������ �����ؾ� ��
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
	 * ������ ����� ���� load DB�� ����� �������� �����ͼ� ����� ���·� ����
	 * 
	 * ���ϱ�� DB - Game�� 1:1�� ����� �ϰ� ������ SQL�� �������� �ʱ� ������ Connection Pool�̳� Mybatis����
	 * tool�� ����ϴ°� ����ġ �ʾ� ���δ�
	 */
	public Pair loadSaveGame() {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			String dbURL = Game.dbPath;

			connection = DriverManager.getConnection(dbURL);

			// ����� ���¹�ȯ
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

			// �����ִ� ����, timer��ȯ
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
	 * ������ ����� ������ ����
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
	 * ��������
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
