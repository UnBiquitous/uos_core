package br.unb.unbiquitous.ubiquitos;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import br.unb.unbiquitous.ubiquitos.ontology.OntologyChangeManagerTest;
import br.unb.unbiquitous.ubiquitos.ontology.OntologyClassTest;
import br.unb.unbiquitous.ubiquitos.ontology.OntologyDataPropertyTest;
import br.unb.unbiquitous.ubiquitos.ontology.OntologyInstanceTest;
import br.unb.unbiquitous.ubiquitos.ontology.OntologyObjectPropertyTest;
import br.unb.unbiquitous.ubiquitos.ontology.OntologyReasonerTest;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.AdaptabitilyEngineTest;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.SmartSpaceGatewayTest;
import br.unb.unbiquitous.ubiquitos.uos.context.UOSApplicationContextTest;
import br.unb.unbiquitous.ubiquitos.uos.deviceManager.DeviceDaoTest;
import br.unb.unbiquitous.ubiquitos.uos.deviceManager.DeviceManagerTest;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverDaoTest;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverDataTest;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverManagerTest;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.ReflectionServiceCallerTest;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.MessageEngineTest;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.MessageHandlerTest;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriverTest;

@RunWith(value=Suite.class)
@SuiteClasses(value={
		UOSApplicationContextTest.class,
		//uos.driver
		//uos.adaptabilityEngine
		AdaptabitilyEngineTest.class,
		SmartSpaceGatewayTest.class,
		//uos.driverManager
		DriverDataTest.class,
		DriverDaoTest.class,
		DriverManagerTest.class,
		ReflectionServiceCallerTest.class,
		//uos.deviceManager
		DeviceDaoTest.class,
		DeviceManagerTest.class,
		//uos.messageEngine
		MessageEngineTest.class,
		UpDriverTest.class,
		MessageHandlerTest.class,
		//ontology
		OntologyChangeManagerTest.class,
		OntologyClassTest.class,
		OntologyDataPropertyTest.class,
		OntologyInstanceTest.class,
		OntologyObjectPropertyTest.class,
        OntologyReasonerTest.class
	}
)
public class UosCoreTestSuite {

}
