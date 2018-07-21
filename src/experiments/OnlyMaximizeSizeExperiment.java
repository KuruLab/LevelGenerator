/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package experiments;

import evoLevel.LevelConfig;

/**
 *
 * @author andre
 */
public class OnlyMaximizeSizeExperiment extends StandardLevelExperiment{
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
        
        LevelConfig.useMaximizeNodeCount = true;
        LevelConfig.minNodeCount = 25;
        LevelConfig.maxNodeCount = 1000;
        LevelConfig.maxGen       = 1000;
        LevelConfig.useDesiredAngles = false;
        LevelConfig.useAverageShortestPath = false; 
        //LevelConfig.useIdealNonLinearity = false; 
        LevelConfig.desiredAngles = new int[0];
        
        LevelConfig.useRefinement = false;
        LevelConfig.refinementProb =  0.0;
        
        LevelConfig.nodeXLeap = 1.1;
        LevelConfig.nodeYLeap = 1.1;
        LevelConfig.nodeZLeap = 0;
        
        LevelConfig.folder = "..\\data\\experiments\\only_size_g1000\\";
    }
}
