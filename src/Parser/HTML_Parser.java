package Parser;

import java.util.Scanner;
import java.util.regex.Pattern;
import Nodes.*;

public class HTML_Parser {

	// https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
	// https://docs.oracle.com/javase/7/docs/api/java/util/regex/Matcher.html
	// https://stackoverflow.com/questions/43932001/test-to-see-if-string-contains-2-letters-followed-by-7-numbers
	// https://www.tutorialspoint.com/java/java_regular_expressions.htm
	// https://www.tutorialspoint.com/java/java_regular_expressions.htm
	// https://stackoverflow.com/questions/43932001/test-to-see-if-string-contains-2-letters-followed-by-7-numbers

	private HTML_Node htmlNode;
	private static final Pattern PARAGRAPH = Pattern.compile("\\s*^\\s*$\\s*", Pattern.MULTILINE);
	private static final Pattern ITALIC = Pattern.compile("[*]{1}?[a-zA-Z0-9]+[*]{1}");
	private static final Pattern BOLD = Pattern.compile("[*]{2}[a-zA-Z0-9]*[*]{2}");
	private static final Pattern HEADING1 = Pattern.compile("[#]{1}[ ]*");
	private static final Pattern HEADING2 = Pattern.compile("[#]{2}[ ]*");
	private static final Pattern NUMBERED_LIST = Pattern.compile("[0-9]{1}[\\.]{1}");
	private static final Pattern BULLETED_LIST = Pattern.compile("[*]{1}[a-zA-Z0-9]*");
	private static final Pattern SEPERATOR = Pattern.compile("---");
	private static final Pattern BLOCK_QUOTE = Pattern.compile(">");
	private static final Pattern BLOCK_CODE = Pattern.compile("```");
	private static final Pattern INLINE_CODE = Pattern.compile("`*([^`]*)`");

	public HTML_Parser() {
		this.htmlNode = new HTML_Node();
	}

	/**
	 * 
	 * @return
	 */
	public HTML_Node getHtmlNode() {
		return htmlNode;
	}

	/**
	 * 
	 * @param htmlNode
	 */
	public void setHtmlNode(HTML_Node htmlNode) {
		this.htmlNode = htmlNode;
	}

	/**
	 * takes in a scanner and then builds a tree like structure
	 * 
	 * @param scan
	 */
	public void parseScanner(Scanner scan) {
		while (scan.hasNextLine()) {
			scan.reset();
			if (scan.hasNext(BLOCK_QUOTE)) {
				this.htmlNode.addNode(parseBlockQuote(scan.nextLine()));
			} else if (scan.hasNext(HEADING1)) {
				String line = scan.nextLine();
				if (line.length() != 0) {
					this.htmlNode.addNode(parseHeader1(line));
				}
			} else if (scan.hasNext(HEADING2)) {
				String stliner = scan.nextLine();
				if (stliner.length() != 0) {
					this.htmlNode.addNode(parseHeader2(stliner));
				}
			} else if (scan.hasNext(NUMBERED_LIST)) {
				Numbered_List_Wrapper_Node wrap = new Numbered_List_Wrapper_Node();
				while (scan.hasNext(NUMBERED_LIST)) {
					scan.next();
					scan.useDelimiter("");
					scan.next();
					scan.reset();
					Number_List_Token_Node token = new Number_List_Token_Node();
					token.addNode(new TextNode(scan.nextLine()));
					wrap.addNode(token);
				}
				this.htmlNode.addNode(wrap);
			} else if (scan.hasNext(ITALIC)) {
				String line = scan.nextLine();
				if (line.length() != 0) {
					Paragraph_Node para = new Paragraph_Node();
					para.addNode(parseItalic(line));
					this.htmlNode.addNode(para);
				}
			} else if (scan.hasNext(BOLD)) {
				String line = scan.nextLine();
				if (line.length() != 0) {
					Paragraph_Node para = new Paragraph_Node();
					para.addNode(parseBold(line));
					this.htmlNode.addNode(para);
				}
			} else if (scan.hasNext(BULLETED_LIST)) {
				Bulleted_List_Wrapper_Node bullet = new Bulleted_List_Wrapper_Node();
				while (scan.hasNext(BULLETED_LIST)) {
					bullet.addNode(parseBulletedList(scan.nextLine()));
				}
				this.htmlNode.addNode(bullet);
			} else if (scan.hasNext(SEPERATOR)) {
				this.htmlNode.addNode(new Seperator_Node());
				scan.nextLine();
			} else if (scan.hasNext(BLOCK_CODE)) {
				scan.nextLine();
				Block_Code_Node block = new Block_Code_Node();
				Inline_Node code = new Inline_Node();
				while (scan.hasNextLine() && !scan.hasNext(BLOCK_CODE)) {
					code.addNode(new TextNode(scan.nextLine()));
				}
				block.addNode(code);
				this.htmlNode.addNode(block);
				if (scan.hasNextLine()) {
					scan.nextLine();
				}
			} else if (scan.hasNext(INLINE_CODE)) {
				this.htmlNode.addNode(parseInlineCode(scan.nextLine()));
			} else {
				scan.useDelimiter("");
				if (!scan.hasNext("\n") && !scan.hasNext(PARAGRAPH)) {
					this.htmlNode.addNode(parseParagraph(scan.nextLine()));

				} else {
					scan.nextLine();
				}
				scan.reset();
			}
		}
		scan.close();
	}

	/**
	 * creates inline code <code></code>
	 * 
	 * @param nextLine
	 * @return
	 */
	private AbstractNode parseInlineCode(String nextLine) {
		StringBuilder str = new StringBuilder();
		for (Character c : nextLine.toCharArray()) {
			if (c != '`') {
				str.append(c);
			}

		}
		TextNode text = new TextNode(str.toString());
		Inline_Node inline = new Inline_Node();
		inline.addNode(text);
		return inline;
	}

	/**
	 * creates bulleted list
	 * <li></li>
	 * 
	 * @param nextLine
	 * @return
	 */
	private AbstractNode parseBulletedList(String nextLine) {
		// Bulleted_List_Wrapper_Node bulletWrap = new
		// Bulleted_List_Wrapper_Node();
		Scanner scan = new Scanner(nextLine);
		if(scan.hasNext()){
			scan.next();
		}
		scan.useDelimiter("");
		if (scan.hasNext()) {
			scan.next();
		}
		Bulleted_List_Node bulletNode = new Bulleted_List_Node();
		StringBuilder str = new StringBuilder();
		while (scan.hasNext()) {
			str.append(scan.next());
		}
		scan.close();
		bulletNode.addNode(new TextNode(str.toString()));
		// bulletWrap.addNode(bulletNode);
		return bulletNode;
	}

	/**
	 * creates paragaph
	 * <p>
	 * </p>
	 * 
	 * @param nextLine
	 * @return
	 */
	private AbstractNode parseParagraph(String nextLine) {
		Paragraph_Node para = new Paragraph_Node();
		Scanner scan = new Scanner(nextLine);
		while (scan.hasNext()) {
			para.addNode(new TextNode(scan.next()));
		}
		scan.close();
		return para;
	}

	/**
	 * creates <blockquote> </blockquote> node
	 * 
	 * @param line
	 * @return
	 */
	private AbstractNode parseBlockQuote(String line) {
		Scanner scan = new Scanner(line);
		scan.useDelimiter("");
		for (int i = 0; i < 2; i++) {
			if (scan.hasNext()) {
				scan.next();
			}
		}
		Block_Quote_Node block = new Block_Quote_Node();
		Paragraph_Node para = new Paragraph_Node();
		if (scan.hasNextLine()) {
			para.addNode(new TextNode(scan.nextLine()));
		}
		block.addNode(para);
		scan.close();
		return block;
	}

	/**
	 * parses text and turns it into an
	 * <h1></h1> node
	 * 
	 * @param scan
	 * @return
	 */
	private AbstractNode parseHeader1(String line) {
		Scanner scan = new Scanner(line);
		if (scan.hasNext()) {
			scan.next();
			scan.useDelimiter("");
			if (scan.hasNext()) {
				scan.next();
			}
			scan.reset();
		}
		HeaderNode1 h1 = new HeaderNode1();
		while (scan.hasNext()) {
			if (scan.hasNext(ITALIC)) {
				h1.addNode(parseItalic(scan.next()));
			} else if (scan.hasNext(BOLD)) {
				h1.addNode(parseBold(scan.next()));
			} else {
				scan.useDelimiter("");
				h1.addNode(new TextNode(scan.next()));
				scan.reset();
			}
		}
		return h1;
	}

	/**
	 * parses text and turns it into an
	 * <h2></h2> node
	 * 
	 * @param scan
	 * @return
	 */
	private AbstractNode parseHeader2(String line) {
		Scanner scan = new Scanner(line);
		if (scan.hasNext()) {
			scan.next();
			scan.useDelimiter("");
			if (scan.hasNext()) {
				scan.next();
			}
			scan.reset();
		}
		HeaderNode2 h2 = new HeaderNode2();
		while (scan.hasNext()) {
			if (scan.hasNext(ITALIC)) {
				h2.addNode(parseItalic(scan.next()));
			} else if (scan.hasNext(BOLD)) {
				h2.addNode(parseBold(scan.next()));
			}
			// add check for starting with bold or italic
			else {
				scan.useDelimiter("");
				h2.addNode(new TextNode(scan.next()));
				scan.reset();

			}
		}
		return h2;
	}

	/**
	 * creates new bold node <strong></strong> and returns it
	 * 
	 * @param str
	 * @return
	 */
	private AbstractNode parseBold(String str) {
		StringBuilder text = new StringBuilder();
		Scanner scan = new Scanner(str);
		scan.useDelimiter("");
		while (scan.hasNext()) {
			if (scan.hasNext("\\*")) {
				scan.next();
			} else {
				text.append(scan.next());
			}
		}
		scan.close();
		Bold_Node bold = new Bold_Node();
		bold.addNode(new TextNode(text.toString()));
		return bold;
	}

	/**
	 * creates new italic node <em></em> and returns it
	 * 
	 * @param str
	 * @return
	 */
	private AbstractNode parseItalic(String str) {
		StringBuilder text = new StringBuilder();
		Scanner scan = new Scanner(str);
		scan.useDelimiter("");
		while (scan.hasNext()) {
			if (scan.hasNext("\\*")) {
				scan.next();
			} else {
				text.append(scan.next());
			}
		}
		scan.close();
		Italic_Node italic = new Italic_Node();
		italic.addNode(new TextNode(text.toString()));
		return italic;
	}

}
