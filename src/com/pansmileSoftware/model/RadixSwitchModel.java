package com.pansmileSoftware.model;

import javax.swing.*;
import javax.swing.event.ListDataListener;

/**
 * This is a model for radix switch JComboBox. It provides
 * force repainting parent JComboBox when something is selected.
 */
public class RadixSwitchModel   implements ComboBoxModel<Byte> {
    private byte selected = 16;
    private byte[] radix = new byte[]{10, 16};
    JComboBox<Byte> owner;

    public RadixSwitchModel(JComboBox<Byte> owner) {
        this.owner = owner;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selected = (Byte) anItem;
        owner.repaint();
    }
    @Override
    public Byte getSelectedItem() {
        return selected;
    }
    @Override
    public int getSize() {
        return 2;
    }
    @Override
    public Byte getElementAt(int index) {
        return radix[index];
    }
    @Override
    public void addListDataListener(ListDataListener l) {

    }
    @Override
    public void removeListDataListener(ListDataListener l) {

    }
}
