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

import java.util.LinkedList;

import jexer.TApplication;
import jexer.TDesktop;
import jexer.TWidget;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import static jexer.TKeypress.*;

/**
 * Desktop is a desktop background and tiled panels manager.
 */
public class Desktop extends TDesktop {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The color and attributes for this desktop background.
     */
    private CellAttributes attributes = new CellAttributes();

    /**
     * Whether or not the terminal shortcut keys are set.
     */
    private boolean terminalShortcuts = false;

    /**
     * The list of panels on this desktop.
     */
    private LinkedList<TWidget> panels = new LinkedList<TWidget>();

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent application
     */
    public Desktop(final TApplication parent) {
        super(parent);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // TDesktop ---------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The default TDesktop draws a hatch character across everything.
     */
    @Override
    public void draw() {
        putAll(GraphicsChars.HATCH, attributes);
    }

    // ------------------------------------------------------------------------
    // Desktop ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the attributes for the background.
     *
     * @return the background attributes
     */
    public CellAttributes getAttributes() {
        return attributes;
    }

    /**
     * Set the attributes for the background.
     *
     * @param attributes the attributes
     */
    public void setAttributes(final CellAttributes attributes) {
        this.attributes = new CellAttributes(attributes);
    }

    /**
     * Claim the keystrokes a tiled terminal will need.  Note package private
     * access.
     */
    void addTerminalShortcuts() {
        if (terminalShortcuts == false) {
            addShortcutKeypress(kbCtrlA);
            addShortcutKeypress(kbCtrlB);
            addShortcutKeypress(kbCtrlC);
            addShortcutKeypress(kbCtrlD);
            addShortcutKeypress(kbCtrlE);
            addShortcutKeypress(kbCtrlF);
            addShortcutKeypress(kbCtrlG);
            addShortcutKeypress(kbCtrlH);
            addShortcutKeypress(kbCtrlU);
            addShortcutKeypress(kbCtrlJ);
            addShortcutKeypress(kbCtrlK);
            addShortcutKeypress(kbCtrlL);
            addShortcutKeypress(kbCtrlM);
            addShortcutKeypress(kbCtrlN);
            addShortcutKeypress(kbCtrlO);
            addShortcutKeypress(kbCtrlP);
            addShortcutKeypress(kbCtrlQ);
            addShortcutKeypress(kbCtrlR);
            addShortcutKeypress(kbCtrlS);
            addShortcutKeypress(kbCtrlT);
            addShortcutKeypress(kbCtrlU);
            addShortcutKeypress(kbCtrlV);
            addShortcutKeypress(kbCtrlW);
            addShortcutKeypress(kbCtrlX);
            addShortcutKeypress(kbCtrlY);
            addShortcutKeypress(kbCtrlZ);
            addShortcutKeypress(kbF1);
            addShortcutKeypress(kbF2);
            addShortcutKeypress(kbF3);
            addShortcutKeypress(kbF4);
            addShortcutKeypress(kbF5);
            addShortcutKeypress(kbF6);
            addShortcutKeypress(kbF7);
            addShortcutKeypress(kbF8);
            addShortcutKeypress(kbF9);
            addShortcutKeypress(kbF10);
            addShortcutKeypress(kbF11);
            addShortcutKeypress(kbF12);
            addShortcutKeypress(kbAltA);
            addShortcutKeypress(kbAltB);
            addShortcutKeypress(kbAltC);
            addShortcutKeypress(kbAltD);
            addShortcutKeypress(kbAltE);
            addShortcutKeypress(kbAltF);
            addShortcutKeypress(kbAltG);
            addShortcutKeypress(kbAltH);
            addShortcutKeypress(kbAltU);
            addShortcutKeypress(kbAltJ);
            addShortcutKeypress(kbAltK);
            addShortcutKeypress(kbAltL);
            addShortcutKeypress(kbAltM);
            addShortcutKeypress(kbAltN);
            addShortcutKeypress(kbAltO);
            addShortcutKeypress(kbAltP);
            addShortcutKeypress(kbAltQ);
            addShortcutKeypress(kbAltR);
            addShortcutKeypress(kbAltS);
            addShortcutKeypress(kbAltT);
            addShortcutKeypress(kbAltU);
            addShortcutKeypress(kbAltV);
            addShortcutKeypress(kbAltW);
            addShortcutKeypress(kbAltX);
            addShortcutKeypress(kbAltY);
            addShortcutKeypress(kbAltZ);

            terminalShortcuts = true;
        }
    }

    /**
     * Release the keystrokes a tiled terminal has claimed.  Note package
     * private access.
     */
    void clearTerminalShortcuts() {
        clearShortcutKeypresses();
        terminalShortcuts = false;
    }

    /**
     * Activate the next panel on this desktop.
     */
    public void nextPanel() {
        if (panels.size() >= 2) {
            sortPanels();
            panels.addLast(panels.removeFirst());
            panels.getFirst().activateAll();
        }
    }

    /**
     * Activate the next panel on this desktop.
     */
    public void previousPanel() {
        if (panels.size() >= 2) {
            sortPanels();
            panels.addFirst(panels.removeLast());
            panels.getFirst().activateAll();
        }
    }

    /**
     * Add a panel to this desktop.
     *
     * @param widget the panel widget
     */
    public void addPanel(final TWidget widget) {
        panels.add(widget);
    }

    /**
     * Remove a panel from this desktop.
     *
     * @param widget the panel widget
     */
    public void removePanel(final TWidget widget) {
        panels.remove(widget);
    }

    /**
     * Sort the list based on absolute screen coordinates, and arrange so
     * that the active panel is the first in the list.
     */
    private void sortPanels() {
        boolean hasActive = false;
        for (int i = 0; i < panels.size(); i++) {
            for (int j = i; j < panels.size(); j++) {
                TWidget a = panels.get(i);
                int az = (a.getAbsoluteY() * getWidth()) + a.getAbsoluteX();
                if (a.isAbsoluteActive()) {
                    hasActive = true;
                }

                TWidget b = panels.get(j);
                int bz = (b.getAbsoluteY() * getWidth()) + b.getAbsoluteX();
                if (bz < az) {
                    panels.set(j, a);
                    panels.set(i, b);
                }
            }
        }

        if (hasActive) {
            while (!panels.getFirst().isAbsoluteActive()) {
                panels.addLast(panels.removeFirst());
            }
        }

        /*
        for (TWidget panel: panels) {
            System.err.println(panel);
        }

        System.err.println("\n\n\n");
        System.err.println(toPrettyString());
        System.err.println("\n");
         */
    }

}
