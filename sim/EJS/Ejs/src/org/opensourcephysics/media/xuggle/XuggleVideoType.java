/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2004  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.xuggle;
import java.io.IOException;
import java.util.TreeSet;

import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoRecorder;
import org.opensourcephysics.media.core.VideoType;

/**
 * This implements the VideoType interface with a Xuggle type.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class XuggleVideoType implements VideoType {
	
  protected static TreeSet<VideoFileFilter> xuggleFileFilters 
  		= new TreeSet<VideoFileFilter>();
  
  private VideoFileFilter singleTypeFilter; // null for general type
    
  /**
   * Constructor attempts to load a xuggle class.
   * This will throw an error if xuggle is not present.
   */
  public XuggleVideoType() {
  	String xuggleClass = "com.xuggle.xuggler.IContainer"; //$NON-NLS-1$
  	try {
			Class.forName(xuggleClass); // throws exception if no xuggle
		} catch (ClassNotFoundException ex) {
			throw new Error("Xuggle not available: unable to load "+xuggleClass); //$NON-NLS-1$
		}
  }

  /**
   * Constructor with a file filter for a specific container type.
   * 
   * @param filter the file filter 
   */
  public XuggleVideoType(VideoFileFilter filter) {
  	this();
  	if (filter!=null) {
			singleTypeFilter = filter;
			xuggleFileFilters.add(filter);
  	}
  }

  /**
   * Opens a named video as a XuggleVideo.
   *
   * @param name the name of the video
   * @return a new Xuggle video
   */
  public Video getVideo(String name) {
    try {
    	Video video = new XuggleVideo(name);
      video.setProperty("video_type", this); //$NON-NLS-1$
      return video;
    } catch(IOException ex) {
      return null;
    }
  }

  /**
   * Reports whether this xuggle type can record videos
   *
   * @return false by default. Subclasses may return true for specific formats.
   */
  public boolean canRecord() {
    return true;
  }

  /**
   * Gets a Xuggle video recorder.
   *
   * @return the video recorder
   */
  public VideoRecorder getRecorder() {
  	return new XuggleVideoRecorder(this);  	
  }

  /**
   * Gets the file filters for this type.
   *
   * @return an array of file filters
   */
  public VideoFileFilter[] getFileFilters() {
    return xuggleFileFilters.toArray(new VideoFileFilter[0]);
  }

  /**
   * Gets the default file filter for this type. May return null.
   *
   * @return the default file filter
   */
  public VideoFileFilter getDefaultFileFilter() {
  	if (singleTypeFilter!=null)
  		return singleTypeFilter;
  	return null;
  }
  
  /**
   * Return true if the specified video is this type.
   *
   * @param video the video
   * @return true if the video is this type
   */
  public boolean isType(Video video) {
    return video.getClass().equals(XuggleVideo.class);
  }

  /**
   * Gets the name and/or description of this type.
   *
   * @return a description
   */
  public String getDescription() {
  	if (singleTypeFilter!=null)
  		return singleTypeFilter.getDescription();
    return MediaRes.getString("XuggleVideoType.Description"); //$NON-NLS-1$
  }

  /**
   * Gets the default extension for this type.
   *
   * @return an extension
   */
  public String getDefaultExtension() {
  	if (singleTypeFilter!=null) {
  		return singleTypeFilter.getDefaultExtension();
  	}
    return null;
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
