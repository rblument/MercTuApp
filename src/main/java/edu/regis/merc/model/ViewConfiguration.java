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
package edu.regis.merc.model;

/**
 * Specifies what computational models, and their components, of the current
 * problem should be displayed in the GUI. * 
 * 
 * @author rickb
 */
public class ViewConfiguration extends Model {
    /**
     * A description of what components of the Turing Machine should be displayed.
     */
    private TmViewConfiguration tmViewConfiguration;
    
    private LCViewConfiguration lcViewConfiguration;
    
    private MuViewConfiguration muViewConfiguration;
    
    public ViewConfiguration() {
        this(DEFAULT_ID);
    }
    
    public ViewConfiguration(int id) {
        super(id);
    }

    public TmViewConfiguration getTmViewConfiguration() {
        return tmViewConfiguration;
    }

    public void setTmViewConfiguration(TmViewConfiguration tmViewConfiguration) {
        this.tmViewConfiguration = tmViewConfiguration;
    }

    public LCViewConfiguration getLcViewConfiguration() {
        return lcViewConfiguration;
    }

    public void setLcViewConfiguration(LCViewConfiguration lcViewConfiguration) {
        this.lcViewConfiguration = lcViewConfiguration;
    }

    public MuViewConfiguration getMuViewConfiguration() {
        return muViewConfiguration;
    }

    public void setMuViewConfiguration(MuViewConfiguration muViewConfiguration) {
        this.muViewConfiguration = muViewConfiguration;
    }
}
