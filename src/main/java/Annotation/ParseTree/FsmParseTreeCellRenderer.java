package Annotation.ParseTree;

import MorphologicalAnalysis.FsmParse;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class FsmParseTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
        FsmParse current;
        Component cell = super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) value;
        if (currentNode.getUserObject() instanceof FsmParse){
            current = (FsmParse) currentNode.getUserObject();
            ((JComponent) cell).setToolTipText(current.withList());
        }
        return this;
    }

}
