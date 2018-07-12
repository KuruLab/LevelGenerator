/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoLevel.LevelConfig;
import evoLevel.LevelDecoder;
import evoLevel.LevelOperators;
import evoLevel.LevelIndividual;
import evoLevel.RandomLevelGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 *
 * @author andre
 */
public class MutationTest {
    
    public static void main(String args[]){
        LevelConfig.mutationProb = 1.0;
        
        System.out.println("Creating Graph");
        RandomLevelGenerator rgg = new RandomLevelGenerator();
        Graph father = rgg.barabasiAlbert("father");
        father.display(false);
        
        System.out.println("Encoding Graph as Individual");
        LevelDecoder fatherEncoder = new LevelDecoder(father);
        LevelIndividual fatherIndividual = fatherEncoder.encode();
        
        System.out.println("Performing Mutation");
        LevelOperators gop = new LevelOperators();
        LevelIndividual mutantIndividual = gop.mutation(fatherIndividual);
        
        System.out.println("Connecting Nodes");
        LevelDecoder mutantDecoder = new LevelDecoder(mutantIndividual);
        LevelIndividual connectedIndividual = mutantDecoder.barabasiAlbertGraph();
        
        System.out.println("Decoding Individual as Graph");
        LevelDecoder finalGraphDecoder = new LevelDecoder(connectedIndividual);
        DefaultGraph son = finalGraphDecoder.decode();
        son.display(false);
    }
}
