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
package jp.kirikiri.tjs2;

class Token {

	// TJS2 token
	public static final int
		T_COMMA = 258,
		T_EQUAL = 259,
		T_AMPERSANDEQUAL = 260,
		T_VERTLINEEQUAL = 261,
		T_CHEVRONEQUAL = 262,
		T_MINUSEQUAL = 263,
		T_PLUSEQUAL = 264,
		T_PERCENTEQUAL = 265,
		T_SLASHEQUAL = 266,
		T_BACKSLASHEQUAL = 267,
		T_ASTERISKEQUAL = 268,
		T_LOGICALOREQUAL = 269,
		T_LOGICALANDEQUAL = 270,
		T_RARITHSHIFTEQUAL = 271,
		T_LARITHSHIFTEQUAL = 272,
		T_RBITSHIFTEQUAL = 273,
		T_QUESTION = 274,
		T_LOGICALOR = 275,
		T_LOGICALAND = 276,
		T_VERTLINE = 277,
		T_CHEVRON = 278,
		T_AMPERSAND = 279,
		T_NOTEQUAL = 280,
		T_EQUALEQUAL = 281,
		T_DISCNOTEQUAL = 282,
		T_DISCEQUAL = 283,
		T_SWAP = 284,
		T_LT = 285,
		T_GT = 286,
		T_LTOREQUAL = 287,
		T_GTOREQUAL = 288,
		T_RARITHSHIFT = 289,
		T_LARITHSHIFT = 290,
		T_RBITSHIFT = 291,
		T_PERCENT = 292,
		T_SLASH = 293,
		T_BACKSLASH = 294,
		T_ASTERISK = 295,
		T_EXCRAMATION = 296,
		T_TILDE = 297,
		T_DECREMENT = 298,
		T_INCREMENT = 299,
		T_NEW = 300,
		T_DELETE = 301,
		T_TYPEOF = 302,
		T_PLUS = 303,
		T_MINUS = 304,
		T_SHARP = 305,
		T_DOLLAR = 306,
		T_ISVALID = 307,
		T_INVALIDATE = 308,
		T_INSTANCEOF = 309,
		T_LPARENTHESIS = 310,
		T_DOT = 311,
		T_LBRACKET = 312,
		T_THIS = 313,
		T_SUPER = 314,
		T_GLOBAL = 315,
		T_RBRACKET = 316,
		T_CLASS = 317,
		T_RPARENTHESIS = 318,
		T_COLON = 319,
		T_SEMICOLON = 320,
		T_LBRACE = 321,
		T_RBRACE = 322,
		T_CONTINUE = 323,
		T_FUNCTION = 324,
		T_DEBUGGER = 325,
		T_DEFAULT = 326,
		T_CASE = 327,
		T_EXTENDS = 328,
		T_FINALLY = 329,
		T_PROPERTY = 330,
		T_PRIVATE = 331,
		T_PUBLIC = 332,
		T_PROTECTED = 333,
		T_STATIC = 334,
		T_RETURN = 335,
		T_BREAK = 336,
		T_EXPORT = 337,
		T_IMPORT = 338,
		T_SWITCH = 339,
		T_IN = 340,
		T_INCONTEXTOF = 341,
		T_FOR = 342,
		T_WHILE = 343,
		T_DO = 344,
		T_IF = 345,
		T_VAR = 346,
		T_CONST = 347,
		T_ENUM = 348,
		T_GOTO = 349,
		T_THROW = 350,
		T_TRY = 351,
		T_SETTER = 352,
		T_GETTER = 353,
		T_ELSE = 354,
		T_CATCH = 355,
		T_OMIT = 356,
		T_SYNCHRONIZED = 357,
		T_WITH = 358,
		T_INT = 359,
		T_REAL = 360,
		T_STRING = 361,
		T_OCTET = 362,
		T_FALSE = 363,
		T_NULL = 364,
		T_TRUE = 365,
		T_VOID = 366,
		T_NAN = 367,
		T_INFINITY = 368,
		T_UPLUS = 369,
		T_UMINUS = 370,
		T_EVAL = 371,
		T_POSTDECREMENT = 372,
		T_POSTINCREMENT = 373,
		T_IGNOREPROP = 374,
		T_PROPACCESS = 375,
		T_ARG = 376,
		T_EXPANDARG = 377,
		T_INLINEARRAY = 378,
		T_ARRAYARG = 379,
		T_INLINEDIC = 380,
		T_DICELM = 381,
		T_WITHDOT = 382,
		T_THIS_PROXY = 383,
		T_WITHDOT_PROXY = 384,
		T_CONSTVAL = 385,
		T_SYMBOL = 386,
		T_REGEXP = 387,

		// 特殊
		T_CAST_INT = 512,	// ( int )
		T_CAST_REAL = 513,	// ( real )
		T_CAST_STRING = 514,// ( string )
		T_CAST_CONST = 515,	// ( const ) キャストじゃないけど、わかりやすく
		T_CAST_EXPR = 516,	// ( expr ) キャストじゃないけど、わかりやすく
		T_ASTERISK_RPARENTHESIS = 517, // *) 関数呼び出しの引数配列展開に対応
		T_ASTERISK_COMMA = 518, // *, 関数呼び出しの引数配列展開に対応

		T_END_OF_VALUE = 10000; // 末尾(ダミー)

	// for pre-processor token
	public static final int
		PT_LPARENTHESIS = 258,
		PT_RPARENTHESIS = 259,
		PT_ERROR = 260,
		PT_COMMA = 261,
		PT_EQUAL = 262,
		PT_NOTEQUAL = 263,
		PT_EQUALEQUAL = 264,
		PT_LOGICALOR = 265,
		PT_LOGICALAND = 266,
		PT_VERTLINE = 267,
		PT_CHEVRON = 268,
		PT_AMPERSAND = 269,
		PT_LT = 270,
		PT_GT = 271,
		PT_LTOREQUAL = 272,
		PT_GTOREQUAL = 273,
		PT_PLUS = 274,
		PT_MINUS = 275,
		PT_ASTERISK = 276,
		PT_SLASH = 277,
		PT_PERCENT = 278,
		PT_EXCLAMATION = 279,
		PT_UN = 280,
		PT_SYMBOL = 281,
		PT_NUM = 282;

	public static final String getTokenString( int token ) {
		switch(token){
		case T_COMMA: return ",";
		case T_EQUAL: return "=";
		case T_AMPERSANDEQUAL: return "&=";
		case T_VERTLINEEQUAL: return "|=";
		case T_CHEVRONEQUAL: return "^=";
		case T_MINUSEQUAL: return "-=";
		case T_PLUSEQUAL: return "+=";
		case T_PERCENTEQUAL: return "%=";
		case T_SLASHEQUAL: return "/=";
		case T_BACKSLASHEQUAL: return "\\=";
		case T_ASTERISKEQUAL: return "*=";
		case T_LOGICALOREQUAL: return "||=";
		case T_LOGICALANDEQUAL: return "&&=";
		case T_RARITHSHIFTEQUAL: return ">>=";
		case T_LARITHSHIFTEQUAL: return "<<=";
		case T_RBITSHIFTEQUAL: return ">>>=";
		case T_QUESTION: return "?";
		case T_LOGICALOR: return "||";
		case T_LOGICALAND: return "&&";
		case T_VERTLINE: return "|";
		case T_CHEVRON: return "^";
		case T_AMPERSAND: return "&";
		case T_NOTEQUAL: return "!=";
		case T_EQUALEQUAL: return "==";
		case T_DISCNOTEQUAL: return "!==";
		case T_DISCEQUAL: return "==";
		case T_SWAP: return "<->";
		case T_LT: return "<";
		case T_GT: return ">";
		case T_LTOREQUAL: return "<=";
		case T_GTOREQUAL: return ">=";
		case T_RARITHSHIFT: return ">>";
		case T_LARITHSHIFT: return "<<";
		case T_RBITSHIFT: return ">>>";
		case T_PERCENT: return "%";
		case T_SLASH: return "/";
		case T_BACKSLASH: return "\\";
		case T_ASTERISK: return "*";
		case T_EXCRAMATION: return "!";
		case T_TILDE: return "~";
		case T_DECREMENT: return "--";
		case T_INCREMENT: return "++";
		case T_NEW: return "new";
		case T_DELETE: return "delete";
		case T_TYPEOF: return "typeof";
		case T_PLUS: return "+";
		case T_MINUS: return "-";
		case T_SHARP: return "#";
		case T_DOLLAR: return "$";
		case T_ISVALID: return "isvalid";
		case T_INVALIDATE: return "invalidate";
		case T_INSTANCEOF: return "instanceof";
		case T_LPARENTHESIS: return "(";
		case T_DOT: return ".";
		case T_LBRACKET: return "[";
		case T_THIS: return "this";
		case T_SUPER: return "super";
		case T_GLOBAL: return "global";
		case T_RBRACKET: return "]";
		case T_CLASS: return "class";
		case T_RPARENTHESIS: return ")";
		case T_COLON: return ":";
		case T_SEMICOLON: return ";";
		case T_LBRACE: return "{";
		case T_RBRACE: return "}";
		case T_CONTINUE: return "continue";
		case T_FUNCTION: return "function";
		case T_DEBUGGER: return "debugger";
		case T_DEFAULT: return "default";
		case T_CASE: return "case";
		case T_EXTENDS: return "extends";
		case T_FINALLY: return "finally";
		case T_PROPERTY: return "property";
		case T_PRIVATE: return "private";
		case T_PUBLIC: return "public";
		case T_PROTECTED: return "protected";
		case T_STATIC: return "static";
		case T_RETURN: return "return";
		case T_BREAK: return "break";
		case T_EXPORT: return "export";
		case T_IMPORT: return "import";
		case T_SWITCH: return "switch";
		case T_IN: return "in";
		case T_INCONTEXTOF: return "incontextof";
		case T_FOR: return "for";
		case T_WHILE: return "while";
		case T_DO: return "do";
		case T_IF: return "if";
		case T_VAR: return "var";
		case T_CONST: return "const";
		case T_ENUM: return "enum";
		case T_GOTO: return "goto";
		case T_THROW: return "throw";
		case T_TRY: return "try";
		case T_SETTER: return "setter";
		case T_GETTER: return "getter";
		case T_ELSE: return "else";
		case T_CATCH: return "catch";
		case T_OMIT: return "...";
		case T_SYNCHRONIZED: return "synchronized";
		case T_WITH: return "with";
		case T_INT: return "int";
		case T_REAL: return "real";
		case T_STRING: return "string";
		case T_OCTET: return "octet";
		case T_FALSE: return "false";
		case T_NULL: return "null";
		case T_TRUE: return "true";
		case T_VOID: return "void";
		case T_NAN: return "NaN";
		case T_INFINITY: return "Infinity";
		case T_UPLUS: return "uplus";
		case T_UMINUS: return "uminus";
		case T_EVAL: return "eval";
		case T_POSTDECREMENT: return "--(post)";
		case T_POSTINCREMENT: return "++(post)";
		case T_IGNOREPROP: return "ignoreprop";
		case T_PROPACCESS: return "propaccess";
		case T_ARG: return "arg";
		case T_EXPANDARG: return "expandarg";
		case T_INLINEARRAY: return "inlinearray";
		case T_ARRAYARG: return "arrayarg";
		case T_INLINEDIC: return "inlinedic";
		case T_DICELM: return "dicelm";
		case T_WITHDOT: return "withdot";
		case T_THIS_PROXY: return "this_proxy";
		case T_WITHDOT_PROXY: return "withdot_proxy";
		case T_CONSTVAL: return "constval";
		case T_SYMBOL: return "symbol";
		case T_REGEXP: return "regexp";

		// 特殊
		case T_CAST_INT: return "(int)";	// ( int )
		case T_CAST_REAL: return "(real)";	// ( real )
		case T_CAST_STRING: return "(string)";// ( string )
		case T_CAST_CONST: return "(const)";	// ( const ) キャストじゃないけど、わかりやすく
		case T_CAST_EXPR: return "(expr)";	// ( expr ) キャストじゃないけど、わかりやすく
		case T_ASTERISK_RPARENTHESIS: return "*)"; // *) 関数呼び出しの引数配列展開に対応
		case T_ASTERISK_COMMA: return "*,"; // *, 関数呼び出しの引数配列展開に対応

		case T_END_OF_VALUE: return "END_OF_VALUE"; // 末尾(ダミー)
		case 0: return "EOF";
		}
		return "unknown";
	}
}

