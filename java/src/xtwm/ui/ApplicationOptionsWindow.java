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
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TApplication;
import jexer.TButton;
import jexer.TCheckBox;
import jexer.TComboBox;
import jexer.TField;
import jexer.TWindow;
import jexer.bits.CellAttributes;
import jexer.event.TKeypressEvent;
import static jexer.TKeypress.*;

import xtwm.plugins.ScreensaverPlugin;

/**
 * This window is used to configure the overall XtermWM application
 * preferences.
 */
public class ApplicationOptionsWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(ApplicationOptionsWindow.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Options for using ptypipe.
     */
    private TComboBox ptypipe = null;

    /**
     * Whether or not terminal windows close on exit.
     */
    private TCheckBox closeOnExit = null;

    /**
     * Terminal scrollback size.
     */
    private TField scrollbackMax = null;

    /**
     * Whether or not windows raise when the mouse moves over them.
     */
    private TCheckBox windowFocusFollowsMouse = null;

    /**
     * Whether or not panels get focus when the mouse moves over them.
     */
    private TCheckBox panelFocusFollowsMouse = null;

    /**
     * Whether or not new windows are placed so as to minimize overlap.
     */
    private TCheckBox smartPlacement = null;

    /**
     * Number of desktops.
     */
    private TField desktopCount = null;

    /**
     * Whether or not to show the desktop pager on startup.
     */
    private TCheckBox desktopPager = null;

    /**
     * Whether or not to hide the top line menu bar.
     */
    private TCheckBox hideMenuBar = null;

    /**
     * Whether or not to hide the bottom line status bar.
     */
    private TCheckBox hideStatusBar = null;

    /**
     * Whether or not to have a confirmation dialog when exiting.
     */
    private TCheckBox confirmOnExit = null;

    /**
     * Options for hiding the text mouse.
     */
    private TComboBox hideTextMouse = null;

    /**
     * Whether or not to show a clock on the menu bar.
     */
    private TCheckBox menuTrayClock = null;

    /**
     * The format for the time display for the clock on the menu bar.
     */
    private TField menuTrayClockFormat = null;

    /**
     * Whether or not to show the desktop number on the menu bar.
     */
    private TCheckBox menuTrayDesktop = null;

    /**
     * The number of seconds to wait for the screensaver to turn on.
     */
    private TField screensaverTimeout = null;

    /**
     * Whether or not the screensaver will require a password.
     */
    private TCheckBox screensaverLock = null;

    /**
     * The password required to unlock the screensaver.
     */
    private TField lockScreenPassword = null;

    /**
     * The selected system screensaver.
     */
    private TComboBox screensaver = null;

    /**
     * Whether or not to convert box-drawing glyphs to simple lines.
     */
    private TCheckBox simpleBoxGlyphs = null;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent the main application
     */
    public ApplicationOptionsWindow(final TApplication parent) {

        super(parent, i18n.getString("windowTitle"), 0, 0, 76, 24,
            MODAL | CENTERED);

        final XTWMApplication app = ((XTWMApplication) getApplication());

        final int buttonOffset = 14;

        // Create a status bar
        statusBar = newStatusBar(i18n.getString("statusBar"));

        // ptypipe option
        addLabel(i18n.getString("usePtypipe"), 3, 2, "tcheckbox.inactive",
            false,
            new TAction() {
                public void DO() {
                    ptypipe.activate();
                }
            });
        List<String> ptypipeOptions = new ArrayList<String>();
        ptypipeOptions.add("auto");
        ptypipeOptions.add("true");
        ptypipeOptions.add("false");
        ptypipe = addComboBox(23, 2, 10, ptypipeOptions, 0, 5,
            new TAction() {
                public void DO() {
                    app.setOption("jexer.TTerminal.ptypipe",
                        ptypipe.getText());
                }
            });
        ptypipe.setText(app.getOption("jexer.TTerminal.ptypipe"), false);
        closeOnExit = addCheckBox(3, 3, i18n.getString("closeWindowOnExit"),
            app.getOption("jexer.TTerminal.closeOnExit").equals("true"));

        addLabel(i18n.getString("scrollbackLines"), 3, 4, "ttext", false,
            new TAction() {
                public void DO() {
                    scrollbackMax.activate();
                }
            });
        scrollbackMax = addField(27, 4, 6, false,
            app.getOption("jexer.TTerminal.scrollbackMax"));

        // Window / panel options
        windowFocusFollowsMouse = addCheckBox(39, 2,
            i18n.getString("windowFocusFollowsMouse"),
            app.getOption("window.focusFollowsMouse", "false").equals("true"));
        panelFocusFollowsMouse = addCheckBox(39, 3,
            i18n.getString("panelFocusFollowsMouse"),
            app.getOption("panel.focusFollowsMouse", "false").equals("true"));
        smartPlacement = addCheckBox(39, 4, i18n.getString("smartPlacement"),
            app.getOption("window.smartPlacement", "true").equals("true"));

        // Desktop count: label, field, spinner
        addLabel(i18n.getString("desktopCount"), 39, 5, "ttext", false,
            new TAction() {
                public void DO() {
                    desktopCount.activate();
                }
            });
        desktopCount = addField(66, 5, 3, false,
            app.getOption("desktop.count", "4"));
        addSpinner(69, 5,
            new TAction() {
                public void DO() {
                    try {
                        int newVal = Integer.parseInt(desktopCount.getText());
                        newVal++;
                        desktopCount.setText(Integer.toString(newVal));
                    } catch (NumberFormatException e) {
                        // SQUASH
                    }
                }
            },
            new TAction() {
                public void DO() {
                    try {
                        int newVal = Integer.parseInt(desktopCount.getText());
                        newVal--;
                        if (newVal >= 1) {
                            desktopCount.setText(Integer.toString(newVal));
                        }
                    } catch (NumberFormatException e) {
                        // SQUASH
                    }
                }
            }
        );

        desktopPager = addCheckBox(39, 6, i18n.getString("desktopPager"),
            app.getOption("desktop.pager", "true").equals("true"));


        // Application options
        hideMenuBar = addCheckBox(3, 10, i18n.getString("hideMenuBar"),
            app.getOption("xtwm.hideMenuBar").equals("true"));
        hideStatusBar = addCheckBox(3, 11, i18n.getString("hideStatusBar"),
            app.getOption("xtwm.hideStatusBar").equals("true"));
        confirmOnExit = addCheckBox(3, 12, i18n.getString("confirmOnExit"),
            app.getOption("xtwm.confirmOnExit").equals("true"));
        addLabel(i18n.getString("hideTextMouse"), 3, 13, "tcheckbox.inactive",
            false,
            new TAction() {
                public void DO() {
                    hideTextMouse.activate();
                }
            });
        List<String> hideTextMouseOptions = new ArrayList<String>();
        hideTextMouseOptions.add("always");
        hideTextMouseOptions.add("never");
        hideTextMouseOptions.add("swing");
        hideTextMouse = addComboBox(23, 13, 10, hideTextMouseOptions, 0, 5,
            new TAction() {
                public void DO() {
                    app.setOption("xtwm.hideTextMouse",
                        hideTextMouse.getText());
                }
            });
        hideTextMouse.setText(app.getOption("xtwm.hideTextMouse"), false);
        menuTrayClock = addCheckBox(3, 14, i18n.getString("menuTrayClock"),
            app.getOption("menuTray.clock").equals("true"));

        addLabel(i18n.getString("menuTrayClockFormat"), 3, 15, "ttext", false,
            new TAction() {
                public void DO() {
                    menuTrayClockFormat.activate();
                }
            });
        menuTrayClockFormat = addField(23, 15, 10, false,
            app.getOption("menuTray.clock.format"));

        menuTrayDesktop = addCheckBox(3, 16, i18n.getString("menuTrayDesktop"),
            app.getOption("menuTray.desktop").equals("true"));
        simpleBoxGlyphs = addCheckBox(3, 17, i18n.getString("simpleBoxGlyphs"),
            app.getOption("xtwm.simpleBoxGlyphs").equals("true"));

        // Screensaver options
        addLabel(i18n.getString("screensaverTimeout"), 39, 10, "ttext", false,
            new TAction() {
                public void DO() {
                    screensaverTimeout.activate();
                }
            });
        screensaverTimeout = addField(66, 10, 5, false,
            app.getOption("screensaver.timeout"));
        screensaverLock = addCheckBox(39, 11, i18n.getString("screensaverLock"),
            app.getOption("screensaver.lock").equals("true"));
        addLabel(i18n.getString("lockScreenPassword"), 39, 12, "ttext", false,
            new TAction() {
                public void DO() {
                    lockScreenPassword.activate();
                }
            });
        lockScreenPassword = addField(61, 12, 10, false,
            app.getOption("xtwm.lockScreenPassword"));

        addLabel(i18n.getString("screensaverList"), 39, 13,
            "tcheckbox.inactive", false,
            new TAction() {
                public void DO() {
                    screensaver.activate();
                }
            });
        List<String> screensaverList = new ArrayList<String>();
        for (ScreensaverPlugin screensaver: app.getScreensavers()) {
            screensaverList.add(screensaver.getPluginName());
        }
        screensaver = addComboBox(49, 13, 22, screensaverList, 0, 5,
            new TAction() {
                public void DO() {
                    app.setOption("xtwm.screensaver", screensaver.getText());
                }
            });
        screensaver.setText(app.getOption("xtwm.screensaver"), false);

        // Buttons
        addButton(i18n.getString("saveButton"), 10, getHeight() - 4,
            new TAction() {
                public void DO() {
                    // Copy values from window to properties, save and close
                    // window.
                    ApplicationOptionsWindow.this.copyOptions();
                    ApplicationOptionsWindow.this.saveOptions();
                    ApplicationOptionsWindow.this.close();
                }
            });

        addButton(i18n.getString("resetButton"), 25, getHeight() - 4,
            new TAction() {
                public void DO() {
                    // Reset to defaults, copy values into window.
                    ApplicationOptionsWindow.this.resetOptions();
                    ApplicationOptionsWindow.this.loadOptions();
                }
            });

        addButton(i18n.getString("okButton"), 40, getHeight() - 4,
            new TAction() {
                public void DO() {
                    // Copy values from window to properties, close window.
                    ApplicationOptionsWindow.this.copyOptions();
                    ApplicationOptionsWindow.this.close();
                }
            });

        TButton cancelButton = addButton(i18n.getString("cancelButton"),
            55, getHeight() - 4,
            new TAction() {
                public void DO() {
                    // Don't copy anything, just close the window.
                    ApplicationOptionsWindow.this.close();
                }
            });

        // Save this for last: make the cancel button default action.
        activate(cancelButton);

        // Reset window fields to the application options.
        loadOptions();
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

        CellAttributes boxColor = getTheme().getColor("ttext");

        int column2 = 38;

        // Terminals
        drawBox(2, 2, 36, 7, boxColor, boxColor);
        putStringXY(4, 2, i18n.getString("osShellTitle"), boxColor);

        // Windows/Panels
        drawBox(column2, 2, column2 + 36, 9, boxColor, boxColor);
        putStringXY(column2 + 2, 2, i18n.getString("windowsTitle"), boxColor);

        // Application
        drawBox(2, 10, 36, 20, boxColor, boxColor);
        putStringXY(4, 10, i18n.getString("applicationTitle"), boxColor);

        // Screensaver
        drawBox(column2, 10, column2 + 36, 16, boxColor, boxColor);
        putStringXY(column2 + 2, 10, i18n.getString("screensaverTitle"),
            boxColor);
    }

    // ------------------------------------------------------------------------
    // ApplicationOptionsWindow -----------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Copy options from window fields to the application properties.
     */
    private void copyOptions() {
        XTWMApplication app = ((XTWMApplication) getApplication());

        app.setOption("jexer.TTerminal.ptypipe", ptypipe.getText());

        app.setOption("jexer.TTerminal.closeOnExit",
            (closeOnExit.isChecked() ? "true" : "false"));

        app.setOption("jexer.TTerminal.scrollbackMax",
            scrollbackMax.getText());

        app.setOption("window.focusFollowsMouse",
            (windowFocusFollowsMouse.isChecked() ? "true" : "false"));

        app.setOption("panel.focusFollowsMouse",
            (panelFocusFollowsMouse.isChecked() ? "true" : "false"));

        app.setOption("window.smartPlacement",
            (smartPlacement.isChecked() ? "true" : "false"));

        try {
            int count = Integer.parseInt(desktopCount.getText());
            app.setOption("desktop.count", Integer.toString(count));
        } catch (NumberFormatException e) {
            // SQUASH
        }

        app.setOption("desktop.pager",
            (desktopPager.isChecked() ? "true" : "false"));

        app.setOption("xtwm.hideMenuBar",
            (hideMenuBar.isChecked() ? "true" : "false"));

        app.setOption("xtwm.hideStatusBar",
            (hideStatusBar.isChecked() ? "true" : "false"));

        app.setOption("xtwm.confirmOnExit",
            (confirmOnExit.isChecked() ? "true" : "false"));

        app.setOption("xtwm.hideTextMouse", hideTextMouse.getText());

        app.setOption("menuTray.clock",
            (menuTrayClock.isChecked() ? "true" : "false"));

        app.setOption("menuTray.clock.format", menuTrayClockFormat.getText());

        app.setOption("menuTray.desktop",
            (menuTrayDesktop.isChecked() ? "true" : "false"));

        app.setOption("xtwm.simpleBoxGlyphs",
            (simpleBoxGlyphs.isChecked() ? "true" : "false"));

        try {
            int timeout = Integer.parseInt(screensaverTimeout.getText());
            if (timeout >= 0) {
                app.setOption("screensaver.timeout", Integer.toString(timeout));
            }
        } catch (NumberFormatException e) {
            // SQUASH
        }

        app.setOption("screensaver.lock",
            (screensaverLock.isChecked() ? "true" : "false"));

        app.setOption("xtwm.lockScreenPassword", lockScreenPassword.getText());

        app.setOption("xtwm.screensaver", screensaver.getText());


        // Make these options effective for the running session.
        app.resolveOptions();
    }

    /**
     * Save application properties.
     */
    private void saveOptions() {
        XTWMApplication app = ((XTWMApplication) getApplication());
        app.saveOptions();
    }

    /**
     * Copy options from the application properties to window fields.
     */
    private void loadOptions() {
        XTWMApplication app = ((XTWMApplication) getApplication());

        ptypipe.setText(app.getOption("jexer.TTerminal.ptypipe"), false);

        closeOnExit.setChecked(app.getOption("jexer.TTerminal.closeOnExit").
            equals("true"));

        scrollbackMax.setText(app.getOption("jexer.TTerminal.scrollbackMax"));

        windowFocusFollowsMouse.setChecked(app.getOption(
            "window.focusFollowsMouse").equals("true"));

        panelFocusFollowsMouse.setChecked(app.getOption(
            "panel.focusFollowsMouse").equals("true"));

        smartPlacement.setChecked(app.getOption("window.smartPlacement").
            equals("true"));

        desktopCount.setText(app.getOption("desktop.count"));

        desktopPager.setChecked(app.getOption("desktop.pager").
            equals("true"));

        hideMenuBar.setChecked(app.getOption("xtwm.hideMenuBar").
            equals("true"));

        hideStatusBar.setChecked(app.getOption("xtwm.hideStatusBar").
            equals("true"));

        confirmOnExit.setChecked(app.getOption("xtwm.confirmOnExit").
            equals("true"));

        hideTextMouse.setText(app.getOption("xtwm.hideTextMouse"));

        menuTrayClock.setChecked(app.getOption("menuTray.clock").
            equals("true"));

        menuTrayClockFormat.setText(app.getOption("menuTray.clock.format"));

        menuTrayDesktop.setChecked(app.getOption("menuTray.desktop").
            equals("true"));

        simpleBoxGlyphs.setChecked(app.getOption("xtwm.simpleBoxGlyphs").
            equals("true"));

        screensaverTimeout.setText(app.getOption("screensaver.timeout"));

        screensaverLock.setChecked(app.getOption("screensaver.lock").
            equals("true"));

        lockScreenPassword.setText(app.getOption("xtwm.lockScreenPassword"));

        screensaver.setText(app.getOption("xtwm.screensaver"));
    }

    /**
     * Reset options from the application.
     */
    private void resetOptions() {
        XTWMApplication app = ((XTWMApplication) getApplication());
        app.setDefaultOptions();
    }

}
