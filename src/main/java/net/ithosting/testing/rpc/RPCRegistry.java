package net.ithosting.testing.rpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RPCRegistry {
    private String target; /* Corresponds to the class or service */
    private String method; 
    private List<Class<?>> params;
    private static Map<String,RPCRegistry> registered=null;
    
    public static boolean debug=true;
    
    public static void main(String args[])
    {
        RPCRegistry.register("client","doStuff",Integer.TYPE, String.class);
    }
    
    public static RPCRegistry register(String target, String method, Class<?>... parameterTypes)
    {
        RPCRegistry retval = new RPCRegistry(target,method,parameterTypes);
        if(registered==null)
        {
            if(debug) {
                System.out.println("Creating new RPC Registry.");
            }
            registered = new ConcurrentHashMap<String,RPCRegistry>();
        }
        if(debug) {
            System.out.println("Registrying \"" + target + ":" + method + "\"");
        }
        registered.put(target + ":" + method, retval);
        return retval;
    }
    public RPCRegistry(String target, String method, Class<?>... parameterTypes)
    {
        this.target = target;
        this.method = method;
        params = Arrays.asList(parameterTypes);
    }
    

}
