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
            List<ReferenceType> classes = vm.allClasses();

            for (ReferenceType refType : classes) {
                try {
                    if (refType.sourceName().equals(fileName)) {
                        Location location = refType.locationsOfLine(lineNumber).get(0);


                        BreakpointRequest breakpointRequest = vm.eventRequestManager().createBreakpointRequest(location);

                        breakpointRequest.addCountFilter(1);
                        breakpointRequest.enable();

                        System.out.println("Breakpoint once sur " + fileName + " ligne " + lineNumber);
                        return;
                    }
                } catch (AbsentInformationException e) {
                    throw new RuntimeException(e);
                }
            }
    }
}