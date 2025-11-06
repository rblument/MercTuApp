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
package edu.regis.merc.err;

/**
 * Thrown when the Lambda Calculus parser encounters illegal syntax.
 * 
 * See the message and if the wrapped cause is not null, it is an EtxException 
 * (indicating the end of the expression was reached before parsing finished).
 * 
 * @author rickb
 */
public class LCParseException extends MercException {
    /**
     * Initialize this exception with the given message.
     * 
     * @param msg a description of the error that occurred
     */
    public LCParseException(String msg) {
        super(msg);
    }
    
    /**
     * Initialize this exception with the given message.
     * 
     * @param msg
     * @param cause Presumable an ExtException
     */
    public LCParseException(String msg, Throwable cause) {
	super(msg, cause);
    }
}