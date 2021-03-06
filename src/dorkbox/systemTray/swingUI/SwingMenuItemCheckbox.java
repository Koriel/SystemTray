/*
 * Copyright 2014 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dorkbox.systemTray.swingUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import dorkbox.systemTray.Checkbox;
import dorkbox.systemTray.Entry;
import dorkbox.systemTray.SystemTray;
import dorkbox.systemTray.peer.CheckboxPeer;
import dorkbox.systemTray.util.ImageUtils;
import dorkbox.util.SwingUtil;

class SwingMenuItemCheckbox implements CheckboxPeer {

    private final SwingMenu parent;
    private final JMenuItem _native = new JMenuItem();

    // these have to be volatile, because they can be changed from any thread
    private volatile ActionListener callback;
    private volatile boolean isChecked = false;

    private static ImageIcon checkedIcon;
    private static ImageIcon uncheckedIcon;

    // this is ALWAYS called on the EDT.
    SwingMenuItemCheckbox(final SwingMenu parent, final Entry entry) {
        this.parent = parent;

        if (SystemTray.SWING_UI != null) {
            _native.setUI(SystemTray.SWING_UI.getItemUI(_native, entry));
        }

        parent._native.add(_native);

        if (checkedIcon == null) {
            // from Brankic1979, public domain
            File checkedFile = ImageUtils.resizeAndCache(ImageUtils.ENTRY_SIZE, ImageUtils.class.getResource("checked_32.png"));
            checkedIcon = new ImageIcon(checkedFile.getAbsolutePath());

            File uncheckedFile = ImageUtils.getTransparentImage(ImageUtils.ENTRY_SIZE);
            uncheckedIcon = new ImageIcon(uncheckedFile.getAbsolutePath());
        }

        _native.setIcon(uncheckedIcon);
    }

    @Override
    public
    void setEnabled(final Checkbox menuItem) {
        SwingUtil.invokeLater(new Runnable() {
            @Override
            public
            void run() {
                _native.setEnabled(menuItem.getEnabled());
            }
        });
    }

    @Override
    public
    void setText(final Checkbox menuItem) {
        SwingUtil.invokeLater(new Runnable() {
            @Override
            public
            void run() {
                _native.setText(menuItem.getText());
            }
        });
    }

    @SuppressWarnings("Duplicates")
    @Override
    public
    void setCallback(final Checkbox menuItem) {
        if (callback != null) {
            _native.removeActionListener(callback);
        }

        callback = menuItem.getCallback(); // can be set to null

        if (callback != null) {
            callback = new ActionListener() {
                @Override
                public
                void actionPerformed(ActionEvent e) {
                    // this will run on the EDT, since we are calling it from the EDT
                    menuItem.setChecked(!isChecked);

                    // we want it to run on the EDT, but with our own action event info (so it is consistent across all platforms)
                    ActionListener cb = menuItem.getCallback();
                    if (cb != null) {
                        try {
                            cb.actionPerformed(new ActionEvent(menuItem, ActionEvent.ACTION_PERFORMED, ""));
                        } catch (Throwable throwable) {
                            SystemTray.logger.error("Error calling menu entry {} click event.", menuItem.getText(), throwable);
                        }
                    }
                }
            };

            _native.addActionListener(callback);
        }
    }

    @Override
    public
    void setShortcut(final Checkbox menuItem) {
        char shortcut = menuItem.getShortcut();
        // yikes...
        final int vKey = SwingUtil.getVirtualKey(shortcut);

        SwingUtil.invokeLater(new Runnable() {
            @Override
            public
            void run() {
                _native.setMnemonic(vKey);
            }
        });
    }

    @Override
    public
    void setChecked(final Checkbox menuItem) {
        boolean checked = menuItem.getChecked();

        // only dispatch if it's actually different
        if (checked != this.isChecked) {
            this.isChecked = checked;

            SwingUtil.invokeLater(new Runnable() {
                @Override
                public
                void run() {
                    if (isChecked) {
                        _native.setIcon(checkedIcon);
                    }
                    else {
                        _native.setIcon(uncheckedIcon);
                    }
                }
            });
        }
    }

    @Override
    public
    void remove() {
        //noinspection Duplicates
        SwingUtil.invokeLater(new Runnable() {
            @Override
            public
            void run() {
                if (callback != null) {
                    _native.removeActionListener(callback);
                    callback = null;
                }

                parent._native.remove(_native);
                _native.removeAll();
            }
        });
    }
}
