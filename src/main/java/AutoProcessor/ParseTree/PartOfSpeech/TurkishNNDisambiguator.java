package AutoProcessor.ParseTree.PartOfSpeech;

import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

public class TurkishNNDisambiguator extends TurkishPartOfSpeechDisambiguator{

    public FsmParse[] disambiguate(FsmParseList[] fsmParses, ParseNodeDrawable node, ParseTreeDrawable parseTree) {
        return complexPOSdisambiguate(fsmParses, "NOUN", false);
    }
}
