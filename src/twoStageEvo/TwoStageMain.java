/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twoStageEvo;

import evoGraph.Config;
import evoGraph.GraphIndividual;
import evoPuzzle.PuzzleConfig;
import evoPuzzle.PuzzleDecoder;
import evoPuzzle.PuzzleIndividual;
import graphstream.GraphStreamUtil;
import org.graphstream.graph.Graph;

/**
 *
 * @author andre
 */
public class TwoStageMain {
    public static void graphConfigSetup(){
        Config.borderSize = 15;
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
        Config.refinementProb =  0.005;

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
        Config.useRefinement = true;
        
        Config.numberOfProcess = 16;
        
        Config.folder = "D:\\Mega\\posdoc\\MapGenerator\\experiments\\two_stage_test\\";
    }
    
    public static void puzzleConfigSetup(){
        PuzzleConfig.popSize = 100;
        PuzzleConfig.maxGen  = 50;
        PuzzleConfig.crossoverProb  =  0.9;
        PuzzleConfig.mutationProb   =  0.1;
    }
    
    public static void main(String[] agrs){
        System.setProperty("org.graphstream.ui.renderer",
                           "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        graphConfigSetup();
        puzzleConfigSetup();
        
        TwoStageGA tsGA = new TwoStageGA();
        tsGA.run();
        
        GraphIndividual individual = tsGA.getBestIndividual();
        PuzzleIndividual puzzle = tsGA.getBestPuzzleIndividual();
        
        PuzzleDecoder decoder = new PuzzleDecoder(individual);
        Graph graph = decoder.decode(puzzle, false);
        
        GraphStreamUtil gUtil = new GraphStreamUtil();
        graph.addAttribute("ui.stylesheet", "url('media/stylesheet.css')");
        gUtil.normalizeNodesSizes(graph, Config.minNodeSize, Config.maxNodeSize);
        gUtil.setupStyle(graph); 
        graph.display(false);
        
        tsGA.exportGraph(graph);
    }
}
