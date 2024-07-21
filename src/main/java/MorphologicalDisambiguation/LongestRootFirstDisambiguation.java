package MorphologicalDisambiguation;

import Corpus.DisambiguationCorpus;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import Util.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class LongestRootFirstDisambiguation implements MorphologicalDisambiguator{
    private HashMap<String, String> rootList;

    /**
     * Constructor for the longest root first disambiguation algorithm. The method reads a list of (surface form, most
     * frequent root word for that surface form) pairs from 'rootlist.txt' file.
     */
    public LongestRootFirstDisambiguation(){
        readFromFile("rootlist.txt");
    }

    /**
     * Constructor for the longest root first disambiguation algorithm. The method reads a list of (surface form, most
     * frequent root word for that surface form) pairs from a given file.
     * @param fileName File that contains list of (surface form, most frequent root word for that surface form) pairs.
     */
    public LongestRootFirstDisambiguation(String fileName){
        readFromFile(fileName);
    }

    /**
     * Reads the list of (surface form, most frequent root word for that surface form) pairs from a given file.
     * @param fileName Input file name.
     */
    private void readFromFile(String fileName) {
        this.rootList = FileUtils.readHashMap(fileName);
    }

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
        String bestRoot;
        ArrayList<FsmParse> correctFsmParses = new ArrayList<>();
        int i = 0;
        for (FsmParseList fsmParseList : fsmParses) {
            String surfaceForm = fsmParseList.getFsmParse(0).getSurfaceForm();
            bestRoot = rootList.get(surfaceForm);
            boolean rootFound = false;
            for (int j = 0; j < fsmParseList.size(); j++) {
                if (fsmParseList.getFsmParse(j).getWord().getName().equals(bestRoot)) {
                    rootFound = true;
                    break;
                }
            }
            if (bestRoot == null || !rootFound){
                bestParse = fsmParseList.getParseWithLongestRootWord();
                fsmParseList.reduceToParsesWithSameRoot(bestParse.getWord().getName());
            } else {
                fsmParseList.reduceToParsesWithSameRoot(bestRoot);
            }
            FsmParse newBestParse = AutoDisambiguator.caseDisambiguator(i, fsmParses, correctFsmParses);
            if (newBestParse != null){
                bestParse = newBestParse;
            } else {
                bestParse = fsmParseList.getFsmParse(0);
            }
            correctFsmParses.add(bestParse);
            i++;
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
