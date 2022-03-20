package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

public class ClassObject {
  private ClassesDefinitions.ClassState _classState;
  private boolean _systemActive;

  public ClassObject() {
    _classState = ClassesDefinitions.ClassState.getDefaultInstance();
    _systemActive = true;
  }

  public void setSystemActive(boolean state) {
    _systemActive = state;
  }

  public ClassesDefinitions.ClassState getClassState() {
    return _classState;
  }

  public boolean isActive() {
    return _systemActive;
  }
}
