/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoGraph.Decoder;
import evoGraph.Evaluation;
import evoGraph.GeneticAlgorithm;
import evoGraph.GraphImageBuilder;
import evoGraph.GraphIndividual;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.swingViewer.ViewPanel;

/**
 *
 * @author andre
 */
public class GeneticAlgorithmTest {
    
    public static void main(String[] agrs){
        GeneticAlgorithm ga = new GeneticAlgorithm();
        ga.run();
        
        System.out.println("Decoding Individual as Graph");
        Evaluation eva = new Evaluation();
        eva.fitness(ga.getBestIndividual(), true);
        Decoder barabasiDecoder = new Decoder(ga.getBestIndividual());
        GraphIndividual barabasiIndividual = barabasiDecoder.barabasiAlbertGraph();
        Decoder graphDecoder = new Decoder(barabasiIndividual);
        DefaultGraph best = graphDecoder.decode();
        ViewPanel view = best.display(false).getDefaultView();
        
        // export json data
        ga.exportGraph(best);
        
    }
}
