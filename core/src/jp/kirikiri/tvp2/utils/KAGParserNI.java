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
package jp.kirikiri.tvp2.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.CompileException;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Holder;
import jp.kirikiri.tjs2.IntVector;
import jp.kirikiri.tjs2.IntWrapper;
import jp.kirikiri.tjs2.NativeInstanceObject;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.ScriptsClass;
import jp.kirikiri.tvp2.base.Storage;
import jp.kirikiri.tvp2.msg.Message;

public class KAGParserNI extends NativeInstanceObject {
	/** none is reported */
	//private static final int tkdlNone = 0;
	/** simple report */
	private static final int tkdlSimple = 1;
	/** complete report ( verbose ) */
	private static final int tkdlVerbose = 2;

	/** owner object */
	private WeakReference<Dispatch2> mOwner;

	/** Dictionary.Clear method pointer */
	private Dispatch2 mDicClear;
	/** Dictionary */
	private Dispatch2 mDicAssign;
	/** DictionaryObject */
	private Dispatch2 mDicObj;

	/** Macro Dictionary Object */
	private Dispatch2 mMacros;

	/** Macro arguments */
	private ArrayList<Dispatch2> mMacroArgs;
	private int mMacroArgStackDepth;
	private int mMacroArgStackBase;

	static class CallStackData {
		/** caller storage */
		public String Storage;
		/** caller nearest label */
		public String Label;
		/** line offset from the label */
		public int Offset;
		/** original line string */
		public String OrgLineStr;
		/** line string (if alive) */
		public String LineBuffer;
		public int Pos;
		/** whether LineBuffer is used or not */
		public boolean LineBufferUsing;
		public int MacroArgStackBase;
		public int MacroArgStackDepth;
		public IntVector ExcludeLevelStack;
		public ArrayList<Boolean> IfLevelExecutedStack;
		public int ExcludeLevel;
		public int IfLevel;

		public CallStackData( final String storage, final String label,
			int offset, final String orglinestr, final String linebuffer,
			int pos, boolean linebufferusing, int macroargstackbase,
			int macroargstackdepth,
			final IntVector excludelevelstack, int excludelevel,
			final ArrayList<Boolean> iflevelexecutedstack, int iflevel) {

			Storage = storage;
			Label = label;
			Offset = offset;
			OrgLineStr = orglinestr;
			LineBuffer = linebuffer;
			Pos = pos;
			LineBufferUsing = linebufferusing;
			MacroArgStackBase = macroargstackbase;
			MacroArgStackDepth = macroargstackdepth;
			ExcludeLevelStack = excludelevelstack;
			ExcludeLevel = excludelevel;
			IfLevelExecutedStack = iflevelexecutedstack;
			IfLevel = iflevel;
		}
		@SuppressWarnings("unchecked")
		public CallStackData( final CallStackData ref ) {
			Storage = ref.Storage;
			Label = ref.Label;
			Offset = ref.Offset;
			OrgLineStr = ref.OrgLineStr;
			LineBuffer = ref.LineBuffer;
			Pos = ref.Pos;
			LineBufferUsing = ref.LineBufferUsing;
			MacroArgStackBase = ref.MacroArgStackBase;
			MacroArgStackDepth = ref.MacroArgStackDepth;
			ExcludeLevelStack = ref.ExcludeLevelStack.clone();
			IfLevelExecutedStack = (ArrayList<Boolean>) ref.IfLevelExecutedStack.clone();
			ExcludeLevel = ref.ExcludeLevel;
			IfLevel = ref.IfLevel;
		}
		@SuppressWarnings("unchecked")
		public void copy( CallStackData ref ) {
			Storage = ref.Storage;
			Label = ref.Label;
			Offset = ref.Offset;
			OrgLineStr = ref.OrgLineStr;
			LineBuffer = ref.LineBuffer;
			Pos = ref.Pos;
			LineBufferUsing = ref.LineBufferUsing;
			MacroArgStackBase = ref.MacroArgStackBase;
			MacroArgStackDepth = ref.MacroArgStackDepth;
			ExcludeLevelStack = ref.ExcludeLevelStack.clone();
			IfLevelExecutedStack = (ArrayList<Boolean>) ref.IfLevelExecutedStack.clone();
			ExcludeLevel = ref.ExcludeLevel;
			IfLevel = ref.IfLevel;
		}
	}
	private ArrayList<CallStackData> mCallStack;

	private ScenarioCacheItem mScenario;
	/** is copied from Scenario */
	private String[] mLines;
	/** is copied from Scenario */
	private int mLineCount;

	private String mStorageName;
	private String mStorageShortName;

	/** current processing line */
	private int mCurLine;
	/** current processing position ( column ) */
	private int mCurPos;
	/** current line string */
	private String mCurLineStr;
	/** line buffer ( if any macro/emb was expanded ) */
	private String mLineBuffer;
	/**  */
	private boolean mLineBufferUsing;
	/** Current Label */
	private String mCurLabel;
	/** Current Page Name */
	private String mCurPage;
	/** line number of previous tag */
	private int mTagLine;

	/** debugging log level */
	private int mDebugLevel;
	/** whether to process special tags */
	private boolean mProcessSpecialTags;
	/** CR is not interpreted as [r] tag when this is true */
	private boolean mIgnoreCR;
	/** マクロ記録状態 */
	private boolean mRecordingMacro;
	/** 記録しているマクロの内容 */
	private StringBuilder mRecordingMacroStr;
	/** 記録しているマクロ名 */
	private String mRecordingMacroName;
	/** テンポラリ文字構築 */
	private StringBuilder mWorkBuilder;

	private Variant mValueVariant;

	private int mExcludeLevel;
	private int mIfLevel;

	private IntVector mExcludeLevelStack;
	private ArrayList<Boolean> mIfLevelExecutedStack;

	private boolean mInterrupted;

	private char[] mWorkChar;

	public KAGParserNI() throws VariantException, TJSException {
		// 初期値を代入しているものはコメントアウト
		// mOwner = null;
		// mScenario = null;
		// mLines = null;
		// mCurLineStr = null;
		mProcessSpecialTags = true;
		// mIgnoreCR = false;
		// mDicClear = null;
		// mDicAssign = null;
		// mDicObj = null;
		// mMacros = null;
		// mRecordingMacro = false;
		mDebugLevel = tkdlSimple;
		// mInterrupted = false;
		// mMacroArgStackDepth = 0;
		// mMacroArgStackBase = 0;

		mRecordingMacroStr = new StringBuilder();
		mWorkBuilder = new StringBuilder();
		mIfLevelExecutedStack = new ArrayList<Boolean>();
		mExcludeLevelStack = new IntVector();
		mCallStack = new ArrayList<CallStackData>();
		mMacroArgs = new ArrayList<Dispatch2>();
		mValueVariant = new Variant();

		// retrieve DictClear method and DictObj object
		Holder<Dispatch2> holder = new Holder<Dispatch2>(null);
		mDicObj = TJS.createDictionaryObject(holder);
		mMacros = TJS.createDictionaryObject();
		Dispatch2 dictclass = holder.mValue;
		holder.mValue = null;
		try {
			// retrieve clear method from dictclass
			Variant val = new Variant();
			int er = dictclass.propGet( 0, "clear", val, dictclass );
			if( er < 0 ) Message.throwExceptionMessage(Message.InternalError );
			mDicClear = val.asObject();

			er = dictclass.propGet( 0, "assign", val, dictclass );
			if( er < 0 ) Message.throwExceptionMessage(Message.InternalError );
			mDicAssign = val.asObject();
		} catch( TJSException e ) {
			dictclass = null;
			mDicObj = null;
			mMacros = null;
			mDicClear = null;
			mDicAssign = null;
			throw e;
		}
		dictclass = null;
	}
	public int construct( Variant[] param, Dispatch2 tjsObj ) throws VariantException, TJSException {
		int hr = super.construct( param, tjsObj );
		if( hr < 0 ) return hr;

		mOwner = new WeakReference<Dispatch2>(tjsObj);
		return Error.S_OK;
	}
	// called before destruction
	public void invalidate() throws VariantException, TJSException {
		// invalidate this object

		// release objects
		mDicAssign = null;
		mDicClear = null;
		mDicObj = null;
		mMacros = null;

		clearMacroArgs();
		clearBuffer();
		mOwner.clear();
		super.invalidate();
	}
	@SuppressWarnings("unchecked")
	public void copy( final KAGParserNI ref ) throws VariantException, TJSException {
		// copy Macros
		Variant[] psrc = new Variant[1];
		{
			Variant src = new Variant(ref.mMacros, ref.mMacros);
			psrc[0] = src;
			mDicAssign.funcCall( 0, null, null, psrc, mMacros );
		}

		// copy MacroArgs
		{
			clearMacroArgs();

			for( int i = 0; i < ref.mMacroArgStackDepth; i++ ) {
				Dispatch2 dic = TJS.createDictionaryObject();
				Dispatch2 isrc = ref.mMacroArgs.get(i);
				Variant src = new Variant(isrc, isrc);
				psrc[0] = src;
				mDicAssign.funcCall( 0, null, null, psrc, dic );
				mMacroArgs.add(dic);
			}
			mMacroArgStackDepth = ref.mMacroArgStackDepth;
		}
		mMacroArgStackBase = ref.mMacroArgStackBase;

		// copy CallStack
		int count = ref.mCallStack.size();
		mCallStack = new ArrayList<CallStackData>(count);
		for( int i = 0; i < count; i++ ) {
			mCallStack.add( new CallStackData(ref.mCallStack.get(i)) );
		}

		// copy StorageName, StorageShortName
		mStorageName = ref.mStorageName;
		mStorageShortName = ref.mStorageShortName;


		// copy Scenario
		if( mScenario != ref.mScenario ) {
			if( mScenario != null ) {
				mScenario = null;
				mLines = null;
				mCurLineStr = null;
			}
			mScenario = ref.mScenario;
			mLines = ref.mLines;
			mLineCount = ref.mLineCount;
		}

		// copy CurStorage, CurLine, CurPos
		mCurLine = ref.mCurLine;
		mCurPos = ref.mCurPos;

		// copy CurLineStr, LineBuffer, LineBufferUsing
		mCurLineStr = ref.mCurLineStr;
		mLineBuffer = ref.mLineBuffer;
		mLineBufferUsing = ref.mLineBufferUsing;

		// copy CurLabel, CurPage, TagLine
		mCurLabel = ref.mCurLabel;
		mCurPage = ref.mCurPage;
		mTagLine = ref.mTagLine;

		// copy DebugLebel, IgnoreCR
		mDebugLevel = ref.mDebugLevel;
		mIgnoreCR = ref.mIgnoreCR;

		// copy RecordingMacro, RecordingMacroStr, RecordingMacroName
		mRecordingMacro = ref.mRecordingMacro;
		mRecordingMacroStr.delete(0, mRecordingMacroStr.length() );
		mRecordingMacroStr.append( ref.mRecordingMacroStr.toString() );
		mRecordingMacroName = ref.mRecordingMacroName;

		// copy ExcludeLevel, IfLevel
		mExcludeLevel = ref.mExcludeLevel;
		mIfLevel = ref.mIfLevel;
		mExcludeLevelStack = ref.mExcludeLevelStack.clone();
		mIfLevelExecutedStack = (ArrayList<Boolean>) ref.mIfLevelExecutedStack.clone();
	}

	public Dispatch2 store() throws TJSException {
		// store current status into newly created dictionary object
		// and return the dictionary object.
		Dispatch2 dic = TJS.createDictionaryObject();
		try {
			Variant val = new Variant();

			Variant[] psrc = new Variant[1];
			// create and assign macro dictionary
			{
				Dispatch2 dsp;

				dsp = TJS.createDictionaryObject();
				Variant tmp = new Variant(dsp, dsp);
				dic.propSet( Interface.MEMBERENSURE, "macros", tmp, dic );

				Variant src = new Variant(mMacros, mMacros);
				psrc[0] = src;
				mDicAssign.funcCall( 0, null, null, psrc, dsp );
			}

			// create and assign macro arguments
			{
				Dispatch2 dsp;
				dsp = TJS.createArrayObject();
				Variant tmp = new Variant(dsp, dsp);
				dic.propSet( Interface.MEMBERENSURE, "macroArgs", tmp, dic );

				for( int i = 0; i < mMacroArgStackDepth; i++ ) {
					Dispatch2 dic1;
					dic1 = TJS.createDictionaryObject();
					tmp.set(dic1, dic1);
					dsp.propSetByNum( Interface.MEMBERENSURE, i, tmp, dsp );

					Dispatch2 isrc = mMacroArgs.get(i);
					Variant src = new Variant(isrc, isrc);
					psrc[0] = src;
					mDicAssign.funcCall( 0, null, null, psrc, dic1 );
				}
			}


			// create call stack array and copy call stack status
			{
				Dispatch2 dsp;
				dsp = TJS.createArrayObject();
				Variant tmp = new Variant(dsp, dsp);
				dic.propSet( Interface.MEMBERENSURE, "callStack", tmp, dic);

				final int size = mCallStack.size();
				for( int i = 0; i < size; i++ ) {
					CallStackData d = mCallStack.get(i);

					Dispatch2 dic1;
					dic1 = TJS.createDictionaryObject();
					tmp.set(dic1, dic1);
					dsp.propSetByNum( Interface.MEMBERENSURE, i, tmp, dsp );

					val.set( d.Storage );
					dic1.propSet( Interface.MEMBERENSURE, "storage", val, dic1 );

					val.set( d.Label );
					dic1.propSet( Interface.MEMBERENSURE, "label", val, dic1 );
					val.set( d.Offset );
					dic1.propSet( Interface.MEMBERENSURE, "offset", val, dic1);
					val.set( d.OrgLineStr );
					dic1.propSet (Interface.MEMBERENSURE, "orgLineStr", val, dic1);
					val.set( d.LineBuffer );
					dic1.propSet( Interface.MEMBERENSURE, "lineBuffer", val, dic1);
					val.set( d.Pos );
					dic1.propSet( Interface.MEMBERENSURE, "pos", val, dic1);
					val.set( d.LineBufferUsing ? 1 : 0 );
					dic1.propSet( Interface.MEMBERENSURE, "lineBufferUsing", val, dic1);
					val.set( d.MacroArgStackBase );
					dic1.propSet( Interface.MEMBERENSURE, "macroArgStackBase", val, dic1);
					val.set( d.MacroArgStackDepth );
					dic1.propSet( Interface.MEMBERENSURE, "macroArgStackDepth", val, dic1);
					val.set( d.ExcludeLevel );
					dic1.propSet( Interface.MEMBERENSURE, "ExcludeLevel", val, dic1);
					val.set( d.IfLevel );
					dic1.propSet( Interface.MEMBERENSURE, "IfLevel", val, dic1);

					storeIntStackToDic( dic1, d.ExcludeLevelStack, "ExcludeLevelStack" );
					storeBoolStackToDic( dic1, d.IfLevelExecutedStack, "IfLevelExecutedStack" );
				}
			}

			// store StorageName, StorageShortName ( Buffer is not stored )
			val.set( mStorageName );
			dic.propSet( Interface.MEMBERENSURE, "storageName", val, dic);
			val.set( mStorageShortName );
			dic.propSet( Interface.MEMBERENSURE, "storageShortName", val, dic);

			// ( Lines and LineCount are not stored )

			// store CurStorage, CurLine, CurPos
			val.set( mCurLine );
			dic.propSet( Interface.MEMBERENSURE, "curLine", val, dic);
			val.set( mCurPos );
			dic.propSet( Interface.MEMBERENSURE, "curPos", val, dic);

			// ( CurLineStr is not stored )

			// LineBuffer, LineBufferUsing
			val.set( mLineBuffer );
			dic.propSet( Interface.MEMBERENSURE, "lineBuffer", val, dic);
			val.set( mLineBufferUsing ? 1 : 0 );
			dic.propSet( Interface.MEMBERENSURE, "lineBufferUsing", val, dic);

			// store CurLabel ( CurPage TagLine is not stored )
			val.set( mCurLabel );
			dic.propSet( Interface.MEMBERENSURE, "curLabel", val, dic);

			// ( DebugLebel and IgnoreCR are not stored )

			// ( RecordingMacro, RecordingMacroStr, RecordingMacroName are not stored)


			// ExcludeLevel, IfLevel, ExcludeLevelStack, IfLevelExecutedStack
			val.set( mExcludeLevel );
			dic.propSet( Interface.MEMBERENSURE, "ExcludeLevel", val, dic);
			val.set( mIfLevel );
			dic.propSet( Interface.MEMBERENSURE, "IfLevel", val, dic);
			storeIntStackToDic(dic, mExcludeLevelStack, "ExcludeLevelStack");
			storeBoolStackToDic(dic, mIfLevelExecutedStack, "IfLevelExecutedStack");

			// store MacroArgStackBase, MacroArgStackDepth
			val.set( mMacroArgStackBase );
			dic.propSet( Interface.MEMBERENSURE, "macroArgStackBase", val, dic );

			val.set( mMacroArgStackDepth );
			dic.propSet( Interface.MEMBERENSURE, "macroArgStackDepth", val, dic );
		} catch( TJSException e ) {
			dic = null;
			throw e;
		}
		return dic;
	}

	private final static String HEX = "0123456789abcdef";
	private void storeIntStackToDic( Dispatch2 dic, IntVector stack, final String membername ) throws VariantException, TJSException {
		StringBuilder p = mWorkBuilder;
		p.delete(0, p.length() );
		final int count = stack.size();
		for( int i = 0; i < count; i++ ) {
			int v = stack.get(i);
			p.append( HEX.charAt((v >> 28) & 0x000f) );
			p.append( HEX.charAt((v >> 24) & 0x000f) );
			p.append( HEX.charAt((v >> 20) & 0x000f) );
			p.append( HEX.charAt((v >> 16) & 0x000f) );
			p.append( HEX.charAt((v >> 12) & 0x000f) );
			p.append( HEX.charAt((v >>  8) & 0x000f) );
			p.append( HEX.charAt((v >>  4) & 0x000f) );
			p.append( HEX.charAt((v >>  0) & 0x000f) );
		}
		Variant val = new Variant( p.toString() );
		p = null;
		dic.propSet(Interface.MEMBERENSURE, membername, val, dic);
	}
	private void restoreIntStackFromStr( IntVector stack, final String str ) {
		stack.clear();
		final int len = str.length() / 8;
		for( int i = 0; i < len; ++i ) {
			stack.add(
				(((str.charAt( 0 ) <= '9') ? (str.charAt( 0 ) - '0') : (str.charAt( 0 ) - 'a' + 10)) << 28) |
				(((str.charAt( 1 ) <= '9') ? (str.charAt( 1 ) - '0') : (str.charAt( 1 ) - 'a' + 10)) << 24) |
				(((str.charAt( 2 ) <= '9') ? (str.charAt( 2 ) - '0') : (str.charAt( 2 ) - 'a' + 10)) << 20) |
				(((str.charAt( 3 ) <= '9') ? (str.charAt( 3 ) - '0') : (str.charAt( 3 ) - 'a' + 10)) << 16) |
				(((str.charAt( 4 ) <= '9') ? (str.charAt( 4 ) - '0') : (str.charAt( 4 ) - 'a' + 10)) << 12) |
				(((str.charAt( 5 ) <= '9') ? (str.charAt( 5 ) - '0') : (str.charAt( 5 ) - 'a' + 10)) <<  8) |
				(((str.charAt( 6 ) <= '9') ? (str.charAt( 6 ) - '0') : (str.charAt( 6 ) - 'a' + 10)) <<  4) |
				(((str.charAt( 7 ) <= '9') ? (str.charAt( 7 ) - '0') : (str.charAt( 7 ) - 'a' + 10)) <<  0)
			);
		}
	}
	private void restoreBoolStackFromStr( ArrayList<Boolean> stack, final String str ) {
		stack.clear();
		int len = str.length();
		for( int i = 0; i < len; ++i ) {
			stack.add( str.charAt(i) == '1');
		}
	}

	static final private String BIT = "01";
	private void storeBoolStackToDic( Dispatch2 dic, ArrayList<Boolean> stack, final String membername ) throws VariantException, TJSException {
		final int count = stack.size();
		StringBuilder builder = mWorkBuilder;
		builder.delete(0,builder.length());
		for( int i = 0; i < count; i++ ) {
			builder.append( BIT.charAt( stack.get(i).booleanValue() ? 1 : 0) );
		}
		Variant val = new Variant(builder.toString());
		builder = null;
		dic.propSet( Interface.MEMBERENSURE, membername, val, dic );
	}
	public void restore( Dispatch2 dic ) throws VariantException, TJSException {
		// restore status from "dic"
		Variant val = new Variant();
		Variant v = new Variant();
		Variant[] psrc = new Variant[1];

		// restore macros
		{
			//val.clear();
			dic.propGet(0, "macros", val, dic);
			if( val.isVoid() != true ) {
				psrc[0] = val;
				mDicAssign.funcCall(0, null, null, psrc, mMacros );
			}
		}

		{
			// restore macro args
			mMacroArgStackDepth = 0;

			val.clear();
			dic.propGet(0, "macroArgs", val, dic );
			if( val.isVoid() != true ) {
				VariantClosure clo = val.asObjectClosure();
				int count = 0;
				clo.propGet(0, "count", v, null );
				count = v.asInteger();

				clearMacroArgs();

				val.clear();
				dic.propGet(0, "macroArgStackDepth", val, dic );
				if( val.isVoid() != true ) mMacroArgStackDepth = val.asInteger();

				for( int i = 0; i < count; i++ ) {
					Dispatch2 dsp = TJS.createDictionaryObject();
					// Variant val1 = new Variant(dsp, dsp); TODO 使われていないようだけど……

					clo.propGetByNum(0, i, v, null);
					psrc[0] = v;
					mDicAssign.funcCall(0, null, null, psrc, dsp );

					mMacroArgs.add(dsp);
				}
			}

			if(mMacroArgStackDepth != mMacroArgs.size())
				Message.throwExceptionMessage(Message.KAGMalformedSaveData);

			mMacroArgStackBase = mMacroArgs.size(); // later reset to MacroArgStackBase

			// restore call stack
			val.clear();
			dic.propGet(0, "callStack", val, dic);
			if( val.isVoid() != true ) {
				VariantClosure clo = val.asObjectClosure();
				int count = 0;
				v.clear();
				clo.propGet(0, "count", v, null );
				count = v.asInteger();

				mCallStack.clear();

				for( int i = 0; i < count; i++ ) {
					String Storage;
					String Label;
					int Offset;
					String OrgLineStr;
					String LineBuffer;
					int Pos;
					boolean LineBufferUsing;
					int MacroArgStackBase;
					int MacroArgStackDepth;
					int ExcludeLevel;
					int IfLevel;
					IntVector ExcludeLevelStack = new IntVector();
					ArrayList<Boolean> IfLevelExecutedStack = new ArrayList<Boolean>();

					clo.propGetByNum(0, i, v, null);
					VariantClosure dic1 = v.asObjectClosure();
					dic1.propGet(0, "storage", val, null );
					Storage = val.asString();
					dic1.propGet(0, "label", val, null );
					Label = val.asString();
					dic1.propGet(0, "offset", val, null );
					Offset = val.asInteger();
					dic1.propGet(0, "orgLineStr", val, null );
					OrgLineStr = val.asString();
					dic1.propGet(0, "lineBuffer", val, null );
					LineBuffer = val.asString();
					dic1.propGet(0, "pos", val, null );
					Pos = val.asInteger();
					dic1.propGet(0, "lineBufferUsing", val, null );
					LineBufferUsing = val.asBoolean();
					dic1.propGet(0, "macroArgStackBase", val, null );
					MacroArgStackBase = val.asInteger();
					dic1.propGet(0, "macroArgStackDepth", val, null );
					MacroArgStackDepth = val.asInteger();
					dic1.propGet(0, "ExcludeLevel", val, null );
					ExcludeLevel = val.asInteger();
					dic1.propGet(0, "IfLevel", val, null );
					IfLevel = val.asInteger();

					String stack_str;
					dic1.propGet(0, "ExcludeLevelStack", val, null );
					stack_str = val.asString();
					restoreIntStackFromStr( ExcludeLevelStack, stack_str );

					dic1.propGet(0, "IfLevelExecutedStack", val, null );
					stack_str = val.asString();
					restoreBoolStackFromStr(IfLevelExecutedStack, stack_str);

					mCallStack.add( new CallStackData(
						Storage, Label, Offset, OrgLineStr, LineBuffer, Pos,
						LineBufferUsing, MacroArgStackBase, MacroArgStackDepth,
						ExcludeLevelStack, ExcludeLevel, IfLevelExecutedStack, IfLevel) );
				}
			}

			// restore StorageName, StorageShortName, CurStorage, CurLabel
			val.clear();
			dic.propGet(0, "storageName", val, dic);
			if(val.isVoid() != true ) mStorageName = val.asString();
			val.clear();
			dic.propGet(0, "storageShortName", val, dic);
			if(val.isVoid() != true) mStorageShortName = val.asString();
			val.clear();
			dic.propGet(0, "curLabel", val, dic);
			if(val.isVoid() != true ) mCurLabel = val.asString();

			// load scenario
			String storage = mStorageName;
			String label = mCurLabel;
			clearBuffer(); // ensure re-loading the scenario
			loadScenario(storage);
			goToLabel(label);

			// ExcludeLevel, IfLevel
			val.clear();
			dic.propGet(0, "ExcludeLevel", val, dic);
			if(val.isVoid() != true ) mExcludeLevel = val.asInteger();
			val.clear();
			dic.propGet(0, "IfLevel", val, dic);
			if(val.isVoid() != true ) mIfLevel = val.asInteger();

			// ExcludeLevelStack, IfLevelExecutedStack
			val.clear();
			dic.propGet(0, "ExcludeLevelStack", val, dic);
			if( val.isVoid() != true ) {
				String stack_str;
				stack_str = val.asString();
				restoreIntStackFromStr(mExcludeLevelStack, stack_str);
			}

			val.clear();
			dic.propGet(0, "IfLevelExecutedStack", val, dic);
			if( val.isVoid() != true ) {
				String stack_str;
				stack_str = val.asString();
				restoreBoolStackFromStr(mIfLevelExecutedStack, stack_str);
			}


			// restore MacroArgStackBase
			val.clear();
			dic.propGet(0, "macroArgStackBase", val, dic);
			if(val.isVoid() != true ) mMacroArgStackBase = val.asInteger();
		}
	}
	public void loadScenario( final String name ) throws VariantException, TJSException {
		// load scenario to buffer

		breakConditionAndMacro();

		if( mStorageName != null && mStorageName.equals(name) ) {
			// avoid re-loading
			rewind();
		} else {
			clearBuffer();

			// fire onScenarioLoad
			Variant param = new Variant(name);
			Variant[] pparam = {param};
			Variant result = new Variant();
			Dispatch2 owner = mOwner.get();
			int status = owner.funcCall(0, EV_ON_SCENARIO_LOAD, result, pparam, owner );

			if( status == Error.S_OK && result.isString() ) {
				// when onScenarioLoad returns string;
				// assumes the string is scenario
				mScenario = TVP.ScnearioCache.getScenario( result.getString(), true );
			} else {
				// else load from file
				mScenario = TVP.ScnearioCache.getScenario(name, false);
			}

			mLines = mScenario.getLines();
			mLineCount = mScenario.getLineCount();

			rewind();

			mStorageName = name;
			mStorageShortName = Storage.extractStorageName(name);
			if( mDebugLevel >= tkdlSimple ) {
				DebugClass.addLog("================================================================================");
				DebugClass.addLog( "Scenario loaded : " + name );
			}
		}

		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant param = new Variant(mStorageName);
			Variant[] pparam = {param};
			owner.funcCall(0, EV_ON_SCENARIO_LOADED, null, pparam, owner );
		}
	}
	public void clear() {
		// clear all states
		TVP.ScnearioCache.clearScnearioCache(); // also invalidates the scenario cache
		clearBuffer();
		clearMacroArgs();
		clearCallStack();
	}
	private void clearBuffer() {
		// clear internal buffer
		if( mScenario != null ) {
			mScenario = null;
			mLines = null;
			mCurLineStr = null;
		}
		mStorageName = null;
		mStorageShortName = null;
		breakConditionAndMacro();
	}
	private void rewind() {
		// set current position to first
		mCurLine = 0;
		mCurPos = 0;
		mCurLineStr = mLines[0];
		mLineBufferUsing = false;
		breakConditionAndMacro();
	}
	private void breakConditionAndMacro() {
		// break condition state and macro recording
		mRecordingMacro = false;
		mExcludeLevel = -1;
		mExcludeLevelStack.clear();
		mIfLevelExecutedStack.clear();
		mIfLevel = 0;
		popMacroArgsTo(mMacroArgStackBase);
			// clear macro argument down to current base stack position
	}
	public void goToLabel( final String name ) throws TJSException {
		// search label and set current position
		// parameter "name" must start with '*'
		if( name == null || name.length() == 0 ) return;

		mScenario.ensureLabelCache();

		ScenarioCacheItem.LabelCacheData newline;

		newline = mScenario.getLabelCache().get(name);

		if( newline != null ) {
			// found the label in cache
			int vl = mLines[newline.Line].indexOf('|');

			mCurLabel = mScenario.getLabelAliasFromLine(newline.Line);
			if(vl!=-1) mCurPage = mLines[newline.Line].substring(vl+1);
			else mCurPage = null;
			mCurLine = newline.Line;
			mCurPos = 0;
			mLineBufferUsing = false;
		} else {
			// not found
			Message.throwExceptionMessage( Message.KAGLabelNotFound, mStorageName, name);
		}

		if( mDebugLevel >= tkdlSimple ) {
			DebugClass.addLog("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
			DebugClass.addLog( mStorageShortName + " : jumped to : " + name);
		}

		breakConditionAndMacro();
	}
	public void goToStorageAndLabel( final String storage, final String label) throws TJSException {
		if(storage != null && storage.length() != 0) loadScenario(storage);
		if(label != null && label.length() != 0) goToLabel(label);
	}
	public void callLabel( final String name ) throws TJSException {
		pushCallStack();
		goToLabel(name);
	}
	/**
	 * skip comment or label, and go to next line.
	 * fire OnScript event if [script] thru [endscript] ( or @script thru @endscript ) is found.
	 * @return
	 * @throws TJSException
	 */
	private boolean skipCommentOrLabel() throws TJSException {
		mScenario.ensureLabelCache();

		mCurPos = 0;
		if(mCurLine >= mLineCount) return false;
		for(; mCurLine < mLineCount; mCurLine++) {
			if(mLines == null) return false; // in this loop, Lines can be null when onScript does so.

			String p = mLines[mCurLine];

			char c0 = 0;
			if( p != null && p.length() > 0 ) {
				c0 = p.charAt(0);
			}

			if( c0 == ';' )
				continue; // comment

			if( c0 == '*' ) {
				// label
				if( mRecordingMacro )
					Message.throwExceptionMessage(Message.LabelOrScriptInMacro);

				int vl = p.indexOf('|');
				boolean pagename;
				if(vl != -1) {
					mCurLabel = mScenario.getLabelAliasFromLine(mCurLine);
					mCurPage = p.substring(vl + 1);
					pagename = true;
				} else {
					mCurLabel = mScenario.getLabelAliasFromLine(mCurLine);
					mCurPage = null;
					pagename = false;
				}

				// fire onLabel callback event
				Dispatch2 owner = mOwner.get();
				if(owner!=null) {
					Variant[] pparam = new Variant[2];
					pparam[0] = new Variant(mCurLabel);
					if(pagename) pparam[1] = new Variant(mCurPage);
					else pparam[1] = new Variant();
					owner.funcCall(0, EV_ON_LABEL, null, pparam, owner);
				}
				continue;
			}

			if( c0 == '[' && ("[iscript]".equals(p) || "[iscript]\\".equals(p) ) ||
				c0 == '@' && "@iscript".equals(p) ) {
				// inline TJS script
	 			if(mRecordingMacro)
					Message.throwExceptionMessage(Message.LabelOrScriptInMacro);

				StringBuilder script = mWorkBuilder;
				script.delete( 0, script.length() );
				mCurLine++;

				int script_start = mCurLine;

				for(;mCurLine < mLineCount; mCurLine++) {
					p = mLines[mCurLine];
					if( p != null && p.length() > 0 ) {
						c0 = p.charAt(0);
						if( c0 == '[' && ( "[endscript]".equals(p) || "[endscript]\\".equals(p) )||
							c0 == '@' && "@endscript".equals(p) ) {
							break;
						}
						if(mExcludeLevel == -1) {
							script.append( p );
							script.append( "\r\n" );
						}
					} else {
						script.append( "\r\n" );
					}
				}

				if( mCurLine == mLineCount )
					Message.throwExceptionMessage(Message.KAGInlineScriptNotEnd);

				// fire onScript callback event
				if(mExcludeLevel == -1) {
					Dispatch2 owner = mOwner.get();
					if(owner != null ) {
						Variant[] pparam = { new Variant(script.toString()), new Variant(mStorageShortName), new Variant(script_start) };
						script = null;
						owner.funcCall(0, EV_ON_SCRIPT, null, pparam, owner );
					}
				}
				continue;
			}
			break;
		}

		if(mCurLine >= mLineCount) return false;

		mCurLineStr = mLines[mCurLine];
		mLineBufferUsing = false;

		if(mDebugLevel >= tkdlVerbose) {
			DebugClass.addLog( mStorageShortName + " : " + mCurLineStr );
		}
		return true;
	}
	private void pushMacroArgs( Dispatch2 args ) throws VariantException, TJSException {
		Dispatch2 dsp;
		if(mMacroArgs.size() > mMacroArgStackDepth) {
			dsp = mMacroArgs.get(mMacroArgStackDepth);
		} else {
			if( mMacroArgStackDepth > mMacroArgs.size())
				Message.throwExceptionMessage(Message.InternalError);
			dsp = TJS.createDictionaryObject();
			mMacroArgs.add(dsp);
		}
		mMacroArgStackDepth++;

		// copy arguments from args to dsp
		Variant src = new Variant(args, args);
		Variant[] psrc = new Variant[1];
		psrc[0] = src;
		mDicAssign.funcCall(0, null, null, psrc, dsp );
	}
	public void popMacroArgs() throws TJSException {
		if(mMacroArgStackDepth == 0) Message.throwExceptionMessage(Message.KAGSyntaxError);
		mMacroArgStackDepth--;
	}
	private void clearMacroArgs() {
		mMacroArgs.clear();
		mMacroArgStackDepth = 0;
	}
	private void popMacroArgsTo(int base) {
		mMacroArgStackDepth = base;
	}
	/**
	 * find nearest label which be pleced before "start".
	 * "labelline" is to be the label's line number (0-based), and
	 * "labelname" is to be its label name.
	 * "labelline" will be -1 and "labelname" be empty if the label is not found.
	 * @param start
	 * @param labelline 見付かったラベル番号
	 * @return 見付かったラベル名
	 * @throws TJSException
	 */
	private String findNearestLabel( int start, IntWrapper labelline ) throws TJSException {
		mScenario.ensureLabelCache();

		String labelname = null;
		start--;
		while(start >= 0) {
			String line = mLines[start];
			if( line.length() > 0 && line.charAt(0) == '*' ) {
				// label found
				labelname = mScenario.getLabelAliasFromLine(start);
				break;
			}
			start--;
		}
		labelline.value = start;
		if(labelline.value == -1) labelname = null;
		return labelname;
	}
	private void pushCallStack() throws TJSException {
		// push current position information
		if( mDebugLevel >= tkdlVerbose)
		{
			DebugClass.addLog( mStorageShortName + " : call stack depth before calling : "
				+ String.valueOf(mCallStack.size()) );
		}

		IntWrapper labelline = new IntWrapper(0);
		String labelname;
		labelname = findNearestLabel( mCurLine, labelline);
		if(labelline.value < 0) labelline.value = 0;

		String curline_content;
		if( mLines != null && mCurLine < mLineCount )
			curline_content = mLines[mCurLine];
		else
			curline_content = "";

		mCallStack.add( new CallStackData(mStorageName, labelname, mCurLine - labelline.value,
			curline_content,
			mLineBuffer, mCurPos, mLineBufferUsing, mMacroArgStackBase, mMacroArgStackDepth,
			mExcludeLevelStack, mExcludeLevel, mIfLevelExecutedStack, mIfLevel));
		mMacroArgStackBase = mMacroArgStackDepth;
	}
	private void popCallStack( final String storage, final String label ) throws VariantException, TJSException {
		// pop call stack information
		if(mCallStack.size() == 0)
			Message.throwExceptionMessage(Message.KAGCallStackUnderflow);

		// pop macro argument information
		CallStackData data = mCallStack.get( mCallStack.size()-1 );
		mMacroArgStackBase = data.MacroArgStackDepth; // later reset to MacroArgStackBase
		popMacroArgsTo(data.MacroArgStackDepth);

		// goto label or previous position
		if( (storage != null && storage.length() != 0) || ( label != null && label.length() != 0) ) {
			// return to specified position
			goToStorageAndLabel(storage, label);
		} else {
			// return to previous calling position
			loadScenario(data.Storage);
			if( data.Label != null && data.Label.length() != 0 ) goToLabel(data.Label);
			mCurLine += data.Offset;
			if(mCurLine > mLineCount)
				Message.throwExceptionMessage(Message.KAGReturnLostSync);
					/* CurLine == LineCount is OK (at end of file) */
			if(mCurLine < mLineCount) {
				if(data.OrgLineStr != mLines[mCurLine]) { // check original line information
					if( data.OrgLineStr == null || mLines[mCurLine] == null )
						Message.throwExceptionMessage(Message.KAGReturnLostSync);
					else if( data.OrgLineStr.equals(mLines[mCurLine]) == false )
						Message.throwExceptionMessage(Message.KAGReturnLostSync);
				}
			}

			if(data.LineBufferUsing) {
				mLineBuffer = data.LineBuffer;
				mCurLineStr = mLineBuffer;
				mLineBufferUsing = true;
			} else {
				if(mCurLine < mLineCount) {
					mCurLineStr = mLines[mCurLine];
					mLineBufferUsing = false;
				}
			}
			mCurPos = data.Pos;

			mExcludeLevelStack = data.ExcludeLevelStack;
			mExcludeLevel = data.ExcludeLevel;
			mIfLevelExecutedStack = data.IfLevelExecutedStack;
			mIfLevel = data.IfLevel;

			if( mDebugLevel >= tkdlSimple ) {
				String label1;
				if( data.Label == null || data.Label.length() ==0 ) label1 = "(start)";
				else label1 = data.Label;
				StringBuilder builder = mWorkBuilder;
				builder.delete(0, builder.length());
				builder.append(mStorageShortName);
				builder.append(" : returned to : ");
				builder.append(label1);
				builder.append(" line offset ");
				builder.append(data.Offset);
				DebugClass.addLog( builder.toString() );
				builder = null;
			}
		}

		// reset MacroArgStackBase
		mMacroArgStackBase = data.MacroArgStackBase;

		// pop casll stack
		mCallStack.remove( mCallStack.size() -1 );

		// call function back
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[0];
			owner.funcCall(0, EV_ON_AFTERETURN, null, arg, owner);
		}

		if( mDebugLevel >= tkdlVerbose ) {
			StringBuilder builder = mWorkBuilder;
			builder.delete(0, builder.length());
			builder.append(mStorageShortName);
			builder.append(" : call stack depth after returning : ");
			builder.append(mCallStack.size());
			DebugClass.addLog( builder.toString() );
			builder = null;
		}
	}
	public void clearCallStack() {
		mCallStack.clear();
		popMacroArgsTo(mMacroArgStackBase = 0); // macro arguments are also cleared
	}

	/**
	 * is white space ?
	 * @param ch
	 * @return
	 */
	static private final boolean isWS( char ch ) {
		return (ch == ' ' || ch == '\t');
	}
	/**
	 * 組み込みタグ
	 *
	 */
	enum SpecialTags {
		tag_other,
		tag_if,
		tag_else,
		tag_elsif,
		tag_ignore,
		tag_endif,
		tag_endignore,
		tag_emb,
		tag_macro,
		tag_endmacro,
		tag_macropop,
		tag_erasemacro,
		tag_jump,
		tag_call,
		tag_return
	};
	static private String __tag_name;
	static private String __eol_name;
	static private String __storage_name;
	static private String __target_name;
	static private String __exp_name;

	static private String STR_TEXT;

	static private String EV_ON_JUMP;
	static private String EV_ON_CALL;
	static private String EV_ON_RETURN;
	static private String EV_ON_AFTERETURN;
	static private String EV_ON_LABEL;
	static private String EV_ON_SCENARIO_LOAD;
	static private String EV_ON_SCENARIO_LOADED;
	static private String EV_ON_SCRIPT;

	static private Variant TAG_INTERRUPT;
	static private Variant TAG_R;
	static private Variant TAG_CH;

	static private Variant VAL_TRUE;

	static private HashMap<String, SpecialTags> special_tags_hash;
	static public void initialize() {
		__tag_name = TJS.mapGlobalStringMap("tagname");
		__eol_name = TJS.mapGlobalStringMap("eol");
		__storage_name = TJS.mapGlobalStringMap("storage");
		__target_name = TJS.mapGlobalStringMap("target");
		__exp_name = TJS.mapGlobalStringMap("exp");

		STR_TEXT = TJS.mapGlobalStringMap("text");

		EV_ON_JUMP = TJS.mapGlobalStringMap("onJump");
		EV_ON_CALL = TJS.mapGlobalStringMap("onCall");
		EV_ON_RETURN = TJS.mapGlobalStringMap("onReturn");
		EV_ON_AFTERETURN = TJS.mapGlobalStringMap("onAfterReturn");
		EV_ON_LABEL = TJS.mapGlobalStringMap("onLabel");
		EV_ON_SCENARIO_LOAD = TJS.mapGlobalStringMap("onScenarioLoad");
		EV_ON_SCENARIO_LOADED = TJS.mapGlobalStringMap("onScenarioLoaded");
		EV_ON_SCRIPT = TJS.mapGlobalStringMap("onScript");

		TAG_INTERRUPT = new Variant("interrupt");
		TAG_R = new Variant( "r" );
		TAG_CH = new Variant("ch");

		VAL_TRUE = new Variant( "true" );

		special_tags_hash = new HashMap<String, SpecialTags>();
		special_tags_hash.put( "if",		SpecialTags.tag_if );
		special_tags_hash.put( "ignore",	SpecialTags.tag_ignore);
		special_tags_hash.put( "endif",		SpecialTags.tag_endif);
		special_tags_hash.put( "endignore",	SpecialTags.tag_endignore);
		special_tags_hash.put( "else",		SpecialTags.tag_else);
		special_tags_hash.put( "elsif",		SpecialTags.tag_elsif);
		special_tags_hash.put( "emb",		SpecialTags.tag_emb);
		special_tags_hash.put( "macro",		SpecialTags.tag_macro);
		special_tags_hash.put( "endmacro",	SpecialTags.tag_endmacro);
		special_tags_hash.put( "macropop",	SpecialTags.tag_macropop);
		special_tags_hash.put( "erasemacro",SpecialTags.tag_erasemacro);
		special_tags_hash.put( "jump",		SpecialTags.tag_jump);
		special_tags_hash.put( "call",		SpecialTags.tag_call);
		special_tags_hash.put( "return",	SpecialTags.tag_return);

	}
	private final char getC() {
		if( mCurPos >= mCurLineStr.length() ) return 0;
		return mCurLineStr.charAt(mCurPos);
	}
	/**
	 * get next tag and return information dictionary object.
	 * return null if the tag not found.
	 * normal characters are interpreted as a "ch" tag.
	 * CR code is interpreted as a "r" tag.
	 * returned tag's line number is stored to TagLine.
	 * tag paremeters are stored into return value.
	 * tag name is also stored into return value, naemd "__tag".
	 * @return
	 * @throws CompileException
	 * @throws TJSException
	 * @throws VariantException
	 */
	public Dispatch2 getNextTag() throws VariantException, TJSException, CompileException {
		// pretty a nasty code.
		all_nest:
		while(true){ parse_start: {

		if( mCurLine >= mLineCount ) return null;
		if( mLines == null ) return null;

		char c = 0;
		while( true ) {
			mDicClear.funcCall(0, null, null, TJS.NULL_ARG, mDicObj); // clear dictionary object

			if( mInterrupted ) {
				// interrupt current parsing return as "interrupted" tag
				mDicObj.propSet(Interface.MEMBERENSURE, __tag_name, TAG_INTERRUPT, mDicObj);
				mInterrupted = false;
				return mDicObj;
			}

			if( mCurLine >= mLineCount ) break all_nest; // all of scenario was decoded

			int tagstartpos = mCurPos;

			if(!mLineBufferUsing && mCurPos == 0)
				if(!skipCommentOrLabel()) return null;

			c = getC();
			if(!mIgnoreCR) { // 改行を無視しない時
				if( (c == '\\' && mCurLineStr.length() == (mCurPos+1)) ||
					(mCurLineStr.length() == mCurPos && mCurPos >= 3 && mCurLineStr.startsWith( "[p]", mCurPos-3 )) )  {
					// 行が [p] で終わっているか、\で終わっている場合
					mCurLine++;
					mCurPos = 0;
					mLineBufferUsing = false;
					continue;
				}

				if( mCurLineStr.length() == mCurPos ) { // 行が終わっている時
					mTagLine = mCurLine;
					mDicObj.propSet( Interface.MEMBERENSURE, __tag_name, TAG_R, mDicObj);
					mDicObj.propSet( Interface.MEMBERENSURE, __eol_name, VAL_TRUE, mDicObj);
					if(mRecordingMacro) mRecordingMacroStr.append( "[r eol=true]" );
					mCurLine++;
					mCurPos = 0;
					mLineBufferUsing = false;
					if(!mRecordingMacro && mExcludeLevel == -1) { return mDicObj; }
					continue;
				}
			}

			char ldelim; // last delimiter
			if( !mLineBufferUsing && mCurPos == 0 && c == '@' ) {
				// @始まり ( line command mode )
				ldelim = 0; // tag last delimiter is a null terminater
			} else {
				if( c != '[' || mCurLineStr.startsWith( "[[", mCurPos ) ) {
					// 通常の文字
					char ch = c;
					mTagLine = mCurLine;

					if( ch == 0 ) {
						// line ended
						mCurLine++;
						mCurPos = 0;
						mLineBufferUsing = false;
						continue;
					} else if(ch == '\t' ) {
						mCurPos++;
						continue;
					} else if( ch != '\n' ) {
						mDicObj.propSet( Interface.MEMBERENSURE, __tag_name, TAG_CH, mDicObj );
						Variant ch_val = new Variant( mCurLineStr.substring(mCurPos, mCurPos+1) );
						mDicObj.propSet( Interface.MEMBERENSURE, STR_TEXT, ch_val, mDicObj );

						if( mRecordingMacro ) {
							if(ch == '[' )
								mRecordingMacroStr.append( "[[" );
							else
								mRecordingMacroStr.append( ch );
						}
					} else {	// \n  ( reline )
						mDicObj.propSet( Interface.MEMBERENSURE, __tag_name, TAG_R, mDicObj );
						if( mRecordingMacro ) mRecordingMacroStr.append( "[r]" );
					}

					c = getC();
					if( c == '[' ) mCurPos++;
					mCurPos++;

					if(!mRecordingMacro && mExcludeLevel == -1) {
						return mDicObj;
					}
					continue;
				}
				ldelim = ']';
			}

// タグの解析開始
			boolean condition = true;
			mTagLine = mCurLine;
			int tagstart = mCurPos;
			mCurPos++;

			if( mCurLineStr.length() <= mCurPos ) Message.throwExceptionMessage(Message.KAGSyntaxError);

// タグ名の解析 - start -----------------------------------------------------
			while( mCurPos < mCurLineStr.length() && isWS(getC()) ) mCurPos++; // skip white space
			if( mCurLineStr.length() <= mCurPos ) Message.throwExceptionMessage(Message.KAGSyntaxError);

			int tagnamestart = mCurPos;
			while( mCurPos < mCurLineStr.length() && !isWS(getC()) && getC() != ldelim)
				mCurPos++;

			if( tagnamestart == mCurPos )
				Message.throwExceptionMessage(Message.KAGSyntaxError);

			String tagname = mCurLineStr.substring(tagnamestart, mCurPos);
			tagname = toLowerCaseHalf(tagname);
			{
				Variant tag_val = new Variant(tagname);
				mDicObj.propSet( Interface.MEMBERENSURE, __tag_name, tag_val, mDicObj);
			}
// タグ名の解析 - end -----------------------------------------------------
			// 組み込みタグチェック
			SpecialTags tagkind;
			SpecialTags tag = special_tags_hash.get(tagname);
			if( mProcessSpecialTags )
				tagkind = (tag != null) ? tag : SpecialTags.tag_other;
			else
				tagkind = SpecialTags.tag_other;

			if(tagkind == SpecialTags.tag_macro) mRecordingMacroName = null;

// 属性の解析
			// tag attributes
			while( true ) {
				while( isWS(getC()) ) mCurPos++;

				if( getC() == ldelim ) {
					// tag ended

					boolean ismacro = false;
					String macrocontent = "";

					if( condition && mExcludeLevel == -1 ) {
						if( tagkind == SpecialTags.tag_endmacro ) {
							// macro recording ended endmacro
							if( !mRecordingMacro ) Message.throwExceptionMessage(Message.KAGSyntaxError);
							mRecordingMacro = false;
							if( mDebugLevel >= tkdlVerbose ) {
								DebugClass.addLog("macro : " + mRecordingMacroName + " : " + mRecordingMacroStr.toString() );
							}

							mRecordingMacroStr.append( "[macropop]" ); // ensure macro arguments are to be popped

							// register macro
							Variant macrocontent1 = new Variant(mRecordingMacroStr.toString());
							mMacros.propSet( Interface.MEMBERENSURE, mRecordingMacroName, macrocontent1, mMacros );
						}

						// record macro
						if( mRecordingMacro ) {
							if( ldelim != 0 ) {
								// normal tag
								mRecordingMacroStr.append(mCurLineStr.substring(  tagstart, mCurPos + 1 ) );
							} else {
								// line command
								if( mCurPos - tagstart >= 1 ) {
									mRecordingMacroStr.append('[');
									//mRecordingMacroStr.append(mCurLineStr.substring( tagstart +1, mCurPos - 1));
									mRecordingMacroStr.append(mCurLineStr.substring( tagstart +1, mCurPos));
									mRecordingMacroStr.append(']');
								}
							}
							if(ldelim == 0) {
								mCurLine++;
								mCurPos = 0;
								mLineBufferUsing = false;
							} else {
								mCurPos++;
							}
							break; // break
						}

						// is macro ?
						Variant macroval = new Variant();
						int hr = mMacros.propGet( 0, tagname, macroval, mMacros );
						ismacro = hr >= 0;
						if(ismacro) ismacro = macroval.isVoid() != true;
						if(ismacro) macrocontent = macroval.asString();
					}

					// tag-specific processing
					if( tagkind == SpecialTags.tag_other && !ismacro ) {
						// not a control tag
						if(ldelim == 0) {
							mCurLine++;
							mCurPos = 0;
							mLineBufferUsing = false;
						} else {
							mCurPos++;
						}
						if(condition && mExcludeLevel == -1) {
							return mDicObj;
						}
						break;
					}

					// if/ignore
					if(tagkind == SpecialTags.tag_if || tagkind == SpecialTags.tag_ignore) {
						mIfLevel++;
						mIfLevelExecutedStack.add(false);
						mExcludeLevelStack.add(mExcludeLevel);

						if( mExcludeLevel == -1 ) {
							Variant val = new Variant();
							mDicObj.propGet(0, __exp_name, val, mDicObj);
							String exp = val.asString();
							if( exp.length() == 0 )
								Message.throwExceptionMessage(Message.KAGSyntaxError);
							Dispatch2 owner = mOwner.get();
							ScriptsClass.executeExpression( exp, owner, val );

							boolean cond = val.asBoolean();
							if(tagkind == SpecialTags.tag_ignore) cond = ! cond;

							mIfLevelExecutedStack.set(mIfLevelExecutedStack.size()-1, cond );
							if(!cond) {
								mExcludeLevel = mIfLevel;
							}
						}
					}

					// elsif
					if( tagkind == SpecialTags.tag_elsif ) {
						if( mIfLevelExecutedStack.isEmpty() ) {
							// no preceded if/ignore tag.
							// should throw an exception?
						} else if( mIfLevelExecutedStack.get(mIfLevelExecutedStack.size()-1) ) {
							mExcludeLevel = mIfLevel;
						} else if( mIfLevel == mExcludeLevel ) {
							Variant val = new Variant();
							String exp;
							mDicObj.propGet(0, __exp_name, val, mDicObj);
							exp = val.asString();
							if( exp.length() == 0 ) Message.throwExceptionMessage(Message.KAGSyntaxError);
							Dispatch2 owner = mOwner.get();
							ScriptsClass.executeExpression(exp, owner, val);

							boolean cond = val.asBoolean();
							if(cond) {
								mIfLevelExecutedStack.set( mIfLevelExecutedStack.size()-1, true );
								mExcludeLevel = -1;
							}
						}
					}

					// else
					if( tagkind == SpecialTags.tag_else ) {
						if( mIfLevelExecutedStack.isEmpty() ) {
							// no preceded if/ignore tag.
							// should throw an exception?
						} else if( mIfLevelExecutedStack.get(mIfLevelExecutedStack.size()-1) ) {
							mExcludeLevel = mIfLevel;
						} else if( mIfLevel == mExcludeLevel ) {
							mIfLevelExecutedStack.set( mIfLevelExecutedStack.size()-1, true );
							mExcludeLevel = -1;
						}
					}

					// endif/endignore
					if( tagkind == SpecialTags.tag_endif || tagkind == SpecialTags.tag_endignore ) {
						// endif
						if(!mExcludeLevelStack.isEmpty() ) {
							mExcludeLevel = mExcludeLevelStack.get(mExcludeLevelStack.size()-1);
							mExcludeLevelStack.remove( mExcludeLevelStack.size()-1);
						}
						if(!mIfLevelExecutedStack.isEmpty())
							mIfLevelExecutedStack.remove(mIfLevelExecutedStack.size()-1);

						mIfLevel--;
						if(mIfLevel < 0) mIfLevel = 0;

						if(ldelim == 0) {
							mCurLine++;
							mCurPos = 0;
							mLineBufferUsing = false;
						} else {
							mCurPos++;
						}

						break; // break
					}


					if( condition && mExcludeLevel == -1 ) {
						if(tagkind == SpecialTags.tag_emb || (ismacro && tagkind==SpecialTags.tag_other)) {
							// embed string
							// insert string to current position
							if(ldelim != 0) mCurPos++;

							if(!ismacro) {
								// execute expression
								Variant val = new Variant();
								mDicObj.propGet(0, __exp_name, val, mDicObj);
								String exp = val.asString();
								if( exp.length() == 0 )
									Message.throwExceptionMessage(Message.KAGSyntaxError);
								Dispatch2 owner = mOwner.get();
								ScriptsClass.executeExpression(exp, owner, val);
								exp = val.asString();

								// count '['
								char[] p = exp.toCharArray();
								int r_count = 0;
								for( int i = 0; i < p.length; i++ ) {
									if( p[i] == '[' ) r_count++;
									r_count++;
								}

								int curposlen = mCurLineStr.length() - mCurPos;
								int finallen = r_count + tagstartpos + curposlen;

								if(ldelim == 0 && !mIgnoreCR) finallen++;

								StringBuilder d = mWorkBuilder;
								d.delete(0,d.length());
								if( tagstartpos > 0 ) {
									d.append( mCurLineStr.substring(0, tagstartpos) );
								}
								//d += tagstartpos;

								// escape '['
								for( int i = 0; i < p.length; i++ ) {
									if( p[i] == '[' ) {
										d.append( '[' );
										d.append( '[' );
									} else {
										d.append( p[i]);
									}
								}
								if( mCurPos < mCurLineStr.length() ) {
									d.append( mCurLineStr.substring(mCurPos) );
								}

								if( ldelim == 0 && !mIgnoreCR ) {
									d.append( '\\' );
								}
								mLineBuffer = d.toString();
								d = null;
							} else {
								int maclen = macrocontent.length();
								int curposlen = mCurLineStr.length() - mCurPos;
								int finallen = tagstartpos + maclen + curposlen;

								if(ldelim == 0 && !mIgnoreCR) finallen++;

								StringBuilder d = mWorkBuilder;
								d.delete(0,d.length());
								if( tagstartpos > 0 ) {
									d.append( mCurLineStr.substring(0,tagstartpos) );
								}
								d.append( macrocontent );
								if( mCurPos < mCurLineStr.length() ) {
									d.append( mCurLineStr.substring(mCurPos) );
								}
								if( ldelim == 0 && !mIgnoreCR ) {
									d.append( '\\' );
								}
								mLineBuffer = d.toString();
								d = null;
							}

							mCurLineStr = mLineBuffer;
							mCurPos = tagstartpos;

							mLineBufferUsing = true;

							// push macro arguments
							if(ismacro) pushMacroArgs(mDicObj);
							break;
						} else if( tagkind == SpecialTags.tag_jump ) {
							// jump tag
							Variant val = new Variant();
							mDicObj.propGet(0, __storage_name, val, mDicObj );
							String attrib_storage = val.asString();
							mDicObj.propGet(0, __target_name, val, mDicObj );
							String attrib_target = val.asString();


							// fire onJump event
							boolean process = true;
							Dispatch2 owner = mOwner.get();
							if( owner!=null ) {
								Variant param = new Variant(mDicObj, mDicObj);
								Variant[] pparam = {param};
								Variant res = new Variant();
								int er = owner.funcCall(0, EV_ON_JUMP, res, pparam, owner );
								if( er == Error.S_OK ) process = res.asBoolean();
							}

							if(process) {
								goToStorageAndLabel( attrib_storage, attrib_target );
								break parse_start; // re-start parsing TODO
							}
						} else if( tagkind == SpecialTags.tag_call ) {
							// call tag
							Variant val = new Variant();
							mDicObj.propGet(0, __storage_name, val, mDicObj );
							String attrib_storage = val.asString();
							mDicObj.propGet(0, __target_name, val, mDicObj );
							String attrib_target = val.asString();

							// fire onCall event
							boolean process = true;
							Dispatch2 owner = mOwner.get();
							if( owner != null ) {
								Variant param = new Variant(mDicObj, mDicObj);
								Variant[] pparam = {param};
								Variant res = new Variant();
								int er = owner.funcCall(0, EV_ON_CALL, res, pparam, owner);
								if(er == Error.S_OK) process = res.asBoolean();
							}

							if( process ) {
								if(ldelim == 0) {
									mCurLine++;
									mCurPos = 0;
									mLineBufferUsing = false;
								} else {
									mCurPos++;
								}

								pushCallStack();
								goToStorageAndLabel(attrib_storage, attrib_target);
								break parse_start; //TODO
							}
						} else if(tagkind == SpecialTags.tag_return) {
							// return tag
							Variant val = new Variant();
							mDicObj.propGet(0, __storage_name, val, mDicObj);
							String attrib_storage = val.asString();
							mDicObj.propGet(0, __target_name, val, mDicObj );
							String attrib_target = val.asString();

							// fire onReturn event
							boolean process = true;
							Dispatch2 owner = mOwner.get();
							if( owner!=null ) {
								Variant param = new Variant(mDicObj, mDicObj);
								Variant[] pparam = {param};
								Variant res = new Variant();
								int er = owner.funcCall(0, EV_ON_RETURN, res, pparam, owner);
								if(er == Error.S_OK) process = res.asBoolean();
							}

							if(process) {
								popCallStack(attrib_storage, attrib_target);
								break parse_start; //TODO
							}
						} else {
							if( tagkind == SpecialTags.tag_macro ) {
								Variant val = new Variant();
								mDicObj.propGet( 0, "name", val, mDicObj );
								mRecordingMacroName = val.asString();
								mRecordingMacroName = toLowerCaseHalf(mRecordingMacroName); // 全角は変換しない toLowerCase
								if( mRecordingMacroName.length() == 0 )
									Message.throwExceptionMessage(Message.KAGSyntaxError);
										// missing macro name
								mRecordingMacro = true; // start recording macro
								mRecordingMacroStr.delete(0, mRecordingMacroStr.length());
							} else if(tagkind == SpecialTags.tag_macropop ) {
								// pop macro arguments
								popMacroArgs();
							} else if(tagkind == SpecialTags.tag_erasemacro) {
								Variant val = new Variant();
								mDicObj.propGet(0, "name", val, mDicObj);
								String macroname = val.asString();
								int hr = mMacros.deleteMember(0, macroname, mMacros);
								if( hr < 0 )
									Message.throwExceptionMessage(Message.UnknownMacroName, macroname);
							}
						}
					}

					if(ldelim == 0) {
						mCurLine++;
						mCurPos = 0;
						mLineBufferUsing = false;
					} else {
						mCurPos++;
					}
					break;
				}

				if(getC() == 0)
					Message.throwExceptionMessage(Message.KAGSyntaxError);

				// attrib name
				if( getC() == '*' ) {
					// macro entity all
					if(!mRecordingMacro) {
						Dispatch2 dsp = getMacroTop();
						if( dsp!=null ) {
							// assign macro arguments to current arguments
							Variant src =new Variant(dsp, dsp);
							Variant[] psrc = {src};
							mDicAssign.funcCall(0, null, null, psrc, mDicObj);
						}
						Variant tag_val = new Variant(tagname);
						mDicObj.propSet( Interface.MEMBERENSURE, __tag_name, tag_val, mDicObj );
						// reset tag_name
					}

					mCurPos++;
					while(mCurPos < mCurLineStr.length() && isWS(getC())) mCurPos++;
					continue;
				}

				int attribnamestart = mCurPos;
				while(mCurPos < mCurLineStr.length() && !isWS(getC()) &&
					getC() != '=' && getC() != ldelim)
					mCurPos++;

				int attribnameend = mCurPos;

				String attribname = mCurLineStr.substring(attribnamestart, attribnameend);
				attribname = toLowerCaseHalf(attribname);

				// =
				while( mCurPos < mCurLineStr.length() && isWS(getC()) ) mCurPos++;

				boolean entity = false;
				boolean macroarg = false;
				String value;

				if( getC() != '=' ) {
					// arrtibute value omitted
					value = "true"; // always true
				} else {
					if( mCurLineStr.length() <= mCurPos ) Message.throwExceptionMessage(Message.KAGSyntaxError);
					mCurPos++;
					if( mCurLineStr.length() <= mCurPos ) Message.throwExceptionMessage(Message.KAGSyntaxError);
					while( mCurLineStr.length() > mCurPos && isWS(getC())) mCurPos++;
					if( mCurLineStr.length() <= mCurPos ) Message.throwExceptionMessage(Message.KAGSyntaxError);

					// attrib value
					char vdelim = 0; // value delimiter

					if(getC() == '&' ) {
						entity = true;
						mCurPos++;
					} else if(getC() == '%' ) {
						macroarg = true;
						mCurPos++;
					}

					if( getC() == '\"' || getC() == '\'' ) {
						vdelim = getC();
						mCurPos++;
					}

					int valuestart = mCurPos;
					while( mCurLineStr.length() > mCurPos &&
						(vdelim != 0 ? (getC() != vdelim) :
							(getC() != ldelim &&
								!isWS(getC())) ) )
					{
						if( getC() == '`' ) {
							// escaped with '`'
							mCurPos++;
							if( mCurLineStr.length() <= mCurPos ) Message.throwExceptionMessage(Message.KAGSyntaxError);
						}
						mCurPos++;
					}

					if(ldelim != 0 && mCurLineStr.length() <= mCurPos)
						Message.throwExceptionMessage(Message.KAGSyntaxError);
					int valueend = mCurPos;

					if(vdelim!=0) mCurPos++;

					// unescape ` character of value
					value = mCurLineStr.substring(valuestart, valueend);
					if( valueend != valuestart ) {
						// value has at least one character
						final int count = value.length();
						StringBuilder v = mWorkBuilder;
						v.delete( 0, v.length() );
						int vp = 0;
						if( !entity && vp < count && value.charAt(vp) == '&') {
							entity = true;
							vp++;
						}
						if( !macroarg && vp < count && value.charAt(vp) == '%') {
							macroarg = true;
							vp++;
						}

						while( vp < count ) {
							if( value.charAt(vp) == '`' ) {
								vp++;
								if( vp >= count ) break;
							}
							v.append( value.charAt(vp) );
							vp++;
						}
						value = v.toString();
						v = null;
					}
				}

				// special attibute processing
				boolean store = true;
				if((!mRecordingMacro && mExcludeLevel == -1) || tagkind == SpecialTags.tag_elsif) {
					// process expression entity or macro argument
					if(entity) {
						Dispatch2 owner = mOwner.get();
						ScriptsClass.executeExpression(value, owner, mValueVariant);
						if(mValueVariant.isVoid() != true) mValueVariant.toString();
					} else if(macroarg) {
						Dispatch2 args = getMacroTop();
						if(args!=null) {
							int vp = value.indexOf('|');
							if( vp != -1 ) {
								String name = value.substring( 0, vp );
								args.propGet(0, name, mValueVariant, args);
								if( mValueVariant.isVoid() )
									mValueVariant.set( value.substring(vp+1) );
							} else {
								args.propGet( 0, value, mValueVariant, args );
							}
						} else {
							mValueVariant.set( value );
						}
					} else {
						mValueVariant.set( value );
					}

					if( "cond".equals(attribname) ) {
						// condition
						Variant val = new Variant();
						Dispatch2 owner = mOwner.get();
						ScriptsClass.executeExpression( mValueVariant.asString(), owner, val );
						condition = val.asBoolean();
						store = false;
					}
				}

				// store value into the dictionary object
				if(store)
					mDicObj.propSet(Interface.MEMBERENSURE, attribname, mValueVariant, mDicObj );
			}
		}
		}} // parse_start:

		return null;
	}

	public Dispatch2 getMacroTop() {
		if(mMacroArgStackDepth == 0) return null;
		return mMacroArgs.get(mMacroArgStackDepth - 1);
	}

	public final String getStorageName() { return mStorageName; }
	public final void interrupt() { mInterrupted = true; };
	public final void resetInterrupt() { mInterrupted = false; };

	public final String getCurLabel() { return mCurLabel; }
	public final int getCurLine() { return mCurLine; }
	public final int getCurPos() { return mCurPos; }
	public final String getCurLineStr() { return mCurLineStr; }

	public final void setProcessSpecialTags(boolean b) { mProcessSpecialTags = b; }
	public final boolean getProcessSpecialTags() { return mProcessSpecialTags; }

	public final void setIgnoreCR(boolean b) { mIgnoreCR = b; }
	public final boolean getIgnoreCR() { return mIgnoreCR; }

	public final void setDebugLevel( int level) { mDebugLevel = level; }
	public final int getDebugLevel() { return mDebugLevel; }

	public final Dispatch2 getMacros() { return mMacros; }

	public int getCallStackDepth() { return mCallStack.size(); }
	public void assign(KAGParserNI src) throws VariantException, TJSException {
		copy( src );
	}

	/**
	 * 半角大文字英字のみを小文字にする
	 * @param str 入力文字列
	 * @return 変換後文字列
	 */
	private String toLowerCaseHalf( String str ) {
		final int count = str.length();
		if( mWorkChar == null || mWorkChar.length < count ) {
			mWorkChar = str.toCharArray();
		} else {
			str.getChars( 0, count, mWorkChar, 0 );
		}
		char[] tmp = mWorkChar;
		boolean ischange = false;
		for( int i = 0; i < count; i++ ) {
			char ch = tmp[i];
			if( ch >= 'A' && ch <= 'Z' ) {
				ch += 'a'-'A';
				tmp[i] = ch;
				ischange = true;
			}
		}
		if( ischange ) {
			return new String(tmp);
		} else {
			return str;
		}
	}
}

