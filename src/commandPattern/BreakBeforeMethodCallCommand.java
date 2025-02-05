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
            List<ReferenceType> classes = vm.allClasses();
            boolean methodFound = false;
            for (ReferenceType refType : classes) {
                List<Method> methods = refType.methods();
                for (Method method : methods) {
                    if (method.name().equals(methodName)) {
                        methodFound = true;
                        Location location = method.location();
                        BreakpointRequest breakpointRequest =
                                vm.eventRequestManager().createBreakpointRequest(location);
                        breakpointRequest.enable();

                        System.out.println("Breakpoint au début de la méthode '" +
                                methodName + "' dans la classe '" + refType.name() + "'");
                    }
                }
            }

            if (!methodFound) {
                System.out.println("La methode '" + methodName + "' n'a pas été trouvée'");
            }
    }
}