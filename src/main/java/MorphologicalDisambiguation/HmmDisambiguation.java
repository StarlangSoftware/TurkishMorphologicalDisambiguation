package MorphologicalDisambiguation;

import Corpus.*;
import Dictionary.Word;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import Ngram.*;

import java.util.ArrayList;

public class HmmDisambiguation extends NaiveDisambiguation {

    protected NGram<Word> wordBiGramModel;
    protected NGram<Word> igBiGramModel;

    /**
     * The train method gets sentences from given {@link DisambiguationCorpus} and both word and the next word of that sentence at each iteration.
     * Then, adds these words together with their part of speech tags to word unigram and bigram models. It also adds the last inflectional group of
     * word to the ig unigram and bigram models.
     * <p>
     * At the end, it calculates the NGram probabilities of both word and ig unigram models by using LaplaceSmoothing, and
     * both word and ig bigram models by using InterpolatedSmoothing.
     *
     * @param corpus {@link DisambiguationCorpus} to train.
     */
    public void train(DisambiguationCorpus corpus) {
        int i, j, k;
        Sentence sentence;
        DisambiguatedWord word, nextWord;
        Word[] words1 = new Word[1];
        Word[] igs1 = new Word[1];
        Word[] words2 = new Word[2];
        Word[] igs2 = new Word[2];
        wordUniGramModel = new NGram<>(1);
        igUniGramModel = new NGram<>(1);
        wordBiGramModel = new NGram<>(2);
        igBiGramModel = new NGram<>(2);
        for (i = 0; i < corpus.sentenceCount(); i++) {
            sentence = corpus.getSentence(i);
            for (j = 0; j < sentence.wordCount() - 1; j++) {
                word = (DisambiguatedWord) sentence.getWord(j);
                nextWord = (DisambiguatedWord) sentence.getWord(j + 1);
                words2[0] = word.getParse().getWordWithPos();
                words1[0] = words2[0];
                words2[1] = nextWord.getParse().getWordWithPos();
                wordUniGramModel.addNGram(words1);
                wordBiGramModel.addNGram(words2);
                for (k = 0; k < nextWord.getParse().size(); k++) {
                    igs2[0] = new Word(word.getParse().getLastInflectionalGroup().toString());
                    igs2[1] = new Word(nextWord.getParse().getInflectionalGroup(k).toString());
                    igBiGramModel.addNGram(igs2);
                    igs1[0] = igs2[1];
                    igUniGramModel.addNGram(igs1);
                }
            }
            if (i > 0 && i % 5000 == 0) {
                System.out.println("Trained " + i + " of sentences of " + corpus.sentenceCount());
            }
        }
        wordUniGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
        igUniGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
        wordBiGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
        igBiGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
    }

    /**
     * The disambiguate method takes {@link FsmParseList} as an input and gets one word with its part of speech tags, then gets its probability
     * from word unigram model. It also gets ig and its probability. Then, hold the logarithmic value of  the product of these probabilities in an array.
     * Also by taking into consideration the parses of these word it recalculates the probabilities and returns these parses.
     *
     * @param fsmParses {@link FsmParseList} to disambiguate.
     * @return ArrayList of FsmParses.
     */
    public ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses) {
        int i, j, k, t, bestIndex;
        double probability, bestProbability;
        Word w1, w2, ig1, ig2;
        if (fsmParses.length == 0) {
            return null;
        }
        for (i = 0; i < fsmParses.length; i++) {
            if (fsmParses[i].size() == 0) {
                return null;
            }
        }
        ArrayList<FsmParse> correctFsmParses = new ArrayList<>();
        double[][] probabilities = new double[fsmParses.length][];
        int[][] best = new int[fsmParses.length][];
        for (i = 0; i < fsmParses.length; i++) {
            probabilities[i] = new double[fsmParses[i].size()];
            best[i] = new int[fsmParses[i].size()];
        }
        for (i = 0; i < fsmParses[0].size(); i++) {
            FsmParse currentParse = fsmParses[0].getFsmParse(i);
            w1 = currentParse.getWordWithPos();
            probability = wordUniGramModel.getProbability(w1);
            for (j = 0; j < currentParse.size(); j++) {
                ig1 = new Word(currentParse.getInflectionalGroup(j).toString());
                probability *= igUniGramModel.getProbability(ig1);
            }
            probabilities[0][i] = Math.log(probability);
        }
        for (i = 1; i < fsmParses.length; i++) {
            for (j = 0; j < fsmParses[i].size(); j++) {
                bestProbability = -Integer.MAX_VALUE;
                bestIndex = -1;
                FsmParse currentParse = fsmParses[i].getFsmParse(j);
                for (k = 0; k < fsmParses[i - 1].size(); k++) {
                    FsmParse previousParse = fsmParses[i - 1].getFsmParse(k);
                    w1 = previousParse.getWordWithPos();
                    w2 = currentParse.getWordWithPos();
                    probability = probabilities[i - 1][k] + Math.log(wordBiGramModel.getProbability(w1, w2));
                    for (t = 0; t < fsmParses[i].getFsmParse(j).size(); t++) {
                        ig1 = new Word(previousParse.lastInflectionalGroup().toString());
                        ig2 = new Word(currentParse.getInflectionalGroup(t).toString());
                        probability += Math.log(igBiGramModel.getProbability(ig1, ig2));
                    }
                    if (probability > bestProbability) {
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
        for (i = 0; i < fsmParses[fsmParses.length - 1].size(); i++) {
            if (probabilities[fsmParses.length - 1][i] > bestProbability) {
                bestProbability = probabilities[fsmParses.length - 1][i];
                bestIndex = i;
            }
        }
        if (bestIndex == -1) {
            return null;
        }
        correctFsmParses.add(fsmParses[fsmParses.length - 1].getFsmParse(bestIndex));
        for (i = fsmParses.length - 2; i >= 0; i--) {
            bestIndex = best[i + 1][bestIndex];
            if (bestIndex == -1) {
                return null;
            }
            correctFsmParses.add(0, fsmParses[i].getFsmParse(bestIndex));
        }
        return correctFsmParses;
    }

    /**
     * Method to save unigrams and bigrams.
     */
    public void saveModel() {
        super.saveModel();
        wordBiGramModel.saveAsText("words2.txt");
        igBiGramModel.saveAsText("igs2.txt");
    }

    /**
     * Method to load unigrams and bigrams.
     */
    public void loadModel() {
        super.loadModel();
        wordBiGramModel = new NGram<>("words2.txt");
        igBiGramModel = new NGram<>("igs2.txt");
    }
}
