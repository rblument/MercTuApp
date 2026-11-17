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
 * Thrown when the Mu function parser encounters illegal syntax..
 *
 * @author AndrewBazen
 */
public class MuParseException extends MercException {
    /**
     * Initialize this exception with the given message.
     *
     * @param msg a description of the error that occurred
     */
    public MuParseException(String msg) {
        super(msg);
    }

    /**
     * Initialize this exception with the given message.
     *
     * @param msg
     * @param cause Presumable an ExtException
     */
    public MuParseException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
