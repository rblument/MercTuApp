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


-- Table definitions for MercTuDB. Assumes the database exists and is
-- selected -- setupDB.sql does both before sourcing this file.
--
-- This is the single source of truth for the schema. Do not copy table
-- definitions into other scripts.


-- ---------------------------------------------------------------
-- Core tutoring tables
-- ---------------------------------------------------------------

CREATE TABLE
  Account (
    UserId VARCHAR(256),
    Password VARCHAR(256) NOT NULL,
    Salt VARCHAR(32) NOT NULL,
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


-- ---------------------------------------------------------------
-- Lambda calculus expression tables
--
-- Declared after the core tables because they carry foreign keys
-- among themselves: LC_EXPRESSION must exist before LC_VARIABLE and
-- LC_ABSTRACTION, which in turn must exist before the link tables.
-- ---------------------------------------------------------------

-- Original lambda calculus tables by Ellis Langham.

-- primary table
CREATE TABLE LC_EXPRESSION (
    Id INT PRIMARY KEY,
    ExprType VARCHAR(10) -- possibles values are 'VAR', 'ABS', 'APP'
);

-- variable table 
CREATE TABLE LC_VARIABLE (
    Id INT PRIMARY KEY,
    Name VARCHAR(50),
    FOREIGN KEY (Id) REFERENCES LC_EXPRESSION(Id)
);

-- abstraction table
CREATE TABLE LC_ABSTRACTION (
    Id INT PRIMARY KEY,
    IsCurried BOOLEAN,
    FOREIGN KEY (Id) REFERENCES LC_EXPRESSION(Id)
);

-- table for abstraction's parameters
CREATE TABLE LC_ABS_PARAMS (
    AbsId INT,
    VarId INT,
    SeqIndex INT,
    PRIMARY KEY (AbsId, SeqIndex),
    FOREIGN KEY (AbsId) REFERENCES LC_ABSTRACTION(Id),
    FOREIGN KEY (VarId) REFERENCES LC_VARIABLE(Id)
);

-- table for an abstraction's body
--
-- NOTE: the primary key includes BodyExprId, where LC_ABS_PARAMS above keys
-- only on (AbsId, SeqIndex). That is carried over from the original script
-- and left alone here so this change stays a consolidation. It is looser than
-- it should be: it permits two rows at the same SeqIndex for one abstraction.
CREATE TABLE LC_ABS_BODY (
    AbsId INT,
    BodyExprId INT,
    SeqIndex INT,
    PRIMARY KEY (AbsId, BodyExprId, SeqIndex),
    FOREIGN KEY (AbsId) REFERENCES LC_ABSTRACTION(Id),
    FOREIGN KEY (BodyExprId) REFERENCES LC_EXPRESSION(Id)
);

-- application table 
CREATE TABLE LC_APPLICATION (
    Id INT PRIMARY KEY,
    FuncAbsId INT, 
    ArgExprId INT, 
    FOREIGN KEY (Id) REFERENCES LC_EXPRESSION(Id),
    FOREIGN KEY (FuncAbsId) REFERENCES LC_ABSTRACTION(Id),
    FOREIGN KEY (ArgExprId) REFERENCES LC_EXPRESSION(Id)
);
