package minesweeper_refactoring;

public class Cell {
	private boolean mine;

	// Only the content of the Cell is visible to the player.
	private String content; // 셀의 상태를 저장

	// Number of adjacent surrounding mines
	private int surroundingMines;

	private int xCo;
	
	private int yCo;

	// ----------------------------------------------------------//

	public Cell(int xCo, int yCo) {
		mine = false;
		content = "";
		surroundingMines = 0;
		
		this.xCo = xCo;
		this.yCo = yCo;
	}

	// -------------GETTERS AND SETTERS----------------------------//
	public boolean getMine() {
		return mine;
	}

	public void setMine(boolean mine) {
		this.mine = mine;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getSurroundingMines() {
		return surroundingMines;
	}

	public void setSurroundingMines(int surroundingMines) {
		this.surroundingMines = surroundingMines;
	}

	public int getxCo() {
		return xCo;
	}

	public int getyCo() {
		return yCo;
	}
	
	@Override
	public int hashCode() {
		int result = 15;
		result += 37 * result + ((mine == true) ? 1 : 0);
		result += 37 * result + surroundingMines;
		result += 37 * result + xCo;
		result += 37 * result + yCo;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		
		if(obj instanceof Cell == false) {
			return false;
		}
		
		Cell cell = (Cell)obj;
		
		return (this.mine == cell.getMine()) && (this.surroundingMines == cell.getSurroundingMines()) && (this.xCo == cell.getxCo()) &&(this.yCo == cell.getyCo());
	}
	// -------------------------------------------------------------//
}
