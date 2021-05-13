package Annotation.ParseTree;

import MorphologicalAnalysis.FsmMorphologicalAnalyzer;

public class TestMorphologicalAnalyzerFrame {

    public static void main(String[] args){
        FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();
        new TreeMorphologicalAnalyzerFrame(fsm);
    }
}
