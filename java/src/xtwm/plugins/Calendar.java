/*
 * Xterm Window Manager
 *
 * The MIT License (MIT)
 *
 * Copyright (C) 2022 Autumn Lamonte
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
 * @author Autumn Lamonte ⚧ Trans Liberation Now
 * @version 1
 */
package xtwm.plugins;

import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TCalendar;
import jexer.TWidget;

import xtwm.ui.XTWMApplication;

/**
 * Calendar is a simple calendar view.
 */
public class Calendar extends PluginWidget {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(Calendar.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The calendar.
     */
    private TCalendar calendar;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     */
    public Calendar(final TWidget parent) {
        super(parent);
    }

    /**
     * No-argument constructor that is intended only for use by
     * XTWMApplication.loadPlugin().
     */
    public Calendar() {
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

        calendar = addCalendar(0, 0, new TAction() {
            public void DO() {
                // Nothing yet, just display the calendar.
            }
        });
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
     * @return a short name, e.g. "Calendar"
     */
    @Override
    public String getPluginName() {
        return i18n.getString("name");
    }

    /**
     * Get the translated full description for this plugin.
     *
     * @return a short name, e.g. "A simple calendar."
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
        // TODO: Expose calendar options like week starts on Monday
        return super.getPluginSettingsEditor(parent);
    }

    /**
     * Get the desired width when rendering this plugin.
     *
     * @return the width
     */
    @Override
    public int getPreferredWidth() {
        return calendar.getWidth();
    }

    /**
     * Get the desired height when rendering this plugin.
     *
     * @return the height
     */
    @Override
    public int getPreferredHeight() {
        return calendar.getHeight();
    }

    // ------------------------------------------------------------------------
    // Calendar ---------------------------------------------------------------
    // ------------------------------------------------------------------------

}
