package MorphologicalDisambiguation;

import Corpus.DisambiguatedWord;
import Corpus.DisambiguationCorpus;
import Corpus.Sentence;
import Dictionary.Word;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import static org.junit.Assert.*;

public class LongestRootFirstDisambiguationTest {

    @Test
    public void testDisambiguation() {
        FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();
        DisambiguationCorpus corpus = new DisambiguationCorpus("penntreebank.txt");
        LongestRootFirstDisambiguation algorithm = new LongestRootFirstDisambiguation();
        int correctParse = 0;
        int correctRoot = 0;
        for (int i = 0; i < corpus.sentenceCount(); i++){
            FsmParseList[] sentenceAnalyses = fsm.robustMorphologicalAnalysis(corpus.getSentence(i));
            ArrayList<FsmParse> fsmParses =  algorithm.disambiguate(sentenceAnalyses);
            for (int j = 0; j < corpus.getSentence(i).wordCount(); j++){
                DisambiguatedWord word = (DisambiguatedWord) corpus.getSentence(i).getWord(j);
                if (fsmParses.get(j).transitionList().toLowerCase(new Locale("tr")).equals(word.getParse().toString().toLowerCase(new Locale("tr")))){
                    correctParse++;
                    correctRoot++;
                } else {
                    if (fsmParses.get(j).getWord().equals(word.getParse().getWord())){
                        correctRoot++;
                        //System.out.println(fsmParses.get(j).transitionList() + "\t" + word.getParse().toString());
                    }
                }
            }
        }
        assertEquals(0.9234, (correctRoot + 0.0) / corpus.numberOfWords(), 0.0001);
        assertEquals(0.8424, (correctParse + 0.0) / corpus.numberOfWords(), 0.0001);
    }

    @Test
    public void testDistinctWordList(){
        FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();
        LongestRootFirstDisambiguation algorithm = new LongestRootFirstDisambiguation();
        try {
            PrintWriter output1 = new PrintWriter(new File("analyzed.txt"));
            PrintWriter output2 = new PrintWriter(new File("not-analyzed.txt"));
            Scanner input = new Scanner(new File("distinct.txt"));
            while (input.hasNext()){
                String word = input.next();
                if (fsm.morphologicalAnalysis(word).size() > 0){
                    Sentence sentence = new Sentence();
                    sentence.addWord(new Word(word));
                    FsmParseList[] sentenceAnalyses = fsm.robustMorphologicalAnalysis(sentence);
                    ArrayList<FsmParse> fsmParses =  algorithm.disambiguate(sentenceAnalyses);
                    output1.println(word + "\t" + fsmParses.get(0).getWord().getName());
                } else {
                   output2.println(word);
                }
            }
            input.close();
            output1.close();
            output2.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}