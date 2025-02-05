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
            List<ReferenceType> classes = vm.allClasses();
            for (ReferenceType refType : classes) {
                try {
                    if (refType.sourceName().equals(fileName)) {
                        Location location = refType.locationsOfLine(lineNumber).get(0);
                        BreakpointRequest breakpointRequest =
                                vm.eventRequestManager().createBreakpointRequest(location);


                        breakpointRequest.addCountFilter(count);
                        breakpointRequest.enable();

                        System.out.println("Breakpoint sur " + fileName + " ligne " + lineNumber +
                                " pour " + count + " fois");
                        return;
                    }
                } catch (AbsentInformationException e) {
                    throw new RuntimeException(e);
                }
            }
    }
}