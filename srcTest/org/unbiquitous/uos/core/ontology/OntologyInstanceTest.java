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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.ontologyEngine.Ontology;
import org.unbiquitous.uos.core.ontologyEngine.exception.ReasonerNotDefinedException;


/**
 *
 * @author anaozaki
 */
public class OntologyInstanceTest {
    String className = "ClasseA";
    String toClassName = "toClasseA";
    String instanceName = "InstanceOfA";
    String instanceName1 = "InstanceName1";
    String instanceName2 = "InstanceName2";
    String objectPropertyName = "ObjectPropertyName";
    String dataPropertyName = "DataPropertyName";
    
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
            Logger.getLogger(OntologyInstanceTest.class.getName()).log(Level.SEVERE, null, ex);
        }              
    }
    
    @Test
    public void addRemoveInstanceOfTest() {
        ontology.getOntologyDeployClass().addClass(className);
        ontology.getOntologyDeployInstance().addInstanceOf(instanceName, className);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployInstance().hasInstanceOf(instanceName, className));
        ontology.getOntologyUndeployInstance().removeInstanceOf(instanceName, className);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyUndeployInstance().hasInstanceOf(instanceName, className));
        ontology.getOntologyUndeployClass().removeClass(className);
        ontology.saveChanges();
    }
    
    @Test
    public void addRemoveObjectPropertyAssertionTest() {
        ontology.getOntologyDeployObjectProperty().addObjectProperty(objectPropertyName);
        ontology.getOntologyDeployInstance().addObjectPropertyAssertion(instanceName1, objectPropertyName, instanceName2);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().hasObjectProperty(instanceName1, objectPropertyName, instanceName2));
        ontology.getOntologyUndeployInstance().removeObjectPropertyAssertion(instanceName1, objectPropertyName, instanceName2);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyReasoner().hasObjectProperty(instanceName, objectPropertyName, instanceName2));
        ontology.getOntologyUndeployObjectProperty().removeObjectProperty(objectPropertyName);
        ontology.saveChanges();
    }
    
    @Test
    public void addRemoveDataPropertyAssertionIntTest() {
        ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
        ontology.getOntologyDeployInstance().addDataPropertyAssertion(instanceName1, dataPropertyName, 12);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().hasDataProperty(instanceName1, dataPropertyName, 12));
        ontology.getOntologyUndeployInstance().removeDataPropertyAssertion(instanceName1, dataPropertyName, 12);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyReasoner().hasDataProperty(instanceName, dataPropertyName, 12));
        ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);
        ontology.saveChanges();
    }
    
    @Test
    public void addRemoveDataPropertyAssertionStrTest() {
        ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
        ontology.getOntologyDeployInstance().addDataPropertyAssertion(instanceName1, dataPropertyName, "str");
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().hasDataProperty(instanceName1, dataPropertyName, "str"));
        ontology.getOntologyUndeployInstance().removeDataPropertyAssertion(instanceName1, dataPropertyName, "str");
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyReasoner().hasDataProperty(instanceName, dataPropertyName, "str"));
        ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);
        ontology.saveChanges();
    }
    
    @Test
    public void addRemoveDataPropertyAssertionFltTest() {
        ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
        ontology.getOntologyDeployInstance().addDataPropertyAssertion(instanceName1, dataPropertyName, 5.5f);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().hasDataProperty(instanceName1, dataPropertyName, 5.5f));
        ontology.getOntologyUndeployInstance().removeDataPropertyAssertion(instanceName1, dataPropertyName, 5.5f);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyReasoner().hasDataProperty(instanceName, dataPropertyName, 5.5f));
        ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);
        ontology.saveChanges();
    }
    @Test
    public void addRemoveDataPropertyAssertionBlnTest() {
        ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
        ontology.getOntologyDeployInstance().addDataPropertyAssertion(instanceName1, dataPropertyName, true);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().hasDataProperty(instanceName1, dataPropertyName, true));
        ontology.getOntologyUndeployInstance().removeDataPropertyAssertion(instanceName1, dataPropertyName, true);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyReasoner().hasDataProperty(instanceName, dataPropertyName, true));
        ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);
        ontology.saveChanges();
    }
    
    @Test
    public void removeInstancesOfTest() {
        ontology.getOntologyDeployClass().addClass(className);
        ontology.getOntologyDeployInstance().addInstanceOf(instanceName, className);
        ontology.getOntologyDeployInstance().addInstanceOf(instanceName2, className);
        ontology.saveChanges();
        ontology.getOntologyUndeployInstance().removeInstancesOfClass(className);
        ontology.saveChanges();
        List<String> strList = ontology.getOntologyReasoner().getInstancesFromClass(className, false); 
        Assert.assertTrue(strList == null || strList.isEmpty());
        ontology.getOntologyUndeployClass().removeClass(className);
        ontology.saveChanges();
    }
    
    @Test
    public void moveInstancesOfTest() {
        ontology.getOntologyDeployClass().addClass(className);
        ontology.getOntologyDeployClass().addClass(toClassName);
        ontology.getOntologyDeployInstance().addInstanceOf(instanceName, className);
        ontology.getOntologyDeployInstance().addInstanceOf(instanceName2, className);
        ontology.saveChanges();
        List<String> strList_ = ontology.getOntologyReasoner().getInstancesFromClass(className, false); 
        ontology.getOntologyDeployInstance().moveInstancesOfClass(className,toClassName);
        ontology.saveChanges();
        List<String> strList = ontology.getOntologyReasoner().getInstancesFromClass(className, false); 
        Assert.assertTrue(strList == null || strList.isEmpty());
        strList = ontology.getOntologyReasoner().getInstancesFromClass(toClassName, false); 
        Assert.assertTrue(strList.equals(strList_));
        ontology.getOntologyUndeployClass().removeClass(className);
        ontology.getOntologyUndeployClass().removeClass(toClassName);
        ontology.saveChanges();
    }
    
    @Test
    public void moveInstanceOfTest() {
        ontology.getOntologyDeployClass().addClass(className);
        ontology.getOntologyDeployClass().addClass(toClassName);
        ontology.getOntologyDeployInstance().addInstanceOf(instanceName, className);
        ontology.saveChanges();
        ontology.getOntologyInstance().moveInstanceOf(instanceName,className,toClassName);
        ontology.saveChanges();
        List<String> strList = ontology.getOntologyReasoner().getInstancesFromClass(toClassName, false); 
        Assert.assertTrue(strList.contains(instanceName));
        strList = ontology.getOntologyReasoner().getInstancesFromClass(className, false); 
        Assert.assertFalse(strList.contains(instanceName));
        ontology.getOntologyUndeployClass().removeClass(className);
        ontology.getOntologyUndeployClass().removeClass(toClassName);
        ontology.saveChanges();
    }
}
