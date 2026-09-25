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
 * The sign-in credentials and basic information associated with a user.
 *
 * @author rickb
 */
public class Account {
    /**
     * The user's login id with the format: name@university.edu.
     */
    protected String userId;

    /**
     * The unhashed password of the account.
     */
    protected String password;

    /**
     * The PBKDF2-HMAC-SHA256 derived key for the user's password and the
     * salt on the account, hex-encoded.
     */
    protected transient String passwordHash;

    /**
     * A 16 byte salt for password hashing.
     */
    protected transient String salt;

    /**
     * The first name of this user for this account.
     */
    protected String firstName;
    
    /**
     * The last name of this user for this account.
     */
    protected String lastName;

    /**
     * The security question the user chose to answer.
     */
    protected int securityQuestion;

    /**
     * An SHA-256 hashed answer to the the security question.
     */
    protected String securityAnswer;

    /**
     * True, if this user is a student.
     */
    protected boolean isStudent;

    /**
     * Default constructor for the Account class. Sets up a new account with
     * blank/default values for all fields.
     */
    public Account() {
        this("", "", 0, ""); // Calls the detailed constructor with empty/default values for all fields.
    }

    /**
     * Constructor that takes only a userID.
     * Allows user to create an account only specifying the login ID.
     * The rest of the values are default
     * 
     * @param userId The user's login ID (e.g., "name@university.edu").
     */
    public Account(String userId) {
        this(userId, "", 0, "");
    }
    
    /**
    * Full constructor for creating an Account.
    * Sets up all the fields of the account with provided values.
    *
    * @param userId The user's login ID (e.g., "name@university.edu").
    * @param passwordHash The user's SHA-256 hashed password.
    * @param securityQuestion The ID of the security question selected by the user.
    * @param securityAnswer The user's SHA-256 encrypted answer to the security question.
    */
    public Account(String userId, String passwordHash, int securityQuestion,
            String securityAnswer) {
        this.userId = userId; 
        this.passwordHash = passwordHash;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        isStudent = true;
    }

    /**
     * Return this user's user id.
     *
     * @return a String with the format "name@university.edu"
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Assign this user's user id.
     *
     * @param userId String "name@university.edu"
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Return the salt for this user's password hash
     * @return the salt for password hashing
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Change the salt for this user's password.
     * @param salt The new salt for the user's password hash
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * Return this user's unhashed password.
     *
     * @return the raw password String
     */
    public String getPassword() {
        return password;
    }

    /**
     * Assign this user's password.
     *
     * @param password the raw password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Return this user's PBKDF2-HMAC-SHA256 derived key for the password + salt.
     *
     * @return a hex-encoded PBKDF2 derived key String
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Assign this user's PBKDF2-HMAC-SHA256 derived key for the raw password + salt.
     *
     * @param passwordHash a hex-encoded PBKDF2 derived key.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Return this StudentUser's first name.
     * @return the name String
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Assign this Student User's first name.
     * @param firstName the name String
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Return this Student User's last name
     * @return the name String
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * Assign this Student User's first name.
     * @param lastName the name String
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Return this user's security question.
     *
     * @return int representing the question the user answered
     */
    public int getSecurityQuestion() {
        return securityQuestion;
    }

    /**
     * Assign this user's security question.
     *
     * @param securityQuestion
     */
    public void setSecurityQuestion(int securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    /**
     * Return this user's security answer.
     *
     * @return a SHA-256 encrypted String
     */
    public String getSecurityAnswer() {
        return securityAnswer;
    }

    /**
     * Assign this user's security answer.
     *
     * @param securityAnswer
     */
    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    /**
     * Return whether this user is a student.
     *
     * @return true, this user is a student, false otherwise
     */
    public boolean isStudent() {
        return isStudent;
    }

    /**
     * Return whether this user is a student.
     *
     * @return true, this user is a student, false otherwise
     */
    public boolean getIsStudent() {
        return isStudent;
    }

    /**
     * Assign whether this user is a student.
     *
     * @param isStudent true, the user is a student.
     */
    public void setIsStudent(boolean isStudent) {
        this.isStudent = isStudent;
    }
    
    public void clear(){
        this.userId = null;
        this.password = null;
        this.passwordHash = null;
        this.salt = null;
        this.firstName = null;
        this.lastName = null;
        this.securityQuestion = 0;
        this.securityAnswer = null;
    }

    /**
     * Return the id and user id of this student.
     *
     * @return
     */
    @Override
    public String toString() {
        return "User: " + userId;
    }
}
