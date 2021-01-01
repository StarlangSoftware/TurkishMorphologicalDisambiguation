package AutoProcessor.ParseTree.PartOfSpeech;

import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

public class TurkishDollarDisambiguator extends TurkishPartOfSpeechDisambiguator{

    public FsmParse[] disambiguate(FsmParseList[] fsmParses, ParseNodeDrawable node, ParseTreeDrawable parseTree) {
        FsmParse[] result = null;
        for (int i = 0; i < fsmParses[0].size(); i++){
            if (fsmParses[0].getFsmParse(i).getWord().getName().equalsIgnoreCase("dolarlık")){
                if (result == null){
                    result = new FsmParse[1];
                    result[0] = fsmParses[0].getFsmParse(i);
                } else {
                    return null;
                }
            }
        }
        if (result == null){
            for (int i = 0; i < fsmParses[0].size(); i++){
                if (fsmParses[0].getFsmParse(i).getWord().getName().equalsIgnoreCase("dolar")){
                    if (result == null){
                        result = new FsmParse[1];
                        result[0] = fsmParses[0].getFsmParse(i);
                    } else {
                        return null;
                    }
                }
            }
        }
        return result;
    }

}
