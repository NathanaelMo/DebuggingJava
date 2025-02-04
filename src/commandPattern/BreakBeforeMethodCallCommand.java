package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.request.BreakpointRequest;

import java.util.List;

public class BreakBeforeMethodCallCommand implements Command {
    private final VirtualMachine vm;
    private final String methodName;

    public BreakBeforeMethodCallCommand(VirtualMachine vm, String methodName) {
        this.vm = vm;
        this.methodName = methodName;
    }

    @Override
    public void execute() {
        try {
            // Parcourir toutes les classes chargées
            List<ReferenceType> classes = vm.allClasses();
            boolean methodFound = false;

            for (ReferenceType refType : classes) {
                // Chercher la méthode dans cette classe
                List<Method> methods = refType.methods();
                for (Method method : methods) {
                    if (method.name().equals(methodName)) {
                        methodFound = true;
                        // Créer un point d'arrêt au début de la méthode
                        Location location = method.location();
                        BreakpointRequest breakpointRequest =
                                vm.eventRequestManager().createBreakpointRequest(location);
                        breakpointRequest.enable();

                        System.out.println("Breakpoint set at the beginning of method '" +
                                methodName + "' in class '" + refType.name() + "'");
                    }
                }
            }

            if (!methodFound) {
                System.out.println("Method '" + methodName + "' not found in any loaded class");
            }

        } catch (Exception e) {
            System.err.println("Error setting method breakpoint: " + e.getMessage());
        }
    }
}