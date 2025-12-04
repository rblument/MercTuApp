// Kristin Ingram 
// last updated: 10/28

package edu.regis.merc.model;

import java.util.ArrayList; // imported 
import java.util.HashMap;
import java.util.List;
import java.util.Map; 

public class LeftHandSide {
    private String name; 
    private List<String> parameters; 

    public LeftHandSide(String name){
        this.name = name; 
        this.parameters = new ArrayList<>(); 
    }

    public void addParameter(String param){
        parameters.add(param); 
    }

    public String getName() {return name;}

    public List <String> getParameters() {return parameters;}

    @Override
    public String toString(){
        return name + "(" + String.join(", ", parameters) + ")";
    }

    public Map<String, Integer> bindArguments(int... args) {
        if (args.length != parameters.size()) {
            throw new IllegalArgumentException("Argument count mismatch.");
        }

        Map<String, Integer> env = new HashMap<>();
        for (int i = 0; i < parameters.size(); i++) {
            env.put(parameters.get(i), args[i]);
        }
        return env;
    }

}
