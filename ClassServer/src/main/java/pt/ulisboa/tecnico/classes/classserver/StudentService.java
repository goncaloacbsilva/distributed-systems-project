package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Student Service remote procedure calls
 */
public class StudentService extends StudentServiceGrpc.StudentServiceImplBase {

    private ClassObject object;
    private static final Logger LOGGER = Logger.getLogger(StudentService.class.getName());

    /**
     * Creates an instance of StudentService
     *
     * @param obj         shared object
     * @param enableDebug debug flag
     */
    public StudentService(ClassObject obj, boolean enableDebug) {

        this.object = obj;

        if (!enableDebug) {
            LOGGER.setLevel(Level.OFF);
        }

        LOGGER.info("Started with debug mode enabled");
    }

    /**
     * "enroll" remote procedure call. Receives enrollRequest from the student client changes the
     * class state and sends the adequate response code through a StreamObserver
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public synchronized void enroll(
            StudentClassServer.EnrollRequest request,
            StreamObserver<StudentClassServer.EnrollResponse> responseObserver) {
        LOGGER.info("Received enroll request");
        ClassesDefinitions.Student student = request.getStudent();
        int student_index = this.object.getClassState().getEnrolledList().indexOf(student);
        int enrolled = this.object.getClassState().getEnrolledList().size();
        int capacity = this.object.getClassState().getCapacity();
        boolean isOpen = this.object.getClassState().getOpenEnrollments();

        if (!isOpen) {
            responseObserver.onNext(
                    StudentClassServer.EnrollResponse.newBuilder()
                            .setCode(ClassesDefinitions.ResponseCode.ENROLLMENTS_ALREADY_CLOSED)
                            .build());
        } else if (student_index != -1) {
            responseObserver.onNext(
                    StudentClassServer.EnrollResponse.newBuilder()
                            .setCode(ClassesDefinitions.ResponseCode.STUDENT_ALREADY_ENROLLED)
                            .build());
            LOGGER.info("Set response as Student is already enrolled");
        } else if (capacity < enrolled + 1) {
            responseObserver.onNext(
                    StudentClassServer.EnrollResponse.newBuilder()
                            .setCode(ClassesDefinitions.ResponseCode.FULL_CLASS)
                            .build());
            LOGGER.info("Set response as Full Class");
        } else {
            LOGGER.info("Building new class state");
            ClassesDefinitions.ClassState.Builder newObject = ClassesDefinitions.ClassState.newBuilder();
            newObject.setCapacity(this.object.getClassState().getCapacity());
            newObject.setOpenEnrollments(this.object.getClassState().getOpenEnrollments());
            List<ClassesDefinitions.Student> enrolled_list =
                    new ArrayList<>(this.object.getClassState().getEnrolledList());
            enrolled_list.add(student);
            newObject.addAllEnrolled(enrolled_list);
            newObject.addAllDiscarded(this.object.getClassState().getDiscardedList());
            this.object.setClassState(newObject.build());
            LOGGER.info("class state built");
            responseObserver.onNext(
                    StudentClassServer.EnrollResponse.newBuilder()
                            .setCode(ClassesDefinitions.ResponseCode.OK)
                            .build());
            LOGGER.info("Set response as OK");
        }
        LOGGER.info("Sending enroll response");
        responseObserver.onCompleted();
    }

    /**
     * "list" remote procedure call. Receives listRequest from the student client and sends the
     * internal class state through a StreamObserver
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void listClass(
            StudentClassServer.ListClassRequest request,
            StreamObserver<StudentClassServer.ListClassResponse> responseObserver) {
        LOGGER.info("Received listClass request");
        StudentClassServer.ListClassResponse.Builder response =
                StudentClassServer.ListClassResponse.newBuilder();
        response.setClassState(this.object.getClassState());
        LOGGER.info(
                "Sending list response with class state: \n" + Stringify.format(response.getClassState()));
        response.setCodeValue(ClassesDefinitions.ResponseCode.OK_VALUE);
        LOGGER.info("Set response as OK");
        LOGGER.info("Sending listClass response");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
