package net.ithosting.testing;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import net.ithosting.testing.model.Bomb;
import net.ithosting.testing.exceptions.Problem;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
        List<ListenableFuture<Bomb>> bombs = new ArrayList<ListenableFuture<Bomb>>();
        for(int i=0;i<10;i++)
        {
            ListenableFuture<Bomb> bomb = service.submit(new Callable<Bomb>(){
                public Bomb call() throws Problem
                {
                    return craftBomb();
                }
            });
            bombs.add(bomb);
        }
        ListenableFuture<List<Bomb>> successfulBombs = Futures.successfulAsList(bombs);
        Futures.addCallback(successfulBombs, new FutureCallback<List<Bomb>>(){
            public void onSuccess(List<Bomb> bombs)
            {
                System.out.println("My successful bombs");
                for(Bomb b : bombs)
                {
                    System.out.println(b);
                }
            }
            public void onFailure(Throwable thrown)
            {
                System.err.println("There was a problem making this bomb.");
            }
        });
    }
    public static Bomb craftBomb() throws Problem
    {
        if(new Random().nextDouble()<=0.5)
        {
            System.err.println("Bomb broke during construction");
            throw new Problem();
        }
        System.out.println("Created new Bomb");
        return new Bomb();
    }
}
