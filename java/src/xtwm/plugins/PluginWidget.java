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
package xtwm.plugins;

import jexer.TWidget;
import jexer.TPanel;
import jexer.bits.MnemonicString;

/**
 * PluginWidget is a plugin.  Its interface can be instantiated in a separate
 * window or as part of a tiled panel.
 */
public abstract class PluginWidget extends TWidget {

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
     * Public constructor.
     *
     * @param parent parent widget
     */
    protected PluginWidget(final TWidget parent) {
        super(parent);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // PluginWidget -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the translated menu label for this plugin.
     *
     * @return a mnemonic string that will be populated in the menu
     */
    public abstract MnemonicString getMenuMnemonic();

    /**
     * Get the translated short name for this plugin.
     *
     * @return a short name, e.g. "Calendar"
     */
    public abstract String getPluginName();

    /**
     * Get the translated full description for this plugin.
     *
     * @return a short name, e.g. "A simple calendar with TODO manager."
     */
    public abstract String getPluginDescription();

    /**
     * Get an interface for editing the plugin settings.
     *
     * @param parent parent widget
     * @return a widget that has settings
     */
    public TWidget getPluginSettingsEditor(final TWidget parent) {
        // Default implementation shows a blank panel.
        return new TPanel(parent, 0, 0, getWidth(), getHeight());
     }

}
