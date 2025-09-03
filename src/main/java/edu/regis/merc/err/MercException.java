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
 * Root of all checked MercApp exceptions.
 * 
 * As it may be possible for the user to recover from this exception, it is not
 * logged. NonRecoverable exceptions are logged.
 * @see edu.regis.err.NonRecoverableException
 * 
 * @author Rickb
 */
public abstract class MercException extends Exception {
    /**
     * Initialize this new instance with the given message.
     *
     * @param msg a string describing the cause of this exception.
     */
    public MercException(String msg) {
	super(msg);
    }

    /**
     * Initialize this new instance with the given message and the underlying
     * Java exception that caused this ShaTu exception.
     *
     * @param msg a string describing the cause of this exception.
     * @param cause the Java exception that caused this ShaTu exception.
     */
    public MercException(String msg, Throwable cause) {
	super(msg, cause);
    }
}
