package devxplaining.sentimentanalysis;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChapterTest {
    protected App mainClass;
    protected Chapter chapter;
    protected String text;
    protected List sentecesSentiment;
    protected static List<Chapter> chapters;
    @BeforeAll
    void setup() throws IOException {
        var inputStream = App.class.getClassLoader().getResourceAsStream("prova.txt");
        text= new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        chapter=new Chapter(text,3);
        mainClass=new App();
    }

    @Test
    void setChapterReducedText(){
        List chapterReducedText;
        String noStopWordsText= "";
        try {
            noStopWordsText = App.stopWordsElimination(chapter.getChapterText());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var document = new edu.stanford.nlp.simple.Document(noStopWordsText);
        chapterReducedText=document.sentences();
        chapter.setChapterReducedText(document.sentences());
        assertEquals(chapter.getChapterReducedText(),chapterReducedText);
    }

    @Test
    void getChapterText() {
        assertEquals(chapter.getChapterText(),text);
    }

    @Test
    void setChapterSentences() {
        mainClass.tokenize(chapter);
        var document = new edu.stanford.nlp.simple.Document(chapter.getChapterText());
        List sentences=new ArrayList();
        for(int i=0;i<document.sentences().size();i++) sentences.add(document.sentences().get(i));
        assertEquals(chapter.getChapterSentences(),sentences);
    }

    @Test
    void addSentenceSentiment() {
        sentecesSentiment=new ArrayList();
        var props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        var pipeline = new StanfordCoreNLP(props);
        var annotation = pipeline.process(chapter.getChapterText());
        for(int i = 0; i<annotation.get(CoreAnnotations.SentencesAnnotation.class).size(); i++)
        {
            var tree=annotation.get(CoreAnnotations.SentencesAnnotation.class).get(i).get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            var sentimentInt = RNNCoreAnnotations.getPredictedClass(tree);
            sentecesSentiment.add(sentimentInt);
        }
        mainClass.analyze(chapter);
        for(int i=0;i<chapter.getChapterSentences().size();i++)
        {
            assertEquals(chapter.getSingleSenteceSentiment(i),sentecesSentiment.get(i));
        }
    }

    @Test
    void computeMeaningfulWords() {
        chapters=new ArrayList<Chapter>();
        Chapter chapter1=new Chapter("CHAPTER 1 Ciao.Sono.Il.Primo.Capitolo.La mia parola chiave è giallo",1);
        Chapter chapter2=new Chapter("CHAPTER 2 Ciao.Sono.Il.Primo.Capitolo.La mia parola chiave è rosso",2);
        Chapter chapter3=new Chapter("CHAPTER 3 Ciao.Sono.Il.Primo.Capitolo.La mia parola chiave è verde",3);
        Chapter chapter4=new Chapter("CHAPTER 1 Ciao.Sono.Il.Primo.Capitolo.La mia parola chiave è blue",4);
        chapters.add(chapter1);
        chapters.add(chapter2);
        chapters.add(chapter3);
        chapters.add(chapter4);

        for(int i=0;i<4;i++)
        {
            var document = new edu.stanford.nlp.simple.Document(chapters.get(i).getChapterText());
            List chapterReducedText=new ArrayList();
            document.sentences().forEach((sentence)->{
                chapterReducedText.add(sentence);
            });
            chapters.get(i).setChapterReducedText(chapterReducedText);
            mainClass.analyze(chapters.get(i));
            chapters.get(i).computeWordsAnalysis();
        }
        for(int i=0;i<4;i++) chapters.get(i).computeMeaningfulWords(chapters,2);

        assertAll(()->assertEquals(chapters.get(0).meaningfulWords.containsKey("giallo"),true),
                ()->assertEquals(chapters.get(1).meaningfulWords.containsKey("rosso"),true),
                ()->assertEquals(chapters.get(2).meaningfulWords.containsKey("verde"),true),
                ()->assertEquals(chapters.get(3).meaningfulWords.containsKey("blue"),true)
        );

    }

    @Test
    void computeWordsAnalysis() {
        List chapterReducedText=new ArrayList();
        chapterReducedText.add("Ciao");
        chapterReducedText.add("ciao");
        chapterReducedText.add("tutti");
        chapter.setChapterReducedText(chapterReducedText);
        mainClass.analyze(chapter);
        chapter.computeWordsAnalysis();
            assertAll(()->assertEquals(chapter.singleWords.containsKey("ciao"),true),
                      ()->assertEquals(chapter.singleWords.get("ciao").getAppearances(),2),
                      ()->assertEquals(chapter.singleWords.containsKey("tutti"),true),
                      ()->assertEquals(chapter.singleWords.get("tutti").getAppearances(),1)

            );

    }

}