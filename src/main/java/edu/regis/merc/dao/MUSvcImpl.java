package edu.regis.merc.dao;

import edu.regis.merc.model.MUDescription;
import edu.regis.merc.model.MuFunction;
import edu.regis.merc.svc.MUSvc;
import edu.regis.merc.svc.MuFunctionSvc;

import java.util.ArrayList;
import java.util.List;

public class MUSvcImpl implements MUSvc {

    private final MuFunctionSvc muSvc = new MuFunctionDAO();

    @Override
    public List<MUDescription> getSteps(int muFunctionId) {
        // Placeholder
        return new ArrayList<>();
    }

    @Override
    public int evaluate(MuFunction function, int... args) {
        return muSvc.evaluate(function, args);
    }
}