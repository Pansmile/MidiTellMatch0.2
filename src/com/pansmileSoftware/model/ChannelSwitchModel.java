package com.pansmileSoftware.model;

import javax.swing.*;
import javax.swing.event.ListDataListener;

/**
 * This is a model for midi-channel switch. It provides force repainting the parent JComboBox
 * when an item is selected.
 */
public class ChannelSwitchModel implements ComboBoxModel<Byte> {
    private byte[] channels = new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
    private byte selected = 1;
    private JComboBox<Byte> owner;

    /***
     * Constructs a <code>ChannelSwitchModel</code> and sets it's parent <code>JComboBox</code>.
     * @param owner  a <code>JComboBox</code> which will use this instance of <code>ChannelSwitchModel</code>.
     */
    public ChannelSwitchModel(JComboBox<Byte> owner) {
        this.owner = owner;
    }

    /***
     * Returns a <code>Byte</code> which is a midi-channel's number.
     * @return a <code>Byte</code> which is selected.
     */
    @Override
    public Byte getSelectedItem() {
        return selected;
    }

    /***
     * Sets received object as selected in current <code>ChannelSwitchModel</code>
     * and it's parent <code>JComboBox</code>.
     * @param anItem  an <code>Object</code> to set as selected.
     */
    @Override
    public void setSelectedItem(Object anItem) {
        selected = (Byte) anItem;
        owner.repaint();
    }

    /***
     * Returns the number of elements in the <code>ChannelSwitchModel</code>.
     * @return  16.
     */
    @Override
    public int getSize() {
        return channels.length;
    }

    /***
     * Returns the element of the <code>ChannelSwitchModel</code> which lays under received index.
     * @param index  the index of required element.
     * @return  a <code>Byte</code> which is the channel number.
     */
    @Override
    public Byte getElementAt(int index) {
        return channels[index];
    }

    //We don't actually need this.
    @Override
    public void addListDataListener(ListDataListener l) {
    }
    //We don't actually need this.
    @Override
    public void removeListDataListener(ListDataListener l) {
    }

}
