package Annotation.ParseTree;

import AnnotatedSentence.LayerNotExistsException;
import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.LayerInfo;
import AnnotatedTree.LayerItemNotExistsException;
import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.WordNotExistsException;
import DataCollector.ParseTree.TreeAction.LayerClearAction;
import DataCollector.ParseTree.TreeAction.MorphologicalAnalysisAction;
import DataCollector.ParseTree.TreeLeafEditorPanel;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;

public class TreeMorphologicalAnalyzerPanel extends TreeLeafEditorPanel {

    private JTree tree;
    private DefaultTreeModel treeModel;
    private FsmMorphologicalAnalyzer fsm;
    private FsmParseList[] fsmParses;

    public TreeMorphologicalAnalyzerPanel(String path, String fileName, FsmMorphologicalAnalyzer fsm, boolean defaultFillEnabled) {
        super(path, fileName, ViewLayerType.PART_OF_SPEECH, defaultFillEnabled);
        this.fsm = fsm;
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Analizler");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setVisible(false);
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getLevel() == fsmParses.length){
                ArrayList<FsmParse> selectedFsmParses = getSelectedParses(node);
                String word1 = selectedFsmParses.get(0).transitionList();
                String word2 = selectedFsmParses.get(0).withList();
                for (int i = 1; i < selectedFsmParses.size(); i++) {
                    word1 = word1 + " " + selectedFsmParses.get(i).transitionList();
                    word2 = word2 + " " + selectedFsmParses.get(i).withList();
                }
                MorphologicalAnalysisAction action = new MorphologicalAnalysisAction(((TreeMorphologicalAnalyzerPanel) tree.getParent().getParent().getParent()), previousNode.getLayerInfo(), word1, word2);
                setAction(action);
                tree.setVisible(false);
            } else {
                if (node != null && node.getLevel() == 0){
                    LayerClearAction action = new LayerClearAction(((TreeMorphologicalAnalyzerPanel) tree.getParent().getParent().getParent()), previousNode.getLayerInfo(), ViewLayerType.INFLECTIONAL_GROUP);
                    setAction(action);
                    tree.setVisible(false);
                }
            }
        });
        pane = new JScrollPane(tree);
        add(pane);
        pane.setFocusTraversalKeysEnabled(false);
        tree.setCellRenderer(new FsmParseTreeCellRenderer());
        ToolTipManager.sharedInstance().registerComponent(tree);
        setFocusable(false);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    private ArrayList<FsmParse> getSelectedParses(DefaultMutableTreeNode node){
        ArrayList<FsmParse> selectedFsmParses = new ArrayList<FsmParse>();
        switch (fsmParses.length){
            case 1:
                if (node.getLevel() == 1){
                    selectedFsmParses.add(fsmParses[0].getFsmParse(node.getParent().getIndex(node)));
                }
                break;
            case 2:
                if (node.getLevel() == 2){
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
                    selectedFsmParses.add(fsmParses[0].getFsmParse(parentNode.getParent().getIndex(parentNode)));
                    selectedFsmParses.add(fsmParses[1].getFsmParse(node.getParent().getIndex(node)));
                }
                break;
            case 3:
                if (node.getLevel() == 3){
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
                    DefaultMutableTreeNode grandParentNode = (DefaultMutableTreeNode) parentNode.getParent();
                    selectedFsmParses.add(fsmParses[0].getFsmParse(grandParentNode.getParent().getIndex(grandParentNode)));
                    selectedFsmParses.add(fsmParses[1].getFsmParse(parentNode.getParent().getIndex(parentNode)));
                    selectedFsmParses.add(fsmParses[2].getFsmParse(node.getParent().getIndex(node)));
                }
                break;
        }
        return selectedFsmParses;
    }

    public void populateLeaf(ParseNodeDrawable node){
        DefaultMutableTreeNode selectedNode = null;
        if (previousNode != null){
            previousNode.setSelected(false);
        }
        previousNode = node;
        ((DefaultMutableTreeNode)treeModel.getRoot()).removeAllChildren();
        treeModel.reload();
        LayerInfo info = node.getLayerInfo();
        if (info.getLayerData(ViewLayerType.TURKISH_WORD) != null){
            try {
                fsmParses = new FsmParseList[info.getNumberOfWords()];
                for (int i = 0; i < info.getNumberOfWords(); i++){
                    fsmParses[i] = fsm.robustMorphologicalAnalysis(info.getTurkishWordAt(i));
                }
                switch (info.getNumberOfWords()){
                    case 1:
                        for (int i = 0; i < fsmParses[0].size(); i++){
                            FsmParse fsmParse = fsmParses[0].getFsmParse(i);
                            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(fsmParse);
                            ((DefaultMutableTreeNode) treeModel.getRoot()).add(childNode);
                            if (node.getLayerData(ViewLayerType.INFLECTIONAL_GROUP) != null && node.getLayerData(ViewLayerType.INFLECTIONAL_GROUP).equals(fsmParse.transitionList())){
                                selectedNode = childNode;
                            }
                        }
                        break;
                    case 2:
                        for (int i = 0; i < fsmParses[0].size(); i++){
                            FsmParse fsmParse0 = fsmParses[0].getFsmParse(i);
                            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(fsmParse0);
                            ((DefaultMutableTreeNode) treeModel.getRoot()).add(childNode);
                            for (int j = 0; j < fsmParses[1].size(); j++){
                                FsmParse fsmParse1 = fsmParses[1].getFsmParse(j);
                                DefaultMutableTreeNode grandChildNode = new DefaultMutableTreeNode(fsmParse1);
                                childNode.add(grandChildNode);
                                if (node.getLayerData(ViewLayerType.INFLECTIONAL_GROUP) != null && node.getLayerData(ViewLayerType.INFLECTIONAL_GROUP).equals(fsmParse0.transitionList() + " " + fsmParse1.transitionList())){
                                    selectedNode = grandChildNode;
                                }
                            }
                        }
                        break;
                    case 3:
                        for (int i = 0; i < fsmParses[0].size(); i++){
                            FsmParse fsmParse0 = fsmParses[0].getFsmParse(i);
                            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(fsmParse0);
                            ((DefaultMutableTreeNode) treeModel.getRoot()).add(childNode);
                            for (int j = 0; j < fsmParses[1].size(); j++){
                                FsmParse fsmParse1 = fsmParses[1].getFsmParse(j);
                                DefaultMutableTreeNode grandChildNode = new DefaultMutableTreeNode(fsmParse1);
                                childNode.add(grandChildNode);
                                for (int k = 0; k < fsmParses[2].size(); k++){
                                    FsmParse fsmParse2 = fsmParses[2].getFsmParse(k);
                                    DefaultMutableTreeNode grandGrandChildNode = new DefaultMutableTreeNode(fsmParse2);
                                    grandChildNode.add(grandGrandChildNode);
                                    if (node.getLayerData(ViewLayerType.INFLECTIONAL_GROUP) != null && node.getLayerData(ViewLayerType.INFLECTIONAL_GROUP).equals(fsmParse0.transitionList() + " " + fsmParse1.transitionList() + " " + fsmParse2.transitionList())){
                                        selectedNode = grandGrandChildNode;
                                    }
                                }
                            }
                        }
                        break;
                }
            } catch (LayerNotExistsException e) {
                e.printStackTrace();
            } catch (WordNotExistsException e) {
                e.printStackTrace();
            }
        }
        treeModel.reload();
        if (selectedNode != null){
            tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(selectedNode)));
        }
        tree.setVisible(true);
        pane.setVisible(true);
        pane.getVerticalScrollBar().setValue(0);
        pane.setBounds(node.getArea().getX() - 5, node.getArea().getY() + 30, 250, 90);
        this.repaint();
        isEditing = true;
    }

    protected boolean defaultFill(ParseNodeDrawable node){
        LayerInfo info;
        if (fsm == null){
            return false;
        }
        if (node.getLayerData(ViewLayerType.INFLECTIONAL_GROUP) != null){
            return false;
        }
        if (node.getLayerData(ViewLayerType.TURKISH_WORD) != null){
            info = node.getLayerInfo();
            try {
                if (info.getNumberOfWords() == 1){
                    FsmParseList fsmParseList = fsm.morphologicalAnalysis(info.getTurkishWordAt(0));
                    if (fsmParseList.size() == 1){
                        node.getLayerInfo().setLayerData(ViewLayerType.INFLECTIONAL_GROUP, fsmParseList.getFsmParse(0).transitionList());
                        node.getLayerInfo().setLayerData(ViewLayerType.META_MORPHEME, fsmParseList.getFsmParse(0).withList());
                        return true;
                    }
                } else {
                    if (info.getNumberOfWords() == 2){
                        FsmParseList fsmParseList1 = fsm.morphologicalAnalysis(info.getTurkishWordAt(0));
                        FsmParseList fsmParseList2 = fsm.morphologicalAnalysis(info.getTurkishWordAt(1));
                        if (fsmParseList1.size() == 1 && fsmParseList2.size() == 1){
                            node.getLayerInfo().setLayerData(ViewLayerType.INFLECTIONAL_GROUP, fsmParseList1.getFsmParse(0).transitionList() + " " + fsmParseList2.getFsmParse(0).transitionList());
                            node.getLayerInfo().setLayerData(ViewLayerType.META_MORPHEME, fsmParseList1.getFsmParse(0).withList() + " " + fsmParseList2.getFsmParse(0).withList());
                            return true;
                        }
                    }
                }
            } catch (LayerNotExistsException e) {
                e.printStackTrace();
            } catch (WordNotExistsException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    protected int getStringSize(ParseNodeDrawable parseNode, Graphics g) {
        int i, stringSize = 0;
        if (parseNode.numberOfChildren() == 0) {
            if (parseNode.getLayerInfo().getLayerSize(ViewLayerType.PART_OF_SPEECH) == 0){
                return g.getFontMetrics().stringWidth(parseNode.getLayerData(ViewLayerType.TURKISH_WORD));
            }
            for (i = 0; i < parseNode.getLayerInfo().getLayerSize(ViewLayerType.PART_OF_SPEECH); i++)
                try {
                    if (g.getFontMetrics().stringWidth(parseNode.getLayerInfo().getLayerInfoAt(ViewLayerType.PART_OF_SPEECH, i)) > stringSize){
                        stringSize = g.getFontMetrics().stringWidth(parseNode.getLayerInfo().getLayerInfoAt(ViewLayerType.PART_OF_SPEECH, i));
                    }
                } catch (LayerNotExistsException | LayerItemNotExistsException | WordNotExistsException e) {
                    return g.getFontMetrics().stringWidth(parseNode.getData().getName());
                }
            return stringSize;
        } else {
            return g.getFontMetrics().stringWidth(parseNode.getData().getName());
        }
    }

    protected void drawString(ParseNodeDrawable parseNode, Graphics g, int x, int y){
        int i;
        if (parseNode.numberOfChildren() == 0){
            if (parseNode.getLayerInfo().getLayerSize(ViewLayerType.PART_OF_SPEECH) == 0){
                g.drawString(parseNode.getLayerData(ViewLayerType.TURKISH_WORD), x, y);
            }
            for (i = 0; i < parseNode.getLayerInfo().getLayerSize(ViewLayerType.PART_OF_SPEECH); i++){
                if (i > 0 && !parseNode.isGuessed()){
                    g.setColor(Color.RED);
                }
                try {
                    g.drawString(parseNode.getLayerInfo().getLayerInfoAt(ViewLayerType.PART_OF_SPEECH, i), x, y);
                    y += 20;
                } catch (LayerNotExistsException | LayerItemNotExistsException | WordNotExistsException e) {
                    g.drawString(parseNode.getData().getName(), x, y);
                }
            }
        } else {
            g.drawString(parseNode.getData().getName(), x, y);
        }
    }

    protected void setArea(ParseNodeDrawable parseNode, int x, int y, int stringSize){
        if (parseNode.numberOfChildren() == 0){
            parseNode.setArea(x - 5, y - 15, stringSize + 10, 20 * (parseNode.getLayerInfo().getLayerSize(ViewLayerType.PART_OF_SPEECH) + 1));
        } else {
            parseNode.setArea(x - 5, y - 15, stringSize + 10, 20);
        }
    }

}
