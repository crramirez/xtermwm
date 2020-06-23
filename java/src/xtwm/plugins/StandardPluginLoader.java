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

import java.util.LinkedList;
import java.util.List;

/**
 * StandardPluginLoader loads the plugins that ship with Xterm Window
 * Manager.
 */
public class StandardPluginLoader implements PluginLoader {

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
     */
    public StandardPluginLoader() {
        // NOP
    }

    // ------------------------------------------------------------------------
    // PluginLoader -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * List the PluginWidget classes that this loader brought in.
     *
     * @return the list of PluginWidget classes
     */
    public List<Class<?>> getPluginClasses() {
        List<Class<?>> plugins = new LinkedList<Class<?>>();

        plugins.add(Calendar.class);
        plugins.add(QuickNotes.class);
        plugins.add(SystemMonitor.class);
        plugins.add(DesktopPager.class);
        plugins.add(BlankScreensaver.class);
        plugins.add(RainScreensaver.class);
        plugins.add(HtopPlugin.class);
        return plugins;
    }

}
