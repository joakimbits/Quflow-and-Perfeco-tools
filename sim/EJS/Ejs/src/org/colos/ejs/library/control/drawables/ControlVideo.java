/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables for inclusion in
 * a DrawingPanel
 * Copyright (c) Dec 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.control.swing.ControlDrawable;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.media.quicktime.QTVideo;

public class ControlVideo extends ControlDrawable {
  static final double TO_RAD = Math.PI/180.0;

  private QTVideo video;
  private double x=0.0,y=0.0,dx=1.0,dy=1.0,angle=0.0;
  private int frame=0,iangle=0;
  private boolean visible = true;
  private String videofile=null;

  protected Drawable createDrawable () {
      try { video = new QTVideo(""); }
      catch (Exception exc) { exc.printStackTrace(); }
    video.setVisible(false);
    return video;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("x");
      infoList.add ("y");
      infoList.add ("sizex");
      infoList.add ("sizey");
      infoList.add ("angle");
      infoList.add ("videofile");
      infoList.add ("frame");
      infoList.add ("visible");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("angle")) return "rotationAngle";
    if (_property.equals("sizex")) return "sizeX";
    if (_property.equals("sizey")) return "sizeY";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("x"))          return "int|double";
    if (_property.equals("y"))          return "int|double";
    if (_property.equals("sizex"))      return "int|double";
    if (_property.equals("sizey"))      return "int|double";
    if (_property.equals("angle"))      return "int|double";
    if (_property.equals("videofile"))  return "File|String";
    if (_property.equals("frame"))      return "int";
    if (_property.equals("visible"))       return "boolean";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Variable properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getDouble()!=x)  { x =_value.getDouble(); if (videofile!=null) video.setX(x); } break;
      case 1 : if (_value.getDouble()!=y)  { y =_value.getDouble(); if (videofile!=null) video.setY(y); } break;
      case 2 : if (_value.getDouble()!=dx) { dx=_value.getDouble(); if (videofile!=null) video.setWidth(dx); } break;
      case 3 : if (_value.getDouble()!=dy) { dy=_value.getDouble(); if (videofile!=null) video.setHeight(dy); } break;
      case 4 :
        if (_value instanceof IntegerValue) {
          if (_value.getInteger() != iangle) {
            iangle = _value.getInteger();
            angle = iangle * TO_RAD;
            if (videofile != null) video.setAngle(angle);
          }
        }
        else {
          if (_value.getDouble() != angle) {
            angle = _value.getDouble();
            iangle = (int) (angle / TO_RAD);
            if (videofile != null) video.setAngle(angle);
          }
        }
        break;
      case 5 : setVideoFile(_value.getString()); break;
      case 6 : if (_value.getInteger()!=frame) {
          frame = _value.getInteger();
//          if (myParent!=null) ((DrawingPanel) myParent.getVisual ()).repaint();
          if (videofile!=null) video.setFrameNumber(frame);
        }
        break;
      case 7 : visible = _value.getBoolean(); if (videofile!=null) video.setVisible(visible); break;
      default: super.setValue(_index-8,_value);
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : x=0.0;  if (videofile!=null) video.setX(x); break;
      case 1 : y=0.0;  if (videofile!=null) video.setY(y); break;
      case 2 : dx=1.0; if (videofile!=null) video.setWidth(dx);  break;
      case 3 : dy=1.0; if (videofile!=null) video.setHeight(dy); break;
      case 4 : iangle = 0; angle = 0.0; if (videofile != null) video.setAngle(angle); break;
      case 5 : setVideoFile(null); break;
      case 6 : frame = 0; if (videofile!=null) video.setFrameNumber(frame); break;
      case 7 : visible = true; if (videofile!=null) video.setVisible(visible); break;
      default: super.setDefaultValue(_index-8); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 :
      case 1 : return "0";
      case 2 :
      case 3 : return "1";
      case 4 : return "0";
      case 5 : return "<none>";
      case 6 : return "0";
      case 7 : return "true";
      default : return super.getDefaultValueString(_index-8);
    }
 }

  public Value getValue (int _index) {
    switch(_index) {
      case 0 : case 1 : case 2 : case 3 : case 4 :
      case 5 : case 6 : case 7 :
        return null;
      default: return super.getValue(_index-8);
    }
  }

// -------------------------------------
// private methods
// -------------------------------------

  private void setVideoFile (String _video) {
    if (_video==null || _video.trim().length()<=0) { videofile = null; video.setVisible(false); return; }
    if (videofile!=null && videofile.equals(_video)) return; // no need to do it again
    if (setVideo (_video)) videofile = _video;
    else videofile = null;
    /*
    if (getProperty("_ejs_codebase")!=null) setVideoClip(getProperty("_ejs_codebase"),_video);
    else if (getSimulation()!=null && getSimulation().getCodebase()!=null) setVideoClip (getSimulation().getCodebase().toString(),_video);
    else setVideoClip (null,_video);
    */
    if (videofile!=null) {
      // Update video
      video.setX(x);
      video.setY(y);
      video.setWidth(dx);
      // video.setHeight(dy);
      video.setAngle(angle);
      video.setFrameNumber (frame);
      video.setVisible(visible);
    }
    else video.setVisible(false);
  }

  private boolean setVideo(String _videoFilename) {
    try {
//      video.load(_videoFilename); does NOT work
      java.io.File file = Simulation.requiresResourceFile(_videoFilename);
      if (file!=null) { video.load(file); return true; }
    }
    catch (Exception ex) { ex.printStackTrace(); }
    return false;
  }

  public void onExit() {
//    System.out.println("Video calling exit");
    super.destroy();
  }


}
