package game.frame;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import game.logics.handler.Logics;
import game.logics.handler.LogicsHandler;
import game.utility.debug.Debugger;
import game.utility.input.keyboard.KeyHandler;
import game.utility.screen.Screen;
import game.utility.screen.ScreenHandler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * The <code>GameWindow</code> class manages both the panel representing 
 * the game window and the execution of the game.
 * 
 * <p>
 * The execution loop is handled by a thread implemented in this class.
 * As long as this thread is alive the game will continue to run.
 * </p>
 * 
 * @author Daniel Pellanda
 * 
 */
public class GameWindow extends JPanel implements Runnable{

	private static final long serialVersionUID = 1L;
	
	public static final long nanoSecond = 1000000000;
	public static final long milliSecond = 1000000;
	
	/**
	 * Defines the cap for the "Frames Per Second". 
	 * 
	 * <p>
	 * The game loop (coded in <code>GameWindow.run()</code>) is executed as many times as
	 * specified in <code>fpsLimit</code> each second.
	 * </p>
	 * 
	 * <p>
	 * E.G.: if <code>fpsLimit</code> value is 60, it means that the game loop will be
	 * executed 60 times per second.
	 * </p>
	 * 
	 */
	public static final int fpsLimit = 60;
	/**
	 * Shows at how many "Frames Per Second" is currently running the game.
	 */
	private int fps = 0;
	
	/**
	 * Stores the screen information (resolution, size of each tile, etc).
	 */
	private final Screen gameScreen = new ScreenHandler();
	/**
	 * Listens the press of keys of the keyboard.
	 */
	private final KeyHandler keyH = new KeyHandler();
	/**
	 * Handles the logic part of the game (entities, interface, game state, etc). 
	 */
	private final Logics logH;
	
	private final Thread gameLoop = new Thread(this);
	private boolean gameRunning = false;
	
	/**
	 * Manages enabling and disabling of Debug Features.
	 */
	private final Debugger debugger;
	
	/**
	 * Basic constructor that sets <code>JPanel</code> attributes and sets up the <code>Logics</code> handler
	 * and the <code>Debugger</code>.
	 * 
	 * @param debug starting mode for the <code>Debugger</code>
	 */
	public GameWindow(final boolean debug) {
		super();
		
		/// Sets up the basic JPanel parameters /// 
		this.setPreferredSize(gameScreen.getScreenSize());
		this.setBackground(Color.black);
		this.setDoubleBuffered(true);
		this.setFocusable(true);
		
		/// Links the keyboard listener to the JPanel ///
		this.addKeyListener(keyH);
		
		/// Sets up Debugger and Logics handler ///
		this.debugger = new Debugger(debug, () -> fps);
		this.logH = new LogicsHandler(gameScreen, keyH, debugger);
	}
	
	/**
	 * Begins the execution of the game loop.
	 */
	public void startGame() {
		gameRunning = true;
		gameLoop.start();
	}
	
	public void stopGame() {
		gameRunning = false;
	}
	
	/**
	 * Updates the class parameters for each frame.
	 */
	private void update() {
		// Updates Logics parameters
		logH.updateAll();
	}
	
	/**
	 * Decides what to draw for each frame.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D board = (Graphics2D)g;
		
		// Draws FPS meter if enabled by debugger
		board.setColor(Color.white);
		if(debugger.isFeatureEnabled("fps meter")) {
			g.setFont(Debugger.debugFont);
			g.drawString("FPS: " + fps, 3, 8);
		}
		
		// Draws logical parts of the game
		logH.drawAll(board);
		
		board.dispose();
	}
	
	/**
	 * Runs the loop, keeping the game alive.
	 */
	@Override
	public void run() {
		// Defines how many nanoseconds have to pass until the next execution loop
		double drawInterval = nanoSecond / fpsLimit;
		// System time after interval has passed
		double nextDraw = System.nanoTime() + drawInterval;
		// Nanoseconds passed from the current loop
		long drawTime = 0;
		// FPS counted for the current second
		int fpsCount = 0;
		
		while(gameLoop.isAlive() && gameRunning) {
			
			// Gets current system time
			long timer = System.nanoTime();
			
			// Updates FPS meter each second passed
			if(drawTime > nanoSecond) {
				fps = fpsCount;
				drawTime = 0;
				fpsCount = 0;
				
				// Prints FPS on console
				if(debugger.isFeatureEnabled("log: fps")) {
					System.out.print("FPS: " + fps + "\n");
				}
			}
			
			/// RUNS EACH FRAME ///
			update();
			repaint();
			
			fpsCount++;
			try {
				// Thread sleeps until it's next loop time
				double sleepTime = nextDraw - System.nanoTime();
				sleepTime = sleepTime < 0 ? 0 : sleepTime / milliSecond;
				Thread.sleep((long) sleepTime);
				
				// Sets up the next loop time for the next frame
				nextDraw = System.nanoTime() + drawInterval;
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog((Component)this, "An error occured! \n Details: \n\n" + e.getMessage() , "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				System.exit(-1);
			}
			
			// Adds the time passed since the last second 
			drawTime += System.nanoTime() - timer;
		}
	}
}