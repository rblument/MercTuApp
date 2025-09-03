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
package edu.regis.merc.util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * An implementation of the SHA-256 algorithm.
 *
 * See sha256(String). For example sha256("Regis Computer Science Rocks!")
 * returns fddfe0c1671993dbe8da88ccfbdf8aae3ae255d41b2808ff86041cca4cff65e5
 *
 * @author rickb
 */
public class SHA_256 {

    /**
     * The singleton instance of this frame.
     */
    private final static SHA_256 SINGLETON;


    // Invoked when this class is loaded
    static {
        SINGLETON = new SHA_256();
    }

    /**
     * Return the singleton instance of this algorithm.
     *
     * @return the SHA_256 singleton
     */
    public static SHA_256 instance() {
        return SINGLETON;
    }

    /**
     * If this is true, our listeners are notified when using this SHA-256
     * algorithm, otherwise they are not notified.
     *
     * A false setting allows one to encrypt a string using SHA-256 outside of
     * using the ShaTu tutor, so to speak. For example, if we want to simply
     * encrypt a student user's password, which doesn't require any interactions
     * with the actual ShatTu tutor since no tutoring is taking place.
     */
    private boolean isSendCallbacks = true;


    // The above fields are part of the ShaTu tutor, 
    // the following fields are part of the SHA-256 algorithm.
    /**
     * The constants (in hex) defined in the SHA-256 specification.
     */
    private static final int[] K = {
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
    };

    /**
     * The initial working variable constants defined in the SHA-256
     * specification.
     */
    private static final int[] H0 = {
        0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    };

    private static final int BLOCK_BITS = 512;

    private static final int BLOCK_BYTES = BLOCK_BITS / 8;

    /**
     * Variables for holding values for labels in CompressionCanvasView
     */
    private final int[] w = new int[64];
    private final int[] h = new int[8];
    private final int[] temp = new int[8];
    private final int[] inTemp = new int[8];
    private int counter = 0;
    private int majority = 0;
    private int choice = 0;
    private int bigSig0Val = 0;
    private int bigSig1Val = 0;
    private int Kt = 0;
    private int Wt = 0;
    private int t1 = 0;
    private int t2 = 0;
    private int mod2 = 0;
    private int mod3 = 0;

    /**
     * Initialize this algorithm with an empty set of SHA-256 listeners.
     */
    private SHA_256() {
    }

    public boolean isSendCallbacks() {
        return isSendCallbacks;
    }

    /**
     * Assign the value of the isIgnoreCallbacks field (see the documentation
     * for this field).
     *
     * @param isSendCallbacks true, sending updates to our listeners, false stop
     * notifying our listeners
     */
    public void setIsIgnoreCallbacks(boolean isSendCallbacks) {
        this.isSendCallbacks = isSendCallbacks;
    }


    /**
     * Create a SHA-256 digest of the given message.
     *
     * @param msg
     */
    public String sha256(String msg) {
        Charset charset = Charset.forName("ASCII");

        byte[] asciiEncodeMsg = msg.getBytes(charset);


        byte[] digest = hash(asciiEncodeMsg);

        String digestStr = bytesToHex(digest);

        return digestStr;
    }

    /**
     * Takes a string in hexidecimal and returns a binary string representation
     * 
     * @param hexString
     */
    private static String hexToBin(String hexString) {
        StringBuffer buffer = new StringBuffer();
        for (int pos = 0; pos < hexString.length(); pos++) {
            switch (hexString.charAt(pos)) {
                case '0':
                    buffer.append("0000");
                    break;

                case '1':
                    buffer.append("0001");
                    break;

                case '2':
                    buffer.append("0010");
                    break;

                case '3':
                    buffer.append("0011");
                    break;

                case '4':
                    buffer.append("0100");
                    break;

                case '5':
                    buffer.append("0101");
                    break;

                case '6':
                    buffer.append("0110");
                    break;

                case '7':
                    buffer.append("0111");
                    break;

                case '8':
                    buffer.append("1000");
                    break;

                case '9':
                    buffer.append("1001");
                    break;

                case 'A':
                case 'a':
                    buffer.append("1010");
                    break;

                case 'B':
                case 'b':
                    buffer.append("1011");
                    break;

                case 'C':
                case 'c':
                    buffer.append("1100");
                    break;

                case 'D':
                case 'd':
                    buffer.append("1101");
                    break;

                case 'E':
                case 'e':
                    buffer.append("1110");
                    break;

                default: // 'F'
                    buffer.append("1111");
                    break;

            }
        }

        return buffer.toString();
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Hashes the given message with SHA-256 and returns the hash.
     *
     * @param message The bytes to hash.
     * @return The hash's bytes.
     */
    public byte[] hash(byte[] message) {
        int [] words = initializeMessage(message);

        if (isSendCallbacks){
            
            // enumerate all blocks (each containing 16 words -- uses method)

            for (int i = 0, n = words.length / 16; i < n; ++i) {
                enumerateMessageBlocks (i, words);
                
        // use method to operate on temp and do compression rounds        
                for (int t = 0; t < w.length; ++t) {
                    compressionRound (t);
                }
         // add values in TEMP to values in H        
                nextMessageBlockHValue ();
            }
        }
        
        return toByteArray(h); 
    }
    
    /**
     * Prepares the message for hashing by creating all words, padding with a zero 
     * bit and adding the 64 bit length of the original message.
     *
     * @param message The bytes to hash.
     * @return The int array of words that make up the message.
     */
    
    public int [] initializeMessage(byte [] message){
        //let H = H0
        System.arraycopy(H0, 0, h, 0, H0.length);

        // initialize all words
        int initWords [] = pad(message);  
        
        return initWords;
    } 
    
     /**
     * Completes message schedule prepartion by creating the array of words for 
     * a single block (64 words) to be hashed.  
     * Appends additional zero bit words until total of 64 32-bit words in block 
     * then adds length of original message to end of block .
     *
     * @param m The block number being enumerated.
     * @param words Array of words created from original message by initializeMessage 
     *        method
     */
    public void enumerateMessageBlocks (int m, int[] words){
        // initialize w from the block's words
            System.arraycopy(words, m * 16, w, 0, 16);
         
            // Modify the zero-ed indexes at the end of the array using the following algorithm:
            for (int t = 16; t < w.length; ++t) {
                 w[t] = smallSig1(w[t - 2]) + w[t - 7] + smallSig0(w[t - 15]) + w[t - 16];
            }
            
            //create copy of array holding initial values for working variables
            
            System.arraycopy(h, 0, temp, 0, h.length);

    }
    
    /**
     * Performs one round of compression for the given message block.  Copies new 
     * inputs given by calculated outputs into a temporary array used in round 
     * compression round 1-63.
     * @param cr The number of the compression round (0-63).
     * 
     */
    public void compressionRound (int cr){
        //     Choice function (if/then/else function using three variables)
        //        =  H              E        F        G
                choice = ch(temp[4], temp[5], temp[6]); 
        //                        A         B       C
                majority = maj(temp[0], temp[1], temp[2]);
        //                                A
                bigSig0Val = bigSig0(temp[0]);
        //                                E        
                bigSig1Val = bigSig1(temp[4]);
                Kt = K[cr];
                Wt = w[cr];
                mod3 = Wt + Kt;
                mod2 = temp[7] + choice + mod3;
                
        //                    E                 D
                t1 = temp[7] + bigSig1Val + choice + Kt + Wt;
                
                //                               
                t2 = bigSig0Val + majority;
                
                // Rick
                // if (t == 0) {
                //    System.out.println("Maj: " + padLeftZeros(Integer.toBinaryString(maj(TEMP[0], TEMP[1], TEMP[2])), 32));
               // System.out.println("Sig0: " + padLeftZeros(Integer.toBinaryString(bigSig0(TEMP[0])), 32));
                
                // }
                // end Rick
                
                //arrays to hold orinal values of working variables and new values 
                //after compression round is finished 
                System.arraycopy(temp, 0, inTemp, 0, temp.length);
                System.arraycopy(temp, 0, temp, 1, temp.length - 1);
                // E
                temp[4] += t1;
                temp[0] = t1 + t2;
    }
    
    /**
     * Adds each outgoing variable value from last compression round to the initial
     * incoming variables from compression round 0 of the message block to prepare
     * for compression of next message block (if there is one).
     * 
     */
    public void nextMessageBlockHValue (){
     // add values in TEMP to values in H
        for (int t = 0; t < h.length; ++t) {
            h[t] += temp[t];
        }
    }

      /**
     * <b>Internal method, no need to call.</b> Pads the given message to have a length
     * that is a multiple of 512 bits (64 bytes), including the addition of a
     * 1-bit, k 0-bits, and the message length as a 64-bit integer.
     * The result is a 32-bit integer array with big-endian byte representation.
     *
     * @param message The message to pad.
     * @return A new array with the padded message bytes.
     */
    public  int[] pad(byte[] message) {
        // new message length: original + 1-bit and padding + 8-byte length
        // --> block count: whole blocks + (padding + length rounded up)
        int finalBlockLength = message.length % BLOCK_BYTES;
        int blockCount = message.length / BLOCK_BYTES + (finalBlockLength + 1 + 8 > BLOCK_BYTES ? 2 : 1);

        final IntBuffer result = IntBuffer.allocate(blockCount * (BLOCK_BYTES / Integer.BYTES));

        // copy as much of the message as possible
        ByteBuffer buf = ByteBuffer.wrap(message);
        for (int i = 0, n = message.length / Integer.BYTES; i < n; ++i) {
            result.put(buf.getInt());
        }
        // copy the remaining bytes (less than 4) and append 1 bit (rest is zero)
        ByteBuffer remainder = ByteBuffer.allocate(4);
        remainder.put(buf).put((byte) 0b10000000).rewind();
        result.put(remainder.getInt());

        // ignore however many pad bytes (implicitly calculated in the beginning)
        result.position(result.capacity() - 2);
        // place original message length as 64-bit integer at the end
        long msgLength = message.length * 8L;
        result.put((int) (msgLength >>> 32));
        result.put((int) msgLength);

        return result.array();
    }

    /**
     * Converts the given int array into a byte array via big-endian conversion
     * (1 int becomes 4 bytes).
     *
     * @param ints The source array.
     * @return The converted array.
     */
    private byte[] toByteArray(int[] ints) {
        ByteBuffer buf = ByteBuffer.allocate(ints.length * Integer.BYTES);
        for (int i : ints) {
            buf.putInt(i);
        }
        return buf.array();
    }

    /**
     * Performs the choice function (bitwise if/then/else) for the compression 
     * round 
     * @param x value of working variable e (temp[4]).
     * @param y value of working variable f (temp[5]).
     * @param x value of working variable g (temp[6]).
     * @return single 32 bit int from the bitwise if/then/else addition
     * 
     */
    private int ch(int x, int y, int z) {
        return (x & y) | ((~x) & z);
    }

    /**
     * Performs the majority function.
     * Outputs the majority of the input bits
     * If two or more input bits are zero, then zero is output.
     * Else (two or more bits are ones) output a one
     * @param x value of working variable a (temp[0]).
     * @param y value of working variable b (temp[1]).
     * @param z value of working variable c (temp[2]).
     * @return single 32 bit int.
     */
    private int maj(int x, int y, int z) {
        return (x & y) | (x & z) | (y & z);
    }

    /**
     * Performs the Sigma 0 function.
     * Takes variable a and performs 3 rotate right functions to get three values
     * to use in modulo addition. (ROTR^2, ROTR^13, ROTR^22
     * @param x value of working variable a (temp[0]).
     * @return single 32 bit int.
     */
    private int bigSig0(int x) {
        return Integer.rotateRight(x, 2)
                ^ Integer.rotateRight(x, 13)
                ^ Integer.rotateRight(x, 22);
    }

    /**
     * Performs the Sigma 1 function.
     * Takes working variable e and performs 3 rotate right functions to get 
     * three values to use in modulo addition. (ROTR^6, ROTR^11, ROTR^25)
     * @param x value of working variable e (temp[4]).
     * @return single 32 bit int.
     */
    private int bigSig1(int x) {
        return Integer.rotateRight(x, 6)
                ^ Integer.rotateRight(x, 11)
                ^ Integer.rotateRight(x, 25);
    }

    /**
     * Performs the Small Sigma 0 function during message preparation.
     * Takes second word in message block and performs 3 rotate right functions 
     * to get three values to use in modulo addition. (ROTR^7, ROTR^18, ROTR^3)
     * @param x value of second word in message block (w[2]).
     * @return single 32 bit int.
     */
    private int smallSig0(int x) {
        return Integer.rotateRight(x, 7)
                ^ Integer.rotateRight(x, 18)
                ^ (x >>> 3);
    }

    /**
     * Performs the Small Sigma 1 function during message preparation.
     * Takes fifteenth word in message block and performs 3 rotate right functions 
     * to get three values to use in modulo addition. (ROTR^17, ROTR^19, ROTR^14)
     * @param x value of fifteenth word in message block (w[14]).
     * @return single 32 bit int.
     */
    private int smallSig1(int x) {
        return Integer.rotateRight(x, 17)
                ^ Integer.rotateRight(x, 19)
                ^ (x >>> 10);
    }

    /**
     * Returns the value of the given working variable at the end of the 
     * compression round in binary form
     * @param value value for index in array holding working variables.
     * @return Binary string representation of value.
     */
    public String getTempOutValue(int value) {
        return Integer.toBinaryString(temp[value]);
    }

    /**
     * Returns the value of the given working variable at the beginning of the 
     * compression round in binary form
     * @param value value for index in array holding working variables.
     * @return Binary string representation of value.
     */
    public String getInTempValue(int value) {
        return Integer.toBinaryString(inTemp[value]);
    }

    /**
     * Returns the final value of the choice function in binary form
     * @return Binary string representation of value.
     */
    public String getChoice() {
        return Integer.toBinaryString(choice);
    }

    /**
     * Returns the final value of the majority function in binary form
     * @return Binary string representation of value.
     */
    public String getMajority() {
        return Integer.toBinaryString(majority);
    }

    /**
     * Returns the final value of the Sigma 0 function from compression round in 
     * binary form
     * @return Binary string representation of value.
     */
    public String getBigSig0Val() {
        return Integer.toBinaryString(bigSig0Val);
    }

    /**
     * Returns the final value of the Sigma 1 function from compression round in 
     * binary form
     * @return Binary string representation of value.
     */
    public String getBigSig1Val() {
        return Integer.toBinaryString(bigSig1Val);
    }

    /**
     * Returns the length of the array holding the incoming working variables
     * @return an integer representation of the number of indicies in the array.
     */
    public int getTempLength() {
        return temp.length;
    }
    
    /**
     * Returns the constant value of variable K for a given round
     * @return Binary string representation of value.
     */
    public String getKt() {
        return Integer.toBinaryString(Kt);
    }

    /**
     * Returns the message schedule value in word array for a given round
     * @return Binary string representation of value.
     */
    public String getWt() {
        return Integer.toBinaryString(Wt);
    }

    /**
     * Returns the final value of the T1 variable from a given compression round 
     * in binary form
     * @return Binary string representation of value.
     */
    public String getT1() {
        return Integer.toBinaryString(t1);
    }

    /**
     * Returns the final value of the T2 variable from a given compression round 
     * in binary form
     * @return Binary string representation of value.
     */
    public String getT2() {
        return Integer.toBinaryString(t2);
    }

    /**
     * Returns the final value of variable Mod2 in binary form
     * @return Binary string representation of value.
     */
    public String getMod2(){
        return Integer.toBinaryString(mod2);
    }
    
    /**
     * Returns the final value of variable Mod3 in binary form
     * @return Binary string representation of value.
     */
    public String getMod3(){
        return Integer.toBinaryString(mod3);
    }
    
    // Rickb
    public String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }
}

