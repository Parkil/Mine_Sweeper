package minesweeper_refactoring.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import minesweeper_refactoring.UI;

public class UIDialog {
	
	private UI uiWindow;
	
	public UIDialog(UI uiWindow) {
		this.uiWindow = uiWindow;
	}
	
	/** open simple choose dialog(Y,N,Cancel Option)
	 * @param title dialog title
	 * @param dialogContent dialog contents
	 * @param options dialog choose options
	 * @param initValue dialog choose options initial value
	 * @return
	 */
	public int getOptionYNCancelDialog(String title, String dialogContent, Object[] options, Object initValue) {
		ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));
		
		return JOptionPane.showOptionDialog(uiWindow, dialogContent,title, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, question, options, initValue);
	}
	
	/** open simple choose dialog(Y,N Option)
	 * @param title dialog title
	 * @param dialogContent dialog contents
	 * @param options dialog choose options
	 * @param initValue dialog choose options initial value
	 * @return
	 */
	public int getOptionYNDialog(String title, String dialogContent, Object[] options, Object initValue) {
		ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));
		
		return JOptionPane.showOptionDialog(uiWindow, dialogContent,title, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, question, options, initValue);
	}
	
	
	
	/** create save progress dialog by window closing
	 * @return
	 */
	public JDialog windowClosingSaveDialog() {
		JDialog dialog = new JDialog(uiWindow, Dialog.ModalityType.DOCUMENT_MODAL);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panel.add(new JLabel("Saving.... Please Wait", SwingConstants.CENTER));
		dialog.add(panel);
		dialog.setTitle("Saving Game...");
		dialog.pack();
		dialog.setLocationRelativeTo(uiWindow);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		return dialog;
	}
	
	/**게임에서 승리했을때 표시되는 dialog를 반환
	 * 
	 * @return 표시되는 dialog와 스코어에 따른 조작이 필요한 컴포넌트를 저장한 HashMap
	 */
	public HashMap<String,Object> gameWonDialog() {
		HashMap<String,Object> retMap = new HashMap<String,Object>();
		
		JDialog dialog = new JDialog(uiWindow, Dialog.ModalityType.DOCUMENT_MODAL);
		retMap.put("dialog", dialog);
		
		JLabel message = new JLabel("Congratulations, you won the game!", SwingConstants.CENTER);

		JPanel statistics = new JPanel();
		statistics.setLayout(new GridLayout(6, 1, 0, 10)); //gameLost에서는 5,1,0,10
		
		/*
		 * JLabel을 생성하고 다른 Class에서 Label의 내용을 setText를 이용하여 변경하고자 할때
		 * new JLabel() 이나 new JLabel("")로 초기화를 하면 setText에서 텍스트 변경시 변경이 되지 않음
		 * 반드시 임시문자열로 생성을 해주어야 정상적으로 변경됨
		 * (jdk 1.8에서 테스트)
		 */
		JLabel bestTimeAnn = new JLabel("temp");
		JLabel time = new JLabel("temp");
		JLabel bestTime = new JLabel("temp");
		JLabel gPlayed = new JLabel("temp");
		JLabel gWon = new JLabel("temp");
		JLabel gPercentage = new JLabel("temp");
		
		retMap.put("bestTimeAnn", bestTimeAnn);
		retMap.put("time", time);
		retMap.put("bestTime", bestTime);
		retMap.put("gPlayed", gPlayed);
		retMap.put("gWon", gWon);
		retMap.put("gPercentage", gPercentage);
		
		statistics.add(bestTimeAnn);
		statistics.add(time);
		statistics.add(bestTime);
		statistics.add(gPlayed);
		statistics.add(gWon);
		statistics.add(gPercentage);

		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		statistics.setBorder(loweredetched);

		// --------BUTTONS----------//
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2, 10, 0));

		JButton exitBtn = new JButton("Exit");
		JButton playAgainBtn = new JButton("Play Again");
		
		retMap.put("exitBtn", exitBtn);
		retMap.put("playAgainBtn", playAgainBtn);
		
		buttons.add(exitBtn);
		buttons.add(playAgainBtn);

		// --------DIALOG-------------//

		JPanel c = new JPanel();
		c.setLayout(new BorderLayout(20, 20));
		c.add(message, BorderLayout.NORTH);
		c.add(statistics, BorderLayout.CENTER);
		c.add(buttons, BorderLayout.SOUTH);

		c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		dialog.setTitle("Game Won");
		dialog.add(c);
		dialog.pack();
		dialog.setLocationRelativeTo(uiWindow);
		
		return retMap;
	}
	
	/**게임에서 패배했을때 표시되는 dialog를 반환
	 * 
	 * @return 표시되는 dialog와 스코어에 따른 조작이 필요한 컴포넌트를 저장한 HashMap
	 */
	public HashMap<String,Object> gameLostDialog() {
		HashMap<String,Object> retMap = new HashMap<String,Object>();
		
		JDialog dialog = new JDialog(uiWindow, Dialog.ModalityType.DOCUMENT_MODAL);
		retMap.put("dialog", dialog);
		
		JLabel message = new JLabel("Sorry, you lost this game. Better luck next time!", SwingConstants.CENTER);

		JPanel statistics = new JPanel();
		statistics.setLayout(new GridLayout(5, 1, 0, 10));
		
		/*
		 * JLabel을 생성하고 다른 Class에서 Label의 내용을 setText를 이용하여 변경하고자 할때
		 * new JLabel() 이나 new JLabel("")로 초기화를 하면 setText에서 텍스트 변경시 변경이 되지 않음
		 * 반드시 임시문자열로 생성을 해주어야 정상적으로 변경됨
		 * (jdk 1.8에서 테스트)
		 */
		JLabel time = new JLabel("temp");
		JLabel bestTime = new JLabel("temp");
		JLabel gPlayed = new JLabel("temp");
		JLabel gWon = new JLabel("temp");
		JLabel gPercentage = new JLabel("temp");
		
		retMap.put("time", time);
		retMap.put("bestTime", bestTime);
		retMap.put("gPlayed", gPlayed);
		retMap.put("gWon", gWon);
		retMap.put("gPercentage", gPercentage);
		
		statistics.add(time);
		statistics.add(bestTime);
		statistics.add(gPlayed);
		statistics.add(gWon);
		statistics.add(gPercentage);

		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		statistics.setBorder(loweredetched);

		// --------BUTTONS----------//
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 3, 2, 0));

		JButton exitBtn = new JButton("Exit");
		JButton restartBtn = new JButton("Restart");
		JButton playAgainBtn = new JButton("Play Again");
		
		retMap.put("exitBtn", exitBtn);
		retMap.put("restartBtn", restartBtn);
		retMap.put("playAgainBtn", playAgainBtn);
		
		buttons.add(exitBtn);
		buttons.add(restartBtn);
		buttons.add(playAgainBtn);

		// --------DIALOG-------------//

		JPanel c = new JPanel();
		c.setLayout(new BorderLayout(20, 20));
		c.add(message, BorderLayout.NORTH);
		c.add(statistics, BorderLayout.CENTER);
		c.add(buttons, BorderLayout.SOUTH);

		c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		dialog.setTitle("Game Lost");
		dialog.add(c);
		dialog.pack();
		dialog.setLocationRelativeTo(uiWindow);
		
		return retMap;
	}
}
