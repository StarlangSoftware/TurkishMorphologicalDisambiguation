package MorphologicalDisambiguation;

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

    public static String selectCaseForParseString(String parseString, int index, FsmParseList[] fsmParses, ArrayList<FsmParse> correctParses) {
        String surfaceForm = fsmParses[index].getFsmParse(0).getSurfaceForm();
        switch (parseString) {
            /* kısmını, duracağını, grubunun */
            case "P2SG$P3SG":
                if (searchForSecondSingularPerson(fsmParses)) {
                    return "P2SG";
                }
                return "P3SG";
                /* bir */
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
            default:
                break;
        }
        return null;
    }
}
