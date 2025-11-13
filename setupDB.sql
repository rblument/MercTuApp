-- 
-- MERC^T: Multiple External Representations of Computation Tutor
-- 
--  (C) Richard Blumenthal, All rights reserved
-- 
--  Unauthorized use, duplication or distribution without the authors'
--  permission is strictly prohibited.
-- 
--   Unless required by applicable law or agreed to in writing, this
--  software is distributed on an "AS IS" basis without warranties
--   or conditions of any kind, either expressed or implied.
-- 

--
-- Author:  rickb
-- Created: Sep 3, 2025
--

-- If the ShaTuDB exists, drop it. In general, you will lose any existing data.
DROP DATABASE IF EXISTS MercTuDB;


-- Create a database in MySql named: MercTuDB
CREATE DATABASE MercTuDB;

-- Create user representing the DICE tutor.
# CREATE USER 'MercTuTs'@'localhost' IDENTIFIED BY 'MercTu2025';

-- Give the ShaTu tutor the following privileges.
GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP ON MercTuDB.* TO 'MercTuTs'@'localhost';

-- Add tables to new database
USE MercTuDB;

-- Table: Account
-- Purpose: Stores user accounts for the tutor application.
CREATE TABLE Account (
   UserId VARCHAR(50) PRIMARY KEY,
   Password VARCHAR(256) NOT NULL,
   FirstName VARCHAR(50) NOT NULL,
   LastName VARCHAR(50) NOT NULL,
   Question int,
   Answer VARCHAR(256),
   IsStudent BOOLEAN DEFAULT TRUE,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table: courses
-- Purpose: Course definitions
CREATE TABLE courses (
    courseId INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    pedagogy ENUM('STUDENT_CHOICE', 'FIXED_SEQUENCE', 'MASTERY_LEARNING', 'MICROADAPTATION', 'OTHER') NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: Unit
-- Purpose: Units within courses
CREATE TABLE Unit (
    unitId INT AUTO_INCREMENT PRIMARY KEY,
    courseId INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    sequenceOrder INT NOT NULL DEFAULT 1,
    pedagogy ENUM('STUDENT_CHOICE', 'FIXED_SEQUENCE', 'MASTERY_LEARNING', 'MICROADAPTATION', 'OTHER'),
    FOREIGN KEY (courseId) REFERENCES courses(courseId) ON DELETE CASCADE
);

-- Table: knowledge_components
-- Purpose: Skills/knowledge assessed by steps
CREATE TABLE knowledge_components (
    kcId INT AUTO_INCREMENT PRIMARY KEY,
    courseId INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    bloomLevel ENUM('REMEMBER', 'UNDERSTAND', 'APPLY', 'ANALYZE', 'EVALUATE', 'CREATE') NOT NULL,
    isCore BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (courseId) REFERENCES courses(courseId) ON DELETE CASCADE
);

-- Table: tasks
-- Purpose: Questions/problems within units
CREATE TABLE tasks (
    taskId INT AUTO_INCREMENT PRIMARY KEY,
    courseId INT NOT NULL,
    unitId INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    taskType ENUM('MESSAGE', 'PROBLEM', 'PRACTICE') NOT NULL,
    sequenceOrder INT NOT NULL DEFAULT 1,
    exampleType ENUM('MESSAGE', 'PROBLEM', 'PRACTICE', 'NONE') NOT NULL DEFAULT 'NONE',
    FOREIGN KEY (courseId) REFERENCES courses(courseId),
    FOREIGN KEY (unitId) REFERENCES Unit(unitId),
    UNIQUE KEY unique_unit_task (unitId, sequenceOrder)
);

-- Table: task_steps
-- Purpose: Individual steps within tasks
CREATE TABLE task_steps (
    stepId INT AUTO_INCREMENT PRIMARY KEY,
    courseId INT NOT NULL,
    unitId INT NOT NULL,
    taskId INT NOT NULL,
    title VARCHAR(200),
    description TEXT,
    sequenceOrder INT NOT NULL DEFAULT 1,
    kcId INT,
    stepSubtype VARCHAR(100),
    timeoutSeconds INT DEFAULT 300,
    defaultState JSON,
    correctState JSON,
    difficultyLevel INT DEFAULT 1,
    FOREIGN KEY (courseId) REFERENCES courses(courseId),
    FOREIGN KEY (unitId) REFERENCES Unit(unitId),
    FOREIGN KEY (taskId) REFERENCES tasks(taskId),
    FOREIGN KEY (kcId) REFERENCES knowledge_components(kcId),
    UNIQUE KEY unique_task_step (taskId, sequenceOrder)
);

-- Table: step_hints
-- Purpose: Progressive hints for each step
CREATE TABLE step_hints (
    hintId INT AUTO_INCREMENT PRIMARY KEY,
    stepId INT NOT NULL,
    hintText TEXT NOT NULL,
    sequenceOrder INT NOT NULL DEFAULT 1,
    FOREIGN KEY (stepId) REFERENCES task_steps(stepId) ON DELETE CASCADE,
    UNIQUE KEY unique_step_hint (stepId, sequenceOrder)
);

-- Table: step_timeouts
-- Purpose: Timeout configurations
CREATE TABLE step_timeouts (
    timeoutId INT AUTO_INCREMENT PRIMARY KEY,
    timeoutType VARCHAR(50) NOT NULL,
    durationSeconds INT NOT NULL,
    eventAction VARCHAR(50) NOT NULL,
    messageText TEXT,
    UNIQUE KEY unique_timeout_config (timeoutType, durationSeconds)
);

-- Table: tutoring_sessions
-- Purpose: Active tutoring sessions
CREATE TABLE tutoring_sessions (
    sessionId INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    courseId INT NOT NULL,
    unitId INT NOT NULL,
    authToken VARCHAR(255) NOT NULL UNIQUE,
    isActive BOOLEAN DEFAULT TRUE,
    startedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    endedAt TIMESTAMP NULL,
    timeSpentSeconds INT DEFAULT 0,
    pointsEarned INT DEFAULT 0,
    maxPoints INT DEFAULT 0,
    currentLevel INT DEFAULT 1,
    FOREIGN KEY (userId) REFERENCES Account(UserId),
    FOREIGN KEY (courseId) REFERENCES courses(courseId),
    FOREIGN KEY (unitId) REFERENCES Unit(unitId)
);

-- Table: pending_session_steps
-- Purpose: Current step being worked on in session
CREATE TABLE pending_session_steps (
    pendingStepId INT AUTO_INCREMENT PRIMARY KEY,
    sessionId INT NOT NULL,
    stepId INT NOT NULL,
    notifyTutor BOOLEAN DEFAULT FALSE,
    isCompleted BOOLEAN DEFAULT FALSE,
    currentHintIndex INT DEFAULT 0,
    studentState JSON,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sessionId) REFERENCES tutoring_sessions(sessionId),
    FOREIGN KEY (stepId) REFERENCES task_steps(stepId),
    UNIQUE KEY active_step_per_session (sessionId)
);

-- Table: session_step_progress
-- Purpose: Historical progress through all steps
CREATE TABLE session_step_progress (
    progressId INT AUTO_INCREMENT PRIMARY KEY,
    sessionId INT NOT NULL,
    stepId INT NOT NULL,
    taskId INT NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    attempts INT DEFAULT 0,
    correctAttempts INT DEFAULT 0,
    hintsUsed INT DEFAULT 0,
    timeSpentSeconds INT DEFAULT 0,
    completedAt TIMESTAMP NULL,
    FOREIGN KEY (sessionId) REFERENCES tutoring_sessions(sessionId),
    FOREIGN KEY (stepId) REFERENCES task_steps(stepId),
    FOREIGN KEY (taskId) REFERENCES tasks(taskId)
);

-- Table: student_adaptation_model
-- Purpose: Adaptive learning state per student
CREATE TABLE student_adaptation_model (
    userId VARCHAR(50) PRIMARY KEY,
    scaffoldLevel ENUM('MINIMAL', 'MEDIUM', 'FULL') DEFAULT 'MEDIUM',
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES Account(userId)
);

-- Table: kc_assessments
-- Purpose: Student performance per knowledge component
CREATE TABLE kc_assessments (
    assessmentId INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    kcId INT NOT NULL,
    proficiencyLevel ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL,
    exposures INT DEFAULT 0,
    successes INT DEFAULT 0,
    hintsUsed INT DEFAULT 0,
    totalQuestions INT DEFAULT 0,
    lastAssessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES Account(userId),
    FOREIGN KEY (kcId) REFERENCES knowledge_components(kcId),
    UNIQUE KEY unique_user_kc (userId, kcId)
);

-- Table: session_results
-- Purpose: Aggregate session outcomes
CREATE TABLE session_results (
    resultId INT AUTO_INCREMENT PRIMARY KEY,
    sessionId INT NOT NULL,
    userId VARCHAR(50) NOT NULL,
    courseId INT NOT NULL,
    unitId INT NOT NULL,
    score DECIMAL(5,2),
    totalQuestions INT DEFAULT 0,
    correctAnswers INT DEFAULT 0,
    kcId INT NULL,
    completedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sessionId) REFERENCES tutoring_sessions(sessionId),
    FOREIGN KEY (userId) REFERENCES Account(userId),
    FOREIGN KEY (courseId) REFERENCES courses(courseId),
    FOREIGN KEY (unitId) REFERENCES Unit(unitId),
    FOREIGN KEY (kcId) REFERENCES knowledge_components(kcId)
);

-- Table: global_statistics
-- Purpose: System-wide analytics (single row)
CREATE TABLE global_statistics (
    statsId INT PRIMARY KEY DEFAULT 1,
    avgScore DECIMAL(5,2) DEFAULT 0.00,
    avgSessionDuration INT DEFAULT 0,
    totalTimeSpent BIGINT DEFAULT 0,
    totalQuestionsAnswered INT DEFAULT 0,
    totalSessions INT DEFAULT 0,
    lastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Truncate Table Assessment;
-- Will delete data, but also reset the next id counter to zero

-- Populate tables

INSERT INTO courses
(courseId,
 title,
 pedagogy,
 description)
VALUES
(1,'Multiple External Representation of Computing Tutor', 'FIXED_SEQUENCE', 
  'Familiarizes students with the Turing Machines, Mu-Recursive Functions, 
     Lambda Calculus, and the relations among these computational models.');



INSERT INTO Unit
(unitId,
courseId,
title,
description,
sequenceOrder,
pedagogy)
VALUES
(0, 1, 'MERC: See One', 
  'In this unit, the student will see an example of how each computational model.', 
    0, 'FIXED_SEQUENCE');



INSERT INTO tasks
(taskId, courseId, unitId, title, description, taskType, sequenceOrder,
exampleType)
VALUES
(0, 1, 0, 'Welcome', 'Let''s get started', 'USAGE', 0, 'N_A');


INSERT INTO task_steps
(stepId, courseId, unitId, taskId, title, description, sequenceOrder,
 kcId, stepSubtype)
VALUES
(0, 1, 0, 0, 'Acknowledge Welcome', 
 'First Step in learning the tutor', 0, 0, 'Information Message');

-- Convert InfoMsgStep into a step_hints row (inferred stepId=0)
INSERT INTO step_hints (hintId, stepId, hintText, sequenceOrder) VALUES
 (1, 0, 'Welcome, I''m Merc. I''ll begin by showing you how this
     application works.\n\n
     When I send you an information message, like this one, all you
     have to do is acknowledge it by pressing the ''Acknowledged'' button.', 0);

INSERT INTO step_timeouts (timeoutId, timeoutType, durationSeconds, eventAction, messageText) VALUES
 (0, 'Info Message', 60, 'Reminder', 'Please acknowledge the current information message to continue.');

INSERT INTO step_hints
(hintId, stepId, hintText, sequenceOrder)
VALUES
(0, 0, 'Acknowledge this message by pressing the ''Acknowledged'' button.', 0);

INSERT INTO knowledge_components
(kcId, courseId, title,
 description,
 bloomLevel, isCore)
VALUES
(0, 1, 'Information Message Acknowledgement', 
 'Student has appropriately demonstrated acknowleding information messages presented by the tutor.',
 'Application', 0);

-- The original script inserted into an ExercisingLocation table which does not exist in the
-- updated schema; skipping that INSERT. If you want this data stored, tell me which
-- table/columns should receive it and I will add the mapping.
-- INSERT INTO ExercisingLocation
-- (Id, CourseId, UnitId, TaskId, StepId)
-- VALUES (0,1,0,0,0);
