/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package experiments;

import evoGraph.Config;

/**
 *
 * @author andre
 */
public class OnlyMaximizeSizeExperiment extends Experiment{
    public static void main(String args[]){  
       OnlyMaximizeSizeExperiment exp = new OnlyMaximizeSizeExperiment();
       exp.run();
    }
    
    public OnlyMaximizeSizeExperiment() {
        super();
    }
    
    @Override
    public void setup(){
        super.setup();
        
        Config.useMaximizeNodeCount = true;
        Config.minNodeCount = 25;
        Config.maxNodeCount = 1000;
        Config.maxGen       = 1000;
        Config.useDesiredAngles = false;
        Config.useAverageShortestPath = false; 
        Config.useIdealNonLinearity = false; 
        Config.desiredAngles = new int[0];
        
        Config.useRefinement = false;
        Config.refinementProb =  0.0;
        
        Config.nodeXLeap = 1.1;
        Config.nodeYLeap = 1.1;
        Config.nodeZLeap = 0;
        
        Config.folder = "D:\\Mega\\posdoc\\MapGenerator\\experiments\\only_size_g1000\\";
    }
}
