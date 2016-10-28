package com.pansmileSoftware.controller;

import javax.sound.midi.MidiMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**This class provides detailed information about any MidiMessages caught by an instance of MidiMonitor.
 * Each time a message is caught a new instance of MessageInfo is constructing in one of MidiMonitor's SpyReceivers.
 * MessageInfo is used to populate {@link com.pansmileSoftware.view.MonitorView#monitorTable} with data each time
 * a MidiMessage is caught in {@link com.pansmileSoftware.view.MonitorView#monitor}.
 * @see MidiMonitor
 * @see SpyReceiver
 * @see SpyTransmitter
 * @see com.pansmileSoftware.view.MonitorView
 * @see MidiMessage
}*/
public class MessageInfo {
    private String time;
    private String deviceAlignment;
    private String type;
    private String channel;
    private String messageData;

    /**
     * Constructs new <code>MessageInfo</code> with received data.
     * @param message  a <code>MidiMessage</code> to construct info about.
     * @param deviceAlignment  a <code>String</code> which is the name of a <code>MidiDevice</code> from where or
     * to where this message was sent.
     * @param radix  a <code>byte</code> which is the radix of a numeric system which will be used to construct string
     * representation of the message.
     * @param showMessageData  a <code>boolean</code> which defines if the info will contain the message's data.
     * @param showMessageInfo  a <code>boolean</code> which defines if the info will contain the message's description.
     * @throws ArrayIndexOutOfBoundsException in case if received SysEx message is invalid.
     */
    public MessageInfo(MidiMessage message, String deviceAlignment, byte radix,
                       boolean showMessageData, boolean showMessageInfo) throws ArrayIndexOutOfBoundsException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        time = (LocalDateTime.now().format(formatter));
        this.deviceAlignment = deviceAlignment;
        StatusByte status = StatusByte.getMessageType(message);
        type = status.getCommandType();
        byte channelNumber = MessageDecoder.deCodeChannel(message.getStatus());
        channel = String.valueOf(channelNumber);
        this.messageData = MessageInfoLibrarian.getFullInfo(status, message , radix, showMessageData, showMessageInfo);

    }

    /**
     * Returns the midi-message's channel number.
     * @return a <code>String</code> which is the midi-message's channel number.
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Returns the name of <code>MidiDevice</code> from where or to where the message was sent.
     * @return a <code>String</code> which is the name of <code>MidiDevice</code>
     * from where or to where the message was sent.
     */
    public String getDeviceAlignment() {
        return deviceAlignment;
    }

    /**
     * Returns the message data.
     * @return a <code>String</code> which is the message data.
     */
    public String getMessage() {
        return messageData;
    }

    /**
     * Returns the time when the message arrived.
     * @return a <code>String</code> which is the time when the message arrived.
     */
    public String getTime() {
        return time;
    }

    /**
     * Resets the information about time when message arrived.
     */
    public void changeTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        time = (LocalDateTime.now().format(formatter));
    }

    /**
     * Returns the type of the message.
     * @return a <code>String</code> which is the type of the message.
     */
    public String getType() {
        return type;
    }
}
