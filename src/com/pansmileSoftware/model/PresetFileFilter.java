package com.pansmileSoftware.model;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * This class is made for checking if a directory contains preset files.
 */
public class PresetFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        boolean isAccepted = false;
        if (f.isDirectory() && f.listFiles() != null) {
            for (File file : f.listFiles()) {
                if (file.getName().contains(".pst")) {
                    isAccepted = true;
                    break;
                }
            }
        } else {
            if (f.getName().contains(".pst")) {
                isAccepted = true;
            }
        }
        return isAccepted;
    }

    @Override
    public String getDescription() {
        return "MIDI Tell-Match preset";
    }
}

