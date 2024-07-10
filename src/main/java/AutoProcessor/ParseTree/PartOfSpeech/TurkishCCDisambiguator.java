package AutoProcessor.ParseTree.PartOfSpeech;

import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

public class TurkishCCDisambiguator extends TurkishPartOfSpeechDisambiguator{

    /**
     * The method disambiguate the words in the parse node whose parent node has a CC pos tag. Uses simple pos
     * disambiguator.
     * @param fsmParses All possible morphological parses of all words in the parse node
     * @param node Parse node to disambiguate
     * @param parseTree Parse tree to disambiguate.
     * @return If the method can successfully disambiguate all words in the parse node, it returns those correct parses,
     * otherwise it returns null.
     */
    public FsmParse[] disambiguate(FsmParseList[] fsmParses, ParseNodeDrawable node, ParseTreeDrawable parseTree) {
        return simplePOSDisambiguate(fsmParses, "CONJ");
    }
}
