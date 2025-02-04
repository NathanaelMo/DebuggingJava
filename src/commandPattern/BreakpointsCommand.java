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
        try {
            List<BreakpointRequest> breakpoints = vm.eventRequestManager().breakpointRequests();

            if (breakpoints.isEmpty()) {
                System.out.println("No active breakpoints");
                return;
            }

            System.out.println("Active breakpoints:");
            for (BreakpointRequest bp : breakpoints) {
                Location location = bp.location();
                System.out.println(String.format("- %s: line %d (enabled: %b)",
                        location.sourceName(),
                        location.lineNumber(),
                        bp.isEnabled()
                ));
            }
        } catch (AbsentInformationException e) {
            System.err.println("Error: Source file information not available");
        } catch (Exception e) {
            System.err.println("Error listing breakpoints: " + e.getMessage());
        }
    }
}