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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jexer.TAction;
import jexer.TApplication;
import jexer.TButton;
import jexer.TCheckBox;
import jexer.TField;
import jexer.TMessageBox;
import jexer.TRadioGroup;
import jexer.TWidget;
import jexer.TWindow;
import jexer.bits.CellAttributes;

/**
 * This window is used by both the "Find" and "Replace" functions.
 */
public class SearchInputWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static ResourceBundle i18n = ResourceBundle.getBundle(SearchInputWindow.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * If true, this is the "Replace Text" window.
     */
    private boolean replace = false;

    /**
     * "Text to find" text.
     */
    private TField textToFind;

    /**
     * "New text" text.
     */
    private TField newText;

    /**
     * Case-sensitive option.
     */
    private TCheckBox caseSensitive;

    /**
     * Whole words only option.
     */
    private TCheckBox wholeWordsOnly;

    /**
     * Regular expression option.
     */
    private TCheckBox regularExpression;

    /**
     * Prompt on replace option.
     */
    private TCheckBox promptOnReplace;

    /**
     * Direction radiobox.
     */
    private TRadioGroup direction;

    /**
     * Scope radiobox.
     */
    private TRadioGroup scope;

    /**
     * Origin radiobox.
     */
    private TRadioGroup origin;

    /**
     * ReplacePrompt is used when "Prompt on replace" is selected.
     */
    private class ReplacePrompt extends TMessageBox {

        /**
         * If set, Change All was pressed.
         */
        public boolean changeAll = false;

        /**
         * Public constructor.
         */
        public ReplacePrompt(final TApplication application, final String title,
            final String caption) {

            super(application, title, caption, TMessageBox.Type.YESNOCANCEL,
                false);

            setWidth(50);

            // Move the YES, NO, and CANCEL buttons, and also add a "Change
            // All" button.

            TButton yesButton = null;
            TButton noButton = null;
            TButton cancelButton = null;
            for (TWidget widget: getChildren()) {
                if (widget instanceof TButton) {
                    if (yesButton == null) {
                        yesButton = (TButton) widget;
                    } else if (noButton == null) {
                        noButton = (TButton) widget;
                    } else if (cancelButton == null) {
                        cancelButton = (TButton) widget;
                    }
                }
            }
            yesButton.setX(3);
            noButton.setX(12);
            cancelButton.setX(getWidth() - 14);
            TButton changeAllButton;
            changeAllButton = addButton(i18n.getString("changeAllButton"),
                20, getHeight() - 4,

                new TAction() {
                    public void DO() {
                        changeAll = true;
                        getApplication().closeWindow(ReplacePrompt.this);
                    }
                });

            int cancelButtonIdx = -1;
            int changeAllButtonIdx = -1;
            List<TWidget> children = getChildren();
            for (int i = 0; i < children.size(); i++) {
                TWidget widget = children.get(i);
                if (widget == cancelButton) {
                    cancelButtonIdx = i;
                }
                if (widget == changeAllButton) {
                    changeAllButtonIdx = i;
                }
            }
            children.set(changeAllButtonIdx, cancelButton);
            children.set(cancelButtonIdx, changeAllButton);
            resetTabOrder();
            activate(cancelButton);

            // Set the secondaryThread to run me
            getApplication().enableSecondaryEventReceiver(this);

            // Yield to the secondary thread.  When I come back from the
            // constructor response will already be set.
            getApplication().yield();
        }

    }

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent the main application
     * @param replace if true, display the "Replace Text" window
     */
    public SearchInputWindow(final TApplication parent, final boolean replace) {

        super(parent, i18n.getString((replace == true ?
                    "replaceWindowTitle" : "findWindowTitle")),
            0, 0, 60, (replace == true ? 21 : 18),
            MODAL | CENTERED);

        this.replace = replace;

        // Create a status bar
        statusBar = newStatusBar(i18n.getString("statusBar"));

        int leftRow = 1;
        int rightRow = 1;
        textToFind = addField(15, leftRow, 41, false);
        addLabel(i18n.getString("textToFind"), 2, leftRow, "tlabel", true,
            new TAction() {
                public void DO() {
                    SearchInputWindow.this.activate(textToFind);
                }
            }
        );

        if (replace == true) {
            leftRow += 2;
            rightRow += 2;
            newText = addField(15, leftRow, 41, false);
            addLabel(i18n.getString("newText"), 2, leftRow, "tlabel", true,
                new TAction() {
                    public void DO() {
                        SearchInputWindow.this.activate(newText);
                    }
                }
            );
        }

        leftRow += 2;
        caseSensitive = addCheckBox(4, leftRow + 1,
            i18n.getString("caseSensitive"), false);
        wholeWordsOnly = addCheckBox(4, leftRow + 2,
            i18n.getString("wholeWordsOnly"), false);
        regularExpression = addCheckBox(4, leftRow + 3,
            i18n.getString("regularExpression"), false);
        if (replace == true) {
            promptOnReplace = addCheckBox(4, leftRow + 4,
                i18n.getString("promptOnReplace"), false);
        }

        leftRow++;
        if (replace == true) {
            leftRow++;
        }
        leftRow += 5;
        scope = addRadioGroup(2, leftRow, 26, i18n.getString("scope"));
        scope.addRadioButton(i18n.getString("global"));
        scope.addRadioButton(i18n.getString("selectedText"));
        scope.setSelected(1);

        rightRow += 2;
        direction = addRadioGroup(30, rightRow, 26,
            i18n.getString("direction"));
        direction.addRadioButton(i18n.getString("forward"));
        direction.addRadioButton(i18n.getString("backward"));
        direction.setSelected(1);
        rightRow++;

        if (replace == true) {
            rightRow++;
        }

        rightRow += 5;
        origin = addRadioGroup(30, rightRow, 26, i18n.getString("origin"));
        origin.addRadioButton(i18n.getString("fromCursor"));
        origin.addRadioButton(i18n.getString("entireScope"));
        origin.setSelected(1);

        leftRow += 5;
        rightRow += 5;

        // Buttons
        addButton(i18n.getString("okButton"),
            (replace == true ? 3 : 16), leftRow,

            new TAction() {
                public void DO() {
                    // Close the window first, then start searching.  Modal
                    // message boxes in the search function will cause the
                    // display to update.
                    SearchInputWindow.this.close();
                    doSearch();
                }
            });

        if (replace == true) {
            addButton(i18n.getString("changeAllButton"),
                15, leftRow,

                new TAction() {
                    public void DO() {
                        // Close the window first, then start searching.
                        // Modal message boxes in the search function will
                        // cause the display to update.
                        SearchInputWindow.this.close();
                        promptOnReplace.setChecked(false);
                        doSearch();
                    }
                });
        }

        TButton cancelButton = addButton(i18n.getString("cancelButton"),
            31, leftRow,

            new TAction() {
                public void DO() {
                    // Don't do anything, just close the window.
                    SearchInputWindow.this.close();
                }
            });

        // Save this for last: make the cancel button default action.
        activate(cancelButton);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

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

        CellAttributes boxColor = getTheme().getColor("ttext");

        // Options
        drawBox(3, (replace == true ? 6 : 4), 29,
            (replace == true ? 12 : 9), boxColor, boxColor);
        putStringXY(5, (replace == true ? 6 : 4),
            i18n.getString("options"), boxColor);
    }

    // ------------------------------------------------------------------------
    // SearchInputWindow ------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Obtain the current editing window.
     *
     * @return the editing window
     */
    private InternalEditorWindow getEditor() {
        XTWMApplication app = ((XTWMApplication) getApplication());
        TWindow window = app.getActiveWindow();
        if (!(window instanceof InternalEditorWindow)) {
            throw new IllegalArgumentException("Program bug: no editor window");
        }
        return (InternalEditorWindow) window;
    }

    /**
     * Match a line.
     *
     * @param line line to match to
     * @param text text to search for
     * @param caseSensitive if true, perform case-sensitive search
     * @param regularExpression if true, perform regular expression search
     * @param wordsOnly if true, perform "words only" search
     * @return column number if there is a match, or 0 if there is no match.
     * 1-based.
     */
    private static int matchLine(final String line, String text,
        final boolean caseSensitive, final boolean regularExpression,
        final boolean wordsOnly) {

        String lineToCheck = line;

        int column = 0;
        if (caseSensitive == false) {
            // Case-insensitive search: force
            lineToCheck = lineToCheck.toLowerCase();
            text = text.toLowerCase();
        }

        if (wordsOnly) {
            StringBuilder sb = new StringBuilder(lineToCheck.length());
            for (int i = 0; i < lineToCheck.length();) {
                int ch = lineToCheck.codePointAt(i);
                if (Character.isAlphabetic(ch)
                    || Character.isDigit(ch)
                ) {
                    sb.append(ch);
                } else {
                    sb.append(' ');
                }
                i += Character.charCount(ch);
            }
            lineToCheck = sb.toString();
        }

        if (regularExpression == true) {
            Pattern pattern = Pattern.compile(text);
            Matcher matcher = pattern.matcher(lineToCheck);
            if (!matcher.find()) {
                return 0;
            }
            column = matcher.start() + 1;
        } else {
            column = lineToCheck.indexOf(text, 0) + 1;
        }
        return column;
    }

    /**
     * Perform the search and replace function.
     */
    private void doSearch() {
        InternalEditorWindow editor = getEditor();
        int replacementCount = 0;

        if (replace == false) {
            // Save search parameters in the application so that find again
            // works.
            XTWMApplication app = ((XTWMApplication) getApplication());
            // Note: do NOT trim the field text here, we want to be able to
            // search for things like "for ".
            app.searchText = textToFind.getText();
            app.searchCaseSensitive = caseSensitive.isChecked();
            app.searchRegularExpression = regularExpression.isChecked();
            app.searchWholeWordsOnly = wholeWordsOnly.isChecked();
            app.searchDirection = direction.getSelected();
            app.searchScope = scope.getSelected();
        }

        int line = 1;
        if (origin.getSelected() == 2) {
            line = editor.getEditingRowNumber();
        }
        int total = editor.getLineCount();
        int row = line;

        do {
            boolean found = searchEditor(editor, textToFind.getText(),
                (direction.getSelected() == 1 ? true : false),
                (origin.getSelected() == 1 ? 1 : editor.getEditingRowNumber()),
                (scope.getSelected() == 2 ? true : false),
                caseSensitive.isChecked(), regularExpression.isChecked(),
                wholeWordsOnly.isChecked());

            if (replace == false) {
                // This is a find only.
                return;
            }

            if (found == false) {
                // No more find/replaces to do.
                break;
            }

            if (promptOnReplace.isChecked()) {
                ReplacePrompt prompt = new ReplacePrompt(getApplication(),
                    i18n.getString("replacePromptTitle"),
                    i18n.getString("replacePromptCaption"));

                if (prompt.changeAll == true) {
                    // User selected change all.  Replace this one, and then
                    // uncheck promptOnReplace.
                    promptOnReplace.setChecked(false);

                    editor.replaceSelection(newText.getText());
                    replacementCount++;
                } else if (prompt.isCancel()) {
                    // Cancel here, done.
                    break;
                } else if (prompt.isNo()) {
                    // Don't replace this one, but keep going.
                    // Nothing to do here.
                } else if (prompt.isYes()) {
                    // Replace this one and keep going.
                    editor.replaceSelection(newText.getText());
                    replacementCount++;
                }
            } else {
                // Replace this one and keep going.
                editor.replaceSelection(newText.getText());
                replacementCount++;
            }
            if (direction.getSelected() == 1) {
                row++;
                if (row > total) {
                    row = 1;
                }
            } else {
                row--;
                if (row == 0) {
                    row = total;
                }
            }
        } while (row != line);

        // All done, report the number of replacements made.
        messageBox(i18n.getString("replacementCountTitle"),
            MessageFormat.format(i18n.getString("replacementCountCaption"),
                replacementCount));

    }

    /**
     * Perform the search function.  Note package private access.
     *
     * @param editor the editor window to search
     * @param text text to search for
     * @param forward if true, search in the forward direction
     * @param line the line number to begin searching from
     * @param selectedTextOnly if true, only search the editor's selected text
     * @param caseSensitive if true, perform case-sensitive search
     * @param regularExpression if true, perform regular expression search
     * @param wholeWordsOnly if true, search on the line's distinct words
     * @return true if the text was found
     */
    static boolean searchEditor(final InternalEditorWindow editor,
        final String text, final boolean forward, final int line,
        final boolean selectedTextOnly, final boolean caseSensitive,
        final boolean regularExpression, final boolean wholeWordsOnly) {

        if ((selectedTextOnly == false) || (editor.hasSelection() == false)) {
            // Search through the entire file, default from cursor.
            int total = editor.getLineCount();
            int row = line;
            do {
                int column = 0;
                column = matchLine(editor.getEditingRawLine(row), text,
                    caseSensitive, regularExpression, wholeWordsOnly);
                if (column > 0) {
                    // Found the next match.
                    editor.setEditingPositionNumber(row, column);
                    editor.setSelection(row - 1, column - 1, row - 1,
                        column + text.length() - 2);
                    return true;
                }
                if (forward == true) {
                    // Go forward
                    row++;
                    if (row > total) {
                        row = 1;
                    }
                } else {
                    // Go backward
                    row--;
                    if (row < 1) {
                        row = total;
                    }
                }
            } while (row != line);

            // Text not found
            return false;
        }

        // Search only through the selected text.
        String selectionText = editor.getSelection();
        assert (selectionText != null);

        String [] lines = selectionText.split("\n");
        int startRow = editor.getSelectionStartRow();
        int startColumn = editor.getSelectionStartColumn();

        int total = lines.length;
        int row = 0;
        do {
            int column = 0;
            column = matchLine(lines[row], text, caseSensitive,
                regularExpression, wholeWordsOnly);
            if (column > 0) {
                // Found the next match.
                editor.setEditingPositionNumber(startRow + row + 1,
                    (row == 0 ? startColumn : 0) + column);
                editor.setSelection(startRow + row,
                    (row == 0 ? startColumn : 0) + column - 1,
                    startRow + row,
                    (row == 0 ? startColumn : 0) + column + text.length() - 2);
                return true;
            }
            if (forward == true) {
                // Go forward
                row++;
                if (row > total) {
                    row = 0;
                }
            } else {
                // Go backward
                row--;
                if (row < 1) {
                    row = total - 1;
                }
            }
        } while (row != 0);

        // Text not found
        return false;
    }

}
