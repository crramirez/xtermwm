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
package xtwm.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TButton;
import jexer.TList;
import jexer.TWidget;
import jexer.TWindow;
import jexer.bits.BorderStyle;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.bits.MnemonicString;
import jexer.event.TKeypressEvent;
import jexer.event.TResizeEvent;
import static jexer.TKeypress.*;

import xtwm.ui.XTWMApplication;
import xtwm.plugins.PluginWidget;
import xtwm.plugins.ScreensaverPlugin;

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

    /**
     * The plugins from the application.
     */
    private List<PluginWidget> widgets;

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
            getWidth() - 62, getHeight() - 4,

            new TAction() {
                public void DO() {
                    int index = plugins.getSelectedIndex();
                    PluginWidget plugin = pluginsById.get(index);
                    assert (plugin != null);
                    plugin.setPluginEnabled(true);
                    enableButton.setEnabled(false);
                    disableButton.setEnabled(true);
                    XTWMApplication app = (XTWMApplication) source.getApplication();
                    app.savePluginProperties(plugin);
                }
            }
        );

        disableButton = addButton(i18n.getString("disableButton"),
            getWidth() - 47, getHeight() - 4,

            new TAction() {
                public void DO() {
                    int index = plugins.getSelectedIndex();
                    PluginWidget plugin = pluginsById.get(index);
                    assert (plugin != null);
                    plugin.setPluginEnabled(false);
                    enableButton.setEnabled(true);
                    disableButton.setEnabled(false);
                    XTWMApplication app = (XTWMApplication) source.getApplication();
                    app.savePluginProperties(plugin);
                }
            }
        );

        settingsButton = addButton(i18n.getString("settingsButton"),
            getWidth() - 32, getHeight() - 4,

            new TAction() {
                public void DO() {
                    int index = plugins.getSelectedIndex();
                    PluginWidget plugin = pluginsById.get(index);
                    assert (plugin != null);
                    makePluginSettingsWindow(plugin);
                }
            }
        );

        addButton(i18n.getString("closeButton"),
            getWidth() - 17, getHeight() - 4,
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
        putStringXY(2, 2, String.format("%-16s %c %-8s %c %-12s",
                i18n.getString("plugin"),
                GraphicsChars.VERTICAL_BAR,
                i18n.getString("enabled"),
                GraphicsChars.VERTICAL_BAR,
                i18n.getString("type")), color);

        hLineXY(2, 3, getWidth() - 4, GraphicsChars.DOUBLE_BAR, color);
        putCharXY(19, 3, 0x256A, color);
        putCharXY(30, 3, 0x256A, color);
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

        if (widgets == null) {
            widgets = app.getWidgets();
            widgets.addAll(app.getScreensavers());
        }

        for (PluginWidget plugin: widgets) {
            String pluginType = "plugin";
            if (plugin instanceof ScreensaverPlugin) {
                pluginType = "screensaver";
            }
            pluginstrings.add(String.format("%-16s %c %-8s %c %-12s",
                    (new MnemonicString(plugin.getMenuMnemonic())).getRawLabel(),
                    GraphicsChars.VERTICAL_BAR,
                    (plugin.isPluginEnabled() ? i18n.getString("enabled") :
                        i18n.getString("disabled")),
                    GraphicsChars.VERTICAL_BAR,
                    pluginType));

            pluginsById.put(pluginstrings.size() - 1, plugin);
        }

        int oldIndex = plugins.getSelectedIndex();
        plugins.setList(pluginstrings);
        plugins.setSelectedIndex(Math.min(plugins.getMaxSelectedIndex(),
                Math.max(oldIndex, 0)));

        int index = plugins.getSelectedIndex();
        PluginWidget plugin = pluginsById.get(index);
        assert (plugin != null);

        // Switching the buttons around will cause focus to move to the list.
        // Let's try to keep focus on the button that was active.
        TWidget active = getParent().getActiveChild();
        if (plugin.isPluginEnabled()) {
            enableButton.setEnabled(false);
            disableButton.setEnabled(true);
        } else {
            enableButton.setEnabled(true);
            disableButton.setEnabled(false);
        }
        if (active.isEnabled()) {
            getParent().activate(active);
        }
    }

    /**
     * Make the window that will be used for the plugin settings UI.
     *
     * @param application the application
     * @return the window
     */
    private void makePluginSettingsWindow(final PluginWidget plugin) {
        XTWMApplication app = (XTWMApplication) getApplication();

        TWindow window = new TWindow(app, String.format("%s - %s",
                i18n.getString("settingsWindowTitle"),
                (new MnemonicString(plugin.getMenuMnemonic())).getRawLabel()),
            60, 20, TWindow.MODAL) {

            @Override
            public void draw() {
                super.draw();

                CellAttributes color = getTheme().getColor("ttext");
                drawBox(2, 2, getWidth() - 2, getHeight() - 4, color, color,
                    BorderStyle.SINGLE, false);
            }

            @Override
            public void onResize(final TResizeEvent resize) {
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
        app.getCurrentDesktop().addWindow(window);

        plugin.getPluginSettingsEditor(window);

        window.addButton(i18n.getString("saveButton"),
            window.getWidth() - 40, window.getHeight() - 4,
            new TAction() {
                public void DO() {
                    app.savePluginProperties(plugin);
                    getApplication().closeWindow(window);
                }
            }
        );

        window.addButton(i18n.getString("closeButton"),
            window.getWidth() - 30, window.getHeight() - 4,
            new TAction() {
                public void DO() {
                    getApplication().closeWindow(window);
                }
            }
        );
    }

}
