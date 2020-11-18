package MorphologicalDisambiguation;

import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

import java.util.ArrayList;

public class RootWordStatisticsDisambiguation implements MorphologicalDisambiguator{
    private RootWordStatistics rootWordStatistics;

    @Override
    public void train(DisambiguationCorpus corpus) {
        rootWordStatistics = new RootWordStatistics("penntreebank_statistics.txt");
    }

    @Override
    public ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses) {
        FsmParse bestParse;
        ArrayList<FsmParse> correctFsmParses = new ArrayList<>();
        for (FsmParseList fsmParseList : fsmParses) {
            String bestRoot = rootWordStatistics.bestRootWord(fsmParseList, 0.0);
            if (bestRoot != null){
                fsmParseList.reduceToParsesWithSameRoot(bestRoot);
                FsmParse newBestParse = fsmParseList.caseDisambiguator();
                if (newBestParse != null){
                    bestParse = newBestParse;
                } else {
                    bestParse = fsmParseList.getFsmParse(0);
                }
            } else {
                bestParse = fsmParseList.getFsmParse(0);
            }
            correctFsmParses.add(bestParse);
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
