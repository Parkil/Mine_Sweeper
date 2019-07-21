package minesweeper_refactoring.ui;

import javax.swing.JButton;

public class CellBtn extends JButton {

	private static final long serialVersionUID = -1594822198303286157L;

	private boolean isMineBuried = false; //해당 셀에 지뢰가 묻혀있는지 여부 

	private String content = ""; //해당셀의 상태

	private int surroundingMineCnt = 0; //해당셀주변의 지뢰개수

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
