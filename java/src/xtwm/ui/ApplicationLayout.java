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

import jexer.TSplitPane;
import jexer.TWidget;
import jexer.TWindow;

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
        root.setAttribute("desktops", Integer.toString(desktops.size()));
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
            windowElems.appendChild(createWindowXml(doc, window));
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

        String theName = "";

        DocumentBuilder domBuilder;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        domBuilder = dbFactory.newDocumentBuilder();
        Document doc = domBuilder.parse(file);

        // Get the document's root XML node
        Node root = doc.getChildNodes().item(0);
        NodeList level1 = root.getChildNodes();
        for (int i = 0; i < level1.getLength(); i++) {
            Node node = level1.item(i);
            String name = node.getNodeName();
            String value = node.getTextContent();

            // TODO
            if (name.equals("name")) {
                theName = value;
            }
        }

    }

    /**
     * Create a DOM element representing a window.
     *
     * @param doc the Document
     * @param window the window
     */
    private static Element createWindowXml(final Document doc,
        final TWindow window) {

        Element root = doc.createElement("window");

        // TODO: fill in stuff based on window class


        return root;
    }

    /**
     * Create a DOM element representing a virtual desktop.
     *
     * @param doc the Document
     * @param desktop the desktop
     */
    private static Element createDesktopXml(final Document doc,
        final VirtualDesktop desktop) {

        Element root = doc.createElement("desktop");

        TWidget rootPanel = desktop.getActivePanel();
        if (rootPanel == desktop.getDesktop()) {
            // No child nodes.
            return root;
        }

        // TODO: build a tree out of the panels/splitpanes


        return root;
    }

}
