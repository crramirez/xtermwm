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
package xtwm.ui;

import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TApplication;
import jexer.TButton;
import jexer.TCheckBox;
import jexer.TField;
import jexer.TWindow;
import jexer.bits.CellAttributes;
import jexer.event.TKeypressEvent;
import static jexer.TKeypress.*;

/**
 * This window is used to configure the editor preferences.
 */
public class EditorOptionsWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(EditorOptionsWindow.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The number of "undo/redo" levels.
     */
    private TField undoLevel;

    /**
     * The number of columns to indent.
     */
    private TField indentLevel;

    /**
     * If true, backspace at an indent level goes back a full indent level.
     * If false, backspace always goes back one column.
     */
    private TCheckBox backspaceUnindents;

    /**
     * The margin to highlight.
     */
    private TField margin;

    /**
     * If true, highlight Java keywords in text files.
     */
    private TCheckBox highlightKeywords;

    /**
     * If true, save files with tab characters.  If false, convert tabs to
     * spaces when saving files.
     */
    private TCheckBox saveWithTabs;

    /**
     * If true, trim trailing whitespace from lines and trailing empty lines
     * from the file automatically on save.
     */
    private TCheckBox trimWhitespace;

    /**
     * Whether or not to use an external editor.
     */
    private TCheckBox externalEditor = null;

    /**
     * Name of the external editor when editing a new file.
     */
    private TField externalEditorNew = null;

    /**
     * Name of the external editor when opening an existing file.
     */
    private TField externalEditorOpen = null;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent the main application
     */
    public EditorOptionsWindow(final XTWMApplication parent) {

        super(parent, i18n.getString("windowTitle"), 0, 0, 66, 23,
            MODAL | CENTERED);

        XTWMApplication app = ((XTWMApplication) getApplication());

        final int buttonOffset = 14;

        // Create a status bar
        statusBar = newStatusBar(i18n.getString("statusBar"));

        // Editor options
        addLabel(i18n.getString("undoLevel"), 3, 3, "ttext", false,
            new TAction() {
                public void DO() {
                    undoLevel.activate();
                }
            });
        undoLevel = addField(22, 3, 25, false,
            app.getOption("editor.internal.undoLevel"));

        addLabel(i18n.getString("indentLevel"), 3, 4, "ttext", false,
            new TAction() {
                public void DO() {
                    indentLevel.activate();
                }
            });
        indentLevel = addField(22, 4, 25, false,
            app.getOption("editor.internal.indentLevel"));

        addLabel(i18n.getString("margin"), 3, 5, "ttext", false,
            new TAction() {
                public void DO() {
                    margin.activate();
                }
            });
        margin = addField(22, 5, 25, false,
            app.getOption("editor.internal.margin"));

        backspaceUnindents = addCheckBox(3, 7,
            i18n.getString("backspaceUnindents"),
            (app.getOption("editor.internal.backspaceUnindents",
                "true").equals("true") ? true : false));
        highlightKeywords = addCheckBox(3, 8,
            i18n.getString("highlightKeywords"),
            (app.getOption("editor.internal.highlightKeywords",
                "true").equals("true") ? true : false));

        saveWithTabs = addCheckBox(3, 9,
            i18n.getString("saveWithTabs"),
            (app.getOption("editor.internal.saveWithTabs",
                "true").equals("true") ? true : false));

        trimWhitespace = addCheckBox(3, 10,
            i18n.getString("trimWhitespace"),
            (app.getOption("editor.internal.trimWhitespace",
                "true").equals("true") ? true : false));

        // Use external editor
        externalEditor = addCheckBox(3, 16, i18n.getString("useExternalEditor"),
            app.getOption("editor.useExternal").equals("true"));

        // External editor
        addLabel(i18n.getString("externalEditorNew"), 5, 17, "ttext", false);
        externalEditorNew = addField(22, 17, 25, false,
            app.getOption("editor.external.new"));
        addLabel(i18n.getString("externalEditorOpen"), 5, 18, "ttext", false);
        externalEditorOpen = addField(22, 18, 25, false,
            app.getOption("editor.external.open"));

        // Buttons
        addButton(i18n.getString("saveButton"), getWidth() - buttonOffset, 4,
            new TAction() {
                public void DO() {
                    // Copy values from window to properties, save and close
                    // window.
                    copyOptions();
                    saveOptions();
                    EditorOptionsWindow.this.close();
                }
            });

        addButton(i18n.getString("okButton"), getWidth() - buttonOffset, 6,
            new TAction() {
                public void DO() {
                    // Copy values from window to properties, close window.
                    copyOptions();
                    EditorOptionsWindow.this.close();
                }
            });

        TButton cancelButton = addButton(i18n.getString("cancelButton"),
            getWidth() - buttonOffset, 8,
            new TAction() {
                public void DO() {
                    // Don't copy anything, just close the window.
                    EditorOptionsWindow.this.close();
                }
            });

        // Save this for last: make the cancel button default action.
        activate(cancelButton);
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

        // Internal editor options
        drawBox(2, 2, 50, 14, boxColor, boxColor);
        putStringXY(4, 2, i18n.getString("internalEditorTitle"), boxColor);

        // External editor options
        drawBox(2, 15, 50, 22, boxColor, boxColor);
        putStringXY(4, 15, i18n.getString("externalEditorTitle"), boxColor);

    }

    // ------------------------------------------------------------------------
    // EditorOptionsWindow ----------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Copy options from window fields to the application properties.
     */
    private void copyOptions() {
        XTWMApplication app = ((XTWMApplication) getApplication());
        app.setOption("editor.internal.undoLevel",
            undoLevel.getText());
        app.setOption("editor.internal.indentLevel",
            indentLevel.getText());
        app.setOption("editor.internal.backspaceUnindents",
            backspaceUnindents.isChecked() ? "true" : "false");
        app.setOption("editor.internal.margin",
            margin.getText());
        app.setOption("editor.internal.highlightKeywords",
            highlightKeywords.isChecked() ? "true" : "false");
        app.setOption("editor.internal.saveWithTabs",
            saveWithTabs.isChecked() ? "true" : "false");
        app.setOption("editor.internal.trimWhitespace",
            trimWhitespace.isChecked() ? "true" : "false");

        app.setOption("editor.useExternal",
            externalEditor.isChecked() ? "true" : "false");
        app.setOption("editor.external.new", externalEditorNew.getText());
        app.setOption("editor.external.open", externalEditorOpen.getText());
    }

    /**
     * Save application properties.
     */
    private void saveOptions() {
        XTWMApplication app = ((XTWMApplication) getApplication());
        app.saveOptions();
     }

}
