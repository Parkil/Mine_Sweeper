package minesweeper_refactoring.ui;

import javax.swing.JButton;

public class CellBtn extends JButton {

	private static final long serialVersionUID = -1594822198303286157L;

	private boolean isMineBuried = false;

	private String content = "";

	private int surroundingMineCnt = 0;

	public boolean isMineBuried() {
		return isMineBuried;
	}

	public void setMineBuried(boolean isMineBuried) {
		this.isMineBuried = isMineBuried;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getSurroundingMineCnt() {
		return surroundingMineCnt;
	}

	public void setSurroundingMineCnt(int surroundingMineCnt) {
		this.surroundingMineCnt = surroundingMineCnt;
	}
}
