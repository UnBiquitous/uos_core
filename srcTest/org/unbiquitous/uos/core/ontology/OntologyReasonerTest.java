/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontology;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.unbiquitous.uos.core.ontologyEngine.Ontology;
import org.unbiquitous.uos.core.ontologyEngine.exception.ReasonerNotDefinedException;


/**
 *
 * @author anaozaki
 */
public class OntologyReasonerTest {
    String className = "ClassName";
    String subClassName = "SubClassName";
    String subSubClassName = "SubSubClassName";
    String instanceName = "InstanceName";
    String instanceName2 = "InstanceName2";
    String instanceName3 = "InstanceName3";
    String objectPropertyName = "ObjectPropertyName";
    String dataPropertyName = "DataPropertyName";
    String dataPropertyName2 = "DataPropertyName2";
    String dataPropertyName3 = "DataPropertyName3";
    String dataPropertyName4 = "DataPropertyName4";
    String equivClassName = "ClasseEquiv";
    String disjClassName = "ClasseDisj";
    String dataPropertyValue = "dataPropertyValue";
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
    public void setup(){
        try {
            ontology = new Ontology(resourceBundle);
            
        } catch (ReasonerNotDefinedException ex) {
            Logger.getLogger(OntologyReasonerTest.class.getName()).log(Level.SEVERE, null, ex);
        }              
    }
    
    public static OWLReasonerFactory ReasonerFactory(){
        return new org.semanticweb.HermiT.Reasoner.ReasonerFactory();
    }
    
    @Test
    public void isConsistentTest() {
        Assert.assertTrue(ontology.getOntologyReasoner().isConsistent());   
    }
    
    @Test
    public void isInstanceOfTest() {
        ontology.getOntologyDeployInstance().addInstanceOf(instanceName, className);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployInstance().hasInstanceOf(instanceName, className));
        Assert.assertTrue(ontology.getOntologyReasoner().isInstanceOf(instanceName, className));
        ontology.getOntologyUndeployInstance().removeInstanceOf(instanceName, className);
        ontology.saveChanges();
    }

    @Test
    public void isSubClassOfTest() {
        ontology.getOntologyDeployClass().addSubClass(subClassName, className);
        ontology.getOntologyDeployClass().addSubClass(subSubClassName, subClassName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().isSubClassOf(subSubClassName, className));
        ontology.getOntologyUndeployClass().removeSubClass(subClassName, className);
        ontology.getOntologyUndeployClass().removeSubClass(subSubClassName, subClassName);
        ontology.saveChanges();
    }

    @Test
    public void hasObjectPropertyTest() {
        ontology.getOntologyDeployObjectProperty().addObjectProperty(objectPropertyName);
        ontology.getOntologyDeployInstance().addObjectPropertyAssertion(instanceName, objectPropertyName, instanceName2);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().hasObjectProperty(instanceName, objectPropertyName, instanceName2));
        ontology.getOntologyUndeployObjectProperty().removeObjectProperty(objectPropertyName);
        ontology.getOntologyUndeployInstance().removeObjectPropertyAssertion(instanceName, objectPropertyName, instanceName2);
        ontology.saveChanges();
    }

    @Test
    public void hasDataPropertyTest() {
        ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
        ontology.getOntologyDeployInstance().addDataPropertyAssertion(instanceName, dataPropertyName,18);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().hasDataProperty(instanceName, dataPropertyName, 18));
        ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);
        ontology.getOntologyUndeployInstance().removeDataPropertyAssertion(instanceName, dataPropertyName, 18);
        ontology.saveChanges();
    }
    
    @Test
    public void hasDataPropertyTest2() {
        ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
        ontology.getOntologyDeployInstance().addDataPropertyAssertion(instanceName, dataPropertyName2,(float)1.8);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().hasDataProperty(instanceName, dataPropertyName2, (float)1.8));
        ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);
        ontology.getOntologyUndeployInstance().removeDataPropertyAssertion(instanceName, dataPropertyName2,(float) 1.8);
        ontology.saveChanges();
    }

    @Test
    public void hasDataPropertyTest3() {
        ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
        ontology.getOntologyDeployInstance().addDataPropertyAssertion(instanceName, dataPropertyName3,false);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().hasDataProperty(instanceName, dataPropertyName3, false));
        ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);
        ontology.getOntologyUndeployInstance().removeDataPropertyAssertion(instanceName, dataPropertyName3,false);
        ontology.saveChanges();
    }

    @Test
    public void hasDataPropertyTest4() {
        ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
        ontology.getOntologyDeployInstance().addDataPropertyAssertion(instanceName, dataPropertyName4,"Teste");
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().hasDataProperty(instanceName, dataPropertyName4, "Teste"));
        ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);
        ontology.getOntologyUndeployInstance().removeDataPropertyAssertion(instanceName, dataPropertyName4,"Teste");
        ontology.saveChanges();
    }

    
    @Test
    public void getInstancesFromClassTest(){
       ontology.getOntologyDeployInstance().addInstanceOf(instanceName, className);
       ontology.getOntologyDeployInstance().addInstanceOf(instanceName2, className);
       ontology.saveChanges();
       List<String> strList = ontology.getOntologyReasoner().getInstancesFromClass(className, false);
       List<String> strListTest = new ArrayList<String>();
       strListTest.add(instanceName);
       strListTest.add(instanceName2);
       Assert.assertTrue(strListTest.containsAll(strList));
       ontology.getOntologyUndeployInstance().removeInstancesOfClass(className);
       ontology.saveChanges();
    }
    
    @Test
    public void getSubClassesFromClassTest(){
       ontology.getOntologyDeployClass().addSubClass(subClassName, className);
       ontology.getOntologyDeployClass().addSubClass(subSubClassName, subClassName);
       ontology.saveChanges();
       List<String> strList = ontology.getOntologyReasoner().getSubClassesFromClass(className, false);
       List<String> strListTest = new ArrayList<String>();
       strListTest.add(subClassName);
       strListTest.add(subSubClassName);
       strListTest.add("Nothing");
       Assert.assertTrue(strListTest.containsAll(strList));
       ontology.getOntologyUndeployClass().removeSubClass(subClassName, className);
       ontology.getOntologyUndeployClass().removeSubClass(subSubClassName, subClassName);
       ontology.saveChanges();
    }
    
    @Test
    public void getSuperClassesFromClassTest(){       
       ontology.getOntologyDeployClass().addSubClass(subClassName, className);
       ontology.getOntologyDeployClass().addSubClass(subSubClassName, subClassName);
       ontology.saveChanges();
       List<String> strList = ontology.getOntologyReasoner().getSuperClassesFromClass(subSubClassName, false);
       List<String> strListTest = new ArrayList<String>();
       strListTest.add("Thing");
       strListTest.add(subClassName);
       strListTest.add(className);
       Assert.assertTrue(strList.containsAll(strListTest)); 
       ontology.getOntologyUndeployClass().removeSubClass(subClassName, className);
       ontology.getOntologyUndeployClass().removeSubClass(subSubClassName, subClassName);
       ontology.saveChanges();
    }
    
    @Test
    public void disjointClassTest() {
        ontology.getOntologyDeployClass().addClass(className);
        ontology.getOntologyDeployClass().addClass(disjClassName);
        ontology.getOntologyDeployClass().addDisjointClass(disjClassName, className);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().areDisjointClasses(className, disjClassName));
        ontology.getOntologyUndeployClass().removeDisjointClass(disjClassName, className);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyReasoner().areDisjointClasses(className, disjClassName));
        ontology.getOntologyUndeployClass().removeClass(className);
        ontology.getOntologyUndeployClass().removeClass(disjClassName);
        ontology.saveChanges();
    }
    
    @Test
    public void equivalentClassTest() {
        ontology.getOntologyDeployClass().addClass(className);
        ontology.getOntologyDeployClass().addClass(equivClassName);
        ontology.getOntologyDeployClass().addEquivalentClass(equivClassName, className);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyReasoner().areEquivalentClasses(className, equivClassName));
        ontology.getOntologyUndeployClass().removeEquivalentClass(equivClassName, className);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyReasoner().areEquivalentClasses(className, equivClassName));
        ontology.getOntologyUndeployClass().removeClass(className);
        ontology.getOntologyUndeployClass().removeClass(equivClassName);
        ontology.saveChanges();
    }
    
    @Test
    public void getDataPropertyValues() {
       ontology.getOntologyDeployDataProperty().addDataProperty(dataPropertyName);
       ontology.getOntologyDeployInstance().addDataPropertyAssertion(instanceName, dataPropertyName, dataPropertyValue);
       ontology.getOntologyDeployInstance().addDataPropertyAssertion(instanceName, dataPropertyName, dataPropertyValue+"1");
       ontology.saveChanges();
       List<String> strList = ontology.getOntologyReasoner().getDataPropertyValues(instanceName, dataPropertyName);
       List<String> strListTest = new ArrayList<String>();
       strListTest.add(dataPropertyValue);
       strListTest.add(dataPropertyValue+"1");
       if(strList == null)
           strList = new ArrayList<String>();
       Assert.assertTrue(strList.containsAll(strListTest)); 
       ontology.getOntologyUndeployInstance().removeDataPropertyAssertion(instanceName, dataPropertyName, dataPropertyValue);
       ontology.getOntologyUndeployInstance().removeDataPropertyAssertion(instanceName, dataPropertyName, dataPropertyValue+"1");
       ontology.getOntologyUndeployDataProperty().removeDataProperty(dataPropertyName);
       ontology.saveChanges();
    }
    
    @Test
    public void getObjectPropertyValues() {
       ontology.getOntologyDeployObjectProperty().addObjectProperty(objectPropertyName);
       ontology.getOntologyDeployInstance().addObjectPropertyAssertion(instanceName, objectPropertyName, instanceName2);
       ontology.getOntologyDeployInstance().addObjectPropertyAssertion(instanceName, objectPropertyName, instanceName3);
       ontology.saveChanges();
       List<String> strList = ontology.getOntologyReasoner().getObjectPropertyValues(instanceName, objectPropertyName);
       List<String> strListTest = new ArrayList<String>();
       strListTest.add(instanceName2);
       strListTest.add(instanceName3);
       if(strList == null)
           strList = new ArrayList<String>();
       Assert.assertTrue(strList.containsAll(strListTest)); 
       ontology.getOntologyUndeployInstance().removeObjectPropertyAssertion(instanceName, objectPropertyName, instanceName2);
       ontology.getOntologyUndeployInstance().removeObjectPropertyAssertion(instanceName, objectPropertyName, instanceName3);
       ontology.getOntologyUndeployObjectProperty().removeObjectProperty(objectPropertyName);
       ontology.saveChanges();
    }
}
