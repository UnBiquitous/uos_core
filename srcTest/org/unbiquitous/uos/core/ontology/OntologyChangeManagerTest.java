/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontology;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.UosApplication;
import org.unbiquitous.uos.core.ontologyEngine.Ontology;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDeploy;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyStart;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyUndeploy;
import org.unbiquitous.uos.core.ontologyEngine.exception.ReasonerNotDefinedException;


/**
 *
 * @author anaozaki
 */
public class OntologyChangeManagerTest implements UosApplication{
    String className1 = "Classe1";
    String className2 = "Classe2";
    String className3 = "Classe3";
    String className4 = "Classe4";
    String className5 = "Classe5";
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
            ontology.initializeOntology();
        } catch (ReasonerNotDefinedException ex) {
            Logger.getLogger(OntologyChangeManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }              
    }
    
    @Test
    public void validateCycleSubClassChangeTest() {   
       ontology.getOntologyDeployClass().addClass(className1);
       ontology.getOntologyDeployClass().addClass(className2);
       ontology.getOntologyDeployClass().addClass(className3);
       ontology.getOntologyDeployClass().addSubClass(className1, className2); 
       ontology.getOntologyDeployClass().addSubClass(className2, className3);
       //ontology.saveChanges();
       ontology.getOntologyDeployClass().addSubClass(className3, className1); 
       ontology.saveChanges();
       Assert.assertTrue(ontology.getOntologyDeployClass().hasSubClass(className1, className2)); 
       Assert.assertTrue(ontology.getOntologyDeployClass().hasSubClass(className2, className3));   
       Assert.assertFalse(ontology.getOntologyDeployClass().hasSubClass(className3, className1));     
       ontology.getOntologyUndeployClass().removeClass(className1);
       ontology.getOntologyUndeployClass().removeClass(className2);
       ontology.getOntologyUndeployClass().removeClass(className3);
       ontology.saveChanges();
   
    }
    
@Test
    public void validateRedundancySubClassChangeTest() {   
       ontology.getOntologyDeployClass().addClass(className1);
       ontology.getOntologyDeployClass().addClass(className2);
       ontology.getOntologyDeployClass().addClass(className3);
       ontology.getOntologyDeployClass().addSubClass(className1, className2); 
       ontology.getOntologyDeployClass().addSubClass(className3, className2);
       ontology.saveChanges();
       ontology.getOntologyDeployClass().addSubClass(className3, className1); 
       ontology.saveChanges();
       Assert.assertTrue(ontology.getOntologyDeployClass().hasSubClass(className1, className2)); 
       Assert.assertTrue(ontology.getOntologyDeployClass().hasSubClass(className3, className1));
       Assert.assertFalse(ontology.getOntologyDeployClass().hasSubClass(className3, className2));
       ontology.getOntologyUndeployClass().removeClass(className1);
       ontology.getOntologyUndeployClass().removeClass(className2);
       ontology.getOntologyUndeployClass().removeClass(className3);
       ontology.saveChanges();
    }
    
    @Test
    public void validateDisjointClassChangeTest() {
        ontology.getOntologyDeployClass().addClass(className1);
        ontology.getOntologyDeployClass().addClass(className2);
        ontology.getOntologyDeployClass().addClass(className3);
        ontology.getOntologyDeployClass().addDisjointClass(className1, className2);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployClass().hasDisjointClass(className1, className2));
        ontology.getOntologyDeployClass().addDisjointClass(className3, className3);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyDeployClass().hasDisjointClass(className3, className3));
        ontology.getOntologyUndeployClass().removeClass(className1);
        ontology.getOntologyUndeployClass().removeClass(className2);
        ontology.getOntologyUndeployClass().removeClass(className3);
        ontology.saveChanges();
    }

    @Override
    public void start(Gateway gateway, OntologyStart ontology) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stop() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Test
    public void init() {
       ontology.getOntologyDeployDataProperty().addDataProperty("datainit");
       ontology.saveChanges();
//       ontology.getOntologyUndeployDataProperty().removeDataProperty("datainit");
//       ontology.saveChanges();
       
//       ontology.getOntologyDeployClass().addClass("testeclasseinit");
//       ontology.saveChanges();
//       Assert.assertTrue(ontology.getOntologyDeployClass().hasClass("testeclasseinit"));
//       ontology.getOntologyDeployClass().addClass("testeclasseinit2");
//       ontology.saveChanges();
//       Assert.assertTrue(ontology.getOntologyDeployClass().hasClass("testeclasseinit"));
//       ontology.getOntologyDeployClass().addDisjointClass("testeclasseinit", "testeclasseinit2");
//       ontology.saveChanges();
//       Assert.assertTrue(ontology.getOntologyDeployClass().hasDisjointClass("testeclasseinit", "testeclasseinit2"));
//       
//       ontology.getOntologyUndeployClass().removeClass("testeclasseinit");
//       ontology.saveChanges();
//       ontology.getOntologyUndeployClass().removeClass("testeclasseinit2");
//       ontology.saveChanges();

    }

    @Override
    public void tearDown(OntologyUndeploy ontology) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(OntologyDeploy ontology, InitialProperties props, String appId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
