package AutoProcessor.ParseTree;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.ParallelTreeBankDrawable;
import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import AnnotatedTree.Processor.Condition.IsTurkishLeafNode;
import AnnotatedTree.Processor.NodeDrawableCollector;
import AnnotatedTree.TreeBankDrawable;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalDisambiguation.RootWordStatistics;

import java.io.File;
import java.util.ArrayList;

public class TestTreeAutoDisambiguator {

    public static void automaticDisambiguation(String dataFolder){
        TreeBankDrawable treeBank = new TreeBankDrawable(new File(dataFolder), ".");
        RootWordStatistics rootWordStatistics = treeBank.extractRootWordStatistics(new FsmMorphologicalAnalyzer());
        TreeAutoDisambiguator treeAutoDisambiguator = new TurkishTreeAutoDisambiguator(rootWordStatistics);
        System.out.println("Treebank read. Now disambiguating...");
        for (int i = 0; i < treeBank.size(); i++){
            ParseTreeDrawable parseTree = treeBank.get(i);
            treeAutoDisambiguator.autoDisambiguate(parseTree);
        }
    }

    public static void autoDisambiguate(String dataFolder, String correctFolder){
        int count = 0, total = 0, overall = 0;
        ParallelTreeBankDrawable treeBank = new ParallelTreeBankDrawable(new File(dataFolder), new File(correctFolder), ".");
        RootWordStatistics rootWordStatistics = treeBank.toTreeBank().extractRootWordStatistics(new FsmMorphologicalAnalyzer());
        TreeAutoDisambiguator treeAutoDisambiguator = new TurkishTreeAutoDisambiguator(rootWordStatistics);
        System.out.println("Parallel Treebank read. Now disambiguating...");
        for (int i = 0; i < treeBank.size(); i++){
            ParseTreeDrawable parseTree = treeBank.fromTree(i);
            treeAutoDisambiguator.autoDisambiguate(parseTree);
            ParseTreeDrawable correctTree = treeBank.toTree(i);
            NodeDrawableCollector nodeDrawableCollector1 = new NodeDrawableCollector((ParseNodeDrawable) parseTree.getRoot(), new IsTurkishLeafNode());
            ArrayList<ParseNodeDrawable> leafList1 = nodeDrawableCollector1.collect();
            NodeDrawableCollector nodeDrawableCollector2 = new NodeDrawableCollector((ParseNodeDrawable) correctTree.getRoot(), new IsTurkishLeafNode());
            ArrayList<ParseNodeDrawable> leafList2 = nodeDrawableCollector2.collect();
            for (int j = 0; j < Math.min(leafList1.size(), leafList2.size()); j++){
                String correctAnalysis = leafList2.get(j).getLayerData(ViewLayerType.INFLECTIONAL_GROUP);
                String autoAnalysis = leafList1.get(j).getLayerData(ViewLayerType.INFLECTIONAL_GROUP);
                if (correctAnalysis != null){
                    if (autoAnalysis != null){
                        if (autoAnalysis.equalsIgnoreCase(correctAnalysis)){
                            count++;
                        } else {
                            System.out.println(parseTree.getName() + ":" + correctAnalysis + "--->" + autoAnalysis);
                        }
                        total++;
                    }
                    overall++;
                }
            }
        }
        System.out.println("Accuracy: " + 100 * count / (total + 0.0) + " Coverage:" + 100 * total / (overall + 0.0));
    }

    public static void main(String[]args){
        /*Accuracy: 99.65697921419917 Coverage:83.21723189734189*/
        autoDisambiguate("../Turkish", "../Penn-Treebank/Turkish");
    }

}
