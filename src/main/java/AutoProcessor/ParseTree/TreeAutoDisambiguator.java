package AutoProcessor.ParseTree;

import AnnotatedTree.ParseTreeDrawable;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalDisambiguation.AutoDisambiguator;

public abstract class TreeAutoDisambiguator extends AutoDisambiguator{
    protected abstract void autoFillSingleAnalysis(ParseTreeDrawable parseTree);
    protected abstract void autoDisambiguateWithRules(ParseTreeDrawable parseTree);
    protected abstract void autoDisambiguateMultipleRootWords(ParseTreeDrawable parseTree);

    protected TreeAutoDisambiguator(FsmMorphologicalAnalyzer morphologicalAnalyzer){
        this.morphologicalAnalyzer = morphologicalAnalyzer;
    }

    public void autoDisambiguate(ParseTreeDrawable parseTree){
        autoFillSingleAnalysis(parseTree);
        autoDisambiguateWithRules(parseTree);
        autoDisambiguateMultipleRootWords(parseTree);
        parseTree.save();
    }

}
