package MorphologicalDisambiguation;

import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

import java.util.ArrayList;

public class RootWordStatisticsDisambiguation implements MorphologicalDisambiguator{
    private RootWordStatistics rootWordStatistics;

    public RootWordStatisticsDisambiguation(){
        rootWordStatistics = new RootWordStatistics("penntreebank_statistics.txt");
    }

    public RootWordStatisticsDisambiguation(String fileName){
        rootWordStatistics = new RootWordStatistics(fileName);
    }

    @Override
    public void train(DisambiguationCorpus corpus) {
    }

    @Override
    public ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses) {
        FsmParse bestParse;
        String bestRoot;
        ArrayList<FsmParse> correctFsmParses = new ArrayList<>();
        int i = 0;
        for (FsmParseList fsmParseList : fsmParses) {
            String rootWords = fsmParseList.rootWords();
            if (rootWords.contains("$")){
                bestRoot = rootWordStatistics.bestRootWord(fsmParseList, 0.0);
                if (bestRoot == null){
                    bestRoot = fsmParseList.getParseWithLongestRootWord().getWord().getName();
                }
            } else {
                bestRoot = rootWords;
            }
            if (bestRoot != null){
                fsmParseList.reduceToParsesWithSameRoot(bestRoot);
                FsmParse newBestParse = AutoDisambiguator.caseDisambiguator(i, fsmParses, correctFsmParses);
                if (newBestParse != null){
                    bestParse = newBestParse;
                } else {
                    bestParse = fsmParseList.getFsmParse(0);
                }
            } else {
                bestParse = fsmParseList.getFsmParse(0);
            }
            correctFsmParses.add(bestParse);
            i++;
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
