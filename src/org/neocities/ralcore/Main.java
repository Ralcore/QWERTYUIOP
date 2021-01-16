package org.neocities.ralcore;

import processing.core.PApplet;

public class Main extends PApplet {

    // Defining the scroll speed, changing how fast notes cross the screen.
    int scrollSpeed = 1;
    // Height offset for the rows.
    int rowOffsetH = 100;
    // Note square height. Also defines the gap between rows, to make it 0.

    public class Note {
        int time;
        int key;
        public void noteTick(){
            // Assuming for now that we know exactly what time the note should be hit on to not calculate it, and that the song starts when the program starts.
            // TODO: Not do those things.
            // Drawing note
            rect((time-millis())*scrollSpeed/10, rowOffsetH, 10, 50);
        }
        public Note(Integer defTime, Integer defKey) {
            time = defTime;
            key = defKey;
        }
    }

    public static void main(String[] args) {
        String[] processingArgs = new String[]{"MySketch"};
        Main mySketch = new Main();
        PApplet.runSketch(processingArgs, mySketch);
    }

    public void settings() {
        size(400, 400);
        //noStroke();
        smooth();
        myNote = new Note(2000, 0);
        myNote2 = new Note(2250, 1);
    }

    Note myNote, myNote2;

    public void draw() {
        background(80);
        for (int i = 0; i < keys.length; i++) {
            if (keys[i]) {
                rect(i*10, width/2, 10, 50);
            }
        }
    }



    // This project uses a modified version of GoToLoop's solution for checking when multiple keys are pressed or held at once.
    // It can be found here: http://studio.processingtogether.com/sp/pad/export/ro.91tcpPtI9LrXp

    // Within this array, elements 0-25 will be A-Z, whilst 26 will be UP and 27 will be down.
    boolean keys[] = new boolean[28];

    public void keyPressed() {
        setKeys(keyCode, true);
        checkNote()
    }

    public void keyReleased() {
        setKeys(keyCode, false);
    }

    // This is seriously the best processing-only solution I could find.
    // Sets array variable for each key depending on whether a key has been hit or released.
    boolean setKeys(int k, boolean b) {
        switch(k){
            case 'A':
                return keys[0] = b;
            case 'B':
                return keys[1] = b;
            case 'C':
                return keys[2] = b;
            case 'D':
                return keys[3] = b;
            case 'E':
                return keys[4] = b;
            case 'F':
                return keys[5] = b;
            case 'G':
                return keys[6] = b;
            case 'H':
                return keys[7] = b;
            case 'I':
                return keys[8] = b;
            case 'J':
                return keys[9] = b;
            case 'K':
                return keys[10] = b;
            case 'L':
                return keys[11] = b;
            case 'M':
                return keys[12] = b;
            case 'N':
                return keys[13] = b;
            case 'O':
                return keys[14] = b;
            case 'P':
                return keys[15] = b;
            case 'Q':
                return keys[16] = b;
            case 'R':
                return keys[17] = b;
            case 'S':
                return keys[18] = b;
            case 'T':
                return keys[19] = b;
            case 'U':
                return keys[20] = b;
            case 'V':
                return keys[21] = b;
            case 'W':
                return keys[22] = b;
            case 'X':
                return keys[23] = b;
            case 'Y':
                return keys[24] = b;
            case 'Z':
                return keys[25] = b;
            case UP:
                return keys[26] = b;
            case DOWN:
                return keys[27] = b;
            default:
                return b;
        }
    }

}