package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class PrintVarCommand implements Command {
    private final LocatableEvent event;
    private final String varName;

    public PrintVarCommand(LocatableEvent event, String varName) {
        this.event = event;
        this.varName = varName;
    }

    @Override
    public void execute() {
        StackFrame currentFrame = null;
        try {
            currentFrame = event.thread().frame(0);
            List<LocalVariable> localVars = currentFrame.visibleVariables();
            for (LocalVariable var : localVars) {
                if (var.name().equals(varName)) {
                    Value value = currentFrame.getValue(var);
                    System.out.println("variable " + varName + " → " + value);
                    return;
                }
            }


            ObjectReference thisObject = currentFrame.thisObject();
            if (thisObject != null) {
                ReferenceType type = thisObject.referenceType();
                List<Field> fields = type.allFields();
                for (Field field : fields) {
                    if (field.name().equals(varName)) {
                        Value value = thisObject.getValue(field);
                        System.out.println("Variable " + varName + " → " + value);
                        return;
                    }
                }
            }

            System.out.println("Variable '" + varName + "' non trouvée");
        } catch (IncompatibleThreadStateException | AbsentInformationException e) {
            throw new RuntimeException(e);
        }


    }
}