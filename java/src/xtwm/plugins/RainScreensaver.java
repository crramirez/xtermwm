/*
 * Xterm Window Manager
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Kevin Lamonte
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
 * @author Kevin Lamonte [kevin.lamonte@gmail.com]
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
 * RainScreensaver drops characters like they are raining from the bottom.
 */
public class RainScreensaver extends ScreensaverPlugin {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(RainScreensaver.class.getName());

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
     * The raining screen.
     */
    private Screen rainScreen = null;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     */
    public RainScreensaver(final TWidget parent) {
        super(parent);
    }

    /**
     * No-argument constructor that is intended only for use by
     * XTWMApplication.loadPlugin().
     */
    public RainScreensaver() {
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
     * @return a short name, e.g. "RainScreensaver"
     */
    @Override
    public String getPluginName() {
        return i18n.getString("name");
    }

    /**
     * Get the translated full description for this plugin.
     *
     * @return a short name, e.g. "Watch the characters fall like rain."
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
     * Draw the raining screen.
     */
    @Override
    public void draw() {
        synchronized (rainScreen) {
            for (int y = 0; y < rainScreen.getHeight(); y++) {
                for (int x = 0; x < rainScreen.getWidth(); x++) {
                    putCharXY(x, y, rainScreen.getCharXY(x, y));
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
        rainScreen = originalScreen.snapshot();

        timer = app.addTimer((int) Math.ceil(1000.0 / 18.2), true,
            new TAction() {
                public void DO() {
                    RainScreensaver.this.doRain();
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
    // RainScreensaver --------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Perform the rain sequence.
     */
    private void doRain() {
        boolean found = false;

        synchronized (rainScreen) {
            for (int x = 0; x < rainScreen.getWidth(); x++) {

                // Add some odds for skipping columns.
                if ((x > 0) && (Math.random() > 0.6)) {
                    x++;
                }

                Cell rainDrop = null;
                int y = rainScreen.getHeight() - 1;
                for (; y >= 0; y--) {
                    Cell ch = rainScreen.getCharXY(x, y);
                    if (!ch.isBlank()) {
                        rainDrop = ch;
                        found = true;
                        break;
                    }
                }
                if (y < 0) {
                    y = 0;
                }
                rainScreen.putCharXY(x, y, new Cell());
                if ((rainDrop != null) && (y < rainScreen.getHeight() - 1)) {
                    rainScreen.putCharXY(x, y + 1, rainDrop);
                }
            }

            if (!found) {
                rainScreen = originalScreen.snapshot();
            }

        } // synchronized (rainScreen)
    }

}
