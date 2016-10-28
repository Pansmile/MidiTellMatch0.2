package com.pansmileSoftware.model;

import com.pansmileSoftware.controller.MidiTellMatch;

import javax.swing.*;

/**
 * This is a model for list of <code>MidiTellMatch</code>es. It provides safe deleting elements.
 */
public class TellMatchModel extends DefaultListModel<MidiTellMatch> {
    //Every time we delete a MIDI Tell-Match we have to close it's sourceTransmitter to prevent
    //deleted Tell-Match from continue working.
    @Override
    public void clear() {
        for (int i = 0; i < getSize(); i++) {
            MidiTellMatch tellMatch = get(i);
            tellMatch.getSourceTransmitter().close();
        }
        super.clear();
    }

    @Override
    public boolean removeElement(Object obj) {
        MidiTellMatch tellMatch = (MidiTellMatch) obj;
        tellMatch.getSourceTransmitter().close();
        return super.removeElement(obj);
    }

    @Override
    public void removeElementAt(int index) {
        MidiTellMatch tellMatch = get(index);
        tellMatch.getSourceTransmitter().close();
        super.remove(index);
    }

    @Override
    public void removeAllElements() {
        for (int i = 0; i < getSize(); i++) {
            MidiTellMatch tellMatch = get(i);
            tellMatch.getSourceTransmitter().close();
        }
        super.removeAllElements();
    }

    @Override
    public void removeRange(int fromIndex, int toIndex) {
        if (fromIndex < toIndex) {
            for (int i = toIndex; i >= fromIndex; i--) {
                MidiTellMatch tellMatch = get(i);
                tellMatch.getSourceTransmitter().close();
            }
            super.removeRange(fromIndex, toIndex);
        } else {
            throw new IllegalArgumentException("fromIndex must be <= toIndex");
        }
    }

    @Override
    public void add(int index, MidiTellMatch element) {
        super.add(index,element);
    }

    @Override
    public MidiTellMatch remove(int index) {
        MidiTellMatch tellMatch = get(index);
        tellMatch.getSourceTransmitter().close();
        return super.remove(index);
    }

    @Override
    public void setElementAt(MidiTellMatch element, int index) {
        MidiTellMatch tellMatch = get(index);
        tellMatch.getSourceTransmitter().close();
        super.setElementAt(element, index);
    }

    @Override
    public MidiTellMatch set(int index, MidiTellMatch element) {
        return super.set(index, element);
    }
}
