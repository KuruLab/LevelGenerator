/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoPuzzle;

import evoGraph.Decoder;
import evoGraph.NodeGene;
import evoGraph.GraphIndividual;
import extendedMetaZelda.Condition;
import extendedMetaZelda.DungeonConfig;
import extendedMetaZelda.Symbol;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author andre
 */
public class RandomPuzzleGenerator {
    
    public PuzzleIndividual newPuzzle(GraphIndividual individual){
        Decoder barabasiDecoder = new Decoder(individual);
        GraphIndividual barabasi = barabasiDecoder.barabasiAlbertGraph();
        //Decoder graphDecoder = new Decoder(barabasi);
        //Graph graph = graphDecoder.decode();
        
        PuzzleIndividual puzzle = new PuzzleIndividual();
        
        ArrayList<NodeGene> rooms = (ArrayList<NodeGene>) barabasi.getNodes().clone();
        ArrayList<Integer> selected = new ArrayList<>();
        Random random = new Random(System.nanoTime());
        int start = random.nextInt(rooms.size());
        selected.add(start);
        int boss = random.nextInt(rooms.size());
        while(selected.contains(boss)){
            boss = random.nextInt(rooms.size());
        }
        selected.add(boss);
        
        PuzzleGene startGene = new PuzzleGene(start);
        startGene.getSymbols().add(new Symbol(Symbol.START));
        puzzle.add(startGene);
        
        PuzzleGene bossGene = new PuzzleGene(boss);
        bossGene.getSymbols().add(new Symbol(Symbol.BOSS));
        puzzle.add(bossGene);
        
        for(int i = 0; i < DungeonConfig.defaultMaxKeys; i++){
            int nextKey = random.nextInt(rooms.size());
            while(selected.contains(nextKey)){
                nextKey = random.nextInt(rooms.size());
            }
            selected.add(nextKey);
            
            PuzzleGene keyGene = new PuzzleGene(nextKey);
            
            Condition condition = new Condition(new Symbol(i));
            Symbol symbol = new Symbol(i+1);
            
            keyGene.getConditions().add(condition);
            keyGene.getSymbols().add(symbol);
            
            puzzle.add(keyGene);
        }
        // switches not supported yet
        return puzzle;
    }
    
    //public PuzzleIndividual newPuzzle(Graph graph){
    //    
    //}
    
}
