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
import org.unbiquitous.uos.core.ontologyEngine.Ontology;
import org.unbiquitous.uos.core.ontologyEngine.exception.ReasonerNotDefinedException;


/**
 *
 * @author anaozaki
 */
public class OntologyObjectPropertyTest {
    String objectPropertyName = "ObjectPropertyA";
    String toObjectPropertyName = "toObjectPropertyA";
    String subObjectPropertyName = "ObjectPropertyB";
    String rangeName = "range";
    String domainName = "domain";
    String inverseName = "inverse";
    
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
            ontology = new Ontology(resourceBundle);
            
        } catch (ReasonerNotDefinedException ex) {
            Logger.getLogger(OntologyObjectPropertyTest.class.getName()).log(Level.SEVERE, null, ex);
        }              
    }
    
    @Test
    public void addObjectPropertyTest() {
        ontology.getOntologyDeployObjectProperty().addObjectProperty(objectPropertyName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployObjectProperty().hasObjectProperty(objectPropertyName));
    }

    @Test
    public void addSubObjectPropertyTest() {
        ontology.getOntologyDeployObjectProperty().addSubObjectProperty(subObjectPropertyName, objectPropertyName);
                ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployObjectProperty()
                .hasSubObjectProperty(subObjectPropertyName, objectPropertyName));
    }

    @Test
    public void addDomainTest() {
        ontology.getOntologyDeployObjectProperty().addObjectPropertyDomain(objectPropertyName, domainName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployObjectProperty().
                hasObjectPropertyDomain(objectPropertyName, domainName));
    }

    @Test
    public void addRangeTest() {
        ontology.getOntologyDeployObjectProperty().addObjectPropertyRange(objectPropertyName, rangeName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployObjectProperty().hasObjectPropertyRange(objectPropertyName, rangeName));
    }
    
    @Test
    public void addTransitiveTest() {
        ontology.getOntologyDeployObjectProperty().addTransitiveProperty(objectPropertyName);
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployObjectProperty().hasTransitiveProperty(objectPropertyName));
    }

    @Test
    public void removeDomainTest() {
        ontology.getOntologyUndeployObjectProperty().removeObjectPropertyDomain(objectPropertyName, domainName);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyDeployObjectProperty().
                hasObjectPropertyDomain(objectPropertyName, domainName));
    }

    @Test
    public void removeRangeTest() {
        ontology.getOntologyUndeployObjectProperty().removeObjectPropertyRange(objectPropertyName, rangeName);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyDeployObjectProperty().
                hasObjectPropertyRange(objectPropertyName, rangeName));
    }

    @Test
    public void removeSubObjectPropertyTest() {
        ontology.getOntologyUndeployObjectProperty().removeSubObjectProperty(subObjectPropertyName, objectPropertyName);
                ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyUndeployObjectProperty()
                .hasSubObjectProperty(subObjectPropertyName, objectPropertyName));
    }
     
    @Test
    public void removeObjectPropertyTest() {
        ontology.getOntologyUndeployObjectProperty().removeObjectProperty(objectPropertyName);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyDeployObjectProperty().hasObjectProperty(objectPropertyName));
    }
    
    @Test
    public void removeTransitiveTest() { 
        ontology.getOntologyUndeployObjectProperty().removeTransitiveProperty(objectPropertyName);
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyDeployObjectProperty().hasTransitiveProperty(objectPropertyName));
    }
    
    @Test
    public void addRemoveInverseTest() {
        ontology.getOntologyDeployObjectProperty().addInverseProperty(objectPropertyName, inverseName );
        ontology.saveChanges();
        Assert.assertTrue(ontology.getOntologyDeployObjectProperty().hasInverseProperty(objectPropertyName, inverseName));
        ontology.getOntologyUndeployObjectProperty().removeInverseProperty(objectPropertyName, inverseName );
        ontology.saveChanges();
        Assert.assertFalse(ontology.getOntologyDeployObjectProperty().hasInverseProperty(objectPropertyName, inverseName));
    }
    
    @Test
    public void moveSubObjectPropertyTest() {
        ontology.getOntologyDeployObjectProperty().addObjectProperty(objectPropertyName);
        ontology.getOntologyDeployObjectProperty().addObjectProperty(toObjectPropertyName);
        ontology.getOntologyDeployObjectProperty().addSubObjectProperty(subObjectPropertyName, objectPropertyName);
        ontology.saveChanges();
        ontology.getOntologyDeployObjectProperty().moveSubObjectProperty(subObjectPropertyName, objectPropertyName, toObjectPropertyName);
        ontology.saveChanges();
        List<String> strList = ontology.getOntologyReasoner().getSubObjectPropertiesFromObjectProperty(objectPropertyName, true);
        Assert.assertFalse(strList.contains(subObjectPropertyName));
        strList = ontology.getOntologyReasoner().getSubObjectPropertiesFromObjectProperty(toObjectPropertyName, true);
        Assert.assertTrue(strList.contains(subObjectPropertyName));
        ontology.getOntologyUndeployObjectProperty().removeObjectProperty(objectPropertyName);
        ontology.getOntologyUndeployObjectProperty().removeObjectProperty(toObjectPropertyName);
        ontology.getOntologyUndeployObjectProperty().removeSubObjectProperty(subObjectPropertyName, objectPropertyName);
        ontology.saveChanges();
    }
}
