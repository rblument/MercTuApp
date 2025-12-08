package edu.regis.merc.view;

import edu.regis.merc.model.GuiCtx;
import edu.regis.merc.model.State;
import edu.regis.merc.model.TuringMachine;
import edu.regis.merc.model.TutoringSession;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class TuringMachineView extends GPanel {
    private TutoringSession model;
    
    private TuringMachine tm;
    
   
    
    private TuringMachineCanvas canvas;

    public TuringMachineView() {
        initializeComponents();
        layoutComponents();
    }

    public TutoringSession getModel() {
        return model;
    }

    public void setModel(TutoringSession model) {
        this.model = model;
        
        tm = model.getProblem().getTuringMachine();
        
        System.out.println("Turing Machine: " + tm);
        
        canvas.setModel(tm);
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
