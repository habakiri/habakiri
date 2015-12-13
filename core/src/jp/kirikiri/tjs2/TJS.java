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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class TJS {
	//private static final String TAG ="TJS";

	public static final int VERSION_MAJOR   = 2;
	public static final int VERSION_MINOR   = 4;
	public static final int VERSION_RELEASE = 28;
	public static final int VERSION_HEX = VERSION_MAJOR * 0x1000000 + VERSION_MINOR * 0x10000 + VERSION_RELEASE;

	private static final int ENV_WINDOWS = 0; // プリプロセッサでは未定義の時この値が入る
	private static final int ENV_ANDROID = 1;
	private static final int ENV_JAVA_APPLICATION = 2;
	private static final int ENV_JAVA_APPLET = 3;

	private static final int GLOBAL_HASH_BITS = 7;
	private static final int MEMBERENSURE = 0x00000200; // create a member if not exists

	private static GlobalStringMap mGlobalStringMap;
	private static ArrayList<String> mNativeClassNames;
	private static ConsoleOutput mConsoleOutput;
	private static MessageMapper mMessageMapper;
	public static boolean mWarnOnNonGlobalEvalOperator;
	public static boolean mUnaryAsteriskIgnoresPropAccess;
	public static boolean mEvalOperatorIsOnGlobal;
	public static boolean EnableDebugMode;
	public static boolean IsTarminating;

	public static boolean IsLowMemory;
	public static StorageInterface mStorage;
	private static Dispatch2 mArrayClass;
	private static Dispatch2 mDictionayClass;
	//private static VariantPool mVAPool;
	public static Variant[] NULL_ARG;

	private HashMap<String,Integer>	mPPValues;
	private ArrayList<WeakReference<ScriptBlock>> mScriptBlocks;
	private CustomObject mGlobal;
	private ScriptCache mCache;

	private static ArrayList<Dispatch> mFinalizeList;
	//public static int mCompactVariantArrayMagic;
	//public static VariantArrayStack mVariantArrayStack;

	// static 関係はここで初期化
	public static void initialize() {
		// mStorage = null; // 事前に設定されるので、ここで初期化するのはまずい
		mFinalizeList = new ArrayList<Dispatch>();

		NULL_ARG = new Variant[0];
		IsTarminating = false;
		mWarnOnNonGlobalEvalOperator = false;
		mUnaryAsteriskIgnoresPropAccess = false;
		mNativeClassNames = new ArrayList<String>();
		mGlobalStringMap = new GlobalStringMap();
		//mCompactVariantArrayMagic = 0;
		//mVariantArrayStack = new VariantArrayStack();
		mEvalOperatorIsOnGlobal = false;
		EnableDebugMode = true;
		mConsoleOutput = null;

		mMessageMapper = new MessageMapper();

		RandomGeneratorNI.setRandomBits128(null);
		//ArrayNI.register();

		//mVAPool = new VariantPool();

		CompileState.mEnableDicFuncQuickHack = true;
		Variant.initialize();
		DictionaryObject.initialize();
		ArrayObject.initialize();
		ByteCodeLoader.initialize();
		CustomObject.initialize();
		MathClass.initialize();
		LexicalAnalyzer.initialize();
		try {
			mArrayClass = new ArrayClass();
			mDictionayClass = new DictionaryClass();
		} catch (VariantException e) {
		} catch (TJSException e) {
		}
	}
	public static void finalizeApplication() {
		if( mFinalizeList != null ) {
			int count = mFinalizeList.size();
			for( int i = 0; i < count; i++ ) {
				Dispatch dsp = mFinalizeList.get(i);
				dsp.doFinalize();
			}
			mFinalizeList.clear();
			mFinalizeList = null;
		}

		mGlobalStringMap = null;
		if( mNativeClassNames != null ) mNativeClassNames.clear();
		mNativeClassNames = null;

		mConsoleOutput = null;
		mMessageMapper = null;
		mStorage = null;
		mArrayClass = null;
		mDictionayClass = null;
		//mVAPool = null;
		NULL_ARG = null;
		ArrayObject.finalizeApplication();
		ByteCodeLoader.finalizeApplication();
		CustomObject.finalizeApplication();
		DictionaryObject.finalizeApplication();
		MathClass.finalizeApplication();
		Variant.finalizeApplication();
		LexicalAnalyzer.finalizeApplication();
	}
	/**
	 * 定期的にコールしてオブジェクトを消す事
	 */
	public static void doObjectFinalize() {
		if( mFinalizeList != null ) {
			System.runFinalization();
			final int count = mFinalizeList.size();
			if( count > 0 ) {
				for( int i = 0; i < count; i++ ) {
					Dispatch dsp = mFinalizeList.get(i);
					dsp.doFinalize();
				}
				mFinalizeList.clear();
			}
		}
	}
	public static void pushObject( Dispatch obj ) {
		if( mFinalizeList != null ) {
			mFinalizeList.add(obj);
		}
	}


	public TJS() throws VariantException, TJSException {
		// create script cache object
		mCache = new ScriptCache(this);

		mPPValues = new HashMap<String,Integer>();
		setPPValue( "version", VERSION_HEX );
		setPPValue( "environment", ENV_JAVA_APPLICATION ); // TODO 適切な値を入れる
		setPPValue( "compatibleSystem", 1 ); // 互換システム true

		mGlobal = new CustomObject(GLOBAL_HASH_BITS);

		mScriptBlocks = new ArrayList<WeakReference<ScriptBlock>>();

		Dispatch2 dsp;
		Variant val;

		// Array
		//dsp = new ArrayClass();
		dsp = mArrayClass;
		val = new Variant(dsp,null);
		mGlobal.propSet(MEMBERENSURE, "Array", val, mGlobal);

		// Dictionary
		//dsp = new DictionaryClass();
		dsp = mDictionayClass;
		val = new Variant(dsp, null);
		mGlobal.propSet(MEMBERENSURE, "Dictionary", val, mGlobal );

		// Date
		dsp = new DateClass();
		val = new Variant(dsp, null);
		mGlobal.propSet( MEMBERENSURE, "Date", val, mGlobal);

		// Math
		{
			Dispatch2 math;

			dsp = math = new MathClass();
			val = new Variant(dsp, null);
			mGlobal.propSet( MEMBERENSURE, "Math", val, mGlobal );

			// Math.RandomGenerator
			dsp = new RandomGeneratorClass();
			val = new Variant(dsp, null);
			math.propSet( MEMBERENSURE, "RandomGenerator", val, math );
		}
		// Exception
		dsp = new ExceptionClass();
		val = new Variant(dsp, null);
		mGlobal.propSet(MEMBERENSURE, "Exception", val, mGlobal );

		// RegExp
		dsp = new RegExpClass();
		val = new Variant(dsp,null);
		mGlobal.propSet(MEMBERENSURE, "RegExp", val, mGlobal);
	}
	public void setPPValue( final String name, int value ) {
		if( name != null ) {
			mPPValues.put( name, Integer.valueOf(value) );
		}
	}
	public int getPPValue( final String name ) {
		Integer ret = mPPValues.get(name);
		if( ret == null ) {
			return 0;
		}
		return ret.intValue();
	}
	public static void outputExceptionToConsole( String msg ) { outputToConsole(msg); }
	public static void outputToConsole(String mes) {
		if( mConsoleOutput == null ) {
			Logger.log( mes );
		} else {
			mConsoleOutput.print(mes);
		}
	}
	public static void outputToConsoleWithCentering( final String msg, int width ) {
		// this function does not matter whether msg includes ZENKAKU characters ...
		if( msg == null ) return;
		int len = msg.length();
		int ns = (width - len)/2;
		if( ns <= 0 ) {
			outputToConsole(msg);
		} else {
			StringBuilder builder = new StringBuilder(ns + len +1);
			while( (ns--) > 0 ) {
				builder.append(' ');
			}
			builder.append(msg);
			outputToConsole(builder.toString());
			builder = null;
		}
	}
	public static void outputToConsoleSeparator(String text, int count) {
		int len = text.length();
		StringBuilder builder = new StringBuilder(len*count);
		while( count > 0 ) {
			builder.append(text);
			count--;
		}
		outputToConsole(builder.toString());
	}
	public static String mapGlobalStringMap( final String str ) {
		return mGlobalStringMap.map( str );
	}
	public static int registerNativeClass( final String name ) {
		final int count = mNativeClassNames.size();
		for( int i = 0; i < count; i++ ) {
			if( mNativeClassNames.get(i).equals(name) ) return i;
		}
		mNativeClassNames.add( mapGlobalStringMap(name) );
		return mNativeClassNames.size() - 1;
	}
	public static int findNaitveClassID( final String name ) {
		final int count = mNativeClassNames.size();
		for( int i = 0; i < count; i++ ) {
			if( mNativeClassNames.get(i).equals(name) ) return i;
		}
		return -1;
	}
	public static final String findNativeClassName( int id ) {
		if( id < 0 || id >= mNativeClassNames.size() ) return null;
		return mNativeClassNames.get(id);
	}
	public static void setConsoleOutput( ConsoleOutput console ) {
		mConsoleOutput = console;
	}
	public static ConsoleOutput getConsoleOutput() {
		return mConsoleOutput;
	}
	//public static int getDictionaryClassID() { return DictionaryClass.ClassID; }
	//public static int getArrayClassID() { return ArrayClass.ClassID; }
	/*
	public static void variantArrayStackCompact() {
		mCompactVariantArrayMagic++;
	}
	public static void VariantArrayStackCompactNow() {
		if( mVariantArrayStack != null ) mVariantArrayStack.compact();
	}
	*/

	public void execScript( final String script, Variant result, Dispatch2 context, final String name, int lineofs ) throws VariantException, TJSException, CompileException {
		if( mCache != null ) mCache.execScript( script, result, context, name, lineofs );
	}
	public void evalExpression( final String expression, Variant result, Dispatch2 context, final String name, int lineofs ) throws VariantException, TJSException, CompileException {
		if( mCache != null ) mCache.evalExpression(expression, result, context, name, lineofs);
	}
	public void compileScript( final String script, final String name, int lineofs, boolean isresultneeded, BinaryStream output ) throws CompileException, VariantException, TJSException {
		Compiler compiler = new Compiler(this);
		if( name != null ) compiler.setName(name, lineofs);
		compiler.compile( script, false, isresultneeded, output );
		compiler = null;
	}
	public void toJavaCode( final String script, String name, int lineofs, boolean isresultneeded) throws CompileException, VariantException, TJSException {
		Compiler compiler = new Compiler(this);
		if( name != null ) compiler.setName(name, lineofs);
		compiler.toJavaCode( script, false, isresultneeded );
		compiler = null;
	}
	public void loadByteCode( Variant result, Dispatch2 context, final String name, BinaryStream input ) throws TJSException {
		ByteCodeLoader loader = new ByteCodeLoader();
		ScriptBlock block = loader.readByteCode(this, name, input);
		if( block != null ) {
			block.executeTopLevel(result, context);
			if( block.getContextCount() == 0 ) {
				removeScriptBlock(block);
			}
			block = null;
		}
	}

	public Dispatch2 getGlobal() { return mGlobal; }
	public static Dispatch2 createArrayObject() throws VariantException, TJSException { return createArrayObject(null); }
	public static Dispatch2 createArrayObject( Holder<Dispatch2> classout ) throws VariantException, TJSException {
		if( classout != null ) classout.mValue = mArrayClass;
		Holder<Dispatch2> holder = new Holder<Dispatch2>(null);
		mArrayClass.createNew(0, null, holder, null, mArrayClass );
		return holder.mValue;
	}
	public static Dispatch2 createDictionaryObject() throws VariantException, TJSException { return createDictionaryObject(null); }
	public static Dispatch2 createDictionaryObject( Holder<Dispatch2> classout ) throws VariantException, TJSException {
		if( classout != null ) classout.mValue = mDictionayClass;
		Holder<Dispatch2> holder = new Holder<Dispatch2>(null);
		mDictionayClass.createNew(0, null, holder, null, mDictionayClass );
		return holder.mValue;
	}
	public void addScriptBlock(ScriptBlock block) {
		mScriptBlocks.add(new WeakReference<ScriptBlock>(block));
	}
	public void removeScriptBlock(ScriptBlock block) {
		final int count = mScriptBlocks.size();
		for( int i = 0; i < count; i++ ) {
			if( mScriptBlocks.get(i).get() == block ) {
				mScriptBlocks.remove(i);
				break;
			}
		}
		compactScriptBlock();
	}
	private void compactScriptBlock() {
		// なくなっているオブジェクトを消す
		final int count = mScriptBlocks.size();
		for( int i = count-1; i >= 0; i-- ) {
			if( mScriptBlocks.get(i).get() == null ) {
				mScriptBlocks.remove(i);
			}
		}
	}
	public void dump() throws VariantException { dump(80); }
	// dumps all existing script block
	public void dump( int width ) throws VariantException {

		// dumps all existing script block
		String version = String.format( "TJS version %d.%d.%d", VERSION_MAJOR, VERSION_MINOR, VERSION_RELEASE);

		outputToConsoleSeparator( "#", width);
		outputToConsoleWithCentering( "TJS Context Dump", width);
		outputToConsoleSeparator( "#", width);
		outputToConsole( version );
		outputToConsole( "" );

		// なくなっているオブジェクトを消す
		compactScriptBlock();

		if( mScriptBlocks.size() > 0 ) {
			String buf = String.format( "Total %d script block(s)", mScriptBlocks.size() );
			outputToConsole(buf);
			outputToConsole( "" );

			int totalcontexts = 0;
			int totalcodesize = 0;
			int totaldatasize = 0;
			for( int i = 0; i < mScriptBlocks.size(); i++ ) {
				ScriptBlock b = mScriptBlocks.get(i).get();
				if( b == null ) continue;
				int n;
				final String name = b.getName();

				String title;
				if( name != null )
					title = b.getNameInfo();
				else
					title = "(no-named script block)";

				String ptr = String.format( " 0x%08X", b.hashCode() );

				title += ptr;

				outputToConsole( title );

				n = b.getContextCount();
				totalcontexts += n;
				buf = String.format( "\tCount of contexts      : %d", n );
				outputToConsole(buf);

				n = b.getTotalVMCodeSize();
				totalcodesize += n;
				buf = String.format( "\tVM code area size      : %d words", n);
				outputToConsole(buf);

				n = b.getTotalVMDataSize();
				totaldatasize += n;
				buf = String.format( "\tVM constant data count : %d", n);
				outputToConsole(buf);
				outputToConsole( "" );
			}

			buf = String.format( "Total count of contexts      : %d", totalcontexts);
			outputToConsole(buf);
			buf = String.format( "Total VM code area size      : %d words", totalcodesize);
			outputToConsole(buf);
			buf = String.format( "Total VM constant data count : %d", totaldatasize);
			outputToConsole(buf);
			outputToConsole( "" );


			for( int i = 0; i < mScriptBlocks.size(); i++ ) {
				ScriptBlock b = mScriptBlocks.get(i).get();
				if( b == null ) continue;
				outputToConsoleSeparator( "-", width);
				final String name = b.getName();
				String title;
				if( name != null )
					title = b.getNameInfo();
				else
					title = "(no-named script block)";

				String ptr;
				ptr = String.format( " 0x%08X", b.hashCode() );
				title += ptr;

				outputToConsoleWithCentering(title, width);

				outputToConsoleSeparator( "-", width);

				b.dump();

				outputToConsole( "" );
				outputToConsole( "" );
			}
		} else {
			outputToConsole( "" );
			outputToConsole( "There are no script blocks in the system." );
		}
	}

	static public void registerMessageMap( final String name, MessageMapper.MessageHolder holder ) {
		if( mMessageMapper != null ) mMessageMapper.register(name, holder);
	}
	static public void unregisterMessageMap( final String name ) {
		if( mMessageMapper != null ) mMessageMapper.unregister(name);
	}
	static public boolean assignMessage( final String name, final String newmsg ) {
		if( mMessageMapper != null ) return mMessageMapper.assignMessage(name, newmsg);
		return false;
	}
	static public String createMessageMapString() {
		if( mMessageMapper != null ) return mMessageMapper.createMessageMapString();
		return "";
	}
	static public String getMessageMapMessage( final String name ) {
		if( mMessageMapper != null ) {
			String ret = mMessageMapper.get(name);
			if( ret != null ) return ret;
			return "";
		}
		return "";
	}

	public void shutdown() {
		//variantArrayStackCompactNow();
		if( mGlobal != null ) {
			try {
				mGlobal.invalidate(0, null, mGlobal );
			} catch (VariantException e) {
			} catch (TJSException e) {
			}
			mGlobal.clear();
			mGlobal = null;
		}
		if( mCache != null ) {
			mCache = null;
		}
	}
	//static public Variant allocateVariant() { return mVAPool.allocate(); }
	//static public void releaseVariant( Variant v ) { mVAPool.release(v); }
	//static public void releaseVariant( Variant[] v ) { mVAPool.release(v); }

	public void lexTest( String script ) {
		Compiler compiler = new Compiler(this);
		int token = 0;
		int token1;
		ScriptLineData lineData = new ScriptLineData( script, 0 );
		Lexer lexer = new Lexer(compiler,script,false,false);
		LexicalAnalyzer lex = new LexicalAnalyzer( compiler, script,false,false);
		try {
			do {
				token = lexer.getNext();
				token1 = lex.getNext();
				if( token1 != token ) {
					int oleline = lineData.getSrcPosToLine(lexer.getCurrentPosition());
					int newline = lineData.getSrcPosToLine(lex.getCurrentPosition());
					System.out.print("error line:"+oleline+", "+newline+"\n");
				}
				int v1 = lexer.getValue();
				int v2 = lex.getValue();
				if( v1 != v2 ) {
					int oleline = lineData.getSrcPosToLine(lexer.getCurrentPosition());
					int newline = lineData.getSrcPosToLine(lex.getCurrentPosition());
					System.out.print("error line:"+oleline+", "+newline+"\n");
				}
				if( v1 != 0 ) {
					Object o1 = lexer.getValue(v1).toJavaObject();
					Object o2 = lex.getValue(v2).toJavaObject();
					if( !o1.equals(o2) ) {
						int oleline = lineData.getSrcPosToLine(lexer.getCurrentPosition());
						int newline = lineData.getSrcPosToLine(lex.getCurrentPosition());
						System.out.print("o1:"+o1.toString()+", o2"+o2.toString()+"\n");
						System.out.print("error line:"+oleline+", "+newline+"\n");
					}
				}
			} while( token != 0 );
		} catch (CompileException e) {}


		long start = System.currentTimeMillis();
		lexer = new Lexer(compiler,script,false,false);
		try {
			do {
				token = lexer.getNext();
				lexer.getValue();
			} while( token != 0 );
		} catch (CompileException e) {}
		long time = System.currentTimeMillis() - start;
		System.out.print( "old lex : " + time + "ms\n" );

		/*
		start = System.currentTimeMillis();

		Parser p = new Parser(this);
		try {
			p.doCompile(script, false, false );
		} catch (CompileException e1) {
			e1.printStackTrace();
		} catch (VariantException e1) {
			e1.printStackTrace();
		} catch (TJSException e1) {
			e1.printStackTrace();
		}
		time = System.currentTimeMillis() - start;
		System.out.print( "parser : " + time + "ms\n" );
		*/


		start = System.currentTimeMillis();
		lex = new LexicalAnalyzer( compiler, script,false,false);
		try {
			do {
				token = lex.getNext();
				lex.getValue();
				/*
				int val = lex.getValue();
				switch(token){
				case Token.T_SYMBOL:
					System.out.print( lex.getString(val) + " ");
					break;
				case Token.T_CONSTVAL:
					if( lex.getValue(val) != null ) {
						Object obj = lex.getValue(val).toJavaObject();
						if( obj instanceof String ) {
							System.out.print( "\"" + lex.getValue(val).toJavaObject().toString() + "\" " );
						} else {
							System.out.print( lex.getValue(val).toJavaObject().toString() + " " );
						}
					} else {
						System.out.print( "null(constval) " );
					}
					break;
				default:
					System.out.print( Token.getTokenString(token)+" " );
					break;
				}
				*/
			} while( token != 0 );
		} catch (CompileException e) {}
		time = System.currentTimeMillis() - start;
		System.out.print( "new lex : " + time + "ms\n" );
	}
}

