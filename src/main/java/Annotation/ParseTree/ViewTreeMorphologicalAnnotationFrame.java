package Annotation.ParseTree;

import AnnotatedSentence.LayerNotExistsException;
import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.*;
import AnnotatedTree.Processor.Condition.IsTurkishLeafNode;
import AnnotatedTree.Processor.NodeDrawableCollector;
import DataCollector.ParseTree.TreeEditorPanel;
import DataCollector.ParseTree.ViewTreeAnnotationFrame;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ViewTreeMorphologicalAnnotationFrame extends ViewTreeAnnotationFrame {

    protected void updateData(int row, String newValue){
        data.get(row).set(TAG_INDEX, newValue);
        ParseTreeDrawable parseTree = treeBank.get(Integer.parseInt(data.get(row).get(COLOR_COLUMN_INDEX - 1)));
        NodeDrawableCollector nodeDrawableCollector = new NodeDrawableCollector((ParseNodeDrawable) parseTree.getRoot(), new IsTurkishLeafNode());
        ArrayList<ParseNodeDrawable> leafList = nodeDrawableCollector.collect();
        ParseNodeDrawable parseNode = leafList.get(Integer.parseInt(data.get(row).get(WORD_POS_INDEX)) - 1);
        parseNode.getLayerInfo().setLayerData(ViewLayerType.INFLECTIONAL_GROUP, newValue);
        parseTree.save();
    }

    protected void prepareData(TreeBankDrawable treeBank) {
        data = new ArrayList<>();
        for (int i = 0; i < treeBank.size(); i++){
            ParseTreeDrawable parseTree = treeBank.get(i);
            NodeDrawableCollector nodeDrawableCollector = new NodeDrawableCollector((ParseNodeDrawable) parseTree.getRoot(), new IsTurkishLeafNode());
            ArrayList<ParseNodeDrawable> leafList = nodeDrawableCollector.collect();
            for (int j = 0; j < leafList.size(); j++){
                LayerInfo layerInfo = leafList.get(j).getLayerInfo();
                ArrayList<String> row = new ArrayList<>();
                row.add(parseTree.getFileDescription().getRawFileName());
                row.add("" + (j + 1));
                try {
                    row.add(layerInfo.getTurkishWordAt(0));
                } catch (LayerNotExistsException | WordNotExistsException e) {
                    row.add("-");
                }
                try {
                    row.add(layerInfo.getMorphologicalParseAt(0).toString());
                } catch (LayerNotExistsException | WordNotExistsException e) {
                    row.add("-");
                }
                row.add(((ParseNodeDrawable) parseTree.getRoot()).toTurkishSentence());
                row.add("" + i);
                row.add("0");
                data.add(row);
            }
        }
    }

    public ViewTreeMorphologicalAnnotationFrame(TreeBankDrawable treeBank, FsmMorphologicalAnalyzer fsm, TreeMorphologicalAnalyzerFrame treeMorphologicalAnalyzerFrame){
        super(treeBank, "Morphological Analysis");
        prepareData(treeBank);
        dataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2){
                    int row = dataTable.rowAtPoint(evt.getPoint());
                    if (row >= 0) {
                        String fileName = data.get(row).get(0);
                        treeMorphologicalAnalyzerFrame.addPanelToFrame(new TreeMorphologicalAnalyzerPanel(TreeEditorPanel.treePath, fileName, fsm, false), fileName);
                    }
                }
            }
        });
        JScrollPane tablePane = new JScrollPane(dataTable);
        add(tablePane, BorderLayout.CENTER);
    }
}
