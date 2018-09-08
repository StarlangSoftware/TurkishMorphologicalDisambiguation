package MorphologicalDisambiguation;

import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

import java.util.ArrayList;

public interface MorphologicalDisambiguator {

    void train(DisambiguationCorpus corpus);
    ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses);
    void saveModel();
    void loadModel();

}
