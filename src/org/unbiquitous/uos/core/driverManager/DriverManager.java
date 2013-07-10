package org.unbiquitous.uos.core.driverManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.UOSMessageContext;
import org.unbiquitous.uos.core.deviceManager.DeviceDao;
import org.unbiquitous.uos.core.driver.DeviceDriver;
import org.unbiquitous.uos.core.driverManager.drivers.DefaultDrivers;
import org.unbiquitous.uos.core.driverManager.drivers.Pointer;
import org.unbiquitous.uos.core.messageEngine.ServiceCallHandler;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

/**
 * This Class is responsible for dealing with the installed drivers in this device. Here we handle its
 * deployment, undeployment, dispatch of request for services and queries.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class DriverManager {
	
	private static Logger logger = Logger.getLogger(DriverManager.class);
	
	private ReflectionServiceCaller serviceCaller;
	
	private DriverDao driverDao;
	private DeviceDao deviceDao;
	private UpDevice currentDevice;
	private Map<Long, UosDriver> instances;
	private List<String> toInitialize;
	private Map<String, TreeNode> driverHash;
	private List<TreeNode> tree;
	
	public DriverManager(UpDevice currentDevice, DriverDao driverDao, DeviceDao deviceDao,  ReflectionServiceCaller serviceCaller) {
		this.driverDao = driverDao;
		this.deviceDao = deviceDao;
		this.serviceCaller = serviceCaller;
		this.currentDevice = currentDevice;
		this.instances = new HashMap<Long, UosDriver>();
		this.toInitialize = new ArrayList<String>();
		this.driverHash = new HashMap<String, TreeNode>();
		this.treeInit();
	}
	
	private void treeInit() {
		tree = new ArrayList<TreeNode>();
		TreeNode pointer = new TreeNode(DefaultDrivers.POINTER.getDriver());
		tree.add(pointer);
		driverHash.put(Pointer.DRIVER_NAME, pointer);
	}
	
	private List<DriverModel> findEquivalentDriver(List<TreeNode> equivalentDrivers) {
		List<DriverModel> list;
		
		for (TreeNode treeNode : equivalentDrivers) {
			list = driverDao.list(treeNode.getUpDriver().getName(),currentDevice.getName());
			if(list != null && !list.isEmpty())
				return list;
		}
		
		for (TreeNode treeNode : equivalentDrivers) {
			return findEquivalentDriver(treeNode.getChildren());
		}
		
		return null;
	}
	
	/**
	 * @see ServiceCallHandler#handleServiceCall(ServiceCall)
	 */
	public ServiceResponse handleServiceCall(ServiceCall serviceCall, UOSMessageContext messageContext) throws DriverManagerException{
		//Handle named InstanceCall
		DriverModel model = null;
		if (serviceCall.getInstanceId() != null ){
			//Find DriversInstance
			model = driverDao.retrieve(serviceCall.getInstanceId(),currentDevice.getName());
			if (model == null){
				logger.error("No Instance found with id '"+serviceCall.getInstanceId()+"'");
				throw new DriverManagerException("No Instance found with id '"+serviceCall.getInstanceId()+"'");
			}
		}else{
			//Handle non-named InstanceCall
			List<DriverModel> list = driverDao.list(serviceCall.getDriver(),currentDevice.getName());
			if(list == null || list.isEmpty()){ //Try to find an equivalent driver

				TreeNode driverNode = driverHash.get(serviceCall.getDriver());
				
				if(driverNode == null) {
					logger.debug("No instance found for handling driver '"+serviceCall.getDriver()+"'");
					throw new DriverManagerException("No instance found for handling driver '"+serviceCall.getDriver()+"'");
				}
				
				list = findEquivalentDriver(driverNode.getChildren());
				
				if(list == null || list.isEmpty()) {
					logger.debug("No instance found for handling driver '"+serviceCall.getDriver()+"'");
					throw new DriverManagerException("No instance found for handling driver '"+serviceCall.getDriver()+"'");
				}
				
			}
			// Select the first driver found (since no specific instance was informed)
			model = list.iterator().next();
		}
		
		return callServiceOnDriver(serviceCall, instances.get(model.rowid()), messageContext);
	}
	
	/**
	 * Method responsible to handle a service call on an instance driver object.
	 * 
	 * @param serviceCall Service Call Request Object about the service to call. 
	 * @param instanceDriver Object of the instance of the driver that must contain the service.
	 * @return Response to return to the caller device.
	 * @throws DriverManagerException
	 */
	private ServiceResponse callServiceOnDriver(ServiceCall serviceCall, Object instanceDriver, UOSMessageContext messageContext) throws DriverManagerException{
		return serviceCaller.callServiceOnDriver(serviceCall, instanceDriver, messageContext);
	}
	
	/**
	 * Method responsible for deploying a driver into the context.
	 * 
	 * @param driver Object representing the interface of the Driver to be deployed.
	 * @param instance Instance of the object implementing the informed Driver. 
	 * @throws DriverManagerException
	 * @throws DriverNotFoundException 
	 */
	public void deployDriver(UpDriver driver, Object instance) throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		deployDriver(driver,instance,null);
	}
	
	static long dCount = 0 ;
	synchronized private long incDeployedDriversCount(){
		return ++dCount;
	}
	
	/**
	 * Method responsible for deploying a driver into the context.
	 * 
	 * @param driver Object representing the interface of the Driver to be deployed.
	 * @param instance Instance of the object implementing the informed Driver. 
	 * @param instanceId Optional instanceId which to call this instance of the driver.
	 * @throws DriverManagerException
	 * @throws DriverNotFoundException 
	 */
	public void deployDriver(UpDriver driver, Object instance, String instanceId) throws DriverManagerException, DriverNotFoundException {
		if (instance instanceof UosDriver){      
			
			if(instanceId == null)
				instanceId = driver.getName()+incDeployedDriversCount();
			
			UosDriver uDriver = (UosDriver) instance;
			DriverModel model = new DriverModel(instanceId, uDriver.getDriver(), this.currentDevice.getName());
			
			if(driverHash.get(driver.getName()) == null) {
				try {
					if(uDriver.getParent() != null) {
						addToEquivalenceTree(uDriver.getParent());
					}
					addToEquivalenceTree(driver);
				} catch(InterfaceValidationException e) {
					throw new DriverManagerException(e);
				}
			}
			
			driverDao.insert(model);
			instances.put(model.rowid(), uDriver);
			toInitialize.add(instanceId);
			logger.debug(	"Deployied Driver : "+model.driver().getName()+
							" with id "+instanceId);
		}else{
			throw new IllegalArgumentException("The deployed DriverIntance must be of type UosDriver.");
		}
	}
	
	public void addToEquivalenceTree(List<UpDriver> drivers) throws InterfaceValidationException {
		
		int removeTries = 0;

		while(!drivers.isEmpty()) {
			UpDriver driver = drivers.get(0);
			try {
				addToEquivalenceTree(driver);
				drivers.remove(driver);
				removeTries = 0;
			} catch (DriverNotFoundException e) {
				drivers.remove(driver);
				drivers.add(driver);
				removeTries++;
			}
			if(removeTries == drivers.size() && removeTries != 0) {
				throw new InterfaceValidationException("The driver did not informe the complete list of equivalent drivers.");
			}
		}
	}
	
	/**
	 * Method responsible for adding the driver to the driverHash and to the equivalence tree. 
	 * 
	 * @param driver Object representing the interface of the Driver to be added.
	 * @throws InterfaceValidationException
	 */
	public void addToEquivalenceTree(UpDriver driver) throws InterfaceValidationException, DriverNotFoundException {
		TreeNode node = new TreeNode(driver);
		List<String> equivalentDrivers = driver.getEquivalentDrivers();
		Set<String> driversNotFound = new HashSet<String>();
		
		if(equivalentDrivers != null) {
			for (String equivalentDriver : equivalentDrivers) {
				TreeNode parent = driverHash.get(equivalentDriver);
				if(parent == null) {
					driversNotFound.add(equivalentDriver);
				} else {
					validateInterfaces(parent.getUpDriver().getServices(), node.getUpDriver().getServices());
					validateInterfaces(parent.getUpDriver().getEvents(), node.getUpDriver().getEvents());
					parent.addChild(node);										
				}
			}
		} else {
			tree.add(node);
		}
		
		if(driversNotFound.size() > 0) {
			throw new DriverNotFoundException("Equivalent drivers not found.", driversNotFound);			
		}
		
		driverHash.put(driver.getName(), node);
	}
	
	private void validateInterfaces(List<UpService> parentServices, List<UpService> driverServices) throws InterfaceValidationException {
		
		if(parentServices == driverServices && driverServices == null)
			return;
		
		if((parentServices == null && driverServices != null) || (parentServices != null && driverServices == null))
			throw new InterfaceValidationException("The deployed DriverInstance must have the same parameters.");
		
		for (UpService service : parentServices) {
			
			if(driverServices.contains(service)) {
				
				Map<String, ParameterType> parameters = service.getParameters();
				
				if(parameters != null) {
					for (String parameterName : parameters.keySet()){
						
						Map<String, ParameterType> driverParameters = driverServices.get(driverServices.indexOf(service)).getParameters();
						if((driverParameters != null) && (parameters.size() == driverParameters.size())) {

							ParameterType driverParameter = driverParameters.get(parameterName);
							if(driverParameter == null || !service.getParameters().get(parameterName).equals(driverParameter)){
								throw new InterfaceValidationException("The deployed DriverInstance must have the same parameters.");						
							} 
						} else {
							throw new InterfaceValidationException("The deployed DriverInstance must have the same parameters.");
						}
					}
				} else if(driverServices.get(driverServices.indexOf(service)).getParameters() != null) {
					throw new InterfaceValidationException("The deployed DriverInstance must have the same parameters.");
				}
				
			} else {
				throw new InterfaceValidationException("The deployed DriverInstance must have the same service name than its parent.");
			}
		}
	}

	/**
	 * Method responsible for undeploying the referenced driver instance from the Driver.
	 * 
	 * @param instanceId The instance id of the Driver to be removed.
	 */
	public void undeployDriver(String instanceId){
		logger.info("Undeploying driver with InstanceId : '"+instanceId+"'");
		
		DriverModel model = driverDao.retrieve(instanceId,currentDevice.getName());
		
		if (model != null){
			UosDriver uDriver = instances.get(model.rowid());
			if (!toInitialize.contains(model.id()))
				uDriver.destroy();
			driverDao.delete(model.id(), currentDevice.getName());
			toInitialize.remove(model.id());
		}else{
			logger.error("Undeploying driver with InstanceId : '"+instanceId+"' was not possible, since it's not present in the current database.");
		}
	}
	
	/**
	 * Method responsible for listing all drivers deployed.
	 * 
	 * @return all Drivers deployed.
	 */
	public List<UosDriver> listDrivers(){
		List<DriverModel> list = driverDao.list(null,currentDevice.getName());
		if (list.isEmpty())
			return null;
		
		List<UosDriver> ret = new ArrayList<UosDriver>();
		for (DriverModel m : list){
			ret.add(instances.get(m.rowid()));
		}
		return ret;
	}
	
	private List<DriverModel> findAllEquivalentDrivers(List<TreeNode> equivalentDrivers) {
		List<DriverModel> list = new ArrayList<DriverModel>();
		
		if(equivalentDrivers == null || equivalentDrivers.isEmpty())
			return list;
		
		for (TreeNode treeNode : equivalentDrivers) {
			list.addAll(driverDao.list(treeNode.getUpDriver().getName(), null));//TODO: [B&M] Should we filter the device?!
			
			List<DriverModel> temp = findAllEquivalentDrivers(treeNode.getChildren());
			for (DriverModel driverModel : temp) {
				if(!list.contains(driverModel)) {
					list.add(driverModel);
				}
			}
		}
		
		return list;
	}
	
	public UpDriver getDriverFromEquivalanceTree(String driverName) {
		TreeNode driver = driverHash.get(driverName);
		return (driver == null) ? null : driver.getUpDriver();
	}
	
	/**
	 * Method responsible for listing Driver Data known about the environment.
	 * 
	 * @param serviceName Service Name used to filter results.
	 * @param driverName Driver Name used to filter results.
	 * @param deviceName Device Name used to filter results.
	 * @return A list of Drivers according to the composition of the parameters.
	 */
	public List<DriverData> listDrivers(String driverName, String deviceName) {
		List<DriverModel> list = driverDao.list(driverName, deviceName);
		Set<DriverModel> baseSet = new LinkedHashSet<DriverModel>(list);

		TreeNode driverNode = driverHash.get(driverName);
		if(driverNode != null) {
			List<TreeNode> equivalentDrivers = driverNode.getChildren();
			baseSet.addAll(findAllEquivalentDrivers(equivalentDrivers));
		}
		if (baseSet == null || baseSet.isEmpty()){
			return null;
		}
		List<DriverData> ret = new ArrayList<DriverData>();
		for (DriverModel dm : baseSet) {
			ret.add(new DriverData(dm.driver(), deviceDao.find(dm.device()), dm.id()));
		}

		return ret;
	}
	
	/**
	 * Initializes the driver that are not initialized yet.
	 */
	public void initDrivers(Gateway gateway){
		try {
			if (driverDao.list("uos.DeviceDriver").isEmpty()){
				DeviceDriver deviceDriver = new DeviceDriver();
				deployDriver(deviceDriver.getDriver(), deviceDriver);
			}
		} catch (Exception e) {
			throw new RuntimeException(e); 
		}
		Iterator<String> it = toInitialize.iterator();
		while(it.hasNext()){
			String id = it.next();
			DriverModel model = driverDao.retrieve(id,currentDevice.getName());
			UosDriver driver = instances.get(model.rowid());
			driver.init(gateway, id);
			it.remove();
			logger.debug(	"Initialized Driver : "+model.driver().getName()+
							" with id "+id);
		}
	}
	
	/**
	 * Method responsible for releasing the resources allocated by the DriverManager and inform the deployed drivers of the shutdown
	 * of the application.
	 */
	public void tearDown(){
		for (DriverModel d : driverDao.list()){
			undeployDriver(d.id());
		}
		dCount = 0;
	}

	public UosDriver driver(String id) {
		DriverModel model = driverDao.retrieve(id,currentDevice.getName());
		if (model != null)
			return instances.get(model.rowid());
		else
			return null;
	}

	public List<DriverModel> list(String name, String device) {
		return driverDao.list(name, device);
	}

	public void delete(String id, String device) {
		driverDao.delete(id, device);
	}

	public void insert(DriverModel driverModel) throws DriverManagerException, DriverNotFoundException {
		try {
			addToEquivalenceTree(driverModel.driver());
			driverDao.insert(driverModel);
		} catch(InterfaceValidationException e) {
			throw new DriverManagerException(e);
		}
	}

	public DriverDao getDriverDao() {
		return driverDao;
	}
}
