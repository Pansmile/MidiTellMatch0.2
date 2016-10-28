package com.pansmileSoftware.controller;

import javax.sound.midi.MidiUnavailableException;

/**
 * This class provides the access to the list of output MIDI devices and apps, which are available in the system.
 * It also provides a way to refresh itself and the MidiSystemInfo in order to check if any devices was added
 * or deleted from the system.
 * This class is a singleton.
 * @see MidiDeviceList
 * @see InputDevicesList
 */
public class OutputDevicesList extends MidiDeviceList {
    /**
     * The only possible instance of this class.
     */
    private static OutputDevicesList outputDevices;

    /**
     * Constructs new <code>OutputDeviceList</code>.
     * @see MidiDeviceList#initialize(String).
     */
    private OutputDevicesList() {
        //Trying to use CoreMidi4j.
        if (MidiDeviceList.isCoreMidiLoaded) {
            this.addAll(initialize("MidiDestination"));
        } else {
            this.addAll(initialize("MidiOut"));
        }
    }

    /**
     * Returns existing or new <code>OutputDeviceList</code>.
     * @return  the <code>OutputDeviceList</code.>
     */
    public static  OutputDevicesList getOutputDevices() {
        if (outputDevices == null) {
            outputDevices = new OutputDevicesList();
        }
        return outputDevices;
    }

    /**
     * Refreshes the state of MidiSystemInfo and reinitialise current list
     * @throws MidiUnavailableException if any of destination is unavailable.
     */
    public void refreshOutputDevices() throws MidiUnavailableException {
        if (MidiDeviceList.isCoreMidiLoaded) {
            reInitialize("MidiDestination");
        } else {
            reInitialize("MidiOut");
        }
    }
}