/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api;

/**
 * This interface contains the methods related to data properties that could be 
 * invoked at the deploy of an application.
 * 
 * @author anaozaki
 */
public interface DeployDataProperty {
    
    /**
     * Add data property to ontology. 
     * 
     * @param dataPropertyName Name of the data property.
     */
    public void addDataProperty(String dataPropertyName);
    
    /**
     * Add sub data property to ontology. 
     * 
     * @param subDataPropertyName Name of the sub data property.
     * @param dataPropertyName Name of the data property.
     */
    public void addSubDataProperty(String subDataPropertyName, String dataPropertyName);
    
    /**
     * Add property's domain, which refers to the subject.  
     * 
     * @param dataPropertyName Name of the data property.
     * @param domainName Name of the concept that will be the domain.
     */
    public void addDataPropertyDomain(String dataPropertyName, String domainName);
    
    /**
     * Add property's range, which refers to the object.  
     * 
     * @param dataPropertyName Name of the data property.
     * @param rangeName Name of the concept that will be the range.
     */
    public void addDataPropertyRange(String dataPropertyName, OntologyDataType dataType);
    
    /**
     * Add property's range, which is a boolean.  
     * 
     * @param dataPropertyName Name of the data property.
     */
    public void addDataPropertyBooleanRange(String dataPropertyName);
    
    /**
     * Add property's range, which is a integer.  
     * 
     * @param dataPropertyName Name of the data property.
     */
    public void addDataPropertyIntegerRange(String dataPropertyName);
    
    /**
     * Add property's range, which is a float.  
     * 
     * @param dataPropertyName Name of the data property.
     */
    public void addDataPropertyFloatRange(String dataPropertyName);
    public void addDataPropertyStringRange(String dataPropertyName);
    public void moveSubDataProperty(String subDataPropertyName, String fromDataPropertyName, String toDataPropertyName); 
    public void changeDataPropertyDomain(String dataPropertyName, String oldDomainName, String newDomainName);
    public void changeDataPropertyRange(String dataPropertyName, OntologyDataType oldRangeName, OntologyDataType newRangeName);
    public boolean hasDataProperty(String dataPropertyName);
    public boolean hasSubDataProperty(String subDataPropertyName, String dataPropertyName);
    public boolean hasDataPropertyBooleanRange(String dataPropertyName);
    public boolean hasDataPropertyIntegerRange(String dataPropertyName);
    public boolean hasDataPropertyFloatRange(String dataPropertyName);
    public boolean hasDataPropertyStringRange(String dataPropertyName);
    public boolean hasDataPropertyDomain(String dataPropertyName, String domainName) ;
}
