package editor;

/**
 * Created by aakashbhalothia on 3/7/16.
 */
public class action {
    public LinkedListText.Node node;
    public DrawCursor cursor;
    public boolean insert;

    public action(LinkedListText.Node i, DrawCursor p, Boolean n) {
        node = i;
        cursor = p;
        insert = n;
    }
}
