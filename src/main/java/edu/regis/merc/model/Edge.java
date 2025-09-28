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

import edu.regis.merc.model.Node;
import edu.regis.merc.model.Model;

/**
 * A directed binary precedence relation between nodes.
 *
 * @author Rickb
 */
public class Edge extends GraphicalComponent {
    /**
     * The previous (from) node in this edge.
     */
    protected Node tail;

    /**
     * The next (to) node in this edge.
     */
    protected Node head;

    /**
     * The type of this branch: "next", "yes, "no" (later for decisions).
     */
    protected String type;


    public Edge() {
        this(DEFAULT_ID);
    }
    
    public Edge(int id) {
        this(id, null, null);
    }

    /**
     * Construct this edge
     * 
     * @param id
     * @param tail
     * @param head
     */
    public Edge(int id, Node tail, Node head) {
	super(id);

	this.tail = tail;
	this.head = head;
    }

    /**
     * Accessor that returns this edge's tail (previous/from) node.
     */
    public Node getTail() {
	return tail;
    }

    /**
     * Mutator that assigns this edge's tail (previous/from) node.
     */
    public void setTail(Node tail) {
	this.tail = tail;
    }

    /**
     * Accessor that returns this edge's head (next/to) node.
     */
    public Node getHead() {
	return head;
    }

    /**
     * Mutator that assigns this edge's head (next/to) node.
     */
    public void setHead(Node head) {
	this.head = head;
    }

    public String getType() {
	return type;
    }
}

