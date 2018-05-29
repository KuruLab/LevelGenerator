/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoGraph.Config;
import evoGraph.Decoder;
import evoGraph.EvoJSONFileReader;
import evoGraph.GraphIndividual;
import evoPuzzle.PuzzleDecoder;
import evoPuzzle.PuzzleEvaluation;
import evoPuzzle.PuzzleIndividual;
import evoPuzzle.RandomPuzzleGenerator;
import graphstream.GraphStreamUtil;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 *
 * @author andre
 */
public class RandomPuzzleTest {
    public static void main(String args[]){
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        
        String filename = "D:\\Mega\\posdoc\\MapGenerator\\experiments\\only_asp_and_din\\1618875503\\map_1618875503.json";
        System.out.println(filename);
        EvoJSONFileReader reader = new EvoJSONFileReader(filename);
        Graph graph = reader.parseJson();
        
        System.out.println("Encoding Graph as Individual");
        Decoder originalEncoder = new Decoder(graph);
        GraphIndividual originalIndividual = originalEncoder.encode();
        
        System.out.println("Connecting Nodes");
        Decoder connectDecoder = new Decoder(originalIndividual);
        GraphIndividual connectedIndividual = connectDecoder.barabasiAlbertGraph();
        
        System.out.println("Decoding Individual back as Graph");
        Decoder graphDecoder = new Decoder(connectedIndividual);
        Graph decodedGraph = graphDecoder.decode();
        
        RandomPuzzleGenerator rpg = new RandomPuzzleGenerator();
        PuzzleIndividual puzzle = rpg.newPuzzle(originalIndividual);
        
        System.out.println(puzzle);
        PuzzleDecoder pdec = new PuzzleDecoder(connectedIndividual);
        decodedGraph = pdec.decode(puzzle, true);
        
        GraphStreamUtil gUtil = new GraphStreamUtil();
        decodedGraph.addAttribute("ui.stylesheet", "url('media/stylesheet.css')");
        gUtil.normalizeNodesSizes(decodedGraph, Config.minNodeSize, Config.maxNodeSize);
        gUtil.setupStyle(decodedGraph);
        
        decodedGraph.display(false);
        
        PuzzleEvaluation peva = new PuzzleEvaluation();
        double[] fitness = peva.fitness(connectedIndividual, puzzle, true);
        System.out.println("Fitness: "+fitness[0]+" - DIN: "+fitness[1]+" TS: "+fitness[2]+" P: "+fitness[3]);
    }
}
