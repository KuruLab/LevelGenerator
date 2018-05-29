/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package extendedMetaZelda;

import extendedMetaZelda.DungeonGenerator.KeyLevelRoomMapping;
import extendedMetaZelda.DungeonGenerator.RetryException;
import graphstream.GraphStreamUtil;
import graphstream.Tree;
import java.util.ArrayList;
import oldmapgenerator.Room;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 *
 * @author andre
 */
public class DungeonUtil {
    
    public void initializeGraphIntensity(Graph graph){
        for(int i = 0; i < graph.getNodeCount(); i++){
            graph.getNode(i).addAttribute("ui.color", 0.0);
            graph.getNode(i).addAttribute("ui.class", "standard");
        }
    }
    
    public void initializeGraphSymbols(Graph graph){
        for(int i = 0; i < graph.getNodeCount(); i++){
            graph.getNode(i).addAttribute("symbol", new Symbol(0));
        }
        for(int i = 0; i < graph.getEdgeCount(); i++){
            graph.getEdge(i).addAttribute("symbol", new Symbol(0));
        }
    }
    
    public void initializeGraphConditions(Graph graph){
        for(int i = 0; i < graph.getNodeCount(); i++){
            graph.getNode(i).addAttribute("condition", new Condition());
        }
    }
    
    public Node findStart(Graph graph){
        for(Node node : graph.getEachNode()){
            Symbol symbol = node.getAttribute("symbol");
            if(symbol.isStart())
                return node;
        }
        return null;
    }
    
    public Node findBoss(Graph graph){
        for(Node node : graph.getEachNode()){
            Symbol symbol = node.getAttribute("symbol");
            if(symbol.isBoss())
                return node;
        }
        return null;
    }
    
    public Node findSymbol(Graph graph, Symbol symbol){
        for(Node node : graph.getEachNode()){
            Symbol s = node.getAttribute("symbol");
            if(symbol.equals(s))
                return node;
        }
        return null;
    }
    
    /**
     * Computes the 'intensity' of each {@link Room}. Rooms generally get more
     * intense the deeper they are into the dungeon.
     *
     * @param levels    the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     * @see Room
     */
    public void computeDepthBasedIntensity(Graph graph) {
        
        graph.getNode(0).setAttribute("symbol", new Symbol(Symbol.START));
        Node boss = (new GraphStreamUtil()).findFartestNode(graph.getNode(0), graph);
        boss.setAttribute("symbol", new Symbol(Symbol.BOSS));
        
        GraphStreamUtil utils = new GraphStreamUtil();
        Tree<Node> tree = utils.minimumSpanningTree(graph);
        ArrayList<Tree<Node>> subTrees = (ArrayList<Tree<Node>>) tree.getSubTrees();
        while (!subTrees.isEmpty()){
            Tree<Node> sub = subTrees.remove(0);
            double depth = sub.getDepth();
            sub.getHead().setAttribute("ui.color", depth);
            subTrees.addAll(sub.getSubTrees());
            
            //System.out.println(sub.getHead().getId()+": "+(double)sub.getHead().getAttribute("ui.color"));
        }

        normalizeIntensity(graph);
        
        for(Node node : graph.getEachNode()){
            //System.out.println("Node "+node.getId()+" is '"+node.getAttribute("symbol")+"' with color "+node.getAttribute("ui.color"));
            Symbol symbol = node.getAttribute("symbol");
            if(symbol.isNothinig() || symbol.isKey()){
                node.setAttribute("ui.class", "standard");
            }
            else if(symbol.isStart()){
                node.setAttribute("ui.color", 0.0);
                node.setAttribute("ui.class", "start");
            }
            else if(symbol.isBoss()){ 
                node.setAttribute("ui.class", "boss");
                node.setAttribute("ui.color", 1.0);
            }
        }
    } 
    
    /**
     * Scales intensities within the dungeon down so that they all fit within
     * the range 0 < intensity < 1.0.
     *
     * @see Room
     */
    public void normalizeIntensity(Graph graph) {
        double maxIntensity = 0.0;
        for (Node room: graph.getEachNode()) {
            maxIntensity = Math.max(maxIntensity, room.getAttribute("ui.color"));
        }
        for (Node room: graph.getEachNode()) {
            room.setAttribute("ui.color", (double) room.getAttribute("ui.color") * 0.99 / maxIntensity);
            //System.out.println((double)room.getAttribute("ui.color"));
        }
    }
    
    
    
}
