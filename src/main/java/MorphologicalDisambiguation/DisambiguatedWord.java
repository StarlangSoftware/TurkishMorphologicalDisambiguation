package MorphologicalDisambiguation;

import Dictionary.Word;
import MorphologicalAnalysis.MorphologicalParse;

public class DisambiguatedWord extends Word {
    private MorphologicalParse parse;

    public DisambiguatedWord(String name, MorphologicalParse parse){
        super(name);
        this.parse = parse;
    }

    public MorphologicalParse getParse() {
        return parse;
    }

}
