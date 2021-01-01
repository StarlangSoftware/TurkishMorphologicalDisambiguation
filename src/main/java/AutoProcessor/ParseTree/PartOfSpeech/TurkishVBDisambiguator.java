package AutoProcessor.ParseTree.PartOfSpeech;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

public class TurkishVBDisambiguator extends TurkishPartOfSpeechDisambiguator{

    public FsmParse[] disambiguate(FsmParseList[] fsmParses, ParseNodeDrawable node, ParseTreeDrawable parseTree) {
        if (node.getLayerData(ViewLayerType.TURKISH_WORD).contains("değil")){
            return null;
        }
        FsmParse[] result = complexPOSdisambiguate(fsmParses, "VERB", false);
        if (result != null){
            for (FsmParse fsmParse : result){
                if (fsmParse.getWord().getName().equals("var")){
                    return null;
                }
            }
        }
        return result;
    }

}
