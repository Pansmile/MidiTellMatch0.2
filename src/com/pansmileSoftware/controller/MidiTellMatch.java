package com.pansmileSoftware.controller;

import com.pansmileSoftware.view.MainView;

import javax.sound.midi.*;
import javax.swing.*;
import java.util.*;

/**
 * This class provides a MIDI-message converter which can listen to the source
 * and send new MIDI-message to the destinations when triggered by the incoming message.
 * @see InputDevicesList
 * @see OutputDevicesList
 * @see IncomingMessageReceiver
 * @see SpyTransmitter
 * @see SpyReceiver
 * @see StatusByte
 * @see MainView
 */
public class MidiTellMatch {
    private MidiDevice source;
    private ArrayList<MidiDevice> destinations;
    private IncomingMessageReceiver sourceReceiver;
    private Transmitter sourceTransmitter;
    private SpyTransmitter destTransmitter;
    private MidiMessage triggerMessage;
    private MidiMessage outgoingMessage;
    private boolean isTriggerShort = false;
    private String triggerString;
    private String outgoingString;
    private StatusByte triggerType;
    private StatusByte outgoingType;
    private static int tellMatchCount = 1;
    private long latencyComp = - 1;
    private boolean isListening;
    private boolean ignoreVelocity = true;
    private byte radix;
    private String name;
    public int sourceIndex;
    private byte inputChannel;
    private byte outputChannel;
    public ArrayList<Integer> destinationsIndexes = new ArrayList<>();
    private boolean detectMidiChannel = true;
    private MainView owner;

    /**
     * Creates new <code>MidiTellMatch</code> using received parameters.
     * @param source  a <code>MidiDevice</code> which will be used as source.
     * @param destinations an <code>ArrayList</code> of <code>MidiDevice</code>s which will be used as destinations.
     * @param owner  a <code>MainView</code> that will own this <code>MidiTellMatch</code>.
     * @param radix  the radix that will be used for encoding and decoding MIDI-messages.
     * @throws MidiUnavailableException if source or some of destinations are unavailable.*/
    public MidiTellMatch(MidiDevice source, ArrayList<MidiDevice> destinations,
                         MainView owner, byte radix) throws MidiUnavailableException {
        this.owner = owner;
        this.source = source;
        openDevice(source);
        this.destinations = destinations;
        for (MidiDevice destination : destinations) {
            openDevice(destination);
        }
        this.inputChannel = 1;
        this.outputChannel = 1;
        this.radix = radix;
        sourceTransmitter = source.getTransmitter();
        destTransmitter = new SpyTransmitter(destinations);
        sourceReceiver = new IncomingMessageReceiver(this);
        sourceTransmitter.setReceiver(sourceReceiver);
        setIndices();
        name = "MIDI Tell-Match" + tellMatchCount++;
        isListening = false;
    }

    /**
     * Defines if incoming message equals to trigger message and in case of equality
     * tries to send outgoing message to destinations.
     * @param message  a <code>MidiMessage</code> to check it's equality to <code>triggerMessage</code>.
     * @param midiChannel*/
    public void trySend(MidiMessage message, byte midiChannel) {
        //If new incoming message is detected getting the data from trigger and incoming message.
        if (triggerMessage != null) {
            byte[] sourceData = message.getMessage();
            byte[] triggerData = triggerMessage.getMessage();

            //Making the velocity of short messages similar if ignoreVelocity.
            if (isTriggerShort && ignoreVelocity) {
                sourceData[sourceData.length - 1] = 127;
                triggerData[triggerData.length - 1] = 127;
            }

            //Detecting equality of trigger and incoming messages and sending outgoing message to destinations.
            if ((!isListening && Arrays.equals(triggerData, sourceData))) {
                try {
                    sendToDestinations();
                } catch (MidiUnavailableException e) {
                    JOptionPane.showMessageDialog(new JFrame(),"Some Destinations are unavailable" + '\n' + e);
                }
                catch (NullPointerException e) {
                    JOptionPane.showMessageDialog(new JFrame(), "Define the outgoing message");
                }
            }
        }
    }

    /**
     * Tries to send the outgoing message to destinations.
     * @throws MidiUnavailableException in case if any of destinations are unavailable.*/
    public void sendToDestinations() throws MidiUnavailableException {
        if (outgoingMessage != null) {
            destTransmitter.send(outgoingMessage, latencyComp);
        }
    }

    /**
     * Sets the latencyComp - an amount of time in Microseconds that determines the delay before sending
     * the outgoingMessage to destination.
     * @param comp  a <code>Double</code> which is an amount of time in milliseconds to convert in microseconds.
     */
    public void setLatencyComp(Double comp) {
        if (comp == 0) {
            latencyComp = - 1;
        }
        else {
            latencyComp = (long) (comp * 1000/ 1);
        }
    }

    /**
     * Provides the Listen function. Sets received <code>MidiMessage</code> as a trigger and rewrites all
     * information connected to it.
     * Also redraws owner's GUI elements connected to triggerMessage.
     * If only current <code>MidiTellMatch</code> is listening, stops listening.
     * @param message  a <code>MidiMessage</code> to set as triggerMessage.
     */
    public void listen(MidiMessage message){
        triggerString = MessageDecoder.decode(message,radix);
        triggerType = StatusByte.getMessageType(message);
        setTriggerMessage(message);
        owner.redrawTriggerGUIElements();
        setListening(false);
        if (!owner.someOneIsListening()) {
            owner.getListenButton().setSelected(false);
        }
    }

    /**
     * Tells owner to show a <code>Dialog</code>, showing that listening was failed
     * because of wrong MIDI-channel in incoming message.
     * @param requiredChannel  a <code>byte</code> which is the number of expected channel.
     * @param detectedChannel  a <code>byte</code> which is the actual detected channel.
     */
    public void listeningFailed(byte requiredChannel, byte detectedChannel) {
        owner.showWrongChannelDialog(requiredChannel, detectedChannel);
        if (!owner.someOneIsListening()) {
            owner.getListenButton().setSelected(false);
        }
    }


    /**
     * Connects <code>MidiTellMatch</code>'s source after rescanning the MIDI-System.
     * @param currentSources  an <code>ArrayList</code> of <code>MidiDevice</code>s which are the sources available
     * in the system at the moment.
     */
    public void restoreSource(ArrayList<MidiDevice> currentSources) {
        currentSources.stream().filter(possibleSource -> ((possibleSource.getDeviceInfo().getName()
                .equals(source.getDeviceInfo().getName())
                && (possibleSource.getDeviceInfo().getDescription().equals(source.getDeviceInfo().getDescription()))))).forEach(
                source1 -> {
                    try {
                        MidiTellMatch.this.setSource(source1);
                    } catch (MidiUnavailableException e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Connects <code>MidiTellMatch</code>'s destinations after rescanning the MIDI-System.
     * @param currentDestinations  an <code>ArrayList</code> of <code>MidiDevice</code>s which are the destinations
     * available in the system at the moment.
     */
    public void restoreDestinations(ArrayList<MidiDevice> currentDestinations) {
        for (int i = 0; i < destinations.size(); i++) {
            for (MidiDevice possibleDestination : currentDestinations) {
                if ((destinations.get(i).getDeviceInfo().getName().equals(
                        possibleDestination.getDeviceInfo().getName())
                        && (destinations.get(i).getDeviceInfo().getDescription().equals(
                        possibleDestination.getDeviceInfo().getDescription())))) {
                    destinations.get(i).close();
                    setDestination(i, possibleDestination);
                }
            }
        }
    }

    /**
     * Sets values of variables, which are the indices of the current <code>MidiTellMatch</code>'s source and
     * destinations in the lists of all sources and destinations.
     */
    public void setIndices() {
        sourceIndex = 0;
        destinationsIndexes.clear();
        for (int i = 0; i < owner.getSources().size(); i++) {
            if (owner.getSources().get(i).getDeviceInfo().equals(source.getDeviceInfo())) {
                sourceIndex = i;
            }
        }
        for (int i = 0; i < owner.getDestinations().size(); i++) {
            for (MidiDevice destination : destinations) {
                if (destination.getDeviceInfo().equals(owner.getDestinations().get(i).getDeviceInfo())) {
                    destinationsIndexes.add(i);
                }
            }
        }
    }

    //Here is a bunch of getters and setters.
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public static void setTellMatchCount(int tellMatchCount) {
        MidiTellMatch.tellMatchCount = tellMatchCount;
    }
    public static int getTellMatchCount() {
        return tellMatchCount;
    }

    /**
     * Sets source MidiDevice  according to received data.
     * @param source  a <code>MidiDevice</code> which is the new source.
     * @throws MidiUnavailableException in case if new source is unavailable.
     * */
    public void setSource(MidiDevice source) throws MidiUnavailableException {
        sourceTransmitter.close();
        sourceReceiver.close();
        this.source = source;
        sourceTransmitter = source.getTransmitter();
        sourceReceiver = new IncomingMessageReceiver(this);
        sourceTransmitter.setReceiver(sourceReceiver);
    }

    /**
     * Returns current <code>MidiTellMatch</code>'s source.
     * @return  a <code>MidiDevice</code> which is the source of current <code>MidiTellMatch</code>.
     */
    public MidiDevice getSource() {
        return source;
    }

    /**
     * Sets and checks listening status of a <code>MidiTellMatch</code> in order to
     *provide <Listen> button to work properly.
     * @param listening  a boolean which must be true in case if current <code>MidiTellMatch</code> should be set
     * to the listening state.
     */
    public void setListening(boolean listening) {
        isListening = listening;
    }

    /**
     * Returns the listening state of current <code>MidiTellMatch</code>.
     * @return true in case if current <code>MidiTellMatch</code> is in the listening state.
     */
    public boolean isListening() {
        return isListening;
    }

    /**
     * Sets the radix of current <code>MidiTellMatch</code> according to the received data.
     * @param radix  a byte which is the radix of a numeric system.
     */
    public void setRadix(byte radix) {
        this.radix = radix;
    }

    /**
     * Returns the radix of current <code>MidiTellMatch</code>'s numeric system.
     * @return  a byte which is the radix of the current <code>MidiTellMatch</code>'s numeric system.
     */
    public byte getRadix() {return radix;}

    /**
     * Sets current <code>MidiTellMatch</code>'s trigger message according to the received data.
     * @param message  a <code>MidiMessage</code> to set as the trigger.
     */
    public void setTriggerMessage(MidiMessage message) {
        this.triggerMessage = message;
        byte newChannel = MessageDecoder.deCodeChannel(triggerMessage.getStatus());
        if (newChannel != inputChannel) {
            inputChannel = newChannel;
        }
        for (StatusByte triggerStatus : StatusByte.getShortList()) {
            if (triggerStatus.getStatusByte(inputChannel) == triggerMessage.getStatus()) {
                isTriggerShort = true;
                break;
            }
        }
    }

    /**
     * Returns current <code>MidiTellMatch</code>'s trigger message.
     * @return  a <code>MidiMessage</code> which is the trigger message of current <code>MidiTellMatch</code>.
     */
    public MidiMessage getTriggerMessage() {
        return triggerMessage;
    }

    /**
     * Sets current <code>MidiTellMatch</code>'s trigger message type.
     * @param triggerType  a <code>StatusByte</code> which is the type of trigger message
     * of current <code>MidiTellMatch</code>.
     */
    public void setTriggerType(StatusByte triggerType) {
        this.triggerType = triggerType;
    }

    /**
     * Returns the type of current <code>MidiTellMatch</code>'s trigger message.
     * @return  a <code>StatusByte</code> which is the type of trigger message
     * of current <code>MidiTellMatch</code>.
     */
    public StatusByte getTriggerType() {
        return triggerType;
    }

    /**
     * Sets current <code>MidiTellMatch</code>'s trigger message string representation.
     * @param triggerString a <code>String</code> to set as string representation
     * of the trigger message of current <code>MidiTellMatch</code>.
     */
    public void setTriggerString(String triggerString) {
        this.triggerString = triggerString;
    }

    /**
     * Returns the string representation of current <code>MidiTellMatch</code>'s trigger message.
     * @return  a <code>String</code> representation of current <code>MidiTellMatch/code>'s trigger message.
     */
    public String getTriggerString() {
        return triggerString;
    }

    /**
     * Sets current <code>MidiTellMatch</code>'s outgoing message according to the received data.
     * @param message  a <code>MidiMessage</code> to set as the outgoing message.
     */
    public void setOutgoingMessage(MidiMessage message) {
        outgoingMessage = message;
    }

    /**
     * Returns current <code>MidiTellMatch</code>'s outgoing message.
     * @return  a <code>MidiMessage</code> which is the outgoing message of current <code>MidiTellMatch</code>.
     */
    public MidiMessage getOutgoingMessage() {
        return outgoingMessage;
    }

    /**
     * Sets current <code>MidiTellMatch</code>'s outgoing message type.
     * @param outgoingType  a <code>StatusByte</code> which is the type of outgoing message
     * of current <code>MidiTellMatch</code>.
     */
    public void setOutgoingType(StatusByte outgoingType) {
        this.outgoingType = outgoingType;
    }

    /**
     * Returns the type of current <code>MidiTellMatch</code>'s outgoing message.
     * @return  a <code>StatusByte</code> which is the type of outgoing message
     * of current <code>MidiTellMatch</code>.
     */
    public StatusByte getOutgoingType() {
        return outgoingType;
    }

    /**
     * Sets current <code>MidiTellMatch</code>'s outgoing message string representation.
     * @param outgoingString a <code>String</code> to set as string representation
     * of the outgoing message of current <code>MidiTellMatch</code>.
     */
    public void setOutgoingString(String outgoingString) {
        this.outgoingString = outgoingString;
    }

    /**
     * Returns the string representation of current <code>MidiTellMatch</code>'s outgoing message.
     * @return  a <code>String</code> representation of current <code>MidiTellMatch/code>'s outgoing message.
     */
    public String getOutgoingString() {
        return outgoingString;
    }

    /**
     * Sets received device as a destination of current <code>MidiTellMatch</code> according to the received index.
     * @param index  an int which is the index of destination which need to be changed.
     * @param device  a <code>MidiDevice</code> to set as destination.
     */
    public void setDestination(int index, MidiDevice device) {
        destinations.set(index, device);
        destTransmitter.setDestinations(destinations);
    }

    /**
     * Sets <code>MidiDevice</code>s from received <code>List</code> as destinations of
     * current <code>MidiTellMatch</code>.
     * @param newDestinations  a <coe>List</coe> of <code>MidiDevice</code>s which are the new destinations.
     * @throws MidiUnavailableException in case if one of devices in <code>newDestinations</code> is unavailable.
     */
    public void setDestinations(List<MidiDevice> newDestinations) throws MidiUnavailableException {
        destinations.clear();
        destinations.addAll(newDestinations);
        ArrayList<SpyReceiver> newReceivers = new ArrayList<>();
        if (destTransmitter.getReceivers().size() > 0) {
            for (SpyReceiver spyReceiver : destTransmitter.getReceivers()) {
                for (MidiDevice destination : destinations) {
                    if (spyReceiver.getSourceOrDest().equals(destination)) {
                        newReceivers.add(spyReceiver);
                        break;
                    }
                }
            }
        }
        destTransmitter.clearReceivers();
        destTransmitter.addReceivers(newReceivers);
        destTransmitter.setDestinations(destinations);
    }

    /**
     * Returns current <code>MidiTellMatch</code>'s destinations as an <code>ArrayList</code>.
     * @return  a <code>List</code> of <code>MidiDevice</code>s which are the destinations
     * of current <code>MidiTellMatch</code>.
     */
    public ArrayList<MidiDevice> getDestinations() {
        return destinations;
    }

    /**
     * Returns the amount of time in microseconds which is the delay before sending the outgoing message
     * after the trigger message is received.
     * @return  a <code>long</code> which is the delay of sending the outgoing message
     * after receiving the trigger message.
     */
    public long getLatencyComp() {
        return latencyComp;
    }

    /**
     * Sets current <code>MidiTellMatch</code>'s ignoreVelocity field according to received data.
     * @param ignoreVelocity  a boolean to set as current <code>MidiTellMatch</code>'s ignoreVelocity field.
     */
    public void setIgnoreVelocity(boolean ignoreVelocity) {
        this.ignoreVelocity = ignoreVelocity;
    }

    /**
     * Returns true in case if current <code>MidiTellMatch</code>'s ignoreVelocity field is true.
     * @return  true in case if current <code>MidiTellMatch</code>'s ignoreVelocity field is true.
     */
    public boolean isIgnoreVelocity() {
        return ignoreVelocity;
    }

    /**
     * Sets current <code>MidiTellMatch</code>'s input channel according to the received data.
     * @param inputChannel  a byte to set as current <code>MidiTellMatch</code>'s input channel.
     */
    public void setInputChannel(byte inputChannel) {
        this.inputChannel = inputChannel;
    }

    /**
     * Returns current <code>MidiTellMatch</code>'s input channel.
     * @return  a byte which is the current <code>MidiTellMatch</code>'s input channel.
     */
    public byte getInputChannel() {
        return inputChannel;
    }

    /**
     * Sets current <code>MidiTellMatch</code>'s output channel according to the received data.
     * @param outputChannel  a byte to set as current <code>MidiTellMatch</code>'s output channel.
     */
    public void setOutputChannel(byte outputChannel) {
        this.outputChannel = outputChannel;
    }

    /**
     * Returns current <code>MidiTellMatch</code>'s output channel.
     * @return  a byte which is the current <code>MidiTellMatch</code>'s output channel.
     */
    public byte getOutputChannel() {
        return outputChannel;
    }

    /**
     * Returns current <code>MidiTellMatch</code>'s source transmitter.
     * @return current <code>MidiTellMatch</code>'s source transmitter.
     */
    public Transmitter getSourceTransmitter() {
        return sourceTransmitter;
    }

    /**
     * Returns current <code>MidiTellMatch</code>'s output transmitter.
     * @return current <code>MidiTellMatch</code>'s output transmitter.
     */
    public SpyTransmitter getDestTransmitter() {
        return destTransmitter;
    }

    /**
     * Lets current <code>MidiTellMatch</code> to set it's input channel according to received MIDI-message
     * while listening for trigger message if received boolean is true.
     * @param detectMidiChannel  a boolean which must be true if we want to let a <code>MidiTellMatch</code>
     * to set it's input channel according to received MIDI-message
     * while listening for trigger message.
     */
    public void setDetectMidiChannel(boolean detectMidiChannel) {
        this.detectMidiChannel = detectMidiChannel;
    }

    /**
     * Returns true if current <code>MidiTellMatch</code>'s detectMidiChannel field is true.
     * @return true if current <code>MidiTellMatch</code>'s detectMidiChannel field is true.
     */
    public boolean getDetectMidiChannel() {
        return detectMidiChannel;
    }

    /**
     * Fills all fields connected to trigger or outgoing message with received data.
     * @param message  a <code>MidiMessage</code> to set as trigger or outgoing.
     * @param channel  a byte which is the input or output channel.
     * @param status  a <code>StatusByte</code> which is the triggerType or outgoingType.
     * @param msg  a <code>String</code> which is the triggerString or outgoingString.
     * @param key  a <code>String</code> that defines which fields should be filled, ones which are connected to
     * trigger message or ones which are connected to outgoing message.
     */
    public void setMessageData(MidiMessage message, byte channel, StatusByte status, String msg, String key) {
        switch (key) {
            case "trigger" :
                triggerMessage = message;
                inputChannel = channel;
                triggerType = status;
                triggerString = msg;
                return;
            case "outgoing" :
                outgoingMessage = message;
                outputChannel = channel;
                outgoingType = status;
                outgoingString = msg;
        }
    }

    /**
     * Tries to open received <code>MidiDevice</code> and throws an exception if failed.
     * @param device  a <code>MidiDevice</code> to open.
     * @throws MidiUnavailableException in case if device is unavailable.
     */
    public void openDevice(MidiDevice device) throws MidiUnavailableException {
        try {
            device.open();
        } catch (MidiUnavailableException e) {
            throw new MidiUnavailableException("device " + device.getDeviceInfo().getName() + " is unavailable");
        }
    }

    /**
     * Creates an <code>ArrayList</code> of strings which are describing current
     * <code>MidiTellMatch</code> and returns it.
     * @return an code>ArrayList</code> of strings which are describing current
     * <code>MidiTellMatch</code>.
     */
    public ArrayList<String> getInfo() {
        ArrayList<String> info = new ArrayList<>();

        info.add(name + "\n");
        if (source.getDeviceInfo().getName() != null) {
            info.add(source.getDeviceInfo().getName() + "\n");
        } else {
            info.add("not defined\n");
        }
        info.add(String.valueOf(destinations.size()) + "\n");
        for (MidiDevice destination : destinations) {
            if (destination.getDeviceInfo().getName() != null) {
                info.add(destination.getDeviceInfo().getName() + "\n");
            } else {
                info.add("not defined \n");
            }
        }
        info.add(((triggerString == null || triggerString.isEmpty()) ? "\n" : triggerString + "\n"));
        info.add(((triggerType == null || String.valueOf(triggerType).isEmpty()) ? "\n" : triggerType + "\n"));
        info.add(inputChannel + "\n");
        info.add((outgoingString == null || outgoingString.isEmpty()) ? "\n" : outgoingString + "\n");
        info.add((outgoingType == null || String.valueOf(outgoingType).isEmpty()) ? "\n" : outgoingType + "\n");
        info.add(outputChannel + "\n");
        info.add(String.valueOf(radix + "\n"));
        info.add(ignoreVelocity + "\n");
        info.add(latencyComp/1000 + "\n");

        return info;
    }

    /**
     * Returns the name of current <code>MidiTellMatch</code>.
     * @return a String which is the name of current <code>MidiTellMatch</code>.
     */
    public String toString() {
        return name;
    }
}
