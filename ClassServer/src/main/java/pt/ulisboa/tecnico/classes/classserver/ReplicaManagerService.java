package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.OrderStudentByTimestamp;
import pt.ulisboa.tecnico.classes.TimestampsManager;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerClassServer;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerGrpc;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReplicaManager related service responsible for handling incoming propagated states and merging
 * them with the current state solving conflicts that may appear during the merge operation
 */
public class ReplicaManagerService extends ReplicaManagerGrpc.ReplicaManagerImplBase {
  private static final Logger LOGGER = Logger.getLogger(ReplicaManagerService.class.getName());
  private final NameServerFrontend _nameServer;
  private final HashMap<String, Boolean> _properties;
  private final ClassStateWrapper _classObj;
  private final TimestampsManager _timestampsManager;
  private final String _address;

  /**
   * Creates an instance of ReplicaManagerService
   *
   * @see ReplicaManagerService
   * @param classObj ClassStateWrapper instance
   * @param enableDebug debug flag (enabled if true)
   * @param properties server properties HashMap
   * @param nameServer NameServerFrontend instance
   * @param address String of local address
   * @param timestamps server local timestamps HashMap
   */
  public ReplicaManagerService(
      ClassStateWrapper classObj,
      boolean enableDebug,
      HashMap<String, Boolean> properties,
      NameServerFrontend nameServer,
      String address,
      HashMap<String, Integer> timestamps) {
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

  /**
   * Creates a 'merge map' with the supplied student list. In this map each key (studentId) is
   * associated to the most recent student object
   *
   * @param list
   * @return
   */
  private Map<String, ClassesDefinitions.Student> createMergeMap(
      List<ClassesDefinitions.Student> list) {
    Map<String, ClassesDefinitions.Student> tempProcessed = new HashMap<>();

    // First create users map
    for (ClassesDefinitions.Student student : list) {
      if (tempProcessed.containsKey(student.getStudentId())) {
        // Check Student Last Change Timestamps
        long currentTS = tempProcessed.get(student.getStudentId()).getLastChange().getSeconds();
        long newTS = student.getLastChange().getSeconds();

        // If new student object is more recent than the current, replace current object with the
        // new one
        if (currentTS < newTS) {
          tempProcessed.put(student.getStudentId(), student);
        }
      } else {
        tempProcessed.put(student.getStudentId(), student);
      }
    }

    return tempProcessed;
  }

  /**
   * Merges current local state with the supplied state
   *
   * @param newState incoming state
   * @param isPrimary true if the incoming state is from a Primary server
   */
  private void merge(ClassesDefinitions.ClassState newState, boolean isPrimary) {

    ClassesDefinitions.ClassState currentState = this._classObj.getClassState();
    ClassesDefinitions.ClassState primaryState;

    // Merged state builder
    ClassesDefinitions.ClassState.Builder mergedState = ClassesDefinitions.ClassState.newBuilder();

    // Create a list with all the enrolled students from both states (can have repeated students)
    LOGGER.info(
        "[ReplicaManager] (Enrolled CURRENT): \n"
            + currentState.getEnrolledList()
            + "\n[ReplicaManager] (Enrolled NEW): \n"
            + newState.getEnrolledList());
    List<ClassesDefinitions.Student> allEnrolled = new ArrayList<>(currentState.getEnrolledList());
    allEnrolled.addAll(newState.getEnrolledList());

    // Create a list with all the discarded students from both states (can have repeated students)
    LOGGER.info(
        "[ReplicaManager] (Discarded CURRENT): \n"
            + currentState.getDiscardedList()
            + "\n[ReplicaManager] (Discarded NEW): \n"
            + newState.getDiscardedList());
    List<ClassesDefinitions.Student> allDiscarded =
        new ArrayList<>(currentState.getDiscardedList());
    allDiscarded.addAll(newState.getDiscardedList());

    // If incoming state is from a primary server we need to update current state capacity,
    // lastClose and openEnrollments using that state
    if (isPrimary) {
      LOGGER.info(
          "[ReplicaManager] Received state is from a primary server. Syncing Capacity and OpenEnrollments...");
      primaryState = newState;
    } else {
      primaryState = currentState;
    }

    mergedState.setCapacity(primaryState.getCapacity());
    mergedState.setLastClose(primaryState.getLastClose());
    mergedState.setOpenEnrollments(primaryState.getOpenEnrollments());

    // Get lastClose timestamp
    long lastClose = mergedState.getLastClose().getSeconds();

    // Create merge maps (Read JavaDoc)
    Map<String, ClassesDefinitions.Student> enrolledMap = createMergeMap(allEnrolled);
    Map<String, ClassesDefinitions.Student> discardedMap = createMergeMap(allDiscarded);

    // Create SortedSet where Students are ordered by their 'lastChange' timestamp
    SortedSet<ClassesDefinitions.Student> enrolledSet =
        new TreeSet<>(new OrderStudentByTimestamp());
    enrolledSet.addAll(enrolledMap.values());

    // Counter for keep tracking of the enrolled students
    int enrolledCount = 0;

    // Merge Enrollments
    for (ClassesDefinitions.Student student : enrolledSet) {

      // Get student last change
      long lastChange = student.getLastChange().getSeconds();

      // If enrollments were closed after student enrollment or class is already full, discard
      // student
      if ((!mergedState.getOpenEnrollments() && lastChange > lastClose)
          || (enrolledCount + 1 > mergedState.getCapacity())) {
        discardedMap.put(student.getStudentId(), student);
        enrolledMap.remove(student.getStudentId());
      } else {
        enrolledCount++;
      }
    }

    // Merge Discarded
    for (ClassesDefinitions.Student student : new ArrayList<>(discardedMap.values())) {

      // Check if enrolled students map contains discarded student
      if (enrolledMap.containsKey(student.getStudentId())) {

        // Conflict, compare students Timestamps
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

    // Add final enrolled and discarded students to mergedState
    mergedState.addAllEnrolled(enrolledMap.values());
    mergedState.addAllDiscarded(discardedMap.values());

    LOGGER.info("FINAL MERGED STATE: \n" + mergedState);

    // Overwrite current local state with the mergedState
    this._classObj.setClassState(mergedState.build());
  }

  /**
   * Receives and handle incoming propagated state, if necessary merges incoming state with the
   * current one
   *
   * @param request
   * @param responseObserver
   */
  @Override
  public void propagateStatePush(
      ReplicaManagerClassServer.PropagateStatePushRequest request,
      StreamObserver<ReplicaManagerClassServer.PropagateStatePushResponse> responseObserver) {
    ReplicaManagerClassServer.PropagateStatePushResponse.Builder response =
        ReplicaManagerClassServer.PropagateStatePushResponse.newBuilder();

    if (!_properties.get("isActive")) {
      response.setCode(ClassesDefinitions.ResponseCode.INACTIVE_SERVER);

    } else {

      if (this._timestampsManager.isTimestampMostUptoDate(request.getTimestampsMap())) {
        LOGGER.info("[ReplicaManager] Processing request...");

        // Merge states
        this.merge(
            request.getClassState(), this._nameServer.isPrimary(request.getPrimaryAddress()));

        // Update local timestamps
        this._timestampsManager.updateTimestamps(request.getTimestampsMap());

        LOGGER.info("Set response as OK");
        response.setCode(ClassesDefinitions.ResponseCode.OK);

        LOGGER.info("Sending propagateStatePush response");
        LOGGER.info(
            "[ReplicaManager] Updated State to: " + this._timestampsManager.getTimestamps());
      }
    }

    responseObserver.onNext(response.build());
    responseObserver.onCompleted();
  }
}
