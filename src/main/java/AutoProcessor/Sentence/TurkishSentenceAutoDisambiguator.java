package AutoProcessor.Sentence;

import AnnotatedSentence.AnnotatedSentence;
import AnnotatedSentence.AnnotatedWord;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import MorphologicalDisambiguation.RootWordStatistics;
import MorphologicalDisambiguation.RootWordStatisticsDisambiguation;

import java.util.ArrayList;

/**
 * Class that implements SentenceAutoDisambiguator for Turkish language.
 */
public class TurkishSentenceAutoDisambiguator extends SentenceAutoDisambiguator{

    RootWordStatisticsDisambiguation rootWordStatisticsDisambiguation;

    /**
     * Constructor for the class.
     * @param rootWordStatistics The object contains information about the selected correct root words in a corpus for a set
     *                           of possible lemma. For example, the lemma
     *                           `günü': 2 possible root words `gün' and `günü'
     *                           `çağlar' : 2 possible root words `çağ' and `çağlar'
     */
    public TurkishSentenceAutoDisambiguator(RootWordStatistics rootWordStatistics) {
        super(new FsmMorphologicalAnalyzer(), rootWordStatistics);
        rootWordStatisticsDisambiguation = new RootWordStatisticsDisambiguation();
    }

    /**
     * Constructor for the class.
     * @param fsm                Finite State Machine based morphological analyzer
     * @param rootWordStatistics The object contains information about the selected correct root words in a corpus for a set
     *                           of possible lemma. For example, the lemma
     *                           `günü': 2 possible root words `gün' and `günü'
     *                           `çağlar' : 2 possible root words `çağ' and `çağlar'
     */
    public TurkishSentenceAutoDisambiguator(FsmMorphologicalAnalyzer fsm, RootWordStatistics rootWordStatistics) {
        super(fsm, rootWordStatistics);
    }

    /**
     * If the words has only single root in its possible parses, the method disambiguates by looking special cases.
     * The cases are implemented in the caseDisambiguator method.
     * @param disambiguatedParse Morphological parse of the word.
     * @param word Word to be disambiguated.
     */
    private void setParseAutomatically(FsmParse disambiguatedParse, AnnotatedWord word){
        word.setParse(disambiguatedParse.transitionList());
        word.setMetamorphicParse(disambiguatedParse.withList());
    }

    /**
     * The method disambiguates words with multiple possible root words in its morphological parses. If the word
     * is already morphologically disambiguated, the method does not disambiguate that word. The method first check
     * for multiple root words by using rootWords method. If there are multiple root words, the method select the most
     * occurring root word (if its occurence wrt other root words occurence is above some threshold) for that word
     * using the bestRootWord method. If root word is selected, then the case for single root word is called.
     * @param sentence The sentence to be disambiguated automatically.
     */
    protected void autoDisambiguateMultipleRootWords(AnnotatedSentence sentence) {
        FsmParseList[] fsmParses = morphologicalAnalyzer.robustMorphologicalAnalysis(sentence);
        ArrayList<FsmParse> correctParses = rootWordStatisticsDisambiguation.disambiguate(fsmParses);
        for (int i = 0; i < sentence.wordCount(); i++){
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (word.getParse() == null){
                setParseAutomatically(correctParses.get(i), word);
            }
        }
    }

}
