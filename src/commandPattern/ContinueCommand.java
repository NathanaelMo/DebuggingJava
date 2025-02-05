package commandPattern;

public class ContinueCommand implements Command {


    public ContinueCommand() {
    }

    @Override
    public void execute() {

    }

    @Override
    public boolean resume() {
        return true;
    }

}