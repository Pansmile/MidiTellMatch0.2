package com.pansmileSoftware.controller;

import javax.sound.midi.*;
/**
 * This static class helps us to encode a String to a MIDI-message.
 */
public class MessageEncoder {
    /**
     * Encodes received <code>String</code> to a <code>MidiMessage</code> using received data and returns this message.
     * @param toEncode  a <code>String</code> to encode.
     * @param radix  a <code>byte</code> which is the radix of a
     * numeric system used in the received <code>String</code>.
     * @param statusByte  a <code>StatusByte</code> which is the type of the message that we need to construct.
     * @param midiChannel  a <code>byte</code> which is the midi-channel of the message that we need to construct.
     * @return a <code>MidiMessage</code> constructed using received data.
     * @throws InvalidMidiDataException in case if encoding is failed because of wrong received data.
     */
    public static MidiMessage encode(String toEncode, byte radix, StatusByte statusByte, byte midiChannel) throws InvalidMidiDataException {
        String[] sBytes = toEncode.split(" ");
        byte[] data = new byte[sBytes.length - 1];

        if (statusByte == null) {
            statusByte = encodeStatus(toEncode, radix, midiChannel);
        }
        try {
            for (int i = 0; i < data.length; i++) {
                int parsed = Integer.parseInt(sBytes[i + 1],radix);
                data[i] = (byte) parsed;
            }
        } catch (NumberFormatException e) {
            throw  new InvalidMidiDataException("\nInvalid Data");
        }

        MidiMessage encoded;

        if (statusByte.equals(StatusByte.SYSTEM_EXCLUSIVE) || statusByte.equals(StatusByte.SPECIAL_SYSTEM_EXCLUSIVE)) {
            encoded = new SysexMessage(statusByte.getStatusByte(), data, data.length);
        } else if (statusByte.equals(StatusByte.META)) {
            encoded = new MetaMessage(statusByte.getStatusByte(), data, data.length);
        } else {
            if (midiChannel != 0) {
                if (data.length > 2) {
                    throw  new InvalidMidiDataException("\nInvalid Data: \n " +
                            "A Short Message could not contain more than two data bytes");
                } else {
                    if (data.length == 2) {
                        encoded = new ShortMessage(statusByte.getStatusByte(midiChannel), data[0], data[1]);
                    } else if (data.length == 1) {
                        encoded = new ShortMessage(statusByte.getStatusByte(midiChannel), data[0], 0);
                    } else {
                        throw new InvalidMidiDataException("\nInvalid Data: \n " +
                                "A Short Message should contain at least one data byte");
                    }
                }
            } else {
                encoded = shortEncode(statusByte, data);
            }
        }
        return encoded;
    }

    /**
     * Returns the type of a <code>MidiMessage</code> which could be encoded from received <code>String</code>.
     * @param toEncode  a <code>String</code> to encode.
     * @param radix  a <code>byte</code> which is the radix of a
     * numeric system used in the received <code>String</code>.
     * @param midiChannel  a <code>byte</code> which is the midi-channel of the message which could be encoded
     * from received <code>String</code>.
     * @return  a <code>StatusByte</code> which is the type of a <code>MidiMessage</code>
     * which could be encoded from received <code>String</code>.
     */
    public static StatusByte encodeStatus(String toEncode, byte radix, byte midiChannel) {
        String[] sBytes = toEncode.split(" ");
        try {
            int parsedStatus = Integer.parseInt(sBytes[0],radix);
            return StatusByte.define(parsedStatus, midiChannel);
        } catch (NumberFormatException e) {
            return StatusByte.WRONG;
        }
    }

    /**
     * Encodes an array of bytes to a <code>ShortMessage</code>.
     * @param statusByte  a <code>StatusByte</code> which is the type of the message that we need to construct.
     * @param data  an array of bytes which is the data of the message that we need to construct.
     * @return <code>MidiMessage</code> constructed using received data.
     * @throws InvalidMidiDataException in case if data is incorrect and construction is failed.
     */
    private static MidiMessage shortEncode(StatusByte statusByte, byte[] data) throws InvalidMidiDataException {
        MidiMessage encoded = new ShortMessage(statusByte.getStatusByte(), data[0], data[1]);
        if (encoded !=null) {
            return encoded;
        }
        else throw new InvalidMidiDataException("Fail to encode");
    }

    /**
     * Encodes the midi-channel of a <code>MidiMessage</code> which could be encoded from received <code>String</code>.
     * In case if data is incorrect returns -1.
     * @param toEncode  a <code>String</code> to encode.
     * @param radix a <code>byte</code> which is the radix of a
     * numeric system used in the received <code>String</code>.
     * @return  a <code>byte</code> which is the midi-channel of a <code>MidiMessage</code> which could be encoded from
     * received <code>String</code>. In case if data is incorrect returns -1.
     */
    public static byte encodeChannel(String toEncode, byte radix) {
        int splitIndex = toEncode.indexOf(" ");
        if (splitIndex != -1) {
            String firstByte = toEncode.substring(0, toEncode.indexOf(" "));
            for (StatusByte possibleStatus : StatusByte.getCorrectValues()) {
                for (byte i = 1; i < 17; i++) {
                    if (firstByte.equals(possibleStatus.getStatusString(radix,i))) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}
