package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.request.BreakpointRequest;

import java.util.List;

public class BreakCommand implements Command {
    private final VirtualMachine vm;
    private final String fileName;
    private final int lineNumber;

    public BreakCommand(VirtualMachine vm, String fileName, int lineNumber) {
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
                // Vérifier si c'est la bonne classe (en comparant avec le nom de fichier)
                if (refType.sourceName().equals(fileName)) {
                    // Obtenir la location pour la ligne demandée
                    Location location = refType.locationsOfLine(lineNumber).get(0);

                    // Créer et activer le point d'arrêt
                    BreakpointRequest breakpointRequest =
                            vm.eventRequestManager().createBreakpointRequest(location);
                    breakpointRequest.enable();

                    System.out.println("Breakpoint set in " + fileName + " at line " + lineNumber);
                    return;
                }
            }

            System.out.println("Could not set breakpoint: file " + fileName + " not found");

        } catch (AbsentInformationException e) {
            System.err.println("Error: Source file information not available");
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Error: Invalid line number " + lineNumber + " in " + fileName);
        } catch (Exception e) {
            System.err.println("Error setting breakpoint: " + e.getMessage());
        }
    }
}