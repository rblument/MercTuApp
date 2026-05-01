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

-- commented out for test 
-- Create user representing the DICE tutor.
-- CREATE USER 'MercTuTs'@'localhost' IDENTIFIED BY 'MercTu2025';
-- Give the ShaTu tutor the following priveledges.
-- GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP ON MercTuDB.* TO 'MercTuTs'@'localhost';
-- Add tables to new database
USE MercTuDB;

CREATE TABLE
  Account (
    UserId VARCHAR(256),
    Password VARCHAR(256) NOT NULL,
    FirstName VARCHAR(256),
    LastName VARCHAR(256),
    Question int,
    Answer VARCHAR(256),
    IsStudent tinyint DEFAULT 0,
    PRIMARY KEY (UserId)
  );

CREATE TABLE
  TutoringSession (
    Id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    UserId varchar(256) NOT NULL,
    SecurityToken varchar(256) NOT NULL,
    IsActive tinyint DEFAULT 0,
    StartDate TIMESTAMP NOT NULL,
    CourseId int NOT NULL,
    UnitId int NOT NULL,
    ProblemId int NOT NULL
  );

CREATE TABLE
  PendingTask (
    SessionId int NOT NULL,
    TaskId int NOT NULL,
    PendingStepId int NOT NULL,
    PRIMARY KEY (SessionId)
  );

CREATE TABLE
  PendingStep (
    Id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    SessionId int NOT NULL,
    StepId int NOT NULL,
    NotifyTutor tinyint DEFAULT 0,
    IsCompleted tinyint DEFAULT 0,
    CurrentHintIndex int NOT NULL
  );

CREATE TABLE
  Student (
    UserId VARCHAR(255),
    FirstName VARCHAR(30) NOT NULL,
    LastName VARCHAR(255) NOT NULL
  );

CREATE TABLE
  StudentModel (
    UserId VARCHAR(255) NOT NULL PRIMARY KEY,
    ScaffoldLevel VARCHAR(16) NOT NULL
  );

CREATE TABLE
  Course (
    Id int NOT NULL DEFAULT -1,
    Title varchar(255) DEFAULT NULL,
    Description varchar(255) DEFAULT NULL,
    PrimaryPedagogy ENUM(
      'STUDENT_CHOICE',
      'FIXED_SEQUENCE',
      'MASTERY_LEARNING',
      'MICROADAPTATION'
    ),
    PRIMARY KEY (Id)
  );

CREATE TABLE
  Unit (
    Id int NOT NULL DEFAULT '-1',
    CourseId int DEFAULT NULL,
    Title varchar(255) DEFAULT NULL,
    Description varchar(255) DEFAULT NULL,
    SequenceIndex int DEFAULT NULL,
    Pedagogy ENUM(
      'STUDENT_CHOICE',
      'FIXED_SEQUENCE',
      'MASTERY_LEARNING',
      'MICROADAPTATION'
    ),
    PRIMARY KEY (Id)
  );

CREATE TABLE
  Problem (
    Id INT NOT NULL DEFAULT -1,
    Title varchar(256) NOT NULL,
    Description varchar(256) NOT NULL,
    UnitId INT NOT NULL,
    SequenceIndex INT NOT NULL,
    TuringMachineId int,
    LambdaCalculusId int,
    MuRecursiveFunctionId int,
    PRIMARY KEY (Id)
  );

CREATE TABLE
  Task (
    Id int NOT NULL DEFAULT 0,
    ProblemId int NOT NULL DEFAULT 1,
    SequenceIndex int DEFAULT NULL,
    Title varchar(256) NOT NULL,
    Description varchar(256) NOT NULL,
    ExercisedComponentId int,
    PRIMARY KEY (Id)
  );

--   When StepType is MODEL_REQUET, the ActionId is an index (id) 
--     into the RequestConfiguration table
CREATE TABLE
  Step (
    Id int NOT NULL DEFAULT -1,
    TaskId INT NOT NULL DEFAULT -1,
    Title VARCHAR(256) DEFAULT '<unknown>',
    Description VARCHAR(256) DEFAULT '',
    Context VARCHAR(2056) DEFAULT '',
    Prompt VARCHAR(1024) DEFAULT '',
    Data TEXT,
    SequenceIndex int DEFAULT -1,
    ExercisedComponentId int,
    ViewConfigId INT NOT NULL,
    StudentAction ENUM(
      'INFORMATION_MESSAGE',
      'HINT_REQUEST',
      'MODEL_REQUEST'
    ),
    ActionId INT,
    TimeoutId int,
    PRIMARY KEY (Id)
  );

CREATE TABLE
  InfoMsgStep (
    SubStepId int NOT NULL,
    Text varchar(4096),
    PRIMARY KEY (SubStepId)
  );

CREATE TABLE
  ViewConfiguration (
    Id INT NOT NULL,
    TmViewConfigId INT,
    LCViewConfigId INT,
    MuViewConfigId INT,
    PRIMARY KEY (Id)
  );

CREATE TABLE
  TmViewConfiguration (
    Id INT NOT NULL,
    StateIds VARCHAR(256),
    TransitionIds VARCHAR(256),
    TapeCellIds VARCHAR(256),
    AcceptStateIndicatorIds VARCHAR(256),
    RejectStateIndicatorIds VARCHAR(256),
    DisplayStartIndicator TINYINT DEFAULT 0,
    DisplayTapeHead TINYINT DEFAULT 0,
    PRIMARY KEY (Id)
  );

--  No underscore characters allows since it's MySQL Wildcard
CREATE TABLE
  TapeConfiguration (
    Id INT NOT NULL,
    CellId INT NOT NULL,
    Content CHAR(1),
    PRIMARY KEY (Id, CellId)
  );

-- Highlights?
CREATE TABLE
  LCViewConfiguration (
    Id INT NOT NULL,
    ParameterIds VARCHAR(256),
    BodyIds VARCHAR(256),
    ArgumentIds VARCHAR(256),
    PRIMARY KEY (Id)
  );

-- Highlights? 
CREATE TABLE
  MuViewConfiguration (
    Id INT NOT NULL,
    HighlightName TINYINT DEFAULT 0,
    ParameterIds VARCHAR(256),
    RhsIds VARCHAR(256),
    ArgumentIds VARCHAR(256),
    PRIMARY KEY (Id)
  );

CREATE TABLE
  RequestConfiguration (
    Id INT NOT NULL,
    TmRequestConfigId INT,
    LCRequestConfigId INT,
    MuRequestConfigId INT,
    PRIMARY KEY (Id)
  );

CREATE TABLE
  TMDescription (
    Id INT NOT NULL,
    TuringMachineId INT,
    SubType ENUM(
      'DISPLAY_ALL',
      'DISPLAY_ACCEPT_STATE',
      'SELECT_ACCEPT_STATE',
      'DISPLAY_INITIAL_STATE',
      'SELECT_INITIAL_STATE',
      'DISPLAY_REJECT_STATE',
      'SELECT_REJECT_STATE',
      'DISPLAY_STATE',
      'SELECT_STATE',
      'DISPLAY_TRANSITION',
      'SELECT_TRANSITION',
      'HIGHLIGHT_TAPE_CELL',
      'SELECT_TAPE_CELL',
      'DISPLAY_CONFIGURATION',
      'SELECT_CONFIGURATION',
      'INPUT_ALPHABET',
      'TAPE_ALPHABET'
    ),
    ComponentId INT,
    DataId INT,
    PRIMARY KEY (Id)
  );

CREATE TABLE
  LCDescription (
    Id INT NOT NULL,
    LambdaCalculusId INT,
    SubType ENUM(
      'DISPLAY_ALL',
      'DISPLAY_EXPRESSION',
      'SELECT_EXPRESSION',
      'DISPLAY_ABSTRACTION',
      'SELECT_ABSTRACTION',
      'DISPLAY_PARAMETER',
      'SELECT_PARAMETER',
      'DISPLAY_BODY',
      'SELECT_BODY',
      'DISPLAY_APPLICATION',
      'SELECT_APPLICATION',
      'DISPLAY_ARG',
      'SELECT_ARG'
    ),
    ComponentId INT,
    DataId INT,
    PRIMARY KEY (Id)
  );

CREATE TABLE
  MUDescription (
    Id INT NOT NULL,
    MuRecursiveFunctionId INT,
    SubType ENUM(
      'DISPLAY_ALL',
      'DISPLAY_FUNCTION_NAME',
      'SELECT_FUNCTION_NAME',
      'DISPLAY_PARAMETER',
      'SELECT_PARAMETER',
      'DISPLAY_RHS_COMPONENT',
      'SELECT_RHS_COMPONENT'
    ),
    ComponentId INT,
    DataId INT,
    PRIMARY KEY (Id)
  );

CREATE TABLE
  Timeout (
    id int NOT NULL,
    TimeoutType varchar(256),
    Seconds int,
    Event varchar(256),
    Msg varchar(4096),
    PRIMARY KEY (id)
  );

CREATE TABLE
  KnowledgeComponent (
    Id int NOT NULL,
    CourseId int NOT NULL,
    Title varchar(256) NOT NULL,
    Description varchar(255),
    BloomLevel varchar(256) NOT NULL,
    IsDomainFocus tinyint,
    Pedagogy varchar(256),
    ExercisingLocations varchar(256),
    Granularity varchar(256),
    PRIMARY KEY (Id)
  );

CREATE TABLE
  ExercisingLocation (
    Id int NOT NULL,
    CourseId int,
    UnitId int,
    TaskId int,
    StepId int,
    PRIMARY KEY (Id)
  );

CREATE TABLE
  Hint (
    Id int NOT NULL DEFAULT '0',
    StepId int NOT NULL,
    Text varchar(256) DEFAULT NULL,
    SequenceIndex int DEFAULT NULL,
    PRIMARY KEY (Id)
  );

CREATE TABLE
  Assessment (
    Id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    UserId VARCHAR(256) NOT NULL,
    KnowledgeComponentId INT NOT NULL,
    AssessmentLevel VARCHAR(32) NOT NULL,
    Exposures INT,
    Successes INT,
    Hints INT
  );

CREATE TABLE
  MuFunction (
    Id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(64),
    Lhs VARCHAR(255),
    Rhs VARCHAR(255)
  );

-- Truncate Table Assessment;
-- Will delete data, but also reset the next id counter to zero
-- A state in a turing machine
CREATE TABLE
  TmState (
    Id INT PRIMARY KEY,
    TmId INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    GuiCtxId INT
  );

CREATE TABLE
  TmTransition (
    Id INT NOT NULL PRIMARY KEY,
    TmId INT NOT NULL,
    FromStateId INT,
    ToStateId INT,
    ReadSymbol CHAR(1),
    WriteSymbol CHAR(1),
    Direction ENUM('LEFT', 'RIGHT'),
    GuiCtxId INT
  );

CREATE TABLE
  GuiCtx (
    Id INT NOT NULL PRIMARY KEY,
    X INT,
    Y INT,
    Width INT,
    Height INT,
    X2 INT,
    Y2 INT
  );

CREATE TABLE
  TuringMachine (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description varchar(256) NOT NULL,
    StartStateId INT,
    AcceptStateId INT,
    RejectStateId INT
    -- CONSTRAINT fk_start FOREIGN KEY (start_state_id) REFERENCES State(state_id),
    -- CONSTRAINT fk_accept FOREIGN KEY (accept_state_id) REFERENCES State(state_id),
    -- CONSTRAINT fk_reject FOREIGN KEY (reject_state_id) REFERENCES State(state_id)
  );

CREATE TABLE
  Alphabet (
    Id INT PRIMARY KEY,
    TmId INT,
    Type ENUM('INPUT', 'TAPE')
    -- FOREIGN KEY (machine_id) REFERENCES turingmachine(machine_id) ON DELETE CASCADE
  );

CREATE TABLE
  AlphabetSymbol (
    id INT NOT NULL,
    alphabetId INT NOT NULL,
    symbol CHAR(1),
    --  PRIMARY KEY (alphabet_id, symbol),
    -- FOREIGN KEY (alphabet_id) REFERENCES alphabets(id) ON DELETE CASCADE
    PRIMARY KEY (id)
  );

--  Truncate Table Assessment;
-- Will delete data, but also reset the next id counter to zero
-- Populate tables
INSERT INTO
  Course (Id, Title, PrimaryPedagogy, Description)
VALUES
  (
    0,
    'Multiple External Representation of Computing Tutor',
    'FIXED_SEQUENCE',
    'Familiarizes students with the Turing Machine, Mu-Recursive Function and
     Lambda Calculus computational models, and the relations among these .'
  );

INSERT INTO
  Unit (
    Id,
    CourseId,
    Title,
    Description,
    SequenceIndex,
    Pedagogy
  )
VALUES
  (
    0,
    0,
    'MERC: See One',
    'In this unit, the student will see examples of each computational model and their components.',
    0,
    'FIXED_SEQUENCE'
  );

INSERT INTO
  Problem (
    Id,
    Title,
    Description,
    UnitId,
    SequenceIndex,
    TuringMachineId,
    LambdaCalculusId,
    MuRecursiveFunctionId
  )
VALUES
  (
    0,
    'Zero Function Overview',
    'In this problem, the student acknowledges seeing the various computational models and their components.',
    0,
    0,
    1,
    3,
    0
  );

INSERT INTO
  Task (
    Id,
    ProblemId,
    SequenceIndex,
    Title,
    Description,
    ExercisedComponentId
  )
VALUES
  (
    10,
    0,
    0,
    'Initial Zero Function Overview',
    'Displays each of the computational models for the zero function',
    -1
  );

INSERT INTO
  Step (
    Id,
    TaskId,
    SequenceIndex,
    Title,
    Description,
    Prompt,
    Context,
    Data,
    ExercisedComponentId,
    ViewConfigId,
    StudentAction,
    ActionId,
    TimeoutId
  )
VALUES
  (
    50,
    10,
    0,
    'Zero Function Outer Parameter',
    'Identifies the outer parameter of the zero function',
    'Identify the outer parameter.',
    'In the Zero Function \\s.\\z.z, select the variable that represents the first parameter.',
    '{"correctComponentId": 101}',
    -1,
    10,
    'INFORMATION_MESSAGE',
    -1,
    0
  ),
  (
    51,
    10,
    1,
    'Zero Function Inner Parameter',
    'Identifies the inner parameter of the zero function',
    'Identify the inner parameter.',
    'Now select the variable that represents the second parameter in the Zero function.',
    '{"correctComponentId": 102}',
    -1,
    10,
    'INFORMATION_MESSAGE',
    -1,
    0
  );

INSERT INTO
  ViewConfiguration (
    Id,
    TmViewConfigId,
    LCViewConfigId,
    MuViewConfigId
  )
VALUES
  (10, 10, 10, 10);

INSERT INTO
  TmViewConfiguration (
    Id,
    StateIds,
    TransitionIds,
    TapeCellIds,
    AcceptStateIndicatorIds,
    RejectStateIndicatorIds,
    DisplayStartIndicator,
    DisplayTapeHead
  )
VALUES
  (10, '0,1,2', '0,1,2', '', '2', '', 1, 1);

INSERT INTO
  TapeConfiguration (Id, CellId, Content)
VALUES
  (10, 0, '1');

INSERT INTO
  TapeConfiguration (Id, CellId, Content)
VALUES
  (10, 1, '0');

INSERT INTO
  LCViewConfiguration (Id, ParameterIds, BodyIds, ArgumentIds)
VALUES
  (10, '', '', '');

INSERT INTO
  MuViewConfiguration (
    Id,
    HighlightName,
    ParameterIds,
    RhsIds,
    ArgumentIds
  )
VALUES
  (10, 0, '', '', '');

INSERT INTO
  InfoMsgStep (SubStepId, Text)
VALUES
  (
    0,
    'Welcome, I''m Merc. I''ll begin by showing you how this
     application works.\n\n
     When I send you an information message, like this one, all you
     have to do is acknowledge it by pressing the ''Acknowledged'' button.'
  );

INSERT INTO
  Timeout (id, TimeoutType, Seconds, Event, Msg)
VALUES
  (
    0,
    'Info Message',
    60,
    'Reminder',
    'Please acknowledge the current information message to continue.'
  );

INSERT INTO
  Hint (Id, StepId, Text, SequenceIndex)
VALUES
  (
    0,
    0,
    'Acknowledge this message by pressing the ''Acknowledged'' button.',
    0
  ),
  (
    1,
    100,
    'Remember that each circle in a diagram represents a state.',
    0
  ),
  (
    2,
    101,
    'The initial state usually has an incoming arrow with no origin.',
    0
  ),
  (
    3,
    104,
    'Think of the tape as infinite memory that the machine can scroll through.',
    0
  );

INSERT INTO
  KnowledgeComponent (
    Id,
    CourseId,
    Title,
    Description,
    BloomLevel,
    IsDomainFocus,
    Pedagogy,
    ExercisingLocations,
    Granularity
  )
VALUES
  (
    0,
    0,
    'Information Message Acknowledgement',
    'Student has appropriately demonstrated acknowleding information messages presented by the tutor.',
    'Application',
    0,
    'Other',
    '0',
    'Knowledge Component'
  );

INSERT INTO
  ExercisingLocation (Id, CourseId, UnitId, TaskId, StepId)
VALUES
  (0, 0, 0, 0, 0);

INSERT INTO
  TmState (Id, TmId, Name, GuiCtxId)
VALUES
  (0, 1, 'Q0', 10),
  (1, 1, 'Q1', 11),
  (2, 1, 'Q2', 12);

INSERT INTO
  TmTransition (
    Id,
    TmId,
    FromStateId,
    ToStateId,
    ReadSymbol,
    WriteSymbol,
    Direction,
    GuiCtxId
  )
VALUES
  (0, 1, 0, 0, '1', '-', 'RIGHT', 20),
  (1, 1, 0, 1, '-', '0', 'RIGHT', 21),
  (2, 1, 1, 2, '-', '-', 'LEFT', 22);

INSERT INTO
  Alphabet (
    Id,
    TmId,
    Type
  )
VALUES
  (0, 1, 'INPUT'),
  (1, 1, 'TAPE');

INSERT INTO
  AlphabetSymbol (Id, AlphabetId, Symbol)
VALUES
  (0, 0, '-'),
  (1, 0, '0'),
  (2, 0, '1');

-- TM: 0, State 0
INSERT INTO
  GuiCtx (Id, X, Y, Width, Height, X2, Y2)
VALUES
  (10, 200, 100, 30, 30, -1, -1);

-- TM: 0, State 1
INSERT INTO
  GuiCtx (Id, X, Y, Width, Height, X2, Y2)
VALUES
  (11, 300, 100, 30, 30, -1, -1);

-- TM: 0, State 2
INSERT INTO
  GuiCtx (Id, X, Y, Width, Height, X2, Y2)
VALUES
  (12, 400, 100, 30, 30, -1, -1);

-- TM: 0, Transition 1
INSERT INTO
  GuiCtx (Id, X, Y, Width, Height, X2, Y2)
VALUES
  (20, 230, 115, 30, 30, 300, 115);

-- TM: 0, Transition 2
INSERT INTO
  GuiCtx (Id, X, Y, Width, Height, X2, Y2)
VALUES
  (21, 330, 115, 30, 30, 400, 115);

-- TM: 0, Transition 3
INSERT INTO
  GuiCtx (Id, X, Y, Width, Height, X2, Y2)
VALUES
  (22, 430, 115, 30, 30, 500, 115);

INSERT INTO
  TuringMachine (
    Id,
    Name,
    Description,
    StartStateId,
    AcceptStateId,
    RejectStateId
  )
VALUES
  (
    1,
    'The Zero TM',
    'A Turing Machine that computes the Zero function',
    0,
    2,
    -1
  );

INSERT INTO
  MuFunction (Name, Lhs, Rhs)
VALUES
  ('add', 'add(x, y)', '(x + y)');