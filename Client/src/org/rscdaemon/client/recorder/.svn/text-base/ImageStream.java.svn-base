package org.rscdaemon.client.recorder;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;

class ImageStream implements PullBufferStream {
	
	private LinkedList<BufferedImage> frames;
	private VideoFormat format;
	private boolean end;
	
	public ImageStream(int width, int height, float frameRate, LinkedList<BufferedImage> frames) {
		end = false;
		this.frames = frames;
		format = new VideoFormat("jpeg", new Dimension(width, height), -1, Format.byteArray, frameRate);
	}
	
	public boolean willReadBlock() {
		return frames.isEmpty();
	}
	
	public void read(Buffer buffer) throws IOException {
		while(frames.isEmpty()) {
			try { Thread.sleep(10L); } catch(Exception exception) { exception.printStackTrace(); }
		}
		BufferedImage image = frames.poll();
		if(image == null) {
			frames.clear();
			buffer.setEOM(true);
			buffer.setOffset(0);
			buffer.setLength(0);
			end = true;
			return;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "jpeg", out);
		buffer.setData(out.toByteArray());
		buffer.setOffset(0);
		buffer.setLength(out.size());
		buffer.setFormat(format);
		buffer.setFlags(buffer.getFlags() | 0x10);
		out.close();
	}
	
	public Format getFormat() {
		return format;
	}
	
	public ContentDescriptor getContentDescriptor() {
		return new ContentDescriptor("raw");
	}
	
	public long getContentLength() {
		return 0L;
	}
	
	public boolean endOfStream() {
		return end;
	}
	
	public Object[] getControls() {
		return new Object[0];
	}
	
	public Object getControl(String s) {
		return null;
	}
	
}
