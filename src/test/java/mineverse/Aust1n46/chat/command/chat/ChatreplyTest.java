package mineverse.Aust1n46.chat.command.chat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChatreplyTest {
	@Test
	public void testShorten() {
		assertEquals("test", Chatreply.shorten("test", 30));
		assertEquals("abc…", Chatreply.shorten("abcdef", 3));
		assertEquals("😀😀…", Chatreply.shorten("😀😀😀", 2));
		assertEquals("abcdef", Chatreply.shorten("abcdef", 0));
	}

	@Test
	public void testShortenKeepsTags() {
		String tag = "<chat=4fad18b1-428d-4a38-a4cc-47919903afd2:[i]:>";
		assertEquals(tag + " 1", Chatreply.shorten(tag + " 1", 30));
		assertEquals("ab" + tag + "…", Chatreply.shorten("ab" + tag + "cdef", 4));
		assertEquals("abc…", Chatreply.shorten("abc" + tag, 3));
	}
}
