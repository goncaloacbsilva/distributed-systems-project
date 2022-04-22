package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.TimestampsManager;
import pt.ulisboa.tecnico.classes.OrderStudentByTimestamp;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerGrpc;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerClassServer;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicaManagerService extends ReplicaManagerGrpc.ReplicaManagerImplBase {
    private ClassStateWrapper _classObj;
    private TimestampsManager _timestampsManager;
    private final NameServerFrontend _nameServer;
    private final HashMap<String, Boolean> _properties;
    private static final Logger LOGGER = Logger.getLogger(ReplicaManagerService.class.getName());
    private String _address;

    public ReplicaManagerService(ClassStateWrapper classObj, boolean enableDebug, HashMap<String, Boolean> properties, NameServerFrontend nameServer, String address, HashMap<String, Integer> timestamps) {
        this._classObj = classObj;
        this._timestampsManager = new TimestampsManager(timestamps);
        this._properties = properties;
        this._address = address;
        this._nameServer = nameServer;

        if (!enableDebug) {
            LOGGER.setLevel(Level.OFF);
        }

        LOGGER.info("Started with debug mode enabled");
    }


    private Map<String, ClassesDefinitions.Student> createMergeMap(List<ClassesDefinitions.Student> list) {
        Map<String, ClassesDefinitions.Student> tempProcessed = new HashMap<>();

        // First create users map
        for (ClassesDefinitions.Student student : list) {
            if (tempProcessed.containsKey(student.getStudentId())) {
                // Check Student Last Change Timestamps
                long currentTS = tempProcessed.get(student.getStudentId()).getLastChange().getSeconds();
                long newTS = student.getLastChange().getSeconds();

                if (currentTS < newTS) {
                    tempProcessed.put(student.getStudentId(), student);
                }
            } else {
                tempProcessed.put(student.getStudentId(), student);
            }
        }

        return tempProcessed;
    }

    private void merge(ClassesDefinitions.ClassState newState, boolean isPrimary) {
        ClassesDefinitions.ClassState currentState = this._classObj.getClassState();

        LOGGER.info("[ReplicaManager] (Enrolled CURRENT): \n" + currentState.getEnrolledList() + "\n[ReplicaManager] (Enrolled NEW): \n" + newState.getEnrolledList());

        List<ClassesDefinitions.Student> allEnrolled = new ArrayList<>(currentState.getEnrolledList());
        allEnrolled.addAll(newState.getEnrolledList());

        LOGGER.info("[ReplicaManager] (Discarded CURRENT): \n" + currentState.getDiscardedList() + "\n[ReplicaManager] (Discarded NEW): \n" + newState.getDiscardedList());

        List<ClassesDefinitions.Student> allDiscarded = new ArrayList<>(currentState.getDiscardedList());
        allDiscarded.addAll(newState.getDiscardedList());

        ClassesDefinitions.ClassState.Builder mergedState = ClassesDefinitions.ClassState.newBuilder();

        if (isPrimary) {
            LOGGER.info("[ReplicaManager] Received state is from a primary server. Syncing Capacity and OpenEnrollments...");
            mergedState.setCapacity(newState.getCapacity());
            mergedState.setLastClose(newState.getLastClose());
            mergedState.setOpenEnrollments(newState.getOpenEnrollments());
        } else {
            mergedState.setCapacity(currentState.getCapacity());
            mergedState.setLastClose(currentState.getLastClose());
            mergedState.setOpenEnrollments(currentState.getOpenEnrollments());
        }

        // Merge maps
        Map<String, ClassesDefinitions.Student> enrolledMap = createMergeMap(allEnrolled);
        Map<String, ClassesDefinitions.Student> discardedMap = createMergeMap(allDiscarded);

        SortedSet<ClassesDefinitions.Student> enrolledSet = new TreeSet<>(new OrderStudentByTimestamp());
        enrolledSet.addAll(enrolledMap.values());

        int enrolledCount = 0;

        long lastClose = mergedState.getLastClose().getSeconds();

        // Create Enrolled map
        for (ClassesDefinitions.Student student : enrolledSet) {

            long lastChange = student.getLastChange().getSeconds();

            if ((!mergedState.getOpenEnrollments() && lastChange > lastClose) || (enrolledCount + 1 > mergedState.getCapacity())) {
                discardedMap.put(student.getStudentId(), student);
                enrolledMap.remove(student.getStudentId());
            } else {
                enrolledCount++;
            }
        }

        // Create Discarded list
        for (ClassesDefinitions.Student student : discardedMap.values()) {
            if (enrolledMap.containsKey(student.getStudentId())) {
                // Conflict, compare students TS
                long enrolledTS = enrolledMap.get(student.getStudentId()).getLastChange().getSeconds();
                long discardedTS = student.getLastChange().getSeconds();

                if (enrolledTS > discardedTS) {
                    // Student is enrolled
                    discardedMap.remove(student.getStudentId());
                } else {
                    // Student is discarded
                    enrolledMap.remove(student.getStudentId());
                }
            }
        }

        // Fuck yeah append everything
        mergedState.addAllEnrolled(enrolledMap.values());
        mergedState.addAllDiscarded(discardedMap.values());

        LOGGER.info("FINAL MERGED STATE: \n" + mergedState);


        this._classObj.setClassState(mergedState.build());
    }


    /**
     * propagates the primary servers state by pushing to the secondary server
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void propagateStatePush(ReplicaManagerClassServer.PropagateStatePushRequest request, StreamObserver<ReplicaManagerClassServer.PropagateStatePushResponse> responseObserver) {
        ReplicaManagerClassServer.PropagateStatePushResponse.Builder response = ReplicaManagerClassServer.PropagateStatePushResponse.newBuilder();

        if (!_properties.get("isActive")) {
            response.setCode(ClassesDefinitions.ResponseCode.INACTIVE_SERVER);

        } else {



            if (!this._timestampsManager.getTimestamps().containsKey(request.getPrimaryAddress())) {
                this._timestampsManager.putTimestamp(request.getPrimaryAddress(), 0);
            }


            if (this._timestampsManager.isTimestampMostUptoDate(this._address,request.getTimestampsMap())) {
                LOGGER.info("[ReplicaManager] Processing request...");

                this.merge(request.getClassState(), this._nameServer.isPrimary(request.getPrimaryAddress()));

                this._timestampsManager.updateTimestamps(request.getTimestampsMap());

                LOGGER.info("Set response as OK");
                response.setCode(ClassesDefinitions.ResponseCode.OK);

                LOGGER.info("Sending propagateStatePush response");
                LOGGER.info("[ReplicaManager] Updated State to: " + this._timestampsManager.getTimestamps());
            }

        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }



}
