package editor;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Data structure to store all the Text in the file
 */

public class LinkedListText {
    public class Node {
        public Text item;
        public Node next;
        public Node prev;

        public Node(Text i, Node p, Node n) {
            item = i;
            prev = p;
            next = n;

        }
    }

    private Node sFront;
    private Node sBack;
    private int size;
    public Node currPos = sFront;
    public int fontSize = 12;
    public String fontName = "Verdana";
    public int fontHeight = 15;
    public double Windowwidth;

    /**
     * Creates an empty list.
     */
    public LinkedListText() {
        size = 0;
        sFront = new Node(new Text(5, 0, ""), null, null);
        sBack = new Node(null, null, null);
        currPos = sFront;
    }

    public void addChar(Text x) {
        if (size == 0) {
            currPos.next = new Node(x, sFront, sBack);
            sBack.prev = currPos.next;
            sFront.next = currPos.next;
            currPos = currPos.next;
        } else {
            currPos.next.prev = new Node(x, currPos, currPos.next);
            currPos.next = currPos.next.prev;
            currPos = currPos.next;
        }
        size += 1;
    }

    public void addSpecificChar(Node toRemove, Text x) {
        if (size == 0) {
            toRemove.next = new Node(x, sFront, sBack);
            sBack.prev = currPos.next;
            toRemove.next = currPos.next;
            toRemove = toRemove.next;
        } else {
            toRemove.next.prev = new Node(x, toRemove.prev, toRemove.next);
            toRemove.next = toRemove.next.prev;
            toRemove = toRemove.next;
        }
        size += 1;
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    public Node getCurrPos() {
        return currPos;
    }

    public int size() {
        return size;
    }


    public Text removeChar() {
        if (isEmpty()) {
            return null;
        }
        Text itemRemoved = currPos.item;
        currPos.next.prev = currPos.prev;
        currPos.prev.next = currPos.next;
        currPos = currPos.prev;
        size -= 1;
        return itemRemoved;
    }

    public Text removeSpecificChar(Node toRemove) {
        if (isEmpty()) {
            return null;
        }
        Text itemRemoved = toRemove.item;
        toRemove.next.prev = toRemove.prev;
        toRemove.prev.next = toRemove.next;
        currPos = toRemove.prev;
        size -= 1;
        return itemRemoved;
    }

    public Text get(int index) {
        Node getReference = sFront.next;
        return helper(getReference, index);
    }

    public Node getFront(){
        return sFront;
    }


    private Text helper(Node x, int index) {
        if (index >= size) {
            return null;
        }
        if (index == 0) {
            return x.item;
        }
        return helper(x.next, index - 1);
    }

    public void Render(double UsableWindowWidth, ArrayLine LinesBuffer) {
        LinesBuffer.currLine = 0;
        double MarginX = 5;
        Node reference = sFront;
        Node lastNodeSpace = reference;
        double YCorSpace = -1;
        double xPoss = MarginX;
        double yPoss = 0;
        Text currRenderItem = get(0);
        if (currRenderItem != null) {
            while (reference != sBack && reference != null ) {
                currRenderItem = reference.item;
                currRenderItem.setFont(Font.font(fontName, fontSize));
                currRenderItem.setX(xPoss);
                if(xPoss == MarginX){
                    if(LinesBuffer.currLine < LinesBuffer.size()){
                        LinesBuffer.set(LinesBuffer.currLine, reference);
                    }
                    else{
                        LinesBuffer.add(LinesBuffer.currLine, reference);
                    }
                }
                currRenderItem.setY(yPoss);
                if(currRenderItem.getText().equals(" ")){
                    lastNodeSpace = reference;
                    YCorSpace = lastNodeSpace.item.getY();
                }
                if (currRenderItem.getText().equals("\r\n") || currRenderItem.getText().equals("\n")) {
                    xPoss = MarginX;
                    yPoss += Math.round(currRenderItem.getLayoutBounds().getHeight() / 2);
                    fontHeight = (int) Math.round(currRenderItem.getLayoutBounds().getHeight() / 2);
                    LinesBuffer.incrementcurrIndex();
                } else {
                    fontHeight = (int) Math.round(currRenderItem.getLayoutBounds().getHeight());
                    xPoss += Math.round(currRenderItem.getLayoutBounds().getWidth());
                }
                if(currRenderItem.getX() + currRenderItem.getLayoutBounds().getWidth() > UsableWindowWidth){
                    xPoss = 5;
                    yPoss += fontHeight;
                    if(YCorSpace == currRenderItem.getY()) {
                        reference = lastNodeSpace;
                    }
                    LinesBuffer.incrementcurrIndex();
                    if(LinesBuffer.currLine < LinesBuffer.size()){
                        LinesBuffer.set(LinesBuffer.currLine, reference);
                    }
                    else{
                        LinesBuffer.add(LinesBuffer.currLine, reference);
                    }
                }
                reference = reference.next;
            }
        }
        LinesBuffer.currLine = (int) Math.round(currPos.item.getY()/fontHeight);
        if(currPos.item.getText().equals("\r\n") || currPos.item.getText().equals("\n")){
            LinesBuffer.incrementcurrIndex();
        }
    }

    public Node findX(double pos, ArrayLine LinesBuffer, double cursorX) {
        Node reference = LinesBuffer.get(LinesBuffer.currLine).prev;
        double PrevXPosReference;
        if (reference.item != null && reference.item.getX() < pos) {
            return reference.prev;
        }
        if (cursorX == 5) {
            if(currPos.item.getText().equals("\n") || currPos.item.getText().equals("\r\n")){
                return LinesBuffer.get(LinesBuffer.currLine - 1).prev;
            }
            else if (currPos.item.getText().equals(" ")){
                return LinesBuffer.get(LinesBuffer.currLine).prev;
            }
        }
        reference = reference.prev;
        Node prevreference = reference.prev;
        while (prevreference.item != null) {
            Text PrevItem = prevreference.item;
            Text CurrItem = reference.item;
            if (prevreference == sFront) {
                return prevreference;
            }
            String characterTyped = PrevItem.getText();
            if (characterTyped.equals("\n") || characterTyped.equals("\r\n")) {
                PrevXPosReference = 5;
            }
            else {
                PrevXPosReference = Math.abs(PrevItem.getX() + PrevItem.getLayoutBounds().getWidth() - pos);
            }
            if (PrevXPosReference < (Math.abs(CurrItem.getX() + CurrItem.getLayoutBounds().getWidth() - pos))) {
                reference = reference.prev;
                prevreference = prevreference.prev;
            } else {
                return reference;
            }
        }
        return reference;
    }

    public Node findClick(double pos, ArrayLine LinesBuffer, int curr) {
        Node reference;
        if (curr < LinesBuffer.size() - 1){
            reference = LinesBuffer.get(curr+1).prev;
        }
        else{
            reference = sBack;
        }
        if (pos <= 5) {
            return LinesBuffer.get(curr).prev;
        }
        double PrevXPosReference;
        reference = reference.prev;
        Node prevreference = reference.prev;
        while (prevreference.item != null) {
            Text PrevItem = prevreference.item;
            Text CurrItem = reference.item;
            if (prevreference == sFront) {
                return prevreference;
            }
            String characterTyped = PrevItem.getText();
            if (characterTyped.equals("\n") || characterTyped.equals("\r\n")) {
                PrevXPosReference = 5;
            } else {
                PrevXPosReference = Math.abs(PrevItem.getX() + PrevItem.getLayoutBounds().getWidth() - pos);
            }
            if (PrevXPosReference < (Math.abs(CurrItem.getX() + CurrItem.getLayoutBounds().getWidth() - pos))) {
                reference = reference.prev;
                prevreference = prevreference.prev;
            } else {
                return reference;
            }
        }
        return reference;
    }


    public Node findXDown(double pos, ArrayLine LinesBuffer, double cursorX) {
        Node reference;
        if (LinesBuffer.currLine < LinesBuffer.size() - 2) {
            reference = LinesBuffer.get(LinesBuffer.currLine + 2).prev;
        } else {
            reference = sBack;
        }
        if (cursorX == 5 ) {
            if(currPos.item.getText().equals("\n") || currPos.item.getText().equals("\r\n")){
                return LinesBuffer.get(LinesBuffer.currLine + 1).prev;
            }
            else if(LinesBuffer.size() > LinesBuffer.currLine+1 && currPos == LinesBuffer.get(LinesBuffer.currLine+1).prev && currPos.item.getText().equals(" ")){
                if(LinesBuffer.size() > LinesBuffer.currLine + 2){
                    return LinesBuffer.get(LinesBuffer.currLine + 2).prev;
                }
            }
        }
        double PrevXPosReference;
        reference = reference.prev;
        Node prevreference = reference.prev;
        while (prevreference.item != null) {
            Text PrevItem = prevreference.item;
            Text CurrItem = reference.item;
            if (prevreference == sFront) {
                return prevreference;
            }
            String characterTyped = PrevItem.getText();
            if (characterTyped.equals("\n") || characterTyped.equals("\r\n") && PrevItem.getY() == currPos.item.getY()) {
                return reference;
            }
            if (characterTyped.equals("\n") || characterTyped.equals("\r\n")){
                PrevXPosReference = 5;
            }else {
                PrevXPosReference = Math.abs(PrevItem.getX() + PrevItem.getLayoutBounds().getWidth() - pos);
            }
            if (PrevXPosReference < (Math.abs(CurrItem.getX() + CurrItem.getLayoutBounds().getWidth() - pos))) {
                reference = reference.prev;
                prevreference = prevreference.prev;
            } else {
                return reference;
            }
        }
        return reference;
    }


}


