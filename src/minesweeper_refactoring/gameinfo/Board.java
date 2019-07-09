package minesweeper_refactoring.gameinfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.util.Pair; //netbean ide���� �����ϴ� ���̺귯��

/*
 * ��������
 * 1.DB�� ����ϴ� ������ 1���� Ŭ������ ���� ����.
 * 
 */
public class Board {
	private int numberOfMines;
	private Cell cells[][];

	private int rows;
	private int cols;

	// ---------------------------------------------//

	/*
	 * ����ã���� �ٵ��� ���� ����
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
	 * �迭�� ������ Cell �ʱ�ȭ
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
	 * ���� ���ڸ� ����
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
	 * �� ������ �ֺ� ������ ������ ǥ�� ������ ��ü ���� �������� üũ�ϰ� ������ ���ڷ� ������ ������ ������ �ϸ� ���� �ʳ�?
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

	// ���� �ֺ��� ���ڻ���
	// �ش� x,y��ǥ�� ��ó(�ڱ� �ֺ� 8ĭ)�� ���ڰ� ������ ���ڰ����� ���� ĭ�� ǥ��
	/*
	 * �ش��ڵ忡���� x,y��ǥ�� ��ȿ���� üũ�� ������, �̰� �ʿ������� �ǹ� -> ������ ������ �Ѿ�°��� ���ڰ� �����Ҹ� ���� ����
	 * 
	 * ������ǥ x,y�� �����¿� 4ĭ + ������ �밢�� 4ĭ
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
	 * �Էµ� x��ǥ�� ������ board�� x��ǥ���� �ִ��� Ȯ��
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
	 * �Էµ� y��ǥ�� ������ board�� y��ǥ���� �ִ��� Ȯ��
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
