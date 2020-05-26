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

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.ServiceLoader;

import jexer.TAction;
import jexer.TApplication;
import jexer.TDesktop;
import jexer.TEditColorThemeWindow;
import jexer.TExceptionDialog;
import jexer.THelpWindow;
import jexer.TInputBox;
import jexer.TMessageBox;
import jexer.TScreenOptionsWindow;
import jexer.TSplitPane;
import jexer.TStatusBar;
import jexer.TWidget;
import jexer.TWindow;
import jexer.backend.Backend;
import jexer.backend.SwingTerminal;
import jexer.bits.CellAttributes;
import jexer.bits.Color;
import jexer.bits.ColorTheme;
import jexer.event.TCommandEvent;
import jexer.event.TKeypressEvent;
import jexer.event.TMenuEvent;
import jexer.event.TResizeEvent;
import jexer.help.Topic;
import jexer.menu.TMenu;
import jexer.menu.TMenuItem;
import jexer.menu.TSubMenu;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

import xtwm.plugins.PluginLoader;
import xtwm.plugins.PluginWidget;

/**
 * The main Xterm Window Manager application.
 */
public class XTWMApplication extends TApplication {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(XTWMApplication.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Release version.
     */
    public static final String VERSION = "0.1.0";

    /**
     * The menu ID marking the beginning of application plugins.
     */
    private static final int APP_MENU_ID_MIN = 10000;

    /**
     * The menu ID marking the beginning of widget plugins.
     */
    private static final int WIDGET_MENU_ID_MIN = 20000;

    /*
     * Available menu commands.  Note that the package private items are
     * handled by other classes.
     */
    private static final int MENU_APPLICATION_PROGRAMS_SHELL            = 2010;
    private static final int MENU_APPLICATION_PROGRAMS_EDITOR           = 2011;
    private static final int MENU_APPLICATION_SETTINGS_DISPLAY          = 2030;
    private static final int MENU_APPLICATION_SETTINGS_COLORS           = 2031;
    private static final int MENU_APPLICATION_SETTINGS_ENVIRONMENT      = 2032;
    private static final int MENU_APPLICATION_SETTINGS_WINDOWS          = 2033;
    private static final int MENU_APPLICATION_SETTINGS_PLUGINS          = 2034;
    private static final int MENU_APPLICATION_SETTINGS_SAVE             = 2035;
    private static final int MENU_APPLICATION_SETTINGS_LOAD             = 2036;
    private static final int MENU_APPLICATION_RUN                       = 2091;
    private static final int MENU_APPLICATION_LOCK_SCREEN               = 2092;
    private static final int MENU_APPLICATION_EXIT                      = 2099;

    // Package private values are handled by TiledTerminal.
    private static final int MENU_TERMINAL_NEW_WINDOW                   = 2100;
            static final int MENU_TERMINAL_HORIZONTAL_SPLIT             = 2101;
            static final int MENU_TERMINAL_VERTICAL_SPLIT               = 2102;
    private static final int MENU_TERMINAL_SEND_KEYS_TO_ALL             = 2103;
            static final int MENU_TERMINAL_SESSION_SAVE_HTML            = 2104;
            static final int MENU_TERMINAL_SESSION_SAVE_TEXT            = 2105;
            static final int MENU_TERMINAL_SESSION_SEND_SIGTERM         = 2106;
            static final int MENU_TERMINAL_SESSION_SEND_OTHER_SIGNAL    = 2107;
            static final int MENU_TERMINAL_CLOSE                        = 2108;

    private static final int MENU_PANEL_SWITCH_TO                       = 2201;
    private static final int MENU_PANEL_NEXT                            = 2202;
    private static final int MENU_PANEL_PREVIOUS                        = 2203;
    private static final int MENU_PANEL_CLOSE                           = 2204;
    private static final int MENU_PANEL_SAVE_LAYOUT                     = 2205;
    private static final int MENU_PANEL_LOAD_LAYOUT                     = 2206;

    private static final int MENU_WINDOW_TO_DESKTOP                     = 2300;
    private static final int MENU_WINDOW_ON_ALL_DESKTOPS                = 2301;
    private static final int MENU_WINDOW_NEXT_DESKTOP                   = 2302;
    private static final int MENU_WINDOW_PREVIOUS_DESKTOP               = 2303;

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The global options.
     */
    private Properties options = null;

    /**
     * The filename to save options to.
     */
    private String configFilename = null;

    /**
     * The virtual desktops.
     */
    private ArrayList<VirtualDesktop> desktops = new ArrayList<VirtualDesktop>();

    /**
     * The current visible desktop, as an index into desktops.
     */
    private int desktopIndex = -1;

    /**
     * The desired number of virtual desktops.
     */
    private int desktopCount = 4;

    /**
     * If true, display a clock in the top-right menu bar line.
     */
    private boolean menuTrayClock = true;

    /**
     * The menu bar clock time format.
     */
    private SimpleDateFormat clockFormat = null;

    /**
     * If true, display the desktop number in the top-right menu bar line.
     */
    private boolean menuTrayDesktop = true;

    /**
     * The Application | Programs submenu.
     */
    private TSubMenu programsMenu;

    /**
     * The Application | Widgets submenu.
     */
    private TSubMenu widgetsMenu;

    /**
     * The directory that plugins have write access to.
     */
    private File pluginDataDir;

    /**
     * The plugin options.
     */
    private Map<String, Properties> pluginOptions;

    /**
     * A map of menu ID to plugin class, for the Application | Programs menu.
     */
    private Map<Integer, Class<? extends PluginWidget>> pluginAppMenuIds;

    /**
     * A map of menu ID to plugin class, for the Application | Widgets menu.
     */
    private Map<Integer, Class<? extends PluginWidget>> pluginWidgetMenuIds;


    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param backendType one of the TApplication.BackendType values
     * @throws Exception if TApplication can't instantiate the Backend.
     */
    public XTWMApplication(final BackendType backendType) throws Exception {
        super(backendType);

        XTWMApplicationImpl();
    }

    /**
     * Public constructor.
     *
     * @param backendType one of the TApplication.BackendType values
     * @param minimumWidth minimum width of window
     * @param minimumHeight minimum height of window
     * @param fontSize the size in points
     * @throws Exception if TApplication can't instantiate the Backend.
     */
    public XTWMApplication(final BackendType backendType,
        final int minimumWidth, final int minimumHeight,
        final int fontSize) throws Exception {

        super(backendType, Math.max(80, minimumWidth),
            Math.max(25, minimumHeight), fontSize);

        XTWMApplicationImpl();
    }

    /**
     * Public constructor.
     *
     * @param backend a Backend that is already ready to go.
     */
    public XTWMApplication(final Backend backend) {
        super(backend);

        XTWMApplicationImpl();
    }

    /**
     * Actual constructor logic.
     */
    private void XTWMApplicationImpl() {
        // We set some default colors here, so that saveOptions() can see
        // them.
        setXTWMColors();
        initializeOptions();
        addAllWidgets();
        getBackend().setTitle(i18n.getString("frameTitle"));
        loadPlugins();
        addDesktops();

        if (getBackend() instanceof jexer.backend.ECMA48Backend) {
            // For the Xterm backend, force a repaint so that the clock will
            // be updated.
            addTimer(500, true,
                new TAction() {
                    public void DO() {
                        XTWMApplication.this.doRepaint();
                    }
                }
            );
        }

    }

    /**
     * Set XTWM colors into the color theme.
     */
    private void setXTWMColors() {
        getTheme().setColorFromString("tmenu", "blue on white");
        getTheme().setColorFromString("tmenu.highlighted", "white on red");
        getTheme().setColorFromString("tmenu.mnemonic.highlighted",
            "white on red");
    }

    // ------------------------------------------------------------------------
    // TApplication behavior --------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Function called immediately before the screen is drawn.  This can be
     * used by subclasses of TApplication to update things, for example to
     * set menuTrayText.
     */
    @Override
    protected void onPreDraw() {
        String text = "";

        if (menuTrayClock) {
            text = clockFormat.format(new Date());
            if (menuTrayDesktop) {
                text += " ";
            }
        }
        if (menuTrayDesktop) {
            text += String.format("[%d]", desktopIndex);
        }
        menuTrayText = text;

        List<TWindow> windows = getCurrentDesktop().getWindows();

        // Special case: if the desktop has tiled terminals, and any windows
        // on the screen are not part of a virtual desktop (i.e. the desktop
        // pager is the only window visible), then permit the blinking cursor
        // on the desktop to be visible.
        if ((windows.size() == 0)
            && (getDesktop().getChildren().size() > 0)
        ) {
            // Permit the desktop cursor to be visible, even if the top-most
            // actual window does not have a cursor.
            desktopCanHaveCursor = true;
        } else {
            desktopCanHaveCursor = false;
        }

    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle menu events.
     *
     * @param menu menu event
     * @return if true, the event was processed and should not be passed onto
     * a window
     */
    @Override
    public boolean onMenu(final TMenuEvent menu) {

        TInputBox inputBox = null;
        TWindow window = null;
        TWidget widget = null;

        // Dispatch menu event
        switch (menu.getId()) {

        case MENU_APPLICATION_PROGRAMS_SHELL:
            // Spawn shell
            getCurrentDesktop().addWindow(new TerminalWindow(this));
            return true;

        case MENU_APPLICATION_PROGRAMS_EDITOR:
            // TODO
            return true;

        case MENU_APPLICATION_SETTINGS_DISPLAY:
            new TScreenOptionsWindow(this) {
                /*
                 * We have finished editing screen options, now save to the
                 * xtwm.properties file.
                 */
                @Override
                public void onClose() {
                    /*
                     * TODO:
                     *
                     *   jexer.ECMA48.sixel
                     *   jexer.ECMA48.sixelSharedPalette
                     *   jexer.ECMA48.wideCharImages
                     *   jexer.ECMA48.rgbColor
                     *   jexer.Swing.tripleBuffer
                     *   jexer.Swing.cursorStyle
                     *   jexer.Swing.mouseStyle
                     */

                    saveOptions();
                    super.onClose();
                }
            };
            return true;

        case MENU_APPLICATION_SETTINGS_COLORS:
            new TEditColorThemeWindow(this) {
                /*
                 * We have finished editing colors, now save to the
                 * xtwm.properties file.
                 */
                @Override
                public void onClose() {
                    for (String key: getTheme().getColorNames()) {
                        options.setProperty("colors." + key,
                            getTheme().getColor(key).toString());
                    }
                    saveOptions();
                    super.onClose();
                }
            };
            return true;

        case MENU_APPLICATION_SETTINGS_ENVIRONMENT:
            new ApplicationOptionsWindow(this);
            return true;

        case MENU_APPLICATION_SETTINGS_WINDOWS:
            // TODO
            return true;

        case MENU_APPLICATION_SETTINGS_PLUGINS:
            // TODO
            return true;

        case MENU_APPLICATION_SETTINGS_LOAD:
            try {
                String filename = fileOpenBox(configFilename == null ?
                    System.getProperty("user.home") + "/.xtwm" :
                    (new File(configFilename)).getParent());
                 if (filename != null) {
                     loadOptions(filename);
                 }
            } catch (IOException e) {
                // Show this exception to the user.
                new TExceptionDialog(this, e);
            }
            return true;

        case MENU_APPLICATION_SETTINGS_SAVE:
            saveOptions();
            return true;

        case MENU_APPLICATION_RUN:
            inputBox = inputBox(i18n.getString("runInputBoxTitle"),
                i18n.getString("runInputBoxCaption"), "",
                TInputBox.Type.OKCANCEL);
            if (inputBox.isOk()) {
                String command = inputBox.getText();
                if (command.length() > 0) {
                    getCurrentDesktop().addWindow(new TerminalWindow(this,
                            command));
                }
            }
            return true;

        case MENU_APPLICATION_LOCK_SCREEN:
            lockScreen();
            return true;

        case MENU_APPLICATION_EXIT:
            // Post a quit command
            postMenuEvent(new TCommandEvent(cmExit));
            return true;

        case MENU_TERMINAL_NEW_WINDOW:
            TDesktop desktop = getDesktop();
            if (desktop.getChildren().size() == 0) {
                new TiledTerminal(desktop);
                desktop.setEchoKeystrokes(desktop.isEchoKeystrokes(), true);
            }
            return true;

        case MENU_TERMINAL_SEND_KEYS_TO_ALL:
            getDesktop().setEchoKeystrokes(!getDesktop().isEchoKeystrokes(),
                true);
            getMenuItem(MENU_TERMINAL_SEND_KEYS_TO_ALL).setChecked(getDesktop().isEchoKeystrokes());
            return true;

        case MENU_PANEL_SWITCH_TO:
            // TODO
            return true;

        case MENU_PANEL_NEXT:
            if (getDesktop() instanceof Desktop) {
                ((Desktop) getDesktop()).nextPanel();
            }
            return true;

        case MENU_PANEL_PREVIOUS:
            if (getDesktop() instanceof Desktop) {
                ((Desktop) getDesktop()).previousPanel();
            }
            return true;

        case MENU_PANEL_CLOSE:
            widget = getDesktop().getActiveChild();
            if ((widget != null) && (widget != getDesktop())) {
                widget.remove();
            }
            return true;

        case MENU_PANEL_SAVE_LAYOUT:
            // TODO
            return true;

        case MENU_PANEL_LOAD_LAYOUT:
            // TODO
            return true;

        case MENU_WINDOW_TO_DESKTOP:
            // TODO
            return true;

        case MENU_WINDOW_ON_ALL_DESKTOPS:
            window = getActiveWindow();
            if (window != null) {
                if (getCurrentDesktop().hasWindow(window)) {
                    for (int i = 0; i < desktops.size(); i++) {
                        desktops.get(i).removeWindow(window);
                    }
                } else {
                    getCurrentDesktop().addWindow(window);
                }
            }
            return true;

        case MENU_WINDOW_NEXT_DESKTOP:
            switchToNextDesktop();
            return true;

        case MENU_WINDOW_PREVIOUS_DESKTOP:
            switchToPreviousDesktop();
            return true;

        case TMenu.MID_HELP_HELP:
            getCurrentDesktop().addWindow(new THelpWindow(this,
                    THelpWindow.HELP_HELP));
            return true;

        case TMenu.MID_HELP_CONTENTS:
            getCurrentDesktop().addWindow(new THelpWindow(this,
                    helpFile.getTableOfContents()));
            return true;

        case TMenu.MID_HELP_INDEX:
            getCurrentDesktop().addWindow(new THelpWindow(this,
                    helpFile.getIndex()));
            return true;

        case TMenu.MID_HELP_SEARCH:
            inputBox = inputBox(i18n.getString("searchHelpInputBoxTitle"),
                i18n.getString("searchHelpInputBoxCaption"), "",
                TInputBox.Type.OKCANCEL);
            if (inputBox.isOk()) {
                getCurrentDesktop().addWindow(new THelpWindow(this,
                        helpFile.getSearchResults(inputBox.getText())));
            }
            return true;

        case TMenu.MID_HELP_PREVIOUS:
            if (helpTopics.size() > 1) {
                Topic previous = helpTopics.remove(helpTopics.size() - 2);
                helpTopics.remove(helpTopics.size() - 1);
                getCurrentDesktop().addWindow(new THelpWindow(this, previous));
            } else {
                getCurrentDesktop().addWindow(new THelpWindow(this,
                        helpFile.getTableOfContents()));
            }
            return true;

        default:
            break;
        }

        if ((menu.getId() >= APP_MENU_ID_MIN)
            && (menu.getId() < WIDGET_MENU_ID_MIN)
        ) {
            // TODO: spawn the application for the plugin

            return true;
        } else if (menu.getId() >= WIDGET_MENU_ID_MIN) {
            // Add the widget to a new window.
            Class<? extends PluginWidget> pluginClass = null;
            pluginClass = pluginWidgetMenuIds.get(menu.getId());

            if (pluginClass != null) {
                PluginWidget plugin = null;
                try {
                    plugin = pluginClass.getConstructor().newInstance();
                    plugin.initialize(this);
                    window = plugin.getWindow(this);
                    plugin.setParent(window, false);
                    plugin.setDimensions(0, 0,
                        plugin.getPreferredWidth(),
                        plugin.getPreferredHeight());
                    window.setDimensions(window.getX(), window.getY(),
                        plugin.getWidth() + 2, plugin.getHeight() + 2);
                    window.setResizable(plugin.isResizable());
                } catch (Exception e) {
                    // Show this exception to the user.
                    new TExceptionDialog(this, e);
                }
            }
            return true;
        } else {
            // Not handled here, pass it on.
            return super.onMenu(menu);
        }
    }

    /**
     * Method that TApplication subclasses can override to handle menu or
     * posted command events.
     *
     * @param command command event
     * @return if true, this event was consumed
     */
    @Override
    protected boolean onCommand(final TCommandEvent command) {
        // Override cmExit to show a different dialog.
        if (command.equals(cmExit)) {
            if (getOption("xtwm.confirmOnExit", "true").equals("true")) {
                if (messageBox(i18n.getString("exitDialogTitle"),
                        i18n.getString("exitDialogText"),
                        TMessageBox.Type.YESNO).isYes()) {

                    exit();
                }
                return true;
            }
            // No confirm on exit, just exit.
            exit();
            return true;
        }

        return super.onCommand(command);
    }

    /**
     * Method that TApplication subclasses can override to handle keystrokes.
     *
     * @param keypress keystroke event
     * @return if true, this event was consumed
     */
    @Override
    protected boolean onKeypress(final TKeypressEvent keypress) {
        // Nothing to override yet.
        return super.onKeypress(keypress);
    }

    // ------------------------------------------------------------------------
    // UI behavior ------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Display the about dialog.
     */
    @Override
    protected void showAboutDialog() {
        String version = getClass().getPackage().getImplementationVersion();
        if (version == null) {
            // This is Java 9+, use a hardcoded string here.
            version = VERSION;
        }
        messageBox(i18n.getString("aboutDialogTitle"),
            MessageFormat.format(i18n.getString("aboutDialogText"), version),
            TMessageBox.Type.OK);
    }

    /**
     * Add the menus for the UI.
     */
    private void addMenus() {

        // Tool menu
        TMenu toolMenu = addMenu(i18n.getString("toolMenuTitle"));
        toolMenu.addDefaultItem(TMenu.MID_REPAINT);
        toolMenu.addDefaultItem(TMenu.MID_VIEW_IMAGE);
        TStatusBar toolStatusBar = toolMenu.newStatusBar(i18n.
            getString("toolMenuStatus"));
        toolStatusBar.addShortcutKeypress(kbF1, cmHelp, i18n.getString("Help"));

        // Application menu ---------------------------------------------------

        TMenu applicationMenu = addMenu(i18n.getString("applicationMenuTitle"));

        TSubMenu subPrograms = applicationMenu.addSubMenu(i18n.
            getString("applicationPrograms"));
        subPrograms.addItem(MENU_APPLICATION_PROGRAMS_SHELL,
            i18n.getString("applicationProgramsShell"), kbCtrlS);
        subPrograms.addItem(MENU_APPLICATION_PROGRAMS_EDITOR,
            i18n.getString("applicationProgramsEditor"));
        programsMenu = subPrograms;

        widgetsMenu = applicationMenu.addSubMenu(i18n.
            getString("applicationWidgets"));

        applicationMenu.addSeparator();

        TSubMenu subSettings = applicationMenu.addSubMenu(i18n.
            getString("applicationSettings"));
        subSettings.addItem(MENU_APPLICATION_SETTINGS_DISPLAY,
            i18n.getString("applicationSettingsDisplay"));
        subSettings.addItem(MENU_APPLICATION_SETTINGS_COLORS,
            i18n.getString("applicationSettingsColors"));
        subSettings.addItem(MENU_APPLICATION_SETTINGS_ENVIRONMENT,
            i18n.getString("applicationSettingsEnvironment"));
        subSettings.addItem(MENU_APPLICATION_SETTINGS_WINDOWS,
            i18n.getString("applicationSettingsWindows"));
        subSettings.addItem(MENU_APPLICATION_SETTINGS_PLUGINS,
            i18n.getString("applicationSettingsPlugins"));
        subSettings.addSeparator();
        subSettings.addItem(MENU_APPLICATION_SETTINGS_SAVE,
            i18n.getString("applicationSettingsSave"));
        subSettings.addItem(MENU_APPLICATION_SETTINGS_LOAD,
            i18n.getString("applicationSettingsLoad"));

        applicationMenu.addSeparator();
        applicationMenu.addItem(MENU_APPLICATION_RUN,
            i18n.getString("applicationRun"), kbCtrlR);
        applicationMenu.addItem(MENU_APPLICATION_LOCK_SCREEN,
            i18n.getString("applicationLockScreen"));
        applicationMenu.addItem(MENU_APPLICATION_EXIT,
            i18n.getString("applicationExit"), kbCtrlQ);
        TStatusBar applicationStatusBar = applicationMenu.newStatusBar(i18n.
            getString("applicationMenuStatus"));
        applicationStatusBar.addShortcutKeypress(kbF1, cmHelp,
            i18n.getString("Help"));

        // Edit menu ----------------------------------------------------------

        addEditMenu();

        // Terminal menu ------------------------------------------------------

        TMenu terminalMenu = addMenu(i18n.getString("terminalMenuTitle"));
        terminalMenu.addItem(MENU_TERMINAL_NEW_WINDOW,
            i18n.getString("terminalNewWindow"));
        terminalMenu.addItem(MENU_TERMINAL_HORIZONTAL_SPLIT,
            i18n.getString("terminalHorizontalSplit"));
        terminalMenu.addItem(MENU_TERMINAL_VERTICAL_SPLIT,
            i18n.getString("terminalVerticalSplit"));
        terminalMenu.addSeparator();
        TMenuItem item = terminalMenu.addItem(MENU_TERMINAL_SEND_KEYS_TO_ALL,
            i18n.getString("terminalSendKeysToAll"));
        item.setCheckable(true);
        terminalMenu.addSeparator();
        TSubMenu subSession = terminalMenu.addSubMenu(i18n.
            getString("terminalSession"));
        subSession.addItem(MENU_TERMINAL_SESSION_SAVE_HTML,
            i18n.getString("terminalSessionSaveHTML"));
        subSession.addItem(MENU_TERMINAL_SESSION_SAVE_TEXT,
            i18n.getString("terminalSessionSaveText"));
        subSession.addItem(MENU_TERMINAL_SESSION_SEND_SIGTERM,
            i18n.getString("terminalSessionSendSIGTERM"));
        subSession.addItem(MENU_TERMINAL_SESSION_SEND_OTHER_SIGNAL,
            i18n.getString("terminalSessionSendOtherSignal"));

        terminalMenu.addSeparator();
        terminalMenu.addItem(MENU_TERMINAL_CLOSE,
            i18n.getString("terminalClose"));

        TStatusBar terminalStatusBar = terminalMenu.newStatusBar(i18n.
            getString("terminalMenuStatus"));
        terminalStatusBar.addShortcutKeypress(kbF1, cmHelp,
            i18n.getString("Help"));

        // Panel menu ---------------------------------------------------------

        TMenu panelMenu = addMenu(i18n.getString("panelMenu"));
        panelMenu.addItem(MENU_PANEL_SWITCH_TO,
            i18n.getString("panelSwitchTo"));
        panelMenu.addSeparator();
        panelMenu.addItem(MENU_PANEL_NEXT, i18n.getString("panelNext"),
            kbShiftF3);
        panelMenu.addItem(MENU_PANEL_PREVIOUS, i18n.getString("panelPrevious"),
            kbShiftF4);
        panelMenu.addItem(MENU_PANEL_CLOSE, i18n.getString("panelClose"));
        panelMenu.addSeparator();
        panelMenu.addItem(MENU_PANEL_SAVE_LAYOUT,
            i18n.getString("panelSaveLayout"));
        panelMenu.addItem(MENU_PANEL_LOAD_LAYOUT,
            i18n.getString("panelLoadLayout"));
        TStatusBar panelStatusBar = panelMenu.newStatusBar(i18n.
            getString("panelMenuStatus"));
        panelStatusBar.addShortcutKeypress(kbF1, cmHelp,
            i18n.getString("Help"));

        // Window menu --------------------------------------------------------

        TMenu windowMenu = addMenu(i18n.getString("windowMenu"));
        windowMenu.addItem(TMenu.MID_TILE, i18n.getString("windowTile"));
        windowMenu.addItem(TMenu.MID_CASCADE, i18n.getString("windowCascade"));
        windowMenu.addItem(TMenu.MID_CLOSE_ALL,
            i18n.getString("windowCloseAll"));
        windowMenu.addSeparator();
        windowMenu.addItem(TMenu.MID_WINDOW_MOVE, i18n.getString("windowMove"),
            kbShiftF7);
        windowMenu.addItem(TMenu.MID_WINDOW_ZOOM, i18n.getString("windowZoom"),
            kbShiftF8);
        windowMenu.addItem(TMenu.MID_WINDOW_NEXT, i18n.getString("windowNext"),
            kbShiftF5);
        windowMenu.addItem(TMenu.MID_WINDOW_PREVIOUS,
            i18n.getString("windowPrevious"), kbShiftF6);
        windowMenu.addItem(TMenu.MID_WINDOW_CLOSE,
            i18n.getString("windowClose"), kbCtrlW);
        windowMenu.addSeparator();
        windowMenu.addItem(MENU_WINDOW_TO_DESKTOP,
            i18n.getString("windowToDesktop"));
        windowMenu.addItem(MENU_WINDOW_ON_ALL_DESKTOPS,
            i18n.getString("windowOnAllDesktops"));
        windowMenu.addSeparator();
        windowMenu.addItem(MENU_WINDOW_NEXT_DESKTOP,
            i18n.getString("windowNextDesktop"), kbShiftF11);
        windowMenu.addItem(MENU_WINDOW_PREVIOUS_DESKTOP,
            i18n.getString("windowPreviousDesktop"), kbShiftF12);

        TStatusBar windowStatusBar = windowMenu.newStatusBar(i18n.
            getString("windowMenuStatus"));
        windowStatusBar.addShortcutKeypress(kbF1, cmHelp,
            i18n.getString("Help"));

        // Help menu ----------------------------------------------------------

        addHelpMenu();

    }

    /**
     * Add all the widgets of the UI.
     */
    private void addAllWidgets() {
        addMenus();
    }

    /**
     * Setup the virtual desktops.
     */
    private void addDesktops() {
        // We have two desktops by default:
        // 0 - The screensaver desktop.  No windows are created on it.
        // 1 - The first normal desktop.
        desktops.add(new VirtualDesktop(this));
        desktopIndex = 1;

        // Set a different color for each desktop.  Eventually expose these
        // colors.
        for (int i = 1; i <= desktopCount; i++) {
            VirtualDesktop desktop = new VirtualDesktop(this);
            desktops.add(desktop);

            CellAttributes desktopAttr = new CellAttributes();
            switch ((i - 1) % 4) {
            case 0:
                desktopAttr.setForeColor(Color.BLUE);
                break;
            case 1:
                desktopAttr.setForeColor(Color.CYAN);
                break;
            case 2:
                desktopAttr.setForeColor(Color.MAGENTA);
                break;
            case 3:
                desktopAttr.setForeColor(Color.RED);
                break;
            }
            desktopAttr.setBackColor(Color.WHITE);
            desktopAttr.setBold(false);
            desktop.getDesktop().setAttributes(desktopAttr);
        }
        setDesktop(getCurrentDesktop().getDesktop(), true);
    }

    // ------------------------------------------------------------------------
    // Options support --------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Initialize the options.
     */
    private void initializeOptions() {
        options = new Properties(getOptionDefaults());

        /*
         * See if xtwm directory exists, and if not try to create it.
         */
        String homeDir = System.getProperty("user.home");
        String configDir = options.getProperty("xtwm.configDir");
        File rcDir = null;
        pluginDataDir = null;

        if (configDir == null) {
            configFilename = homeDir + "/.xtwm/xtwm.properties";
            rcDir = new File(homeDir, ".xtwm");
        } else {
            // configDir is set, either from the default xtwm.properties, or
            // by another xtwm.properties upstream in the classpath.
            rcDir = new File(configDir.replace("$HOME", homeDir));
            configFilename = rcDir.getPath() + "/xtwm.properties";
        }

        if (rcDir.isFile()) {
            // A file exists where we expect ~/.xtwm to be.  The user will
            // have to specify a new configFilename when they try to load or
            // save.
            configFilename = null;
        } else if (!rcDir.exists()) {
            // ~/.xtwm needs to be created.
            rcDir.mkdir();
        }

        pluginDataDir = new File(rcDir, "plugindata");
        if (pluginDataDir.isFile()) {
            // A file exists where we expect ~/.xtwm/plugindata to be.
            // Plugins will not be able to read/write data.
            pluginDataDir = null;
        } else if (!pluginDataDir.exists()) {
            // ~/.xtwm/plugindata needs to be created.
            pluginDataDir.mkdir();
        }

        if (configFilename != null) {
            File configFile = new File(configFilename);
            if (!configFile.exists()) {
                // xtwm.properties needs to be created.
                saveOptions();
            } else if (configFile.isDirectory()) {
                // A directory exists where we expect xtwm.properties to be.
                // The user will have to specify a new configFilename when
                // they try to load or save.
                configFilename = null;
            }

            if (configFile.isFile()) {
                // xtwm.properties is here, let's use it.
                loadOptions(configFilename);
            }
        }
    }

    /**
     * Get the default values for the supported options.
     *
     * @return the default values
     */
    private Properties getOptionDefaults() {
        // Load xtwm.properties into the default properties.
        Properties defaults = new Properties();
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        try {
            defaults.load(loader.getResourceAsStream("xtwm.properties"));
        } catch (IOException e) {
            // Show this exception to the user.
            new TExceptionDialog(this, e);
        }
        return defaults;
    }

    /**
     * Load options from a file.
     *
     * @param filename the name of the file to read from
     */
    private void loadOptions(final String filename) {
        if (!(new File(filename)).isFile()) {
            return;
        }

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(filename);
            options.load(fileReader);
        } catch (IOException e) {
            // Show this exception to the user.
            new TExceptionDialog(this, e);
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    // SQUASH
                }
                fileReader = null;
            }
        }

        resolveOptions();
    }

    /**
     * Set default options.  Note package private access.
     */
    void setDefaultOptions() {

        // Application options, keep these in sync with xtwm.properties.
        setOption("desktop.count", "4");
        setOption("desktop.pager", "true");
        setOption("editor.external.new", "$VISUAL");
        setOption("editor.external.open", "$VISUAL {0}");
        setOption("editor.internal.backspaceUnindents", "true");
        setOption("editor.internal.indentLevel", "4");
        setOption("editor.internal.saveWithTabs", "false");
        setOption("editor.internal.trimWhitespace", "true");
        setOption("editor.internal.undoLevel", "50");
        setOption("jexer.ECMA48.rgbColor", "false");
        setOption("jexer.ECMA48.sixel", "true");
        setOption("jexer.ECMA48.wideCharImages", "true");
        setOption("jexer.Swing.cursorStyle", "underline");
        setOption("jexer.Swing.mouseStyle", "default");
        setOption("jexer.Swing.tripleBuffer", "true");
        setOption("jexer.TTerminal.closeOnExit", "true");
        setOption("jexer.TTerminal.ptypipe", "auto");
        setOption("jexer.TTerminal.scrollbackMax", "2000");
        setOption("menuTray.clock", "true");
        setOption("menuTray.clock.format", "h:mm:ss a");
        setOption("menuTray.desktop", "true");
        setOption("panel.focusFollowsMouse", "false");
        setOption("screensaver.lock", "true");
        setOption("screensaver.timeout", "600");
        setOption("ui.font.adjustHeight", "0");
        setOption("ui.font.adjustWidth", "0");
        setOption("ui.font.adjustX", "0");
        setOption("ui.font.adjustY", "0");
        setOption("ui.font.name", "");
        setOption("ui.font.size", "20");
        setOption("window.focusFollowsMouse", "false");
        setOption("window.smartPlacement", "true");
        setOption("xtwm.confirmOnExit", "true");
        setOption("xtwm.hideStatusLine", "true");
        setOption("xtwm.hideTextMouse", "swing");
        setOption("xtwm.lockScreenPassword", "");
        setOption("xtwm.maximizeOnSwing", "true");
        setOption("xtwm.useExternalEditor", "false");

        // Colors
        getTheme().setDefaultTheme();
        setXTWMColors();
        for (String key: getTheme().getColorNames()) {
            options.setProperty("colors." + key,
                getTheme().getColor(key).toString());
        }
    }

    /**
     * Save options to the user preferences file.  Note package private
     * access.
     */
    void saveOptions() {
        assert (configFilename != null);

        // Read the shipped xtwm.properties, and replace the values with the
        // current option values.
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        BufferedReader reader = null;
        FileWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(loader.
                    getResourceAsStream("xtwm.properties")));
            writer = new FileWriter(configFilename);

            for (String line = reader.readLine(); line != null;
                 line = reader.readLine()) {

                if ((line.indexOf('=') == -1)
                    || (line.trim().startsWith("#"))
                ) {
                    // Comment or non-key-value line, write it and move on.
                    writer.write(line);
                    writer.write("\n");
                    continue;
                }

                // key-value line, replace value with actual.
                String key = line.substring(0, line.indexOf('=')).trim();
                String value = options.getProperty(key);
                writer.write(String.format("%s = %s\n", key, value));
            }

            // Save the colors.
            for (String key: getTheme().getColorNames()) {
                CellAttributes value = getTheme().getColor(key);
                writer.write(String.format("colors.%s = %s\n", key, value));
            }

        } catch (IOException e) {
            // Show this exception to the user.
            new TExceptionDialog(this, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // SQUASH
                }
                reader = null;
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // SQUASH
                }
                writer = null;
            }
        }
    }

    /**
     * Reset global variables to match loaded options.  Note package private
     * access.
     */
    void resolveOptions() {

        // Put some of the option values in other places.
        for (Object keyObj: options.keySet()) {
            String key = (String) keyObj;

            // colors.* is copied into the color theme.
            if (key.startsWith("colors.")) {
                String colorKey = key.substring(7);
                String colorValue = options.getProperty(key);

                /*
                System.err.println("colorKey '" + colorKey + "' colorValue '"
                    + colorValue + "'");
                */

                getTheme().setColorFromString(colorKey, colorValue);
            }

            // jexer.* is copied into the main runtime properties.
            if (key.startsWith("jexer.")) {
                System.setProperty(key, options.getProperty(key));
            }
        }
        // We may have changed some Jexer options, let the backend see those
        // changes.
        getBackend().reloadOptions();

        // Now reset any XTWM variables based on option values.
        setHideStatusBar(getOption("xtwm.hideStatusLine").equals("true"));
        menuTrayClock = getOption("menuTray.clock").equals("true");
        clockFormat = new SimpleDateFormat(getOption("menuTray.clock.format"));
        menuTrayDesktop = getOption("menuTray.desktop").equals("true");
        smartWindowPlacement = getOption("window.smartPlacement",
            "true").equals("true");
        setFocusFollowsMouse(getOption("window.focusFollowsMouse",
                "true").equals("true"));

        // Display options
        if (getScreen() instanceof SwingTerminal) {
            SwingTerminal terminal = (SwingTerminal) getScreen();
            if (!options.getProperty("ui.font.name", "").equals("")) {
                try {
                    terminal.setFont(new Font(options.getProperty(
                            "ui.font.name"), Font.PLAIN,
                            Integer.parseInt(options.getProperty(
                                "ui.font.size"))));
                } catch (NumberFormatException e) {
                    // SQUASH
                }
            } else {
                try {
                    terminal.setFontSize(Integer.parseInt(
                        options.getProperty("ui.font.size")));
                } catch (NumberFormatException e) {
                    // SQUASH
                }
            }
            try {
                int adjustX = Integer.parseInt(options.getProperty(
                    "ui.font.adjustX"));
                if (adjustX != 0) {
                    terminal.setTextAdjustX(adjustX);
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
            try {
                int adjustY = Integer.parseInt(options.getProperty(
                    "ui.font.adjustY"));
                if (adjustY != 0) {
                    terminal.setTextAdjustY(adjustY);
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
            try {
                int adjustWidth = Integer.parseInt(options.getProperty(
                    "ui.font.adjustWidth"));
                if (adjustWidth != 0) {
                    terminal.setTextAdjustWidth(adjustWidth);
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
            try {
                int adjustHeight = Integer.parseInt(options.getProperty(
                    "ui.font.adjustHeight"));
                if (adjustHeight != 0) {
                    terminal.setTextAdjustHeight(adjustHeight);
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
        } // if (getScreen() instanceof SwingTerminal)
    }

    /**
     * Get an option value.
     *
     * @param key name of the option
     * @return the option value, or null if it is undefined
     */
    public String getOption(final String key) {
        return options.getProperty(key);
    }

    /**
     * Get an option value.
     *
     * @param key name of the option
     * @param defaultValue the value to return if the option is not defined
     * @return the option value, or defaultValue
     */
    public String getOption(final String key, final String defaultValue) {
        String result = options.getProperty(key);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    /**
     * Set an option value.
     *
     * @param key name of the option
     * @param value the new the option value
     */
    public void setOption(final String key, final String value) {
        options.setProperty(key, value);
    }

    // ------------------------------------------------------------------------
    // XTWMApplication --------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Lock the screen.
     */
    private void lockScreen() {
        setHideMenuBar(true);
        setHideStatusBar(true);

        String lockScreenPassword = getOption("xtwm.lockPassword", "");

        for (;;) {
            TInputBox inputBox = inputBox(i18n.
                getString("lockScreenInputBoxTitle"),
                i18n.getString("lockScreenInputBoxCaption"), "",
                TInputBox.Type.OKCANCEL);
            if (inputBox.isOk()) {
                String password = inputBox.getText();
                if (lockScreenPassword.length() > 0) {
                    if (password.length() > 0) {
                        if (password.equals(lockScreenPassword)) {
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
        } // for (;;)

        setHideMenuBar(false);
        setHideStatusBar(getOption("xtwm.hideStatusLine",
                "true").equals("true"));
    }

    // Desktop management -----------------------------------------------------

    /**
     * Get the current desktop.
     *
     * @return the virtual desktop
     */
    public VirtualDesktop getCurrentDesktop() {
        return desktops.get(desktopIndex);
    }

    /**
     * Switch to the next desktop.
     */
    public void switchToNextDesktop() {
        assert (desktops.size() >= 2);

        if (desktops.size() == 2) {
            return;
        }

        getCurrentDesktop().hide();
        desktopIndex++;
        if (desktopIndex == desktops.size()) {
            desktopIndex = 1;
        }
        getCurrentDesktop().show();
        setDesktop(getCurrentDesktop().getDesktop(), false);
        getMenuItem(MENU_TERMINAL_SEND_KEYS_TO_ALL).setChecked(getDesktop().isEchoKeystrokes());
    }

    /**
     * Switch to the previous desktop.
     */
    public void switchToPreviousDesktop() {
        assert (desktops.size() >= 2);

        if (desktops.size() == 2) {
            return;
        }

        getCurrentDesktop().hide();
        desktopIndex--;
        if (desktopIndex < 1) {
            desktopIndex = desktops.size() - 1;
        }
        getCurrentDesktop().show();
        setDesktop(getCurrentDesktop().getDesktop(), false);
        getMenuItem(MENU_TERMINAL_SEND_KEYS_TO_ALL).setChecked(getDesktop().isEchoKeystrokes());
    }

    /**
     * Obtain the desktops.
     *
     * @return the list of desktops
     */
    public List<VirtualDesktop> getDesktops() {
        return desktops.subList(1, desktops.size());
    }

    /**
     * Put a window on all desktops.
     *
     * @param window the window
     */
    public void putOnAllDesktops(final TWindow window) {
        // Funny, making a window visible to all desktops means actually
        // removing it from all of them.
        for (int i = 0; i < desktops.size(); i++) {
            desktops.get(i).removeWindow(window);
        }
    }

    // Plugin management ------------------------------------------------------

    /**
     * Load all plugins (xtwm.plugins.PluginLoader) via the ServiceLoader
     * interface.
     */
    private void loadPlugins() {
        pluginOptions = new HashMap<String, Properties>();
        pluginAppMenuIds = new HashMap<Integer, Class<? extends PluginWidget>>();
        pluginWidgetMenuIds = new HashMap<Integer, Class<? extends PluginWidget>>();

        ServiceLoader<PluginLoader> services;
        services = ServiceLoader.load(PluginLoader.class);

        for (PluginLoader loader: services) {
            List<Class<?>> plugins = loader.getPluginClasses();
            for (Class<?> pluginClass: plugins) {
                if (PluginWidget.class.isAssignableFrom(pluginClass)) {
                    loadPlugin(pluginClass);
                }
            }
        }
    }

    /**
     * Load one plugin into the menu.
     *
     * @param plugin the plugin to load
     */
    private void loadPlugin(final Class<?> plugin) {
        if (!PluginWidget.class.isAssignableFrom(plugin)) {
            throw new IllegalArgumentException("Class " + plugin +
                " is not an instance of xtwm.plugins.PluginWidget");
        }

        // Every plugin must have a zero-argument constructor.
        try {
            PluginWidget widget = (PluginWidget) plugin.getDeclaredConstructor().newInstance();
            String pluginName = widget.getPluginName();
            loadPluginProperties(pluginName);

            if (widget.isApplication()) {
                int menuId = pluginAppMenuIds.size() + APP_MENU_ID_MIN;
                programsMenu.addItem(menuId, widget.getMenuMnemonic());
                pluginAppMenuIds.put(menuId, widget.getClass());
            }

            if (widget.isWidget()) {
                int menuId = pluginWidgetMenuIds.size() + WIDGET_MENU_ID_MIN;
                widgetsMenu.addItem(menuId, widget.getMenuMnemonic());
                pluginWidgetMenuIds.put(menuId, widget.getClass());
            }

        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Plugin " + plugin.getName() +
                " does not have a no-argument constructor");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Plugin " + plugin.getName() +
                "'s no-argument constructor failed", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Plugin " + plugin.getName() +
                "'s no-argument constructor failed", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new IllegalArgumentException("Plugin " + plugin.getName() +
                "'s no-argument constructor failed", e);
        }

        programsMenu.sort(APP_MENU_ID_MIN - 1);
        widgetsMenu.sort();
    }

    /**
     * Load the properties for a plugin.
     *
     * @param pluginName name of the plugin
     */
    public void loadPluginProperties(final String pluginName) {
        Properties props = new Properties();
        File propsFile = new File(pluginDataDir, pluginName + ".properties");
        try {
            if (!propsFile.exists()) {
                props.store(new FileWriter(propsFile),
                    "Properties for plugin: " + pluginName);
            } else {
                props.load(new FileReader(propsFile));
            }
        } catch (IOException e) {
            // Show this exception to the user.
            new TExceptionDialog(this, e);
        }

        pluginOptions.put(pluginName, props);
    }

    /**
     * Get a plugin option value.
     *
     * @param pluginName name of the plugin
     * @param key name of the option
     * @return the option value, or null if it is undefined
     */
    public String getPluginOption(final String pluginName, final String key) {
        Properties pluginProps = pluginOptions.get(pluginName);
        if (pluginProps == null) {
            return null;
        }
        return pluginProps.getProperty(key);
    }

    /**
     * Get a plugin option value.
     *
     * @param pluginName name of the plugin
     * @param key name of the option
     * @param defaultValue the value to return if the option is not defined
     * @return the option value, or defaultValue
     */
    public String getPluginOption(final String pluginName, final String key,
        final String defaultValue) {

        Properties pluginProps = pluginOptions.get(pluginName);
        if (pluginProps == null) {
            return null;
        }
        String result = pluginProps.getProperty(key);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    /**
     * Set a plugin option value.
     *
     * @param pluginName name of the plugin
     * @param key name of the option
     * @param value the new the option value
     */
    public void setPluginOption(final String pluginName, final String key,
        final String value) {

        Properties pluginProps = pluginOptions.get(pluginName);
        if (pluginProps != null) {
            pluginProps.setProperty(key, value);
        }
    }

    /**
     * Get the directory that plugins are expected to read and write from.
     *
     * @return the plugin data directory, or null if it is not available
     */
    public File getPluginDataDir() {
        return pluginDataDir;
    }

}
