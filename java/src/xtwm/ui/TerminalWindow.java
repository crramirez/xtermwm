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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ResourceBundle;

import jexer.TApplication;
import jexer.TExceptionDialog;
import jexer.TInputBox;
import jexer.TTerminalWindow;
import jexer.event.TMenuEvent;

/**
 * TerminalWindow is a terminal window with a few extra menu functions.
 */
public class TerminalWindow extends TTerminalWindow {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(TerminalWindow.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor spawns a shell.
     *
     * @param application TApplication that manages this window
     */
    public TerminalWindow(final TApplication application) {
        super(application, 0, 0, RESIZABLE);
    }

    /**
     * Public constructor spawns a shell.
     *
     * @param application TApplication that manages this window
     * @param commandLine the command line to execute
     */
    public TerminalWindow(final TApplication application,
        final String commandLine) {

        super(application, 0, 0, RESIZABLE, commandLine.split("\\s+"));
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
        TInputBox inputBox;
        Writer writer = null;

        switch (event.getId()) {

        case XTWMApplication.MENU_TERMINAL_SESSION_SAVE_HTML:
            try {
                String filename = fileSaveBox(".");
                if (filename != null) {
                    writer = new BufferedWriter(new FileWriter(filename));
                    writer.write("<html><body bgcolor=\"black\">\n<pre {font-family: 'Courier New', monospace;}><code>");
                    terminal.writeSessionAsHtml(writer);
                    writer.write("</code></body></html>");
                }
            } catch (IOException e) {
                // Show this exception to the user.
                new TExceptionDialog(getApplication(), e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        // SQUASH
                    }
                }
            }
            return;

        case XTWMApplication.MENU_TERMINAL_SESSION_SAVE_TEXT:
            try {
                String filename = fileSaveBox(".");
                if (filename != null) {
                    writer = new BufferedWriter(new FileWriter(filename));
                    terminal.writeSessionAsText(writer);
                }
            } catch (IOException e) {
                // Show this exception to the user.
                new TExceptionDialog(getApplication(), e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        // SQUASH
                    }
                }
            }
            return;

        case XTWMApplication.MENU_TERMINAL_SESSION_SEND_SIGTERM:
            if (terminal.isReading()) {
                terminal.signalShellChildProcess("SIGTERM");
            }
            return;

        case XTWMApplication.MENU_TERMINAL_SESSION_SEND_OTHER_SIGNAL:
            if (terminal.isReading()) {
                inputBox = inputBox(i18n.getString("signalInputBoxTitle"),
                    i18n.getString("signalInputBoxCaption"), "",
                    TInputBox.Type.OKCANCEL);
                if (inputBox.isOk()) {
                    try {
                        int signal = Integer.parseInt(inputBox.getText());
                        terminal.signalShellChildProcess(signal);
                    } catch (NumberFormatException e) {
                        // Squash the exception, this is a signal name instead.
                        terminal.signalShellChildProcess(inputBox.getText());
                    }
                }
            }
            return;

        case XTWMApplication.MENU_TERMINAL_CLOSE:
            close();
            return;

        default:
            break;
        }

        // I didn't take it, pass it on.
        super.onMenu(event);
    }

    // ------------------------------------------------------------------------
    // TTerminalWindow --------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // TerminalWindow ---------------------------------------------------------
    // ------------------------------------------------------------------------

}
