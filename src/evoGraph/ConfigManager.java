/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoGraph;

import evoGraph.Config;
import evoGraph.EvoJSONFileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author andre
 */
public class ConfigManager {
    
    public void saveRunTime(String name){        
        String pathStr = name.replace("/", File.separator).replace("\\", File.separator);
        String folderStr = Config.folder.replace("/", File.separator).replace("\\", File.separator);
        Integer borderS = Config.borderSize;
        Double edgeS = Config.edgeSize;
        Integer minNC = Config.minNodeCount;
        Integer maxNC = Config.maxNodeCount;
        Double minNS = Config.minNodeSize;
        Double maxNS = Config.maxNodeSize;
        Double minED = Config.minEdgeDistance;
        Double bFactor = Config.barabasiFactor;
        Integer links = Config.maxLinksPerStep;
        Integer nip = Config.areaIntersectionPenalty;
        Integer eip = Config.edgeIntersectionPenalty;
        Integer ncp = Config.nodeCountPenalty;
        Integer edp = Config.edgeDistancePentalty;
        Double crxP = Config.crossoverProb;
        Double mutP = Config.mutationProb;
        Double refP = Config.refinementProb;
        Double xL = Config.nodeXLeap;
        Double yL = Config.nodeYLeap;
        Double zL = Config.nodeZLeap;
        Integer pop = Config.popSize;
        Integer gen = Config.maxGen;
        Boolean useDA = Config.useDesiredAngles;
        Boolean useAvgSP = Config.useAverageShortestPath;
        Boolean useMNC = Config.useMaximizeNodeCount;
        Boolean useINL = Config.useIdealNonLinearity;
        int[] angles = Config.desiredAngles;
        Integer iNL = Config.idealNonLinearity;
        saveConfig(pathStr,
                folderStr,
                borderS, edgeS,
                minNC, maxNC, minNS, maxNS,
                minED, bFactor, links,
                nip, eip, ncp, edp,
                crxP, mutP, refP,
                xL, yL, zL,
                pop, gen,
                useDA, useAvgSP, useMNC, useINL,
                angles, iNL);
    }
    
    public void saveConfig(String path,
            String folder,
            Integer borderS, Double edgeS,
            Integer minNC, Integer maxNC, Double minNS, Double maxNS,
            Double minED, Double bFactor, Integer links,
            Integer nip, Integer eip, Integer ncp, Integer edp,
            Double crxP, Double mutP, Double refP,
            Double xL, Double yL, Double zL,
            Integer pop, Integer gen,
            Boolean useDA, Boolean useASP, Boolean useMNC, Boolean useINL,
            int[] angles, Integer iNL)
    {
        PrintWriter pw = null;
        try {
            JSONObject json = new JSONObject();
            json.put("folder", folder.replace("/", File.separator).replace("\\", File.separator));
            json.put("borderSize", borderS);
            json.put("edgeSize", edgeS);
            
            json.put("minNodeCount", minNC);
            json.put("maxNodeCount", maxNC);
            json.put("minNodeSize", minNS);
            json.put("maxNodeSize", maxNS);
            
            json.put("minEdgeDistance", minED);
            
            json.put("barabasiFactor", bFactor);
            json.put("maxLinksPerStep", links);
            
            json.put("areaIntersectionPenalty", nip);
            json.put("edgeIntersectionPenalty", eip);
            json.put("nodeCountPenalty", ncp);
            json.put("edgeDistancePentalty", edp);
            
            json.put("crossoverProb", crxP);
            json.put("mutationProb", mutP);
            json.put("refinementProb", refP);
            
            json.put("nodeXLeap", xL);
            json.put("nodeYLeap", yL);
            json.put("nodeZLeap", zL);
            
            json.put("popSize", pop);
            json.put("maxGen", gen);
            
            json.put("useDesiredAngles", useDA);
            json.put("useAverageShortestPath", useASP);
            json.put("useIdealNonLinearity", useINL);
            json.put("useMaximizeNodeCount", useMNC);
            
            JSONArray array = new JSONArray();
            for(int i = 0; i < angles.length; i++)
                array.add(angles[i]);
            json.put("desiredAngles", array);
            json.put("idealNonLinearity", iNL);
            
            // non-user defined
            json.put("scaleFactor", 50);
            json.put("expansionProb", 0.05);
            
            File file = new File(path);
            pw = new PrintWriter(file);
            json.writeJSONString(pw);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }
    }
    
    public void loadConfig(String path){
        try {
            EvoJSONFileReader reader = new EvoJSONFileReader(path);
            String raw = reader.readRawString();
            
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(raw);
            
            Config.folder = ((String) json.get("folder")).replace("/", File.separator).replace("\\", File.separator);
            Config.borderSize = (int)(long) json.get("borderSize");
            Config.edgeSize = (double) json.get("edgeSize");
            Config.minNodeCount = (int) (long) json.get("minNodeCount");
            Config.maxNodeCount = (int) (long) json.get("maxNodeCount");
            Config.minNodeSize = (double) json.get("minNodeSize");
            Config.maxNodeSize = (double) json.get("maxNodeSize");
            Config.minEdgeDistance = (double) json.get("minEdgeDistance");
            Config.barabasiFactor = (double) json.get("barabasiFactor");
            Config.maxLinksPerStep = (int) (long) json.get("maxLinksPerStep");
            Config.areaIntersectionPenalty = (int)(long) json.get("areaIntersectionPenalty");
            Config.edgeIntersectionPenalty = (int)(long) json.get("edgeIntersectionPenalty");
            Config.nodeCountPenalty = (int)(long) json.get("nodeCountPenalty");
            Config.edgeDistancePentalty = (int)(long) json.get("edgeDistancePentalty");
            Config.crossoverProb = (double) json.get("crossoverProb");
            Config.mutationProb = (double) json.get("mutationProb");
            Config.refinementProb = (double) json.get("refinementProb");
            Config.nodeXLeap = (double) json.get("nodeXLeap");
            Config.nodeYLeap = (double) json.get("nodeYLeap");
            Config.nodeZLeap = (double) json.get("nodeZLeap");
            Config.popSize = (int)(long) json.get("popSize");
            Config.maxGen = (int)(long) json.get("maxGen");
            Config.useDesiredAngles = (boolean) json.get("useDesiredAngles");
            Config.useAverageShortestPath = (boolean) json.get("useAverageShortestPath");
            Config.useIdealNonLinearity = (boolean) json.get("useIdealNonLinearity");
            Config.useMaximizeNodeCount = (boolean) json.get("useMaximizeNodeCount");
            
            JSONArray array = (JSONArray) json.get("desiredAngles");
            int[] angles = new int[array.size()];
            for(int i = 0; i < angles.length; i++){
                angles[i] = (int)(long) array.get(i);
            }
            Config.desiredAngles = angles;
            Config.idealNonLinearity = (int)(long) json.get("idealNonLinearity");
        } catch (ParseException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
}
