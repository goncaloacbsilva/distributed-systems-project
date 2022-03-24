package pt.ulisboa.tecnico.classes;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

public class ResponseException extends Exception {
    private ClassesDefinitions.ResponseCode _code;

    public ResponseException(ClassesDefinitions.ResponseCode code) {
        super();
        this._code = code;
    }

    public ClassesDefinitions.ResponseCode getResponseCode() {
        return this._code;
    }
}
