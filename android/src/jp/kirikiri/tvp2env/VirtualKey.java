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

import android.view.KeyEvent;

/**
 * 仮想キーコード
 *
 */
public class VirtualKey {
	static public final int VK_0 = KeyEvent.KEYCODE_0;
	static public final int VK_1 = KeyEvent.KEYCODE_1;
	static public final int VK_2 = KeyEvent.KEYCODE_2;
	static public final int VK_3 = KeyEvent.KEYCODE_3;
	static public final int VK_4 = KeyEvent.KEYCODE_4;
	static public final int VK_5 = KeyEvent.KEYCODE_5;
	static public final int VK_6 = KeyEvent.KEYCODE_6;
	static public final int VK_7 = KeyEvent.KEYCODE_7;
	static public final int VK_8 = KeyEvent.KEYCODE_8;
	static public final int VK_9 = KeyEvent.KEYCODE_9;
	static public final int VK_A = KeyEvent.KEYCODE_A;
	//static public final int VK_ACCEPT = KeyEvent.KEYCODE_ACCEPT;
	static public final int VK_ADD = KeyEvent.KEYCODE_PLUS;
	//static public final int VK_AGAIN = KeyEvent.KEYCODE_AGAIN;
	//static public final int VK_ALL_CANDIDATES = KeyEvent.KEYCODE_ALL_CANDIDATES;
	//static public final int VK_ALPHANUMERIC = KeyEvent.KEYCODE_ALPHANUMERIC;
	static public final int VK_ALT = KeyEvent.KEYCODE_ALT_LEFT;
	static public final int VK_ALT_LEFT = KeyEvent.KEYCODE_ALT_LEFT; /* add */
	static public final int VK_ALT_RIGHT = KeyEvent.KEYCODE_ALT_RIGHT; /* add */
	//static public final int VK_ALT_GRAPH = KeyEvent.KEYCODE_ALT_GRAPH;
	//static public final int VK_AMPERSAND = KeyEvent.KEYCODE_AMPERSAND;
	static public final int VK_ASTERISK = KeyEvent.KEYCODE_STAR;
	static public final int VK_AT = KeyEvent.KEYCODE_AT;
	static public final int VK_B = KeyEvent.KEYCODE_B;
	static public final int VK_BACK = KeyEvent.KEYCODE_BACK; /* add */
	static public final int VK_BACK_QUOTE = KeyEvent.KEYCODE_GRAVE;
	static public final int VK_BACK_SLASH = KeyEvent.KEYCODE_BACKSLASH;
	static public final int VK_BACK_SPACE = KeyEvent.KEYCODE_BACK;
	//static public final int VK_BEGIN = KeyEvent.KEYCODE_BEGIN;
	//static public final int VK_BRACELEFT = KeyEvent.KEYCODE_LEFT_BRACKET; {
	//static public final int VK_BRACERIGHT = KeyEvent.KEYCODE_RIGHT_BRACKET; }
	static public final int VK_C = KeyEvent.KEYCODE_C;
	static public final int VK_CANCEL = KeyEvent.KEYCODE_BACK;
	static public final int VK_CAPS_LOCK = 115; // KeyEvent.KEYCODE_CAPS_LOCK; API 11
	//static public final int VK_CIRCUMFLEX = KeyEvent.KEYCODE_CIRCUMFLEX;
	static public final int VK_CENTER = KeyEvent.KEYCODE_DPAD_CENTER; /* add */
	static public final int VK_CLEAR = KeyEvent.KEYCODE_CLEAR;
	static public final int VK_CLOSE_BRACKET = KeyEvent.KEYCODE_RIGHT_BRACKET;
	//static public final int VK_CODE_INPUT = KeyEvent.KEYCODE_CODE_INPUT;
	//static public final int VK_COLON = KeyEvent.KEYCODE_COLON;
	static public final int VK_COMMA = KeyEvent.KEYCODE_COMMA;
	//static public final int VK_COMPOSE = KeyEvent.KEYCODE_COMPOSE;
	static public final int VK_CONTEXT_MENU = KeyEvent.KEYCODE_MENU;
	static public final int VK_CONTROL = 113; // KeyEvent.KEYCODE_CTRL_LEFT; API 11
	static public final int VK_CONTROL_LEFT = 113; // KeyEvent.KEYCODE_CTRL_LEFT; API 11 /* add */
	static public final int VK_CONTROL_RIGHT = 114; // KeyEvent.KEYCODE_CTRL_RIGHT; API 11 /* add */
	//static public final int VK_CONVERT = KeyEvent.KEYCODE_CONVERT;
	//static public final int VK_COPY = KeyEvent.KEYCODE_COPY;
	//static public final int VK_CUT = KeyEvent.KEYCODE_CUT;
	static public final int VK_D = KeyEvent.KEYCODE_D;
	//static public final int VK_DEAD_ABOVEDOT = KeyEvent.KEYCODE_DEAD_ABOVEDOT;
	//static public final int VK_DEAD_ABOVERING = KeyEvent.KEYCODE_DEAD_ABOVERING;
	//static public final int VK_DEAD_ACUTE = KeyEvent.KEYCODE_DEAD_ACUTE;
	//static public final int VK_DEAD_BREVE = KeyEvent.KEYCODE_DEAD_BREVE;
	//static public final int VK_DEAD_CARON = KeyEvent.KEYCODE_DEAD_CARON;
	//static public final int VK_DEAD_CEDILLA = KeyEvent.KEYCODE_DEAD_CEDILLA;
	//static public final int VK_DEAD_CIRCUMFLEX = KeyEvent.KEYCODE_DEAD_CIRCUMFLEX;
	//static public final int VK_DEAD_DIAERESIS = KeyEvent.KEYCODE_DEAD_DIAERESIS;
	//static public final int VK_DEAD_DOUBLEACUTE = KeyEvent.KEYCODE_DEAD_DOUBLEACUTE;
	//static public final int VK_DEAD_GRAVE = KeyEvent.KEYCODE_DEAD_GRAVE;
	//static public final int VK_DEAD_IOTA = KeyEvent.KEYCODE_DEAD_IOTA;
	//static public final int VK_DEAD_MACRON = KeyEvent.KEYCODE_DEAD_MACRON;
	//static public final int VK_DEAD_OGONEK = KeyEvent.KEYCODE_DEAD_OGONEK;
	//static public final int VK_DEAD_SEMIVOICED_SOUND = KeyEvent.KEYCODE_DEAD_SEMIVOICED_SOUND;
	//static public final int VK_DEAD_TILDE = KeyEvent.KEYCODE_DEAD_TILDE;
	//static public final int VK_DEAD_VOICED_SOUND = KeyEvent.KEYCODE_DEAD_VOICED_SOUND;
	//static public final int VK_DECIMAL = KeyEvent.KEYCODE_DECIMAL;
	static public final int VK_DELETE = KeyEvent.KEYCODE_DEL;
	static public final int VK_DIVIDE = KeyEvent.KEYCODE_SLASH;
	//static public final int VK_DOLLAR = KeyEvent.KEYCODE_DOLLAR;
	static public final int VK_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
	static public final int VK_E = KeyEvent.KEYCODE_E;
	static public final int VK_END = KeyEvent.KEYCODE_ENDCALL;
	static public final int VK_ENTER = KeyEvent.KEYCODE_ENTER;
	static public final int VK_RETURN = KeyEvent.KEYCODE_ENTER;
	static public final int VK_EQUALS = KeyEvent.KEYCODE_EQUALS;
	static public final int VK_ESCAPE = 111; // KeyEvent.KEYCODE_ESCAPE; API 11
	//static public final int VK_EURO_SIGN = KeyEvent.KEYCODE_EURO_SIGN;
	//static public final int VK_EXCLAMATION_MARK = KeyEvent.KEYCODE_EXCLAMATION_MARK;
	static public final int VK_F = KeyEvent.KEYCODE_F;
	static public final int VK_F1 = 131; // KeyEvent.KEYCODE_F1; API 11
	static public final int VK_F10 = 140; // KeyEvent.KEYCODE_F10; API 11
	static public final int VK_F11 = 141; // KeyEvent.KEYCODE_F11; API 11
	static public final int VK_F12 = 142; // KeyEvent.KEYCODE_F12; API 11
	//static public final int VK_F13 = KeyEvent.KEYCODE_F13;
	//static public final int VK_F14 = KeyEvent.KEYCODE_F14;
	//static public final int VK_F15 = KeyEvent.KEYCODE_F15;
	//static public final int VK_F16 = KeyEvent.KEYCODE_F16;
	//static public final int VK_F17 = KeyEvent.KEYCODE_F17;
	//static public final int VK_F18 = KeyEvent.KEYCODE_F18;
	//static public final int VK_F19 = KeyEvent.KEYCODE_F19;
	static public final int VK_F2 = 132; // KeyEvent.KEYCODE_F2; API 11
	//static public final int VK_F20 = KeyEvent.KEYCODE_F20;
	//static public final int VK_F21 = KeyEvent.KEYCODE_F21;
	//static public final int VK_F22 = KeyEvent.KEYCODE_F22;
	//static public final int VK_F23 = KeyEvent.KEYCODE_F23;
	//static public final int VK_F24 = KeyEvent.KEYCODE_F24;
	static public final int VK_F3 = 133; // KeyEvent.KEYCODE_F3; API 11
	static public final int VK_F4 = 134; // KeyEvent.KEYCODE_F4; API 11
	static public final int VK_F5 = 135; // KeyEvent.KEYCODE_F5; API 11
	static public final int VK_F6 = 136; // KeyEvent.KEYCODE_F6; API 11
	static public final int VK_F7 = 137; // KeyEvent.KEYCODE_F7; API 11
	static public final int VK_F8 = 138; // KeyEvent.KEYCODE_F8; API 11
	static public final int VK_F9 = 139; // KeyEvent.KEYCODE_F9; API 11
	//static public final int VK_FINAL = KeyEvent.KEYCODE_FINAL;
	//static public final int VK_FIND = KeyEvent.KEYCODE_FIND;
	//static public final int VK_FULL_WIDTH = KeyEvent.KEYCODE_FULL_WIDTH;
	static public final int VK_G = KeyEvent.KEYCODE_G;
	//static public final int VK_GREATER = KeyEvent.KEYCODE_GREATER;
	static public final int VK_H = KeyEvent.KEYCODE_H;
	//static public final int VK_HALF_WIDTH = KeyEvent.KEYCODE_HALF_WIDTH;
	//static public final int VK_HELP = KeyEvent.KEYCODE_HELP;
	//static public final int VK_HIRAGANA = KeyEvent.KEYCODE_HIRAGANA;
	static public final int VK_HOME = KeyEvent.KEYCODE_HOME;
	static public final int VK_I = KeyEvent.KEYCODE_I;
	static public final int VK_INPUT_METHOD_ON_OFF = 95; // KeyEvent.KEYCODE_SWITCH_CHARSET; API 9
	static public final int VK_INSERT = 124; // KeyEvent.KEYCODE_INSERT; API 11
	//static public final int VK_INVERTED_EXCLAMATION_MARK = KeyEvent.KEYCODE_INVERTED_EXCLAMATION_MARK;
	static public final int VK_J = KeyEvent.KEYCODE_J;
	//static public final int VK_JAPANESE_HIRAGANA = KeyEvent.KEYCODE_JAPANESE_HIRAGANA;
	//static public final int VK_JAPANESE_KATAKANA = KeyEvent.KEYCODE_JAPANESE_KATAKANA;
	//static public final int VK_JAPANESE_ROMAN = KeyEvent.KEYCODE_JAPANESE_ROMAN;
	static public final int VK_K = KeyEvent.KEYCODE_K;
	//static public final int VK_KANA = KeyEvent.KEYCODE_KANA;
	//static public final int VK_KANA_LOCK = KeyEvent.KEYCODE_KANA_LOCK;
	//static public final int VK_KANJI = KeyEvent.KEYCODE_KANJI;
	//static public final int VK_KATAKANA = KeyEvent.KEYCODE_KATAKANA;
	static public final int VK_KP_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
	static public final int VK_KP_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
	static public final int VK_KP_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
	static public final int VK_KP_UP = KeyEvent.KEYCODE_DPAD_UP;
	static public final int VK_L = KeyEvent.KEYCODE_L;
	static public final int VK_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
	static public final int VK_LEFT_PARENTHESIS = 162; // KeyEvent.KEYCODE_NUMPAD_LEFT_PAREN; API 11
	//static public final int VK_LESS = KeyEvent.KEYCODE_LESS;
	static public final int VK_M = KeyEvent.KEYCODE_M;
	static public final int VK_MENU = KeyEvent.KEYCODE_MENU; /* add */
	static public final int VK_META = 117; // KeyEvent.KEYCODE_META_LEFT; /* add */ API 11
	static public final int VK_META_LEFT = 117; // KeyEvent.KEYCODE_META_LEFT; /* add */ API 11
	static public final int VK_META_RIGHT = 118; // KeyEvent.KEYCODE_META_RIGHT; /* add */ API 11
	static public final int VK_MINUS = KeyEvent.KEYCODE_MINUS;
	//static public final int VK_MODECHANGE = KeyEvent.KEYCODE_MODECHANGE;
	static public final int VK_MULTIPLY = KeyEvent.KEYCODE_STAR;
	static public final int VK_N = KeyEvent.KEYCODE_N;
	//static public final int VK_NONCONVERT = KeyEvent.KEYCODE_NONCONVERT;
	static public final int VK_NUM_LOCK = 156; // KeyEvent.KEYCODE_NUM_LOCK; API 11
	static public final int VK_NUMBER_SIGN = 143; // KeyEvent.KEYCODE_NUMBER_SUBTRACT;
	static public final int VK_NUMPAD0 = 144; // KeyEvent.KEYCODE_NUMPAD_0; API 11
	static public final int VK_NUMPAD1 = 145; // KeyEvent.KEYCODE_NUMPAD_1; API 11
	static public final int VK_NUMPAD2 = 146; // KeyEvent.KEYCODE_NUMPAD_2; API 11
	static public final int VK_NUMPAD3 = 147; // KeyEvent.KEYCODE_NUMPAD_3; API 11
	static public final int VK_NUMPAD4 = 148; // KeyEvent.KEYCODE_NUMPAD_4; API 11
	static public final int VK_NUMPAD5 = 149; // KeyEvent.KEYCODE_NUMPAD_5; API 11
	static public final int VK_NUMPAD6 = 150; // KeyEvent.KEYCODE_NUMPAD_6; API 11
	static public final int VK_NUMPAD7 = 151; // KeyEvent.KEYCODE_NUMPAD_7; API 11
	static public final int VK_NUMPAD8 = 152; // KeyEvent.KEYCODE_NUMPAD_8; API 11
	static public final int VK_NUMPAD9 = 153; // KeyEvent.KEYCODE_NUMPAD_9; API 11
	static public final int VK_O = KeyEvent.KEYCODE_O;
	static public final int VK_OPEN_BRACKET = KeyEvent.KEYCODE_LEFT_BRACKET;
	static public final int VK_P = KeyEvent.KEYCODE_P;
	static public final int VK_PAGE_DOWN = 93; // KeyEvent.KEYCODE_PAGE_DOWN; API 9
	static public final int VK_PAGE_UP = 92; // KeyEvent.KEYCODE_PAGE_UP; API 9
	//static public final int VK_PASTE = KeyEvent.KEYCODE_PASTE;
	static public final int VK_PAUSE = 121; // KeyEvent.KEYCODE_BREAK; API 11
	static public final int VK_PERIOD = KeyEvent.KEYCODE_PERIOD;
	static public final int VK_PLUS = KeyEvent.KEYCODE_PLUS;
	//static public final int VK_PREVIOUS_CANDIDATE = KeyEvent.KEYCODE_PREVIOUS_CANDIDATE;
	static public final int VK_PRINTSCREEN = 120; // KeyEvent.KEYCODE_SYSRQ; API 11
	//static public final int VK_PROPS = KeyEvent.KEYCODE_PROPS;
	static public final int VK_Q = KeyEvent.KEYCODE_Q;
	static public final int VK_QUOTE = KeyEvent.KEYCODE_APOSTROPHE;
	//static public final int VK_QUOTEDBL = KeyEvent.KEYCODE_QUOTEDBL;
	static public final int VK_R = KeyEvent.KEYCODE_R;
	static public final int VK_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
	static public final int VK_RIGHT_PARENTHESIS = 163; // KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN; API 11
	//static public final int VK_ROMAN_CHARACTERS = KeyEvent.KEYCODE_ROMAN_CHARACTERS;
	static public final int VK_S = KeyEvent.KEYCODE_S;
	static public final int VK_SCROLL_LOCK = 116; // KeyEvent.KEYCODE_SCROLL_LOCK; API 11
	static public final int VK_SEMICOLON = KeyEvent.KEYCODE_SEMICOLON;
	//static public final int VK_SEPARATER = KeyEvent.KEYCODE_SEPARATER;
	//static public final int VK_SEPARATOR = KeyEvent.KEYCODE_SEPARATOR;
	static public final int VK_SHIFT = KeyEvent.KEYCODE_SHIFT_LEFT;
	static public final int VK_SHIFT_LEFT = KeyEvent.KEYCODE_SHIFT_LEFT; /* add */
	static public final int VK_SHIFT_RIGHT = KeyEvent.KEYCODE_SHIFT_RIGHT; /* add */
	static public final int VK_SLASH = KeyEvent.KEYCODE_SLASH;
	static public final int VK_SPACE = KeyEvent.KEYCODE_SPACE;
	//static public final int VK_STOP = KeyEvent.KEYCODE_STOP;
	static public final int VK_SUBTRACT = KeyEvent.KEYCODE_MINUS;
	static public final int VK_T = KeyEvent.KEYCODE_T;
	static public final int VK_TAB = KeyEvent.KEYCODE_TAB;
	static public final int VK_U = KeyEvent.KEYCODE_U;
	//static public final int VK_UNDEFINED = KeyEvent.KEYCODE_UNDEFINED;
	//static public final int VK_UNDERSCORE = KeyEvent.KEYCODE_UNDERSCORE;
	//static public final int VK_UNDO = KeyEvent.KEYCODE_UNDO;
	static public final int VK_UP = KeyEvent.KEYCODE_DPAD_UP;
	static public final int VK_V = KeyEvent.KEYCODE_V;
	static public final int VK_W = KeyEvent.KEYCODE_W;
	static public final int VK_WINDOWS = 171; // KeyEvent.KEYCODE_WINDOW; API 11
	static public final int VK_X = KeyEvent.KEYCODE_X;
	static public final int VK_Y = KeyEvent.KEYCODE_Y;
	static public final int VK_Z = KeyEvent.KEYCODE_Z;

	// add android
	static public final int VK_VOLUME_UP = KeyEvent.KEYCODE_VOLUME_UP;
	static public final int VK_VOLUME_DOWN = KeyEvent.KEYCODE_VOLUME_DOWN;
	static public final int VK_PADLEFT = KeyEvent.KEYCODE_DPAD_LEFT;
	static public final int VK_PADRIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
	static public final int VK_PADUP = KeyEvent.KEYCODE_DPAD_UP;
	static public final int VK_PADDOWN = KeyEvent.KEYCODE_DPAD_DOWN;
	static public final int VK_PAD1 = 188;//KeyEvent.KEYCODE_BUTTON_1; API 12
	static public final int VK_PAD2 = 189;//KeyEvent.KEYCODE_BUTTON_2; API 12
	static public final int VK_PAD3 = 190;//KeyEvent.KEYCODE_BUTTON_3; API 12
	static public final int VK_PAD4 = 191;//KeyEvent.KEYCODE_BUTTON_4; API 12
	static public final int VK_PAD5 = 192;//KeyEvent.KEYCODE_BUTTON_5; API 12
	static public final int VK_PAD6 = 193;//KeyEvent.KEYCODE_BUTTON_6; API 12
	static public final int VK_PAD7 = 194;//KeyEvent.KEYCODE_BUTTON_7; API 12
	static public final int VK_PAD8 = 195;//KeyEvent.KEYCODE_BUTTON_8; API 12
	static public final int VK_PAD9 = 196;//KeyEvent.KEYCODE_BUTTON_9; API 12
	static public final int VK_PAD10 = 197;//KeyEvent.KEYCODE_BUTTON_10; API 12
	static public final int VK_PAD11 = 198;//KeyEvent.KEYCODE_BUTTON_11; API 12
	static public final int VK_PAD12 = 199;//KeyEvent.KEYCODE_BUTTON_12; API 12
	static public final int VK_PAD13 = 200;//KeyEvent.KEYCODE_BUTTON_13; API 12
	static public final int VK_PAD14 = 201;//KeyEvent.KEYCODE_BUTTON_14; API 12
	static public final int VK_PAD15 = 202;//KeyEvent.KEYCODE_BUTTON_15; API 12
	static public final int VK_PAD16 = 203;//KeyEvent.KEYCODE_BUTTON_16; API 12
	static public final int VK_PADA = 96; // KeyEvent.KEYCODE_BUTTON_A; API 9
	static public final int VK_PADB = 97; // KeyEvent.KEYCODE_BUTTON_A; API 9
	static public final int VK_PADC = 98; // KeyEvent.KEYCODE_BUTTON_A; API 9
	static public final int VK_PADX = 99; // KeyEvent.KEYCODE_BUTTON_A; API 9
	static public final int VK_PADY = 100; // KeyEvent.KEYCODE_BUTTON_A; API 9
	static public final int VK_PADZ = 101; // KeyEvent.KEYCODE_BUTTON_A; API 9
	static public final int VK_PADL1 = 102; // KeyEvent.KEYCODE_BUTTON_L1; API 9
	static public final int VK_PADL2 = 104; // KeyEvent.KEYCODE_BUTTON_L2; API 9
	static public final int VK_PADR1 = 103; // KeyEvent.KEYCODE_BUTTON_R1; API 9
	static public final int VK_PADR2 = 105; // KeyEvent.KEYCODE_BUTTON_R2; API 9
	static public final int VK_PADMODE = 110; // KeyEvent.KEYCODE_BUTTON_MODE; API 9
	static public final int VK_PADSELECT = 109; // KeyEvent.KEYCODE_BUTTON_SELECT; API 9
	static public final int VK_PADSTART = 108; // KeyEvent.KEYCODE_BUTTON_START; API 9
	static public final int VK_PADTHUMBL = 106; // KeyEvent.KEYCODE_BUTTON_THUMBL; API 9
	static public final int VK_PADTHUMBR = 107; // KeyEvent.KEYCODE_BUTTON_THUMBR; API 9

	// 吉里吉里
	public static final int VK_LBUTTON = 0;
	public static final int VK_RBUTTON = 0;
	public static final int VK_MBUTTON = 0;
	public static final int VK_CAPITAL = 0;
	public static final int VK_KANA = 0;
	public static final int VK_HANGEUL = 0;
	public static final int VK_HANGUL = 0;
	public static final int VK_JUNJA = 0;
	public static final int VK_FINAL = 0;
	public static final int VK_HANJA = 0;
	public static final int VK_KANJI = 0;
	public static final int VK_CONVERT = 0;
	public static final int VK_NONCONVERT = 0;
	public static final int VK_ACCEPT = 0;
	public static final int VK_MODECHANGE = 0;
	public static final int VK_PRIOR = 0;
	public static final int VK_NEXT = 0;
	public static final int VK_SELECT = 0;
	public static final int VK_PRINT = 0;
	public static final int VK_EXECUTE = 0;
	public static final int VK_SNAPSHOT = 0;
	public static final int VK_HELP = 0;
	public static final int VK_LWIN = 0;
	public static final int VK_RWIN = 0;
	public static final int VK_APPS = 0;
	public static final int VK_SEPARATOR = 0;
	public static final int VK_DECIMAL = 0;
	public static final int VK_F13 = 0;
	public static final int VK_F14 = 0;
	public static final int VK_F15 = 0;
	public static final int VK_F16 = 0;
	public static final int VK_F17 = 0;
	public static final int VK_F18 = 0;
	public static final int VK_F19 = 0;
	public static final int VK_F20 = 0;
	public static final int VK_F21 = 0;
	public static final int VK_F22 = 0;
	public static final int VK_F23 = 0;
	public static final int VK_F24 = 0;
	public static final int VK_NUMLOCK = 0;
	public static final int VK_SCROLL = 0;
	public static final int VK_LSHIFT = VK_SHIFT_LEFT;
	public static final int VK_RSHIFT = VK_SHIFT_RIGHT;
	public static final int VK_LCONTROL = VK_CONTROL_LEFT;
	public static final int VK_RCONTROL = VK_CONTROL_RIGHT;
	public static final int VK_LMENU = VK_MENU;
	public static final int VK_RMENUZ = VK_MENU;
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
				//else if( "altgraph".equalsIgnoreCase(str) ) return VK_ALT_GRAPH;
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
		//case '^': return VK_CIRCUMFLEX;
		case '\\': return VK_BACK_SLASH;
		case '@': return VK_AT;
		case '[': return VK_OPEN_BRACKET;
		case ';': return VK_SEMICOLON;
		//case ':': return VK_COLON;
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
