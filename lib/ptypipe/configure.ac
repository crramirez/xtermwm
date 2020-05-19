AC_INIT([ptypipe], [1.0.0])
AC_CONFIG_SRCDIR([./ptypipe.c])
AM_CONFIG_HEADER([config.h])
AM_INIT_AUTOMAKE([foreign subdir-objects])

dnl Checks for programs
AC_PROG_CC
AC_PROG_INSTALL

dnl Checks for header files.
AC_CHECK_HEADERS(pty.h)
AC_CHECK_HEADERS(libutil.h)
AC_CHECK_HEADERS(util.h)

dnl Checks for libraries
AC_SEARCH_LIBS(forkpty, util)
AC_CHECK_FUNCS(forkpty)

subdirs="$subdirs"
AC_SUBST(subdirs)
AC_CONFIG_FILES([Makefile])
AC_OUTPUT
