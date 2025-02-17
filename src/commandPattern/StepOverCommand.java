package commandPattern;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.StepRequest;

public class StepOverCommand implements Command {

    private final LocatableEvent event;
    private final VirtualMachine vm;


    public StepOverCommand(VirtualMachine vm, LocatableEvent event) {
        this.event = event;
        this.vm = vm;
    }

    @Override
    public void execute() {
        StepRequest stepRequest = vm.eventRequestManager().createStepRequest(event.thread(), StepRequest.STEP_LINE, StepRequest.STEP_OVER);
        stepRequest.enable();
    }

    @Override
    public boolean resume() {
        return true;
    }

}