package Annotation.Sentence;

import Dictionary.TxtWord;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.Transition;
import WordNet.SynSet;
import WordNet.WordNet;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class FsmParseListCellRenderer extends DefaultListCellRenderer {
    private final WordNet wordNet;

    public FsmParseListCellRenderer(WordNet wordNet){
        this.wordNet = wordNet;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component cell = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof FsmParse){
            ArrayList<SynSet> synSets;
            FsmParse currentParse = (FsmParse) value;
            TxtWord word = (TxtWord) currentParse.getWord();
            if (currentParse.getRootPos().equalsIgnoreCase("VERB")){
                Transition verbTransition = new Transition("mAk");
                String verbForm = verbTransition.makeTransition(word, word.getName());
                synSets = wordNet.getSynSetsWithLiteral(verbForm);
            } else {
                synSets = wordNet.getSynSetsWithLiteral(word.getName());
            }
            StringBuilder definitions = new StringBuilder("<html>");
            for (SynSet synSet : synSets){
                if (synSet.getPos() != null && synSet.getPos().toString().startsWith(currentParse.getRootPos())) {
                    definitions.append(synSet.getDefinition()).append("<br>");
                }
            }
            definitions.append("</html>");
            ((JComponent) cell).setToolTipText(definitions.toString());
        }
        return this;
    }
}
