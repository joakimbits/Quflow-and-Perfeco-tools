package org.opensourcephysics.media.xuggle;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.media.core.ScratchVideoRecorder;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.tools.ResourceLoader;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.ICodec.ID;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

public class XuggleVideoRecorder extends ScratchVideoRecorder {

	private IContainer outContainer;
	private IStream outStream;
	private IStreamCoder outStreamCoder;
	private IConverter outConverter;
	private IRational timebase = IRational.make(1, 9000);	
	private BufferedImage xuggleImage;
	private String tempFileBasePath;
	private String tempFileType = "png"; //$NON-NLS-1$

	/**
   * Constructs a XuggleVideoRecorder object.
   */
  public XuggleVideoRecorder(XuggleVideoType type) {
    super(type);
  }

  /**
   * Discards the current video and resets the recorder to a ready state.
   */
	@Override
  public void reset() {
    try {
			closeStream();
		} catch (IOException e) {}
		if (outConverter!=null) {
			outConverter.delete();
			outConverter = null;
		}
    deleteTempFiles();
    super.reset();
  }

  /**
   * Called by the garbage collector when this recorder is no longer in use.
   */
	@Override
  protected void finalize() {
  	reset();
  }
  
  /**
   * Appends a frame to the current video by saving the image in a tempFile.
   *
   * @param image the image to append
   * @return true if image successfully appended
   */
	@Override
	protected boolean append(Image image) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		if (dim==null) {
			dim = new Dimension(w, h);
		}
		// can't append images that are different size than first
		if (dim.width!=w || dim.height!=h)
			return false;
		// convert to BufferedImage if needed
		if (!(image instanceof BufferedImage)) {
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);			
			img.getGraphics().drawImage(image, 0, 0, null);
			image = img;
		}
		BufferedImage source = (BufferedImage)image;
		String fileName = tempFileBasePath+"_"+tempFiles.size()+".tmp"; //$NON-NLS-1$ //$NON-NLS-2$
    try {
			ImageIO.write(source, tempFileType, new BufferedOutputStream(
			    new FileOutputStream(fileName)));
		} catch (Exception e) {
			return false;
		}
		File imageFile = new File(fileName);
		if (imageFile.exists()) {
			synchronized (tempFiles) {
				tempFiles.add(imageFile);
			}
			imageFile.deleteOnExit();
		}
		return true;
	}

  /**
   * Saves the video to the current scratchFile.
   * 
   * @throws IOException
   */
	@Override
	protected void saveScratch() throws IOException {
		// get the FileFilter selected by the user
    FileFilter fileFilter = chooser.getFileFilter();
		if (!hasContent || !(fileFilter instanceof VideoFileFilter))
			return;
		
		// set container format
		IContainerFormat format = IContainerFormat.make();
		VideoFileFilter xuggleFilter = (VideoFileFilter)fileFilter;
		format.setOutputFormat(xuggleFilter.getContainerType(), null, null);
		
		// set the pixel type--may depend on selected fileFilter?
		IPixelFormat.Type pixelType = IPixelFormat.Type.YUV420P;

		// open the output stream, write the images, close the stream
		openStream(format, pixelType);
		// open temp images and encode
		long timeStamp = 0;
		synchronized (tempFiles) {
			for (File imageFile: tempFiles) {
				if (!imageFile.exists())
					throw new IOException("temp image file not found"); //$NON-NLS-1$
				BufferedImage image = ResourceLoader.getBufferedImage(imageFile.getAbsolutePath());
				if (image==null) {
					throw new IOException("unable to load temp image file"); //$NON-NLS-1$
				}
				encodeImage(image, pixelType, timeStamp);
				timeStamp += frameDuration*1000; // frameDuration in ms, timestamp in microsec
			}
		}
		closeStream();
		deleteTempFiles();
		hasContent = false;
		canRecord = false;
	}

  /**
   * Starts the video recording process.
   *
   * @return true if video recording successfully started
   */
	@Override
	protected boolean startRecording() {
		try {
			tempFileBasePath = XML.stripExtension(scratchFile.getAbsolutePath());
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
  /**
   * Opens/initializes the output stream using a specified Xuggle format.
   * 
   * @param format the format
   * @param pixelType the pixel type
   * @throws IOException
   */
	private boolean openStream(IContainerFormat format, IPixelFormat.Type pixelType) 
			throws IOException {
		outContainer = IContainer.make();
		if (outContainer.open(scratchFile.getAbsolutePath(), IContainer.Type.WRITE, format)<0) {
			OSPLog.finer("Xuggle could not open output file"); //$NON-NLS-1$
			return false;
		}	
		outStream = outContainer.addNewStream(0);
		outStreamCoder = outStream.getStreamCoder();	
		outStreamCoder.setNumPicturesInGroupOfPictures(30);
		ICodec codec = ICodec.guessEncodingCodec(format, null, scratchFile.getAbsolutePath(), null, ICodec.Type.CODEC_TYPE_VIDEO);
		outStreamCoder.setCodec(codec);	
		outStreamCoder.setBitRate(25000);
		outStreamCoder.setBitRateTolerance(9000);	
		outStreamCoder.setPixelType(pixelType);
	  if(dim==null && frameImage!=null) {
	    dim = new Dimension(frameImage.getWidth(null), frameImage.getHeight(null));
	  }
	  if(dim!=null) {
			outStreamCoder.setHeight(dim.height);
			outStreamCoder.setWidth(dim.width);
	  }
		outStreamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
		outStreamCoder.setGlobalQuality(0);

		IRational frameRate = IRational.make(1000/frameDuration);
		outStreamCoder.setFrameRate(frameRate);
		outStreamCoder.setTimeBase(timebase);

		if (outStreamCoder.open()<0) {
			OSPLog.finer("Xuggle could not open stream encoder"); //$NON-NLS-1$
			return false;
		}
	
		if (outContainer.writeHeader()<0) {
			OSPLog.finer("Xuggle could not write file header"); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
  /**
   * Encodes an image and writes it to the output stream.
   * 
   * @param image the image to encode (may be any image type)
   * @param pixelType the pixel type
   * @param timeStamp the time stamp in microseconds
   * @throws IOException
   */
	private boolean encodeImage(BufferedImage image, IPixelFormat.Type pixelType, long timeStamp) 
			throws IOException {
		// convert image to type TYPE_3BYTE_BGR
		BufferedImage bgrImage = convertImageToType(image, BufferedImage.TYPE_3BYTE_BGR);
		// convert bgr image to xuggle picture
		IVideoPicture picture = getPicture(bgrImage, pixelType, timeStamp);
		if (picture==null)
			throw new RuntimeException("could not convert to picture"); //$NON-NLS-1$
		// make a packet
		IPacket packet = IPacket.make();
		if (outStreamCoder.encodeVideo(packet, picture, 0) < 0) {
			throw new RuntimeException("could not encode video"); //$NON-NLS-1$
		}
		if (packet.isComplete()) {
			if (outContainer.writePacket(packet) < 0) {
				throw new RuntimeException("could not save packet to container"); //$NON-NLS-1$
			}
			return true;
		}
		return false;		
	}
	
  /**
   * Closes the output stream.
   * 
   * @throws IOException
   */
	private void closeStream() throws IOException {
    if (outContainer!=null) {
    	if (outContainer.writeTrailer() < 0) {
    		throw new RuntimeException("could not write trailer to output file"); //$NON-NLS-1$
    	}
    	outStreamCoder.close();
    	outStreamCoder.delete();
    	outStream.delete();
    	outContainer.close();
    	outContainer.delete();
    	outContainer = null;
    	outStreamCoder = null;
    	outStream = null;
    }
	}

  /**
   * Converts a bgr source image to a xuggle picture.
   *
   * @param bgrImage the source image (must be type TYPE_3BYTE_BGR)
   * @param pixelType the pixel type
   * @param timeStamp the timestamp in microseconds
   * @return the xuggle picture
   */
	private IVideoPicture getPicture(BufferedImage bgrImage, IPixelFormat.Type pixelType, long timeStamp) {
		IVideoPicture picture = null;
		try {
			IConverter converter = getConverter(bgrImage, pixelType);
			picture = converter.toPicture(bgrImage, timeStamp);
			picture.setQuality(0);		
		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Error err) {
			err.printStackTrace();
		}
		return picture;
	 }
	
  /**
   * Converts a source image to a specified type. 
   * Xuggle requires type BufferedImage.TYPE_3BYTE_BGR.
   *
   * @param source the source image
   * @param imageType the desired image type
   * @return an image of the specified type
   */
	private BufferedImage convertImageToType(BufferedImage source, int imageType){
	  // if the source image is the desired type, just return it
	  if (source.getType() == imageType)
	    return source; 
	  // otherwise copy the source into an image of the desired type
		if (xuggleImage==null 
				|| xuggleImage.getHeight()!=source.getHeight()
				|| xuggleImage.getWidth()!=source.getWidth()
				|| xuggleImage.getType()!=imageType) {
			xuggleImage = new BufferedImage(source.getWidth(), source.getHeight(), imageType);			
		}
		xuggleImage.getGraphics().drawImage(source, 0, 0, null);		    
	  return xuggleImage;
	}
	
  /**
   * Gets the converter for converting images to pictures. 
   *
   * @param bgrImage the source image (must be type TYPE_3BYTE_BGR)
   * @param pixelType the desired pixel type
   */
	private IConverter getConverter(BufferedImage bgrImage, IPixelFormat.Type pixelType){
		if (outConverter==null 
				|| outConverter.getPictureType()!= pixelType) {
			try {
				outConverter = ConverterFactory.createConverter(bgrImage, pixelType);
				dim = new Dimension(bgrImage.getWidth(), bgrImage.getHeight());
			} catch(UnsupportedOperationException e){
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		return outConverter;
	}
	
	/**
	 * Given the short name of a container, prints out information about
	 * it, including which codecs Xuggler can write (mux) into that container.
	 * 
	 * @param name the short name of the format (e.g. "flv")
	 */
	public static void getSupportedCodecs(String name) {
	  IContainerFormat format = IContainerFormat.make();
	  format.setOutputFormat(name, null, null);
	
	  List<ID> codecs = format.getOutputCodecsSupported();
	  if (codecs.isEmpty())
	    System.out.println("no supported codecs for "+name); //$NON-NLS-1$
	  else {
	    System.out.println(name+" ("+format+") supports following codecs:"); //$NON-NLS-1$ //$NON-NLS-2$
	    for(ID id : codecs) {
	      if (id != null) {
	        ICodec codec = ICodec.findEncodingCodec(id);
	        if (codec != null) {
	          System.out.println(codec);
	        }
	      }
	    }
	  }
	}
	
}
