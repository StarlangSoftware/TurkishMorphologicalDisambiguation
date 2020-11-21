package MorphologicalDisambiguation;

import DataStructure.CounterHashMap;
import Dictionary.Word;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import MorphologicalAnalysis.MorphologicalTag;

import java.util.ArrayList;

public abstract class AutoDisambiguator {

    protected FsmMorphologicalAnalyzer morphologicalAnalyzer;
    protected RootWordStatistics rootWordStatistics;

    private static boolean isAnyWordSecondPerson(int index, ArrayList<FsmParse> correctParses) {
        int count = 0;
        for (int i = index - 1; i >= 0; i--) {
            if (correctParses.get(i).containsTag(MorphologicalTag.A2SG) || correctParses.get(i).containsTag(MorphologicalTag.P2SG)) {
                count++;
            }
        }
        return count >= 1;
    }

    private static boolean isPossessivePlural(int index, ArrayList<FsmParse> correctParses) {
        for (int i = index - 1; i >= 0; i--) {
            if (correctParses.get(i).isNoun()) {
                return correctParses.get(i).isPlural();
            }
        }
        return false;
    }

    private static String nextWordPos(FsmParseList nextParseList) {
        CounterHashMap<String> map = new CounterHashMap<>();
        for (int i = 0; i < nextParseList.size(); i++) {
            map.put(nextParseList.getFsmParse(i).getPos());
        }
        return map.max();
    }

    private static boolean isBeforeLastWord(int index, FsmParseList[] fsmParses){
        return index + 2 == fsmParses.length;
    }

    private static boolean isNextWordNoun(int index, FsmParseList[] fsmParses){
        return index + 1 < fsmParses.length && nextWordPos(fsmParses[index + 1]).equals("NOUN");
    }

    private static boolean isFirstWord(int index){
        return index == 0;
    }

    private static boolean isCapital(String surfaceForm){
        return Word.isCapital(surfaceForm);
    }

    private static String selectCaseForParseString(String parseString, int index, FsmParseList[] fsmParses, ArrayList<FsmParse> correctParses) {
        String surfaceForm = fsmParses[index].getFsmParse(0).getSurfaceForm();
        switch (parseString) {
            /* kısmını, duracağını, grubunun */
            case "P2SG$P3SG":
                if (isAnyWordSecondPerson(index, correctParses)) {
                    return "P2SG";
                }
                return "P3SG";
                /* BİR */
            case "A2SG+P2SG$A3SG+P3SG":
                if (isAnyWordSecondPerson(index, correctParses)) {
                    return "A2SG+P2SG";
                }
                return "A3SG+P3SG";
            case "ADJ$ADV$DET$NUM+CARD":
                return "DET";
                /* tahminleri, işleri, hisseleri */
            case "A3PL+P3PL+NOM$A3PL+P3SG+NOM$A3PL+PNON+ACC$A3SG+P3PL+NOM":
                if (isPossessivePlural(index, correctParses)) {
                    return "A3SG+P3PL+NOM";
                }
                return "A3PL+P3SG+NOM";
                /* Ocak, Cuma, ABD */
            case "A3SG$PROP+A3SG":
                if (index > 0) {
                    if (isCapital(surfaceForm)) {
                        return "PROP+A3SG";
                    }
                    return "A3SG";
                }
                /* şirketin, seçimlerin, borsacıların, kitapların */
            case "P2SG+NOM$PNON+GEN":
                if (isAnyWordSecondPerson(index, correctParses)) {
                    return "P2SG+NOM";
                }
                return "PNON+GEN";
                /* ÇOK */
            case "ADJ$ADV$DET$POSTP+PCABL":
                if (index > 0 && correctParses.get(index - 1).containsTag(MorphologicalTag.ABLATIVE)) {
                    return "POSTP+PCABL";
                }
                if (index + 1 < fsmParses.length) {
                    switch (nextWordPos(fsmParses[index + 1])) {
                        case "NOUN":
                            return "ADJ";
                        case "ADJ":
                        case "ADV":
                        case "VERB":
                            return "ADV";
                        default:
                            break;
                    }
                }
            case "ADJ$NOUN+A3SG+PNON+NOM":
                if (isNextWordNoun(index, fsmParses)) {
                    return "ADJ";
                }
                return "NOUN+A3SG+PNON+NOM";
                /* fanatiklerini, senetlerini, olduklarını */
            case "A3PL+P2SG$A3PL+P3PL$A3PL+P3SG$A3SG+P3PL":
                if (isAnyWordSecondPerson(index, correctParses)) {
                    return "A3PL+P2SG";
                }
                if (isPossessivePlural(index, correctParses)) {
                    return "A3SG+P3PL";
                } else {
                    return "A3PL+P3SG";
                }
            case "ADJ$NOUN+PROP+A3SG+PNON+NOM":
                if (index > 0) {
                    if (isCapital(surfaceForm)) {
                        return "NOUN+PROP+A3SG+PNON+NOM";
                    }
                    return "ADJ";
                }
                /* BU, ŞU */
            case "DET$PRON+DEMONSP+A3SG+PNON+NOM":
                if (isNextWordNoun(index, fsmParses)) {
                    return "DET";
                }
                return "PRON+DEMONSP+A3SG+PNON+NOM";
                /* gelebilir */
            case "AOR+A3SG$AOR^DB+ADJ+ZERO":
                if (isBeforeLastWord(index, fsmParses)) {
                    return "AOR+A3SG";
                } else if (isFirstWord(index)) {
                    return "AOR^DB+ADJ+ZERO";
                } else {
                    if (isNextWordNoun(index, fsmParses)) {
                        return "AOR^DB+ADJ+ZERO";
                    } else {
                        return "AOR+A3SG";
                    }
                }
            case "ADV$NOUN+A3SG+PNON+NOM":
                return "ADV";
            case "ADJ$ADV":
                if (isNextWordNoun(index, fsmParses)) {
                    return "ADJ";
                }
                return "ADV";
            case "P2SG$PNON":
                if (isAnyWordSecondPerson(index, correctParses)) {
                    return "P2SG";
                }
                return "PNON";
                /* etti, kırdı */
            case "NOUN+A3SG+PNON+NOM^DB+VERB+ZERO$VERB+POS":
                if (isBeforeLastWord(index, fsmParses)) {
                    return "VERB+POS";
                }
                /* İLE */
            case "CONJ$POSTP+PCNOM":
                return "POSTP+PCNOM";
                /* gelecek */
            case "POS+FUT+A3SG$POS^DB+ADJ+FUTPART+PNON":
                if (isBeforeLastWord(index, fsmParses)) {
                    return "POS+FUT+A3SG";
                }
                return "POS^DB+ADJ+FUTPART+PNON";
            default:
                break;
        }
        return null;
    }

    public static FsmParse caseDisambiguator(int index, FsmParseList[] fsmParses, ArrayList<FsmParse> correctParses) {
        FsmParseList fsmParseList = fsmParses[index];
        FsmParse defaultParse = fsmParseList.caseDisambiguator();
        if (defaultParse != null){
            return defaultParse;
        }
        String defaultCase = selectCaseForParseString(fsmParses[index].parsesWithoutPrefixAndSuffix(), index, fsmParses, correctParses);
        if (defaultCase != null) {
            for (int i = 0; i < fsmParseList.size(); i++) {
                FsmParse fsmParse = fsmParseList.getFsmParse(i);
                if (fsmParse.transitionList().contains(defaultCase)) {
                    return fsmParse;
                }
            }
        }
        return null;
    }

}
