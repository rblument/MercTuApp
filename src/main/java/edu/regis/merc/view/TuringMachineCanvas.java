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
import edu.regis.merc.model.GuiCtx;
import edu.regis.merc.model.Model;
import edu.regis.merc.model.State;
import edu.regis.merc.model.Transition;
import edu.regis.merc.model.TuringMachine;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * A panel (canvas) capable of displaying a Turing Machine state model.
 * 
 * The displayed states can be selected and repositioned using the mouse.
 * 
 * @author rickb
 */
public class TuringMachineCanvas extends JPanel implements MouseListener, SelectionListener, Scrollable {
    /**
     * Component views should never appear within this many pixels of the border.
     */
    private static final int BORDER_PADDING = 5;
    
    /**
     * The Turing Machine model displayed in this canvas (view).
     */
    private TuringMachine model;

    /**
     * If non-null, the component, such as a TM state, currently selected.
     */
    private Selectable selection;

    /**
     * Initialize the canvas
     */
    public TuringMachineCanvas() {
	setPreferredSize(new Dimension(475,450));

	setLayout(null);
        
        addMouseListener(this);

	setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    /**
     * Display the given Turing Machine in this canvas.
     * 
     * @param model a Turing Machine whose state control model is displayed.
     */
    public void setModel(TuringMachine model) {
	removeAll();

	this.model = model;

	updateViews();
    }

    /**
     * Select the given state and display this selection in the corresponding
     * StateView for the given state. 
     * 
     * @param node a state to select
     */
    public void select(State node) {
        if (selection != null) {
            if (selection instanceof StateView stateView) {
                stateView.deselect();
            }
        }
        
        if (node != null) {
            Component[] components = getComponents();

            for (int i = 0; i < components.length; i++) {
                if (components[i] instanceof StateView) {
                    StateView stateView = (StateView) components[i];
                    if (stateView.getModel() == node) {
                        stateView.select();
                    
                        selection = stateView;
                    }
                }
            }
        }
    }

    /**
     * Utility that returns the id of the selected node or edge
     * 
     * @return the id of the currently selected node or DEFAULT_ID if nothing is
     *         currently selected.
     */
    public int selectedNodeId() {
	if (selection == null) {
	    return Model.DEFAULT_ID;
	} else {
	    return ((StateView) selection).getModel().getId();
	}
    }

    /**
     * Create all of the Node and Edge Views for the states and transitions in
     * the current Turing Machine model (used during a call to setModel).
     */
    private void updateViews() {
	buildNodeViews();
	buildEdgeViews();

	repaint();
    }

    /**
     * For each state in the Turing Machine model, create a corresponding
     * StateView that can be displayed in this canvas.     * 
     */
    private void buildNodeViews() {
        Insets insets = getInsets();

	int maxW = 0;
	int maxH = 0;

        for (State state : model.getStates()) {
	    StateView view = new StateView(state);
	    view.addSelectionChangeListener(this);

	    if (state.getGuiCtx().getIsSelected())
		selection = view;
            
            GuiCtx gCtx = view.getModel().getGuiCtx();
	    int x = gCtx.getX();
	    int y = gCtx.getY();
            int w = gCtx.getWidth();
            int h = gCtx.getHeight();
            
            view.setLocation(new Point(x, y));
	    view.setBounds(x + insets.left, y + insets.top, w, h);
            
	    if ((x + w) > maxW)
		maxW = w;
	    
	    if ((y + h) > maxH)
		maxH = h;
             
            add(view);
	}
        
        int padding = BORDER_PADDING * 2;
        model.getGuiCtx().setWidth(maxW + padding); // BORDER_PADDING on ach side
	model.getGuiCtx().setHeight(maxH + padding);
        
        setSize(model.getGuiCtx().getWidth(), model.getGuiCtx().getHeight());
    }

    /**
     * For each transition in the Turing Machine model, create a corresponding
     * TransitionView that can be displayed in this canvas.     * 
     */
    private void buildEdgeViews() {
        ArrayList<TransitionView> views = new ArrayList<>();
        HashSet<Transition> transitions = model.getCachedTransitions();

        int maxW = 0;
        int maxH = 0;

        for (Transition transition : transitions) {
            // ToDo: Mimic buildNodeViews as needed.
            TransitionView view = new TransitionView(transition);
            view.addSelectionChangeListener(this);

            if (transition.getGuiCtx().getIsSelected()) {
                selection = view;
            }

            GuiCtx gCtx = view.getModel().getGuiCtx();
            int x1 = gCtx.getX();
            int y1 = gCtx.getY();
            int x2 = gCtx.getX2();
            int y2 = gCtx.getY2();


            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            int width = Math.abs(x2 - x1);
            int height = Math.abs(y2 - y1);

            view.setLocation(x1, y1);
            view.setBounds(minX, minY, width, height);

            if ((minX + width) > maxW)
                maxW = width;

            if ((minY + height) > maxH)
                maxH = height;

            views.add(view);
        }

        int padding = BORDER_PADDING * 2;
        model.getGuiCtx().setWidth(maxW + padding);
	    model.getGuiCtx().setHeight(maxH + padding);

        setSize(model.getGuiCtx().getWidth(), model.getGuiCtx().getHeight());
    }

    @Override
    public Dimension getPreferredSize() {
	return getMinimumSize();
    }
    
    @Override
    public Dimension getMinimumSize() {
	if (model == null) {
            return new Dimension(200,200);
            
        } else {
	    //size = new Dimension();
            Dimension d = getSize();
	    d.width = model.getGuiCtx().getWidth();
	    d.height = model.getGuiCtx().getHeight();
	 
	    Insets insets = getInsets();
	    d.width += insets.left + insets.right + (BORDER_PADDING * 2);
	    d.height += insets.top + insets.bottom + (BORDER_PADDING * 2);

	    return d;
        }
    }

    @Override
    public void selectionChange(Selectable source) {
	if (source == selection) {
	    if (!source.isSelected()){
		selection = null;
	    }
	} else {
	    if (selection != null) {
		selection.deselect();
	    }

	    selection = source;
	}

	repaint();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    // ToDo update this when needed.
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        int cellSize = 50; // model.getCellSize();
        double scrollPos = 0;
        if(orientation == SwingConstants.HORIZONTAL) {
            //scrolling left
            if(direction < 0) {
                scrollPos = visibleRect.getMinX();
               
            }
            //scrolling right
            else if (direction > 0) {
                scrollPos = visibleRect.getMaxX();
            }
        } else {
            //scrolling up
            if(direction < 0) {
                scrollPos = visibleRect.getMinY();
            }
            //scrolling down
            else if (direction > 0) {
                scrollPos = visibleRect.getMaxY();
            }
        }
        int increment = Math.abs((int) Math.IEEEremainder(scrollPos, cellSize));
        if(increment == 0) {
            increment = cellSize;
        }
      
        return  increment;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, 
					   int orientation, int direction)
    {
         return getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // This will be called if no StateView was clicked on.
        select(null); 
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }    
}
