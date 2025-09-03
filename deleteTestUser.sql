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
 * Author:  rickb
 * Created: Sep 3, 2025
 */

-- Author:  rickb
-- Created: Jan 11, 2025

-- Eventually, this script will be useless since it's a very heavy hammer which 
-- cleans up the 'test@regis.edu' user, but also all assessment data for every 
-- student account. It's usefulness is that it leaves static course data untouched.

DELETE FROM Account WHERE UserId = 'test@regis.edu';

DELETE FROM StudentModel WHERE UserId = 'test@regis.edu';

# Note this will clear every user. It resents the auto increment to zero.
TRUNCATE TABLE Assessment;

TRUNCATE TABLE TutoringSession;

TRUNCATE TABLE PendingTask;

TRUNCATE TABLE PendingStep;