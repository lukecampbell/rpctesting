package net.ithosting.testing.examples;

import net.ithosting.testing.rpc.RPCClient;

public class FibonacciClient {
    private String hostname;
    public FibonacciClient(String hostname)
    {
        this.hostname = hostname;
        
    }
    public int fib(int n)
    {
        try {
        RPCClient client = new RPCClient(hostname,"rpcfibonacci","fib");
        return Integer.parseInt(client.call(Integer.toString(n)));
        } catch(Exception e) {
            return 0;
        }
    }
    public static void main(String[] args) {
        FibonacciClient cli = new FibonacciClient("localhost");
        System.out.println("Fib(4) = " + cli.fib(4));
    }
}
