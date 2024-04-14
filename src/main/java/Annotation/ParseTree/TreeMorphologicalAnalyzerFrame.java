package Annotation.ParseTree;

import AnnotatedTree.TreeBankDrawable;
import AutoProcessor.ParseTree.TreeAutoDisambiguator;
import AutoProcessor.ParseTree.TurkishTreeAutoDisambiguator;
import DataCollector.ParseTree.TreeEditorFrame;
import DataCollector.ParseTree.TreeEditorPanel;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class TreeMorphologicalAnalyzerFrame extends TreeEditorFrame {
    private final JCheckBox autoDisambiguation;
    private final FsmMorphologicalAnalyzer fsm;

    /**
     * Constructor of the Disambiguation frame for parse trees. It reads the annotated tree bank and adds automatic
     * disambiguation button.
     */
    public TreeMorphologicalAnalyzerFrame(final FsmMorphologicalAnalyzer fsm){
        this.setTitle("Morphological Analyzer");
        this.fsm = fsm;
        autoDisambiguation = new JCheckBox("AutoDisambiguation", true);
        toolBar.add(autoDisambiguation);
        TreeBankDrawable treeBank = new TreeBankDrawable(new File(TreeEditorPanel.treePath));
        JMenuItem itemViewAnnotations = addMenuItem(projectMenu, "View Annotations", KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        itemViewAnnotations.addActionListener(e -> new ViewTreeMorphologicalAnnotationFrame(treeBank, fsm, this));
    }

    /**
     * The method automatically disambiguate words in the parse tree using TurkishTreeAutoDisambiguator.
     */
    private void autoDisambiguate(){
        TreeAutoDisambiguator treeAutoDisambiguator;
        if (autoDisambiguation.isSelected()){
            TreeEditorPanel current = (TreeEditorPanel) ((JScrollPane) projectPane.getSelectedComponent()).getViewport().getView();
            treeAutoDisambiguator = new TurkishTreeAutoDisambiguator();
            treeAutoDisambiguator.autoDisambiguate(current.currentTree);
            current.currentTree.reload();
            current.repaint();
        }
    }

    @Override
    protected TreeEditorPanel generatePanel(String currentPath, String rawFileName) {
        return new TreeMorphologicalAnalyzerPanel(currentPath, rawFileName, fsm, !autoDisambiguation.isSelected());
    }

    /**
     * The function displays the next tree according to count and the index of the parse tree. For example, if the
     * current tree  fileName is 0123.train, after the call of nextTree(3), ViewerPanel will display 0126.train. If the
     * next tree  does not exist, nothing will happen. If the autoDisambiguation is selected, it automatically
     * morphologically disambiguate words.
     * @param count Number of trees to go forward
     */
    protected void nextTree(int count){
        super.nextTree(count);
        autoDisambiguate();
    }

    /**
     * Overloaded function that displays the previous tree according to count and the index of the parse tree. For
     * example, if the current tree fileName is 0123.train, after the call of previousTree(4), ViewerPanel will
     * display 0119.train. If the previous tree does not exist, nothing will happen. If the autoDisambiguation is
     * selected, it automatically morphologically disambiguate words.
     * @param count Number of trees to go backward
     */
    protected void previousTree(int count){
        super.previousTree(count);
        autoDisambiguate();
    }

}
