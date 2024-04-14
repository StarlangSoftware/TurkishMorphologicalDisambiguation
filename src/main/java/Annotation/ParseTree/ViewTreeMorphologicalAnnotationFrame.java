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

    /**
     * After finding the corresponding parse tree in that row, updates the morphological analysis layer
     * of that word in the leaf node associated with that row.
     * @param row Index of the row
     * @param newValue Named entity tag to be assigned
     */
    protected void updateData(int row, String newValue){
        data.get(row).set(TAG_INDEX, newValue);
        ParseTreeDrawable parseTree = treeBank.get(Integer.parseInt(data.get(row).get(COLOR_COLUMN_INDEX - 1)));
        NodeDrawableCollector nodeDrawableCollector = new NodeDrawableCollector((ParseNodeDrawable) parseTree.getRoot(), new IsTurkishLeafNode());
        ArrayList<ParseNodeDrawable> leafList = nodeDrawableCollector.collect();
        ParseNodeDrawable parseNode = leafList.get(Integer.parseInt(data.get(row).get(WORD_POS_INDEX)) - 1);
        parseNode.getLayerInfo().setLayerData(ViewLayerType.INFLECTIONAL_GROUP, newValue);
        parseTree.save();
    }

    /**
     * Constructs the data table. For every sentence, the columns are:
     * <ol>
     *     <li>Parse tree file name</li>
     *     <li>Index of the word</li>
     *     <li>First word in the leaf node</li>
     *     <li>Morphological analysis of the first word, - otherwise</li>
     *     <li>Sentence of the parse tree</li>
     *     <li>Sentence index</li>
     * </ol>
     * @param treeBank Annotated NER treebank
     */
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

    /**
     * Constructs Morphological disambiguation frame viewer. If the user double-clicks any row, the method automatically
     * creates a new panel showing associated parse tree.
     * @param treeBank Annotated parse tree
     * @param fsm Morphological analyzer
     * @param treeMorphologicalAnalyzerFrame Frame in which new panels will be created, when the user double-clicks a row.
     */
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
