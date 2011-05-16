package rmi_view_server;

public interface RMIViewServer extends java.rmi.Remote

{

  /**
   * Receives a value for a given variable
   * @param name
   * @param value
   * @return true if a variable with this name is defined 
   * @throws java.rmi.RemoteException
   */
  public boolean getDouble (String name, double value) throws java.rmi.RemoteException;

  /**
   * Updates the view with the values received
   */
  public void update() throws java.rmi.RemoteException;
  
  /**
   * Resets the view
   */
  public void reset() throws java.rmi.RemoteException;
  
}