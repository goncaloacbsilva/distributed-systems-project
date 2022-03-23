package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

public class ClassObject {
  private ClassesDefinitions.ClassState _classState;

  public ClassObject() {
    _classState = ClassesDefinitions.ClassState.getDefaultInstance();
  }

  public void setClassState(ClassesDefinitions.ClassState _classState) {
    this._classState = _classState;
  }

  public ClassesDefinitions.ClassState getClassState() {
    return _classState;
  }


}
