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

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TDesktop;
import jexer.TList;
import jexer.TSplitPane;
import jexer.TWidget;
import jexer.TWindow;
import jexer.bits.MnemonicString;
import jexer.event.TKeypressEvent;
import static jexer.TKeypress.*;

import xtwm.plugins.PluginWidget;
import xtwm.plugins.ScreensaverPlugin;
import xtwm.ui.TiledTerminal;
import xtwm.ui.XTWMApplication;

/**
 * SwitchWidgetWindow is the UI to change a panel from one kind of widget or
 * TiledTerminal to something else.
 */
public class SwitchWidgetWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(SwitchWidgetWindow.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The list of widgets pane.
     */
    private TList widgets;

    /**
     * The widget classes by list ID.
     */
    private HashMap<Integer, PluginWidget> widgetsById;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.  The window will be centered on screen.
     *
     * @param application the TApplication that manages this window
     */
    public SwitchWidgetWindow(final XTWMApplication application) {

        // Register with the TApplication
        super(application, i18n.getString("windowTitle"), 0, 0, 60, 18, MODAL);


        List<String> widgetNames = new ArrayList<String>();
        widgetNames.add("Terminal");

        widgetsById = new HashMap<Integer, PluginWidget>();

        for (PluginWidget widget: application.getWidgets()) {
            if (!widget.isWidget()) {
                continue;
            }
            String name = new MnemonicString(widget.
                getMenuMnemonic()).getRawLabel();

            if (widget instanceof ScreensaverPlugin) {
                widgetNames.add(String.format("%s %s", name,
                        i18n.getString("screensaver")));
            } else {
                widgetNames.add(name);
            }
            widgetsById.put(widgetNames.size() - 1, widget);
        }

        widgets = addList(widgetNames, 1, 1, getWidth() - 4, getHeight() - 6,
            new TAction() {
                // When the user presses Enter
                public void DO() {
                    int index = widgets.getSelectedIndex();
                    if (index == 0) {
                        // Switch to TiledTerminal
                        switchToTerminal();
                    } else {
                        PluginWidget widget = widgetsById.get(index);
                        if (widget != null) {
                            // Switch to PluginWidget
                            switchToPluginWidget(widget.getClass());
                        }
                    }
                    getApplication().closeWindow(SwitchWidgetWindow.this);
                }
            });
        widgets.setSelectedIndex(0);

        addButton(i18n.getString("okButton"), getWidth() - 41, getHeight() - 4,
            new TAction() {
                public void DO() {
                    int index = widgets.getSelectedIndex();
                    if (index == 0) {
                        // Switch to TiledTerminal
                        switchToTerminal();
                    } else {
                        PluginWidget widget = widgetsById.get(index);
                        if (widget != null) {
                            // Switch to PluginWidget
                            switchToPluginWidget(widget.getClass());
                        }
                    }
                    getApplication().closeWindow(SwitchWidgetWindow.this);
                }
            }
        );

        addButton(i18n.getString("cancelButton"), getWidth() - 29,
            getHeight() - 4,
            new TAction() {
                public void DO() {
                    getApplication().closeWindow(SwitchWidgetWindow.this);
                }
            }
        );

        // Default to the widget list
        activate(widgets);

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

    // ------------------------------------------------------------------------
    // TWindow ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // SwitchWidgetWindow -----------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Switch the current desktop's active child widget to a TiledTerminal.
     */
    private void switchToTerminal() {
        XTWMApplication app = (XTWMApplication) getApplication();

        TDesktop desktop = app.getCurrentDesktop().getDesktop();
        TWidget widget = desktop.getActiveChild();
        assert (widget != null);
        while ((widget.getParent() != desktop)
            && !(widget.getParent() instanceof TSplitPane)
        ) {
            widget = widget.getParent();
        }

        if (widget instanceof TiledTerminal) {
            return;
        }
        TWidget parent = widget.getParent();
        if (parent instanceof TSplitPane) {
            ((TSplitPane) parent).replaceWidget(widget,
                new TiledTerminal(desktop));
        } else {
            assert (parent instanceof TDesktop);
            widget.remove();
            new TiledTerminal(parent);
        }

        // System.err.println(desktop.toPrettyString());
    }

    /**
     * Switch the current desktop's active child widget to a PluginWidget.
     *
     * @param pluginClass the widget class to switch to
     */
    private void switchToPluginWidget(final Class<? extends PluginWidget> pluginClass) {
        XTWMApplication app = (XTWMApplication) getApplication();

        TDesktop desktop = app.getCurrentDesktop().getDesktop();
        TWidget widget = desktop.getActiveChild();
        assert (widget != null);
        while ((widget.getParent() != desktop)
            && !(widget.getParent() instanceof TSplitPane)
        ) {
            widget = widget.getParent();
        }

        PluginWidget newWidget = app.makePluginWidget(pluginClass);
        TWidget parent = widget.getParent();
        if (parent instanceof TSplitPane) {
            TSplitPane split = (TSplitPane) parent;
            split.replaceWidget(widget, newWidget);
            split.getChildren().remove(widget);
            widget.close();
        } else {
            assert (parent instanceof TDesktop);
            widget.remove();
            newWidget.setParent(desktop, false);
            newWidget.setDimensions(0, 0, desktop.getWidth(),
                app.getDesktopBottom() - app.getDesktopTop());
        }

        // System.err.println(desktop.toPrettyString());
    }

}
