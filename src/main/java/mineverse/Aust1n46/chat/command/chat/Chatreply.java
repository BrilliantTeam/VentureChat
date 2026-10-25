package mineverse.Aust1n46.chat.command.chat;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.clip.placeholderapi.PlaceholderAPI;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import mineverse.Aust1n46.chat.utilities.Format;
import mineverse.Aust1n46.chat.utilities.ScheduleUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Chatreply extends Command {
	private static final int CACHE_SIZE = 500;
	private static final long PENDING_MILLIS = 3000;
	private static final Pattern TAG_PATTERN = Pattern.compile("<[a-zA-Z]+=[0-9a-fA-F-]{36}:(.*?):>");
	private static final Map<Integer, String[]> messages = Collections.synchronizedMap(new LinkedHashMap<Integer, String[]>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, String[]> eldest) {
			return size() > CACHE_SIZE;
		}
	});
	private static final Map<UUID, SimpleEntry<String[], Long>> pending = new ConcurrentHashMap<>();
	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();

	public Chatreply() {
		super("chatreply");
	}

	private static FileConfiguration config() {
		return MineverseChat.getInstance().getConfig();
	}

	public static boolean isEnabled() {
		return config().getBoolean("reply.enabled", false);
	}

	public static void remember(int hash, String sender, String chat) {
		if (isEnabled()) {
			messages.put(hash, new String[] { sender, chat });
		}
	}

	public static String sender(Player player) {
		if (!isEnabled()) {
			return "";
		}
		return Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(player, config().getString("reply.sender", "{player_name}：")));
	}

	public static String[] take(Player player) {
		SimpleEntry<String[], Long> entry = pending.remove(player.getUniqueId());
		return entry == null || System.currentTimeMillis() - entry.getValue() > PENDING_MILLIS ? null : entry.getKey();
	}

	public static String quote(Player player, String[] reply) {
		return Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(player, config().getString("reply.format", "&8┌── {reply_sender}&8| &7{reply_message}")))
				.replace("{reply_sender}", reply[0])
				.replace("{reply_message}", shorten(Format.stripColor(reply[1]).trim(), config().getInt("reply.max-length", 30))) + "\n";
	}

	static String untag(String message) {
		return TAG_PATTERN.matcher(message).replaceAll("$1");
	}

	private static Component text(String path, String def) {
		return LEGACY.deserialize(Format.FormatStringAll(config().getString(path, def)));
	}

	private static void openDialog(Player player, String[] reply) {
		Dialog dialog = Dialog.create(factory -> factory.empty()
				.base(DialogBase.builder(text("reply.dialog.title", "&f回覆訊息"))
						.body(List.of(DialogBody.plainMessage(LEGACY.deserialize(reply[0] + untag(reply[1])), 300)))
						.inputs(List.of(DialogInput.text("message", text("reply.dialog.label", "&7訊息內容"))
								.width(300)
								.maxLength(256)
								.build()))
						.build())
				.type(DialogType.confirmation(
						ActionButton.builder(text("reply.dialog.send", "&a送出"))
								.action(DialogAction.customClick((response, audience) -> {
									String message = response.getText("message");
									if (message != null && !message.isBlank()) {
										ScheduleUtil.runEntityTask(MineverseChat.getInstance(), player, () -> send(player, reply, message.trim()));
									}
								}, ClickCallback.Options.builder().uses(1).build()))
								.build(),
						ActionButton.builder(text("reply.dialog.cancel", "&c取消")).build())));
		player.showDialog(dialog);
	}

	private static void send(Player player, String[] reply, String message) {
		pending.put(player.getUniqueId(), new SimpleEntry<>(reply, System.currentTimeMillis()));
		player.chat(message);
	}

	static String shorten(String message, int max) {
		if (max <= 0) {
			return message;
		}
		StringBuilder result = new StringBuilder();
		Matcher matcher = TAG_PATTERN.matcher(message);
		int index = 0;
		while (index < message.length()) {
			if (max <= 0) {
				return result + "…";
			}
			if (matcher.find(index) && matcher.start() == index) {
				result.append(matcher.group());
				max -= matcher.group(1).codePointCount(0, matcher.group(1).length());
				index = matcher.end();
			} else {
				int next = message.offsetByCodePoints(index, 1);
				result.append(message, index, next);
				max--;
				index = next;
			}
		}
		return result.toString();
	}

	@Override
	public boolean execute(CommandSender sender, String command, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(LocalizedMessage.COMMAND_MUST_BE_RUN_BY_PLAYER.toString());
			return true;
		}
		if (args.length < 1) {
			sender.sendMessage(LocalizedMessage.COMMAND_INVALID_ARGUMENTS.toString().replace("{command}", "/chatreply").replace("{args}", "[id] {message}"));
			return true;
		}
		String[] reply = null;
		try {
			reply = messages.get(Integer.parseInt(args[0]));
		} catch (NumberFormatException ignored) {
		}
		if (reply == null || !isEnabled()) {
			sender.sendMessage(Format.FormatStringAll(config().getString("reply.expired", "&c此訊息已無法回覆。")));
			return true;
		}
		Player player = (Player) sender;
		if (args.length == 1) {
			openDialog(player, reply);
		} else {
			send(player, reply, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
		}
		return true;
	}
}
