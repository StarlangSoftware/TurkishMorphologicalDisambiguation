package MorphologicalDisambiguation;

import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import org.junit.Test;

import static org.junit.Assert.*;

public class RootWordStatisticsTest {

    @Test
    public void testRootWordStatistics() {
        FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();
        RootWordStatistics rootWordStatistics = new RootWordStatistics("penntreebank_statistics.txt");
        assertTrue(rootWordStatistics.containsKey("yasasını"));
        assertTrue(rootWordStatistics.containsKey("yapılandırıyorlar"));
        assertTrue(rootWordStatistics.containsKey("çöküşten"));
        assertEquals("yasa", rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("yasasını"), 0.0));
        assertEquals("karşılaş", rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("karşılaşabilir"), 0.0));
        assertNull(rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("karşılaşabilir"), 0.7));
        assertEquals("anlat", rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("anlattı"), 0.0));
        assertNull(rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("anlattı"), 0.9));
        assertEquals("ver", rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("vermesini"), 0.0));
        assertNull(rootWordStatistics.bestRootWord(fsm.morphologicalAnalysis("vermesini"), 0.9));
    }

}