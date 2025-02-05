package commandPattern;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.BreakpointRequest;

import java.util.List;

public class BreakpointsCommand implements Command {
    private final VirtualMachine vm;

    public BreakpointsCommand(VirtualMachine vm) {
        this.vm = vm;
    }

    @Override
    public void execute() {
            List<BreakpointRequest> breakpoints = vm.eventRequestManager().breakpointRequests();

            if (breakpoints.isEmpty()) {
                System.out.println("Aucun breakpoints");
                return;
            }

            System.out.println("Breakpoints:");
            for (BreakpointRequest bp : breakpoints) {
                Location location = bp.location();
                try {
                    System.out.println(String.format("- %s: line %d (enabled: %b)",
                            location.sourceName(),
                            location.lineNumber(),
                            bp.isEnabled()
                    ));
                } catch (AbsentInformationException e) {
                    throw new RuntimeException(e);
                }
            }
    }
}