/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import config.GeneralConfig;
import evoLevel.LevelConfig;
import evoLevel.LevelDecoder;
import evoLevel.LevelIndividual;
import evoLevel.RandomLevelGenerator;
import image.MapImageBuilder;
import io.LevelFileWriter;
import java.io.File;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.geom.Point3;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePointPosition;

/**
 *
 * @author andre
 */
public class RandomGraphGeneratorTest {
    
    public static void main(String[] args){
        RandomLevelGenerator rgg = new RandomLevelGenerator();
        
        Graph g1 = rgg.barabasiAlbert("random");
        //g1.display(false);
        
        LevelDecoder encoder = new LevelDecoder(g1);
        LevelIndividual individual = encoder.encode();
        //System.out.println("encode time: "+(encoder.getTime()/1000000000.0)+"s");
        
        LevelDecoder decoder = new LevelDecoder(individual);
        DefaultGraph g2 = decoder.decode();
        //System.out.println("decode time: "+(decoder.getTime()/1000000000.0)+"s");
        
        g2.display(false);
        exportGraph(g2, "random");
        //DefaultGraph g2 = rgg.barabasiAlbert("g2");
        //g2.display(true);
    }
    
    public static void exportGraph(Graph g, String name){
        File dir = new File(LevelConfig.folder, name);
        /*if(!dir.exists()){
            dir.mkdir();
        }
        else{
            System.out.println("Dir "+dir+" already exists!");
        }*/
        int maxX = 0;
        int maxY = 0;
        for(Node node : g.getEachNode()){
            Point3 p = nodePointPosition(node);
            //System.out.println(node.getId()+": "+p.toString());
            maxX = (int) Math.max(maxX, p.x + ((double) node.getAttribute("width")/2));
            maxY = (int) Math.max(maxY, p.y + ((double) node.getAttribute("height")/2));
        }
        LevelFileWriter fw = new LevelFileWriter(g, LevelConfig.folder, name);
        System.out.println("Exporting Map\n"+dir+"\\level_"+name+".json");

        fw.exportMapJSON("Evolutionary BarabasiAlbert Generator",
                maxX, maxY, GeneralConfig.borderSize, false);
        
        // export PNG
        MapImageBuilder gib = new MapImageBuilder(dir+"\\img_"+name+".png");
        gib.buildImage(g);
        
        //LevelConfigManager cm = new LevelConfigManager();
        //cm.saveRunTime(dir+"\\config_"+hashString+".json");  
    }
}
