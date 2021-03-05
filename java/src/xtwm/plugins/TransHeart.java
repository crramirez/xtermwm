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
import jexer.TWidget;
import jexer.bits.CellAttributes;
import jexer.bits.Color;

import xtwm.ui.XTWMApplication;

/**
 * TransHeart is a heart-shaped transgender flag.
 */
public class TransHeart extends PluginWidget {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(TransHeart.class.getName());

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
     * Public constructor.
     *
     * @param parent parent widget
     */
    public TransHeart(final TWidget parent) {
        super(parent);
    }

    /**
     * No-argument constructor that is intended only for use by
     * XTWMApplication.loadPlugin().
     */
    public TransHeart() {
        super(null);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // PluginWidget -----------------------------------------------------------
    // ------------------------------------------------------------------------

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
    }

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
     * @return a short name, e.g. "TransHeart"
     */
    @Override
    public String getPluginName() {
        return i18n.getString("name");
    }

    /**
     * Get the translated full description for this plugin.
     *
     * @return a short name, e.g. "A heart-shaped transgender flag."
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
        return i18n.getString("windowTitle");
    }

    /**
     * Get an interface for editing the plugin settings.
     *
     * @param parent parent widget
     * @return a widget that has settings
     */
    @Override
    public TWidget getPluginSettingsEditor(final TWidget parent) {
        return super.getPluginSettingsEditor(parent);
    }

    /**
     * Get the desired width when rendering this plugin.
     *
     * @return the width
     */
    @Override
    public int getPreferredWidth() {
        return 17;
    }

    /**
     * Get the desired height when rendering this plugin.
     *
     * @return the height
     */
    @Override
    public int getPreferredHeight() {
        return 8;
    }

    // ------------------------------------------------------------------------
    // TransHeart -------------------------------------------------------------
    // ------------------------------------------------------------------------

    /*

  ▄███▄   ▄███▄
 ███████████████
 ▀█████████████▀
   ▀█████████▀
     ▀█████▀
       ▀█▀

     */

    /**
     * Draw the heart.
     */
    @Override
    public void draw() {
        CellAttributes blue = new CellAttributes();
        blue.setForeColor(Color.BLUE);
        blue.setBackColor(Color.BLACK);
        blue.setBold(false);

        CellAttributes pink = new CellAttributes();
        pink.setForeColor(Color.RED);
        pink.setBackColor(Color.BLACK);
        pink.setBold(true);

        CellAttributes pinkBlue = new CellAttributes();
        pinkBlue.setForeColor(Color.RED);
        pinkBlue.setBackColor(Color.BLUE);
        pinkBlue.setBold(true);

        CellAttributes pinkBlack = new CellAttributes();
        pinkBlack.setForeColor(Color.RED);
        pinkBlack.setBackColor(Color.BLACK);
        pinkBlack.setBold(true);

        CellAttributes pinkWhite = new CellAttributes();
        pinkWhite.setForeColor(Color.RED);
        pinkWhite.setBackColor(Color.WHITE);
        pinkWhite.setBold(true);

        CellAttributes white = new CellAttributes();
        white.setForeColor(Color.WHITE);
        white.setBackColor(Color.BLACK);
        white.setBold(false);

        for (int i = 0; i < getHeight(); i++) {
            hLineXY(0, i, getWidth(), ' ', blue);
        }
        putStringXY(1, 1, " ▄███▄   ▄███▄ ", blue);
        putStringXY(1, 2, "▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄", pinkBlue);
        putStringXY(1, 3, "▀█████████████▀", pinkBlack);
        putStringXY(2, 3,  "▀▀▀▀▀▀▀▀▀▀▀▀▀", pinkWhite);
        putStringXY(1, 4, "  ▀█████████▀", white);
        putStringXY(4, 4,    "▄▄▄▄▄▄▄▄▄", pinkWhite);
        putStringXY(1, 5, "    ▀█████▀", pinkBlack);
        putStringXY(6, 5,      "▀▀▀▀▀", pinkBlue);
        putStringXY(1, 6, "      ▀█▀", blue);
    }

}
