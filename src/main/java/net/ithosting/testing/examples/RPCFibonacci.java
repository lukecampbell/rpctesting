package net.ithosting.testing.examples;

import net.ithosting.testing.rpc.RPCService;
import java.io.IOException;
public class RPCFibonacci extends RPCService {
    public RPCFibonacci(String hostname) throws IOException
    {
        super(hostname);
    }
    public String fib(String input)
    {
        int i = Integer.parseInt(input);
        if(i<=0) return "0";
        if(i==1) return "1";
        int a = Integer.parseInt(fib(Integer.toString(i-1)));
        int b = Integer.parseInt(fib(Integer.toString(i-2)));
        return Integer.toString(a + b);
    }
    public static void main(String[] args) throws IOException
    {
        RPCService service = new RPCFibonacci("localhost");
        service.start();
    }

}
