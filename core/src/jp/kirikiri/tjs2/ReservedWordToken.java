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

public class ReservedWordToken {
	// 最適化のためにコピー
	private static final int
		T_NEW = 300,
		T_DELETE = 301,
		T_TYPEOF = 302,
		T_ISVALID = 307,
		T_INVALIDATE = 308,
		T_INSTANCEOF = 309,
		T_THIS = 313,
		T_SUPER = 314,
		T_GLOBAL = 315,
		T_CLASS = 317,
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
		T_INFINITY = 368;

	public static int getToken( final String str ) {
		final int len = str.length();
		int id = -1;
		char ch;
		L0: switch( len ) {
		case 2:
			ch = str.charAt(1);
			if( ch == 'o' && str.charAt(0) == 'd' ) id = T_DO; // "do";
			else if( ch == 'n' && str.charAt(0) == 'i' ) id = T_IN; // "in";
			else if( ch == 'f' && str.charAt(0) == 'i' ) id = T_IF; // "if";
			break L0;
		case 3:
			switch( str.charAt(0) ) {
			case 'f':
				if( str.charAt(2) == 'r' && str.charAt(1) == 'o' ) id = T_FOR; // for
				break L0;
			case 'i':
				if( str.charAt(2) == 't' && str.charAt(1) == 'n' ) id = T_INT; // int
				break L0;
			case 'n':
				if( str.charAt(2) == 'w' && str.charAt(1) == 'e' ) id = T_NEW; // new
				break L0;
			case 't':
				if( str.charAt(2) == 'y' && str.charAt(1) == 'r' ) id = T_TRY; // try
				break L0;
			case 'v':
				if( str.charAt(2) == 'r' && str.charAt(1) == 'a' ) id = T_VAR; // var
				break L0;
			case 'N':
				if( str.charAt(2) == 'N' && str.charAt(1) == 'a' ) id = T_NAN; // NaN
				break L0;
			}
			break L0;
		case 4:
			switch( str.charAt(0) ) {
			case 'c':
				if( "case".equals(str) ) id = T_CASE;
				break L0;
			case 'e':
				ch = str.charAt(1);
				if( ch == 'n' && str.charAt(3) == 'm' && str.charAt(2) == 'u' ) id = T_ENUM; // enum
				else if( ch == 'l' && str.charAt(3) == 'e' && str.charAt(2) == 's' ) id = T_ELSE; // else
				break L0;
			case 'g':
				if( "goto".equals(str) ) id = T_GOTO;
				break L0;
			case 'n':
				if( "null".equals(str) ) id = T_NULL;
				break L0;
			case 'r':
				if( "real".equals(str) ) id = T_REAL;
				break L0;
			case 't':
				ch = str.charAt(1);
				if( ch == 'h' && str.charAt(3) == 's' && str.charAt(2) == 'i' ) id = T_THIS; // this
				else if( ch == 'r' && str.charAt(3) == 'e' && str.charAt(2) == 'u') id = T_TRUE; // true
				break L0;
			case 'v':
				if( "void".equals(str) ) id = T_VOID;
				break L0;
			case 'w':
				if( "with".equals(str) ) id = T_WITH;
				break L0;
			}
			break L0;
		case 5:
			switch( str.charAt(0) ) {
			case 'b':
				if( "break".equals(str) ) id = T_BREAK;
				break L0;
			case 'c':
				ch = str.charAt(1);
				if( ch == 'o' && "const".equals(str) ) id = T_CONST;
				else if( ch == 'a' && "catch".equals(str) ) id = T_CATCH;
				else if( ch == 'l' && "class".equals(str) ) id = T_CLASS;
				break L0;
			case 'f':
				if( "false".equals(str) ) id = T_FALSE;
				break L0;
			case 'o':
				if( "octet".equals(str) ) id = T_OCTET;
				break L0;
			case 's':
				if( "super".equals(str) ) id = T_SUPER;
				break L0;
			case 't':
				if( "throw".equals(str) ) id = T_THROW;
				break L0;
			case 'w':
				if( "while".equals(str) ) id = T_WHILE;
				break L0;
			}
			break L0;
		case 6:
			switch( str.charAt(0) ) {
			case 'd':
				if( "delete".equals(str) ) id = T_DELETE;
				break L0;
			case 'e':
				if( "export".equals(str) ) id = T_EXPORT;
				break L0;
			case 'g':
				ch = str.charAt(1);
				if( ch == 'l' && "global".equals(str) ) id = T_GLOBAL;
				else if( ch == 'e' && "getter".equals(str) ) id = T_GETTER;
				break L0;
			case 'i':
				if( "import".equals(str) ) id = T_IMPORT;
				break L0;
			case 'p':
				if( "public".equals(str) ) id = T_PUBLIC;
				break L0;
			case 'r':
				if( "return".equals(str) ) id = T_RETURN;
				break L0;
			case 's':
				switch( str.charAt(2) ) {
				case 't':
					if( "setter".equals(str) ) id = T_SETTER;
					break L0;
				case 'a':
					if( "static".equals(str) ) id = T_STATIC;
					break L0;
				case 'r':
					if( "string".equals(str) ) id = T_STRING;
					break L0;
				case 'i':
					if( "switch".equals(str) ) id = T_SWITCH;
					break L0;
				}
			case 't':
				if( "typeof".equals(str) ) id = T_TYPEOF;
				break L0;
			}
			break L0;
		case 7:
			switch( str.charAt(0) ) {
			case 'd':
				if( "default".equals(str) ) id = T_DEFAULT;
				break L0;
			case 'e':
				if( "extends".equals(str) ) id = T_EXTENDS;
				break L0;
			case 'f':
				if( "finally".equals(str) ) id = T_FINALLY;
				break L0;
			case 'i':
				if( "isvalid".equals(str) ) id = T_ISVALID;
				break L0;
			case 'p':
				if( "private".equals(str) ) id = T_PRIVATE;
				break L0;
			}
			break L0;
		case 8:
			switch( str.charAt(0) ) {
			case 'c':
				if( "continue".equals(str) ) id = T_CONTINUE;
				break L0;
			case 'd':
				if( "debugger".equals(str) ) id = T_DEBUGGER;
				break L0;
			case 'f':
				if( "function".equals(str) ) id = T_FUNCTION;
				break L0;
			case 'p':
				if( "property".equals(str) ) id = T_PROPERTY;
				break L0;
			case 'I':
				if( "Infinity".equals(str) ) id = T_INFINITY;
				break L0;
			}
			break L0;
		case 9:
			if( "protected".equals(str) ) id = T_PROTECTED;
			break L0;
		case 10:
			ch = str.charAt(9);
			if( ch == 'e' && "invalidate".equals(str) ) id = T_INVALIDATE;
			else if( ch == 'f' && "instanceof".equals(str) ) id = T_INSTANCEOF;
			break L0;
		case 11:
			if( "incontextof".equals(str) ) id = T_INCONTEXTOF;
			break L0;
		case 12:
			if( "synchronized".equals(str) ) id = T_SYNCHRONIZED;
			break L0;
		}
		return id;
	}
}
