package minesweeper_old;

public class Main {
	/*
	 * ��������
	 * 1.���ڰ� ���� ��ĭŬ�� �̺�Ʈ ����
	 * 2.���ڰ� �ִ� ĭ Ŭ���� �̺�Ʈ�� ����� ���� �ʴ� ��찡 �����Ѵ�
	 * -> Ȯ�ΰ�� ���ڰ� �ִ� ĭ�� Ŭ���ϸ� �����ӵ��� ������ ǥ�õȴ� �̸� Ȯ���غ���
	 * -> Ŭ���̺�Ʈ ��ü�� �� ������ �ʴ� ���̽��� �ִ� �̺�Ʈ �� ���� Refactoring�� �ʿ�(������ ������ϸ� ������ ������� ���̽��� ����)
	 * -> Ȯ�ΰ�� mouseClicked�� �ƴ� mousePressed,mouseReleased�̺�Ʈ�� �ɸ��� ���̽��� �־���. mouseClicked��mouseReleased ������ �ɸ��� �̺�Ʈ�� ��ŭ
	 * mouseReleased�̺�Ʈ�� ó���� �ϸ� ������ ����
	 * 3.Game.java endGame() �ӵ� ����
	 */
	public static void main(String[] args) {
		Game game = new Game();
	}
}
