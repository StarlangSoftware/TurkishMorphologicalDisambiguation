package MorphologicalDisambiguation;

import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import org.junit.Test;

import static org.junit.Assert.*;

public class RootWordStatisticsTest {

    @Test
    public void testRootWordStatistics() {
        FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();
        RootWordStatistics rootWordStatistics = new RootWordStatistics("penntreebank_statistics.txt");
        assertTrue(rootWordStatistics.containsKey("it$iti$itici"));
        assertTrue(rootWordStatistics.containsKey("yas$yasa$yasama"));
        assertTrue(rootWordStatistics.containsKey("tutuk$tutukla"));
        assertEquals("çık", rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("çıkma"), 0.0));
        assertEquals("danışman", rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("danışman"), 0.0));
        assertNull(rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("danışman"), 0.7));
        assertEquals("görüşme", rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("görüşme"), 0.0));
        assertNull(rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("görüşme"), 0.7));
        assertEquals("anlaş", rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("anlaşma"), 0.0));
        assertNull(rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("anlaşma"), 0.7));
    }

}