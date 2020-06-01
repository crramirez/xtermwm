Xterm Window Manager
====================

Xterm Window Manager (XtermWM) is a desktop environment / window
manager for the console.  Major features include:

  * Virtual desktops and desktop pager widget.

  * Cascading/overlapping/floating windows with mouse-draggable move
    and resizing.

  * Tiled/paned windows with mouse selection and divider-draggable
    resizing.

  * A plugin system for adding custom widgets, windows, and other
    functions.

  * The ability to run in Xterm-like environments (Unix command line
    shells or over ssh) and as a Swing component for X11/Windows/Mac,
    with the same behavior and look-and-feel.  The mouse can also work
    for the raw Linux console when GPM installed and XtermWM is run
    inside [LCxterm](https://lcxterm.sourceforge.io).

  * Image support, for both Swing and Xterm.  When running under
    Xterm, images are rendered as sixel graphic sequences.

  * Tiled and windowed terminal shells with good Xterm/VT100 support,
    including mouse.  Terminal shells will also resize correctly due
    to the included [ptypipe](https://gitlab.com/klamonte/ptypipde)
    utility.

  * Detachable sessions and share-able sessions with read-only client
    support.



Intended Audience
-----------------

XtermWM is expected to be helpful to the following kinds of people:

  * Console users who want to have multiple windowed terminals, but
    not a full X11 desktop environment.

  * Data center / system administrators who want a more capable
    physical console at the rack / head node, but will not (or cannot)
    install X11.

  * People who used the DOS-era "shells" like IBM DOS Shell, DESQview,
    WordPerfect Office, and Direct Access, and want similar features
    in their terminals without having to learn Emacs Lisp.



License
-------

XtermWM is available to all under the MIT License.  See the file
LICENSE for the full license text.

The interface library that XtermWM is built out of is also available
separately under the MIT license at https://jexer.sourceforge.io .



Screenshots
-----------

![Terminals and Pager Image](/screenshots/pager_1.png?raw=true "Two terminal windows with the Desktop Pager and Calendar widgets")



Developer Information
---------------------

XtermWM is currently in alpha, features are being added.  When it
releases, it will be looking for a project maintainer.  See Issue #1
for more details.
