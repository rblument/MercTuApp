package edu.regis.merc.model;

/** Public recovery information; never includes the stored answer or token. */
public class SecurityQuestionPrompt {
    private int questionId;
    private String text;

    public SecurityQuestionPrompt(SecurityQuestion question) {
        questionId = question.getId();
        text = question.toString();
    }

    public int getQuestionId() { return questionId; }
    public String getText() { return text; }
}
