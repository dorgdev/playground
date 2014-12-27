import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class XXX {

  public static class CommandReader {
    public String readCommand() { return ""; }
  }
  
  public static interface Command { 
    public void run(String[] args);
  }
  
  public static class CommandRepository {
    public static Command getCommand(String name) { return new CopyCommand(); }
  }
  
  public static class CopyCommand implements Command { 
    public void run(String[] args) {}
    public void prepareCopy() {}
  }
  public static class MoveCommand implements Command { 
    public void run(String[] args) {}
    public void prepareMove() {}
  }
  
  private static final Set<String> VALID_COMMANDS = new HashSet<String>(
      Arrays.asList("copy", "stat", "move", "list", "chmod", "delete"));

  public void ProcessCopyCommand(String[] params) {
    // ...
  }

  public String nextCommand() {
    CommandReader reader = new CommandReader();
    String command = reader.readCommand();
    while (command != null && !VALID_COMMANDS.contains(command)) {
      System.out.println("Invalid command, trying to read again...");
      command = reader.readCommand();
    }
    return command;
  }
  
  public void runTheNextCommand(String[] params) {
    String command = nextCommand();
    if (command == null) {
      System.out.println("No more commands. ");
      return;
    }
    if (command.equals("copy")) {
      ProcessCopyCommand(params);
    }
    // ...
  }

}
