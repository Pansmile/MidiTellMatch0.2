package com.pansmileSoftware.controller;


import com.pansmileSoftware.model.MonitorTableModel;

import javax.sound.midi.*;

/**
 * Each time a message is arriving from transmitter which is connected to an instance of this class,
 * it constructs new MessageInfo object and delegate it to the MIDI-monitor through it's table controller.
 * @see MessageInfo
 * @see SpyTransmitter
 * @see MidiMonitor
 * @see com.pansmileSoftware.view.MonitorView
 */
public class SpyReceiver implements Receiver {
    private byte radix = 16;
    private MidiDevice sourceOrDest;
    private String deviceAlignment;
    private MonitorTableModel model;
    private boolean showMessageData;
    private boolean showMessageInfo;
    private boolean isDestReceiver;

    /**
     * The constructor of a SpyReceiver instance
     * @param device  a MidiDevice to spy on.
     * @param model  a controller of monitor's table.
     * @param isDestReceiver  a boolean that shows is this receiver connected to a destination.
     */
    public SpyReceiver(MidiDevice device, MonitorTableModel model, boolean isDestReceiver) {
        this.model = model;
        this.sourceOrDest = device;
        this.isDestReceiver = isDestReceiver;
        //Building the sourceOrDest's name for making MessageInfo instances in send() method.
        StringBuilder nameBuilder = new StringBuilder();
        String parentDeviceName = sourceOrDest.getDeviceInfo().getName();
        String parentDeviceDescription = sourceOrDest.getDeviceInfo().getDescription();
        //Checking if sourceOrDest's name and description are completing or duplicating each other
        // or if one of them isEmpty.
        if (!parentDeviceName.isEmpty() && !parentDeviceDescription.isEmpty()
                && !parentDeviceName.contains(parentDeviceDescription)
                && !parentDeviceDescription.contains(parentDeviceName)
                && !parentDeviceDescription.contains("No details available")) {
            nameBuilder.append(parentDeviceName).append(" ").append(parentDeviceDescription);
        } else if (!parentDeviceName.isEmpty()) {
            nameBuilder.append(parentDeviceName);
        } else if (!parentDeviceDescription.isEmpty()) {
            nameBuilder.append(parentDeviceDescription);
        } else {
            nameBuilder.append("Unknown device");
        }
        //Deleting "CoreMIDI4J - " prefix from sourceOrDest's name.
        if (String.valueOf(nameBuilder).contains("CoreMIDI4J")) {
            nameBuilder.delete(0, nameBuilder.indexOf("-") + 2);
        }

        //Defining if sourceOrDest is a source or dest, than modifying the deviceAlignment string
        // with "From" or "To" prefix.
        if (sourceOrDest.getMaxReceivers() == 0) {
            deviceAlignment = "From " + String.valueOf(nameBuilder);
        } else if (sourceOrDest.getMaxTransmitters() == 0) {
            deviceAlignment = "To " + String.valueOf(nameBuilder);
        } else {
            deviceAlignment = String.valueOf(nameBuilder);
        }
        //Show message data and message detailed description in messageInfo.
        showMessageData = true;
        showMessageInfo = true;
    }
    /**
     * Constructs new {@link MessageInfo} using received message and additional known parameters from current instance
     * of SpyReceiver and sends it to the controller.
     * @param message  a MidiMessage to construct an info
     * @param latencyStamp  the count of microseconds, which the controller has to wait for before adding the messageInfo
     * to the table if receiver is connected to a destination.
     */
    @Override
    public void send(MidiMessage message, long latencyStamp) {
        MessageInfo info = new MessageInfo(message, deviceAlignment, radix, showMessageData, showMessageInfo);
        if (!isDestReceiver) {
            model.add(info);
        } else if (latencyStamp<= 0) {
            model.addWithLatencyStamp(info, 0);
        } else {
            model.addWithLatencyStamp(info, latencyStamp);
        }
    }
    //We don't really need this.
    @Override
    public void close() {}

    /**
     * Sets the radix of numeric system that will be used while constructing MessageInfo instances in send() method.
     * @param radix  a radix to set
     */
    public void setRadix(byte radix) {
        this.radix = radix;
    }

    /**
     * Defines if the data of incoming messages will be used while constructing MessageInfo in send() method
     * @param showMessageData  true if we need to use message's data.
     */
    public void setShowMessageData(boolean showMessageData) {
        this.showMessageData = showMessageData;
    }

    /**
     * Defines if the detailed description of incoming messages will be used while constructing
     * MessageInfo in send() method.
     * @param showMessageInfo  true if we need to use detailed descriptions.
     */
    public void setShowMessageInfo(boolean showMessageInfo) {
        this.showMessageInfo = showMessageInfo;
    }

    /**
     * Returns the sourceOrDest of current SpyReceiver
     * @return a device which is connected current SpyReceiver.
     */
    public MidiDevice getSourceOrDest() {
        return sourceOrDest;
    }

    /**
     * Returns true if this receiver is associated with a destination.
     * @return <code>true</code> if this receiver is associated with a destination.
     */
    public boolean isDestReceiver() {
        return isDestReceiver;
    }
}

