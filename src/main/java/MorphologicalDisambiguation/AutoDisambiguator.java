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

    private static boolean nextWordExists(int index, FsmParseList[] fsmParses) {
        return index + 1 < fsmParses.length;
    }

    private static boolean isNextWordNoun(int index, FsmParseList[] fsmParses){
        return index + 1 < fsmParses.length && nextWordPos(fsmParses[index + 1]).equals("NOUN");
    }

    private static boolean isNextWordNounOrAdjective(int index, FsmParseList[] fsmParses) {
        return index + 1 < fsmParses.length && (nextWordPos(fsmParses[index + 1]).equals("NOUN") || nextWordPos(fsmParses[index + 1]).equals("ADJ"));
    }

    private static boolean isFirstWord(int index){
        return index == 0;
    }

    private static boolean isCapital(String surfaceForm){
        return Word.isCapital(surfaceForm);
    }

    private static boolean containsTwoNeOrYa(FsmParseList[] fsmParses, String word) {
        int count = 0;
        for (FsmParseList fsmPars : fsmParses) {
            String surfaceForm = fsmPars.getFsmParse(0).getSurfaceForm();
            if (surfaceForm.equals(word)) {
                count++;
            }
        }
        return count == 2;
    }

    private static boolean hasPreviousWordTag(int index, ArrayList<FsmParse> correctParses, MorphologicalTag tag) {
        return index > 0 && correctParses.get(index - 1).containsTag(tag);
    }

    private static String selectCaseForParseString(String parseString, int index, FsmParseList[] fsmParses, ArrayList<FsmParse> correctParses) {
        String surfaceForm = fsmParses[index].getFsmParse(0).getSurfaceForm();
        String root = fsmParses[index].getFsmParse(0).getWord().getName();
        String lastWord = fsmParses[fsmParses.length - 1].getFsmParse(0).getSurfaceForm();
        switch (parseString) {
            /* kısmını, duracağını, grubunun */
            case "P2SG$P3SG":
                if (isAnyWordSecondPerson(index, correctParses)) {
                    return "P2SG";
                }
                return "P3SG";
            case "A2SG+P2SG$A3SG+P3SG":
                if (isAnyWordSecondPerson(index, correctParses)) {
                    return "A2SG+P2SG";
                }
                return "A3SG+P3SG";
            /* BİR */
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
                /* FAZLA */
            case "ADJ$ADV$POSTP+PCABL":
                if (hasPreviousWordTag(index, correctParses, MorphologicalTag.ABLATIVE)) {
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
                if (isNextWordNounOrAdjective(index, fsmParses)) {
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
                    if (isNextWordNounOrAdjective(index, fsmParses)) {
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
            case "ADJ^DB$NOUN+A3SG+PNON+NOM^DB":
                if (root.equals("yok") || root.equals("düşük") || root.equals("eksik") || root.equals("rahat") || root.equals("orta") || root.equals("vasat")) {
                    return "ADJ^DB";
                }
                return "NOUN+A3SG+PNON+NOM^DB";
                /* yaptık, şüphelendik */
            case "POS+PAST+A1PL$POS^DB+ADJ+PASTPART+PNON$POS^DB+NOUN+PASTPART+A3SG+PNON+NOM":
                return "POS+PAST+A1PL";
                /* ederim, yaparım */
            case "AOR+A1SG$AOR^DB+ADJ+ZERO^DB+NOUN+ZERO+A3SG+P1SG+NOM":
                return "AOR+A1SG";
                /* geçti, vardı, aldı */
            case "ADJ^DB+VERB+ZERO$VERB+POS":
                if (root.equals("var") && !isPossessivePlural(index, correctParses)) {
                    return "ADJ^DB+VERB+ZERO";
                }
                return "VERB+POS";
                /* ancak */
            case "ADV$CONJ":
                return "CONJ";
                /* yaptığı, ettiği */
            case "ADJ+PASTPART+P3SG$NOUN+PASTPART+A3SG+P3SG+NOM":
                if (isNextWordNounOrAdjective(index, fsmParses)) {
                    return "ADJ+PASTPART+P3SG";
                }
                return "NOUN+PASTPART+A3SG+P3SG+NOM";
                /* ÖNCE, SONRA */
            case "ADV$NOUN+A3SG+PNON+NOM$POSTP+PCABL":
                if (hasPreviousWordTag(index, correctParses, MorphologicalTag.ABLATIVE)) {
                    return "POSTP+PCABL";
                }
                return "ADV";
            case "NARR+A3SG$NARR^DB+ADJ+ZERO":
                if (isBeforeLastWord(index, fsmParses)) {
                    return "NARR+A3SG";
                }
                return "NARR^DB+ADJ+ZERO";
            case "ADJ$NOUN+A3SG+PNON+NOM$NOUN+PROP+A3SG+PNON+NOM":
                if (index > 0) {
                    if (isCapital(surfaceForm)) {
                        return "NOUN+PROP+A3SG+PNON+NOM";
                    }
                } else {
                    if (isNextWordNounOrAdjective(index, fsmParses)) {
                        return "ADJ";
                    }
                    return "NOUN+A3SG+PNON+NOM";
                }
                /* ödediğim */
            case "ADJ+PASTPART+P1SG$NOUN+PASTPART+A3SG+P1SG+NOM":
                if (isNextWordNounOrAdjective(index, fsmParses)) {
                    return "ADJ+PASTPART+P1SG";
                }
                return "NOUN+PASTPART+A3SG+P1SG+NOM";
                /* O */
            case "DET$PRON+DEMONSP+A3SG+PNON+NOM$PRON+PERS+A3SG+PNON+NOM":
                if (isNextWordNoun(index, fsmParses)) {
                    return "DET";
                }
                return "PRON+PERS+A3SG+PNON+NOM";
                /* BAZI */
            case "ADJ$DET$PRON+QUANTP+A3SG+P3SG+NOM":
                return "DET";
                /* ONUN, ONA, ONDAN, ONUNLA, OYDU, ONUNKİ */
            case "DEMONSP$PERS":
                return "PERS";
            case "ADJ$NOUN+A3SG+PNON+NOM$VERB+POS+IMP+A2SG":
                if (isNextWordNounOrAdjective(index, fsmParses)) {
                    return "ADJ";
                }
                return "NOUN+A3SG+PNON+NOM";
                /* hazineler, kıymetler */
            case "A3PL+PNON+NOM$A3SG+PNON+NOM^DB+VERB+ZERO+PRES+A3PL$PROP+A3PL+PNON+NOM":
                if (index > 0) {
                    if (isCapital(surfaceForm)) {
                        return "PROP+A3PL+PNON+NOM";
                    }
                    return "A3PL+PNON+NOM";
                }
                /* ARTIK, GERİ */
            case "ADJ$ADV$NOUN+A3SG+PNON+NOM":
                if (root.equals("artık")) {
                    return "ADV";
                } else if (isNextWordNoun(index, fsmParses)) {
                    return "ADJ";
                }
                return "ADV";
            case "P1SG+NOM$PNON+NOM^DB+VERB+ZERO+PRES+A1SG":
                if (isBeforeLastWord(index, fsmParses) || root.equals("değil")) {
                    return "PNON+NOM^DB+VERB+ZERO+PRES+A1SG";
                }
                return "P1SG+NOM";
                /* görülmektedir */
            case "POS+PROG2$POS^DB+NOUN+INF+A3SG+PNON+LOC^DB+VERB+ZERO+PRES":
                return "POS+PROG2";
                /* NE */
            case "ADJ$ADV$CONJ$PRON+QUESP+A3SG+PNON+NOM":
                if (lastWord.equals("?")) {
                    return "PRON+QUESP+A3SG+PNON+NOM";
                }
                if (containsTwoNeOrYa(fsmParses, "ne")) {
                    return "CONJ";
                }
                if (isNextWordNoun(index, fsmParses)) {
                    return "ADJ";
                }
                return "ADV";
                /* TÜM */
            case "DET$NOUN+A3SG+PNON+NOM":
                return "DET";
                /* AZ */
            case "ADJ$ADV$POSTP+PCABL$VERB+POS+IMP+A2SG":
                if (hasPreviousWordTag(index, correctParses, MorphologicalTag.ABLATIVE)) {
                    return "POSTP+PCABL";
                }
                if (isNextWordNounOrAdjective(index, fsmParses)) {
                    return "ADJ";
                }
                return "ADV";
                /* görülmedik */
            case "NEG+PAST+A1PL$NEG^DB+ADJ+PASTPART+PNON$NEG^DB+NOUN+PASTPART+A3SG+PNON+NOM":
                if (surfaceForm.equals("alışılmadık")) {
                    return "NEG^DB+ADJ+PASTPART+PNON";
                }
                return "NEG+PAST+A1PL";
            case "DATE$NUM+FRACTION":
                return "NUM+FRACTION";
                /* giriş, satış, öpüş, vuruş */
            case "POS^DB+NOUN+INF3+A3SG+PNON+NOM$RECIP+POS+IMP+A2SG":
                return "POS^DB+NOUN+INF3+A3SG+PNON+NOM";
                /* başka, yukarı */
            case "ADJ$POSTP+PCABL":
                if (hasPreviousWordTag(index, correctParses, MorphologicalTag.ABLATIVE)) {
                    return "POSTP+PCABL";
                }
                return "ADJ";
                /* KARŞI */
            case "ADJ$ADV$NOUN+A3SG+PNON+NOM$POSTP+PCDAT":
                if (hasPreviousWordTag(index, correctParses, MorphologicalTag.DATIVE)) {
                    return "POSTP+PCDAT";
                }
                if (isNextWordNoun(index, fsmParses)) {
                    return "ADJ";
                }
                return "ADV";
                /* BEN */
            case "NOUN+A3SG$NOUN+PROP+A3SG$PRON+PERS+A1SG":
                return "PRON+PERS+A1SG";
                /* yapıcı, verici */
            case "ADJ+AGT$NOUN+AGT+A3SG+PNON+NOM":
                if (isNextWordNounOrAdjective(index, fsmParses)) {
                    return "ADJ+AGT";
                }
                return "NOUN+AGT+A3SG+PNON+NOM";
                /* BİLE */
            case "ADV$VERB+POS+IMP+A2SG":
                return "ADV";
                /* ortalamalar, uzaylılar, demokratlar */
            case "NOUN+ZERO+A3PL+PNON+NOM$VERB+ZERO+PRES+A3PL":
                return "NOUN+ZERO+A3PL+PNON+NOM";
                /* yasa, diye, yıla */
            case "NOUN+A3SG+PNON+DAT$VERB+POS+OPT+A3SG":
                return "NOUN+A3SG+PNON+DAT";
                /* BİZ, BİZE */
            case "NOUN+A3SG$PRON+PERS+A1PL":
                return "PRON+PERS+A1PL";
                /* AZDI */
            case "ADJ^DB+VERB+ZERO$POSTP+PCABL^DB+VERB+ZERO$VERB+POS":
                return "ADJ^DB+VERB+ZERO";
                /* BİRİNCİ, İKİNCİ, ÜÇÜNCÜ, DÖRDÜNCÜ, BEŞİNCİ */
            case "ADJ$NUM+ORD":
                return "ADJ";
                /* AY */
            case "INTERJ$NOUN+A3SG+PNON+NOM$VERB+POS+IMP+A2SG":
                return "NOUN+A3SG+PNON+NOM";
                /* konuşmam, savunmam, etmem */
            case "NEG+AOR+A1SG$POS^DB+NOUN+INF2+A3SG+P1SG+NOM":
                return "NEG+AOR+A1SG";
                /* YA */
            case "CONJ$INTERJ":
                if (containsTwoNeOrYa(fsmParses, "ya")) {
                    return "CONJ";
                }
                if (nextWordExists(index, fsmParses) && fsmParses[index + 1].getFsmParse(0).getSurfaceForm().equalsIgnoreCase("da")) {
                    return "CONJ";
                }
                return "INTERJ";
            case "A3PL+P3PL$A3PL+P3SG$A3SG+P3PL":
                if (isPossessivePlural(index, correctParses)) {
                    return "A3SG+P3PL";
                }
                return "A3PL+P3SG";
                /* YÜZDE, YÜZLÜ */
            case "NOUN$NUM+CARD^DB+NOUN+ZERO":
                return "NOUN";
                /* almanlar, uzmanlar, elmaslar, katiller */
            case "ADJ^DB+VERB+ZERO+PRES+A3PL$NOUN+A3PL+PNON+NOM$NOUN+A3SG+PNON+NOM^DB+VERB+ZERO+PRES+A3PL":
                return "NOUN+A3PL+PNON+NOM";
                /* fazlası, yetkilisi */
            case "ADJ+JUSTLIKE$NOUN+ZERO+A3SG+P3SG+NOM":
                return "NOUN+ZERO+A3SG+P3SG+NOM";
                /* HERKES, HERKESTEN, HERKESLE, HERKES */
            case "NOUN+A3SG+PNON$PRON+QUANTP+A3PL+P3PL":
                return "PRON+QUANTP+A3PL+P3PL";
                /* BEN, BENDEN, BENCE, BANA, BENDE */
            case "NOUN+A3SG$PRON+PERS+A1SG":
                return "PRON+PERS+A1SG";
                /* karşısından, geriye, geride */
            case "ADJ^DB+NOUN+ZERO$NOUN":
                return "ADJ^DB+NOUN+ZERO";
                /* gideceği, kalacağı */
            case "ADJ+FUTPART+P3SG$NOUN+FUTPART+A3SG+P3SG+NOM":
                if (isNextWordNounOrAdjective(index, fsmParses)) {
                    return "ADJ+FUTPART+P3SG";
                }
                return "NOUN+FUTPART+A3SG+P3SG+NOM";
                /* bildiğimiz, geçtiğimiz, yaşadığımız */
            case "ADJ+PASTPART+P1PL$NOUN+PASTPART+A3SG+P1PL+NOM":
                return "ADJ+PASTPART+P1PL";
                /* eminim, memnunum, açım */
            case "NOUN+ZERO+A3SG+P1SG+NOM$VERB+ZERO+PRES+A1SG":
                return "VERB+ZERO+PRES+A1SG";
                /* yaparlar, olabilirler, değiştirirler */
            case "AOR+A3PL$AOR^DB+ADJ+ZERO^DB+NOUN+ZERO+A3PL+PNON+NOM":
                return "AOR+A3PL";
                /* san, yasa */
            case "NOUN+A3SG+PNON+NOM$NOUN+PROP+A3SG+PNON+NOM$VERB+POS+IMP+A2SG":
                if (index > 0) {
                    if (isCapital(surfaceForm)) {
                        return "NOUN+PROP+A3SG+PNON+NOM";
                    }
                    return "NOUN+A3SG+PNON+NOM";
                }
                /* etmeyecek, yapmayacak, koşmayacak */
            case "NEG+FUT+A3SG$NEG^DB+ADJ+FUTPART+PNON":
                return "NEG+FUT+A3SG";
                /* etmeli, olmalı */
            case "POS+NECES+A3SG$POS^DB+NOUN+INF2+A3SG+PNON+NOM^DB+ADJ+WITH":
                if (isBeforeLastWord(index, fsmParses)) {
                    return "POS+NECES+A3SG";
                }
                if (isNextWordNounOrAdjective(index, fsmParses)) {
                    return "POS^DB+NOUN+INF2+A3SG+PNON+NOM^DB+ADJ+WITH";
                }
                return "POS+NECES+A3SG";
                /* DE */
            case "CONJ$NOUN+PROP+A3SG+PNON+NOM$VERB+POS+IMP+A2SG":
                if (index > 0) {
                    if (isCapital(surfaceForm)) {
                        return "NOUN+PROP+A3SG+PNON+NOM";
                    }
                    return "CONJ";
                }
                /* GEÇ, SIK */
            case "ADJ$ADV$VERB+POS+IMP+A2SG":
                if (surfaceForm.equals("sık")) {
                    String previousWord = "";
                    String nextWord = "";
                    if (index - 1 > -1) {
                        previousWord = fsmParses[index - 1].getFsmParse(0).getSurfaceForm();
                    }
                    if (index + 1 < fsmParses.length) {
                        nextWord = fsmParses[index + 1].getFsmParse(0).getSurfaceForm();
                    }
                    if (previousWord.equals("sık") || nextWord.equals("sık")) {
                        return "ADV";
                    }
                }
                if (isNextWordNoun(index, fsmParses)) {
                    return "ADJ";
                }
                return "ADV";
            /* BİRLİKTE */
            case "ADV$POSTP+PCINS":
                if (hasPreviousWordTag(index, correctParses, MorphologicalTag.INSTRUMENTAL)) {
                    return "POSTP+PCINS";
                }
                return "ADV";
                /* yavaşça, dürüstçe, fazlaca */
            case "ADJ+ASIF$ADV+LY$NOUN+ZERO+A3SG+PNON+EQU":
                return "ADV+LY";
                /* FAZLADIR, FAZLAYDI, ÇOKTU, ÇOKTUR */
            case "ADJ^DB$POSTP+PCABL^DB":
                if (hasPreviousWordTag(index, correctParses, MorphologicalTag.ABLATIVE)) {
                    return "POSTP+PCABL^DB";
                }
                return "ADJ^DB";
                /* kaybettikleri, umdukları, gösterdikleri */
            case "ADJ+PASTPART+P3PL$NOUN+PASTPART+A3PL+P3PL+NOM$NOUN+PASTPART+A3PL+P3SG+NOM$NOUN+PASTPART+A3SG+P3PL+NOM":
                if (isNextWordNounOrAdjective(index, fsmParses)) {
                    return "ADJ+PASTPART+P3PL";
                }
                if (isPossessivePlural(index, correctParses)) {
                    return "NOUN+PASTPART+A3SG+P3PL+NOM";
                }
                return "NOUN+PASTPART+A3PL+P3SG+NOM";
                /* yılın, yolun */
            case "NOUN+A3SG+P2SG+NOM$NOUN+A3SG+PNON+GEN$VERB+POS+IMP+A2PL$VERB^DB+VERB+PASS+POS+IMP+A2SG":
                if (isAnyWordSecondPerson(index, correctParses)) {
                    return "NOUN+A3SG+P2SG+NOM";
                }
                return "NOUN+A3SG+PNON+GEN";
                /* sürmekte, beklenmekte, değişmekte */
            case "POS+PROG2+A3SG$POS^DB+NOUN+INF+A3SG+PNON+LOC":
                return "POS+PROG2+A3SG";
                /* KİMSE, KİMSEDE, KİMSEYE */
            case "NOUN+A3SG+PNON$PRON+QUANTP+A3SG+P3SG":
                return "PRON+QUANTP+A3SG+P3SG";
                /* DOĞRU */
            case "ADJ$NOUN+A3SG+PNON+NOM$POSTP+PCDAT":
                if (hasPreviousWordTag(index, correctParses, MorphologicalTag.DATIVE)) {
                    return "POSTP+PCDAT";
                }
                return "ADJ";
                /* ikisini, ikisine, fazlasına */
            case "ADJ+JUSTLIKE^DB+NOUN+ZERO+A3SG+P2SG$NOUN+ZERO+A3SG+P3SG":
                return "NOUN+ZERO+A3SG+P3SG";
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
