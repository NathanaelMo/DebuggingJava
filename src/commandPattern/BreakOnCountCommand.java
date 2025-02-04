package commandPattern;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.BreakpointRequest;

import java.util.List;

public class BreakOnCountCommand implements Command {
    private final VirtualMachine vm;
    private final String fileName;
    private final int lineNumber;
    private final int count;

    public BreakOnCountCommand(VirtualMachine vm, String fileName, int lineNumber, int count) {
        this.vm = vm;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.count = count;
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

                    // Ne s'active qu'après avoir été atteint 'count' fois
                    breakpointRequest.addCountFilter(count);
                    breakpointRequest.enable();

                    System.out.println("Breakpoint set in " + fileName + " at line " + lineNumber +
                            " to trigger on " + count + "th hit");
                    return;
                }
            }

            System.out.println("Could not set breakpoint: file " + fileName + " not found");

        } catch (AbsentInformationException e) {
            System.err.println("Error: Source file information not available");
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Error: Invalid line number " + lineNumber + " in " + fileName);
        } catch (Exception e) {
            System.err.println("Error setting count breakpoint: " + e.getMessage());
        }
    }
}