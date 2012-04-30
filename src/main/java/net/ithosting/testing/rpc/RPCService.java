package net.ithosting.testing.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RPCService {
    private Connection connection;
    private Channel channel;
    private ConnectionFactory factory;
    private QueueingConsumer consumer;
    private boolean shutdown;
    private final boolean debug;
    private final String queueName;

    public String getQueueName() {
        return queueName;
    }

    public RPCService(String hostname) throws IOException {
        shutdown = false;
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        queueName = this.getClass().getSimpleName().toLowerCase();

        if (System.getProperty("debug") != null)
            debug = true;
        else
            debug = false;
    }

    public static BasicProperties responseProperties(String corrId,
            final int code) {
        BasicProperties properties = new BasicProperties.Builder()
                .correlationId(corrId).headers(new HashMap<String, Object>() {
                    {
                        put("code", "" + code);
                    }
                }).build();
        return properties;
    }

    public void start() throws IOException {
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(queueName, false, false, true, null);
        String response = null;

        channel.basicQos(1);
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, false, consumer);
        if (debug) {
            System.out.println(" [x] Awaiting RPC Requests.");
        }
        while (!shutdown) {
            QueueingConsumer.Delivery delivery = null;
            BasicProperties replyProps = null;
            BasicProperties props = null;
            try {
                delivery = consumer.nextDelivery();
            } catch (InterruptedException e) {
                System.err.println("Failed to retrieve message.");
                continue;
            }
            /* - Handle the headers for the response - */

            props = delivery.getProperties();
            replyProps = responseProperties(props.getCorrelationId(), 0);
            String methodName = props.getHeaders().get("op").toString();
            try {
                Method method = this.getClass().getMethod(methodName,
                        String.class);
                response = method.invoke(this, new String(delivery.getBody()))
                        .toString();
                if(response==null) response = "None";
            } catch (NoSuchMethodException e) {
                if (debug) {
                    System.err.println("Class: "
                            + this.getClass().getSimpleName());
                    for (Method m : this.getClass().getMethods()) {
                        System.err.println(m.getName());
                    }
                    System.err.println(" [!] No Such Method " + methodName);

                }
                response = "No Such Method " + methodName;
                replyProps = responseProperties(props.getCorrelationId(), 1);

            } catch (InvocationTargetException e) {
                System.err.println("Problem executing...");
                response = e.getMessage();

            } catch (IllegalAccessException e) {
                System.err.println("Problem executing...");
                response = e.getMessage();
            }
            try {
                if(response==null) response = "None";
                channel.basicPublish("", props.getReplyTo(), replyProps,
                        response.getBytes());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                if(debug) {
                    System.out.println(" [.] " + methodName + " : " + response);
                }
            } catch (IOException e) {
                if (debug) {
                    System.err.println("Problem with connection...");
                    e.printStackTrace();
                }
                shutdown = true;
            } catch (IllegalStateException e) {
                if (debug) {
                    System.err.println(e.getMessage());
                }
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }

        }
        close();

    }

    public void close() throws IOException {
        connection.close();
    }

}
