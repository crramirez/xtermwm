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
package xtwm.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TApplication;
import jexer.TComboBox;
import jexer.TDesktop;
import jexer.TField;
import jexer.TPanel;
import jexer.TStatusBar;
import jexer.TWidget;
import jexer.TWindow;
import jexer.bits.BorderStyle;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.bits.WidgetUtils;
import jexer.event.TKeypressEvent;
import jexer.event.TMenuEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;
import jexer.menu.TMenu;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

import xtwm.ui.VirtualDesktop;
import xtwm.ui.XTWMApplication;

/**
 * DesktopPager is a simple desktop pager in the style of classic X11 virtual
 * desktop pagers like OLWM and AfterStep.
 */
public class DesktopPager extends PluginWidget {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(DesktopPager.class.getName());

    /**
     * For the setting window, the button width field.
     */
    private TField buttonWidth = null;

    /**
     * For the setting window, the button height field.
     */
    private TField buttonHeight = null;

    /**
     * For the setting window, the columns field.
     */
    private TField initialColumns = null;

    /**
     * For the setting window, the startup location.
     */
    private TComboBox startupLocation = null;

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The width of a button.
     */
    private int BUTTON_WIDTH = 5;

    /**
     * The height of a button.
     */
    private int BUTTON_HEIGHT = 3;

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * PagerButton is the button that represents a desktop.
     */
    class PagerButton extends TWidget {

        /**
         * The desktop for this button.
         */
        private VirtualDesktop desktop;

        /**
         * The desktop number.
         */
        private int number;

        /**
         * Public constructor.
         *
         * @param parent parent widget
         * @param desktop the desktop
         * @param number the desktop number
         */
        public PagerButton(final TWidget parent, final VirtualDesktop desktop,
            final int number) {

            super(parent, 0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);

            this.desktop = desktop;
            this.number = number;
        }

        /**
         * Handle mouse button presses.
         *
         * @param mouse mouse button event
         */
        @Override
        public void onMouseDown(final TMouseEvent mouse) {
            if (mouse.isMouse1()) {
                if (app.getDesktop() == desktop.getDesktop()) {
                    return;
                }
                while (app.getDesktop() != desktop.getDesktop()) {
                    app.switchToNextDesktop();
                }
                return;
            }
        }

        /**
         * Draw the button.
         */
        @Override
        public void draw() {
            CellAttributes color = new CellAttributes(desktop.getDesktop().
                getAttributes());
            CellAttributes borderColor = new CellAttributes(color);
            CellAttributes windowBorder = getTheme().getColor("twindow.border");
            borderColor.setBackColor(windowBorder.getBackColor());
            borderColor.setBold(true);

            if (app.getDesktop() == desktop.getDesktop()) {
                drawBox(0, 0, getWidth(), getHeight(), borderColor, color,
                    BorderStyle.SINGLE_V_DOUBLE_H, false);
            } else {
                drawBox(0, 0, getWidth(), getHeight(), borderColor, color);
            }

            for (int x = 1; x < getWidth() - 1; x++) {
                for (int y = 1; y < getHeight() - 1; y++) {
                    putCharXY(x, y, GraphicsChars.HATCH, color);
                }
            }
            String str = Integer.toString(number);
            if (app.getDesktop() == desktop.getDesktop()) {
                color.setUnderline(true);
            }
            putStringXY((getWidth() - str.length()) / 2,
                (getHeight() - 1) / 2, str, color);
        }

    }

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     */
    public DesktopPager(final TWidget parent) {
        super(parent);
    }

    /**
     * No-argument constructor that is intended only for use by
     * XTWMApplication.loadPlugin().
     */
    public DesktopPager() {
        super(null);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle window/screen resize events.
     *
     * @param resize resize event
     */
    @Override
    public void onResize(final TResizeEvent resize) {
        super.onResize(resize);

        if (resize.getType() == TResizeEvent.Type.WIDGET) {
            layoutButtons();
        }
    }

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
    @Override
    public String getMenuMnemonic() {
        return i18n.getString("mnemonic");
    }

    /**
     * Get the translated short name for this plugin.
     *
     * @return a short name, e.g. "DesktopPager"
     */
    @Override
    public String getPluginName() {
        return i18n.getString("name");
    }

    /**
     * Get the translated full description for this plugin.
     *
     * @return a short name, e.g. "A desktop pager allowing one to change the
     * active desktop by clicking."
     */
    @Override
    public String getPluginDescription() {
        return i18n.getString("description");
    }

    /**
     * Initialize the plugin.  Since plugins are required to have a
     * no-argument constructor, this method is called to provide a hook for
     * the plugin to perform initialization.  Subclasses that override
     * initialize should call super.initialize() to set the XTWMApplication
     * reference.
     *
     * @param app the application that will be using this plugin
     */
    @Override
    public void initialize(final XTWMApplication app) {
        super.initialize(app);

        int columns = 1;
        try {
            BUTTON_WIDTH = Integer.parseInt(getOption("buttonWidth", "5"));
            BUTTON_HEIGHT = Integer.parseInt(getOption("buttonHeight", "3"));
            columns = Integer.parseInt(getOption("initialColumns", "1"));
        } catch (NumberFormatException e) {
            // SQUASH
        }
        if (BUTTON_WIDTH < 3) {
            BUTTON_WIDTH = 3;
        }
        if (BUTTON_WIDTH > 10) {
            BUTTON_WIDTH = 10;
        }
        if (BUTTON_HEIGHT < 3) {
            BUTTON_HEIGHT = 3;
        }
        if (BUTTON_HEIGHT > 10) {
            BUTTON_HEIGHT = 10;
        }
        if (columns < 1) {
            columns = 1;
        }
        setWidth(BUTTON_WIDTH * columns);

        refreshDesktops();
    }

    /**
     * See if this is an "Application" plugin.
     *
     * @return true if this plugin should be listed the Application |
     * Programs menu
     */
    @Override
    public boolean isApplication() {
        return false;
    }

    /**
     * Get the command line to execute if this is an "Application" plugin.
     *
     * @return a command line
     */
    @Override
    public String getApplicationCommand() {
        return null;
    }

    /**
     * See if this is a "Widget" plugin.
     *
     * @return true if this plugin should be available in the Application |
     * Widgets meny and the Panel | "Switch to" dialog
     */
    @Override
    public boolean isWidget() {
        return true;
    }

    /**
     * Get the translated window title for this plugin.
     *
     * @return the title for the window when this widget is opened in the
     * Application | Widgets menu, or null if this widget should have no
     * title for its window.
     */
    public String getWindowTitle() {
        return null;
    }

    /**
     * Whether or not this plugin should be on all desktops when loaded as a
     * widget at startup.
     *
     * @return true if this plugin should be on all desktops
     */
    @Override
    public boolean isOnAllDesktops() {
        return true;
    }

    /**
     * Get an interface for editing the plugin settings.
     *
     * @param parent parent widget
     * @return a widget that has settings
     */
    @Override
    public TWidget getPluginSettingsEditor(final TWidget parent) {

        parent.addLabel(i18n.getString("buttonWidth"), 3, 3, "ttext", false,
            new TAction() {
                public void DO() {
                    buttonWidth.activate();
                }
            });
        buttonWidth = parent.addField(20, 3, 5, false,
            Integer.toString(BUTTON_WIDTH),
            new TAction() {
                public void DO() {
                    setOption("buttonWidth", buttonWidth.getText());
                }
            },
            new TAction() {
                public void DO() {
                    setOption("buttonWidth", buttonWidth.getText());
                }
            });

        parent.addLabel(i18n.getString("buttonHeight"), 3, 4, "ttext", false,
            new TAction() {
                public void DO() {
                    buttonHeight.activate();
                }
            });
        buttonHeight = parent.addField(20, 4, 5, false,
            Integer.toString(BUTTON_HEIGHT),
            new TAction() {
                public void DO() {
                    setOption("buttonHeight", buttonHeight.getText());
                }
            },
            new TAction() {
                public void DO() {
                    setOption("buttonHeight", buttonHeight.getText());
                }
            });
        parent.addLabel(i18n.getString("buttonHeight"), 3, 4, "ttext", false,
            new TAction() {
                public void DO() {
                    buttonHeight.activate();
                }
            });

        parent.addLabel(i18n.getString("initialColumns"), 3, 5, "ttext", false,
            new TAction() {
                public void DO() {
                    initialColumns.activate();
                }
            });
        initialColumns = parent.addField(20, 5, 5, false,
            getOption("initialColumns", "1"),
            new TAction() {
                public void DO() {
                    setOption("initialColumns", initialColumns.getText());
                }
            },
            new TAction() {
                public void DO() {
                    setOption("initialColumns", initialColumns.getText());
                }
            });

        parent.addLabel(i18n.getString("startupLocation"), 3, 6,
            "tcheckbox.inactive", false,
            new TAction() {
                public void DO() {
                    startupLocation.activate();
                }
            });
        List<String> startupLocationOptions = new ArrayList<String>();
        startupLocationOptions.add("top-right");
        startupLocationOptions.add("right");
        startupLocationOptions.add("bottom-right");
        startupLocationOptions.add("bottom");
        startupLocationOptions.add("bottom-left");
        startupLocationOptions.add("left");
        startupLocationOptions.add("top-left");
        startupLocationOptions.add("top");
        startupLocation = parent.addComboBox(20, 6, 20, startupLocationOptions,
            0, 5,
            new TAction() {
                public void DO() {
                    setOption("startupLocation", startupLocation.getText());
                }
            });
        startupLocation.setText(getOption("startupLocation", "top-right"),
            false);

        return parent;
    }

    /**
     * Get the desired width when rendering this plugin.
     *
     * @return the width
     */
    @Override
    public int getPreferredWidth() {
        return getWidth();
    }

    /**
     * Get the desired height when rendering this plugin.
     *
     * @return the height
     */
    @Override
    public int getPreferredHeight() {
        return getHeight();
    }

    /**
     * Check if widget should be in a resizable window.
     *
     * @return true if the widget should be resizable when in a window
     */
    @Override
    public boolean isResizable() {
        return true;
    }

    /**
     * Get the window that will be used for this plugin when isWindowed() is
     * true.  The plugin will be reparented inside this window.
     *
     * @param application the application
     * @return the window
     */
    @Override
    public TWindow getWindow(final XTWMApplication application) {
        TWindow window = new TWindow(application, getWindowTitle(),
            application.getScreen().getWidth(),
            application.getDesktopBottom() - application.getDesktopTop()) {

            // For DesktopPager, only a few menu items are handled as a
            // normal TWindow.  Everything else should be routed to the
            // desktop if it is set.
            public void onMenu(final TMenuEvent menu) {
                switch (menu.getId()) {
                case TMenu.MID_WINDOW_CLOSE:
                    application.closeWindow(this);
                    return;

                case TMenu.MID_WINDOW_NEXT:
                    // Fall through...
                case TMenu.MID_WINDOW_PREVIOUS:
                    // Fall through...
                case TMenu.MID_WINDOW_MOVE:
                    super.onMenu(menu);
                    return;
                default:
                    break;
                }

                TDesktop desktop = getApplication().getDesktop();
                if (desktop != null) {
                    desktop.onMenu(menu);
                    return;
                }
            }

            public void onResize(final TResizeEvent resize) {
                if (resize.getType() == TResizeEvent.Type.WIDGET) {
                    if (getChildren().size() == 1) {
                        TWidget widget = getChildren().get(0);
                        widget.onResize(new TResizeEvent(resize.getBackend(),
                            TResizeEvent.Type.WIDGET,
                            getWidth() - 2, getHeight() - 2));
                        return;
                    }
                }

                if (resize.getType() == TResizeEvent.Type.SCREEN) {
                    ensureOnScreen();
                    if ((getX() + getWidth() < getScreen().getWidth())
                        || (getY() + getHeight() < getApplication().
                            getDesktopBottom())
                    ) {
                        // The pager is in the middle somewhere, restore it
                        // to its startup location.
                        setStartupLocation(this, DesktopPager.this);
                    }
                    return;
                }

                super.onResize(resize);
            }

            @Override
            public void onKeypress(final TKeypressEvent keypress) {
                if (inWindowMove || inWindowResize || inKeyboardResize) {
                    super.onKeypress(keypress);
                    return;
                }

                TDesktop desktop = getApplication().getDesktop();
                if (desktop != null) {
                    desktop.onKeypress(keypress);
                    return;
                }
            }
        };

        TStatusBar statusBar = window.newStatusBar(i18n.getString("statusBar"));
        statusBar.addShortcutKeypress(kbF1, cmHelp,
            i18n.getString("statusBarHelp"));
        statusBar.addShortcutKeypress(kbShiftF10, cmMenu,
            i18n.getString("statusBarMenu"));

        window.setCloseBox(false);
        window.setZoomBox(false);
        application.putOnAllDesktops(window);
        window.addShortcutKeypress(kbCtrlW);
        setStartupLocation(window, this);

        return window;
    }

    // ------------------------------------------------------------------------
    // DesktopPager -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Set the location of the desktop pager window to match the startupLocation
     * option.
     *
     * @param window the window containing the desktop pager
     * @param pager the desktop pager
     */
    private void setStartupLocation(final TWindow window,
        final DesktopPager pager) {

        TApplication app = window.getApplication();

        int startupX = 0;
        int startupY = app.getDesktopTop();
        String location = pager.getOption("startupLocation", "top-right");
        if (location.equals("top-right")
            || location.equals("right")
            || location.equals("bottom-right")
        ) {
            startupX = app.getScreen().getWidth() - (getWidth() + 2);
        } else if (location.equals("top")
            || location.equals("bottom")
        ) {
            startupX = app.getScreen().getWidth() - (getWidth() + 2);
            startupX /= 2;
        }
        if (location.equals("bottom-left")
            || location.equals("bottom")
            || location.equals("bottom-right")
        ) {
            startupY = app.getDesktopBottom() - (getHeight() + 2);
        } else if (location.equals("left")
            || location.equals("right")
        ) {
            startupY = app.getDesktopBottom() - (getHeight() + 2);
            startupY /= 2;
        }
        window.setX(startupX);
        window.setY(startupY);
    }

    /**
     * Refresh the buttons to match the number of desktops.
     */
    public void refreshDesktops() {
        ArrayList<TWidget> buttons = new ArrayList<TWidget>(getChildren());

        for (TWidget button: buttons) {
            if (button instanceof PagerButton) {
                button.remove();
            }
        }

        int i = 1;
        for (VirtualDesktop desktop: app.getDesktops()) {
            new PagerButton(this, desktop, i);
            i++;
        }

        layoutButtons();
        if (isWindowed()) {
            TWindow window = (TWindow) getParent();
            window.setWidth(window.getMinimumWindowWidth());
            window.setHeight(window.getMinimumWindowHeight());
        }
    }

    /**
     * Layout the desktop buttons to best fit the widget size.
     */
    private void layoutButtons() {
        int x = Math.max(getWidth() / BUTTON_WIDTH, 1);
        int y = Math.max(getHeight() / BUTTON_HEIGHT, 1);
        int n = getChildren().size();

        int columns = Math.min(x, n);
        int rows = Math.max((int) Math.ceil((double) n / columns), 1);
        int minWidth = BUTTON_WIDTH * columns;
        int minHeight = BUTTON_HEIGHT * rows;

        /*
        System.err.println("n " + n + " x " + x + " y " + y);
        System.err.println("   columns " + columns);
        System.err.println("      rows " + rows);
        System.err.println("  minWidth " + minWidth);
        System.err.println(" minHeight " + minHeight);
         */

        if (getWidth() < minWidth) {
            setWidth(minWidth);
        }
        if (getHeight() < minHeight) {
            setHeight(minHeight);
        }

        if (isWindowed()) {
            TWindow window = (TWindow) getParent();
            window.setMinimumWindowHeight(minHeight + 2);
            window.setMinimumWindowWidth(minWidth + 2);
            if ((y > rows) && ((x - 1) * y >= n)) {
                window.setMinimumWindowWidth(minWidth + 2 - BUTTON_WIDTH);
            }
            window.setWidth(Math.max(window.getWidth(),
                    window.getMinimumWindowWidth()));
            window.setHeight(Math.max(window.getHeight(),
                    window.getMinimumWindowHeight()));
        }

        for (int i = 0; i < n; i++) {
            TWidget w = getChildren().get(i);
            int col = i % columns;
            int row = i / columns;
            w.setX(col * BUTTON_WIDTH);
            w.setY(row * BUTTON_HEIGHT);
        }

    }

}
