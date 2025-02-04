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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

public class ScriptableDebugger {

    private Class debugClass;
    private VirtualMachine vm;

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

    public void startDebugger() throws VMDisconnectedException, InterruptedException, AbsentInformationException, IOException {
        EventSet eventSet = null;
        while ((eventSet = vm.eventQueue().remove()) != null) {
            for (Event event : eventSet) {
                System.out.println(event.toString());
                if(event instanceof VMDisconnectEvent ) {
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
                if(event instanceof ClassPrepareEvent ) {
                    setBreakPoint(debugClass.getName(),6);
                    setBreakPoint(debugClass.getName(),9);
                    setBreakPoint(debugClass.getName(),11);
                }
                if(event instanceof BreakpointEvent){
                    controleManuel(event);
                }
                if(event instanceof StepEvent){
                    event.request().disable();;
                    controleManuel(event);
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
        for (ReferenceType targetClass : vm.allClasses()) {
            if (targetClass.name().equals(className)) {
                Location location = targetClass.locationsOfLine(lineNumber).get(0);
                BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
                bpReq.enable();
            }
        }
    }

    private void controleManuel(Event event) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String command = reader.readLine();
        Command commandReceived = null;
        if (command != null && command.trim().equalsIgnoreCase("step")) {
            commandReceived = new StepCommand(vm, (LocatableEvent) event);
        } else if (command != null && command.trim().equalsIgnoreCase("step-over")) {
            commandReceived = new StepOverCommand(vm, (LocatableEvent) event);
        } else if (command != null && command.trim().equalsIgnoreCase("continue")) {
            commandReceived = new ContinueCommand();
        } else if (command != null && command.trim().equalsIgnoreCase("frame")) {
            commandReceived = new FrameCommand((LocatableEvent) event);
        } else {
            System.out.println("Commande inconnue ! Veuillez recommencer : ");
            controleManuel(event);
        }
        if (commandReceived != null){
            commandReceived.execute();
        }
    }
}
