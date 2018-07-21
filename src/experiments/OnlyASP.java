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
public class OnlyASP extends StandardLevelExperiment{
    public static void main(String args[]){  
       OnlyASP exp = new OnlyASP();
       exp.run();
    }
    
    public OnlyASP() {
        super();
    }
    
    @Override
    public void setup(){
        super.setup();
        
        LevelConfig.useMaximizeNodeCount = false;
        LevelConfig.minNodeCount = 25;
        LevelConfig.maxNodeCount = 50;
        
        LevelConfig.useDesiredAngles = false;
        LevelConfig.useAverageShortestPath = true; 
        LevelConfig.desiredAngles = new int[0]; 
        
        LevelConfig.folder = "..\\data\\experiments\\only_asp\\";
    }
}
