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
public class OntologyClassTest implements UosApplication{
    String className = "ClasseA";
    String toClassName = "toClasseA";
    String subClassName = "ClasseB";
    String equivClassName = "ClasseEquiv";
    String disjClassName = "ClasseDisj";
    String classNameAnnotation = "classNameAnnotation";
    private static String DEFAULT_UBIQUIT_BUNDLE_FILE = "ubiquitos";
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
    public void setup() throws IOException{
        try {
        	new File("resources/owl/uoscontext.owl").createNewFile();
            ontology = new Ontology(resourceBundle);
            ontology.initializeOntology();
        } catch (ReasonerNotDefinedException ex) {
            Logger.getLogger(OntologyChangeManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }              
    }
    
    @Test
    public void addRemoveClassTest() {
        ontology.getOntologyDeployClass().addClass(className);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployClass().hasClass(className));
        ontology.getOntologyUndeployClass().removeClass(className);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyUndeployClass().hasClass(className));
    }
    
    @Test
    public void addRemoveSubClassTest() {
        ontology.getOntologyDeployClass().addClass(className);
        ontology.getOntologyDeployClass().addClass(subClassName);
        ontology.getOntologyDeployClass().addSubClass(subClassName, className);   
        ontology.saveChanges();       
        Assert.assertTrue(ontology.getOntologyDeployClass()
                .hasSubClass(subClassName, className));
        ontology.getOntologyUndeployClass().removeSubClass(subClassName, className);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyDeployClass()
                .hasSubClass(subClassName, className));
        ontology.getOntologyUndeployClass().removeClass(className);
        ontology.getOntologyUndeployClass().removeClass(subClassName);
        ontology.saveChanges();
    }
    
    @Test
    public void addRemoveEquivalentClassTest() {
        ontology.getOntologyDeployClass().addClass(className);
        ontology.getOntologyDeployClass().addClass(equivClassName);
        ontology.getOntologyDeployClass().addEquivalentClass(equivClassName, className);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployClass()
                .hasEquivalentClass(equivClassName, className));
        ontology.getOntologyUndeployClass().removeEquivalentClass(equivClassName, className);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyUndeployClass()
                .hasEquivalentClass(equivClassName, className));
        ontology.getOntologyUndeployClass().removeClass(className);
        ontology.getOntologyUndeployClass().removeClass(equivClassName);
        ontology.saveChanges();
    }

    @Test
    public void addRemoveDisjointClassTest() {
        ontology.getOntologyDeployClass().addClass(className);
        ontology.getOntologyDeployClass().addClass(disjClassName);
        ontology.getOntologyDeployClass().addDisjointClass(disjClassName, className);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployClass()
                .hasDisjointClass(disjClassName, className));
        ontology.getOntologyUndeployClass().removeDisjointClass(disjClassName, className);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyUndeployClass()
                .hasDisjointClass(disjClassName, className));
        ontology.getOntologyUndeployClass().removeClass(className);
        ontology.getOntologyUndeployClass().removeClass(disjClassName);
        ontology.saveChanges();
    }
    
    @Test
    public void moveSubClassTest() {
        ontology.getOntologyDeployClass().addClass(className);
        ontology.getOntologyDeployClass().addClass(toClassName);
        ontology.getOntologyDeployClass().addSubClass(subClassName, className);
        ontology.saveChanges();
        ontology.getOntologyDeployClass().moveSubClass(subClassName, className, toClassName);
        ontology.saveChanges();
        List<String> strList = ontology.getOntologyReasoner().getSubClassesFromClass(className, true);
        Assert.assertFalse(strList.contains(subClassName));
        strList = ontology.getOntologyReasoner().getSubClassesFromClass(toClassName, true);
        Assert.assertTrue(strList.contains(subClassName));
        ontology.getOntologyUndeployClass().removeClass(className);
        ontology.getOntologyUndeployClass().removeClass(toClassName);
        ontology.getOntologyUndeployClass().removeSubClass(subClassName, className);
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

    @Override
    public void init(OntologyDeploy ontology, String appId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void tearDown(OntologyUndeploy ontology) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Test
    public void init() {
//       ontology.getOntologyDeployDataProperty().addDataProperty("datainit");
//       ontology.saveChanges();
       ontology.getOntologyUndeployDataProperty().removeDataProperty("datainit");
       ontology.saveChanges();
//        ontology.getOntologyDeployClass().addClass("testeclasseinit");
//       ontology.saveChanges();
//       Assert.assertTrue(ontology.getOntologyDeployClass().hasClass("testeclasseinit"));
//       ontology.getOntologyDeployClass().addClass("testeclasseinit2");
//       ontology.saveChanges();
//       Assert.assertTrue(ontology.getOntologyDeployClass().hasClass("testeclasseinit"));
//       ontology.getOntologyDeployClass().addDisjointClass("testeclasseinit", "testeclasseinit2");
//       ontology.saveChanges();
//       Assert.assertTrue(ontology.getOntologyDeployClass().hasDisjointClass("testeclasseinit", "testeclasseinit2"));
//       ontology.getOntologyUndeployClass().removeClass("testeclasseinit");
//       ontology.saveChanges();
//       ontology.getOntologyUndeployClass().removeClass("testeclasseinit2");
//       ontology.saveChanges();

    }
}
