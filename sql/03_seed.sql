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
    '{"representation": "LAMBDA_CALCULUS", "correctComponentId": 101}',
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
    '{"representation": "LAMBDA_CALCULUS", "correctComponentId": 102}',
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


-- ---------------------------------------------------------------
-- The successor function.
--
-- Written one statement per table rather than per row: the rows above
-- cost about eight lines each, which this term cannot afford -- it is
-- 24 rows on its own.
--
-- The comment below lists every node, so the ids can be checked against
-- the structure without tracing the inserts. A variable is one row
-- shared by all of its occurrences, matching the terms above, where id
-- 102 is both the parameter and the body of abstraction 2.
--
-- Ids start at 240 rather than 200 because 200-204 and 220-225 were
-- used by Church numerals 1 and 2, which were dropped: a Problem holds
-- a single LambdaCalculusId, so nothing could display them until there
-- is a problem of their own. Leaving the gap keeps those ids free if
-- they come back.
-- ---------------------------------------------------------------

-- Successor:  \n. \f. \x. f (n f x)
--   240  ABS  param=241 body=242
--   241  VAR  n
--   242  ABS  param=243 body=244
--   243  VAR  f
--   244  ABS  param=245 body=246
--   245  VAR  x
--   246  APP  func=243 arg=247
--   247  APP  func=248 arg=245
--   248  APP  func=241 arg=243
INSERT INTO LC_EXPRESSION (Id, ExprType) VALUES
  (240,'ABS'), (241,'VAR'), (242,'ABS'), (243,'VAR'), (244,'ABS'), (245,'VAR'),
  (246,'APP'), (247,'APP'), (248,'APP');
INSERT INTO LC_VARIABLE (Id, Name) VALUES (241,'n'), (243,'f'), (245,'x');
INSERT INTO LC_ABSTRACTION (Id, IsCurried) VALUES (240,FALSE), (242,FALSE), (244,FALSE);
INSERT INTO LC_APPLICATION (Id, FuncExprId, ArgExprId) VALUES (246,243,247), (247,248,245), (248,241,243);
INSERT INTO LC_ABS_PARAMS (AbsId, VarId, SeqIndex) VALUES (240,241,0), (242,243,0), (244,245,0);
INSERT INTO LC_ABS_BODY (AbsId, BodyExprId, SeqIndex) VALUES (240,242,0), (242,244,0), (244,246,0);


-- ---------------------------------------------------------------
-- Problem 1: the successor function.
--
-- Placed after the lambda rows above because it refers to expression
-- 240. Successor is the second of the initial primitive functions, so
-- it takes SequenceIndex 1, directly after the zero function. The
-- indices have to stay contiguous: the tutor finds the next problem by
-- an exact UnitId/SequenceIndex match and treats an empty result as the
-- end of the unit, so a gap would end the unit early.
--
-- Only the lambda calculus representation is filled in. The mu
-- recursive form of successor, S(Z(0)), needs function application in
-- MuExpression, which does not exist yet, and there is no Turing
-- machine for it, so both ids are left null.
-- ---------------------------------------------------------------

INSERT INTO
  Problem (Id, Title, Description, UnitId, SequenceIndex,
           TuringMachineId, LambdaCalculusId, MuRecursiveFunctionId)
VALUES
  (1, 'Successor Function',
   'The student sees that a Natural number is a count of applications, and that successor adds one more.',
   0, 1, NULL, 240, NULL);

INSERT INTO
  Task (Id, ProblemId, SequenceIndex, Title, Description, ExercisedComponentId)
VALUES
  (11, 1, 0, 'Representing the Next Natural Number',
   'Builds up the idea that a Natural number is f applied to a base some number of times, and that successor applies f once more.', -1);

-- The steps follow the concept rather than the syntax: what stands for
-- zero, what counts, and what successor adds. Successor has no addition
-- available to it, so "the next Natural number" has to be shown as one
-- more application rather than as n + 1.
--
-- Target ids are the rows above: 245 is x, 243 is f, and 246 is the
-- application f (n f x) that wraps the number successor was given.
INSERT INTO
  Step (Id, TaskId, SequenceIndex, Title, Description, Prompt, Context, Data,
        ExercisedComponentId, ViewConfigId, StudentAction, ActionId, TimeoutId)
VALUES
  (52, 11, 0, 'Zero as No Applications',
   'Shows that the base value, with the counting function applied no times, stands for zero',
   'Identify zero.',
   'A Natural number is written here as a function applied to a base value some number of times: the base alone is 0, one application is 1, two applications is 2. Select the symbol that stands for 0.',
   '{"representation": "LAMBDA_CALCULUS", "correctComponentId": 245}',
   -1, 10, 'INFORMATION_MESSAGE', -1, 0),
  (53, 11, 1, 'The Counting Function',
   'Shows that each additional application moves to the next Natural number',
   'Identify the function that counts.',
   'Applying one particular function one more time is what moves from a Natural number to the next one. Select that function.',
   '{"representation": "LAMBDA_CALCULUS", "correctComponentId": 243}',
   -1, 10, 'INFORMATION_MESSAGE', -1, 0),
  (54, 11, 2, 'The Application Successor Adds',
   'Shows that successor is one further application wrapped around the number it was given',
   'Identify the application successor adds.',
   'Inside the body, n f x is the number successor was given. Successor does not add 1 to it -- it applies f around it one more time. Select that outermost application.',
   '{"representation": "LAMBDA_CALCULUS", "correctComponentId": 246}',
   -1, 10, 'INFORMATION_MESSAGE', -1, 0);

INSERT INTO
  Hint (Id, StepId, Text, SequenceIndex)
VALUES
  (20, 52, 'A number counts how many times the function is applied. Zero applications leaves the base value untouched.', 0),
  (21, 52, 'The base value is x. Click the x!', 1),
  (22, 53, 'Going from one Natural number to the next means applying this one more time.', 0),
  (23, 53, 'It is f. Click the f!', 1),
  (24, 54, 'n f x is what the given number already builds. Successor wraps one more application of f around it.', 0),
  (25, 54, 'Click the parentheses around the whole body, not the f inside it.', 1);


-- ---------------------------------------------------------------
-- Problem 2: a projection function.
--
-- P_i^k(x1, ..., xk) = xi simply returns one of the values it is given.
-- There are infinitely many of them, so one concrete example is seeded,
-- P_2^3, which is the one in Dr. Blumenthal's notes. Nothing in the code
-- is specific to it: LeftHandSide takes any parameter list and
-- LCAbstraction takes any number of parameters.
--
-- The lambda form is written uncurried, as one abstraction over three
-- parameters, matching how it is written in the notes. IsCurried is set
-- true because that is what LCParser sets when it reads the comma form,
-- \x1,x2,x3. x2. Nothing reads the flag yet, so it is a label rather
-- than behaviour. The curried form, \x1.(\x2.(\x3. x2)), is the same
-- function and belongs in a later problem.
--
-- Unlike successor, projection needs no function application, so the mu
-- recursive form is filled in here: its right hand side is a bare
-- variable, which retrieveMuFunction already handles. The steps still
-- grade against lambda, because MuRecView draws the whole parameter
-- list as a single label and so cannot report which xi was clicked.
-- ---------------------------------------------------------------

-- P_2^3:  \x1 x2 x3. x2
--   260  ABS  param=261,262,263 body=262
--   261  VAR  x1
--   262  VAR  x2
--   263  VAR  x3
INSERT INTO LC_EXPRESSION (Id, ExprType) VALUES
  (260,'ABS'), (261,'VAR'), (262,'VAR'), (263,'VAR');
INSERT INTO LC_VARIABLE (Id, Name) VALUES (261,'x1'), (262,'x2'), (263,'x3');
INSERT INTO LC_ABSTRACTION (Id, IsCurried) VALUES (260,TRUE);
INSERT INTO LC_ABS_PARAMS (AbsId, VarId, SeqIndex) VALUES (260,261,0), (260,262,1), (260,263,2);
INSERT INTO LC_ABS_BODY (AbsId, BodyExprId, SeqIndex) VALUES (260,262,0);

-- Id 2 is free: Z is an explicit 0 and add took 1 from AUTO_INCREMENT.
INSERT INTO
  MuFunction (Id, Name, Lhs, Rhs)
VALUES
  (2, 'P', 'P(x1, x2, x3)', 'x2');

INSERT INTO
  Problem (Id, Title, Description, UnitId, SequenceIndex,
           TuringMachineId, LambdaCalculusId, MuRecursiveFunctionId)
VALUES
  (2, 'Projection Function',
   'The student sees that a projection computes nothing and simply returns one of the values it is given.',
   0, 2, NULL, 260, 2);

INSERT INTO
  Task (Id, ProblemId, SequenceIndex, Title, Description, ExercisedComponentId)
VALUES
  (12, 2, 0, 'Selecting Among the Given Values',
   'Shows that a projection returns one of its inputs unchanged, and that the subscript picks which one.', -1);

-- Step 56 asks about a projection that is not the one on screen, so the
-- student has to read the subscript rather than recognise the picture.
-- 262 is x2 and 263 is x3.
INSERT INTO
  Step (Id, TaskId, SequenceIndex, Title, Description, Prompt, Context, Data,
        ExercisedComponentId, ViewConfigId, StudentAction, ActionId, TimeoutId)
VALUES
  (55, 12, 0, 'The Value a Projection Returns',
   'Shows that a projection hands back one of its inputs unchanged',
   'Identify the value this projection returns.',
   'A projection does not compute anything. P_2^3 is given three values and returns one of them unchanged. Select the value it returns.',
   '{"representation": "LAMBDA_CALCULUS", "correctComponentId": 262}',
   -1, 10, 'INFORMATION_MESSAGE', -1, 0),
  (56, 12, 1, 'Reading the Subscript',
   'Shows that the subscript chooses which input is returned and the superscript counts them',
   'Identify what a different projection would return.',
   'In P_i^k the superscript k says how many values are given and the subscript i says which one comes back. Select the value that P_3^3 would return.',
   '{"representation": "LAMBDA_CALCULUS", "correctComponentId": 263}',
   -1, 10, 'INFORMATION_MESSAGE', -1, 0);

INSERT INTO
  Hint (Id, StepId, Text, SequenceIndex)
VALUES
  (26, 55, 'A projection performs no arithmetic. Whatever it returns is already one of the values on screen.', 0),
  (27, 55, 'The subscript 2 means the second of the three. Click x2!', 1),
  (28, 56, 'The subscript changed from 2 to 3, so a different one of the three comes back.', 0),
  (29, 56, 'P_3^3 returns the third of its three values. Click x3!', 1);
