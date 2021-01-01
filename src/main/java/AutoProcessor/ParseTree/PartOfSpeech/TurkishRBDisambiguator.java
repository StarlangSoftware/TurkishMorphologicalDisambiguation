package AutoProcessor.ParseTree.PartOfSpeech;

import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

public class TurkishRBDisambiguator extends TurkishPartOfSpeechDisambiguator{

    public FsmParse[] disambiguate(FsmParseList[] fsmParses, ParseNodeDrawable node, ParseTreeDrawable parseTree) {
        return complexPOSdisambiguate(fsmParses, "ADVERB", true);
    }
}
