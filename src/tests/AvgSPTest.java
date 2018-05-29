/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoGraph.Decoder;
import evoGraph.Evaluation;
import evoGraph.GraphIndividual;
import evoGraph.RandomGraphGenerator;
import extendedMetaZelda.DungeonGenerator;
import org.graphstream.graph.Graph;

/**
 *
 * @author andre
 */
public class AvgSPTest {
    public static void main(String[] args){
        System.out.println("Creating Graph");
        RandomGraphGenerator rgg = new RandomGraphGenerator();
        Graph original = rgg.barabasiAlbert("father");
        
        System.out.println("Encoding Graph as Individual");
        Decoder originalEncoder = new Decoder(original);
        GraphIndividual originalIndividual = originalEncoder.encode();
        
        Decoder barabasiDec = new Decoder(originalIndividual);
        GraphIndividual barabasiInd = barabasiDec.barabasiAlbertGraph();
        Decoder graphDec = new Decoder(barabasiInd);
        Graph graph = graphDec.decode();
        DungeonGenerator dgg = new DungeonGenerator(graph);
        dgg.generate(false);
        
        Evaluation evaluation = new Evaluation();
        long start = System.nanoTime();
        double apsp = evaluation.averageShortestPath(graph);
        long end = System.nanoTime();
        System.out.println("APSP Result: "+apsp+" - Time: "+(end-start)+" ns");
        
        /*evaluation = new Evaluation();
        start = System.nanoTime();
        double astarsp = evaluation.aStarAVGShortestPath(graph);
        end = System.nanoTime();
        System.out.println("A*SP Result: "+astarsp+" - Time: "+(end-start)+" ns");*/
        
        evaluation = new Evaluation();
        start = System.nanoTime();
        double eastarsp = evaluation.efficientAStarAVGShortestPath(graph);
        end = System.nanoTime();
        System.out.println("A*SP Result: "+eastarsp+" - Time: "+(end-start)+" ns");
    }
}
