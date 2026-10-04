package edu.regis.merc.model;

/** Minimal recovery payload: no password, question selection, or session data. */
public class RecoveryRequest {
    private String userId;
    private String securityAnswer;

    public RecoveryRequest(String userId, String securityAnswer) {
        this.userId = userId;
        this.securityAnswer = securityAnswer;
    }

    public String getUserId() { return userId; }
    public String getSecurityAnswer() { return securityAnswer; }
}
