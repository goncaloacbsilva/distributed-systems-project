package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

public class ClassObject {
  private ClassesDefinitions.ClassState _classState;

  public ClassObject() {
    _classState = ClassesDefinitions.ClassState.getDefaultInstance();
  }

  public synchronized void setClassState(ClassesDefinitions.ClassState classState) {
    this._classState = classState;
  }

  public synchronized ClassesDefinitions.ClassState getClassState() {
    return _classState;
  }
}
