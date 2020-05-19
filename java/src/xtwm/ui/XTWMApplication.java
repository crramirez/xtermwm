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
import java.util.Properties;
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TApplication;
import jexer.TEditColorThemeWindow;
import jexer.TExceptionDialog;
import jexer.TFontChooserWindow;
import jexer.THelpWindow;
import jexer.TInputBox;
import jexer.TMessageBox;
import jexer.TStatusBar;
import jexer.TWindow;
import jexer.backend.Backend;
import jexer.backend.SwingTerminal;
import jexer.bits.CellAttributes;
import jexer.bits.Color;
import jexer.bits.ColorTheme;
import jexer.event.TCommandEvent;
import jexer.event.TKeypressEvent;
import jexer.event.TMenuEvent;
import jexer.help.Topic;
import jexer.menu.TMenu;
import jexer.menu.TSubMenu;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

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
    public static final String VERSION = "0.0.1";

    /*
     * Available menu commands.  Note that the package private items are
     * handled by other classes.
     */
    private static final int MENU_APPLICATION_PROGRAMS_SHELL            = 2010;
    private static final int MENU_APPLICATION_PROGRAMS_EDITOR           = 2011;
    private static final int MENU_APPLICATION_WIDGETS_CALCULATOR        = 2020;
    private static final int MENU_APPLICATION_WIDGETS_CALENDAR          = 2021;
    private static final int MENU_APPLICATION_WIDGETS_DESKTOP_PAGER     = 2022;
    private static final int MENU_APPLICATION_WIDGETS_FILE_MANAGER      = 2023;
    private static final int MENU_APPLICATION_WIDGETS_QUICK_NOTE        = 2024;
    private static final int MENU_APPLICATION_WIDGETS_PERF_MONITOR      = 2025;
    private static final int MENU_APPLICATION_SETTINGS_DISPLAY          = 2030;
    private static final int MENU_APPLICATION_SETTINGS_COLORS           = 2031;
    private static final int MENU_APPLICATION_SETTINGS_ENVIRONMENT      = 2032;
    private static final int MENU_APPLICATION_SETTINGS_WINDOWS          = 2033;
    private static final int MENU_APPLICATION_SETTINGS_SAVE             = 2034;
    private static final int MENU_APPLICATION_SETTINGS_LOAD             = 2035;
    private static final int MENU_APPLICATION_RUN                       = 2091;
    private static final int MENU_APPLICATION_LOCK_SCREEN               = 2092;
    private static final int MENU_APPLICATION_EXIT                      = 2099;

    private static final int MENU_TERMINAL_NEW_WINDOW                   = 2100;
    private static final int MENU_TERMINAL_HORIZONTAL_SPLIT             = 2101;
    private static final int MENU_TERMINAL_VERTICAL_SPLIT               = 2102;
    private static final int MENU_TERMINAL_SEND_KEYS_TO_ALL             = 2103;
    private static final int MENU_TERMINAL_SESSION_SAVE_HTML            = 2104;
    private static final int MENU_TERMINAL_SESSION_SAVE_TEXT            = 2105;
    private static final int MENU_TERMINAL_SESSION_SEND_SIGTERM         = 2106;
    private static final int MENU_TERMINAL_SESSION_SEND_OTHER_SIGNAL    = 2107;
    private static final int MENU_TERMINAL_CLOSE                        = 2108;

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
     * The password used to unlock the lock screen.  Unfortunately the lock
     * screen cannot use the operating system password for this, because the
     * only means of verifying credentials requires some ridiculous insecure
     * shenanigans I don't want to get into.
     */
    private String lockScreenPassword = "a";

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
     * The clock time format.
     */
    private SimpleDateFormat clockFormat = null;

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

        // We have two desktops by default:
        // 0 - The screensaver desktop.  No windows are created on it.
        // 1 - The first normal desktop.
        desktops.add(new VirtualDesktop(this));
        desktops.add(new VirtualDesktop(this));
        desktopIndex = 1;

        // Now add the other desktops.
        for (int i = 1; i < desktopCount; i++) {
            desktops.add(new VirtualDesktop(this));
        }

        // Menu system tray
        clockFormat = new SimpleDateFormat("MM/dd/YYYY hh:mm:ss a");
        addTimer(500, true,
            new TAction() {
                public void DO() {
                    menuTrayText = String.format("%s [%d]",
                        clockFormat.format(new Date()), desktopIndex);
                    XTWMApplication.this.doRepaint();
                }
            }
        );

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

        TInputBox inputBox;
        TWindow window;

        // Dispatch menu event
        switch (menu.getId()) {

        case MENU_APPLICATION_PROGRAMS_SHELL:
            // Spawn shell
            currentDesktop().addWindow(openTerminal(0, 0));
            return true;

        case MENU_APPLICATION_PROGRAMS_EDITOR:
            // TODO
            return true;

        case MENU_APPLICATION_WIDGETS_CALCULATOR:
            // TODO
            return true;

        case MENU_APPLICATION_WIDGETS_CALENDAR:
            // TODO
            return true;

        case MENU_APPLICATION_WIDGETS_DESKTOP_PAGER:
            // TODO
            return true;

        case MENU_APPLICATION_WIDGETS_FILE_MANAGER:
            // TODO
            return true;

        case MENU_APPLICATION_WIDGETS_QUICK_NOTE:
            // TODO
            return true;

        case MENU_APPLICATION_WIDGETS_PERF_MONITOR:
            // TODO
            return true;

        case MENU_APPLICATION_SETTINGS_DISPLAY:
            new TFontChooserWindow(this);
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
                    currentDesktop().addWindow(openTerminal(0, 0, command));
                }
            }
            return true;

        case MENU_APPLICATION_LOCK_SCREEN:
            lockScreen();
            return true;

        case MENU_APPLICATION_EXIT:
            // Post a quit command
            postMenuEvent(new TCommandEvent(cmQuit));
            return true;

        case MENU_TERMINAL_NEW_WINDOW:
            // TODO
            return true;

        case MENU_TERMINAL_HORIZONTAL_SPLIT:
            // TODO
            return true;

        case MENU_TERMINAL_VERTICAL_SPLIT:
            // TODO
            return true;

        case MENU_TERMINAL_SEND_KEYS_TO_ALL:
            // TODO
            return true;

        case MENU_TERMINAL_SESSION_SAVE_HTML:
            // TODO
            return true;

        case MENU_TERMINAL_SESSION_SAVE_TEXT:
            // TODO
            return true;

        case MENU_TERMINAL_SESSION_SEND_SIGTERM:
            // TODO
            return true;

        case MENU_TERMINAL_SESSION_SEND_OTHER_SIGNAL:
            // TODO
            return true;

        case MENU_TERMINAL_CLOSE:
            // TODO
            return true;

        case MENU_PANEL_SWITCH_TO:
            // TODO
            return true;

        case MENU_PANEL_NEXT:
            // TODO
            return true;

        case MENU_PANEL_PREVIOUS:
            // TODO
            return true;

        case MENU_PANEL_CLOSE:
            // TODO
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
            // Funny, making a window visible to all desktops means actually
            // removing it from all of them.
            window = getActiveWindow();
            if (window != null) {
                if (currentDesktop().hasWindow(window)) {
                    for (int i = 0; i < desktops.size(); i++) {
                        desktops.get(i).removeWindow(window);
                    }
                } else {
                    currentDesktop().addWindow(window);
                }
            }
            return true;

        case MENU_WINDOW_NEXT_DESKTOP:
            nextDesktop();
            return true;

        case MENU_WINDOW_PREVIOUS_DESKTOP:
            previousDesktop();
            return true;

        case TMenu.MID_HELP_HELP:
            currentDesktop().addWindow(new THelpWindow(this,
                    THelpWindow.HELP_HELP));
            return true;

        case TMenu.MID_HELP_CONTENTS:
            currentDesktop().addWindow(new THelpWindow(this,
                    helpFile.getTableOfContents()));
            return true;

        case TMenu.MID_HELP_INDEX:
            currentDesktop().addWindow(new THelpWindow(this,
                    helpFile.getIndex()));
            return true;

        case TMenu.MID_HELP_SEARCH:
            inputBox = inputBox(i18n.getString("searchHelpInputBoxTitle"),
                i18n.getString("searchHelpInputBoxCaption"), "",
                TInputBox.Type.OKCANCEL);
            if (inputBox.isOk()) {
                currentDesktop().addWindow(new THelpWindow(this,
                        helpFile.getSearchResults(inputBox.getText())));
            }
            return true;

        case TMenu.MID_HELP_PREVIOUS:
            if (helpTopics.size() > 1) {
                Topic previous = helpTopics.remove(helpTopics.size() - 2);
                helpTopics.remove(helpTopics.size() - 1);
                currentDesktop().addWindow(new THelpWindow(this, previous));
            } else {
                currentDesktop().addWindow(new THelpWindow(this,
                        helpFile.getTableOfContents()));
            }
            return true;

        default:
            // Not handled here.
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
            if (messageBox(i18n.getString("exitDialogTitle"),
                    i18n.getString("exitDialogText"),
                    TMessageBox.Type.YESNO).isYes()) {

                exit();
            }
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
            i18n.getString("applicationProgramsShell"));
        subPrograms.addItem(MENU_APPLICATION_PROGRAMS_EDITOR,
            i18n.getString("applicationProgramsEditor"));

        TSubMenu subWidgets = applicationMenu.addSubMenu(i18n.
            getString("applicationWidgets"));
        subWidgets.addItem(MENU_APPLICATION_WIDGETS_CALCULATOR,
            i18n.getString("applicationWidgetsCalculator"));
        subWidgets.addItem(MENU_APPLICATION_WIDGETS_CALENDAR,
            i18n.getString("applicationWidgetsCalendar"));
        subWidgets.addItem(MENU_APPLICATION_WIDGETS_DESKTOP_PAGER,
            i18n.getString("applicationWidgetsDesktopPager"));
        subWidgets.addItem(MENU_APPLICATION_WIDGETS_FILE_MANAGER,
            i18n.getString("applicationWidgetsFileManager"));
        subWidgets.addItem(MENU_APPLICATION_WIDGETS_QUICK_NOTE,
            i18n.getString("applicationWidgetsQuickNote"));
        subWidgets.addItem(MENU_APPLICATION_WIDGETS_PERF_MONITOR,
            i18n.getString("applicationWidgetsPerformanceMonitor"));

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
            i18n.getString("terminalNewWindow"), kbCtrlN);
        terminalMenu.addItem(MENU_TERMINAL_HORIZONTAL_SPLIT,
            i18n.getString("terminalHorizontalSplit"));
        terminalMenu.addItem(MENU_TERMINAL_VERTICAL_SPLIT,
            i18n.getString("terminalVerticalSplit"));
        terminalMenu.addSeparator();
        terminalMenu.addItem(MENU_TERMINAL_SEND_KEYS_TO_ALL,
            i18n.getString("terminalSendKeysToAll"));
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
        panelMenu.addItem(MENU_PANEL_NEXT, i18n.getString("panelNext"), kbF7);
        panelMenu.addItem(MENU_PANEL_PREVIOUS, i18n.getString("panelPrevious"),
            kbShiftF7);
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
            kbShiftF5);
        windowMenu.addItem(TMenu.MID_WINDOW_ZOOM, i18n.getString("windowZoom"),
            kbF5);
        windowMenu.addItem(TMenu.MID_WINDOW_NEXT, i18n.getString("windowNext"),
            kbF6);
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
            i18n.getString("windowNextDesktop"), kbF9);
        windowMenu.addItem(MENU_WINDOW_PREVIOUS_DESKTOP,
            i18n.getString("windowPreviousDesktop"), kbShiftF9);

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

        setOption("ui.font.name", "");
        setOption("ui.font.size", "20");
        setOption("ui.font.adjustX", "0");
        setOption("ui.font.adjustY", "0");
        setOption("ui.font.adjustWidth", "0");
        setOption("ui.font.adjustHeight", "0");

        setOption("jexer.TTerminal.ptypipe", "auto");
        setOption("jexer.TTerminal.closeOnExit", "false");
        setOption("jexer.Swing.cursorStyle", "underline");
        setOption("jexer.Swing.tripleBuffer", "true");
        setOption("jexer.ECMA48.rgbColor", "false");
        setOption("jexer.ECMA48.sixel", "true");

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
        }
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

        for (;;) {
            TInputBox inputBox = inputBox(i18n.
                getString("lockScreenInputBoxTitle"),
                i18n.getString("lockScreenInputBoxCaption"), "",
                TInputBox.Type.OKCANCEL);
            if (inputBox.isOk()) {
                String password = inputBox.getText();
                if (password.length() > 0) {
                    if (password.equals(lockScreenPassword)) {
                        break;
                    }
                }
            }
        } // for (;;)

        setHideMenuBar(false);
        setHideStatusBar(false);
    }

    /**
     * Get the current desktop.
     *
     * @return the virtual desktop
     */
    private VirtualDesktop currentDesktop() {
        return desktops.get(desktopIndex);
    }

    /**
     * Switch to the next desktop.
     */
    private void nextDesktop() {
        assert (desktops.size() >= 2);

        if (desktops.size() == 2) {
            return;
        }

        currentDesktop().hide();
        desktopIndex++;
        if (desktopIndex == desktops.size()) {
            desktopIndex = 1;
        }
        currentDesktop().show();
    }

    /**
     * Switch to the previous desktop.
     */
    private void previousDesktop() {
        assert (desktops.size() >= 2);

        if (desktops.size() == 2) {
            return;
        }

        currentDesktop().hide();
        desktopIndex--;
        if (desktopIndex < 1) {
            desktopIndex = desktops.size() - 1;
        }
        currentDesktop().show();
    }

}
