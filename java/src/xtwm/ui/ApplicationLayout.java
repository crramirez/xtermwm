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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import jexer.TExceptionDialog;
import jexer.TSplitPane;
import jexer.TTerminalWindow;
import jexer.TWidget;
import jexer.TWindow;

import xtwm.plugins.DesktopPager;
import xtwm.plugins.PluginWidget;

/**
 * ApplicationLayout reads and writes the desktop and window layout to XML
 * files.
 */
public class ApplicationLayout {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // ApplicationLayout ------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Write the layout of an application to file.
     *
     * @param app the application
     * @param filename the name of the file to write to
     * @throws IOException if a java.io operation throws
     */
    public static void saveToXml(final XTWMApplication app,
        final String filename) throws IOException,
                                      ClassNotFoundException,
                                      IllegalAccessException,
                                      InstantiationException,
                                      ParserConfigurationException,
                                      SAXException {

        List<VirtualDesktop> desktops = app.getDesktops();
        List<TWindow> windows = app.getAllWindows();

        File file = new File(filename);

        DocumentBuilder domBuilder;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        domBuilder = dbFactory.newDocumentBuilder();
        Document doc = domBuilder.newDocument();
        doc.setXmlStandalone(true);
        Element root = doc.createElement("layout");
        root.setAttribute("desktopCount", Integer.toString(desktops.size()));
        root.setAttribute("width", Integer.toString(app.getScreen().getWidth()));
        root.setAttribute("height", Integer.toString(app.getScreen().getHeight()));
        root.setAttribute("hideMenuBar",
            (app.getOption("xtwm.hideMenuBar", "false").equals("true") ?
                "true" : "false"));
        root.setAttribute("hideStatusBar",
            (app.getOption("xtwm.hideStatusBar", "true").equals("true") ?
                "true" : "false"));

        doc.appendChild(root);

        // Add desktops
        Element desktopElems = doc.createElement("desktops");
        for (VirtualDesktop desktop: desktops) {
            desktopElems.appendChild(createDesktopXml(doc, desktop));
        }
        root.appendChild(desktopElems);

        // Add windows
        Element windowElems = doc.createElement("windows");
        for (TWindow window: windows) {
            Element elem = createWindowXml(app, doc, window);
            if (elem != null) {
                windowElems.appendChild(elem);
            }
        }
        root.appendChild(windowElems);

        // Write it out
        DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS) reg.
                                                getDOMImplementation("LS");
        LSSerializer ls = impl.createLSSerializer();

        ls.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        LSOutput lsOut = impl.createLSOutput();
        lsOut.setEncoding("UTF-8");
        FileWriter output = new FileWriter(file);
        lsOut.setCharacterStream(output);
        ls.write(doc, lsOut);

        output.close();
    }

    /**
     * Read the layout from file and set up the application to match.
     *
     * @param app the application
     * @param filename the name of the file to read from
     * @throws IOException if a java.io operation throws
     */
    public static void loadFromXml(final XTWMApplication app,
        final String filename) throws IOException,
                                      ParserConfigurationException,
                                      SAXException {

        File file = new File(filename);
        if (!file.exists()
            || !file.isFile()
        ) {
            return;
        }

        double scaleX = 1.0;
        double scaleY = 1.0;

        DocumentBuilder domBuilder;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        domBuilder = dbFactory.newDocumentBuilder();
        Document doc = domBuilder.parse(file);

        // Get the document's root XML node
        Node root = doc.getChildNodes().item(0);
        if (!(root instanceof Element)) {
            return;
        }
        Element rootElem = (Element) root;

        // Process attributes on the "layout" tag.
        String attrValue = rootElem.getAttribute("width");
        if (attrValue.length() > 0) {
            try {
                int intValue = Integer.parseInt(attrValue);
                if (intValue >= 10) {
                    scaleX = (double) app.getScreen().getWidth() / intValue;
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
        }
        attrValue = rootElem.getAttribute("height");
        if (attrValue.length() > 0) {
            try {
                int intValue = Integer.parseInt(attrValue);
                if (intValue >= 10) {
                    scaleY = (double) app.getScreen().getHeight() / intValue;
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
        }
        attrValue = rootElem.getAttribute("desktopCount");
        if (attrValue.length() > 0) {
            try {
                int intValue = Integer.parseInt(attrValue);
                if (intValue >= 1) {
                    while (app.getDesktops().size() > intValue) {
                        app.removeDesktop();
                    }
                    while (app.getDesktops().size() < intValue) {
                        app.addDesktop();
                    }
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
        }
        attrValue = rootElem.getAttribute("hideMenuBar");
        if (attrValue.length() > 0) {
            app.setHideMenuBar(attrValue.equals("true"));
        }
        attrValue = rootElem.getAttribute("hideStatusBar");
        if (attrValue.length() > 0) {
            app.setHideStatusBar(attrValue.equals("true"));
        }

        // Clear everything in the application except for the desktop
        // pager.
        for (VirtualDesktop desktop: app.getDesktops()) {
            while (desktop.getDesktop().getChildren().size() > 0) {
                desktop.getActivePanel().close();
            }
        }
        List<TWindow> windowsToRemove = new ArrayList<TWindow>();
        for (TWindow window: app.getAllWindows()) {
            if (window.getChildren().size() == 1) {
                TWidget child = window.getChildren().get(0);
                if (child instanceof DesktopPager) {
                    continue;
                }
            }
            windowsToRemove.add(window);
        }
        for (TWindow window: windowsToRemove) {
            app.closeWindow(window);
        }

        NodeList level1 = root.getChildNodes();
        for (int i = 0; i < level1.getLength(); i++) {
            Node node = level1.item(i);
            String name = node.getNodeName();
            String value = node.getTextContent();

            if (name.equals("desktops")) {
                NodeList desktopNodes = node.getChildNodes();
                int desktopIdx = 0;
                for (int j = 0; j < desktopNodes.getLength(); j++) {
                    Node desktop = desktopNodes.item(j);
                    if ((desktop instanceof Element)
                        && (desktop.getNodeName().equals("desktop"))
                    ) {
                        addDesktopFromXml(desktopIdx, app, scaleX, scaleY, doc,
                            (Element) desktop);
                        desktopIdx++;
                    }
                }
            }

            if (name.equals("windows")) {
                NodeList windowNodes = node.getChildNodes();
                for (int j = 0; j < windowNodes.getLength(); j++) {
                    Node window = windowNodes.item(j);
                    if (window instanceof Element) {
                        addWindowFromXml(app, scaleX, scaleY, doc,
                            (Element) window);
                    }
                }
            }
        }

    }

    /**
     * Add a window to the application based on a DOM element.
     *
     * @param app the application
     * @param scaleX the ratio of layout width to current application width
     * @param scaleY the ratio of layout height to current application height
     * @param doc the document
     * @param elem the element
     */
    private static void addWindowFromXml(final XTWMApplication app,
        final double scaleX, final double scaleY, final Document doc,
        final Element elem) {

        int x = 0;
        int y = 0;
        int width = 10;
        int height = 10;
        int desktop = 0;

        String attrValue = elem.getAttribute("x");
        if (attrValue.length() > 0) {
            try {
                int intValue = Integer.parseInt(attrValue);
                if (intValue >= 1) {
                    x = intValue;
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
        }
        attrValue = elem.getAttribute("y");
        if (attrValue.length() > 0) {
            try {
                int intValue = Integer.parseInt(attrValue);
                if (intValue >= 1) {
                    y = intValue;
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
        }
        attrValue = elem.getAttribute("width");
        if (attrValue.length() > 0) {
            try {
                int intValue = Integer.parseInt(attrValue);
                if (intValue >= 1) {
                    width = intValue;
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
        }
        attrValue = elem.getAttribute("height");
        if (attrValue.length() > 0) {
            try {
                int intValue = Integer.parseInt(attrValue);
                if (intValue >= 1) {
                    height = intValue;
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
        }
        attrValue = elem.getAttribute("desktop");
        if (attrValue.length() > 0) {
            try {
                int intValue = Integer.parseInt(attrValue);
                if ((intValue >= 1)
                    && (intValue <= app.getDesktops().size())
                ) {
                    desktop = intValue;
                }
            } catch (NumberFormatException e) {
                // SQUASH
            }
        }
        String type = elem.getAttribute("type");
        if (type.equals("terminal")) {
            List<String> commandLine = new ArrayList<String>();
            NodeList childNodes = elem.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                if (childNode.getNodeName().equals("commandLine")) {
                    NodeList argList = childNode.getChildNodes();
                    for (int k = 0; k < argList.getLength(); k++) {
                        Node argNode = argList.item(k);
                        if (argNode.getNodeName().equals("arg")) {
                            String arg = argNode.getTextContent();
                            commandLine.add(arg);
                        }
                    }
                }
            }

            TTerminalWindow terminal = app.openTerminal(0, 0, TWindow.RESIZABLE,
                commandLine.toArray(new String[0]));
            terminal.setX((int) (x * scaleX));
            terminal.setY((int) (y * scaleY));
            terminal.setWidth(width);
            terminal.setHeight(height);
            terminal.ensureOnScreen();
            if (desktop > 0) {
                app.getDesktops().get(desktop - 1).addWindow(terminal);
            }
        } else if (type.equals("internalEditor")) {
            String filename = "";
            InternalEditorWindow editor;
            NodeList childNodes = elem.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                if (childNode.getNodeName().equals("filename")) {
                    filename = childNode.getTextContent();
                    break;
                }
            }
            if (filename.length() > 0) {
                try {
                    editor = new InternalEditorWindow(app, new File(filename),
                        0, 0, 82, 25);
                } catch (IOException e) {
                    new TExceptionDialog(app, e);
                    return;
                }
            } else {
                editor = new InternalEditorWindow(app);
            }
            editor.setX((int) (x * scaleX));
            editor.setY((int) (y * scaleY));
            editor.setWidth(width);
            editor.setHeight(height);
            editor.ensureOnScreen();
            if (desktop > 0) {
                app.getDesktops().get(desktop - 1).addWindow(editor);
            }
        } else if (type.equals("externalEditor")) {
            String filename = "";
            ExternalEditorWindow editor;
            NodeList childNodes = elem.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                if (childNode.getNodeName().equals("filename")) {
                    filename = childNode.getTextContent();
                    break;
                }
            }
            if (filename.length() > 0) {
                editor = new ExternalEditorWindow(app, filename);
            } else {
                editor = new ExternalEditorWindow(app);
            }
            editor.setX((int) (x * scaleX));
            editor.setY((int) (y * scaleY));
            editor.setWidth(width);
            editor.setHeight(height);
            editor.ensureOnScreen();
            if (desktop > 0) {
                app.getDesktops().get(desktop - 1).addWindow(editor);
            }
        }
    }

    /**
     * Add a desktop panel layout to the application based on a DOM element.
     *
     * @param idx the desktop number
     * @param app the application
     * @param scaleX the ratio of layout width to current application width
     * @param scaleY the ratio of layout height to current application height
     * @param doc the document
     * @param elem the element
     */
    private static void addDesktopFromXml(final int idx,
        final XTWMApplication app, final double scaleX,
        final double scaleY, final Document doc, final Element elem) {

        // System.err.println("addDesktop: " + idx);

        if ((idx < 0) || (idx > app.getDesktops().size() - 1)) {
            return;
        }

        Desktop desktop = app.getDesktops().get(idx).getDesktop();
        NodeList childNodes = elem.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node childNode = childNodes.item(j);
            if ((childNode instanceof Element)
                && (childNode.getNodeName().equals("splitPane")
                    || childNode.getNodeName().equals("terminal")
                    || childNode.getNodeName().equals("plugin"))
            ) {
                TWidget widget = getWidgetFromXml(app, desktop, scaleX, scaleY,
                    doc, (Element) childNode);
                if (widget != null) {
                    // System.err.println("addDesktop(): widget = " + widget.toPrettyString());

                    if (widget instanceof TSplitPane) {
                        TSplitPane split = (TSplitPane) widget;
                        if (split.isVertical()) {
                            split.setSplit((int) (split.getSplit() * scaleX));
                        } else {
                            split.setSplit((int) (split.getSplit() * scaleY));
                        }
                    }
                    // Only load the first widget, bail out now.
                    return;
                }
            }
        }
    }

    /**
     * Create a desktop panel widget based on a DOM element.
     *
     * @param app the application
     * @param parent the parent of the widget
     * @param scaleX the ratio of layout width to current application width
     * @param scaleY the ratio of layout height to current application height
     * @param doc the document
     * @param elem the element
     * @return the widget
     */
    @SuppressWarnings({"unchecked"})
    private static TWidget getWidgetFromXml(final XTWMApplication app,
        final TWidget parent, final double scaleX, final double scaleY,
        final Document doc, final Element elem) {

        // System.err.println("getWidgetFromXml(): elem = " + elem);

        String type = elem.getNodeName();
        if (type.equals("terminal")) {
            TiledTerminal terminal = new TiledTerminal(parent);
            return terminal;
        }
        if (type.equals("plugin")) {
            String className = elem.getAttribute("class");
            try {
                Class<? extends PluginWidget> pluginClass;
                ClassLoader loader = Thread.currentThread().
                        getContextClassLoader();
                pluginClass = (Class<? extends PluginWidget>) loader.loadClass(className);
                PluginWidget plugin = app.makePluginWidget(pluginClass);
                if (plugin != null) {
                    plugin.setParent(parent, false);
                }
                return plugin;
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        if (type.equals("splitPane")) {
            int split = Math.min(parent.getWidth(), parent.getHeight()) / 2;
            boolean vertical = true;
            String splitType = elem.getAttribute("type");
            if (splitType.equals("vertical")) {
                vertical = true;
            } else if (splitType.equals("horizontal")) {
                vertical = false;
            } else {
                return null;
            }
            String splitString = elem.getAttribute("split");
            try {
                split = Integer.parseInt(splitString);
            } catch (NumberFormatException e) {
                return null;
            }
            TSplitPane splitPane = new TSplitPane(parent, 0, 0,
                parent.getWidth(), parent.getHeight(), vertical);

            TWidget widget1 = null;
            TWidget widget2 = null;
            TWidget widget = null;
            NodeList childNodes = elem.getChildNodes();
            int widgetIdx = 0;
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                if ((childNode instanceof Element)
                    && (childNode.getNodeName().equals("splitPane")
                        || childNode.getNodeName().equals("terminal")
                        || childNode.getNodeName().equals("plugin"))
                ) {
                    widget = getWidgetFromXml(app, parent, scaleX, scaleY,
                        doc, (Element) childNode);
                    if (widget instanceof TSplitPane) {
                        TSplitPane subSplit = (TSplitPane) widget;
                        if (subSplit.isVertical()) {
                            subSplit.setSplit((int) (subSplit.getSplit() * scaleX));
                        } else {
                            subSplit.setSplit((int) (subSplit.getSplit() * scaleY));
                        }
                    }
                    if (widgetIdx == 0) {
                        widget1 = widget;
                    } else if (widgetIdx == 1) {
                        widget2 = widget;
                    }
                    widgetIdx++;
                    if (widgetIdx >= 2) {
                        break;
                    }
                }
            }
            if (vertical) {
                splitPane.setLeft(widget1);
                splitPane.setRight(widget2);
            } else {
                splitPane.setTop(widget1);
                splitPane.setBottom(widget2);
            }
            return splitPane;
        }

        // Not found.
        return null;
    }

    /**
     * Create a DOM element representing a window.
     *
     * @param app the application
     * @param doc the document
     * @param window the window
     * @return the new element
     */
    private static Element createWindowXml(final XTWMApplication app,
        final Document doc, final TWindow window) {

        Element root = doc.createElement("window");

        if (window instanceof ExternalEditorWindow) {
            root.setAttribute("type", "externalEditor");
            ExternalEditorWindow editor = (ExternalEditorWindow) window;
            Element elem = doc.createElement("filename");
            elem.appendChild(doc.createTextNode(editor.getFilename()));
            root.appendChild(elem);
        } else if (window instanceof InternalEditorWindow) {
            root.setAttribute("type", "internalEditor");
            InternalEditorWindow editor = (InternalEditorWindow) window;
            Element elem = doc.createElement("filename");
            elem.appendChild(doc.createTextNode(
                editor.getFilename() == null ? "" : editor.getFilename()));
            root.appendChild(elem);
        } else if (window instanceof TTerminalWindow) {
            root.setAttribute("type", "terminal");
            TTerminalWindow terminal = (TTerminalWindow) window;
            Element argsList = doc.createElement("commandLine");
            for (String arg: terminal.getCommandLine()) {
                Element elem = doc.createElement("arg");
                elem.appendChild(doc.createTextNode(arg));
                argsList.appendChild(elem);
            }
            root.appendChild(argsList);
        } else {
            // No other kinds of open windows supported.
            return null;
        }

        // Window layout will be saved, add its position.
        root.setAttribute("x", Integer.toString(window.getX()));
        root.setAttribute("y", Integer.toString(window.getY()));
        root.setAttribute("width", Integer.toString(window.getWidth()));
        root.setAttribute("height", Integer.toString(window.getHeight()));

    outer:
        for (int i = 0; i < app.getDesktops().size(); i++) {
            VirtualDesktop desktop = app.getDesktops().get(i);
            for (TWindow w: desktop.getWindows()) {
                if (w == window) {
                    root.setAttribute("desktop", Integer.toString(i + 1));
                    break outer;
                }
            }
        }

        return root;
    }

    /**
     * Create a DOM element representing a virtual desktop.
     *
     * @param doc the document
     * @param desktop the desktop
     * @return the new element
     */
    private static Element createDesktopXml(final Document doc,
        final VirtualDesktop desktop) {

        Element root = doc.createElement("desktop");

        TWidget rootPanel = desktop.getActivePanel();
        if (rootPanel == desktop.getDesktop()) {
            // No child nodes.
            return root;
        }

        rootPanel = desktop.getDesktop().getChildren().get(0);

        // Recursively build the tree of the panels and splits.
        root.appendChild(createWidgetXml(doc, rootPanel));

        return root;
    }

    /**
     * Create a DOM element representing a widget on a desktop.
     *
     * @param doc the document
     * @param widget the widget
     * @return the new element
     */
    private static Element createWidgetXml(final Document doc,
        final TWidget widget) {

        Element root = null;
        if (widget instanceof TSplitPane) {
            TSplitPane split = (TSplitPane) widget;
            root = doc.createElement("splitPane");
            root.setAttribute("type",
                (split.isVertical() ? "vertical" : "horizontal"));
            root.setAttribute("split", Integer.toString(split.getSplit()));
            if (split.isVertical()) {
                assert (split.getLeft() != null);
                assert (split.getRight() != null);
                root.appendChild(createWidgetXml(doc, split.getLeft()));
                root.appendChild(createWidgetXml(doc, split.getRight()));
            } else {
                assert (split.getTop() != null);
                assert (split.getBottom() != null);
                root.appendChild(createWidgetXml(doc, split.getTop()));
                root.appendChild(createWidgetXml(doc, split.getBottom()));
            }
        } else if (widget instanceof TiledTerminal) {
            root = doc.createElement("terminal");
        } else if (widget instanceof PluginWidget) {
            root = doc.createElement("plugin");
            root.setAttribute("class", widget.getClass().getName());
        } else {
            // No other types supported.
            root = doc.createElement("unknown");
            // System.err.println("Unknown: " + widget.toPrettyString());
        }
        return root;
    }

}
