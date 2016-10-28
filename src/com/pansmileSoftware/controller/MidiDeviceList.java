package com.pansmileSoftware.controller;

import javax.sound.midi.*;
import java.util.*;

/**This abstract class is a parent of InputDeviceList and OutputDeviceList.
 * It provides common methods for both of this classes.
 * @see InputDevicesList
 * @see OutputDevicesList
 */
public class MidiDeviceList extends ArrayList<MidiDevice> {
    static boolean isCoreMidiLoaded;
    /**Checks if CoreMidi4J library is installed in the system (for Mac OS only).*/
    public MidiDeviceList() {
        if (System.getProperty("os.name").contains("OS X")) {
            try {
                Class deviceProviderClass =
                        Class.forName("uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider");
                isCoreMidiLoaded = true;
            } catch (ClassNotFoundException e0) {
                isCoreMidiLoaded = false;
            }
        }
    }

    /**Creates a <code>List</code> of <code>MidiDevice</code>s filtering available devices by their names.
     * @param filteringParam  a string that defines which devices should be added to the list.
     * @return the list that contain devices filtered by filteringParam.
     */
    List<MidiDevice> initialize(String filteringParam) {
        List<MidiDevice> result = new ArrayList<>();
        //Adding MIDI devices to list.
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            try {
                if(MidiSystem.getMidiDevice(info).getClass().getName().contains(filteringParam)) {
                    result.add(MidiSystem.getMidiDevice(info));
                }
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        return  result;
    }

    /**Clears and reinitialise the list of <code>MidiDevice</code>s.
     * @param filteringParam  a string that defines which devices should be added to the list.
     */
    public void reInitialize(String filteringParam) throws MidiUnavailableException {
        this.clear();
        this.addAll(initialize(filteringParam));
    }
}

