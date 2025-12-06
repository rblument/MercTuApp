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
package edu.regis.merc.dao;

import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.*;
import edu.regis.merc.svc.CourseSvc;
import edu.regis.merc.svc.ProblemSvc;
import edu.regis.merc.svc.ServiceFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * An XML-based Data Access Object implementing CourseSvc behaviors.
 *
 * @author rickb
 */
public class CourseDAO extends MySqlDAO implements CourseSvc {

    /**
     * Instantiate this Course DAO with default values.
     */
    public CourseDAO() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Course retrieve(int courseId) throws ObjNotFoundException, NonRecoverableException {
        final String sql = "SELECT Title,PrimaryPedagogy,Description FROM Course WHERE Id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, courseId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Course course = new Course(courseId);

                course.setTitle(rs.getString(1));
                course.setPrimaryPedagogy(TaskSelectionKind.valueOf(rs.getString(2).toUpperCase()));
                course.setDescription(rs.getString(3));
                course.setExercisingLocations(retrieveExercisingLocations(courseId, conn));
                course.setUnits(retrieveUnits(course, conn));
                course.setOutcomes(retrieveKnowledgeComponents(course, conn));

                return course;

            } else {
                throw new ObjNotFoundException("Course Id:" + courseId);
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-1" + e.toString(), e);
        } finally {
            close(conn, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CourseDigest retrieveDigest(int courseId, Connection conn)
            throws ObjNotFoundException, NonRecoverableException {

        final String sql = "SELECT Title,Description FROM Course WHERE Id = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, courseId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                CourseDigest digest = new CourseDigest(courseId);

                digest.setTitle(rs.getString(1));
                digest.setDescription(rs.getString(2));

                return digest;

            } else {
                throw new ObjNotFoundException("Course Id:" + courseId);
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-2" + e.toString(), e);
        } finally {
            close(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnitDigest retrieveUnitDigest(int courseId, int unitId, Connection conn)
            throws ObjNotFoundException, NonRecoverableException {

        final String sql = "SELECT Title,Description FROM Unit WHERE CourseId = ? AND Id = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, courseId);
            stmt.setInt(2, unitId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UnitDigest digest = new UnitDigest(unitId);

                digest.setTitle(rs.getString(1));
                digest.setDescription(rs.getString(2));

                return digest;

            } else {
                throw new ObjNotFoundException("Unit Id:" + courseId);
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-3" + e.toString(), e);
        } finally {
            close(stmt);
        }
    }

    /**
     * Extract child &lt;Outcome> elements from given XML DOM parent element
     * adding each as a Outcome to the given Course.
     *
     * @param parent an XML DOM Element containing one or more child
     *               &lt;Outcome> node elements.
     * @throws ShaTuException a nonrecoverable exception also see getCause()
     * @return a List of KnowledgeComponent outcomes
     */
    private ArrayList<KnowledgeComponent> retrieveKnowledgeComponents(Course course, Connection conn)
            throws NonRecoverableException {

        final String sql = "SELECT Id, Title, Description, BloomLevel, IsDomainFocus, Pedagogy, ExercisingLocations, Granularity FROM KnowledgeComponent WHERE CourseId = ?";

        ArrayList<KnowledgeComponent> outcomes = new ArrayList<>();

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, course.getId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                KnowledgeComponent comp = new KnowledgeComponent(rs.getInt(1));
                comp.setTitle(rs.getString(2));
                comp.setDescription(rs.getString(3));
                comp.setBloomLevel(BloomLevel.findValue(rs.getString(4)));
                comp.setIsDomainFocus(rs.getBoolean(5));
                comp.setPedagogy(TaskSelectionKind.findValue(rs.getString(6)));

                comp.setGranularity(OutcomeGranularity.findValue(rs.getString(8)));

                // Link the exercising locations in this outcome to those in the course.
                ArrayList<ExercisingLocation> locations = new ArrayList<>();
                String[] ids = rs.getString(7).split(",");
                for (int i = 0; i < ids.length; i++)
                    locations.add(course.findLocation(i));

                comp.setExercisingLocations(locations);

                outcomes.add(comp);
            }

            return outcomes;

        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-5" + e.toString(), e);
        } finally {
            close(stmt);
        }
    }

    /**
     * Return the units in this course from the &lt;Unit> elements.
     * 
     * @param parent a &lt;Course> element
     * @return a List of Units
     * @throws NonRecoverableException
     */
    private ArrayList<Unit> retrieveUnits(Course course, Connection conn) throws NonRecoverableException {
        final String sql = "SELECT Id,Title,Description,SequenceIndex,Pedagogy FROM Unit WHERE CourseId = ?";

        ArrayList<Unit> units = new ArrayList<>();

        PreparedStatement stmt = null;
        
        ProblemSvc problemSvc = ServiceFactory.findProbelmSvc();

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, course.getId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Unit unit = new Unit(rs.getInt("Id"));
                unit.setTitle(rs.getString("Title"));
                unit.setDescription(rs.getString("Description"));
                unit.setSequenceId(rs.getInt("SequenceIndex"));
                unit.setPedagogy(TaskSelectionKind.valueOf(rs.getString(5)));

                unit.setProblems(problemSvc.retrieveByUnitId(unit.getId()));

                units.add(unit);
            }

            return units;
            
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-6" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }
    }

       /**
     * Extract child &lt;ExercisingLocation> elements from given XML DOM parent
     * element and return the list of these locations.
     *
     * @param parent an XML DOM Element containing one or more child
     *               &lt;ExercisingLocation> node elements.
     * @throws ShaTuException a nonrecoverable exception also see getCause()
     * @return a List of ExercisingElements
     */
    private ArrayList<ExercisingLocation> retrieveExercisingLocations(int courseId, Connection conn)
            throws NonRecoverableException {

        final String sql = "SELECT Id, UnitId, TaskId, StepId FROM ExercisingLocation WHERE CourseId = ?";

        ArrayList<ExercisingLocation> locations = new ArrayList<>();

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, courseId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ExercisingLocation location = new ExercisingLocation(rs.getInt("Id"));
                location.setCourseId(courseId);
                location.setUnitId(rs.getInt("UnitId"));
                location.setTaskId(rs.getInt("TaskId"));
                location.setStepId(rs.getInt("StepId"));

                locations.add(location);
            }

            return locations;

        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-12" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }
    }
}
