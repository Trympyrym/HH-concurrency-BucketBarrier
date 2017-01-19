import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by trympyrym on 14.01.17.
 */
public class BucketBarrier implements Bucket, Drop {

    private final Queue<Thread> threads = new ArrayDeque<>();

    private int numberOfDropsToWait = 0; //changes only inside synchronized(threads) block

    private final Object leakMutex = new Object();
    private int numberOfTasksWaitingForLeakMutex = 0; //changes only inside synchronized(leakMutex) block

    private final Object leakingInProgress = new Object();
    private boolean controllerWaitingForLIPMutex = true; //changes only inside synchronized(LIP) block

    @Override
    public void arrived() throws InterruptedException {
        synchronized (threads)
        {
            System.out.println(Thread.currentThread().getName() + " entered thread block");
            threads.add(Thread.currentThread());
            numberOfDropsToWait--;
            threads.notifyAll();
            do
            {
                threads.wait();
            } while (threads.peek() != Thread.currentThread());
            System.out.println(Thread.currentThread().getName() + " left thread block");
        }


        synchronized (leakMutex) {
            System.out.println(Thread.currentThread().getName() + " entered leakMutex block");
            numberOfTasksWaitingForLeakMutex++;
            leakMutex.notify();
            leakMutex.wait();
            System.out.println(Thread.currentThread().getName() + " left leakMutex block");
        }





    }

    @Override
    public void awaitDrop() throws InterruptedException {
        synchronized (threads)
        {
            numberOfDropsToWait++;
            while (numberOfDropsToWait > 0)
            {
                threads.wait();
            }
        }
    }

    @Override
    public void leak() throws InterruptedException {

        synchronized (leakMutex)
        {
            System.out.println(Thread.currentThread().getName() + " entered leakMutex block. counter =" +  numberOfTasksWaitingForLeakMutex);
            while (numberOfTasksWaitingForLeakMutex == 0)
            {
                leakMutex.wait();
            }
            leakMutex.notifyAll();
            numberOfTasksWaitingForLeakMutex--;
            System.out.println(Thread.currentThread().getName() + " left leakMutex block");
        }

        synchronized (threads)
        {
            System.out.println(Thread.currentThread().getName() + " entered thread block");
            threads.poll();
            threads.notifyAll();
            System.out.println(Thread.currentThread().getName() + " left thread block");
        }

    }
}
