#!/bin/sh

userresources=$HOME/.Xresources
usermodmap=$HOME/.Xmodmap
sysresources=/usr/lib/X11/xinit/.Xresources
sysmodmap=/usr/lib/X11/xinit/.Xmodmap

# Merge in defaults and keymaps

if [ -f $sysresources ]; then
    /usr/bin/xrdb -merge $sysresources
fi

if [ -f $sysmodmap ]; then
    /usr/bin/xmodmap $sysmodmap
fi

if [ -f $userresources ]; then
    /usr/bin/xrdb -merge $userresources
fi

if [ -f $usermodmap ]; then
    /usr/bin/xmodmap $usermodmap
fi

# Determine current resolution of the first screen
screensize=$(xrandr | grep '*' | sed -r 's|[^0-9]*([0-9]*x[0-9]*).*|\1|' | head -n 1 | tr 'x' ',')

exec /usr/bin/chromium-browser --no-first-run --start-maximized --start-fullscreen --window-position=0,0 --window-size=$screensize "http://localhost:8080/faces/pages/workflow-local/WF_L_0.xhtml"

