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

/**
 * One of the three computational models a problem is shown in.
 *
 * A student selects a component within one of these, and a step expects its
 * answer in one of them. Component ids are only unique within a single
 * representation: the lambda calculus takes them from LC_EXPRESSION primary
 * keys while the mu-recursive view numbers its own, so the same integer names
 * different things in different panels. Naming the representation alongside
 * the component id is what keeps those apart.
 *
 * @author Andrew Bazen
 */
public enum RepresentationKind {
    TURING_MACHINE("Turing Machine"),

    LAMBDA_CALCULUS("Lambda Calculus"),

    MU_RECURSIVE("Mu-Recursive Function");

    /**
     * A human readable name for this representation.
     */
    private final String title;

    RepresentationKind(String title) {
        this.title = title;
    }

    /**
     * Return a human readable name for this representation.
     *
     * @return a String
     */
    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
