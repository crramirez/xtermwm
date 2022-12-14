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

import java.util.ArrayList;
import java.util.List;

import jexer.TApplication;
import jexer.TSplitPane;
import jexer.TWidget;
import jexer.TWindow;

/**
 * VirtualDesktop contains a list of windows associated with it.
 */
public class VirtualDesktop {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The windows on this desktop.
     */
    private ArrayList<TWindow> windows = new ArrayList<TWindow>();

    /**
     * The application this desktop is part of.
     */
    private TApplication application;

    /**
     * The desktop on the application background.
     */
    private Desktop desktop;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param application TApplication that manages the windows on this desktop
     */
    public VirtualDesktop(final TApplication application) {
        this.application = application;
        desktop = new Desktop(application);
    }

    // ------------------------------------------------------------------------
    // VirtualDesktop ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Sync windows with application.  Note package private access.
     */
    void sync() {
        for (int i = 0; i < windows.size(); i++) {
            TWindow window = windows.get(i);
            if (!application.hasWindow(window)) {
                // Application doesn't have the window, remove it from this
                // desktop.
                windows.remove(i);
                i--;
            }
        }
    }

    /**
     * Hide all windows on this desktop.
     */
    public void hide() {
        sync();
        for (int i = 0; i < windows.size(); i++) {
            TWindow window = windows.get(i);
            if (window.isShown()) {
                application.hideWindow(window);
            }
        }
    }

    /**
     * Show all windows on this desktop.
     */
    public void show() {
        sync();
        for (int i = 0; i < windows.size(); i++) {
            TWindow window = windows.get(i);
            if (window.isHidden()) {
                application.showWindow(window);
            }
        }
    }

    /**
     * Get the windows on this desktop.
     *
     * @return the windows
     */
    public List<TWindow> getWindows() {
        return windows;
    }

    /**
     * Add a window to this desktop.
     *
     * @param window the window to add
     */
    public void addWindow(final TWindow window) {
        if (window == null) {
            return;
        }
        if (!windows.contains(window)) {
            windows.add(window);
        }
    }

    /**
     * Remove a window from this desktop.
     *
     * @param window the window to remove
     */
    public void removeWindow(final TWindow window) {
        if (window == null) {
            return;
        }
        if (windows.contains(window)) {
            windows.remove(window);
        }
    }

    /**
     * See if a window is associated with this desktop.
     *
     * @param window the window to check
     * @return true if this window is on this desktop
     */
    public boolean hasWindow(final TWindow window) {
        if (window == null) {
            return false;
        }
        return windows.contains(window);
    }

    /**
     * Get the Desktop instance.
     *
     * @return the desktop, or null if it is not set
     */
    public final Desktop getDesktop() {
        return desktop;
    }

    /**
     * Obtain the active panel for this desktop.
     *
     * @return the panel, or the desktop itself if it has no panels
     */
    public TWidget getActivePanel() {
        // Grab the active child, and then drill up to the next TSplitPane.
        TWidget widget = desktop.getActiveChild();
        while ((widget.getParent() != desktop)
            && !(widget.getParent() instanceof TSplitPane)
        ) {
            widget = widget.getParent();
        }
        return widget;
    }

}
