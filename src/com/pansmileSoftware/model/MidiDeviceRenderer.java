package com.pansmileSoftware.model;


import javax.sound.midi.MidiDevice;
import javax.swing.*;
import java.awt.*;

/**
 * This class renders the MIDI-devices in sources and destinations lists.
 */
public class MidiDeviceRenderer extends JLabel implements ListCellRenderer<MidiDevice> {
    @Override
    public Component getListCellRendererComponent(final JList<? extends MidiDevice> list, MidiDevice value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        //Sometimes the behaviour of getName() and getDescription() can be unpredictable.
        //Here i'm trying to prevent getting large names with repeated information
        StringBuilder nameBuilder = new StringBuilder();
        String parentDeviceName = value.getDeviceInfo().getName();
        String parentDeviceDescription = value.getDeviceInfo().getDescription();
        if (!parentDeviceName.contains(parentDeviceDescription) && !parentDeviceDescription.contains(parentDeviceName)
                && !parentDeviceDescription.contains("No details available")) {
            nameBuilder.append(parentDeviceName).append(" ").append(parentDeviceDescription);
            //Also getName() or getDescription could be empty. We need to get at leas one of them.
        } else if (!parentDeviceName.isEmpty()) {
            nameBuilder.append(parentDeviceName);
        } else if (!parentDeviceDescription.isEmpty()) {
            nameBuilder.append(parentDeviceDescription);
        }
        //We don't need to see "CoreMIDI4J-" annotation in each device's name, cause we are
        //using only CoreMidi4J devices in case if required library is installed.
        if (String.valueOf(nameBuilder).contains("CoreMIDI4J")) {
            nameBuilder.delete(0, nameBuilder.indexOf("-") + 2);
        }
        this.setText(String.valueOf(nameBuilder));

        this.setSize(10,50);
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
