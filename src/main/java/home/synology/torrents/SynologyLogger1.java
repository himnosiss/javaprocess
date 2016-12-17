package home.synology.torrents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class SynologyLogger1 {

	public static Path outputFilePath = Paths.get("/volume1/misc/javaProcess.log");

	private static StringBuilder initLogger(String level) {
		StringBuilder message = new StringBuilder("\n");
		message.append(LocalDateTime.now().toString());
		message.append(level);
		return message;
	}

	public static final void warn(String... s) {
		StringBuilder message = initLogger("WARN");
		for (String string : s) {
			message.append(string);
		}
		writeMessage(message);
	}

	public static final void info(Object... s) {
		StringBuilder message = initLogger(" INFO ");
		for (Object string : s) {
			message.append(String.valueOf(string));
		}
		writeMessage(message);
	}

	public static final void error(String... s) {
		StringBuilder message = initLogger(" ERROR ");
		for (String string : s) {
			message.append(string);
		}
		writeMessage(message);
	}

	private static void writeMessage(StringBuilder message) {

		try {
			Files.write(outputFilePath, message.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
