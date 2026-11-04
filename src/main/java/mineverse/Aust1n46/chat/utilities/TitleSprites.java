package mineverse.Aust1n46.chat.utilities;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class TitleSprites {
	private static final Map<String, Title> TITLES = load();

	private TitleSprites() {
	}

	public static final class Title {
		private final String[] parts;
		private final String suffix;

		private Title(String[] parts, String suffix) {
			this.parts = parts;
			this.suffix = suffix;
		}

		public String json(String extensions) {
			StringBuilder json = new StringBuilder();
			for (String part : parts) {
				String[] sprite = part.split("=", 2);
				json.append("{\"type\":\"object\",\"object\":\"atlas\",\"atlas\":\"minecraft:blocks\",\"sprite\":\"brilliant:block/title/")
						.append(sprite[0]).append("\",\"fallback\":\"").append(sprite[1]).append("\",\"color\":\"white\"")
						.append(extensions).append("},");
			}
			return json.toString();
		}

		public String suffix() {
			return suffix;
		}
	}

	private static Map<String, Title> load() {
		Map<String, Title> titles = new HashMap<>();
		InputStream stream = TitleSprites.class.getResourceAsStream("/animated_titles.txt");
		if (stream == null) {
			return titles;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			reader.lines().map(line -> line.split("\t")).filter(parts -> parts.length == 3)
					.forEach(parts -> titles.put(parts[0], new Title(parts[1].split(","), parts[2])));
		} catch (Exception ignored) {
		}
		return titles;
	}

	public static Title find(String text) {
		return TITLES.get(text.toLowerCase(Locale.ROOT));
	}
}
