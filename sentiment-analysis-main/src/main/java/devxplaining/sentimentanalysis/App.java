package devxplaining.sentimentanalysis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.mxgraph.layout.mxCircleLayout;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.simple.Sentence;
import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class App extends JFrame {
    protected static List<Chapter> chapters;  //Lista di tutti i capitoli
    protected static mxGraph visualGraph = new mxGraph();  //Grafo dei capitoli.I vertici indicano i capitoli mentre gli archi indicano le parole in comune tra i vertici coinvolti
    protected static Object parent=visualGraph.getDefaultParent();

    protected static String fileName;  //nome del file da leggere. Deve essere caricato nella cartella recources !

    protected static int numberOfWordToExtract;  //numero di parole significative da estrarre

    public static void main(String[] args){
        try {
        String text=getUserInput();
        chapters=new ArrayList<Chapter>();
        System.out.println("Testo originale\n"+text);
        chapterDivision(text); //Divisione del testo in capitoli
        chapters.remove(0); //Il primo capitolo risulterà sempre nullo,quindi lo elimino
        chaptersNPL(); //Studio del testo
        extractAndVisualizeChapterData(); //Estrazione parole significative e visualizzazione dati di ogni capitolo
        computeGraph(); //Popolo e visualizzo il grafo
        }catch (Exception e)
        {
            System.out.println("Crash: "+e.getCause().toString());
        }
    }

    public static void extractAndVisualizeChapterData()
    {
        for(int i=0;i<chapters.size();i++)
        {
            System.out.println("Chapter["+(i+1)+"]: Calcolo delle parole più significative in corso...");
            chapters.get(i).computeMeaningfulWords(chapters,numberOfWordToExtract); //Calcolo le parole più significative
            chapters.get(i).display(); //Visualizzo i dati del capitolo
        }
    }

    public static void chaptersNPL() throws IOException {
        //Considero singolarmente ogni capitolo
        for(int i=0;i<chapters.size();i++)
        {
            System.out.println("Chapter["+(i+1)+"]: Tokenizzazione in corso...");
            tokenize(chapters.get(i)); //Divido il testo per frasi. La vera tokenizzazione verrà effettuata dopo l'eliminazione delle stop words.

            System.out.println("Chapter["+(i+1)+"]: Analisi del testo in corso...");
            analyze(chapters.get(i)); //Computo l'analisi del sentimento di ogni frase

            System.out.println("Chapter["+(i+1)+"]: Visualizzazione PunteggioSentimentale-Frase:\n");
            for(int j=0;j<chapters.get(i).getChapterSentences().size();j++) System.out.println(""+chapters.get(i).getSingleSenteceSentiment(j)+" "+chapters.get(i).getChapterSentences().get(j));

            System.out.println("Chapter["+(i+1)+"]: Pulizia dei testi dalle stop words in corso...");
            String noStopWordsText=stopWordsElimination(chapters.get(i).getChapterText()); //Elimino le stop words
            var document = new edu.stanford.nlp.simple.Document(noStopWordsText);
            chapters.get(i).setChapterReducedText(document.sentences());
            System.out.println("Chapter["+(i+1)+"]: dopo eliminazione stopwords:\n"+chapters.get(i).getChapterReducedText());

            System.out.println("Chapter["+(i+1)+"]: Stemmatizzazione in corso...");
            String stemmatizedText=stemmer(chapters.get(i)); //computo la stemmatizzazione del testo
            document = new edu.stanford.nlp.simple.Document(stemmatizedText);
            chapters.get(i).setChapterReducedText(document.sentences());
            System.out.println("Chapter["+(i+1)+"]: dopo stemmatizzazione:\n"+chapters.get(i).getChapterReducedText());

            System.out.println("Chapter["+(i+1)+"]: Analisi singole parole in corso...");
            chapters.get(i).computeWordsAnalysis(); //calcolo le occorrenze di ogni parola e il sentimento.
            //La libreria usata per il calcolo del sentimento lavora su frasi, quindi ad ogni parola assegno la media dei sentimenti delle frasi in cui essa è presente.
        }
    }

    public static String getUserInput() throws IOException {
        //Presa del nome del testo da leggere
        Scanner myObj = new Scanner(System.in);
        String text;
        try{
            System.out.println("Enter file name loaded in resources (includere anche il .txt):");
            fileName= myObj.nextLine();  // Read user input
            text= loadResourceFromClasspath(fileName);
        }catch(Exception e)
        {
            System.out.println("Non ho trovato il file, controlla che sia caricato nella cartella resources.Computazione avviata su pinocchio.txt");
            text= loadResourceFromClasspath("pinocchio.txt");
        }
        //Presa del numero di parole da estrarre
        do{
            System.out.println("Enter number of words to extract for each chapter (>0):");
            try{
                numberOfWordToExtract =Integer.parseInt(myObj.nextLine());  // Read user input
            }catch(Exception e)
            {
                numberOfWordToExtract=0;
            }
        }while(numberOfWordToExtract<1);
        return text;
    }

    public static String stemmer(Chapter chapter) //Stemmatizzazione
    {
        String finalText="";
        SnowballStemmer stemmer=new englishStemmer();
        String[] word;
        for(int i=0;i<chapter.getChapterReducedText().size();i++) //Analizzo le parole NON STOPWORD
        {
            word=chapter.getChapterReducedText().get(i).toString().split(" ");
            for(int j=0;j<word.length;j++) {
                stemmer.setCurrent(word[j]); //Imposto la singola parola su cui fare stemmatizzazione
                if (stemmer.stem()){ //Stemmatizzo
                    finalText+=stemmer.getCurrent()+" "; //Aggiungo al risultato finale
                }
            }
        }
        return finalText;
    }

    public static void computeGraph()
    {
        //visualGraph.setAllowLoops(true);
        visualGraph.getModel().beginUpdate();
        //inserisco i vertici del grafo. Ogni vertice rappresenterà un capitolo.
        for(int i=0;i<chapters.size();i++) visualGraph.insertVertex(parent,""+i+1,i+1,i*100,i*100,80,30);
        //Ogni capitolo lo confronto con i successivi per vedere la presenza di mostValuableWords in comune così da collegarli.
        Chapter firstChapter;
        try{
            for(int i=0;i<chapters.size()-1;i++)
            {
                firstChapter=chapters.get(i);
                for(int j=i+1;j<chapters.size();j++)
                {
                    List<String> text = new ArrayList<>();
                    Chapter secondChapter=chapters.get(j);
                    firstChapter.mostValuableWords.forEach((key,value)->{
                        if(secondChapter.mostValuableWords.containsKey(key)) //trovo una parola in comune
                        {
                            text.add(value.getWord()+","); //la conservo
                        }
                    });
                    if(text.size()>0) //Se c'è almeno una mostValuableWord in comune, collego i due vertici.
                    {
                        String result = text
                                .stream()
                                .collect(Collectors.joining());
                        visualGraph.insertEdge(parent, null, result.substring(0,result.length()-1), visualGraph.getChildVertices(parent)[i], visualGraph.getChildVertices(parent)[j]);
                    }
                }
            }
        }finally {
            visualGraph.getModel().endUpdate();
        }
        //imposto il layout circolare e visualizzo il grafo.
        mxCircleLayout layout = new mxCircleLayout(visualGraph);
        layout.execute(visualGraph.getDefaultParent());
        mxGraphComponent graphComponent = new mxGraphComponent(visualGraph);
        GraphFrame frame = new GraphFrame();
        frame.add(graphComponent);
        frame.pack();
        frame.setVisible(true);
    }

    public static void chapterDivision(String content) //Divisione per capitoli
    {
        var document = new edu.stanford.nlp.simple.Document(content);
        int i=0,j=0;
        boolean stoplight=false;
        int nummberOfCharacters=0;
        //Pulisco tutto ciò che c'è prima del capitolo iniziale.
        for(;i<document.sentences().size();i++) //Per ogni frase del testo
        {
            Sentence text=document.sentences().get(i);
            for(j=0;j<text.length();j++)  {  //Per ogni parola della frase presa in considerazione
                while(j<text.length() && !text.word(j).contains("CHAPTER")) { //Fin quando la parola non contiene CHAPTER (considero contains e non equals per i casi in cui
                                                                                //è presente CHAPTER1 invece di CHAPTER 1)
                    nummberOfCharacters+=text.word(j).length()+1; //Calcolo il numero di caratteri (+1 per lo spazio)
                    j++; //Passo alla parola successiva
                }
                if(j<text.length() && text.word(j).contains("CHAPTER")) { //Se sono uscito perchè ho beccato la parola CHAPTER
                    stoplight=true; //imposto il sefamoro a true
                    break; //esco dal while
                }
            }
            if(stoplight) break; //se il semoforo è true esco dal ciclo
        }
        String realContent= content.substring(nummberOfCharacters); //Testo dal capitolo 1
        String[] listOfChapter=realContent.split("CHAPTER"); //Divido il testo per la parola CHAPTER
        for(i=0;i<listOfChapter.length;i++){
            Chapter chapter=new Chapter("CHAPTER"+listOfChapter[i],i); //istanzio un nuovo capitolo
            chapters.add(chapter); //lo aggiungo alla lista
        }
    }

    public static String stopWordsElimination(String content) throws IOException { //Eliminazione stop words
        var stopWordsList= loadResourceFromClasspath("stopWords.txt"); //carico il dizionario delle stop words
        var documentStopWords = new edu.stanford.nlp.simple.Document(stopWordsList);
        String testoFinale ="";
        var document = new edu.stanford.nlp.simple.Document(content); //carico il testo del capitolo

        for(int i=0;i<document.sentences().size();i++) //per ogni frase del capitolo
        {
            Sentence text=document.sentences().get(i);
            for(int j=0;j<text.length();j++) //per ogni parola della frase presa in considerazione
            {
                if(!documentStopWords.sentences().toString().contains(text.word(j).toString().toLowerCase())) testoFinale+=(text.word(j).toString())+" "; //se la parola non è una stop word, la prendo
                else if (text.word(j).toString().equals(".") || text.word(j).toString().equals(",") || text.word(j).toString().equals(";") || text.word(j).toString().equals(":")
                        || text.word(j).toString().equals("-") || text.word(j).toString().equals("?") || text.word(j).toString().equals("!")) testoFinale+=text.word(j).toString(); //non elimino la punteggiatura
            }
        }
        return testoFinale;
    }

    public static void tokenize(Chapter chapter) { //Tokenizzazione
        var document = new edu.stanford.nlp.simple.Document(chapter.getChapterText());
        List sentences=new ArrayList(); //vettore di frasi
        for(int i=0;i<document.sentences().size();i++) sentences.add(document.sentences().get(i)); //la funzione document.senteces() divide il testo secondo il punto "."
        chapter.setChapterSentences(sentences);
    }

    public static void analyze(Chapter chapter) { //Analisi delle singole frasi
        var props = new Properties();
        // tokenizer, sentence splitting, consistuency parsing, sentiment analysis
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        var pipeline = new StanfordCoreNLP(props);
        var annotation = pipeline.process(chapter.getChapterText()); //mi preparo a processare il testo
        for(int i=0;i<annotation.get(CoreAnnotations.SentencesAnnotation.class).size();i++) //per ogni frase
        {
            var tree=annotation.get(CoreAnnotations.SentencesAnnotation.class).get(i).get(SentimentAnnotatedTree.class); //costruisco l'albero delle relazioni
            var sentimentInt = RNNCoreAnnotations.getPredictedClass(tree); //prendo il valore sentimentale
            chapter.addSentenceSentiment(sentimentInt); //lo imposto
        }
    }

    private static String loadResourceFromClasspath(String fileName) throws IOException { //Caricamento del testo dalla cartella resources
        var inputStream = App.class.getClassLoader().getResourceAsStream(fileName);
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
