package minesweeper_refactoring;

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
	 * 4.���ڸ� �����ϰų�, ������ Ŭ�����ؼ� restart�� �ϰ� �Ǹ� ���ڸ� ���ġ �ؾ� �ϴµ� ���ڰ� �״�� ����
	 * 
	 * event�� ����(�̺�Ʈ�� ���� Ŭ������ �����ϰ� listener�� �Ķ���ͷ� Ŭ���� ������ �ѱ�)�� �ص� ����� �ӵ����� ȿ���� �ִ�.
	 * ->Event�� ������ ��� 1���� Ŭ������ ���� ������ �ѱ�� �ӵ����������� �߻��ϴµ�
	 * 
	 * UI - extends JFrame�̱� ������ Frame ���õ� ���׸� ó��
	 * dialog�� ���� Ŭ������ ���� ó��
	 * 
	 * ����� UI�� ���� ��Ȳ���� JUnit test case�� �ۼ��Ҽ� �ִ��� Ȯ�� �ʿ�
	 */
	public static void main(String[] args) {
		Game game = new Game();
	}
}