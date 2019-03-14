package fr.testappli.googlemapapi;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class MapResult {
    private List<List<HashMap<String, String>>> routes;
    private JSONArray jLegs;
    private JSONArray jSteps;
    private ArrayList<String> sManeuver;

    MapResult(MapResult newMapResult){
        setRoutes(newMapResult.routes);
        setLegs(newMapResult.jLegs);
        setSteps(newMapResult.jSteps);
        setManeuver(newMapResult.sManeuver);
    }



    MapResult(List<List<HashMap<String, String>>> routes, JSONArray jLegs, JSONArray jSteps, ArrayList<String> sManeuver){
        setRoutes(routes);
        setLegs(jLegs);
        setSteps(jSteps);
        setManeuver(sManeuver);
    }

    public List<List<HashMap<String, String>>> getRoutes(){
        return this.routes;
    }

    public JSONArray getLegs(){
        return this.jLegs;
    }

    public JSONArray getSteps(){
        return this.jSteps;
    }

    public ArrayList<String> getManeuver(){
        return this.sManeuver;
    }

    private void setRoutes(List<List<HashMap<String, String>>> newRoutes){
        this.routes = newRoutes;
    }

    private void setLegs(JSONArray newLegs){
        this.jLegs = newLegs;
    }

    private void setSteps(JSONArray newSteps){
        this.jSteps = newSteps;
    }

    private void setManeuver(ArrayList<String> newManeuver){
        this.sManeuver = newManeuver;
    }
}
