package MorphologicalDisambiguation;

import Corpus.*;
import Dictionary.Word;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import Ngram.*;

import java.io.*;
import java.util.ArrayList;

public class HmmDisambiguation extends NaiveDisambiguation{

    protected NGram<Word> wordBiGramModel;
    protected NGram<Word> igBiGramModel;

    public void train(DisambiguationCorpus corpus) {
        int i, j, k;
        Sentence sentence;
        DisambiguatedWord word, nextWord;
        Word[] words = new Word[2];
        Word[] igs = new Word[2];
        wordUniGramModel = new NGram<Word>(1);
        igUniGramModel = new NGram<Word>(1);
        wordBiGramModel = new NGram<Word>(2);
        igBiGramModel = new NGram<Word>(2);
        for (i = 0; i < corpus.sentenceCount(); i++){
            sentence = corpus.getSentence(i);
            for (j = 0; j < sentence.wordCount() - 1; j++){
                word = (DisambiguatedWord) sentence.getWord(j);
                nextWord = (DisambiguatedWord) sentence.getWord(j + 1);
                words[0] = word.getParse().getWordWithPos();
                words[1] = nextWord.getParse().getWordWithPos();
                wordUniGramModel.addNGram(words);
                wordBiGramModel.addNGram(words);
                for (k = 0; k < nextWord.getParse().size(); k++){
                    igs[0] = new Word(word.getParse().getLastInflectionalGroup().toString());
                    igs[1] = new Word(nextWord.getParse().getInflectionalGroup(k).toString());
                    igBiGramModel.addNGram(igs);
                    igs[0] = igs[1];
                    igUniGramModel.addNGram(igs);
                }
            }
            if (i > 0 && i % 5000 == 0){
                System.out.println("Trained " + i + " of sentences of " + corpus.sentenceCount());
            }
        }
        wordUniGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
        igUniGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
        wordBiGramModel.calculateNGramProbabilities(new InterpolatedSmoothing<Word>(new LaplaceSmoothing<>()));
        igBiGramModel.calculateNGramProbabilities(new InterpolatedSmoothing<Word>(new LaplaceSmoothing<>()));
    }

    public ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses) {
        int i, j, k, t, bestIndex;
        double probability, bestProbability;
        Word w1, w2, ig1, ig2;
        if (fsmParses.length == 0){
            return null;
        }
        for (i = 0; i < fsmParses.length; i++){
            if (fsmParses[i].size() == 0){
                return null;
            }
        }
        ArrayList<FsmParse> correctFsmParses = new ArrayList<FsmParse>();
        double probabilities[][] = new double[fsmParses.length][];
        int best[][] = new int[fsmParses.length][];
        for (i = 0; i < fsmParses.length; i++){
            probabilities[i] = new double[fsmParses[i].size()];
            best[i] = new int[fsmParses[i].size()];
        }
        for (i = 0; i < fsmParses[0].size(); i++){
            FsmParse currentParse = fsmParses[0].getFsmParse(i);
            w1 = currentParse.getWordWithPos();
            probability = wordUniGramModel.getProbability(w1);
            for (j = 0; j < currentParse.size(); j++){
                ig1 = new Word(currentParse.getInflectionalGroup(j).toString());
                probability *= igUniGramModel.getProbability(ig1);
            }
            probabilities[0][i] = Math.log(probability);
        }
        for (i = 1; i < fsmParses.length; i++){
            for (j = 0; j < fsmParses[i].size(); j++){
                bestProbability = -Integer.MAX_VALUE;
                bestIndex = -1;
                FsmParse currentParse = fsmParses[i].getFsmParse(j);
                for (k = 0; k < fsmParses[i - 1].size(); k++){
                    FsmParse previousParse = fsmParses[i - 1].getFsmParse(k);
                    w1 = previousParse.getWordWithPos();
                    w2 = currentParse.getWordWithPos();
                    probability = probabilities[i - 1][k] + Math.log(wordBiGramModel.getProbability(w1, w2));
                    for (t = 0; t < fsmParses[i].getFsmParse(j).size(); t++){
                        ig1 = new Word(previousParse.lastInflectionalGroup().toString());
                        ig2 = new Word(currentParse.getInflectionalGroup(t).toString());
                        probability += Math.log(igBiGramModel.getProbability(ig1, ig2));
                    }
                    if (probability > bestProbability){
                        bestIndex = k;
                        bestProbability = probability;
                    }
                }
                probabilities[i][j] = bestProbability;
                best[i][j] = bestIndex;
            }
        }
        bestProbability = -Integer.MAX_VALUE;
        bestIndex = -1;
        for (i = 0; i < fsmParses[fsmParses.length - 1].size(); i++){
            if (probabilities[fsmParses.length - 1][i] > bestProbability){
                bestProbability = probabilities[fsmParses.length - 1][i];
                bestIndex = i;
            }
        }
        if (bestIndex == -1){
            return null;
        }
        correctFsmParses.add(fsmParses[fsmParses.length - 1].getFsmParse(bestIndex));
        for (i = fsmParses.length - 2; i >= 0; i--){
            bestIndex = best[i + 1][bestIndex];
            if (bestIndex == -1){
                return null;
            }
            correctFsmParses.add(0, fsmParses[i].getFsmParse(bestIndex));
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
