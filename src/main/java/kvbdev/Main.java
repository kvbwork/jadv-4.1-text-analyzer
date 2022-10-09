package kvbdev;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    protected static final int QUEUE_SIZE = 100;
    protected static final String LETTERS = "abc";
    protected static final int TRIM_WORD_MAX_LEN = 80;

    public static final List<ArrayBlockingQueue<String>> queueList = Stream
            .generate(() -> new ArrayBlockingQueue<String>(QUEUE_SIZE))
            .limit(LETTERS.length())
            .collect(Collectors.toList());

    public static void main(String[] args) throws InterruptedException {

        int maxTexts = 10_000;
        int maxLength = 100_000;

        System.out.println("Создание потоков-счетчиков");

        Map<Character, CharCounter> workerMap = new HashMap<>();
        ThreadGroup textWorkersGroup = new ThreadGroup("TextWorkers");

        for (int i = 0; i < LETTERS.length(); i++) {
            char ch = LETTERS.charAt(i);
            CharCounter charCounter = new CharCounter(ch, queueList.get(i));
            workerMap.put(ch, charCounter);
            new Thread(textWorkersGroup, charCounter, "CharCounter" + i).start();
        }

        System.out.println("Генерация текстов");
        startTextGenerator(maxTexts, maxLength).join();

        System.out.println("Завершение работы потоков-счетчиков");
        workerMap.values().forEach(CharCounter::shutdown);
        awaitShutdown(textWorkersGroup);

        workerMap.values().forEach(Main::printStats);
    }

    protected static Thread startTextGenerator(int maxTexts, int maxLength) {
        TextGenerator textGenerator = new TextGenerator(LETTERS, maxTexts, maxLength,
                text -> {
                    for (BlockingQueue<String> queue : queueList) {
                        queue.put(text);
                    }
                }
        );

        Thread textGeneratorThread = new Thread(textGenerator, "TextGenerator");
        textGeneratorThread.start();

        return textGeneratorThread;
    }

    protected static void printStats(CharCounter charCounter) {
        String trimWord = charCounter.getText().substring(0, TRIM_WORD_MAX_LEN);
        System.out.println();
        System.out.println("Искомый символ: '" + charCounter.getChar() + "'");
        System.out.println("Максимальное количество: " + charCounter.getCount());
        System.out.println("Найдено в слове: " + trimWord + "...");
        System.out.println("Обработано текстов: " + charCounter.getTotalTextCounter());
    }

    protected static void awaitShutdown(ThreadGroup threadGroup) throws InterruptedException {
        Thread[] threads = new Thread[1];
        while (threadGroup.activeCount() > 0) {
            threadGroup.enumerate(threads, false);
            threads[0].join();
        }
    }

}
