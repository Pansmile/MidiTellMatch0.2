package com.pansmileSoftware.controller;


import javax.sound.midi.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class provides us a transmitter that can be connected to the SpyReceiver
 * to spy on destinations of <code>MidiTellMatch</code>es. This gets the possibility to catch any <code>MidiMessage</code> sent
 * by <code>MidiTellMatch</code> to any of it's destinations.
 * @see MidiTellMatch
 * @see SpyReceiver
 * @see MidiMonitor
 */
public class SpyTransmitter implements Transmitter {
    Set<SpyReceiver> receivers = new HashSet<>();
    List<MidiDevice> destinations;

    /**
     * The constructor gives a link to the list of Midi-devices, used in current <code>MidiTellMatch</code>.
     * @param destinations  a list of MIDI-devices, used in parent <code>MidiTellMatch</code> as destinations.
     * */
    public SpyTransmitter(List<MidiDevice> destinations) {
        this.destinations = destinations;
    }

    /**
     * Clears current <code>SpyTransmitter</code>'s list of <code>SpyReceivers</code>
     * */
    @Override
    public void close() {
        clearReceivers();
    }

    /**
     * Connects a <code>SpyReceiver</code> to current <code>SpyTransmitter</code>.
     * */
    @Override
    public void setReceiver(Receiver receiver) {
        receivers.add((SpyReceiver) receiver);
    }

    /**
     * This method needs to be implemented but I do not advice to use it, cause it returns
     * only the first <code>SpyReceiver</code> that connected to current <code>SpyTransmitter</code>.
     * As soon as current <code>SpyTransmitter</code> could be connected to many <code>SpyReceiver</code>s.
     * Use method {@link SpyTransmitter#getReceivers()} instead.
     * @return  one of <code>SpyReceiver</code>s connected to the current <code>SpyTransmitter</code>.
     */
    @Override
    public Receiver getReceiver() {
        return receivers.iterator().next();
    }

    /**
     * Clears the <code>Set</code> of connected <code>SpyReceiver</code>s.
     */
    public void clearReceivers() {
        receivers.clear();
    }

    /**
     * Returns all <code>SpyReceivers</code> connected to current <code>SpyTransmitter</code>.
     * @return a <code>Set</code> of <code>SpyReceiver</code>s connected to current <code>SpyTransmitter</code>.
     */
    public Set<SpyReceiver> getReceivers() {
        return receivers;
    }

    /**
     * Sends received <code>MidiMessage</code> to destinations after waiting for an amount of time.
     * Also sends received message to connected <code>SpyReceiver</code>s immediately.
     * @param message  a <code>MidiMessage</code> to send.
     * @param latencyStamp  an amount of time in microseconds to wait for before sending the message to destinations.
     */
    public void send(MidiMessage message, long latencyStamp) {
        if (latencyStamp > 0) {
            //In case if the latencyStamp > 0 , creating new thread that will sleep for an amount of time in
            // microseconds (latencyStamp) before sending the message to destinations.
            Thread thread = new Thread() {
                public synchronized void run() {
                    try {
                        //Sleeping.
                        TimeUnit.MICROSECONDS.sleep(latencyStamp);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Sending the message to destinations.
                    sendToDestinations(message);
                }
            };
            //Starting the thread.
            thread.start();
        } else {
            //Sending the message to destinations if latencyStamp <= 0.
            sendToDestinations(message);
        }
        //If there are SpyReceivers connected, sending the message to them.
        if (receivers.size() > 0) {
            spy(message, latencyStamp);
        }
    }

    /**
     * Sends received <code>MidiMessage</code> to destinations.
     * @param message  a <code>MidiMessage</code> to send.
     */
    private void sendToDestinations(MidiMessage message) {
        //For each destination of destinations.
        for (MidiDevice destination : destinations) {
            try {
                //Trying to open a destination.
                if (!destination.isOpen()) {
                    destination.open();
                }
                //Getting the destination's receiver and sending the message to it.
                destination.getReceiver().send(message, -1);
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends received <code>MidiMessage</code> to connected <code>SpyReceiver</code>s.
     * @param message  a <code>MidiMessage</code> to send.
     * @param latencyStamp  an mount of time in microseconds which will be used by a <code>SpyReceiver</code>
     * to delay sending of the message.
     */
    private void spy(MidiMessage message, long latencyStamp) {
        for (SpyReceiver receiver : receivers) {
            receiver.send(message, latencyStamp);
        }
    }

    /**
     * Changes the destinations of current <code>SpyTransmitter</code>.
     * @param newDestinations  a <code>List</code> of <code>MidiDevice</code>s to set as
     * destinations instead of old ones.
     */
    public void setDestinations(List<MidiDevice> newDestinations) {
        destinations = newDestinations;
    }

    /**
     * Connects new <code>SpyReceiver</code>s to current <code>SpyTransmitter</code> if such receivers are found in
     * received <code>List</code>.
     * @param newReceivers  an <code>ArrayList</code> of <code>SpyReceiver</code> to search for new ones.
     */
    public void addReceivers(ArrayList<SpyReceiver> newReceivers) {
        receivers.addAll(newReceivers.stream().collect(Collectors.toList()));
    }
}

