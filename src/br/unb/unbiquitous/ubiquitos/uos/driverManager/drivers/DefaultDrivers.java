package br.unb.unbiquitous.ubiquitos.uos.driverManager.drivers;

import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpService;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpService.ParameterType;

public enum DefaultDrivers {
	POINTER() {

		@Override
		public UpDriver getDriver() {
			UpDriver pointer = new UpDriver(Pointer.DRIVER_NAME);
			UpService register = new UpService("registerListener").addParameter("eventKey", ParameterType.MANDATORY);
			pointer.addService(register);
			UpService unregister = new UpService("unregisterListener").addParameter("eventKey", ParameterType.OPTIONAL);
			pointer.addService(unregister);
			UpService upService = new UpService(Pointer.MOVE_EVENT);
			upService.addParameter(Pointer.AXIS_X, ParameterType.MANDATORY)
				.addParameter(Pointer.AXIS_Y, ParameterType.MANDATORY);
			pointer.addEvent(upService);
			
			return pointer;
		}
		
	},
	CLICKABLE() {
		@Override
		public UpDriver getDriver() {
			UpDriver clickable = new UpDriver(Clickable.DRIVER_NAME);
			UpService register = new UpService("registerListener").addParameter("eventKey", ParameterType.MANDATORY);
			clickable.addService(register);
			UpService unregister = new UpService("unregisterListener").addParameter("eventKey", ParameterType.OPTIONAL);
			clickable.addService(unregister);
			UpService upService = new UpService(Pointer.MOVE_EVENT);
			upService.addParameter(Pointer.AXIS_X, ParameterType.MANDATORY)
				.addParameter(Pointer.AXIS_Y, ParameterType.MANDATORY);
			clickable.addEvent(upService);
			clickable.addEquivalentDrivers(POINTER.getDriver().getName());
			UpService buttonPressed = new UpService(Clickable.BUTTON_PRESSED_EVENT).addParameter(Clickable.BUTTON, ParameterType.MANDATORY);
			clickable.addEvent(buttonPressed);
		    UpService buttonReleased = new UpService(Clickable.BUTTON_RELEASED_EVENT).addParameter(Clickable.BUTTON, ParameterType.MANDATORY);
		    clickable.addEvent(buttonReleased);
		    
			return clickable;
		}
	},
	SCROLLABLE() {
		@Override
		public UpDriver getDriver() {
			UpDriver scrollable = new UpDriver(Scrollable.DRIVER_NAME);
			UpService register = new UpService("registerListener").addParameter("eventKey", ParameterType.MANDATORY);
			scrollable.addService(register);
			UpService unregister = new UpService("unregisterListener").addParameter("eventKey", ParameterType.OPTIONAL);
			scrollable.addService(unregister);
			UpService upService = new UpService(Pointer.MOVE_EVENT);
			upService.addParameter(Pointer.AXIS_X, ParameterType.MANDATORY)
				.addParameter(Pointer.AXIS_Y, ParameterType.MANDATORY);
			scrollable.addEvent(upService);
			scrollable.addEquivalentDrivers(POINTER.getDriver().getName());
			UpService scroll = new UpService(Scrollable.SCROLL_EVENT).addParameter(Scrollable.DISTANCE, ParameterType.MANDATORY);
			scrollable.addEvent(scroll);
		    
			return scrollable;
		}
	};

	public abstract UpDriver getDriver();
}
