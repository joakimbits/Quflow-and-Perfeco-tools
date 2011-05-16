package org.colos.ejs.model_elements.apache_numerics;

import org.apache.commons.math.analysis.solvers.*;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.integration.*;

/**
 * Encapsulates access to a UnivariateRealFunction
 * @author Francisco Esquembre
 * @version 1.0, December 2010
 *
 */
public class RealFunction {
  static public final String SOLVER_BISECTION = "Bisection";
  static public final String SOLVER_BRENT_DEKKER = "Brent-Dekker";
  static public final String SOLVER_NEWTON = "Newton";
  static public final String SOLVER_SECANT = "Secant";
  static public final String SOLVER_MULLER = "Muller";
  static public final String SOLVER_LAGUERRE = "Laguerre";
//  static public final String SOLVER_RIDDER = "Ridder";

  static public final String INTEGRATOR_ROMBERG = "Romberg";
  static public final String INTEGRATOR_SIMPSON = "Simpson";
  static public final String INTEGRATOR_TRAPEZOID = "Trapezoid";
  static public final String INTEGRATOR_LEGENDRE_GAUSS = "Legendre-Gauss";

  private UnivariateRealFunction function;
  private UnivariateRealSolver solver;
  private UnivariateRealIntegrator integrator;
  
  /**
   * Standard constructor to be called by the simulation
   * @param _model
   * @param _filename The filename to read
   * @see #setFilename(String)
   */
  public RealFunction(UnivariateRealFunction _function) {
    this.function = _function;
    this.solver = new BisectionSolver();
    this.integrator = new RombergIntegrator();
  }

  /**
   * Sets the root finding method
   * @param solverMethod
   */
  public void setSolver(String solverMethod) {
    if      (SOLVER_BISECTION.equals(solverMethod))    solver = new BisectionSolver();
    else if (SOLVER_BRENT_DEKKER.equals(solverMethod)) solver = new BrentSolver();
    else if (SOLVER_NEWTON.equals(solverMethod))       solver = new NewtonSolver();
    else if (SOLVER_SECANT.equals(solverMethod))       solver = new SecantSolver();
    else if (SOLVER_MULLER.equals(solverMethod))       solver = new MullerSolver();
    else if (SOLVER_LAGUERRE.equals(solverMethod))     solver = new LaguerreSolver();
//    else if (SOLVER_RIDDER.equals(solverMethod))       solver = new RidderSolver();
  }
  
  /**
   * Sets the root finding solver
   * @param _solver UnivariateRealSolver
   */
  public void setSolver(UnivariateRealSolver _solver) { solver = _solver; }
  
  /**
   * Returns the root finder solver
   * @return
   */
  public UnivariateRealSolver getSolver() { return solver; }
  
  /**
   * Sets the integration method
   * @param integratorMethod
   */
  public void setIntegrator(String integratorMethod) {
    if      (INTEGRATOR_ROMBERG.equals(integratorMethod))        integrator = new RombergIntegrator();
    else if (INTEGRATOR_SIMPSON.equals(integratorMethod))        integrator = new SimpsonIntegrator();
    else if (INTEGRATOR_TRAPEZOID.equals(integratorMethod))      integrator = new TrapezoidIntegrator();
    else if (INTEGRATOR_LEGENDRE_GAUSS.equals(integratorMethod)) integrator = new LegendreGaussIntegrator(2,100);
  }

  /**
   * Sets the integrator
   * @param _integrator UnivariateRealIntegrator
   */
  public void setIntegrator(UnivariateRealIntegrator _integrator) { integrator = _integrator; }

  /**
   * Returns the integrator
   * @return
   */
  public UnivariateRealIntegrator getIntegrator() { return this.integrator; }
  
  /**
   * Evaluates the function at the argument
   * @return Double.NaN if failed
   */
  public double value(double x) {
    try {
      return function.value(x);
    }
    catch (Exception exc) {
      exc.printStackTrace();
      return Double.NaN;
    }
  }

  /**
   * Integrates the function in the given interval
   * @return Double.NaN if failed
   */
  public double integrate(double min, double max) {
    try { 
      return integrator.integrate(function,min,max);
    }
    catch (Exception exc) {
      exc.printStackTrace();
      return Double.NaN;
    }
  }

  /**
   * Solves for a zero root in the given interval
   * @return Double.NaN if failed
   */
  public double solve(double min, double max) {
    try { 
      return solver.solve(function,min,max);
    }
    catch (Exception exc) {
      exc.printStackTrace();
      return Double.NaN;
    }
  }

  /**
   * Solves for a zero root in the given interval with the given start value
   * @return Double.NaN if failed
   */
  public double solve(double min, double max, double start) {
    try { 
      return solver.solve(function,min,max);
    }
    catch (Exception exc) {
      exc.printStackTrace();
      return Double.NaN;
    }
  }

}
