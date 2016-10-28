package com.pansmileSoftware.model;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * This class renders presets in JComboBox.
 */
public class PresetRenderer extends JLabel implements ListCellRenderer<File> {
    @Override
    public Component getListCellRendererComponent(JList<? extends File> list, File value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        this.setText(value.getName().substring(0,value.getName().indexOf(".pst")));
        this.setSize(10,50);

        Color background;
        Color foreground;

        if (isSelected) {
            background = Color.WHITE;
            foreground = Color.BLUE;

        } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        }
        setBackground(background);
        setForeground(foreground);

        return this;
    }
}
