package AutoProcessor.ParseTree.PartOfSpeech;

import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import MorphologicalAnalysis.MorphologicalTag;

import java.util.ArrayList;

public class TurkishPRPDisambiguator extends TurkishPartOfSpeechDisambiguator{

    public FsmParse[] disambiguate(FsmParseList[] fsmParses, ParseNodeDrawable node, ParseTreeDrawable parseTree) {
        FsmParse[] result = null;
        FsmParse bestFsmParse;
        ArrayList<FsmParse> bestList = new ArrayList<>();
        if (fsmParses.length > 1){
            return null;
        }
        for (int i = 0; i < fsmParses[0].size(); i++){
            if (fsmParses[0].getFsmParse(i).getInitialPos() != null && fsmParses[0].getFsmParse(i).containsTag(MorphologicalTag.PERSONALPRONOUN)){
                bestList.add(fsmParses[0].getFsmParse(i));
            }
        }
        bestFsmParse = caseDisambiguator(bestList);
        if (bestFsmParse != null) {
            result = new FsmParse[1];
            result[0] = bestFsmParse;
        } else {
            bestFsmParse = parseShortEnough(bestList);
            if (bestFsmParse != null){
                result = new FsmParse[1];
                result[0] = bestFsmParse;
            }
        }
        return result;
    }

}
