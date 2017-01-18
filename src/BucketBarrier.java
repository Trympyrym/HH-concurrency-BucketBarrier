import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by trympyrym on 14.01.17.
 */
public class BucketBarrier implements Bucket, Drop {

    private Queue<Thread> threads = new ArrayDeque<>();

    private volatile Integer numberOfDropsToWait = 0; //changes only inside synchronized(threads) block

    private Object leakMutex = new Object();
    private volatile Integer numberOfTasksWaitingForLeakMutex = 0; //changes only inside synchronized(leakMutex) block

    private Object leakingInProgress = new Object();
    private volatile Boolean controllerWaitingForLIPMutex = true; //changes only inside synchronized(LIP) block

    @Override
    public void arrived() throws InterruptedException {
        synchronized (threads)
        {
            threads.add(Thread.currentThread());
        }

        synchronized (threads)
        {
            numberOfDropsToWait--;
            threads.notifyAll();
            do
            {
                threads.wait();
            } while (threads.peek() != Thread.currentThread());
        }


        synchronized (leakMutex) {
            numberOfTasksWaitingForLeakMutex++;
            leakMutex.notify();
            leakMutex.wait();
            numberOfTasksWaitingForLeakMutex--;
        }


        synchronized (leakingInProgress)
        {

            controllerWaitingForLIPMutex = false;
            leakingInProgress.notify();
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
        synchronized (threads)
        {
            threads.notifyAll();
        }

        synchronized (leakMutex)
        {
            while (numberOfTasksWaitingForLeakMutex == 0)
            {
                leakMutex.wait();
            }
            leakMutex.notify();
        }

        synchronized (threads)
        {
            threads.poll();
        }


        synchronized (leakingInProgress)
        {

            while (controllerWaitingForLIPMutex)
            {

                leakingInProgress.wait();

            }

            controllerWaitingForLIPMutex = true;

        }

    }
}
