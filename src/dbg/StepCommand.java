package dbg;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.StepRequest;

public class StepCommand implements Command {
    private final LocatableEvent event;
    private final VirtualMachine vm;


    public StepCommand(VirtualMachine vm, LocatableEvent event) {
        this.event = event;
        this.vm = vm;
    }

    @Override
    public void execute() {
        StepRequest stepRequest = vm.eventRequestManager().createStepRequest(event.thread(), StepRequest.STEP_MIN, StepRequest.STEP_OVER);
        stepRequest.enable();
    }
}