//	Pong.java
//	Justin, Ruth, Seth
//	OCCC 11/15/14
//	Advanced Java
//	Final Project
//	Last updated 12/06/2014

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JOptionPane;
import java.util.Random;
import sun.audio.*;
import java.io.*;

public class Pong extends JFrame{

	//	Named-constants
	//private static final int WIDTH = 602;
	private static final int DIAMETER = 602;
	private static final int UPDATE_INTERVAL = 30;
	private static final int CANVAS_WIDTH = 850;
	private static final int CANVAS_HEIGHT = 710;
	private static final int BLUE_ANGLE_START = 90;
	private static final int RED_ANGLE_START = -90;
	private static final int COUNT_DOWN_START = 3;
	private static final Color BACKGROUND_COLOR2 = new Color(150, 150, 200, 150);
	private static final Color BACKGROUND_COLOR = new Color(150, 150, 200, 100);
		
	//	Variables for game position
	private static int paddleX, paddleY;
	private static int xPosition, yPosition;
	private static int center_x;
	private static int center_y;
	private static int blueOffset, redOffset;
	
	//	Game components
	private static Paddle bluePaddle, redPaddle;
	private static Ball ball;
	private static int blueScore, redScore;
	private static int lastHit;
	private static int quadrant;
	private static double angle, speed;
	private static Random randNum;
	private static boolean mode360, countingDown, bouncing;
	private static Timer countDownTimer;
	private static int countDown;
	private static InputStream inputStream;
	private static AudioStream countDownStream, checkStream, uncheckStream,
		radioSelectStream, bounceStream, scoreStream, fireworksStream, gameOverStream,
		backgroundStream;
	private static int playTo;
	private static boolean gameOver;
	private static Color victoryColor;
	//	For storing key events
	private static boolean aIsPressed, dIsPressed, leftIsPressed, rightIsPressed;
	
	//	The drawing canvas (extends JPanel)	
	GamePainter gp;
		
	//	Pong Constructor
	public Pong(){
	
		/*****************		Initialization		*****************/
		ball = new Ball(Color.WHITE);
		lastHit = 1;				
		quadrant = 0;
		angle = 0;
		speed = 7;
		playTo = 20;
		gameOver = false;
		randNum = new Random(System.currentTimeMillis());
		mode360 = true;
		countingDown = true;
		bouncing = false;
		countDownTimer = new Timer( 1000, new CountDownListener() );
		victoryColor = Color.RED;
		
		try{
			countDownStream = new AudioStream(new FileInputStream("resources/sounds/countDown.wav"));
			checkStream = new AudioStream(new FileInputStream("resources/sounds/check.wav"));
			uncheckStream = new AudioStream(new FileInputStream("resources/sounds/uncheck.wav"));
			radioSelectStream = new AudioStream(new FileInputStream("resources/sounds/radioSelect.wav"));
			bounceStream = new AudioStream(new FileInputStream("resources/sounds/bounce.wav"));
			scoreStream = new AudioStream(new FileInputStream("resources/sounds/score.wav"));
			gameOverStream = new AudioStream(new FileInputStream("resources/sounds/gameOver.wav"));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		aIsPressed = dIsPressed = leftIsPressed = rightIsPressed = false;
		
		gp = new GamePainter();
		gp.setPreferredSize( new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT) );
				
		//	Set game to initial position
		setPosition( 100, 55 );// Initializes paddles and position variables
		newGame();// initializes scores and blue/red position offset variables
		/****					End initialization				*****/
		
		//	Key Listener
		addKeyListener( new KeyAdapter(){
		
			@Override
			public void keyPressed(KeyEvent e){
				switch(e.getKeyCode()){
										
					case KeyEvent.VK_A:						
						dIsPressed = false;
						aIsPressed = true;
						break;
					case KeyEvent.VK_D:
						aIsPressed = false;
						dIsPressed = true;
						break;
					case KeyEvent.VK_LEFT:
						rightIsPressed = false;
						leftIsPressed = true;
						break;
					case KeyEvent.VK_RIGHT:
						leftIsPressed = false;
						rightIsPressed = true;
						break;
					default:
						break;						
				}//end switch
				
				repaint();
			}//end keyPressed method
		
			@Override
			public void keyReleased(KeyEvent e){
				switch(e.getKeyCode()){
				
					case KeyEvent.VK_A:
						aIsPressed = false;
						break;
					case KeyEvent.VK_D:
						dIsPressed = false;
						break;
					case KeyEvent.VK_LEFT:
						leftIsPressed = false;
						break;
					case KeyEvent.VK_RIGHT:
						rightIsPressed = false;
						break;
					default:
						break;						
				}//end switch
			}//end keyReleased method
		});//end KeyListener
				
		this.setContentPane(gp);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Pong!");
		this.setFocusable(true);
		this.getContentPane().setBackground(new Color(120, 120, 120));
		this.getFrames()[0].setIconImage(Toolkit.getDefaultToolkit().getImage("resources/images/logo.png"));
		this.pack();
		this.setVisible(true);
		
		//	A new thread for updating the game
		Thread updateThread = new Thread(){
		
			@Override
			public void run(){
			
				while(true){
				
					update();
					repaint();
					
					//	Sleep to give the other thread a chance to run
					try{
						Thread.sleep( UPDATE_INTERVAL );
					}
					catch(InterruptedException ignore){}
				}
			}//end run method
		};//end updateThread
		updateThread.start();
	}//end Pong constructor
	
	//	Updates the current state of the game
	public void update(){
	
		
		//	No need to move the ball until game starts
		if ( !countingDown && !gameOver ){
			//	Check if ball is out of bounds and update score
			if ( ball.getX() < 0 || ball.getX() > CANVAS_WIDTH ||
				ball.getY() < 0 || ball.getY() > CANVAS_HEIGHT ){
				
				score();
			}

			//	Move the ball
			if ( !bouncing ){
				bouncing = true;
				bounce();
			}
			ball.moveBall();
		}//end if not counting down
		
		//	Move the paddles
		if ( aIsPressed ){	
		
			if ( !mode360 ){			
				if ( bluePaddle.getAngleStart() < 255 )
					bluePaddle.setAngleStart( bluePaddle.getAngleStart() + 5 );
			}
			else{
				bluePaddle.setAngleStart( bluePaddle.getAngleStart() + 5 );
			}
		}//end aIsPressed
		
		else if ( dIsPressed ){
		
			if ( !mode360 ){
				if ( bluePaddle.getAngleStart() > BLUE_ANGLE_START )
					bluePaddle.setAngleStart( bluePaddle.getAngleStart() - 5 );
			}
			else{
				bluePaddle.setAngleStart( bluePaddle.getAngleStart() - 5 );
			}
		}//end dIsPressed
		
		if ( leftIsPressed ){
		
			if ( !mode360 ){
				if ( redPaddle.getAngleStart() < 75 )
					redPaddle.setAngleStart( redPaddle.getAngleStart() + 5 );
			}
			else{
				redPaddle.setAngleStart( redPaddle.getAngleStart() + 5 );
			}
		}//end leftIsPressed
		
		else if ( rightIsPressed ){
		
			if ( !mode360 ){
				if ( redPaddle.getAngleStart() > RED_ANGLE_START )
					redPaddle.setAngleStart( redPaddle.getAngleStart() - 5 );
			}
			else{
				redPaddle.setAngleStart( redPaddle.getAngleStart() - 5);
			}
		}//end rightIsPressed
		
		//	Get the focus back
		requestFocusInWindow();
		
	}//end update() method
	
	//	Reverse direction of ball if it hit paddle
	public void bounce(){
		
		boolean changeAngle = false;
		
		if( bluePaddle.intersects( ball.getX(), ball.getY(), ball.getDiameter(), ball.getDiameter() ) ){
		
			lastHit = 1;
			changeAngle = true;
		}
		else if( redPaddle.intersects( ball.getX(), ball.getY(), ball.getDiameter(), ball.getDiameter() ) ){
			
			lastHit = 2;
			changeAngle = true;
		}
		
		if ( changeAngle ){
		
			//	Play the bounce sound
			try{
				bounceStream = new AudioStream(new FileInputStream("resources/sounds/bounce.wav"));
				AudioPlayer.player.start(bounceStream);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			//	Right side of boundary
			if ( ball.getX() > center_x ) {
			
				//	Quadrant 4
				if ( ball.getY() > center_y ){
					
					//	Sub Q 13
					if ( ball.getY() - center_y > Math.sin(3 * Math.PI / 8) ){
						
						angle = (double) ( randNum.nextInt(90) + 90 );
						quadrant = 13;
					}
					//	Sub Q 14
					else if ( ball.getY() - center_y > Math.sin(Math.PI / 4) ){
						
						angle = (double) ( randNum.nextInt(300 - 200 + 1) + 200 );
						quadrant = 14;
					}
					//	Sub Q 15
					else if ( ball.getY() - center_y > Math.sin(Math.PI / 8) ){
					
						angle = (double) ( randNum.nextInt(265 - 180 + 1) + 180 );
						quadrant = 15;
					}					
					//	Sub Q 16
					else{
					
						angle = (double) ( randNum.nextInt(220 - 140 + 1) + 140 );
						quadrant = 16;
					}
					
				}//end Quadrant 4
				
				//	Quadrant 1
				else{
								
					//	Sub Q 4
					if ( ball.getY() - center_y < - Math.sin(3 * Math.PI / 8) ){
						
						angle = (double) ( randNum.nextInt(130 - 50 + 1) + 50 );
						quadrant = 4;						
					}
					//	Sub Q 3
					else if ( ball.getY() - center_y < - Math.sin(Math.PI / 4) ){
					
						angle = (double) ( randNum.nextInt(175 - 85 + 1) + 85 );
						quadrant = 3;
					}
					//	Sub Q 2
					else if ( ball.getY() - center_y < - Math.sin(Math.PI / 8) ){
						
						angle = (double) ( randNum.nextInt(185 - 105 + 1) + 105 );
						quadrant = 2;
					}
					//	Sub Q 1
					else{
						
						angle = (double) ( randNum.nextInt(220 - 140 + 1) + 140 );
						quadrant = 1;
					}
				}//end Quadrant 1
				
			}//end right side of boundary
			
			//	Left side of boundary
			else{
			
				//	Quadrant 3
				if ( ball.getY() > center_y ){				
					
					//	Sub Q 12
					if ( ball.getY() - center_y > Math.sin(3 * Math.PI / 8) ){
						
						angle = (double) ( randNum.nextInt(330 - 230 + 1) + 230 );
						quadrant = 12;
					}
					//	Sub Q 11
					else if ( ball.getY() - center_y > Math.sin(Math.PI / 4) ){
						
						angle = (double) ( randNum.nextInt(350 - 270 + 1) + 270 );
						quadrant = 11;
					}
					//	Sub Q 10
					else if ( ball.getY() - center_y > Math.sin(Math.PI / 8) ){
					
						angle = (double) ( randNum.nextInt(90) - 85 );
						quadrant = 10;
					}
					
					//	Sub Q 9
					else{
					
						angle = (double) ( randNum.nextInt(80) - 40 );
						quadrant = 9;
					}
				}//end Quadrant 3
				
				//	Quadrant 2
				else{
					
					//	Sub Q 5
					if ( ball.getY() - center_y < - Math.sin(3 * Math.PI / 8) ){
						
						angle = (double) ( randNum.nextInt(130 - 50 + 1) + 50 );
						quadrant = 5;
					}
					//	Sub Q 6
					else if ( ball.getY() - center_y < - Math.sin(Math.PI / 4) ){
					
						angle = (double) ( randNum.nextInt(95 - 5 + 1) + 5 );
						quadrant = 6;
					}
					//	Sub Q 7
					else if ( ball.getY() - center_y < - Math.sin(Math.PI / 8) ){
						
						angle = (double) ( randNum.nextInt(80) );
						quadrant = 7;
					}
					//	Sub Q 8
					else{
						
						angle = (double) ( randNum.nextInt(80) - 40 );
						quadrant = 8;
					}		
				}//end Quadrant 2
				
			}//end left side of boundary
			
			ball.setDx( (int) (speed * Math.cos(Math.toRadians(angle))) );
			ball.setDy( (int) (speed * Math.sin(Math.toRadians(angle))) );
		}//end if (changeAngle)
		
		bouncing = false;		
	}//end bounce method
	
	//	Updates the score and resets the ball
	public static void score(){
		
		//	Play score sound
		try{
			scoreStream = new AudioStream(new FileInputStream("resources/sounds/score.wav"));
			AudioPlayer.player.start(scoreStream);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		//	Blue scored
		if ( lastHit == 1 ){
			++blueScore;
			if ( blueScore == 10 ) blueOffset = 11;
			else if ( blueScore == 100 ) blueOffset = 22;
		}//end blue scored
		
		//	Red scored
		else if ( lastHit == 2 ){
			++redScore;
			if ( redScore == 10 ) redOffset = 11;
			else if ( redScore == 100 ) redOffset = 22;
		}//end red scored
		
		//	Check if the game is over!
		if ( redScore == playTo || blueScore == playTo ){
			gameOver();
		}
		
		centerBall();
	}//end score method
	
	//	Sets the x/y coordinates of the playing circle
	private static void setPosition(int x, int y){
	
		paddleX = x + 1;
		paddleY = y + 1;
		
		bluePaddle = new Paddle(paddleX, paddleY, BLUE_ANGLE_START, Color.BLUE);
		redPaddle = new Paddle(paddleX, paddleY, RED_ANGLE_START, Color.RED);
		
		xPosition = x;
		yPosition = y;
				
		center_x = xPosition + DIAMETER / 2;
		center_y = yPosition + DIAMETER / 2;
		
		centerBall();
	}//end setPosition method
	
	//	Resets the ball to the center of the game
	private static void centerBall(){
	
		ball.setX( center_x - (ball.getDiameter() / 2) );
		ball.setY( center_y - (ball.getDiameter() / 2) );
	}//end centerBall method
	
	//	A canvas to draw the game on
	public static class GamePainter extends JPanel{
		
		private static BufferedImage image;
		private static Image backgroundImage;
		private static JPanel menuPanel;
		private static JPanel optionsPanel;
		private static JLabel optionsLabel;
		private static JCheckBox mode360Checkbox;
		private static JCheckBox showBorderCheckbox;
		private static JLabel themeLabel;
		private static JRadioButton wofThemeRadioButton;
		private static JRadioButton berlinThemeRadioButton;
		private static JRadioButton planetsThemeRadioButton;
		private static JRadioButton whirlpoolThemeRadioButton;
		private static JRadioButton spaceThemeRadioButton;
		private static ButtonGroup themeButtonGroup;
		private static JLabel playToLabel;
		private static JRadioButton playTo10RadioButton, playTo20RadioButton, playTo30RadioButton;
		private static ButtonGroup playToButtonGroup;
		private static JButton newGameButton;
		private static JButton resetButton;
		private static boolean showBorder;
		private static JPanel menuButtonPanel;
		private static JButton menuButton;
		private static JLabel musicLabel;
		private static JCheckBox musicCheckbox;
		
		//	GamePainter Constructor
		public GamePainter(){
		
			//	Load the background image
			try{
				image = ImageIO.read( new File("resources/images/space.jpg") );
				backgroundImage = image.getScaledInstance(CANVAS_WIDTH, CANVAS_HEIGHT, Image.SCALE_SMOOTH);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			//	Initialize components
			menuPanel = new JPanel(new BorderLayout());
			optionsPanel = new JPanel(new GridLayout(17, 1));
			optionsPanel.setVisible(false);
			optionsLabel = new JLabel("Options:");
			mode360Checkbox = new JCheckBox("360 Mode");
			mode360Checkbox.setBackground(BACKGROUND_COLOR);
			mode360Checkbox.setSelected(true);
			showBorderCheckbox = new JCheckBox("Show Border");
			showBorderCheckbox.setBackground(BACKGROUND_COLOR);
			showBorderCheckbox.setSelected(true);
			
			themeLabel = new JLabel("Themes:");
			wofThemeRadioButton = new JRadioButton("W.O.F.");
			berlinThemeRadioButton = new JRadioButton("Berlin");
			planetsThemeRadioButton = new JRadioButton("Planets");
			whirlpoolThemeRadioButton = new JRadioButton("Whirlpool");
			spaceThemeRadioButton = new JRadioButton("Space");
			spaceThemeRadioButton.setSelected(true);
			
			wofThemeRadioButton.setBackground(BACKGROUND_COLOR);
			berlinThemeRadioButton.setBackground(BACKGROUND_COLOR);
			planetsThemeRadioButton.setBackground(BACKGROUND_COLOR);
			whirlpoolThemeRadioButton.setBackground(BACKGROUND_COLOR);
			spaceThemeRadioButton.setBackground(BACKGROUND_COLOR);
			
			
			wofThemeRadioButton.addActionListener(new ThemeListener());
			berlinThemeRadioButton.addActionListener(new ThemeListener());
			planetsThemeRadioButton.addActionListener(new ThemeListener());
			whirlpoolThemeRadioButton.addActionListener(new ThemeListener());
			spaceThemeRadioButton.addActionListener(new ThemeListener());
			
			themeButtonGroup = new ButtonGroup();
			themeButtonGroup.add(wofThemeRadioButton);
			themeButtonGroup.add(berlinThemeRadioButton);
			themeButtonGroup.add(planetsThemeRadioButton);
			themeButtonGroup.add(whirlpoolThemeRadioButton);
			themeButtonGroup.add(spaceThemeRadioButton);
			
			playToLabel = new JLabel("Play to:");
			playTo10RadioButton = new JRadioButton("10");
			playTo20RadioButton = new JRadioButton("20");
			playTo30RadioButton = new JRadioButton("30");
			
			playTo10RadioButton.setBackground(BACKGROUND_COLOR);
			playTo20RadioButton.setBackground(BACKGROUND_COLOR);
			playTo30RadioButton.setBackground(BACKGROUND_COLOR);
			
			playTo10RadioButton.addActionListener(new PlayToListener());
			playTo20RadioButton.addActionListener(new PlayToListener());
			playTo20RadioButton.setSelected(true);
			playTo30RadioButton.addActionListener(new PlayToListener());
			
			playToButtonGroup = new ButtonGroup();
			playToButtonGroup.add(playTo10RadioButton);
			playToButtonGroup.add(playTo20RadioButton);
			playToButtonGroup.add(playTo30RadioButton);
			
			mode360Checkbox.addItemListener(new CheckboxListener());
			showBorderCheckbox.addItemListener(new CheckboxListener());
			
			newGameButton = new JButton("New Game");
			newGameButton.setBackground(BACKGROUND_COLOR);
			newGameButton.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseReleased(MouseEvent e){
					if ( e.getSource() == newGameButton ) newGame();
				}
			});
			resetButton = new JButton("Reset");
			resetButton.setBackground(BACKGROUND_COLOR);
			resetButton.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseReleased(MouseEvent e){				
					if ( e.getSource() == resetButton ) reset();
				}
			});
			
			showBorder = true;
			
			musicLabel = new JLabel("Music");
			musicCheckbox = new JCheckBox("On / Off");
			musicCheckbox.setBackground(BACKGROUND_COLOR);
			musicCheckbox.addItemListener(new CheckboxListener());
			
			//	Set up the options panel
			optionsPanel.add(optionsLabel);
			optionsPanel.add(mode360Checkbox);
			optionsPanel.add(showBorderCheckbox);
			optionsPanel.add(themeLabel);
			optionsPanel.add(wofThemeRadioButton);
			optionsPanel.add(berlinThemeRadioButton);
			optionsPanel.add(planetsThemeRadioButton);
			optionsPanel.add(whirlpoolThemeRadioButton);
			optionsPanel.add(spaceThemeRadioButton);
			optionsPanel.add(playToLabel);
			optionsPanel.add(playTo10RadioButton);
			optionsPanel.add(playTo20RadioButton);
			optionsPanel.add(playTo30RadioButton);
			optionsPanel.add(musicLabel);
			optionsPanel.add(musicCheckbox);
			optionsPanel.add(newGameButton);
			optionsPanel.add(resetButton);
			
			//	Menu button setup
			menuButtonPanel = new JPanel(new GridLayout(1, 1));
			menuButton = new JButton("Show Menu");
			menuButton.setBackground(BACKGROUND_COLOR);
			menuButton.setForeground(Color.WHITE);
			menuButton.addActionListener(new MenuButtonListener());
			menuButtonPanel.add(menuButton);
			
			//	Set up the layout
			menuPanel.add(menuButtonPanel, BorderLayout.NORTH);
			menuPanel.add(optionsPanel, BorderLayout.SOUTH);
			menuPanel.setBackground(BACKGROUND_COLOR);
			optionsPanel.setBackground(BACKGROUND_COLOR);
			menuButtonPanel.setBackground(BACKGROUND_COLOR);
			
			this.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 200));
			this.add(menuPanel);
			
		}//end GamePainter constructor
		
		@Override
		protected void paintComponent(Graphics g) {
		
			super.paintComponent(g);			
			Graphics2D g2d = (Graphics2D) g;
			
			//	Background
            g2d.drawImage(backgroundImage, 0, 0, null);
			
			//	The boundary
			if ( showBorder ){
				g2d.setColor(Color.WHITE);
				g2d.drawOval(xPosition, yPosition, DIAMETER, DIAMETER);
			}//end show border
			
			//	Paddle 1
			g2d.setStroke( new BasicStroke(7.0f) );
			
			g2d.setPaint( new GradientPaint((float) bluePaddle.getX(), (float) redPaddle.getY(), bluePaddle.getColor(),
				(float) (bluePaddle.getX() + bluePaddle.getWidth()), (float) (bluePaddle.getY() + bluePaddle.getHeight()),
				new Color( 150, 150, 255 )) );
			g2d.draw( bluePaddle );
			
			//	Paddle 2
			g2d.setColor( redPaddle.getColor() );
			g2d.draw( redPaddle );
			
			g2d.setStroke(new BasicStroke(2.0f));
			
			//	The ball
			if ( !countingDown && !gameOver){
				g2d.setColor(ball.getColor());
				g2d.fillOval(ball.getX(), ball.getY(), ball.getDiameter(), ball.getDiameter());
			}
			
			//	Scores
			g2d.setColor(new Color(0, 0, 255, 90));
			g2d.fillOval(5, 5, 100, 100);
			g2d.setColor(new Color(255, 0, 0, 90));
			g2d.fillOval(CANVAS_WIDTH - 105, 5, 100, 100);
			
			g2d.setColor(Color.WHITE);
			g2d.setFont(g2d.getFont().deriveFont(40.0f));
						
			g2d.drawString( Integer.toString(blueScore), 43 - blueOffset, 68 );
			g2d.drawString( Integer.toString(redScore), CANVAS_WIDTH - 65 - redOffset, 68 );
			
			//	The count down
			if ( countingDown ){				
				g2d.setColor(Color.YELLOW);
				
				if ( countDown > 0 ){				
					g2d.drawString( Integer.toString(countDown),
						center_x - Integer.toString(countDown).length() * 11,
						center_y - 5);
				}
				else{
					g2d.drawString( "GO!", center_x - 33, center_y - 5 );
				}
			}//end count down
			
			if( gameOver ){
				
				if(blueScore == playTo){
					victoryColor = Color.BLUE;
				}
				else{
					victoryColor = Color.RED;
				}
				g2d.setColor(BACKGROUND_COLOR2);
				g2d.fillRoundRect(140, 230, 500, 200, 10, 10);
				
				//bottom string
				g2d.setColor(Color.BLACK);
				g2d.setFont(g2d.getFont().deriveFont(90.0f));
				g2d.drawString("Game Over!", 148, 319);
				if(victoryColor == Color.BLUE){
					g2d.drawString("Blue Wins!", 171, 400);
				}
				else{
					g2d.drawString("Red Wins!", 171, 400);
				} 
				
				//top string
				g2d.setColor(victoryColor);
				g2d.setFont(g2d.getFont().deriveFont(89.0f));
				g2d.drawString("Game Over!", 148, 315);
				if(victoryColor == Color.BLUE){
					g2d.drawString("Blue Wins!", 169, 397);
				}
				else{
					g2d.drawString("Red Wins!", 169, 397);
				} 
			}
		}//end paintComponent

		//	Item listener for checkboxes
		private class CheckboxListener implements ItemListener{

			@Override
			public void itemStateChanged(ItemEvent ie){

				//	Play sound
				if ( ((JCheckBox) ie.getSource()).isSelected() ){
					try{
						checkStream = new AudioStream(new FileInputStream("resources/sounds/check.wav"));
						AudioPlayer.player.start(checkStream);
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
				else{
					try{
						uncheckStream = new AudioStream(new FileInputStream("resources/sounds/uncheck.wav"));
						AudioPlayer.player.start(uncheckStream);
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
				
				//	360 mode
				if ( ie.getSource() == mode360Checkbox ){

					if (mode360Checkbox.isSelected()){

						mode360 = true;
					}
					else{

						mode360 = false;
						
						//	Reset paddles to default positions
						bluePaddle.setAngleStart(bluePaddle.getAngleStart() % 360);
						redPaddle.setAngleStart(redPaddle.getAngleStart() % 360);
						
						if ( bluePaddle.getAngleStart() > 255 ||
							bluePaddle.getAngleStart() < BLUE_ANGLE_START ){
							
							bluePaddle.setAngleStart(BLUE_ANGLE_START);
						}
						
						if ( redPaddle.getAngleStart() > 75 ||
							redPaddle.getAngleStart() < RED_ANGLE_START ){
							
							redPaddle.setAngleStart(RED_ANGLE_START);
						}						
					}
				}//end 360 mode
				
				//	Show border
				else if ( ie.getSource() == showBorderCheckbox ){
				
					if ( showBorderCheckbox.isSelected() ) showBorder = true;					
					else showBorder = false;

					showBorderCheckbox.setVisible(true);
				}//end show border
				
				//	Music
				else if ( ie.getSource() == musicCheckbox ){
				
					if ( musicCheckbox.isSelected() ){
						try{
							backgroundStream = new AudioStream(new FileInputStream("resources/sounds/background.wav"));
							AudioPlayer.player.start(backgroundStream);
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					else{
						try{
							AudioPlayer.player.stop(backgroundStream);
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
				}//end music
				
				repaint();
			}//end itemStateChanged method
		}//end CheckboxListener
		
		//	Action listener for theme radio buttons
		private class ThemeListener implements ActionListener{
		
			@Override
			public void actionPerformed(ActionEvent ae){
			
				try{
					//AudioPlayer.player.stop(radioSelectStream);
					radioSelectStream = new AudioStream(new FileInputStream("resources/sounds/radioSelect.wav"));
					AudioPlayer.player.start(radioSelectStream);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
				if ( ae.getSource() == wofThemeRadioButton ){
					setImage("resources/images/wheelOfFortune.png");
					setPosition(126, 51);
				}
				else if ( ae.getSource() == berlinThemeRadioButton ){
					setImage("resources/images/berlin.jpg");
					setPosition(126, 27);
				}
				else if ( ae.getSource() == planetsThemeRadioButton ){
					setImage("resources/images/planets.jpg");
					setPosition(130, 60);
				}
				else if ( ae.getSource() == whirlpoolThemeRadioButton ){
					setImage("resources/images/whirlpool2.jpg");
					setPosition(100, 55);
				}
				else if ( ae.getSource() == spaceThemeRadioButton ){
					setImage("resources/images/space.jpg");
					setPosition(100, 55);
				}
			}//end actionPerformed method
		}//end ThemeListener class
		
		//	Action listener for play to options
		private class PlayToListener implements ActionListener{
		
			@Override
			public void actionPerformed(ActionEvent ae){
			
				if ( ae.getSource() == playTo10RadioButton ){
					playTo = 10;
				}
				else if ( ae.getSource() == playTo20RadioButton ){
					playTo = 20;
				}
				else if ( ae.getSource() == playTo30RadioButton ){
					playTo = 30;
				}
				
			}//end actionPerformed method
		}
		
		//	Action listener for menu button
		private class MenuButtonListener implements ActionListener{
		
			public void actionPerformed(ActionEvent ae){
			
				if ( menuButton.getText().equals("Show Menu") ){
					menuButton.setText("Hide Menu");
					optionsPanel.setVisible(true);
				}
				else{
					menuButton.setText("Show Menu");
					optionsPanel.setVisible(false);
				}
			}
		}//end menu button listener
		
		//	Opens fileName and sets background to that image
		public static void setImage(String fileName){
		
			//	Load the background image
			try{
				image = ImageIO.read( new File(fileName) );
				backgroundImage = image.getScaledInstance(CANVAS_WIDTH, CANVAS_HEIGHT, Image.SCALE_SMOOTH);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}//end setImage method
		
	}//end GamePainter class
	
	//	Starts a new game keeping other settings as they are
	public static void newGame(){
	
		blueScore = redScore = blueOffset = redOffset = 0;
		centerBall();
		bluePaddle.setAngleStart(BLUE_ANGLE_START);
		redPaddle.setAngleStart(RED_ANGLE_START);
		countDown();
		gameOver = false;
	}//end newGame method
	
	//	Resets all settings to defaults
	public static void reset(){
	
		newGame();
		GamePainter.mode360Checkbox.setSelected(true);
		//GamePainter.spaceThemeRadioButton.setSelected(true);
		GamePainter.spaceThemeRadioButton.doClick();
		GamePainter.showBorderCheckbox.setSelected(true);
	}//end reset method
	
	//	Game over
	public static void gameOver(){
	
		gameOver = true;
		
		try{
			gameOverStream = new AudioStream(new FileInputStream("resources/sounds/gameOver.wav"));
			AudioPlayer.player.start(gameOverStream);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}//end gameOver method
	
	//	Counts down to a new game
	public static void countDown(){
	
		countDown = COUNT_DOWN_START;
		countingDown = true;
		countDownTimer.restart();
		try{
			AudioPlayer.player.stop(countDownStream);
			countDownStream = new AudioStream(new FileInputStream("resources/sounds/countDown.wav"));
			AudioPlayer.player.start(countDownStream);
		}
		catch(Exception e){e.printStackTrace();}
	}//end countDown method
	
	//	Listener for the count down timer
	private static class CountDownListener implements ActionListener{
		
		@Override
		public void actionPerformed(ActionEvent e){
		
			//	Decrement the count down
			--countDown;
			
			//	Stop the timer after count down reaches 0
			if ( countDown < 0 ){
				countDownTimer.stop();
				countingDown = false;
			}
		}//end actionPerformed method
	}//end CountDownListener
		
	//	Main method
	public static void main(String[] args){
		
		//	Event dispatching for thread safety
		SwingUtilities.invokeLater( new Runnable(){
			@Override
			public void run(){	
				//	Constructor does everything
				new Pong();
			}
		} );
	}//end main
	
}//end Pong class