package MorphologicalDisambiguation;

import Corpus.Sentence;
import Dictionary.Word;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import Ngram.*;

import java.util.ArrayList;

public class RootFirstDisambiguation extends NaiveDisambiguation {

    protected NGram<Word> wordBiGramModel;
    protected NGram<Word> igBiGramModel;

    /**
     * The train method initially creates new NGrams; wordUniGramModel, wordBiGramModel, igUniGramModel, and igBiGramModel. It gets the
     * sentences from given corpus and gets each word as a DisambiguatedWord. Then, adds the word together with its part of speech
     * tags to the wordUniGramModel. It also gets the transition list of that word and adds it to the igUniGramModel.
     * <p>
     * If there exists a next word in the sentence, it adds the current and next {@link DisambiguatedWord} to the wordBiGramModel with
     * their part of speech tags. It also adds them to the igBiGramModel with their transition lists.
     * <p>
     * At the end, it calculates the NGram probabilities of both word and ig unigram models by using LaplaceSmoothing, and
     * both word and ig bigram models by using InterpolatedSmoothing.
     *
     * @param corpus {@link DisambiguationCorpus} to train.
     */
    public void train(DisambiguationCorpus corpus) {
        int i, j;
        Sentence sentence;
        DisambiguatedWord word;
        Word[] words1 = new Word[1];
        Word[] igs1 = new Word[1];
        Word[] words2 = new Word[2];
        Word[] igs2 = new Word[2];
        wordUniGramModel = new NGram<>(1);
        wordBiGramModel = new NGram<>(2);
        igUniGramModel = new NGram<>(1);
        igBiGramModel = new NGram<>(2);
        for (i = 0; i < corpus.sentenceCount(); i++) {
            sentence = corpus.getSentence(i);
            for (j = 0; j < sentence.wordCount(); j++) {
                word = (DisambiguatedWord) sentence.getWord(j);
                words1[0] = word.getParse().getWordWithPos();
                wordUniGramModel.addNGram(words1);
                igs1[0] = new Word(word.getParse().getTransitionList());
                igUniGramModel.addNGram(igs1);
                if (j + 1 < sentence.wordCount()) {
                    words2[0] = words1[0];
                    words2[1] = ((DisambiguatedWord) sentence.getWord(j + 1)).getParse().getWordWithPos();
                    wordBiGramModel.addNGram(words2);
                    igs2[0] = igs1[0];
                    igs2[1] = new Word(((DisambiguatedWord) sentence.getWord(j + 1)).getParse().getTransitionList());
                    igBiGramModel.addNGram(igs2);
                }
            }
        }
        wordUniGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
        igUniGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
        wordBiGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
        igBiGramModel.calculateNGramProbabilities(new LaplaceSmoothing<>());
    }

    /**
     * The getWordProbability method returns the probability of a word by using word bigram or unigram model.
     *
     * @param word             Word to find the probability.
     * @param correctFsmParses FsmParse of given word which will be used for getting part of speech tags.
     * @param index            Index of FsmParse of which part of speech tag will be used to get the probability.
     * @return The probability of the given word.
     */
    protected double getWordProbability(Word word, ArrayList<FsmParse> correctFsmParses, int index) {
        if (index != 0 && correctFsmParses.size() == index) {
            return wordBiGramModel.getProbability(correctFsmParses.get(index - 1).getWordWithPos(), word);
        } else {
            return wordUniGramModel.getProbability(word);
        }
    }

    /**
     * The getIgProbability method returns the probability of a word by using ig bigram or unigram model.
     *
     * @param word             Word to find the probability.
     * @param correctFsmParses FsmParse of given word which will be used for getting transition list.
     * @param index            Index of FsmParse of which transition list will be used to get the probability.
     * @return The probability of the given word.
     */
    protected double getIgProbability(Word word, ArrayList<FsmParse> correctFsmParses, int index) {
        if (index != 0 && correctFsmParses.size() == index) {
            return igBiGramModel.getProbability(new Word(correctFsmParses.get(index - 1).getTransitionList()), word);
        } else {
            return igUniGramModel.getProbability(word);
        }
    }

    /**
     * The getBestRootWord method takes a {@link FsmParseList} as an input and loops through the list. It gets each word with its
     * part of speech tags as a new {@link Word} word and its transition list as a {@link Word} ig. Then, finds their corresponding
     * probabilities. At the end returns the word with the highest probability.
     *
     * @param fsmParseList {@link FsmParseList} is used to get the part of speech tags and transition lists of words.
     * @return The word with the highest probability.
     */
    protected Word getBestRootWord(FsmParseList fsmParseList) {
        double bestProbability, probability;
        Word bestWord;
        bestProbability = -Integer.MAX_VALUE;
        bestWord = null;
        for (int j = 0; j < fsmParseList.size(); j++) {
            Word word = fsmParseList.getFsmParse(j).getWordWithPos();
            Word ig = new Word(fsmParseList.getFsmParse(j).getTransitionList());
            double wordProbability = wordUniGramModel.getProbability(word);
            double igProbability = igUniGramModel.getProbability(ig);
            probability = wordProbability * igProbability;
            if (probability > bestProbability) {
                bestWord = word;
                bestProbability = probability;
            }
        }
        return bestWord;
    }

    /**
     * The getParseWithBestIgProbability gets each {@link FsmParse}'s transition list as a {@link Word} ig. Then, finds the corresponding
     * probabilitt. At the end returns the parse with the highest ig probability.
     *
     * @param parseList        {@link FsmParseList} is used to get the {@link FsmParse}.
     * @param correctFsmParses FsmParse is used to get the transition lists.
     * @param index            Index of FsmParse of which transition list will be used to get the probability.
     * @return The parse with the highest probability.
     */
    protected FsmParse getParseWithBestIgProbability(FsmParseList parseList, ArrayList<FsmParse> correctFsmParses, int index) {
        double bestProbability, probability;
        FsmParse bestParse = null;
        bestProbability = -Integer.MAX_VALUE;
        for (int j = 0; j < parseList.size(); j++) {
            Word ig = new Word(parseList.getFsmParse(j).getTransitionList());
            probability = getIgProbability(ig, correctFsmParses, index);
            if (probability > bestProbability) {
                bestParse = parseList.getFsmParse(j);
                bestProbability = probability;
            }
        }
        return bestParse;
    }

    /**
     * The disambiguate method gets an array of fsmParses. Then loops through that parses and finds the most probable root
     * word and removes the other words which are identical to the most probable root word. At the end, gets the most probable parse
     * among the fsmParses and adds it to the correctFsmParses {@link ArrayList}.
     *
     * @param fsmParses {@link FsmParseList} to disambiguate.
     * @return correctFsmParses {@link ArrayList} which holds the most probable parses.
     */
    public ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses) {
        int i;
        Word bestWord;
        FsmParse bestParse;
        ArrayList<FsmParse> correctFsmParses = new ArrayList<>();
        for (i = 0; i < fsmParses.length; i++) {
            bestWord = getBestRootWord(fsmParses[i]);
            fsmParses[i].reduceToParsesWithSameRootAndPos(bestWord);
            bestParse = getParseWithBestIgProbability(fsmParses[i], correctFsmParses, i);
            if (bestParse != null) {
                correctFsmParses.add(bestParse);
            }
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
