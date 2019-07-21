package minesweeper_old;

public class Main {
	/*
	 * 개선할점
	 * 1.숫자가 없는 빈칸클릭 이벤트 제거
	 * 2.지뢰가 있는 칸 클릭시 이벤트가 제대로 먹지 않는 경우가 존재한다
	 * -> 확인결과 지뢰가 있는 칸을 클릭하면 반응속도가 느리게 표시된다 이를 확인해볼것
	 * -> 클릭이벤트 자체가 잘 먹히지 않는 케이스가 있다 이벤트 쪽 부터 Refactoring이 필요(게임을 재시작하면 증상이 사라지는 케이스가 있음)
	 * -> 확인결과 mouseClicked가 아닌 mousePressed,mouseReleased이벤트만 걸리는 케이스가 있었음. mouseClicked가mouseReleased 다음에 걸리는 이벤트인 만큼
	 * mouseReleased이벤트에 처리를 하면 문제가 없음
	 * 3.Game.java endGame() 속도 개선
	 */
	public static void main(String[] args) {
		Game game = new Game();
	}
}
