package org.fountainmc.api.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final List<CommandHandler> commands;

    public CommandManager() {
        this.commands = new ArrayList<CommandHandler>();
    }

    public void registerCommands(Object source) {
        for (Method method : source.getClass().getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                if (method.isAnnotationPresent(Command.class)) {
                    Command command = method.getAnnotation(Command.class);
                    commands.add(new CommandHandler(command, method, source));
                }
            }
        }
    }

    public void fireCommand(String command, String[] arguments, CommandSender sender) {
        for (CommandHandler handler : commands) {
            if (handler.command.name().equalsIgnoreCase(command)) {
                fireCommand(handler, arguments, sender);
            }
        }
    }

    public void fireCommand(CommandHandler handler, String[] arguments, CommandSender sender) {
        Class<? extends CommandSender> senderClass = handler.command.allow();
        if (senderClass.isAssignableFrom(sender.getClass())) {
            try {
                handler.method.invoke(handler.source, arguments, senderClass.cast(sender));
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public List<CommandHandler> getCommands() {
        return commands;
    }

    public class CommandHandler {

        private final Command command;
        private final Method method;
        private final Object source;

        private CommandHandler(Command command, Method method, Object source) {
            this.command = command;
            this.method = method;
            this.source = source;
        }

        public Command getCommand() {
            return command;
        }

        public Method getMethod() {
            return method;
        }

        public Object getSource() {
            return source;
        }
    }

}
