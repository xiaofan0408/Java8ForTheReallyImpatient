package general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ThreadedWorker {
    public static <T> void runInThreads(List<T> allInput, Function<List<T>, Runnable> taskFactory, int numThreads) {
        runInThreads(splitInTasks(allInput, taskFactory, numThreads), numThreads);
    }

    public static <T> List<Runnable> splitInTasks(List<T> allInput, Function<List<T>, Runnable> taskFactory, int numThreads) {
        int inputPerSegment = allInput.size() / numThreads;

        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            List<T> sublist = allInput.subList(inputPerSegment * i, inputPerSegment * (i + 1));
            Runnable task = taskFactory.apply(sublist);
            tasks.add(task);
        }
        return tasks;
    }

    public static List<Runnable> splitInTasks(long[] allInput, Function<long[], Runnable> taskFactory, int numThreads) {
        int inputPerSegment = allInput.length / numThreads;

        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < numThreads - 1; i++) {
            long[] sublist = Arrays.copyOfRange(allInput, inputPerSegment * i, inputPerSegment * (i + 1));
            Runnable task = taskFactory.apply(sublist);
            tasks.add(task);
        }
        long[] sublist = Arrays.copyOfRange(allInput, inputPerSegment * (numThreads - 1), allInput.length);
        Runnable task = taskFactory.apply(sublist);
        tasks.add(task);
        return tasks;
    }

    public static void runNTimesInThreads(Runnable task, int numThreads) {
        List<Runnable> tasks = Stream.generate(() -> task).limit(numThreads).collect(Collectors.toList());
        runInThreads(tasks, numThreads);
    }

    public static void runInThreads(List<Runnable> tasks, int numThreads) {
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        tasks.forEach(pool::execute);

        pool.shutdown();

        try {
            if(!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                throw new RuntimeException("I wasn't done yet :(");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Dammit checked exceptions", e);
        }
    }
}
