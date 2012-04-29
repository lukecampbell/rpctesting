package net.ithosting.testing.model;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.google.common.util.concurrent.Atomics;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class MQService {
    public void runMe()
    {
        System.out.println("ran callback");
    }
    public static void main(String[] args) throws IOException, java.lang.InterruptedException
    {
        AtomicReference<Boolean> done = Atomics.newReference(false);
        ConnectionFactory factory = new ConnectionFactory();
        Channel channel = null;
        MQService service = new MQService();
        
        QueueingConsumer consumer = null;
        try {
            
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare("java_queue",true,false,true,null);
        }
        catch(IOException e)
        {
            System.err.println("Problem initializing connection");
            e.printStackTrace();
            System.exit(1);
            
        }
        consumer = new QueueingConsumer(channel);
        channel.basicConsume("java_queue",true,consumer);
        
        while(!done.get())
        {
            
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            Map<String,Object> headers = delivery.getProperties().getHeaders();
            if(headers!=null)
            {
                System.out.println("Headers");
                System.out.println(headers);
                if (headers.containsKey("op"))
                {
                    MQService callback = new MQService();
                    Method method=null;
                    try {
                    method = callback.getClass().getMethod(headers.get("op").toString());
                    } catch(NoSuchMethodException e)
                    {
                        System.err.println("RPC Call Failed, NoSuchMethodException");
                        System.err.println(e.getMessage());
                        continue;
                    }
                    try {
                        method.invoke(callback);
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            String message = new String(delivery.getBody());
            if(message!=null)
            {
                System.out.println("Body");
                System.out.println(message);
            }
            
            
        }
        
    }

}
