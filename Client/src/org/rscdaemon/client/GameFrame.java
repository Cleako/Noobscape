package org.rscdaemon.client;

import java.awt.*;

public class GameFrame extends Frame {

    public GameFrame(GameWindow gameWindow, int width, int height, String title, boolean resizable, boolean flag1) {
        frameOffset = 28;
        frameWidth = width;
        frameHeight = height;
        aGameWindow = gameWindow;
        if (flag1)
            frameOffset = 48;
        else
            frameOffset = 28;
        setTitle(title);
        setResizable(resizable);
        show();
        toFront();
        resize(frameWidth, frameHeight);
        aGraphics49 = getGraphics();
    }

    public Graphics getGraphics() {
        Graphics g = super.getGraphics();
        if (graphicsTranslate == 0)
            g.translate(0, 24);
        else
            g.translate(-5, 0);
        return g;
    }

	public void resize(int i, int j) {
        super.resize(i, j + frameOffset);
    }

	public boolean handleEvent(Event event) {
        if (event.id == 401)
            aGameWindow.keyDown(event, event.key);
        else if (event.id == 402)
            aGameWindow.keyUp(event, event.key);
        else if (event.id == 501)
            aGameWindow.mouseDown(event, event.x, event.y - 24);
        else if (event.id == 506)
            aGameWindow.mouseDrag(event, event.x, event.y - 24);
        else if (event.id == 502)
            aGameWindow.mouseUp(event, event.x, event.y - 24);
        else if (event.id == 503)
            aGameWindow.mouseMove(event, event.x, event.y - 24);
        else if (event.id == 201)
            aGameWindow.destroy();
        else if (event.id == 1001)
            aGameWindow.action(event, event.target);
        else if (event.id == 403)
            aGameWindow.keyDown(event, event.key);
        else if (event.id == 404)
            aGameWindow.keyUp(event, event.key);
        return true;
    }
//Create Account
    public final void paint(Graphics g) {
        aGameWindow.paint(g);
    }

    int frameWidth;
    int frameHeight;
    int graphicsTranslate;
    int frameOffset;
    GameWindow aGameWindow;
    Graphics aGraphics49;
}
