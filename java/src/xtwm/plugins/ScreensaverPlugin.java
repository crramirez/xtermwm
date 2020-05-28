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

import jexer.TTimer;
import jexer.TWidget;
import jexer.TWindow;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;

import xtwm.ui.XTWMApplication;

/**
 * ScreensaverPlugin is a plugin whose draw() method can be used as the
 * system screensaver.  It can also be instantiated in a separate window or
 * as part of a tiled panel.
 */
public abstract class ScreensaverPlugin extends PluginWidget {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Constructor for subclasses.
     *
     * @param parent parent widget
     */
    protected ScreensaverPlugin(final TWidget parent) {
        super(parent);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // PluginWidget -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the desired width when rendering this plugin.
     *
     * @return the width
     */
    @Override
    public int getPreferredWidth() {
        return getWidth();
    }

    /**
     * Get the desired height when rendering this plugin.
     *
     * @return the height
     */
    @Override
    public int getPreferredHeight() {
        return getHeight();
    }

    /**
     * Check if widget should be in a resizable window.
     *
     * @return true if the widget should be resizable when in a window
     */
    @Override
    public boolean isResizable() {
        return true;
    }

    /**
     * Get the window that will be used for this plugin when isWindowed() is
     * true.  The plugin will be reparented inside this window.
     *
     * @param application the application
     * @return the window
     */
    @Override
    public TWindow getWindow(final XTWMApplication application) {
        TWindow window = new TWindow(application, getWindowTitle(),
            application.getScreen().getWidth(),
            application.getDesktopBottom() - application.getDesktopTop()) {

            public void onResize(final TResizeEvent resize) {
                // If there is only one widget in the window (which should be
                // typical for a plugin window), resize that widget to match
                // the window dimensions.
                if (resize.getType() == TResizeEvent.Type.WIDGET) {
                    if (getChildren().size() == 1) {
                        TWidget widget = getChildren().get(0);
                        widget.onResize(new TResizeEvent(
                            TResizeEvent.Type.WIDGET,
                            getWidth() - 2, getHeight() - 2));
                        return;
                    }
                }
                super.onResize(resize);
            }
        };

        return window;
    }

    // ------------------------------------------------------------------------
    // ScreensaverPlugin ------------------------------------------------------
    // ------------------------------------------------------------------------


}
