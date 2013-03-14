/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api;

/**
 * This interface contains the methods related to object properties that could be invoked 
 * at the undeploy of an application.
 * 
 * @author anaozaki
 */
public interface UndeployObjectProperty {
    
    /**
     * Remove object property from ontology.
     * 
     * @param objectPropertyName Name of the object property.
     */
    public void removeObjectProperty(String objectPropertyName);
    
    /**
     * Remove the sub object property axiom from ontology. The object property 
     * itself is not removed.
     * 
     * @param subObjectPropertyName Name of the sub object property.
     * @param objectPropertyName Name of the object property.
     */
    public void removeSubObjectProperty(String subObjectPropertyName, String objectPropertyName);
    
    /**
     * Remove object property domain. The domain is not removed, only the 
     * axiom is removed.
     * 
     * @param objectPropertyName Name of the object property.
     * @param domainName Name of the domain.
     */
    public void removeObjectPropertyDomain(String objectPropertyName, String domainName);
    
    /**
     * Remove object property range. The range is not removed, only the axiom is 
     * removed.
     * 
     * @param objectPropertyName Name of the object property.
     * @param rangeName Name of the range.
     */
    public void removeObjectPropertyRange(String objectPropertyName, String rangeName);
    
     /**
     * Unset object property as being transitive. 
     * 
     * @param objectPropertyName Name of the object property.
     */
    public void removeTransitiveProperty(String objectPropertyName);
    public void removeSymmetricProperty(String objectPropertyName);
    public void removeInverseProperty(String objectPropertyName, String inversePropertyName);
    public boolean hasSubObjectProperty(String subObjectPropertyName, String objectPropertyName);
    public boolean hasObjectProperty(String objectPropertyName);
    public boolean hasObjectPropertyDomain(String objectPropertyName, String domainName);
    public boolean hasTransitiveProperty(String objectPropertyName);
    public boolean hasInverseProperty(String objectPropertyName, String inverseObjectPropertyName);
}
