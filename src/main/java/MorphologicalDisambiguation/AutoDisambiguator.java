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

    private static boolean searchForSecondSingularPerson(FsmParseList[] fsmParses) {
        int count = 0;
        for (FsmParseList fsmPars : fsmParses) {
            boolean secondSingular = false;
            for (int j = 0; j < fsmPars.size(); j++) {
                if (fsmPars.getFsmParse(j).containsTag(MorphologicalTag.A2SG) || fsmPars.getFsmParse(j).containsTag(MorphologicalTag.P2SG)) {
                    secondSingular = true;
                    break;
                }
            }
            if (secondSingular) {
                count++;
            }
        }
        return count >= 2;
    }

    private static boolean searchForNumberOfNoun(int index, ArrayList<FsmParse> correctParses) {
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

    public static String selectCaseForParseString(String parseString, int index, FsmParseList[] fsmParses, ArrayList<FsmParse> correctParses) {
        String surfaceForm = fsmParses[index].getFsmParse(0).getSurfaceForm();
        switch (parseString) {
            /* kısmını, duracağını, grubunun */
            case "P2SG$P3SG":
                if (searchForSecondSingularPerson(fsmParses)) {
                    return "P2SG";
                }
                return "P3SG";
                /* BİR */
            case "ADJ$ADV$DET$NUM+CARD":
                return "DET";
                /* tahminleri, işleri, hisseleri */
            case "A3PL+P3PL+NOM$A3PL+P3SG+NOM$A3PL+PNON+ACC$A3SG+P3PL+NOM":
                if (searchForNumberOfNoun(index, correctParses)) {
                    return "A3SG+P3PL+NOM";
                }
                return "A3PL+P3SG+NOM";
                /* Ocak, Cuma, ABD */
            case "A3SG$PROP+A3SG":
                if (index > 0) {
                    if (Word.isCapital(surfaceForm)) {
                        return "PROP+A3SG";
                    }
                    return "A3SG";
                }
                /* şirketin, seçimlerin, borsacıların, kitapların */
            case "P2SG+NOM$PNON+GEN":
                if (searchForSecondSingularPerson(fsmParses)) {
                    return "P2SG+NOM";
                }
                return "PNON+GEN";
                /* ÇOK */
            case "ADJ$ADV$DET$POSTP+PCABL":
                if (correctParses.get(index - 1).containsTag(MorphologicalTag.ABLATIVE)) {
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
                if (index + 1 < fsmParses.length) {
                    if ("NOUN".equals(nextWordPos(fsmParses[index + 1]))) {
                        return "ADJ";
                    }
                }
                return "NOUN+A3SG+PNON+NOM";
                /* fanatiklerini, senetlerini, olduklarını */
            case "A3PL+P2SG$A3PL+P3PL$A3PL+P3SG$A3SG+P3PL":
                if (searchForSecondSingularPerson(fsmParses)) {
                    return "A3PL+P2SG";
                }
                if (searchForNumberOfNoun(index, correctParses)) {
                    return "A3SG+P3PL";
                } else {
                    return "A3PL+P3SG";
                }
            case "ADJ$NOUN+PROP+A3SG+PNON+NOM":
                if (index > 0) {
                    if (Word.isCapital(surfaceForm)) {
                        return "NOUN+PROP+A3SG+PNON+NOM";
                    }
                    return "ADJ";
                }
                /* BU, ŞU */
            case "DET$PRON+DEMONSP+A3SG+PNON+NOM":
                if (index + 1 < fsmParses.length) {
                    if ("NOUN".equals(nextWordPos(fsmParses[index + 1]))) {
                        return "DET";
                    }
                }
                return "PRON+DEMONSP+A3SG+PNON+NOM";
                /* gelebilir */
            case "AOR+A3SG$AOR^DB+ADJ+ZERO":
                if (index + 2 == fsmParses.length) {
                    return "AOR+A3SG";
                } else if (index == 0) {
                    return "AOR^DB+ADJ+ZERO";
                } else {
                    if ("NOUN".equals(nextWordPos(fsmParses[index + 1]))) {
                        return "AOR^DB+ADJ+ZERO";
                    } else {
                        return "AOR+A3SG";
                    }
                }
            case "ADV$NOUN+A3SG+PNON+NOM":
                return "ADV";
            case "ADJ$ADV":
                if (nextWordPos(fsmParses[index + 1]).equals("NOUN")) {
                    return "ADJ";
                }
                return "ADV";
            case "P2SG$PNON":
                if (searchForSecondSingularPerson(fsmParses)) {
                    return "P2SG";
                }
                return "PNON";
                /* etti, kırdı */
            case "NOUN+A3SG+PNON+NOM^DB+VERB+ZERO$VERB+POS":
                if (index + 2 == fsmParses.length) {
                    return "VERB+POS";
                }
                /* ile */
            case "CONJ$POSTP+PCNOM":
                return "POSTP+PCNOM";
            case "POS+FUT+A3SG$POS^DB+ADJ+FUTPART+PNON":
                if (index + 2 == fsmParses.length) {
                    return "POS+FUT+A3SG";
                }
                return "POS^DB+ADJ+FUTPART+PNON";
            default:
                break;
        }
        return null;
    }
}
