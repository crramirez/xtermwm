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
package xtwm.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TButton;
import jexer.TList;
import jexer.TWindow;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.event.TKeypressEvent;
import static jexer.TKeypress.*;

import xtwm.ui.XTWMApplication;
import xtwm.plugins.PluginWidget;

/**
 * PluginOptionsWindow enables/disables and configures the available plugins.
 */
public class PluginOptionsWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(PluginOptionsWindow.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The list of plugins pane.
     */
    private TList plugins;

    /**
     * The plugins by list ID.
     */
    private HashMap<Integer, PluginWidget> pluginsById;

    /**
     * The "Enable" button.
     */
    private TButton enableButton;

    /**
     * The "Disable" button.
     */
    private TButton disableButton;

    /**
     * The "Settings" button.
     */
    private TButton settingsButton;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.  The window will be centered on screen.
     *
     * @param application the TApplication that manages this window
     */
    public PluginOptionsWindow(final XTWMApplication application) {

        // Register with the TApplication
        super(application, i18n.getString("windowTitle"), 0, 0, 68, 18, MODAL);

        plugins = addList(new ArrayList<String>(), 1, 3,
            getWidth() - 4, getHeight() - 8, null,
            new TAction() {
                // When the user navigates
                public void DO() {
                    refreshPluginList();
                }
            });

        enableButton = addButton(i18n.getString("enableButton"),
            getWidth() - 67, getHeight() - 4,

            new TAction() {
                public void DO() {
                    int index = plugins.getSelectedIndex();
                    PluginWidget plugin = pluginsById.get(index);
                    if (plugin != null) {
                        // TODO
                    }
                }
            }
        );

        disableButton = addButton(i18n.getString("disableButton"),
            getWidth() - 47, getHeight() - 4,

            new TAction() {
                public void DO() {
                    int index = plugins.getSelectedIndex();
                    PluginWidget plugin = pluginsById.get(index);
                    if (plugin != null) {
                        // TODO
                    }
                }
            }
        );

        settingsButton = addButton(i18n.getString("settingsButton"),
            getWidth() - 27, getHeight() - 4,

            new TAction() {
                public void DO() {
                    int index = plugins.getSelectedIndex();
                    PluginWidget plugin = pluginsById.get(index);
                    if (plugin != null) {
                        // TODO
                    }
                }
            }
        );

        addButton(i18n.getString("closeButton"),
            getWidth() - 11, getHeight() - 4,
            new TAction() {
                public void DO() {
                    getApplication().closeWindow(PluginOptionsWindow.this);
                }
            }
        );

        // Default to the plugins list
        activate(plugins);
        refreshPluginList();

        // Add shortcut text
        newStatusBar(i18n.getString("statusBar"));
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
        // Escape - behave like cancel
        if (keypress.equals(kbEsc)) {
            getApplication().closeWindow(this);
            return;
        }

        // Pass to my parent
        super.onKeypress(keypress);
    }

    /**
     * Method that subclasses can override to do processing when the UI is
     * idle.  Note that repainting is NOT assumed.  To get a refresh after
     * onIdle, call doRepaint().
     */
    @Override
    public void onIdle() {
        refreshPluginList();
    }

    // ------------------------------------------------------------------------
    // TWindow ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the options panel.
     */
    @Override
    public void draw() {
        // Draw window and border.
        super.draw();

        CellAttributes color = getTheme().getColor("ttext");

        hLineXY(2, 2, getWidth() - 4, ' ', color);
        putStringXY(2, 2, String.format("%-12s %c %12s %c %12s",
                i18n.getString("plugin"),
                GraphicsChars.VERTICAL_BAR,
                i18n.getString("enabled"),
                GraphicsChars.VERTICAL_BAR,
                i18n.getString("type")), color);

        hLineXY(2, 3, getWidth() - 4, GraphicsChars.DOUBLE_BAR, color);
        putCharXY(15, 3, 0x256A, color);
        putCharXY(29, 3, 0x256A, color);
        putCharXY(41, 3, 0x256A, color);
    }

    // ------------------------------------------------------------------------
    // PluginOptionsWindow ----------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Refresh the plugins window.
     */
    private void refreshPluginList() {
        List<String> pluginstrings = new ArrayList<String>();

        pluginsById = new HashMap<Integer, PluginWidget>();

        long now = System.currentTimeMillis();
        XTWMApplication app = (XTWMApplication) getApplication();

        // TODO
        /*
        for (PluginWidget plugin: ((MultiPluginWidget) app.getPluginWidget()).getPluginWidgets()) {
            SessionInfo sessionInfo = plugin.getSessionInfo();
            TelnetInputStream telnet = (TelnetInputStream) sessionInfo;
            long connect = sessionInfo.getStartTime();
            int hours = (int)  (((now - connect) / 1000) / 3600);
            int mins  = (int) ((((now - connect) / 1000) % 3600) / 60);
            int secs  = (int)  (((now - connect) / 1000) % 60);
            pluginstrings.add(String.format("%-12s %c %11s %c  %02d:%02d:%02d %c %10d",
                    sessionInfo.getUsername(),
                    GraphicsChars.VERTICAL_BAR,
                    (plugin.isReadOnly() ? "Read-Only" : "Read+Write"),
                    GraphicsChars.VERTICAL_BAR,
                    hours, mins, secs,
                    GraphicsChars.VERTICAL_BAR,
                    sessionInfo.getIdleTime()));

            pluginsById.put(pluginstrings.size() - 1, plugin);
        }
         */

        int oldIndex = plugins.getSelectedIndex();
        plugins.setList(pluginstrings);
        plugins.setSelectedIndex(Math.min(plugins.getMaxSelectedIndex(),
                Math.max(oldIndex, 0)));

        int index = plugins.getSelectedIndex();
        PluginWidget plugin = pluginsById.get(index);
        if (plugin == null) {
            // TODO
        } else {
            // TODO
        }
    }

}