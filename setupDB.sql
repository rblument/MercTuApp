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
-- CREATE USER 'MercTuTs'@'localhost' IDENTIFIED BY 'MercTu2025';

-- Give the ShaTu tutor the following priveledges.
GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP ON MercTuDB.* TO 'MercTuTs'@'localhost';

-- Add tables to new database
USE MercTuDB;

CREATE TABLE Account (
   UserId VARCHAR(256),
   Password VARCHAR(256) NOT NULL,
   FirstName VARCHAR(256),
   LastName VARCHAR(256),
   Question int,
   Answer VARCHAR(256),
   IsStudent tinyint DEFAULT 0,
   PRIMARY KEY (UserId)
);

CREATE TABLE TutoringSession (
    Id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    UserId varchar(256) NOT NULL,
    SecurityToken varchar(256) NOT NULL,
    IsActive tinyint DEFAULT 0,
    StartDate TIMESTAMP NOT NULL,
    CourseId int NOT NULL,
    UnitId int NOT NULL
);

CREATE TABLE PendingTask (
  SessionId int NOT NULL,
  TaskId int NOT NULL,
  PendingStepId int NOT NULL,
  PRIMARY KEY (SessionId)
);

CREATE TABLE PendingStep (
  Id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  SessionId int NOT NULL,
  StepId int NOT NULL,
  NotifyTutor tinyint DEFAULT 0,
  IsCompleted tinyint DEFAULT 0,
  CurrentHintIndex int NOT NULL
);

CREATE TABLE Student (
   UserId VARCHAR(255),
   FirstName VARCHAR(30) NOT NULL,
   LastName VARCHAR(255) NOT NULL
);

CREATE TABLE StudentModel (
   UserId VARCHAR(255) NOT NULL PRIMARY KEY,
   ScaffoldLevel VARCHAR(16) NOT NULL
);

CREATE TABLE Course (
  Id int NOT NULL DEFAULT '0',
  Title varchar(255) DEFAULT NULL,
  PrimaryPedagogy ENUM(
      'STUDENT_CHOICE',
      'FIXED_SEQUENCE',
      'MASTERY_LEARNING',
      'MICROADAPTATION',
      'OTHER',
      'ERROR'
   ),
  Description varchar(255) DEFAULT NULL,
  PRIMARY KEY (Id));

CREATE TABLE Unit (
  UnitId int NOT NULL DEFAULT '0',
  CourseId int DEFAULT NULL,
  Title varchar(255) DEFAULT NULL,
  Description varchar(255) DEFAULT NULL,
  SequenceIndex int DEFAULT NULL,
     Pedagogy ENUM(
      'STUDENT_CHOICE',
      'FIXED_SEQUENCE',
      'MASTERY_LEARNING',
      'MICROADAPTATION',
      'OTHER',
      'ERROR'
   ),
  PRIMARY KEY (UnitId));

# A static description of a task to complete within a course unit.
CREATE TABLE Task (
  TaskId int NOT NULL DEFAULT 0,
  CourseId int NOT NULL DEFAULT 1,
  UnitId int DEFAULT NULL,
  Title varchar(256) NOT NULL,
  Description varchar(256) NOT NULL,
  Kind ENUM(
      'MESSAGE',
      'PROBLEM',
      'USAGE',
      'ERROR'
   ),
  SequenceIndex int,
  ExampleType ENUM(
      'MESSAGE',
      'PROBLEM',
      'USAGE',
      'N_A',
      'ERROR'
   ),
  ProblemId int,
  PRIMARY KEY (TaskId));

CREATE TABLE Step (
  Id int NOT NULL,
  CourseId int NOT NULL,
  UnitId int NOT NULL,
  TaskId int NOT NULL,
  Title varchar(256) DEFAULT NULL,
  Description varchar(256) DEFAULT NULL,
  SequenceIndex int DEFAULT NULL,
  ExercisedComponentId int,
  StepSubType varchar(256) DEFAULT NULL,
  SubTypeId int,
  TimeoutId int,
  PRIMARY KEY (Id));

CREATE TABLE InfoMsgStep (
  SubStepId int NOT NULL,
  Text varchar(4096),
  PRIMARY KEY(SubStepId));

CREATE TABLE Timeout (
  id int NOT NULL,
  TimeoutType varchar(256),
  Seconds int,
  Event varchar(256),
  Msg varchar(4096),
  PRIMARY KEY(id));

CREATE TABLE KnowledgeComponent (
  Id int NOT NULL ,
  CourseId int NOT NULL,
  Title varchar(256) NOT NULL,
  Description varchar(255),
  BloomLevel varchar(256) NOT NULL,
  IsDomainFocus tinyint,
  Pedagogy varchar(256),
  ExercisingLocations varchar(256),
  Granularity varchar(256),
  PRIMARY KEY (Id));

CREATE TABLE ExercisingLocation (
  Id int NOT NULL,
  CourseId int,
  UnitId int,
  TaskId int,
  StepId int,
  PRIMARY KEY (Id));

CREATE TABLE Hint (
  Id int NOT NULL DEFAULT '0',
  StepId int NOT NULL,
  Text varchar(256) DEFAULT NULL,
  SequenceIndex int DEFAULT NULL,
  PRIMARY KEY (Id));

CREATE TABLE Assessment (
   Id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
   UserId VARCHAR(256) NOT NULL,
   KnowledgeComponentId INT NOT NULL,
   AssessmentLevel VARCHAR(32) NOT NULL,
   Exposures INT,
   Successes INT,
   Hints INT
);


CREATE TABLE State (
    state_id INT AUTO_INCREMENT PRIMARY KEY,
    machine_id INT NOT NULL,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE MuFunction(
  Id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  Name VARCHAR(64),
  Lhs VARCHAR(255),
  Rhs VARCHAR(255)
);

-- Truncate Table Assessment;
-- Will delete data, but also reset the next id counter to zero

CREATE TABLE TuringMachine (
    machine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description varchar(256) NOT NULL,
    start_state_id INT,
    accept_state_id INT,
    reject_state_id INT,
    CONSTRAINT fk_start FOREIGN KEY (start_state_id) REFERENCES State(state_id),
    CONSTRAINT fk_accept FOREIGN KEY (accept_state_id) REFERENCES State(state_id),
    CONSTRAINT fk_reject FOREIGN KEY (reject_state_id) REFERENCES State(state_id)
);


CREATE TABLE alphabets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    machine_id INT,
    type VARCHAR(50), -- 'input' or 'tape'
    FOREIGN KEY (machine_id) REFERENCES turingmachine(machine_id) ON DELETE CASCADE
);

CREATE TABLE alphabet_symbols (
    alphabet_id INT,
    symbol CHAR(1),
    PRIMARY KEY (alphabet_id, symbol),
    FOREIGN KEY (alphabet_id) REFERENCES alphabets(id) ON DELETE CASCADE
);

 Truncate Table Assessment;
-- Will delete data, but also reset the next id counter to zero

 --Populate tables

INSERT INTO Course
(Id,
 Title,
 PrimaryPedagogy,
 Description)
VALUES
(1,'Multiple External Representation of Computing Tutor', 'FIXED_SEQUENCE', 
  'Familiarizes students with the Turing Machines, Mu-Recursive Functions, 
     Lambda Calculus, and the relations among these computational models.');


INSERT INTO Unit
(UnitId,
CourseId,
Title,
Description,
SequenceIndex,
Pedagogy)
VALUES
(0, 1, 'MERC: See One', 
  'In this unit, the student will see an example of how each computational model.', 
    0, 'FIXED_SEQUENCE');


INSERT INTO Task
(TaskId, CourseId, UnitId, Title, Description, Kind, SequenceIndex,
ExampleType, ProblemId)
VALUES
(0, 1, 0, 'Welcome', 'Let''s get started', 'USAGE', 0, 'N_A', -1);


INSERT INTO Step
(Id, CourseId, UnitId, TaskId, Title, Description, SequenceIndex, 
 ExercisedComponentId, StepSubType, SubTypeId, TimeoutId)
VALUES
(0, 1, 0, 0, 'Acknowledge Welcome', 
 'First Step in learning the tutor', 0, 0, 'Information Message', 0, 0);

INSERT INTO InfoMsgStep (SubStepId, Text) VALUES
 (0, 'Welcome, I''m Merc. I''ll begin by showing you how this
     application works.\n\n
     When I send you an information message, like this one, all you
     have to do is acknowledge it by pressing the ''Acknowledged'' button.');

INSERT INTO Timeout (id, TimeoutType, Seconds, Event, Msg) VALUES
 (0, 'Info Message', 60, 'Reminder', 'Please acknowledge the current information message to continue.');

INSERT INTO Hint
(Id, StepId, Text, SequenceIndex)
VALUES
(0, 0, 'Acknowledge this message by pressing the ''Acknowledged'' button.', 0);

INSERT INTO KnowledgeComponent
(Id, CourseId, Title,
 Description,
 BloomLevel, IsDomainFocus, Pedagogy, ExercisingLocations, Granularity)
VALUES
(0, 1, 'Information Message Acknowledgement', 
 'Student has appropriately demonstrated acknowleding information messages presented by the tutor.',
 'Application', 0, 'Other', '0', 'Knowledge Component');

INSERT INTO ExercisingLocation
(Id, CourseId, UnitId, TaskId, StepId)
VALUES (0,1,0,0,0);

INSERT INTO State
(state_id, machine_id, name)

VALUES (-1, 0, 'Q-1');

INSERT INTO State
(state_id, machine_id, name)

VALUES (0, 0, 'Q0');

INSERT INTO State
(state_id, machine_id, name)

VALUES (0, 0, 'Q1');

INSERT INTO State
(state_id, machine_id, name)

VALUES (0, 0, 'Q2');

INSERT INTO turingmachine
(machine_id, name, description, start_state_id, accept_state_id, reject_state_id)

values
(0, 'The Zero TM', 'The Zero Function', 1, 3, -1);




INSERT INTO MuFunction (Name, Lhs, Rhs)
VALUES ('add', 'add(x, y)', '(x + y)');

