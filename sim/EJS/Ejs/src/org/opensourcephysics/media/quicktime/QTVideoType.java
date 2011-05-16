/*
 * The org.opensourcephysics.media.quicktime package provides QuickTime
 * services including implementations of the Video and VideoRecorder interfaces.
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
package org.opensourcephysics.media.quicktime;

import java.io.*;

import org.opensourcephysics.media.core.*;

/**
 * This implements the VideoType interface with QuickTime for Java
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class QTVideoType implements VideoType {
	
  protected static VideoFileFilter movFilter
  	= new VideoFileFilter("mov", new String[] {"mov"}); //$NON-NLS-1$ //$NON-NLS-2$

  /**
   * Constructor queries QTSession to make sure QTJava is working.
   * This will throw an error if QTJava is not present.
   */
  public QTVideoType() {
    quicktime.QTSession.isInitialized();
  }

  /**
   * Opens a video as a new QTVideo.
   *
   * @param name the name of the video
   * @return the new video
   */
  public Video getVideo(String name) {
    try {
    	Video video = new QTVideo(name);
      video.setProperty("video_type", this); //$NON-NLS-1$
      return video;
    }
    catch (IOException ex) {
      return null;
    }
  }

  /**
   * Gets a video recorder.
   *
   * @return the video recorder
   */
  public VideoRecorder getRecorder() {
    return new QTVideoRecorder();
  }

  /**
   * Reports whether this type can record videos
   *
   * @return true if this can record videos
   */
  public boolean canRecord() {
    return true;
  }

  /**
   * Gets the name and/or description of this type.
   *
   * @return a description
   */
  public String getDescription() {
    return movFilter.getDescription();
  }

  /**
   * Gets the name and/or description of this type.
   *
   * @return a description
   */
  public String getDefaultExtension() {
    return "mov"; //$NON-NLS-1$
  }

  /**
   * Gets the file filter for this type.
   *
   * @return a file filter
   */
  public VideoFileFilter[] getFileFilters() {
    return new VideoFileFilter[] {movFilter};
  }

  /**
   * Gets the default file filter for this type. May return null.
   *
   * @return the default file filter
   */
  public VideoFileFilter getDefaultFileFilter() {
  	return movFilter;
  }

  /**
   * Return true if the specified video is this type.
   *
   * @param video the video
   * @return true if the video is this type
   */
  public boolean isType(Video video) {
    return video.getClass().equals(QTVideo.class);
  }
}


