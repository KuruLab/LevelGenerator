/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoLevel;

import util.GraphStreamUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePointPosition;

/**
 *
 * @author andre
 */
public class LevelDecoder {
    
    private LevelIndividual individual;
    private Graph graph;
    //private long time;
    
    public LevelDecoder(LevelIndividual ind){
        this.individual = ind;
        this.graph = null;
        //this.time = System.nanoTime();
    }

    public LevelDecoder(Graph graph) {
        this.individual = null;
        this.graph = graph;
        //this.time = System.nanoTime();
    }
    
    // get a GraphStream's graph and convert to an individual graph
    public LevelIndividual encode(){
        if(this.graph != null){
            LevelIndividual gI = new LevelIndividual();

            for(int i = 0; i < graph.getNodeCount(); i++){
                LevelGene nI = new LevelGene();
                nI.setWidth(graph.getNode(i).getAttribute("width"));
                nI.setHeight(graph.getNode(i).getAttribute("height"));
                Point3 p = nodePointPosition(graph.getNode(i));
                int[] xyz = {Math.round((float) p.x), Math.round((float) p.y), Math.round((float) p.z)};
                nI.setXYZ(xyz);
                
                nI.setConnectedNodes(new ArrayList<>());
                
                gI.addNode(nI);
            }
            
            for(int i = 0; i < graph.getEdgeCount(); i++){
                Edge edge = graph.getEdge(i);
                int node0Idx = edge.getNode0().getIndex();
                int node1Idx = edge.getNode1().getIndex();
                if(!gI.getNode(node0Idx).getConnectedNodes().contains(gI.getNode(node1Idx)))
                    gI.getNode(node0Idx).getConnectedNodes().add(gI.getNode(node1Idx));
                if(!gI.getNode(node1Idx).getConnectedNodes().contains(gI.getNode(node0Idx)))
                    gI.getNode(node1Idx).getConnectedNodes().add(gI.getNode(node0Idx));
            }
            
            //this.time = System.nanoTime() - this.time;
            return gI;
        }
        else{
            System.err.println("Encoder: Null Graph");
            //this.time = System.nanoTime() - this.time;
            return null;
        }
    }
    
    // get an individual and convert to another connected individual, but
    // based on http://barabasi.com/f/622.pdf (p.25-28) barabasi's model
    public LevelIndividual barabasiAlbertGraph() {
        if (this.individual != null) {
            
            // new Graph being created
            LevelIndividual gI = new LevelIndividual(); 
            
            // clears every existing connection on the original graph
            for(int i = 0; i < this.individual.getNodes().size(); i++){
                this.individual.getNode(i).setConnectedNodes(new ArrayList<>());
            }
            // starts with the node0
            gI.addNode(this.individual.getNode(0));
            // h[j] is the network-based(?) cost from node 'j' to node0
            double[] h = new double[this.individual.getNodes().size()];
            // obviously, there is no netowrk-cost from node0 to himself, so it is 0
            h[0] = 0;
            
            // iteration to add new nodes, step-by-step
            for(int i = 1; i < this.individual.getNodes().size(); i++){
                LevelGene newNode = this.individual.getNode(i);
                
                // keep the same seed for the same node
                int seed = newNode.toHashString().hashCode();
                //System.out.println("hash: "+seed);
                Random random = new Random(seed);
                
                // calculate all costs from node i to any existing node j in gI
                double[] costs = new double[i];
                ArrayList<Integer> connected = new ArrayList<>(); // list of the already connected nodes, in case m > 1
                double prob = 1.0; // new probability of connection
                while(connected.size() < LevelConfig.maxLinksPerStep){
                    for(int j = 0; j < i; j++){
                        // C_i = min_j [delta * d_ij + h_j]
                        double d_ij = Math.sqrt(Math.pow(newNode.getXYZ()[0] - gI.getNode(j).getXYZ()[0], 2) +
                                                Math.pow(newNode.getXYZ()[1] - gI.getNode(j).getXYZ()[1], 2));
                        costs[j] = LevelConfig.barabasiFactor * d_ij  + h[j];
                    }
                    int minJ = 0;           // the best j so far
                    double minC = costs[0]; // the min cost so far
                    // find the min cost and the best j
                    for(int j = 1; j < i; j++){
                        if(!connected.contains(j)){
                            if(costs[j] < minC){
                                minC = costs[j];
                                minJ = j;
                            }
                        }
                    }
                    connected.add(minJ);
                    // connect the node i to the best node j found and vice-versa
                    gI.getNode(minJ).getConnectedNodes().add(newNode);
                    newNode.getConnectedNodes().add(gI.getNode(minJ));
                    
                    // update the probability of a new connection
                    prob /= ((double) connected.size() + 1);
                    if(random.nextDouble() < prob){
                        break; // if fail the test, then we don't add a new node, even if connected.size < maxLinkPerStep
                    }
                }
                // debug
                //if(newNode.getConnectedNodes().size() == 0 || gI.getNode(minJ).getConnectedNodes().size() == 0){
                //    System.err.println("Connection Error");
                //}
                // set the network-cost (?) of the node i as the it's distance from j plus the network-cost of j
                h[i] =  Math.sqrt( Math.pow(newNode.getXYZ()[0] - gI.getNode(connected.get(0)).getXYZ()[0], 2)  +
                                   Math.pow(newNode.getXYZ()[1] - gI.getNode(connected.get(0)).getXYZ()[1], 2)) + h[connected.get(0)];
                // finally, add node i to the new graph
                gI.addNode(newNode);
            }
            // adjust the node size, based on it's degree
            //RandomGraphGenerator rgg = new RandomGraphGenerator();
            normalizeNodesSizes(gI, LevelConfig.minNodeSize, LevelConfig.maxNodeSize);
            return gI;
        } else {
            System.err.println("Encoder: Null Graph");
            //this.time = System.nanoTime() - this.time;
            return null;
        }
    }
    
    public static void normalizeNodesSizes(LevelIndividual graph, double minimumsize, double maximumsize) {
        int smaller = -1;
        int greater = -1;
        for (LevelGene n : graph.getNodes()) {
            if (n.getConnectedNodes().size()> greater || smaller == -1) {
                greater = n.getConnectedNodes().size();
            }
            if (n.getConnectedNodes().size() < smaller || greater == -1) {
                smaller = n.getConnectedNodes().size();
            }
        }
        for (LevelGene n : graph.getNodes()) {
            double scale = (double) (n.getConnectedNodes().size() - smaller) / (double) (greater - smaller);
            double roundedSize = (double) Math.round((scale * maximumsize) + minimumsize);
            
            if(roundedSize % 2 != 0)
                roundedSize++;
            
            n.setWidth(roundedSize);
            n.setHeight(roundedSize);
        }
    }
    
    // get a connected individual graph and convert to GraphStream's
    public DefaultGraph decode(){
        if(this.individual != null){
            //System.out.println("Decoding individual of size: "+individual.getNodes().size());
            DefaultGraph g = new DefaultGraph("x");
            g.addAttribute("ui.antialias");
            g.addAttribute("ui.quality");
            g.addAttribute("ui.stylesheet", "url('media/stylesheet.css')");
            
            /*Layout layout = new SpringBox(false);
            g.addSink(layout);
            layout.addAttributeSink(g);

            //BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator();
            //gen.addSink(g);
            //gen.begin();
            //while(g.getNodeCount() < individual.getNodes().size()){
            //    gen.nextEvents();
            //}
            //gen.end();*/
            for(int i = 0; i < individual.getNodes().size(); i++){
                //try{
                    g.addNode(individual.getNode(i).getXYZ()[0]+"."+
                          individual.getNode(i).getXYZ()[1]+"."+
                          individual.getNode(i).getXYZ()[2]);
                /*} catch (org.graphstream.graph.IdAlreadyInUseException ex){
                    g.addNode(individual.getNode(i).getXYZ()[0]+"."+
                          individual.getNode(i).getXYZ()[1]+"."+
                          individual.getNode(i).getXYZ()[2]+"x2");
                }*/
            }
            //System.out.println("Creating graph of size: "+g.getNodeCount());
            // Remove all initial edges and all attributes
            for (Node node : g) {
                //g.clearAttributes();
                Iterator<Edge> it = node.getEdgeIterator();
                while (it.hasNext()) {
                    it.next();
                    it.remove();
                }
            }
            // Setup all attributes and all edges
            for(int i = 0; i < individual.getNodes().size(); i++){
                /*for(String key : individual.getNode(i).getEachAttributeKey()) {
                    g.getNode(i).setAttribute(key, individual.getNode(i).getAttribute(key));
                }*/
                g.getNode(i).addAttribute("ui.label", g.getNode(i).getId());
                if (null != g.getNode(i).getAttribute("ui.style")) {
                    g.getNode(i).setAttribute("ui.style", g.getNode(i).getAttribute("ui.style") + " size: " + individual.getNode(i).getWidth() + ";");
                } else {
                    g.getNode(i).addAttribute("ui.style", " size: " + individual.getNode(i).getWidth() + ";");
                }
                
                g.getNode(i).setAttribute("xyz", individual.getNode(i).getXYZ());
                g.getNode(i).setAttribute("width", (double) individual.getNode(i).getWidth());
                g.getNode(i).setAttribute("height", (double) individual.getNode(i).getHeight());
                // setup edges
                for(int j = 0; j < individual.getNode(i).getConnectedNodes().size(); j++){
                    String node0ID = individual.getNode(i).getXYZ()[0]+"."+
                                     individual.getNode(i).getXYZ()[1]+"."+
                                     individual.getNode(i).getXYZ()[2];
                    String node1ID = individual.getNode(i).getConnectedNodes().get(j).getXYZ()[0]+"."+
                                     individual.getNode(i).getConnectedNodes().get(j).getXYZ()[1]+"."+
                                     individual.getNode(i).getConnectedNodes().get(j).getXYZ()[2];
                    String edgeID0 = node0ID+"_"+node1ID;
                    String edgeID1 = node1ID+"_"+node0ID;
                                    
                    if(g.getEdge(edgeID0) == null && g.getEdge(edgeID1) == null)
                        if(g.getNode(node0ID) != null && g.getNode(node1ID) != null){
                            g.addEdge(edgeID0, node0ID, node1ID, false);
                            double d_ij = GraphPosLengthUtils.edgeLength(g.getEdge(edgeID0));
                            g.getEdge(edgeID0).addAttribute("weight", d_ij);
                        }
                }
            }

            // adjust the node size, based on it's degree
            GraphStreamUtil gUtil = new GraphStreamUtil();
            //PuzzleUtil dUtil = new PuzzleUtil();
            gUtil.normalizeNodesSizes(g, LevelConfig.minNodeSize, LevelConfig.maxNodeSize); 
            //dUtil.initializeGraphSymbols(g);
            gUtil.setupStyle(g);        
            return g;
        }
        else{
            System.err.println("Decoder: Null Individual");
            //this.time = System.nanoTime() - this.time;
            return null;
        }
    }
    // suspicious behaviour
    /*public Graph individualToGraphStream(){
        Individual barabasi = this.barabasiAlbertGraph();
        LevelDecoder graphDecoder = new LevelDecoder(barabasi);
        return graphDecoder.decode();
    }
    
    public Graph individualToDungeon(boolean debug){
        Individual barabasi = this.barabasiAlbertGraph();
        LevelDecoder graphDecoder = new LevelDecoder(barabasi);
        Graph graph = graphDecoder.decode();
        DungeonGenerator dgg = new DungeonGenerator(graph);
        dgg.generate(debug);
        return graph;
    }
    
    public Graph individualToDungeon(){       
        return individualToDungeon(false);
    }*/
      
    public LevelIndividual getIndividual() {
        return individual;
    }

    public void setIndividual(LevelIndividual individual) {
        this.individual = individual;
    }

    /*public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }*/
}
