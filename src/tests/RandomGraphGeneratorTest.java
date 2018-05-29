/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoGraph.Decoder;
import evoGraph.GraphIndividual;
import evoGraph.RandomGraphGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 *
 * @author andre
 */
public class RandomGraphGeneratorTest {
    
    public static void main(String[] args){
        RandomGraphGenerator rgg = new RandomGraphGenerator();
        
        Graph g1 = rgg.barabasiAlbert("g1");
        g1.display(false);
        
        Decoder encoder = new Decoder(g1);
        GraphIndividual individual = encoder.encode();
        //System.out.println("encode time: "+(encoder.getTime()/1000000000.0)+"s");
        
        Decoder decoder = new Decoder(individual);
        DefaultGraph g2 = decoder.decode();
        //System.out.println("decode time: "+(decoder.getTime()/1000000000.0)+"s");
        
        g2.display(false);

        //DefaultGraph g2 = rgg.barabasiAlbert("g2");
        //g2.display(true);
    }
}
