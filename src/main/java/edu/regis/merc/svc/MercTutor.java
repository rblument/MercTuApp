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
package edu.regis.merc.svc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.regis.merc.err.IllegalArgException;
import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.Account;
import edu.regis.merc.model.Assessment;
import edu.regis.merc.model.AssessmentLevel;
import edu.regis.merc.model.Course;
import edu.regis.merc.model.KnowledgeComponent;
import edu.regis.merc.model.PendingStep;
import edu.regis.merc.model.PendingTask;
import edu.regis.merc.model.StepCompletion;
import edu.regis.merc.model.Student;
import edu.regis.merc.model.StudentModel;
import edu.regis.merc.model.Task;
import edu.regis.merc.model.TutoringSession;
import edu.regis.merc.model.Unit;
import edu.regis.merc.util.SHA_256;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The MERC tutor.
 *
 * @author rickb
 */
public class MercTutor implements TutorSvc {

    /**
     * The id of the default course taught by the this tutor.
     */
    private static final int DEFAULT_COURSE_ID = 1;

    /**
     * Handler for logging non-exception messages from this class versus thrown
     * exception, which are logged by the exception.
     */
    private static final Logger LOGGER = Logger.getLogger(MercTutor.class.getName());

    /**
     * The current tutoring session, which contains information on the current
     * Student, StudentModel, Course, Task, Step, etc.
     */
    private TutoringSession session;

    private Student student;
   
    private StudentModel studentModel;

    /**
     * Convenience reference to the current gson object.
     */
    private Gson gson;

    //private Objective currObjective;

    /**
     * Initialize the tutor singleton (a NoOp).
     */
    public MercTutor() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TutorReply request(ClientRequest request) {
        // Uses reflection to invoke a method derived from the request name in
        // the client request (e.g., ":SignIn" invokes "signIn(...)").
        Logger.getLogger(MercTutor.class.getName()).log(Level.INFO, request.getRequestType().getRequestName());

        // Efficiently produce "signIn" from ":SignIn", for example.
        char c[] = request.getRequestType().getRequestName().toCharArray();
        c[1] = Character.toLowerCase(c[1]);

        char m[] = new char[c.length - 1];
        for (int i = 1; i < c.length; i++) {
            m[i - 1] = c[i];
        }

        String methodName = new String(m);

        // Most methods require verifying the given security token with the
        // one current known in the DB for the given user.
        switch (methodName) {
            case "completedStep":
            //case "getTask":
            case "newExample":
            case "requestHint":
            case "resetPassword":
                String userId = request.getUserId();
                try {
                    if (verifySession(userId, request.getSecurityToken())) {

                        Account account = ServiceFactory.findAccountSvc().retrieve(userId);
                        student = new Student(account);

                        try {
                            StudentModelSvc stuModSvc = ServiceFactory.findStudentModelSvc();
                            studentModel = stuModSvc.retrieve(userId);
                            student.setStudentModel(studentModel);

                        } catch (ObjNotFoundException ex) {
                            TutorReply reply = new TutorReply(":ERR");
                            reply.setData("Student model not found for: " + userId);
                            return reply;
                        }

                        session = ServiceFactory.findSessionSvc().retrieve(student.getAccount().getUserId());

                    } else {
                        TutorReply reply = new TutorReply(":ERR");
                        reply.setData("Illegal Security Token");
                        return reply;
                    }

                } catch (ObjNotFoundException ex) {
                    return createError("No session exists for user: " + userId, ex);
                } catch (NonRecoverableException ex) {
                    return createError(ex.toString(), ex);
                }

                String msg = "Session verified for " + request.getUserId();
                Logger.getLogger(MercTutor.class.getName()).log(Level.INFO, msg);
                break;

            default: // e.g., signIn itself, newAccount
                Logger.getLogger(MercTutor.class.getName()).log(Level.INFO, "No token verification required");
        }

        // Security token has been verified or not required (e.g., signIn,
        // createAccount).
        try {
            Method method = getClass().getMethod(methodName, String.class);

            return (TutorReply) method.invoke(this, request.getData());

        } catch (NoSuchMethodException ex) {
            return createError("Tutor received an unknown request type: " + request.getRequestType().getRequestName(),
                    ex);
        } catch (SecurityException ex) {
            return createError("ShaTuTutor_ERR_2", ex);
        } catch (IllegalAccessException ex) {
            return createError("ShaTuTutor_ERR_3", ex);
        } catch (IllegalArgumentException ex) {
            return createError("ShaTuTutor_ERR_4", ex);
        } catch (InvocationTargetException ex) {
            return createError("ShaTuTutor_ERR_5", (Exception) ex.getCause());
        }
    }

    /**
     * Creates a new student account
     *
     * This method handles ":CreateAccount" requests from the GUI client.
     *
     * @param jsonAcct a JSon encoded Account object
     * @throws NonRecoverableException perhaps see getCause().getErrorCode().
     * @return a TutorReply if successful the status is "Created", otherwise the
     *         status is ":ERR".
     */
    public TutorReply createAccount(String jsonAcct) throws NonRecoverableException {
        Account acct = gson.fromJson(jsonAcct, Account.class);

        int courseId = DEFAULT_COURSE_ID; // Currently only one course

        try {
            ServiceFactory.findAccountSvc().create(acct);
        } catch (IllegalArgException e) {
            return createError(String.format("Account %s exists", acct.getUserId()), null);
        }

        try {
            Course course = ServiceFactory.findCourseSvc().retrieve(courseId);

            student = createStudent(acct, course);

            createSession(student, course);

            return new TutorReply("Created");

        } catch (ObjNotFoundException ex) {
            return createError("Unknown course: " + courseId, null);
        }

        // try {
        // }
        // catch (IllegalArgException ex) { // The account already exists
        // return new TutorReply("IllegalUserId");
        // }
    }

    /**
     * Verifies the user is in the database and the security question and answer
     * match.
     *
     * This method handles ":VerifyUser" requests from the GUI client.
     *
     * @param jsonAcct a JSon encoded Account object
     * @return a TutorReply if successful the status is "Created", otherwise the
     *         status is ":ERR".
     * @throws edu.regis.shatu.err.NonRecoverableException
     */
    public TutorReply verifyUser(String jsonAcct) throws NonRecoverableException {
        Account requestAcct = gson.fromJson(jsonAcct, Account.class);
        AccountSvc acctSvc = ServiceFactory.findAccountSvc();

        if (!acctSvc.exists(requestAcct.getUserId())) {
            return new TutorReply("IllegalUserId");
        }

        try {
            Account dbAcct = acctSvc.retrieve(requestAcct.getUserId());

            if ((dbAcct.getSecurityAnswer().equals(requestAcct.getSecurityAnswer())) &&
                    (dbAcct.getSecurityQuestion() == requestAcct.getSecurityQuestion())) {

                student = new Student(dbAcct);

                try {
                    StudentModelSvc stuModSvc = ServiceFactory.findStudentModelSvc();
                    studentModel = stuModSvc.retrieve(dbAcct.getUserId());
                    student.setStudentModel(studentModel);
                } catch (ObjNotFoundException e) {
                    student = createStudent(dbAcct, ServiceFactory.findCourseSvc().retrieve(DEFAULT_COURSE_ID));
                }

                // Check if session already exists before creating
                SessionSvc sessionSvc = ServiceFactory.findSessionSvc();
                TutoringSession session;

                try {
                    session = sessionSvc.retrieve(dbAcct.getUserId()); // already exists
                } catch (ObjNotFoundException e) {
                    // session does not exist, create it
                    session = createSession(student, ServiceFactory.findCourseSvc().retrieve(DEFAULT_COURSE_ID));
                }

                TutorReply reply = new TutorReply("Verified");
                reply.setData("\"" + session.getSecurityToken() + "\"");
                return reply;

            } else {
                return new TutorReply("InvalidAnswer");
            }

        } catch (ObjNotFoundException e) {
            return new TutorReply("UnknownUser");
        } catch (NonRecoverableException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return new TutorReply();
        }
    }

    /**
     * Resets password for user currently in database
     *
     * This method handles ":ResetPassword" requests from the GUI client.
     *
     * @param jsonAcct a JSon encoded Account object
     * @return a TutorReply if successful the status is "Created", otherwise the
     *         status is ":ERR".
     * @throws edu.regis.shatu.err.NonRecoverableException
     */
    public TutorReply resetPassword(String jsonAcct) throws NonRecoverableException {
        Account acct = gson.fromJson(jsonAcct, Account.class);
        AccountSvc acctSvc = ServiceFactory.findAccountSvc();

        if (!acctSvc.exists(acct.getUserId())) {
            return new TutorReply("IllegalUserId");
        }

        try {
            // Retrieve full DB account so we don't lose names or other info
            Account dbAcct = acctSvc.retrieve(acct.getUserId());

            // Only update the changed fields
            dbAcct.setPassword(acct.getPassword());

            // Only set security question/answer if needed
            dbAcct.setSecurityQuestion(acct.getSecurityQuestion());
            dbAcct.setSecurityAnswer(acct.getSecurityAnswer());

            acctSvc.update(dbAcct);

            return new TutorReply("PasswordReset");

        } catch (ObjNotFoundException ex) {
            return new TutorReply("IllegalUserId");
        } catch (IllegalArgException ex) {
            return createError("Password Account Update error", ex);
        }
    }

    /**
     * Attempts to sign a student in.
     *
     * This method handles ":SignIn" requests from the GUI client.
     *
     * @param jsonUser a JSon encoded User object
     * @return a TutorReply, if successful, the status is "Authenticated" with
     *         data being a JSon encoded TutoringSession object.
     */
    public TutorReply signIn(String jsonUser) {
        System.out.println("Received sign in: " + jsonUser);
        Account requestAcct = gson.fromJson(jsonUser, Account.class);

        try {
            Account dbAcct = ServiceFactory.findAccountSvc().retrieve(requestAcct.getUserId());

            if (dbAcct.getPassword().equals(requestAcct.getPassword())) {
                student = new Student(dbAcct);
                String userId = dbAcct.getUserId();

                try {
                    StudentModelSvc stuModSvc = ServiceFactory.findStudentModelSvc();
                    studentModel = stuModSvc.retrieve(userId);
                    student.setStudentModel(studentModel);

                } catch (ObjNotFoundException ex) {
                    TutorReply reply = new TutorReply(":ERR");
                    reply.setData("Student model not found in sign in for: " + userId);
                    return reply;
                }

                SessionSvc svc = ServiceFactory.findSessionSvc();
                TutoringSession session = svc.retrieve(student.getAccount().getUserId());

                TutorReply reply = new TutorReply("Authenticated");

                reply.setData(gson.toJson(session));

                return reply;

            } else {
                return new TutorReply("InvalidPassword");
            }

        } catch (ObjNotFoundException e) {
            return new TutorReply("UnknownUser");
        } catch (NonRecoverableException ex) {
            Logger.getLogger(MercTutor.class
                    .getName()).log(Level.SEVERE, null, ex);
            return new TutorReply();
        }
    }


    /**
     * Returns a hint to the GUI client, if any
     *
     * This method handles ":RequestHint" requests from the GUI client.
     *
     * @param jsonObj
     * @return a TutorReply, if successful, the status is "Hint" with data being
     *         a displayable hint text string.
     */
    public TutorReply requestHint(String jsonObj) {
        System.out.println("requestHint");
        StepCompletion completion = gson.fromJson(jsonObj, StepCompletion.class);
        
        return new TutorReply(":ERR", "Request Hint Not Implemented");     
    }

    /**
     * @param jsonObj a JSon encoded StepCompletion object
     * @return
     */
    public TutorReply completedStep(String jsonObj) {
        System.out.println("completedStep");

        //StepCompletion completion = gson.fromJson(jsonObj, StepCompletion.class);
        
                return new TutorReply(":ERR", "Step completion Not Implemented");
    }

    /**
     * Handles :NewExample requests from the client.
     *
     * @param json a JSon String encoding a NewExampleRequest object
     * @return TutorReply
     */
    public TutorReply newExample(String json) {
        System.out.println("nexExample()");

        return null;
    }


    /**
     * Create and save a new tutoring session associated with the given account.
     *
     * @param account the student user
     * @throws NonRecoverableException
     * @return the new TutoringSession
     */
    private TutoringSession createSession(Student student, Course course) throws NonRecoverableException {
        Account account = student.getAccount();

        TutoringSession tSession = new TutoringSession(student);
        tSession.setStartDate(new GregorianCalendar());
        tSession.setCourse(course.getDigest());
        tSession.setUnit(course.currentUnit().getDigest());

        Task task = getFirstTask(course);
        PendingTask pendingTask = new PendingTask(task);
        pendingTask.setCurrentStep(new PendingStep(task.getCurrentStep()));
        tSession.addTask(pendingTask);

        // Generate the security token for this tutoring session.
        Random rnd = new Random();
        String clearToken = "Session" + account.getUserId() + Integer.toString(rnd.nextInt());
        tSession.setSecurityToken(SHA_256.instance().sha256(clearToken));

        try {
            ServiceFactory.findSessionSvc().create(tSession);

            return tSession;

        } catch (IllegalArgException ex) {
            throw new NonRecoverableException("Session already exists", ex);
        }
    }

    /**
     * Create and save the student and their initial student model.
     *
     * @param acct
     * @param course
     * @return
     */
    private Student createStudent(Account account, Course course)
            throws NonRecoverableException {

        student = new Student(account);
        studentModel = student.getStudentModel();

        for (KnowledgeComponent outcome : course.getOutcomes()) {
            Assessment assessment = new Assessment(outcome, AssessmentLevel.NOT_STARTED);

            studentModel.addAssessment(outcome.getId(), assessment);
        }

        StudentModelSvc stuSvc = ServiceFactory.findStudentModelSvc();

        stuSvc.create(student);

        return student;

    }

    /**
     * Verify that the user with the given id has a session with the given
     * session id.
     *
     * @param userId    String "user@regis.edu"
     * @param sessionId String identifying a previously generated session id.
     * @return the current TutoringSession associated with the given user id and
     *         session id
     */
    private boolean verifySession(String userId, String sessionId)
            throws ObjNotFoundException, NonRecoverableException {

        SessionSvc svc = ServiceFactory.findSessionSvc();
        String dbToken = svc.retrieveSecurityToken(userId);

        return dbToken.equals(sessionId);
    }

    /**
     * Return the first task that should be performed in the given course.
     *
     * @param course
     * @return a Task that should be completed first.
     * @throws NonRecoverableException see the message text.
     */
    private Task getFirstTask(Course course) throws NonRecoverableException {
        System.out.println("Tutor: primary: " + course.getPrimaryPedagogy());
        switch (course.getPrimaryPedagogy()) {
            case STUDENT_CHOICE:
                return null; // ToDo

            case FIXED_SEQUENCE:
                Unit unit = course.findUnitBySequenceId(0);

                if (unit == null) {
                    throw new NonRecoverableException("Unit 0 not found in course: " + course.getId());
                }

                Task task = unit.findTaskBySequence(0);

                if (task == null) {
                    throw new NonRecoverableException("Task 0 not found in Unit 0 of course: " + course.getId());
                }

                return task;

            case MASTERY_LEARNING:
                return null; // ToDo

            case MICROADAPTATION:
                return null; // ToDo

            default:
                throw new NonRecoverableException("Unknwon task selection in course: " + course.getId());
        }
    }

    /**
     * Utility for logging an error and an creating a tutoring reply error with
     * the given message, and optional originating exception.
     *
     * @param errMsg a displayable error message
     * @param ex     the original exception, if any, that caused the error,
     *               otherwise null.
     * @return a TutorReply with an ":ERR" status
     */
    public TutorReply createError(String errMsg, Exception ex) {
        if (ex == null) {
            Logger.getLogger(MercTutor.class.getName()).log(Level.SEVERE, errMsg);
        } else {
            Logger.getLogger(MercTutor.class.getName()).log(Level.SEVERE, errMsg, ex);
        }

        return new TutorReply(":ERR", errMsg);
    }
}
