package com.pansmileSoftware.controller;

import com.pansmileSoftware.view.MainView;

import javax.sound.midi.*;
import java.io.*;
import java.util.ArrayList;
import java.util.*;

/**
 This class is made for managing presets saving and loading. Actually it just writes text data from
 Tell-Matches info to a file in correct order or reads these info from a file in correct order.
 */
public class GlobalPresetManager {
    /**
     * Writes the information about <code>MidiTellMatch</code>es from received <code>List</code>
     * into the received <code>File</code>.
     * @see MidiTellMatch#getInfo()
     * @param midiTellMatches  the list of <code>MidiTellMatch</code>es to get the information.
     * @param file  the file to write the information.
     * @throws IOException  if an I/O error occurs
     */
    public static void saveGlobal(List<MidiTellMatch> midiTellMatches, File file) throws IOException {
        //Preparing writing to the file.
        FileOutputStream write = new FileOutputStream(file);
        //Getting the information about each <<code>MidiTellMatch</code> and write it to the file.
        for (MidiTellMatch tellMatch : midiTellMatches) {
            for (String toWrite : tellMatch.getInfo()) {
                write.write(toWrite.getBytes());
            }
        }
        //Setting a flag of the end of file.
        String end = "/1/2/3***end***/4/5/6";
        write.write(end.getBytes());
        write.close();
    }

    /**
     * Reconstructs <code>MidiTellMatch</code>es from the information in received <code>File</code> and adds these
     * <code>MidiTellMatch</code>es into received <code>MainView</code>'s <code>JList</code> of
     * <code>MidiTellMatch</code>es.
     * @param owner  a <code>MainView</code> where we need to add reconstructed <code>MidiTellMatch</code>es.
     * @param file  a <code>File</code> where the information about <code>MidiTellMatch</code>es is written.
     * @throws IOException  if an I/O error occurs.
     * @throws MidiUnavailableException  if an MidiDeviceUnavailable error occurs.
     * @throws InvalidMidiDataException  if an InvalidMidiData error occurs.
     */
    public static ArrayList<MidiTellMatch> loadGlobal(MainView owner, File file) throws IOException,
            MidiUnavailableException, InvalidMidiDataException {
        ArrayList<MidiTellMatch> midiTellMatches = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        while (true) {
            //Getting the name.
            String name = reader.readLine();
            if (name.equals("/1/2/3***end***/4/5/6") || name.equals(" ")) {
                break;
            }
            //Getting the source name.
            String sourceName = reader.readLine();
            MidiDevice source = owner.getSources().get(0);
            //looking for available sources.
            for (MidiDevice possibleSource : owner.getSources()) {
                String possibleName = possibleSource.getDeviceInfo().getName();
                if (possibleName != null && possibleName.contains(sourceName)) {
                    source = possibleSource;
                    break;
                }
            }
            //Getting the number of destination of current <code>MidiTellMatch</code>.
            int destCount = Integer.parseInt(reader.readLine());
            //Searching for destinations.
            ArrayList<MidiDevice> destinations = new ArrayList<>();
            for (int i = 0; i < destCount; i++) {
                String destName = reader.readLine();
                for (MidiDevice possibleDest: owner.getDestinations()) {
                    String possibleName = possibleDest.getDeviceInfo().getName();
                    if (possibleName != null && possibleName.contains(destName)) {
                        destinations.add(possibleDest);
                        break;
                    }
                }
            }
            if (destinations.size() == 0) {
                destinations.add(owner.getDestinations().get(0));
            }

            //Reading trigger message info.
            String trigger = reader.readLine();
            String triggerTypeString = reader.readLine();
            byte inputChannel = Byte.parseByte(reader.readLine());
            //Reading the outgoing message info.
            String outgoing = reader.readLine();
            String outgoingTypeString = reader.readLine();
            byte outputChannel = Byte.parseByte(reader.readLine());
            //Reading common info.
            byte radix = Byte.parseByte(reader.readLine());
            boolean ignoreVelocity = Boolean.parseBoolean(reader.readLine());
            double latencyComp = Double.parseDouble(reader.readLine());

            //Reconstructing a <code>MidiTellMatch</code> using received information.
            MidiTellMatch tellMatch = new MidiTellMatch(source,destinations,owner,radix);
            if (!trigger.isEmpty() && !triggerTypeString.isEmpty()) {
                tellMatch.setTriggerMessage(MessageEncoder.encode(trigger, radix,
                        StatusByte.valueOf(triggerTypeString), inputChannel));
                tellMatch.setTriggerString(trigger);
                tellMatch.setInputChannel(inputChannel);
                tellMatch.setTriggerType(StatusByte.valueOf(triggerTypeString));
            }
            if (!outgoing.isEmpty() && !outgoingTypeString.isEmpty()) {
                tellMatch.setOutgoingMessage(MessageEncoder.encode(outgoing, radix,
                        StatusByte.valueOf(outgoingTypeString), inputChannel));
                tellMatch.setOutgoingString(outgoing);
                tellMatch.setOutputChannel(outputChannel);
                tellMatch.setOutgoingType(StatusByte.valueOf(outgoingTypeString));
            }
            tellMatch.setIgnoreVelocity(ignoreVelocity);
            if (latencyComp != - 1){
                tellMatch.setLatencyComp(latencyComp);
            }
            tellMatch.setName(name);
            midiTellMatches.add(tellMatch);
        }
        reader.close();
        return midiTellMatches;
    }
}
