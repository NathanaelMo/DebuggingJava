package dbg;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.StepRequest;

public class StepCommand implements Command {


    public StepCommand() {
    }

    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) {
        StepRequest stepRequest = vm.eventRequestManager().createStepRequest(event.thread(), StepRequest.STEP_MIN, StepRequest.STEP_OVER);
        stepRequest.enable();
    }
}