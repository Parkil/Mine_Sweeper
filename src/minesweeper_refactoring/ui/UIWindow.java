package minesweeper_refactoring.ui;

import javax.swing.*;
import java.awt.*;

import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import minesweeper_refactoring.GameEventExec;
import minesweeper_refactoring.event.EventMenu;
import minesweeper_refactoring.event.EventMouse;
import minesweeper_refactoring.event.EventWindow;

public class UIWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	// The buttons
	private CellBtn[][] cellBtnArr;

	// Number of Buttons in Grid
	private int rows;
	private int cols;
	private int mineCnt;

	// Labels
	private JLabel minesLabel;

	private JLabel timePassedLabel;
	private Thread timer;
	private int timePassed;
	private boolean stopTimer;

	// Frame settings
	private final String FRAME_TITLE = "Minesweeper ~ Developed by Haris Muneer";

	private int FRAME_WIDTH = 520;
	private int FRAME_HEIGHT = 550;
	private int FRAME_LOC_X = 430;
	private int FRAME_LOC_Y = 50;

	// Icons
	private Icon redMine;
	private Icon mine;
	private Icon flag;
	private Icon tile;

	// Menu Bar and Items

	private JMenuBar menuBar;
	private JMenu gameMenu;
	private JMenuItem newGame;
	private JMenuItem statistics;
	private JMenuItem exit;
	
	private UIDialog uiDialog;
	
	//overloading
	public UIWindow(int rows, int cols, int mineCnt) {
		uiDialog = new UIDialog(this);
		
		this.rows = rows;
		this.cols = cols;
		this.mineCnt = mineCnt;
		
		cellBtnArr = new CellBtn[rows][cols];

		// Set frame
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setTitle(FRAME_TITLE);
		setLocation(FRAME_LOC_X, FRAME_LOC_Y);

		// The layout of the frame:
		JPanel gameBoard;
		JPanel tmPanel;

		// Build the "gameBoard".
		gameBoard = new JPanel();
		gameBoard.setLayout(new GridLayout(rows, cols, 0, 0));
		
		//x,y - rows,cols invert for array location matching ui board cell location
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				cellBtnArr[x][y] = new CellBtn();
				cellBtnArr[x][y].setName(x+ "," + y);
				cellBtnArr[x][y].setFont(new Font("Serif", Font.BOLD, 24));
				cellBtnArr[x][y].setBorder(BorderFactory.createLineBorder(Color.black, 1, true));
				cellBtnArr[x][y].setFocusPainted(false);
				
				gameBoard.add(cellBtnArr[x][y]);
			}
		}

		setBtnCellStatus();

		JPanel timePassedPanel = new JPanel();
		timePassedPanel.setLayout(new BorderLayout(10, 0));

		// Initialize the time passed label.
		this.timePassedLabel = new JLabel("  0  ", SwingConstants.CENTER);
		timePassedLabel.setFont(new Font("Serif", Font.BOLD, 20));

		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

		timePassedLabel.setBorder(loweredetched);
		timePassedLabel.setBackground(new Color(110, 110, 255));
		timePassedLabel.setForeground(Color.white);
		timePassedLabel.setOpaque(true);

		JLabel iT = new JLabel("", SwingConstants.CENTER);
		iT.setIcon(new ImageIcon(getClass().getResource("/resources/clock.png")));

		timePassedPanel.add(iT, BorderLayout.WEST);
		timePassedPanel.add(timePassedLabel, BorderLayout.CENTER);
		timePassedPanel.setOpaque(false);

		this.timePassed = 0;
		this.stopTimer = true;

		JPanel minesPanel = new JPanel();
		minesPanel.setLayout(new BorderLayout(10, 0));

		// Initialize mines label.
		this.minesLabel = new JLabel("  0  ", SwingConstants.CENTER);
		minesLabel.setFont(new Font("Serif", Font.BOLD, 20));
		minesLabel.setBorder(loweredetched);
		minesLabel.setBackground(new Color(110, 110, 255));
		minesLabel.setForeground(Color.white);

		minesLabel.setOpaque(true);
		setMines(mineCnt);

		JLabel mT = new JLabel("", SwingConstants.CENTER);
		mT.setIcon(new ImageIcon(getClass().getResource("/resources/mine.png")));

		minesPanel.add(minesLabel, BorderLayout.WEST);
		minesPanel.add(mT, BorderLayout.CENTER);
		minesPanel.setOpaque(false);

		// Build the "tmPanel".
		tmPanel = new JPanel();
		tmPanel.setLayout(new BorderLayout(0, 20));

		tmPanel.add(timePassedPanel, BorderLayout.WEST);
		tmPanel.add(minesPanel, BorderLayout.EAST);
		tmPanel.setOpaque(false);
		
		menuBar = new JMenuBar();

		gameMenu = new JMenu("Game");

		newGame = new JMenuItem("   New Game");
		statistics = new JMenuItem("   Statistics");
		exit = new JMenuItem("   Exit");

		newGame.setName("New Game");
		statistics.setName("Statistics");
		exit.setName("Exit");

		gameMenu.add(newGame);
		gameMenu.add(statistics);
		gameMenu.add(exit);

		menuBar.add(gameMenu);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(0, 10));
		p.add(gameBoard, BorderLayout.CENTER);
		p.add(tmPanel, BorderLayout.SOUTH);

		p.setBorder(BorderFactory.createEmptyBorder(60, 60, 14, 60));
		p.setOpaque(false);

		setLayout(new BorderLayout());
		JLabel background = new JLabel(new ImageIcon(getClass().getResource("/resources/2.jpg")));

		add(background);

		background.setLayout(new BorderLayout(0, 0));

		background.add(menuBar, BorderLayout.NORTH);
		background.add(p, BorderLayout.CENTER);

		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/mine.png")));

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		setIcon();
	}
	
	
	private void setBtnCellStatus() {
		
		int x, y;
		int currentMines = 0;
		
		while (currentMines != mineCnt) {
			x = (int) Math.floor(Math.random() * cols);
			y = (int) Math.floor(Math.random() * rows);
			
			if (!cellBtnArr[x][y].isMineBuried()) {
				cellBtnArr[x][y].setMineBuried(true);
				incSurroundMineCnt(x, y);
				currentMines++;
			}
		}
	}
	
	private void incSurroundMineCnt(int xCo, int yCo) {
		for (int x = makeValidCoordinateX(xCo - 1); x <= makeValidCoordinateX(xCo + 1); x++) {
			for (int y = makeValidCoordinateY(yCo - 1); y <= makeValidCoordinateY(yCo + 1); y++) {
				if (x != xCo || y != yCo) {
					int orgCnt = cellBtnArr[x][y].getSurroundingMineCnt();
					cellBtnArr[x][y].setSurroundingMineCnt(++orgCnt);
				}
			}
		}
	}
	
	public void findZeroes(int xCo, int yCo) {
		int surroundMineCnt;

		for (int x = makeValidCoordinateX(xCo - 1); x <= makeValidCoordinateX(xCo + 1); x++) {
			for (int y = makeValidCoordinateY(yCo - 1); y <= makeValidCoordinateY(yCo + 1); y++) {
				if ("".equals(cellBtnArr[x][y].getContent())) {
					CellBtn btn = cellBtnArr[x][y];
					
					surroundMineCnt = btn.getSurroundingMineCnt();
					btn.setContent(Integer.toString(surroundMineCnt));
					
					if (!btn.isMineBuried()) {
						btn.setIcon(null);
					}
					
					if (surroundMineCnt == 0) {
						btn.setBackground(Color.white);
						btn.setText("");
						findZeroes(x, y);
					} else {
						btn.setBackground(Color.white);
						btn.setText(Integer.toString(surroundMineCnt));
						setTextColor(btn);
					}
				}
			}
		}
	}
	
	private int makeValidCoordinateX(int i) {
		if (i < 0)
			i = 0;
		else if (i > cols - 1)
			i = cols - 1;

		return i;
	}
	
	private int makeValidCoordinateY(int i) {
		if (i < 0)
			i = 0;
		else if (i > rows - 1)
			i = rows - 1;

		return i;
	}
	
	public void resetBtn() {
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cellBtnArr[x][y].setContent("");
			}
		}
	}
	
	public void startTimer() {
		stopTimer = false;

		timer = new Thread() {
			@Override
			public void run() {
				while (!stopTimer) {
					timePassed++;

					// Update the time passed label.
					timePassedLabel.setText("  " + timePassed + "  ");

					// Wait 1 second.
					try {
						sleep(1000);
					} catch (InterruptedException ex) {
					}
				}
			}
		};

		timer.start();
	}

	public void interruptTimer() {
		stopTimer = true;

		try {
			if (timer != null)
				timer.join();
		} catch (InterruptedException ex) {

		}
	}

	public void resetTimer() {
		timePassed = 0;
		timePassedLabel.setText("  " + timePassed + "  ");
	}

	public void setTimePassed(int t) {
		timePassed = t;
		timePassedLabel.setText("  " + timePassed + "  ");
	}

	public void initGame() {
		hideAll();
		enableAll();
	}

	// Makes buttons clickable
	public void enableAll() {
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cellBtnArr[x][y].setEnabled(true);
			}
		}
	}

	// Makes buttons non-clickable
	public void disableAll() {
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				cellBtnArr[x][y].setEnabled(false);
			}
		}
	}

	// Resets the content of all buttons
	public void hideAll() {
		for(int x=0 ; x<rows ; x++) {
			for(int y=0 ; y<cols ; y++) {
				cellBtnArr[x][y].setText("");
				cellBtnArr[x][y].setBackground(new Color(0, 103, 200));
				cellBtnArr[x][y].setIcon(tile);
			}
		}
	}
	
	public void setButtonListeners(GameEventExec exec) {
		addWindowListener(new EventWindow(exec));
		
		EventMouse eventMouse = new EventMouse(exec);
		for(int x=0 ; x<rows ; x++) {
			for(int y=0 ; y<cols ; y++) {
				cellBtnArr[x][y].addMouseListener(eventMouse);
			}
		}
		
		newGame.addActionListener(new EventMenu(exec));
		statistics.addActionListener(new EventMenu(exec));
		exit.addActionListener(new EventMenu(exec));
		
		newGame.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		exit.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		statistics.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	public CellBtn[][] getCellBtnArr() {
		return cellBtnArr;
	}

	public int getTimePassed() {
		return timePassed;
	}

	public void setLook(String look) {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if (look.equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setMines(int m) {
		mineCnt = m;
		minesLabel.setText("  " + Integer.toString(m) + "  ");
	}

	public void incMines() {
		mineCnt++;
		setMines(mineCnt);
	}

	public void decMines() {
		mineCnt--;
		setMines(mineCnt);
	}

	public int getMines() {
		return mineCnt;
	}

	private static Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
		Image img = icon.getImage();
		Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight, java.awt.Image.SCALE_SMOOTH);
		return new ImageIcon(resizedImage);
	}

	public void setIcon() {
		CellBtn btn = cellBtnArr[0][0];
		int bOffset = btn.getInsets().left;
		int bWidth = btn.getWidth();
		int bHeight = btn.getHeight();

		ImageIcon d;

		d = new ImageIcon(getClass().getResource("/resources/redmine.png"));
		redMine = resizeIcon(d, bWidth - bOffset, bHeight - bOffset);

		d = new ImageIcon(getClass().getResource("/resources/mine.png"));
		mine = resizeIcon(d, bWidth - bOffset, bHeight - bOffset);

		d = new ImageIcon(getClass().getResource("/resources/flag.png"));
		flag = resizeIcon(d, bWidth - bOffset, bHeight - bOffset);

		d = new ImageIcon(getClass().getResource("/resources/tile.png"));
		tile = resizeIcon(d, bWidth - bOffset, bHeight - bOffset);
	}

	public Icon getIconMine() {
		return mine;
	}

	public Icon getIconRedMine() {
		return redMine;
	}

	public Icon getIconFlag() {
		return flag;
	}

	public Icon getIconTile() {
		return tile;
	}
	
	public UIDialog getUIDialog() {
		return uiDialog;
	}
	
	public void setTextColor(JButton b) {
		if (b.getText().equals("1"))
			b.setForeground(Color.blue);
		else if (b.getText().equals("2"))
			b.setForeground(new Color(76, 153, 0));
		else if (b.getText().equals("3"))
			b.setForeground(Color.red);
		else if (b.getText().equals("4"))
			b.setForeground(new Color(153, 0, 0));
		else if (b.getText().equals("5"))
			b.setForeground(new Color(153, 0, 153));
		else if (b.getText().equals("6"))
			b.setForeground(new Color(96, 96, 96));
		else if (b.getText().equals("7"))
			b.setForeground(new Color(0, 0, 102));
		else if (b.getText().equals("8"))
			b.setForeground(new Color(153, 0, 76));
	}
	
	public void setButtonImages() {
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				cellBtnArr[x][y].setIcon(null);
				
				if ("".equals(cellBtnArr[x][y].getContent())) {
					cellBtnArr[x][y].setIcon(getIconTile());
				} else if ("F".equals(cellBtnArr[x][y].getContent())) {
					cellBtnArr[x][y].setIcon(getIconFlag());
					cellBtnArr[x][y].setBackground(Color.blue);
				} else if ("0".equals(cellBtnArr[x][y].getContent())) {
					cellBtnArr[x][y].setBackground(Color.white);
				} else {
					cellBtnArr[x][y].setBackground(Color.white);
					cellBtnArr[x][y].setText(cellBtnArr[x][y].getContent());
					setTextColor(cellBtnArr[x][y]);
				}
			}
		}
	}
}
