package edu.regis.merc.view;

import edu.regis.merc.model.LCExpression;
import javax.swing.*;
import java.awt.*;

public class LambdaCalcView extends GPanel {

    private LCExpression model;
    private JLabel expressionLabel;
    
    public LambdaCalcView() {
        
        setBorder(BorderFactory.createTitledBorder("Lambda Calculus"));
        initializeComponents();
        layoutComponents();
        
        
    }
    
    public LCExpression getModel(){
        return model;
    }
    
    public void setModel(LCExpression model){
        this.model = model;
        
        if (model != null) {
            System.out.println("View received: " + model.toString()); // Debug log
            expressionLabel.setText(model.toString());
        } else {
            System.out.println("View received NULL model"); // Debug log
            expressionLabel.setText("No Equation Data");
        }
    }
    
    public JLabel getExpressionLabel(){
        return expressionLabel;
    }
    
    public void setExpressionLabel(JLabel expressionLabel){
        this.expressionLabel = expressionLabel;
    }
    
    private void initializeComponents(){
        expressionLabel = new JLabel();
        expressionLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        
    }
    
    private void layoutComponents(){
        setLayout(new GridBagLayout());
        addc(expressionLabel, 0,0, 1,1, 0,0,
	     GridBagConstraints.NORTHWEST,  GridBagConstraints.NONE,
	     0,0,0,0);
        
    }
}
