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
package edu.regis.merc.util;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Utility for loading images (icons) from a file.
 * 
 * These will be located within the project at:
 *    src/main/java/resources/img/*
 * 
 * @author rickb
 */
public class ImgFactory {
    /**
     * Directory in the resources directory where the images are located.
     */
    private static final String DIRECTORY = "/img/";
    
    /**
     * Create and return an image icon by loading it from a file.
     * 
     * @param fileName, a String specifying the name of the file containing the 
     *                  image to load, such as, "Save16.gif"
     * @param altText a String with an alternate description of the image.
     * @return ImageIcon with the corresponding seven segment display.
     */
    public static ImageIcon createIcon(String fileName, String altText) {
        return new ImageIcon(createImage(fileName), altText);
    
    }
    
     /**
     * Create and return an image by loading it from a file.
     * 
     * @param fileName, a String specifying the name of the file containing the 
     *                  image to load, such as, "Save16.gif"
     * @return BufferedImage with the corresponding seven segment display.
     */
    public static BufferedImage createImage(String fileName) {
        String path = DIRECTORY + fileName;
 
        try {
            return ImageIO.read(ImgFactory.class.getResourceAsStream(path));
            
        } catch (IOException e) {
            System.err.println("Couldn't find image file: " + path);  
            return null;
        }
    }
}
