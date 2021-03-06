/*
 * TCSS 305 � Winter 2015
 * Assignment 6 - Tetris
 */

package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import model.Point;
import model.TetrisPiece;

/**
 * The display panel for the next Tetris piece.
 * 
 * @author Justin Arnett (jarnett@uw.edu)
 * @version 04 March 2015
 */
@SuppressWarnings("serial")
public class NextPiecePanel extends JPanel implements Observer {
    
    /** The padding of the panel. */
    private static final double PADDING = 0.50;
    /** The padding of the border. */
    private static final int BORDER_PADDING = 9;
    /** Text box length on graphic. */
    private static final int BLACK_BOX_LENGTH = 50;
    /** Size of the font of the next piece text. */
    private static final int FONT_SIZE = 15;
    /** Horizontal shift on font. */
    private static final int FONT_SHIFT = 9;
    /** The preferred size of the game panel. */
    private static final Dimension PREFERRED_DIMENSION = new Dimension(180, 180);
    
    /** The next piece the Tetris game will play. */
    private TetrisPiece myNextPiece;
    
    /** The rendered block size. */
    private int myBlockSize;
    
    /** The starting point of the Tetris piece render. */
    private Point myOrigin;

    
    /**
     * The panel that will render the next Tetris piece that will be played.
     * 
     * @param theTetrisPiece The next Tetris piece.
     */
    public NextPiecePanel(final TetrisPiece theTetrisPiece) {
        super();
        myNextPiece = theTetrisPiece;
        displayPanel();
    }
    
    
    /**
     * The panel area that renders the next game piece.
     */
    private void displayPanel() {
        setPreferredSize(PREFERRED_DIMENSION);
        setBackground(Color.CYAN);
    }
    
    
    /**
     * Prepares the next piece to display.
     * 
     * @param thePiece The tetris piece.
     */
    public void setNextPiece(final TetrisPiece thePiece) {
        myNextPiece = thePiece;
        repaint();
    }
    
    
    @Override
    public void paintComponent(final Graphics theGraphics) {
        super.paintComponent(theGraphics);
        final Graphics2D graphic = (Graphics2D) theGraphics;
        graphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                 RenderingHints.VALUE_ANTIALIAS_ON);
        
        
        myBlockSize = (int) (this.getWidth() / myNextPiece.getWidth() * PADDING);
        // Calculates the center of the tetris piece.
        final Point center = new Point(myNextPiece.getWidth() * myBlockSize / 2,
                             myNextPiece.getHeight() * myBlockSize / 2);
        final Point panelCenter = new Point(this.getWidth() / 2, this.getHeight() / 2);
        // Calculates where the next piece should render.
        myOrigin = new Point(panelCenter.x() - center.x(), panelCenter.y() - center.y());
        
        
        graphic.setPaint(Color.BLACK);
        graphic.fill(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));
        
        graphic.setPaint(myNextPiece.getColor());
        for (int i = 0; i < myNextPiece.getPoints().length; i++) {
            drawBlock(graphic);
        }
        drawBorderGraphic(graphic);
    }
    
    
    /**
     * Renders the individual blocks.
     * 
     * @param theGraphics The rendering graphic.
     */
    private void drawBlock(final Graphics2D theGraphics) {
        for (final Point p : myNextPiece.getPoints()) {
            theGraphics.fill(new Rectangle2D.Double(p.x() * myBlockSize + myOrigin.x(),
                               (myNextPiece.getHeight() - p.y()) * myBlockSize + myOrigin.y(),
                               myBlockSize,
                               myBlockSize));
            Color oldColor = theGraphics.getColor();
            theGraphics.setPaint(new Color(0, 0, 0, 100));
            Stroke oldStroke = theGraphics.getStroke();
            int thickness = 3;
            theGraphics.setStroke(new BasicStroke(thickness));
            theGraphics.drawRect(p.x() * myBlockSize + myOrigin.x() + (thickness/2),
                                 (myNextPiece.getHeight() - p.y())
                                                   * myBlockSize + myOrigin.y() + (thickness/2),
                                 myBlockSize - thickness,
                                 myBlockSize - thickness);
            theGraphics.setStroke(oldStroke);
            theGraphics.setPaint(oldColor);
            
        }
    }
    
    
    /**
     * Draws the border graphic of the next piece panel.
     * 
     * @param theGraphics The rendering graphic.
     */
    private void drawBorderGraphic(final Graphics2D theGraphics) {
        theGraphics.setStroke(new BasicStroke(2));
        theGraphics.setPaint(Color.RED);
        theGraphics.drawRoundRect(BORDER_PADDING, BORDER_PADDING,
                                  this.getWidth() - BORDER_PADDING * 2,
                                  this.getHeight() - BORDER_PADDING * 2,
                                  BORDER_PADDING, BORDER_PADDING);
        theGraphics.setPaint(Color.BLACK);
        theGraphics.fillRect(this.getWidth() / 2 - BLACK_BOX_LENGTH,
                             BORDER_PADDING / 2,
                             BLACK_BOX_LENGTH * 2,
                             BORDER_PADDING * 2);
        theGraphics.setPaint(Color.GREEN);
        theGraphics.setFont(new Font("Verdana", 1, FONT_SIZE));
        theGraphics.drawString("Next Piece",
                               this.getWidth() / 2 - BLACK_BOX_LENGTH + FONT_SHIFT,
                               BORDER_PADDING * 2);
    }
    
    
    /**
     * The observer method for listening for the next tetris piece.
     * 
     * @param theObj The object sending the update.
     * @param theArg The data being sent.
     */
    public void update(final Observable theObj, final Object theArg) {
        if (theArg instanceof TetrisPiece) {
            myNextPiece = (TetrisPiece) theArg;
            //repaint();
        }
        repaint();
    }

}
