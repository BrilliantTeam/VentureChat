package mineverse.Aust1n46.chat.utilities;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public final class EmojiSprites {
	private static final Pattern CODE = Pattern.compile(":([A-Za-z0-9_]+):");
	private static final Map<String, String> SPRITES = load();

	private EmojiSprites() {
	}

	private static Map<String, String> load() {
		Map<String, String> sprites = new HashMap<>();
		InputStream stream = EmojiSprites.class.getResourceAsStream("/animated_emojis.txt");
		if (stream == null) {
			return sprites;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			reader.lines().map(line -> line.split("\t")).filter(parts -> parts.length == 2 && parts[1].matches("[a-z0-9_.-]+"))
					.forEach(parts -> sprites.put(parts[0], "brilliant:block/emoji/" + parts[1]));
		} catch (Exception ignored) {
		}
		return sprites;
	}

	public static String apply(String json) {
		if (SPRITES.isEmpty() || !hasSprite(json)) {
			return json;
		}
		try {
			return convert(JsonParser.parseString(json)).toString();
		} catch (RuntimeException e) {
			return json;
		}
	}

	private static boolean hasSprite(String text) {
		Matcher matcher = CODE.matcher(text);
		while (matcher.find()) {
			if (SPRITES.containsKey(matcher.group(1))) {
				return true;
			}
		}
		return false;
	}

	private static JsonElement convert(JsonElement element) {
		if (element.isJsonArray()) {
			JsonArray out = new JsonArray();
			for (JsonElement child : element.getAsJsonArray()) {
				out.add(convert(child));
			}
			return out;
		}
		if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
			JsonArray parts = split(element.getAsString());
			if (parts == null) {
				return element;
			}
			JsonObject wrapper = new JsonObject();
			wrapper.addProperty("text", "");
			wrapper.add("extra", parts);
			return wrapper;
		}
		if (!element.isJsonObject()) {
			return element;
		}
		JsonObject out = new JsonObject();
		for (Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
			JsonElement value = entry.getValue();
			out.add(entry.getKey(), value.isJsonPrimitive() ? value : convert(value));
		}
		JsonElement text = out.get("text");
		if (text != null && text.isJsonPrimitive()) {
			JsonArray parts = split(text.getAsString());
			if (parts != null) {
				JsonElement extra = out.get("extra");
				if (extra != null && extra.isJsonArray()) {
					parts.addAll(extra.getAsJsonArray());
				}
				out.addProperty("text", "");
				out.add("extra", parts);
			}
		}
		return out;
	}

	private static JsonArray split(String text) {
		if (!hasSprite(text)) {
			return null;
		}
		JsonArray parts = new JsonArray();
		Matcher matcher = CODE.matcher(text);
		int last = 0;
		while (matcher.find()) {
			String sprite = SPRITES.get(matcher.group(1));
			if (sprite == null) {
				continue;
			}
			if (matcher.start() > last) {
				parts.add(new JsonPrimitive(text.substring(last, matcher.start())));
			}
			JsonObject object = new JsonObject();
			object.addProperty("type", "object");
			object.addProperty("object", "atlas");
			object.addProperty("atlas", "minecraft:blocks");
			object.addProperty("sprite", sprite);
			object.addProperty("fallback", matcher.group());
			parts.add(object);
			last = matcher.end();
		}
		if (last < text.length()) {
			parts.add(new JsonPrimitive(text.substring(last)));
		}
		return parts;
	}
}
