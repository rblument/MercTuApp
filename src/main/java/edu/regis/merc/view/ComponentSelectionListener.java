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
package edu.regis.merc.view;

import edu.regis.merc.model.RepresentationKind;

/**
 * Notified when a student selects a component in one of the representation
 * panels.
 *
 * A component id is only unique within a single representation, so a panel
 * reports both: the panel it is, and what was selected inside it. A panel that
 * does not report its representation cannot be answered against, which is
 * deliberate, which is what stops a new panel from being silently ignored by
 * grading.
 *
 * This is kept separate from the existing SelectionListener, whose
 * selectionChange takes a Selectable. That interface requires drag coordinates
 * and highlight behaviour that mean nothing for a text label in a flow layout.
 *
 * @author Andrew Bazen
 */
public interface ComponentSelectionListener {
    /**
     * Called when the student selects a component.
     *
     * @param representation the panel the selection was made in
     * @param componentId the id of the selected component, unique within that
     *        representation only
     */
    void componentSelected(RepresentationKind representation, int componentId);
}
