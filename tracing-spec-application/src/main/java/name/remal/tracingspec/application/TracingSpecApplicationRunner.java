/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.remal.tracingspec.application;

import static java.util.Comparator.comparing;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IFactory;

@Component
@RequiredArgsConstructor
@ToString
public class TracingSpecApplicationRunner implements CommandLineRunner {

    private final IFactory picocliFactory;

    private final Collection<CommandLineCommand> commandLineCommands;

    @Override
    @SneakyThrows
    public void run(String... args) {
        val rootCommand = new RootCommand();
        val commandLine = new CommandLine(rootCommand, picocliFactory);
        commandLineCommands.stream()
            .map(cmd -> new CommandLine(cmd, picocliFactory))
            .sorted(comparing(cmd -> cmd.getCommandSpec().name()))
            .forEach(commandLine::addSubcommand);
        commandLine.addSubcommand(new HelpCommand());
        processCommands(commandLine);

        val thrownException = new AtomicReference<Throwable>();
        commandLine.setExecutionExceptionHandler((exception, cl, pr) -> {
            thrownException.set(exception);
            return Integer.MIN_VALUE;
        });

        val exitCode = commandLine.execute(args);
        if (exitCode != 0) {
            val exception = thrownException.get();
            if (exception != null) {
                throw exception;
            }

            throw new ExitException(exitCode);
        }
    }

    private static void processCommands(CommandLine commandLine) {
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);

        commandLine.setUnmatchedArgumentsAllowed(true);
        commandLine.setUnmatchedOptionsAllowedAsOptionParameters(true);
        commandLine.setUnmatchedOptionsArePositionalParams(false);

        if (commandLine.getCommandSpec().root().name().isEmpty()) {
            commandLine.getCommandSpec().usageMessage().synopsisHeading("Usage:");
        }

        commandLine.getSubcommands().values().forEach(TracingSpecApplicationRunner::processCommands);
    }

    @Command(
        name = "",
        versionProvider = VersionProvider.class,
        mixinStandardHelpOptions = false
    )
    @ToString
    private static class RootCommand {
    }

}
