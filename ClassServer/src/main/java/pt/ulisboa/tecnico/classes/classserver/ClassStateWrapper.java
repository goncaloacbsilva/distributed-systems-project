package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;

import java.util.logging.Logger;

public class ClassStateWrapper {
  private static final Logger LOGGER = Logger.getLogger(ClassStateWrapper.class.getName());
  private ClassState _classState;

  /**
   * Creates an instance of ClassStateWrapper. Initializes the class state with a default instance
   */
  public ClassStateWrapper() {
    _classState = ClassState.getDefaultInstance();
  }

  /**
   * Returns the current class state
   *
   * @return ClassState
   */
  public synchronized ClassState getClassState() {
    return _classState;
  }

  /**
   * Sets the new class state
   *
   * @param classState new class state
   */
  public synchronized void setClassState(ClassState classState) {
    this._classState = classState;
  }
}
