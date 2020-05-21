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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TDesktop;
import jexer.TExceptionDialog;
import jexer.TInputBox;
import jexer.TSplitPane;
import jexer.TTerminalWidget;
import jexer.TWidget;
import jexer.event.TMenuEvent;

/**
 * TiledTerminal is a terminal widget that can be split vertically or
 * horizontally into two terminals.
 */
public class TiledTerminal extends TTerminalWidget {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(TiledTerminal.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * If we are in the middle of close, do not call remove().
     */
    private boolean inClose = false;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent application
     */
    public TiledTerminal(final TWidget parent) {
        super(parent, 0, 0, parent.getWidth(), parent.getHeight(),
            new TAction() {
                public void DO() {
                    if (source.getParent() instanceof TSplitPane) {
                        ((TSplitPane) source.getParent()).removeSplit(source,
                            true);
                        return;
                    }
                    if (source.getParent() instanceof Desktop) {
                        // This is the top-level terminal, clear the
                        // shortcuts for the desktop.
                        ((Desktop) source.getParent()).clearTerminalShortcuts();
                    }
                    if (source.getParent() instanceof TDesktop) {
                        source.remove();
                        return;
                    }
                }
            });
        if (getApplication().getDesktop() instanceof Desktop) {
            ((Desktop) (getApplication().getDesktop())).addTerminalShortcuts();
        }
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

        case XTWMApplication.MENU_TERMINAL_HORIZONTAL_SPLIT:
            splitHorizontal(false, new TiledTerminal(getParent()));
            getParent().setEchoKeystrokes(isEchoKeystrokes(), true);
            return;

        case XTWMApplication.MENU_TERMINAL_VERTICAL_SPLIT:
            splitVertical(false, new TiledTerminal(getParent()));
            getParent().setEchoKeystrokes(isEchoKeystrokes(), true);
            return;

        case XTWMApplication.MENU_TERMINAL_SESSION_SAVE_HTML:
            try {
                String filename = fileSaveBox(".");
                if (filename != null) {
                    writer = new BufferedWriter(new FileWriter(filename));
                    writer.write("<html><body bgcolor=\"black\">\n<pre {font-family: 'Courier New', monospace;}><code>");
                    writeSessionAsHtml(writer);
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
                    writeSessionAsText(writer);
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
            signalShellChildProcess("SIGTERM");
            return;

        case XTWMApplication.MENU_TERMINAL_SESSION_SEND_OTHER_SIGNAL:
            inputBox = inputBox(i18n.getString("signalInputBoxTitle"),
                i18n.getString("signalInputBoxCaption"), "",
                TInputBox.Type.OKCANCEL);
            if (inputBox.isOk()) {
                try {
                    int signal = Integer.parseInt(inputBox.getText());
                    signalShellChildProcess(signal);
                } catch (NumberFormatException e) {
                    // Squash the exception, this is a signal name instead.
                    signalShellChildProcess(inputBox.getText());
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
    // TTerminalWidget --------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle widget close.
     */
    @Override
    public void close() {
        super.close();

        if (getParent() instanceof TSplitPane) {
            ((TSplitPane) getParent()).removeSplit(this, true);
        }
        if (getParent() instanceof Desktop) {
            // This is the top-level terminal, clear the shortcuts for the
            // desktop.
            ((Desktop) getParent()).clearTerminalShortcuts();
        }
        if (getParent() instanceof TDesktop) {
            if (!inClose) {
                inClose = true;
                remove();
            }
        }
    }

    // ------------------------------------------------------------------------
    // TiledTerminal ----------------------------------------------------------
    // ------------------------------------------------------------------------

}
