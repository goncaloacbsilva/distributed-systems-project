package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;


import java.util.HashMap;
import java.util.logging.Logger;


public class ClassStateWrapper {
  private ClassState _classState;

  private static final Logger LOGGER = Logger.getLogger(ClassStateWrapper.class.getName());

  /** Creates an instance of ClassStateWrapper. Initializes the class state with a default instance */
  public ClassStateWrapper() {
    _classState = ClassState.getDefaultInstance();
  }




  /**
   * Sets the new class state
   *
   * @param classState new class state
   */
  public synchronized void setClassState(ClassState classState) {
    this._classState = classState;
  }

  /**
   * Returns the current class state
   *
   * @return ClassState
   */
  public synchronized ClassState getClassState() {
    return _classState;
  }


}
