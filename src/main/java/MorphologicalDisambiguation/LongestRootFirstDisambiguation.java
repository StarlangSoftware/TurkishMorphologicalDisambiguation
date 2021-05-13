package MorphologicalDisambiguation;

import Corpus.DisambiguationCorpus;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import Util.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class LongestRootFirstDisambiguation implements MorphologicalDisambiguator{
    private HashMap<String, String> rootList;

    public LongestRootFirstDisambiguation(){
        readFromFile(FileUtils.getInputStream("rootlist.txt"));
    }

    public LongestRootFirstDisambiguation(String fileName){
        readFromFile(FileUtils.getInputStream(fileName));
    }

    private void readFromFile(InputStream inputStream) {
        String line;
        String[] items;
        rootList = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            do  {
                line = br.readLine();
                if (line == null){
                    break;
                }
                items = line.split(" ");
                if (items.length == 2){
                    rootList.put(items[0], items[1]);
                } else {
                    System.out.println(line);
                }
            } while (true);
            br.close();
        } catch (IOException e) {
        }
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
