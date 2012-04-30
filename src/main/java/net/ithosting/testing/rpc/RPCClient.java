package net.ithosting.testing.rpc;

import java.util.HashMap;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;

public class RPCClient {
    private Connection connection;
    private Channel channel;
    private ConnectionFactory factory;
    private QueueingConsumer consumer;
    private String target;
    private String op;
    private String replyQueue;
    private boolean debug;

    public RPCClient(String hostname, String target, String op)
            throws java.io.IOException {
        if (System.getProperty("debug") != null)
            debug = true;
        factory = new ConnectionFactory();
        factory.setHost(hostname);
        connection = factory.newConnection();
        channel = connection.createChannel();
        replyQueue = channel.queueDeclare().getQueue();
        this.target=target;
        this.op = op;
        if (debug)
            System.out.println("Reply Queue: " + replyQueue);
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueue, true,consumer);
    }

    public static BasicProperties requestProperties(final String corrId, final String replyQueue, final String op) {
        return new BasicProperties.Builder().correlationId(corrId)
                .headers(new HashMap<String, Object>() {
                    {
                        put("op", op);
                    }
                })
                .replyTo(replyQueue)
                .build();
    }

    public String call(String arg) throws IOException, InterruptedException {
        String corrId = java.util.UUID.randomUUID().toString();
        String response = "None";
        BasicProperties request = requestProperties(corrId,replyQueue,op);
        channel.basicPublish("", target, request, arg.getBytes());
        while(true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            if(delivery.getProperties().getCorrelationId().equals(corrId)) {
                response = new String(delivery.getBody());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                break;
            }
            else {
                if(debug) { 
                    System.err.println("Wrong Message: " + response);
                }
            }
        }
        
        return response;
    }
    public static void main(String[] args) throws Exception
    {
        RPCClient client = new RPCClient("localhost","rpcfibonacci","fib");
        System.out.println("Making request");
        String response = client.call("4");
        System.out.println("Fib(4) = " + response);
        client.close();
    }
    public void close() throws IOException
    {
        connection.close();
    }

}
