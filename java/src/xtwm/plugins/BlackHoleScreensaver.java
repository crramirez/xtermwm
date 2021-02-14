/*
 * Xterm Window Manager
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Autumn Lamonte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * @author Autumn Lamonte [AutumnWalksTheLake@gmail.com] ⚧ Trans Liberation Now
 * @version 1
 */
package xtwm.plugins;

import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TTimer;
import jexer.TWidget;
import jexer.backend.Screen;
import jexer.bits.Cell;

/**
 * BlackHoleScreensaver sucks the screen into a single point.
 */
public class BlackHoleScreensaver extends ScreensaverPlugin {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(BlackHoleScreensaver.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The animation timer.
     */
    private TTimer timer = null;

    /**
     * The original screen.
     */
    private Screen originalScreen = null;

    /**
     * The black hole screen.
     */
    private Screen blackHoleScreen = null;

    /**
     * The black hole X location.
     */
    private int blackHoleX = 0;

    /**
     * The black hole Y location.
     */
    private int blackHoleY = 0;

    /**
     * The "event horizon" for the black hole.
     */
    private int blackHoleR = 100;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     */
    public BlackHoleScreensaver(final TWidget parent) {
        super(parent);
    }

    /**
     * No-argument constructor that is intended only for use by
     * XTWMApplication.loadPlugin().
     */
    public BlackHoleScreensaver() {
        super(null);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // PluginWidget -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the translated menu label for this plugin.
     *
     * @return a mnemonic string that will be populated in the menu
     */
    @Override
    public String getMenuMnemonic() {
        return i18n.getString("mnemonic");
    }

    /**
     * Get the translated short name for this plugin.
     *
     * @return a short name, e.g. "BlackHoleScreensaver"
     */
    @Override
    public String getPluginName() {
        return i18n.getString("name");
    }

    /**
     * Get the translated full description for this plugin.
     *
     * @return a short name, e.g. "Screen falls into a black hole."
     */
    @Override
    public String getPluginDescription() {
        return i18n.getString("description");
    }

    /**
     * See if this is an "Application" plugin.
     *
     * @return true if this plugin should be listed the Application |
     * Programs menu
     */
    @Override
    public boolean isApplication() {
        return false;
    }

    /**
     * Get the command line to execute if this is an "Application" plugin.
     *
     * @return a command line
     */
    @Override
    public String getApplicationCommand() {
        return null;
    }

    /**
     * See if this is a "Widget" plugin.
     *
     * @return true if this plugin should be available in the Application |
     * Widgets meny and the Panel | "Switch to" dialog
     */
    @Override
    public boolean isWidget() {
        return false;
    }

    /**
     * Get the translated window title for this plugin.
     *
     * @return the title for the window when this widget is opened in the
     * Application | Widgets menu, or null if this widget should have no
     * title for its window.
     */
    public String getWindowTitle() {
        return i18n.getString("windowTitle");
    }

    // ------------------------------------------------------------------------
    // ScreensaverPlugin ------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the black hole screen.
     */
    @Override
    public void draw() {
        synchronized (blackHoleScreen) {
            for (int y = 0; y < blackHoleScreen.getHeight(); y++) {
                for (int x = 0; x < blackHoleScreen.getWidth(); x++) {
                    putCharXY(x, y, blackHoleScreen.getCharXY(x, y));
                }
            }
        }
    }

    /**
     * This method is called when the screensaver is activated.
     *
     * @param screen a snapshot of the screen "under" the screensaver.
     */
    @Override
    public void startScreensaver(final Screen screen) {
        originalScreen = screen;
        blackHoleScreen = originalScreen.snapshot();

        blackHoleX = (int) (Math.random() * screen.getWidth());
        blackHoleY = (int) (Math.random() * screen.getHeight());
        blackHoleR = Math.max(screen.getWidth(), screen.getHeight()) / 2;

        timer = app.addTimer((int) Math.ceil(1000.0 / 18.2), true,
            new TAction() {
                public void DO() {
                    BlackHoleScreensaver.this.doBlackHole();
                }
            });
    }

    /**
     * This method is called when the screensaver ends.
     */
    @Override
    public void endScreensaver() {
        if (timer != null) {
            app.removeTimer(timer);
        }
    }

    // ------------------------------------------------------------------------
    // BlackHoleScreensaver ---------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Perform the black hole sequence.
     */
    private void doBlackHole() {
        boolean found = false;

        synchronized (blackHoleScreen) {
            int width = blackHoleScreen.getWidth();
            int height = blackHoleScreen.getHeight();
            blackHoleR--;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {

                    // Add some odds for skipping columns.  This leaves a
                    // nice raggedy effect on the "black hole" edge.
                    if ((x > 0) && (Math.random() > 0.6)) {
                        x++;
                    }

                    Cell ch = blackHoleScreen.getCharXY(x, y);
                    if (!ch.isBlank()) {
                        found = true;
                    }

                    int distance = (int) Math.sqrt(Math.pow(blackHoleX - x, 2) +
                        Math.pow((blackHoleY - y) * 2, 2));

                    if (distance > blackHoleR) {
                        blackHoleScreen.putCharXY(x, y, new Cell());
                    } else if (distance == blackHoleR) {
                        int dX = Math.min(Math.abs(blackHoleX - x), 1);
                        int dY = Math.min(Math.abs(blackHoleY - y), 1);
                        if (blackHoleX == x) {
                            dX = 0;
                        } else if (blackHoleX < x) {
                            dX = Math.max(-dX, -1);
                        } else {
                            dX = Math.min(dX, 1);
                        }
                        if (blackHoleY == y) {
                            dY = 0;
                        } else if (blackHoleY < y) {
                            dY = Math.max(-dY, -1);
                        } else {
                            dY = Math.min(dY, 1);
                        }
                        blackHoleScreen.putCharXY(x + dX, y + dY, new Cell(ch));
                    }
                }
            }

            if (!found) {
                blackHoleScreen = originalScreen.snapshot();
                blackHoleX = (int) (Math.random() * originalScreen.getWidth());
                blackHoleY = (int) (Math.random() * originalScreen.getHeight());
                blackHoleR = Math.max(width, height) / 2;
            }

        } // synchronized (blackHoleScreen)
    }

}
