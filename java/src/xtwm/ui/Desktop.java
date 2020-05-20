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

import jexer.TApplication;
import jexer.TDesktop;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import static jexer.TKeypress.*;

/**
 * Desktop is the main document editor window.
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

}
