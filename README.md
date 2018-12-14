# MorphologicalDisambiguation
For Developers
============

## Requirements

* [Java Development Kit 8 or higher](#java), Open JDK or Oracle JDK
* [Maven](#maven)
* [Git](#git)

### Java 

To check if you have a compatible version of Java installed, use the following command:

    java -version
    
If you don't have a compatible version, you can download either [Oracle JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or [OpenJDK](https://openjdk.java.net/install/)    

### Maven
To check if you have Maven installed, use the following command:

    mvn --version
    
To install Maven, you can follow the instructions [here](https://maven.apache.org/install.html).     

### Git

Install the [latest version of Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git).

## Download Code

In order to work on code, create a fork from GitHub page. 
Use Git for cloning the code to your local or below line for Ubuntu:

	git clone <your-fork-git-link>

A directory called MorphologicalDisambiguation will be created. Or you can use below link for exploring the code:

	git clone https://github.com/olcaytaner/MorphologicalDisambiguation.git

## Open project with IntelliJ IDEA

Steps for opening the cloned project:

* Start IDE
* Select **File | Open** from main menu
* Choose `MorphologicalDisambiguation/pom.xml` file
* Select open as project option
* Couple of seconds, dependencies with Maven will be downloaded. 

<!--- See the snapshot of the project at the beginning:

// put the link of ss

![Main IDE page](https://github.com/master/dev/site/images/zemberek-ide-main.png))
--->
## Compile

**From IDE**

After being done with the downloading and Maven indexing, select **Build Project** option from **Build** menu. After compilation process, user can run `MorphologicalDisambiguation`.

**From Console**

Use below line to generate jar file:

     mvn install


------------------------------------------------

Morphological Disambiguation
============
+ [Maven Usage](#maven-usage)
+ [Creating MorphologicalDisambiguator](#creating-morphologicaldisambiguator)
+ [Training MorphologicalDisambiguator](#training-morphologicaldisambiguator)
+ [Sentence Disambiguation](#sentence-disambiguation)


### Maven Usage

    <groupId>NlpToolkit</groupId>
    <artifactId>MorphologicalDisambiguation</artifactId>
    <version>1.0.1</version>
    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>
    <repositories>
        <repository>
            <id>NlpToolkit</id>
            <url>http://haydut.isikun.edu.tr:8081/artifactory/NlpToolkit</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>NlpToolkit</groupId>
            <artifactId>Classification</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>NlpToolkit</groupId>
            <artifactId>Corpus</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>NlpToolkit</groupId>
            <artifactId>Dictionary</artifactId>
            <version>1.0.2</version>
        </dependency>
        <dependency>
            <groupId>NlpToolkit</groupId>
            <artifactId>MorphologicalAnalysis</artifactId>
            <version>1.0.2</version>
        </dependency>
        <dependency>
            <groupId>NlpToolkit</groupId>
            <artifactId>NGram</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>NlpToolkit</groupId>
            <artifactId>DataStructure</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>

## Creating MorphologicalDisambiguator 

MorphologicalDisambiguator provides Turkish morphological disambiguation. There are possible disambiguation techniques. Depending on the technique used, disambiguator can be instantiated as follows:

* Using `RootFirstDisambiguation`,

        MorphologicalDisambiguator morphologicalDisambiguator = new RootFirstDisambiguation();
    
* Using `HmmDisambiguation`,
        
        MorphologicalDisambiguator morphologicalDisambiguator = new HmmDisambiguation();

* Using `DummyDisambiguation`,
     
        MorphologicalDisambiguator morphologicalDisambiguator = new DummyDisambiguation();
    

## Training MorphologicalDisambiguator

To train the disambiguator, an instance of `DisambiguationCorpus` object is needed. This can be instantiated and the disambiguator can be trained and saved as follows:

    DisambiguationCorpus corpus = new DisambiguationCorpus("penn_treebank.txt");
    morphologicalDisambiguator.train(corpus);
    morphologicalDisambiguator.saveModel();
    
      
## Sentence Disambiguation

To disambiguate a sentence, a `FsmMorphologicalAnalyzer` instance is required. This can be created as below, further information can be found [here](https://github.com/olcaytaner/MorphologicalAnalysis/blob/master/README.md#creating-fsmmorphologicalanalyzer).

    FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();
    
A sentence can be disambiguated as follows: 
    
    Sentence sentence = new Sentence("Yarın doktora gidecekler");
    FsmParseList[] fsmParseList = fsm.robustMorphologicalAnalysis(sentence);
    System.out.println("All parses");
    System.out.println("--------------------------");
    for(int i = 0; i < fsmParseList.length; i++){
        System.out.println(fsmParseList[i]);
    }
    ArrayList<FsmParse> candidateParses = morphologicalDisambiguator.disambiguate(fsmParseList);
    System.out.println("Parses after disambiguation");
    System.out.println("--------------------------");
    for(int i = 0; i < candidateParses.size(); i++){
        System.out.println(candidateParses.get(i));
    }

Output

    
    All parses
    --------------------------
    yar+NOUN+A3SG+P2SG+NOM
    yar+NOUN+A3SG+PNON+GEN
    yar+VERB+POS+IMP+A2PL
    yarı+NOUN+A3SG+P2SG+NOM
    yarın+NOUN+A3SG+PNON+NOM
    
    doktor+NOUN+A3SG+PNON+DAT
    doktora+NOUN+A3SG+PNON+NOM
    
    git+VERB+POS+FUT+A3PL
    git+VERB+POS^DB+NOUN+FUTPART+A3PL+PNON+NOM
    
    Parses after disambiguation
    --------------------------
    yarın+NOUN+A3SG+PNON+NOM
    doktor+NOUN+A3SG+PNON+DAT
    git+VERB+POS+FUT+A3PL
