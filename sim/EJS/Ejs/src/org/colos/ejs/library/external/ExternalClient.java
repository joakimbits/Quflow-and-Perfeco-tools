package org.colos.ejs.library.external;

/**
 * Interface for simulations that can be used by external applications
 * to set and get values
 *
 */
public interface ExternalClient {

  /**
   * Provides any initialization command for the external application.
   */
   public String _externalInitCommand(String _appFile);

  /**
   * Set the value of all the variables in the external application.
   * If _any is true, then set the values of all sessions.
   */
  public void _externalSetValues(boolean _any, ExternalApp _app); //Gonzalo 090610

  /**
   * Get the value of all the variables in the external application.
   * If _any is true, then get the values of all sessions.
   */
  public void _externalGetValues(boolean _any, ExternalApp _app); //Gonzalo 090610

  public void _externalGetValuesAndUpdate(boolean _any, ExternalApp _app);

} // End of interface

