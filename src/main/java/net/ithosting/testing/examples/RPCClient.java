package net.ithosting.testing.examples;
import java.io.IOException;
import java.util.HashMap;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RPCClient {
    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpcfibonacci";
    private String replyQueueName;
    private QueueingConsumer consumer;

    public RPCClient() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection =  factory.newConnection();
        channel = connection.createChannel();
        replyQueueName = channel.queueDeclare().getQueue();
        System.out.println("Using queue: " + replyQueueName);

        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);

    }

    public String call(String message) throws InterruptedException, IOException
    {
        String response = null;
        String corrId = java.util.UUID.randomUUID().toString();
        BasicProperties props = new BasicProperties
            .Builder()
            .correlationId(corrId)
            .headers(new HashMap<String,Object>(){
                {
                    put("op","fib");
                }
            })
            .replyTo(replyQueueName)
            .build();
        channel.basicPublish("", requestQueueName, props, message.getBytes());
        while(true)
        {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            if(delivery.getProperties().getCorrelationId().equals(corrId)) {
                response = new String(delivery.getBody());
                break;
            }
            else {
                response = new String(delivery.getBody());
                System.err.println("Wrong Message: " + response);
            }
        }
        return response;
    }
    public void close() throws IOException
    {
        connection.close();
    }
    public static void main(String[] args)
    {
        RPCClient client = null;
        String response = null;
        try {
            client = new RPCClient();
        } catch(IOException e) {
            System.err.println("Failed to initialize connection");
            System.exit(1);
        }
        try {
            response = client.call("abc");
        } catch(IOException e) {
            System.err.println("Failed to make request.");
            System.exit(1);
        } catch(InterruptedException e) {
            System.err.println("Interrupted connection");
        }
        System.out.println("Response: " + response);
        System.exit(0);
    }
}
