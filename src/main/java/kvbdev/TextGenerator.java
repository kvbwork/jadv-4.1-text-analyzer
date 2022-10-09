package kvbdev;

import java.util.Random;

public class TextGenerator implements Runnable {
    protected final String letters;
    protected final int maxTexts;
    protected final int maxLength;
    protected InterruptableConsumer<String> textConsumer;

    public TextGenerator(String letters, int maxTexts, int maxLength, InterruptableConsumer<String> textConsumer) {
        this.letters = letters;
        this.maxTexts = maxTexts;
        this.maxLength = maxLength;
        this.textConsumer = textConsumer;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < maxTexts; i++) {
                textConsumer.accept(generateText(letters, maxLength));
            }
        } catch (InterruptedException ignored) {
        }
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

}
