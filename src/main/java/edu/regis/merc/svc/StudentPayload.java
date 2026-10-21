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
package edu.regis.merc.svc;

import edu.regis.merc.model.RepresentationKind;

/**
 * What a student did, sent as the data of a ClientRequest.
 *
 * This carries the payload for the requests a student drives: completing a
 * step, asking for a hint, and asking for the answer.
 *
 * Both sides of the connection use this class. The
 * client used to build the JSon by concatenating field names as string
 * literals, so a renamed field on the server wasn't a compile error. Since
 * grading requires a representation, a mistyped key would quietly make every
 * answer wrong.
 *
 * @author Andrew Bazen
 */
public class StudentPayload {
    /**
     * The database id of the step the student is working on.
     */
    private int stepId;

    /**
     * Which of the three computational models the student selected a component
     * in. Null on requests that do not involve a selection.
     *
     * Component ids are only unique within one representation, so an answer is
     * meaningless without this.
     */
    private RepresentationKind representation;

    /**
     * The id of the component the student selected, within the representation
     * named above.
     */
    private int selectedComponentId;

    /**
     * Which hint of the step's hints the student is asking for.
     */
    private int hintIndex;

    /**
     * Required by Gson, which instantiates this before populating its fields.
     */
    public StudentPayload() {
    }

    /**
     * Initialize a payload for a step the student is working on.
     *
     * @param stepId the database id of the step
     */
    public StudentPayload(int stepId) {
        this.stepId = stepId;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public RepresentationKind getRepresentation() {
        return representation;
    }

    public void setRepresentation(RepresentationKind representation) {
        this.representation = representation;
    }

    public int getSelectedComponentId() {
        return selectedComponentId;
    }

    public void setSelectedComponentId(int selectedComponentId) {
        this.selectedComponentId = selectedComponentId;
    }

    public int getHintIndex() {
        return hintIndex;
    }

    public void setHintIndex(int hintIndex) {
        this.hintIndex = hintIndex;
    }
}
