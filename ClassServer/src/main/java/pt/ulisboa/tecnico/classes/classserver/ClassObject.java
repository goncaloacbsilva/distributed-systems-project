package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

/** Internal class state wrapper */
public class ClassObject {
  private ClassesDefinitions.ClassState _classState;

  /** Creates an instance of ClassObject. Initializes the class state with a default instance */
  public ClassObject() {
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
