/*
 * MERC^T: Multiple External Representations of Computation Tutor
 * 
 *  (C) Richard Blumenthal, All rights reserved
 * 
 *  Unauthorized use, duplication or distribution without the authors'
 *  permission is strictly prohibited.
 * 
 *  Unless required by applicable law or agreed to in writing, this
 *  software is distributed on an "AS IS" basis without warranties
 *  or conditions of any kind, either expressed or implied.
 */
package edu.regis.merc.view;

import edu.regis.merc.model.Selectable;
import edu.regis.merc.model.Edge;
import edu.regis.merc.model.GuiCtx;
import edu.regis.merc.model.Transition;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

/**
 * A view of an Edge model for a TuringTransition component displayed as an arrow.
 *
 * The view's location is set in ChartCanvas.setEdgeViews(...)
 * 
 * ToDo: Mimic the StateView, as required.
 *
 * @author Rickb
 */
public class TransitionView extends JComponent implements MouseListener, Selectable {
    /**
     * The model displayed by this view
     */
    protected Transition model;

    protected Font font;

    protected int halfW;
    protected int halfH;
    
    private int dragX;
    private int dragY;
    
    private boolean isHighlighted;

    JPopupMenu popupMenu;

    public TransitionView(Transition model) {
        isHighlighted = false;
        
        
	setModel(model);

        addMouseListener(this);
    }

    public Edge getModel() {
	return model;
    }

    public void setModel(Transition model) {
	this.model = model;
	updateView();
    }

    /**
     * Add a changeListener to listen for changes.
     * 
     * @param l
     *            Listener to add
     */
    public void addSelectionChangeListener(SelectionListener l)   {
        listenerList.add(SelectionListener.class, l);
    }

    /**
     * Remove a changeListener.
     * 
     * @param l
     *            Listener to remove
     */
    public void removeSelectionChangeListener(SelectionListener l)   {
        listenerList.remove(SelectionListener.class, l);
    }


    public void updateView() {
    	//Font f = new Font("Dialog", Font.PLAIN, fontPtSize);
	GuiCtx ctx = model.getGuiCtx();
	Font f = new Font(ctx.getFontName(), 
			  ctx.getFontStyle(), 
			  ctx.getFontSize());

	setFont(f);

	FontMetrics metrics = getFontMetrics(f);

	int maxH = 0;
	int maxW = 0;

	int x1 = ctx.getX();
	int y1 = ctx.getY();
	int x2 = ctx.getX2();
	int y2 = ctx.getY2();

	if (x1 == x2) { // Vertical line
	    maxW = metrics.stringWidth(model.getType()) + 16;
	    maxH = Math.abs(y2 - y1);

	} else {        // Horixontal line
	    maxH  = metrics.getHeight() + 16;
	    maxW = Math.abs(x2 - x1);
	}

	halfH = maxH / 2;
	halfW = maxW / 2;


	Dimension d = new Dimension(maxW, maxH);

	setPreferredSize(d);
	setSize(d);
    }

    private void paintBackground(Graphics g) {
	/*
	GuiCtx ctx = model.getGuiCtx();

	int h = ctx.getHeight() - 1;
	int w = ctx.getWidth() - 1;
	
	// Paint the background
	if (!isBeingDragged) {
	  g.setColor(bkgColour);
	  g.fillRect(0,0, w, h);

	  //g.fillRect(0,0, w, d.height-1);
	}

	if (ctx.getIsSelected()) {
	    g.setColor(stripeColor);
            stripeRect(g, 0, GAP, w, h, 8, 3);
    
            g.setColor(bkgColour);
            g.fillRect(7, GAP+7, w-14, h-14);
	}
	*/
    }

    /**
     * Draw the component on the given graphics. This handles all of the
     * look and feel of this component.
     */
    protected void paintComponent(Graphics g) {
	GuiCtx ctx = model.getGuiCtx();

	FontMetrics metrics = getFontMetrics(getFont());
	int fontH = metrics.getHeight();

	Dimension d = getSize();

	paintBackground(g);

	// Paint the enclosing frame
	g.setColor(Color.black);
	if (ctx.getX() == ctx.getX2()) {              // vertical line
	    //System.out.println("height: " + d.height);
        int centerX = d.height / 2;
	    g.drawLine(centerX, 0, centerX, d.height-1);
	    // TBD upside down line

	    int baseline = 2;
	    if (ctx.getY() > ctx.getY2()) {                // upward arrow
		g.drawLine(6, 0, 12, 6); // rarrowhead
		g.drawLine(6, 0, 0,  6);  // larrowhead
		baseline = d.height - fontH;
	    } else {                                       // down arrow
		g.drawLine(6, d.height-1, 12, d.height-6); // rarrowhead
		g.drawLine(6, d.height-1, 0, d.height-6);  // larrowhead
	    }

//	    if (model.getType().equals("yes"))
//		g.drawString("yes", 7, baseline);
//	    if (model.getType().equals("no"))
//		g.drawString("no",  7, baseline);
		

	} else {                                      // horizontal
        int centerY = d.height / 2;
	    int baseline = fontH + 1;
	    g.drawLine(0, centerY, d.width-1, centerY);

	    // TBD backgrounds sline
//	    g.drawLine(d.width-1, baseline, d.width-6 , baseline - 6); // arrow
//	    g.drawLine(d.width-1, baseline, d.width-6 , baseline + 6); // arrow

//	    if (model.getType().equals("yes"))
//		g.drawString("yes", 4, fontH);
//	    else if (model.getType().equals("no"))
//		g.drawString("no", 4, fontH);
	}
    }

    /**
     * Draw stripes over a rectangle - yet another thing missing from the AWT
     * GNU License from Greenfoot
     */
    public static void stripeRect(Graphics g, int x, int y, int width, int height, int separation, int thickness)
    {
        for (int offset = 0; offset < width + height; offset += separation)
            for (int i = 0; i < thickness; i++, offset++) {
                int x1, y1, x2, y2;

                if (offset < height) {
                    x1 = x;
                    y1 = y + offset;
                }
                else {
                    x1 = x + offset - height;
                    y1 = y + height;
                }

                if (offset < width) {
                    x2 = x + offset;
                    y2 = y;
                }
                else {
                    x2 = x + width;
                    y2 = y + offset - width;
                }

                g.drawLine(x1, y1, x2, y2);
            }
    }


    protected void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            //getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void setPopupMenu(JPopupMenu popupMenu)   {
        this.popupMenu = popupMenu;
    }

    public JPopupMenu getPopupMenu()
    {
        if (popupMenu == null) {
            //popupMenu = role.createPopupMenu(classBrowser, this, interactionListener);
            //popupMenu.setInvoker(this);
        }
        return popupMenu;
    }

    @Override
    public boolean isSelected() {
	return model.getGuiCtx().getIsSelected();
    }

    @Override
    public void select()   {
	model.getGuiCtx().setIsSelected(true);

        fireSelectionChangeEvent();
    }

    @Override
    public void deselect() {
        if (isSelected()) {
	    model.getGuiCtx().setIsSelected(false);
            fireSelectionChangeEvent();
        }
    }
    
     /**
     * {@inheritDoc}
     */
    @Override
    public void highlight() {
        isHighlighted = true;
        fireSelectionChangeEvent();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void unhighlight() {
        isHighlighted = false;
        fireSelectionChangeEvent();
    }
    
    @Override
    public int getDragX() {
        return dragX;
    }
    
    @Override
    public int getDragY() {
        return dragY;
    }


    protected void fireSelectionChangeEvent()
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == SelectionListener.class) {
                ((SelectionListener) listeners[i + 1]).selectionChange(this);
            }
        }
    }


    protected void doubleClick()    {
        //gClass.edit();
    }


    /**
     * Mouse-click on this class view. Chek for double-click and handle.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1 && ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)) {
            doubleClick();
        }
    }

    public void mouseEntered(MouseEvent e)
    { }

    public void mouseExited(MouseEvent e)
    { }

    /**
     * The mouse was pressed on the component. Do what you have to do.
     */
    public void mousePressed(MouseEvent e)
    {
        select();
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e)
    {
        maybeShowPopup(e);
    }

}
