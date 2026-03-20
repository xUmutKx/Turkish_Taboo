package tabu;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class TabuMIDlet extends MIDlet {
    private Display display;
    private GameCanvas canvas;

    public void startApp() {
        display = Display.getDisplay(this);
        if (canvas == null) canvas = new GameCanvas(this);
        display.setCurrent(canvas);
    }
    public void pauseApp() {}
    public void destroyApp(boolean u) {}
    public Display getDisplay() { return display; }
    public void exit() { destroyApp(true); notifyDestroyed(); }
}
