package MorphologicalDisambiguation;

import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

import java.util.ArrayList;

public class LongestRootFirstDisambiguation implements MorphologicalDisambiguator{

    /**
     * Train method implements method in {@link MorphologicalDisambiguator}.
     *
     * @param corpus {@link DisambiguationCorpus} to train.
     */
    @Override
    public void train(DisambiguationCorpus corpus) {
    }

    /**
     * The disambiguate method gets an array of fsmParses. Then loops through that parses and finds the longest root
     * word. At the end, gets the parse with longest word among the fsmParses and adds it to the correctFsmParses
     * {@link ArrayList}.
     *
     * @param fsmParses {@link FsmParseList} to disambiguate.
     * @return correctFsmParses {@link ArrayList} which holds the parses with longest root words.
     */
    @Override
    public ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses) {
        FsmParse bestParse;
        ArrayList<FsmParse> correctFsmParses = new ArrayList<>();
        for (FsmParseList fsmParseList : fsmParses) {
            bestParse = fsmParseList.getParseWithLongestRootWord();
            fsmParseList.reduceToParsesWithSameRootAndPos(bestParse.getWordWithPos());
            FsmParse newBestParse = fsmParseList.caseDisambiguator();
            if (newBestParse != null){
                bestParse = newBestParse;
            }
            correctFsmParses.add(bestParse);
        }
        return correctFsmParses;
    }

    /**
     * Overridden saveModel method to save a model.
     */
    @Override
    public void saveModel() {
    }

    /**
     * Overridden loadModel method to load a model.
     */
    @Override
    public void loadModel() {
    }
}
