package net.ithosting.testing.examples;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;


public class FibonacciService {
    private boolean shutdown=false;
    private static int fib(int n){
        if(n==0) return 0;
        if(n==1) return 1;
        return fib(n-1) + fib(n-2);
    }
    
    public void startService()
    {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection     = null;
        Channel channel           = null;
        QueueingConsumer consumer = null;

        try {
            factory.setHost("localhost");
            connection = factory.newConnection();
            channel = connection.createChannel();
            
            channel.queueDeclare("rpc_queue",false,false,false,null);
            channel.basicQos(1);
            
            consumer = new QueueingConsumer(channel);
            channel.basicConsume("rpc_queue",false,consumer);
        } catch(IOException e) {
            System.err.println("Failed to initialize connection, shutting down...");
            return;
        }
        

        System.out.println(" [x] Awaiting RPC Requests.");
        while(!shutdown)
        {
            
            QueueingConsumer.Delivery delivery = null;
            try {
                delivery = consumer.nextDelivery();
            } catch(InterruptedException e) {
                System.out.println("Failed to retrieve message");
                continue;
            }
            
            BasicProperties props = delivery.getProperties();
            BasicProperties replyProps = new BasicProperties
                .Builder()
                .correlationId(props.getCorrelationId())
                .build();

            String message = new String(delivery.getBody());
            int n = Integer.parseInt(message);
            System.out.println(" [.] fib(" + message + ")");
            String response = "" + fib(n);
            try {
            channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
            } catch(IOException e) {
                shutdown=true;
            }
        }
    }
    public static void main(String[] args)
    {
        FibonacciService service = new FibonacciService();
        service.startService();
    }
}
