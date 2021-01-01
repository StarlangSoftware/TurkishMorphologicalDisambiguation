package Annotation.ParseTree;

import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalDisambiguation.RootWordStatistics;

public class TestMorphologicalAnalyzerFrame {

    public static void main(String[] args){
        FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();
        RootWordStatistics rootWordStatistics = new RootWordStatistics("penntreebank_statistics.txt");
        new TreeMorphologicalAnalyzerFrame(fsm, rootWordStatistics);
    }
}
