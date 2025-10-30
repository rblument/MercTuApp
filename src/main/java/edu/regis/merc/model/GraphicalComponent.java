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
 * A titled model containing information for displaying this model with
 * manually positioning in an associated canvas (e.g., TuringMachineViewCanvas).
 * 
 * @author rickb
 */
public class GraphicalComponent extends TitledModel {
    /**
     * Swing-independent attributes for displaying and positioning this component.
     */
    protected GuiCtx guiCtx;
    
    public GraphicalComponent() {
        this(DEFAULT_ID);
    }
    
    public GraphicalComponent(int id) {
        super(id);
        
        guiCtx = new GuiCtx();
    }

    public GuiCtx getGuiCtx() {
        return guiCtx;
    }

    public void setGuiCtx(GuiCtx guiCtx) {
        this.guiCtx = guiCtx;
    }
}
