package MorphologicalDisambiguation;

import Classification.Classifier.C45;
import Classification.Performance.ExperimentPerformance;
import Classification.Parameter.Parameter;
import Classification.Performance.ClassificationPerformance;
import Corpus.*;
import Corpus.WordFormat;
import MorphologicalAnalysis.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;

public class TestMorphologicalDisambiguation {

    private static void testWordFormats(){
        DisambiguationCorpus corpus2 = new DisambiguationCorpus("Data/MorphologicalDisambiguation/penn_treebank.txt");
        corpus2.writeToFile("penntreebank-surface.txt", WordFormat.SURFACE);
        corpus2.writeToFile("penntreebank-root.txt", WordFormat.ROOT);
        corpus2.writeToFile("penntreebank-suffix.txt", WordFormat.SUFFIX);
    }

    private static void checkCorpus(FsmMorphologicalAnalyzer fsm, DisambiguationCorpus corpus){
        try {
            PrintWriter pw = new PrintWriter(new File("errors.txt"));
            for (int i = 0; i < corpus.sentenceCount(); i++){
                Sentence currentSentence = corpus.getSentence(i);
                FsmParseList[] fsmParseList = fsm.robustMorphologicalAnalysis(currentSentence);
                for (int j = 0; j < currentSentence.wordCount(); j++){
                    DisambiguatedWord word = (DisambiguatedWord) currentSentence.getWord(j);
                    boolean found = false;
                    for (int k = 0; k < fsmParseList[j].size(); k++){
                        if (word.getParse().toString().toUpperCase(new Locale("tr")).equals(fsmParseList[j].getFsmParse(k).toString().toUpperCase(new Locale("tr")))){
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        pw.println(word.getName() + " " + word.getParse());
                    }
                }
                if (i % 1000 == 0){
                    System.out.println(i);
                }
            }
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void train(MorphologicalDisambiguator morphologicalDisambiguator, DisambiguationCorpus corpus){
        morphologicalDisambiguator.train(corpus);
        morphologicalDisambiguator.saveModel();
    }

    private static void test(FsmMorphologicalAnalyzer fsm, MorphologicalDisambiguator morphologicalDisambiguator, DisambiguationCorpus corpus, ExperimentPerformance[] performances){
        morphologicalDisambiguator.loadModel();
        int correct = 0, total = 0, correctRoot = 0, correctPos = 0, overall = 0;
        boolean found[];
        for (int i = 0; i < corpus.sentenceCount(); i++){
            Sentence currentSentence = corpus.getSentence(i);
            FsmParseList[] fsmParseList = fsm.robustMorphologicalAnalysis(currentSentence);
            found = new boolean[currentSentence.wordCount()];
            for (int j = 0; j < currentSentence.wordCount(); j++) {
                DisambiguatedWord word = (DisambiguatedWord) currentSentence.getWord(j);
                for (int k = 0; k < fsmParseList[j].size(); k++) {
                    if (word.getParse().toString().toUpperCase(new Locale("tr")).equals(fsmParseList[j].getFsmParse(k).toString().toUpperCase(new Locale("tr")))) {
                        found[j] = true;
                        break;
                    }
                }
            }
            ArrayList<FsmParse> candidateParses = morphologicalDisambiguator.disambiguate(fsmParseList);
            if (candidateParses != null && currentSentence.wordCount() == candidateParses.size()){
                for (int j = 0; j < currentSentence.wordCount(); j++){
                    DisambiguatedWord word = (DisambiguatedWord) currentSentence.getWord(j);
                    if (found[j]){
                        total++;
                        if (word.getParse().toString().toUpperCase(new Locale("tr")).equals(candidateParses.get(j).toString().toUpperCase(new Locale("tr")))){
                            correct++;
                        }
                        if (word.getParse().getWord().getName().toUpperCase(new Locale("tr")).equals(candidateParses.get(j).getWord().getName().toUpperCase(new Locale("tr")))){
                            correctRoot++;
                        }
                        if (word.getParse().getPos().equals(candidateParses.get(j).getPos())){
                            correctPos++;
                        }
                    }
                }
            }
            overall += currentSentence.wordCount();
            if (i % 1000 == 0){
                System.out.println("Tested " + i + " sentences");
            }
        }
        System.out.format("Coverage: %.2f\n", 100 * total / (overall + 0.0));
        performances[0].add(new ClassificationPerformance(100 * correctRoot / (total + 0.0)));
        System.out.format("Current Root Accuracy: %.2f\n", performances[0].meanClassificationPerformance().getAccuracy());
        performances[1].add(new ClassificationPerformance(100 * correctPos / (total + 0.0)));
        System.out.format("Current Pos Accuracy: %.2f\n", performances[1].meanClassificationPerformance().getAccuracy());
        performances[2].add(new ClassificationPerformance(100 * correct / (total + 0.0)));
        System.out.format("Current Disambiguation Accuracy: %.2f\n", performances[2].meanClassificationPerformance().getAccuracy());
    }

    private static MorphologicalDisambiguator trainDummy(DisambiguationCorpus corpus){
        return new DummyDisambiguation();
    }

    private static MorphologicalDisambiguator trainRootFirst(DisambiguationCorpus corpus){
        MorphologicalDisambiguator disambiguator = new RootFirstDisambiguation();
        train(disambiguator, corpus);
        return disambiguator;
    }

    private static MorphologicalDisambiguator trainHmm(DisambiguationCorpus corpus){
        MorphologicalDisambiguator disambiguator = new HmmDisambiguation();
        train(disambiguator, corpus);
        return disambiguator;
    }

    private static MorphologicalDisambiguator trainClassifier(DisambiguationCorpus corpus){
        MorphologicalDisambiguator disambiguator = new RootFirstClassifierDisambiguation(new C45(), new Parameter(1));
        train(disambiguator, corpus);
        return disambiguator;
    }

    public static void main(String[] args){
        /*FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();
        //DisambiguationCorpus corpus = new DisambiguationCorpus("Data/MorphologicalDisambiguation/milliyet.txt");
        DisambiguationCorpus corpus = new DisambiguationCorpus("Data/MorphologicalDisambiguation/penn_treebank.txt");
        ExperimentPerformance[] performances = new ExperimentPerformance[3];
        for (int i = 0; i < 3; i++){
            performances[i] = new ExperimentPerformance();
        }
        corpus.shuffleSentences(1);
        for (int i = 0; i < 10; i++){
            DisambiguationCorpus trainCorpus = (DisambiguationCorpus) corpus.getTrainCorpus(i, 10);
            DisambiguationCorpus testCorpus = (DisambiguationCorpus) corpus.getTestCorpus(i, 10);
            MorphologicalDisambiguator disambiguator = trainClassifier(trainCorpus);
            test(fsm, disambiguator, testCorpus, performances);
        }*/
    }
}
