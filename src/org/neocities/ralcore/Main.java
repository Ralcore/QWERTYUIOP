package org.neocities.ralcore;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.HashMap;

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
    int leniency40 = 75;
    // ...and for an 80.
    int leniency80 = 25;

    public class Note {
        int time;
        int key;
        int row;
        public void noteTick(){
            // Assuming for now that we know exactly what time the note should be hit on to not calculate it, and that the song starts when the program starts.
            // TODO: Not do those things.
            if (millis() - leniencyMiss > time) {
                // If the note is deemed old, destroy it.
                rows[row].remove(0);
            } else {
                // Drawing note
                rect((time - millis()) * scrollSpeed / 10, rowOffsetH+(row-1)*rowHeight, 10, 50);
            }
        }
        public Note(Integer defTime, Integer defKey, Integer defRow) {
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
        // Temporary hard-coded chart code
        rows[0].add(new Note(2500, (int) 'Z', 0));
        rows[0].add(new Note(2750, (int) 'X', 0));
        rows[2].add(new Note(3000, (int) 'Z', 2));
        rows[2].add(new Note(3250, (int) 'X', 2));
        rows[0].add(new Note(3500, (int) 'Z', 0));
        rows[0].add(new Note(3750, (int) 'X', 0));
        rows[2].add(new Note(4000, (int) 'Z', 2));
        rows[2].add(new Note(4250, (int) 'X', 2));

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