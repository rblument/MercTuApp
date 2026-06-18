package edu.regis.merc.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Custom JLabel that acts as an interactive and clickable piece of a math
 * equation. It stores the database ID of the component it represents so it can
 * be sent to the server for grading
 */
public class InteractiveLabel extends JLabel {

    /**
     * the unique database ID associated with this component. This id is essential
     * for synch between client-side selection and server-side grading logic
     */
    private int componentId;
    private boolean isSelected = false;

    /**
     * Constructs an interactive math label
     * 
     * @param text        - the mathematical symbol to display (e.g., "x","λ" "0")
     * @param componentId
     */
    public InteractiveLabel(String text, int componentId) {
        super(text);
        this.componentId = componentId;

        setFont(new Font("Monospaced", Font.BOLD, 24));

        setOpaque(true);
        setBackground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isSelected) {
                    setBackground(new Color(230, 240, 255)); // Light blue hover
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isSelected) {
                    setBackground(Color.WHITE);
                }
            }
        });
    }

    /**
     * Returns the database ID required for the server-side grading loop
     * 
     * @return
     */
    public int getComponentId() {
        return componentId;
    }

    /**
     * Toggles the selection state and updates the background color.
     * * This method is called by the parent view to manage exclusive selection
     * and visual synchronization across the tutoring session.
     * * @param selected True if this component is the student's chosen answer.
     */
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        if (selected) {
            setBackground(new Color(150, 180, 255)); // Darker blue when clicked
        } else {
            setBackground(Color.WHITE); // Back to normal
        }
    }

}
