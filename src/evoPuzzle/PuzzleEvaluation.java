/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoPuzzle;

import evoGraph.Config;
import evoGraph.DungeonDistanceCost;
import evoGraph.GraphIndividual;
import extendedMetaZelda.Condition;
import extendedMetaZelda.Symbol;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.edgeLength;

/**
 *
 * @author andre
 */
public class PuzzleEvaluation {
    
    public double[] fitness(GraphIndividual original, PuzzleIndividual puzzle, boolean debug) {
        PuzzleDecoder pdec = new PuzzleDecoder(original);
        Graph graph = pdec.decode(puzzle, false);
       
        double[] fitness = new double[5];
        
        Hashtable<String, Integer> pathCount = new Hashtable<>();
        ArrayList<Node> targets = new ArrayList<>();
        ArrayList<Integer> visited = new ArrayList<>();
        Node start = graph.getNode(puzzle.getStart().getNodeID());
        Node boss = graph.getNode(puzzle.getBoss().getNodeID());
        targets.add(start);
        for(int i = 2; i < puzzle.getNodes().size(); i++){
            targets.add(graph.getNode(puzzle.getNodes().get(i).getNodeID()));
        }
        targets.add(boss);
            
        /*for(Node node : targets){
            Symbol symbol = node.getAttribute("symbol");
            System.out.println(node.getIndex()+": "+symbol.toString());
        }*/
        int keyLevel = 0;
        double totalCost = 0;
        double penalty = 0;
        Node current = targets.remove(0);
        while(!targets.isEmpty()){
            Symbol symbol = current.getAttribute("symbol");
            if(symbol.isKey())
                keyLevel = symbol.getValue();
            else{
                Condition condition = current.getAttribute("condition");
                keyLevel = condition.getKeyLevel();
            }
            
            Node next = targets.get(0);
            DungeonDistanceCost pdc = new DungeonDistanceCost(keyLevel);
            AStar astar = new AStar(graph);
            astar.setCosts(pdc);
            astar.compute(current.getId(), next.getId());
            Path path = astar.getShortestPath();
            
            double cost = 0;
            //System.out.println("Next key: "+symbol.toString()+": ");
            Node pathStep = current;
            //System.out.println("Path from "+current.getIndex()+" to "+next.getIndex());
            for (Node step : path.getNodePath()) {
                if(!visited.contains(step.getIndex()))
                    visited.add(step.getIndex());
                if(step.equals(path.getRoot())){
                    continue;
                }
                else{
                    if(pathCount.containsKey(step.getId())){
                        int value = pathCount.get(step.getId());
                        pathCount.replace(step.getId(), ++value);
                    }
                    else{
                        pathCount.put(step.getId(), 1);
                    }
                }
                Edge edge = pathStep.getEdgeBetween(step);
                Symbol edgeKey = edge.getAttribute("symbol");
                if(keyLevel < edgeKey.getValue()){
                    //System.out.println("Bad keylevel: "+keyLevel+" < "+edgeKey.getValue()+" -> From: "+pathStep.getIndex()+" To: "+step.getIndex());
                    penalty += ((edgeKey.getValue()-keyLevel)*1000);
                }
                cost += edgeLength(edge);
                // if(keyLevel < edgeKey.getValue())
                //     System.out.println(keyLevel+" < "+edgeKey.getValue());
                //System.out.println(step.getIndex()+" cost: "+(edgeLength(pathStep.getEdgeBetween(step)) + penalty));
                pathStep = step;
                //System.out.println(step.getIndex()+" S:"+step.getAttribute("symbol").toString()+" C:"+step.getAttribute("condition").toString());
            }
            //System.out.println();
            // prevent the existance of broken dungeons and grants solvability on final solutions

            totalCost += cost;
            current = targets.remove(0);
            keyLevel++;
        }
        int nonlinearity = Integer.MIN_VALUE;
        //String maxKey = "";
        for(String key : pathCount.keySet()){
            if(nonlinearity < pathCount.get(key)){
                nonlinearity = pathCount.get(key);
                //maxKey = key;
            }
        }
        //System.out.println("Nonlinearity: "+maxKey+": "+nonlinearity);
        fitness[1] = Math.abs(nonlinearity - Config.idealNonLinearity);
        fitness[2] = 1.0/totalCost;
        fitness[3] = 1.0/visited.size();
        fitness[4] = penalty;
        fitness[0] = fitness[1] + fitness[2] + fitness[3] + fitness[4];
        
        if (debug) {
            System.out.println(
                    "Fitness: " + fitness[0] + 
                    "(DIN: " + fitness[1] + 
                    " TD: " + fitness[2] + 
                    " VR: "  + fitness[3] + 
                    " P: " + fitness[4] +
                    ")");
        }
        
        return fitness;
    }
    
    protected static final Comparator<Node> KEYVALUE_COMPARATOR = new Comparator<Node>(){
        @Override
        public int compare(Node arg0, Node arg1) {
            Symbol key0 = arg0.getAttribute("symbol");
            Symbol key1 = arg1.getAttribute("symbol");
            return key0.getValue() > key1.getValue() ? 1
                 : key0.getValue() < key1.getValue() ? -1
                 : 0;
        }
    };

    
}
