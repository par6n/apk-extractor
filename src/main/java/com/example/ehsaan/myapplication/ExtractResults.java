package com.example.ehsaan.myapplication;

import java.io.File;

public class ExtractResults {
    public File file;
    public final boolean result;

    public ExtractResults( boolean result ) {
        this.result = result;
    }

    public void setFile( File file ) {
        this.file = file;
    }
}
