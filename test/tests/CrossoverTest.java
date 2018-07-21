/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

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
public class CrossoverTest {
    
    public static void main(String args[]){
        RandomLevelGenerator rgg = new RandomLevelGenerator();
        Graph father = rgg.barabasiAlbert("father");
        father.display(false);
        Graph mother = rgg.barabasiAlbert("mother");
        mother.display(false);
        
        LevelOperators gop = new LevelOperators();
        LevelDecoder fatherEncoder = new LevelDecoder(father);
        LevelDecoder motherEncoder = new LevelDecoder(mother);
        LevelIndividual fatherIndividual = fatherEncoder.encode();
        LevelIndividual motherIndividual = motherEncoder.encode();
        
        LevelIndividual[] son = gop.crossover(fatherIndividual, motherIndividual);
        
        LevelDecoder decoder0 = new LevelDecoder(son[0]);
        LevelDecoder decoder1 = new LevelDecoder(son[1]);
        
        LevelIndividual[] barabasiSon = new LevelIndividual[2];
        barabasiSon[0] = decoder0.barabasiAlbertGraph();
        barabasiSon[1] = decoder1.barabasiAlbertGraph();
        
        LevelDecoder decoder2 = new LevelDecoder(barabasiSon[0]);
        LevelDecoder decoder3 = new LevelDecoder(barabasiSon[1]);
        
        DefaultGraph son0 = decoder2.decode();
        son0.display(false);
        
        DefaultGraph son1 = decoder3.decode();
        son1.display(false);
        
        /*Individual[] son = gop.graphX(fatherIndividual, motherIndividual);
        LevelDecoder decoder0 = new LevelDecoder(son[0]);
        LevelDecoder decoder1 = new LevelDecoder(son[1]);
        
        DefaultGraph son0 = decoder0.decode();
        son0.display(false);
        DefaultGraph son1 = decoder1.decode();
        son1.display(false);*/
    }
    
}
