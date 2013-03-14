/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api;

/**
 * This interface contains the methods related to object properties that could
 * be invoked at the deploy of an application.
 * 
 * @author anaozaki
 */
public interface DeployObjectProperty {
    
    /**
     * Add object property to ontology. 
     * 
     * @param objectPropertyName Name of the object property.
     */
    public void addObjectProperty(String objectPropertyName);
    
    /**
     * Add sub object property to ontology. 
     * 
     * @param subObjectPropertyName Name of the sub object property.
     * @param objectPropertyName Name of the object property.
     */
    public void addSubObjectProperty(String subObjectPropertyName, String objectPropertyName);
    
    /**
     * Add object property domain, which refers to the subject. 
     * 
     * @param objectPropertyName Name of the object property.
     * @param domainName Name of the concept that will be the domain.
     */
    public void addObjectPropertyDomain(String objectPropertyName, String domainName);
    
    /**
     * Add object property range, which refers to the object. 
     * 
     * @param objectPropertyName Name of the object property.
     * @param rangeName Name of the concept that will be the range.
     */
    public void addObjectPropertyRange(String objectPropertyName, String rangeName);
    
    /**
     * Set object property as being transitive. 
     * 
     * @param objectPropertyName Name of the object property.
     */
    public void addTransitiveProperty(String objectPropertyName);
    public void addSymmetricProperty(String objectPropertyName);
    public void addInverseProperty(String objectPropertyName, String inversePropertyName);
    public void moveSubObjectProperty(String subObjectPropertyName, String fromObjectPropertyName, String toObjectPropertyName);
    public void changeObjectPropertyDomain(String objectPropertyName, String oldDomainName, String newDomainName);
    public void changeObjectPropertyRange(String objectPropertyName, String oldRangeName, String newRangeName);
    public boolean hasSubObjectProperty(String subObjectPropertyName, String objectPropertyName);
    public boolean hasObjectProperty(String objectPropertyName);
    public boolean hasObjectPropertyRange(String objectPropertyName, String rangeName);
    public boolean hasObjectPropertyDomain(String objectPropertyName, String domainName);
    public boolean hasTransitiveProperty(String objectPropertyName);
    public boolean hasInverseProperty(String objectPropertyName, String inverseObjectPropertyName);
    
}
