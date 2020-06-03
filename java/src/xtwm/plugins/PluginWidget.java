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

import jexer.TDesktop;
import jexer.TWidget;
import jexer.TWindow;
import jexer.TPanel;
import jexer.TSplitPane;
import jexer.event.TMenuEvent;
import jexer.event.TResizeEvent;
import jexer.menu.TMenu;

import xtwm.ui.Desktop;
import xtwm.ui.TiledTerminal;
import xtwm.ui.XTWMApplication;

/**
 * A PluginWidget is a plugin that can be optionally be instantiated in a
 * separate window and/or as part of a tiled panel.
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

    /**
     * If true, this plugin will be enabled.
     */
    protected boolean pluginEnabled = true;

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

    /**
     * Process menu events.
     */
    @Override
    public void onMenu(TMenuEvent event) {
        assert (getParent() != null);
        TSplitPane pane;
        TDesktop desktop;

        switch (event.getId()) {

        case XTWMApplication.MENU_TERMINAL_HORIZONTAL_SPLIT:
            pane = splitHorizontal(false, new TiledTerminal(getParent()));
            desktop = getApplication().getDesktop();
            if (desktop instanceof Desktop) {
                pane.setFocusFollowsMouse(((Desktop) desktop).
                    getFocusFollowsMouse());
            }
            getParent().setEchoKeystrokes(isEchoKeystrokes(), true);
            return;

        case XTWMApplication.MENU_TERMINAL_VERTICAL_SPLIT:
            pane = splitVertical(false, new TiledTerminal(getParent()));
            desktop = getApplication().getDesktop();
            if (desktop instanceof Desktop) {
                pane.setFocusFollowsMouse(((Desktop) desktop).
                    getFocusFollowsMouse());
            }
            getParent().setEchoKeystrokes(isEchoKeystrokes(), true);
            return;

        case XTWMApplication.MENU_TERMINAL_CLOSE:
            closeFromMenu();
            return;

        default:
            break;
        }

        // I didn't take it, pass it on.
        super.onMenu(event);
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
     * @return a short name, e.g. "A simple calendar."
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
     * Get the translated window title for this plugin.
     *
     * @return the title for the window when this widget is opened in the
     * Application | Widgets menu, or null if this widget should have no
     * title for its window.
     */
    public abstract String getWindowTitle();

    /**
     * See if this plugin is enabled.
     *
     * @return true if this plugin should be enabled
     */
    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    /**
     * Set plugin enabled.
     *
     * @param enabled if true, this plugin will be enabled
     */
    public void setPluginEnabled(final boolean pluginEnabled) {
        this.pluginEnabled = pluginEnabled;
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
    public void initialize(final XTWMApplication app) {
        this.app = app;
        pluginEnabled = getOption("enabled", "true").equals("true");
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
     * Get the window that will be used for this plugin when isWindowed() is
     * true.  The plugin will be reparented inside this window.
     *
     * @param application the application
     * @return the window
     */
    public TWindow getWindow(final XTWMApplication application) {
        TWindow window = new TWindow(application, getWindowTitle(),
            application.getScreen().getWidth(),
            application.getDesktopBottom() - application.getDesktopTop()) {

            public void onMenu(final TMenuEvent menu) {
                // Override the NOCLOSEBOX and HIDEONCLOSE behavior for a
                // widget window.  This permits a widget window to be
                // closeable from the menu even if it lacks the close button.
                if (menu.getId() == TMenu.MID_WINDOW_CLOSE) {
                    getApplication().closeWindow(this);
                    return;
                }
                super.onMenu(menu);
            }

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

    /**
     * Check if this widget is in a window.
     *
     * @return true if the widget is in a window
     */
    protected boolean isWindowed() {
        return (getParent() instanceof TWindow);
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

    /**
     * Called when the user selects Terminal | Close from the menu.  The
     * default implementation behaves like a TiledTerminal close.
     */
    public void closeFromMenu() {
        close();

        TDesktop desktop = getApplication().getDesktop();
        if (desktop instanceof Desktop) {
            ((Desktop) desktop).removePanel(this);
        }

        if (getParent() instanceof TSplitPane) {
            if (getParent().hasChild(this)) {
                ((TSplitPane) getParent()).removeSplit(this, true);
            }
        }
        if (getParent() instanceof TDesktop) {
            remove();
        }
    }

}
