package com.pansmileSoftware.model;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * This class is a JFileChooser for presets folder.
 */
public class PresetsRootChooser extends JFileChooser {
    public PresetsRootChooser() {
        setDialogTitle("Choose presets folder");
        setDialogType(JFileChooser.SAVE_DIALOG);
        setFileSelectionMode(DIRECTORIES_ONLY);

        setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Folder";
            }
        });
        setApproveButtonText("Choose");

    }
}