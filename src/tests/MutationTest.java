/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoGraph.Config;
import evoGraph.Decoder;
import evoGraph.GeneticOperators;
import evoGraph.GraphIndividual;
import evoGraph.RandomGraphGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 *
 * @author andre
 */
public class MutationTest {
    
    public static void main(String args[]){
        Config.mutationProb = 1.0;
        
        System.out.println("Creating Graph");
        RandomGraphGenerator rgg = new RandomGraphGenerator();
        Graph father = rgg.barabasiAlbert("father");
        father.display(false);
        
        System.out.println("Encoding Graph as Individual");
        Decoder fatherEncoder = new Decoder(father);
        GraphIndividual fatherIndividual = fatherEncoder.encode();
        
        System.out.println("Performing Mutation");
        GeneticOperators gop = new GeneticOperators();
        GraphIndividual mutantIndividual = gop.mutation(fatherIndividual);
        
        System.out.println("Connecting Nodes");
        Decoder mutantDecoder = new Decoder(mutantIndividual);
        GraphIndividual connectedIndividual = mutantDecoder.barabasiAlbertGraph();
        
        System.out.println("Decoding Individual as Graph");
        Decoder finalGraphDecoder = new Decoder(connectedIndividual);
        DefaultGraph son = finalGraphDecoder.decode();
        son.display(false);
    }
}
