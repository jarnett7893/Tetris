/*
 * TCSS 305 � Winter 2015
 * Assignment 6 - Tetris
 */

package gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import model.Board;
import model.Board.BoardData;
import model.Board.CompletedLines;
import model.Point;

import sound.SoundEffects;
import sound.SoundPlayer;
import zelda.Guard;
import zelda.Link;
import zelda.Skelly;


/**
 * The panel the displays the game state of Tetris.
 * 
 * @author Justin Arnett (jarnett@uw.edu)
 * @version 08 March 2015
 */
@SuppressWarnings("serial")
public class GamePanel extends JPanel implements Observer {

    /** Font size of game over message. */
    private static final int FONT_SIZE = 6;
    /** The preferred size of the game panel. */
    private static final Dimension PREFERRED_DIMENSION = new Dimension(400, 800);
    /** The amount of lines removed from the top of the Tetris board. */
    private static final int REMOVED_LINES = 4;
    /** The amount of padding for the inner square in each tetris block. */
    private static final int PADDING = 0;
    private static final int THICKNESS = 3;
    /** The default font for messages. */
    private static final String DEFAULT_FONT = "Verdana";
    
    /** The default left key binding. */
    private static final int DEFAULT_LEFT_BINDING = KeyEvent.VK_A;
    /** The default right key binding. */
    private static final int DEFAULT_RIGHT_BINDING = KeyEvent.VK_D;
    /** The default down key binding. */
    private static final int DEFAULT_DOWN_BINDING = KeyEvent.VK_S;
    /** The default clockwise key binding. */
    private static final int DEFAULT_CW_BINDING = KeyEvent.VK_E;
    /** The default counterclockwise key binding. */
    private static final int DEFAULT_CCW_BINDING = KeyEvent.VK_Q;
    /** The default drop key binding. */
    private static final int DEFAULT_DROP_BINDING = KeyEvent.VK_W;
    /** The default pause key binding. */
    private static final int DEFAULT_PAUSE_BINDING = KeyEvent.VK_P;
    
    /** Used for calculating resize. */
    private static final int TEN = 10;
    /** Used for calculating resize. */
    private static final int THREE = 3;
    /** Used for calculating resize. */
    private static final int FOUR = 4;
    /** Used for calculating resize. */
    private static final int FIVE = 5;
    /** Used for calculating resize. */
    private static final int EIGHT = 8;
    
    
    /** The size of the rendered block in pixels. */
    private int myBlockSize;
    
    /** The origin point of the rendered Tetris board. */
    private Point myOrigin;
    
    /** The Tetris game. */
    private final Board myTetris;
    
    /** The tetris data for block locations. */
    private List<Color[]> myGameData;
    
    /** 
     * Auxiliary storage for board data.  Used to quickly update game
     * after an animation.
     */
    private List<Color[]> myTempData;
    
    /** The timer used to run the tetris game. */
    private final Timer myTimer;
    
    /** Indicates that a game is over. */
    private boolean myGameIsOver;
    
    /** Indicates that the game is paused. */
    private boolean myGameIsPaused;
    
    /** The map of actions and their key code. */
    private Map<KeyAction, Integer> myKeys;
    
    /** The key bindings for the game. */
    private KeyBindings myKeyBindings;
    
    /** The pause action for the key binding. */
    private KeyAction myPauseKeyAction;

    /** The sound player. */
    private final SoundPlayer mySoundPlayer;
    
    /** Current guard image. */
    private Guard myGuard;
    
    /** The timer for the guard. */
    private Timer myGuardTimer;
    
    /** Completed lines. */
    private CompletedLines myCompletedLines;
    
    /** Current link image for animation. */
    private Link myLink;
    
    /** The timer for link animation. */
    private Timer myLinkTimer;
    
    /** Checks if link should be rendered. */
    private boolean myLinkIsAnimated;
    
    /** Is Zelda theme activated? */
    private boolean myZeldaTheme;
    
    /** The amount of guards that are killed at the given moment. */
    private int myDeadGuards;
    
    /** Current skeleton image. */
    private Skelly mySkelly;
    
    
    
    /**
     * Builds the display panel for a game of tetris.
     * 
     * @param theTetris The tetris game.
     * @param theTimer The tetris game timer.
     * @param thePlayer The player for playing sound.
     */
    public GamePanel(final Board theTetris, final Timer theTimer,
                     final SoundPlayer thePlayer) {
        super(true);  // Sets JPanel to enable double buffering.
        myTetris = theTetris;
        initialize();
        setBlockSize();
        myTimer = theTimer;
        mySoundPlayer = thePlayer;
        startGameGUI();
    }
    
    
    /**
     * Helper method for constructor to initialize fields.
     */
    private void initialize() {
        myGuard = Guard.GUARD_1;
        myLink = Link.LINK_1;
        mySkelly = Skelly.SKELLY_1;
        myLinkTimer = createLinkTimer();
        myGuardTimer = createGuardTimer();
        myLinkIsAnimated = false;
        myZeldaTheme = false;
        myGameIsOver = false;
        myGameIsPaused = false;
        myKeys = new HashMap<KeyAction, Integer>();
        myDeadGuards = myTetris.getWidth();
    }
    
    
    
    /**
     * Creates a timer for Tetris.
     * 
     * @return The timer for Tetris.
     */
    private Timer createLinkTimer() {
        final Timer time = new Timer(120, new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                setKills();
                repaint();
                myLink = myLink.advance();
                if (myLink.equals(Link.LINK_1)) {
                    myLinkIsAnimated = false;
                    myLinkTimer.stop();
                    myTimer.start();
                    myGameData = myTempData;
                }
            }
        });
        return time;
    }
    
    
    
    /**
     * Sets the amount of guards current dead for the
     * current step in animation of Link.
     */
    private void setKills() {
        switch(myLink) {
            case LINK_1:
                myDeadGuards = myTetris.getWidth() * FOUR / FIVE;
                break;
            case LINK_2:
                myDeadGuards = myTetris.getWidth() * THREE / FIVE;
                break;
            case LINK_3:
                myDeadGuards = myTetris.getWidth() * 2 / FIVE;
                break;
            case LINK_4:
                myDeadGuards = myTetris.getWidth() * 1 / FIVE;
                break;
            case LINK_5:
                myDeadGuards = myTetris.getWidth() * 0 / FIVE;
                break;
            case LINK_6:
                myDeadGuards = myTetris.getWidth() * FIVE / FIVE;
                break;
            default:
                break;
        }
    }
    
    
    
    /**
     * Creates a timer for Tetris.
     * 
     * @return The timer for Tetris.
     */
    private Timer createGuardTimer() {
        final Timer time = new Timer(250, new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                myGuard = myGuard.advance();
                mySkelly = mySkelly.advance();
                repaint();
            }
        });
        return time;
    }
    
    
    /**
     * Calculates the rendered block size depending on the height and
     * width of the game panel.
     */
    private void setBlockSize() {
        final int blockHeightCheck = this.getHeight() / myTetris.getHeight();
        final int blockWidthCheck = this.getWidth() / myTetris.getWidth();
        
        if (blockHeightCheck < blockWidthCheck) {
            myBlockSize = blockHeightCheck;
        } else {
            myBlockSize = blockWidthCheck;
        }
    }
    
    
    /**
     * Runs the link animation for swinging his sword.
     */
    private void runLinkAnimation() {
        myLinkIsAnimated = true;
        myTimer.stop();
        myLinkTimer.start();
    }
    
    
    
    /**
     * Initializes the panel for rendering the tetris game.
     */
    private void startGameGUI() {
        //this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setPreferredSize(PREFERRED_DIMENSION);
        this.setBackground(Color.LIGHT_GRAY);
        
        registerKeyActions();
        myKeyBindings = new KeyBindings(myKeys, this);
        
        
        myKeyBindings.enableAllKeys();
        myGuardTimer.start();
    }
    
    
    /**
     * Updates the global theme settings of the game panel.
     * 
     * @param theThemeStatus The status of the Zelda theme.
     */
    public void updateTheme(final boolean theThemeStatus) {
        myZeldaTheme = theThemeStatus;
        repaint();
    }
    
    
    
    /**
     * Stops the current game and signals a game over.
     */
    public void gameOver() {  
        myKeyBindings.disableAllKeys();
        myGameIsOver = true;
        unpause();
        repaint();
    }
    
    
    /**
     * Starts a new Tetris game.
     */
    public void newGame() {
        myGameIsOver = false;
        unpause();
        myKeyBindings.disableAllKeys();
        myTetris.clear();
        myKeyBindings.enableAllKeys();
        repaint();
    }
    
    
    /**
     * Toggles the state of pause for Tetris.
     */
    public void togglePause() {
        if (myGameIsPaused) {
            unpause();
        } else {
            pause();
        }
    }
    
    
    /**
     * Pauses the current game by stopping the timer
     * and disabling key bindings.
     */
    public void pause() {
        if (!myGameIsOver) {
            myKeyBindings.disableAllKeys();
            myKeyBindings.enableKey(myPauseKeyAction);
            myTimer.stop();
            myGameIsPaused = true;
            repaint();
        }
    }
    
    
    /**
     * Unpauses the current game by starting the timer
     * and enabling key binding if only the game is not
     * over.
     */
    public void unpause() {
        if (!myGameIsOver) {
            myKeyBindings.enableAllKeys();
            myTimer.start();
        }
        myGameIsPaused = false;
        repaint();
    }
    
    
    /**
     * Gets the map of key bindings.
     * 
     * @return The map of key bindings.
     */
    public Map<KeyAction, Integer> getKeyBindings() {
        return myKeyBindings.getKeys();
    }
    
    
    /**
     * Updates the key bindings of the Tetris game.
     * 
     * @param theKeys The new key bindings.
     */
    public void setKeyBindings(final Map<KeyAction, Integer> theKeys) {
        myKeyBindings.disableAllKeys();
        myKeyBindings.setKeys(theKeys);
        if (myGameIsOver || myGameIsPaused) {
            myKeyBindings.enableKey(myPauseKeyAction);
        } else {
            myKeyBindings.enableAllKeys();
        }
    }
    
    
    /**
     * Renders the tetris game in a resizable panel.
     * 
     * @param theGraphics The Graphics2D used for rendering.
     */
    @Override
    public void paintComponent(final Graphics theGraphics) {
        super.paintComponent(theGraphics);
        final Graphics2D graphic = (Graphics2D) theGraphics;
        graphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                 RenderingHints.VALUE_ANTIALIAS_ON);
        setBlockSize();
        
        // Centers the game panel for resizing.
        final Point center = new Point(myTetris.getWidth() * myBlockSize / 2,
                                       myTetris.getHeight() * myBlockSize / 2);
        final Point panelCenter = new Point(this.getWidth() / 2, this.getHeight() / 2);
        myOrigin = new Point(panelCenter.x() - center.x(), panelCenter.y() - center.y());
        
        // Draws the game backdrop.
        drawBackground(graphic, center);
        
        // Draws the board through a 2d array for the coordinates.
        for (int y = 0; y < myGameData.size() - REMOVED_LINES; y++) {
            for (int x = 0; x < myGameData.get(y).length; x++) {
                drawBlock(x, y, graphic);
            }
        }
        
        if (myLinkIsAnimated && !myCompletedLines.getCompletedLines().isEmpty()) {
            drawSkellies(graphic);
            
        }
        
        if (myLinkIsAnimated) {
            graphic.drawImage(myLink.getImage(),
                       (myTetris.getWidth() * myBlockSize + myOrigin.x()) / 2 - myBlockSize,
                       (myTetris.getHeight() - myCompletedLines.getCompletedLines().get(0) - 1)
                                 * myBlockSize + myOrigin.y(),
                       myBlockSize * THREE,
                       myBlockSize * THREE,
                       null);
        }
        if (myGameIsPaused) {
            drawPause(graphic, center);
        }
        if (myGameIsOver) {
            drawGameOver(graphic, center);
        }
    }
    
    
    /**
     * Draws the game over pop up.
     * 
     * @param theGraphics The graphics.
     * @param theCenter The center of the game.
     */
    private void drawGameOver(final Graphics2D theGraphics, final Point theCenter) {
        theGraphics.setPaint(Color.DARK_GRAY);
        theGraphics.fillRoundRect(myOrigin.x() + theCenter.x() / FIVE,
                         myOrigin.y() + theCenter.y() / FIVE * 2,
                         theCenter.x() / FONT_SIZE * TEN,
                         theCenter.y() / FONT_SIZE + THREE,
                         FONT_SIZE * 2, FONT_SIZE * 2);
        theGraphics.setPaint(Color.RED);
        theGraphics.drawRoundRect(myOrigin.x() + theCenter.x() / FIVE,
                              myOrigin.y() + theCenter.y() / FIVE * 2,
                              theCenter.x() / FONT_SIZE * TEN,
                              theCenter.y() / FONT_SIZE + THREE,
                              FONT_SIZE * 2, FONT_SIZE * 2);
        
        theGraphics.setFont(new Font(DEFAULT_FONT, 1, theCenter.x() / FONT_SIZE));
        theGraphics.drawString("- Game Over -",
                           myOrigin.x() + theCenter.x() / THREE + 5,
                           myOrigin.y() + theCenter.y() / 2 + 5);
    }
    
    
    
    /**
     * Draws the pause pop up.
     * 
     * @param theGraphics The graphics.
     * @param theCenter The center of the game.
     */
    private void drawPause(final Graphics2D theGraphics, final Point theCenter) {
        theGraphics.setPaint(Color.DARK_GRAY);
        theGraphics.fillRoundRect(myOrigin.x() + theCenter.x() / THREE,
                              myOrigin.y() + theCenter.y() / FIVE * 2,
                              theCenter.x() / FONT_SIZE * EIGHT,
                              theCenter.y() / FONT_SIZE + THREE,
                              FONT_SIZE * 2, FONT_SIZE * 2);
        theGraphics.setPaint(Color.YELLOW);
        theGraphics.drawRoundRect(myOrigin.x() + theCenter.x() / THREE,
                              myOrigin.y() + theCenter.y() / FIVE * 2,
                              theCenter.x() / FONT_SIZE * EIGHT,
                              theCenter.y() / FONT_SIZE + THREE,
                              FONT_SIZE * 2, FONT_SIZE * 2);

        theGraphics.setFont(new Font(DEFAULT_FONT, 1, theCenter.x() / FONT_SIZE));
        theGraphics.drawString("- Paused -",
                           myOrigin.x() + theCenter.x() / 2 + 3,
                           myOrigin.y() + theCenter.y() / 2 + 5);
    }
    
    
    
    /**
     * Backdrop of the tetris game area.
     * 
     * @param theGraphics The graphics tool.
     * @param theCenter The center of the game.
     */
    private void drawBackground(final Graphics2D theGraphics, final Point theCenter) {
        if (myZeldaTheme) {
            java.net.URL imgURL = getClass().getResource("/images/backdrop.gif");
            theGraphics.drawImage(new ImageIcon(imgURL).getImage(),
                              myOrigin.x(),
                              myOrigin.y(),
                              theCenter.x() * 2,
                              theCenter.y() * 2, null);
        } else {
            theGraphics.setPaint(Color.BLACK);
            theGraphics.fillRect(0, 0,
                                 this.getWidth(),
                                 this.getHeight());
            java.net.URL imgURL = getClass().getResource("/images/tetris_backdrop.png");
            theGraphics.drawImage(new ImageIcon(imgURL).getImage(),
                              myOrigin.x(),
                              myOrigin.y(),
                              theCenter.x() * 2,
                              theCenter.y() * 2, null);
        }
    }
    
    
    /**
     * Draws the skeletons as link kills guards.
     * 
     * @param theGraphics The renderer.
     */
    private void drawSkellies(final Graphics2D theGraphics) {
        for (final Integer row : myCompletedLines.getCompletedLines()) {
            for (int col = myTetris.getWidth() - 1; col >= myDeadGuards; col--) {
                theGraphics.drawImage(mySkelly.getImage(),
                                col * myBlockSize + myOrigin.x(),
                                (myTetris.getHeight() - row - 1) * myBlockSize + myOrigin.y(),
                                myBlockSize, myBlockSize, null);
            }
        }
    }

    
    
    
    /**
     * Draws a single tetris block.
     * 
     * @param theX The x coordinate of the block.
     * @param theY The y coordinate of the block.
     * @param theGraphics The graphics.
     */
    private void drawBlock(final int theX, final int theY, final Graphics2D theGraphics) {
        theGraphics.setPaint(Color.GRAY);
        if (myGameData.get(theY)[theX] != null) {
            if (!myGameIsPaused) {
                theGraphics.setPaint(myGameData.get(theY)[theX]);
            }
            if (myZeldaTheme) {
                /*
                float opacity = 0.55f;
                Color oldColor = theGraphics.getColor();
                Stroke oldStroke = theGraphics.getStroke();
                theGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                
                theGraphics.fillOval(theX * myBlockSize + myOrigin.x(),
                                     (myTetris.getHeight() - theY - 1) * myBlockSize + myOrigin.y(),
                                      myBlockSize, myBlockSize);
                
                theGraphics.setPaint(new Color(0, 0, 0, 255));
                theGraphics.setStroke(new BasicStroke(THICKNESS));
                theGraphics.drawOval(theX * myBlockSize + myOrigin.x() + PADDING + (THICKNESS/2),
                                     (myTetris.getHeight() - theY - 1) 
                                                       * myBlockSize + myOrigin.y() + PADDING + (THICKNESS/2),
                                     myBlockSize - (PADDING * 2) - THICKNESS,
                                     myBlockSize - (PADDING * 2) - THICKNESS);
                
                theGraphics.setStroke(oldStroke);
                theGraphics.setPaint(oldColor);
                theGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                */
                drawGuards(theX, theY, theGraphics);
            } else {
                theGraphics.fillRect(theX * myBlockSize + myOrigin.x(),
                                (myTetris.getHeight() - theY - 1) * myBlockSize + myOrigin.y(),
                                 myBlockSize, myBlockSize);
                theGraphics.setPaint(new Color(0, 0, 0, 100));
                Stroke oldStroke = theGraphics.getStroke();
                theGraphics.setStroke(new BasicStroke(THICKNESS));
                theGraphics.drawRect(theX * myBlockSize + myOrigin.x() + PADDING + (THICKNESS/2),
                                     (myTetris.getHeight() - theY - 1) 
                                                       * myBlockSize + myOrigin.y() + PADDING + (THICKNESS/2),
                                     myBlockSize - (PADDING * 2) - THICKNESS,
                                     myBlockSize - (PADDING * 2) - THICKNESS);
                theGraphics.setStroke(oldStroke);
            }
        }
    }
    
    
    /**
     * Draws Guards in place of regular blocks.
     * 
     * @param theX The x coordinate on the board.
     * @param theY The y coordinate on the board.
     * @param theGraphics The rendering graphics.
     */
    public void drawGuards(final int theX, final int theY, final Graphics2D theGraphics) {
        Image img = myGuard.getImage();
        if (theGraphics.getColor().getAlpha() < 250) {
            float opacity = 0.5f;
            theGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        }
        
        theGraphics.drawImage(img,
                              theX * myBlockSize + myOrigin.x(),
                              (myTetris.getHeight() - theY - 1) * myBlockSize + myOrigin.y(),
                              myBlockSize,
                              myBlockSize,
                              null);
        theGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    
    @Override
    public void update(final Observable theObj, final Object theArg) {
        if (theArg instanceof CompletedLines) {
            myCompletedLines = (CompletedLines) theArg;
            if (myZeldaTheme) {
                runLinkAnimation();
            }
        } else if (!myLinkIsAnimated && theArg instanceof BoardData) {
            myGameData = ((BoardData) theArg).getBoardData();
        } else if (myLinkIsAnimated && theArg instanceof BoardData) {
            myTempData = ((BoardData) theArg).getBoardData();
        }
        repaint();
        //System.out.println(myTetris);
    }
    
    

    /**
     * Registers abstract actions to the key binding actions.
     */
    private void registerKeyActions() {
        // Left key binding.
        final Action leftAction = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                myTetris.left();
            }
        };
        myKeys.put(new KeyAction(leftAction, "Left"), DEFAULT_LEFT_BINDING);
        
        // Right key binding.
        final Action rightAction = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                myTetris.right();
            }
        };
        myKeys.put(new KeyAction(rightAction, "Right"), DEFAULT_RIGHT_BINDING);
        
        // Down key binding.
        final Action downAction = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                myTetris.down();
            }
        };
        myKeys.put(new KeyAction(downAction, "Down"), DEFAULT_DOWN_BINDING);
        
        // Clockwise key binding.
        final Action cwAction = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                myTetris.rotateCW();
            }
        };
        myKeys.put(new KeyAction(cwAction, "Clockwise"), DEFAULT_CW_BINDING);
        
        // Counterclockwise key binding.
        final Action ccwAction = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                myTetris.rotateCCW();
            }
        };
        myKeys.put(new KeyAction(ccwAction, "Counterclockwise"), DEFAULT_CCW_BINDING);
        
        // Drop key binding.
        final Action dropAction = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                myTetris.drop();
                SoundEffects.BLOCK.play(mySoundPlayer);
            }
        };
        myKeys.put(new KeyAction(dropAction, "Drop"), DEFAULT_DROP_BINDING);
        
     // Pause key binding.
        final Action pauseAction = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                togglePause();
            }
        };
        myPauseKeyAction = new KeyAction(pauseAction, "Pause");
        myKeys.put(myPauseKeyAction, DEFAULT_PAUSE_BINDING);
    }
    
    
} // end of GamePanel


