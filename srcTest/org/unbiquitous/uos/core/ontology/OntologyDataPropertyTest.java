/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontology;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.ontologyEngine.Ontology;
import org.unbiquitous.uos.core.ontologyEngine.exception.ReasonerNotDefinedException;


/**
 *
 * @author anaozaki
 */
public class OntologyDataPropertyTest {
    String dataPropertyName = "DataPropertyA";
    String toDataPropertyName = "toDataPropertyA";
    String subDataPropertyName = "DataPropertyB";
    String dataPropertyBooleanRangeName = "DataPropertyBooleanRange";
    String dataPropertyIntRangeName = "DataPropertyIntRange";
    String dataPropertyFloatRangeName = "DataPropertyFloatRange";
    String dataPropertyStringRangeName = "DataPropertyStringRange";
    String dataPropertyDomainName = "DataPropertyDomain";
    private static String DEFAULT_UBIQUIT_BUNDLE_FILE = "ubiquitos_main";
    private ResourceBundle resourceBundle = ResourceBundle
				.getBundle(DEFAULT_UBIQUIT_BUNDLE_FILE);
    Ontology ontology;
    
    @Before public void setUp() throws IOException{
		new File("resources/owl/uoscontext.owl").createNewFile();
	}
	
	@After public void tearDown(){
		new File("resources/owl/uoscontext.owl").delete();
	}
    
    @Before 
    public void setup(){
        try {
            ontology = new Ontology(new InitialProperties(resourceBundle));
            
        } catch (ReasonerNotDefinedException ex) {
            Logger.getLogger(OntologyDataPropertyTest.class.getName()).log(Level.SEVERE, null, ex);
        }              
    }
    
    @Test
    public void addRemoveDataPropertyTest() {
        ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployDataProperty().hasDataProperty(dataPropertyName));
        ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyUndeployDataProperty().hasDataProperty(dataPropertyName));
    }

    @Test
    public void addRemoveSubDataPropertyTest() {
        ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
        ontology.getOntologyDeployDataProperty().addSubDataProperty(subDataPropertyName, dataPropertyName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployDataProperty()
                .hasSubDataProperty(subDataPropertyName, dataPropertyName));
        ontology.getOntologyUndeployDataProperty().removeSubDataProperty(dataPropertyName, subDataPropertyName);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyUndeployDataProperty().hasSubDataProperty(dataPropertyName, subDataPropertyName));
        ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);  
        ontology.saveChanges();
    }
    
    @Test
    public void addRemoveDataPropertyBooleanRangeTest(){
        ontology.getOntologyDeployDataProperty().addDataPropertyBooleanRange(dataPropertyBooleanRangeName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployDataProperty()
                .hasDataPropertyBooleanRange(dataPropertyBooleanRangeName));
        ontology.getOntologyUndeployDataProperty().removeDataPropertyBooleanRange(dataPropertyBooleanRangeName);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyUndeployDataProperty()
                .hasDataPropertyBooleanRange(dataPropertyBooleanRangeName));
    }
    
    @Test
    public void addRemoveDataPropertyIntegerRangeTest(){
        ontology.getOntologyDeployDataProperty().addDataPropertyIntegerRange(dataPropertyIntRangeName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployDataProperty()
                .hasDataPropertyIntegerRange(dataPropertyIntRangeName));
        ontology.getOntologyUndeployDataProperty().removeDataPropertyIntegerRange(dataPropertyIntRangeName);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyUndeployDataProperty()
                .hasDataPropertyIntegerRange(dataPropertyIntRangeName));
    }
    
    @Test
    public void addRemoveDataPropertyFloatRangeTest(){
        ontology.getOntologyDeployDataProperty().addDataPropertyFloatRange(dataPropertyFloatRangeName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployDataProperty()
                .hasDataPropertyFloatRange(dataPropertyFloatRangeName));
        ontology.getOntologyUndeployDataProperty().removeDataPropertyFloatRange(dataPropertyFloatRangeName);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyUndeployDataProperty()
                .hasDataPropertyFloatRange(dataPropertyFloatRangeName));
    }
    
    @Test
    public void addRemoveDataPropertyStringRangeTest(){
        ontology.getOntologyDeployDataProperty().addDataPropertyStringRange(dataPropertyStringRangeName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployDataProperty()
                .hasDataPropertyStringRange(dataPropertyStringRangeName));
        ontology.getOntologyUndeployDataProperty().removeDataPropertyStringRange(dataPropertyStringRangeName);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyDeployDataProperty()
                .hasDataPropertyStringRange(dataPropertyStringRangeName));
    }
    
    @Test
    public void addRemoveDataPropertyDomainTest(){
        ontology.getOntologyDeployDataProperty().addDataPropertyDomain(dataPropertyName, dataPropertyDomainName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployDataProperty()
                .hasDataPropertyDomain(dataPropertyName, dataPropertyDomainName));
        ontology.getOntologyUndeployDataProperty().removeDataPropertyDomain(dataPropertyName, dataPropertyDomainName);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyDeployDataProperty()
                .hasDataPropertyDomain(dataPropertyName, dataPropertyDomainName));
    }
    
    @Test
    public void moveSubDataPropertyTest() {
        ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
        ontology.getOntologyDeployDataProperty().addDataProperty(toDataPropertyName);
        ontology.getOntologyDeployDataProperty().addSubDataProperty(subDataPropertyName, dataPropertyName);
        ontology.saveChanges();
        ontology.getOntologyDeployDataProperty().moveSubDataProperty(subDataPropertyName, dataPropertyName, toDataPropertyName);
        ontology.saveChanges();
        List<String> strList = ontology.getOntologyReasoner().getSubDataPropertiesFromDataProperty(dataPropertyName, true);
        Assert.assertFalse(strList.contains(subDataPropertyName));
        strList = ontology.getOntologyReasoner().getSubDataPropertiesFromDataProperty(toDataPropertyName, true);
        Assert.assertTrue(strList.contains(subDataPropertyName));
        ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);
        ontology.getOntologyUndeployDataProperty().removeDataProperty(toDataPropertyName);
        ontology.getOntologyUndeployDataProperty().removeSubDataProperty(subDataPropertyName, dataPropertyName);
        ontology.saveChanges();
    }
}
