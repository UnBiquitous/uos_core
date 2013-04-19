package org.unbiquitous.uos.core.application;

import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDeploy;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyStart;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyUndeploy;

/**
 * 
 * This interface represents the applications to be deployed in the middleware context.
 * Each application to run within the middleware must have a launcher registered in the 'ubiquitos.application.deploylist'
 * and it's launcher must implement the underlying interface.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public interface UosApplication {

	/**
	 * This method is invoked to start the execution of an application.
	 * 
	 * @param gateway Provides information on how to access the smart space features through the uOS.
	 */
	void start(Gateway gateway, OntologyStart ontology);
	
	/**
	 * This method is invoked during the end of the execution of an application.
	 * 
	 * @throws Exception
	 */
	void stop() throws Exception;

        /**
	 * This method is invoked in the initialization of the application in the middleware.
	 * The structural changes in the ontology are done here.
         *
         * @param knowledgeBase Provides methods to change the ontology schema.
	 */
	public void init(OntologyDeploy ontology);


	/**
	 * This method is invoked during the teardown process of the application in the middleware.
	 * The structural changes and instances in the ontology may be removed.
         *
	 * @throws Exception
	 */
	void tearDown(OntologyUndeploy ontology) throws Exception;
   
}
