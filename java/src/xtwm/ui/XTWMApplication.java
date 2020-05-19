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
import java.util.Properties;
import java.util.ResourceBundle;

import jexer.TApplication;
import jexer.TEditColorThemeWindow;
import jexer.TExceptionDialog;
import jexer.TFontChooserWindow;
import jexer.TMessageBox;
import jexer.TStatusBar;
import jexer.backend.Backend;
import jexer.backend.SwingTerminal;
import jexer.bits.CellAttributes;
import jexer.bits.Color;
import jexer.bits.ColorTheme;
import jexer.event.TCommandEvent;
import jexer.event.TKeypressEvent;
import jexer.event.TMenuEvent;
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
     * Available menu commands.  Note many of these are package private and
     * handled by other classes.
     */

    private static final int MENU_FILE_SETUP_DISPLAY                    = 2031;
    private static final int MENU_FILE_SETUP_COLORS                     = 2032;
    private static final int MENU_FILE_SETUP_ENVIRONMENT                = 2033;

    private static final int MENU_FILE_SHELL                            = 2098;
    private static final int MENU_FILE_EXIT                             = 2099;

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

        // Dispatch menu event
        switch (menu.getId()) {

        case MENU_FILE_SHELL:
            // Spawn shell
            openTerminal(0, 0);
            return true;

        case MENU_FILE_EXIT:
            // Post a quit command
            postMenuEvent(new TCommandEvent(cmQuit));
            return true;

        case MENU_FILE_SETUP_DISPLAY:
            new TFontChooserWindow(this);
            return true;

        case MENU_FILE_SETUP_COLORS:
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

        case MENU_FILE_SETUP_ENVIRONMENT:
            new ApplicationOptionsWindow(this);
            return true;

        }

        return super.onMenu(menu);
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

        // File menu ----------------------------------------------------------

        TMenu fileMenu = addMenu(i18n.getString("fileMenuTitle"));

        TSubMenu subSetup = fileMenu.addSubMenu(i18n.getString("fileSetup"));
        subSetup.addItem(MENU_FILE_SETUP_DISPLAY,
            i18n.getString("fileSetupDisplay"));
        subSetup.addItem(MENU_FILE_SETUP_COLORS,
            i18n.getString("fileSetupColors"));
        subSetup.addItem(MENU_FILE_SETUP_ENVIRONMENT,
            i18n.getString("fileSetupEnvironment"));

        fileMenu.addSeparator();
        fileMenu.addItem(MENU_FILE_SHELL, i18n.getString("fileShell"));
        fileMenu.addItem(MENU_FILE_EXIT, i18n.getString("fileExit"), kbAltX);
        TStatusBar fileStatusBar = fileMenu.newStatusBar(i18n.
            getString("fileMenuStatus"));
        fileStatusBar.addShortcutKeypress(kbF1, cmHelp, i18n.getString("Help"));

        // Edit menu ----------------------------------------------------------

        addEditMenu();

        // Window menu --------------------------------------------------------

        addWindowMenu();

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

}
