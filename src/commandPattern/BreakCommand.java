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
            List<ReferenceType> classes = vm.allClasses();

            for (ReferenceType refType : classes) {
                try {
                    String r = refType.sourceName();
                    if (r.equals(fileName)) {
                        Location location = refType.locationsOfLine(lineNumber).get(0);
                        BreakpointRequest breakpointRequest =
                                vm.eventRequestManager().createBreakpointRequest(location);
                        breakpointRequest.enable();

                        System.out.println("Breakpoint sur " + fileName + " ligne " + lineNumber);
                        return;
                    }
                } catch (AbsentInformationException e) {
                    throw new RuntimeException(e);
                }
            }



    }
}