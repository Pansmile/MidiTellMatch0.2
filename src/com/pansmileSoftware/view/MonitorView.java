package com.pansmileSoftware.view;

import com.pansmileSoftware.controller.MidiMonitor;
import com.pansmileSoftware.model.MonitorTableModel;
import com.pansmileSoftware.model.RadixSwitchModel;

import javax.swing.*;
import java.awt.event.*;

/**
 * This is the GUI form for MIDI-monitor module.
 */
public class MonitorView extends JFrame {
    private JTable monitorTable;
    private JPanel monitorPanel;
    private JButton clearButton;
    private JComboBox radixSwitch;
    private JCheckBox showMessageDataCheckBox;
    private JCheckBox showMessageInfoCheckBox;
    private JSpinner maxMemorySpinner;
    private MonitorTableModel model;
    private MidiMonitor monitor;
    private MainView owner;

    /**
     * Initializes the monitor's GUI.
     * @param owner  a <code>MainView</code> which owns current <code>MonitorView</code>.
     */
    public MonitorView(MainView owner){
        super("MIDI MonitorView");
        setContentPane(monitorPanel);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        model = new MonitorTableModel();
        monitorTable.setModel(model);
        this.owner = owner;

        //Deleting the monitor when closing the window of monitorView.
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                monitor.dispose();
                owner.getMonitorList().remove(monitor);
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        //Adding the automatic scroll down to monitorTable. The table will scroll down if new element is added.
        monitorTable.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                monitorTable.scrollRectToVisible(monitorTable.getCellRect(monitorTable.getRowCount()-1, 0, true));
            }});
        //Setting the width of table's columns.
        monitorTable.getColumnModel().getColumn(0).setMaxWidth(80);
        monitorTable.getColumnModel().getColumn(0).setMinWidth(80);
        monitorTable.getColumnModel().getColumn(1).setMinWidth(100);
        monitorTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        monitorTable.getColumnModel().getColumn(1).setMaxWidth(250);
        monitorTable.getColumnModel().getColumn(2).setMinWidth(100);
        monitorTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        monitorTable.getColumnModel().getColumn(2).setMaxWidth(250);
        monitorTable.getColumnModel().getColumn(3).setMaxWidth(50);
        monitorTable.getColumnModel().getColumn(3).setMinWidth(50);
        monitorTable.getColumnModel().getColumn(4).setMinWidth(100);

        //Providing possibility of selecting and copying the cell's content
        JTextField textField = new JTextField();
        textField.setEditable(true);
        monitorTable.setDefaultEditor(String.class, new javax.swing.DefaultCellEditor(textField));

        //Selecting checkboxes by default.
        showMessageDataCheckBox.setSelected(true);
        showMessageInfoCheckBox.setSelected(true);

        //Setting default value of maxMemorySpinner.
        maxMemorySpinner.setValue(1000);
        //Listening to the changes in maxMemorySpinner, sending it's value to the MonitorTable's model
        //and removing excess rows from the table.
        maxMemorySpinner.addChangeListener(e -> {
            model.setMaxRowCount((int) maxMemorySpinner.getValue());
            int removeCount = model.getRowCount() - (int) maxMemorySpinner.getValue();
            if (removeCount > 0) {
                for (int i = 0; i < removeCount; i++) {
                    model.remove(0);
                }
            }
        });
        //Initializing the radixSwitch and it's model.
        RadixSwitchModel radixModel = new RadixSwitchModel(radixSwitch);
        radixSwitch.setModel(radixModel);
        radixSwitch.getModel().setSelectedItem((byte)16);
        //Listening for the radix changes.
        radixSwitch.addActionListener(e -> {
            monitor.setRadix((byte)radixModel.getSelectedItem());
        });

        //Defining the behaviour of message data filter.
        showMessageDataCheckBox.addActionListener(e ->
                monitor.setShowMessageData(showMessageDataCheckBox.isSelected()));

        showMessageInfoCheckBox.addActionListener(e ->
                monitor.setShowMessageInfo(showMessageInfoCheckBox.isSelected()));
        //Clearing the monitorTable.
        clearButton.addActionListener(e -> model.clear());

        pack();
        setVisible(true);
    }

    /**
     * Returns the model of monitorTable <code>JTable</code>.
     * @return the model of monitorTable
     */
    public MonitorTableModel getModel() {
        return model;
    }

    /**
     * Sets the received {@link MidiMonitor} as monitor for this instance.
     * @param monitor  a <code>MidiMonitor</code> to set
     */
    public void setMonitor(MidiMonitor monitor) {
        this.monitor = monitor;
    }

}

