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

import jexer.TWidget;
import jexer.TWindow;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.bits.WidgetUtils;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;

import xtwm.ui.VirtualDesktop;
import xtwm.ui.XTWMApplication;

/**
 * DesktopPager is a simple desktop pager in the style of classic X11 virtual
 * desktop pagers like OLWM and AfterStep.
 */
public class DesktopPager extends PluginWidget {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(DesktopPager.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The standard width of a button.
     */
    private static final int BUTTON_WIDTH = 5;

    /**
     * The standard height of a button.
     */
    private static final int BUTTON_HEIGHT = 3;

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * PagerButton is the button that represents a desktop.
     */
    class PagerButton extends TWidget {

        /**
         * The desktop for this button.
         */
        private VirtualDesktop desktop;

        /**
         * The desktop number.
         */
        private int number;

        /**
         * Public constructor.
         *
         * @param parent parent widget
         * @param desktop the desktop
         * @param number the desktop number
         */
        public PagerButton(final TWidget parent, final VirtualDesktop desktop,
            final int number) {

            super(parent, 0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);

            this.desktop = desktop;
            this.number = number;
        }

        /**
         * Handle mouse button presses.
         *
         * @param mouse mouse button event
         */
        @Override
        public void onMouseDown(final TMouseEvent mouse) {
            if (mouse.isMouse1()) {
                if (app.getDesktop() == desktop.getDesktop()) {
                    return;
                }
                while (app.getDesktop() != desktop.getDesktop()) {
                    app.switchToNextDesktop();
                }
                return;
            }
        }

        /**
         * Draw the button.
         */
        @Override
        public void draw() {
            CellAttributes color = desktop.getDesktop().getAttributes();
            CellAttributes borderColor = new CellAttributes(color);
            CellAttributes windowBorder = getTheme().getColor("twindow.border");
            borderColor.setBackColor(windowBorder.getBackColor());
            borderColor.setBold(true);

            if (app.getDesktop() == desktop.getDesktop()) {
                drawBox(0, 0, getWidth(), getHeight(), borderColor, color,
                    3, false);
            } else {
                drawBox(0, 0, getWidth(), getHeight(), borderColor, color);
            }

            for (int x = 1; x < getWidth() - 1; x++) {
                for (int y = 1; y < getHeight() - 1; y++) {
                    putCharXY(x, y, GraphicsChars.HATCH, color);
                }
            }
            String str = Integer.toString(number);
            putStringXY((getWidth() - str.length()) / 2,
                (getHeight() - 1) / 2, str);
        }

    }

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     */
    public DesktopPager(final TWidget parent) {
        super(parent);
    }

    /**
     * No-argument constructor that is intended only for use by
     * XTWMApplication.loadPlugin().
     */
    public DesktopPager() {
        super(null);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle window/screen resize events.
     *
     * @param resize resize event
     */
    @Override
    public void onResize(final TResizeEvent resize) {
        super.onResize(resize);

        if (resize.getType() == TResizeEvent.Type.WIDGET) {
            layoutButtons();
        }
    }

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
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
     * @return a short name, e.g. "DesktopPager"
     */
    @Override
    public String getPluginName() {
        return i18n.getString("name");
    }

    /**
     * Get the translated full description for this plugin.
     *
     * @return a short name, e.g. "A simple calendar with TODO manager."
     */
    @Override
    public String getPluginDescription() {
        return i18n.getString("description");
    }

    /**
     * Initialize the plugin.  Since plugins are required to have a
     * no-argument constructor, this method is called to provide a hook for
     * the plugin to perform initialization.  Subclasses that override
     * initialize should call super.initialize() to set the XTWMApplication
     * reference.
     *
     * @param app the application that will be using this plugin
     */
    @Override
    public void initialize(final XTWMApplication app) {
        super.initialize(app);

        if (isWindowed()) {
            ((TWindow) getParent()).setCloseBox(false);
            ((TWindow) getParent()).setZoomBox(false);
        }

        int i = 1;
        for (VirtualDesktop desktop: app.getDesktops()) {
            new PagerButton(this, desktop, i);
            i++;
        }

        layoutButtons();

        if (isWindowed()) {
            // Place on right side, and put on all desktops.
            ((TWindow) getParent()).setX(getScreen().getWidth() -
                (getWidth() + 2));
            app.putOnAllDesktops((TWindow) getParent());
        }
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
        return true;
    }

    /**
     * Get the translated window title for this plugin.
     *
     * @return the title for the window when this widget is opened in the
     * Application | Widgets menu, or null if this widget should have no
     * title for its window.
     */
    public String getWindowTitle() {
        return null;
    }

    /**
     * Get an interface for editing the plugin settings.
     *
     * @param parent parent widget
     * @return a widget that has settings
     */
    @Override
    public TWidget getPluginSettingsEditor(final TWidget parent) {
        // TODO: Expose options like polling time, # cores, etc.
        return super.getPluginSettingsEditor(parent);
    }

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

    // ------------------------------------------------------------------------
    // DesktopPager -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Layout the desktop buttons to best fit the widget size.
     */
    private void layoutButtons() {
        int x = Math.max(getWidth() / BUTTON_WIDTH, 1);
        int y = Math.max(getHeight() / BUTTON_HEIGHT, 1);
        int n = getChildren().size();

        int columns = Math.min(x, n);
        int rows = Math.max((int) Math.ceil((double) n / columns), 1);
        int minWidth = BUTTON_WIDTH * columns;
        int minHeight = BUTTON_HEIGHT * rows;

        /*
        System.err.println("n " + n + " x " + x + " y " + y);
        System.err.println("   columns " + columns);
        System.err.println("      rows " + rows);
        System.err.println("  minWidth " + minWidth);
        System.err.println(" minHeight " + minHeight);
         */

        if (getWidth() < minWidth) {
            setWidth(minWidth);
        }
        if (getHeight() < minHeight) {
            setHeight(minHeight);
        }

        if (isWindowed()) {
            ((TWindow) getParent()).setMinimumWindowHeight(minHeight + 2);
            ((TWindow) getParent()).setMinimumWindowWidth(minWidth + 2);
            if ((y > rows) && ((x - 1) * y >= n)) {
                ((TWindow) getParent()).setMinimumWindowWidth(minWidth + 2 -
                    BUTTON_WIDTH);
            }
        }

        for (int i = 0; i < n; i++) {
            TWidget w = getChildren().get(i);
            int col = i % columns;
            int row = i / columns;
            w.setX(col * BUTTON_WIDTH);
            w.setY(row * BUTTON_HEIGHT);
        }

    }


}
