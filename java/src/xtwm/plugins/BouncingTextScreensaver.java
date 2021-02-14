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
package xtwm.plugins;

import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TField;
import jexer.TTimer;
import jexer.TWidget;
import jexer.backend.Screen;
import jexer.bits.Cell;
import jexer.bits.CellAttributes;
import jexer.bits.StringUtils;

/**
 * BouncingTextScreensaver displays a logo that bounces around.
 */
public class BouncingTextScreensaver extends ScreensaverPlugin {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(BouncingTextScreensaver.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The animation timer.
     */
    private TTimer timer = null;

    /**
     * The text to display.
     */
    private String text = "";

    /**
     * The text X position in pixels.
     */
    private int textX = 0;

    /**
     * The text Y position in pixels.
     */
    private int textY = 0;

    /**
     * The bounce vector.
     */
    private int bounceDegrees = (int) (Math.random() * 360.0);

    /**
     * For the setting window, the text to display field.
     */
    private TField textToDisplay = null;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     */
    public BouncingTextScreensaver(final TWidget parent) {
        super(parent);
    }

    /**
     * No-argument constructor that is intended only for use by
     * XTWMApplication.loadPlugin().
     */
    public BouncingTextScreensaver() {
        super(null);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
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
     * @return a short name, e.g. "BouncingTextScreensaver"
     */
    @Override
    public String getPluginName() {
        return i18n.getString("name");
    }

    /**
     * Get the translated full description for this plugin.
     *
     * @return a short name, e.g. "Screen falls into a black hole."
     */
    @Override
    public String getPluginDescription() {
        return i18n.getString("description");
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
        return false;
    }

    /**
     * Get the translated window title for this plugin.
     *
     * @return the title for the window when this widget is opened in the
     * Application | Widgets menu, or null if this widget should have no
     * title for its window.
     */
    public String getWindowTitle() {
        return i18n.getString("windowTitle");
    }

    /**
     * Get an interface for editing the plugin settings.
     *
     * @param parent parent widget
     * @return a widget that has settings
     */
    @Override
    public TWidget getPluginSettingsEditor(final TWidget parent) {

        parent.addLabel(i18n.getString("textToDisplay"), 3, 3, "ttext", false,
            new TAction() {
                public void DO() {
                    textToDisplay.activate();
                }
            });

        textToDisplay = parent.addField(22, 3, 33, false,
            getOption("textToDisplay", i18n.getString("defaultText")),
            new TAction() {
                public void DO() {
                    setOption("textToDisplay", textToDisplay.getText());
                }
            },
            new TAction() {
                public void DO() {
                    setOption("textToDisplay", textToDisplay.getText());
                }
            });

        return parent;
    }

    // ------------------------------------------------------------------------
    // ScreensaverPlugin ------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw a blank screen with the text in color.
     */
    @Override
    public void draw() {
        CellAttributes color = new CellAttributes();
        putAll(' ', color);

        int x = textX / getScreen().getTextWidth();
        int y = textY / getScreen().getTextHeight();

        // Convert bounceDegrees as HSL hue to RGB.
        double theta = bounceDegrees;
        while (theta < 0) {
            theta = theta + 360.0;
        }
        while (theta > 360.0) {
            theta = theta - 360.0;
        }
        double S = 100.0;
        double L = 50.0;
        double C = (1.0 - Math.abs((2.0 * L) - 1.0)) * S;
        double Hp = theta / 60.0;
        double X = C * (1.0 - Math.abs((Hp % 2) - 1.0));
        double Rp = 0.0;
        double Gp = 0.0;
        double Bp = 0.0;
        if (Hp <= 1.0) {
            Rp = C;
            Gp = X;
        } else if (Hp <= 2.0) {
            Rp = X;
            Gp = C;
        } else if (Hp <= 3.0) {
            Gp = C;
            Bp = X;
        } else if (Hp <= 4.0) {
            Gp = X;
            Bp = C;
        } else if (Hp <= 5.0) {
            Rp = X;
            Bp = C;
        } else if (Hp <= 6.0) {
            Rp = C;
            Bp = X;
        }
        double m = L - (C / 2.0);
        int red   = ((int) ((Rp + m) * 255.0)) << 16;
        int green = ((int) ((Gp + m) * 255.0)) << 8;
        int blue  =  (int) ((Bp + m) * 255.0);
        color.setForeColorRGB((red | green | blue) & 0xFFFFFF);
        color.setBackColorRGB(0);
        assert (color.isRGB());
        putStringXY(x, y, text, color);
    }

    /**
     * This method is called when the screensaver is activated.
     *
     * @param screen a snapshot of the screen "under" the screensaver.
     */
    @Override
    public void startScreensaver(final Screen screen) {
        text = getOption("textToDisplay", i18n.getString("defaultText"));

        timer = app.addTimer((int) Math.ceil(1000.0 / 18.2), true,
            new TAction() {
                public void DO() {
                    BouncingTextScreensaver.this.doBouncingText();
                }
            });
    }

    /**
     * This method is called when the screensaver ends.
     */
    @Override
    public void endScreensaver() {
        if (timer != null) {
            app.removeTimer(timer);
        }
    }

    // ------------------------------------------------------------------------
    // BouncingTextScreensaver ------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Move the text one cell along the bounce vector.
     */
    private void doBouncingText() {
        double theta = 2.0 * Math.PI * bounceDegrees / 360.0;
        double dx = getScreen().getTextWidth() * Math.cos(theta);
        double dy = getScreen().getTextWidth() * Math.sin(theta);
        int textWidth = getScreen().getTextWidth();
        int textHeight = getScreen().getTextHeight();
        int width = StringUtils.width(text) * textWidth;

        double newTheta = theta;
        if (textX + dx + width > (getScreen().getWidth() + 1) * textWidth) {
            // Hit the right edge.
            dx = -Math.abs(dx);
        }
        if (textX + dx < 0) {
            // Hit the left edge.
            dx = Math.abs(dx);
        }
        if (textY + dy > getScreen().getHeight() * textHeight) {
            // Hit the bottom edge.
            dy = -Math.abs(dy);
        }
        if (textY + dy < 0) {
            // Hit the top edge.
            dy = Math.abs(dy);
        }
        newTheta = Math.atan2(dy, dx);
        bounceDegrees = (int) (newTheta * 360.0 / (2.0 * Math.PI));

        textX += dx;
        textY += dy;
    }

}
