// Kristin Ingram 
// Last updated: December 08

package edu.regis.merc;

import edu.regis.merc.dao.MuFunctionDAO;
import edu.regis.merc.model.LeftHandSide;
import edu.regis.merc.model.MuExpression;
import edu.regis.merc.model.MuFunction;

public class MuDAOTest {
    public static void main(String[] args) {

        MuFunctionDAO dao = new MuFunctionDAO();

        System.out.println("\n===== INSERTING A MU FUNCTION INTO DB =====");

        LeftHandSide lhs = new LeftHandSide("testFunc");
        lhs.addParameter("x");
        
        MuExpression rhs = new MuExpression("0"); // f(x) = 0

        MuFunction f = new MuFunction(999, lhs, rhs);

        dao.insertFunction(f);

        System.out.println("\n===== LOADING ALL MU FUNCTIONS FROM DB =====");
        dao.getAllFunctions();

        System.out.println("\nTest complete.\n");
    }
}
