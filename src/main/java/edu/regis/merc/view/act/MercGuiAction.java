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
package edu.regis.merc.view.act;

import edu.regis.merc.util.ImgFactory;
import java.awt.Image;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

/**
 * Abstract root for all GUI actions in the MERC application.
 * 
 * Provides support for loading image icons.
 * 
 * @author rickb
 */
public abstract class MercGuiAction extends AbstractAction {
    /**
     * Initialize this action with the given name.
     * 
     * @param name a String naming this action.
     */
    public MercGuiAction(String name) {
        super(name);
    }
    
    /**
     * Load and return the image icon specified by the given image file name.
     * 
     * See ImgFactoryas for the location of the image to be loaged
     * 
     * @param imageFileName file name containing the image, such as, "save16.gif".
     * @param altText a String describing the image
     * @return 
     */
    protected ImageIcon loadIcon(String imageFileName, String altText) {
        // ToDo: Better error reporting
        Image img = ImgFactory.createImage(imageFileName);
        
        return new ImageIcon(img, altText);
    }
}

