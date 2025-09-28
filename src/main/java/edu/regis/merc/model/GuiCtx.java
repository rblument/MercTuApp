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

import java.awt.Font;

/**
 * A swing-independent holder for a state's graphical information.
 *
 * @author Rickb
 */
public class GuiCtx {
    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;

    private int x2 = 0;
    private int y2 = 0;

    private String fontName = "Dialog";
    private int fontStyle  = Font.PLAIN;
    private int fontSize = 10; // pts

    private int borderWidth = 1;

    protected boolean isSelected = false;

    public GuiCtx() {
    }

    public boolean getIsSelected() {
	return isSelected;
    }

    public void setIsSelected(boolean flag) {
	isSelected = flag;
    }


    public String getFontName() {
	return fontName;
    }

    public void setFontName(String name) {
	fontName = name;
    }

    public int getX() {
	return x;
    }

    public void setX(int x) {
	this.x = x;
    }

    public int getY() {
	return y;
    }

    public void setY(int y) {
	this.y = y;
    }

    public int getWidth() {
	return width;
    }

    public void setWidth(int width) {
	this.width = width;
    }

    public int getHeight() {
	return height;
    }

    public void setHeight(int height) {
	this.height = height;
    }

    public int getFontStyle() {
	return fontStyle;
    }

    public void setFontStyle(int style) {
	this.fontStyle = style;
    }

    public int getFontSize() {
	return fontSize;
    }

    public void setFontSize(int size) {
	this.fontSize = size;
    }

    public int getBorderWidth() {
	return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
	this.borderWidth = borderWidth;
    }

    public int getX2() {
	return x2;
    }

    public void setX2(int x) {
	this.x2 = x;
    }

    public int getY2() {
	return y2;
    }

    public void setY2(int y) {
	this.y2 = y;
    }

}
