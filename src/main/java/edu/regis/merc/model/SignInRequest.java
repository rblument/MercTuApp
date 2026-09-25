package edu.regis.merc.model;

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
public class SignInRequest {
    /**
     * The user's login id with the format: name@university.edu.
     */
    protected String userId;

    /**
     * The unencrypted/unhashed password of the user.
     */
    protected String password;

    /**
     * Default constructor for creating an empty SignInRequest.
     */
    public SignInRequest() {}

    /**
     * Constructor for a fully formed SignInRequest.
     * @param userId The user's login id.
     * @param password The user's unecnrypted/unhashed password.
     */
    public SignInRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
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
     * Return this user's password.
     *
     * @return the unencrypted/unhashed passowrd
     */
    public String getPassword() {
        return password;
    }

    /**
     * Assign this user's password.
     *
     * @param password an unencrypted/unhashed String
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
