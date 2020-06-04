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
import jexer.backend.ECMA48Terminal;
import jexer.backend.MultiBackend;
import jexer.backend.SwingTerminal;
import jexer.bits.Cell;
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

import xtwm.plugins.DesktopPager;
import xtwm.plugins.PluginLoader;
import xtwm.plugins.PluginWidget;
import xtwm.plugins.ScreensaverPlugin;

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
    public static final String VERSION = "0.2.0";

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
    private static final int MENU_APPLICATION_SETTINGS_EDITOR           = 2033;
    private static final int MENU_APPLICATION_SETTINGS_PLUGINS          = 2034;
    private static final int MENU_APPLICATION_SETTINGS_SAVE             = 2035;
    private static final int MENU_APPLICATION_SETTINGS_LOAD             = 2036;
    private static final int MENU_APPLICATION_RUN                       = 2091;
    private static final int MENU_APPLICATION_LOCK_SCREEN               = 2092;
    private static final int MENU_APPLICATION_DETACH_TMUX               = 2093;
    private static final int MENU_APPLICATION_DETACH_SCREEN             = 2094;
    private static final int MENU_APPLICATION_DISCONNECT                = 2095;
    private static final int MENU_APPLICATION_CLIENTS                   = 2096;
    private static final int MENU_APPLICATION_EXIT                      = 2099;

    // Package private values are handled by InternalEditor.
    private static final int MENU_EDIT_OPEN                             = 2100;
            static final int MENU_EDIT_SAVE                             = 2101;
            static final int MENU_EDIT_SAVE_AS                          = 2102;

    // Public values are handled by PluginWidget.  Package private values are
    // handled by TiledTerminal.
    private static final int MENU_TERMINAL_NEW_WINDOW                   = 2200;

    /**
     * Terminal | Horizontal split menu item.
     */
     public static final int MENU_TERMINAL_HORIZONTAL_SPLIT             = 2201;

    /**
     * Terminal | Vertical split menu item.
     */
     public static final int MENU_TERMINAL_VERTICAL_SPLIT               = 2202;

    private static final int MENU_TERMINAL_SEND_KEYS_TO_ALL             = 2203;
            static final int MENU_TERMINAL_SESSION_SAVE_HTML            = 2204;
            static final int MENU_TERMINAL_SESSION_SAVE_TEXT            = 2205;
            static final int MENU_TERMINAL_SESSION_SEND_SIGTERM         = 2206;
            static final int MENU_TERMINAL_SESSION_SEND_OTHER_SIGNAL    = 2207;

    /**
     * Terminal | Close menu item.
     */
     public static final int MENU_TERMINAL_CLOSE                        = 2208;

    private static final int MENU_PANEL_SWITCH_TO                       = 2301;
    private static final int MENU_PANEL_NEXT                            = 2302;
    private static final int MENU_PANEL_PREVIOUS                        = 2303;
    private static final int MENU_PANEL_CLOSE                           = 2304;
    private static final int MENU_PANEL_SAVE_LAYOUT                     = 2305;
    private static final int MENU_PANEL_LOAD_LAYOUT                     = 2306;

    private static final int MENU_WINDOW_TO_DESKTOP                     = 2400;
    private static final int MENU_WINDOW_ON_ALL_DESKTOPS                = 2401;
    private static final int MENU_WINDOW_NEXT_DESKTOP                   = 2402;
    private static final int MENU_WINDOW_PREVIOUS_DESKTOP               = 2403;

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
     * The desktop pager widget.
     */
    private DesktopPager desktopPager;

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
     * Whether or not to convert box-drawing glyphs to simple lines.
     */
    private boolean simpleBoxGlyphs = false;

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

    /**
     * Last used search text.  Note package private access.
     */
    String searchText = "";

    /**
     * Last used search case sensitivity flag.  Note package private access.
     */
    boolean searchCaseSensitive = false;

    /**
     * Last used search regular expression flag.  Note package private
     * access.
     */
    boolean searchRegularExpression = false;

    /**
     * Last used search whole words only flag.  Note package private access.
     */
    boolean searchWholeWordsOnly = false;

    /**
     * Last used search direction.  1 means forward, 2 means backward.  Note
     * package private access.
     */
    int searchDirection = 1;

    /**
     * Last used search scope.  1 means entire file, 2 means selected text
     * only.  Note package private access.
     */
    int searchScope = 1;

    /**
     * The Application menu.  Note package private access.
     */
    TMenu applicationMenu = null;

    /**
     * The list of screensaver classes.
     */
    private List<Class<? extends ScreensaverPlugin>> screensavers = new ArrayList<Class<? extends ScreensaverPlugin>>();

    /**
     * The selected screensaver type.
     */
    private Class<? extends ScreensaverPlugin> screensaverClass = null;

    /**
     * The screensaver timeout in seconds.  0 means screensaver is disabled.
     */
    private int screensaverTimeout = 0;

    /**
     * Whether or not the screensaver also locks the screen.
     */
    private boolean screensaverLock = true;

    /**
     * The desktop that was active before the screensaver kicked in.
     */
    private int screensaverLastDesktop = -1;

    /**
     * The actively running screensaver.
     */
    private ScreensaverPlugin screensaver = null;

    /**
     * The list of widget classes.
     */
    private List<Class<? extends PluginWidget>> widgets = new ArrayList<Class<? extends PluginWidget>>();

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

        // Call resolveOptions() again to alter behavior based on loaded
        // plugins.
        resolveOptions();

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

        if ((getBackend() instanceof jexer.backend.SwingBackend)
            && (getOption("xtwm.maximizeOnSwing").equals("true"))
        ) {
            // Maximize the Swing window.
            invokeLater(new Runnable() {
                public void run() {
                    SwingTerminal terminal;
                    terminal = (SwingTerminal) getScreen();
                    jexer.backend.SwingComponent component;
                    component = terminal.getSwingComponent();
                    javax.swing.JFrame frame = component.getFrame();
                    if (frame != null) {
                        frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
                    }
                }
            });
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
        getCurrentDesktop().sync();

        long now = System.currentTimeMillis();

        if ((screensaverTimeout > 0) && (desktopIndex != 0)) {
            if ((((now - lastUserInputTime) / 1000) > screensaverTimeout)
                && (!isModalThreadRunning())
            ) {
                // Screensaver needs to be active.
                screensaverLastDesktop = desktopIndex;
                getCurrentDesktop().hide();
                desktopIndex = 0;
                getCurrentDesktop().show();
                TDesktop desktop = getCurrentDesktop().getDesktop();
                setDesktop(getCurrentDesktop().getDesktop(), false);
                setHideMenuBar(true);
                setHideStatusBar(true);
                if (screensaverClass != null) {
                    assert (screensaver == null);
                    try {
                        screensaver = screensaverClass.getConstructor().newInstance();
                        screensaver.initialize(this);
                        screensaver.setParent(desktop, false);
                        screensaver.setDimensions(0, 0,
                            desktop.getWidth(), desktop.getHeight());
                        screensaver.startScreensaver(getScreen().snapshot());
                    } catch (Exception e) {
                        if (e instanceof RuntimeException) {
                            throw (RuntimeException) e;
                        }
                        new TExceptionDialog(this, e);
                    }
                }
            }
        } else if ((screensaverTimeout > 0) && (desktopIndex == 0)) {
            if (((now - lastUserInputTime) / 1000) < screensaverTimeout) {
                // Screensaver needs to be turned off.
                assert (screensaver != null);

                if ((getOption("screensaver.lock").equals("true"))
                    && (!screensaver.isUnlocked())
                ) {
                    // Have the screensaver ask for a password.
                    screensaver.checkPassword();
                }

                if (screensaver.isUnlocked()) {
                    // The user successfully entered the password (or there
                    // is no password).
                    screensaver.endScreensaver();
                    screensaver.remove(true);
                    screensaver = null;

                    // Switch the desktop back.
                    getCurrentDesktop().hide();
                    desktopIndex = screensaverLastDesktop;
                    getCurrentDesktop().show();
                    getCurrentDesktop().getDesktop().setFocusFollowsMouse(getOption("panel.focusFollowsMouse", "true").equals("true"));
                    setDesktop(getCurrentDesktop().getDesktop(), false);
                    getMenuItem(MENU_TERMINAL_SEND_KEYS_TO_ALL).setChecked(getDesktop().isEchoKeystrokes());
                    setHideMenuBar(getOption("xtwm.hideMenuBar").equals("true"));
                    setHideStatusBar(getOption("xtwm.hideStatusBar").equals("true"));
                }
            }
        }

        // Menu tray text
        String text = "";
        if (menuTrayClock) {
            text = clockFormat.format(new Date(now));
            if (menuTrayDesktop) {
                text += " ";
            }
        }
        if (menuTrayDesktop) {
            text += String.format("[%d]", desktopIndex);
        }
        if (getBackend() instanceof MultiBackend) {
            int readOnlyCount = 0;
            List<Backend> backends = ((MultiBackend) getBackend()).getBackends();
            for (Backend backend: backends) {
                if (backend.isReadOnly()) {
                    readOnlyCount++;
                }
            }
            int readWriteCount = backends.size() - readOnlyCount;
            if (readWriteCount >= 3) {
                text += " " + (char) 0x263B;
                text += "x" + Integer.toString(readWriteCount);
            } else if (readWriteCount > 0) {
                text += " ";
                for (int i = 0; i < readWriteCount; i++) {
                    text += (char) 0x263B;
                }
            }
            if (readOnlyCount >= 3) {
                text += " " + (char) 0x263A;
                text += "x" + Integer.toString(readOnlyCount);
            } else if (readOnlyCount > 0) {
                text += " ";
                for (int i = 0; i < readOnlyCount; i++) {
                    text += (char) 0x263A;
                }
            }
        }

        menuTrayText = text;

        List<TWindow> windows = getCurrentDesktop().getWindows();

        // Special case: if the desktop has tiled terminals, and any windows
        // on the screen are not part of a virtual desktop (i.e. the desktop
        // pager is the only window visible), then permit the blinking cursor
        // on the desktop to be visible.
        if ((windows.size() == 0)
            && (getDesktop().getChildren().size() > 0)
            && (desktopIndex != 0)
        ) {
            // Permit the desktop cursor to be visible, even if the top-most
            // actual window does not have a cursor.
            desktopCanHaveCursor = true;
        } else {
            desktopCanHaveCursor = false;
        }

    }

    /**
     * Function called immediately after the screen is drawn, while the
     * screen is still synchronized/locked.  This can be used by subclasses
     * of TApplication to alter the final post-rendered screen before it goes
     * out -- or even replace the entire thing such as a screensaver.
     */
    @Override
    protected void onPostDraw() {
        if (desktopIndex == 0) {
            // The screensaver is active, draw it.
            getScreen().resetClipping();
            getCurrentDesktop().getDesktop().drawChildren();
        }

        if (simpleBoxGlyphs) {
            // Many terminal fonts lack all of the box-drawing glyphs.  This
            // filter will convert them to the simplest form.
            int width = getScreen().getWidth();
            int height = getScreen().getHeight();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    Cell cell = getScreen().getCharXY(x, y);
                    if (cell.isImage()) {
                        continue;
                    }
                    cell.setChar(simpleBoxDrawingChar(cell.getChar()));
                    getScreen().putCharXY(x, y, cell);
                }
            }
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
            if (getOption("editor.useExternal", "false").equals("true")) {
                new ExternalEditorWindow(this);
            } else {
                new InternalEditorWindow(this);
            }
            return true;

        case MENU_APPLICATION_SETTINGS_DISPLAY:
            getCurrentDesktop().addWindow(new TScreenOptionsWindow(this) {
                /*
                 * We have finished editing screen options, now save to the
                 * xtwm.properties file.
                 */
                @Override
                public void onClose() {
                    if (getScreen() instanceof ECMA48Terminal) {
                        ECMA48Terminal terminal = (ECMA48Terminal) getScreen();
                        setOption("jexer.ECMA48.sixel",
                            (terminal.hasSixel() ? "true" : "false"));
                        setOption("jexer.ECMA48.sixelSharedPalette",
                            (terminal.hasSixelSharedPalette() ? "true" :
                                "false"));
                        setOption("jexer.ECMA48.sixelPaletteSize",
                            Integer.toString(terminal.getSixelPaletteSize()));
                        setOption("jexer.ECMA48.wideCharImages",
                            (terminal.isWideCharImages() ? "true" : "false"));
                        setOption("jexer.ECMA48.rgbColor",
                            (terminal.isRgbColor() ? "true" : "false"));
                    }

                    if (getScreen() instanceof SwingTerminal) {
                        SwingTerminal terminal = (SwingTerminal) getScreen();
                        setOption("jexer.Swing.tripleBuffer",
                            (terminal.isTripleBuffer() ? "true" : "false"));
                        setOption("jexer.Swing.cursorStyle",
                            terminal.getCursorStyle().toString().toLowerCase());
                        setOption("jexer.Swing.mouseStyle",
                            terminal.getMouseStyle());
                    }

                    saveOptions();
                    super.onClose();
                }
            });
            return true;

        case MENU_APPLICATION_SETTINGS_COLORS:
            getCurrentDesktop().addWindow(new TEditColorThemeWindow(this) {
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
            });
            return true;

        case MENU_APPLICATION_SETTINGS_ENVIRONMENT:
            getCurrentDesktop().addWindow(new ApplicationOptionsWindow(this));
            return true;

        case MENU_APPLICATION_SETTINGS_EDITOR:
            getCurrentDesktop().addWindow(new EditorOptionsWindow(this));
            return true;

        case MENU_APPLICATION_SETTINGS_PLUGINS:
            getCurrentDesktop().addWindow(new PluginOptionsWindow(this));
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

        case MENU_APPLICATION_DETACH_TMUX:
            try {
                String sessionName = System.getProperty("xtwm.tmuxSessionName");
                if (sessionName != null) {
                    Runtime.getRuntime().exec("tmux detach-client -s " +
                        System.getProperty("xtwm.tmuxSessionName"));
                } else {
                    Runtime.getRuntime().exec("tmux detach-client");
                }
            } catch (IOException e) {
                new TExceptionDialog(this, e);
            }
            return true;

        case MENU_APPLICATION_DETACH_SCREEN:
            try {
                Runtime.getRuntime().exec("screen -d");
            } catch (IOException e) {
                new TExceptionDialog(this, e);
            }
            return true;

        case MENU_APPLICATION_DISCONNECT:
            assert (getBackend() instanceof MultiBackend);
            assert (menu.getBackend() instanceof jexer.backend.ECMA48Backend);
            menu.getBackend().shutdown();
            return true;

        case MENU_APPLICATION_CLIENTS:
            getCurrentDesktop().addWindow(new ClientsWindow(this));
            return true;

        case MENU_APPLICATION_EXIT:
            // Post a quit command
            postMenuEvent(new TCommandEvent(menu.getBackend(), cmExit));
            return true;

        case MENU_EDIT_OPEN:
            try {
                String filename = fileOpenBox(".");
                if (filename != null) {
                    getCurrentDesktop().addWindow(openEditor(filename));
                }
            } catch (IOException e) {
                // Show this exception to the user.
                new TExceptionDialog(this, e);
            }
            return true;

        case TMenu.MID_FIND:
            menuSearchFind();
            return true;

        case TMenu.MID_REPLACE:
            menuSearchReplace();
            return true;

        case TMenu.MID_SEARCH_AGAIN:
            menuSearchAgain();
            return true;

        case TMenu.MID_GOTO_LINE:
            menuSearchGoToLine();
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
            menuPanelSwitchTo();
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
            menuWindowToDesktop();
            return true;

        case MENU_WINDOW_ON_ALL_DESKTOPS:
            window = getActiveWindow();
            if (window != null) {
                if ((desktopPager != null)
                    && (desktopPager.getWindow() == window)
                ) {
                    return true;
                }
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
            Class<? extends PluginWidget> pluginClass = null;
            pluginClass = pluginAppMenuIds.get(menu.getId());

            if (pluginClass != null) {
                PluginWidget plugin = makePluginWidget(pluginClass);
                String cmdline = plugin.getApplicationCommand();
                jexer.TTerminalWindow terminal = openTerminal(0, 0, cmdline);
                terminal.setTitle(plugin.getWindowTitle(), true);
                getCurrentDesktop().addWindow(terminal);
            }
            return true;
        } else if (menu.getId() >= WIDGET_MENU_ID_MIN) {
            // Add the widget to a new window.
            Class<? extends PluginWidget> pluginClass = null;
            pluginClass = pluginWidgetMenuIds.get(menu.getId());

            if (pluginClass != null) {
                PluginWidget plugin = makePluginWidget(pluginClass);
                if (plugin.isOnAllDesktops()) {
                    makePluginWindow(plugin);
                } else {
                    getCurrentDesktop().addWindow(makePluginWindow(plugin));
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
            if (getBackend() instanceof MultiBackend) {
                if (((MultiBackend) getBackend()).getBackends().size() > 1) {
                    if (messageBox(i18n.getString("exitAllDialogTitle"),
                            i18n.getString("exitAllDialogText"),
                            TMessageBox.Type.YESNO).isYes()) {

                        exit();
                    }
                    return true;
                }
            } else if (getOption("xtwm.confirmOnExit", "true").equals("true")) {
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

        applicationMenu = addMenu(i18n.getString("applicationMenuTitle"));

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
        subSettings.addItem(MENU_APPLICATION_SETTINGS_EDITOR,
            i18n.getString("applicationSettingsEditor"));
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
        applicationMenu.addSeparator();
        if (System.getProperty("xtwm.insideTmux", "false").equals("true")
            || ((getScreen() instanceof ECMA48Terminal)
                && (System.getenv().get("TMUX") != null))
        ) {
            applicationMenu.addItem(MENU_APPLICATION_DETACH_TMUX,
                i18n.getString("applicationDetachTmux"));
            applicationMenu.addSeparator();
        }
        if ((getScreen() instanceof ECMA48Terminal)
            && (System.getenv().get("STY") != null)
        ) {
            applicationMenu.addItem(MENU_APPLICATION_DETACH_SCREEN,
                i18n.getString("applicationDetachScreen"));
            applicationMenu.addSeparator();
        }
        if (getBackend() instanceof MultiBackend) {
            applicationMenu.addItem(MENU_APPLICATION_DISCONNECT,
                i18n.getString("applicationDisconnect"));
            applicationMenu.addItem(MENU_APPLICATION_CLIENTS,
                i18n.getString("applicationClients"));
            applicationMenu.addSeparator();
        }
        applicationMenu.addItem(MENU_APPLICATION_LOCK_SCREEN,
            i18n.getString("applicationLockScreen"));
        applicationMenu.addItem(MENU_APPLICATION_EXIT,
            i18n.getString("applicationExit"), kbCtrlQ);
        TStatusBar applicationStatusBar = applicationMenu.newStatusBar(i18n.
            getString("applicationMenuStatus"));
        applicationStatusBar.addShortcutKeypress(kbF1, cmHelp,
            i18n.getString("Help"));

        // Edit menu ----------------------------------------------------------

        TMenu editMenu = addEditMenu();
        editMenu.addSeparator();
        editMenu.addItem(MENU_EDIT_OPEN, i18n.getString("editMenuOpen"),
            kbCtrlO);
        editMenu.addItem(MENU_EDIT_SAVE, i18n.getString("editMenuSave"), false);
        editMenu.addItem(MENU_EDIT_SAVE_AS, i18n.getString("editMenuSaveAs"),
            false);
        editMenu.addSeparator();
        editMenu.addItem(TMenu.MID_FIND, i18n.getString("editMenuFind"),
            kbCtrlF, false);
        editMenu.addItem(TMenu.MID_REPLACE, i18n.getString("editMenuReplace"),
            false);
        editMenu.addItem(TMenu.MID_SEARCH_AGAIN,
            i18n.getString("editMenuSearchAgain"), kbCtrlL, false);
        editMenu.addItem(TMenu.MID_GOTO_LINE,
            i18n.getString("editMenuGotoLine"), kbCtrlG, false);

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
        setOption("editor.useExternal", "false");
        setOption("jexer.ECMA48.rgbColor", "false");
        setOption("jexer.ECMA48.sixel", "true");
        setOption("jexer.ECMA48.sixelSharedPalette", "true");
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
        setOption("xtwm.hideMenuBar", "false");
        setOption("xtwm.hideStatusBar", "true");
        setOption("xtwm.hideTextMouse", "swing");
        setOption("xtwm.lockScreenPassword", "");
        setOption("xtwm.maximizeOnSwing", "true");
        setOption("xtwm.screensaver", "");
        setOption("xtwm.serverPassword", "elsa");
        setOption("xtwm.simpleBoxGlyphs", "false");

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
        setHideMenuBar(getOption("xtwm.hideMenuBar").equals("true"));
        setHideStatusBar(getOption("xtwm.hideStatusBar").equals("true"));
        menuTrayClock = getOption("menuTray.clock").equals("true");
        clockFormat = new SimpleDateFormat(getOption("menuTray.clock.format"));
        menuTrayDesktop = getOption("menuTray.desktop").equals("true");
        smartWindowPlacement = getOption("window.smartPlacement",
            "true").equals("true");
        setFocusFollowsMouse(getOption("window.focusFollowsMouse",
                "true").equals("true"));
        String hideTextMouse = getOption("xtwm.hideTextMouse");
        if (hideTextMouse.equals("always")) {
            textMouse = false;
        } else if (hideTextMouse.equals("never")) {
            textMouse = true;
        } else if (getBackend() instanceof SwingTerminal) {
            textMouse = false;
        }
        simpleBoxGlyphs = getOption("xtwm.simpleBoxGlyphs",
            "true").equals("true");
        try {
            screensaverTimeout = Integer.parseInt(getOption(
                "screensaver.timeout"));
        } catch (NumberFormatException e) {
            // SQUASH
        }
        screensaverLock = getOption("screensaver.lock").equals("true");
        setScreensaverClass(getOption("xtwm.screensaver", ""));

        if (getCurrentDesktop() != null) {
            getCurrentDesktop().getDesktop().setFocusFollowsMouse(getOption(
                "panel.focusFollowsMouse", "true").equals("true"));
        }

        int desktopCount = desktops.size() - 1;
        try {
            desktopCount = Integer.parseInt(getOption("desktop.count"));
            if (desktopCount > 0) {
                while (desktops.size() - 1 > desktopCount) {
                    removeDesktop();
                }
                while (desktops.size() - 1 < desktopCount) {
                    addDesktop();
                }
            }
        } catch (NumberFormatException e) {
            // SQUASH
        }

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

        String lockScreenPassword = getOption("xtwm.lockScreenPassword", "");

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

        setHideMenuBar(getOption("xtwm.hideMenuBar").equals("true"));
        setHideStatusBar(getOption("xtwm.hideStatusBar").equals("true"));
    }

    // Desktop management -----------------------------------------------------

    /**
     * Setup the virtual desktops.
     */
    private void addDesktops() {
        // We have two desktops by default:
        // 0 - The screensaver desktop.  No windows are created on it.
        // 1 - The first normal desktop.
        desktopIndex = 1;
        setDesktop(getCurrentDesktop().getDesktop(), true);
    }

    /**
     * Add one desktop to the end of the list.
     */
    public void addDesktop() {
        VirtualDesktop desktop = new VirtualDesktop(this);
        desktops.add(desktop);

        desktop.getDesktop().setFocusFollowsMouse(getOption(
            "panel.focusFollowsMouse", "true").equals("true"));

        // Set a different color for each desktop.  Eventually expose these
        // colors.
        CellAttributes desktopAttr = new CellAttributes();
        switch ((desktops.size() + 2) % 4) {
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

        if (desktopPager != null) {
            desktopPager.refreshDesktops();
        }
    }

    /**
     * Remove the last desktop.
     */
    public void removeDesktop() {
        if (desktops.size() <= 2) {
            return;
        }

        if (desktopIndex == desktops.size() - 1) {
            switchToPreviousDesktop();
        }
        VirtualDesktop desktop = desktops.get(desktops.size() - 1);
        List<TWindow> windows = desktop.getWindows();
        desktops.remove(desktops.size() - 1);
        desktop = desktops.get(desktops.size() - 1);
        for (TWindow window: windows) {
            desktop.addWindow(window);
        }

        if (desktopPager != null) {
            desktopPager.refreshDesktops();
        }
    }

    /**
     * Get the current desktop.
     *
     * @return the virtual desktop, or null if no virtual desktops are
     * defined
     */
    public VirtualDesktop getCurrentDesktop() {
        if ((desktopIndex >= desktops.size()) || (desktopIndex < 0)) {
            assert (desktops.size() == 0);
            return null;
        }
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
        getCurrentDesktop().getDesktop().setFocusFollowsMouse(getOption(
            "panel.focusFollowsMouse", "true").equals("true"));
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
        getCurrentDesktop().getDesktop().setFocusFollowsMouse(getOption(
            "panel.focusFollowsMouse", "true").equals("true"));
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
        boolean loadDesktopPager = getOption("desktop.pager").equals("true");

        // Every plugin must have a zero-argument constructor.
        try {
            PluginWidget widget = (PluginWidget) plugin.getDeclaredConstructor().newInstance();
            String pluginName = widget.getPluginName();
            loadPluginProperties(pluginName);
            widget.initialize(this);

            if ((widget instanceof ScreensaverPlugin)
                && (widget.isPluginEnabled())
            ) {
                addScreensaver(((ScreensaverPlugin) widget).getClass());
            }

            if (widget.isApplication()) {
                if (!widgets.contains(widget.getClass())) {
                    widgets.add(widget.getClass());
                }
                if (widget.isPluginEnabled()) {
                    int menuId = pluginAppMenuIds.size() + APP_MENU_ID_MIN;
                    programsMenu.addItem(menuId, widget.getMenuMnemonic());
                    pluginAppMenuIds.put(menuId, widget.getClass());
                }
            }

            if (widget.isWidget()) {
                if (!widgets.contains(widget.getClass())) {
                    widgets.add(widget.getClass());
                }
                if (widget.isPluginEnabled()) {
                    int menuId = pluginWidgetMenuIds.size() + WIDGET_MENU_ID_MIN;
                    widgetsMenu.addItem(menuId, widget.getMenuMnemonic());
                    pluginWidgetMenuIds.put(menuId, widget.getClass());
                    if ((loadDesktopPager == true)
                        && (widget instanceof DesktopPager)
                    ) {
                        // Load the desktop pager automatically.
                        desktopPager = (DesktopPager) makePluginWidget(widget.getClass());
                        makePluginWindow(desktopPager);
                    } else if (widget.isLoadOnStartup()) {
                        // Load the widget automatically.  Any widgets loaded
                        // on startup will also be on all desktops.
                        PluginWidget newWidget = makePluginWidget(widget.getClass());
                        makePluginWindow(newWidget);
                    }
                }
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
     * Save the properties for a plugin.
     *
     * @param plugin an instance of the plugin
     */
    public void savePluginProperties(final PluginWidget plugin) {
        String pluginName = plugin.getPluginName();
        Properties pluginProps = pluginOptions.get(pluginName);
        if (pluginProps == null) {
            pluginProps = new Properties();
        }

        pluginProps.setProperty("enabled",
            (plugin.isPluginEnabled() ? "true" : "false"));
        pluginProps.setProperty("loadOnStartup",
            (plugin.isLoadOnStartup() ? "true" : "false"));

        File propsFile = new File(pluginDataDir, pluginName + ".properties");
        try {
            pluginProps.store(new FileWriter(propsFile),
                "Properties for plugin: " + pluginName);
        } catch (IOException e) {
            // Show this exception to the user.
            new TExceptionDialog(this, e);
        }
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

    /**
     * Add a screensaver to the list of available system screensavers.
     *
     * @param screensaver the screensaver plugin class
     */
    private void addScreensaver(final Class<? extends ScreensaverPlugin> screensaver) {
        if (!screensavers.contains(screensaver)) {
            screensavers.add(screensaver);
            if (screensaverClass == null) {
                screensaverClass = screensaver;
            }
        }
    }

    /**
     * Set the system screensaver class.  If the plugin cannot be found, the
     * blank screensaver is selected.
     *
     * @param name the plugin name
     */
    private void setScreensaverClass(final String name) {
        screensaverClass = xtwm.plugins.BlankScreensaver.class;

        if ((name == null) || (name.length() == 0)) {
            return;
        }

        for (ScreensaverPlugin screensaver: getScreensavers()) {
            if (screensaver.getPluginName().equals(name)) {
                screensaverClass = screensaver.getClass();
                return;
            }
        }
    }

    /**
     * Get the list of available system screensavers.
     *
     * @return a list containing screensaver instances
     */
    public List<ScreensaverPlugin> getScreensavers() {
        List<ScreensaverPlugin> result = new ArrayList<ScreensaverPlugin>();
        for (Class<? extends ScreensaverPlugin> pluginClass: screensavers) {
            result.add((ScreensaverPlugin) makePluginWidget(pluginClass));
        }
        return result;
    }

    /**
     * Get the currently selected system screensaver.
     *
     * @return an instance of the system screensaver, or null if not set
     */
    public ScreensaverPlugin getSystemScreensaver() {
        if (screensaverClass == null) {
            return null;
        }

        return (ScreensaverPlugin) makePluginWidget(screensaverClass);
    }

    /**
     * Get the list of available widgets.
     *
     * @return a list containing widget instances
     */
    public List<PluginWidget> getWidgets() {
        List<PluginWidget> result = new ArrayList<PluginWidget>();
        for (Class<? extends PluginWidget> pluginClass: widgets) {
            result.add(makePluginWidget(pluginClass));
        }
        return result;
    }

    /**
     * Instantiate the window for a plugin widget.
     *
     * @param plugin the plugin
     * @return the window
     */
    private TWindow makePluginWindow(PluginWidget plugin) {
        TWindow window = plugin.getWindow(this);
        plugin.setParent(window, false);
        plugin.setDimensions(0, 0, plugin.getPreferredWidth(),
            plugin.getPreferredHeight());
        window.setDimensions(window.getX(), window.getY(),
            plugin.getWidth() + 2, plugin.getHeight() + 2);
        window.setResizable(plugin.isResizable());
        return window;
    }

    /**
     * Instantiate a plugin widget.  Note package private access.
     *
     * @param pluginClass the plugin class
     * @return the plugin widget
     */
    PluginWidget makePluginWidget(Class<? extends PluginWidget> pluginClass) {

        assert (pluginClass != null);

        PluginWidget plugin = null;
        try {
            plugin = pluginClass.getConstructor().newInstance();
            plugin.initialize(this);
            return plugin;
        } catch (Exception e) {
            // Show this exception to the user.
            new TExceptionDialog(this, e);
            return null;
        }
    }

    // Miscellaneous ----------------------------------------------------------

    /**
     * Convert a complex box-drawing character to a simpler one.
     *
     * @param ch the codepoint
     * @return the codepoint, or a simplified version
     */
    private int simpleBoxDrawingChar(final int ch) {
        switch (ch) {
        case 0x2501:
        case 0x2504:
        case 0x2505:
        case 0x2508:
        case 0x2509:
        case 0x254C:
        case 0x254D:
        case 0x2550:
        case 0x2574:
        case 0x2576:
        case 0x2578:
        case 0x257A:
        case 0x257C:
        case 0x257E:
            // ─
            return 0x2500;

        case 0x2503:
        case 0x2506:
        case 0x2507:
        case 0x250A:
        case 0x250B:
        case 0x254E:
        case 0x254F:
        case 0x2551:
        case 0x2575:
        case 0x2577:
        case 0x2579:
        case 0x257B:
        case 0x257D:
        case 0x257F:
            // │
            return 0x2502;

        case 0x250D:
        case 0x250E:
        case 0x250F:
        case 0x2552:
        case 0x2553:
        case 0x2554:
        case 0x256D:
            // ┌
            return 0x250C;

        case 0x2511:
        case 0x2512:
        case 0x2513:
        case 0x2555:
        case 0x2556:
        case 0x2557:
        case 0x256E:
            // ┐
            return 0x2510;

        case 0x2515:
        case 0x2516:
        case 0x2517:
        case 0x2558:
        case 0x2559:
        case 0x255A:
        case 0x2570:
            // └
            return 0x2514;

        case 0x2519:
        case 0x251A:
        case 0x251B:
        case 0x255B:
        case 0x255C:
        case 0x255D:
        case 0x256F:
            // ┘
            return 0x2518;

        case 0x251D:
        case 0x251E:
        case 0x251F:
        case 0x2520:
        case 0x2521:
        case 0x2522:
        case 0x2523:
        case 0x255E:
        case 0x255F:
        case 0x2560:
            // ├
            return 0x251C;

        case 0x2525:
        case 0x2526:
        case 0x2527:
        case 0x2528:
        case 0x2529:
        case 0x252A:
        case 0x252B:
        case 0x2561:
        case 0x2562:
        case 0x2563:
            // ┤
            return 0x2524;

        case 0x252D:
        case 0x252E:
        case 0x252F:
        case 0x2530:
        case 0x2531:
        case 0x2532:
        case 0x2533:
        case 0x2564:
        case 0x2565:
        case 0x2566:
            // ┬
            return 0x252C;

        case 0x2535:
        case 0x2536:
        case 0x2537:
        case 0x2538:
        case 0x2539:
        case 0x253A:
        case 0x253B:
        case 0x2567:
        case 0x2568:
        case 0x2569:
            // ┴
            return 0x2534;

        case 0x253D:
        case 0x253E:
        case 0x253F:
        case 0x2540:
        case 0x2541:
        case 0x2542:
        case 0x2543:
        case 0x2544:
        case 0x2545:
        case 0x2546:
        case 0x2547:
        case 0x2548:
        case 0x2549:
        case 0x254A:
        case 0x254B:
        case 0x256A:
        case 0x256B:
        case 0x256C:
            // ┼
            return 0x253C;

        case 0x2580:
        case 0x2581:
        case 0x2582:
        case 0x2583:
        case 0x2584:
        case 0x2585:
        case 0x2586:
        case 0x2587:
        case 0x2588:
        case 0x258A:
        case 0x258B:
        case 0x258C:
        case 0x258D:
        case 0x258E:
        case 0x258F:
        case 0x2590:
        case 0x2594:
        case 0x2595:
        case 0x2596:
        case 0x2597:
        case 0x2598:
        case 0x2599:
        case 0x259A:
        case 0x259B:
        case 0x259C:
        case 0x259D:
        case 0x259E:
        case 0x259F:
            // Turn most of the Block Elements into full block: █
            return 0x2588;

        default:
            return ch;
        }
    }

    /**
     * Convenience function to open a file in an editor window and make it
     * active.
     *
     * @param filename the file to open
     * @return the editor window opened, either an ExternalTerminalWindow or
     * a InternalEditorWindow, or null if filename exists and is not a file
     * @throws IOException if a java.io operation throws
     */
    public TWindow openEditor(final String filename) throws IOException {

        File file = new File(filename);
        if (file.exists() && !file.isFile()) {
            return null;
        }

        if (getOption("editor.useExternal", "false").equals("true")) {
            ExternalEditorWindow editor;
            editor = new ExternalEditorWindow(this, filename);
            return editor;
        } else {
            return openInternalEditor(filename);
        }
    }

    /**
     * Convenience function to open a file in an internal editor window and
     * make it active.
     *
     * @param filename the file to open
     * @return the editor window, or null if filename exists and is not a
     * file
     * @throws IOException if a java.io operation throws
     */
    public InternalEditorWindow openInternalEditor(final String filename) throws IOException {

        File file = new File(filename);
        if (file.exists() && !file.isFile()) {
            return null;
        }

        InternalEditorWindow editor;
        try {
            editor = new InternalEditorWindow(this, new File(filename),
                0, 0, Math.min(82, getScreen().getWidth()),
                Math.min(25, getDesktopBottom() - getDesktopTop()));
        } catch (IOException e) {
            // Show this exception to the user.
            new TExceptionDialog(this, e);
            return null;
        }
        return editor;
    }

    /**
     * Handle the Panel | Switch To menu item.
     */
    private void menuPanelSwitchTo() {
        Desktop desktop = getCurrentDesktop().getDesktop();

        // System.err.println(desktop.toPrettyString());

        if (desktop.getChildren().size() == 0) {
            return;
        }

        // Grab the active child, and then drill up to the next TSplitPane.
        TWidget widget = desktop.getActiveChild();
        while ((widget.getParent() != desktop)
            && !(widget.getParent() instanceof TSplitPane)
        ) {
            widget = widget.getParent();
        }
        if ((widget instanceof PluginWidget)
            || (widget instanceof TiledTerminal)
        ) {
            getCurrentDesktop().addWindow(new SwitchWidgetWindow(this));
        }
    }

    /**
     * Handle the Window | To Desktop menu item.
     */
    private void menuWindowToDesktop() {
        TWindow window = getActiveWindow();
        if (window == null) {
            return;
        }
        if ((desktopPager != null) && (desktopPager.getWindow() == window)) {
            return;
        }

        TInputBox inputBox = inputBox(i18n.
            getString("windowToDesktopInputBoxTitle"),
            i18n.getString("windowToDesktopInputBoxCaption"), "",
            TInputBox.Type.OKCANCEL);

        if (!inputBox.isOk()) {
            return;
        }

        if (inputBox.getText().trim().length() == 0) {
            return;
        }

        int desktopNumber = desktopIndex;
        try {
            desktopNumber = Integer.parseInt(inputBox.getText().trim());
        } catch (NumberFormatException e) {
            // SQUASH
        }
        if ((desktopNumber < 1)
            || (desktopNumber > desktops.size() - 1)
            || (desktopNumber == desktopIndex)
        ) {
            return;
        }

        // We are moving the window.
        window.hide();
        getCurrentDesktop().removeWindow(window);
        desktops.get(desktopNumber).addWindow(window);
    }

    /**
     * Handle the Edit | Find menu item.
     */
    private void menuSearchFind() {
        getCurrentDesktop().addWindow(new SearchInputWindow(this, false));
    }

    /**
     * Handle the Edit | Replace menu item.
     */
    private void menuSearchReplace() {
        getCurrentDesktop().addWindow(new SearchInputWindow(this, true));
    }

    /**
     * Handle the Edit | Search again menu item.
     */
    private void menuSearchAgain() {
        if (searchText.length() == 0) {
            return;
        }
        TWindow window = getActiveWindow();
        if (!(window instanceof InternalEditorWindow)) {
            return;
        }
        InternalEditorWindow editor = (InternalEditorWindow) window;
        assert (editor.getZ() == 0);

        int line = editor.getEditingRowNumber();
        line++;
        if (line > editor.getLineCount()) {
            line = 1;
        }
        SearchInputWindow.searchEditor(editor, searchText,
            (searchDirection == 1 ? true : false),
            line, (searchScope == 2 ? true : false),
            searchCaseSensitive, searchRegularExpression, searchWholeWordsOnly);
    }

    /**
     * Handle the Edit | Go to line menu item.
     */
    private void menuSearchGoToLine() {
        TWindow window = getActiveWindow();
        if (window instanceof InternalEditorWindow) {
            TInputBox inputBox = inputBox(i18n.
                getString("searchGoToLineInputBoxTitle"),
                i18n.getString("searchGoToLineInputBoxCaption"), "",
                TInputBox.Type.OKCANCEL);

            if (!inputBox.isOk()) {
                return;
            }

            InternalEditorWindow editor = (InternalEditorWindow) window;
            if (inputBox.getText().trim().length() > 0) {
                int lineNumber = editor.getEditingRowNumber();
                try {
                    lineNumber = Integer.parseInt(inputBox.getText().trim());
                    if (lineNumber < 0) {
                        editor.setEditingRowNumber(1);
                    } else if (lineNumber > editor.getLineCount()) {
                        editor.setEditingRowNumber(editor.getLineCount());
                    } else {
                        editor.setEditingRowNumber(lineNumber);
                    }
                } catch (NumberFormatException e) {
                    // SQUASH
                }
            }
        }
    }

}
