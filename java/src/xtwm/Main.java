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
package xtwm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import jexer.TApplication;
import jexer.TApplication.BackendType;
import jexer.backend.ECMA48Backend;
import jexer.backend.HeadlessBackend;
import jexer.backend.MultiBackend;
import jexer.net.TelnetServerSocket;
import xtwm.ui.XTWMApplication;

/**
 * This class is the main driver for XTWM.
 */
public class Main {

    /**
     * The application that will run.
     */
    private static XTWMApplication app = null;

    /**
     * The headless server socket.
     */
    private static ServerSocket server = null;

    /**
     * Request a password on a new socket connection.
     *
     * @param socket the socket
     * @return true if the password matches the xtwm.serverPassword value
     */
    private static boolean isPasswordOk(final Socket socket) throws IOException {

        OutputStreamWriter writer = new OutputStreamWriter(socket.
            getOutputStream(), "UTF-8");
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.
                getInputStream(), "UTF-8"));

        String password = "";
        do {
            writer.write("Password: ");
            writer.flush();
            password = reader.readLine();
        } while ((password == null) || (password.length() == 0));

        return password.trim().equals(app.getOption("xtwm.serverPassword",
                "ThePasswordToUseWhenNotDefinedInTheRCFile"));
    }

    /**
     * Run the application as a server.
     *
     * @param pid_filename the filename to write the server information to
     */
    private static void runServer(final String pid_filename) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                File pidFile = new File(pid_filename);
                pidFile.delete();
            }
        });

        try {
            // Create a headless screen and use it to establish a
            // MultiBackend.
            HeadlessBackend headlessBackend = new HeadlessBackend();
            MultiBackend multiBackend = new MultiBackend(headlessBackend);

            // Create the XTWM application and spin it up.
            app = new XTWMApplication(multiBackend);
            (new Thread(app)).start();
            multiBackend.setListener(app);

            // Fire up the telnet server and write its port to the PID file.
            server = new TelnetServerSocket(0, 5,
                InetAddress.getLoopbackAddress());
            FileWriter pidFile = new FileWriter(pid_filename);
            pidFile.write(String.format("%d", server.getLocalPort()));
            pidFile.close();

            // Accept connections as long as the application is running.
            Thread serverThread = new Thread() {
                public void run() {
                    while (app.isRunning() && !server.isClosed()) {
                        Socket socket = null;
                        try {
                            socket = server.accept();

                            // The first client can connect without a
                            // password.  All other clients require one.
                            if (multiBackend.getBackends().size() > 0) {
                                if (!isPasswordOk(socket)) {
                                    socket.close();
                                    continue;
                                }
                            }

                            ECMA48Backend ecmaBackend = new ECMA48Backend(app,
                                socket.getInputStream(),
                                socket.getOutputStream(), true);
                            multiBackend.addBackend(ecmaBackend, true);
                        } catch (IOException e) {
                            if (socket != null) {
                                try {
                                    socket.close();
                                } catch (IOException e2) {
                                    // SQUASH
                                }
                                socket = null;
                            }
                        }
                    }
                }
            };
            serverThread.start();

            // Poll for application exit.
            while (app.isRunning()) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    // SQUASH
                }
            }

            // When the application exits, kill all of the connections too.
            multiBackend.shutdown();
            server.close();
        } catch (IOException e) {
            if (app != null) {
                app.restoreConsole();
            }
            e.printStackTrace();
            System.exit(2);
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (Exception e) {
                    // SQUASH
                }
            }
        }
    }

    /**
     * Display usage string and exit.
     */
    private static void showUsage() {
        System.err.println("USAGE: java -jar xtwm.jar OPTIONS");
        System.err.println();
        System.err.println("OPTIONS:");
        System.err.println();
        System.err.println("   [ --width columns ] [ --height rows ]");
        System.err.println();
        System.err.println("   [ --server pid_filename ]");
        System.err.println();
        System.err.println("   [ --help | -h | -? ]");
        System.err.println("   [ --version ]");

        System.exit(1);
    }

    /**
     * Main entry point.
     *
     * @param args Command line arguments
     */
    public static void main(final String [] args) {

        try {
            // Swing is the default backend on Windows and Mac unless
            // explicitly overridden by jexer.Swing.
            BackendType backendType = BackendType.XTERM;
            if (System.getProperty("os.name").startsWith("Windows")) {
                backendType = TApplication.BackendType.SWING;
            }
            if (System.getProperty("os.name").startsWith("Mac")) {
                backendType = TApplication.BackendType.SWING;
            }
            if (System.getProperty("jexer.Swing") != null) {
                if (System.getProperty("jexer.Swing",
                        "false").equals("true")) {

                    backendType = TApplication.BackendType.SWING;
                } else {
                    backendType = TApplication.BackendType.XTERM;
                }
            }

            if (backendType == TApplication.BackendType.SWING) {
                // Defaults for Swing: no text mouse.
                if (System.getProperty("jexer.textMouse") == null) {
                    System.setProperty("jexer.textMouse", "false");
                }
                if (System.getProperty("jexer.Swing.mouseStyle") == null) {
                    System.setProperty("jexer.Swing.mouseStyle", "default");
                }
            }

            /*
             * Supported arguments:
             *
             * --help | -h | -?
             * --version
             * --width width
             * --height height
             * --server pid_filename
             */
            int width = -1;
            int height = -1;
            String pid_filename = null;
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--help")
                    || args[i].equals("-h")
                    || args[i].equals("-?")
                ) {
                    showUsage();
                }

                if (args[i].equals("--width")) {
                    if (i == args.length - 1) {
                        showUsage();
                    }
                    try {
                        width = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        showUsage();
                    }
                    i++;
                    continue;
                }
                if (args[i].equals("--height")) {
                    if (i == args.length - 1) {
                        showUsage();
                    }
                    try {
                        height = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        showUsage();
                    }
                    i++;
                    continue;
                }
                if (args[i].equals("--version")) {
                    String version = Main.class.getPackage().getImplementationVersion();
                    if (version == null) {
                        // This is Java 9+, use a hardcoded string here.
                        version = XTWMApplication.VERSION;
                    }
                    System.out.println("Xterm Window Manager " + version);
                    System.out.println("Copyright (c) 2020 Kevin Lamonte");
                    System.out.println();
                    System.out.println("Available to all under the MIT License.");
                    System.exit(1);
                }
                if (args[i].equals("--server")) {
                    if (i == args.length - 1) {
                        showUsage();
                    }
                    pid_filename = args[i + 1];
                    i++;
                    continue;
                }
            }

            if (pid_filename != null) {
                runServer(pid_filename);
                return;
            }

            // We know we will be starting the application, so set it up.
            if ((width > 0) && (height > 0)) {
                app = new XTWMApplication(backendType, width, height, 20);
            } else {
                app = new XTWMApplication(backendType);
            }

            app.run();
        } catch (Throwable t) {
            if (app != null) {
                app.restoreConsole();
            }
            t.printStackTrace();
            System.exit(2);
        }
    }

}
