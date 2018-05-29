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
import graphstream.MyToolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 *
 * @author andre
 */
public class CrossoverTest {
    
    public static void main(String args[]){
        RandomGraphGenerator rgg = new RandomGraphGenerator();
        Graph father = rgg.barabasiAlbert("father");
        father.display(false);
        Graph mother = rgg.barabasiAlbert("mother");
        mother.display(false);
        
        GeneticOperators gop = new GeneticOperators();
        Decoder fatherEncoder = new Decoder(father);
        Decoder motherEncoder = new Decoder(mother);
        GraphIndividual fatherIndividual = fatherEncoder.encode();
        GraphIndividual motherIndividual = motherEncoder.encode();
        
        GraphIndividual[] son = gop.crossover(fatherIndividual, motherIndividual);
        
        Decoder decoder0 = new Decoder(son[0]);
        Decoder decoder1 = new Decoder(son[1]);
        
        GraphIndividual[] barabasiSon = new GraphIndividual[2];
        barabasiSon[0] = decoder0.barabasiAlbertGraph();
        barabasiSon[1] = decoder1.barabasiAlbertGraph();
        
        Decoder decoder2 = new Decoder(barabasiSon[0]);
        Decoder decoder3 = new Decoder(barabasiSon[1]);
        
        DefaultGraph son0 = decoder2.decode();
        son0.display(false);
        
        DefaultGraph son1 = decoder3.decode();
        son1.display(false);
        
        /*Individual[] son = gop.graphX(fatherIndividual, motherIndividual);
        Decoder decoder0 = new Decoder(son[0]);
        Decoder decoder1 = new Decoder(son[1]);
        
        DefaultGraph son0 = decoder0.decode();
        son0.display(false);
        DefaultGraph son1 = decoder1.decode();
        son1.display(false);*/
    }
    
}
