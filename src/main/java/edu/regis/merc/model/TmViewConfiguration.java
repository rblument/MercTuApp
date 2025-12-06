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

import java.util.ArrayList;

/**
 * Specifies which components of the Turing Machine computation model for the
 * current problem should be displayed to the student in the GUI.
 * 
 * @author rickb
 */
public class TmViewConfiguration extends Model {
    /**
     * A list of the states (ids) that should be displayed.
     */
    private ArrayList<Integer> stateIds;
    
    /**
     * A list of the transitions (ids) that should be displayed.
     */
    private ArrayList<Integer> transitionIds;
    
    /**
     * A list of the states (ids) that should be displayed.
     */
    private ArrayList<Integer> tapeCellIds;
    
    /**
     * If true, the start state (>) indicator should be displayed.
     */
    private boolean displayStartStateIndicator = true;
    
    /**
     * A list of the accept states (ids) indicators that should be displayed.
     */
    private ArrayList<Integer> acceptStateIndicatorIds;
    
    /**
     * A list of the reject state (ids) indicators that should be displayed.
     */
    private ArrayList<Integer> rejectStateIndicatorIds;
    
    /**
     * If true, the tape head at the current tape location should be displayed
     */
    private boolean displayTapeHead = true;
    
    
    public TmViewConfiguration() {
        this(DEFAULT_ID);
    }
    
    public TmViewConfiguration(int id) {
        super(id);
        
        stateIds = new ArrayList<>();
        transitionIds = new ArrayList<>();
        tapeCellIds = new ArrayList<>();
        acceptStateIndicatorIds = new ArrayList<>();
        rejectStateIndicatorIds = new ArrayList<>();
    }
    
    public boolean containsStateId(int id) {
        return stateIds.contains(id);
    }

    public ArrayList<Integer> getStateIds() {
        return stateIds;
    }

    public void setStateIds(ArrayList<Integer> stateIds) {
        this.stateIds = stateIds;
    }
    
    public boolean containsTransitionId(int id) {
        return transitionIds.contains(id);
    }

    public ArrayList<Integer> getTransitionIds() {
        return transitionIds;
    }

    public void setTransitionIds(ArrayList<Integer> transitionIds) {
        this.transitionIds = transitionIds;
    }

    public boolean containsTapeCellId(int id) {
        return tapeCellIds.contains(id);
    }
    
    public ArrayList<Integer> getTapeCellIds() {
        return tapeCellIds;
    }

    public void setTapeCellIds(ArrayList<Integer> tapeCellIds) {
        this.tapeCellIds = tapeCellIds;
    }

    public boolean isDisplayStartStateIndicator() {
        return displayStartStateIndicator;
    }

    public void setDisplayStartStateIndicator(boolean displayStartStateIndicator) {
        this.displayStartStateIndicator = displayStartStateIndicator;
    }
    
    public boolean containsAcceptStateId(int id) {
        return acceptStateIndicatorIds.contains(id);
    }

    public ArrayList<Integer> getAcceptStateIndicatorIds() {
        return acceptStateIndicatorIds;
    }

    public void setAcceptStateIndicatorIds(ArrayList<Integer> acceptStateIndicatorIds) {
        this.acceptStateIndicatorIds = acceptStateIndicatorIds;
    }

    public boolean containsRejectStateId(int id) {
        return rejectStateIndicatorIds.contains(id);
    }
    
    public ArrayList<Integer> getRejectStateIndicatorIds() {
        return rejectStateIndicatorIds;
    }

    public void setRejectStateIndicatorIds(ArrayList<Integer> rejectStateIndicatorIds) {
        this.rejectStateIndicatorIds = rejectStateIndicatorIds;
    }

    public boolean isDisplayTapeHead() {
        return displayTapeHead;
    }

    public void setDisplayTapeHead(boolean displayTapeHead) {
        this.displayTapeHead = displayTapeHead;
    }
    

}
