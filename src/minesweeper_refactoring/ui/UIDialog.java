package minesweeper_refactoring.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import minesweeper_refactoring.UI;

public class UIDialog {
	
	private UI uiWindow;
	
	public UIDialog(UI uiWindow) {
		this.uiWindow = uiWindow;
	}
	
	/** open simple choose dialog
	 * @param title dialog title
	 * @param dialogContent dialog contents
	 * @param options dialog choose options
	 * @param initValue dialog choose options initial value
	 * @return
	 */
	public int getOptionDialog(String title, String dialogContent, Object[] options, Object initValue) {
		ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));
		
		return JOptionPane.showOptionDialog(uiWindow, dialogContent,title, JOptionPane.YES_NO_CANCEL_OPTION,
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
}
