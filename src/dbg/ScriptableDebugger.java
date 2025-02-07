package dbg;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.StepRequest;
import commandPattern.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptableDebugger {

    Class debugClass;
    VirtualMachine vm;

    private boolean isStepBack = false;

    Location previousLocation;

    int nbStep = 0;

    private DebuggerGUI gui;

    private long targetCodeIndex = -1;

    List<Location> list = new ArrayList<>();

    public void setGUI(DebuggerGUI gui) {
        this.gui = gui;
    }

    public VirtualMachine connectAndLaunchVM() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(debugClass.getName());
        VirtualMachine vm = launchingConnector.launch(arguments);
        return vm;
    }
    public void attachTo(Class debuggeeClass) {

        this.debugClass = debuggeeClass;
        try {
            vm = connectAndLaunchVM();
            enableClassPrepareRequest(vm);
            startDebugger();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        } catch (VMStartException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        } catch (VMDisconnectedException e) {
            System.out.println("Virtual Machine is disconnected: " + e.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startDebugger() throws Exception {
        EventSet eventSet = null;
        while ((eventSet = vm.eventQueue().remove()) != null) {
            for (Event event : eventSet) {
                System.out.println(event.toString());
                if (event instanceof VMDisconnectEvent) {
                    System.out.println("End of program.");
                    InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
                    OutputStreamWriter writer = new OutputStreamWriter(System.out ) ;
                    try {
                        reader.transferTo(writer) ;
                        writer.flush();
                    }catch( IOException e ) {
                        System.out.println("Target VM input stream reading error.");
                    }
                }
                if (event instanceof BreakpointEvent) {
                    if (isStepBack && ((LocatableEvent)event).location().codeIndex() != targetCodeIndex) {
                        // Créer et activer une StepRequest pour avancer jusqu'à la bonne position
                        StepRequest stepRequest = vm.eventRequestManager().createStepRequest(
                                ((BreakpointEvent)event).thread(),
                                StepRequest.STEP_MIN,
                                StepRequest.STEP_INTO
                        );
                        stepRequest.enable();
                        vm.resume();
                        continue;
                    }
                }
                if (event instanceof StepEvent) {
                    if (isStepBack && ((LocatableEvent)event).location().codeIndex() != targetCodeIndex) {
                        event.request().enable();
                        vm.resume();
                        continue;
                    }
                    event.request().disable();
                }
                if (event instanceof ClassPrepareEvent) {
                    if(isStepBack){
                        setBreakPointStepBack(debugClass.getName());
                    } else {
                        setBreakPoint(debugClass.getName(), 19);
                    }
                }
                if (event instanceof BreakpointEvent || event instanceof StepEvent) {
                    Command command = null;
                    boolean resume = false;
                    while (!resume) {
                        if (!(event instanceof BreakpointEvent)) {
                            event.request().disable();
                        }
                        command = controleManuel(event);
                        if (command == null) {
                            resume = false;
                        } else {
                            resume = command.resume();
                        }
                    }
                }

                vm.resume();
            }
        }
    }

    public void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter(debugClass.getName());
        classPrepareRequest.enable();
    }

    public void setBreakPoint(String className, int lineNumber) throws AbsentInformationException {
        for (ReferenceType refType : vm.allClasses()) {
            if (refType.name().equals(className)) {
                Location location = refType.locationsOfLine(lineNumber).get(0);
                BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
                bpReq.enable();
            }
        }
    }

    public void setBreakPointStepBack(String className) throws AbsentInformationException {
        for (ReferenceType refType : vm.allClasses()) {
            if (refType.name().equals(className)) {
                int lineNumber = previousLocation.lineNumber();
                Location lineLocation = refType.locationsOfLine(lineNumber).get(0);
                BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(lineLocation);
                bpReq.enable();
                targetCodeIndex = previousLocation.codeIndex();
            }
        }
    }

    private Command controleManuel(Event event) throws IOException {
        isStepBack = false;
        String command = gui.getNextCommand();;
        Command commandReceived = null;

        if (command != null && command.trim().equalsIgnoreCase("step")) {
            commandReceived = new StepCommand(vm, (LocatableEvent) event);
            list.add(((LocatableEvent) event).location());
            nbStep++;
        } else if (command != null && command.trim().equalsIgnoreCase("step-over")) {
            commandReceived = new StepOverCommand(vm, (LocatableEvent) event);
            list.add(((LocatableEvent) event).location());
            nbStep++;
        } else if (command != null && command.trim().equalsIgnoreCase("continue")) {
            commandReceived = new ContinueCommand();
            list.add(((LocatableEvent) event).location());
            nbStep++;
        } else if (command != null && command.trim().equalsIgnoreCase("frame")) {
            commandReceived = new FrameCommand((LocatableEvent) event);
        } else if (command != null && command.trim().equalsIgnoreCase("temporaries")) {
            commandReceived = new TemporariesCommand((LocatableEvent) event);
        } else if (command != null && command.trim().equalsIgnoreCase("stack")) {
            commandReceived = new StackCommand((LocatableEvent) event);
        } else if (command != null && command.trim().equalsIgnoreCase("receiver")) {
            commandReceived = new ReceiverCommand((LocatableEvent) event);
        } else if (command != null && command.trim().equalsIgnoreCase("sender")) {
            commandReceived = new SenderCommand((LocatableEvent) event);
        } else if (command != null && command.trim().equalsIgnoreCase("receiver-variables")) {
            commandReceived = new ReceiverVariablesCommand((LocatableEvent) event);
        } else if (command != null && command.trim().equalsIgnoreCase("method")) {
            commandReceived = new MethodCommand((LocatableEvent) event);
        } else if (command != null && command.trim().equalsIgnoreCase("arguments")) {
            commandReceived = new ArgumentsCommand((LocatableEvent) event);
        } else if (command != null && command.startsWith("print-var(") && command.endsWith(")")) {
            String varName = command.substring(10, command.length() - 1);
            commandReceived = new PrintVarCommand((LocatableEvent) event, varName);
        } else if (command != null && command.startsWith("break(") && command.endsWith(")")) {
            String args = command.substring(6, command.length() - 1);
            String[] parts = args.split(" ");
            if (parts.length == 2) {
                String fileName = parts[0].trim();
                int lineNumber = Integer.parseInt(parts[1].trim());
                commandReceived = new BreakCommand(vm, fileName, lineNumber);
            } else {
                System.out.println("Erreur de format pour break(filename ligne)");
            }
        } else if (command != null && command.trim().equalsIgnoreCase("breakpoints")) {
            commandReceived = new BreakpointsCommand(vm);
        }  else if (command != null && command.startsWith("break-once(") && command.endsWith(")")) {
            String args = command.substring(11, command.length() - 1);
            String[] parts = args.split(" ");
            if (parts.length == 2) {
                String fileName = parts[0].trim();
                int lineNumber = Integer.parseInt(parts[1].trim());
                commandReceived = new BreakOnceCommand(vm, fileName, lineNumber);
            } else {
                System.out.println("Erreur de format pour break-once(filename ligne)");
            }
        } else if (command != null && command.startsWith("break-on-count(") && command.endsWith(")")) {
            String args = command.substring(15, command.length() - 1);
            String[] parts = args.split(" ");
            if (parts.length == 3) {
                String fileName = parts[0].trim();
                int lineNumber = Integer.parseInt(parts[1].trim());
                int count = Integer.parseInt(parts[2].trim());
                commandReceived = new BreakOnCountCommand(vm, fileName, lineNumber, count);
            } else {
                System.err.println("Erreur de format pour break-on-count(filename ligne nombre)");
            }
        } else if (command != null && command.startsWith("break-before-method-call(") && command.endsWith(")")) {
            String methodName = command.substring(25, command.length() - 1).trim();
            commandReceived = new BreakBeforeMethodCallCommand(vm, methodName);
        } else if (command.trim().equalsIgnoreCase("step-back")) {
            if(nbStep > 0) {
                previousLocation = list.getLast();
                list.removeLast();
                nbStep--;
                try {
                    vm = connectAndLaunchVM();
                    enableClassPrepareRequest(vm);

                    isStepBack = true;
                    return new ContinueCommand();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Nombre incorrect");
            }
        } else if (command.startsWith("step-back(") && command.endsWith(")")) {
            int n = Integer.parseInt(command.substring(10, command.length() - 1));
            if(n>0 && n <= nbStep){
                previousLocation = list.get(list.size() - n);
                for(int i = 0; i < n;i++){
                    list.removeLast();
                    nbStep--;
                }
                try {
                    vm = connectAndLaunchVM();
                    enableClassPrepareRequest(vm);

                    isStepBack = true;
                    return new ContinueCommand();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Nombre incorrect");
            }

        } else {
            System.out.println("Commande inconnue ! Veuillez recommencer : ");
        }
        if (commandReceived != null){
            commandReceived.execute();
        }
        return commandReceived;
    }
}
