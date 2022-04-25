package pt.ulisboa.tecnico.classes;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

import java.util.ArrayList;
import java.util.List;

public abstract class RPCFrontendCall {
    private ClassesDefinitions.ResponseCode _responseCode;
    private List<String> _targetServerQualifiers;
    private boolean _previousServerIsInactive;

    public RPCFrontendCall(List<String> qualifiers) {
        this._targetServerQualifiers = new ArrayList<>(qualifiers);
        this._previousServerIsInactive = false;
    }

    public void exec() throws ResponseException, StatusRuntimeException {
        createStubForRequest(this._targetServerQualifiers, this._previousServerIsInactive);

        _responseCode = requestCall();

        if (_responseCode == ClassesDefinitions.ResponseCode.INACTIVE_SERVER) {
            this._previousServerIsInactive = true;
            exec();
        } else {
            throw new ResponseException(_responseCode);
        }

    }

    public abstract void createStubForRequest(List<String> qualifiers, boolean previousIsInactive);
    public abstract ClassesDefinitions.ResponseCode requestCall() throws StatusRuntimeException;

}
