/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoLevel;

import config.GeneralConfig;
import java.util.HashMap;
import java.util.Random;
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.geom.Point3;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePointPosition;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import util.GraphStreamUtil;

/**
 *
 * @author andre
 */
public class RandomLevelGenerator {
    
    protected Random rng;
    
    public RandomLevelGenerator(){
        System.setProperty(
            "org.graphstream.ui.renderer",
            "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        rng = new Random(System.nanoTime());
    }
    
    public LevelIndividual barabasiAlbertIndividual(String graphName){
        LevelIndividual graph = new LevelIndividual();
        Graph dGraph = barabasiAlbert(graphName);
        for(int i = 0; i < dGraph.getNodeCount(); i++)
            graph.addNode(dGraph.getNode(i));
        return graph;
    }
    
    public Graph barabasiAlbert(String graphName){
        DefaultGraph g = new DefaultGraph(graphName);
        g.addAttribute("ui.antialias");
        g.addAttribute("ui.quality");
        g.addAttribute("ui.stylesheet", "url('media/stylesheet.css')");
        
        Layout layout = new SpringBox(false);
        g.addSink(layout);
        layout.addAttributeSink(g);
        
        BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator(1);
        gen.addSink(g);
        gen.begin();
        int numOfNodes = rng.nextInt(LevelConfig.minNodeCount) + LevelConfig.minNodeCount;
        while(g.getNodeCount() < numOfNodes){
            gen.nextEvents();
        }
        gen.end();
        
        GraphStreamUtil gUtil = new GraphStreamUtil();
        //PuzzleUtil dUtil = new PuzzleUtil();
        gUtil.normalizeNodesSizes(g, LevelConfig.minNodeSize, LevelConfig.maxNodeSize); 
        //dUtil.initializeGraphSymbols(g);
        gUtil.setupStyle(g);
        
        while(layout.getStabilization() < 0.95){
            layout.compute();
        }
        gUtil.scalePosition(g, GeneralConfig.borderSize, LevelConfig.scaleFactor, true);
        correctInvalidNodes(g);
        /*// printing for debug
        for(Node node : g.getEachNode()){
            Point3 p = nodePointPosition(node);
            System.out.println(node.getId()+" "+p.toString());
        }*/
        return g;
    }
    
    // same as above, but with fixed size
    public DefaultGraph barabasiAlbert(String graphName, int size){
        DefaultGraph g = new DefaultGraph(graphName);
        g.addAttribute("ui.antialias");
        g.addAttribute("ui.quality");
        g.addAttribute("ui.stylesheet", "url('media/stylesheet.css')");
        
        Layout layout = new SpringBox(false);
        g.addSink(layout);
        layout.addAttributeSink(g);
        
        BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator(1);
        gen.addSink(g);
        gen.begin();
        while(g.getNodeCount() < size){
            gen.nextEvents();
        }
        gen.end();
        
        GraphStreamUtil util = new GraphStreamUtil();
        util.normalizeNodesSizes(g, LevelConfig.minNodeSize, LevelConfig.maxNodeSize);
        
        util.setupStyle(g); 
        while(layout.getStabilization() < 0.95){
            layout.compute();
        }
        util.scalePosition(g, GeneralConfig.borderSize, LevelConfig.scaleFactor, true);
        correctInvalidNodes(g);
        
        return g;
    }
    
    // prevent the generator from giving bad graphs, i.e., with duplicated node IDs
    public void correctInvalidNodes(Graph graph){
        HashMap<Point3, String> map = new HashMap<>();
        // first find the graph dimensions
        int width = 0;
        int height = 0;
        for(Node node : graph.getEachNode()){
            Point3 p = nodePointPosition(node);
            //System.out.println(node.getId()+": "+p.toString());
            width = (int) Math.max(width, p.x + ((double) node.getAttribute("width")/2));
            height = (int) Math.max(height, p.y + ((double) node.getAttribute("height")/2));
        }
        // then, verify if there is duplicated coords and fix it randomly
        for(int i = 0; i < graph.getNodeCount(); i++){
            Node node = graph.getNode(i);
            Point3 point = nodePointPosition(node);
            while(map.containsKey(point)){
                // in case of duplicated ID (in other words, duplicated coordinates), find a random one within the width x height dimension
                int minX = (int) Math.max(GeneralConfig.borderSize, point.x - (double) node.getAttribute("width"));
                int maxX = (int) Math.min(point.x + (double) node.getAttribute("width"), width - GeneralConfig.borderSize);
                point.x = rng.nextInt(maxX - minX) + minX;
                
                int minY = (int) Math.max(GeneralConfig.borderSize, point.y - (double) node.getAttribute("height"));
                int maxY = (int) Math.min(point.y + (double) node.getAttribute("height"), height - GeneralConfig.borderSize);
                point.y = rng.nextInt(maxY - minY) + minY;
            }
            map.put(point, node.getId());
            graph.getNode(i).setAttribute("xyz", point.x, point.y, point.z);
        }
    }
}
