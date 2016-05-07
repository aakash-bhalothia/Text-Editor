package editor;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *Draw Cursor
 *  Created by aakashbhalothia on 2/29/16.
 */
public class DrawCursor extends Rectangle{

    public DrawCursor(double x, double y, double width, double height) {
        super(x,y,width,height);
    }
    public class CursorBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 1;
        private Color[] boxColors =
            {Color.WHITE, Color.BLACK};

        CursorBlinkEventHandler() {
            changeColor();
        }

        private void changeColor() {
            setFill(boxColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % boxColors.length;
        }

        @Override
        public void handle(ActionEvent event) {
            changeColor();
        }
    }

    public void makeCursorColorChange() {
        // Create a Timeline that will call the "handle" function of CursorBlinkEventHandler
        // every 0.5 seconds.
        final Timeline timeline = new Timeline();
        // The cursor should continue blinking forever.
        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorBlinkEventHandler cursorChange = new CursorBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }





}
