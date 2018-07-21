/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoLevel.LevelConfig;
import io.LevelFileReader;
import util.GraphStreamUtil;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 *
 * @author andre
 */
public class JSONReaderTest {
    
    public static void main(String args[]){
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        
        //String path = "D:\\Mega\\posdoc\\MapGenerator\\experiments\\stantard_pm0.005\\32373742\\map_32373742.json";
        LevelFileReader reader = new LevelFileReader(LevelConfig.folder, "test");
        Graph graph = reader.parseJsonToGraph();
        
        GraphStreamUtil util = new GraphStreamUtil();
        util.normalizeNodesSizes(graph, LevelConfig.minNodeSize, LevelConfig.maxNodeSize);
        util.setupStyle((DefaultGraph) graph);
        
        graph.display(false);
    }
    
}
