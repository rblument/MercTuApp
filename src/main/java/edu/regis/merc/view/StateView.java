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
import edu.regis.merc.model.State;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/**
 * A view displaying a Turing Machine State (in a parent TuringMachineCanvas).
 *
 * @author rickb
 */
public class StateView extends JComponent implements MouseListener, MouseMotionListener, Selectable {

    /**
     * The standard background color for this node.
     */
    private final Color bkgColor = Color.WHITE;

    /**
     * The model displayed by this view
     */
    private State model;

    /**
     * If true, this state is currently being dragged in
     */
    private boolean isBeingDragged = false;

    /**
     * The initial x location of this view prior to being moved by the mouse.
     */
    private int dragX;

    /**
     * The initial x location of this view prior to being moved by the mouse.
     */
    private int dragY;

    /**
     * If true, this view is currently highlighted.
     *
     * Highlighting occurs when the mouse enters this view. It is not the same
     * as being selected.
     */
    private boolean isHighlighted;

    /**
     * Menu that is displayed with a right mouse click occurs in this view.
     */
    private JPopupMenu popupMenu;

    /**
     * Initialize this view to display the given state.
     *
     * @param model a (Turing Machine) State
     */
    public StateView(State model) {
        isBeingDragged = false;
        isHighlighted = false;

        initializePopupMenu();

        addMouseListener(this);
        addMouseMotionListener(this);

        setModel(model);
    }

    /**
     * Return the (Turing Machine) state displayed in this view.
     *
     * @return a State
     */
    public State getModel() {
        return model;
    }

    /**
     * Display the given (Turing Machine) State in this view.
     *
     * @param model a State.
     */
    public void setModel(State model) {
        this.model = model;
        updateView();
    }

    /**
     * Add a changeListener to listen for changes.
     *
     * @param l listener to add.
     */
    public void addSelectionChangeListener(SelectionListener l) {
        listenerList.add(SelectionListener.class, l);
    }

    /**
     * Remove a changeListener.
     *
     * @param l listener to remove
     */
    public void removeSelectionChangeListener(SelectionListener l) {
        listenerList.remove(SelectionListener.class, l);
    }

    /**
     * Update the size of this view using the State model's GuiCtx.
     */
    public void updateView() {
        GuiCtx gCtx = model.getGuiCtx();
        Font f = new Font(gCtx.getFontName(),
                gCtx.getFontStyle(),
                gCtx.getFontSize());

        setFont(f);

        FontMetrics metrics = getFontMetrics(f);

        Dimension d = new Dimension(gCtx.getWidth(), gCtx.getWidth());

        setPreferredSize(d);
        setSize(d);
    }

    /**
     * Draw the component on the given graphics. This handles the look and
     * feel of this component.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Dimension d = getPreferredSize();

        // Paint the background
        int h = d.height - 1;
        int w = d.width - 1;

        if (!isBeingDragged) {
            GuiCtx ctx = model.getGuiCtx();
            if (ctx.getIsSelected()) {
                g.setColor(Color.LIGHT_GRAY);
            } else {
                g.setColor(bkgColor);
            }

            g.fillOval(0, 0, h - 1, h - 1);
        }

        // Paint the foreground
        if (isHighlighted) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.BLACK);
        }

        g.drawOval(0, 0, d.width - 1, d.height - 1);

        g.setColor(Color.BLACK);

        if (!isBeingDragged) {
            int y = d.height - (d.height / 2);
            g.drawString(model.getName(), 5, y);
        }
    }

    /**
     * Show the popup menu on a right click.
     * 
     * @param e 
     */
    protected void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelected() {
        return model.getGuiCtx().getIsSelected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void select() {
        model.getGuiCtx().setIsSelected(true);

        fireSelectionChangeEvent();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDragX() {
        return dragX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDragY() {
        return dragY;
    }

    protected void fireSelectionChangeEvent() {
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

    /**
     * Handles a double click on this state.
     */
    protected void doubleClick() {
        JOptionPane.showMessageDialog(MainFrame.instance(), "Double click on this state.");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            doubleClick();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        highlight();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        unhighlight();
    }

    /**
     * The mouse was pressed on the component. Do what you have to do.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        select();
        maybeShowPopup(e);

        isBeingDragged = true;
        dragX = e.getX();
        dragY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
        isBeingDragged = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isBeingDragged) {
            int newX = getX() + e.getX() - dragX;
            int newY = getY() + e.getY() - dragY;

            setLocation(newX, newY);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    /**
     * Initialize the popup menu that appears when a right click is made in this view.
     */
    private void initializePopupMenu() {
        popupMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Selection One");

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainFrame.instance(), "Option 1 selected!");
            }
        });

        popupMenu.add(item);

        item = new JMenuItem("Selection Two");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainFrame.instance(), "Option 2 selected!");
            }
        });

        popupMenu.add(item);
    }
}
