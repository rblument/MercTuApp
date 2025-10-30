package edu.regis.merc.view;

import edu.regis.merc.model.GuiCtx;
import edu.regis.merc.model.State;
import edu.regis.merc.model.TuringMachine;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class TuringMachineView extends GPanel {
    
    private TuringMachine model;
    
    private TuringMachineCanvas canvas;

    public TuringMachineView() {
        initializeComponents();
        layoutComponents();
    }

    public TuringMachine getModel() {
        return model;
    }

    public void setModel(TuringMachine model) {
        this.model = model;
        
        canvas.setModel(model);
    }
    
    private void initializeComponents() {
        canvas = new TuringMachineCanvas();
    }
    
    private void layoutComponents() {
        canvas.setBackground(Color.white);
	addc(canvas, 0,0, 1,1, 1.0,1.0,
	     GridBagConstraints.NORTHWEST,  GridBagConstraints.BOTH,
	     0,0,0,0);	
    }
}
