package com.pansmileSoftware.model;

import com.pansmileSoftware.controller.MessageInfo;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This is a model for main table of <code>MonitorView</code> that included in MIDI Tell-Match app.
 * @see MonitorView
 * @see com.pansmileSoftware.controller.MidiMonitor
 * @see MessageInfo
 */
public class MonitorTableModel extends AbstractTableModel {
    private List<MessageInfo> infoList;
    private int maxRowCount;

    /**
     * Initializes infoList as empty <code>ArrayList</code> and gives to <code>maxRowCount</code> it's
     * default value.
     */
    public MonitorTableModel(){
        infoList = new ArrayList<>();
        maxRowCount = 1000;
    }

    /**
     * Returns the number of columns in parent <code>JTable</code>.
     * @return the number of columns in parent <code>JTable</code>.
     */
    @Override
    public int getColumnCount() {
        return 5;
    }

    /**
     * Returns the rows count, which equals <code>infoList.size()</code>.
     * @return the parent <code>JTable</code> rows count.
     */
    @Override
    public int getRowCount() {
        return infoList.size();
    }

    /**
     * Returns an <code>JTable</code>'s element which lays in the required cell.
     * @param rowIndex  index of a row to search in.
     * @param columnIndex  index of a column to find required cell in the row.
     * @return an object that contained in required cell.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return (infoList.get(rowIndex).getTime());
            case 1:
                return infoList.get(rowIndex).getDeviceAlignment();
            case 2:
                return infoList.get(rowIndex).getType();
            case 3:
                return infoList.get(rowIndex).getChannel();
            case 4:
                return infoList.get(rowIndex).getMessage();
            default:
                return "";
        }
    }

    /**
     * Returns the name of the column of received number.
     * @param column  a number of parent <code>JTable</code> column
     * @return a string that contain the name of required column.
     */
    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Time";
            case 1:
                return "Source/Destination";
            case 2:
                return "Message";
            case 3:
                return "Channel";
            case 4:
                return "Data";
            default: return "";
        }
    }
    /**
     * Adds received {@link MessageInfo} to the infoList, deletes items from beginning of
     * it if it's size is bigger than maxRowCount. Fires redrawing of the parent <code>JTable</code>.
     * @param info  information about a MIDI-message to add.
     */
    public void add(MessageInfo info) {
        synchronized (this) {
            infoList.add(infoList.size(),info);
            if (infoList.size() > maxRowCount) {
                infoList.remove(0);
            }
        }


        fireTableDataChanged();
    }
    /**
     * Removes all data from infoList and fires redrawing of parent <code>JTable</code>.
     */
    public void clear() {
        infoList.clear();
        fireTableDataChanged();
    }

    /**
     * Removes an element of received index from infoList and fires redrawing of parent <code>JTable</code>.
     */
    public void remove(int index) {
        infoList.remove(index);
        fireTableDataChanged();
    }

    /**
     * Adds Received <code>MessageInfo</code> to the parent <code>JTable</code> after waiting for an amount of
     * microseconds that <code>latencyStamp</code>.
     * @param info  a <code>MessageInfo</code> to add.
     * @param latencyStamp  an amount of microseconds to wait for.
     */
    public void addWithLatencyStamp(MessageInfo info, long latencyStamp) {
        Thread delay = new Thread() {
            @Override
            public void run() {
                try {
                    TimeUnit.MICROSECONDS.sleep(latencyStamp);
                    info.changeTime();
                    add(info);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        delay.start();
    }

    /**
     * Sets <code>maxRowCount</code> according to received value.
     * @param maxRowCount  an <vode>int</vode> which is the count of rows,
     * which will be shown in parent <code>JTable</code>
     */
    public void setMaxRowCount(int maxRowCount) {
        this.maxRowCount = maxRowCount;
    }

}

