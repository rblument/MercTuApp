package edu.regis.merc.view;

import javax.swing.*;
import java.awt.*;

public class TuringMachineView extends JPanel {

    public TuringMachineView() {
        setBorder(BorderFactory.createTitledBorder("Turing Machine"));
        setLayout(new GridBagLayout());
        add(new JLabel("Turing Machine simulation area"));
    }
}
