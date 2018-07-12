/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoLevel.LevelDecoder;
import evoLevel.LevelEvaluation;
import evoLevel.LevelGA;
import image.LevelImageBuilder;
import evoLevel.LevelIndividual;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.swingViewer.ViewPanel;

/**
 *
 * @author andre
 */
public class GeneticAlgorithmTest {
    
    public static void main(String[] agrs){
        LevelGA ga = new LevelGA();
        ga.run();
        
        System.out.println("Decoding Individual as Graph");
        LevelEvaluation eva = new LevelEvaluation();
        eva.fitness(ga.getBestIndividual(), true);
        LevelDecoder barabasiDecoder = new LevelDecoder(ga.getBestIndividual());
        LevelIndividual barabasiIndividual = barabasiDecoder.barabasiAlbertGraph();
        LevelDecoder graphDecoder = new LevelDecoder(barabasiIndividual);
        DefaultGraph best = graphDecoder.decode();
        ViewPanel view = best.display(false).getDefaultView();
        
        // export json data
        ga.exportGraph(best);
        
    }
}
