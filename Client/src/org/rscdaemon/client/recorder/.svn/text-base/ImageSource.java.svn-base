package org.rscdaemon.client.recorder;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

public class ImageSource extends PullBufferDataSource {
	
	public ImageSource(int width, int height, float frameRate, LinkedList<BufferedImage> frames) {
		streams[0] = new ImageStream(width, height, frameRate, frames);
	}
	
	public void setLocator(MediaLocator medialocator) {
	}
	
	public MediaLocator getLocator() {
		return null;
	}
	
	public String getContentType() {
		return "raw";
	}
	
	public void connect() {
	}
	
	public void disconnect() {
	}
	
	public void start() {
	}
	
	public void stop() {
	}
	
	public PullBufferStream[] getStreams() {
		return streams;
	}
	
	public Time getDuration() {
		return DURATION_UNKNOWN;
	}
	
	public Object[] getControls() {
		return new Object[0];
	}
	
	public Object getControl(String s1) {
		return null;
	}
	
	private ImageStream[] streams = new ImageStream[1];
}
