package com.pansmileSoftware.view;

import com.pansmileSoftware.controller.*;
import com.pansmileSoftware.model.*;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * This is the main GUI frame that provide access to  all necessary functions and elements of the program.
 * @see MidiTellMatch
 * @see MonitorView
 * @see MidiMonitor
 * @see InputDevicesList
 * @see OutputDevicesList
 * @see MidiDeviceList
 * @see StatusByte
 * @see MessageSwitchModel
 * @see AutoCompletion
 * @see MidiDeviceReceiver
 */
public class MainView extends JFrame {
    private JPanel rootPanel;
    private JList<MidiDevice> sources;
    private JScrollPane tellmatchscroll;
    private JList<MidiTellMatch> midiTellMatches;
    private JList<MidiDevice> destinations;
    private JTextField triggerMessage;
    private JTextField outgoingMessage;
    private JButton addTellMatchButton;
    private JButton removeButton;
    private JButton refreshMidiSystemButton;
    private JToggleButton listenButton;
    private JButton saveButton;
    private JComboBox<File> presetBox;
    private JComboBox<Byte> inputChannelSwitch;
    private JComboBox<Byte> outputChannelSwitch;
    private JComboBox<Byte> radixSwitch;
    private JComboBox<StatusByte> outgoingType;
    private JComboBox<StatusByte> triggerType;
    private JLabel radixLabel;
    private JLabel inputChannelLabel;
    private JSpinner latencyCompensation;
    private JLabel latencyCompensationLabel;
    private JCheckBox ignoreVelocityBox;
    private JCheckBox detectChannelCheckBox;
    private JCheckBox linkChannelsCheckBox;
    private JButton resetInOutButton;
    private JCheckBox addOrReplaceCheckBox;
    private JButton monitorButton;
    private File presetsFolder;
    private PresetsRootChooser presetRootChooser;
    private PresetFileFilter presetFileFilter;
    private FileDialog PresetSaver;
    private DefaultListModel<MidiTellMatch> tellMatchModel;
    private DefaultListModel<MidiDevice> sourceModel;
    private DefaultListModel<MidiDevice> destModel;
    private MessageSwitchModel triggerTypeModel;
    private MessageSwitchModel outgoingTypeModel;
    private Dimension tellMatchesDimension;
    private MainView thisApp;
    private MidiTellMatch currentTellMatch;
    private ArrayList<MidiMonitor> monitorList;

    /**
     * Constructs a new instance of MainView
     */
    public MainView() {
        super("MIDI Tell-Match");
        setContentPane(rootPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        thisApp = this;

        //Disabling all controls that must be associated with a Tell-Match.
        addTellMatchButton.setEnabled(false);
        removeButton.setEnabled(false);
        listenButton.setEnabled(false);
        outgoingMessage.setEnabled(false);
        triggerMessage.setEnabled(false);
        saveButton.setEnabled(false);
        triggerType.setEnabled(false);
        outgoingType.setEnabled(false);
        radixSwitch.setEnabled(false);
        latencyCompensation.setEnabled(false);
        inputChannelSwitch.setEnabled(false);
        outputChannelSwitch.setEnabled(false);
        ignoreVelocityBox.setEnabled(false);
        detectChannelCheckBox.setSelected(true);
        detectChannelCheckBox.setEnabled(false);
        resetInOutButton.setEnabled(false);
        addOrReplaceCheckBox.setSelected(false);

        PresetSaver = new FileDialog(new Frame("Save Preset"));

        //setting mnemonics
        listenButton.setMnemonic(KeyEvent.VK_L);
        refreshMidiSystemButton.setMnemonic(KeyEvent.VK_R);
        //Initializing models.
        tellMatchModel = new TellMatchModel();
        sourceModel = new DefaultListModel<>();
        destModel = new DefaultListModel<>();

        inputChannelSwitch.setModel(new ChannelSwitchModel(inputChannelSwitch));
        outputChannelSwitch.setModel(new ChannelSwitchModel(outputChannelSwitch));
        latencyCompensation.setModel(new SpinnerNumberModel(0.0, 0.0, 10001.0, 0.1));

        //Preparing the list of Tell-Matches to work and display properly.
        midiTellMatches.setCellRenderer(new TellMatchRenderer());
        midiTellMatches.setModel(tellMatchModel);
        tellMatchesDimension = new Dimension(200, 0);
        midiTellMatches.setPreferredSize(tellMatchesDimension);


        //Preparing the sources and destinations lists.
        sources.setModel(sourceModel);
        destinations.setModel(destModel);
        sources.setCellRenderer(new MidiDeviceRenderer());
        destinations.setCellRenderer(new MidiDeviceRenderer());
        fillSourcesAndDestinations();

        //Preparing the radix switch to work properly.
        radixSwitch.setModel(new RadixSwitchModel(radixSwitch));
        radixSwitch.getModel().setSelectedItem((Byte.valueOf("16")));

        //Preparing the message-type switches.
        triggerTypeModel = new MessageSwitchModel(triggerType);
        triggerType.setModel(triggerTypeModel);
        outgoingTypeModel = new MessageSwitchModel(outgoingType);
        outgoingType.setModel(outgoingTypeModel);

        //Preparing Preset folder chooser.
        presetFileFilter = new PresetFileFilter();
        presetRootChooser = new PresetsRootChooser();

        //Setting the default config and presets paths.
        String configPath = System.getProperty("user.home") + System.getProperty("file.separator")
                + "Documents/MIDI Tell-Match/configs/presetPathConfig.txt";
        String presetPath = System.getProperty("user.home") + System.getProperty("file.separator")
                + "Documents/MIDI Tell-Match/presets";
        try {
            FileInputStream stream = new FileInputStream(configPath);
        } catch (FileNotFoundException e) {
            System.out.println("Not found!");
            try {
                FileOutputStream writer = new FileOutputStream(configPath);
                System.out.println(presetPath);
                writer.write(presetPath.getBytes());
                writer.close();
            } catch (IOException e2) {
                e.printStackTrace();
            }
        }

        //Trying to read presets path from config file.
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(configPath)));
            presetPath = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Trying to load the presets folder.
        presetsFolder = new File(presetPath);
        presetRootChooser.setCurrentDirectory(presetsFolder);
        if (!presetFileFilter.accept(presetsFolder)) {
            if (presetRootChooser.showDialog(rootPanel, "Choose presets folder") == PresetsRootChooser.APPROVE_OPTION) {
                presetsFolder = presetRootChooser.getSelectedFile();
                //Setting user presets folder path.
                presetPath = presetRootChooser.getSelectedFile().getPath();
                try {
                    FileOutputStream writer = new FileOutputStream(configPath);
                    System.out.println(presetPath);
                    writer.write(presetPath.getBytes());
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                presetsFolder = null;
            }
        }

        //Modifying a PresetSaver dialog.
        if (presetsFolder != null) {
            PresetSaver.setDirectory(presetsFolder.getPath());
        }
        PresetSaver.setMode(FileDialog.SAVE);
        PresetSaver.setFilenameFilter((dir, name) -> name.endsWith(".pst"));

        monitorButton.addActionListener(e -> {
            if (monitorList == null) {
                monitorList = new ArrayList();
            }
            MonitorView monitorView = new MonitorView(thisApp);
            MidiMonitor monitor = new MidiMonitor(sources.getSelectedValuesList(),
                    destinations.getSelectedValuesList(), getAllTellMatches(), monitorView.getModel());
            monitorView.setMonitor(monitor);
            monitorList.add(monitor);

        });

        //Restoring Global Preset.
        if (sources.getModel().getSize() > 0 && destinations.getModel().getSize() > 0) {
            if (presetsFolder != null) {
                File toLoad = new File(presetsFolder + "/startUp.pst");
                loadPreset(toLoad);
            }
        } else if (sources.getModel().getSize() == 0 && destinations.getModel().getSize() != 0) {
            JOptionPane.showMessageDialog(new JFrame("ERROR!"), "No sources available");
        } else if (destinations.getModel().getSize() == 0 && sources.getModel().getSize() != 0) {
            JOptionPane.showMessageDialog(new JFrame("ERROR!"), "No destinations available");
        } else {
            JOptionPane.showMessageDialog(new JFrame("ERROR!"), "No MIDI-devices available");
        }
        //Preparing the presetBox.
        final PresetModel presetModel = new PresetModel(presetBox);
        presetBox.setModel(presetModel);
        fillPresetBox(presetModel);
        //presetModel.insertElementAt(new EmptyPreset(), 0);
        if (presetModel.getSize() != 0) {
            presetBox.setSelectedIndex(0);
            presetBox.setRenderer(new PresetRenderer());
        }


        //There are actionListeners for all UI components below.

        //Setting the behaviour of <Refresh MIDI System> button.
        //This button is made for getting the access to new MIDI-Devices that appeared in the system
        //after launching th MIDI Tell-Match app or to see if some of the used devices are gone.
        refreshMidiSystemButton.addActionListener(e -> {
            //Refreshing the lists of devices.
            try {
                InputDevicesList.getInputDevicesList().refreshInputDevicesList();
            } catch (MidiUnavailableException e19) {
                JOptionPane.showMessageDialog(sources, e19.toString());
            }
            try {
                OutputDevicesList.getOutputDevices().refreshOutputDevices();
            } catch (MidiUnavailableException e20) {
                JOptionPane.showMessageDialog(sources, e20.toString());
            }
            //Filling the sources and destinations JLists with actual data.
            fillSourcesAndDestinations();
            for (int i = 0; i < tellMatchModel.size(); i++) {
                MidiTellMatch tellMatch = tellMatchModel.get(i);
                tellMatch.restoreSource(InputDevicesList.getInputDevicesList());
                tellMatch.restoreDestinations(OutputDevicesList.getOutputDevices());
                tellMatch.setIndices();
            }
        });

        //Setting the behaviour of the radixSwitch.
        // will listen to the radix changes.
        radixSwitch.addItemListener(e -> {
            for (int i = 0; i < midiTellMatches.getModel().getSize(); i++) {
                MidiTellMatch tellMatch = midiTellMatches.getModel().getElementAt(i);
                byte ghostRadix = tellMatch.getRadix();
                if (ghostRadix != (byte) radixSwitch.getModel().getSelectedItem()) {
                    tellMatch.setRadix((Byte) radixSwitch.getModel().getSelectedItem());
                    if (tellMatch.getOutgoingMessage() != null) {
                        tellMatch.setOutgoingString(MessageDecoder.decode(tellMatch.getOutgoingMessage(),
                                tellMatch.getRadix()));
                    }
                    if (tellMatch.getTriggerMessage() != null) {
                        tellMatch.setTriggerString(MessageDecoder.decode(tellMatch.getTriggerMessage(),
                                tellMatch.getRadix()));
                    }
                    triggerMessage.setText(currentTellMatch.getTriggerString());
                    outgoingMessage.setText(currentTellMatch.getOutgoingString());
                }
            }
        });


        //Setting the behaviour of the inputChannelSwitch.
        inputChannelSwitch.addActionListener(e -> {
            byte newChannel = (byte) inputChannelSwitch.getModel().getSelectedItem();
            //Reconstructing trigger message with new channel.
            midiTellMatches.getSelectedValuesList().stream().filter(tellMatch ->
                    tellMatch.getInputChannel() != newChannel
                            && (!checkStatus(tellMatch.getTriggerType()))).forEach(tellMatch -> {
                tellMatch.setInputChannel((Byte) inputChannelSwitch.getModel().getSelectedItem());
                //Reconstructing trigger message with new channel.
                if (tellMatch.getTriggerMessage() != null) {
                    try {
                        ShortMessage message = (ShortMessage) tellMatch.getTriggerMessage();
                        tellMatch.setTriggerMessage(new ShortMessage(tellMatch.getTriggerType().getStatusByte(),
                                (tellMatch.getInputChannel() - 1), message.getData1(), message.getData2()));
                        tellMatch.setTriggerString(MessageDecoder.decode(tellMatch.getTriggerMessage(),
                                tellMatch.getRadix()));
                    } catch (InvalidMidiDataException e20) {
                        showInvalidMidiDataDialog(e20);
                    }
                }
            });
            triggerMessage.setText(currentTellMatch.getTriggerString());
            if (linkChannelsCheckBox.isSelected() && outputChannelSwitch.isEnabled()) {
                outputChannelSwitch.setSelectedItem(inputChannelSwitch.getSelectedItem());
            }
        });

        //Setting the behaviour of the outputChannelSwitch.
        outputChannelSwitch.addActionListener(e -> {
            byte newChannel = (byte) outputChannelSwitch.getModel().getSelectedItem();
            //Reconstructing outgoing message with new channel.
            midiTellMatches.getSelectedValuesList().stream().filter(tellMatch ->
                    tellMatch.getOutputChannel() != newChannel
                            && (!checkStatus(currentTellMatch.getOutgoingType()))).forEach(tellMatch -> {
                tellMatch.setOutputChannel((Byte) outputChannelSwitch.getModel().getSelectedItem());
                //Reconstructing outgoing message with new channel.
                if (tellMatch.getOutgoingMessage() != null) {
                    try {
                        ShortMessage message = (ShortMessage) tellMatch.getOutgoingMessage();
                        tellMatch.setOutgoingMessage(new ShortMessage(tellMatch.getOutgoingType().getStatusByte(),
                                (tellMatch.getOutputChannel() - 1), message.getData1(), message.getData2()));
                        tellMatch.setOutgoingString(MessageDecoder.decode(tellMatch.getOutgoingMessage(),
                                tellMatch.getRadix()));
                    } catch (InvalidMidiDataException e20) {
                        showInvalidMidiDataDialog(e20);
                    }
                }
            });
            outgoingMessage.setText(currentTellMatch.getOutgoingString());
            if (linkChannelsCheckBox.isSelected() && inputChannelSwitch.isEnabled()) {
                inputChannelSwitch.setSelectedItem(outputChannelSwitch.getSelectedItem());
            }
        });

        //Common selection listener for sources and destinations lists.
        //This listener sets the addTelMatchButton enabled or disabled.
        final ListSelectionListener listener1 = e -> {
            if (!e.getValueIsAdjusting()) {
                if (sources.getSelectedIndex() == -1
                        || destinations.getSelectedIndex() == -1) {
                    addTellMatchButton.setEnabled(false);
                } else {
                    addTellMatchButton.setEnabled(true);
                }
            }
        };
        sources.addListSelectionListener(listener1);
        destinations.addListSelectionListener(listener1);

        //Setting the behaviour of latencyCompensation JSpinner.
        // This spinner sets the latency for selected Tell-Matches.
        latencyCompensation.addChangeListener(e -> {
            if (latencyCompensation.getValue() != null) {
                Double comp = (Double) latencyCompensation.getModel().getValue();
                for (MidiTellMatch tellMatch : midiTellMatches.getSelectedValuesList()) {
                    tellMatch.setLatencyComp(comp);
                }
            }
        });

        //Setting the behaviour of the addTellMatchButton.
        //This button creates a new Tell-Match and adds it to the list
        addTellMatchButton.addActionListener(e -> {
            MidiTellMatch tellMatch;
            try {
                tellMatch = new MidiTellMatch(
                        sources.getSelectedValue(), (ArrayList<MidiDevice>) destinations.getSelectedValuesList(),
                        thisApp, (byte) radixSwitch.getModel().getSelectedItem());
                tellMatchModel.addElement(tellMatch);
            } catch (MidiUnavailableException e1) {
                showDeviceUnavailableDialog(e1);
            }
            if (!detectChannelCheckBox.isEnabled()) {
                detectChannelCheckBox.setEnabled(true);
            }
            tellMatchesDimension.height += 15;
            midiTellMatches.setSelectedIndex(tellMatchModel.getSize() - 1);
            midiTellMatches.ensureIndexIsVisible(tellMatchModel.getSize() - 1);
            triggerType.setSelectedIndex(-1);
            outgoingType.setSelectedIndex(-1);
            //Trying to connect the new Tell-Match to existing MidiMonitors.
            reconnectMonitor();
        });

        //Redraws all GUI elements that connected to the selected MIDI Tell-Match data
        // in case of selection changes.
        midiTellMatches.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (midiTellMatches.getSelectedIndex() == -1) {
                    //Disabling Tell-Match-associated controls if no Tell-Match is selected.
                    removeButton.setEnabled(false);
                    listenButton.setEnabled(false);
                    outgoingMessage.setEnabled(false);
                    triggerMessage.setEnabled(false);
                    triggerType.setEnabled(false);
                    outgoingType.setEnabled(false);
                    radixSwitch.setEnabled(false);
                    saveButton.setEnabled(false);
                    latencyCompensation.setEnabled(false);
                    inputChannelSwitch.setEnabled(false);
                    outputChannelSwitch.setEnabled(false);
                    ignoreVelocityBox.setEnabled(false);
                    detectChannelCheckBox.setEnabled(false);
                    linkChannelsCheckBox.setEnabled(false);
                    resetInOutButton.setEnabled(false);
                } else {
                    //Enabling Tell-Match associated controls if a Tell-Match is selected
                    removeButton.setEnabled(true);
                    sources.clearSelection();
                    destinations.clearSelection();
                    outgoingMessage.setEnabled(true);
                    triggerMessage.setEnabled(true);
                    outgoingType.setEnabled(true);
                    triggerType.setEnabled(true);
                    radixSwitch.setEnabled(true);
                    saveButton.setEnabled(true);
                    latencyCompensation.setEnabled(true);
                    inputChannelSwitch.setEnabled(true);
                    outputChannelSwitch.setEnabled(true);
                    ignoreVelocityBox.setEnabled(true);
                    detectChannelCheckBox.setEnabled(true);
                    linkChannelsCheckBox.setEnabled(true);
                    currentTellMatch = midiTellMatches.getSelectedValuesList().get(0);
                    resetInOutButton.setEnabled(true);
                    listenButton.setEnabled(true);

                    //Selecting or deselecting ignoreVelocityCheckBox according to the selected Tell-Match data.
                    if (currentTellMatch.isIgnoreVelocity()) {
                        ignoreVelocityBox.setSelected(true);
                    } else {
                        ignoreVelocityBox.setSelected(false);
                    }

                    //Selecting current Tell-Match's sources and destinations in required lists.
                    sources.setSelectedIndex(currentTellMatch.sourceIndex);
                    int[] selectionIndexes = new int[currentTellMatch.destinationsIndexes.size()];
                    for (int i = 0; i < currentTellMatch.destinationsIndexes.size(); i++) {
                        selectionIndexes[i] = currentTellMatch.destinationsIndexes.get(i);
                    }
                    destinations.setSelectedIndices(selectionIndexes);

                    //Redrawing other GUI elements.
                    if (currentTellMatch.getOutgoingString() != null) {
                        outgoingMessage.setText(currentTellMatch.getOutgoingString());
                    } else {
                        outgoingMessage.setText("");
                    }
                    if (currentTellMatch.getTriggerString() != null) {
                        triggerMessage.setText(currentTellMatch.getTriggerString());
                    } else {
                        triggerMessage.setText("");
                    }
                    if (currentTellMatch.getTriggerType() != null) {
                        triggerTypeModel.setSelectedItem(currentTellMatch.getTriggerType());
                    } else {
                        triggerTypeModel.selectNothing();
                    }
                    if (currentTellMatch.getOutgoingType() != null) {
                        outgoingTypeModel.setSelectedItem(currentTellMatch.getOutgoingType());
                    } else {
                        outgoingTypeModel.selectNothing();
                    }

                    //Enabling or disabling channel switches according to trigger and outgoing message types.
                    if (checkStatus(currentTellMatch.getTriggerType())) {
                        inputChannelSwitch.setEnabled(false);
                    } else {
                        inputChannelSwitch.setEnabled(true);
                        inputChannelSwitch.setSelectedItem(currentTellMatch.getInputChannel());
                    }
                    if (checkStatus(currentTellMatch.getOutgoingType())) {
                        outputChannelSwitch.setEnabled(false);
                    } else {
                        outputChannelSwitch.setEnabled(true);
                        outputChannelSwitch.setSelectedItem(currentTellMatch.getOutputChannel());
                    }
                    //Redrawing other GUI elements.
                    if (currentTellMatch.getLatencyComp() != - 1) {
                        latencyCompensation.setValue(currentTellMatch.getLatencyComp()/1000 * 1.0);
                    } else {
                        latencyCompensation.setValue(0.0);
                    }
                    radixSwitch.setSelectedItem(currentTellMatch.getRadix());
                    detectChannelCheckBox.setSelected(currentTellMatch.getDetectMidiChannel());
                }
            }
        });

        //Setting behaviour of ignoreVelocityBox, which
        // sets ignoreVelocity param for selected Tell-Matches.*/
        ignoreVelocityBox.addActionListener(e -> {
            for (MidiTellMatch tellMatch : midiTellMatches.getSelectedValuesList()) {
                tellMatch.setIgnoreVelocity(ignoreVelocityBox.isSelected());
            }
        });

        //Setting behaviour of the saveButton, which
        // triggers saving of selected MIDI Tell-Matches to a preset.
        saveButton.addActionListener(e -> {
            PresetSaver.setVisible(true);
            StringBuilder tmpName = null;
            if (PresetSaver.getFile() != null) {
                tmpName = new StringBuilder(PresetSaver.getFile());
            }
            if (tmpName != null) {
                if (!String.valueOf(tmpName).contains(".pst")) {
                    tmpName.append(".pst");
                }
                File toSave = new File(presetsFolder + "/" + String.valueOf(tmpName));
                try {
                    GlobalPresetManager.saveGlobal(midiTellMatches.getSelectedValuesList(), toSave);
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
                presetModel.clear();
                fillPresetBox(presetModel);
            }
        });

        //Setting behaviour of the presetBox, which
        // triggers loading of a selected preset.
        presetBox.addActionListener(e -> {
            boolean sourcesAvailable = sourceModel.getSize() > 0;
            boolean destinationsAvailable = destModel.getSize() > 0;
            if (sourcesAvailable && destinationsAvailable) {
                if (!addOrReplaceCheckBox.isSelected()) {
                    loadPreset(presetModel.getSelectedItem());
                } else {
                    tellMatchesDimension.height -= (15 * tellMatchModel.getSize());
                    tellMatchModel.clear();
                    loadPreset(presetModel.getSelectedItem());
                }
            } else {
                if (destinationsAvailable || sourcesAvailable) {
                    showNoDevicesDialog(sourcesAvailable ? "destinations" : "sources");
                } else {
                    showNoDevicesDialog("devices");
                }
            }

        });
        //Setting behaviour of the removeButton, which
        // removes selected Tell-Matches from the list and sets new selection.*/
        removeButton.addActionListener(e -> {
            //Defining which Tell-Matches we want to remove.
            int[] indices = midiTellMatches.getSelectedIndices();
            int nextSelection = 0;
            //Trying to find a Tell-Match to select.
            MidiTellMatch possibleSelection = null;
            if (indices.length > 1) {
                int toSelect = 0;
                boolean isFound = false;
                for (int i = indices.length - 1; i >= 0; i--) {
                    toSelect = indices[i] + 1;
                    for (int j : indices) {
                        isFound = true;
                        if (j == toSelect || toSelect > tellMatchModel.getSize() - 1) {
                            isFound = false;
                            break;
                        }
                    }
                    if (isFound) {
                        possibleSelection = tellMatchModel.getElementAt(toSelect);
                        break;
                    }
                }
            } else {
                int selected = indices[0];
                if (selected == tellMatchModel.getSize() - 1) {
                    nextSelection = selected - 1;
                } else {
                    nextSelection = selected;
                }
            }
            //Removing Tell-Matches.
            try {
                for (MidiTellMatch tellMatch : midiTellMatches.getSelectedValuesList()) {
                    MidiTellMatch.setTellMatchCount(MidiTellMatch.getTellMatchCount() - 1);
                    tellMatchModel.removeElement(tellMatch);
                    tellMatchesDimension.height -= 15;
                }
                //Setting new selection.
                if (indices.length > 1) {
                    if (possibleSelection == null) {
                        possibleSelection = tellMatchModel.getElementAt(0);
                    }
                    midiTellMatches.setSelectedValue(possibleSelection, true);
                } else {
                    if (nextSelection < tellMatchModel.getSize()) {
                        midiTellMatches.setSelectedIndex(nextSelection);
                    } else if (tellMatchModel.getSize() > 0) {
                        midiTellMatches.setSelectedIndex(0);
                    } else {
                        midiTellMatches.setSelectedIndex(-1);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e2) {
                System.out.println("Impossible");
            }
            if (MidiTellMatch.getTellMatchCount() == 0) {
                detectChannelCheckBox.setEnabled(false);
            }

            removeButton.setSelected(false);

        });

        //Setting behaviour of the listenButton, which
        // allows selected Tell-Matches to listen for incoming MIDI data in order to set their trigger message.
        listenButton.addItemListener(ev -> {
            if (ev.getStateChange() == ItemEvent.SELECTED) {
                triggerMessage.setText("");
                for (MidiTellMatch tellMatch : midiTellMatches.getSelectedValuesList()) {
                    tellMatch.setListening(true);
                }
            } else if (ev.getStateChange() == ItemEvent.DESELECTED) {
                if (someOneIsListening()) {
                    for (MidiTellMatch tellMatch : midiTellMatches.getSelectedValuesList()) {
                        tellMatch.setListening(false);
                    }
                }
                triggerMessage.setText(currentTellMatch.getTriggerString());
            }
        });

        //Setting behaviour of the outgoingType JComboBox, which
        // switches outgoing message type for selected Tell-Matches.
        outgoingType.addItemListener(e -> {
            if (outgoingTypeModel.getSelectedItem() != null) {
                checkTypeAndDisable(outgoingTypeModel, outputChannelSwitch);
                StatusByte status = outgoingTypeModel.getSelectedItem();
                midiTellMatches.getSelectedValuesList().stream().filter(tellMatch ->
                        tellMatch.getOutgoingType() != status).forEach(tellMatch -> {
                    outgoingMessage.setText(status.getStatusString(currentTellMatch.getRadix(),
                            currentTellMatch.getOutputChannel()));
                    tellMatch.setOutgoingType(status);
                    tellMatch.setOutgoingMessage(null);
                    tellMatch.setOutgoingString("");
                });
            }

        });

        //Setting behaviour of the TriggerType JComboBox, which
        // switches trigger message type for selected Tell-Matches.
        triggerType.addItemListener(e -> {
            if (triggerTypeModel.getSelectedItem() != null) {
                checkTypeAndDisable(triggerTypeModel, inputChannelSwitch);
                StatusByte status =  triggerTypeModel.getSelectedItem();
                midiTellMatches.getSelectedValuesList().stream().filter(tellMatch ->
                        tellMatch.getTriggerType() != status).forEach(tellMatch -> {
                    triggerMessage.setText(status.getStatusString(currentTellMatch.getRadix(),
                            currentTellMatch.getInputChannel()));
                    tellMatch.setTriggerType(status);
                    tellMatch.setTriggerMessage(null);
                    tellMatch.setTriggerString("");
                });

            }
        });

        //Setting behaviour of the outgoingMessage JTextField, which
        // tries to set the outgoing message for selected MIDI Tell-Matches
        // by encoding it from text in outgoingMessage JTextField.
        outgoingMessage.addActionListener(e -> encodeMessage(outgoingMessage, outgoingTypeModel, outputChannelSwitch, "outgoing"));

        //Setting behaviour of the triggerMessage JTextField, which
        // tries to set the trigger message for selected MIDI Tell-Matches
        // by encoding it from text in triggerMessage JTextField.
        triggerMessage.addActionListener(e -> encodeMessage(triggerMessage, triggerTypeModel, inputChannelSwitch, "trigger"));

        //Setting behaviour of the resetInOutButton, which
        // tries to change sources and destinations of selected MIDI Tell-Matches
        // according to selection in sources and destinations lists after pushing the resetInOutButton.
        resetInOutButton.addActionListener(e -> {
            if (sources.getSelectedIndex() == -1 || destinations.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(midiTellMatches, "Select a source and at least one destination to set");
            } else {
                for (MidiTellMatch tellMatch : midiTellMatches.getSelectedValuesList()) {
                    if (resetSource(tellMatch)) {
                        resetDestinations(tellMatch);
                        tellMatch.setIndices();
                    } else if (resetDestinations(tellMatch)) {
                        tellMatch.setIndices();
                    }
                }
                reconnectMonitor();
            }
        });

        //Setting behaviour of the detectChannelCheckBox, which
        // sets the detectMidiChannel parameter for selected MIDI Tell-Matches.
        detectChannelCheckBox.addActionListener(e -> {
            for (MidiTellMatch tellMatch : midiTellMatches.getSelectedValuesList()) {
                tellMatch.setDetectMidiChannel(detectChannelCheckBox.isSelected());
            }
        });

        //Adding the possibility to rename a MIDI Tell-Match by double-clicking on it's name in the list.*/
        midiTellMatches.addMouseListener(new MouseInputListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String tmp = currentTellMatch.getName();
                    currentTellMatch.setName(JOptionPane.showInputDialog(
                            "Rename this Tell-Match?", JOptionPane.OK_CANCEL_OPTION));
                    if (currentTellMatch.getName() == null) {
                        currentTellMatch.setName(tmp);
                    }
                }
            }
            //We don't actually need this.
            @Override
            public void mousePressed(MouseEvent e) {
            }
            //We don't actually need this.
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            //We don't actually need this.
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            //We don't actually need this.
            @Override
            public void mouseExited(MouseEvent e) {
            }
            //We don't actually need this.
            @Override
            public void mouseDragged(MouseEvent e) {
            }
            //We don't actually need this.
            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        pack();
        setMaximumSize(new Dimension(900, 400));
        setMinimumSize(new Dimension(800, 350));
        setVisible(true);
    }

    /**
     * Encodes a string from a message JTextField to a MIDI-message then sets this message and it's
     * data to selected <code>MidiTellMatch</code>es according to key and redraws required GUI components according to
     * this data.
     * @see MidiTellMatch#setMessageData(MidiMessage, byte, StatusByte, String, String)
     * @param messageField  a textField where is the string to encode.
     * @param messageTypeModel  a controller of JComboBox where encoded StatusByteSwitch will be selected.
     * @param channelSwitch  a JComboBox where encoded channel will be selected
     * @param key  a string that defines which message whe must encode (trigger or outgoing)
     */
    private void encodeMessage(JTextField messageField, MessageSwitchModel messageTypeModel,
                               JComboBox<Byte> channelSwitch, String key) {
        String toEncode = messageField.getText();
        //Trying to encode the channel with each radix. encodeChannel() method returns -1 if radix is incorrect;
        byte possibleRadix = 16;
        byte channel = MessageEncoder.encodeChannel(toEncode, possibleRadix);
        if (channel == -1) {
            possibleRadix = 10;
            channel = MessageEncoder.encodeChannel(toEncode, possibleRadix);
        }

        //Getting the backUp string just in case.
        String toRestore = currentTellMatch.getTriggerString();
        if (key.equals("outgoing")) {
            toRestore = currentTellMatch.getOutgoingString();
        }

        //If both of radix values is wrong, entered data seems to be incorrect.
        if (channel == -1) {
            showInvalidMidiDataDialog(new InvalidMidiDataException("Invalid Input"));
            messageField.setText(toRestore);
        } else {
            //Trying to define the status byte of entered message. encodeStatus() method returns StatusByte.WRONG
            // in case if entered message is incorrect.
            StatusByte status = MessageEncoder.encodeStatus(toEncode, possibleRadix, channel);
            if (status != StatusByte.WRONG) {
                for (MidiTellMatch tellMatch : midiTellMatches.getSelectedValuesList()) {
                    //If a Tell-Match and entered message has different radix, user may change radix
                    // according to incoming data or stop parsing.
                    if (tellMatch.getRadix() != possibleRadix) {
                        int changeRadix = JOptionPane.showConfirmDialog(this, "The radix of entered message is " +
                                possibleRadix + "\n" + "Do you want to change Tell-Match's radix " +
                                "according to entered data?", "Radix is different!", JOptionPane.YES_NO_OPTION);
                        if (changeRadix == JOptionPane.YES_OPTION) {
                            radixSwitch.setSelectedItem(possibleRadix);
                        } else {
                            messageField.setText(toRestore);
                            break;
                        }
                    }
                    //If everything goes ok, let's try to encode the string to a MIDI-message.
                    try {
                        MidiMessage message = MessageEncoder.encode(toEncode, possibleRadix, status, channel);
                        //Sending the message and it's data to the Tell-Match.
                        tellMatch.setMessageData(message, channel, status, toEncode, key);
                        //Redrawing required GUI elements.
                        messageField.setText(toEncode);
                        messageTypeModel.setSelectedItem(status);
                        channelSwitch.setSelectedItem(channel);
                        //And if something gone wrong it's better to stop whole process.
                    } catch (InvalidMidiDataException e24) {
                        showInvalidMidiDataDialog(e24);
                        messageField.setText(toRestore);
                        break;
                    }
                }
            } else {
                showInvalidMidiDataDialog(new InvalidMidiDataException("Invalid input"));
                messageField.setText(toRestore);
            }

        }
    }


    /**
     *Receives available sources and destinations. In case if CoreMidi4J library is loaded,
     * only it's implementations of MIDI-devices will be in use.
     * @see //github.com/DerekCook/CoreMidi4J.git (for Mac users only)
     */
    private void fillSourcesAndDestinations() {
        sourceModel.clear();
        destModel.clear();
        for (MidiDevice source : InputDevicesList.getInputDevicesList()) {
            sourceModel.addElement(source);
        }
        for (MidiDevice dest : OutputDevicesList.getOutputDevices()) {
            destModel.addElement(dest);
        }
    }

    /**
     * Populates the presetBox JComboBox with files from the presetFolder
     * @param presetModel  the controller of presetBox.
     */
    public void fillPresetBox(PresetModel presetModel) {
        if (presetsFolder != null && presetsFolder.isDirectory() && presetsFolder.listFiles() != null) {
            for (File preset : presetsFolder.listFiles()) {
                if (preset.getName().contains(".pst")) {
                    presetModel.addElement(preset);
                    presetModel.setSelectedItem(preset);
                }
            }
        }
    }

    /**
     * Redraws GUI elements connected to {@link MainView#currentTellMatch#triggerMessage}
     * in case if listening on source after pushing the {@link MainView#listenButton}
     * was successful.
     */
    public void redrawTriggerGUIElements() {
        triggerType.setSelectedItem(currentTellMatch.getTriggerType());
        triggerTypeModel.showSelected();
        inputChannelSwitch.setSelectedItem(currentTellMatch.getInputChannel());
    }
    /**
     * Shows a message dialog in case if listening for a MIDI-message on a source of
     * current MIDI Tell-Match was not successful because of wrong MIDI-channel settings
     */
    public void showWrongChannelDialog(byte requiredChannel, byte detectedChannel) {
        JOptionPane.showMessageDialog(new JDialog(), "Incoming message use MIDI-channel " + detectedChannel
                + "\nPlease, set current Tell-Match's input channel to  " + detectedChannel
                + "\nor change MIDI-channel in properties of your MIDI-device to " + requiredChannel);
    }

    /**
     * Changes destinations in received MIDI Tell-Match according to the selection in destinations JList.
     * @param tellMatch  a MIDI Tell-Match whose destinations must be changed.
     * @return true in case of success.
     */
    public boolean resetDestinations(MidiTellMatch tellMatch) {
        boolean isChanged = false;
        for (MidiDevice aDest : destinations.getSelectedValuesList()) {
            isChanged = false;
            for (MidiDevice currentDest : tellMatch.getDestinations()) {
                if (aDest != currentDest) {
                    isChanged = true;
                    break;
                }
            }
        }
        if (isChanged) {
            try {
                tellMatch.setDestinations(destinations.getSelectedValuesList());
            } catch (MidiUnavailableException e15) {
                JOptionPane.showMessageDialog(destinations, "Some destinations became unavailable");
            }
        }
        return isChanged;
    }

    /**
     * Checks if one of selected <code>MidiTellMatch</code>es is listening at the moment
     * @return true if at least one of <code>MidiTellMatch</code>es is listening.
     */
    public boolean someOneIsListening() {
        boolean someOneIsListening = false;
        for (MidiTellMatch tellMatch: midiTellMatches.getSelectedValuesList()) {
            if (tellMatch.isListening()) {
                someOneIsListening = true;
                break;
            }
        }
        return  someOneIsListening;
    }

    /**
     * Changes source in received MIDI Tell-Match according to the selection in sources JList.
     * @param tellMatch  a MIDI Tell-Match whose source must be changed.
     * @return true in case of success.
     */
    public boolean resetSource(MidiTellMatch tellMatch) {
        boolean isChanged = false;
        try {
            if (!tellMatch.getSource().equals(sources.getSelectedValue())) {
                tellMatch.setSource(sources.getSelectedValue());
                isChanged = true;

            }
        } catch (MidiUnavailableException e16) {
            JOptionPane.showMessageDialog(destinations, "Source became unavailable");
        }
        return isChanged;
    }

    /**
     * Returns a link to the listenButton in order to provide
     * the possibility to deselect it from other class.
     * @return listenButton.
     */
    public JToggleButton getListenButton() {
        return listenButton;
    }

    /**
     * Returns the list of existing sources.
     * @return an <code>ArrayList</code> of existing sources.
     */
    public ArrayList<MidiDevice> getSources() {
        ArrayList<MidiDevice> sources = new ArrayList<>();
        for (int i = 0; i < sourceModel.size(); i++) {
            sources.add(sourceModel.getElementAt(i));
        }
        return sources;
    }
    /**
     * Returns the list of existing destinations.
     * @return an <code>ArrayList</code> of existing destinations.
     */
    public ArrayList<MidiDevice> getDestinations() {
        ArrayList<MidiDevice> destinations = new ArrayList<>();
        for (int i = 0; i < destModel.size(); i++) {
            destinations.add(destModel.getElementAt(i));
        }
        return destinations;
    }

    /**
     * Sends received preset file and a link to current instance of MainView to the {@link GlobalPresetManager}
     * in order to get a list of <code>MidiTellMatch</code>es reconstructed from the preset and add them into the
     * the list of <code>MidiTellMatch</code>es.
     * @param toLoad — a preset to load
     */
    public void loadPreset(File toLoad) {
        try {
            ArrayList<MidiTellMatch> tellMatchToLoad = GlobalPresetManager.loadGlobal(thisApp, toLoad);
            for (MidiTellMatch tellMatch : tellMatchToLoad) {
                tellMatchModel.addElement(tellMatch);
                tellMatchesDimension.height += 15;
            }
        } catch (IOException e0) {
            e0.printStackTrace();
        } catch (MidiUnavailableException e25) {
            showDeviceUnavailableDialog(e25);
        } catch (InvalidMidiDataException e26) {
            showInvalidMidiDataDialog(e26);
        }

    }

    /**
     * Checks the selected item in trigger or outgoing type JComboBox according to the received controller
     * and sets input or output channel JComboBox disabled if !{@link MainView#checkStatus(StatusByte)}
     * @param model  the controller of a JComboBox<StatusByteSwitch>.
     * @param channelSwitch  a JComboBox<Byte> to enable or disable.
     */
    private void checkTypeAndDisable(MessageSwitchModel model, JComboBox<Byte> channelSwitch) {
        StatusByte status = model.getSelectedItem();
        channelSwitch.setEnabled(!checkStatus(status));
    }

    /**
     * Checks if received StatusByte is not a statusByte of a {@link ShortMessage}*
     * @param status  a StatusByte to check.
     * @return  true if status is not a statusByte of a ShortMessage.
     */
    private boolean checkStatus(StatusByte status) {
        return status == StatusByte.SYSTEM_EXCLUSIVE
                || status == StatusByte.SPECIAL_SYSTEM_EXCLUSIVE
                || status == StatusByte.META
                || status == StatusByte.WRONG;
    }

    /**
     * Shows a message about an MidiUnavailableException.
     * Called in case if some any devices are unavailable.
     * @param exception  an exception that will be represented in the message.
     */
    public void showDeviceUnavailableDialog(MidiUnavailableException exception) {
        JOptionPane.showMessageDialog(this,exception);
    }

    /**
     * Shows a message about an InvalidMidiDataException.
     * Called in case of incorrect MIDI-data input.
     * @param exception  an exception that will be represented in the message.
     */
    public void showInvalidMidiDataDialog(InvalidMidiDataException exception) {
        JOptionPane.showMessageDialog(this,exception);
    }

    /**
     * Shows a message which says that no sources and/or destinations detected in the system.
     */
    private void showNoDevicesDialog(String deviceType) {
        JOptionPane.showMessageDialog(this, "No " + deviceType + " are available in the system. \n" +
                "Please, connect any " + deviceType + " and try again");
    }

    /**
     * Returns all existing <code>MidiTellMatch</code>es in a <code>List</code>.
     * @return a <code>List</code>* of existing <code>MidiTellMatch</code>es.
     */
    private Set<MidiTellMatch> getAllTellMatches() {
        Set<MidiTellMatch> tellMatches = new HashSet<>();
        for (int i = 0; i < tellMatchModel.getSize(); i++) {
            tellMatches.add(tellMatchModel.getElementAt(i));
        }
        return tellMatches;
    }

    /**
     * Connects existing <code>MidiMonitor</code>s to the <code>MidiTellMatches</code> after changing
     * those destinations.
     * */
    private void reconnectMonitor() {
        if (monitorList != null && monitorList.size() > 0) {
            for (MidiMonitor monitor : monitorList) {
                monitor.rescanDestinations(getAllTellMatches());
            }
        }
    }

    /**
     * Returns all existing <code>MidiMonitor</code>s in a <code>List</code>.
     * @return a <code>List</code> of existing <code>MidiMonitor</code>s.
     */
    public List<MidiMonitor> getMonitorList() {
        return  monitorList;
    }

    public static void main(String[] args) {
        MainView
                main = new MainView();
    }

}

