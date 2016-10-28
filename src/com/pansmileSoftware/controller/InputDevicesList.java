package com.pansmileSoftware.controller;

import javax.sound.midi.MidiUnavailableException;

/**
 * This class provides the access to the list of input MIDI devices and apps, which are available in the system.
 * It also provides a way to refresh itself and th MidiSystemInfo in order to check if any devices was added or deleted
 * from the system.
 * This class is a singleton.
 * @see MidiDeviceList
 * @see OutputDevicesList
 */
public class InputDevicesList extends MidiDeviceList {
    /**
     * The only possible instance of this class.
     */
    private static InputDevicesList inputDevices;

    /**
     * Constructs new <code>InputDeviceList</code>.
     * @see MidiDeviceList#initialize(String).
     */
    private InputDevicesList() {
        //Trying to use CoreMidi4j.
        if (MidiDeviceList.isCoreMidiLoaded) {
            this.addAll(initialize("MidiSource"));
        } else {
            this.addAll(initialize("MidiIn"));
        }
    }

    /**
     * Returns existing or new <code>InputDeviceList</code>.
     * @return the <code>InputDeviceList</code.>
     */
    public static InputDevicesList getInputDevicesList(){
        if (inputDevices == null) {
            inputDevices = new InputDevicesList();
        }
        return inputDevices;
    }

    /**
     * Refreshes the state of MidiSystemInfo and reinitialise current list
     * @throws MidiUnavailableException if any of sources is unavailable.
     */
    public void refreshInputDevicesList() throws MidiUnavailableException {
        //Trying to use CoreMidi4j.
        if (MidiDeviceList.isCoreMidiLoaded) {
            reInitialize("MidiSource");
        } else {
            reInitialize("MidiIn");
        }
    }
}
