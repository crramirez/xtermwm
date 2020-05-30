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
import jexer.TField;
import jexer.TPanel;
import jexer.TWidget;
import jexer.TWindow;
import jexer.backend.Screen;
import jexer.bits.CellAttributes;
import jexer.bits.StringUtils;
import jexer.event.TKeypressEvent;
import jexer.event.TResizeEvent;
import static jexer.TKeypress.*;

import xtwm.ui.XTWMApplication;

/**
 * ScreensaverPlugin is a plugin whose draw() method can be used as the
 * system screensaver.  It can also be instantiated in a separate window or
 * as part of a tiled panel.
 */
public abstract class ScreensaverPlugin extends PluginWidget {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(ScreensaverPlugin.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * If true, the user entered the password, or there is no password.
     */
    protected boolean unlocked = false;

    /**
     * The password panel.
     */
    protected TPanel passwordPanel = null;

    /**
     * The password field.
     */
    protected TField passwordField = null;

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

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        if (passwordPanel != null) {
            if (keypress.equals(kbEsc)) {
                passwordPanel.setEnabled(false);
                passwordPanel.setVisible(false);
                return;
            }
            passwordPanel.setEnabled(true);
            passwordPanel.setVisible(true);
            passwordPanel.onKeypress(keypress);
        }
    }

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
                        widget.onResize(new TResizeEvent(resize.getBackend(),
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

    /**
     * This method is called when the screensaver is activated.
     *
     * @param screen a snapshot of the screen "under" the screensaver.
     */
    public abstract void startScreensaver(final Screen screen);

    /**
     * This method is called when the screensaver ends.
     */
    public abstract void endScreensaver();

    /**
     * Called to flag the screensaver to ask for a password.
     */
    public void checkPassword() {
        String password = app.getOption("xtwm.lockScreenPassword", "");
        if (password.length() == 0) {
            unlocked = true;
            return;
        }

        if (passwordPanel != null) {
            return;
        }

        // Add a simple password box widget.
        int panelWidth = 50;
        int panelHeight = 6;
        passwordPanel = new TPanel(this, (getWidth() - panelWidth) / 2,
            (getHeight() - panelHeight) / 2, panelWidth, panelHeight) {

            /**
             * Draw a shadowed box for the "dialog".
             */
            public void draw() {
                CellAttributes border = getTheme().getColor("twindow.border");
                CellAttributes background = getTheme().getColor("twindow.background");
                int borderType = 2;
                drawBox(0, 0, getWidth(), getHeight(), border, background,
                    borderType, true);

                String title = i18n.getString("lockScreenInputBoxTitle");
                int titleLength = StringUtils.width(title);
                int titleLeft = (getWidth() - titleLength - 2) / 2;
                putCharXY(titleLeft, 0, ' ', border);
                putStringXY(titleLeft + 1, 0, title, border);
                putCharXY(titleLeft + titleLength + 1, 0, ' ', border);
            }
        };
        passwordPanel.addLabel(i18n.getString("lockScreenInputBoxCaption"),
            2, 1);
        passwordField = passwordPanel.addField(2, 3, panelWidth - 4, false,
            "",
            new TAction() {
                public void DO() {
                    String text = passwordField.getText();
                    if (text.length() > 0) {
                        if (text.equals(password)) {
                            unlocked = true;
                            passwordPanel.remove();
                            passwordPanel = null;
                        }
                    }
                }
            }, null);
    }

    /**
     * Called to see if the password was entered OK.
     *
     * @return true if the user entered the password successfully
     */
    public boolean isUnlocked() {
        return unlocked;
    }

}
