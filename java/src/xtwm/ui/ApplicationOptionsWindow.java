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

/**
 * This window is used to configure the TJ overall application preferences.
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
     * Triple-buffer support.
     */
    private TCheckBox tripleBuffer;

    /**
     * Cursor style.
     */
    private TComboBox cursorStyle;

    /**
     * Sixel support.
     */
    private TCheckBox sixel;

    /**
     * 24-bit RGB color for normal system colors.
     */
    private TCheckBox rgbColor;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent the main application
     */
    public ApplicationOptionsWindow(final TApplication parent) {

        super(parent, i18n.getString("windowTitle"), 0, 0, 76, 18,
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
        ptypipe = addComboBox(22, 2, 10, ptypipeOptions, 0, 5,
            new TAction() {
                public void DO() {
                    app.setOption("jexer.TTerminal.ptypipe",
                        ptypipe.getText());
                }
            });
        ptypipe.setText(app.getOption("jexer.TTerminal.ptypipe"), false);
        closeOnExit = addCheckBox(3, 3, i18n.getString("closeWindowOnExit"),
            app.getOption("jexer.TTerminal.closeOnExit").equals("true"));

        tripleBuffer = addCheckBox(3, 7, i18n.getString("tripleBuffer"),
            app.getOption("jexer.Swing.tripleBuffer", "true").equals("true"));

        addLabel(i18n.getString("cursorStyle"), 3, 8, "ttext", false,
            new TAction() {
                public void DO() {
                    cursorStyle.activate();
                }
            });
        ArrayList<String> cursorStyles = new ArrayList<String>();
        cursorStyles.add(i18n.getString("cursorStyleBlock").toLowerCase());
        cursorStyles.add(i18n.getString("cursorStyleOutline").toLowerCase());
        cursorStyles.add(i18n.getString("cursorStyleUnderline").toLowerCase());
        cursorStyle = addComboBox(22, 8, 25, cursorStyles, 0, 4, null);
        cursorStyle.setText(app.getOption("jexer.Swing.cursorStyle",
                "underline").toLowerCase());

        sixel = addCheckBox(3, 12, i18n.getString("sixel"),
            app.getOption("jexer.ECMA48.sixel", "true").equals("true"));
        rgbColor = addCheckBox(3, 13, i18n.getString("rgbColor"),
            app.getOption("jexer.ECMA48.rgbColor", "false").equals("true"));

        // Buttons
        addButton(i18n.getString("saveButton"), getWidth() - buttonOffset, 4,
            new TAction() {
                public void DO() {
                    // Copy values from window to properties, save and close
                    // window.
                    ApplicationOptionsWindow.this.copyOptions();
                    ApplicationOptionsWindow.this.saveOptions();
                    ApplicationOptionsWindow.this.close();
                }
            });

        addButton(i18n.getString("resetButton"), getWidth() - buttonOffset, 6,
            new TAction() {
                public void DO() {
                    // Reset to defaults, copy values into window.
                    ApplicationOptionsWindow.this.resetOptions();
                    ApplicationOptionsWindow.this.loadOptions();
                }
            });

        addButton(i18n.getString("okButton"), getWidth() - buttonOffset, 8,
            new TAction() {
                public void DO() {
                    // Copy values from window to properties, close window.
                    ApplicationOptionsWindow.this.copyOptions();
                    ApplicationOptionsWindow.this.close();
                }
            });

        TButton cancelButton = addButton(i18n.getString("cancelButton"),
            getWidth() - buttonOffset, 10,
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

        // Shells
        drawBox(2, 2, 50, 6, boxColor, boxColor);
        putStringXY(4, 2, i18n.getString("osShellTitle"), boxColor);

        // Swing backend options
        drawBox(2, 7, 50, 11, boxColor, boxColor);
        putStringXY(4, 7, i18n.getString("swingTitle"), boxColor);

        // ECMA48 backend options
        drawBox(2, 12, 50, 16, boxColor, boxColor);
        putStringXY(4, 12, i18n.getString("ecma48Title"), boxColor);

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

        if (closeOnExit.isChecked()) {
            app.setOption("jexer.TTerminal.closeOnExit", "true");
        } else {
            app.setOption("jexer.TTerminal.closeOnExit", "false");
        }

        if (tripleBuffer.isChecked()) {
            app.setOption("jexer.Swing.tripleBuffer", "true");
        } else {
            app.setOption("jexer.Swing.tripleBuffer", "false");
        }

        app.setOption("jexer.Swing.cursorStyle",
            cursorStyle.getText().toLowerCase());

        if (rgbColor.isChecked()) {
            app.setOption("jexer.ECMA48.rgbColor", "true");
        } else {
            app.setOption("jexer.ECMA48.rgbColor", "false");
        }

        if (sixel.isChecked()) {
            app.setOption("jexer.ECMA48.sixel", "true");
        } else {
            app.setOption("jexer.ECMA48.sixel", "false");
        }

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

        tripleBuffer.setChecked(app.getOption("jexer.Swing.tripleBuffer").
            equals("true"));

        cursorStyle.setText(app.getOption("jexer.Swing.cursorStyle").
            toLowerCase());

        rgbColor.setChecked(app.getOption("jexer.ECMA48.rgbColor").
            equals("true"));

        sixel.setChecked(app.getOption("jexer.ECMA48.sixel").
            equals("true"));

    }

    /**
     * Reset options from the application.
     */
    private void resetOptions() {
        XTWMApplication app = ((XTWMApplication) getApplication());
        app.setDefaultOptions();
    }

}
