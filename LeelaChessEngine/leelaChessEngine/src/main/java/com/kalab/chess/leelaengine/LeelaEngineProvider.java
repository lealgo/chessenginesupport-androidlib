package com.kalab.chess.leelaengine;

import com.kalab.chess.enginesupport.ChessEngineProvider;

import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LeelaEngineProvider extends ChessEngineProvider {

    @Override
    public ParcelFileDescriptor openLibFile(File f) throws FileNotFoundException {

        if (getContext() != null && getContext().getAssets() != null && f.getName().endsWith("liblc0.so"))
        {
            try {
                // Create temp file
                File outputFile = File.createTempFile("prefix", "extension");
                OutputStream os = new FileOutputStream(outputFile);

                // Add engine
                InputStream engine = new FileInputStream(f);
                pipe(engine, os);

                // Add weights
                InputStream weights = getContext().getAssets().open("networks/embed.bin");
                pipe(weights, os);

                // Close and return temp file
                os.flush();
                os.close();
                return ParcelFileDescriptor.open(outputFile, ParcelFileDescriptor.MODE_READ_ONLY);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return super.openLibFile(f);
    }

    static void pipe(InputStream is, OutputStream os)
    {
        try {
            byte[] buf = new byte[8192];
            while (true) {
                int len = is.read(buf);
                if (len <= 0)
                    break;
                os.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
