/**
 * 
 */
package net.leegorous.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author leegorous
 * 
 */
public class ConfigPattern {

	public final static String WHITE_SPACE = "\\s*";
	public final static String END = WHITE_SPACE + "(;\\s*(\\r?\\n)?|\\r?\\n)";

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private Pattern pattern;

	public ConfigPattern(String regex) {
		setPattern(regex);
	}

	public ConfigPattern(String name, String regex) {
		setPattern("@" + name + WHITE_SPACE + "(" + regex + ")" + END);
	}

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(String regex) {
		setPattern(Pattern.compile(regex));
	}

	public void setPattern(Pattern pattern) {
		if (pattern == null)
			throw new IllegalArgumentException("pattern could not be null");
		this.pattern = pattern;
	}

	public String getValue(String string) {
		Matcher m = getMatcher(string);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	public Matcher getMatcher(String str) {
		checkPattern();
		return pattern.matcher(str);
	}

	public String toString() {
		checkPattern();
		return pattern.pattern();
	}

	private void checkPattern() {
		if (pattern == null)
			throw new IllegalStateException("pattern not set");
	}
}
