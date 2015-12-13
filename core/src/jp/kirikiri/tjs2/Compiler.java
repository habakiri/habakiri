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
import java.util.Stack;

import jp.kirikiri.tjs2.translate.JavaCodeIntermediate;

/**
 *
 */
public class Compiler implements SourceCodeAccessor {
	private static final boolean LOGD = false;
	private static final String TAG = "Compiler";

	private static final String ERROR_TAG = "Syntax error";

	private TJS mOwner;

	// 以下の3つは実行時には不要のはず、ScriptBlock もコンパイル時にいるものと実行時にいるもので分離した方がいいかな
	private boolean mUsingPreProcessor;
	private int mCompileErrorCount;
	private LexicalAnalyzer mLexicalAnalyzer;

	// 以下の2つは実行時に必要、TopLevel は不要になるけど。
	private InterCodeObject mTopLevelObject;
	private ArrayList<InterCodeObject> mInterCodeObjectList;

	// InterCodeGenerator は、インターフェイスを作った方がいいかな？
	// TJS2 バイトコードのみじゃなくて、他のコードジェネレーターも作りやすいように。

	/** 現在のコンテキスト */
	private InterCodeGenerator mInterCodeGenerator;
	/** トップレベルコンテキスト */
	private InterCodeGenerator mTopLevelGenerator;
	/** コンテキストスタック */
	private Stack<InterCodeGenerator> mGeneratorStack;
	/** コードリスト */
	private ArrayList<InterCodeGenerator> mInterCodeGeneratorList;

	public void notifyUsingPreProcessror() { mUsingPreProcessor = true; }
	public boolean isUsingPreProcessor() { return mUsingPreProcessor; }


	private boolean mIsUnlex;
	private int mUnlexToken;
	private int mUnlexValue;
	private int mToken;
	private int mValue;
	private ExprNode mNode;
	private String mFirstError;
	private int mFirstErrorPos;

	// 以下の4つは実行時にいるかな、名前以外はエラー発生時に必要になるだけだろうけど。
	private String mName;
	private int mLineOffset;
	private String mScript;
	private ScriptLineData mLineData;

	private final void pushContextStack( String name, int type) throws TJSException, VariantException {
		InterCodeGenerator ctx = new InterCodeGenerator(mInterCodeGenerator,name,this,type);
		if( mInterCodeGenerator == null ) {
			if( mTopLevelGenerator != null ) throw new TJSException(Error.InternalError);
			mTopLevelGenerator = ctx;
		}
		mGeneratorStack.push(ctx);
		mInterCodeGenerator = ctx;
	}
	private final void popContextStack() throws VariantException, TJSException {
		mInterCodeGenerator.commit();
		mGeneratorStack.pop();
		if( mGeneratorStack.size() >= 1 )
			mInterCodeGenerator = mGeneratorStack.peek();
		else
			mInterCodeGenerator = null;
	}


	public Compiler( TJS owner ) {
		mOwner = owner;

		// Java で初期値となる初期化は省略
		//mScript = null;
		//mName = null;
		//mInterCodeContext = null;
		//mTopLevelContext = null;
		//mLexicalAnalyzer = null;
		//mUsingPreProcessor = false;
		//mLineOffset = 0;
		//mCompileErrorCount = 0;
		//mNode = null;

		mGeneratorStack = new Stack<InterCodeGenerator>();

		mInterCodeGeneratorList = new ArrayList<InterCodeGenerator>();
		mInterCodeObjectList = new ArrayList<InterCodeObject>();
	}
	/*
	protected void finalize() {
		if( mTopLevelObject != null ) mTopLevelObject = null;
		if( mGeneratorStack != null ) {
			while( mGeneratorStack.size() > 0 ) {
				mGeneratorStack.pop();
			}
		}

		if( mLexicalAnalyzer != null ) mLexicalAnalyzer = null;
		if( mScript != null ) mScript = null;
		if( mName != null ) mName = null;;

		try {
			super.finalize();
		} catch (Throwable e) {
		}
	}
	*/

	public LexicalAnalyzer getLexicalAnalyzer() {
		return mLexicalAnalyzer;
	}
	public int srcPosToLine( int pos ) {
		return mLineData.getSrcPosToLine(pos);
	}

	private void unlex( int token, int value ) {
		mIsUnlex = true;
		mUnlexToken = token;
		mUnlexValue = value;
	}
	private void unlex() {
		mIsUnlex = true;
		mUnlexToken = mToken;
		mUnlexValue = mValue;
	}
	private int lex() throws CompileException {
		if( mIsUnlex ) {
			mIsUnlex = false;
			mToken = mUnlexToken;
			mValue = mUnlexValue;
			return mUnlexToken;
		} else {
			mToken = mLexicalAnalyzer.getNext();
			mValue = mLexicalAnalyzer.getValue();
			return mToken;
		}
	}
	public void parse( String script, boolean isexpr, boolean resultneeded ) throws VariantException, CompileException, TJSException {
		mCompileErrorCount = 0;
		mLexicalAnalyzer = new LexicalAnalyzer(this,script,isexpr,resultneeded);
		mLineData = new ScriptLineData( script, mLineOffset );

		program();

		mLexicalAnalyzer = null;

		if( mCompileErrorCount > 0 ) {
			throw new TJSScriptError( mFirstError, this, mFirstErrorPos);
		}
	}
	public void error( final String msg ) throws CompileException {
		if( mCompileErrorCount == 0 ) {
			mFirstError = msg;
			mFirstErrorPos = mLexicalAnalyzer.getCurrentPosition();
		}
		mCompileErrorCount++;

		String str = Error.SyntaxError.replace( "%1", msg );
		//int line = 1+mLexicalAnalyzer.getCurrentLine();
		int line = 1+srcPosToLine(mLexicalAnalyzer.getCurrentPosition());
		String message = "Line (" + line + ") : " + str;

		Logger.log( ERROR_TAG, message );
		if( mCompileErrorCount > 20 ) throw new CompileException( Error.TooManyErrors, this, mFirstErrorPos );
	}
	// ----------------------------- parser
	// const_dic_elm
	private ExprNode exprConstDicElm() throws VariantException, CompileException, TJSException {
		int token = lex();
		Variant var;
		if( token == Token.T_CONSTVAL ) {
			String name = mLexicalAnalyzer.getString(mValue);
			token = lex();
			if( token != Token.T_COMMA ) {
				error( Error.ConstDicDelimiterError );
				unlex(); // syntax error
				return null;
			}
			token = lex();
			switch( token ) {
			case Token.T_MINUS:
				token = lex();
				if( token != Token.T_CONSTVAL ) {
					error( Error.ConstDicValueError );
					unlex(); // syntax error
					return null;
				}
				var = mLexicalAnalyzer.getValue(mValue);
				var.changeSign();
				mInterCodeGenerator.getCurrentNode().addDictionaryElement( name, var );
				return null;
			case Token.T_PLUS:
				token = lex();
				if( token != Token.T_CONSTVAL ) {
					error( Error.ConstDicValueError );
					unlex(); // syntax error
					return null;
				}
				var = mLexicalAnalyzer.getValue(mValue);
				var.toNumber();
				mInterCodeGenerator.getCurrentNode().addDictionaryElement( name, var );
				return null;
			case Token.T_CONSTVAL:
				mInterCodeGenerator.getCurrentNode().addDictionaryElement( name, mLexicalAnalyzer.getValue(mValue) );
				return null;
			case Token.T_VOID:
				mInterCodeGenerator.getCurrentNode().addDictionaryElement( name, new Variant() );
				return null;
			case Token.T_LPARENTHESIS: {
				unlex();
				ExprNode node = exprConstInlineArrayOrDic();
				mInterCodeGenerator.getCurrentNode().addDictionaryElement( name, node.getValue() );
				return null;
			}
			default:
				error( Error.ConstDicValueError );
				unlex();
				break;
			}
		}
		unlex();
		return null;
	}
	// const_dic_elm_list
	private ExprNode exprConstDicElmList() throws VariantException, CompileException, TJSException {
		int token;
		do {
			exprConstDicElm();
			token = lex();
		} while( token == Token.T_COMMA );
		unlex();
		return null;
	}
	// const_inline_dic
	private ExprNode exprConstInlineDic() throws VariantException, CompileException, TJSException {
		ExprNode node = mInterCodeGenerator.makeNP0( Token.T_CONSTVAL );
		Dispatch2 dsp = TJS.createDictionaryObject();
		node.setValue( new Variant(dsp,dsp) );
		mInterCodeGenerator.pushCurrentNode( node );
		exprConstDicElmList();
		node = mInterCodeGenerator.getCurrentNode();
		mInterCodeGenerator.popCurrentNode();
		int token = lex();
		if( token == Token.T_RBRACKET ) {
			return node;
		} else {
			error( Error.DicRBRACKETError );
			unlex(); // error
		}
		return null;
	}
	// const_array_elm
	private ExprNode exprConstArrayElm() throws VariantException, CompileException, TJSException {
		int token = lex();
		Variant var;
		switch( token ) {
		case Token.T_MINUS:
			token = lex();
			if( token != Token.T_CONSTVAL ) {
				error( Error.ConstArrayValueError );
				unlex(); // syntax error
				return null;
			}
			var = mLexicalAnalyzer.getValue(mValue);
			var.changeSign();
			mInterCodeGenerator.getCurrentNode().addArrayElement( var );
			return null;
		case Token.T_PLUS:
			token = lex();
			if( token != Token.T_CONSTVAL ) {
				error( Error.ConstArrayValueError );
				unlex(); // syntax error
				return null;
			}
			var = mLexicalAnalyzer.getValue(mValue);
			var.toNumber();
			mInterCodeGenerator.getCurrentNode().addArrayElement( var );
			return null;
		case Token.T_CONSTVAL:
			mInterCodeGenerator.getCurrentNode().addArrayElement( mLexicalAnalyzer.getValue(mValue) );
			return null;
		case Token.T_VOID:
			mInterCodeGenerator.getCurrentNode().addArrayElement( new Variant() );
			return null;
		case Token.T_LPARENTHESIS: {
			unlex();
			ExprNode node = exprConstInlineArrayOrDic();
			mInterCodeGenerator.getCurrentNode().addArrayElement( node.getValue() );
			return null;
			}
		}
		unlex();
		return null;
	}
	// const_array_elm_list, const_array_elm_list_opt
	private ExprNode exprConstArrayElmList() throws VariantException, CompileException, TJSException {
		int token;
		do {
			exprConstArrayElm();
			token = lex();
		} while( token == Token.T_COMMA );
		unlex();
		return null;
	}
	// const_inline_array
	private ExprNode exprConstInlineArray() throws VariantException, CompileException, TJSException {
		ExprNode node = mInterCodeGenerator.makeNP0( Token.T_CONSTVAL );
		Dispatch2 dsp = TJS.createArrayObject();
		node.setValue( new Variant(dsp,dsp) );
		mInterCodeGenerator.pushCurrentNode( node );
		exprConstArrayElmList();
		node = mInterCodeGenerator.getCurrentNode();
		mInterCodeGenerator.popCurrentNode();
		int token = lex();
		if( token == Token.T_RBRACKET ) {
			return node;
		} else {
			error( Error.ArrayRBRACKETError );
			unlex(); // error
		}
		return null;
	}
	private ExprNode exprConstInlineArrayOrDic() throws CompileException, VariantException, TJSException {
		int token = lex();
		if( token == Token.T_LPARENTHESIS ) {
			token = lex();
			if( token != Token.T_CONST ) {
				error( Error.ConstDicArrayStringError );
				unlex();
			}
			token = lex();
			if( token != Token.T_RPARENTHESIS ) {
				error( Error.ConstDicArrayStringError );
				unlex();
			}
			token = lex();
			if( token == Token.T_PERCENT ) { // may be dic
				token = lex();
				if( token == Token.T_LBRACKET ) {
					return exprConstInlineDic();
				} else {
					error( Error.ConstDicLBRACKETError );
					unlex();
				}
			} else if( token == Token.T_LBRACKET ) { // may be array
				return exprConstInlineArray();
			} else {
				error( Error.ConstArrayLBRACKETError );
				unlex();
			}
		} else if( token == Token.T_CAST_CONST ) {
			token = lex();
			if( token == Token.T_PERCENT ) { // may be dic
				token = lex();
				if( token == Token.T_LBRACKET ) {
					return exprConstInlineDic();
				} else {
					error( Error.ConstDicLBRACKETError );
					unlex();
				}
			} else if( token == Token.T_LBRACKET ) { // may be array
				return exprConstInlineArray();
			} else {
				error( Error.ConstArrayLBRACKETError );
				unlex();
			}
		}
		return null;
	}
	// dic_dummy_elm_opt
	/*
	private ExprNode exprDicDummyElmOpt() throws CompileException {
		int token = lex();
		if( token == Token.T_RBRACKET ) {
			unlex();
		} else if( token != Token.T_COMMA ) {
			unlex();	// syntax error
			throw new CompileException(Error.DicDelimiterError);
		}
		return null;
	}
	*/
	// dic_elm
	private ExprNode exprDicElm() throws VariantException, CompileException, TJSException {
		ExprNode node = null;
		int nodeCount = mInterCodeGenerator.getNodeToDeleteVectorCount();
		ExprNode node0 = exprExprNoComma();
		int token = lex();
		if( token == Token.T_COMMA ) {
			node = exprExprNoComma();
			return mInterCodeGenerator.makeNP2(Token.T_DICELM,node0,node);
		} else if( token == Token.T_COLON ) {
			int curNodeCount = mInterCodeGenerator.getNodeToDeleteVectorCount();
			if( nodeCount == (curNodeCount-1) ) {
				if( node0.getOpecode() == Token.T_SYMBOL ) {
					node0.setOpecode(Token.T_CONSTVAL);
					node = exprExprNoComma();
					return mInterCodeGenerator.makeNP2(Token.T_DICELM,node0,node);
				}
			}
			error( Error.DicError );
			unlex(); // error
		} else {
			error( Error.DicDelimiterError );
			unlex(); // error
		}
		return null;
	}
	// dic_elm_list
	private ExprNode exprDicElmList() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_RBRACKET ) {
			unlex();
		} else {
			unlex();
			ExprNode node = exprDicElm();
			mInterCodeGenerator.getCurrentNode().add( node );
			token = lex();
			while( token == Token.T_COMMA ) {
				token = lex();
				if( token == Token.T_RBRACKET ) {
					break;
				}
				unlex();
				node = exprDicElm();
				mInterCodeGenerator.getCurrentNode().add( node );
				token = lex();
			}
			unlex();
		}
		return null;
	}
	// inline_dic
	private ExprNode exprInlineDic() throws CompileException, VariantException, TJSException {
		int token = lex();
		if( token == Token.T_PERCENT ) {
			token = lex();
			if( token == Token.T_LBRACKET ) {
				ExprNode node = mInterCodeGenerator.makeNP0(Token.T_INLINEDIC);
				mInterCodeGenerator.pushCurrentNode(node);
				exprDicElmList();
//				exprDicDummyElmOpt();
				token = lex();
				if( token == Token.T_RBRACKET ) {
					node = mInterCodeGenerator.getCurrentNode();
					mInterCodeGenerator.popCurrentNode();
					return node;
				} else {
					error( Error.DicRBRACKETError );
					unlex(); // error
				}
			} else {
				error( Error.DicLBRACKETError );
				unlex();
			}
		} else {
			unlex();
		}
		return null;
	}
	// array_elm
	private ExprNode exprArrayElm() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_COMMA || token == Token.T_RBRACKET ) {
			unlex();
			return mInterCodeGenerator.makeNP1(Token.T_ARRAYARG,null);
		} else {
			unlex();
			ExprNode node = exprExprNoComma();
			return mInterCodeGenerator.makeNP1(Token.T_ARRAYARG,node);
		}
	}
	// array_elm_list
	private ExprNode exprArrayElmList() throws VariantException, CompileException, TJSException {
		int token;
		do {
			ExprNode node = exprArrayElm();
			mInterCodeGenerator.getCurrentNode().add( node );
			token = lex();
		} while( token == Token.T_COMMA );
		unlex();
		return null;
	}
	// inline_array
	private ExprNode exprInlineArray() throws CompileException, VariantException, TJSException {
		int token = lex();
		if( token == Token.T_LBRACKET ) {
			ExprNode node = mInterCodeGenerator.makeNP0(Token.T_INLINEARRAY);
			mInterCodeGenerator.pushCurrentNode(node);
			exprArrayElmList();
			token = lex();
			if( token == Token.T_RBRACKET ) {
				node = mInterCodeGenerator.getCurrentNode();
				mInterCodeGenerator.popCurrentNode();
				return node;
			} else {
				error( Error.ArrayRBRACKETError );
				unlex(); // error
			}
		} else {
			unlex();
		}
		return null;
	}
	// call_arg
	private ExprNode exprCallArg() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_RPARENTHESIS ) {
			unlex(); // empty
			return null;
//		} else if( token == Token.T_ASTERISK ) {
//			return mInterCodeGenerator.makeNP1( Token.T_EXPANDARG, null );
		} else {
			unlex();
			ExprNode node = exprExprNoComma();
			if( node != null ) {
				token = lex();
				if( token == Token.T_ASTERISK ) {
					return mInterCodeGenerator.makeNP1( Token.T_EXPANDARG, node );
				} else if( token == Token.T_ASTERISK_RPARENTHESIS ) {
					unlex( Token.T_RPARENTHESIS, 0 );
					return mInterCodeGenerator.makeNP1( Token.T_EXPANDARG, node );
				} else if( token == Token.T_ASTERISK_COMMA ) {
					unlex( Token.T_COMMA, 0 );
					return mInterCodeGenerator.makeNP1( Token.T_EXPANDARG, node );
				} else {
					unlex();
				}
			} else {
				token = lex();
				if( token == Token.T_ASTERISK ) {
					return mInterCodeGenerator.makeNP1( Token.T_EXPANDARG, null );
				} else if( token == Token.T_ASTERISK_RPARENTHESIS ) {
					unlex( Token.T_RPARENTHESIS, 0 );
					return mInterCodeGenerator.makeNP1( Token.T_EXPANDARG, null );
				} else {
					unlex();
				}
			}
			return node;
			/*
			ExprNode node = exprMulDivExpr();
			if( node != null ) {
				token = lex();
				if( token == Token.T_ASTERISK ) {
					return mInterCodeGenerator.makeNP1( Token.T_EXPANDARG, node );
				} else {
					unlex();
				}
			}
			return exprExprNoComma();
			*/
		}
	}
	private ExprNode exprCallArgList2( ExprNode node ) throws VariantException, CompileException, TJSException {
		int token;
		node = mInterCodeGenerator.makeNP1( Token.T_ARG, node );
		do {
			ExprNode n2 = exprCallArg();
			node = mInterCodeGenerator.makeNP2( Token.T_ARG, n2, node );
			token = lex();
		} while( token == Token.T_COMMA );
		unlex();
		return node;
	}
	// call_arg_list
	private ExprNode exprCallArgList() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_OMIT ) {
			return mInterCodeGenerator.makeNP0( Token.T_OMIT );
		} else {
			unlex();
			ExprNode node = exprCallArg();
			token = lex();
			if( token == Token.T_COMMA ) {
				return exprCallArgList2( node );
			} else {
				unlex();
				return mInterCodeGenerator.makeNP1( Token.T_ARG, node );
			}
		}
	}
	// func_call_expr
	private ExprNode exprFuncCallExpr( ExprNode node ) throws VariantException, CompileException, TJSException {
		boolean newExpression = false;
		if( node  == null ) {
			node = exprPriorityExpr();
			newExpression = true;
		}
		if( node != null && newExpression == false ) {
			int token = lex();
			if( token != Token.T_LPARENTHESIS ) {
				error( Error.NotFoundFuncCallLPARENTHESISError );
				unlex();
			}
			ExprNode n2 = exprCallArgList();
			token = lex();
			if( token != Token.T_RPARENTHESIS ) {
				error( Error.NotFoundFuncCallRPARENTHESISError );
				unlex();
			}
			node = mInterCodeGenerator.makeNP2( Token.T_LPARENTHESIS, node, n2 );
		}
		return node;
	}
	// factor_expr
	private ExprNode exprFactorExpr() throws CompileException, VariantException, TJSException {
		int token = lex();
		ExprNode node = null;
		switch( token ) {
		case Token.T_CONSTVAL:
			node = mInterCodeGenerator.makeNP0(Token.T_CONSTVAL);
			node.setValue( mLexicalAnalyzer.getValue(mValue) );
			return node;
		case Token.T_SYMBOL:
			node = mInterCodeGenerator.makeNP0(Token.T_SYMBOL );
			node.setValue( new Variant(mLexicalAnalyzer.getString(mValue)) );
			return node;
		case Token.T_THIS:
			return mInterCodeGenerator.makeNP0( Token.T_THIS );
		case Token.T_SUPER:
			return mInterCodeGenerator.makeNP0( Token.T_SUPER );
		case Token.T_FUNCTION:
			unlex();
			return exprFuncExprDef();
		case Token.T_GLOBAL:
			return mInterCodeGenerator.makeNP0( Token.T_GLOBAL );
		case Token.T_VOID:
			return mInterCodeGenerator.makeNP0( Token.T_VOID );
		case Token.T_LBRACKET: // [
			unlex();
			return exprInlineArray();
		case Token.T_PERCENT: // %
			unlex();
			return exprInlineDic();
		case Token.T_CAST_CONST: // (const)
			unlex();
			return exprConstInlineArrayOrDic();
		case Token.T_LPARENTHESIS: // (
			token = lex();
			if( token == Token.T_CONST ) {
				token = lex();
				if( token != Token.T_RPARENTHESIS ) {
					error( Error.NotFoundRPARENTHESISError );
					unlex(); // syntax error
				}
				unlex( Token.T_CAST_CONST, 0 );
				return exprConstInlineArrayOrDic();
			} else {
				unlex();
				mNode = expr();
				token = lex();
				if( token != Token.T_RPARENTHESIS ) {
					error( Error.NotFoundRPARENTHESISError );
					unlex(); // syntax error
				}
				unlex( Token.T_CAST_EXPR, 0 );
				return null;
			}
		case Token.T_SLASHEQUAL:	// /=
			mLexicalAnalyzer.setStartOfRegExp();
			token = lex();
			if( token == Token.T_REGEXP ) {
				node = mInterCodeGenerator.makeNP0( Token.T_REGEXP );
				node.setValue( mLexicalAnalyzer.getValue(mValue) );
				return node;
			} else {
				// 正規表現がない
				error( Error.NotFoundRegexError );
				unlex();
			}
			break;
		case Token.T_SLASH:	// /
			mLexicalAnalyzer.setStartOfRegExp();
			token = lex();
			if( token == Token.T_REGEXP ) {
				node = mInterCodeGenerator.makeNP0( Token.T_REGEXP );
				node.setValue( mLexicalAnalyzer.getValue(mValue) );
				return node;
			} else {
				// 正規表現がない
				error( Error.NotFoundRegexError );
				unlex();
			}
			break;
		}
		unlex();
		return null;
	}
	// priority_expr'
	private ExprNode exprPriorityExpr1() throws CompileException, VariantException, TJSException {
		ExprNode node = exprFactorExpr();
		if( node == null ) {
			int token = lex();
			if( token == Token.T_CAST_EXPR ) { // (expr)
				node = mNode;
				mNode = null;
				return node;
			} else if( token == Token.T_DOT ) {
				mLexicalAnalyzer.setNextIsBareWord();
				token = lex();
				if( token == Token.T_SYMBOL ) {
					ExprNode n2 = mInterCodeGenerator.makeNP0(Token.T_CONSTVAL);
					n2.setValue( mLexicalAnalyzer.getValue(mValue) );
					return mInterCodeGenerator.makeNP1( Token.T_WITHDOT, n2 );
				} else {
					error( Error.NotFoundSymbolAfterDotError );
					unlex();
				}
			} else {
				unlex();
			}
		}
		return node;
	}
	// priority_expr
	private ExprNode exprPriorityExpr() throws CompileException, VariantException, TJSException {
		ExprNode node = exprPriorityExpr1();
		//if( node != null ) {
		if( node != null ) {
			int token = lex();
			while( token == Token.T_LBRACKET || token == Token.T_DOT || token == Token.T_INCREMENT || token == Token.T_DECREMENT ||
					token == Token.T_EXCRAMATION || token == Token.T_LPARENTHESIS ) {
				switch(token) {
				case Token.T_LBRACKET: { // [
					ExprNode n2 = expr();
					token = lex();
					if( token == Token.T_RBRACKET ) { // ]
						node = mInterCodeGenerator.makeNP2( Token.T_LBRACKET, node, n2 );
					} else {
						// ] がない
						error( Error.NotFoundDicOrArrayRBRACKETError );
						unlex();
					}
					break;
				}
				case Token.T_DOT: // .
					mLexicalAnalyzer.setNextIsBareWord();
					token = lex();
					if( token == Token.T_SYMBOL ) {
						ExprNode n2 = mInterCodeGenerator.makeNP0( Token.T_CONSTVAL );
						n2.setValue( mLexicalAnalyzer.getValue(mValue) );
						node = mInterCodeGenerator.makeNP2( Token.T_DOT, node, n2 );
					} else {
						error( Error.NotFoundSymbolAfterDotError );
						unlex();
					}
					break;
				case Token.T_INCREMENT: // ++
					node = mInterCodeGenerator.makeNP1( Token.T_POSTINCREMENT, node );
					break;
				case Token.T_DECREMENT:
					node = mInterCodeGenerator.makeNP1( Token.T_POSTDECREMENT, node );
					break;
				case Token.T_EXCRAMATION: // !
					node = mInterCodeGenerator.makeNP1( Token.T_EVAL, node );
					break;
				case Token.T_LPARENTHESIS: // (
					unlex();
					node = exprFuncCallExpr( node );
					break;
				}
				token = lex();
			}
			unlex();
			/*
			token = lex();
			if( token == Token.T_DOT ) {
				ExprNode tmp = exprPriorityExpr();
				if( tmp != null ) node = tmp;
			} else if( token == Token.T_LPARENTHESIS ) {
				unlex();
				node = exprFuncCallExpr( node );
			} else {
				unlex();
			}
			*/
		}
		return node;
	}
	// incontextof_expr
	private ExprNode exprIncontextOfExpr() throws CompileException, VariantException, TJSException {
		ExprNode node = exprPriorityExpr();
		int token = lex();
		if( token == Token.T_INCONTEXTOF ) {
			ExprNode n2 = exprIncontextOfExpr();
			return mInterCodeGenerator.makeNP2( Token.T_INCONTEXTOF, node, n2 );
		} else {
			unlex();
			return node;
		}
	}
	// unary_expr
	private ExprNode exprUnaryExpr() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_LPARENTHESIS ) { // ( の時、先読みしてトークンを切り替える
			token = lex();
			switch( token ) {
			case Token.T_INT:
				token = lex();
				if( token != Token.T_RPARENTHESIS ) {
					unlex();
					ExprNode n1 = exprUnaryExpr();
					ExprNode retnode = mInterCodeGenerator.makeNP1( Token.T_INT, n1 );
					token = lex();
					if( token != Token.T_RPARENTHESIS ) {
						error( Error.NotFoundRPARENTHESISError );
						unlex(); // syntax error
					}
					return retnode;
				} else {
					unlex( Token.T_CAST_INT, 0 );
				}
				break;
			case Token.T_REAL:
				token = lex();
				if( token != Token.T_RPARENTHESIS ) {
					unlex();
					ExprNode n1 = exprUnaryExpr();
					ExprNode retnode = mInterCodeGenerator.makeNP1( Token.T_REAL, n1 );
					token = lex();
					if( token != Token.T_RPARENTHESIS ) {
						error( Error.NotFoundRPARENTHESISError );
						unlex(); // syntax error
					}
					return retnode;
				} else {
					unlex( Token.T_CAST_REAL, 0 );
				}
				break;
			case Token.T_STRING:
				token = lex();
				if( token != Token.T_RPARENTHESIS ) {
					unlex();
					ExprNode n1 = exprUnaryExpr();
					ExprNode retnode = mInterCodeGenerator.makeNP1( Token.T_STRING, n1 );
					token = lex();
					if( token != Token.T_RPARENTHESIS ) {
						error( Error.NotFoundRPARENTHESISError );
						unlex(); // syntax error
					}
					return retnode;
				} else {
					unlex( Token.T_CAST_STRING, 0 );
				}
				break;
			case Token.T_CONST:
				token = lex();
				if( token != Token.T_RPARENTHESIS ) {
					error( Error.NotFoundRPARENTHESISError );
					unlex(); // syntax error
				}
				unlex( Token.T_CAST_CONST, 0 );
				break;
			default:
				unlex();
				mNode = expr();
				token = lex();
				if( token != Token.T_RPARENTHESIS ) {
					error( Error.NotFoundRPARENTHESISError );
					unlex(); // syntax error
				}
				unlex( Token.T_CAST_EXPR, 0 );
				break;
			}
		} else {
			unlex();
		}
		ExprNode node = exprIncontextOfExpr();
		if( node == null ) {
			token = lex();
			switch( token ) {
			case Token.T_EXCRAMATION: // !
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_EXCRAMATION, node );
			case Token.T_TILDE: // ~
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_TILDE, node );
			case Token.T_DECREMENT: // --
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_DECREMENT, node );
			case Token.T_INCREMENT: // ++
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_INCREMENT, node );
			case Token.T_NEW:
				node = exprFuncCallExpr(null);
				if( node != null ) node.setOpecode( Token.T_NEW );
				return node;
			case Token.T_INVALIDATE:
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_INVALIDATE, node );
			case Token.T_ISVALID:
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_ISVALID, node );
			case Token.T_DELETE:
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_DELETE, node );
			case Token.T_TYPEOF:
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_TYPEOF, node );
			case Token.T_SHARP: // #
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_SHARP, node );
			case Token.T_DOLLAR: // $
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_DOLLAR, node );
			case Token.T_PLUS: // +
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_UPLUS, node );
			case Token.T_MINUS: // -
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_UMINUS, node );
			case Token.T_AMPERSAND: // &
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_IGNOREPROP, node );
			case Token.T_ASTERISK: // *
				node = exprUnaryExpr();
				if( node == null ) {
					unlex( Token.T_ASTERISK_RPARENTHESIS, 0 );
					return null;
				} else {
					return mInterCodeGenerator.makeNP1( Token.T_PROPACCESS, node );
				}
			case Token.T_CAST_INT: // (int)
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_INT, node );
			case Token.T_CAST_REAL: // (real)
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_REAL, node );
			case Token.T_CAST_STRING: // (string)
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_STRING, node );
			case Token.T_INT:
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_INT, node );
			case Token.T_REAL:
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_REAL, node );
			case Token.T_STRING:
				node = exprUnaryExpr();
				return mInterCodeGenerator.makeNP1( Token.T_STRING, node );
			default:
				unlex();
				break;
			}
		} else {
			token = lex();
			switch( token ) {
			case Token.T_ISVALID:
				return mInterCodeGenerator.makeNP1( Token.T_ISVALID, node );
			case Token.T_INSTANCEOF: {
				ExprNode n2 = exprUnaryExpr();
				return mInterCodeGenerator.makeNP2( Token.T_INSTANCEOF, node, n2 );
			}
			default:
				unlex();
				break;
			}
		}
		return node;
	}
	// mul_div_expr, mul_div_expr_and_asterisk
	private ExprNode exprMulDivExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprUnaryExpr();
		if( node != null ) {
			int token = lex();
			while( token == Token.T_PERCENT || token == Token.T_SLASH || token == Token.T_BACKSLASH || token == Token.T_ASTERISK ) {
				ExprNode n2 = exprUnaryExpr();
				if( n2 == null ) {
					token = lex();
					if( token == Token.T_RPARENTHESIS ) {
						unlex( Token.T_ASTERISK_RPARENTHESIS, 0 );
						return node;
					} else if( token == Token.T_COMMA ) {
						unlex( Token.T_ASTERISK_COMMA, 0 );
						return node;
					} else {
						error( Error.NotFoundAsteriskAfterError );
					}
					break;
				}
				node = mInterCodeGenerator.makeNP2( token, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// add_sub_expr
	private ExprNode exprAddSubExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprMulDivExpr();
		if( node != null ) {
			int token = lex();
			while( token == Token.T_PLUS || token == Token.T_MINUS ) {
				ExprNode n2 = exprMulDivExpr();
				node = mInterCodeGenerator.makeNP2( token, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// shift_expr
	private ExprNode exprShiftExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprAddSubExpr();
		if( node != null ) {
			int token = lex();
			ExprNode n2;
			while( token == Token.T_RARITHSHIFT || token == Token.T_LARITHSHIFT || token == Token.T_RBITSHIFT ) {
				n2 = exprAddSubExpr();
				node = mInterCodeGenerator.makeNP2( token, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// compare_expr
	private ExprNode exprCompareExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprShiftExpr();
		if( node != null ) {
			int token = lex();
			while( token == Token.T_LT || token == Token.T_GT || token == Token.T_LTOREQUAL || token == Token.T_GTOREQUAL ) {
				ExprNode n2 = exprShiftExpr();
				node = mInterCodeGenerator.makeNP2( token, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// identical_expr
	private ExprNode exprIdenticalExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprCompareExpr();
		if( node != null ) {
			int token = lex();
			while( token == Token.T_NOTEQUAL || token == Token.T_EQUALEQUAL || token == Token.T_DISCNOTEQUAL | token == Token.T_DISCEQUAL ) {
				ExprNode n2 = exprCompareExpr();
				node = mInterCodeGenerator.makeNP2( token, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// and_expr
	private ExprNode exprAndExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprIdenticalExpr();
		if( node != null ) {
			int token = lex();
			ExprNode n2;
			while( token == Token.T_AMPERSAND ) { // &
				n2 = exprIdenticalExpr();
				node = mInterCodeGenerator.makeNP2( Token.T_AMPERSAND, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// exclusive_or_expr
	private ExprNode exprExclusiveOrExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprAndExpr();
		if( node != null ) {
			int token = lex();
			ExprNode n2;
			while( token == Token.T_CHEVRON ) { // ^
				n2 = exprAndExpr();
				node = mInterCodeGenerator.makeNP2( Token.T_CHEVRON, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// inclusive_or_expr
	private ExprNode exprInclusiveOrExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprExclusiveOrExpr();
		if( node != null ) {
			int token = lex();
			ExprNode n2;
			while( token == Token.T_VERTLINE ) { // |
				n2 = exprExclusiveOrExpr();
				node = mInterCodeGenerator.makeNP2( Token.T_VERTLINE, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// logical_and_expr
	private ExprNode exprLogicalAndExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprInclusiveOrExpr();
		if( node != null ) {
			int token = lex();
			ExprNode n2;
			while( token == Token.T_LOGICALAND ) { // &&
				n2 = exprInclusiveOrExpr();
				node = mInterCodeGenerator.makeNP2( Token.T_LOGICALAND, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// logical_or_expr
	private ExprNode exprLogicalOrExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprLogicalAndExpr();
		if( node != null ) {
			int token = lex();
			ExprNode n2;
			while( token == Token.T_LOGICALOR ) { // ||
				n2 = exprLogicalAndExpr();
				node = mInterCodeGenerator.makeNP2( Token.T_LOGICALOR, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// cond_expr
	private ExprNode exprCondExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprLogicalOrExpr();
		if( node != null ) {
			int token = lex();
			while( token == Token.T_QUESTION ) { // ?
				ExprNode n2 = exprCondExpr();
				token = lex();
				if( token != Token.T_COLON ) {
					error( Error.NotFound3ColonError );
					unlex();
				}
				ExprNode n3 = exprCondExpr();
				node = mInterCodeGenerator.makeNP3( Token.T_QUESTION, node, n2, n3 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// assign_expr
	private ExprNode exprAssignExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprCondExpr();
		if( node != null ) {
			int token = lex();
			while( token == Token.T_SWAP || token == Token.T_EQUAL || token == Token.T_AMPERSANDEQUAL
				|| token == Token.T_VERTLINEEQUAL || token == Token.T_CHEVRONEQUAL || token == Token.T_MINUSEQUAL
				|| token == Token.T_PLUSEQUAL || token == Token.T_PERCENTEQUAL || token == Token.T_SLASHEQUAL
				|| token == Token.T_BACKSLASHEQUAL || token == Token.T_ASTERISKEQUAL || token == Token.T_LOGICALOREQUAL
				|| token == Token.T_LOGICALANDEQUAL || token == Token.T_RARITHSHIFTEQUAL || token == Token.T_LARITHSHIFTEQUAL
				|| token == Token.T_RBITSHIFTEQUAL ) {
				ExprNode n2 = exprAssignExpr();
				node = mInterCodeGenerator.makeNP2( token, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	//  comma_expr
	private ExprNode exprCommaExpr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprAssignExpr();
		if( node != null ) {
			int token = lex();
			while( token == Token.T_COMMA ) {
				ExprNode n2 = exprAssignExpr();
				node = mInterCodeGenerator.makeNP2( token, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
	}
	// expr
	private ExprNode expr() throws VariantException, CompileException, TJSException {
		ExprNode node = exprCommaExpr();
		if( node != null ) {
			int token = lex();
			while( token == Token.T_IF ) {
				ExprNode n2 = expr();
				node = mInterCodeGenerator.makeNP2( token, node, n2 );
				token = lex();
			}
			unlex();
		}
		return node;
/*
		int token = lex();
		if( token == Token.T_IF ) {
			ExprNode n2 = expr();
			return mInterCodeGenerator.makeNP2( Token.T_IF, node, n2 );
		} else {
			unlex();
			return node;
		}
*/
	}
	// expr_no_comma
	private ExprNode exprExprNoComma() throws VariantException, CompileException, TJSException {
		return exprAssignExpr();
	}
	// throw
	private ExprNode exprThrow() throws VariantException, CompileException, TJSException {
		if( LOGD ) Logger.log("throw");
		// throw は消化済み
		ExprNode node = expr();
		int token = lex();
		if( token == Token.T_SEMICOLON ) {
			mInterCodeGenerator.processThrowCode( node );
		} else {
			error( Error.NotFoundSemicolonAfterThrowError );
			unlex();
		}
		return null;
	}
	// catch
	private ExprNode exprCatch() throws CompileException, VariantException {
		if( LOGD ) Logger.log("catch");
		int token = lex();
		if( token == Token.T_CATCH ) {
			token = lex();
			if( token == Token.T_LPARENTHESIS ) {	// (
				token = lex();
				if( token == Token.T_RPARENTHESIS ) {	// )
					mInterCodeGenerator.enterCatchCode(null);
				} else if( token == Token.T_SYMBOL ) {
					int value = mValue;
					token = lex();
					if( token != Token.T_RPARENTHESIS ) {	// )
						error( Error.NotFoundRPARENTHESISAfterCatchError );
						unlex();
					}
					mInterCodeGenerator.enterCatchCode(mLexicalAnalyzer.getString(value));
				} else {
					error( Error.NotFoundRPARENTHESISAfterCatchError );
					unlex();
				}
			} else {
				unlex();
				mInterCodeGenerator.enterCatchCode(null);
			}
		} else {
			error( Error.NotFoundCatchError );
			unlex();
		}
		return null;
	}
	// try
	private ExprNode exprTry() throws CompileException, VariantException, TJSException {
		if( LOGD ) Logger.log("try");
		// try は消化済み
		mInterCodeGenerator.enterTryCode();
		exprBlockOrStatment();
		exprCatch();
		exprBlockOrStatment();
		mInterCodeGenerator.exitTryCode();
		return null;
	}
	// case
	private ExprNode exprCase() throws VariantException, CompileException, TJSException {
		if( LOGD ) Logger.log("case");
		int token = lex();
		if( token == Token.T_CASE ) {
			ExprNode node = expr();
			token = lex();
			if( token != Token.T_COLON ) {
				error( Error.NotFoundCaseColonError );
				unlex();
			}
			mInterCodeGenerator.processCaseCode(node);
		} else if( token == Token.T_DEFAULT ) {
			token = lex();
			if( token != Token.T_COLON ) {
				error( Error.NotFoundDefaultColonError );
				unlex();
			}
			mInterCodeGenerator.processCaseCode(null);
		} else {
			error( Error.NotFoundCaseOrDefaultError );
			unlex();	// ここに来ることはないはず
		}
		return null;
	}
	// with
	private ExprNode exprWith() throws VariantException, CompileException, TJSException {
		if( LOGD ) Logger.log("with");
		// with は消化済み
		int token = lex();
		if( token != Token.T_LPARENTHESIS ) {	// (
			error( Error.NotFoundWithLPARENTHESISError );
			unlex();
		}
		ExprNode node = expr();
		token = lex();
		if( token != Token.T_RPARENTHESIS ) {	// )
			error( Error.NotFoundWithRPARENTHESISError );
			unlex();
		}
		mInterCodeGenerator.enterWithCode(node);
		exprBlockOrStatment();
		mInterCodeGenerator.exitWidthCode();
		return null;
	}
	// switch
	private ExprNode exprSwitch() throws VariantException, CompileException, TJSException {
		if( LOGD ) Logger.log("switch");
		// switch は消化済み
		int token = lex();
		if( token != Token.T_LPARENTHESIS ) {	// (
			error( Error.NotFoundSwitchLPARENTHESISError );
			unlex();
		}
		ExprNode node = expr();
		token = lex();
		if( token != Token.T_RPARENTHESIS ) {	// )
			error( Error.NotFoundSwitchRPARENTHESISError );
			unlex();
		}
		mInterCodeGenerator.enterSwitchCode(node);
		exprBlock();
		mInterCodeGenerator.exitSwitchCode();
		return null;
	}
	// a return statement.
	private ExprNode exprReturn() throws VariantException, CompileException, TJSException {
		if( LOGD ) Logger.log("return");
		// return は消化済み
		int token = lex();
		if( token == Token.T_SEMICOLON ) {
			mInterCodeGenerator.returnFromFunc(null);
		} else {
			unlex();
			ExprNode node = expr();
			token = lex();
			if( token != Token.T_SEMICOLON ) {
				error( Error.NotFoundSemicolonAfterReturnError );
				unlex();
			}
			mInterCodeGenerator.returnFromFunc(node);
		}
		return null;
	}
	// extends_list, extends_name
	private ExprNode exprExtendsList() throws VariantException, CompileException, TJSException {
		if( LOGD ) Logger.log("extends");
		ExprNode node = exprExprNoComma();
		mInterCodeGenerator.createExtendsExprCode( node, false );
		int token = lex();
		if( token == Token.T_COMMA ) {
			exprExtendsList();
		} else {
			unlex();
		}
		return null;
	}
	// class_extender
	private ExprNode exprClassExtender() throws VariantException, CompileException, TJSException {
		ExprNode node = exprExprNoComma();
		int token = lex();
		if( token == Token.T_COMMA ) {
			mInterCodeGenerator.createExtendsExprCode(node,false);
			exprExtendsList();
		} else {
			mInterCodeGenerator.createExtendsExprCode(node,true);
			unlex();
		}
		return null;
	}
	// class_def
	private ExprNode exprClassDef() throws TJSException, CompileException, VariantException {
		int token = lex();
		if( token == Token.T_CLASS ) {
			token = lex();
			if( token != Token.T_SYMBOL ) {
				error( Error.NotFoundSymbolAfterClassError );
				unlex();
			}
			pushContextStack( mLexicalAnalyzer.getString(mValue), ContextType.CLASS );
			if( LOGD ) Logger.log(TAG, "Class:"+mLexicalAnalyzer.getString(mValue));
			token = lex();
			if( token == Token.T_EXTENDS ) {
				exprClassExtender();
			} else {
				unlex();
			}
			exprBlock();
			popContextStack();
		} else {
			unlex();
		}
		return null;
	}
	// property_handler_getter
	private ExprNode exprPropertyGetter() throws CompileException, TJSException, VariantException {
		int token = lex();
		if( token == Token.T_LPARENTHESIS ) {	// (
			token = lex();
			if( token != Token.T_RPARENTHESIS ) {	// )
				error( Error.NotFoundPropGetRPARENTHESISError );
				unlex();
			}
		} else {
			unlex();
		}
		pushContextStack( "(getter)", ContextType.PROPERTY_GETTER );
		mInterCodeGenerator.enterBlock();
		exprBlock();
		mInterCodeGenerator.exitBlock();
		popContextStack();
		return null;
	}
	// property_handler_setter
	private ExprNode exprPropertySetter() throws CompileException, TJSException, VariantException {
		int token = lex();
		if( token != Token.T_LPARENTHESIS ) {	// (
			error( Error.NotFoundPropSetLPARENTHESISError );
			unlex();
		}
		token = lex();
		if( token != Token.T_SYMBOL ) {
			error( Error.NotFoundPropSetSymbolError );
			unlex();
		}
		int value = mValue;

		token = lex();
		if( token != Token.T_RPARENTHESIS ) {	// )
			error( Error.NotFoundPropSetRPARENTHESISError );
			unlex();
		}
		pushContextStack( "(setter)", ContextType.PROPERTY_SETTER );
		mInterCodeGenerator.enterBlock();
		mInterCodeGenerator.setPropertyDeclArg( mLexicalAnalyzer.getString(value) );
		exprBlock();
		mInterCodeGenerator.exitBlock();
		popContextStack();
		return null;
	}

	// property_handler_def_list
	private ExprNode exprPropertyHandlerDefList() throws CompileException, TJSException, VariantException {
		int token = lex();
		if( token == Token.T_SETTER ) {
			exprPropertySetter();
			token = lex();
			if( token == Token.T_GETTER ) {
				exprPropertyGetter();
			} else {
				unlex();
			}
		} else if( token == Token.T_GETTER ) {
			exprPropertyGetter();
			token = lex();
			if( token == Token.T_SETTER ) {
				exprPropertySetter();
			} else {
				unlex();
			}
		} else {
			error( Error.NotFoundPropError );
			unlex();
		}
		return null;
	}

	// property_def
	private ExprNode exprPropertyDef() throws CompileException, TJSException, VariantException {
		int token = lex();
		if( token == Token.T_PROPERTY ) {
			token = lex();
			if( token != Token.T_SYMBOL ) {
				error( Error.NotFoundSymbolAfterPropError );
				unlex();
			}
			int value = mValue;
			token = lex();
			if( token != Token.T_LBRACE ) {
				error( Error.NotFoundLBRACEAfterPropError );
				unlex();
			}
			pushContextStack( mLexicalAnalyzer.getString(value), ContextType.PROPERTY );
			if( LOGD ) Logger.log("Property: " + mLexicalAnalyzer.getString(value) );
			exprPropertyHandlerDefList();
			token = lex();
			if( token != Token.T_RBRACE) {
				error( Error.NotFoundRBRACEAfterPropError );
				unlex();
			}
			popContextStack();
		} else {
			unlex();
		}
		return null;
	}

	// func_decl_arg_collapse, func_decl_arg, func_decl_arg_at_least_one, func_decl_arg_list
	// exprFuncDeclArgs にまとめてしまってる
	/*
	private ExprNode exprFuncDeclArg() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_SYMBOL ) {
			int value = mValue;
			token = lex();
			if( token == Token.T_EQUAL ) {
				ExprNode node = exprExprNoComma();
				mInterCodeGenerator.addFunctionDeclArg( mLexicalAnalyzer.getString(value), node );
			} else if( token == Token.T_ASTERISK ) {
				mInterCodeGenerator.addFunctionDeclArgCollapse( mLexicalAnalyzer.getString(value) );
			} else {
				unlex();
				mInterCodeGenerator.addFunctionDeclArg( mLexicalAnalyzer.getString(value), null );
			}
		} else {
			unlex();
		}
		return null;
	}
	*/
	// func_decl_arg_opt +
	private ExprNode exprFuncDeclArgs() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_SYMBOL ) {
			int value = mValue;
			token = lex();
			if( token == Token.T_EQUAL ) {	// symbol = ???
				ExprNode node = exprExprNoComma();
				mInterCodeGenerator.addFunctionDeclArg( mLexicalAnalyzer.getString(value), node );
				token = lex();
				if( token == Token.T_COMMA ) {
					exprFuncDeclArgs();
				} else {
					unlex();
				}
			} else if( token == Token.T_ASTERISK ) {	// symbol *
				mInterCodeGenerator.addFunctionDeclArgCollapse( mLexicalAnalyzer.getString(value) );
			} else {	// symbol
				mInterCodeGenerator.addFunctionDeclArg( mLexicalAnalyzer.getString(value), null );
				if( token == Token.T_COMMA ) {
					exprFuncDeclArgs();
				} else {
					unlex();
				}
			}
		} else if( token == Token.T_ASTERISK ) {
			mInterCodeGenerator.addFunctionDeclArgCollapse( null );
		} else {
			unlex();
		}
		return null;
	}
	// func_decl_arg_opt
	private ExprNode exprFuncDeclArgOpt() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_LPARENTHESIS ) {
			exprFuncDeclArgs();
			token = lex();
			if( token != Token.T_RPARENTHESIS ) {	// )
				error( Error.NotFoundFuncDeclRPARENTHESISError );
				unlex();
			}
		} else {	// empty
			unlex();
		}
		return null;
	}
	// func_def
	private ExprNode exprFunctionDef() throws CompileException, TJSException, VariantException {
		int token = lex();
		if( token == Token.T_FUNCTION ) {
			token = lex();
			if( token != Token.T_SYMBOL ) {
				error( Error.NotFoundFuncDeclSymbolError );
				unlex();
			}
			pushContextStack( mLexicalAnalyzer.getString(mValue), ContextType.FUNCTION );
			if( LOGD ) Logger.log(TAG, "Function:"+mLexicalAnalyzer.getString(mValue));
			mInterCodeGenerator.enterBlock();
			exprFuncDeclArgOpt();
			exprBlock();
			mInterCodeGenerator.exitBlock();
			popContextStack();
		} else {
			unlex(); // ここに来ることはないはず
			throw new TJSException( Error.InternalError );
		}
		return null;
	}
	// func_expr_def
	private ExprNode exprFuncExprDef() throws TJSException, VariantException, CompileException {
		ExprNode node = null;
		int token = lex();
		if( token == Token.T_FUNCTION ) {
			pushContextStack( "(anonymous)", ContextType.EXPR_FUNCTION );
			mInterCodeGenerator.enterBlock();
			exprFuncDeclArgOpt();
			exprBlock();
			mInterCodeGenerator.exitBlock();
			Variant v = new Variant(mInterCodeGenerator);
			popContextStack();
			node = mInterCodeGenerator.makeNP0( Token.T_CONSTVAL );
			node.setValue( v );
		} else {
			unlex();
			throw new TJSException( Error.InternalError );
		}
		return node;
	}
	// variable_id
	private ExprNode exprVariableId() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_SYMBOL ) {
			int value = mValue;
			token = lex();
			if( token == Token.T_EQUAL ) {
				ExprNode node = exprExprNoComma();
				mInterCodeGenerator.initLocalVariable( mLexicalAnalyzer.getString(value), node );
				if( LOGD ) Logger.log( "var name:"+mLexicalAnalyzer.getString(value) + "=xx");
			} else {
				unlex();
				mInterCodeGenerator.addLocalVariable( mLexicalAnalyzer.getString(value) );
				if( LOGD ) Logger.log( "var name:"+ mLexicalAnalyzer.getString(value) );
			}
		} else {
			error( Error.NotFoundSymbolAfterVarError );
			unlex();
		}
		return null;
	}
	// variable_id_list
	private ExprNode exprVariableIdList() throws VariantException, CompileException, TJSException {
		exprVariableId();
		int token = lex();
		if( token == Token.T_COMMA ) {
			exprVariableIdList();
		} else {
			unlex();
		}
		return null;
	}
	// variable_def_inner
	private ExprNode exprVariableDefInner() throws VariantException, CompileException, TJSException {	// 現在のバージョンではconstを明確に区別してない
		int token = lex();
		if( token == Token.T_VAR ) {
			if( LOGD ) Logger.log("var ");
			exprVariableIdList();
		} else if( token == Token.T_CONST ) {
			if( LOGD ) Logger.log("const ");
			exprVariableIdList();
		} else {
			unlex();
			throw new TJSException( Error.InternalError );
		}
		return null;
	}
	// variable_def
	private ExprNode exprVariableDef() throws VariantException, CompileException, TJSException {
		exprVariableDefInner();
		int token = lex();
		if( token != Token.T_SEMICOLON ) {
			error( Error.NotFoundVarSemicolonError );
			unlex();
		}
		return null;
	}
	// for_third_clause
	private ExprNode exprForThridClause() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_RPARENTHESIS ) {
			unlex();
			mInterCodeGenerator.setForThirdExprCode( null );
		} else {
			unlex();
			ExprNode node = expr();
			mInterCodeGenerator.setForThirdExprCode( node );
		}
		return null;
	}
	// for_second_clause
	private ExprNode exprForSecondClause() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_SEMICOLON ) {
			unlex();
			mInterCodeGenerator.createForExprCode(null);
		} else {
			unlex();
			ExprNode node = expr();
			mInterCodeGenerator.createForExprCode(node);
		}
		return null;
	}
	// for_first_clause
	private ExprNode exprForFirstClause() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_VAR || token == Token.T_CONST ) {
			unlex();
			mInterCodeGenerator.enterForCode( true );
			exprVariableDefInner();
		} else if( token == Token.T_SEMICOLON ) {
			unlex();
			mInterCodeGenerator.enterForCode( false );
		} else {
			unlex();
			ExprNode node = expr();
			mInterCodeGenerator.enterForCode( false );
			mInterCodeGenerator.createExprCode( node );
		}
		return null;
	}
	// for
	private ExprNode exprFor() throws VariantException, CompileException, TJSException {
		if( LOGD ) Logger.log("for");
		int token = lex();
		if( token != Token.T_LPARENTHESIS ) {	// (
			error( Error.NotFoundForLPARENTHESISError );
			unlex();
		}
		exprForFirstClause();
		token = lex();
		if( token != Token.T_SEMICOLON ) {
			error( Error.NotFoundForSemicolonError );
			unlex();
		}
		exprForSecondClause();
		token = lex();
		if( token != Token.T_SEMICOLON ) {
			error( Error.NotFoundForSemicolonError );
			unlex();
		}
		exprForThridClause();
		token = lex();
		if( token != Token.T_RPARENTHESIS ) {
			error( Error.NotFoundForRPARENTHESISError );
			unlex();
		}
		exprBlockOrStatment();
		mInterCodeGenerator.exitForCode();
		return null;
	}
	// if, if_else
	private ExprNode exprIf() throws VariantException, CompileException, TJSException {
		if( LOGD ) Logger.log("if");
		int token = lex();
		if( token != Token.T_LPARENTHESIS ) {	// (
			error( Error.NotFoundIfLPARENTHESISError );
			unlex();
		}
		mInterCodeGenerator.enterIfCode();
		ExprNode node = expr();
		mInterCodeGenerator.crateIfExprCode(node);
		token = lex();
		if( token != Token.T_RPARENTHESIS ) {	// )
			error( Error.NotFoundIfRPARENTHESISError );
			unlex();
		}
		exprBlockOrStatment();
		mInterCodeGenerator.exitIfCode();
		token = lex();
		if( token == Token.T_ELSE ) {
			mInterCodeGenerator.enterElseCode();
			exprBlockOrStatment();
			mInterCodeGenerator.exitElseCode();
		} else {
			unlex();
		}
		return null;
	}
	// do_while
	private ExprNode exprDo() throws CompileException, VariantException, TJSException {
		if( LOGD ) Logger.log("do-while");
		mInterCodeGenerator.enterWhileCode(true);
		exprBlockOrStatment();
		int token = lex();
		if( token != Token.T_WHILE ) {
			error( Error.NotFoundDoWhileError);
			unlex();
		}
		token = lex();
		if( token != Token.T_LPARENTHESIS ) {	// (
			error( Error.NotFoundDoWhileLPARENTHESISError );
			unlex();
		}
		ExprNode node = expr();
		token = lex();
		if( token != Token.T_RPARENTHESIS ) {	// )
			error( Error.NotFoundDoWhileRPARENTHESISError );
			unlex();
		}
		mInterCodeGenerator.createWhileExprCode(node,true);
		token = lex();
		if( token != Token.T_SEMICOLON ) {	// ;
			error( Error.NotFoundDoWhileSemicolonError );
			unlex();
		}
		mInterCodeGenerator.exitWhileCode(true);
		return null;
	}
	// while
	private ExprNode exprWhile() throws CompileException, VariantException, TJSException {
		if( LOGD ) Logger.log("while");
		mInterCodeGenerator.enterWhileCode(false);
		int token = lex();
		if( token != Token.T_LPARENTHESIS ) {	// (
			error( Error.NotFoundWhileLPARENTHESISError );
			unlex();
		}
		ExprNode node = expr();
		token = lex();
		if( token != Token.T_RPARENTHESIS ) {	// )
			error( Error.NotFoundWhileRPARENTHESISError );
			unlex();
		}
		mInterCodeGenerator.createWhileExprCode( node, false );
		exprBlockOrStatment();
		mInterCodeGenerator.exitWhileCode(false);
		return null;
	}
	// block
	private ExprNode exprBlock() throws VariantException, CompileException, TJSException {
		if( LOGD ) Logger.log("block{}");
		int token = lex();
		if( token != Token.T_LBRACE ) {
			error( Error.NotFoundLBRACEAfterBlockError );
			unlex(); // error
		}
		mInterCodeGenerator.enterBlock();
		exprDefList();
		mInterCodeGenerator.exitBlock();
		token = lex();
		if( token != Token.T_RBRACE ) {
			error( Error.NotFoundRBRACEAfterBlockError );
			unlex(); // error
		}
		return null;
	}
	// statement
	private ExprNode exprStatement() throws VariantException, CompileException, TJSException {
		int token = lex();
		ExprNode node = null;
		switch( token ) {
		case Token.T_IF:	// if or if else
			node = exprIf();
			break;
		case Token.T_WHILE:
			node = exprWhile();
			break;
		case Token.T_DO:
			node = exprDo();
			break;
		case Token.T_FOR:
			node = exprFor();
			break;
		case Token.T_BREAK:
			token = lex();
			if( token != Token.T_SEMICOLON ) {
				error( Error.NotFoundBreakSemicolonError );
				unlex();
			}
			mInterCodeGenerator.doBreak();
			break;
		case Token.T_CONTINUE:
			token = lex();
			if( token != Token.T_SEMICOLON ) {
				error( Error.NotFoundContinueSemicolonError );
				unlex();
			}
			mInterCodeGenerator.doContinue();
			break;
		case Token.T_DEBUGGER:
			token = lex();
			if( token != Token.T_SEMICOLON ) {
				error( Error.NotFoundBebuggerSemicolonError );
				unlex();
			}
			mInterCodeGenerator.doDebugger();
			break;
		case Token.T_VAR:
		case Token.T_CONST:
			unlex();
			node = exprVariableDef();
			break;
		case Token.T_FUNCTION:
			unlex();
			node = exprFunctionDef();
			break;
		case Token.T_PROPERTY:
			unlex();
			node = exprPropertyDef();
			break;
		case Token.T_CLASS:
			unlex();
			node = exprClassDef();
			break;
		case Token.T_RETURN:
			node = exprReturn();
			break;
		case Token.T_SWITCH:
			node = exprSwitch();
			break;
		case Token.T_WITH:
			node = exprWith();
			break;
		case Token.T_CASE:
		case Token.T_DEFAULT:
			unlex();
			node = exprCase();
			break;
		case Token.T_TRY:
			node = exprTry();
			break;
		case Token.T_THROW:
			node = exprThrow();
			break;
		case Token.T_SEMICOLON:
			// ignore
			break;
		default:
			unlex();
			node = expr();
			token = lex();
			if( token != Token.T_SEMICOLON ) {
				error( Error.NotFoundSemicolonOrTokenTypeError );
				unlex();
			}
			mInterCodeGenerator.createExprCode( node );
			break;
		}
		return node;
	}
	// block_or_statement
	private ExprNode exprBlockOrStatment() throws VariantException, CompileException, TJSException {
		int token = lex();
		if( token == Token.T_LBRACE ) {	// block expression
			mInterCodeGenerator.enterBlock();
			exprDefList();
			token = lex();
			if( token != Token.T_RBRACE ) {
				error( Error.NotFoundBlockRBRACEError );
				unlex();
			}
			mInterCodeGenerator.exitBlock();
		} else {
			unlex();
			exprStatement();
		}
		return null;
	}
	// def_list
	private ExprNode exprDefList() throws VariantException, CompileException, TJSException {
		int token = lex();
		while( token > 0 && token != Token.T_RBRACE ) {
			unlex();
			exprBlockOrStatment();
			token = lex();
		}
		unlex();
		return null;
	}
	// program, global_list
	private void program() throws VariantException, CompileException, TJSException {
		pushContextStack( "global", ContextType.TOP_LEVEL );
		int token;
		do {
			exprDefList();
			token = lex();
			if( token > 0 ) {
				error( Error.EndOfBlockError );
				unlex();
			}
		} while( token > 0 );
		popContextStack();
	}

	public TJS getTJS() {
		return mOwner;
	}

	public String getName() { return mName; }
	public void setName( String name, int lineofs ) {
		mName = null;
		if( name != null ) {
			mLineOffset = lineofs;
			mName = new String(name);
		}
	}
	public int getLineOffset() { return mLineOffset; }

	public ScriptBlock doCompile( String text, boolean isexpression, boolean isresultneeded ) throws CompileException, VariantException, TJSException {
		if( text == null ) return null;
		if( text.length() == 0 ) return null;

		mScript = text;
		// ラインリスト生成はここで行わない

		parse( text, isexpression, isresultneeded );

		// InterCodeObject を生成する
		ScriptBlock ret;
		ret = generateInterCodeObjects();
		return ret;
	}

	private static final int
		MEMBERENSURE		= 0x00000200, // create a member if not exists
		IGNOREPROP			= 0x00000800; // ignore property invoking

	private ScriptBlock generateInterCodeObjects() throws VariantException, TJSException {
		// dumpClassStructure();
		ScriptBlock block = new ScriptBlock( mOwner, mName, mLineOffset, mScript, mLineData );

		mInterCodeObjectList.clear();
		// 1st. pass, まずはInterCodeObjectを作る
		final int count = mInterCodeGeneratorList.size();
		for( int i = 0; i < count; i++ ) {
			InterCodeGenerator gen = mInterCodeGeneratorList.get(i);
			mInterCodeObjectList.add( gen.creteCodeObject(block) );
		}
		Variant val = new Variant();
		// 2nd. pass, 次にInterCodeObject内のリンクを解決する
		for( int i = 0; i < count; i++ ) {
			InterCodeGenerator gen = mInterCodeGeneratorList.get(i);
			InterCodeObject obj = mInterCodeObjectList.get(i);
			gen.createSecond( obj );
			gen.dateReplace( this ); // DaraArray の中の InterCodeGenerator を InterCodeObject に差し替える
			//obj.dateReplace( this ); // DaraArray の中の InterCodeGenerator を InterCodeObject に差し替える

			ArrayList<InterCodeGenerator.Property> p = gen.getProp();
			if( p != null ) {
				final int pcount = p.size();
				for( int j = 0; j < pcount; j++ ) {
					InterCodeGenerator.Property prop = p.get(j);
					val.set( getCodeObject(getCodeIndex(prop.Value)) );
					obj.mParent.propSet( MEMBERENSURE|IGNOREPROP, prop.Name, val, obj.mParent );
				}
				p.clear();
			}
		}
		mTopLevelObject = getCodeObject(getCodeIndex(mTopLevelGenerator));
		block.setObjects(mTopLevelObject, mInterCodeObjectList);

		// 解放してしまう
		mInterCodeGenerator = null;
		mTopLevelGenerator = null;
		mGeneratorStack = null;
		mInterCodeGeneratorList.clear();
		mInterCodeGeneratorList = null;
		mInterCodeObjectList.clear();
		mInterCodeObjectList = null;
		return block;
	}

	public String getLineDescriptionString(int pos) {
		// get short description, like "mainwindow.tjs(321)"
		// pos is in character count from the first of the script
		StringBuilder builer = new StringBuilder(512);
		int line = srcPosToLine(pos)+1;
		if( mName != null ) {
			builer.append( mName );
		} else {
			builer.append("anonymous@");
			builer.append( toString() );
		}
		builer.append('(');
		builer.append( String.valueOf(line) );
		builer.append(')');
		return builer.toString();
	}

	public int lineToSrcPos(int line) {
		// assumes line is added by LineOffset
		line -= mLineOffset;
		return mLineData.getLineToSrcPos(line);
	}

	public String getLine(int line ) {
		// note that this function DOES matter LineOffset
		line -= mLineOffset;
		return mLineData.getLine(line);
	}

	public void compile( String text, boolean isexpression, boolean isresultneeded, BinaryStream output ) throws CompileException, VariantException, TJSException {
		if( text == null ) return;
		if( text.length() == 0 ) return;

		mScript = text;

		parse( text, isexpression, isresultneeded );

		// ここでバイトコード出力する
		//if( mName != null && mName.endsWith(".tjs") ) {
		//	String filename = mName.substring(0,mName.length()-4) + ".tjb";
		//	exportByteCode(filename);
		//}
		exportByteCode(output);
	}
	public void toJavaCode( String text, boolean isexpression, boolean isresultneeded ) throws CompileException, VariantException, TJSException {
		if( text == null ) return;
		if( text.length() == 0 ) return;

		mScript = text;

		parse( text, isexpression, isresultneeded );

		ArrayList<JavaCodeIntermediate> clazz = new ArrayList<JavaCodeIntermediate>();
		final int count = mInterCodeGeneratorList.size();
		for( int i = 0; i < count; i++ ) {
			InterCodeGenerator v = mInterCodeGeneratorList.get(i);
			if( v != null ) {
				int type = v.getContextType();
				ArrayList<String> codes;
				switch(type) {
					case ContextType.TOP_LEVEL:
						break;
					case ContextType.FUNCTION: {
						InterCodeGenerator parent = v.getParent();
						if( parent.getContextType() == ContextType.CLASS ) {
							final String parentName = parent.getName();
							codes = v.toJavaCode( 0, 0 );
							final int ccount = clazz.size();
							boolean inserted = false;
							for( int j = 0; j < ccount; j++ ) {
								JavaCodeIntermediate ci = clazz.get(j);
								if( ci.getName().equals(parentName) ) {
									ci.addMember(new JavaCodeIntermediate.ClosureCode(v.getName(),type,codes) );
									inserted = true;
									break;
								}
							}
							if( inserted == false ) {
								JavaCodeIntermediate ci = new JavaCodeIntermediate(parentName);
								ci.addMember(new JavaCodeIntermediate.ClosureCode(v.getName(),type,codes) );
								clazz.add(ci);
							}
						}
						break;
					}
					case ContextType.EXPR_FUNCTION:
						break;
					case ContextType.PROPERTY: {
						InterCodeGenerator parent = v.getParent();
						if( parent.getContextType() == ContextType.CLASS ) {
							final String parentName = parent.getName();
							final int ccount = clazz.size();
							boolean inserted = false;
							for( int j = 0; j < ccount; j++ ) {
								JavaCodeIntermediate ci = clazz.get(j);
								if( ci.getName().equals(parentName) ) {
									ci.addProperty(v.getName(),new JavaCodeIntermediate.Property(v.getName()) );
									inserted = true;
									break;
								}
							}
							if( inserted == false ) {
								JavaCodeIntermediate ci = new JavaCodeIntermediate(parentName);
								ci.addProperty(v.getName(),new JavaCodeIntermediate.Property(v.getName()) );
								clazz.add(ci);
							}
						}
						break;
					}
					case ContextType.PROPERTY_SETTER:
					case ContextType.PROPERTY_GETTER: {
						InterCodeGenerator parent = v.getParent();
						if( parent.getContextType() == ContextType.PROPERTY ) {
							InterCodeGenerator parentparent = parent.getParent();
							if( parentparent.getContextType() == ContextType.CLASS ) {
								final String propName = parent.getName();
								final String className = parentparent.getName();
								final int ccount = clazz.size();
								boolean inserted = false;
								JavaCodeIntermediate target = null;
								for( int j = 0; j < ccount; j++ ) {
									JavaCodeIntermediate ci = clazz.get(j);
									if( ci.getName().equals(className) ) {
										target = ci;
										inserted = true;
										break;
									}
								}
								if( inserted == false ) {
									JavaCodeIntermediate ci = new JavaCodeIntermediate(className);
									target = ci;
									clazz.add(ci);
								}
								JavaCodeIntermediate.Property prop = target.getProperty(propName);
								if( prop == null ) {
									prop = new JavaCodeIntermediate.Property(propName);
									target.addProperty( propName, prop );
								}
								codes = v.toJavaCode( 0, 0 );
								if( type == ContextType.PROPERTY_SETTER ) {
									prop.setSetter(codes);
								} else {
									prop.setGetter(codes);
								}
							}
						}
						break;
					}
					case ContextType.CLASS: {
						JavaCodeIntermediate ci = new JavaCodeIntermediate(v.getName());
						codes = v.toJavaCode( 0, 0 );
						ci.setInitializer( codes );
						clazz.add(ci);
						break;
					}
					case ContextType.SUPER_CLASS_GETTER:
						break;
				}
			}
		}
		final int ccount = clazz.size();
		for( int j = 0; j < ccount; j++ ) {
			JavaCodeIntermediate ci = clazz.get(j);
			ci.write();
		}
	}
	private static final int FILE_TAG_SIZE = 8;
	private static final int TAG_SIZE = 4;
	private static final int CHUNK_SIZE_LEN = 4;
	public static final byte[] FILE_TAG = { 'T', 'J', 'S', '2', '1', '0', '0', 0 };
	private void exportByteCode( BinaryStream output ) throws TJSException {
		byte[] filetag = FILE_TAG;
		byte[] codetag = { 'T', 'J', 'S', '2' };
		byte[] objtag = { 'O', 'B', 'J', 'S' };
		byte[] datatag = { 'D', 'A', 'T', 'A' };

		final int count = mInterCodeGeneratorList.size();
		ArrayList<ByteBuffer> objarray = new ArrayList<ByteBuffer>(count*2);
		ConstArrayData constarray = new ConstArrayData();
		int objsize = 0;
		for( int i = 0; i < count; i++ ) {
			InterCodeGenerator obj = mInterCodeGeneratorList.get(i);
			ByteBuffer buf = obj.exportByteCode(this,constarray);
			objarray.add(buf);
			objsize += buf.capacity() + TAG_SIZE + CHUNK_SIZE_LEN; // tag + size
		}
		objsize += TAG_SIZE + CHUNK_SIZE_LEN + 4 + 4; // OBJS tag + size + toplevel + count
		ByteBuffer dataarea = constarray.exportBuffer();
		int datasize = dataarea.capacity() + TAG_SIZE + CHUNK_SIZE_LEN; // DATA tag + size
		int filesize = objsize + datasize + FILE_TAG_SIZE + CHUNK_SIZE_LEN; // TJS2 tag + file size
		byte[] filesizearray = { (byte) (filesize&0xff), (byte) ((filesize>>>8)&0xff), (byte) ((filesize>>>16)&0xff), (byte) ((filesize>>>24)&0xff) };
		byte[] datasizearray = { (byte) (datasize&0xff), (byte) ((datasize>>>8)&0xff), (byte) ((datasize>>>16)&0xff), (byte) ((datasize>>>24)&0xff) };
		byte[] objsizearray = { (byte) (objsize&0xff), (byte) ((objsize>>>8)&0xff), (byte) ((objsize>>>16)&0xff), (byte) ((objsize>>>24)&0xff) };
		byte[] objcountarray = { (byte) (count&0xff), (byte) ((count>>>8)&0xff), (byte) ((count>>>16)&0xff), (byte) ((count>>>24)&0xff) };

		int toplevel = -1;
		if( mTopLevelGenerator != null ) {
			toplevel = getCodeIndex( mTopLevelGenerator );
		}
		byte[] toparray = { (byte) (toplevel&0xff), (byte) ((toplevel>>>8)&0xff), (byte) ((toplevel>>>16)&0xff), (byte) ((toplevel>>>24)&0xff) };
		output.write(filetag);
		output.write(filesizearray);
		output.write(datatag);
		output.write(datasizearray);
		output.write(dataarea);
		output.write(objtag);
		output.write(objsizearray);
		output.write(toparray);
		output.write(objcountarray);
		for( int i = 0; i < count; i++ ) {
			ByteBuffer buf = objarray.get(i);
			int size = buf.capacity();
			byte[] bufsizearray = { (byte) (size&0xff), (byte) ((size>>>8)&0xff), (byte) ((size>>>16)&0xff), (byte) ((size>>>24)&0xff) };
			output.write(codetag);
			output.write(bufsizearray);
			output.write(buf);
		}
		output.close();
		output = null;
		objarray.clear();
		objarray = null;
		constarray = null;
		dataarea = null;
	}
	/*
	public boolean isReusable() {
		return getContextCount() == 1 && mTopLevelObject != null && !mUsingPreProcessor;
	}

	public int getContextCount() {
		return mInterCodeObjectList.size();
	}
	*/

	public void add( InterCodeGenerator gen ) {
		mInterCodeGeneratorList.add( gen );
	}
	public void remove( InterCodeGenerator gen ) {
		mInterCodeGeneratorList.remove( gen );
	}

	// for generat code
	/**
	 * 位置を確定するために使う
	 */
	public int getCodeIndex( InterCodeGenerator gen ) {
		return mInterCodeGeneratorList.indexOf(gen);
	}
	public int getObjectIndex( InterCodeObject gen ) {
		return mInterCodeObjectList.indexOf(gen);
	}
	public InterCodeObject getCodeObject( int index ) {
		if( index >= 0 && index < mInterCodeObjectList.size() ) {
			return mInterCodeObjectList.get(index);
		} else {
			return null;
		}
	}

	/*
	private void dumpClassStructure() {
		final int count = mInterCodeGeneratorList.size();
		for( int i = 0; i < count; i++ ) {
			InterCodeGenerator gen = mInterCodeGeneratorList.get(i);
			if( gen.isClass() ) {
				gen.dumpClassStructure(0);
			}
		}
	}
	*/

	/*
	public String getNameInfo() {
		if( mLineOffset == 0 ) {
			return new String(mName);
		} else {
			return mName + "(line +" + String.valueOf(mLineOffset) + ")";
		}
	}

	public int getTotalVMCodeSize() {
		int size = 0;
		final int count = mInterCodeObjectList.size();
		for( int i = 0; i < count; i++ ) {
			size += mInterCodeObjectList.get(i).getCodeSize();
		}
		return size;
	}

	public int getTotalVMDataSize() {
		int size = 0;
		final int count = mInterCodeObjectList.size();
		for( int i = 0; i < count; i++ ) {
			size += mInterCodeObjectList.get(i).getDataSize();
		}
		return size;
	}
	*/

	public String getScript() {
		return mScript;
	}
	public final int getMaxLine() {
		return mLineData.getMaxLine();
	}
	@Override
	public int codePosToSrcPos(int codepos) {
		return 0; // allways 0, 基本的に使われない
	}
}
