package AutoProcessor.ParseTree.PartOfSpeech;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.*;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

import java.util.ArrayList;

public abstract class TurkishPartOfSpeechDisambiguator implements PartOfSpeechDisambiguator{

    public static boolean isLastNode(int i, ArrayList<ParseNodeDrawable> leafList){
        int j = i + 1;
        while (j < leafList.size()){
            if (leafList.get(j).getLayerData(ViewLayerType.INFLECTIONAL_GROUP) == null || !leafList.get(j).getLayerData(ViewLayerType.INFLECTIONAL_GROUP).contains("PUNC")){
                return false;
            }
            j++;
        }
        return true;
    }

    public static String defaultCaseForParseString(String rootForm, String parseString, String partOfSpeech){
        String defaultCase = null;
        switch (parseString){
            case "A3PL+P3PL+NOM$A3PL+P3SG+NOM$A3PL+PNON+ACC$A3SG+P3PL+NOM":
                defaultCase = "A3PL+P3SG+NOM";
                break;
            case "P3SG+NOM$PNON+ACC":
                if (partOfSpeech.equals("PROP")){
                    defaultCase = "PNON+ACC";
                } else {
                    defaultCase = "P3SG+NOM";
                }
                break;
            case "A3SG+P2SG$A3SG+PNON":
                defaultCase = "A3SG+PNON";
                break;
            case "A2SG+P2SG$A3SG+P3SG":
                defaultCase = "A3SG+P3SG";
                break;
            case "P2SG$P3SG":
                defaultCase = "P3SG";
                break;
            case "A3SG+PNON+NOM^DB+VERB+ZERO+PRES+A3PL$A3PL+PNON+NOM":
                defaultCase = "A3PL+PNON+NOM";
                break;
            case "P2SG+NOM$PNON+GEN":
                defaultCase = "PNON+GEN";
                break;
            case "AOR^DB+ADJ+ZERO$AOR+A3SG":
                defaultCase = "AOR+A3SG";
                break;
            case "P2SG$PNON":
                defaultCase = "PNON";
                break;
            case "ADV+SINCE$VERB+ZERO+PRES+COP+A3SG":
                if (rootForm.equalsIgnoreCase("yıl") || rootForm.equalsIgnoreCase("süre") || rootForm.equalsIgnoreCase("zaman") || rootForm.equalsIgnoreCase("ay")){
                    defaultCase = "ADV+SINCE";
                } else {
                    defaultCase = "VERB+ZERO+PRES+COP+A3SG";
                }
                break;
            case "CONJ$VERB+POS+IMP+A2SG":
                defaultCase = "CONJ";
                break;
            case "NEG+IMP+A2SG$POS^DB+NOUN+INF2+A3SG+PNON+NOM":
                defaultCase = "POS^DB+NOUN+INF2+A3SG+PNON+NOM";
                break;
            case "NEG+OPT+A3SG$POS^DB+NOUN+INF2+A3SG+PNON+DAT":
                defaultCase = "POS^DB+NOUN+INF2+A3SG+PNON+DAT";
                break;
            case "NOUN+A3SG+P3SG+NOM$NOUN^DB+ADJ+ALMOST":
                defaultCase = "NOUN+A3SG+P3SG+NOM";
                break;
            case "ADJ$VERB+POS+IMP+A2SG":
                defaultCase = "ADJ";
                break;
            case "NOUN+A3SG+PNON+NOM$VERB+POS+IMP+A2SG":
                defaultCase = "NOUN+A3SG+PNON+NOM";
                break;
            case "INF2+A3SG+P3SG+NOM$INF2^DB+ADJ+ALMOST$":
                defaultCase = "INF2+A3SG+P3SG+NOM";
                break;
        }
        return defaultCase;
    }

    protected FsmParse parseShortEnough(ArrayList<FsmParse> fsmParses){
        int minLength = Integer.MAX_VALUE, min2Length = Integer.MAX_VALUE;
        FsmParse bestFsmParse = null;
        for (FsmParse fsmParse : fsmParses){
            int length = fsmParse.transitionList().length();
            if (length < minLength){
                min2Length = minLength;
                minLength = length;
                bestFsmParse = fsmParse;
            } else {
                if (length < min2Length){
                    min2Length = length;
                }
            }
        }
        if (min2Length - minLength > 10 && !bestFsmParse.transitionList().endsWith("ADV+SINCE") && !bestFsmParse.transitionList().endsWith("NOUN+A3SG+P1SG+DAT") && !bestFsmParse.transitionList().endsWith("NOUN+A3SG+PNON+DAT")){
            return bestFsmParse;
        }
        return null;
    }

    protected FsmParse caseDisambiguator(ArrayList<FsmParse> fsmParses){
        String defaultCase;
        if (fsmParses.size() == 1){
            return fsmParses.get(0);
        }
        if (fsmParses.size() == 0){
            return null;
        }
        String parseString = new FsmParseList(fsmParses).parsesWithoutPrefixAndSuffix();
        defaultCase = defaultCaseForParseString(fsmParses.get(0).getWord().getName(), parseString, fsmParses.get(0).getFinalPos());
        if (defaultCase != null){
            for (FsmParse fsmParse : fsmParses){
                if (fsmParse.transitionList().contains(defaultCase)){
                    return fsmParse;
                }
            }
        }
        return null;
    }

    private FsmParse[] singleWordInitialPosDisambiguate(FsmParseList[] fsmParses, String partOfSpeech){
        FsmParse[] result = null;
        for (int i = 0; i < fsmParses[0].size(); i++){
            if (fsmParses[0].getFsmParse(i).getInitialPos() != null && fsmParses[0].getFsmParse(i).getInitialPos().equals(partOfSpeech)){
                if (result == null){
                    result = new FsmParse[1];
                    result[0] = fsmParses[0].getFsmParse(i);
                } else {
                    return null;
                }
            }
        }
        return result;
    }

    public FsmParse[] simpleSingleWordDisambiguate(FsmParseList[] fsmParses, String partOfSpeech) {
        if (fsmParses.length > 1){
            return null;
        }
        return singleWordInitialPosDisambiguate(fsmParses, partOfSpeech);
    }

    private FsmParse[] singleWordWithParseLengthDisambiguate(FsmParseList[] fsmParses, String partOfSpeech, boolean initialPos){
        FsmParse[] result = null;
        FsmParse bestFsmParse;
        ArrayList<FsmParse> bestList = new ArrayList<>();
        for (int i = 0; i < fsmParses[0].size(); i++){
            if (initialPos){
                if (fsmParses[0].getFsmParse(i).getInitialPos() != null && fsmParses[0].getFsmParse(i).getInitialPos().equals(partOfSpeech)){
                    bestList.add(fsmParses[0].getFsmParse(i));
                }
            } else {
                if (fsmParses[0].getFsmParse(i).getFinalPos() != null && fsmParses[0].getFsmParse(i).getFinalPos().equals(partOfSpeech)){
                    bestList.add(fsmParses[0].getFsmParse(i));
                }
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

    public FsmParse[] simplePOSDisambiguate(FsmParseList[] fsmParses, String partOfSpeech) {
        FsmParse[] result = null;
        switch (fsmParses.length){
            case 1:
                return singleWordInitialPosDisambiguate(fsmParses, partOfSpeech);
            case 2:
                for (int i = 0; i < fsmParses[0].size(); i++){
                    if (fsmParses[0].getFsmParse(i).getInitialPos() != null && fsmParses[0].getFsmParse(i).getInitialPos().equals(partOfSpeech)){
                        if (result == null){
                            result = new FsmParse[2];
                            result[0] = fsmParses[0].getFsmParse(i);
                        } else {
                            return null;
                        }
                    }
                }
                for (int i = 0; i < fsmParses[1].size(); i++){
                    if (fsmParses[1].getFsmParse(i).getInitialPos() != null && fsmParses[1].getFsmParse(i).getInitialPos().equals(partOfSpeech)){
                        if (result != null && result[1] == null){
                            result[1] = fsmParses[1].getFsmParse(i);
                        } else {
                            return null;
                        }
                    }
                }
                if (result != null && result[1] == null){
                    return null;
                }
                break;
        }
        return result;
    }

    public FsmParse[] singleWordDisambiguate(FsmParseList[] fsmParses, String partOfSpeech) {
        if (fsmParses.length > 1){
            return null;
        }
        return singleWordWithParseLengthDisambiguate(fsmParses, partOfSpeech, true);
    }

    public FsmParse[] complexPOSdisambiguate(FsmParseList[] fsmParses, String partOfSpeech, boolean initialPos) {
        FsmParse[] result = null;
        FsmParse bestFsmParse;
        ArrayList<FsmParse> bestList;
        switch (fsmParses.length) {
            case 1:
                return singleWordWithParseLengthDisambiguate(fsmParses, partOfSpeech, initialPos);
            case 2:
                bestList = new ArrayList<>();
                for (int i = 0; i < fsmParses[0].size(); i++){
                    bestList.add(fsmParses[0].getFsmParse(i));
                }
                bestFsmParse = caseDisambiguator(bestList);
                if (bestFsmParse != null){
                    result = new FsmParse[2];
                    result[0] = bestFsmParse;
                } else {
                    return null;
                }
                bestList = new ArrayList<>();
                for (int i = 0; i < fsmParses[1].size(); i++) {
                    if (initialPos){
                        if (fsmParses[1].getFsmParse(i).getInitialPos() != null && fsmParses[1].getFsmParse(i).getInitialPos().equals(partOfSpeech)){
                            bestList.add(fsmParses[1].getFsmParse(i));
                        }
                    } else {
                        if (fsmParses[1].getFsmParse(i).getFinalPos() != null && fsmParses[1].getFsmParse(i).getFinalPos().equals(partOfSpeech)) {
                            bestList.add(fsmParses[1].getFsmParse(i));
                        }
                    }
                }
                bestFsmParse = caseDisambiguator(bestList);
                if (bestFsmParse != null) {
                    result[1] = bestFsmParse;
                } else {
                    bestFsmParse = parseShortEnough(bestList);
                    if (bestFsmParse != null){
                        result[1] = bestFsmParse;
                    } else {
                        result = null;
                    }
                }
                break;
        }
        return result;
    }

    public boolean containsPOS(FsmParseList[] fsmParses, String partOfSpeech, boolean initialPos){
        for (int i = 0; i < fsmParses.length; i++){
            for (int j = 0; j < fsmParses[i].size(); j++){
                if (initialPos){
                    if (fsmParses[i].getFsmParse(j).getInitialPos() != null && fsmParses[i].getFsmParse(j).getInitialPos().equals(partOfSpeech)){
                        return true;
                    }
                } else {
                    if (fsmParses[i].getFsmParse(j).getFinalPos() != null && fsmParses[i].getFsmParse(j).getFinalPos().equals(partOfSpeech)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public FsmParse[] complexMultipleWordsPOSdisambiguate(FsmParseList[] fsmParses, String partOfSpeech) {
        FsmParse[] result = null;
        FsmParse bestFsmParse;
        ArrayList<FsmParse> bestList;
        switch (fsmParses.length){
            case 1:
                return singleWordWithParseLengthDisambiguate(fsmParses, partOfSpeech, true);
            case 2:
                bestList = new ArrayList<>();
                for (int i = 0; i < fsmParses[0].size(); i++){
                    if (fsmParses[0].getFsmParse(i).getInitialPos() != null && fsmParses[0].getFsmParse(i).getInitialPos().equals(partOfSpeech)){
                        bestList.add(fsmParses[0].getFsmParse(i));
                    }
                }
                bestFsmParse = caseDisambiguator(bestList);
                if (bestFsmParse == null){
                    bestFsmParse = parseShortEnough(bestList);
                }
                if (bestFsmParse != null){
                    result = new FsmParse[2];
                    result[0] = bestFsmParse;
                    bestList = new ArrayList<>();
                    for (int i = 0; i < fsmParses[1].size(); i++){
                        if (fsmParses[1].getFsmParse(i).getInitialPos() != null && fsmParses[1].getFsmParse(i).getInitialPos().equals(partOfSpeech)){
                            bestList.add(fsmParses[1].getFsmParse(i));
                        }
                    }
                    bestFsmParse = caseDisambiguator(bestList);
                    if (bestFsmParse != null) {
                        result[1] = bestFsmParse;
                    } else {
                        bestFsmParse = parseShortEnough(bestList);
                        if (bestFsmParse != null){
                            result[1] = bestFsmParse;
                        } else {
                            result = null;
                        }
                    }
                }
                break;
        }
        return result;
    }

}
