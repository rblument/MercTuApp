// Kristin Ingram 
// Last updated: 12/2

package edu.regis.merc.svc;

import edu.regis.merc.model.MUDescription;
import edu.regis.merc.model.MuFunction;

import java.util.List;

public interface MUSvc {

    List<MUDescription> getSteps(int muFunctionId);

    int evaluate(MuFunction function, int... args);
}