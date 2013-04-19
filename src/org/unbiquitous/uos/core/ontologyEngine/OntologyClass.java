/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.ontologyEngine.api.DeployClass;
import org.unbiquitous.uos.core.ontologyEngine.api.UndeployClass;
import org.unbiquitous.uos.core.ontologyEngine.exception.CycleException;
import org.unbiquitous.uos.core.ontologyEngine.exception.DeclarationException;
import org.unbiquitous.uos.core.ontologyEngine.exception.DisjunctionException;
import org.unbiquitous.uos.core.ontologyEngine.exception.RedundancyException;
import org.unbiquitous.uos.core.ontologyEngine.exception.RemovalException;


/**
 *
 * @author anaozaki
 */
public class OntologyClass implements DeployClass, UndeployClass {

    private static final Logger logger = Logger.getLogger(OntologyClass.class);
    private OWLOntology localContext;
    private OWLOntologyManager manager;
    private OntologyChangeManager changeManager;
    String prefix;

    OntologyClass(OWLOntologyManager manager, OWLOntology localContext, OntologyChangeManager changeManager) {
        this.manager = manager;
        this.localContext = localContext;
        this.changeManager = changeManager;
        prefix = localContext.getOntologyID().getOntologyIRI() + "#";
    }

    @Override
    public void addClass(String className) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLAxiom axiom = factory.getOWLDeclarationAxiom(cls);
        axiom = changeManager.getAnnotatedAxiom(axiom);

        try {
            synchronized (this) {
                AddAxiom addAxiom = new AddAxiom(localContext, axiom);
                changeManager.validateAddChange(axiom);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            AddAxiom addAxiom_ = new AddAxiom(localContext, axiom);
            axiom.getAnnotations().add(changeManager.getAxiomAnnotation(axiom));
            manager.applyChange(addAxiom_);
            logger.info(ex.getMessage() + " Change ( addClass " + className + " ) will not be applied. Added annotation.");
        }
    }

    @Override
    public void addSubClass(String subClassName, String className) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLClass subCls = factory.getOWLClass(IRI.create(prefix + subClassName));
        OWLAxiom axiom = factory.getOWLSubClassOfAxiom(subCls, cls);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateAddSubClassChange(axiom, subCls, cls);
                manager.applyChange(addAxiom);
            }
        } catch (RedundancyException ex) {
            logger.info(ex.getMessage());
            OWLClass cls_ = factory.getOWLClass(IRI.create(prefix + ex.getCls()));
            OWLClass subCls_ = factory.getOWLClass(IRI.create(prefix + ex.getSubCls()));
            OWLAxiom axiom_ = factory.getOWLSubClassOfAxiom(subCls_, cls_);
            axiom_ = changeManager.getAnnotatedAxiom(axiom_);
            RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom_);
            synchronized (this) {
                manager.applyChange(removeAxiom);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            AddAxiom addAxiom_ = new AddAxiom(localContext, axiom);
            axiom.getAnnotations().add(changeManager.getAxiomAnnotation(axiom));
            manager.applyChange(addAxiom_);
            logger.info(ex.getMessage() + " Change ( addSubClass "
                    + subClassName + " of " + className + " ) will not be applied. Added annotation. ");
        } catch (CycleException ex) {
            logger.error(ex.getMessage() + " Change (" + subClassName
                    + " subClassOf " + className + ") will not be applied.");
        }
    }

    @Override
    public void addEquivalentClass(String className, String equivClassName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLClass equivCls = factory.getOWLClass(IRI.create(prefix + equivClassName));
        OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(cls, equivCls);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);
    }

    @Override
    public void addDisjointClass(String className, String disjClassName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLClass distCls = factory.getOWLClass(IRI.create(prefix + disjClassName));
        OWLAxiom axiom = factory.getOWLDisjointClassesAxiom(cls, distCls);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateDisjointChange(addAxiom, cls, distCls);
                manager.applyChange(addAxiom);
            }
        } catch (DisjunctionException ex) {
            logger.error(ex.getMessage() + " Change (" + className + " disjointWith " + disjClassName + ") will not be applied.");
        }
    }

    @Override
    public boolean hasClass(String className) {
        return localContext.containsClassInSignature(IRI.create(prefix + className));
    }

    @Override
    public boolean hasSubClass(String subClassName, String className) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLClass subcls = factory.getOWLClass(IRI.create(prefix + subClassName));
        OWLAxiom axiom = factory.getOWLSubClassOfAxiom(subcls, cls);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }

    @Override
    public boolean hasEquivalentClass(String equivClassName, String className) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLClass equivCls = factory.getOWLClass(IRI.create(prefix + equivClassName));
        OWLEquivalentClassesAxiom axiom = factory.getOWLEquivalentClassesAxiom(equivCls, cls);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }

    @Override
    public boolean hasDisjointClass(String disjClassName, String className) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLClass disjCls = factory.getOWLClass(IRI.create(prefix + disjClassName));
        OWLDisjointClassesAxiom axiom = factory.getOWLDisjointClassesAxiom(disjCls, cls);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }

    @Override
    public void removeClass(String className) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLAxiom axiom = factory.getOWLDeclarationAxiom(cls);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        List<OWLOntologyChange> changes= new ArrayList<OWLOntologyChange>();
        for(OWLAxiom ax : localContext.getReferencingAxioms(cls)){
            ax = ax.getAxiomWithoutAnnotations();
            ax = changeManager.getAnnotatedAxiom(ax);
            changes.add(new RemoveAxiom(localContext, ax));
        }
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChanges(changes);  
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeClass"  + className + ") will not be applied.");
        }
    }
//    @Override
//    public void removeClass(String className) {
//        OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(localContext));
//        OWLDataFactory factory = manager.getOWLDataFactory();
//        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
//        OWLAxiom axiom = factory.getOWLDeclarationAxiom(cls);
//        axiom = changeManager.getAnnotatedAxiom(axiom);
//        try {
//            synchronized (this) {
//                changeManager.validateRemovalChange(axiom);
//                cls.accept(remover);
//                manager.applyChanges(remover.getChanges());
//            }
//        } catch (RemovalException ex) {
//            logger.error(ex.getMessage()
//                    + "Class not defined in Ontology or not defined by this application. Change ( removeClass" + className + ") will not be applied. ");
//        }
//    }

    @Override
    public void removeSubClass(String subClassName, String className) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLClass subCls = factory.getOWLClass(IRI.create(prefix + subClassName));
        OWLAxiom axiom = factory.getOWLSubClassOfAxiom(subCls, cls);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeSubClass" + subClassName + " of " + className + ") will not be applied.");
        }
    }

    @Override
    public void removeEquivalentClass(String className, String equivClassName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLClass equivCls = factory.getOWLClass(IRI.create(prefix + equivClassName));
        OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(cls, equivCls);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeEquivalentClass" + className + " of " + equivClassName + ") will not be applied.");
        }
    }

    @Override
    public void removeDisjointClass(String className, String disjClassName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLClass disjCls = factory.getOWLClass(IRI.create(prefix + disjClassName));
        OWLAxiom axiom = factory.getOWLDisjointClassesAxiom(disjCls, cls);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeDisjointClass" + className + " of " + disjClassName + ") will not be applied.");
        }
    }

    @Override
    public void moveSubClass(String subClassName, String fromClassName, String toClassName) {
        addSubClass(subClassName, toClassName);
        removeSubClass(subClassName, fromClassName);
    }
}
