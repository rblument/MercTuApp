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

import java.math.BigDecimal;
import java.util.Date;

/**
 * Model for a result row (table: Results)
 */
public class Result {
    private int id; // ResultId
    private int sessionId;
    private String userId;
    private int courseId;
    private int unitId;
    private Integer knowledgeComponentId; // nullable
    private BigDecimal score; // DECIMAL(5,2)
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private Date timestamp;

    public Result() {
        this.id = Model.DEFAULT_ID;
        this.knowledgeComponentId = null;
        this.score = BigDecimal.ZERO;
        this.timestamp = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public Integer getKnowledgeComponentId() {
        return knowledgeComponentId;
    }

    public void setKnowledgeComponentId(Integer knowledgeComponentId) {
        this.knowledgeComponentId = knowledgeComponentId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(int incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}

