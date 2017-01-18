import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        // write your code here
        int iterations = 1000000;
        int numOfThreads = 5;

        BucketBarrier barrier = new BucketBarrier();

        Task task = new Task(iterations, barrier);
        List<Thread> threads = IntStream.range(0, numOfThreads)
                .mapToObj(i -> new Thread(task, "thread" + i))
                .collect(Collectors.toList());
        threads.forEach(Thread::start);
        for (int i = 0; i < numOfThreads; i++)
        {
            barrier.awaitDrop();
        }



        for (int i = 0; i < numOfThreads; i++)
        {
            barrier.leak();
        }

        for (Thread thread : threads)
        {
            thread.join();
        }
        System.out.println(task.getBlackHole());
    }
}
