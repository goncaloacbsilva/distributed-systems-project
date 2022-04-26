package pt.ulisboa.tecnico.classes.admin;

import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

import java.util.List;
import java.util.Scanner;



/**
 * Admin Console
 *
 * Syntax: <command> <qualifier> <index>
 *
 * qualifier: [P,S]
 * index: 0,1,2,...
 *
 * Example: activate S 1
 * Note: Index is 0 and qualifier is P by default.
 *       > activate
 *           is equivalent to
 *       > activate P 0
 */
public class Admin {

    // Define Admin commands
    private static final String EXIT_CMD = "exit";
    private static final String DUMP_CMD = "dump";
    private static final String ACTIVATE_CMD = "activate";
    private static final String DEACTIVATE_CMD = "deactivate";

    private static final String DEACTIVATE_GOSSIP_CMD = "deactivateGossip";

    private static final String ACTIVATE_GOSSIP_CMD = "activateGossip";
    private static final String GOSSIP_CMD = "gossip";

    /**
     * Admin client entry point
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println(Admin.class.getSimpleName());
        System.out.printf("Received %d Argument(s)%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("args[%d] = %s%n", i, args[i]);
        }

        final AdminFrontend frontend = new AdminFrontend();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.printf("%n> ");

            String line = scanner.nextLine();
            String[] commandArgs = line.split(" ", 3);
            String command = commandArgs[0];
            String qualifier;
            String serverId;

            if (commandArgs.length < 2) {
                // If no qualifier is provided assume P as default
                qualifier = "P";
                serverId = "0";
            } else if (commandArgs.length < 3){
                qualifier = commandArgs[1];
                serverId = "0";
            }else {
                qualifier = commandArgs[1];
                serverId = commandArgs[2];
            }


            try {

                switch (command) {

                    case DUMP_CMD -> {
                        ClassesDefinitions.ClassState classState = frontend.dump(List.of(qualifier),serverId);
                        System.out.println(Stringify.format(classState));
                    }

                    case ACTIVATE_CMD -> {
                        System.out.println(Stringify.format(frontend.activate(List.of(qualifier),serverId)));
                    }

                    case DEACTIVATE_CMD -> {
                        System.out.println(Stringify.format(frontend.deactivate(List.of(qualifier),serverId)));
                    }

                    case DEACTIVATE_GOSSIP_CMD -> {
                        System.out.println(Stringify.format(frontend.deactivateGossip(List.of(qualifier),serverId)));
                    }

                    case ACTIVATE_GOSSIP_CMD -> {
                        System.out.println(Stringify.format(frontend.activateGossip(List.of(qualifier),serverId)));
                    }

                    case GOSSIP_CMD -> {
                        System.out.println(Stringify.format(frontend.gossip(List.of(qualifier),serverId)));
                    }

                    case EXIT_CMD -> {
                        scanner.close();
                        System.exit(0);
                    }
                }

            } catch (ResponseException exception) {
                System.out.println(Stringify.format(exception.getResponseCode()));
            }
        }
    }
}
