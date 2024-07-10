package AutoProcessor.ParseTree;

import AnnotatedTree.ParseTreeDrawable;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalDisambiguation.AutoDisambiguator;

public abstract class TreeAutoDisambiguator extends AutoDisambiguator{
    protected abstract void autoFillSingleAnalysis(ParseTreeDrawable parseTree);
    protected abstract void autoDisambiguateWithRules(ParseTreeDrawable parseTree);
    protected abstract void autoDisambiguateMultipleRootWords(ParseTreeDrawable parseTree);

    /**
     * Constructor for the TreeAutoDisambiguator. Sets the morphological analyzer.
     * @param morphologicalAnalyzer Morphological analyzer used in disambiguation.
     */
    protected TreeAutoDisambiguator(FsmMorphologicalAnalyzer morphologicalAnalyzer){
        this.morphologicalAnalyzer = morphologicalAnalyzer;
    }

    /**
     * The method disambiguates the given parse tree. There are three main steps: In the first step, it eliminates
     * the words having single morphological analysis. In the third step, depending on the pos tag of the parent node
     * of a leaf node, the method tries to use different rule based disambiguators. In the third step, for the remaining
     * words, longest root word disambiguation algorithm is used.
     * @param parseTree Parse tree to disambiguate.
     */
    public void autoDisambiguate(ParseTreeDrawable parseTree){
        autoFillSingleAnalysis(parseTree);
        autoDisambiguateWithRules(parseTree);
        autoDisambiguateMultipleRootWords(parseTree);
        parseTree.save();
    }

}
