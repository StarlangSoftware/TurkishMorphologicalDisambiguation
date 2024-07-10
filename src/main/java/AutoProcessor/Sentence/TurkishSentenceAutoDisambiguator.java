package AutoProcessor.Sentence;

import AnnotatedSentence.AnnotatedSentence;
import AnnotatedSentence.AnnotatedWord;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import MorphologicalDisambiguation.LongestRootFirstDisambiguation;

import java.util.ArrayList;

/**
 * Class that implements SentenceAutoDisambiguator for Turkish language.
 */
public class TurkishSentenceAutoDisambiguator extends SentenceAutoDisambiguator{

    LongestRootFirstDisambiguation longestRootFirstDisambiguation;

    /**
     * Constructor for the class.
     */
    public TurkishSentenceAutoDisambiguator() {
        super(new FsmMorphologicalAnalyzer());
        longestRootFirstDisambiguation = new LongestRootFirstDisambiguation();
    }

    /**
     * Constructor for the class.
     * @param fsm                Finite State Machine based morphological analyzer
     */
    public TurkishSentenceAutoDisambiguator(FsmMorphologicalAnalyzer fsm) {
        super(fsm);
    }

    /**
     * If the word has only single root in its possible parses, the method disambiguates by looking special cases.
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
        ArrayList<FsmParse> correctParses = longestRootFirstDisambiguation.disambiguate(fsmParses);
        for (int i = 0; i < sentence.wordCount(); i++){
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (word.getParse() == null){
                setParseAutomatically(correctParses.get(i), word);
            }
        }
    }

}
