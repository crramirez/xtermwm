Name:           xtermwm
Version:        0.0.1
Release:        1%{?dist}
Summary:        Xterm Window Manager

Group:          User Interface/Desktops
License:        MIT
URL:            http://xtermwm.sourceforge.net/
Source0:        https://downloads.sourceforge.net/project/xtermwm/xtermwm/1.0beta/xtermwm-1.0beta.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

Requires:  java-devel
BuildRequires:  ant java-devel gcc desktop-file-utils


%description
Xterm Window Manager is a desktop environment / window manager for the
console.  Major features include:
    * The ability to run in both Xterm-like environments (Unix command
      line shells or over ssh) and as a Swing component for
      X11/Windows/Mac, with the same behavior and look-and-feel.
    * Image support, for both Swing and Xterm.  When running under
      Xterm, images are rendered as sixel graphic sequences.
    * A text terminal window / shell with good Xterm/VT100 support,
      including mouse.  Terminal windows will also resize correctly if
      using 'ptypipe'.
    * Support for the raw Linux console.  GPM mouse support will work
      when run inside 'lcxterm' or 'qodem'.


%prep
%setup -q


%build
%configure
make %{?_smp_mflags}


%install
rm -rf $RPM_BUILD_ROOT
make install DESTDIR=$RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT%{_mandir}/man1
cp docs/xtwm.1 lib/ptypipe/ptypipe.1 $RPM_BUILD_ROOT%{_mandir}/man1/
mkdir -p %{buildroot}%{_datadir}/java
cp java/build/jar/xtwm.jar $RPM_BUILD_ROOT%{_datadir}/java/
# Create desktop file
mkdir -p %{buildroot}%{_datadir}/applications
cat > %{buildroot}%{_datadir}/applications/%{name}.desktop << EOL
[Desktop Entry]
Type=Application
Version=1.0
Name=xtwm
Comment=%{summary}
Exec=%{name}
Icon=%{name}
Terminal=false
Categories=Development;Java;
EOL

# Install icons
mkdir -p %{buildroot}%{_datadir}/icons/hicolor/{64x64,512x512}
install -pDm 0644 build/icons/xtwm_logo_64.png \
                 %{buildroot}%{_datadir}/icons/hicolor/64x64/apps/%{name}.png
install -pDm 0644 build/icons/xtwm_logo_128.png \
                 %{buildroot}%{_datadir}/icons/hicolor/128x128/apps/%{name}.png

desktop-file-validate %{buildroot}%{_datadir}/applications/%{name}.desktop


%post
/bin/touch --no-create %{_datadir}/icons/hicolor &>/dev/null || :


%postun
if [ $1 -eq 0 ] ; then
    /bin/touch --no-create %{_datadir}/icons/hicolor &>/dev/null
    /usr/bin/gtk-update-icon-cache %{_datadir}/icons/hicolor &>/dev/null || :
fi


%posttrans
/usr/bin/gtk-update-icon-cache %{_datadir}/icons/hicolor &>/dev/null || :


%clean
rm -rf $RPM_BUILD_ROOT


%files
%defattr(-,root,root,-)
%{_bindir}/xtwm
%{_bindir}/ptypipe
%{_mandir}/man1/xtwm.1.gz
%{_mandir}/man1/ptypipe.1.gz
%{_datadir}/applications/%{name}.desktop
%{_datadir}/icons/hicolor/*/apps/%{name}.png
%{_datadir}/java/xtwm.jar
%doc ChangeLog LICENSE README.md


%changelog
* Sat Nov 30 2019 Kevin Lamonte <lamonte at, users.sourceforge.net> - 0.0.1-1
- Initial package creation
