package com.pansmileSoftware.controller;

import com.pansmileSoftware.model.MonitorTableModel;

import javax.sound.midi.*;
import java.util.*;

/**
 This class provides a controller for MIDI-monitor function.
 @see SpyReceiver
 @see SpyTransmitter
 @see MessageInfoLibrarian
 @see com.pansmileSoftware.view.MonitorView
 @see MonitorTableModel
 */
public class MidiMonitor {
    private ArrayList<SpyReceiver> receivers;

    /**
     * Constructs a <code>MidiMonitor</code> that will listen to sources and spy on destinations
     * @param sources  a list of MIDI-devises to listen to.
     * @param destinations  a list of MIDI-devices to spy on.
     * @param tellMatches  a list of existing <code>MidiTellMatch</code>es.
     * @param  model  a controller of JTable to populate with data
     */
    public MidiMonitor(List<MidiDevice> sources, List<MidiDevice> destinations,
                       Set<MidiTellMatch> tellMatches, MonitorTableModel model) {
        //Tries to initialize the MessageInfoLibrarian in order to
        // have possibility of constructing detailed info about caught messages.
        if (!MessageInfoLibrarian.isIsInitialized()) {
            MessageInfoLibrarian.initialize();
        }
        //initializing the list of current MIDI-MonitorView's receivers.
        receivers = new ArrayList<>();

        //Getting the receivers from sources and adding them to the list.
        for (MidiDevice source: sources) {
            try {
                if (!source.isOpen()) {
                    source.open();
                }
                SpyReceiver sourceReceiver = new SpyReceiver(source, model, false);
                receivers.add(sourceReceiver);
                Transmitter transmitter = source.getTransmitter();
                transmitter.setReceiver(sourceReceiver);
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }

        //Creating new receivers and connecting  each of them to required destination,
        // and adding to the list in case if this destination is used in one of existing MIDI Tell-Matches.
        for (MidiDevice destination : destinations) {
            SpyReceiver destReceiver = new SpyReceiver(destination, model, true);
            receivers.add(destReceiver);
            for (MidiTellMatch tellMatch: tellMatches) {
                tryToConnectASpyToTellMatch(tellMatch, destination, destReceiver);
            }
        }
    }

    /**
     * Checks if a <code>MidiTellMatch</code> is using same destinations as the current <code>MidiMonitor</code>
     * is using and connects a <code>SpyReceiver</code> to the <code>MidiTellMatch</code>'es destTransmitter.
     * @param tellMatch  a <code>MidiTellMatch</code> to check and connect in case of success.
     * @param destination  a <code>MidiDevice</code> which is used in current <code>MidiMonitor</code> as
     * one of the destinations.
     * @param destReceiver  a <code>SpyReceiver</code> associated to the destination
     * and current <code>MidiMonitor</code>
     */
    private void tryToConnectASpyToTellMatch(MidiTellMatch tellMatch, MidiDevice destination, SpyReceiver destReceiver) {
        tellMatch.getDestinations().stream().filter(device -> device.equals(destination)).forEach(device -> {
            tellMatch.getDestTransmitter().setReceiver(destReceiver);
        });
    }

    /**
     * Sets the numeric system for MIDI-message data
     * @param radix  a radix of numeric system.
     */
    public void setRadix(byte radix) {
        for (SpyReceiver receiver : receivers) {
            receiver.setRadix(radix);
        }
    }
    /**
     * Defines if the data of MIDI-messages will be shown in monitorTable.
     * @param showMessageData  must be true if data should be visible.
     */
    public void setShowMessageData(boolean showMessageData) {
        for (SpyReceiver receiver : receivers) {
            receiver.setShowMessageData(showMessageData);
        }
    }
    /**
     * Defines if the detailed information about MIDI-messages will be shown in
     * {@link com.pansmileSoftware.view.MonitorView#monitorTable}.
     * @param showMessageInfo  must be true if detailed information should be visible.
     */
    public void setShowMessageInfo(boolean showMessageInfo) {
        for (SpyReceiver receiver : receivers) {
            receiver.setShowMessageInfo(showMessageInfo);
        }
    }

    /**
     * Checks for new <code>MidiTellMatch</code>es connected to current <code>MidiMonitor</code>'s destinations
     * and connecting required <code>SpyReceiver</code>s to this <code>MidiTellMatch</code>es.
     * @param newTellMatches  a <code>Set</code> of <code>MidiTellMatch</code>es which are exiting at the moment.*/
    public void rescanDestinations(Set<MidiTellMatch> newTellMatches) {
        receivers.stream().filter(SpyReceiver::isDestReceiver).forEach(aReceiver -> {
            newTellMatches.stream().filter(aTellMatch -> !aTellMatch.getDestTransmitter().getReceivers()
                    .contains(aReceiver)).forEach(aTellMatch -> {
                tryToConnectASpyToTellMatch(aTellMatch, aReceiver.getSourceOrDest(), aReceiver);
            });
        });
    }

    /**
     * Clears all existing connections
     * */
    public void dispose() {
        receivers.clear();
        receivers = null;
    }
}

