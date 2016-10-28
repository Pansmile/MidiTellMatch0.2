package com.pansmileSoftware.controller;

import javax.sound.midi.*;
import java.util.*;

/**
 This static class is used to construct complete information about a MIDI-message to represent it in MIDI-monitor.
 */
public class MessageInfoLibrarian {
    private static HashMap<String, ArrayList<Integer>> noteChart;
    private static String[] cCChart;
    private static String[] sysExProtocols;
    private static HashMap<Integer,HashMap<Integer,String>> sysExSubBytes;
    private static HashMap<Integer, String> showControlFormat;
    private static boolean isInitialized;

    /**
     * Initializes the MessageInfoLibrarian by populating it's fields with data.
     */
    public static void initialize() {

        //Populating the chart of notes with data.
        if (noteChart == null) {
            noteChart = new HashMap<>(12);
            String[] noteNames = new String[] {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
            for (int i = 0; i < 12; i++) {
                ArrayList<Integer> noteNumbers = new ArrayList<>();
                for (int j = i; j < 127; j+=12) {
                    noteNumbers.add(j);
                }
                noteChart.put(noteNames[i], noteNumbers);
            }
        }
        //Populating the array of CC types with required content.
        if (cCChart == null) {
            cCChart = new String[] {
                    "Bank Select (MSB)","Modulation Wheel (MSB)","Breath Controller (MSB)","Controller 3",
                    "Foot Controller (MSB)","Portamento Time (MSB)","Data Entry (MSB)","MainView Volume (MSB)",
                    "Balance (MSB)","Controller 9","Pan Position (MSB)","Expression (MSB)","Effect Control-1 (MSB)",
                    "Effect Control-2 (MSB)","Controller 14","Controller 15","Ribbon/GP Slider-1",
                    "Knob-1/GP Slider-2","GP Slider-3","Knob-2/GP Slider-4","Knob-3/Controller 20",
                    "Knob-4/Controller 21","Controller 22","Controller 23","Controller 24","Controller 25",
                    "Controller 26","Controller 27","Controller 28","Controller 29","Controller 30","Controller 31",
                    "Bank Select (LSB)","Modulation Wheel (LSB)","Breath controller (LSB)","Controller 35",
                    "Foot Pedal (LSB)","Portamento Time (LSB)","Data Entry (LSB)","Volume (LSB)","Balance (LSB)",
                    "Controller 41","Pan position (LSB)","Expression (LSB)","Effect Control 1 (LSB)",
                    "Effect Control 2 (LSB)","Controller 46","Controller 47","Ribbon/GP Slider-1 (LSB)",
                    "Knob-1/GP Slider-2 (LSB)","GP Slider-3 (LSB)","Knob-2/GP Slider-4 (LSB)",
                    "Knob-3/(LSB)/Controller 52","Knob-4 (LSB)/Controller 53","Controller 54","Controller 55",
                    "Controller 56","Controller 57","Controller 58","Controller 59","Controller 60","Controller 61",
                    "Controller 62","Controller 63","Hold Pedal (on/off)","Portamento (on/off)",
                    "Sustenuto Pedal (on/off)","Soft Pedal (on/off)","Legato Pedal (on/off)","Hold 2 Pedal (on/off)",
                    "Sound Variation","Resonance/Timbre","Sound Release Time","Sound Attack Time",
                    "Frequency Cutoff/Brightness","Decay Time","Vibrato Rate","Vibrato Depth","Vibrato Delay",
                    "Sound Control-10","Decay/GP Button-1 (on/off)","Hi-Pass Filter Frequency/GP Button-2 (on/off)",
                    "GP Button-3 (on/off)","GP Button-4 (on/off)","Controller 84","Controller 85","Controller 86",
                    "Controller 87","Controller 88","Controller 89","Controller 90","Reverb Send Level","Tremolo Depth",
                    "Chorus Send Level","Celeste Level/Detune","Phaser Depth","Data Button increment",
                    "Data Button decrement","Non-registered Parameter (LSB)","Non-registered Parameter (MSB)",
                    "Registered Parameter (LSB)","Registered Parameter (MSB)","Controller 102","Controller 103",
                    "Controller 104","Controller 105","Controller 106","Controller 107","Controller 108",
                    "Controller 109","Controller 110","Controller 111","Controller 112","Controller 113",
                    "Controller 114","Controller 115","Controller 116","Controller 117","Controller 118",
                    "Controller 119","All Sound Off","All Controllers Off","Local Keyboard (on/off)","All Notes Off",
                    "Omni Mode Off","Omni Mode On","Mono Operation","Poly Operation"
            };
        }
        //Populating the array of SysEx types with data.
        if (sysExProtocols == null) {
            sysExProtocols = new String[] {
                    "Unused","MIDI Time Code","MIDI Show Control","Notation Information","Device Control",
                    "Real Time MTC Cueing","MIDI Machine Control", "MIDI Machine Control Responses",
                    "MIDI Tuning Standard (Real Time)","Controller Destination Setting","Key-based Instrument Control",
                    "Scalable Polyphony MIDI MIP Message","Mobile Phone Control Message"
            };
        }
        //Populating a map of MIDI Show Control format types with data  .
        if (showControlFormat == null) {
            showControlFormat = new HashMap<>(57);
            showControlFormat.put(0x00, "reserved for extensions");
            showControlFormat.put(0x01, "Lighting");
            showControlFormat.put(0x02, "Moving Lights");
            showControlFormat.put(0x03, "Colour Changers");
            showControlFormat.put(0x04, "Strobes");
            showControlFormat.put(0x05, "Lasers");
            showControlFormat.put(0x06, "Chasers");
            showControlFormat.put(0x10, "Sound");
            showControlFormat.put(0x11, "Music");
            showControlFormat.put(0x12, "CD Players");
            showControlFormat.put(0x13, "EPROM Playback");
            showControlFormat.put(0x14, "Audio Tape Machines");
            showControlFormat.put(0x15, "Intercoms");
            showControlFormat.put(0x16, "Amplifiers");
            showControlFormat.put(0x17, "Audio Effects Devices");
            showControlFormat.put(0x18, "Equalisers");
            showControlFormat.put(0x20, "Machinery");
            showControlFormat.put(0x21, "Rigging");
            showControlFormat.put(0x22, "Flys");
            showControlFormat.put(0x23, "Lifts");
            showControlFormat.put(0x24, "Turntables");
            showControlFormat.put(0x25, "Trusses");
            showControlFormat.put(0x26, "Robots");
            showControlFormat.put(0x27, "Animation");
            showControlFormat.put(0x28, "Floats");
            showControlFormat.put(0x29, "Breakaways");
            showControlFormat.put(0x2A, "Barges");
            showControlFormat.put(0x30, "Video");
            showControlFormat.put(0x31, "Video Tape Machines");
            showControlFormat.put(0x32, "Video Cassette Machines");
            showControlFormat.put(0x33, "Video Disc Players");
            showControlFormat.put(0x34, "Video Switchers");
            showControlFormat.put(0x35, "Video Effects");
            showControlFormat.put(0x36, "Video Character Generators");
            showControlFormat.put(0x37, "Video Still Stores");
            showControlFormat.put(0x38, "Video Monitors");
            showControlFormat.put(0x40, "Projection");
            showControlFormat.put(0x41, "Film Projectors");
            showControlFormat.put(0x42, "Slide Projectors");
            showControlFormat.put(0x43, "Video Projectors");
            showControlFormat.put(0x44, "Dissolvers");
            showControlFormat.put(0x45, "Shutter Controls");
            showControlFormat.put(0x50, "Process Control");
            showControlFormat.put(0x51, "Hydraulic Oil");
            showControlFormat.put(0x52, "H20");
            showControlFormat.put(0x53, "CO2");
            showControlFormat.put(0x54, "Compressed Air");
            showControlFormat.put(0x55, "Natural Gas");
            showControlFormat.put(0x56, "Fog");
            showControlFormat.put(0x57, "Smoke");
            showControlFormat.put(0x58, "Cracked Haze");
            showControlFormat.put(0x60, "Pyro");
            showControlFormat.put(0x61, "Fireworks");
            showControlFormat.put(0x62, "Explosions");
            showControlFormat.put(0x63, "Flame");
            showControlFormat.put(0x64, "Smoke pots");
            showControlFormat.put(0x7F, "All-types");
        }
        //Populating a map of SysEx types and their specified commands with data.
        if (sysExSubBytes == null) {
            sysExSubBytes = new HashMap<>(8);

            HashMap<Integer, String> timeCodeTypes = new HashMap<>(2);
            timeCodeTypes.put(0x01,"FULL_MESSAGE");
            timeCodeTypes.put(0x02,"USER_BITS");
            sysExSubBytes.put(0x01,timeCodeTypes);

            HashMap<Integer, String> showControlCommands = new HashMap<>(27);
            showControlCommands.put(0x00, "Reserved for extensions");
            showControlCommands.put(0x01, "GO");
            showControlCommands.put(0x02, "STOP");
            showControlCommands.put(0x03, "RESUME");
            showControlCommands.put(0x04, "TIMED_GO");
            showControlCommands.put(0x05, "LOAD");
            showControlCommands.put(0x06, "SET");
            showControlCommands.put(0x07, "FIRE");
            showControlCommands.put(0x08, "ALL_OFF");
            showControlCommands.put(0x09, "RESTORE");
            showControlCommands.put(0x0A, "RESET");
            showControlCommands.put(0x0B, "GO_OFF");
            showControlCommands.put(0x10, "GO/JAM_CLOCK");
            showControlCommands.put(0x11, "STANDBY_+");
            showControlCommands.put(0x12, "STANDBY_-");
            showControlCommands.put(0x13, "SEQUENCE_+");
            showControlCommands.put(0x14, "SEQUENCE_-");
            showControlCommands.put(0x15, "START_CLOCK");
            showControlCommands.put(0x16, "STOP_CLOCK");
            showControlCommands.put(0x17, "ZERO_CLOCK");
            showControlCommands.put(0x18, "SET_CLOCK");
            showControlCommands.put(0x19, "MTC_CHASE_ON");
            showControlCommands.put(0x1A, "MTC_CHASE_OFF");
            showControlCommands.put(0x1B, "OPEN_CUE_LIST");
            showControlCommands.put(0x1C, "CLOSE_CUE_LIST");
            showControlCommands.put(0x1D, "OPEN_CUE_PATH");
            showControlCommands.put(0x1E, "CLOSE_CUE_PATH");
            sysExSubBytes.put(0x02, showControlCommands);

            HashMap<Integer, String> notationInformation = new HashMap<>(3);
            notationInformation.put(0x01,"BAR_NUMBER");
            notationInformation.put(0x02,"TIME_SIGNATURE (IMMEDIATE)");
            notationInformation.put(0x03,"Time Signature (DELAYED)");
            sysExSubBytes.put(0x03, notationInformation);

            HashMap<Integer ,String> deviceControlCommands = new HashMap<>(5);
            deviceControlCommands.put(0x01, "MASTER_VOLUME");
            deviceControlCommands.put(0x02, "MASTER_BALANCE");
            deviceControlCommands.put(0x03, "MASTER_FINE_TUNING");
            deviceControlCommands.put(0x04, "MASTER_COURSE_TUNING");
            deviceControlCommands.put(0x05, "GLOBAL_PARAMETER_CONTROL");
            sysExSubBytes.put(0x04, deviceControlCommands);

            HashMap<Integer, String> realTimeMTCCueingTypes = new HashMap<>(15);
            realTimeMTCCueingTypes.put(0x00, "SPECIAL");
            realTimeMTCCueingTypes.put(0x01, "PUNCH_IN_POINTS");
            realTimeMTCCueingTypes.put(0x02, "PUNCH_OUT_POINTS");
            realTimeMTCCueingTypes.put(0x03, "RESERVED");
            realTimeMTCCueingTypes.put(0x04, "RESERVED");
            realTimeMTCCueingTypes.put(0x05, "EVENT_START_POINTS");
            realTimeMTCCueingTypes.put(0x06, "EVENT_STOP_POINTS");
            realTimeMTCCueingTypes.put(0x07, "EVENT_START_POINTS (with additional info)");
            realTimeMTCCueingTypes.put(0x08, "EVENT_STOP_POINTS (with additional info");
            realTimeMTCCueingTypes.put(0x09, "RESERVED");
            realTimeMTCCueingTypes.put(0x0A, "RESERVED");
            realTimeMTCCueingTypes.put(0x0B, "CUE_POINTS");
            realTimeMTCCueingTypes.put(0x0C, "CUE_POINTS (with additional info)");
            realTimeMTCCueingTypes.put(0x0D, "RESERVED");
            realTimeMTCCueingTypes.put(0x0E, "EVENT_NAME (in additional info)");
            sysExSubBytes.put(0x05, realTimeMTCCueingTypes);

            HashMap<Integer, String> machineControlCommands = new HashMap<>(15);
            machineControlCommands.put(0x01, "STOP");
            machineControlCommands.put(0x02, "PLAY");
            machineControlCommands.put(0x03, "DEFERRED_PLAY");
            machineControlCommands.put(0x04, "FAST_FORWARD");
            machineControlCommands.put(0x05, "REWIND");
            machineControlCommands.put(0x06, "RECORD_STROBE (PUNCH_IN)");
            machineControlCommands.put(0x07, "RECORD_EXIT (PUNCH_OUT)");
            machineControlCommands.put(0x08, "RECORD_PAUSE");
            machineControlCommands.put(0x09, "PAUSE_PLAYBACK");
            machineControlCommands.put(0x0A, "EJECT");
            machineControlCommands.put(0x0B, "CHASE");
            machineControlCommands.put(0x0D, "MMC_RESET");
            machineControlCommands.put(0x40, "WRITE/RECORD_READY/ARM_TRACKS");
            machineControlCommands.put(0x44, "GOTO");
            machineControlCommands.put(0x47, "SHUTTLE");
            sysExSubBytes.put(0x06, machineControlCommands);

            HashMap<Integer,String> tuningStandardCommands = new HashMap<>(4);
            tuningStandardCommands.put(0x02, "SINGLE_NOTE_TUNING_CHANGE");
            tuningStandardCommands.put(0x07, "SINGLE_NOTE_TUNING_CHANGE_WITH_BANK_SELECT");
            tuningStandardCommands.put(0x08, "SCALE/OCTAVE_TUNING, 1_BYTE_FORMAT");
            tuningStandardCommands.put(0x09, "SCALE/OCTAVE_TUNING, 2_BYTE_FORMAT");
            sysExSubBytes.put(0x08,tuningStandardCommands);

            HashMap<Integer, String> controllerDestinationSettings = new HashMap<>(3);
            controllerDestinationSettings.put(0x01, "CHANNEL_PRESSURE (AFTERTOUCH)");
            controllerDestinationSettings.put(0x02, "POLYPHONIC_KEY_PRESSURE (AFTERTOUCH)");
            controllerDestinationSettings.put(0x03, "CONTROLLER (CONTROL_CHANGE)");
            sysExSubBytes.put(0x09, controllerDestinationSettings);
        }
        isInitialized = true;
    }

    /**
     * Gets the note name from a Note_On or Note_Off message's second byte.
     * @param value  an <code>int</code> which is the note number (from 0 to 127 that matches notes from C-2 to G8)
     * @return  a <Code>String</Code> which is the note name and octave number.
     */
    private static String getNote(int value) {
        for (Map.Entry<String,ArrayList<Integer>> note : noteChart.entrySet()) {
            int octave = note.getValue().indexOf(value) - 2;
            if (octave != -3) {
                return String.valueOf(note.getKey() + octave);
            }
        }
        return "Not defined";
    }

    /**
     * Gets the controller name from a Control_Change message's second byte.
     * @param value  an <code>int</code> which is a Control_Change message's second byte
     * @return a <code>String</code> which is the name of the controller.
     */
    private static String getCC(int value) {
        return cCChart[value];
    }

    /**
     * Gets the pitch-bend value from a Pitch_Bend message's data bytes.
     * @param message  a Pitch_Bend <code>MidiMessage</code> to get the dat from.
     * @return a <code>short</code> which is the pitch_bend wheel shift amount.
     */
    private static String getPitch(MidiMessage message) {
        short result;
        result = (short)Byte.toUnsignedInt(message.getMessage()[2]);
        result = (short)(Short.toUnsignedInt(result) << 7);
        result = (short)(Short.toUnsignedInt(result)
                | Short.toUnsignedInt((short)Byte.toUnsignedInt(message.getMessage()[1])));
        result -= 8192;
        return String.valueOf(result);
    }

    //Constructing info about a SysEx message.

    /**
     * Constructs information about received SysEx message.
     * @param message  a <code>MidiMessage</code> to construct information about.
     * @return a <code>String</code> which is the description of received message.
     * @throws ArrayIndexOutOfBoundsException in case if message is invalid.
     */
    private static String getSysExInfo(MidiMessage message) throws ArrayIndexOutOfBoundsException {
        //Getting the type of SysEx Message.
        int messageType = message.getMessage()[3];
        StringBuilder result = new StringBuilder(sysExProtocols[messageType]);
        //Getting the actual command data.
        int subByte = message.getMessage()[4];
        //If message is MIDI Show Control Message, fifth byte contains MSC format data
        //instead of command type.
        if (messageType == 0x02) {
            int showControlType = subByte;
            //The command type for MSC message lays in the sixth byte.
            subByte = message.getMessage()[5];
            for (Map.Entry<Integer, String> showControlCommand : showControlFormat.entrySet()) {
                if (showControlType == showControlCommand.getKey()) {
                    result.append(" - ").append(showControlCommand.getValue());
                    break;
                }
            }
        }
        //Getting the name of the command.
        for (Map.Entry<Integer, HashMap<Integer, String>> pair : sysExSubBytes.entrySet()) {
            if (messageType == pair.getKey()) {
                for (Map.Entry<Integer, String> command : pair.getValue().entrySet()) {
                    if (subByte == command.getKey()) {
                        result.append(" - ").append(command.getValue());
                        break;
                    }
                }
                break;
            }
        }
        //Constructing info for messages of unknown types.
        if (result.length() == 0) {
            result.append("Universal System Exclusive").append(" ").append(
                    message.getLength()).append(" bytes");
        }
        return String.valueOf(result);
    }

    /**
     * Defines the type of message to get the proper description and returns the description.
     * @param status  a <code>StatusByte</code> which is the type of the message.
     * @param message  a <code>MidiMessage</code> to get it's description.
     * @return a <code>String</code>  which is the received message's description.
     */
    private static String getInfo(StatusByte status, MidiMessage message) {
        switch (status) {
            case NOTE_ON:
                return getNote(message.getMessage()[1]) + " ON";
            case NOTE_OFF:
                return getNote(message.getMessage()[1]) + " OFF";
            case CONTROL_CHANGE:
                return getCC(message.getMessage()[1]);
            case PITCH_BEND:
                return getPitch(message);
            case SYSTEM_EXCLUSIVE:
                try {
                    return getSysExInfo(message);
                } catch (ArrayIndexOutOfBoundsException e) {
                    return "Unknown Manufacturer " + message.getLength() + " bytes";
                }
            case CHANNEL_PRESSURE:
                return "Aftertouch";
            case POLY_PRESSURE:
                return "Aftertouch";
            default:
                return "";
        }
    }

    //Constructing final info to represent in MIDI-monitor

    /**
     * Constructs message's full description according received data.
     * @param status  a <code>StatusByte</code> which is the type of the message.
     * @param message  a <code>MidiMessage</code> to get it's description.
     * @param radix  a <code>byte</code> which is the radix of a numeric system that will be used to represent
     * the message's data as a <code>String</code>.
     * @param showMessageData  a <code>boolean</code> which defines if the description will contain the message's data.
     * @param showMessageInfo  a <code>boolean</code> which defines if the description will contain some
     * detailed information about the message.
     * @return a <code>String</code>  which is the final description.
     */
    public static String getFullInfo(StatusByte status, MidiMessage message, byte radix,
                                     boolean showMessageData, boolean showMessageInfo)  {
        if (showMessageInfo) {
            String messageInfo = getInfo(status, message);
            return (messageInfo.isEmpty() ? (showMessageData ? MessageDecoder.decode(
                    message, radix).toUpperCase() : "")
                    : (showMessageData ? MessageDecoder.decode(message, radix).toUpperCase()
                    + " :  " + messageInfo : messageInfo));
        } else {
            return (showMessageData ? MessageDecoder.decode(message, radix) : "");
        }
    }

    /**
     * Returns true in case if the <code>MessageInfoLibrarian</code> is already initialised.
     * @return true in case if the <code>MessageInfoLibrarian</code> is already initialised.
     */
    public static boolean isIsInitialized() {
        return isInitialized;
    }

}
