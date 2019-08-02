package minesweeper_refactoring;

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
	 * 4.지뢰를 선택하거나, 게임을 클리어해서 restart를 하게 되면 지뢰를 재배치 해야 하는데 지뢰가 그대로 있음
	 * 
	 * event를 위임(이벤트를 별도 클래스로 생성하고 listener에 파라메터로 클래스 참조를 넘김)만 해도 상당한 속도개선 효과가 있다.
	 * ->Event를 여러개 묶어서 1개의 클래스로 만들어서 참조를 넘기면 속도저하현상이 발생하는듯
	 * 
	 * UI - extends JFrame이기 때문에 Frame 관련된 사항만 처리
	 * dialog는 따로 클래스를 빼서 처리
	 * 
	 * 사용자 UI가 들어가는 상황에서 JUnit test case를 작성할수 있는지 확인 필요
	 * 
	 * [해야할일]
	 * Game에 들어있는 Swing구성요소를 UI로 이관(처리)
	 * 전체적으로 테스트를 진행하고 예전 소스와 다르게 처리되는 부분을 수정
	 */
	public static void main(String[] args) {
		new Game();
	}
}
