package editor;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;


public class Editor extends Application {
    private static int WINDOW_WIDTH = 500;
    private static int WINDOW_HEIGHT = 500;
    public LinkedListText TextBuffer;
    public ScrollBar scrollBar;
    public ArrayLine LinesBuffer = new ArrayLine(100);
    private String fontName = "Verdana";
    private int fontSize = 12;
    private int fontHeight = 15;
    double mouseVar;
    public DrawCursor cursor = new DrawCursor(LinesBuffer.marginX, 0, 1, fontHeight);
    public Group root;
    public Group textRoot;
    public UndoRedoStack undoRedoStack = new UndoRedoStack(this);
    private static String newfilename;

    private int WidthRoundOff(Text currItem) {
        return (int) Math.round(currItem.getLayoutBounds().getWidth());
    }

    /**
     * An EventHandler to handle keys that get pressed.
     */
    private class KeyEventHandler implements EventHandler<KeyEvent> {
        int textCenterX;
        int textCenterY;

        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            textCenterX = windowWidth / 2;
            textCenterY = windowHeight / 2;
            // All new Nodes need to be added to the root in order to be displayed.
        }

        public void handle(KeyEvent keyEvent) {
            TextBuffer.fontHeight = fontHeight;
            TextBuffer.fontSize = fontSize;
            // Shortcut Keys
            if (keyEvent.isShortcutDown()) {
                if (keyEvent.getCode() == KeyCode.P) {
                    System.out.println((int) cursor.getX() + ", " + (int) cursor.getY());
                }
                if (keyEvent.getCode() == KeyCode.S) {
                    save(newfilename);
                }
                if (keyEvent.getCode() == KeyCode.Z) {
                    undoRedoStack.undo();
                }
                if (keyEvent.getCode() == KeyCode.Y) {
                    undoRedoStack.redo();
                }
                if (keyEvent.getCode() == KeyCode.MINUS) {
                    if (fontSize <= 4) {
                        fontSize = 4;
                    } else {
                        fontSize -= 4;
                    }
                    TextBuffer.fontSize = fontSize;
                    TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                    cursor.setHeight(TextBuffer.fontHeight);
                    String characterTyped = TextBuffer.currPos.item.getText();
                    if (characterTyped.equals("\r") || characterTyped.equals("\n") || characterTyped.equals("\r\n")) {
                        cursor.setX(5);
                        cursor.setY(TextBuffer.currPos.item.getY() + TextBuffer.fontHeight);
                    } else {
                        cursor.setX(TextBuffer.currPos.item.getX());
                        cursor.setY(TextBuffer.currPos.item.getY());
                    }
                    scrollBarMax(LinesBuffer.size() * TextBuffer.fontHeight);
                }
                if (keyEvent.getCode() == KeyCode.PLUS || keyEvent.getCode() == KeyCode.EQUALS) {
                    fontSize += 4;
                    TextBuffer.fontSize = fontSize;
                    TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                    cursor.setHeight(TextBuffer.fontHeight);
                    String characterTyped = TextBuffer.currPos.item.getText();
                    if (characterTyped.equals("\r") || characterTyped.equals("\n") || characterTyped.equals("\r\n")) {
                        cursor.setX(5);
                        cursor.setY(TextBuffer.currPos.item.getY() + TextBuffer.fontHeight);
                    } else {
                        cursor.setX(TextBuffer.currPos.item.getX() + WidthRoundOff(TextBuffer.currPos.item));
                        cursor.setY(TextBuffer.currPos.item.getY());
                    }
                    scrollBarMax(LinesBuffer.size() * TextBuffer.fontHeight);
                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.
                TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                String characterTyped = keyEvent.getCharacter();
                undoRedoStack.redoStack.clear();
                // Ignore control keys, which have non-zero length, as well as the backspace key, which is
                // represented as a character of value = 8 on Windows.
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    Text TextcharacterTyped = new Text(Math.round(cursor.getX()) + cursor.getWidth(), cursor.getY(), characterTyped);
                    TextcharacterTyped.setTextOrigin(VPos.TOP);
                    TextcharacterTyped.setFont(Font.font(fontName, fontSize));
                    if (characterTyped.equals("\r") || characterTyped.equals("\n") || characterTyped.equals("\r\n")) {
                        TextcharacterTyped = new Text(Math.round(cursor.getX()) + cursor.getWidth(), cursor.getY(), "\n");
                        TextBuffer.addChar(TextcharacterTyped);
                        Enter(TextcharacterTyped);
                        action newItem = new action(TextBuffer.currPos, cursor, true);
                        undoRedoStack.undoStack.push(newItem);
                        keyEvent.consume();
                    } else {
                        TextBuffer.addChar(TextcharacterTyped);
                        NormalChar(TextcharacterTyped);
                        //TextBuffer.WordWrap(LinesBuffer, WINDOW_WIDTH, scrollBar.getWidth() + 5);
                        action newItem = new action(TextBuffer.currPos, cursor, true);
                        undoRedoStack.undoStack.push(newItem);
                        cursor.setX(TextBuffer.currPos.item.getX() + TextBuffer.currPos.item.getLayoutBounds().getWidth());
                        cursor.setY(TextBuffer.currPos.item.getY());
                        keyEvent.consume();
                    }
                }
            }
            if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.LEFT) {
                    if (TextBuffer.size() > 0 && TextBuffer.get(0) != TextBuffer.currPos.next.item) {
                        TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                        scrollBarSnap();
                        String characterTyped = TextBuffer.currPos.prev.item.getText();
                        if (characterTyped.equals("\r") || characterTyped.equals("\n") || characterTyped.equals("\r\n") || LinesBuffer.get(LinesBuffer.currLine).prev == TextBuffer.currPos) {
                            TextBuffer.currPos = TextBuffer.currPos.prev;
                            cursor.setX(TextBuffer.currPos.next.item.getX());
                            cursor.setY(TextBuffer.currPos.next.item.getY());
                            LinesBuffer.decrementcurrIndex();
                        } else {
                            TextBuffer.currPos = TextBuffer.currPos.prev;
                            if (TextBuffer.currPos == LinesBuffer.get(LinesBuffer.currLine).prev && TextBuffer.currPos.item.getText().equals(" ")) {
                                cursor.setX(LinesBuffer.marginX);
                                cursor.setY(TextBuffer.currPos.next.item.getY());
                            } else {
                                cursor.setX(TextBuffer.currPos.item.getX() + Math.round(TextBuffer.currPos.item.getLayoutBounds().getWidth()));
                                cursor.setY(TextBuffer.currPos.item.getY());
                            }
                        }
                    }
                }
                if (code == KeyCode.RIGHT) {
                    Text currItem;
                    if (TextBuffer.size() > 0) {
                        TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                        if (TextBuffer.currPos.next.item != null) {
                            scrollBarSnap();
                            String characterTyped = TextBuffer.currPos.next.item.getText();
                            if (characterTyped.equals("\n") || characterTyped.equals("\r\n")) {
                                TextBuffer.currPos = TextBuffer.currPos.next;
                                cursor.setY(TextBuffer.currPos.next.item.getY());
                                cursor.setX(LinesBuffer.marginX);
                                LinesBuffer.incrementcurrIndex();
                            } else {
                                TextBuffer.currPos = TextBuffer.currPos.next;
                                currItem = TextBuffer.currPos.item;
                                if (LinesBuffer.size() - 2 > LinesBuffer.currLine && TextBuffer.currPos == LinesBuffer.get(LinesBuffer.currLine + 1).prev && TextBuffer.currPos.item.getText().equals(" ")) {
                                    cursor.setX(LinesBuffer.marginX);
                                    cursor.setY(TextBuffer.currPos.next.item.getY());
                                } else {
                                    cursor.setX(currItem.getX() + Math.round(currItem.getLayoutBounds().getWidth()));
                                    cursor.setY(currItem.getY());
                                }
                                if (LinesBuffer.size() - 1 > LinesBuffer.currLine && TextBuffer.currPos == LinesBuffer.get(LinesBuffer.currLine + 1)) {
                                    LinesBuffer.incrementcurrIndex();
                                }
                            }

                        }
                        scrollBarSnap();
                    }
                }

                if (code == KeyCode.UP) {
                    if (LinesBuffer.currLine > 0) {
                        String characterTypedOld = TextBuffer.currPos.item.getText();
                        TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                        TextBuffer.currPos = TextBuffer.findX(cursor.getX(), LinesBuffer, cursor.getX());
                        String characterTyped = TextBuffer.currPos.item.getText();
                        if (characterTyped.equals("\n") || characterTyped.equals("\r\n")) {
                            cursor.setX(LinesBuffer.marginX);
                            cursor.setY(TextBuffer.currPos.next.item.getY());
                            LinesBuffer.decrementcurrIndex();
                        } else {
                            cursor.setX(TextBuffer.currPos.item.getX() + WidthRoundOff(TextBuffer.currPos.item));
                            cursor.setY(TextBuffer.currPos.item.getY());
                            LinesBuffer.decrementcurrIndex();
                            if (characterTypedOld.equals("\n") || characterTypedOld.equals("\r\n")) {
                                if (TextBuffer.currPos == LinesBuffer.get(LinesBuffer.currLine).prev && TextBuffer.currPos.item.getText().equals(" ")) {
                                    cursor.setX(LinesBuffer.marginX);
                                    cursor.setY(TextBuffer.currPos.next.item.getY());
                                }
                            } else {
                                if (LinesBuffer.size() > LinesBuffer.currLine + 1 && TextBuffer.currPos == LinesBuffer.get(LinesBuffer.currLine + 1).prev && TextBuffer.currPos.item.getText().equals(" ")) {
                                    cursor.setX(LinesBuffer.marginX);
                                    cursor.setY(TextBuffer.currPos.next.item.getY());
                                }
                            }

                        }
                        scrollBarSnap();

                    }
                }
                if (code == KeyCode.DOWN) {
                    if (LinesBuffer.currLine < LinesBuffer.size() - 1) {
                        TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                        TextBuffer.currPos = TextBuffer.findXDown(cursor.getX(), LinesBuffer, cursor.getX());
                        String characterTyped = TextBuffer.currPos.item.getText();
                        if (characterTyped.equals("\n") || characterTyped.equals("\r\n")) {
                            cursor.setX(LinesBuffer.marginX);
                            cursor.setY(TextBuffer.currPos.next.item.getY());
                        } else {
                            if (LinesBuffer.size() - 2 > LinesBuffer.currLine && TextBuffer.currPos == LinesBuffer.get(LinesBuffer.currLine + 1).prev && TextBuffer.currPos.item.getText().equals(" ")) {
                                cursor.setX(LinesBuffer.marginX);
                                cursor.setY(TextBuffer.currPos.next.item.getY());
                            } else {
                                cursor.setX(TextBuffer.currPos.item.getX() + WidthRoundOff(TextBuffer.currPos.item));
                                cursor.setY(TextBuffer.currPos.item.getY());
                            }
                        }
                        scrollBarSnap();
                        if (LinesBuffer.size() - 2 > LinesBuffer.currLine && TextBuffer.currPos == LinesBuffer.get(LinesBuffer.currLine + 2).prev && TextBuffer.currPos.item.getText().equals(" ")) {
                            cursor.setX(LinesBuffer.marginX);
                            cursor.setY(TextBuffer.currPos.next.item.getY());
                        }
                        LinesBuffer.incrementcurrIndex();
                    }

                }
                if (code == KeyCode.BACK_SPACE) {
                    Text reference;
                    if (TextBuffer.size() > 0 && TextBuffer.get(0) != TextBuffer.currPos.next.item) {
                        TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                        undoRedoStack.redoStack.clear();
                        int move = WidthRoundOff(TextBuffer.currPos.item);
                        String characterTyped = TextBuffer.currPos.item.getText();
                        if (characterTyped.equals("\n") || characterTyped.equals("\r\n")) {
                            reference = TextBuffer.currPos.item;
                            action newItem = new action(TextBuffer.currPos, cursor, false);
                            undoRedoStack.undoStack.push(newItem);
                            root.getChildren().remove(TextBuffer.removeChar());
                            TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                            cursor.setX(reference.getX() + move);
                            cursor.setY(reference.getY());
                            LinesBuffer.remove(LinesBuffer.currLine);
                            LinesBuffer.decrementcurrIndex();
                            double docLength = TextBuffer.fontHeight * LinesBuffer.size() - 1;
                            if (LinesBuffer.currLine == LinesBuffer.size() - 1) {
                                scrollBarMax(docLength);
                            }
                            scrollBarSnap();
                        } else {
                            reference = TextBuffer.currPos.item;
                            action newItem = new action(TextBuffer.currPos, cursor, false);
                            undoRedoStack.undoStack.push(newItem);
                            root.getChildren().remove(TextBuffer.removeChar());
                            TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                            if (TextBuffer.currPos.item.getText().equals("\n") || TextBuffer.currPos.item.getText().equals("\r\n")) {
                                cursor.setY(TextBuffer.currPos.next.item.getY());
                                cursor.setX(LinesBuffer.marginX);
                            } else if (LinesBuffer.size() > LinesBuffer.currLine + 1 && TextBuffer.currPos == LinesBuffer.get(LinesBuffer.currLine + 1).prev && TextBuffer.currPos.item.getText().equals(" ")) {
                                cursor.setX(LinesBuffer.marginX);
                                cursor.setY(TextBuffer.currPos.next.item.getY());
                            } else {
                                cursor.setX(TextBuffer.currPos.item.getX() + WidthRoundOff(TextBuffer.currPos.item));
                                cursor.setY(TextBuffer.currPos.item.getY());
                            }
                            scrollBarSnap();
                        }
                    }

                }
            }
        }
    }

    private double usableWidth(double windowWidth) {
        return windowWidth - scrollBar.getWidth() - 10;
    }


    private void scrollBarMax(double docLength) {
        if (docLength < WINDOW_HEIGHT) {
            scrollBar.setMax(0);
        } else {
            scrollBar.setMax(Math.round(docLength - WINDOW_HEIGHT));
        }

    }


    private void scrollBarSnap() {
        if (Math.ceil(cursor.getY()) < scrollBar.getValue()) {
            scrollBar.setValue(cursor.getY());
        }
        if (Math.floor(cursor.getY()) > WINDOW_HEIGHT) {
            scrollBar.setValue(cursor.getY() - WINDOW_HEIGHT + TextBuffer.fontHeight);
        }
    }


    /**
     * An event handler that displays the current position of the mouse whenever it is clicked.
     */
    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        MouseClickEventHandler(Group root) {
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            double mousePressedX = mouseEvent.getX();
            double mousePressedY = mouseEvent.getY() + scrollBar.getValue();
            TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
            int YCor = (int) Math.round(mousePressedY / TextBuffer.fontHeight);
            if (YCor > LinesBuffer.size() - 1) {
                YCor = LinesBuffer.size() - 1;
            } else if (mousePressedY < 0) {
                YCor = 0;
            }
            TextBuffer.currPos = TextBuffer.findClick(mousePressedX, LinesBuffer, YCor);
            String characterTyped = TextBuffer.currPos.item.getText();
            if (characterTyped.equals("\n") || characterTyped.equals("\r\n")) {
                cursor.setY(TextBuffer.currPos.next.item.getY());
                cursor.setX(LinesBuffer.marginX);
            } else {
                cursor.setX(TextBuffer.currPos.item.getX() + WidthRoundOff(TextBuffer.currPos.item));
                cursor.setY(TextBuffer.currPos.item.getY());
            }
            scrollBarSnap();
            // Display text right above the click.

        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Create a Node that will be the parent of all things displayed on the screen.
        textRoot = new Group();
        root = new Group();
        textRoot.getChildren().add(root);
        TextBuffer = new LinkedListText();
        LinesBuffer.add(LinesBuffer.currLine, TextBuffer.currPos);
        root.getChildren().add(cursor);
        cursor.makeCursorColorChange();
        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        Scene scene = new Scene(textRoot, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);
        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
            new KeyEventHandler(textRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(new MouseClickEventHandler(root));
        // Make a vertical scroll bar on the right side of the screen.
        scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        // Set the height of the scroll bar so that it fills the whole window.
        scrollBar.setPrefHeight(WINDOW_HEIGHT);
        scrollBar.setLayoutX(WINDOW_WIDTH - scrollBar.getWidth());
        // Set the range of the scroll bar.
        scrollBar.setMin(0);
        scrollBar.setValue(0);
        scrollBar.setMax(0);
        scrollBar.setBlockIncrement(TextBuffer.fontHeight);
        /** When the scroll bar changes position, change the Text */
        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                ObservableValue<? extends Number> observableValue,
                Number oldValue,
                Number newValue) {
                if (newValue.doubleValue() != oldValue.doubleValue()) {
                    root.setLayoutY(-Math.round(newValue.doubleValue()));
                    mouseVar = Math.round(newValue.doubleValue());
                }
            }
        });

        // Add the scroll bar to the scene graph, so that it appears on the screen.
        textRoot.getChildren().add(scrollBar);

        primaryStage.setTitle("Editor");
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(
                ObservableValue<? extends Number> observableValue,
                Number oldScreenWidth,
                Number newScreenWidth) {
                WINDOW_WIDTH = newScreenWidth.intValue();
                scrollBar.setPrefHeight(WINDOW_HEIGHT);
                scrollBar.setLayoutX(WINDOW_WIDTH - scrollBar.getWidth());
                TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                cursor.setX((TextBuffer.currPos.item.getX()) + Math.round(TextBuffer.currPos.item.getLayoutBounds().getWidth()));
                cursor.setY(TextBuffer.currPos.item.getY());
                scrollBarMax(LinesBuffer.size() * TextBuffer.fontHeight);
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(
                ObservableValue<? extends Number> observableValue,
                Number oldScreenHeight,
                Number newScreenHeight) {
                WINDOW_HEIGHT = newScreenHeight.intValue();
                scrollBar.setPrefHeight(WINDOW_HEIGHT);
                scrollBar.setLayoutX(WINDOW_WIDTH - scrollBar.getWidth());
                TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                cursor.setX((TextBuffer.currPos.item.getX()) + Math.round(TextBuffer.currPos.item.getLayoutBounds().getWidth()));
                cursor.setY(TextBuffer.currPos.item.getY());
                scrollBarMax(LinesBuffer.size() * TextBuffer.fontHeight);
            }
        });
        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
        open(newfilename);
    }

    private static class Print {
        private static void print(String[] args) {
            if (args[1].equals("debug")) {
                System.out.println("Let's debug");
            }
        }
    }

    private void open(String inputFilename) {
        // Check to make sure that the input file exists!
        try {
            File inputFile = new File(inputFilename);
            FileReader reader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            int intRead = -1;
            while ((intRead = bufferedReader.read()) != -1) {
                // The integer read can be cast to a char, because we're assuming ASCII.
                char charRead = (char) intRead;
                String characterTyped = Character.toString(charRead);
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    Text TextcharacterTyped = new Text(Math.round(cursor.getX()) + 1, cursor.getY(), characterTyped);
                    TextcharacterTyped.setTextOrigin(VPos.TOP);
                    TextcharacterTyped.setFont(Font.font(fontName, TextBuffer.fontSize));
                    TextBuffer.addChar(TextcharacterTyped);
                    if (TextcharacterTyped.getText().equals("\r") || TextcharacterTyped.getText().equals("\r\n")) {
                        cursor.setX(LinesBuffer.marginX);
                        cursor.setY(TextcharacterTyped.getY() + TextBuffer.fontHeight);
                    } else {
                        cursor.setX(TextcharacterTyped.getX() + WidthRoundOff(TextBuffer.currPos.item));
                        cursor.setY(TextcharacterTyped.getY());
                    }
                    root.getChildren().add(TextcharacterTyped);
                    TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                }
            }
            cursor.setX(LinesBuffer.marginX);
            cursor.setY(0);
            TextBuffer.currPos = TextBuffer.getFront();
            LinesBuffer.currLine = 0;
            scrollBarMax(LinesBuffer.size() * TextBuffer.fontHeight);
            bufferedReader.close();
        } catch (FileNotFoundException fileNotFoundException) {
            newfilename = inputFilename;
        } catch (IOException ioException) {
            System.out.println("Error; exception was: " + ioException);
        }
    }

    private void save(String outputFilename) {
        try {
            FileWriter writer = new FileWriter(outputFilename);
            for (int i = 0; i < TextBuffer.size(); i++) {
                String charRead = TextBuffer.get(i).getText();
                if (charRead.equals("\r")) {
                    charRead = "\n";
                }
                writer.write(charRead);
            }
            writer.close();
        } catch (IOException ioException) {
            System.out.println("Error; exception was: " + ioException);
        }

    }

    public void Enter(Text TextcharacterTyped) {
        if (TextBuffer.size() == 1) {
            LinesBuffer.set(LinesBuffer.currLine, TextBuffer.currPos);
        }
        if (LinesBuffer.currLine < LinesBuffer.size()) {
            if (TextBuffer.currPos.next == LinesBuffer.get(LinesBuffer.currLine)) {
                LinesBuffer.set(LinesBuffer.currLine, TextBuffer.currPos);
            }
        }
        LinesBuffer.incrementcurrIndex();
        LinesBuffer.add(LinesBuffer.currLine, TextBuffer.currPos.next);
        TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
        double docLength = TextBuffer.fontHeight * LinesBuffer.size();
        scrollBarMax(docLength);
        scrollBarSnap();
        double width = TextcharacterTyped.getLayoutBounds().getWidth();
        int round = (int) Math.round(width);
        cursor.setY(TextBuffer.currPos.item.getY() + TextBuffer.fontHeight);
        cursor.setX(LinesBuffer.marginX);
        root.getChildren().add(TextcharacterTyped);
    }

    public void NormalChar(Text TextcharacterTyped) {
        double docLength;
        double width = TextcharacterTyped.getLayoutBounds().getWidth();
        double round = Math.round(width);
        TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
        docLength = TextBuffer.fontHeight * LinesBuffer.size();
        scrollBarMax(docLength);
        scrollBarSnap();
        cursor.setX(Math.round(cursor.getX()) + round);
        root.getChildren().add(TextcharacterTyped);
    }

    public void backSpace(editor.LinkedListText.Node node) {
        Text reference;
        if (TextBuffer.size() > 0 && TextBuffer.get(0) != node.next.item) {
            int move = WidthRoundOff(node.item);
            String characterTyped = node.item.getText();
            if (characterTyped.equals("\n") || characterTyped.equals("\r\n")) {
                reference = node.item;
                root.getChildren().remove(TextBuffer.removeSpecificChar(node));
                TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                cursor.setX(reference.getX() + move);
                cursor.setY(reference.getY());
                LinesBuffer.remove(LinesBuffer.currLine);
                LinesBuffer.decrementcurrIndex();
                double docLength = TextBuffer.fontHeight * LinesBuffer.size();
                scrollBarMax(docLength);
                scrollBarSnap();
            } else {
                reference = node.item;
                root.getChildren().remove(TextBuffer.removeSpecificChar(node));
                TextBuffer.Render(usableWidth(WINDOW_WIDTH), LinesBuffer);
                cursor.setX(reference.getX());
                cursor.setY(reference.getY());
                scrollBarSnap();
            }
        }
    }

    public void UndoRedoNormalChar(editor.LinkedListText.Node node) {
        TextBuffer.addChar(node.item);
        NormalChar(node.item);
        cursor.setX(TextBuffer.currPos.item.getX() + WidthRoundOff(TextBuffer.currPos.item));
        cursor.setY(TextBuffer.currPos.item.getY());
    }

    public void UndoRedoEnter(editor.LinkedListText.Node node) {
        TextBuffer.addChar(node.item);
        Enter(node.item);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("No filename was provided.");
            System.exit(1);
        }
        newfilename = args[0];
        if(args.length>1){
            Print.print(args);
        }
        launch(args);
    }
}