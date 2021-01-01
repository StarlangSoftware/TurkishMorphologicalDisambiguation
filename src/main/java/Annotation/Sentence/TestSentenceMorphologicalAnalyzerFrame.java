package Annotation.Sentence;

import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import WordNet.WordNet;

public class TestSentenceMorphologicalAnalyzerFrame {

    public static void main(String[] args){
        FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();
        WordNet wordNet = new WordNet();
        new SentenceMorphologicalAnalyzerFrame(fsm, wordNet);
    }

}
