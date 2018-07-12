/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoLevel.LevelDecoder;
import evoLevel.LevelEvaluation;
import evoLevel.LevelIndividual;
import evoLevel.RandomLevelGenerator;
import org.graphstream.graph.Graph;

/**
 *
 * @author andre
 */
public class AvgSPTest {
    public static void main(String[] args){
        System.out.println("Creating Graph");
        RandomLevelGenerator rgg = new RandomLevelGenerator();
        Graph original = rgg.barabasiAlbert("father");
        
        System.out.println("Encoding Graph as Individual");
        LevelDecoder originalEncoder = new LevelDecoder(original);
        LevelIndividual originalIndividual = originalEncoder.encode();
        
        LevelDecoder barabasiDec = new LevelDecoder(originalIndividual);
        LevelIndividual barabasiInd = barabasiDec.barabasiAlbertGraph();
        LevelDecoder graphDec = new LevelDecoder(barabasiInd);
        Graph graph = graphDec.decode();
        //DungeonGenerator dgg = new DungeonGenerator(graph);
        //dgg.generate(false);
        
        LevelEvaluation evaluation = new LevelEvaluation();
        long start = System.nanoTime();
        double apsp = evaluation.averageShortestPath(graph);
        long end = System.nanoTime();
        System.out.println("APSP Result: "+apsp+" - Time: "+(end-start)+" ns");
        
        /*evaluation = new LevelEvaluation();
        start = System.nanoTime();
        double astarsp = evaluation.aStarAVGShortestPath(graph);
        end = System.nanoTime();
        System.out.println("A*SP Result: "+astarsp+" - Time: "+(end-start)+" ns");*/
        
        evaluation = new LevelEvaluation();
        start = System.nanoTime();
        double eastarsp = evaluation.efficientAStarAVGShortestPath(graph);
        end = System.nanoTime();
        System.out.println("A*SP Result: "+eastarsp+" - Time: "+(end-start)+" ns");
    }
}
