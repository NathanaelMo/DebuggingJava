package dbg;

import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import java.util.Map;

public class ExecutionState {
    final Location location;
    final Map<String, Value> variables;
    final ThreadReference thread;

    ExecutionState(Location loc, Map<String, Value> vars, ThreadReference t) {
        this.location = loc;
        this.variables = vars;
        this.thread = t;
    }
}
