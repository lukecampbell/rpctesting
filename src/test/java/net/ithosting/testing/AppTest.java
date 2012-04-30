package net.ithosting.testing;

import net.ithosting.testing.examples.RPCFibonacci;
import net.ithosting.testing.rpc.RPCClient;
import net.ithosting.testing.rpc.RPCService;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    public void testAdd()
    {
    	int c = 2 + 2;
    	assertTrue(c==4);
    }
    public void testRPC() throws java.lang.Exception
    {
    	Thread serviceThread;
    	RPCService service;
    	RPCClient  client;
    	service = new RPCFibonacci("localhost");
    	serviceThread = new Thread(service);
    	serviceThread.start();
    	client = new RPCClient("localhost","rpcfibonacci","fib");
    	String response = client.call("4");
    	int i = Integer.parseInt(response);
    	assertTrue(i==3);
    }
}
