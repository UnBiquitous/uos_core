import org.junit.Test;
import org.unbiquitous.uos.core.ContextException;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOS;


public class UOSTest_Start {

	@Test(expected=ContextException.class) 
	public void couldNotStartTheMiddlewareTwice(){
		UOS uos = new UOS();
		uos.start(new InitialProperties());
		uos.start(new InitialProperties());
	}
	
	@Test
	public void canStartTheMiddlewareAfterAStop() throws Exception{
		UOS uos = new UOS();
		uos.start(new InitialProperties());
		uos.stop();
		uos.start(new InitialProperties());
	}
}
