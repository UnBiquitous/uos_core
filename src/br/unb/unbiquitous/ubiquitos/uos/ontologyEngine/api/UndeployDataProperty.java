/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api;

/**
 * This interface contains the methods related to data properties that could be invoked 
 * at the undeploy of an application.
 * 
 * @author anaozaki
 */
public interface UndeployDataProperty {
    
    /**
     * Remove data property from ontology. Domain and range axioms are also 
     * removed.
     * 
     * @param dataPropertyName Name of the data property. 
     */
    public void removeDataProperty(String dataPropertyName);
    
    /**
     * Remove sub data property axiom. The data property itself is not removed.
     * 
     * @param subDataPropertyName Name of the sub data property.
     * @param dataPropertyName Name of the data property.
     */
    public void removeSubDataProperty(String subDataPropertyName, String dataPropertyName);
    
    /**
     * Remove data property domain. The domain is not removed, only the 
     * axiom is removed.
     * 
     * @param dataPropertyName Name of the data property.
     * @param domainName Name of the domain.
     */
    public void removeDataPropertyDomain(String dataPropertyName, String domainName);
    
    /**
     * Remove data property range. The range is not removed, only the axiom is 
     * removed.
     * 
     * @param dataPropertyName Name of the data property.
     * @param rangeName Name of the range.
     */
    public void removeDataPropertyRange(String dataPropertyName, OntologyDataType dataType);
    public void removeDataPropertyBooleanRange(String dataPropertyName);
    public void removeDataPropertyFloatRange(String dataPropertyName);
    public void removeDataPropertyIntegerRange(String dataPropertyName);
    public void removeDataPropertyStringRange(String dataPropertyName);
    public boolean hasDataProperty(String dataPropertyName);
    public boolean hasSubDataProperty(String subDataPropertyName, String dataPropertyName);
    public boolean hasDataPropertyBooleanRange(String dataPropertyName);
    public boolean hasDataPropertyFloatRange(String dataPropertyName);
    public boolean hasDataPropertyIntegerRange(String dataPropertyName);
    public boolean hasDataPropertyDomain(String dataPropertyName, String domainName) ;
}
