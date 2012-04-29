package net.ithosting.testing.model;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class MQClient {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try
        {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare("java_queue", true, false, true, null);
            String message = "Don't stop looking up.";
            channel.basicPublish("","java_queue",null,message.getBytes());
            channel.close();
            connection.close();
            
        }
        catch(java.io.IOException e)
        {
            
        }

    }

}
