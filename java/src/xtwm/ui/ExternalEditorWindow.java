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

import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import jexer.TTerminalWindow;

import xtwm.ui.XTWMApplication;

/**
 * ExternalEditorWindow is a terminal shell wrapper to an editor.
 */
public class ExternalEditorWindow extends TTerminalWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(ExternalEditorWindow.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The file being edited.
     */
    private String filename;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent the main application
     */
    public ExternalEditorWindow(final XTWMApplication parent) {

        super(parent, 0, 0, getCommandLineNew(parent));

        setTitle(i18n.getString("editorTitle"), true);
        newStatusBar(i18n.getString("statusBarRunning"));
    }

    /**
     * Public constructor.
     *
     * @param parent the main application
     * @param filename a file to open
     */
    public ExternalEditorWindow(final XTWMApplication parent,
        final String filename) {

        super(parent, 0, 0, getCommandLineOpen(parent, filename));

        this.filename = filename;

        setTitle(MessageFormat.format(i18n.getString("editorTitleFilename"),
                new File(filename).getName()), true);
        newStatusBar(i18n.getString("statusBarRunning"));
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // TTerminalWindow --------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Hook for subclasses to be notified of the shell termination.
     */
    @Override
    public void onShellExit() {
        close();
    }

    // ------------------------------------------------------------------------
    // ExternalEditorWindow ---------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the filename being edited.
     *
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Replace $VISUAL and $EDITOR with environment options.
     *
     * @param command the original command line
     * @return the command line with $VISUAL and $EDITOR replaced
     */
    private static String replaceEnvVars(final String command) {
        String result = command;
        String visualEnv = System.getenv("VISUAL");
        String editorEnv = System.getenv("EDITOR");
        if (visualEnv != null) {
            result = result.replace("$VISUAL", visualEnv);
        }
        if (editorEnv != null) {
            result = result.replace("$EDITOR", editorEnv);
        }
        return result;
    }

    /**
     * Get the appropriate command line for the editor.
     *
     * @param app the main application
     * @return the command to spawn an editor
     */
    private static String getCommandLineNew(final XTWMApplication app) {
        String editorBin = app.getOption("editor.external.new");
        return replaceEnvVars(editorBin);
    }

    /**
     * Get the appropriate command line for the editor.
     *
     * @param app the main application
     * @param filename a file to open
     * @return the command to spawn an editor
     */
    private static String getCommandLineOpen(final XTWMApplication app,
        final String filename) {

        String editorBin = app.getOption("editor.external.open");
        String command = MessageFormat.format(editorBin, filename);
        return replaceEnvVars(command);
    }

}
