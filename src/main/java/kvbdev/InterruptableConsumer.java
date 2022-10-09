package kvbdev;

@FunctionalInterface
public interface InterruptableConsumer<T> {
    void accept(T obj) throws InterruptedException;
}
