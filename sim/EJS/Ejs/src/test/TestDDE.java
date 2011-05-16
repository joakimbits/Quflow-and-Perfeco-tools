package test;

import org.opensourcephysics.numerics.DDE;
import org.opensourcephysics.numerics.ODEInterpolatorEventSolver;
import org.opensourcephysics.numerics.ODESolverInterpolator;
import org.opensourcephysics.numerics.dde_solvers.interpolation.IntervalData;
import org.opensourcephysics.numerics.dde_solvers.rk.*;

public class TestDDE implements DDE {

  public double[] dbl_state;
  public ODEInterpolatorEventSolver ejs_solver;
  public ODESolverInterpolator ejs_test;
  public double dbl_maxstepsize = 0.05;
  public double dbl_stepsize = 0.01;
  public double dbl_atol = 0.000001;
  public double dbl_rtol = 0.000001;
  public boolean bol_estimatefirststep = true;
  public int int_maxinternalsteps = 200;
  public double dbl_Tmax = 10;
  public double dbl_delay = -2;
  public double[] delaysArray = new double[] {dbl_delay};

  static public void main(String args[]) {
    new TestDDE();  
  }
  
  public TestDDE() {

    dbl_state = new double[]{1, 2, 0};//Initial Condition

    ejs_test = new Radau5(this);//Stiff Solver

    ejs_solver = new ODEInterpolatorEventSolver(ejs_test);

    ejs_solver.initialize(dbl_stepsize);
    ejs_solver.setEnableExceptions(true);
    ejs_solver.setZenoEffectDetection(500);


    ejs_solver.setEstimateFirstStep(bol_estimatefirststep);
    ejs_solver.setInternalStepSize(dbl_stepsize);
    ejs_solver.setMaximumInternalStepSize(dbl_maxstepsize);
    ejs_solver.setMaximumInternalSteps(int_maxinternalsteps);

    ejs_solver.setTolerances(dbl_atol, dbl_rtol);

    do {

      System.out.print("step[" + dbl_state[0] + "," + dbl_state[1] + ","
              + dbl_state[2] + "]" + "\n");//Output

      // ejs_solver.reinitialize(); No need to reinitialize if you have not changed the state

      ejs_solver.maxStep();


    } while (dbl_state[2] <= dbl_Tmax);
  }

  public double[] getState() {

    return dbl_state;

  }

  public void getRate(double[] _dbl_state, double[] _dbl_rate) {

//    _dbl_rate[0] = 0 * _dbl_state[0] + 1 * _dbl_state[1] + 1;
//    _dbl_rate[1] = -1 * _dbl_state[0] + -20000 * _dbl_state[1];
//    _dbl_rate[2] = 1;
  }

  public void getRate(double[] currentState, IntervalData[] intervals, double[] rate) {

    double time = currentState[2];
    double past1 = intervals[0].interpolate(time- dbl_delay, 1);

    rate[0] = 0 * currentState[0] + 1 * past1 + 1;
    rate[1] = -1 * currentState[0] + -20000 * currentState[1];//Stiff System
    rate[2] = 1;

  }

  public double getMaximumDelay() {
    return this.dbl_delay;
  }

  public double[] getDelays(double[] state) {
    return delaysArray; // more efficient than new double[]{dbl_delay};
  }

  public double[] getInitialConditionDiscontinuities() {
    return null;
  }

  public double[] getInitialCondition(double time, double[] state) { // The initial state before t=0 is constant
    state[0] = 1;
    state[1] = 2;
    state[2] = time;
    return state;
  }
}
