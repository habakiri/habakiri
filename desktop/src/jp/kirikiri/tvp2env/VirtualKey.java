/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * 楓 software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * ソースコード形式かバイナリ形式か、変更するかしないかを問わず、以下の条件を満
 * たす場合に限り、再頒布および使用が許可されます。
 *
 * ・ソースコードを再頒布する場合、上記の著作権表示、本条件一覧、および下記免責
 *   条項を含めること。
 * ・バイナリ形式で再頒布する場合、頒布物に付属のドキュメント等の資料に、上記の
 *   著作権表示、本条件一覧、および下記免責条項を含めること。
 * ・書面による特別の許可なしに、本ソフトウェアから派生した製品の宣伝または販売
 *   促進に、組織の名前またはコントリビューターの名前を使用してはならない。
 *
 * 本ソフトウェアは、著作権者およびコントリビューターによって「現状のまま」提供
 * されており、明示黙示を問わず、商業的な使用可能性、および特定の目的に対する適
 * 合性に関する暗黙の保証も含め、またそれに限定されない、いかなる保証もありませ
 * ん。著作権者もコントリビューターも、事由のいかんを問わず、損害発生の原因いか
 * んを問わず、かつ責任の根拠が契約であるか厳格責任であるか（過失その他の）不法
 * 行為であるかを問わず、仮にそのような損害が発生する可能性を知らされていたとし
 * ても、本ソフトウェアの使用によって発生した（代替品または代用サービスの調達、
 * 使用の喪失、データの喪失、利益の喪失、業務の中断も含め、またそれに限定されな
 * い）直接損害、間接損害、偶発的な損害、特別損害、懲罰的損害、または結果損害に
 * ついて、一切責任を負わないものとします。
 ******************************************************************************
 * 本ソフトウェアは、吉里吉里2 ( http://kikyou.info/tvp/ ) のソースコードをJava
 * に書き換えたものを一部使用しています。
 * 吉里吉里2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
 ******************************************************************************
 */
package jp.kirikiri.tvp2env;

import java.awt.event.KeyEvent;

/**
 * 仮想キーコード
 *
 */
public class VirtualKey {
	static public final int VK_0 = KeyEvent.VK_0;
	static public final int VK_1 = KeyEvent.VK_1;
	static public final int VK_2 = KeyEvent.VK_2;
	static public final int VK_3 = KeyEvent.VK_3;
	static public final int VK_4 = KeyEvent.VK_4;
	static public final int VK_5 = KeyEvent.VK_5;
	static public final int VK_6 = KeyEvent.VK_6;
	static public final int VK_7 = KeyEvent.VK_7;
	static public final int VK_8 = KeyEvent.VK_8;
	static public final int VK_9 = KeyEvent.VK_9;
	static public final int VK_A = KeyEvent.VK_A;
	static public final int VK_ACCEPT = KeyEvent.VK_ACCEPT;
	static public final int VK_ADD = KeyEvent.VK_ADD;
	static public final int VK_AGAIN = KeyEvent.VK_AGAIN;
	static public final int VK_ALL_CANDIDATES = KeyEvent.VK_ALL_CANDIDATES;
	static public final int VK_ALPHANUMERIC = KeyEvent.VK_ALPHANUMERIC;
	static public final int VK_ALT = KeyEvent.VK_ALT;
	static public final int VK_ALT_GRAPH = KeyEvent.VK_ALT_GRAPH;
	static public final int VK_AMPERSAND = KeyEvent.VK_AMPERSAND;
	static public final int VK_ASTERISK = KeyEvent.VK_ASTERISK;
	static public final int VK_AT = KeyEvent.VK_AT;
	static public final int VK_B = KeyEvent.VK_B;
	static public final int VK_BACK_QUOTE = KeyEvent.VK_BACK_QUOTE;
	static public final int VK_BACK_SLASH = KeyEvent.VK_BACK_SLASH;
	static public final int VK_BACK_SPACE = KeyEvent.VK_BACK_SPACE;
	static public final int VK_BEGIN = KeyEvent.VK_BEGIN;
	static public final int VK_BRACELEFT = KeyEvent.VK_BRACELEFT;
	static public final int VK_BRACERIGHT = KeyEvent.VK_BRACERIGHT;
	static public final int VK_C = KeyEvent.VK_C;
	static public final int VK_CANCEL = KeyEvent.VK_CANCEL;
	static public final int VK_CAPS_LOCK = KeyEvent.VK_CAPS_LOCK;
	static public final int VK_CIRCUMFLEX = KeyEvent.VK_CIRCUMFLEX;
	static public final int VK_CLEAR = KeyEvent.VK_CLEAR;
	static public final int VK_CLOSE_BRACKET = KeyEvent.VK_CLOSE_BRACKET;
	static public final int VK_CODE_INPUT = KeyEvent.VK_CODE_INPUT;
	static public final int VK_COLON = KeyEvent.VK_COLON;
	static public final int VK_COMMA = KeyEvent.VK_COMMA;
	static public final int VK_COMPOSE = KeyEvent.VK_COMPOSE;
	static public final int VK_CONTEXT_MENU = KeyEvent.VK_CONTEXT_MENU;
	static public final int VK_CONTROL = KeyEvent.VK_CONTROL;
	static public final int VK_CONVERT = KeyEvent.VK_CONVERT;
	static public final int VK_COPY = KeyEvent.VK_COPY;
	static public final int VK_CUT = KeyEvent.VK_CUT;
	static public final int VK_D = KeyEvent.VK_D;
	static public final int VK_DEAD_ABOVEDOT = KeyEvent.VK_DEAD_ABOVEDOT;
	static public final int VK_DEAD_ABOVERING = KeyEvent.VK_DEAD_ABOVERING;
	static public final int VK_DEAD_ACUTE = KeyEvent.VK_DEAD_ACUTE;
	static public final int VK_DEAD_BREVE = KeyEvent.VK_DEAD_BREVE;
	static public final int VK_DEAD_CARON = KeyEvent.VK_DEAD_CARON;
	static public final int VK_DEAD_CEDILLA = KeyEvent.VK_DEAD_CEDILLA;
	static public final int VK_DEAD_CIRCUMFLEX = KeyEvent.VK_DEAD_CIRCUMFLEX;
	static public final int VK_DEAD_DIAERESIS = KeyEvent.VK_DEAD_DIAERESIS;
	static public final int VK_DEAD_DOUBLEACUTE = KeyEvent.VK_DEAD_DOUBLEACUTE;
	static public final int VK_DEAD_GRAVE = KeyEvent.VK_DEAD_GRAVE;
	static public final int VK_DEAD_IOTA = KeyEvent.VK_DEAD_IOTA;
	static public final int VK_DEAD_MACRON = KeyEvent.VK_DEAD_MACRON;
	static public final int VK_DEAD_OGONEK = KeyEvent.VK_DEAD_OGONEK;
	static public final int VK_DEAD_SEMIVOICED_SOUND = KeyEvent.VK_DEAD_SEMIVOICED_SOUND;
	static public final int VK_DEAD_TILDE = KeyEvent.VK_DEAD_TILDE;
	static public final int VK_DEAD_VOICED_SOUND = KeyEvent.VK_DEAD_VOICED_SOUND;
	static public final int VK_DECIMAL = KeyEvent.VK_DECIMAL;
	static public final int VK_DELETE = KeyEvent.VK_DELETE;
	static public final int VK_DIVIDE = KeyEvent.VK_DIVIDE;
	static public final int VK_DOLLAR = KeyEvent.VK_DOLLAR;
	static public final int VK_DOWN = KeyEvent.VK_DOWN;
	static public final int VK_E = KeyEvent.VK_E;
	static public final int VK_END = KeyEvent.VK_END;
	static public final int VK_ENTER = KeyEvent.VK_ENTER;
	static public final int VK_RETURN = KeyEvent.VK_ENTER;
	static public final int VK_EQUALS = KeyEvent.VK_EQUALS;
	static public final int VK_ESCAPE = KeyEvent.VK_ESCAPE;
	static public final int VK_EURO_SIGN = KeyEvent.VK_EURO_SIGN;
	static public final int VK_EXCLAMATION_MARK = KeyEvent.VK_EXCLAMATION_MARK;
	static public final int VK_F = KeyEvent.VK_F;
	static public final int VK_F1 = KeyEvent.VK_F1;
	static public final int VK_F10 = KeyEvent.VK_F10;
	static public final int VK_F11 = KeyEvent.VK_F11;
	static public final int VK_F12 = KeyEvent.VK_F12;
	static public final int VK_F13 = KeyEvent.VK_F13;
	static public final int VK_F14 = KeyEvent.VK_F14;
	static public final int VK_F15 = KeyEvent.VK_F15;
	static public final int VK_F16 = KeyEvent.VK_F16;
	static public final int VK_F17 = KeyEvent.VK_F17;
	static public final int VK_F18 = KeyEvent.VK_F18;
	static public final int VK_F19 = KeyEvent.VK_F19;
	static public final int VK_F2 = KeyEvent.VK_F2;
	static public final int VK_F20 = KeyEvent.VK_F20;
	static public final int VK_F21 = KeyEvent.VK_F21;
	static public final int VK_F22 = KeyEvent.VK_F22;
	static public final int VK_F23 = KeyEvent.VK_F23;
	static public final int VK_F24 = KeyEvent.VK_F24;
	static public final int VK_F3 = KeyEvent.VK_F3;
	static public final int VK_F4 = KeyEvent.VK_F4;
	static public final int VK_F5 = KeyEvent.VK_F5;
	static public final int VK_F6 = KeyEvent.VK_F6;
	static public final int VK_F7 = KeyEvent.VK_F7;
	static public final int VK_F8 = KeyEvent.VK_F8;
	static public final int VK_F9 = KeyEvent.VK_F9;
	static public final int VK_FINAL = KeyEvent.VK_FINAL;
	static public final int VK_FIND = KeyEvent.VK_FIND;
	static public final int VK_FULL_WIDTH = KeyEvent.VK_FULL_WIDTH;
	static public final int VK_G = KeyEvent.VK_G;
	static public final int VK_GREATER = KeyEvent.VK_GREATER;
	static public final int VK_H = KeyEvent.VK_H;
	static public final int VK_HALF_WIDTH = KeyEvent.VK_HALF_WIDTH;
	static public final int VK_HELP = KeyEvent.VK_HELP;
	static public final int VK_HIRAGANA = KeyEvent.VK_HIRAGANA;
	static public final int VK_HOME = KeyEvent.VK_HOME;
	static public final int VK_I = KeyEvent.VK_I;
	static public final int VK_INPUT_METHOD_ON_OFF = KeyEvent.VK_INPUT_METHOD_ON_OFF;
	static public final int VK_INSERT = KeyEvent.VK_INSERT;
	static public final int VK_INVERTED_EXCLAMATION_MARK = KeyEvent.VK_INVERTED_EXCLAMATION_MARK;
	static public final int VK_J = KeyEvent.VK_J;
	static public final int VK_JAPANESE_HIRAGANA = KeyEvent.VK_JAPANESE_HIRAGANA;
	static public final int VK_JAPANESE_KATAKANA = KeyEvent.VK_JAPANESE_KATAKANA;
	static public final int VK_JAPANESE_ROMAN = KeyEvent.VK_JAPANESE_ROMAN;
	static public final int VK_K = KeyEvent.VK_K;
	static public final int VK_KANA = KeyEvent.VK_KANA;
	static public final int VK_KANA_LOCK = KeyEvent.VK_KANA_LOCK;
	static public final int VK_KANJI = KeyEvent.VK_KANJI;
	static public final int VK_KATAKANA = KeyEvent.VK_KATAKANA;
	static public final int VK_KP_DOWN = KeyEvent.VK_KP_DOWN;
	static public final int VK_KP_LEFT = KeyEvent.VK_KP_LEFT;
	static public final int VK_KP_RIGHT = KeyEvent.VK_KP_RIGHT;
	static public final int VK_KP_UP = KeyEvent.VK_KP_UP;
	static public final int VK_L = KeyEvent.VK_L;
	static public final int VK_LEFT = KeyEvent.VK_LEFT;
	static public final int VK_LEFT_PARENTHESIS = KeyEvent.VK_LEFT_PARENTHESIS;
	static public final int VK_LESS = KeyEvent.VK_LESS;
	static public final int VK_M = KeyEvent.VK_M;
	static public final int VK_META = KeyEvent.VK_META;
	static public final int VK_MINUS = KeyEvent.VK_MINUS;
	static public final int VK_MODECHANGE = KeyEvent.VK_MODECHANGE;
	static public final int VK_MULTIPLY = KeyEvent.VK_MULTIPLY;
	static public final int VK_N = KeyEvent.VK_N;
	static public final int VK_NONCONVERT = KeyEvent.VK_NONCONVERT;
	static public final int VK_NUM_LOCK = KeyEvent.VK_NUM_LOCK;
	static public final int VK_NUMBER_SIGN = KeyEvent.VK_NUMBER_SIGN;
	static public final int VK_NUMPAD0 = KeyEvent.VK_NUMPAD0;
	static public final int VK_NUMPAD1 = KeyEvent.VK_NUMPAD1;
	static public final int VK_NUMPAD2 = KeyEvent.VK_NUMPAD2;
	static public final int VK_NUMPAD3 = KeyEvent.VK_NUMPAD3;
	static public final int VK_NUMPAD4 = KeyEvent.VK_NUMPAD4;
	static public final int VK_NUMPAD5 = KeyEvent.VK_NUMPAD5;
	static public final int VK_NUMPAD6 = KeyEvent.VK_NUMPAD6;
	static public final int VK_NUMPAD7 = KeyEvent.VK_NUMPAD7;
	static public final int VK_NUMPAD8 = KeyEvent.VK_NUMPAD8;
	static public final int VK_NUMPAD9 = KeyEvent.VK_NUMPAD9;
	static public final int VK_O = KeyEvent.VK_O;
	static public final int VK_OPEN_BRACKET = KeyEvent.VK_OPEN_BRACKET;
	static public final int VK_P = KeyEvent.VK_P;
	static public final int VK_PAGE_DOWN = KeyEvent.VK_PAGE_DOWN;
	static public final int VK_PAGE_UP = KeyEvent.VK_PAGE_UP;
	static public final int VK_PASTE = KeyEvent.VK_PASTE;
	static public final int VK_PAUSE = KeyEvent.VK_PAUSE;
	static public final int VK_PERIOD = KeyEvent.VK_PERIOD;
	static public final int VK_PLUS = KeyEvent.VK_PLUS;
	static public final int VK_PREVIOUS_CANDIDATE = KeyEvent.VK_PREVIOUS_CANDIDATE;
	static public final int VK_PRINTSCREEN = KeyEvent.VK_PRINTSCREEN;
	static public final int VK_PROPS = KeyEvent.VK_PROPS;
	static public final int VK_Q = KeyEvent.VK_Q;
	static public final int VK_QUOTE = KeyEvent.VK_QUOTE;
	static public final int VK_QUOTEDBL = KeyEvent.VK_QUOTEDBL;
	static public final int VK_R = KeyEvent.VK_R;
	static public final int VK_RIGHT = KeyEvent.VK_RIGHT;
	static public final int VK_RIGHT_PARENTHESIS = KeyEvent.VK_RIGHT_PARENTHESIS;
	static public final int VK_ROMAN_CHARACTERS = KeyEvent.VK_ROMAN_CHARACTERS;
	static public final int VK_S = KeyEvent.VK_S;
	static public final int VK_SCROLL_LOCK = KeyEvent.VK_SCROLL_LOCK;
	static public final int VK_SEMICOLON = KeyEvent.VK_SEMICOLON;
	static public final int VK_SEPARATER = KeyEvent.VK_SEPARATER;
	static public final int VK_SEPARATOR = KeyEvent.VK_SEPARATOR;
	static public final int VK_SHIFT = KeyEvent.VK_SHIFT;
	static public final int VK_SLASH = KeyEvent.VK_SLASH;
	static public final int VK_SPACE = KeyEvent.VK_SPACE;
	static public final int VK_STOP = KeyEvent.VK_STOP;
	static public final int VK_SUBTRACT = KeyEvent.VK_SUBTRACT;
	static public final int VK_T = KeyEvent.VK_T;
	static public final int VK_TAB = KeyEvent.VK_TAB;
	static public final int VK_U = KeyEvent.VK_U;
	static public final int VK_UNDEFINED = KeyEvent.VK_UNDEFINED;
	static public final int VK_UNDERSCORE = KeyEvent.VK_UNDERSCORE;
	static public final int VK_UNDO = KeyEvent.VK_UNDO;
	static public final int VK_UP = KeyEvent.VK_UP;
	static public final int VK_V = KeyEvent.VK_V;
	static public final int VK_W = KeyEvent.VK_W;
	static public final int VK_WINDOWS = KeyEvent.VK_WINDOWS;
	static public final int VK_X = KeyEvent.VK_X;
	static public final int VK_Y = KeyEvent.VK_Y;
	static public final int VK_Z = KeyEvent.VK_Z;

	public static final int VK_LBUTTON = 0;
	public static final int VK_RBUTTON = 0;
	public static final int VK_MBUTTON = 0;
	public static final int VK_BACK = KeyEvent.VK_BACK_SPACE;
	public static final int VK_MENU = 0;
	public static final int VK_CAPITAL = 0;
	public static final int VK_HANGEUL = 0;
	public static final int VK_HANGUL = 0;
	public static final int VK_JUNJA = 0;
	public static final int VK_HANJA = 0;
	public static final int VK_PRIOR = 0;
	public static final int VK_NEXT = 0;
	public static final int VK_SELECT = 0;
	public static final int VK_PRINT = 0;
	public static final int VK_EXECUTE = 0;
	public static final int VK_SNAPSHOT = 0;
	public static final int VK_LWIN = 0;
	public static final int VK_RWIN = 0;
	public static final int VK_APPS = 0;
	public static final int VK_NUMLOCK = 0;
	public static final int VK_SCROLL = 0;
	public static final int VK_LSHIFT = 0;
	public static final int VK_RSHIFT = 0;
	public static final int VK_LCONTROL = 0;
	public static final int VK_RCONTROL = 0;
	public static final int VK_LMENU = 0;
	public static final int VK_RMENUZ = 0;
	public static final int VK_PADLEFT = 0;
	public static final int VK_PADUP = 0;
	public static final int VK_PADRIGHT = 0;
	public static final int VK_PADDOWN = 0;
	public static final int VK_PAD1 = 0;
	public static final int VK_PAD2 = 0;
	public static final int VK_PAD3 = 0;
	public static final int VK_PAD4 = 0;
	public static final int VK_PAD5 = 0;
	public static final int VK_PAD6 = 0;
	public static final int VK_PAD7 = 0;
	public static final int VK_PAD8 = 0;
	public static final int VK_PAD9 = 0;
	public static final int VK_PAD10 = 0;
	public static final int VK_PADANY = 0;
	public static final int VK_PROCESSKEY = 0;
	public static final int VK_ATTN = 0;
	public static final int VK_CRSEL = 0;
	public static final int VK_EXSEL = 0;
	public static final int VK_EREOF = 0;
	public static final int VK_PLAY = 0;
	public static final int VK_ZOOM = 0;
	public static final int VK_NONAME = 0;
	public static final int VK_PA1 = 0;
	public static final int VK_OEM_CLEAR = 0;

	static public int getKeyCode( String str ) {
		if( str == null || str.length() == 0 ) return -1;

		if( str.length() == 1 ) {
			return getKeyCode( str.charAt(0) );
		} else {
			char c0 = str.charAt(0);
			switch( c0 ) {
			case 'a':
				if( "alt".equalsIgnoreCase(str) ) return VK_ALT;
				else if( "altgraph".equalsIgnoreCase(str) ) return VK_ALT_GRAPH;
				break;
			case 'b':
				if("BackSpace".equalsIgnoreCase(str)) return VK_BACK_SPACE;
				break;
			case 'c':
				if("ctrl".equalsIgnoreCase(str)) return VK_CONTROL;
				break;
			case 'd':
				if("down".equalsIgnoreCase(str)) return VK_DOWN;
				else if("Delete".equalsIgnoreCase(str)) return VK_DELETE;
				break;
			case 'e':
				if("Enter".equalsIgnoreCase(str)) return VK_ENTER;
				else if("Esc".equalsIgnoreCase(str)) return VK_ESCAPE;
				else if("End".equalsIgnoreCase(str)) return VK_END;
				break;
			case 'f': {// F1 ～ F12
				char c1 = str.charAt(1);
				if( str.length() == 2 ) {
					switch( c1 ) {
					case '1': return VK_F1;
					case '2': return VK_F2;
					case '3': return VK_F3;
					case '4': return VK_F4;
					case '5': return VK_F5;
					case '6': return VK_F6;
					case '7': return VK_F7;
					case '8': return VK_F8;
					case '9': return VK_F9;
					}
				} else {
					char c2 = str.charAt(2);
					switch( c2 ) {
					case '0': return VK_F10;
					case '1': return VK_F11;
					case '2': return VK_F12;
					}
				}
			}
			case 'h':
				if("Home".equalsIgnoreCase(str)) return VK_HOME;
				break;
			case 'i':
				if("Insert".equalsIgnoreCase(str)) return VK_INSERT;
				break;
			case 'l':
				if("left".equalsIgnoreCase(str)) return VK_LEFT;
				break;
			case 'm':
				if("meta".equalsIgnoreCase(str)) return VK_META;
				break;
			case 'p':
				if("Pause".equalsIgnoreCase(str)) return VK_PAUSE;
				else if("PageUp".equalsIgnoreCase(str)) return VK_PAGE_UP;
				else if("PageDown".equalsIgnoreCase(str)) return VK_PAGE_DOWN;
				break;
			case 'r':
				if("right".equalsIgnoreCase(str)) return VK_RIGHT;
				break;
			case 's':
				if("shift".equalsIgnoreCase(str)) return VK_SHIFT;
				else if("Space".equalsIgnoreCase(str)) return VK_SPACE;
				else if("ScrollLock".equalsIgnoreCase(str)) return VK_SCROLL_LOCK;
				break;
			case 't':
				if("tab".equalsIgnoreCase(str)) return VK_TAB;
				break;
			case 'u':
				if("up".equalsIgnoreCase(str)) return VK_UP;
				break;
			case 'w':
				if("win".equalsIgnoreCase(str)) return VK_WINDOWS;
				break;
			}
		}
		return -1;
	}
	static public int getKeyCode( char c ) {
		switch( c ) {
		case 'a': return VK_A;
		case 'b': return VK_B;
		case 'c': return VK_C;
		case 'd': return VK_D;
		case 'e': return VK_E;
		case 'f': return VK_F;
		case 'g': return VK_G;
		case 'h': return VK_H;
		case 'i': return VK_I;
		case 'j': return VK_J;
		case 'k': return VK_K;
		case 'l': return VK_L;
		case 'm': return VK_M;
		case 'n': return VK_N;
		case 'o': return VK_O;
		case 'p': return VK_P;
		case 'q': return VK_Q;
		case 'r': return VK_R;
		case 's': return VK_S;
		case 't': return VK_T;
		case 'u': return VK_U;
		case 'v': return VK_V;
		case 'w': return VK_W;
		case 'x': return VK_X;
		case 'y': return VK_Y;
		case 'z': return VK_Z;
		case '0': return VK_0;
		case '1': return VK_1;
		case '2': return VK_2;
		case '3': return VK_3;
		case '4': return VK_4;
		case '5': return VK_5;
		case '6': return VK_6;
		case '7': return VK_7;
		case '8': return VK_8;
		case '9': return VK_9;
		case '-': return VK_MINUS;
		case '^': return VK_CIRCUMFLEX;
		case '\\': return VK_BACK_SLASH;
		case '@': return VK_AT;
		case '[': return VK_OPEN_BRACKET;
		case ';': return VK_SEMICOLON;
		case ':': return VK_COLON;
		case ']': return VK_CLOSE_BRACKET;
		case ',': return VK_COMMA;
		case '.': return VK_PERIOD;
		case '/': return VK_SLASH;
		case '*': return VK_MULTIPLY;
		case '+': return VK_ADD;
		}
		return -1;
	}
}
