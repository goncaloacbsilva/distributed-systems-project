package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;


public class ClassStateWrapper {
  private ClassesDefinitions.ClassState _classState;
  private boolean _isActive;

  /** Creates an instance of ClassStateWrapper. Initializes the class state with a default instance */
  public ClassStateWrapper() {
    _classState = ClassesDefinitions.ClassState.getDefaultInstance();
  }

  /**
   * Sets the new class state
   *
   * @param classState new class state
   */
  public synchronized void setClassState(ClassesDefinitions.ClassState classState) {
    this._classState = classState;
  }

  /**
   * Returns the current class state
   *
   * @return ClassState
   */
  public synchronized ClassesDefinitions.ClassState getClassState() {
    return _classState;
  }
}
