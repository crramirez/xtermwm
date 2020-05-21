/*
 * ptypipe - turn any pipe into a TTY, with resizable window support
 *
 * The MIT License (MIT)
 *
 * Copyright (C) 2020 Kevin Lamonte
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

/* #define DEBUG */

/* #define MAKE_RAW */

/* #define PASS_SIGNALS */

/* PATH_MAX */
#include <limits.h>

/* select() */
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>

/* forkpty() */
#ifdef __APPLE__
#  include <util.h>
#else
#  if defined(__FreeBSD__) || \
      defined(__OpenBSD__) || \
      defined(__NetBSD__)
#    include <sys/types.h>
#    include <util.h>
#  else
#    include <pty.h>
#  endif
#endif

/* fprintf() */
#include <stdio.h>

/* perror() */
#include <errno.h>

/* abort() */
#include <stdlib.h>

/* kill() and signal() */
#include <signal.h>

/* wait4() */
#include <sys/resource.h>
#include <sys/wait.h>

/* memmove() */
#include <string.h>

/* assert() */
#include <assert.h>

/* ioctl() */
#include <sys/ioctl.h>

#ifdef MAKE_RAW

/* tcgetattr(), tcsetattr(), cfmakeraw() */
#include <termios.h>

/**
 * Set a tty into raw mode.
 *
 * @param tty_fd the tty descriptor
 */
static void set_raw_termios(const int tty_fd) {
    struct termios old_termios;
    struct termios new_termios;

    if (tcgetattr(tty_fd, &old_termios) < 0) {
        /*
         * Error, bail out.
         */
        return;
    }
    memcpy(&new_termios, &old_termios, sizeof(struct termios));
    cfmakeraw(&new_termios);
    if (tcsetattr(tty_fd, TCSANOW, &new_termios) < 0) {
        /*
         * Error, bail out.
         */
        return;
    }

    /*
     * All OK
     */
#ifdef DEBUG
    fprintf(stderr, "set_raw_termios() OK\n");
#endif
    return;
}

#endif /* MAKE_RAW */

#ifdef PASS_SIGNALS

/* The child pid */
static pid_t signal_child_pid = -1;

/**
 * Monitor for a signal, and send to the child process.
 *
 * @param signum the signal
 */
static void signal_handler(int signum) {
    if (signal_child_pid > 0) {
        kill(signal_child_pid, signum);
    }
}

#endif

/**
 * Wait for the child process to exit, and exit with its status.  This
 * function never returns.
 *
 * @param child_pid the child's process ID
 */
static void do_exit(pid_t child_pid) {
    int status;
    pid_t rc = wait4(child_pid, &status, 0, NULL);
    if (rc != child_pid) {
        perror("wait4()");
        exit(-1);
    }
    exit(status);
}

/**
 * Command-line entry point.
 *
 * @param argc count of command line arguments
 * @param argv vector of command line arguments
 * @return int return code of child process, or -1 if forkpty() failed
 */
int main(int argc, char **argv) {

    pid_t child_pid;
    int child_tty_fd;
    int rc;
    char ttyname_buffer[PATH_MAX];
    char buffer[1024];
    int buffer_n = 0;
    int select_fd_max;
    fd_set readfds;
    fd_set writefds;
    fd_set exceptfds;
    int i;
    struct winsize console_size;
    int dtterm_begin = -1;
    int dtterm_state = 0;
    int dtterm_arg = 0;

    /*
     * Check arguments.
     */
    if (argc == 1) {
        fprintf(stderr, "USAGE: ptypipe { args... }\n");
        fflush(stderr);
        exit(-1);
    }

    /*
     * Fork and put the child on a new tty.
     */
    child_pid = forkpty(&child_tty_fd, ttyname_buffer, NULL, NULL);

#ifdef DEBUG
    fprintf(stdout, "child_pid %d child_tty_fd %d ttyname_buffer %s\n",
        child_pid, child_tty_fd, ttyname_buffer);
#endif

    if (child_pid == 0) {
        /*
         * I am the child, exec.
         */
        if (!isatty(STDOUT_FILENO)) {
            perror("isatty()");
            abort();
        }

        /*
         * If LINES and COLUMNS are set, unset them.  They can screw up
         * resizing on ncurses applications.
         */
        unsetenv("LINES");
        unsetenv("COLUMNS");

#ifdef MAKE_RAW
        set_raw_termios(STDIN_FILENO);
        set_raw_termios(STDOUT_FILENO);
#endif

        /*
         * Spawn the other command.
         */
        execvp(argv[1], &argv[1]);

        /*
         * If we got here, then we failed to spawn.  Emit the error message
         * and crap out.
         */
        perror("execvp()");
        exit(-1);
    }

#ifdef PASS_SIGNALS

    /* Define several common signals to be passed to the child process. */
    signal_child_pid = child_pid;
    signal(SIGINT, signal_handler);
    signal(SIGILL, signal_handler);
    signal(SIGABRT, signal_handler);
    signal(SIGFPE, signal_handler);
    signal(SIGTERM, signal_handler);
    signal(SIGHUP, signal_handler);
    signal(SIGQUIT, signal_handler);
    signal(SIGTRAP, signal_handler);
    signal(SIGPIPE, signal_handler);
    signal(SIGURG, signal_handler);
    signal(SIGTSTP, signal_handler);
    signal(SIGCONT, signal_handler);
    signal(SIGTTIN, signal_handler);
    signal(SIGTTOU, signal_handler);
    signal(SIGPOLL, signal_handler);
    signal(SIGXCPU, signal_handler);
    signal(SIGXFSZ, signal_handler);
    signal(SIGVTALRM, signal_handler);
    signal(SIGPROF, signal_handler);
    signal(SIGUSR1, signal_handler);
    signal(SIGUSR2, signal_handler);

#endif

    /*
     * Main loop reads from either side and passes data to the other.
     */
#ifdef MAKE_RAW
    set_raw_termios(child_tty_fd);
#endif
    if (child_tty_fd > STDIN_FILENO) {
        select_fd_max = child_tty_fd + 1;
    } else {
        select_fd_max = STDIN_FILENO + 1;
    }
    for (;;) {
        /*
         * Initialize select() structures.
         */
        FD_ZERO(&readfds);
        FD_ZERO(&writefds);
        FD_ZERO(&exceptfds);
        FD_SET(STDIN_FILENO, &readfds);
        FD_SET(child_tty_fd, &readfds);

        /*
         * Wait for data to appear somewhere.
         */
        rc = select(select_fd_max, &readfds, &writefds, &exceptfds, NULL);

        /*
         * See if the child (TTY side) has data, and if so post it to stdout.
         */
        if (FD_ISSET(child_tty_fd, &readfds)) {
            /*
             * Data is available for reading from the child.
             */
            rc = read(child_tty_fd, buffer, sizeof(buffer) - buffer_n);
            if (rc < 0) {
                if (errno == EIO) {
                    /*
                     * This is EOF.  Exit with the child's status.
                     */
                    do_exit(child_pid);
                }
                perror("read()");
            } else if (rc == 0) {
                /*
                 * This is EOF.  Exit with the child's status.
                 */
                do_exit(child_pid);
            } else {
                /*
                 * We have something in buffer to emit to stdout.
                 */
                buffer_n = rc;
                while (buffer_n > 0) {
                    int n = write(STDOUT_FILENO, buffer, buffer_n);
                    if (n < 0) {
                        perror("write()");
                    } else {
                        if (n < buffer_n) {
                            /*
                             * Shift the buffer down.
                             */
                            memmove(buffer, buffer + n, buffer_n - n);
                            buffer_n -= n;
                            memset(buffer + buffer_n, 0,
                                sizeof(buffer) - buffer_n);
                        } else if (n == buffer_n) {
                            buffer_n = 0;
                        } else if (n > buffer_n) {
                            fprintf(stderr, "Wrote too many bytes!\n");
                            fflush(stderr);
                            abort();
                        }
                    }
                }
            } /* if (rc < 0) */
        } /* if (FD_ISSET(child_tty_fd, &readfds)) */

        assert (buffer_n == 0);

        /*
         * See if the parent (pipe side) has data, and if so post it to
         * the child tty.
         */
        if (FD_ISSET(STDIN_FILENO, &readfds)) {
            /*
             * Data is available for reading from the parent.
             */
            rc = read(STDIN_FILENO, buffer, sizeof(buffer) - buffer_n);
#ifdef DEBUG
            fprintf(stderr, "read() rc = %d\n", rc);
#endif
            if (rc < 0) {
                if (errno == EIO) {
                    /*
                     * This is EOF.  Kill the child with SIGHUP.
                     */
                    kill(child_pid, SIGHUP);
                    do_exit(child_pid);
                }
                perror("read()");
            } else if (rc == 0) {
                /*
                 * We have run out of stdin.  Don't care!
                 */
            } else {
                /*
                 * We have something in buffer to emit to the child.
                 */
                buffer_n = rc;

rescan_buffer:

#ifdef DEBUG
                for (i = 0; i < buffer_n; i++) {
                    fprintf(stderr, "%d char %02x\n", i, buffer[i]);
                }
#endif

                /*
                 * Read input for "CSI 8 ; <rows> ; <cols> t" .  This is the
                 * xterm/dtterm sequence to resize the text area.  Use that
                 * to send a new window size to the child via
                 * ioctl(TIOCSWINSZ).
                 */
                if (dtterm_begin == -1) {
                    for (i = 0; i < buffer_n; i++) {
                        if (buffer[i] == 0x1B) {
                            /* Saw the inital ESC */
                            dtterm_begin = i;
                            break;
                        }
                    }
                }
                if (dtterm_begin >= 0) {
                    if (ioctl(child_tty_fd, TIOCGWINSZ, &console_size) < 0) {
                        /*
                         * Failed to get the old window size.  The next ioctl
                         * likely won't work, but otherwise we don't care.
                         * Keep parsing so as to strip the sequence from the
                         * child's input.
                         */
                    }
                    dtterm_arg = 0;
                    for (i = dtterm_begin; i < buffer_n; i++) {

#ifdef DEBUG
                        fprintf(stderr, "%d state %d char %c %02x arg %d\n", i,
                            dtterm_state, buffer[i], buffer[i], dtterm_arg);
#endif

                        switch (buffer[i]) {
                        case 0x1B:
                            if (dtterm_state == 0) {
                                dtterm_state = 1;
                            } else {
                                goto write_child;
                            }
                            break;
                        case '[':
                            if (dtterm_state == 1) {
                                dtterm_state = 2;
                            } else {
                                goto write_child;
                            }
                            break;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            if ((dtterm_state >= 2) || (dtterm_state <= 4)) {
                                dtterm_arg *= 10;
                                dtterm_arg += buffer[i] - '0';
                            } else {
                                goto write_child;
                            }
                            break;
                        case ';':
                            if (dtterm_state == 2) {
                                if (dtterm_arg != 8) {
                                    goto write_child;
                                }
                            }
                            if (dtterm_state == 3) {
                                console_size.ws_row = dtterm_arg;
                            }
                            dtterm_state++;
                            dtterm_arg = 0;
                            break;
                        case 't':
                            if (dtterm_state == 4) {
                                console_size.ws_col = dtterm_arg;

#ifdef DEBUG
                                fprintf(stderr, "resize %d rows %d cols\n",
                                    console_size.ws_row, console_size.ws_col);
#endif

                                /*
                                 * Got it all, now set the window size and
                                 * strip the sequence out.
                                 */
                                if (ioctl(child_tty_fd, TIOCSWINSZ,
                                        &console_size) < 0) {
                                    /*
                                     * Failed to set the new window size.  Oh
                                     * well.
                                     */
                                }

                                /*
                                 * Make sure we include this 't' in the
                                 * stripping.
                                 */
                                i++;

#ifdef DEBUG
                                fprintf(stderr, "buffer_n before %d\n",
                                    buffer_n);
#endif

                                /*
                                 * Shift the buffer down.
                                 */
                                memmove(buffer + dtterm_begin,
                                    buffer + i,
                                    buffer_n - i);
                                buffer_n -= (i - dtterm_begin);
                                memset(buffer + buffer_n, 0,
                                    sizeof(buffer) - buffer_n);

#ifdef DEBUG
                                fprintf(stderr, "buffer_n after %d\n",
                                    buffer_n);
#endif

                                /*
                                 * Might have several strings in the buffer,
                                 * check again for another.
                                 */
                                dtterm_state = 0;
                                dtterm_arg = 0;
                                dtterm_begin = -1;
                                goto rescan_buffer;

                            }
                            goto write_child;

                        default:
                            /*
                             * This isn't a valid sequence, bail out.
                             */
                            goto write_child;

                        } /* switch (buffer[i]) */

                    } /* for (i = dtterm_begin; i < buffer_n; i++) */

                    /*
                     * buffer runs out before our sequence is parsed.  Skip
                     * the write, see if we get more data later.
                     *
                     * Note that this continue matches the outer-level
                     * for (;;) loop.
                     *
                     * Special case exception: if there is a single ESC, emit
                     * that.  This will permit a user to hit ESC and have it
                     * passed through.
                     */
                    if ((buffer_n == 1) && (buffer[0] == 0x1B)) {
                        goto write_child;
                    }
                    continue;

                } /* if (dtterm_begin >= 0) */

write_child:
                dtterm_state = 0;
                dtterm_begin = -1;

#ifdef DEBUG
                for (i = 0; i < buffer_n; i++) {
                    fprintf(stderr, "%d char %02x\n", i, buffer[i]);
                }
#endif

                /*
                 * Emit buffer to child.
                 */
                while (buffer_n > 0) {
                    int n = write(child_tty_fd, buffer, buffer_n);
#ifdef DEBUG
                    fprintf(stderr, "write() n = %d\n", n);
#endif
                    if (n < 0) {
                        perror("write()");
                    } else {
                        if (n < buffer_n) {
                            /*
                             * Shift the buffer down.
                             */
                            memmove(buffer, buffer + n, buffer_n - n);
                            buffer_n -= n;
                            memset(buffer + buffer_n, 0,
                                sizeof(buffer) - buffer_n);
                        } else if (n == buffer_n) {
                            buffer_n = 0;
                        } else if (n > buffer_n) {
                            fprintf(stderr, "Wrote too many bytes!\n");
                            fflush(stderr);
                            abort();
                        }
                    }
                }
            } /* if (rc < 0) */
        } /* if (FD_ISSET(STDIN_FILENO, &readfds)) */

    } /* for (;;) */

    /*
     * We should never get here.
     */
    fprintf(stderr, "Main exits, huh?\n");
    fflush(stderr);
    abort();
}
