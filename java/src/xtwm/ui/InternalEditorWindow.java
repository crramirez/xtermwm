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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

import jexer.TApplication;
import jexer.TEditorWidget;
import jexer.THScroller;
import jexer.TMessageBox;
import jexer.TScrollableWindow;
import jexer.TVScroller;
import jexer.TWidget;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.event.TCommandEvent;
import jexer.event.TKeypressEvent;
import jexer.event.TMenuEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;
import jexer.menu.TMenu;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

import xtwm.ui.XTWMApplication;

/**
 * InternalEditorWindow is a basic text file editor.
 */
public class InternalEditorWindow extends TScrollableWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(InternalEditorWindow.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Hang onto my editor so I can resize it with the window.
     */
    protected InternalEditorWidget editField;

    /**
     * If true, hide the mouse after typing a keystroke.
     */
    private boolean hideMouseWhenTyping = true;

    /**
     * If true, the mouse should not be displayed because a keystroke was
     * typed.
     */
    private boolean typingHidMouse = false;

    /**
     * The file being edited.
     */
    private File file = null;

    /**
     * If true, we have opened a buffer on a file that does not exist.  If it
     * exists when we try to save it, ask for confirmation.
     */
    private boolean isNewFile = true;

    /**
     * InternalEditorWidget is a TEditorWidget that highlights the breakpoint
     * and execution lines.
     */
    public class InternalEditorWidget extends TEditorWidget {

        /**
         * Public constructor.
         *
         * @param parent parent widget
         * @param text text on the screen
         * @param x column relative to parent
         * @param y row relative to parent
         * @param width width of text area
         * @param height height of text area
         */
        public InternalEditorWidget(final TWidget parent, final String text,
            final int x, final int y, final int width, final int height) {

            super(parent, text, x, y, width, height);

            XTWMApplication app = (XTWMApplication) getApplication();
            try {
                setUndoLevel(Integer.parseInt(app.getOption(
                        "editor.internal.undoLevel", "50")));
            } catch (NumberFormatException e) {
                // SQUASH
            }

            try {
                document.setTabSize(Integer.parseInt(app.getOption(
                        "editor.internal.indentLevel", "4")));
            } catch (NumberFormatException e) {
                // SQUASH
            }

            setHighlighting(app.getOption(
                "editor.internal.highlightKeywords", "true").equals("true") ?
                true : false);

            document.setBackspaceUnindents(app.getOption(
                "editor.internal.backspaceUnindents", "true").equals("true") ?
                true : false);

            document.setSaveWithTabs(app.getOption(
                "editor.internal.saveWithTabs", "false").equals("true") ?
                true : false);

        }

    }

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent the main application
     */
    public InternalEditorWindow(final TApplication parent) {
        this(parent, i18n.getString("newTextDocument"));
    }

    /**
     * Public constructor with a custom title.
     *
     * @param parent the main application
     * @param title the window title
     */
    public InternalEditorWindow(final TApplication parent, final String title) {

        super(parent, title, 0, 0, parent.getScreen().getWidth(),
            parent.getDesktopBottom() - parent.getDesktopTop(), RESIZABLE);

        editField = new InternalEditorWidget(this, "", 0, 0,
            getWidth() - 2, getHeight() - 2);
        setupAfterEditor();
    }

    /**
     * Public constructor opens a file.
     *
     * @param parent the main application
     * @param file the file to open
     * @throws IOException if a java.io operation throws
     */
    public InternalEditorWindow(final TApplication parent,
        final File file) throws IOException {

        this(parent, file, 0, 0, parent.getScreen().getWidth(),
            parent.getScreen().getHeight() - 2);
    }

    /**
     * Public constructor opens a file.
     *
     * @param parent the main application
     * @param file the file to open
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of window
     * @param height height of window
     * @throws IOException if a java.io operation throws
     */
    public InternalEditorWindow(final TApplication parent, final File file,
        final int x, final int y, final int width,
        final int height) throws IOException {

        super(parent, file.getName(), x, y, width, height, RESIZABLE);

        this.file = file;
        assert (file != null);

        String contents = "";
        if ((file.exists()) && (file.isFile())) {
            contents = readFileData(file);
            isNewFile = false;
        } else {
            setTitle(MessageFormat.format(i18n.getString("newFileTitle"),
                    file.getName()));
        }
        editField = new InternalEditorWidget(this, contents, 0, 0,
            getWidth() - 2, getHeight() - 2);
        setupAfterEditor();

    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Called by application.switchWindow() when this window gets the
     * focus, and also by application.addWindow().
     */
    public void onFocus() {
        // Enable the edit and search menu items, since we are using an
        // internal editor.
        getApplication().enableMenuItem(TMenu.MID_UNDO);
        getApplication().enableMenuItem(TMenu.MID_REDO);
        getApplication().enableMenuItem(TMenu.MID_FIND);
        getApplication().enableMenuItem(TMenu.MID_REPLACE);
        getApplication().enableMenuItem(TMenu.MID_SEARCH_AGAIN);
        getApplication().enableMenuItem(TMenu.MID_GOTO_LINE);

        // Enable the File | Save and Save as
        getApplication().enableMenuItem(XTWMApplication.MENU_EDIT_SAVE);
        getApplication().enableMenuItem(XTWMApplication.MENU_EDIT_SAVE_AS);
    }

    /**
     * Called by application.switchWindow() when another window gets the
     * focus.
     */
    public void onUnfocus() {
        // Disable the edit and search menu items.
        getApplication().disableMenuItem(TMenu.MID_UNDO);
        getApplication().disableMenuItem(TMenu.MID_REDO);
        getApplication().disableMenuItem(TMenu.MID_FIND);
        getApplication().disableMenuItem(TMenu.MID_REPLACE);
        getApplication().disableMenuItem(TMenu.MID_SEARCH_AGAIN);
        getApplication().disableMenuItem(TMenu.MID_GOTO_LINE);

        // Disable the File | Save and Save as
        getApplication().disableMenuItem(XTWMApplication.MENU_EDIT_SAVE);
        getApplication().disableMenuItem(XTWMApplication.MENU_EDIT_SAVE_AS);
    }

    /**
     * Prompt the user to save if we are about to be closed and the editor is
     * dirty.
     */
    @Override
    public void onPreClose() {
        super.onPreClose();
        askToSave();
    }

    /**
     * Handle mouse press events.
     *
     * @param mouse mouse button press event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        // Use TWidget's code to pass the event to the children.
        super.onMouseDown(mouse);

        if (hideMouseWhenTyping) {
            typingHidMouse = false;
        }

        if (mouseOnEditor(mouse)) {
            // The editor might have changed, update the scollbars.
            setBottomValue(editField.getMaximumRowNumber());
            setVerticalValue(editField.getVisibleRowNumber());
            setRightValue(editField.getMaximumColumnNumber());
            setHorizontalValue(editField.getEditingColumnNumber());
        } else {
            if (mouse.isMouseWheelUp() || mouse.isMouseWheelDown()) {
                // Vertical scrollbar actions
                editField.setVisibleRowNumber(getVerticalValue());
            }
        }
    }

    /**
     * Handle mouse release events.
     *
     * @param mouse mouse button release event
     */
    @Override
    public void onMouseUp(final TMouseEvent mouse) {
        // Use TWidget's code to pass the event to the children.
        super.onMouseUp(mouse);

        if (hideMouseWhenTyping) {
            typingHidMouse = false;
        }

        if (mouse.isMouse1() && mouseOnVerticalScroller(mouse)) {
            // Clicked on vertical scrollbar
            editField.setVisibleRowNumber(getVerticalValue());
        }
        if (mouse.isMouse1() && mouseOnHorizontalScroller(mouse)) {
            // Clicked on horizontal scrollbar
            editField.setVisibleColumnNumber(getHorizontalValue());
            setHorizontalValue(editField.getVisibleColumnNumber());
        }
    }

    /**
     * Method that subclasses can override to handle mouse movements.
     *
     * @param mouse mouse motion event
     */
    @Override
    public void onMouseMotion(final TMouseEvent mouse) {
        // Use TWidget's code to pass the event to the children.
        super.onMouseMotion(mouse);

        if (hideMouseWhenTyping) {
            typingHidMouse = false;
        }

        if (mouseOnEditor(mouse) && mouse.isMouse1()) {
            // The editor might have changed, update the scollbars.
            setBottomValue(editField.getMaximumRowNumber());
            setVerticalValue(editField.getVisibleRowNumber());
            setRightValue(editField.getMaximumColumnNumber());
            setHorizontalValue(editField.getEditingColumnNumber());
        } else {
            if (mouse.isMouse1() && mouseOnVerticalScroller(mouse)) {
                // Clicked/dragged on vertical scrollbar
                editField.setVisibleRowNumber(getVerticalValue());
            }
            if (mouse.isMouse1() && mouseOnHorizontalScroller(mouse)) {
                // Clicked/dragged on horizontal scrollbar
                editField.setVisibleColumnNumber(getHorizontalValue());
                setHorizontalValue(editField.getVisibleColumnNumber());
            }
        }

    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        if (hideMouseWhenTyping) {
            typingHidMouse = true;
        }

        // Use TWidget's code to pass the event to the children.
        super.onKeypress(keypress);

        // The editor might have changed, update the scollbars.
        setBottomValue(editField.getMaximumRowNumber());
        setVerticalValue(editField.getVisibleRowNumber());
        setRightValue(editField.getMaximumColumnNumber());
        setHorizontalValue(editField.getEditingColumnNumber());
    }

    /**
     * Handle window/screen resize events.
     *
     * @param event resize event
     */
    @Override
    public void onResize(final TResizeEvent event) {
        if (event.getType() == TResizeEvent.Type.WIDGET) {
            // Resize the text field
            TResizeEvent editSize = new TResizeEvent(TResizeEvent.Type.WIDGET,
                event.getWidth() - 2, event.getHeight() - 2);
            editField.onResize(editSize);

            // Have TScrollableWindow handle the scrollbars
            super.onResize(event);
            return;
        }

        // Pass to children instead
        for (TWidget widget: getChildren()) {
            widget.onResize(event);
        }
    }

    /**
     * Method that subclasses can override to handle posted command events.
     *
     * @param command command event
     */
    @Override
    public void onCommand(final TCommandEvent command) {
        if (command.equals(cmOpen)) {
            try {
                String filename = fileOpenBox(".");
                if (filename != null) {
                    ((XTWMApplication) getApplication()).openEditor(
                        filename);
                }
            } catch (IOException e) {
                messageBox(i18n.getString("errorDialogTitle"),
                    MessageFormat.format(i18n.
                        getString("errorOpeningFileDialog"), e.getMessage()));
            }
            return;
        }

        if (command.equals(cmSave)) {
            saveFile();
            return;
        }

        // Didn't handle it, let children get it instead
        super.onCommand(command);
    }

    /**
     * Handle posted menu events.
     *
     * @param menu menu event
     */
    @Override
    public void onMenu(final TMenuEvent menu) {

        switch (menu.getId()) {

        case TMenu.MID_UNDO:
            editField.undo();
            return;

        case TMenu.MID_REDO:
            editField.redo();
            return;

        case XTWMApplication.MENU_EDIT_SAVE:
            saveFile();
            return;

        case XTWMApplication.MENU_EDIT_SAVE_AS:
            try {
                String filename = fileSaveBox(".");
                if (filename != null) {
                    editField.saveToFilename(filename);
                    file = new File(filename);
                    setTitle(file.getName());
                }
            } catch (IOException e) {
                messageBox(i18n.getString("errorDialogTitle"),
                    MessageFormat.format(i18n.
                        getString("errorSavingFile"), e.getMessage()));
            }
            return;

        default:
            break;
        }

        super.onMenu(menu);
    }

    // ------------------------------------------------------------------------
    // TWindow ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the window.
     */
    @Override
    public void draw() {
        // Draw as normal.
        super.draw();

        // Add the row:col on the bottom row
        CellAttributes borderColor = getBorder();
        String location = String.format(" %d:%d ",
            editField.getEditingRowNumber(),
            editField.getEditingColumnNumber());
        int colon = location.indexOf(':');
        putStringXY(10 - colon, getHeight() - 1, location, borderColor);

        // Add dirty indicator
        if (editField.isDirty()) {
            putCharXY(2, getHeight() - 1, GraphicsChars.OCTOSTAR, borderColor);
        }

        // Add overwrite indicator
        if (editField.isOverwrite()) {
            putCharXY(4, getHeight() - 1, GraphicsChars.HATCH, borderColor);
        }

    }

    /**
     * Check if a mouse press/release/motion event coordinate is over the
     * editor.
     *
     * @param mouse a mouse-based event
     * @return whether or not the mouse is on the editor
     */
    private final boolean mouseOnEditor(final TMouseEvent mouse) {
        if ((mouse.getAbsoluteX() >= getAbsoluteX() + 1)
            && (mouse.getAbsoluteX() <  getAbsoluteX() + getWidth() - 1)
            && (mouse.getAbsoluteY() >= getAbsoluteY() + 1)
            && (mouse.getAbsoluteY() <  getAbsoluteY() + getHeight() - 1)
        ) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if this window does not want the application-wide mouse
     * cursor drawn over it.
     *
     * @return true if this window does not want the application-wide mouse
     * cursor drawn over it
     */
    @Override
    public boolean hasHiddenMouse() {
        return (super.hasHiddenMouse() || typingHidMouse);
    }

    // ------------------------------------------------------------------------
    // InternalEditorWindow ---------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Setup other fields after the editor is created.
     */
    protected void setupAfterEditor() {
        hScroller = new THScroller(this, 17, getHeight() - 2, getWidth() - 20);
        vScroller = new TVScroller(this, getWidth() - 2, 0, getHeight() - 2);
        setMinimumWindowWidth(25);
        setMinimumWindowHeight(10);
        setTopValue(1);
        setBottomValue(editField.getMaximumRowNumber());
        setLeftValue(1);
        setRightValue(editField.getMaximumColumnNumber());

        statusBar = newStatusBar(i18n.getString("statusBar"));
        statusBar.addShortcutKeypress(kbF1, cmHelp,
            i18n.getString("statusBarHelp"));
        statusBar.addShortcutKeypress(kbF2, cmSave,
            i18n.getString("statusBarSave"));
        statusBar.addShortcutKeypress(kbF3, cmOpen,
            i18n.getString("statusBarOpen"));
        statusBar.addShortcutKeypress(kbF10, cmMenu,
            i18n.getString("statusBarMenu"));

        // Hide mouse when typing option
        if (System.getProperty("jexer.TEditor.hideMouseWhenTyping",
                "true").equals("false")) {

            hideMouseWhenTyping = false;
        }
    }

    /**
     * Ask to save the file if it is dirty.  If the user accepts, save the
     * file.
     */
    public void askToSave() {
        if (!editField.isDirty()) {
            return;
        }

        if (file != null) {
            if (messageBox(i18n.getString("saveFileTitle"),
                    MessageFormat.format(i18n.getString("saveFilePrompt"),
                        file.getName()), TMessageBox.Type.YESNO).isYes()) {

                saveIfDirty();
            }
        }
    }

    /**
     * Save the file if it is dirty.
     */
    public void saveIfDirty() {
        if (editField.isDirty()) {
            if (file != null) {
                try {
                    editField.saveToFilename(file.getPath());
                } catch (IOException e) {
                    messageBox(i18n.getString("errorDialogTitle"),
                        MessageFormat.format(i18n.
                            getString("errorSavingFile"), e.getMessage()));
                }
            }
        }
    }

    /**
     * Read file data into a string.
     *
     * @param file the file to open
     * @return the file contents
     * @throws IOException if a java.io operation throws
     */
    private String readFileData(final File file) throws IOException {
        StringBuilder fileContents = new StringBuilder();
        Scanner scanner = new Scanner(file);
        String EOL = System.getProperty("line.separator");

        try {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + EOL);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }

    /**
     * Read file data into a string.
     *
     * @param filename the file to open
     * @return the file contents
     * @throws IOException if a java.io operation throws
     */
    private String readFileData(final String filename) throws IOException {
        return readFileData(new File(filename));
    }

    /**
     * Set the current editing column number.  1-based.
     *
     * @param column the new editing column number.  Column 1 is the leftmost
     * column.
     */
    public void setEditingColumnNumber(final int column) {
        editField.setEditingColumnNumber(column);
    }

    /**
     * Get the current editing row number.  1-based.
     *
     * @return the editing row number.  Row 1 is the first row.
     */
    public int getEditingRowNumber() {
        return editField.getEditingRowNumber();
    }

    /**
     * Set the current editing row number.  1-based.
     *
     * @param row the new editing row number.  Row 1 is the first row.
     */
    public void setEditingRowNumber(final int row) {
        editField.setEditingRowNumber(row);
    }

    /**
     * Get the number of lines in the underlying Document.
     *
     * @return the number of lines
     */
    public int getLineCount() {
        return editField.getLineCount();
    }

    /**
     * Get the current editing row plain text.  1-based.
     *
     * @param row the new editing row number.  Row 1 is the first row.
     * @return the plain text of the row
     */
    public String getEditingRawLine(final int row) {
        return editField.getEditingRawLine(row);
    }

    /**
     * Set the current editing row and column number.  1-based.
     *
     * @param row the new editing row number.  Row 1 is the first row.
     * @param column the new editing column number.  Column 1 is the leftmost
     * column.
     */
    public void setEditingPositionNumber(final int row, final int column) {
        editField.setEditingRowNumber(row);
        if (editField.getEditingRowNumber() == row) {
            editField.setEditingColumnNumber(column);
        }
    }

    /**
     * Set the selection.
     *
     * @param startRow the starting row number.  0-based: row 0 is the first
     * row.
     * @param startColumn the starting column number.  0-based: column 0 is
     * the first column.
     * @param endRow the ending row number.  0-based: row 0 is the first row.
     * @param endColumn the ending column number.  0-based: column 0 is the
     * first column.
     */
    public void setSelection(final int startRow, final int startColumn,
        final int endRow, final int endColumn) {

        editField.setSelection(startRow, startColumn, endRow, endColumn);
    }

    /**
     * Replace whatever is being selected with new text.  If not in
     * selection, nothing is replaced.
     *
     * @param text the new replacement text
     */
    public void replaceSelection(final String text) {
        editField.replaceSelection(text);
    }

    /**
     * Check if selection is available.
     *
     * @return true if a selection has been made
     */
    public boolean hasSelection() {
        return editField.hasSelection();
    }

    /**
     * Copy text within the selection bounds to a string.
     *
     * @return the selection as a string, or null if there is no selection
     */
    public String getSelection() {
        return editField.getSelection();
    }

    /**
     * Get the selection starting row number.
     *
     * @return the starting row number, or -1 if there is no selection.
     * 0-based: row 0 is the first row.
     */
    public int getSelectionStartRow() {
        return editField.getSelectionStartRow();
    }

    /**
     * Get the selection starting column number.
     *
     * @return the starting column number, or -1 if there is no selection.
     * 0-based: column 0 is the first column.
     */
    public int getSelectionStartColumn() {
        return editField.getSelectionStartColumn();
    }

    /**
     * Get the entire contents of the editor as one string.
     *
     * @return the editor contents
     */
    public String getText() {
        return editField.getText();
    }

    /**
     * Save file implementation.
     */
    private void saveFile() {
        XTWMApplication app;
        app = ((XTWMApplication) getApplication());
        if (app.getOption("editor.internal.trimWhitespace",
                "true").equals("true")
        ) {
            editField.cleanWhitespace();
        }

        try {
            if (file != null) {
                if (file.exists() && (isNewFile == true)) {
                    if (messageBox(i18n.getString("overwriteFileTitle"),
                            MessageFormat.format(i18n.
                                getString("overwriteFilePrompt"),
                                file.getName()),
                            TMessageBox.Type.YESNO).isNo()) {

                        return;
                    }
                }
                editField.saveToFilename(file.getPath());
                isNewFile = false;

            } else if (isNewFile == true) {

                // This is a brand-new file.
                String filename = fileSaveBox(".");
                if (filename != null) {
                    file = new File(filename);
                    if (file.exists()) {
                        if (messageBox(i18n.getString("overwriteFileTitle"),
                                MessageFormat.format(i18n.
                                    getString("overwriteFilePrompt"),
                                    file.getName()),
                                TMessageBox.Type.YESNO).isNo()) {
                            file = null;
                            return;
                        }
                    }
                    editField.saveToFilename(file.getPath());
                    setTitle(file.getName());
                    isNewFile = false;
                }

            }

        } catch (IOException e) {
            messageBox(i18n.getString("errorDialogTitle"),
                MessageFormat.format(i18n.
                    getString("errorSavingFile"), e.getMessage()));
        }
    }

}
