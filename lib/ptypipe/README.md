ptypipe
=======


ptypipe is a small command-line utility that runs its first argument
in a pseudo terminal created with forkpty().  This is handy for the
following cases:

* Undoing glibc's behavior of buffering stdin/stdout, which makes
  pipe-based wrapped command shells useless.

* Using a different window size for programs than the controlling
  TTY.

* Notifying a pipe-based wrapped shell that the window size has
  changed.

One can get a similar result to this program through other means.  The
'script' utility supplied in most POSIX-like systems can emulate a
TTY.  'pipetty' (licensed GPL) is also available from the KBtin
source.


Usage
-----

```
ptypipe { command } { arguments }
```


Resizable Window Support
------------------------

ptypipe monitors its stdin for the dtterm/xterm sequence:

```
ESC [ 8 ; { rows } ; { cols } t
```

If this sequence is seen, it is stripped from the input to the child
process and ioctl(TIOCSWINSZ) is executed to notify the child of a new
window size.  One could thus do something like:

```
/bin/echo -ne "\033[8;20;40t" | ptypipe ls
```

...and ls will execute with a window size of 20 rows by 40 columns.

[Jexer](https://gitlab.com/klamonte/jexer) illustrates how ptypipe can
work for a larger program.  Jexer is 100% Java, but needs to spawn
interactive terminals.  It can do this with either 'script' or
'ptypipe'.  Using 'script' results in terminal windows that cannot be
resized; using 'ptypipe' those terminal windows resize as expected in
modern programs.


License
-------

ptypipe is licensed under the MIT License.  See the file LICENSE for
the full license text.


Bugs
----

If an ESC is in the stream ahead of a correct resize sequence, the
resize sequence is not noticed and will be emitted to the child
process.
