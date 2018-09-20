package MorphologicalDisambiguation;

import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

import java.util.ArrayList;

public interface MorphologicalDisambiguator {

    /**
     * Method to train the given {@link DisambiguationCorpus}.
     *
     * @param corpus {@link DisambiguationCorpus} to train.
     */
    void train(DisambiguationCorpus corpus);

    /**
     * Method to disambiguate the given {@link FsmParseList}.
     *
     * @param fsmParses {@link FsmParseList} to disambiguate.
     * @return ArrayList of {@link FsmParse}.
     */
    ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses);

    /**
     * Method to save a model.
     */
    void saveModel();

    /**
     * Method to load a model.
     */
    void loadModel();

}
