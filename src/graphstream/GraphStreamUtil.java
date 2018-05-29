/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphstream;

import extendedMetaZelda.Symbol;
import evoGraph.Config;
import extendedMetaZelda.Condition;
import extendedMetaZelda.DungeonUtil;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import javax.swing.JOptionPane;
import org.graphstream.algorithm.AStar;
import org.graphstream.algorithm.AStar.DistanceCosts;
import org.graphstream.algorithm.Prim;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.edgeLength;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePointPosition;

/**
 *
 * @author andre
 */
public class GraphStreamUtil {
    
    /*public Hashtable<String, Integer> mapNodeID2Index(Graph graph){
        Hashtable<String, Integer> id2IndexMap = new Hashtable<>();
        
        for(int i = 0; i < graph.getNodeCount(); i++){
            Integer index = new Integer(i);
            String coord = graph.getNode(i).getId();
            if(id2IndexMap.containsKey(coord)){
                System.out.println("Error: table already contains coord "+coord);
            }
            else{
                id2IndexMap.put(coord, index);
                //System.err.println(index+": "+coord);
            }
        }
        return id2IndexMap;
    }*/
    
    public double getGraphArea(Graph graph){
        int[] bounds = getGraphBounds(graph);
        return (double)(bounds[0]*bounds[1]);
    }
    
    public double getGraphAreaWithBorder(Graph graph){
        int[] bounds = getGraphBoundsWithBorder(graph);
        return (double)(bounds[0]*bounds[1]);
    }
    
    public int[] getGraphBounds(Graph graph){
        int[] bounds = new int[3];
        bounds[0] = 0;
        bounds[1] = 0;
        bounds[2] = 0;
        for(Node node : graph.getEachNode()){
            Point3 p = nodePointPosition(node);
            //System.out.println(node.getId()+": "+p.toString());
            bounds[0] = (int) Math.max(bounds[0], p.x + ((double) node.getAttribute("width") /2));
            bounds[1] = (int) Math.max(bounds[1], p.y + ((double) node.getAttribute("height")/2));
        }
        return bounds;
    }
    
    public int[] getGraphBoundsWithBorder(Graph graph){
        int[] bounds = new int[3];
        bounds[0] = 0;
        bounds[1] = 0;
        bounds[2] = 0;
        for(Node node : graph.getEachNode()){
            Point3 p = nodePointPosition(node);
            //System.out.println(node.getId()+": "+p.toString());
            bounds[0] = (int) Math.max(bounds[0], p.x + ((double) node.getAttribute("width") /2) + Config.borderSize);
            bounds[1] = (int) Math.max(bounds[1], p.y + ((double) node.getAttribute("height")/2) + Config.borderSize);
        }
        return bounds;
    }
    
    public Tree<Node> randomSpanningTree(Graph graph, Random random){
        ArrayList<Node> available = new ArrayList<>();
        ArrayList<Node> taken = new ArrayList<>();
        int[] possibleChilds = new int[graph.getNodeCount()];
        for(int i = 0; i < graph.getNodeCount(); i++){
            available.add(graph.getNode(i));
            possibleChilds[i] = graph.getNode(i).getDegree();
        }
        Node first = available.remove(0);
        taken.add(first);
        Tree<Node> tree = new Tree<>(first);
        while(!available.isEmpty()){
            int nextIndex = random.nextInt(taken.size());
            Node parent = taken.get(nextIndex);
            while(possibleChilds[parent.getIndex()] == 0){
                nextIndex = random.nextInt(taken.size());
                parent = taken.get(nextIndex);
            }
            Iterator<Node> iter = parent.getNeighborNodeIterator();
            while(iter.hasNext()){
                Node child = iter.next();
                if(taken.contains(child))
                    continue;
                else{
                    available.remove(child);
                    taken.add(child);
                    tree.addLeaf(parent, child);
                    possibleChilds[parent.getIndex()]--;
                    possibleChilds[child.getIndex()]--;
                    parent.getEdgeBetween(child).setAttribute("ui.class", "intree");
                }
            }
        }
        return tree;
    }
    
    public Tree<Node> minimumSpanningTree(Graph graph){
        Prim prim = new Prim("ui.class", "intree", "notintree");
        prim.init(graph);
        prim.compute();
        
        ArrayList<Edge> treeEdges = new ArrayList<>();
        for(Edge edge : prim.getTreeEdges()){
            //System.out.println(edge);
            treeEdges.add(edge);
        }
        Node start = (new DungeonUtil()).findStart(graph);
        if(start == null)
            start = graph.getNode(0);
        //System.out.println("Calculating spanning tree from root node "+start.getId());
        Tree<Node> tree = new Tree(start);
        ArrayList<Node> openChild = new ArrayList<>();
        openChild.add(start);
        while(!openChild.isEmpty()){
            Node root = openChild.remove(0);
            ArrayList<Edge> toRemove = new ArrayList<>();
            for(Edge edge : treeEdges){
                if(edge.getSourceNode().equals(root)){
                    tree.addLeaf(root, edge.getTargetNode());
                    toRemove.add(edge);
                    openChild.add(edge.getTargetNode());
                }
                else if(edge.getTargetNode().equals(root)){
                    tree.addLeaf(root, edge.getSourceNode());
                    toRemove.add(edge);
                    openChild.add(edge.getSourceNode());
                }
            }
            treeEdges.removeAll(toRemove);
        }
        //System.out.println(tree.toString());
        return tree;
    } 
    
    public Node findFartestNode(Node from, Graph graph){
        AStar astar = new AStar(graph);
 	astar.setCosts(new DistanceCosts());
        Node to = null;
        double bestCost = -1;
        for(Node node : graph.getEachNode()){
            astar.compute(from.getId(), node.getId());
            double cost = 0;
            try {
                for (Edge edge : astar.getShortestPath().getEdgePath()) {
                    cost += (double) edgeLength(edge);
                }
                if (cost > bestCost) {
                    to = node;
                    bestCost = cost;
                }
            } catch (java.lang.NullPointerException ex) {
                JOptionPane.showMessageDialog(null, "It was not possible to find a valid path to a node.\nPlease, fix the graph edges before 'Apply'.", "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
        return to;
    }
    
    public void setupStyle(Graph graph) {
        //Random random = new Random();
        //System.out.println("Setting style...");
        for (int i = 0; i < graph.getNodeCount(); i++) {
            Node node = graph.getNode(i);
            Symbol symbol = (Symbol) node.getAttribute("symbol");
            Condition cond = (Condition) node.getAttribute("condition");
            //System.out.println("Node "+node.getId()+" is "+symbol.toString());
            if(symbol == null || symbol.isNothinig()){
                node.addAttribute("ui.label", String.format("%s:", i));
                node.setAttribute("ui.class", "standard");
            }
            else{
                node.addAttribute("ui.label", String.format("%s: *%s*", i, symbol));
                if(symbol.isKey())
                    node.setAttribute("ui.class", "standard");
                if(symbol.isStart())
                    node.setAttribute("ui.class", "start");
                if(symbol.isBoss())
                    node.setAttribute("ui.class", "boss");    
            }
            if(cond == null || cond.getKeyLevel() == 0){
                node.addAttribute("ui.label", String.format("%s [%s]", node.getAttribute("ui.label"), 0));
            }
            else{
                node.addAttribute("ui.label", String.format("%s [%s]", node.getAttribute("ui.label"), cond.toString()));
            }
        }
        for (int i = 0; i < graph.getEdgeCount(); i++) {
            Edge edge = graph.getEdge(i);
            Symbol symbol = (Symbol) edge.getAttribute("symbol");
            //System.out.println(edge.toString()+": "+symbol.toString());
            if(symbol == null || symbol.isNothinig())
                edge.addAttribute("ui.label", "");
            else
                edge.addAttribute("ui.label", String.format("%s", symbol.toString()));
        }
    }
    
    public void normalizeNodesSizes(Graph graph, double minimumsize, double maximumsize) { 
        int smaller = Integer.MAX_VALUE;
        int greater = Integer.MIN_VALUE;
        for (Node n : graph.getEachNode()) {
            // remove previous size
            if (null != n.getAttribute("ui.style")) {
                String style = n.getAttribute("ui.style");
                if(style.contains("size:"))
                {
                    String[] styles = style.split(";");
                    String newStyle = "";
                    for(int i = 0; i < styles.length; i++){
                        if(!styles[i].contains("size"))
                            newStyle += styles[i]+"; ";
                    }
                    n.setAttribute("ui.style", newStyle);
                }
            } 
            
            if (n.getDegree() > greater) {
                greater = n.getDegree();
            }
            if (n.getDegree() < smaller) {
                smaller = n.getDegree();
            }
        }
        //System.out.println("Normalize -> greater: "+greater+" smaller: "+smaller);
        for (Node n : graph.getEachNode()) {
            double scale = (double) (n.getDegree() - smaller) / (double) (greater - smaller);
            double roundedSize = (double) Math.round((scale * maximumsize) + minimumsize);
            if( (int) roundedSize % 2 > 0)
                roundedSize++;
            roundedSize = (int) roundedSize;
            if (null != n.getAttribute("ui.style")) {
                n.setAttribute("ui.style", n.getAttribute("ui.style") + " size: " + roundedSize + ";");
            } else {
                n.addAttribute("ui.style", " size: " + roundedSize + ";");
            }
            n.setAttribute("width", roundedSize);
            n.setAttribute("height", roundedSize);
        }
    }
    
    public void scalePosition(Graph graph, double scalar, boolean normalize){
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minXwidth = 0;
        double minYheight = 0;
        for(int i = 0; i < graph.getNodeCount(); i++){
            Point3 p = nodePointPosition(graph.getNode(i));
            p.scale(scalar);
            //System.out.println("Point "+graph.getNode(i).getId()+": "+p.toString());
            graph.getNode(i).setAttribute("xyz", Math.round(p.x), Math.round(p.y), 0.0);
            if(normalize){
                if(p.x < minX){
                    minX = p.x;
                    minXwidth = graph.getNode(i).getAttribute("width");
                }
                if(p.y < minY){
                    minY = p.y;
                    minYheight = graph.getNode(i).getAttribute("height");
                }
            }
        }
        //System.out.println("minX, minY: "+minX+", "+minY);
        // positive values normalization
        if(normalize){
            minX -= minXwidth/2;
            minY -= minYheight/2;
            for (int i = 0; i < graph.getNodeCount(); i++) {
                Point3 p = nodePointPosition(graph.getNode(i));
                graph.getNode(i).setAttribute("xyz", Math.round(p.x + Math.abs(minX))+ Config.borderSize, Math.round(p.y + Math.abs(minY)) + Config.borderSize, 0.0);
                //p = nodePointPosition(graph.getNode(i));
                //graph.getNode(i).
            }
        }
    }
}
