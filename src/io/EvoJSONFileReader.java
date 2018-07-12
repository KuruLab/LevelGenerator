/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import puzzle.Condition;
import puzzle.Symbol;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author andre
 */
public class EvoJSONFileReader {
    
    private String filename;
    
    public EvoJSONFileReader(String _filename){
        this.filename = _filename;
    }
    
    public String readRawString(){
        String jsonString = new String();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            File file = new File(this.filename);
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line = new String();
            try {
                do{
                    line = br.readLine();
                    if(line != null){
                        jsonString += line;
                    }
                } while(line != null);
            } catch (IOException ex) {
                Logger.getLogger(EvoJSONFileReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EvoJSONFileReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(EvoJSONFileReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jsonString;
    }
        
    public Graph parseJson(){
        String graphID = "-1";
        DefaultGraph graph = null;
        try {
            String rawJSon = readRawString();
            //System.out.println(rawJSon);
            JSONParser parser = new JSONParser(); 
            JSONObject obj = (JSONObject) parser.parse(rawJSon);
            
            graphID = (String) obj.get("graph");
            
            JSONArray nodeArray = (JSONArray) obj.get("nodes");
            
            graph = new DefaultGraph(graphID);
            graph.addAttribute("ui.stylesheet", "url('media/stylesheet.css')");
            graph.addAttribute("ui.antialias");
            graph.addAttribute("ui.quality");
            
            for(int i = 0; i < nodeArray.size(); i++){
                JSONObject nodeObj = (JSONObject) nodeArray.get(i);
                String nodeID = (String) nodeObj.get("id");
                Double width = (Double) nodeObj.get("width");
                Double height = (Double) nodeObj.get("height");
                Double itnsty = (Double) nodeObj.get("intensity");
                String symbol = (String) nodeObj.get("symbol");
                String cond = (String) nodeObj.get("condition");
                JSONArray pos = (JSONArray) nodeObj.get("position");
                
                Double x = (Double) pos.get(0);
                Double y = (Double) pos.get(1);
                Double z = (Double) pos.get(2);
                
                graph.addNode(nodeID);
                graph.getNode(i).addAttribute("width",  width.doubleValue());
                graph.getNode(i).addAttribute("height", height.doubleValue());
                graph.getNode(i).addAttribute("xyz", x.doubleValue(), y.doubleValue(), z.doubleValue());
                graph.getNode(i).addAttribute("ui.color", itnsty);
                
                Symbol sym = new Symbol(symbol);
                Condition con = new Condition(new Symbol(cond));
                
                graph.getNode(i).addAttribute("symbol", sym);
                graph.getNode(i).addAttribute("condition", con);
            }
            
            JSONArray edgeArray = (JSONArray) obj.get("edges");
            for(int i = 0; i < edgeArray.size(); i++){
                JSONObject edgeObj = (JSONObject) edgeArray.get(i);
                String edgeID = (String) edgeObj.get("id");
                String from = (String) edgeObj.get("a");
                String to = (String) edgeObj.get("b");
                String symbol = (String) edgeObj.get("symbol");
                
                graph.addEdge(edgeID, to, from);
                
                Symbol sym = new Symbol(symbol);
                graph.getEdge(i).addAttribute("symbol", sym);
            }
        } catch (ParseException ex) {
            Logger.getLogger(EvoJSONFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return graph;
    }
}
