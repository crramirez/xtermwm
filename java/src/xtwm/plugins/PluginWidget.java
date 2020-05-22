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

import java.io.File;

import jexer.TWidget;
import jexer.TPanel;

import xtwm.ui.XTWMApplication;

/**
 * PluginWidget is a plugin.  Its interface can be instantiated in a separate
 * window or as part of a tiled panel.
 */
public abstract class PluginWidget extends TWidget {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The XTWMApplication using this plugin.
     */
    protected XTWMApplication app;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Constructor for subclasses.
     *
     * @param parent parent widget
     */
    protected PluginWidget(final TWidget parent) {
        super(parent);
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
     * Get the translated menu label for this plugin.
     *
     * @return a mnemonic string that will be populated in the menu
     */
    public abstract String getMenuMnemonic();

    /**
     * Get the translated short name for this plugin.
     *
     * @return a short name, e.g. "Calendar"
     */
    public abstract String getPluginName();

    /**
     * Get the translated full description for this plugin.
     *
     * @return a short name, e.g. "A simple calendar with TODO manager."
     */
    public abstract String getPluginDescription();

    /**
     * See if this is an "Application" plugin.
     *
     * @return true if this plugin should be listed the Application |
     * Programs menu
     */
    public abstract boolean isApplication();

    /**
     * Get the command line to execute if this is an "Application" plugin.
     *
     * @return a command line
     */
    public abstract String getApplicationCommand();

    /**
     * See if this is a "Widget" plugin.
     *
     * @return true if this plugin should be available in the Application |
     * Widgets meny and the Panel | "Switch to" dialog
     */
    public abstract boolean isWidget();

    /**
     * Initialize the plugin.  Since plugins are required to have a
     * no-argument constructor, this method is called to provide a hook for
     * the plugin perform initialization.  Subclasses that override
     * initialize should call super.initialize() to set the XTWMApplication
     * reference.
     *
     * @param app the application that will be using this plugin
     */
    public void initialize(final XTWMApplication app) {
        this.app = app;
    }

    /**
     * Get an interface for editing the plugin settings.
     *
     * @param parent parent widget
     * @return a widget that has settings
     */
    public TWidget getPluginSettingsEditor(final TWidget parent) {
        // Default implementation shows a blank panel.
        return new TPanel(parent, 0, 0, getWidth(), getHeight());
    }

    /**
     * Get the desired width when rendering this plugin.
     *
     * @return the width
     */
    public abstract int getPreferredWidth();

    /**
     * Get the desired height when rendering this plugin.
     *
     * @return the height
     */
    public abstract int getPreferredHeight();

    /**
     * Check if widget should be in a resizable window.
     *
     * @return true if the widget should be resizable when in a window
     */
    public boolean isResizable() {
        // Default is no.
        return false;
    }

    /**
     * Get an option value for this plugin.
     *
     * @param key name of the option
     * @return the option value, or null if it is undefined
     */
    protected String getOption(final String key) {
        if (app == null) {
            return null;
        }
        return app.getPluginOption(getPluginName(), key);
    }

    /**
     * Get an option value for this plugin.
     *
     * @param key name of the option
     * @param defaultValue the value to return if the option is not defined
     * @return the option value, or defaultValue
     */
    protected String getOption(final String key, final String defaultValue) {
        if (app == null) {
            return defaultValue;
        }
        return app.getPluginOption(getPluginName(), key, defaultValue);
    }

    /**
     * Set an option value for this plugin.
     *
     * @param key name of the option
     * @param value the new the option value
     */
    protected void setOption(final String key, final String value) {
        if (app != null) {
            app.setPluginOption(getPluginName(), key, value);
        }
    }

    /**
     * Obtain a File relative to the plugin data directory.  Note that there
     * is a single plugin data directory for all plugins.
     *
     * @param pathname a pathname string
     * @return a File instance
     */
    protected File makeDataFile(final String pathname) {
        if (app == null) {
            throw new UnsupportedOperationException("Not a plugin of " +
                "XTWMApplication");
        }
        return new File(app.getPluginDataDir(), pathname);
    }

}
