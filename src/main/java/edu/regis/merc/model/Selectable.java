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
package edu.regis.merc.model;

/**
 * A component displayed in a canvas that can be selected and displays as such.
 * 
 * @author rickb
 */
public interface Selectable {
    /**
     * Return whether this component is selected.
     * 
     * @return true if selected, otherwise false.
     */
    boolean isSelected();

    /**
     * Select this component, which should then have isSelected return true and
     * display the component as selected in the canvas in which it's displayed.
     */
    void select();

    /**
     * Deselect this component, which should then have isSelected return false and
     * display the component as unselected in the canvas in which it's displayed.
     */
    void deselect();
    
    /**
     * As a result of the mouse entering this component, highlight it.
     */
    void highlight();
    
    /**
     * As a result of the mouse exiting this component, unhighlight it.
     */
    void unhighlight();
    
    /**
     * Return the initial x location of the component when the mouse was clicked
     * (prior to dragging this component with the mouse).
     * 
     * @return initial x position
     */
    int getDragX();
    
    /**
     * Return the initial y location of the component when the mouse was clicked
     * (prior to dragging this component with the mouse).
     * 
     * @return initial y position
     */
    int getDragY();
}
