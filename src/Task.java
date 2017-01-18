import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by trympyrym on 14.01.17.
 */
public class Task implements Runnable {
    private final int iterations;
    private BucketBarrier barrier;
    public Task(int iterations, BucketBarrier barrier)
    {
        this.iterations = iterations;
        this.barrier = barrier;
    }

    private int blackHole;

    @Override
    public void run()
    {
        int blackHole = 0;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < iterations; i++)
        {
            blackHole += random.nextInt();
            onIteration();
        }
        try {
            barrier.arrived();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < iterations; i++)
        {
            blackHole += random.nextInt();
            onIteration();
        }

        this.blackHole = blackHole;

    }

    protected void onIteration()
    {

    }

    public int getBlackHole()
    {
        return blackHole;
    }
}
