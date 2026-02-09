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
/**
 * Author:  Ellis Langham
 * Created: Feb 9, 2026
 */

-- 1. Base Expression Table
CREATE TABLE IF NOT EXISTS LC_EXPRESSION (
    Id INT PRIMARY KEY,
    ExprType VARCHAR(10) -- 'VAR', 'ABS', or 'APP'
);

-- 2. Variables (e.g., "x")
CREATE TABLE IF NOT EXISTS LC_VARIABLE (
    Id INT PRIMARY KEY,
    Name VARCHAR(10),
    FOREIGN KEY (Id) REFERENCES LC_EXPRESSION(Id)
);

-- 3. Abstractions (Functions, e.g., "\x. ...")
CREATE TABLE IF NOT EXISTS LC_ABSTRACTION (
    Id INT PRIMARY KEY,
    IsCurried BOOLEAN,
    FOREIGN KEY (Id) REFERENCES LC_EXPRESSION(Id)
);

-- 4. Applications (Function Calls, e.g., "(x y)")
CREATE TABLE IF NOT EXISTS LC_APPLICATION (
    Id INT PRIMARY KEY,
    FOREIGN KEY (Id) REFERENCES LC_EXPRESSION(Id)
);

-- 5. Linking Tables (Connecting Abstractions to Parameters/Body)
CREATE TABLE IF NOT EXISTS LC_ABS_PARAMS (
    AbsId INT,
    VarId INT,
    SeqIndex INT, 
    PRIMARY KEY (AbsId, VarId, SeqIndex),
    FOREIGN KEY (AbsId) REFERENCES LC_ABSTRACTION(Id),
    FOREIGN KEY (VarId) REFERENCES LC_VARIABLE(Id)
);

CREATE TABLE IF NOT EXISTS LC_ABS_BODY (
    AbsId INT,
    BodyExprId INT,
    SeqIndex INT,
    PRIMARY KEY (AbsId, BodyExprId, SeqIndex),
    FOREIGN KEY (AbsId) REFERENCES LC_ABSTRACTION(Id),
    FOREIGN KEY (BodyExprId) REFERENCES LC_EXPRESSION(Id)
);

-- 6. Linking Tables (Connecting Applications to Function/Arg)
CREATE TABLE IF NOT EXISTS LC_APP_FUNC (
    AppId INT,
    FuncExprId INT,
    PRIMARY KEY (AppId, FuncExprId),
    FOREIGN KEY (AppId) REFERENCES LC_APPLICATION(Id),
    FOREIGN KEY (FuncExprId) REFERENCES LC_EXPRESSION(Id)
);

CREATE TABLE IF NOT EXISTS LC_APP_ARG (
    AppId INT,
    ArgExprId INT,
    PRIMARY KEY (AppId, ArgExprId),
    FOREIGN KEY (AppId) REFERENCES LC_APPLICATION(Id),
    FOREIGN KEY (ArgExprId) REFERENCES LC_EXPRESSION(Id)
);


-- LAMBDA CALCULUS DATA

-- 1. Create the Variable 'x'
INSERT INTO LC_EXPRESSION (Id, ExprType) VALUES (100, 'VAR');
INSERT INTO LC_VARIABLE (Id, Name) VALUES (100, 'x');

-- 2. Create the Abstraction (\x. x)
INSERT INTO LC_EXPRESSION (Id, ExprType) VALUES (1, 'ABS');
INSERT INTO LC_ABSTRACTION (Id, IsCurried) VALUES (1, FALSE);

-- 3. Link Variable 'x' as Parameter of Abstraction 1
INSERT INTO LC_ABS_PARAMS (AbsId, VarId, SeqIndex) VALUES (1, 100, 0);

-- 4. Link Variable 'x' as Body of Abstraction 1
INSERT INTO LC_ABS_BODY (AbsId, BodyExprId, SeqIndex) VALUES (1, 100, 0);