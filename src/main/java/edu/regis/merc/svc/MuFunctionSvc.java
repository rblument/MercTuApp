// Kristin Ingram 
// Last updated : 12/02

package edu.regis.merc.svc;

import java.util.List;

import edu.regis.merc.model.MuFunction;


public interface MuFunctionSvc{
    // return all μ recursive functions defined in system 
   // @return list of MuFunction
   List<MuFunction> getAllFunctions();

   // insert a new μ recursive function into system
   // @param f the function to insert
   void insertFunction(MuFunction f); 

   // evaluate a μ recursive function with given arguments 
   // @param function the MuFunction to evaluate
   // @param args the input arguments to the function
   // @return the integer result of evaluation
   int evaluate(MuFunction function, int... args); 
}

