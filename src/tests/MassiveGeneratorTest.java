/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoGraph.Decoder;
import evoGraph.Evaluation;
import evoGraph.GeneticOperators;
import evoGraph.GraphIndividual;
import evoGraph.RandomGraphGenerator;
import java.text.DecimalFormat;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 *
 * @author andre
 */
public class MassiveGeneratorTest {
    
    public static void main(String args[]){
        long generatorTime = 0;
        long encodeTime = 0;
        long crossOverTime = 0;
        long connectorTime = 0;
        long evaluationTime = 0;
        long decodeTime = 0;
        
        int size = 1000;
        
        for(int i = 0; i < size; i++){
            if(i % (size/10.0) == 0)
                System.out.println(i);
            long initialTime = System.nanoTime();
            RandomGraphGenerator rgg = new RandomGraphGenerator();
            Graph father = rgg.barabasiAlbert("father");
            Graph mother = rgg.barabasiAlbert("mother");
            long finalTime = System.nanoTime();
            generatorTime += finalTime - initialTime;

            initialTime = System.nanoTime();
            Decoder fatherEncoder = new Decoder(father);
            Decoder motherEncoder = new Decoder(mother);
            GraphIndividual fatherIndividual = fatherEncoder.encode();
            GraphIndividual motherIndividual = motherEncoder.encode();
            finalTime = System.nanoTime();
            encodeTime += finalTime - initialTime; 
            
            initialTime = System.nanoTime();
            GraphIndividual[] barabasiSon = new GraphIndividual[2];
            //Decoder decoder0 = new Decoder(son[0]);
            //Decoder decoder1 = new Decoder(son[1]);
            Decoder decoder0 = new Decoder(fatherIndividual);
            Decoder decoder1 = new Decoder(motherIndividual);
            barabasiSon[0] = decoder0.barabasiAlbertGraph();
            barabasiSon[1] = decoder1.barabasiAlbertGraph();
            finalTime = System.nanoTime();
            connectorTime += finalTime - initialTime;
            
            initialTime = System.nanoTime();
            Evaluation eva = new Evaluation();
            double fitness0 = eva.fitness(barabasiSon[0], false);
            double fitness1 = eva.fitness(barabasiSon[1], false);
            finalTime = System.nanoTime();
            evaluationTime += finalTime - initialTime;
            
            initialTime = System.nanoTime();
            Decoder decoder2 = new Decoder(barabasiSon[0]);
            Decoder decoder3 = new Decoder(barabasiSon[1]);
            DefaultGraph son0 = decoder2.decode();
            DefaultGraph son1 = decoder3.decode();
            finalTime = System.nanoTime();
            decodeTime += finalTime - initialTime;
            
            initialTime = System.nanoTime();
            GeneticOperators gop = new GeneticOperators();
            GraphIndividual[] son = gop.crossover(fatherIndividual, motherIndividual);
            son[0] = gop.mutation(son[0]);
            son[1] = gop.mutation(son[1]);
            son[0] = gop.fixInvalidIndividual(son[0]);
            son[1] = gop.fixInvalidIndividual(son[1]);
            finalTime = System.nanoTime();
            crossOverTime += finalTime - initialTime;
        }
        generatorTime = generatorTime / (size*2);
        encodeTime = encodeTime / (size*2);
        crossOverTime = crossOverTime / (size);
        connectorTime = connectorTime / (size*2);
        evaluationTime = evaluationTime / (size*2);
        decodeTime = decodeTime / (size*2);
        
        System.out.println("Generator Time: \t"+generatorTime/1000000.0+"ms");
        System.out.println("Encode Time:    \t"+encodeTime/1000000.0+"ms");
        System.out.println("Breeding Time:  \t"+crossOverTime/1000000.0+"ms");
        System.out.println("Connector Time: \t"+connectorTime/1000000.0+"ms");
        System.out.println("Decode Time:    \t"+decodeTime/1000000.0+"ms");
        System.out.println("Decode Time:    \t"+evaluationTime/1000000.0+"ms");
        
        System.out.println("$10$ & "
            + "$"+String.format("%.4f",generatorTime/1000000.0)+"$ & "
            + "$"+String.format("%.4f",encodeTime/1000000.0)+"$ & "
            + "$"+String.format("%.4f",crossOverTime/1000000.0)+"$ & "
            + "$"+String.format("%.4f",connectorTime/1000000.0)+"$ & "
            + "$"+String.format("%.4f",decodeTime/1000000.0)+"$ & "
            + "$"+String.format("%.4f",evaluationTime/1000000.0)+"$ \\\\");
    }
    
}
