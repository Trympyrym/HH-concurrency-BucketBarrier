/**
 * Created by trympyrym on 14.01.17.
 */
public interface Bucket {
    void awaitDrop() throws InterruptedException;
    void leak() throws InterruptedException;
}
