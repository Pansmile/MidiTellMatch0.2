package com.pansmileSoftware.controller;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

/**
 * This is the receiver that looks for incoming data from parent <code>MidiTellMatch</code> source and
 * sends this data to this <code>MidiTellMatch</code> in order to compare it with the trigger message.
 * @see MidiTellMatch
 * @see IncomingMessageReceiver
 */
public class IncomingMessageReceiver  implements Receiver {
    private MidiTellMatch owner;

    /**
     * Constructs an instance of this class and sets it's owner.
     * @param owner  a <code>MidiTellMatch</code> that owns this <code>IncomingMessageReceiver</code>
     */
    public IncomingMessageReceiver(MidiTellMatch owner) {
        this.owner = owner;
    }

    /**
     * Sets the first received message as owner's trigger Message if owner is listening,
     * else sends received message to owner using it's <code>trySend()</code> method.
     * @param message  a <code>MidiMessage</code> to process.
     * @param timeStamp  a timeStamp that is used in the super class
     * but did' not used in this implementation of <code>Receiver</code>
     * */
    @Override
    public void send(MidiMessage message, long timeStamp) {
        //If we are listening on the source to set the trigger message,
        //first arrived message will become the owner's trigger message.
        if (owner.isListening()) {
            byte detectedChannel = MessageDecoder.deCodeChannel(message.getStatus());
            if (owner.getInputChannel() != detectedChannel) {
                if (owner.getDetectMidiChannel()) {
                    owner.setInputChannel(detectedChannel);
                    owner.listen(message);
                } else {
                    owner.listeningFailed(owner.getInputChannel(), detectedChannel);
                }
            } else {
                owner.listen(message);
            }
        } else {
            owner.trySend(message, owner.getInputChannel());
        }
    }
    //I'm not sure do we actually need this.
    @Override
    public void close() {
    }
}

