package com.pansmileSoftware.controller;

import javax.sound.midi.*;

/**This is the enum that contains all possible Midi-message's status bytes. It used to define the type of a message.
 * Also here is some methods for working with status-bytes.
 * @see MidiMessage
 * @see ShortMessage
 * @see SysexMessage
 * @see MetaMessage
 * */
public enum StatusByte {
    NOTE_ON(ShortMessage.NOTE_ON, "Note On"),
    NOTE_OFF(ShortMessage.NOTE_OFF, "Note Off"),
    PROGRAM_CHANGE(ShortMessage.PROGRAM_CHANGE, "Program Change"),
    CONTROL_CHANGE(ShortMessage.CONTROL_CHANGE, "Control Change"),
    POLY_PRESSURE(ShortMessage.POLY_PRESSURE, "Poly Pressure"),
    CHANNEL_PRESSURE(ShortMessage.CHANNEL_PRESSURE, "Channel Pressure"),
    PITCH_BEND(ShortMessage.PITCH_BEND, "Pitch Bend"),
    ACTIVE_SENSING(ShortMessage.ACTIVE_SENSING, "Active Sensing"),
    SONG_POSITION_POINTER(ShortMessage.SONG_POSITION_POINTER, "Song Position Pointer"),
    SONG_SELECT(ShortMessage.SONG_SELECT, "Song Select"),
    START(ShortMessage.START, "Start"),
    STOP(ShortMessage.STOP, "Stop"),
    CONTINUE(ShortMessage.CONTINUE, "Continue"),
    MIDI_TIME_CODE(ShortMessage.MIDI_TIME_CODE, "MIDI Time-Code"),
    TIMING_CLOCK(ShortMessage.TIMING_CLOCK, "Timing Clock"),
    TUNE_REQUEST(ShortMessage.TUNE_REQUEST, "Tune Request"),
    SYSTEM_RESET(ShortMessage.SYSTEM_RESET, "System Reset"),
    END_OF_EXCLUSIVE(ShortMessage.END_OF_EXCLUSIVE, "End of Exclusive"),
    SYSTEM_EXCLUSIVE(SysexMessage.SYSTEM_EXCLUSIVE, "SysEx"),
    SPECIAL_SYSTEM_EXCLUSIVE(SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE, "SysEx Special"),
    META(MetaMessage.META, "Meta"),
    WRONG(0,"Incorrect Data");

    /**A <code>String</code> representation of a status byte.*/
    private String commandType;
    /**An <code>int</code> representation of a status byte.*/
    private int statusByte;
    /**An <code>Array</code> that contains all possible <code>StatusByte</code>s except <code>StatusByte.WRONG</code>.*/
    private static StatusByte[] correctValues;
    /**An <code>Array</code> that contains all <code>StatusByte</code>s possible for <code>ShortMessage</code>s.*/
    private static StatusByte[] shortList;

    /**Constructs a <code>StatusByte</code>
     * @param statusByte  an <code>int</code> representation of a status byte.
     * @param commandType  A <code>String</code> representation of a status byte.
     */
    StatusByte(int statusByte, String commandType) {
        this.commandType = commandType;
        this.statusByte = statusByte;
    }

    /**Returns the <code>String</code> representation of a <code>StatusByte</code>.
     * @return a <code>String</code> representation of a <code>StatusByte</code>.
     */
    public String getCommandType() {
        return commandType;
    }

    /**Returns the default <code>int</code> representation of a <code>StatusByte</code>
     * @return an int representation of <code>StatusByte</code>
     */
    public int getStatusByte() {
        return statusByte;
    }

    /**Returns the <code>int</code> representation of a <code>StatusByte</code> according to MIDI-channel.
     * @param midiChannel  the MIDI-channel.
     * @return an int representation of <code>StatusByte</code>
     */
    public int getStatusByte(byte midiChannel) {
        return statusByte + (midiChannel - 1);
    }

    /**Creates the <code>String</code> that tells how a <code>StatusByte</code>s <code>int</code> representation
     * will appear according to numeric system and MIDI-channel.
     * @param radix  the radix of a numeric system.
     * @param midiChannel  the MIDI-channel
     * @return a <code>String</code> that tells how a <code>StatusByte</code>s <code>int</code> representation
     * will appear according to numeric system and MIDI-channel.
     */
    public String getStatusString(byte radix, byte midiChannel) {
        String status = "";
        if (radix == 10) {
            status += getStatusByte(midiChannel);
        } else if (radix == 16) {
            status = Long.toHexString((long) getStatusByte(midiChannel));
        }
        return status;
    }

    /**Defines which <code>StatusByte</code> matches received data.
     * @param data  an <code>int</code> which is the data of a <code>MidiMessage</code>.
     * @param midiChannel  a <code>byte</code> which is the MIDI-channel of a <code>MidiMessage</code>
     * @return a <code>StatusByte</code> which matches received data or <code>StatusByte.WRONG</code>
     * if no matches fond.
     */
    public static StatusByte define(int data, byte midiChannel) {
        StatusByte result = WRONG;
        int k = 1;
        if (midiChannel == 0) {
            k = 0;
        }
        for (StatusByte status : getCorrectValues()) {
            if (status.getStatusByte() == data - (midiChannel - k) ) {
                result = status;
                break;
            }
            if (result == WRONG) {
                for (byte i = 0; i < 16; i ++) {
                    if (data - i == status.getStatusByte()) {
                        result = status;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**Defines which <code>StatusByte</code> matches the received message.
     * @param message  a <code>MidiMessage</code> to find matches.
     * @return a <code>StatusByte</code> that matches the received message
     * or <code>StatusByte.WRONG</code> if no matches found.
     */
    public static StatusByte getMessageType(MidiMessage message) {
        int data = message.getStatus();
        for (StatusByte possibleStatus : getCorrectValues()) {
            for (int i = 0; i < 16; i++) {
                if (data - i == possibleStatus.statusByte) {
                    return possibleStatus;
                }
            }
        }
        return WRONG;
    }

    /**Returns an <code>Array</code> that contains all possible <code>StatusByte</code>s
     * except <code>StatusByte.WRONG</code>.
     * @return an <code>Array</code> that contains all possible <code>StatusByte</code>s
     * except <code>StatusByte.WRONG</code>.*/
    public static StatusByte[] getCorrectValues() {
        if (correctValues == null) {
            correctValues = new StatusByte[StatusByte.values().length - 1];
            for (int i = 0; i < (StatusByte.values().length - 1); i++) {
                correctValues[i] = StatusByte.values()[i];
            }
        }
        return correctValues;
    }
    /**Returns an <code>Array</code> that contains all <code>StatusByte</code>s
     *  possible for <code>ShortMessage</code>s.
     *  @return an <code>Array</code> that contains all <code>StatusByte</code>s
     *  possible for <code>ShortMessage</code>s.*/
    public static StatusByte[] getShortList() {
        if (shortList == null) {
            shortList = new StatusByte[] {
                    StatusByte.NOTE_ON,
                    StatusByte.NOTE_OFF,
                    StatusByte.CONTROL_CHANGE,
                    StatusByte.PROGRAM_CHANGE,
                    StatusByte.ACTIVE_SENSING,
                    StatusByte.CHANNEL_PRESSURE,
                    StatusByte.CONTINUE,
                    StatusByte.END_OF_EXCLUSIVE,
                    StatusByte.MIDI_TIME_CODE,
                    StatusByte.PITCH_BEND,
                    StatusByte.POLY_PRESSURE,
                    StatusByte.SONG_POSITION_POINTER,
                    StatusByte.SONG_SELECT,
                    StatusByte.START,
                    StatusByte.STOP,
                    StatusByte.SYSTEM_RESET,
                    StatusByte.TIMING_CLOCK,
                    StatusByte.TUNE_REQUEST
            };
        }
        return shortList;
    }
}
