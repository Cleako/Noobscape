package org.rscdaemon.client;

import java.awt.*;

public final class GameImageMiddleMan extends GameImage {

    public GameImageMiddleMan(int width, int height, int k, Component component) {
        super(width, height, k, component);
    }

    public final void method245(int i, int j, int k, int l, int i1, int j1, int k1) {
        if (i1 >= 50000) {
            _mudclient.method71(i, j, k, l, i1 - 50000, j1, k1);
            return;
        }
        if (i1 >= 40000) {
            _mudclient.method68(i, j, k, l, i1 - 40000, j1, k1);
            return;
        }
        if (i1 >= 20000) {
            _mudclient.method45(i, j, k, l, i1 - 20000, j1, k1);
            return;
        }
        if (i1 >= 5000) {
            _mudclient.method52(i, j, k, l, i1 - 5000, j1, k1);
            return;
        }
        super.spriteClip1(i, j, k, l, i1);
    }

    public mudclient _mudclient;
}
