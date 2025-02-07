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

    //Vqariable pour savoir si la vm est démarrée après un stepback
    private boolean isStepBack = false;

    //Location du step precedent
    Location previousLocation;

    //Nombre de commande step ou step-over ou continue
    int nbStep = 0;

    private DebuggerGUI gui;

    //numéro pour savoir à quelle endroit de la ligne on est après un stepback
    private long targetCodeIndex = -1;

    //liste des location
    List<Location> list = new ArrayList<>();

    //Configure l'interface
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
                    //Si on arrive au breakpoint après un step-back on lance un evenement de step si on est pas encore au bon endroit dans la ligne
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
                    //Après un step back on step si on est pas encore au bon endroit dans la ligne
                    if (isStepBack && ((LocatableEvent)event).location().codeIndex() != targetCodeIndex) {
                        event.request().enable();
                        vm.resume();
                        continue;
                    }
                    event.request().disable();
                }
                if (event instanceof ClassPrepareEvent) {
                    if(isStepBack){
                        //Après step back on utilise cette méthode
                        setBreakPointStepBack(debugClass.getName());
                    } else {
                        setBreakPoint(debugClass.getName(), 19);
                    }
                }
                if (event instanceof BreakpointEvent || event instanceof StepEvent) {
                    Command command = null;
                    boolean resume = false;
                    //On reste dans la boucle si la commande envoyer n'est pas step step-over ou continue
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

    //Recupere dans la variable targetCodeIndex l'endroit ou nous etions dans la ligne
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

    //Gestionnaire de commande
    private Command controleManuel(Event event) throws IOException {
        isStepBack = false;
        String command = gui.getNextCommand();;
        Command commandReceived = null;

        if (command != null && command.trim().equalsIgnoreCase("step")) {
            commandReceived = new StepCommand(vm, (LocatableEvent) event);
            //on ajoute la location dans la variable list avant d'exécuter le step
            list.add(((LocatableEvent) event).location());
            nbStep++;
        } else if (command != null && command.trim().equalsIgnoreCase("step-over")) {
            commandReceived = new StepOverCommand(vm, (LocatableEvent) event);
            //on ajoute la location dans la variable list avant d'exécuter le step over
            list.add(((LocatableEvent) event).location());
            nbStep++;
        } else if (command != null && command.trim().equalsIgnoreCase("continue")) {
            commandReceived = new ContinueCommand();
            //on ajoute la location dans la variable list avant d'exécuter le continue
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
            //on vérifie que nbStep est superieur à 0
            if(nbStep > 0) {
                //on récupere la derniere location de la liste et on initialise previousLocation
                previousLocation = list.getLast();
                //on enleve la derniere location de la liste
                list.removeLast();
                //On enleve 1 à nbStep
                nbStep--;
                try {
                    //On redemarre la vm
                    vm = connectAndLaunchVM();
                    enableClassPrepareRequest(vm);
                    //on modifie le isStepBack à true
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
            //on vérifie que n est superieur à 0 et inferieur ou egal à nbStep
            if(n>0 && n <= nbStep){
                //on récupere la location voulue
                previousLocation = list.get(list.size() - n);
                for(int i = 0; i < n;i++){
                    //On supprime les locations inutiles
                    list.removeLast();
                    //décrement nbStep
                    nbStep--;
                }
                try {

                    //On redemarre la vm
                    vm = connectAndLaunchVM();
                    enableClassPrepareRequest(vm);
                    //on modifie le isStepBack à true
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
