package com.pansmileSoftware.model;

import com.pansmileSoftware.controller.StatusByte;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;

/**
 This is a model for message type switches. It provides autoCompletion, searching for the content by typing it's name
 and force repainting parent JComboBox when something is selected.
 */
public class MessageSwitchModel  implements  ComboBoxModel<StatusByte> {
    private StatusByte[] messageTypes = StatusByte.getCorrectValues();
    private StatusByte selected;
    private JComboBox<StatusByte> owner;

    public MessageSwitchModel(JComboBox<StatusByte> owner) {
        this.owner = owner;
        AutoCompletion.enable(owner);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem != null && !(anItem instanceof String)) {
            selected = (StatusByte) anItem;
            owner.repaint();
            showSelected();
        }
    }

    @Override
    public StatusByte getSelectedItem() {
        return selected;
    }

    @Override
    public int getSize() {
        return messageTypes.length;
    }

    @Override
    public StatusByte getElementAt(int index) {
        return messageTypes[index];
    }

    //We don't actually need this.
    @Override
    public void addListDataListener(ListDataListener l) {
    }
    //We don't actually need this.
    @Override
    public void removeListDataListener(ListDataListener l) {
    }

    //Force showing selected item.
    public void showSelected() {
        JTextComponent editor = (JTextComponent) owner.getEditor().getEditorComponent();
        if (selected != null) {
            editor.setText(selected.toString());
        } else {
            editor.setText("");
        }
    }

    //Removing any selection.
    public void selectNothing() {
        selected = null;
        showSelected();
    }
}
