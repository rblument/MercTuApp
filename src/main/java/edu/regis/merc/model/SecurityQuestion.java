package edu.regis.merc.model;

/** Shared question catalog. IDs are persisted in Account and must remain stable. */
public enum SecurityQuestion {
    BIRTH_CITY(0, "What city were you born in?"),
    MOTHERS_MAIDEN_NAME(1, "What is your mother's maiden name?"),
    FIRST_CAR_MAKE(2, "What was the make of your first car?"),
    STREET_AT_TEN(3, "What street did you live on when you were 10 years old?");

    private final int id;
    private final String text;

    SecurityQuestion(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public static SecurityQuestion fromId(int id) {
        for (SecurityQuestion question : values()) {
            if (question.id == id) {
                return question;
            }
        }
        throw new IllegalArgumentException("Unknown security question ID");
    }

    @Override
    public String toString() {
        return text;
    }
}
