/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoPuzzle;

import evoGraph.Config;
import evoGraph.Decoder;
import evoGraph.EvoJSONFileReader;
import evoGraph.GraphIndividual;
import graphstream.GraphStreamUtil;
import org.graphstream.graph.Graph;

/**
 *
 * @author andre
 */
public class PuzzleMain {
    
    public static void setup(){
        Config.borderSize = 5;
        Config.edgeSize = 6;

        Config.minNodeCount = 25;
        Config.maxNodeCount = 100;

        Config.minEdgeDistance = 5;
    
        Config.areaIntersectionPenalty =  100;
        Config.edgeIntersectionPenalty = 1000;
        Config.nodeCountPenalty        = 1000;
        Config.edgeDistancePentalty    = 1000;

        Config.minNodeSize = 15.0;
        Config.maxNodeSize = 30.0;
        Config.scaleFactor = 50;
        Config.expansionProb = 0.05; // used only on graphX crossover
        
        Config.barabasiFactor = 5; // delta
        Config.maxLinksPerStep = 2; // m

        Config.crossoverProb  =  0.90;
        Config.mutationProb   =  0.10;
        Config.refinementProb =  0.01;

        Config.nodeXLeap = 1.1;
        Config.nodeYLeap = 1.1;
        Config.nodeZLeap = 0;

        Config.popSize = 100;
        Config.maxGen  = 100;
        
        Config.desiredAngles = new int[3];
        Config.desiredAngles[0] = 0;
        Config.desiredAngles[1] = 90;
        Config.desiredAngles[2] = 180;
        
        Config.idealNonLinearity = 3;
    
        Config.useDesiredAngles = true;
        Config.useAverageShortestPath = true;
        Config.useMaximizeNodeCount = true;
        Config.useIdealNonLinearity = true;
        
        Config.useRefinement = false;
        Config.numberOfProcess = 16;
        
        Config.folder = "D:\\Mega\\posdoc\\MapGenerator\\experiments\\stantard\\";
    }
    
    public static void puzzleConfigSetup(){
        PuzzleConfig.popSize = 100;
        PuzzleConfig.maxGen  = 50;
        PuzzleConfig.crossoverProb  =  0.9;
        PuzzleConfig.mutationProb   =  0.1;
    }
    
    public static void main(String args[]) throws InterruptedException{
        setup();
        puzzleConfigSetup();
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        
        String filename = "D:\\Mega\\posdoc\\MapGenerator\\experiments\\only_asp_and_din\\1618875503\\map_1618875503.json";
        System.out.println(filename);
        EvoJSONFileReader reader = new EvoJSONFileReader(filename);
        Graph graph = reader.parseJson();
        
        PuzzleGA pga = new PuzzleGA(graph);
        Thread t = new Thread(pga);
        t.start();
        t.join();
        
        //System.out.println("Encoding Graph as Individual");
        Decoder originalEncoder = new Decoder(graph);
        GraphIndividual originalIndividual = originalEncoder.encode();
        
        //System.out.println("Connecting Nodes");
        Decoder connectDecoder = new Decoder(originalIndividual);
        GraphIndividual original = connectDecoder.barabasiAlbertGraph();
        
        PuzzleIndividual puzzle = pga.getBestIndividual();
        
        PuzzleEvaluation peva = new PuzzleEvaluation();
        double[] fitness = peva.fitness(original, puzzle, false);
        System.out.println(puzzle+"\nFitness: "+String.format("%.6f", fitness[0])+" / "
                + "DIN: "+fitness[1]+" "
                + "TS: "+String.format("%.6f", fitness[2])+"("+String.format("%.2f", 1.0/fitness[2])+") "
                + "VR: "+String.format("%.6f", fitness[3])+"("+String.format("%.2f", 1.0/fitness[3])+") "
                + "P: "+fitness[4]);
        
        PuzzleDecoder decoder = new PuzzleDecoder(original);
        graph = decoder.decode(puzzle, false);
        
        GraphStreamUtil gUtil = new GraphStreamUtil();
        graph.addAttribute("ui.stylesheet", "url('media/stylesheet.css')");
        gUtil.normalizeNodesSizes(graph, Config.minNodeSize, Config.maxNodeSize);
        gUtil.setupStyle(graph); 
        graph.display(false);
    }
}
