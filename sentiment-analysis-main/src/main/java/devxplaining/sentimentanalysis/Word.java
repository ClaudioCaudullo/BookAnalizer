package devxplaining.sentimentanalysis;

public class Word { //Classe singola parola
    protected String word; //Parola
    protected int appearances; //Numero di apparizioni
    protected int totalSentimentPoints; //Punteggio sentimentale totale
    protected float sentimentValue; //Punteggio sentimentale medio (quello che verrà preso in considerazione)

    public Word(String word) {
        this.word = word;
        this.appearances = 1;
        this.totalSentimentPoints = 0;
        this.sentimentValue = 0f;
    }

    public int getTotalSentimentPoints() {
        return totalSentimentPoints;
    }

    public String getWord() {
        return word;
    }

    public int getAppearances() {
        return appearances;
    }

    public float getSentimentValue() {
        return sentimentValue;
    }

    public void addSentimentPoint(int value)
    {
        this.totalSentimentPoints+=value;
    }

    public void addAppearance()
    {
        this.appearances++;
    }

    public void computeSentimentValue()
    {
        this.sentimentValue= this.totalSentimentPoints/this.appearances;
    }

}
