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
import java.util.HashMap;
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TButton;
import jexer.TList;
import jexer.TWindow;
import jexer.backend.Backend;
import jexer.backend.MultiBackend;
import jexer.backend.SessionInfo;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.net.TelnetInputStream;
import jexer.event.TKeypressEvent;
import static jexer.TKeypress.*;

import xtwm.ui.XTWMApplication;

/**
 * ClientsWindow is the UI to manage connected clients of a shared session.
 */
public class ClientsWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(ClientsWindow.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The list of clients pane.
     */
    private TList clients;

    /**
     * The clients by list ID.
     */
    private HashMap<Integer, Backend> clientsById;

    /**
     * The "Read-Only" button.
     */
    private TButton readOnlyButton;

    /**
     * The "Read+Write" button.
     */
    private TButton readWriteButton;

    /**
     * The "Disconnect" button.
     */
    private TButton disconnectButton;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.  The window will be centered on screen.
     *
     * @param application the TApplication that manages this window
     */
    public ClientsWindow(final XTWMApplication application) {

        // Register with the TApplication
        super(application, i18n.getString("windowTitle"), 0, 0, 68, 18, MODAL);

        clients = addList(new ArrayList<String>(), 1, 3,
            getWidth() - 4, getHeight() - 8, null,
            new TAction() {
                // When the user navigates
                public void DO() {
                    refreshClientsList();
                }
            });

        readOnlyButton = addButton(i18n.getString("readOnlyButton"),
            getWidth() - 67, getHeight() - 4,

            new TAction() {
                public void DO() {
                    int index = clients.getSelectedIndex();
                    Backend backend = clientsById.get(index);
                    if (backend != null) {
                        readOnlyButton.setEnabled(false);
                        readWriteButton.setEnabled(true);
                        backend.setReadOnly(true);
                        refreshClientsList();
                    }
                }
            }
        );

        readWriteButton = addButton(i18n.getString("readWriteButton"),
            getWidth() - 47, getHeight() - 4,

            new TAction() {
                public void DO() {
                    int index = clients.getSelectedIndex();
                    Backend backend = clientsById.get(index);
                    if (backend != null) {
                        readOnlyButton.setEnabled(true);
                        readWriteButton.setEnabled(false);
                        backend.setReadOnly(false);
                        refreshClientsList();
                    }
                }
            }
        );

        disconnectButton = addButton(i18n.getString("disconnectButton"),
            getWidth() - 27, getHeight() - 4,

            new TAction() {
                public void DO() {
                    int index = clients.getSelectedIndex();
                    Backend backend = clientsById.get(index);
                    if (backend != null) {
                        backend.shutdown();
                        refreshClientsList();
                    }
                }
            }
        );

        addButton(i18n.getString("closeButton"),
            getWidth() - 11, getHeight() - 4,
            new TAction() {
                public void DO() {
                    getApplication().closeWindow(ClientsWindow.this);
                }
            }
        );

        // Default to the clients list
        activate(clients);
        refreshClientsList();

        // Add shortcut text
        newStatusBar(i18n.getString("statusBar"));
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

    /**
     * Method that subclasses can override to do processing when the UI is
     * idle.  Note that repainting is NOT assumed.  To get a refresh after
     * onIdle, call doRepaint().
     */
    @Override
    public void onIdle() {
        refreshClientsList();
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

        CellAttributes color = getTheme().getColor("ttext");

        hLineXY(2, 2, getWidth() - 4, ' ', color);
        putStringXY(2, 2, String.format("%-12s %c %11s %c %9s %c %10s",
                i18n.getString("username"),
                GraphicsChars.VERTICAL_BAR,
                i18n.getString("permissions"),
                GraphicsChars.VERTICAL_BAR,
                i18n.getString("connected"),
                GraphicsChars.VERTICAL_BAR,
                i18n.getString("idle")), color);

        hLineXY(2, 3, getWidth() - 4, GraphicsChars.DOUBLE_BAR, color);
        putCharXY(15, 3, 0x256A, color);
        putCharXY(29, 3, 0x256A, color);
        putCharXY(41, 3, 0x256A, color);
    }

    // ------------------------------------------------------------------------
    // ClientsWindow ----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Refresh the clients window.
     */
    private void refreshClientsList() {
        List<String> clientStrings = new ArrayList<String>();

        clientsById = new HashMap<Integer, Backend>();

        long now = System.currentTimeMillis();
        XTWMApplication app = (XTWMApplication) getApplication();
        for (Backend backend: ((MultiBackend) app.getBackend()).getBackends()) {
            SessionInfo sessionInfo = backend.getSessionInfo();
            TelnetInputStream telnet = (TelnetInputStream) sessionInfo;
            long connect = sessionInfo.getStartTime();
            int hours = (int)  (((now - connect) / 1000) / 3600);
            int mins  = (int) ((((now - connect) / 1000) % 3600) / 60);
            int secs  = (int)  (((now - connect) / 1000) % 60);
            clientStrings.add(String.format("%-12s %c %11s %c  %02d:%02d:%02d %c %10d",
                    sessionInfo.getUsername(),
                    GraphicsChars.VERTICAL_BAR,
                    (backend.isReadOnly() ? "Read-Only" : "Read+Write"),
                    GraphicsChars.VERTICAL_BAR,
                    hours, mins, secs,
                    GraphicsChars.VERTICAL_BAR,
                    sessionInfo.getIdleTime()));

            clientsById.put(clientStrings.size() - 1, backend);
        }

        int oldIndex = clients.getSelectedIndex();
        clients.setList(clientStrings);
        clients.setSelectedIndex(Math.min(clients.getMaxSelectedIndex(),
                Math.max(oldIndex, 0)));

        int index = clients.getSelectedIndex();
        Backend backend = clientsById.get(index);
        if (backend == null) {
            readOnlyButton.setEnabled(false);
            readWriteButton.setEnabled(false);
            disconnectButton.setEnabled(false);
        } else {
            disconnectButton.setEnabled(true);
            if (backend.isReadOnly()) {
                readOnlyButton.setEnabled(false);
                readWriteButton.setEnabled(true);
            } else {
                readOnlyButton.setEnabled(true);
                readWriteButton.setEnabled(false);
            }
        }
    }

}
