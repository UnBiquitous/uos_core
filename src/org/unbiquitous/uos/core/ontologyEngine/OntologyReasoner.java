package org.unbiquitous.uos.core.ontologyEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.ontologyEngine.api.StartReasoner;
import org.unbiquitous.uos.core.ontologyEngine.exception.ReasonerNotDefinedException;


/**
 *
 * @author anaozaki
 */
public class OntologyReasoner implements StartReasoner {

    private OWLOntologyManager manager;
    private OWLReasonerFactory reasonerFactory;
    OWLReasonerConfiguration config;
    OWLReasoner reasoner;
    private OWLOntology localContext;
    private static final Logger logger = UOSLogging.getLogger();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock read = readWriteLock.readLock();
    private final Lock write = readWriteLock.writeLock();
    String prefix = null;
    private static String REASONER_FACTORY = "ubiquitos.ontology.reasonerFactory";

    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    private OWLReasonerFactory createReasonerFactory(ResourceBundle resourceBundle) {
        try {
            
            if (resourceBundle.containsKey(REASONER_FACTORY)) {
                Class clazz = Class.forName(resourceBundle.getString(REASONER_FACTORY));
                return (OWLReasonerFactory) clazz.getMethod("ReasonerFactory").invoke(null, (Object[]) null);
            }
           
        } catch (Exception e) {
            
            logger.log(Level.SEVERE,"Not possible to create ReasonerFactory",e);        
        } 
           
        return null;
    }

    public OntologyReasoner(OWLOntologyManager manager, OWLOntology localContext, ResourceBundle resourceBundle) throws ReasonerNotDefinedException {
        
        try {
        write.lock();
       
        prefix = localContext.getOntologyID().getOntologyIRI() + "#";
        this.manager = manager;
        this.localContext = localContext;
        if ((reasonerFactory = createReasonerFactory(resourceBundle)) == null) {
            throw new ReasonerNotDefinedException("Property file does not contain ontology reasoner defined.");
        }
        config = new SimpleConfiguration();
        
            reasoner = reasonerFactory.createReasoner(localContext);
            reasoner.precomputeInferences();
        } finally {
            write.unlock();
        }
    }

    @Override
    public boolean isConsistent() {
        read.lock();
        boolean rs = false;
        try {
            rs = reasoner.isConsistent();
        } finally {
            read.unlock();
        }
        return rs;
    }

    @Override
    public boolean isInstanceOf(String instanceName, String className) {
        boolean rs = false;
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLAxiom axiom = manager.getOWLDataFactory().getOWLClassAssertionAxiom(cls, instance);
        read.lock();
        try {
            reasoner.flush();
            rs = reasoner.isEntailed(axiom);
        } finally {
            read.unlock();
        }
        return rs;
    }

    @Override
    public boolean isSubClassOf(String subClassName, String className) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass subCls = factory.getOWLClass(IRI.create(prefix + subClassName));
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLAxiom axiom = manager.getOWLDataFactory().getOWLSubClassOfAxiom(subCls, cls);
        read.lock();
        boolean rs = false;
        try {
            reasoner.flush();
            rs = reasoner.isEntailed(axiom);
        } finally {
            read.unlock();
        }
        return rs;
    }

    @Override
    public boolean hasObjectProperty(String instanceName1, String objectPropertyName, String instanceName2) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLNamedIndividual instance1 = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName1));
        OWLObjectProperty obj = factory.getOWLObjectProperty(IRI.create(prefix + objectPropertyName));
        OWLNamedIndividual instance2 = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName2));
        OWLAxiom axiom = manager.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(obj, instance1, instance2);
        read.lock();
        boolean rs = false;
        try {
            reasoner.flush();
            rs = reasoner.isEntailed(axiom);
        } finally {
            read.unlock();
        }
        return rs;
    }

    @Override
    public boolean hasDataProperty(String instanceName1, String dataPropertyName, String string) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLNamedIndividual instance1 = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName1));
        OWLDataProperty dat = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = manager.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(dat, instance1, string);
        read.lock();
        boolean rs = false;
        try {
            reasoner.flush();
            rs = reasoner.isEntailed(axiom);
        } finally {
            read.unlock();
        }
        return rs;
    }

    @Override
    public boolean hasDataProperty(String instanceName1, String dataPropertyName, boolean booleanValue) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLNamedIndividual instance1 = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName1));
        OWLDataProperty dat = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = manager.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(dat, instance1, booleanValue);
        read.lock();
        boolean rs = false;
        try {
            reasoner.flush();
            rs = reasoner.isEntailed(axiom);
        } finally {
            read.unlock();
        }
        return rs;
    }

    @Override
    public boolean hasDataProperty(String instanceName1, String dataPropertyName, int intNumber) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLNamedIndividual instance1 = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName1));
        OWLDataProperty dat = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = manager.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(dat, instance1, intNumber);
        read.lock();
        boolean rs = false;
        try {
            reasoner.flush();
            rs = reasoner.isEntailed(axiom);
        } finally {
            read.unlock();
        }
        return rs;
    }

    @Override
    public boolean hasDataProperty(String instanceName1, String dataPropertyName, float floatNumber) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLNamedIndividual instance1 = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName1));
        OWLDataProperty dat = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = manager.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(dat, instance1, floatNumber);
        read.lock();
        boolean rs = false;
        try {
            reasoner.flush();
            rs = reasoner.isEntailed(axiom);
        } finally {
            read.unlock();
        }
        return rs;
    }

    @Override
    public List<String> getInstancesFromClass(String className, boolean direct) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        Set<OWLNamedIndividual> list;
        read.lock();
        try {
            reasoner.flush();
            list = reasoner.getInstances(cls, direct).getFlattened();
        } finally {
            read.unlock();
        }
        List<String> strList = new ArrayList<String>();
        for (OWLNamedIndividual ind : list) {
            strList.add(ind.getIRI().getFragment());
        }
        return strList;
    }

    @Override
    public List<String> getSubClassesFromClass(String className, boolean direct) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        Set<OWLClass> list;
        read.lock();
        try {
            reasoner.flush();
            list = reasoner.getSubClasses(cls, direct).getFlattened();
        } finally {
            read.unlock();
        }
        List<String> strList = new ArrayList<String>();
        for (OWLClass clss : list) {
            strList.add(clss.getIRI().getFragment());
        }
        return strList;
    }

    @Override
    public List<String> getSuperClassesFromClass(String className, boolean direct) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        Set<OWLClass> list;
        read.lock();
        try {
            reasoner.flush();
            list = reasoner.getSuperClasses(cls, direct).getFlattened();
        } finally {
            read.unlock();
        }
        List<String> strList = new ArrayList<String>();
        for (OWLClass clss : list) {
            strList.add(clss.getIRI().getFragment());
        }

        return strList;
    }

    @Override
    public List<String> getSubDataPropertiesFromDataProperty(String dataPropertyName, boolean direct) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dtp = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        Set<OWLDataProperty> list;
        read.lock();
        try {
            reasoner.flush();
            list = reasoner.getSubDataProperties(dtp, direct).getFlattened();
        } finally {
            read.unlock();
        }
        List<String> strList = new ArrayList<String>();
        for (OWLDataProperty dtps : list) {
            strList.add(dtps.getIRI().getFragment());
        }
        return strList;
    }

    @Override
    public List<String> getSuperDataPropertiesFromDataProperty(String dataPropertyName, boolean direct) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dtp = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        Set<OWLDataProperty> list;
        read.lock();
        try {
            reasoner.flush();
            list = reasoner.getSuperDataProperties(dtp, direct).getFlattened();
        } finally {
            read.unlock();
        }
        List<String> strList = new ArrayList<String>();
        for (OWLDataProperty dtps : list) {
            strList.add(dtps.getIRI().getFragment());
        }
        return strList;
    }

    @Override
    public List<String> getDataPropertyValues(String instanceName, String dataPropertyName) {
        if ((localContext.containsDataPropertyInSignature(IRI.create(prefix + dataPropertyName)))
                && (localContext.containsIndividualInSignature(IRI.create(prefix + instanceName)))) {
            OWLDataFactory factory = manager.getOWLDataFactory();
            OWLNamedIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
            OWLDataProperty dat = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
            if (dat != null && instance != null) {
                Set<OWLLiteral> list = null;
                read.lock();
                try {
                    reasoner.flush();
                    list = reasoner.getDataPropertyValues(instance, dat);
                } finally {
                    read.unlock();
                }

                List<String> strList = new ArrayList<String>();
                for (OWLLiteral li : list) {
                    strList.add(li.getLiteral());
                }
                return strList;
            }
        }
        return null;
    }

    @Override
    public List<String> getSubObjectPropertiesFromObjectProperty(String objectPropertyName, boolean direct) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty op = factory.getOWLObjectProperty(IRI.create(prefix + objectPropertyName));
        Set<OWLObjectPropertyExpression> list;
        read.lock();
        try {
            reasoner.flush();
            list = reasoner.getSubObjectProperties(op, direct).getFlattened();
        } finally {
            read.unlock();
        }
        List<String> strList = new ArrayList<String>();
        for (OWLObjectPropertyExpression ops : list) {
            strList.add(ops.asOWLObjectProperty().getIRI().getFragment());
        }
        return strList;
    }

    @Override
    public List<String> getSuperObjectPropertiesFromObjectProperty(String objectPropertyName, boolean direct) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty op = factory.getOWLObjectProperty(IRI.create(prefix + objectPropertyName));
        Set<OWLObjectPropertyExpression> list;
        read.lock();
        try {
            reasoner.flush();
            list = reasoner.getSuperObjectProperties(op, direct).getFlattened();
        } finally {
            read.unlock();
        }
        List<String> strList = new ArrayList<String>();
        for (OWLObjectPropertyExpression ops : list) {
            strList.add(ops.asOWLObjectProperty().getIRI().getFragment());
        }
        return strList;
    }

    @Override
    public List<String> getObjectPropertyValues(String instanceName, String objectPropertyName) {
        if ((localContext.containsObjectPropertyInSignature(IRI.create(prefix + objectPropertyName)))
                && (localContext.containsIndividualInSignature(IRI.create(prefix + instanceName)))) {
            OWLDataFactory factory = manager.getOWLDataFactory();
            OWLNamedIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
            OWLObjectProperty obj = factory.getOWLObjectProperty(IRI.create(prefix + objectPropertyName));
            if (obj != null && instance != null) {
                NodeSet<OWLNamedIndividual> list = null;
                read.lock();
                try {
                    reasoner.flush();
                    //TODO Testar instance e obj ...
                    list = reasoner.getObjectPropertyValues(instance, obj);
                } finally {
                    read.unlock();
                }
                Set<OWLNamedIndividual> values = list.getFlattened();
                List<String> strList = new ArrayList<String>();
                for (OWLNamedIndividual ind : values) {
                    strList.add(ind.getIRI().getFragment());
                }
                return strList;
            }
        }
        return null;
    }

    @Override
    public boolean areDisjointClasses(String className1, String className2) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className1));
        OWLClass cls2 = factory.getOWLClass(IRI.create(prefix + className2));
        OWLAxiom axiom = manager.getOWLDataFactory().getOWLDisjointClassesAxiom(cls, cls2);
        read.lock();
        boolean rs = false;
        try {
            reasoner.flush();
            rs = reasoner.isEntailed(axiom);
        } finally {
            read.unlock();
        }
        return rs;
    }

    @Override
    public boolean areEquivalentClasses(String className1, String className2) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className1));
        OWLClass cls2 = factory.getOWLClass(IRI.create(prefix + className2));
        OWLAxiom axiom = manager.getOWLDataFactory().getOWLEquivalentClassesAxiom(cls, cls2);
        read.lock();
        boolean rs = false;
        try {
            reasoner.flush();
            rs = reasoner.isEntailed(axiom);
        } finally {
            read.unlock();
        }
        return rs;
    }

    public boolean isEntailed(OWLAxiom axiom) {
        read.lock();
        boolean rs = false;
        try {
            reasoner.flush();
            rs = reasoner.isEntailed(axiom);
        } finally {
            read.unlock();
        }
        return rs;
    }
}
