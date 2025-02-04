package commandPattern;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.BreakpointRequest;

import java.util.List;

public class BreakOnceCommand implements Command {
    private final VirtualMachine vm;
    private final String fileName;
    private final int lineNumber;

    public BreakOnceCommand(VirtualMachine vm, String fileName, int lineNumber) {
        this.vm = vm;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    @Override
    public void execute() {
        try {
            // Parcourir toutes les classes chargées
            List<ReferenceType> classes = vm.allClasses();

            for (ReferenceType refType : classes) {
                if (refType.sourceName().equals(fileName)) {
                    Location location = refType.locationsOfLine(lineNumber).get(0);

                    // Créer le point d'arrêt
                    BreakpointRequest breakpointRequest =
                            vm.eventRequestManager().createBreakpointRequest(location);

                    // Configurer pour qu'il se désactive après avoir été atteint
                    breakpointRequest.addCountFilter(1);
                    breakpointRequest.enable();

                    System.out.println("One-time breakpoint set in " + fileName + " at line " + lineNumber);
                    return;
                }
            }

            System.out.println("Could not set breakpoint: file " + fileName + " not found");

        } catch (AbsentInformationException e) {
            System.err.println("Error: Source file information not available");
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Error: Invalid line number " + lineNumber + " in " + fileName);
        } catch (Exception e) {
            System.err.println("Error setting one-time breakpoint: " + e.getMessage());
        }
    }
}