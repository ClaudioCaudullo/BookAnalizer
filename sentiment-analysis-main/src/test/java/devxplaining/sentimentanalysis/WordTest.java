package devxplaining.sentimentanalysis;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

class WordTest {

    Word word;

    @BeforeAll
    void setup() throws IOException {
        word=new Word("Ciao");
        word.addAppearance();
        word.addSentimentPoint(1);
    }

    @Test
    void getWord() {
        assertEquals(word.getWord(),"Ciao");
    }

    @Test
    void addSentimentPoint() {
        word.addSentimentPoint(3);
        assertEquals(word.getTotalSentimentPoints(),4);
    }

    @Test
    void addAppearance() {
        word.addAppearance();
        assertEquals(word.getAppearances(),3);
    }

    @AfterAll
    void computeSentimentValue() {
        word.computeSentimentValue();
        assertEquals(word.getSentimentValue(),4/3);
    }
}