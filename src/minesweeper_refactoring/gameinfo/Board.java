package minesweeper_refactoring.gameinfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.util.Pair; //netbean ide에서 제공하는 라이브러리

/*
 * 개선방향
 * 1.DB와 통신하는 로직을 1개의 클래스로 따로 뺀다.
 * 
 */
public class Board {
	private int numberOfMines;
	private Cell cells[][];

	private int rows;
	private int cols;

	// ---------------------------------------------//

	/*
	 * 지뢰찾기의 바둑판 정보 생성
	 */
	public Board(int numberOfMines, int r, int c) {
		this.rows = r;
		this.cols = c;
		this.numberOfMines = numberOfMines;

		cells = new Cell[rows][cols];

		// Step 1: First create a board with empty Cells
		createEmptyCells();

		// Step 2: Then set mines randomly at cells
		setMines();

		// Step 3: Then set the number of surrounding mines("neighbours") at each cell
		setSurroundingMinesNumber();
	}

	// ------------------------------------------------------------------//
	// STEP 1//
	/*
	 * 배열에 생성된 Cell 초기화
	 */
	public void createEmptyCells() {
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cells[x][y] = new Cell();
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

		while (currentMines != numberOfMines) {
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

	

	// --------------------------------------------//
	// ---------GETTERS AND SETTERS-------------//
	public void setNumberOfMines(int numberOfMines) {
		this.numberOfMines = numberOfMines;
	}

	public int getNumberOfMines() {
		return numberOfMines;
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
	// -----------------------------------------//

	public void resetBoard() {
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cells[x][y].setContent("");
			}
		}
	}

}
