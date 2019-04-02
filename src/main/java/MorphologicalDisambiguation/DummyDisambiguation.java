package MorphologicalDisambiguation;

import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

import java.util.ArrayList;
import java.util.Random;

public class DummyDisambiguation implements MorphologicalDisambiguator {
    /**
     * Train method implements method in {@link MorphologicalDisambiguator}.
     *
     * @param corpus {@link DisambiguationCorpus} to train.
     */
    public void train(DisambiguationCorpus corpus) {
    }

    /**
     * Overridden disambiguate method takes an array of {@link FsmParseList} and loops through its items, if the current FsmParseList's
     * size is greater than 0, it adds a random parse of this list to the correctFsmParses {@link ArrayList}.
     *
     * @param fsmParses {@link FsmParseList} to disambiguate.
     * @return correctFsmParses {@link ArrayList}.
     */
    @Override
    public ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses) {
        Random random = new Random();
        ArrayList<FsmParse> correctFsmParses = new ArrayList<>();
        for (FsmParseList fsmParseList : fsmParses) {
            if (fsmParseList.size() > 0) {
                correctFsmParses.add(fsmParseList.getFsmParse(random.nextInt(fsmParseList.size())));
            }
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
