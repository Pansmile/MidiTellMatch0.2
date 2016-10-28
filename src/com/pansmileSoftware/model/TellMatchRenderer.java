package com.pansmileSoftware.model;

import com.pansmileSoftware.controller.MidiTellMatch;

import javax.swing.*;
import java.awt.*;

/**
 * This is a renderer for a JList of <code>MidiTellMatch</code>es.
 */
public class TellMatchRenderer extends JLabel implements ListCellRenderer<MidiTellMatch> {
    @Override
    public Component getListCellRendererComponent(JList<? extends MidiTellMatch> list, MidiTellMatch value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        setText(value.getName());
        setFont(Font.decode("Helvetica Neue"));

        Color background;
        Color foreground;

        if (isSelected) {
            background = Color.BLUE;
            foreground = Color.WHITE;
        } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        }
        setBackground(background);
        setForeground(foreground);
        return this;
    }
}
