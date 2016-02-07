package org.rscdaemon.client.recorder;

import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class Recorder implements Runnable, ControllerListener, DataSinkListener {

    private int width;
    private int height;
    private LinkedList<BufferedImage> frames;
    private Object dummy1 = new Object();
    private Object dummy2 = new Object();
    private boolean finished = false;
    private boolean configured = true;
    private float frameRate;
    private MediaLocator locator;
    private String format;

    public Recorder(int width, int height, float frameRate, LinkedList<BufferedImage> frames, String output, String format) {
        this.width = width;
        this.height = height;
        this.frames = frames;
        this.frameRate = frameRate;
        locator = new MediaLocator("file:" + output);
        this.format = format;
    }

    public void controllerUpdate(ControllerEvent e) {
        if ((e instanceof ConfigureCompleteEvent) || (e instanceof RealizeCompleteEvent) || (e instanceof PrefetchCompleteEvent)) {
            synchronized (dummy1) {
                configured = true;
                dummy1.notifyAll();
            }
        } else if (e instanceof ResourceUnavailableEvent) {
            synchronized (dummy1) {
                configured = false;
                dummy1.notifyAll();
            }
        } else if (e instanceof EndOfMediaEvent) {
            e.getSourceController().stop();
            e.getSourceController().close();
        }
    }

    private boolean waitForConfigure(Processor processor, int limit) {
        synchronized (dummy1) {
            try {
                for (; processor.getState() < limit && configured; dummy1.wait()) ;
            }
            catch (Exception e) {
            }
        }
        return configured;
    }

    public void run() {
        try {
            Processor processor = Manager.createProcessor(new ImageSource(width, height, frameRate, frames));
            processor.addControllerListener(this);
            processor.configure();
            processor.addControllerListener(this);
            processor.configure();
            if (!waitForConfigure(processor, 180)) {
                throw new Exception("Failed to configure the processor.");
            }

            processor.setContentDescriptor(new ContentDescriptor(format));
            TrackControl[] trackControls = processor.getTrackControls();
            Format[] supportedFormats = trackControls[0].getSupportedFormats();
            if (supportedFormats == null || supportedFormats.length <= 0) {
                throw new Exception("The mux does not support the input format " + trackControls[0].getFormat());
            }
            trackControls[0].setFormat(supportedFormats[0]);
            processor.realize();
            if (!waitForConfigure(processor, 300)) {
                throw new Exception("Failed to realize the processor.");
            }

            DataSink sink = createSink(processor, locator);
            if (sink == null) {
                throw new Exception("Failed to create a DataSink for the given output MediaLocator " + locator);
            }
            sink.addDataSinkListener(this);

            finished = false;
            processor.start();
            sink.start();

            block();

            sink.close();
            processor.removeControllerListener(this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DataSink createSink(Processor processor, MediaLocator locator) throws Exception {
        DataSource source = processor.getDataOutput();
        if (source == null) {
            throw new Exception("Something is really wrong: the processor does not have an output DataSource");
        }
        try {
            DataSink sink = Manager.createDataSink(source, locator);
            sink.open();
            return sink;
        }
        catch (Exception e) {
        }
        return null;
    }

    private void block() {
        synchronized (dummy2) {
            try {
                while (!finished) {
                    dummy2.wait();
                }
            }
            catch (Exception e) {
            }
        }
    }

    public void dataSinkUpdate(DataSinkEvent e) {
        if (e instanceof EndOfStreamEvent) {
            synchronized (dummy2) {
                finished = true;
                dummy2.notifyAll();
            }
        } else if (e instanceof DataSinkErrorEvent) {
            synchronized (dummy2) {
                finished = true;
                dummy2.notifyAll();
            }
        }
    }

}