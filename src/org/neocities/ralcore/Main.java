package org.neocities.ralcore;

import com.opencsv.CSVReaderHeaderAware;

import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import processing.core.PApplet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.opencsv.CSVReader;

public class Main extends PApplet {

    // Defining the scroll speed, changing how fast notes cross the screen.
    int scrollSpeed = 4;
    // Height offset for the rows.
    int rowOffsetH = 100;
    // Note square height. Also defines the gap between rows, to make it 0.
    int rowHeight = 50;
    // Millisecond leniency for a note to get missed...
    int leniencyMiss = 250;
    // ...and for a 40...
    int leniency40 = 175;
    // ...and for an 80.
    int leniency80 = 100;
    // The BPM of the chart, which will be defined in the chart's metadata.
    float bpm = 120;

    public class Note {
        float time;
        int key;
        int row;
        public void noteTick(){
            // Assuming for now that we know exactly what time the note should be hit on to not calculate it, and that the song starts when the program starts.
            // TODO: Not do those things.
            textSize(32);
            textAlign(CENTER, TOP);
            if (millis() - leniencyMiss > time) {
                // If the note is deemed old, destroy it.
                rows[row].remove(0);
            } else {
                // Drawing note
                // A - x-coord: position based on when note should be hit, multiplied by an arbitrary scalar of the scrollspeed, minus half the note width.
                // B - y-coord: the height of the note plus the row number times how high each note is.
                fill(255, 255, 255);
                rect((time - millis()) * scrollSpeed / 10 - 25, rowOffsetH+(row-1)*rowHeight, 50, 50);
                fill(0, 0, 0);
                text((char)key, (time - millis()) * scrollSpeed / 10, rowOffsetH+(row-1)*rowHeight);
            }
        }
        public Note(Float defTime, Integer defKey, Integer defRow) {
            time = defTime;
            key = defKey;
            row = defRow;
        }
    }

    public static void main(String[] args) {
        String[] processingArgs = new String[]{"MySketch"};
        Main mySketch = new Main();
        PApplet.runSketch(processingArgs, mySketch);
    }

    // Creating the array that will hold the ArrayList for each row
    ArrayList<Note>[] rows = new ArrayList[3];

    public void settings() {
        size(400, 400);
        smooth();
        // Setting up the array of ArrayLists that will store row data
        for (int i = 0; i < 3; i++) {
            rows[i] = new ArrayList<Note>();
        }
        // Importing chart info using simple-json, yonked from here: https://www.tutorialspoint.com/how-to-read-the-contents-of-a-json-file-using-java
        try {
            // Creating JSONParser object and setting up file
            FileReader chartJSON = new FileReader("Charts/1 HALLEY LABS R&D - catching up (first wip)/metadata.json");
            JSONParser jsonParser = new JSONParser();
            // Parsing JSON contents
            JSONObject jsonContents = (JSONObject) jsonParser.parse(chartJSON);
            // TODO: make this read title and artist and that kinda funky thing; for now we just read the BPM
            bpm = ((Double) jsonContents.get("bpm")).floatValue();
            chartJSON.close();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        // Import chart from file using OpenCSV
        // Setting up the list to hold the chart temporarily:
        List<String[]> importedChart = new ArrayList<String[]>();
        try {
            // Creating file and csv reader objects
            FileReader chartFile = new FileReader("Charts/1 HALLEY LABS R&D - catching up (first wip)/normal.csv");
            CSVReaderHeaderAware csv = new CSVReaderHeaderAware(chartFile);
            // Reading in file
            importedChart = csv.readAll();
            // TODO: potentially rework so that this reads line-by-line for efficiency
            // TODO: rework so that bpm changes per chart
            // Iterating through imported CSV, using data to create note objects in rows.
            for (int i = 0; i < importedChart.size(); i++) {
                rows[Integer.parseInt(importedChart.get(i)[2])].add(new Note(Float.parseFloat(importedChart.get(i)[0])*(60000/bpm), (int) importedChart.get(i)[1].charAt(0), Integer.parseInt(importedChart.get(i)[2])));
            }
            csv.close();
            chartFile.close();
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public void draw() {
        background(80);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < rows[i].size(); j++) {
                rows[i].get(j).noteTick();
            }
        }
    }

    public void checkNote() {
        // Checking which row the user is hitting on
        // If the user is hitting neither up or down, or both up or down, we want to hit centre row.
        int currentRow;
        if (!((keys.getOrDefault(UP, false) == true) ^ (keys.getOrDefault(DOWN, false) == true))) {
            currentRow = 1;
        } else if (keys.getOrDefault(UP, false) == true) {
            currentRow = 0;
        } else {
            currentRow = 2;
        }
        if (millis() > rows[currentRow].get(0).time - leniencyMiss && millis() < rows[currentRow].get(0).time + leniencyMiss) {
            // If the hit time is not outside the miss leniency, then we know the note is a hit.
            if (keyCode != rows[currentRow].get(0).key || millis() < rows[currentRow].get(0).time - leniency40 || millis() > rows[currentRow].get(0).time + leniency40) {
                // If the wrong key is hit, or the note is hit outside the 40 leniency, it's a miss.
                print("Miss!");
            } else if (millis() < rows[currentRow].get(0).time - leniency80 || millis() > rows[currentRow].get(0).time + leniency80) {
                // If the note is outside the 80 leniency, it's a 40.
                print("40!");
            } else {
                //Otherwise, it's an 80.
                print("80!");
            }
            rows[currentRow].remove(0);
        }
    }

    // This project uses a modified version of jeremydouglass' solution for checking when multiple keys are pressed or held at once.
    // It can be found here: https://discourse.processing.org/t/solution-for-repeating-keypressed-while-holding-key/3989/3

    // Within this HashMap, the key is the character, and the value is it's state.
    HashMap<Integer, Boolean> keys = new HashMap<Integer, Boolean>();

    // Sets the value in the HashMap to either:
    // false - Not Held
    // true - Held
    // These properties are used to a) prevent holding buttons from hitting notes and b) check UP/DOWN inputs to change rows

    public void keyPressed() {
        // Only set to true if the key is not blocked (preventing repeating keys without releasing them)
        if (!keys.getOrDefault(keyCode, false) && keyCode != UP && keyCode != DOWN) {
            checkNote();
        }
        keys.put(keyCode, true);
    }

    public void keyReleased() {
        keys.put(keyCode, false);
    }

}