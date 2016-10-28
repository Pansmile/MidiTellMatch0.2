package com.pansmileSoftware.controller;

import javax.sound.midi.MidiMessage;
import java.io.Serializable;

/**
 * This class helps us to get a String which represents the data of received MIDI-message and the channel
 * which this message is using.
 * Note that channels numeration starts from 1 as in usual midi-apps.

 */
public class MessageDecoder implements Serializable {

    /**
     * Decodes received <code>MidiMessage</code> to <code>String</code> in a numeric system using received radix
     * @param message  a <code>MidiMessage</code> to decode.
     * @param radix  an <code>int</code> which is the radix of required numeric system.
     */
    public static String decode(MidiMessage message, int radix) {
        StringBuilder decodedMessage = new StringBuilder();
        int statusByte = message.getStatus();
        byte[] data = message.getMessage();

        for (int i = 0; i < data.length; i++) {
            long aByte = data[i];
            if (i == 0)  {
                aByte += 256;
            } else if ((i == data.length - 1 && statusByte == 0xf0)) {
                aByte += 256;
            }
            decodedMessage.append((radix == 16 ? Long.toHexString(aByte) : aByte)).append(" ");
        }
        return String.valueOf(decodedMessage);
    }

    /**
     * Decodes received <code>StatusByte</code> in order to get it's message's channel.
     * Channels numeration starts from 1 as in usual midi-apps.
     * @param statusByte  a <code>StatusByte</code> to decode.
     * @return  a <code>byte</code> which is the midi channel. If failed to decode, returns 0.
     */
    public static byte deCodeChannel(int statusByte) {
        for (StatusByte possibleStatus : StatusByte.getCorrectValues()) {
            for (byte i = 1; i < 17; i++) {
                int toParse  = possibleStatus.getStatusByte() ;
                int toEqual = statusByte + 1 - i;
                if (toEqual == toParse) {
                    return i;
                }
            }
        }
        return 0;
    }
}

