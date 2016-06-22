import controlP5.*;
import processing.core.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Frank Verhagen on 25-4-2016.
 * In this class the interactive Zadkine map is drawn from the data gotten in DrawZadkineMap.
 */
public class DrawZadkineMap extends PApplet {

    // These four variables represent the 'box' the points are within. The declared values are the ones for the 1000 map
    private float NORTH =51.922134f;
    private float EAST =4.488572f;
    private float SOUTH =51.913155f;
    private float WEST =4.477501f;

    //T he width of the map. Is completely responsive to the whole visualisation.
    // Although it is not a good plan to make the width smaller than 600, since the buttons won't be fully visible in that case.
    private int WIDTH = 800;
    private int HEIGHT = 800;

    // For the buttons I'm using the Processing library ControlP5
    private ControlP5 cp5;

    // Integer used to determine per how many frames the waterlevel should increase. The higher this value is, the slower the water rises
    private int frames = 1;

    // If the firstRun is true, the code will draw every point. If false, it only updates the water level.
    private boolean firstRun = true;

    // In this two lists the coordinates of the maps are stored as a Vector. One list for the small map and one list for the big one.
    private ArrayList<PVector> mappings500;
    private ArrayList<PVector> mappings1000;

    // The water level starts at -10 NAP.
    private float waterLevel = -10f;
    // Size of the increment steps of the water level
    private float raiseWater = 0.1f;

    //If the map is 1000x1000m this is true. For the 500x500 map it will be false
    private boolean big = true;

    //If this is false the flooding animation will be paused
    private boolean playing = false;

    //If this is true the waterlevel will increment every frame. If false it will increment each 8 frames
    private boolean fast = true;

    //Checks the previous water level. This is needed for the updating drawer so it only draws water that wasn't already in drawn since the last increment
    private float previousWaterLevel=  waterLevel - 0.1f;

    //If the size is changed this boolean ensures all the water will be redrawn
    private boolean sizeChanged = false;

    public static void main(String args[]) {
       PApplet.main(new String[]{DrawZadkineMap.class.getName()});

    }
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public void setup() {
        frameRate(200); //Framerate will be very high. Essential for a smooth user experience
        background(255, 255, 255); //Make the background white
        mappings500 = loadData("Zadkinecoordinates500m.csv", 500); //Loads and parses the csv data to the arraylist, with the parameter the map will have a size of 500x500m
        mappings1000 = loadData("Zadkinecoordinates1000m.csv", 1000); // Idem dito, but now 1000x1000m
        drawButtons(); // Calls a method which draws all the ControlP5 buttons
    }

    public void draw() {
        if(big) { // Check which map has to be drawn
            createMap(mappings1000);
        } else {
            createMap(mappings500);
        }

        // Overlay the rest of the rest of the screen with white, so the graph has a smooth line around itself.
        noStroke();
        fill(255, 255, 255);
        rect(0, 0, 100, HEIGHT);
        rect(WIDTH-100, 100, WIDTH, HEIGHT);
        rect(100, HEIGHT-100, WIDTH, HEIGHT-100);
        rect(100, 30, WIDTH, 70);

        //Increase waterlevel each [value of frame] frames
        if (frameCount % frames == 0) {
            if(playing&& waterLevel <103) { // When the waterlevel reaches 103 it stops increasing since everything is under water by then.
                //Increase water level by 10 centimeters
                fill(255, 255, 255);
                waterLevel = waterLevel + raiseWater;
                previousWaterLevel = waterLevel -0.1f;
            }

            //White rectangle for behind the waterlevel text
            fill(255, 255, 255);
            rect(HEIGHT/2-150, 0, 400, 30);

            //Show current NAP waterlevel
            fill(0, 0, 0);
            textSize(20);
            text("Waterniveau: " + String.format("%.1f", waterLevel) + "m NAP", HEIGHT/2-140, 23);

        }

    }

    private ArrayList<PVector> loadData(String filename, int size) { // Method to retrieve the data from a csv file and assign the mapped Vectors to a list.
        ArrayList<PVector> mappedVectors = new ArrayList<PVector>();

        try {
            float MAX_X = 0f;
            float MIN_X = 0f;
            float MAX_Y = 0f;
            float MIN_Y = 0f;
            float MIN_Z = -16;
            float MAX_Z = 215;

            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            fill(0);  // Use color variable 'c' as fill color
            noStroke();  // Don't draw a stroke around shapes

            if(size == 500){
                setMapSize(500);
            } else {
                setMapSize(1000);
            }
            while ((strLine = br.readLine()) != null) {

                String[] tokens = strLine.split(",");

                try {
                    // try to parse the whole row. Only succeeds if all values are correct floats
                    float x = Float.parseFloat(tokens[1]);
                    float  y = Float.parseFloat(tokens[0]);
                    float z = Float.parseFloat(tokens[2]);

                    if(MAX_Y < y){ // Find the lowest and highest values
                        MAX_Y = y;
                    }
                    if(MAX_X < x){
                        MAX_X = x;
                    }
                    if(MIN_Y > y){
                        MIN_Y = y;
                    }
                    if(MIN_X > x){
                        MIN_X = x;
                    }

                    float mapX = map(lon_to_float(x), 0, HEIGHT, 100, HEIGHT-100);      // Convert the longitude to X axis
                    float mapY = map(lat_to_float(y),0, WIDTH, 100, WIDTH-100);         //Convert the latitude to Y axis
                    float mapZ = map(z, MIN_Z, MAX_Z, 0, 216);                          //Mapping Z
                    PVector mappedVector = new PVector(mapX, mapY, mapZ);               //PVector holding all mapped values
                    mappedVectors.add(mappedVector);
                } catch (NumberFormatException e) {
                }
            }
            in.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return mappedVectors;
    }

    private void createMap(ArrayList<PVector> mappings) { // Draw map from the list with vectors
        if (firstRun) {
            for (PVector mapping : mappings) {
                float mapX = mapping.x;
                float mapY = mapping.y;
                float mapZ = mapping.z;

                if (mapZ < 21.5f) {
                    //Color of ground, cars, roads
                    stroke(color(227, 227, 227));
                    fill(color(227, 227, 227));
                } else if (mapZ < 31.5f) {
                    //Middle color
                    stroke(color(213, 123, 123));
                    fill(color(213, 123, 123));
                }
                else {
                        //Color of top of building
                        stroke(color(136, 19, 19));
                        fill(color(136, 19, 19));
                    }

                rect(mapX, mapY, 5f, 5f); //create a rectangle at the point of mapped vector.

            }

            firstRun = false;  //Set firstRun to false to create water

        } else if (!firstRun) {

            for (PVector mapping : mappings) {
                float mapX = mapping.x;
                float mapY = mapping.y;
                float mapZ = mapping.z-12.31625f; ///Fix to match the Z values with NAP
                int waterColor = color(66, 133, 244); //Color of water

                if (waterLevel > mapZ && mapZ > previousWaterLevel) {  //Draw all the new water since last increase of waterLevel
                    stroke(waterColor);
                    fill(waterColor);
                    rect(mapX, mapY, 5f, 5f);            //Creat the water on the points under the waterLevel variable
                }

                if(sizeChanged){ //Redraw all water because of the size change
                    if (waterLevel > mapZ) {
                        stroke(waterColor);
                        fill(waterColor);
                        rect(mapX, mapY, 5f, 5f);            //create ellipse at points of mapped x
                    }
                }
            }
            sizeChanged= false;
        }
    }

    private float lat_to_float(float lat) { //Converts latitude to Y
        int i_lat = interpolate(0, HEIGHT, NORTH, SOUTH, lat);
        return i_lat;
    }

    private float lon_to_float(float lon) {//Converts longitude to X
        int i_lon = interpolate(0, WIDTH, WEST, EAST, lon);
        return i_lon;
    }

    private int interpolate(float lo_to, float hi_to, float lo_from, float hi_from, float current) {
        return round( lo_to + (current-lo_from) * (hi_to-lo_to)/(hi_from-lo_from));
    }

    private void setMapSize(int size) { // Method to set the map size.
        float centerlat = 51.91764310494602f; // These are the exact LatLon coordinates of the Zadkine statue.
        float centerlon = 4.483036283475881f; //
        double latdistance;
        float londistance;
        if(size == 1000) {
            latdistance = 0.002245780; // 500m is ~0.002245780 degrees
            londistance = 0.5f;
        } else {
            latdistance = 0.002245780/2; // 250m is 0.002245780 / 2 degrees
            londistance = 0.25f;
        }
        NORTH = (float) (centerlat + (latdistance * 2));  // Calculating the north and south latitudes, depending on the latdistance
        SOUTH = (float) (centerlat - (latdistance * 2));
        WEST = (float) (centerlon + (londistance * (360 / (Math.cos(centerlat) * 400075.16)))); // Calculating the longitude depending on the latitude and the circumference of the earth
        EAST = (float) (centerlon - (londistance * (360 / (Math.cos(centerlat) * 400075.16)))); //
    }

    private void drawButtons() { // Void that draws all the buttons: Play/Pause, Reset, Resize, Speed and save as JPG
        cp5 = new ControlP5(this);
        // Creates the play/pause button
        cp5.addButton("Play")
                .setValue(0)
                .setPosition(100, 50)
                .setSize(70, 30)
                .addCallback(event -> {
                            if (event.getAction() == ControlP5.ACTION_PRESSED) {

                                if (playing) {
                                    playing = false;
                                    event.getController().setLabel("Play");
                                } else {
                                    playing = true;
                                    event.getController().setLabel("Pause");
                                }

                            }

                        }
                );


        // Creates the size changing button
        cp5.addButton("1000")
                .setValue(0)
                .setPosition(180, 50)
                .setSize(70, 30)
                .addCallback(event -> {
                            if (event.getAction() == ControlP5.ACTION_PRESSED) {
                                if (big) {
                                    big = false;

                                    firstRun = true;
                                    background(255);
                                    createMap(mappings500);
                                    event.getController().setLabel("500");
                                    sizeChanged=true;

                                } else {
                                    big = true;

                                    firstRun = true;
                                    background(255);
                                    createMap(mappings1000);
                                    event.getController().setLabel("1000");
                                    sizeChanged=true;

                                }


                            }

                        }
                )
        ;

        // Creates the speed button
        cp5.addButton("Fast")
                .setValue(0)
                .setPosition(260, 50)
                .setSize(70, 30)
                .addCallback(event -> {
                            if (event.getAction() == ControlP5.ACTION_PRESSED) {

                                if (fast) {
                                    fast = false;
                                    frames = 25;
                                    event.getController().setLabel("Slow");
                                } else {
                                    fast = true;
                                    frames = 2;
                                    event.getController().setLabel("Fast");
                                }

                            }

                        }
                )
        ;
        // Creates the reset button
        cp5.addButton("Reset")
                .setValue(0)
                .setPosition(340, 50)
                .setSize(70, 30)
                .addCallback(event -> {
                            if (event.getAction() == ControlP5.ACTION_PRESSED) {
                                background(255);
                                waterLevel = -10f;
                                firstRun = true;

                            }

                        }
                )
        ;

        // Creates the save to jpg button
        cp5.addButton("Save as JPG")
                .setValue(0)
                .setPosition(500, 50)
                .setSize(70, 30)
                .addCallback(event -> {
                            if (event.getAction() == ControlP5.ACTION_PRESSED) {
                                if (big) {
                                    saveFrame("screenshots/Graph_1000_Waterlevel_" + waterLevel + ".jpg");

                                } else {
                                    saveFrame("screenshots/Graph_500_WaterLevel_" + waterLevel + ".jpg");
                                }
                            }
                        }
                )
        ;

        // Creates the stop button
        cp5.addButton("Stop")
                .setValue(0)
                .setPosition(420, 50)
                .setSize(70, 30)
                .addCallback(event -> {
                            if (event.getAction() == ControlP5.ACTION_PRESSED) {
                                background(255);
                                waterLevel = -10f;
                                firstRun = true;
                                playing = false;
                                cp5.getController("Play").setLabel("Play");
                            }
                        }
                )
        ;
    }

}
