package MorphologicalDisambiguation;

import Corpus.Sentence;
import Dictionary.Word;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import Ngram.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class RootFirstDisambiguation extends NaiveDisambiguation{

    protected NGram<Word> wordBiGramModel;
    protected NGram<Word> igBiGramModel;

    public void train(DisambiguationCorpus corpus) {
        int i, j;
        Sentence sentence;
        DisambiguatedWord word;
        Word[] words = new Word[2];
        Word[] igs = new Word[2];
        wordUniGramModel = new NGram<Word>(1);
        wordBiGramModel = new NGram<Word>(2);
        igUniGramModel = new NGram<Word>(1);
        igBiGramModel = new NGram<Word>(2);
        for (i = 0; i < corpus.sentenceCount(); i++){
            sentence = corpus.getSentence(i);
            for (j = 0; j < sentence.wordCount(); j++){
                word = (DisambiguatedWord) sentence.getWord(j);
                words[0] = word.getParse().getWordWithPos();
                wordUniGramModel.addNGram(words);
                igs[0] = new Word(word.getParse().getTransitionList());
                igUniGramModel.addNGram(igs);
                if (j + 1 < sentence.wordCount()){
                    words[1] = ((DisambiguatedWord) sentence.getWord(j + 1)).getParse().getWordWithPos();
                    wordBiGramModel.addNGram(words);
                    igs[1] = new Word(((DisambiguatedWord) sentence.getWord(j + 1)).getParse().getTransitionList());
                    igBiGramModel.addNGram(igs);
                }
            }
            if (i > 0 && i % 5000 == 0){
                System.out.println("Trained " + i + " of sentences of " + corpus.sentenceCount());
            }
        }
        wordUniGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
        igUniGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
        wordBiGramModel.calculateNGramProbabilities(new InterpolatedSmoothing<>(new LaplaceSmoothing()));
        igBiGramModel.calculateNGramProbabilities(new InterpolatedSmoothing<>(new LaplaceSmoothing()));
    }

    protected double getWordProbability(Word word, ArrayList<FsmParse> correctFsmParses, int index) {
        if (index != 0 && correctFsmParses.size() == index){
            return wordBiGramModel.getProbability(correctFsmParses.get(index - 1).getWordWithPos(), word);
        } else {
            return wordUniGramModel.getProbability(word);
        }
    }

    protected double getIgProbability(Word word, ArrayList<FsmParse> correctFsmParses, int index) {
        if (index != 0 && correctFsmParses.size() == index){
            return igBiGramModel.getProbability(new Word(correctFsmParses.get(index - 1).getTransitionList()), word);
        } else {
            return igUniGramModel.getProbability(word);
        }
    }

    protected Word getBestRootWord(FsmParseList fsmParseList){
        double bestProbability, probability;
        Word bestWord;
        bestProbability = -Integer.MAX_VALUE;
        bestWord = null;
        for (int j = 0; j < fsmParseList.size(); j++){
            Word word = fsmParseList.getFsmParse(j).getWordWithPos();
            Word ig = new Word(fsmParseList.getFsmParse(j).getTransitionList());
            double wordProbability = wordUniGramModel.getProbability(word);
            double igProbability = igUniGramModel.getProbability(ig);
            probability = wordProbability * igProbability;
            if (probability > bestProbability){
                bestWord = word;
                bestProbability = probability;
            }
        }
        return bestWord;
    }

    protected FsmParse getParseWithBestIgProbability(FsmParseList parseList, ArrayList<FsmParse> correctFsmParses, int index){
        double bestProbability, probability;
        FsmParse bestParse = null;
        bestProbability = -Integer.MAX_VALUE;
        for (int j = 0; j < parseList.size(); j++){
            Word ig = new Word(parseList.getFsmParse(j).getTransitionList());
            probability = getIgProbability(ig, correctFsmParses, index);
            if (probability > bestProbability){
                bestParse = parseList.getFsmParse(j);
                bestProbability = probability;
            }
        }
        return bestParse;
    }

    public ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses) {
        int i;
        Word bestWord;
        FsmParse bestParse;
        ArrayList<FsmParse> correctFsmParses = new ArrayList<FsmParse>();
        for (i = 0; i < fsmParses.length; i++){
            bestWord = getBestRootWord(fsmParses[i]);
            fsmParses[i].reduceToParsesWithSameRootAndPos(bestWord);
            bestParse = getParseWithBestIgProbability(fsmParses[i], correctFsmParses, i);
            if (bestParse != null){
                correctFsmParses.add(bestParse);
            }
        }
        return correctFsmParses;
    }

    public void saveModel() {
        super.saveModel();
        wordBiGramModel.save("words.2gram");
        igBiGramModel.save("igs.2gram");
    }

    public void loadModel() {
        super.loadModel();
        ObjectInputStream inObject;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            inObject = new ObjectInputStream(classLoader.getResourceAsStream("words.2gram"));
            wordBiGramModel = (NGram<Word>) inObject.readObject();
            inObject = new ObjectInputStream(classLoader.getResourceAsStream("igs.2gram"));
            igBiGramModel = (NGram<Word>) inObject.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}
