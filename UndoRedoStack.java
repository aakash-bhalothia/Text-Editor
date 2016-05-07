package editor;

import java.util.Stack;
/**
 * Created by aakashbhalothia on 3/7/16.
 */
public class UndoRedoStack extends Stack {
    /*public class Node {
        public Text item;
        public DrawCursor cursor;
        public boolean insert;

        public Node(Text i, DrawCursor p, Boolean n) {
            item = i;
            cursor = p;
            insert = n;
        }
    }*/

    public Stack undoStack;
    public Stack redoStack;
    private Editor editor;

    public UndoRedoStack(Editor e) {
        undoStack = new Stack();
        redoStack = new Stack();
        editor = e;
    }

    public void undo(){
        if (undoStack.empty()){
            return;
        }
        if (undoStack.size() > 100){
            undoStack.remove(0);
        }
        action action = (action) undoStack.pop();
        if(action.insert){
            editor.backSpace(action.node);
        }
        else {
            if(action.node.item.getText().equals("\r") || action.node.item.getText().equals("\r\n")){
                editor.UndoRedoEnter(action.node);
            }
            else {
                editor.UndoRedoNormalChar(action.node);
            }
        }
        redoStack.push(action);
    }

    public void redo(){
        if (redoStack.empty()){
            return;
        }
        if (redoStack.size() > 100){
            redoStack.remove(0);
        }
        action action = (action) redoStack.pop();
        if(!action.insert){
            editor.backSpace(action.node);
        }
        else {
            if(action.node.item.getText().equals("\r") || action.node.item.getText().equals("\r\n")){
                editor.UndoRedoEnter(action.node);
            }
            else {
                editor.UndoRedoNormalChar(action.node);
            }
        }
        undoStack.push(action);
    }

}
