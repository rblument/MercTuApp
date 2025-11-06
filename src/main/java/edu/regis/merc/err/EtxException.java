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
 * Thrown when a parser encounters the input expression (End of Text).
 * 
 * @author rickb
 */
public class EtxException extends MercException {
    /**
     * Initialize this exception with the given message.
     * 
     * @param msg a description of the error that occurred
     */
    public EtxException(String msg) {
        super(msg);
    }
}