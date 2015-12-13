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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Lexer extends LexBase {
	//private static final String TAG = "Lexer";
	//private static final boolean LOGD = false;

	private boolean mIsFirst;
	private boolean mIsExprMode;
	private boolean mResultNeeded;
	private boolean mRegularExpression;
	private boolean mBareWord;
	private boolean mDicFunc;	// dicfunc quick-hack

	private int mValue;
	private int mPrevToken;
	private int mPrevPos;
	private int mNestLevel;
	private int mIfLevel;
	private static final int BUFFER_CAPACITY = 1024;
	//private static final int CARRIAGE_RETURN = 13;
	private static final int LINE_FEED = 10;


	private Queue<TokenPair>	mRetValDeque;
	private ArrayList<EmbeddableExpressionData> mEmbeddableExpressionDataStack;
	private ArrayList<Object> mValues;
	Variant getValue(int idx) { return new Variant(mValues.get(idx)); }
	String getString( int idx ) {
		Object ret = mValues.get(idx);
		if( ret instanceof String ) {
			return (String)ret;
		} else {
			return null;
		}
	}

	private Compiler mBlock;
	private static HashMap<String,Integer> mReservedWordHash;
	private static void initReservedWordsHashTable() {
		if( mReservedWordHash == null ) {
			mReservedWordHash = new HashMap<String,Integer>();
		}
		if( mReservedWordHash.size() > 0 ) return;

		mReservedWordHash.put( "break", Token.T_BREAK );
		mReservedWordHash.put( "continue", Token.T_CONTINUE );
		mReservedWordHash.put( "const", Token.T_CONST );
		mReservedWordHash.put( "catch", Token.T_CATCH );
		mReservedWordHash.put( "class", Token.T_CLASS );
		mReservedWordHash.put( "case", Token.T_CASE );
		mReservedWordHash.put( "debugger", Token.T_DEBUGGER );
		mReservedWordHash.put( "default", Token.T_DEFAULT );
		mReservedWordHash.put( "delete", Token.T_DELETE );
		mReservedWordHash.put( "do", Token.T_DO );
		mReservedWordHash.put( "extends", Token.T_EXTENDS );
		mReservedWordHash.put( "export", Token.T_EXPORT );
		mReservedWordHash.put( "enum", Token.T_ENUM );
		mReservedWordHash.put( "else", Token.T_ELSE );
		mReservedWordHash.put( "function", Token.T_FUNCTION );
		mReservedWordHash.put( "finally", Token.T_FINALLY );
		mReservedWordHash.put( "false", Token.T_FALSE );
		mReservedWordHash.put( "for", Token.T_FOR );
		mReservedWordHash.put( "global", Token.T_GLOBAL );
		mReservedWordHash.put( "getter", Token.T_GETTER );
		mReservedWordHash.put( "goto", Token.T_GOTO );
		mReservedWordHash.put( "incontextof", Token.T_INCONTEXTOF );
		mReservedWordHash.put( "invalidate", Token.T_INVALIDATE );
		mReservedWordHash.put( "instanceof", Token.T_INSTANCEOF );
		mReservedWordHash.put( "isvalid", Token.T_ISVALID );
		mReservedWordHash.put( "import", Token.T_IMPORT );
		mReservedWordHash.put( "int", Token.T_INT );
		mReservedWordHash.put( "in", Token.T_IN );
		mReservedWordHash.put( "if", Token.T_IF );
		mReservedWordHash.put( "null", Token.T_NULL );
		mReservedWordHash.put( "new", Token.T_NEW );
		mReservedWordHash.put( "octet", Token.T_OCTET );
		mReservedWordHash.put( "protected", Token.T_PROTECTED );
		mReservedWordHash.put( "property", Token.T_PROPERTY );
		mReservedWordHash.put( "private", Token.T_PRIVATE );
		mReservedWordHash.put( "public", Token.T_PUBLIC );
		mReservedWordHash.put( "return", Token.T_RETURN );
		mReservedWordHash.put( "real", Token.T_REAL );
		mReservedWordHash.put( "synchronized", Token.T_SYNCHRONIZED );
		mReservedWordHash.put( "switch", Token.T_SWITCH );
		mReservedWordHash.put( "static", Token.T_STATIC );
		mReservedWordHash.put( "setter", Token.T_SETTER );
		mReservedWordHash.put( "string", Token.T_STRING );
		mReservedWordHash.put( "super", Token.T_SUPER );
		mReservedWordHash.put( "typeof", Token.T_TYPEOF );
		mReservedWordHash.put( "throw", Token.T_THROW );
		mReservedWordHash.put( "this", Token.T_THIS );
		mReservedWordHash.put( "true", Token.T_TRUE );
		mReservedWordHash.put( "try", Token.T_TRY );
		mReservedWordHash.put( "void", Token.T_VOID );
		mReservedWordHash.put( "var", Token.T_VAR );
		mReservedWordHash.put( "while", Token.T_WHILE );
		mReservedWordHash.put( "NaN", Token.T_NAN );
		mReservedWordHash.put( "Infinity", Token.T_INFINITY );
		mReservedWordHash.put( "with", Token.T_WITH );
	}

	public Lexer( Compiler block, String script, boolean isexpr, boolean resultneeded ) {
		super();
		initReservedWordsHashTable();

		mRetValDeque = new LinkedList<TokenPair>();
		mEmbeddableExpressionDataStack = new ArrayList<EmbeddableExpressionData>();
		mValues = new ArrayList<Object>();

		mBlock = block;
		mIsExprMode = isexpr;
		mResultNeeded = resultneeded;
		mPrevToken = -1;
		if( mIsExprMode ) {
			mStream = new StringStream(script+";");
		} else {
			if( script.startsWith("#!") == true ) {
				// #! を // に置換
				mStream = new StringStream( "//" + script.substring(2));
			} else {
				mStream = new StringStream(script);
			}
		}
		if( CompileState.mEnableDicFuncQuickHack ) { //----- dicfunc quick-hack
			mDicFunc = false;
			if( mIsExprMode && (script.startsWith("[") == true || script.startsWith("%[") == true) ) {
				mDicFunc = true;
			}
		}

		//mIfLevel = 0;
		//mPrevPos = 0;
		//mNestLevel = 0;
		mIsFirst = true;
		//mRegularExpression = false;
		//mBareWord = false;
		putValue(null);
	}
	public void setStartOfRegExp() { mRegularExpression = true; }
	public void setNextIsBareWord() { mBareWord = true; }

	private String parseRegExp() throws CompileException {
		boolean ok = false;
		boolean lastbackslash = false;
		StringBuilder str = new StringBuilder(BUFFER_CAPACITY);
		StringBuilder flag = new StringBuilder(BUFFER_CAPACITY);
		int c = mStream.next();
		while( c != -1 ) {
			if( c == '\\' ) {
				str.append( (char)c );
				if( lastbackslash == true )
					lastbackslash = false;
				else
					lastbackslash = true;
			} else if( c == '/' && lastbackslash == false ) {
				c = mStream.next();
				if( c == -1 ) {
					ok = true;
					break;
				}
				//int flagMask = 0;
				flag.delete( 0, flag.length() );
				while( c >= 'a' && c <= 'z' ) {
					/*
					switch( c ) {
					case 'g':
						break;
					case 'i':
						flagMask |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
						break;
					case 'l':
						break;
					}
					*/
					flag.append( (char)c );
					c = mStream.next();
					if( c == -1 ) break;
				}
				mStream.ungetC();
				str.insert( 0, "//" );
				String flgStr = flag.toString();
				str.insert( 2, flgStr );
				str.insert( 2+flgStr.length(), "/" );
				ok = true;
				break;
			} else {
				lastbackslash = false;
				str.append( (char)c );
			}
			c = mStream.next();
		}
		if( !ok ) {
			throw new CompileException(Error.StringParseError);
		}
		return str.toString();
	}
	// 渡されたByteBufferを切り詰めた、新しいByteBufferを作る
	private ByteBuffer compactByteBuffer( ByteBuffer b ) {
		int count = b.position();
		ByteBuffer ret = ByteBuffer.allocateDirect(count);
		b.position(0);
		for( int i = 0; i < count; i++ ) {
			ret.put( b.get() );
		}
		ret.position(0);
		return ret;
	}
	private ByteBuffer parseOctet() throws CompileException {
		boolean leading = true;
		byte cur = 0;

		int count = mStream.countOctetTail() / 2 + 1;
		ByteBuffer buffer = ByteBuffer.allocateDirect(count);
		int c = mStream.peekC();
		while( c != -1 ) {
			if( mStream.skipComment() == StringStream.ENDED ) {
				throw new CompileException(Error.StringParseError);
			}
			c = mStream.getC();
			int next = mStream.peekC();
			if( c == '%' && next == '>' ) {
				mStream.incOffset();

				if( !leading ) {
					buffer.put(cur);
				}
				return compactByteBuffer(buffer);
			}
			int num = getHexNum(c);
			if( num != -1 ) {
				if( leading ) {
					cur = (byte)num;
					leading = false;
				} else {
					cur <<= 4;
					cur += num;
					buffer.put(cur);
					leading = true;
				}
			}
			if( leading == false && c == ',' ) {
				buffer.put(cur);
				leading = true;
			}
		}
		throw new CompileException(Error.StringParseError);
	}
	private String parseString( int delimiter ) throws CompileException {
		return mStream.readString(delimiter,false);
	}
	private int parsePPExpression( String script ) throws CompileException {
		PreprocessorExpressionParser parser = new PreprocessorExpressionParser( mBlock.getTJS(), script );
		return parser.parse();
	}
	private int processPPStatement() throws CompileException {
		// process pre-prosessor statements.
		int offset = mStream.getOffset();
		int c = mStream.getC();
		if( c == 's' && mStream.equalString("et") == true ) {
			// set statemenet
			mBlock.notifyUsingPreProcessror();
			mStream.skipSpace();
			c = mStream.getC();
			if( c == -1 || c != '(' ) {
				throw new CompileException(Error.PPError);
			}
			c = mStream.next();
			StringBuilder script = new StringBuilder(BUFFER_CAPACITY);
			int plevel = 0;
			while( c != -1 && (plevel != 0 || c != ')') ) {
				if( c == '(' ) plevel++;
				else if( c == ')' ) plevel--;
				script.append( (char)c );
				c = mStream.getC();
			}
			if( c != -1 ) mStream.ungetC();

			parsePPExpression( script.toString() );
			if( mStream.isEOF() ) return StringStream.ENDED;
			return StringStream.CONTINUE;
		}
		if( c == 'i' && mStream.equalString("f") == true ) {
			// if statement
			mBlock.notifyUsingPreProcessror();
			mStream.skipSpace();
			c = mStream.getC();
			if( c == -1 || c != '(' ) {
				throw new CompileException(Error.PPError);
			}
			c = mStream.next();
			StringBuilder script = new StringBuilder(BUFFER_CAPACITY);
			int plevel = 0;
			while( c != -1 && ( plevel != 0 || c != ')' ) ) {
				if( c == '(' ) plevel++;
				else if( c == ')' ) plevel--;
				script.append( (char)c );
				c = mStream.getC();
			}
//			if( c != -1 ) mStream.ungetC();
			int ret = parsePPExpression( script.toString() );
			if( ret == 0 ) return skipUntilEndif();
			mIfLevel++;
			if( mStream.isEOF() ) return StringStream.ENDED;
			return StringStream.CONTINUE;
		}

		if( c == 'e' && mStream.equalString("ndif") == true ) {
			// endif statement
			mIfLevel--;
			if( mIfLevel < 0 ) throw new CompileException( Error.PPError );
			if( mStream.isEOF() ) return StringStream.ENDED;
			return StringStream.CONTINUE;
		}
		mStream.setOffset( offset );
		return StringStream.NOT_COMMENT;
	}
	private int skipUntilEndif() throws CompileException {
		int exl = mIfLevel;
		mIfLevel++;
		int c = mStream.getC();
		while( true ) {
			if( c == '/' ) {
				// comment
				mStream.ungetC();
				int ret = mStream.skipComment();
				switch( ret ) {
				case StringStream.ENDED:
					throw new CompileException( Error.PPError );
				case StringStream.CONTINUE:
					break;
				case StringStream.NOT_COMMENT:
					c = mStream.next();
					if( mStream.isEOF() ) throw new CompileException( Error.PPError );
					break;
				}
			} else if( c == '@' ) {
				//c = mStream.getC();
				boolean skipp = false;
				if( mStream.equalString( "if" ) ) {
					mIfLevel++;
					skipp = true;
				} else if( mStream.equalString( "set" ) ) {
					skipp = true;
				} else if( mStream.equalString( "endif" ) ) {
					mIfLevel--;
					if( mIfLevel == exl ) { // skip ended
						mStream.skipSpace();
						if( mStream.isEOF() ) return StringStream.ENDED;
						return StringStream.CONTINUE;
					}
				} else {
					c = mStream.getC();
				}
				if( skipp ) {
					mStream.skipSpace();
					if( mStream.isEOF() ) throw new CompileException( Error.PPError );
					c = mStream.getC();
					if( c != '(' ) throw new CompileException( Error.PPError );
					c = mStream.next();
					int plevel = 0;
					while( c != -1 && ( plevel > 0 || c != ')' ) ) {
						if( c == '(' ) plevel++;
						else if( c == ')' ) plevel--;
						c = mStream.next();
					}
					if( c == -1 ) throw new CompileException( Error.PPError );
					mStream.skipSpace();
					if( mStream.isEOF() ) throw new CompileException( Error.PPError );
				}
			} else {
				c = mStream.getC();
				if( c == -1 ) throw new CompileException( Error.PPError );
			}
		}
	}
	private int getHexNum( int c ) {
		if( c >= 'a' && c <= 'f' ) return c-'a'+10;
		if( c >= 'A' && c <= 'F' ) return c-'A'+10;
		if( c >= '0' && c <= '9' ) return c-'0';
		return -1;
	}
	private int putValue( Object val ) {
		mValues.add( val );
		return mValues.size() - 1;
	}
	private int getToken() throws CompileException {
		if( mStream.isEOF() == true ) return 0;
		if( mRegularExpression == true ) {
			mRegularExpression = false;
			mStream.setOffset( mPrevPos );
			mStream.skipSpace();
			mStream.next(); // 最初の'/'を読み飛ばし
			String pattern = parseRegExp();
			mValue = putValue(pattern);
			return Token.T_REGEXP;
		}

		int c;
		boolean retry;
		do {
			retry = false;
			mStream.skipSpace();
			mPrevPos = mStream.getOffset();
			c = mStream.getC();
			switch(c) {
			case 0:
			case -1:
				return 0;
			case '>':
				c = mStream.getC();
				if( c == '>' ) {	// >>
					c = mStream.getC();
					if( c == '>' ) {	// >>>
						c = mStream.getC();
						if( c == '=' ) {	// >>>=
							return Token.T_RBITSHIFTEQUAL;
						} else {
							mStream.ungetC();
							return Token.T_RBITSHIFT;
						}
					} else if( c == '=' ) {	// >>=
						return Token.T_RARITHSHIFTEQUAL;
					} else {	// >>
						mStream.ungetC();
						return Token.T_RARITHSHIFT;
					}
				} else if( c == '=' ) {	// >=
					return Token.T_GTOREQUAL;
				} else {
					mStream.ungetC();
					return Token.T_GT;
				}

			case '<':
				c = mStream.getC();
				if( c == '<' ) {	// <<
					c = mStream.getC();
					if( c == '=' ) {	// <<=
						return Token.T_LARITHSHIFTEQUAL;
					} else {	// <<
						mStream.ungetC();
						return Token.T_LARITHSHIFT;
					}
				} else if( c == '-' ) {	// <-
					c = mStream.getC();
					if( c == '>' ) {	// <->
						return Token.T_SWAP;
					} else {	// <
						mStream.ungetC();
						mStream.ungetC();
						return Token.T_LT;
					}
				} else if( c == '=' ) {	// <=
					return Token.T_LTOREQUAL;
				} else if( c == '%' ) {	// '<%'   octet literal
					ByteBuffer buffer = parseOctet();
					mValue = putValue(buffer);
					return Token.T_CONSTVAL;
				} else {	// <
					mStream.ungetC();
					return Token.T_LT;
				}

			case '=':
				c = mStream.getC();
				if( c == '=' ) { // ===
					c = mStream.getC();
					if( c == '=' ) {
						return Token.T_DISCEQUAL;
					} else { // ==
						mStream.ungetC();
						return Token.T_EQUALEQUAL;
					}
				} else if( c == '>' ) {	// =>
					return Token.T_COMMA;
				} else {	// =
					mStream.ungetC();
					return Token.T_EQUAL;
				}

			case '!':
				c = mStream.getC();
				if( c == '=' ) {
					c = mStream.getC();
					if( c == '=' ) { // !==
						return Token.T_DISCNOTEQUAL;
					} else { // !=
						mStream.ungetC();
						return Token.T_NOTEQUAL;
					}
				} else {	// !
					mStream.ungetC();
					return Token.T_EXCRAMATION;
				}

			case '&':
				c = mStream.getC();
				if( c == '&' ) {
					c = mStream.getC();
					if( c == '=' ) { // &&=
						return Token.T_LOGICALANDEQUAL;
					} else { // &&
						mStream.ungetC();
						return Token.T_LOGICALAND;
					}
				} else if( c == '=' ) { // &=
					return Token.T_AMPERSANDEQUAL;
				} else {	// &
					mStream.ungetC();
					return Token.T_AMPERSAND;
				}

			case '|':
				c = mStream.getC();
				if( c == '|' ) {
					c = mStream.getC();
					if( c == '=' ) { // ||=
						return Token.T_LOGICALOREQUAL;
					} else { // ||
						mStream.ungetC();
						return Token.T_LOGICALOR;
					}
				} else if( c == '=' ) { // |=
					return Token.T_VERTLINEEQUAL;
				} else { // |
					mStream.ungetC();
					return Token.T_VERTLINE;
				}

			case '.':
				c = mStream.getC();
				if( c >= '0' && c <= '9' ) { // number
					mStream.ungetC();
					mStream.ungetC();
					Object o = parseNumber();
					if( o != null ) {
						if( o instanceof Integer )
							mValue = putValue((Integer)o);
						else if( o instanceof Double )
							mValue = putValue((Double)o);
						else
							mValue = putValue(null);
					} else {
						mValue = putValue(null);
					}
					return Token.T_CONSTVAL;
				} else if( c == '.' ) {
					c = mStream.getC();
					if( c == '.' ) { // ...
						return Token.T_OMIT;
					} else { // .
						mStream.ungetC();
						mStream.ungetC();
						return Token.T_DOT;
					}
				} else { // .
					mStream.ungetC();
					return Token.T_DOT;
				}

			case '+':
				c = mStream.getC();
				if( c == '+' ) { // ++
					return Token.T_INCREMENT;
				} else if( c == '=' ) { // +=
					return Token.T_PLUSEQUAL;
				} else { // +
					mStream.ungetC();
					return Token.T_PLUS;
				}

			case '-':
				c = mStream.getC();
				if( c == '-' ) { // --
					return Token.T_DECREMENT;
				} else if( c == '=' ) {
					return Token.T_MINUSEQUAL; // -=
				} else { // -
					mStream.ungetC();
					return Token.T_MINUS;
				}

			case '*':
				c = mStream.getC();
				if( c == '=' ) { // *=
					return Token.T_ASTERISKEQUAL;
				} else { // *
					mStream.ungetC();
					return Token.T_ASTERISK;
				}

			case '/':
				c = mStream.getC();
				if( c == '/' || c == '*' ) {
					mStream.ungetC();
					mStream.ungetC();
					int comment = mStream.skipComment();
					if( comment == StringStream.CONTINUE ) {
						retry = true;
						break;
					} else if( comment == StringStream.ENDED ) {
						return 0;
					}
				}
				if( c == '=' ) {	// /=
					return Token.T_SLASHEQUAL;
				} else {	// /
					mStream.ungetC();
					return Token.T_SLASH;
				}

			case '\\':
				c = mStream.getC();
				if( c == '=' ) {	// \=
					return Token.T_BACKSLASHEQUAL;
				} else {	// \
					mStream.ungetC();
					return Token.T_BACKSLASH;
				}

			case '%':
				c = mStream.getC();
				if( c == '=' ) { // %=
					return Token.T_PERCENTEQUAL;
				} else { // %
					mStream.ungetC();
					return Token.T_PERCENT;
				}

			case '^':
				c = mStream.getC();
				if( c == '=' ) { // ^=
					return Token.T_CHEVRONEQUAL;
				} else { // ^
					mStream.ungetC();
					return Token.T_CHEVRON;
				}

			case '[':
				mNestLevel++;
				return Token.T_LBRACKET;

			case ']':
				mNestLevel--;
				return Token.T_RBRACKET;

			case '(':
				mNestLevel++;
				return Token.T_LPARENTHESIS;

			case ')':
				mNestLevel--;
				return Token.T_RPARENTHESIS;

			case '~':
				return Token.T_TILDE;

			case '?':
				return Token.T_QUESTION;

			case ':':
				return Token.T_COLON;

			case ',':
				return Token.T_COMMA;

			case ';':
				return Token.T_SEMICOLON;

			case '{':
				mNestLevel++;
				return Token.T_LBRACE;

			case '}':
				mNestLevel--;
				return Token.T_RBRACE;

			case '#':
				return Token.T_SHARP;

			case '$':
				return Token.T_DOLLAR;

			case '\'':
			case '\"': {
				// literal string
				String str = parseString(c);
				mValue = putValue(str);
				return Token.T_CONSTVAL;
			}

			case '@': {
				// embeddable expression in string (such as @"this can be embeddable like &variable;")
				int offset = mStream.getOffset();
				mStream.skipSpace();
				c = mStream.next();
				if( c == '\'' || c == '\"' ) {
					EmbeddableExpressionData data = new EmbeddableExpressionData();
					data.mState = EmbeddableExpressionData.START;
					data.mWaitingNestLevel = mNestLevel;
					data.mDelimiter = c;
					data.mNeedPlus = false;
					if( mStream.isEOF() ) return 0;
					mEmbeddableExpressionDataStack.add( data );
					return -1;
				} else {
					mStream.setOffset( offset );
				}
				// possible pre-procesor statements
				switch( processPPStatement() ) {
				case StringStream.CONTINUE:
					retry = true;
					break;
				case StringStream.ENDED:
					return 0;
				case StringStream.NOT_COMMENT:
					mStream.setOffset( offset );
				}
				break;
			}
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': {
				mStream.ungetC();
				Object o = parseNumber();
				if( o != null ) {
					if( o instanceof Integer )
						mValue = putValue((Integer)o);
					else if( o instanceof Double )
						mValue = putValue((Double)o);
					else
						throw new CompileException( Error.NumberError );
				} else {
					throw new CompileException( Error.NumberError );
				}
				return Token.T_CONSTVAL;
			}
			}	// switch(c)
		} while( retry );

		if( isWAlpha(c) == false && c != '_' ) {
			String str = Error.InvalidChar.replace( "%1", escapeC((char)c) );
			throw new CompileException( str );
		}
		int oldC = c;
		int offset = mStream.getOffset() - 1;
		int nch = 0;
		while( isWDigit(c) || isWAlpha(c) || c == '_' || c > 0x0100 || /*c == CARRIAGE_RETURN || */c == LINE_FEED ) {
			c = mStream.getC();
			nch++;
		}
		if( nch == 0 ) {
			String str = Error.InvalidChar.replace( "%1", escapeC((char)oldC) );
			throw new CompileException( str );
		} else {
			mStream.ungetC();
		}
		String str = mStream.substring( offset, offset + nch );
		int strLen = str.length();
		StringBuilder symStr = new StringBuilder(BUFFER_CAPACITY);
		for( int i = 0; i < strLen; i++ ) {
			char t = str.charAt(i);
			if( /*t != CARRIAGE_RETURN &&*/ t != LINE_FEED )
				symStr.append(t);
		}
		str = symStr.toString();

		int retnum;
		if( mBareWord ) {
			retnum = -1;
		} else {
			Integer id = mReservedWordHash.get( str );
			if( id != null ) {
				retnum = id.intValue();
			} else {
				retnum = -1;
			}
		}

		mBareWord = false;

		if( retnum == -1 ) { // not a reserved word
			mValue = putValue(str);
			return Token.T_SYMBOL;
		}
		switch( retnum ) {
		case Token.T_FALSE:
			mValue = putValue( Integer.valueOf(0) );
			return Token.T_CONSTVAL;
		case Token.T_NULL:
			mValue = putValue( new VariantClosure(null) );
			return Token.T_CONSTVAL;
		case Token.T_TRUE:
			mValue = putValue( Integer.valueOf(1) );
			return Token.T_CONSTVAL;
		case Token.T_NAN:
			mValue = putValue( Double.valueOf( Double.NaN ) );
			return Token.T_CONSTVAL;
		case Token.T_INFINITY:
			mValue = putValue( Double.valueOf( Double.POSITIVE_INFINITY ) );
			return Token.T_CONSTVAL;
		}
		return retnum;
	}
	public int getValue() { return mValue; }
	public int getNext() throws CompileException {
		//if( LOGD ) Log.v(TAG,"getNext");
		if( mIsFirst ) {
			mIsFirst = false;
			/*
			try {
				preProcess(); // オリジナルはここで改行コードの置換しているけど、何もしない
			} catch( CompileException e ) {
				return 0;
			}
			*/
			if( mIsExprMode && mResultNeeded ) {
				mValue = 0;
				return Token.T_RETURN;
			}
		}
		int n = 0;
		mValue = 0;
		do {
			if( mRetValDeque.size() > 0 ) {
				TokenPair pair = mRetValDeque.poll();
				mValue = pair.value;
				mPrevToken = pair.token;
				return pair.token;
			}
			try {
				if( mEmbeddableExpressionDataStack.size() == 0 ) {
					mStream.skipSpace();
					n = getToken();

					if( CompileState.mEnableDicFuncQuickHack ) { // dicfunc quick-hack
						if( mDicFunc ) {
							if( n == Token.T_PERCENT ) {
								// push "function { return %"
								mRetValDeque.offer( new TokenPair(Token.T_FUNCTION,0) );
								mRetValDeque.offer( new TokenPair(Token.T_LBRACE,0) );
								mRetValDeque.offer( new TokenPair(Token.T_RETURN,0) );
								mRetValDeque.offer( new TokenPair(Token.T_PERCENT,0) );
								n = -1;
							} else if ( n == Token.T_LBRACKET && mPrevToken != Token.T_PERCENT ) {
								// push "function { return ["
								mRetValDeque.offer( new TokenPair(Token.T_FUNCTION,0) );
								mRetValDeque.offer( new TokenPair(Token.T_LBRACE,0) );
								mRetValDeque.offer( new TokenPair(Token.T_RETURN,0) );
								mRetValDeque.offer( new TokenPair(Token.T_LBRACKET,0) );
								n = -1;
							} else if( n == Token.T_RBRACKET ) {
								// push "] ; } ( )"
								mRetValDeque.offer( new TokenPair(Token.T_RBRACKET,0) );
								mRetValDeque.offer( new TokenPair(Token.T_SEMICOLON,0) );
								mRetValDeque.offer( new TokenPair(Token.T_RBRACE,0) );
								mRetValDeque.offer( new TokenPair(Token.T_LPARENTHESIS,0) );
								mRetValDeque.offer( new TokenPair(Token.T_RPARENTHESIS,0) );
								n = -1;
							}
						}
					}
				} else {
					// embeddable expression mode
					EmbeddableExpressionData data = mEmbeddableExpressionDataStack.get( mEmbeddableExpressionDataStack.size() -1 );
					switch( data.mState ) {
					case EmbeddableExpressionData.START:
						mRetValDeque.offer( new TokenPair(Token.T_LPARENTHESIS,0) );
						n = -1;
						data.mState = EmbeddableExpressionData.NEXT_IS_STRING_LITERAL;
						break;

					case EmbeddableExpressionData.NEXT_IS_STRING_LITERAL: {
						String str = mStream.readString( data.mDelimiter, true );
						int res = mStream.getStringStatus();
						if( res == StringStream.DELIMITER ) {
							if( str.length() > 0 ) {
								if( data.mNeedPlus ) {
									mRetValDeque.offer( new TokenPair(Token.T_PLUS,0) );
								}
							}
							if( str.length() > 0 || data.mNeedPlus == false ) {
								int v = putValue(str);
								mRetValDeque.offer( new TokenPair(Token.T_CONSTVAL,v) );
							}
							mRetValDeque.offer( new TokenPair(Token.T_RPARENTHESIS,0) );
							mEmbeddableExpressionDataStack.remove( mEmbeddableExpressionDataStack.size() - 1 );
							n = -1;
							break;
						} else {
							// c is next to ampersand mark or '${'
							if( str.length() > 0 ) {
								if( data.mNeedPlus ) {
									mRetValDeque.offer( new TokenPair(Token.T_PLUS,0) );
								}
								int v = putValue(str);
								mRetValDeque.offer( new TokenPair(Token.T_CONSTVAL,v) );
								data.mNeedPlus = true;
							}
							if( data.mNeedPlus == true ) {
								mRetValDeque.offer( new TokenPair(Token.T_PLUS,0) );
							}
							mRetValDeque.offer( new TokenPair(Token.T_STRING,0) );
							mRetValDeque.offer( new TokenPair(Token.T_LPARENTHESIS,0) );
							data.mState = EmbeddableExpressionData.NEXT_IS_EXPRESSION;
							if( res == StringStream.AMPERSAND ) {
								data.mWaitingToken = Token.T_SEMICOLON;
							} else if( res == StringStream.DOLLAR ) {
								data.mWaitingToken = Token.T_RBRACE;
								mNestLevel++;
							}
							n = -1;
							break;
						}
					}
					case EmbeddableExpressionData.NEXT_IS_EXPRESSION:
						mStream.skipSpace();
						n = getToken();
						if( n == data.mWaitingToken && mNestLevel == data.mWaitingNestLevel ) {
							// end of embeddable expression mode
							mRetValDeque.offer( new TokenPair(Token.T_RPARENTHESIS,0) );
							data.mNeedPlus = true;
							data.mState = EmbeddableExpressionData.NEXT_IS_STRING_LITERAL;
							n = -1;
						}
						break;
					}
				}
				if( n == 0 ) {
					if( mIfLevel != 0 ) {
						throw new CompileException( Error.PPError );
					}
				}
			} catch( CompileException e ) {
				mBlock.error(e.getMessage());
				return 0;
			} /*catch( ScriptException e ) {
				return 0;
			} catch( TJSException e ) {
				return 0;
			}*/
		} while ( n < 0 );
		mPrevToken = n;
		return n;
	}
	public int getCurrentPosition() {
		int offset = mStream.getOffset();
		return offset > 0 ? offset - 1 : 0;
	}
	/*
	public int srcPosToLine( int pos ) {
		return mStream.getSrcPosToLine(pos);
	}
	*/
	/*
	public int getCurrentLine() {
		return mBlock.srcPosToLine(mStream.getOffset());
	}
	*/
	//public int lineToSrcPos(int line) {
	//	return mStream.getLineToSrcPos(line);
	//}
	/*
	public String getLine(int line) {
		return mStream.getLine(line);
	}
	public final int getMaxLine() {
		return mStream.getMaxLine();
	}
	*/
}
