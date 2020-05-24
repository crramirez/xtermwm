/*
 * Jexer - Java Text User Interface
 *
 * The MIT License (MIT)
 *
 * Copyright (C) 2019 Kevin Lamonte
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
package jexer;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import jexer.backend.ECMA48Terminal;
import jexer.backend.SwingTerminal;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.event.TKeypressEvent;
import static jexer.TKeypress.*;

/**
 * TFontChooserWindow provides an easy UI for users to alter the running
 * font.
 *
 */
public class TFontChooserWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(TFontChooserWindow.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The Swing screen.
     */
    private SwingTerminal terminal = null;

    /**
     * The ECMA48 screen.
     */
    private ECMA48Terminal ecmaTerminal = null;

    /**
     * The font name.
     */
    private TComboBox fontName;

    /**
     * The font size.
     */
    private TField fontSize;

    /**
     * The X text adjustment.
     */
    private TField textAdjustX;

    /**
     * The Y text adjustment.
     */
    private TField textAdjustY;

    /**
     * The height text adjustment.
     */
    private TField textAdjustHeight;

    /**
     * The width text adjustment.
     */
    private TField textAdjustWidth;

    /**
     * The sixel palette size.
     */
    private TComboBox sixelPaletteSize;

    /**
     * The original font size.
     */
    private int oldFontSize = 20;

    /**
     * The original font.
     */
    private Font oldFont = null;

    /**
     * The original text adjust X value.
     */
    private int oldTextAdjustX = 0;

    /**
     * The original text adjust Y value.
     */
    private int oldTextAdjustY = 0;

    /**
     * The original text adjust height value.
     */
    private int oldTextAdjustHeight = 0;

    /**
     * The original text adjust width value.
     */
    private int oldTextAdjustWidth = 0;

    /**
     * The original sixel palette (number of colors) value.
     */
    private int oldSixelPaletteSize = 1024;

    /**
     * The wideCharImages option.
     */
    private TCheckBox wideCharImages;

    /**
     * The original wideCharImages value.
     */
    private boolean oldWideCharImages = true;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.  The window will be centered on screen.
     *
     * @param application the TApplication that manages this window
     */
    public TFontChooserWindow(final TApplication application) {

        // Register with the TApplication
        super(application, i18n.getString("windowTitle"), 0, 0, 60, 21, MODAL);

        // Add shortcut text
        newStatusBar(i18n.getString("statusBar"));

        if (getScreen() instanceof SwingTerminal) {
            terminal = (SwingTerminal) getScreen();
        }
        if (getScreen() instanceof ECMA48Terminal) {
            ecmaTerminal = (ECMA48Terminal) getScreen();
        }

        addLabel(i18n.getString("fontName"), 3, 2, "ttext", false);
        addLabel(i18n.getString("fontSize"), 3, 3, "ttext", false);
        addLabel(i18n.getString("textAdjustX"), 3, 5, "ttext", false);
        addLabel(i18n.getString("textAdjustY"), 3, 6, "ttext", false);
        addLabel(i18n.getString("textAdjustHeight"), 3, 7, "ttext", false);
        addLabel(i18n.getString("textAdjustWidth"), 3, 8, "ttext", false);
        addLabel(i18n.getString("sixelPaletteSize"), 3, 12, "ttext", false);
        wideCharImages = addCheckBox(3, 13, i18n.getString("wideCharImages"),
            (ecmaTerminal != null ? ecmaTerminal.getWideCharImages() :
                System.getProperty("jexer.ECMA48.wideCharImages",
                    "true").equals("true")));
        oldWideCharImages = wideCharImages.isChecked();

        int col = 23;
        if (terminal == null) {
            // Non-Swing case: we can't change font stuff
            addLabel(i18n.getString("unavailable"), col, 2);
            addLabel(i18n.getString("unavailable"), col, 3);
            addLabel(i18n.getString("unavailable"), col, 5);
            addLabel(i18n.getString("unavailable"), col, 6);
            addLabel(i18n.getString("unavailable"), col, 7);
            addLabel(i18n.getString("unavailable"), col, 8);
        }
        if (ecmaTerminal == null) {
            // Swing case: we can't change sixel and wideCharImages
            addLabel(i18n.getString("unavailable"), col, 12);
            wideCharImages.setEnabled(false);
        }
        if (ecmaTerminal != null) {
            oldSixelPaletteSize = ecmaTerminal.getSixelPaletteSize();

            String [] sixelSizes = { "2", "256", "512", "1024", "2048" };
            List<String> sizes = new ArrayList<String>();
            sizes.addAll(Arrays.asList(sixelSizes));
            sixelPaletteSize = addComboBox(col, 12, 10, sizes, 0, 6,
                new TAction() {
                    public void DO() {
                        try {
                            ecmaTerminal.setSixelPaletteSize(Integer.parseInt(
                                sixelPaletteSize.getText()));
                        } catch (NumberFormatException e) {
                            // SQUASH
                        }
                    }
                }
            );
            sixelPaletteSize.setText(Integer.toString(oldSixelPaletteSize));
        }

        if (terminal != null) {
            oldFont = terminal.getFont();
            oldFontSize = terminal.getFontSize();
            oldTextAdjustX = terminal.getTextAdjustX();
            oldTextAdjustY = terminal.getTextAdjustY();
            oldTextAdjustHeight = terminal.getTextAdjustHeight();
            oldTextAdjustWidth = terminal.getTextAdjustWidth();

            String [] fontNames = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            List<String> fonts = new ArrayList<String>();
            fonts.add(0, i18n.getString("builtInTerminus"));
            fonts.addAll(Arrays.asList(fontNames));
            fontName = addComboBox(col, 2, 25, fonts, 0, 8,
                new TAction() {
                    public void DO() {
                        if (fontName.getText().equals(i18n.
                                getString("builtInTerminus"))) {

                            terminal.setDefaultFont();
                        } else {
                            terminal.setFont(new Font(fontName.getText(),
                                    Font.PLAIN, terminal.getFontSize()));
                            fontSize.setText(Integer.toString(
                                terminal.getFontSize()));
                            textAdjustX.setText(Integer.toString(
                                terminal.getTextAdjustX()));
                            textAdjustY.setText(Integer.toString(
                                terminal.getTextAdjustY()));
                            textAdjustHeight.setText(Integer.toString(
                                terminal.getTextAdjustHeight()));
                            textAdjustWidth.setText(Integer.toString(
                                terminal.getTextAdjustWidth()));
                        }
                    }
                }
            );

            // Font size
            fontSize = addField(col, 3, 3, true,
                Integer.toString(terminal.getFontSize()),
                new TAction() {
                    public void DO() {
                        int currentSize = terminal.getFontSize();
                        int newSize = currentSize;
                        try {
                            newSize = Integer.parseInt(fontSize.getText());
                        } catch (NumberFormatException e) {
                            fontSize.setText(Integer.toString(currentSize));
                        }
                        if (newSize != currentSize) {
                            terminal.setFontSize(newSize);
                            textAdjustX.setText(Integer.toString(
                                terminal.getTextAdjustX()));
                            textAdjustY.setText(Integer.toString(
                                terminal.getTextAdjustY()));
                            textAdjustHeight.setText(Integer.toString(
                                terminal.getTextAdjustHeight()));
                            textAdjustWidth.setText(Integer.toString(
                                terminal.getTextAdjustWidth()));
                        }
                    }
                },
                null);

            addSpinner(col + 3, 3,
                new TAction() {
                    public void DO() {
                        int currentSize = terminal.getFontSize();
                        int newSize = currentSize;
                        try {
                            newSize = Integer.parseInt(fontSize.getText());
                            newSize++;
                        } catch (NumberFormatException e) {
                            fontSize.setText(Integer.toString(currentSize));
                        }
                        fontSize.setText(Integer.toString(newSize));
                        if (newSize != currentSize) {
                            terminal.setFontSize(newSize);
                            textAdjustX.setText(Integer.toString(
                                terminal.getTextAdjustX()));
                            textAdjustY.setText(Integer.toString(
                                terminal.getTextAdjustY()));
                            textAdjustHeight.setText(Integer.toString(
                                terminal.getTextAdjustHeight()));
                            textAdjustWidth.setText(Integer.toString(
                                terminal.getTextAdjustWidth()));
                        }
                    }
                },
                new TAction() {
                    public void DO() {
                        int currentSize = terminal.getFontSize();
                        int newSize = currentSize;
                        try {
                            newSize = Integer.parseInt(fontSize.getText());
                            newSize--;
                        } catch (NumberFormatException e) {
                            fontSize.setText(Integer.toString(currentSize));
                        }
                        fontSize.setText(Integer.toString(newSize));
                        if (newSize != currentSize) {
                            terminal.setFontSize(newSize);
                            textAdjustX.setText(Integer.toString(
                                terminal.getTextAdjustX()));
                            textAdjustY.setText(Integer.toString(
                                terminal.getTextAdjustY()));
                            textAdjustHeight.setText(Integer.toString(
                                terminal.getTextAdjustHeight()));
                            textAdjustWidth.setText(Integer.toString(
                                terminal.getTextAdjustWidth()));
                        }
                    }
                }
            );

            // textAdjustX
            textAdjustX = addField(col, 5, 3, true,
                Integer.toString(terminal.getTextAdjustX()),
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustX();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustX.getText());
                        } catch (NumberFormatException e) {
                            textAdjustX.setText(Integer.toString(currentAdjust));
                        }
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustX(newAdjust);
                        }
                    }
                },
                null);

            addSpinner(col + 3, 5,
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustX();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustX.getText());
                            newAdjust++;
                        } catch (NumberFormatException e) {
                            textAdjustX.setText(Integer.toString(currentAdjust));
                        }
                        textAdjustX.setText(Integer.toString(newAdjust));
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustX(newAdjust);
                        }
                    }
                },
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustX();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustX.getText());
                            newAdjust--;
                        } catch (NumberFormatException e) {
                            textAdjustX.setText(Integer.toString(currentAdjust));
                        }
                        textAdjustX.setText(Integer.toString(newAdjust));
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustX(newAdjust);
                        }
                    }
                }
            );

            // textAdjustY
            textAdjustY = addField(col, 6, 3, true,
                Integer.toString(terminal.getTextAdjustY()),
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustY();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustY.getText());
                        } catch (NumberFormatException e) {
                            textAdjustY.setText(Integer.toString(currentAdjust));
                        }
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustY(newAdjust);
                        }
                    }
                },
                null);

            addSpinner(col + 3, 6,
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustY();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustY.getText());
                            newAdjust++;
                        } catch (NumberFormatException e) {
                            textAdjustY.setText(Integer.toString(currentAdjust));
                        }
                        textAdjustY.setText(Integer.toString(newAdjust));
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustY(newAdjust);
                        }
                    }
                },
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustY();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustY.getText());
                            newAdjust--;
                        } catch (NumberFormatException e) {
                            textAdjustY.setText(Integer.toString(currentAdjust));
                        }
                        textAdjustY.setText(Integer.toString(newAdjust));
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustY(newAdjust);
                        }
                    }
                }
            );

            // textAdjustHeight
            textAdjustHeight = addField(col, 7, 3, true,
                Integer.toString(terminal.getTextAdjustHeight()),
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustHeight();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustHeight.getText());
                        } catch (NumberFormatException e) {
                            textAdjustHeight.setText(Integer.toString(currentAdjust));
                        }
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustHeight(newAdjust);
                        }
                    }
                },
                null);

            addSpinner(col + 3, 7,
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustHeight();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustHeight.getText());
                            newAdjust++;
                        } catch (NumberFormatException e) {
                            textAdjustHeight.setText(Integer.toString(currentAdjust));
                        }
                        textAdjustHeight.setText(Integer.toString(newAdjust));
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustHeight(newAdjust);
                        }
                    }
                },
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustHeight();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustHeight.getText());
                            newAdjust--;
                        } catch (NumberFormatException e) {
                            textAdjustHeight.setText(Integer.toString(currentAdjust));
                        }
                        textAdjustHeight.setText(Integer.toString(newAdjust));
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustHeight(newAdjust);
                        }
                    }
                }
            );

            // textAdjustWidth
            textAdjustWidth = addField(col, 8, 3, true,
                Integer.toString(terminal.getTextAdjustWidth()),
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustWidth();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustWidth.getText());
                        } catch (NumberFormatException e) {
                            textAdjustWidth.setText(Integer.toString(currentAdjust));
                        }
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustWidth(newAdjust);
                        }
                    }
                },
                null);

            addSpinner(col + 3, 8,
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustWidth();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustWidth.getText());
                            newAdjust++;
                        } catch (NumberFormatException e) {
                            textAdjustWidth.setText(Integer.toString(currentAdjust));
                        }
                        textAdjustWidth.setText(Integer.toString(newAdjust));
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustWidth(newAdjust);
                        }
                    }
                },
                new TAction() {
                    public void DO() {
                        int currentAdjust = terminal.getTextAdjustWidth();
                        int newAdjust = currentAdjust;
                        try {
                            newAdjust = Integer.parseInt(textAdjustWidth.getText());
                            newAdjust--;
                        } catch (NumberFormatException e) {
                            textAdjustWidth.setText(Integer.toString(currentAdjust));
                        }
                        textAdjustWidth.setText(Integer.toString(newAdjust));
                        if (newAdjust != currentAdjust) {
                            terminal.setTextAdjustWidth(newAdjust);
                        }
                    }
                }
            );

        }

        addButton(i18n.getString("okButton"),
            getWidth() - 13, getHeight() - 10,
            new TAction() {
                public void DO() {
                    // Copy values out.
                    if (ecmaTerminal != null) {
                        ecmaTerminal.setWideCharImages(wideCharImages.
                            isChecked());
                    }

                    // Close window.
                    TFontChooserWindow.this.close();
                }
            });

        TButton cancelButton = addButton(i18n.getString("cancelButton"),
            getWidth() - 13, getHeight() - 8,
            new TAction() {
                public void DO() {
                    // Restore old values, then close the window.
                    if (terminal != null) {
                        synchronized (terminal) {
                            terminal.setFont(oldFont);
                            terminal.setFontSize(oldFontSize);
                            terminal.setTextAdjustX(oldTextAdjustX);
                            terminal.setTextAdjustY(oldTextAdjustY);
                            terminal.setTextAdjustHeight(oldTextAdjustHeight);
                            terminal.setTextAdjustWidth(oldTextAdjustWidth);
                        }
                    }
                    if (ecmaTerminal != null) {
                        ecmaTerminal.setSixelPaletteSize(oldSixelPaletteSize);
                        ecmaTerminal.setWideCharImages(oldWideCharImages);
                    }
                    TFontChooserWindow.this.close();
                }
            });

        // Save this for last: make the cancel button default action.
        activate(cancelButton);

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
            // Restore old values, then close the window.
            if (terminal != null) {
                terminal.setFont(oldFont);
                terminal.setFontSize(oldFontSize);
            }
            if (ecmaTerminal != null) {
                ecmaTerminal.setSixelPaletteSize(oldSixelPaletteSize);
                ecmaTerminal.setWideCharImages(oldWideCharImages);
            }
            getApplication().closeWindow(this);
            return;
        }

        // Pass to my parent
        super.onKeypress(keypress);
    }

    // ------------------------------------------------------------------------
    // TWindow ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw me on screen.
     */
    @Override
    public void draw() {
        super.draw();

        int left = 34;

        CellAttributes color = getTheme().getColor("ttext");
        drawBox(2, 2, left + 24, 11, color, color);
        putStringXY(4, 2, i18n.getString("swingOptions"), color);

        drawBox(2, 12, left + 12, 16, color, color);
        putStringXY(4, 12, i18n.getString("xtermOptions"), color);

        drawBox(left + 2, 5, left + 22, 10, color, color, 3, false);
        putStringXY(left + 4, 5, i18n.getString("sample"), color);
        for (int i = 6; i < 9; i++) {
            hLineXY(left + 3, i, 18, GraphicsChars.HATCH, color);
        }

    }

    // ------------------------------------------------------------------------
    // TFontChooserWindow -----------------------------------------------------
    // ------------------------------------------------------------------------

}
