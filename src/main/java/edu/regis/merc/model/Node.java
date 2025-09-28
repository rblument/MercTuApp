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

import edu.regis.merc.model.TitledModel;
import java.util.ArrayList;
/**
 * A generic node appearing in a context-dependent workflow
 *
 * @author rickb
 */
public class Node extends GraphicalComponent {
    /**
     *.
     */
    protected ArrayList<Edge> incomingEdges;
    
    protected ArrayList<Edge> outgoingEdges;

    /**
     * An edge pointing to the (next) node after this node, if any.
     */
    protected Edge next;
    
    public Node () {
        this(DEFAULT_ID);
    }

    public Node(int id) {
        super(id);

	this.guiCtx = new GuiCtx();
        incomingEdges = new ArrayList<>();
        outgoingEdges = new ArrayList<>();
    }

    public void addIncomingEdge(Edge edge) {
        incomingEdges.add(edge);
    }
    
    public void deleteIncomingEdge(Edge edge) {
        incomingEdges.remove(edge);
    }
    
    public ArrayList<Edge> getIncomingEdges() {
        return incomingEdges;
    }

    public void setIncomingEdges(ArrayList<Edge> incomingEdges) {
        this.incomingEdges = incomingEdges;
    }
    
    public void addOutgoingEdge(Edge edge) {
        outgoingEdges.add(edge);
    }
    
    public void deleteOutgoingEdge(Edge edge) {
        outgoingEdges.remove(edge);
    }

    public ArrayList<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public void setOutgoingEdges(ArrayList<Edge> outgoingEdges) {
        this.outgoingEdges = outgoingEdges;
    }

    /**
     * Accessor that returns this node's next edge.
     */
    public Edge getNext() {
	return next;
    }

    public String toString() {
	return "Node: " + getId();
    }
}

