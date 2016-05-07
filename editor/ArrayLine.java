package editor;

import java.util.ArrayList;

/**
 * Created by aakashbhalothia on 2/29/16.
 * Every index position points to the Node of LinkedListText which is the first node of a new line.
 */

public class ArrayLine extends ArrayList<LinkedListText.Node> {

    public int currLine;
    public int marginX = 5;
    public int Mysize = 0;


    public ArrayLine(int capacity) {
        super(capacity);
        currLine = 0;
    }


/*    public void addLine() {
        size += 1;
        if (sBack == sFront) {
            if (size == capacity) {
                if (sFront == 0) {
                    sFront = capacity;
                }
                sFront -= 1;
                resize(capacity * RFACTOR);
                return;
            } else {
                if (sBack == length) {
                    sBack = -1;
                }
                sBack += 1;
            }
        }
        if (sFront == 0) {
            sFront = capacity;
        }
        sFront -= 1;
        currIndex += 1;
        Lines[currIndex] = new LinkedListText();
    }*/

    @Override
    public void add(int index, LinkedListText.Node T) {
        super.add(index, T);
        Mysize += 1;
        currLine = index;
    }
    

/*    public void printDeque() {
        int start = sFront + 1;
        while (start != sBack) {
            if (start == capacity) {
                start = 0;
            }
            System.out.print(Lines[start] + " ");
            start += 1;
            if (start == capacity) {
                start = 0;
            }

        }
    }*/

    public void removeLine(int index) {
        super.remove(index);
        Mysize  -=1;
    }

    public void incrementcurrIndex() {
        currLine += 1;
    }

    public void decrementcurrIndex() {
        if (currLine>0) {
            currLine -= 1;
        }
    }
}

