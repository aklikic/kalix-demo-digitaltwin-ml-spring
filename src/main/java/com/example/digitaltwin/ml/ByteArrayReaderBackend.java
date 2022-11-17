package com.example.digitaltwin.ml;

import ai.h2o.mojos.runtime.api.backend.DirReaderBackend;
import ai.h2o.mojos.runtime.api.backend.ReaderBackend;
import ai.h2o.mojos.runtime.api.backend.ResourceInfo;
import ai.h2o.mojos.runtime.api.backend.ZipFileReaderBackend;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ByteArrayReaderBackend implements ReaderBackend {

    private final int length;
    private final String weakHash = "";
    private final ByteArrayInputStream bis;
    private Map<String,ReaderBackend> readerByName = new HashMap<>();

    public ByteArrayReaderBackend(byte[] data) {
        this.length = data.length;
        this.bis = new ByteArrayInputStream(data);
    }

    @Override
    public ResourceInfo getResourceInfo(String s) throws IOException {
        System.out.println("getResourceInfo: "+s);
        if(isMojo(s))
            return new ResourceInfo(length,weakHash);
        return getReader(s).getResourceInfo(s);

    }

    @Override
    public InputStream getInputStream(String s) throws IOException {
        System.out.println("getInputStream: "+s);
        if(isMojo(s))
            return bis;
        return getReader(s).getInputStream(s);
    }

    private boolean isMojo(String s){
       return s.endsWith(".mojo");
    }

    private ReaderBackend getReader(String s) throws IOException{
        if(readerByName.containsKey(s))
            return readerByName.get(s);
        ReaderBackend br = autodetectBackend(getFile(s));
        readerByName.put(s,br);
        return br;
    }

    private File getFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);

        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }
    }

    private static ReaderBackend autodetectBackend(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        } else if (file.isDirectory()) {
            return DirReaderBackend.open(file);
        } else if (file.isFile()) {
            return ZipFileReaderBackend.open(file);
        } else {
            throw new IOException("Unsupported file type: " + file.getAbsolutePath());
        }
    }

    @Override
    public boolean exists(String s) {
        return true;
    }

    @Override
    public Collection<String> list() {
        return new ArrayList<>();
    }

    @Override
    public void close() throws IOException {
        bis.close();
        readerByName.values().stream().forEach(br -> {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
