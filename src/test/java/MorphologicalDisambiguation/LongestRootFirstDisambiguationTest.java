package MorphologicalDisambiguation;

import AnnotatedSentence.AnnotatedCorpus;
import AnnotatedSentence.AnnotatedWord;
import Corpus.DisambiguatedWord;
import Corpus.DisambiguationCorpus;
import Corpus.Sentence;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

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

    private AnnotatedCorpus getCorpus(String name){
        switch (name){
            case "atis":
                return new AnnotatedCorpus(new File("../../Atis/Turkish-Phrase/"));
            case "etstur":
                return new AnnotatedCorpus(new File("../../Etstur/Turkish-Phrase/"));
            case "framenet":
                AnnotatedCorpus corpus = new AnnotatedCorpus();
                File[] listOfFiles = new File("../../FrameNet-Examples/Turkish-Phrase/").listFiles();
                if (listOfFiles != null){
                    Arrays.sort(listOfFiles);
                    for (File file:listOfFiles){
                        if (file.isDirectory() && !file.isHidden()){
                            corpus.combine(new AnnotatedCorpus(file));
                        }
                    }
                }
                return corpus;
            case "kenet":
                return new AnnotatedCorpus(new File("../../Kenet-Examples/Turkish-Phrase/"));
            case "penn":
                return new AnnotatedCorpus(new File("../../Penn-Treebank/Turkish-Phrase/"));
            case "penn20":
                return new AnnotatedCorpus(new File("../../Penn-Treebank-20/Turkish-Phrase/"));
        }
        return null;
    }

    private void updateMaps(HashMap<String, Integer> correct, HashMap<String, Integer> incorrect, String key, boolean isCorrect){
        if (!correct.containsKey(key)){
            correct.put(key, 0);
            incorrect.put(key, 0);
        }
        if (isCorrect){
            correct.put(key, correct.get(key) + 1);
        } else {
            incorrect.put(key, incorrect.get(key) + 1);
        }
    }

    @Test
    public void testDisambiguation2() {
        HashMap<String, Integer> parseCorrectMap = new HashMap<>();
        HashMap<String, Integer> parseIncorrectMap = new HashMap<>();
        HashMap<String, Integer> rootCorrectMap = new HashMap<>();
        HashMap<String, Integer> rootIncorrectMap = new HashMap<>();
        FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer(0);
        AnnotatedCorpus corpus = getCorpus("atis");
        LongestRootFirstDisambiguation algorithm = new LongestRootFirstDisambiguation();
        String matchString;
        int correctParse = 0;
        int correctRoot = 0;
        for (int i = 0; i < corpus.sentenceCount(); i++){
            FsmParseList[] sentenceAnalyses = fsm.robustMorphologicalAnalysis(corpus.getSentence(i));
            boolean[] isParseCorrect = new boolean[corpus.getSentence(i).wordCount()];
            boolean[] isRootCorrect = new boolean[corpus.getSentence(i).wordCount()];
            ArrayList<FsmParse> fsmParses =  algorithm.disambiguate(sentenceAnalyses);
            for (int j = 0; j < corpus.getSentence(i).wordCount(); j++){
                AnnotatedWord word = (AnnotatedWord) corpus.getSentence(i).getWord(j);
                isParseCorrect[j] = false;
                isRootCorrect[j] = true;
                if (fsmParses.get(j).transitionList().toLowerCase(new Locale("tr")).equals(word.getParse().toString().toLowerCase(new Locale("tr")))){
                    isParseCorrect[j] = true;
                } else {
                    if (!fsmParses.get(j).getWord().equals(word.getParse().getWord())){
                        isRootCorrect[j] = false;
                    }
                }
                if (isParseCorrect[j]){
                    correctParse++;
                }
                if (isRootCorrect[j]){
                    correctRoot++;
                }
            }
            FsmParseList[] sentenceAnalyses2 = fsm.robustMorphologicalAnalysis(corpus.getSentence(i));
            for (int j = 0; j < corpus.getSentence(i).wordCount(); j++) {
                AnnotatedWord word = (AnnotatedWord) corpus.getSentence(i).getWord(j);
                sentenceAnalyses2[j].reduceToParsesWithSameRoot(word.getParse().getWord().getName());
                if (sentenceAnalyses2[j].size() > 0){
                    matchString = sentenceAnalyses2[j].parsesWithoutPrefixAndSuffix();
                    if (!matchString.contains("$")){
                        matchString = "";
                    }
                } else {
                    matchString = "";
                }
                String lower = word.getName().toLowerCase(new Locale("tr"));
                updateMaps(rootCorrectMap, rootIncorrectMap, lower, isRootCorrect[j]);
                if (!matchString.isEmpty() && isRootCorrect[j]){
                    updateMaps(parseCorrectMap, parseIncorrectMap, lower, isParseCorrect[j]);
                }
            }
        }
        System.out.println("Roots:");
        for (String key : rootCorrectMap.keySet()){
            System.out.println(key + "\t" + rootCorrectMap.get(key) + "\t" + rootIncorrectMap.get(key));
        }
        System.out.println("Parses:");
        for (String key : parseCorrectMap.keySet()){
            System.out.println(key + "\t" + parseCorrectMap.get(key) + "\t" + parseIncorrectMap.get(key));
        }
        System.out.println((correctRoot + 0.0) / corpus.numberOfWords());
        System.out.println((correctParse + 0.0) / corpus.numberOfWords());
    }

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
                    Sentence sentence = new Sentence(word);
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