/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoLevel.LevelDecoder;
import evoLevel.LevelIndividual;
import evoLevel.RandomLevelGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 *
 * @author andre
 */
public class RandomGraphGeneratorTest {
    
    public static void main(String[] args){
        RandomLevelGenerator rgg = new RandomLevelGenerator();
        
        Graph g1 = rgg.barabasiAlbert("g1");
        g1.display(false);
        
        LevelDecoder encoder = new LevelDecoder(g1);
        LevelIndividual individual = encoder.encode();
        //System.out.println("encode time: "+(encoder.getTime()/1000000000.0)+"s");
        
        LevelDecoder decoder = new LevelDecoder(individual);
        DefaultGraph g2 = decoder.decode();
        //System.out.println("decode time: "+(decoder.getTime()/1000000000.0)+"s");
        
        g2.display(false);

        //DefaultGraph g2 = rgg.barabasiAlbert("g2");
        //g2.display(true);
    }
}
