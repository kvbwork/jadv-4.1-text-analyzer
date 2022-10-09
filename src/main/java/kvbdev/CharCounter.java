package kvbdev;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class CharCounter implements Runnable {
    protected static final long POLL_TIMEOUT_MILLIS = 1000L;

    protected final char desiredChar;
    protected final BlockingQueue<String> sourceQueue;

    protected volatile boolean shutdown;
    protected int maxCharCount;
    protected String textWithMaxCharCount;
    protected long totalTextCounter;

    public CharCounter(char desiredChar, BlockingQueue<String> sourceQueue) {
        this.sourceQueue = sourceQueue;
        this.desiredChar = desiredChar;
    }

    @Override
    public void run() {
        shutdown = false;
        maxCharCount = 0;
        totalTextCounter = 0;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (shutdown && sourceQueue.isEmpty()) break;

                String text = sourceQueue.poll(POLL_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                if (text == null) continue;

                int charCount = countChar(desiredChar, text);
                if (charCount > maxCharCount) {
                    maxCharCount = charCount;
                    textWithMaxCharCount = text;
                }
                totalTextCounter++;
            }
        } catch (InterruptedException ignored) {
        }
    }

    public void shutdown() {
        shutdown = true;
    }

    protected int countChar(char ch, String text) {
        int counter = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ch) counter++;
        }
        return counter;
    }

    public char getChar() {
        return desiredChar;
    }

    public int getCount() {
        return maxCharCount;
    }

    public String getText() {
        return textWithMaxCharCount;
    }

    public long getTotalTextCounter() {
        return totalTextCounter;
    }
}
