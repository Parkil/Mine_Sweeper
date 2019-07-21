package minesweeper_refactoring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;

import javafx.util.Pair; //netbean ide에서 제공하는 라이브러리

public class Board {
	private int totMineCnt; //설정된 지뢰 총수
	private Cell cells[][];

	private int rows;
	private int cols;
	
	private HashMap<JButton, Cell> indexMap = new HashMap<JButton, Cell>(); //JButton과 Cell을 연결하는 인덱스 정보

	// ---------------------------------------------//

	/*
	 * 지뢰찾기의 바둑판 정보 생성
	 */
	public Board(int numberOfMines, int r, int c) {
		/*
		this.rows = r;
		this.cols = c;
		this.totMineCnt = numberOfMines;

		cells = new Cell[rows][cols];

		// Step 1: First create a board with empty Cells
		createEmptyCells();

		// Step 2: Then set mines randomly at cells
		setMines();

		// Step 3: Then set the number of surrounding mines("neighbours") at each cell
		setSurroundingMinesNumber();
		*/
	}

	// ------------------------------------------------------------------//
	// STEP 1//
	/*
	 * 배열에 생성된 Cell 초기화
	 */
	public void createEmptyCells() {
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cells[x][y] = new Cell(x, y);
				
				JButton btn = new JButton();
				indexMap.put(btn, cells[x][y]);
			}
		}
	}

	// ------------------------------------------------------------------//
	// STEP 2//
	/*
	 * 셀에 지뢰를 설정
	 */
	public void setMines() {
		int x, y;
		boolean hasMine;
		int currentMines = 0;

		while (currentMines != totMineCnt) {
			// Generate a random x coordinate (between 0 and cols)
			x = (int) Math.floor(Math.random() * cols);

			// Generate a random y coordinate (between 0 and rows)
			y = (int) Math.floor(Math.random() * rows);

			hasMine = cells[x][y].getMine();

			if (!hasMine) {
				cells[x][y].setMine(true);
				currentMines++;
			}
		}
	}
	// ------------------------------------------------------------------//

	// ------------------------------------------------------------------//
	// STEP 3//
	/*
	 * 각 셀마다 주변 지뢰의 개수를 표시 지금은 전체 셀을 기준으로 체크하고 있으나 지뢰로 설정된 셀에만 진행을 하면 되지 않나?
	 */
	public void setSurroundingMinesNumber() {
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cells[x][y].setSurroundingMines(calculateNeighbours(x, y));
			}
		}
	}
	// ------------------------------------------------------------------//

	// ---------------------HELPER FUNCTIONS---------------------------//

	// 지뢰 주변의 숫자생성
	// 해당 x,y좌표의 근처(자기 주변 8칸)에 지뢰가 있으면 지뢰개수를 현재 칸에 표시
	/*
	 * 해당코드에서는 x,y좌표가 유효한지 체크를 했지만, 이게 필요할지는 의문 -> 지정된 범위를 넘어가는곳에 지뢰가 존재할리 없기 때문
	 * 
	 * 현재좌표 x,y의 상하좌우 4칸 + 각방향 대각선 4칸
	 */
	// Calculates the number of surrounding mines ("neighbours")
	public int calculateNeighbours(int xCo, int yCo) {
		int neighbours = 0;

		// Check the neighbours (the columns xCo - 1, xCo, xCo + 1)
		for (int x = makeValidCoordinateX(xCo - 1); x <= makeValidCoordinateX(xCo + 1); x++) {
			// Check the neighbours (the rows yCo - 1, yCo, yCo + 1).
			for (int y = makeValidCoordinateY(yCo - 1); y <= makeValidCoordinateY(yCo + 1); y++) {
				// Skip (xCo, yCo), since that's no neighbour.
				if (x != xCo || y != yCo)
					if (cells[x][y].getMine()) // If the neighbour contains a mine, neighbours++.
						neighbours++;
			}
		}

		return neighbours;
	}

	// ------------------------------------------------------------------//

	// Simply makes a coordinate a valid one (i.e within the boundaries of the
	// Board)
	/*
	 * 입력된 x좌표가 생성된 board의 x좌표내에 있는지 확인
	 */
	public int makeValidCoordinateX(int i) {
		if (i < 0)
			i = 0;
		else if (i > cols - 1)
			i = cols - 1;

		return i;
	}

	// Simply makes a coordinate a valid one (i.e within the boundaries of the
	// Board)
	/*
	 * 입력된 y좌표가 생성된 board의 y좌표내에 있는지 확인
	 */
	public int makeValidCoordinateY(int i) {
		if (i < 0)
			i = 0;
		else if (i > rows - 1)
			i = rows - 1;

		return i;
	}

	// ------------------------------------------------------------------//

	// -------------DATA BASE------------------------//

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

			for (int x = 0; x < cols; x++) {
				for (int y = 0; y < rows; y++) {
					resultSet.next();

					cells[x][y].setContent(resultSet.getString("CONTENT"));
					cells[x][y].setMine(resultSet.getBoolean("MINE"));
					cells[x][y].setSurroundingMines(resultSet.getInt("SURROUNDING_MINES"));
				}
			}

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

			for (int x = 0; x < cols; x++) {
				for (int y = 0; y < rows; y++) {
					statement.setString(1, cells[x][y].getContent());
					statement.setBoolean(2, cells[x][y].getMine());
					statement.setInt(3, (int) cells[x][y].getSurroundingMines());

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

	// --------------------------------------------//
	// ---------GETTERS AND SETTERS-------------//
	public void setNumberOfMines(int numberOfMines) {
		this.totMineCnt = numberOfMines;
	}

	public int getNumberOfMines() {
		return totMineCnt;
	}

	public Cell[][] getCells() {
		return cells;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}
	
	//JButton 리스트 반환
	public List<JButton> getJButtonList() {
		return indexMap.keySet().stream().collect(Collectors.toList());
	}
	
	//JButton객체를 이용한 리스트 반환
	public Cell getCellByJButton(JButton btn) {
		return indexMap.get(btn);
	}
	
	public JButton getJButtonByCell(Cell cell) {
		JButton result = null;
		Iterator<JButton> keyIter = indexMap.keySet().iterator();
		while(keyIter.hasNext()) {
			JButton key = keyIter.next();
			Cell value = indexMap.get(key);
			if(value.equals(cell)) {
				result = key;
				break;
			}
		}
		
		return result;
	}
	
	
	// -----------------------------------------//

	public void resetBoard() {
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cells[x][y].setContent("");
			}
		}
	}

}
