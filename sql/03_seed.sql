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


-- Seed data for the "See One" unit: the Zero Function overview problem with
-- its Turing machine, lambda expressions, mu-recursive function, hints and
-- knowledge component.
--
-- Assumes 02_schema.sql has already run against the selected database.


-- MuFunction.Id is AUTO_INCREMENT, and MySQL normally reinterprets an
-- explicitly inserted 0 as "generate the next value". That silently moved the
-- zero function Z off id 0, leaving Problem.MuRecursiveFunctionId = 0 pointing
-- at nothing, so ProblemDAO could not load the mu-recursive representation of
-- the Zero Function. NO_AUTO_VALUE_ON_ZERO makes an explicit 0 mean 0.
--
-- MuFunction is the only seeded table where this applies: TuringMachine seeds
-- a non-zero id, and Assessment, PendingStep and TutoringSession have no seed
-- rows at all.
SET SESSION sql_mode = CONCAT(@@SESSION.sql_mode, ',NO_AUTO_VALUE_ON_ZERO');


-- ---------------------------------------------------------------
-- Course, units, problems, tasks, steps and their view configs
-- ---------------------------------------------------------------

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

INSERT INTO
  Hint (Id, StepId, Text, SequenceIndex)
VALUES
  (
    10,
    50,
    'Look at the equation \\s.\\z.z. The outer parameter is the first variable declared after the first lambda.',
    0
  ),
  (
    11,
    50,
    'The outer parameter is the letter s. Click the s!',
    1
  ),
  (
    12,
    51,
    'The inner parameter is the second variable declared, right before the body.',
    0
  ),
  (
    13,
    51,
    'The inner parameter is the letter z. Click the z!',
    1
  );

INSERT INTO
  MuFunction (Id, Name, Lhs, Rhs)
VALUES
  (0, 'Z', 'Z(x)', '0');


-- ---------------------------------------------------------------
-- Lambda calculus expressions: the identity function and the
-- Church numeral for zero.
-- ---------------------------------------------------------------

-- LAMBDA CALCULUS DATA
-- 1. Create the Variable 'x'
INSERT INTO
    LC_EXPRESSION (Id, ExprType)
VALUES
    (100, 'VAR');

INSERT INTO
    LC_VARIABLE (Id, Name)
VALUES
    (100, 'x');

-- 2. Create the Abstraction (\x. x)
INSERT INTO
    LC_EXPRESSION (Id, ExprType)
VALUES
    (1, 'ABS');

INSERT INTO
    LC_ABSTRACTION (Id, IsCurried)
VALUES
    (1, FALSE);

-- 3. Link Variable 'x' as Parameter of Abstraction 1
INSERT INTO
    LC_ABS_PARAMS (AbsId, VarId, SeqIndex)
VALUES
    (1, 100, 0);

-- 4. Link Variable 'x' as Body of Abstraction 1
INSERT INTO
    LC_ABS_BODY (AbsId, BodyExprId, SeqIndex)
VALUES
    (1, 100, 0);

-- 5. Create the Variables 's' and 'z' for the zero function 
INSERT INTO
    LC_EXPRESSION (Id, ExprType)
VALUES
    (101, 'VAR');

INSERT INTO
    LC_VARIABLE (Id, Name)
VALUES
    (101, 's');

INSERT INTO
    LC_EXPRESSION (Id, ExprType)
VALUES
    (102, 'VAR');

INSERT INTO
    LC_VARIABLE (Id, Name)
VALUES
    (102, 'z');

-- 6. Create the inner abstraciton (\z. z)
INSERT INTO
    LC_EXPRESSION (Id, ExprType)
VALUES
    (2, 'ABS');

INSERT INTO
    LC_ABSTRACTION (Id, IsCurried)
VALUES
    (2, FALSE);

INSERT INTO
    LC_ABS_PARAMS (AbsId, VarId, SeqIndex)
VALUES
    (2, 102, 0);

INSERT INTO
    LC_ABS_BODY (AbsId, BodyExprId, SeqIndex)
VALUES
    (2, 102, 0);

-- 7. create the outer abstraction (\s. (\z. z))
INSERT INTO
    LC_EXPRESSION (Id, ExprType)
VALUES
    (3, 'ABS');

INSERT INTO
    LC_ABSTRACTION (Id, IsCurried)
VALUES
    (3, FALSE);

INSERT INTO
    LC_ABS_PARAMS (AbsId, VarId, SeqIndex)
VALUES
    (3, 101, 0);

INSERT INTO
    LC_ABS_BODY (AbsId, BodyExprId, SeqIndex)
VALUES
    (3, 2, 0);
