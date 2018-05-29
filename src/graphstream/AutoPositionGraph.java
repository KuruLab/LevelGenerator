package graphstream;


import java.util.Random;
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.graph.implementations.DefaultGraph;
import static org.graphstream.algorithm.Toolkit.*;
import org.graphstream.graph.Node;
import org.graphstream.stream.file.FileSinkDGS;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andre
 */
public class AutoPositionGraph {
    
    public static int border = 5;
    
    public static void main(String[] args) throws Exception {
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        DefaultGraph g = new DefaultGraph("1");
        g.addAttribute("ui.antialias");
        g.addAttribute("ui.quality");
        g.addAttribute("ui.stylesheet", "url('media/stylesheet.css')");
        
        Layout layout = new SpringBox(false);
        g.addSink(layout);
        layout.addAttributeSink(g);
        
        BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator();
        gen.addSink(g);
        gen.begin();
        for (int i = 0; i < 10; i++) {
            gen.nextEvents();
        }
        gen.end();
        MyToolkit.normalizeNodesSizes(g, 5.0, 15.0);
        setupStyle(g);
        
        while(layout.getStabilization() < 0.95){
            layout.compute();
        }
        MyToolkit.scalePosition(g, 50, true);
        
        System.out.println("Position after layout stabilization");
        int maxX = 0;
        int maxY = 0;
        for(Node node : g.getEachNode()){
            Point3 p = nodePointPosition(node);
            System.out.println(node.getId()+": "+p.toString());
            maxX = (int) Math.max(maxX, p.x + ((double) node.getAttribute("width")/2));
            maxY = (int) Math.max(maxY, p.y + ((double) node.getAttribute("height")/2));
        }
        
        System.out.println("Node Area Overlap: "+MyToolkit.nodeAreaOverlap(g));
        JSONFileWriter fw = new JSONFileWriter(g);
        fw.exportDataJSON("D:\\MEGA\\posdoc\\MASGameBuilder2\\Assets\\StreamingAssets\\data_"+g.getId()+".json",
                "GraphStream", maxX, maxY);
        fw.exportMapJSON("D:\\MEGA\\posdoc\\MASGameBuilder2\\Assets\\StreamingAssets\\map_"+g.getId()+".json",
                BarabasiAlbertGenerator.class.getSimpleName());
        
        g.display(false);
    }
    
    public static void setupStyle(DefaultGraph graph) {
        Random random = new Random();
        for (Node node : graph) {
            node.addAttribute("ui.label", node.getId());
            node.setAttribute("ui.class", "normal");
            node.setAttribute("ui.color", 0.5);
        }
    }
}
