/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoLevel.LevelDecoder;
import evoLevel.LevelEvaluation;
import evoLevel.LevelOperators;
import evoLevel.LevelIndividual;
import evoLevel.RandomLevelGenerator;
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
            RandomLevelGenerator rgg = new RandomLevelGenerator();
            Graph father = rgg.barabasiAlbert("father");
            Graph mother = rgg.barabasiAlbert("mother");
            long finalTime = System.nanoTime();
            generatorTime += finalTime - initialTime;

            initialTime = System.nanoTime();
            LevelDecoder fatherEncoder = new LevelDecoder(father);
            LevelDecoder motherEncoder = new LevelDecoder(mother);
            LevelIndividual fatherIndividual = fatherEncoder.encode();
            LevelIndividual motherIndividual = motherEncoder.encode();
            finalTime = System.nanoTime();
            encodeTime += finalTime - initialTime; 
            
            initialTime = System.nanoTime();
            LevelIndividual[] barabasiSon = new LevelIndividual[2];
            //Decoder decoder0 = new LevelDecoder(son[0]);
            //Decoder decoder1 = new LevelDecoder(son[1]);
            LevelDecoder decoder0 = new LevelDecoder(fatherIndividual);
            LevelDecoder decoder1 = new LevelDecoder(motherIndividual);
            barabasiSon[0] = decoder0.barabasiAlbertGraph();
            barabasiSon[1] = decoder1.barabasiAlbertGraph();
            finalTime = System.nanoTime();
            connectorTime += finalTime - initialTime;
            
            initialTime = System.nanoTime();
            LevelEvaluation eva = new LevelEvaluation();
            double fitness0 = eva.fitness(barabasiSon[0], false);
            double fitness1 = eva.fitness(barabasiSon[1], false);
            finalTime = System.nanoTime();
            evaluationTime += finalTime - initialTime;
            
            initialTime = System.nanoTime();
            LevelDecoder decoder2 = new LevelDecoder(barabasiSon[0]);
            LevelDecoder decoder3 = new LevelDecoder(barabasiSon[1]);
            DefaultGraph son0 = decoder2.decode();
            DefaultGraph son1 = decoder3.decode();
            finalTime = System.nanoTime();
            decodeTime += finalTime - initialTime;
            
            initialTime = System.nanoTime();
            LevelOperators gop = new LevelOperators();
            LevelIndividual[] son = gop.crossover(fatherIndividual, motherIndividual);
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
