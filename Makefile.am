bin_PROGRAMS =
bin_SCRIPTS = bin/xtwm
man_MANS = docs/xtwm.1

SUBDIRS = @subdirs@ .

EXTRA_DIST = \
@srcdir@/build \
@srcdir@/bin/xtwm \
@srcdir@/docs/xtwm.1 \
@srcdir@/ChangeLog \
@srcdir@/LICENSE \
@srcdir@/README.md \
@srcdir@/lib/ptypipe \
@srcdir@/java/build.xml \
@srcdir@/java/Makefile \
@srcdir@/java/resources \
@srcdir@/java/src

install-data-hook:
	-mkdir -p -m 755 $(datadir)/java
	-$(install_sh) -c -m 644 @srcdir@/java/build/jar/xtwm.jar $(datadir)/java

dist-hook:
	rm -rf `find $(distdir)/lib -type f -name "*.o"`
	rm -rf `find $(distdir)/lib/ptypipe/autom4te.cache`
	rm -rf `find $(distdir)/lib/ptypipe/Makefile`
	rm -rf `find $(distdir)/lib/ptypipe/config.log`
	rm -rf `find $(distdir)/lib/ptypipe/.deps`
	rm -f $(distdir)/lib/ptypipe/ptypipe

ACLOCAL_AMFLAGS =
