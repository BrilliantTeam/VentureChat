package mineverse.Aust1n46.chat.utilities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EmojiSpritesTest {
	private static final String SPRITE = "{\"type\":\"object\",\"object\":\"atlas\",\"atlas\":\"minecraft:blocks\",\"sprite\":\"brilliant:block/emoji/x9_meow\",\"fallback\":\":x9_meow:\"}";

	@Test
	public void splitsTextAroundAnimatedEmoji() {
		String json = "{\"text\":\"a:x9_meow:b\",\"color\":\"gray\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\":x9_meow:\"},\"extra\":[\"c\"]}";
		assertEquals("{\"text\":\"\",\"color\":\"gray\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\":x9_meow:\"},\"extra\":[\"a\"," + SPRITE + ",\"b\",\"c\"]}",
				EmojiSprites.apply(json));
	}

	@Test
	public void convertsStringsInsideArrays() {
		assertEquals("[\"\",{\"text\":\"\",\"extra\":[" + SPRITE + "]}]", EmojiSprites.apply("[\"\",\":x9_meow:\"]"));
	}

	@Test
	public void keepsMixedCaseCodeAndUnknownCodes() {
		assertEquals("{\"text\":\"\",\"extra\":[\":nope: \",{\"type\":\"object\",\"object\":\"atlas\",\"atlas\":\"minecraft:blocks\",\"sprite\":\"brilliant:block/emoji/v8_uto\",\"fallback\":\":v8_Uto:\"}]}",
				EmojiSprites.apply("{\"text\":\":nope: :v8_Uto:\"}"));
	}

	@Test
	public void leavesOtherTextAlone() {
		String json = "{\"text\":\"12:30:45 :nope:\"}";
		assertEquals(json, EmojiSprites.apply(json));
	}
}
