package Annotation.ParseTree;

import AnnotatedTree.TreeBankDrawable;
import AutoProcessor.ParseTree.TreeAutoDisambiguator;
import AutoProcessor.ParseTree.TurkishTreeAutoDisambiguator;
import DataCollector.ParseTree.TreeEditorFrame;
import DataCollector.ParseTree.TreeEditorPanel;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class TreeMorphologicalAnalyzerFrame extends TreeEditorFrame {
    private JCheckBox autoDisambiguation;
    private FsmMorphologicalAnalyzer fsm;

    public TreeMorphologicalAnalyzerFrame(final FsmMorphologicalAnalyzer fsm){
        this.setTitle("Morphological Analyzer");
        this.fsm = fsm;
        autoDisambiguation = new JCheckBox("AutoDisambiguation", true);
        toolBar.add(autoDisambiguation);
        TreeBankDrawable treeBank = new TreeBankDrawable(new File(TreeEditorPanel.treePath));
        JMenuItem itemViewAnnotations = addMenuItem(projectMenu, "View Annotations", KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        itemViewAnnotations.addActionListener(e -> {
            new ViewTreeMorphologicalAnnotationFrame(treeBank, fsm, this);
        });
    }

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

    protected void nextTree(int count){
        super.nextTree(count);
        autoDisambiguate();
    }

    protected void previousTree(int count){
        super.previousTree(count);
        autoDisambiguate();
    }

}
