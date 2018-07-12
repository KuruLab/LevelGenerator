/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoLevel.LevelDecoder;
import evoLevel.LevelIndividual;
import evoLevel.RandomLevelGenerator;
import java.util.Random;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 *
 * @author andre
 */
public class IndividualHashTest {
    
    public static void main(String args[]){
        System.out.println("Creating Graph");
        RandomLevelGenerator rgg = new RandomLevelGenerator();
        Graph original = rgg.barabasiAlbert("father");
        //father.display(false);
        
        System.out.println("Encoding Graph as Individual");
        LevelDecoder encoder = new LevelDecoder(original);   
        LevelIndividual individual = encoder.encode();
        
        System.out.println("Connecting Nodes");
        LevelDecoder decoder = new LevelDecoder(individual);
        LevelIndividual connectedIndividual = decoder.barabasiAlbertGraph();
        System.out.println(connectedIndividual.toHashString());
        Random random = new Random(individual.toHashString().hashCode());
        for(int i = 0; i < 10; i++)
            System.out.println(String.format("%.4f", random.nextDouble()));
        
        LevelIndividual clone = individual.clone();
        decoder = new LevelDecoder(clone);
        LevelIndividual connectedClone = decoder.barabasiAlbertGraph();
        System.out.println(connectedClone.toHashString());
        random = new Random(clone.toHashString().hashCode());
        for(int i = 0; i < 10; i++)
            System.out.println(String.format("%.4f", random.nextDouble()));
        
        System.out.println("Decoding Individual as Graph");
        decoder = new LevelDecoder(connectedIndividual);
        DefaultGraph graph1 = decoder.decode();
        graph1.display(false);
        
        decoder = new LevelDecoder(connectedClone);
        DefaultGraph graph2 = decoder.decode();
        graph2.display(false);
    }
    
}
