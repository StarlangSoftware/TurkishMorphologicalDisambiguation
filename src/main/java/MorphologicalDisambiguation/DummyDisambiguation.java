package MorphologicalDisambiguation;

import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

import java.util.ArrayList;
import java.util.Random;

public class DummyDisambiguation implements MorphologicalDisambiguator{
    public void train(DisambiguationCorpus corpus) {
    }

    @Override
    public ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses) {
        Random random = new Random();
        ArrayList<FsmParse> correctFsmParses = new ArrayList<>();
        for (int i = 0; i < fsmParses.length; i++){
            FsmParseList fsmParseList = fsmParses[i];
            if (fsmParseList.size() > 0){
                correctFsmParses.add(fsmParseList.getFsmParse(random.nextInt(fsmParseList.size())));
            }
        }
        return correctFsmParses;
    }

   @Override
    public void saveModel() {
    }

    @Override
    public void loadModel() {
    }
}
