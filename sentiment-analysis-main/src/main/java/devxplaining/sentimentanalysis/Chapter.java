package devxplaining.sentimentanalysis;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Chapter {


    protected int chapterNumber; //Numero di capitolo
    protected String chapterText; //Testo completo
    protected List chapterSentences; //Lista delle frasi del testo
    protected List sentecesSentiment; //Lista del punteggio sentimentale di ogni frase
    protected List chapterReducedText; //Lista delle frasi del testo senza le stop words
    protected LinkedHashMap<String,Word> singleWords; //Hashmap delle parole non stop word contenente
    protected LinkedHashMap<String,Word> meaningfulWords; //Stessa struttura dell'hashmap precedente ma contenente solo le parole più significative
    protected Map<String,Word>mostValuableWords; //Hashmap identica a meaningfulWords ma contenente solo le x parole più significative, dove x è il valore delle parole da estrarre

    public Chapter(String chapterText,int chapterNumber) {
        this.chapterText = chapterText;
        this.sentecesSentiment = new ArrayList();
        this.singleWords=new LinkedHashMap<>();
        this.meaningfulWords=new LinkedHashMap<>();
        this.mostValuableWords=new LinkedHashMap<>();
        this.chapterNumber=chapterNumber;
    }

    private record WordAppearences (String word, int appeareances) {}


    public List getChapterReducedText() {
        return chapterReducedText;
    }

    public void setChapterReducedText(List chapterReducedText) {
        this.chapterReducedText = chapterReducedText;
    }

    public String getChapterText() {
        return chapterText;
    }


    public int getSingleSenteceSentiment(int index) {
        return (int) sentecesSentiment.get(index);
    }


    public List getChapterSentences() {
        return chapterSentences;
    }

    public void setChapterSentences(List chapterSentences) {
        this.chapterSentences = chapterSentences;
    }

    public void addSentenceSentiment(int sentecesSentiment)
    {
        this.sentecesSentiment.add(sentecesSentiment);
    }



    public void computeMeaningfulWords(List<Chapter> chapters,int numberOfWordsToExtract) //Calcolo delle parole più significative
    {
        int[] counter = new int[1];
        singleWords.forEach((key,stats)->{ //per ogni parola delle singleWords
            System.out.println("Sto analizzando la parola "+key);
            counter[0]=0; //resetto il counter
            for(int j=0;j<chapters.size();j++) //per ogni capitolo
            {
                if(chapters.get(j).chapterText.equals(this.chapterText)) continue; //se il capitolo è lo stesso di quello che sto analizzando, lo salto
                if(chapters.get(j).singleWords.containsKey(key)){ //se il j-esimo capitolo contiene la parola che stiamo analizzando
                    counter[0]++; //aumento il valore del counter
                    System.out.println(key+" è presente nel capitolo "+(j+1)+". Valore del counter: "+counter[0]);
                }
                if(counter[0]>=(chapters.size()/10.0)*4) { //Se alla fine la parola è presente in almeno il 40% dei capitoli allora essa sarà poco significativa.
                    System.out.println("Non prendo la parola "+key);
                   counter[0]=-1; //Imposto un valore assurdo al counter per capire che non devo prendere la parola
                   break;
                }
            }
            if(counter[0]>=0) this.meaningfulWords.put(key,stats); //Se il counter ha un valore non negativo, allora posso aggiungere la parola alla lista di quelle significative
        });
        System.out.println("Parole significative:");
        meaningfulWords.forEach((key,stats)->{
            System.out.println(key);
        });
        computeMostValuableWords(numberOfWordsToExtract); //calcolo quelle da estrarre
    }

    public void computeWordsAnalysis() //Calcolo numero di apparizioni e sentimento medio di ogni singola parola
    {
        String sentence;
        int sentimentPoint;
        for(int i=0;i<chapterReducedText.size();i++) //Per ogni frase non contenente stop-word
        {
            sentence=chapterReducedText.get(i).toString();
            sentimentPoint= (int) sentecesSentiment.get(i); //Prendo il valore sentimentale della frase
            String[] words=sentence.split(" "); //Divido le parole della frase tramite lo spazio
            for(int j=0;j<words.length;j++) //Per ogni parola
            {
                if(words[j].equals('"')) continue; //Se è una doppia virgoletta la salto
                //Pulisco la parola in caso di caratteri speciali
                words[j]=words[j].replace('\"','.');
                words[j]=words[j].replace(".","");
                words[j]=words[j].replace(",","");
                words[j]=words[j].replace(";","");
                words[j]=words[j].replace(":","");
                words[j]=words[j].replace("-","");
                words[j]=words[j].replace("?","");
                words[j]=words[j].replace("!","");
                words[j]=words[j].replace("(","");
                words[j]=words[j].replace(")","");
                words[j]=words[j].replace("{","");
                words[j]=words[j].replace("}","");
                words[j]=words[j].replace("[","");
                words[j]=words[j].replace("]","");
                words[j]=words[j].replace("_","");
                words[j]=words[j].replace("`","");
                words[j]=words[j].toLowerCase();
                if(!singleWords.containsKey(words[j])){ //se non è presente in lista
                    Word word=new Word(words[j].toString());//creo una nuova istanza di Word per la parola che sto analizzando
                    word.addSentimentPoint(sentimentPoint);//imposto il suo valore sentimentale al valore della frase in cui è presente
                    singleWords.put(words[j].toString(),word);//aggiungo la Word alla mappa delle parole singole
                }
                else { //se già era in lista
                    singleWords.get(words[j]).addAppearance(); //aumento di 1 le apparizioni
                    singleWords.get(words[j]).addSentimentPoint(sentimentPoint); //aumento il punteggio sentimentale considerando la nuova frase in cui è presente la word
                }
            }
        }
        if(singleWords.containsKey("")) singleWords.remove("");
        System.out.println("PAROLA | COUNTER | MEDIA_SENTIMENTO");
        singleWords.forEach((word,stats)->{ //per ogni parola
            stats.computeSentimentValue(); //calcolo il valore sentimentale considerandone le apparizioni
            System.out.println(word+" "+stats.getAppearances()+" "+stats.getSentimentValue());
        });
    }

    public void computeMostValuableWords(int numberOfWordsToExtract) //Calcolo le parole più significative da estrarre
    {
        meaningfulWords=hashmapOrder(meaningfulWords); //ordino, in maniera decrescente secondo il numero di appazioni, la mappa delle parole più significative
        System.out.println("Hashmap ordinata:");
        meaningfulWords.forEach((key,stats)->System.out.println(key+":"+stats.getAppearances()));

        if (meaningfulWords.size()<=numberOfWordsToExtract) mostValuableWords=meaningfulWords; //se il numero di parole da estrarre è maggiore o uguale al numero di parole significative
                                                                                                //allora le considero tutte
        else{ //altrimennti prendo le prime numberOfWordsToExtract dall'hashmap ordinata
            mostValuableWords= meaningfulWords.entrySet().stream().limit(numberOfWordsToExtract).collect(Collectors.toMap(p->p.getKey(), p->p.getValue()));
        }
        mostValuableWords=hashmapOrder((HashMap<String, Word>) mostValuableWords);
        System.out.println("Le "+numberOfWordsToExtract+" parole più presenti:");
        mostValuableWords.forEach((key,stats)->System.out.println(key+":"+stats.getAppearances()));
    }

    public void recordSort(ArrayList<WordAppearences> a) { //ordinamento dei record
        WordAppearences app; //record d'appoggio
        //selection sort
        for(int i=0;i<a.size()-1;i++)
        {
            for(int j=i+1;j<a.size();j++)
                if(a.get(i).appeareances<a.get(j).appeareances) {
                    app=a.get(j);
                    a.set(j,a.get(i));
                    a.set(i,app);
                }
        }
    }

    public LinkedHashMap<String, Word> hashmapOrder(HashMap<String,Word> map) //ordinamento hashmap
    {
        ArrayList<WordAppearences> tempArray=new ArrayList<WordAppearences>(); //creo una lista di record d'appoggio
        map.forEach((key,stats)->{
            WordAppearences values=new WordAppearences(stats.getWord(),stats.getAppearances()); //creo il record che rappresenta ogni entry dell'hashmap
            tempArray.add(values); //inserisco il record in lista
        });
        recordSort(tempArray); //ordino i record
        LinkedHashMap<String,Word> orderedMap=new LinkedHashMap<String,Word>();
        for(int i=0;i<tempArray.size();i++){
            orderedMap.put(tempArray.get(i).word,map.get(tempArray.get(i).word)); //aggiungo alla nuova hashmap i valori della lista dei record ordinata in ordine decrescente
        }
        return orderedMap;
    }

    public void display() //Stampo a schermo i dati del capitolo
    {
        JFrame frame = new JFrame("CHAPTER "+chapterNumber);

        JLabel label1=new JLabel("Full text",SwingConstants.CENTER);
        label1.setFont(new Font("Serif", Font.CENTER_BASELINE, 20));

        JLabel label2=new JLabel("Single non stop-word stats",SwingConstants.CENTER);
        label2.setFont(new Font("Serif", Font.CENTER_BASELINE, 20));

        JLabel label3=new JLabel("Most valuable words (words are stemmatized)");
        label3.setFont(new Font("Serif", Font.CENTER_BASELINE, 20));

        String formatted = chapterText.replace("\n", "<br>"); //formatto il testo completo del capitolo
        formatted = "<html><font size='3'>" + formatted + "</font></html>";
        JLabel text = new JLabel(formatted);
        JScrollPane scrollerText = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); //aggiungo uno scroller
        JPanel leftPanel=new JPanel();
        leftPanel.setLayout((new BoxLayout(leftPanel,BoxLayout.PAGE_AXIS)));
        leftPanel.add(label1);
        leftPanel.add(scrollerText);

        JTable midTable= new JTable(singleWords.size(),3); //tabella centrale delle parole
        midTable.getColumnModel().getColumn(0).setHeaderValue("WORD");
        midTable.getColumnModel().getColumn(1).setHeaderValue("APPEARANCES");
        midTable.getColumnModel().getColumn(2).setHeaderValue("SENTIMENT");
        int[] row=new int[1];
        row[0]=0;
        singleWords.forEach((key,stats)->{
            if(!key.equals("") || !key.equals(" "))
            {
                midTable.setValueAt(key,row[0],0);
                midTable.setValueAt(stats.getAppearances(),row[0],1);
                switch ((int) stats.getSentimentValue()) //converto il valore sentimentale intero in stringa
                {
                    case 1:
                        midTable.setValueAt("Negative",row[0],2);
                        break;
                    case 2:
                        midTable.setValueAt("Neutral",row[0],2);
                        break;
                    case 3:
                        midTable.setValueAt("Positive",row[0],2);
                        break;
                    case 4:
                        midTable.setValueAt("Very positive",row[0],2);
                        break;
                }
                row[0]++;
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( SwingConstants.CENTER );
        JScrollPane scrollerMidTable = new JScrollPane(midTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel centerPanel=new JPanel();
        centerPanel.setLayout((new BoxLayout(centerPanel,BoxLayout.PAGE_AXIS)));
        centerPanel.add(label2);
        centerPanel.add(scrollerMidTable);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        TableModel tableModel = midTable.getModel();
        for (int columnIndex = 0; columnIndex < tableModel.getColumnCount(); columnIndex++)
        {
            midTable.getColumnModel().getColumn(columnIndex).setCellRenderer(rightRenderer);
        }

        JTable rightTable= new JTable(mostValuableWords.size(),2);
        rightTable.getColumnModel().getColumn(0).setHeaderValue("WORD");
        rightTable.getColumnModel().getColumn(1).setHeaderValue("APPEARANCES");
        row[0]=0;
        mostValuableWords.forEach((key,stats)->{
            rightTable.setValueAt(key,row[0],0);
            rightTable.setValueAt(stats.getAppearances(),row[0],1);
            row[0]++;
        });
        rightTable.setDefaultRenderer(String.class, centerRenderer);
        JScrollPane scrollerRightTable = new JScrollPane(rightTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel rightPanel=new JPanel();
        rightPanel.setLayout((new BoxLayout(rightPanel,BoxLayout.PAGE_AXIS)));
        rightPanel.add(label3);
        rightPanel.add(scrollerRightTable);
        tableModel = rightTable.getModel();
        for (int columnIndex = 0; columnIndex < tableModel.getColumnCount(); columnIndex++)
        {
            rightTable.getColumnModel().getColumn(columnIndex).setCellRenderer(rightRenderer);
        }


        frame.setLayout(new BorderLayout(50,100));
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.setPreferredSize(new Dimension(20000,20000));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
