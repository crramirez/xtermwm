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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ResourceBundle;

import jexer.TWidget;
import jexer.bits.CellAttributes;

import xtwm.ui.XTWMApplication;

/**
 * SystemMonitor is a simple real-time performance monitor that shows the
 * CPU, memory, and disk usage exposed to the JVM through the Java management
 * interfaces (java.lang.management).
 */
public class SystemMonitor extends PluginWidget {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(SystemMonitor.class.getName());

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
    public SystemMonitor(final TWidget parent) {
        super(parent);
    }

    /**
     * No-argument constructor that is intended only for use by
     * XTWMApplication.loadPlugin().
     */
    public SystemMonitor() {
        super(null);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the values obtained from the Java management interfaces.
     */
    @Override
    public void draw() {
        CellAttributes textColor;
        CellAttributes valueColor;

        textColor = getTheme().getColor("plugins.calendar.text");
        valueColor = getTheme().getColor("plugins.calendar.value");

        putAll(' ', textColor);

        // CPU count
        putStringXY(1, 0, i18n.getString("numCpu"), textColor);
        String cpuCount = Integer.toString(Runtime.getRuntime().
            availableProcessors());
        putStringXY(getWidth() - cpuCount.length() - 1, 0, cpuCount,
            valueColor);

        // Load average
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        putStringXY(1, 1, i18n.getString("loadAverage"), textColor);
        String loadAverage = String.format("%.2f",
            osBean.getSystemLoadAverage());
        putStringXY(getWidth() - loadAverage.length() - 1, 1, loadAverage,
            valueColor);

    }

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

        // Set the colors to use.
        app.getTheme().setColorFromString("plugins.calendar.text",
            "white on blue");
        app.getTheme().setColorFromString("plugins.calendar.value",
            "bold yellow on blue");
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
     * @return a short name, e.g. "SystemMonitor"
     */
    @Override
    public String getPluginName() {
        return i18n.getString("name");
    }

    /**
     * Get the translated full description for this plugin.
     *
     * @return a short name, e.g. "A simple monitor for the JVM."
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
     * Whether or not this plugin should be on all desktops when loaded as a
     * widget at startup.
     *
     * @return true if this plugin should be on all desktops
     */
    @Override
    public boolean isOnAllDesktops() {
        return true;
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
        return 30;
    }

    /**
     * Get the desired height when rendering this plugin.
     *
     * @return the height
     */
    @Override
    public int getPreferredHeight() {
        return 10;
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
    // SystemMonitor ----------------------------------------------------------
    // ------------------------------------------------------------------------

}
