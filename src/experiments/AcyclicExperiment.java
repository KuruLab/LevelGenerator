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
public class AcyclicExperiment extends Experiment{
    
    public static void main(String args[]){  
       AcyclicExperiment exp = new AcyclicExperiment();
       exp.run();
    }
    
    public AcyclicExperiment() {
        super();
    }
    
    @Override
    public void setup(){
        super.setup();
        
        Config.maxLinksPerStep = 1; // m
        Config.folder = "D:\\Mega\\posdoc\\MapGenerator\\experiments\\acyclic\\";
    }
    
}
