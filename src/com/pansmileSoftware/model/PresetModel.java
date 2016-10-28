package com.pansmileSoftware.model;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 * This is a model for JComboBox that manages preset files.
 */
public class PresetModel extends AbstractListModel<File> implements MutableComboBoxModel<File> {
    private ArrayList<File> presetList = new ArrayList<>();
    private File selected;
    JComboBox<File> owner;

    /**Constructs an instance of this class and sets it's owner.*/
    public PresetModel(JComboBox<File> owner) {
        this.owner = owner;
    }

    /**Adds a <code>File</code> into presetList and repaints the owner.
     * @param item  a <code>File</code> to add.*/
    @Override
    public void addElement(File item) {
        presetList.add(item);
        owner.repaint();
    }

    /**Removes received object from presetList and repaints the owner.
     * @param obj  an <code>Object</code> to remove.*/
    @Override
    public void removeElement(Object obj) {
        int index = 0;
        boolean isExist = false;
        for (int i = 0; i < presetList.size(); i++) {
            if (obj.equals(presetList.get(i))) {
                index = i;
                isExist = true;
            }
        }
        if (isExist){
            presetList.remove(index);
            owner.repaint();
        }
    }

    /**Inserts a <code>File</code> into presetList using an index and repaints the owner.
     * @param item  a <code>File</code> to insert.
     * @param index  an index that defines the place in the presetList where received File must be inserted.  */
    @Override
    public void insertElementAt(File item, int index) {
        presetList.add(index, item);
        owner.repaint();
    }

    /**Removes the element that lays in the presetList under received index and repaints the owner.
     * @param index  the index of element that we want to remove from presetList.*/
    @Override
    public void removeElementAt(int index) {
        if (index > 0 && index < presetList.size()) {
            presetList.remove(index);
            owner.repaint();

        }
    }

    /**Sets the received object selected and repaints the owner.
     * @param anItem  an item to select.*/
    @Override
    public void setSelectedItem(Object anItem) {
        presetList.stream().filter(preset -> preset.equals(anItem)).forEach(preset -> selected = preset);
        owner.repaint();
    }

    /**Returns the item that selected in this model.
     * @return selected  the selected <code>File</code>.*/
    @Override
    public File getSelectedItem() {
        return selected;
    }

    /**Returns the size of the presetList.
     * @return  an int which is the size of presetList.*/
    @Override
    public int getSize() {
        return presetList.size();
    }

    /**Returns the element that lays in the presetList under received index
     * @param index  an index to get the proper element.
     * @return  the <code>File</code> that lays in the presetList under received index.*/
    @Override
    public File getElementAt(int index) {
        return presetList.get(index);
    }

    /**Deletes all elements from presetList and repaints the owner.*/
    public void clear(){
        presetList.clear();
        owner.repaint();
    }
}
