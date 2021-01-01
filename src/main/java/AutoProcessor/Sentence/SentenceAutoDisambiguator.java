package AutoProcessor.Sentence;

import AnnotatedSentence.AnnotatedSentence;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalDisambiguation.AutoDisambiguator;
import MorphologicalDisambiguation.RootWordStatistics;

/**
* Abstract class to disambiguate a sentence automatically. By implementing 3 abstract methods,
* the class can disambiguate
* (1) Simplest situation: Morphological analyses with a single analysis. For this case, there is actually no disambiguation,
* system selects 'the' morphological analysis.
* (2) More complex situation: Morphological analyses in which the possible analyses contain only one distinct root word. For this
* case, the root word would be fixed, but the correct morphological analysis depends on the context.
* (3) Most complex situation: Morphological analyses where there are multiple candidate root words and possibly multiple candidate
* morphological analyses for each candidate root word.
* Each method tries to disambiguate and if successful, sets the correct morphological analysis and the correct
* metamorpheme of the word.
 * */
public abstract class SentenceAutoDisambiguator extends AutoDisambiguator{

    /**
     * The method should disambiguate morphological analyses where there are multiple candidate root words
     * and possibly multiple candidate morphological analyses for each candidate root word. If it finds the correct
     * morphological analysis of a word(s), it should set morphological analysis and metamorpheme of that(those) word(s).
     * To disambiguate between the root words, one can use the root word statistics.
     * @param sentence The sentence to be disambiguated automatically.
     */
    protected abstract void autoDisambiguateMultipleRootWords(AnnotatedSentence sentence);

    /**
     * Constructor for the class.
     * @param morphologicalAnalyzer Morphological analyzer for parsing the words. Morphological analyzer will return all
     *                              possible parses of each word so that the automatic disambiguator can disambiguate the
     *                              words.
     * @param rootWordStatistics The object contains information about the selected correct root words in a corpus for a set
     *                           of possible lemma. Root word statistics can be used to distinguish between possible root words.
     */
    protected SentenceAutoDisambiguator(FsmMorphologicalAnalyzer morphologicalAnalyzer, RootWordStatistics rootWordStatistics){
        this.morphologicalAnalyzer = morphologicalAnalyzer;
        this.rootWordStatistics = rootWordStatistics;
    }

    /**
     * The main method to automatically disambiguate a sentence. The algorithm
     * 1. Disambiguates the morphological analyses with a single analysis.
     * 2. Disambiguates the morphological analyses in which the possible analyses contain only one
     * distinct root word.
     * 3. Disambiguates the morphological analyses where there are multiple candidate root words and
     * possibly multiple candidate morphological analyses for each candidate root word.
     * @param sentence The sentence to be disambiguated automatically.
     */
    public void autoDisambiguate(AnnotatedSentence sentence){
        autoDisambiguateMultipleRootWords(sentence);
    }

}
