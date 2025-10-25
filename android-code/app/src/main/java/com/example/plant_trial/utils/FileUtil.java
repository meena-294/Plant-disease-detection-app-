package com.example.plant_trial.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtil {

    public static MappedByteBuffer loadMappedFile(Context context, String fileName) throws IOException {
        try (AssetFileDescriptor fileDescriptor = context.getAssets().openFd(fileName);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {

            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();

            // Map the file to the memory, which is much faster than reading it byte by byte
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }
}
