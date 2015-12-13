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
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;

import jp.kirikiri.tjs2.translate.JavaCodeGenerator;

/**
 * TJS2 のバイトコードを生成する
 */
public class InterCodeGenerator implements SourceCodeAccessor {

	//private static final int INC_SIZE_BYTE = 1024;
	private static final int INC_ARRAY_COUNT = 256;
	//private static final int INC_SIZE_BYTE = 512;
	//private static final int INC_SIZE_LONG_BYTE = 2048;
	private static final int SEARCH_CONST_VAL_SIZE = 20;

	private VectorWrap<ExprNode> mNodeToDeleteVector;
	private VectorWrap<ExprNode> mCurrentNodeVector;

	private Compiler mBlock;
	private InterCodeGenerator mParent;
	private String mName;
	private int mContextType;
	private short[] mCodeArea;
	private int mCodeAreaPos;
	private short[] mCode;
	private int mPrevSourcePos;
	private boolean mSourcePosArraySorted;
	//private LongBuffer mSourcePosArray; // 上位をcodePos, 下位をsourcePos とする
	private long[] mSourcePosArray; // 上位をcodePos, 下位をsourcePos とする
	private int mSrcPosArrayPos;
	private ArrayList<Variant> mDataArea; // Dataの中に function として、InterCodeGenerator が入っている可能性がある、後で差し替えること。
	private ArrayList<Variant> mInterCodeDataArea; // Compiler に持たせた方がいいかな？
	private Variant[] mDataArray;
	private LocalNamespace mNamespace;
	private int mVariableReserveCount;
	private int mFrameBase;
	private int mMaxFrameCount;

	private IntVector mJumpList;
	private boolean mAsGlobalContextMode;
	private ExprNode mSuperClassExpr;
	private VectorWrap<NestData> mNestVector;
	private Stack<ArrayArg> mArrayArgStack;
	private Stack<FuncArg> mFuncArgStack;
	private InterCodeGenerator mPropSetter;
	private InterCodeGenerator mPropGetter;
	private InterCodeGenerator mSuperClassGetter;
	private int mMaxVariableCount;
	private ArrayList<FixData> mFixList;
	private VectorWrap<NonLocalFunctionDecl> mNonLocalFunctionDeclVector;
	private int mFunctionRegisterCodePoint;
	private int mPrevIfExitPatch;
	private IntVector mSuperClassGetterPointer;
	private int mFuncDeclArgCount;
	private int mFuncDeclUnnamedArgArrayBase;
	private int mFuncDeclCollapseBase;

	public static class SubParam {
		public int mSubType;
		public int mSubFlag;
		public int mSubAddress;
		public SubParam() {
			//mSubType = 0;
			//mSubFlag = 0;
			//mSubAddress = 0;
		}
		public SubParam(SubParam param) {
			mSubType = param.mSubType;
			mSubFlag = param.mSubFlag;
			mSubAddress = param.mSubAddress;
		}
	}

	// tNestType
	public static int
		ntBlock		= 0,
		ntWhile		= 1,
		ntDoWhile	= 2,
		ntFor		= 3,
		ntSwitch	= 4,
		ntIf		= 5,
		ntElse		= 6,
		ntTry		= 7,
		ntCatch		= 8,
		ntWith 		= 9;

	static class NestData {
		int Type;
		int VariableCount;
		// union {
			boolean VariableCreated;
			// boolean IsFirstCase; 上と同じとみなす
		//};
		int RefRegister;
		int StartIP;
		int LoopStartIP;
		IntVector ContinuePatchVector;
		IntVector ExitPatchVector;
		int Patch1;
		int Patch2;
		ExprNode PostLoopExpr;

		public NestData() {
			ContinuePatchVector = new IntVector();
			ExitPatchVector = new IntVector();
		}
	}

	static class FixData {
		int StartIP;
		int Size;
		int NewSize;
		boolean BeforeInsertion;
		ShortBuffer Code;

		public FixData( int startip, int size, int newsize, ShortBuffer code, boolean beforeinsertion) {
			StartIP = startip;
			Size = size;
			NewSize = newsize;
			Code = code;
			BeforeInsertion = beforeinsertion;
		}
		public FixData( final FixData fixdata ) {
			//Code = null;
			copy( fixdata );
		}
		public void copy( final FixData fixdata ) {
			Code = null;
			StartIP = fixdata.StartIP;
			Size = fixdata.Size;
			NewSize = fixdata.NewSize;
			BeforeInsertion = fixdata.BeforeInsertion;

			short[] newbuff = new short[NewSize];
			//ByteBuffer buff = ByteBuffer.allocate(NewSize*2);
			//buff.order( ByteOrder.nativeOrder() );
			//ShortBuffer ibuff = buff.asShortBuffer();
			ShortBuffer ibuff = ShortBuffer.wrap(newbuff);
			ibuff.clear();
			ShortBuffer tmp = fixdata.Code.duplicate();
			tmp.flip();
			ibuff.put( tmp );
			Code = ibuff;
		}
	}
	static class NonLocalFunctionDecl {
		int DataPos;
		int NameDataPos;
		boolean ChangeThis;
		public NonLocalFunctionDecl( int datapos, int namedatapos, boolean changethis ){
			DataPos = datapos;
			NameDataPos = namedatapos;
			ChangeThis = changethis;
		}
	};
	//private static final int fatNormal = 0, fatExpand = 1, fatUnnamedExpand = 2;
	static class FuncArgItem {
		public int Register;
		public int Type; // tTJSFuncArgType Type;
		public FuncArgItem( int reg ) {
			this( reg, fatNormal );
		}
		public FuncArgItem( int reg, int type ) {
			Register = reg;
			Type = type;
		}
	}
	static class FuncArg {
		public boolean IsOmit;
		public boolean HasExpand;
		public ArrayList<FuncArgItem> ArgVector;
		public FuncArg() {
			//IsOmit = HasExpand = false;
			ArgVector = new ArrayList<FuncArgItem>();
		}
	}
	static class ArrayArg {
		int Object;
		int Counter;
	}

	public InterCodeGenerator( InterCodeGenerator parent, String name, Compiler block, int type ) throws VariantException {
		//super(getContextHashSize(type));
		//super.mCallFinalize = false;

		mNodeToDeleteVector = new VectorWrap<ExprNode>();
		mCurrentNodeVector = new VectorWrap<ExprNode>();

		mJumpList = new IntVector();
		mNestVector = new VectorWrap<NestData>();
		mArrayArgStack = new Stack<ArrayArg>();
		mFuncArgStack = new Stack<FuncArg>();
		mNamespace = new LocalNamespace();
		mFixList = new ArrayList<FixData>();
		mNonLocalFunctionDeclVector = new VectorWrap<NonLocalFunctionDecl>();
		mSuperClassGetterPointer = new IntVector();

		mParent = parent;

		mPropGetter = mPropSetter = mSuperClassGetter = null;

		mCodeArea = new short[INC_ARRAY_COUNT];
		mDataArea = new ArrayList<Variant>();
		mInterCodeDataArea = new ArrayList<Variant>();

		mFrameBase = 1;

		//mSuperClassExpr = null;

		//mMaxFrameCount = 0;
		//mMaxVariableCount = 0;

		//mFuncDeclArgCount = 0;
		//mFuncDeclUnnamedArgArrayBase = 0;
		mFuncDeclCollapseBase = -1;

		//mFunctionRegisterCodePoint = 0;

		mPrevSourcePos = -1;
		//mSourcePosArraySorted = false;
		//mSourcePosArray = null;

		if(name != null && name.length() > 0 ) {
			mName = name;
		}
		//else {
		//	mName = null;
		//}
		//mAsGlobalContextMode = false;
		mContextType = type;
		switch(mContextType) // decide variable reservation count with context type
		{
			case ContextType.TOP_LEVEL:			mVariableReserveCount = 2; break;
			case ContextType.FUNCTION:			mVariableReserveCount = 2; break;
			case ContextType.EXPR_FUNCTION: 	mVariableReserveCount = 2; break;
			case ContextType.PROPERTY:			mVariableReserveCount = 0; break;
			case ContextType.PROPERTY_SETTER:	mVariableReserveCount = 2; break;
			case ContextType.PROPERTY_GETTER:	mVariableReserveCount = 2; break;
			case ContextType.CLASS:				mVariableReserveCount = 2; break;
			case ContextType.SUPER_CLASS_GETTER:mVariableReserveCount = 2; break;
		}
		mBlock = block;
		mBlock.add( this );

		if( mContextType == ContextType.CLASS ) {
			// add class information to the class instance information
			if( mMaxFrameCount < 1 ) mMaxFrameCount = 1;

			int dp = putData(new Variant(mName));
			int lexPos = getLexPos();
			// const %1, name
			// addci %-1, %1
			// cl %1
			/*
			putCode(VM_CONST, lexPos);
			putCode(1);
			putCode(dp);
			putCode(VM_ADDCI);
			putCode(-1);
			putCode(1);
			putCode(VM_CL);
			putCode(1);
			*/
			if( (mCodeAreaPos+7) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(lexPos);
			mCodeArea[mCodeAreaPos] = (short)VM_CONST; mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)1; mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)dp; mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)VM_ADDCI; mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)-1; mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)1; mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)VM_CL; mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)1; mCodeAreaPos++;

			// update FunctionRegisterCodePoint
			mFunctionRegisterCodePoint = mCodeAreaPos; // update FunctionRegisterCodePoint
		}
	}
	protected void finalizeObject() throws VariantException, TJSException {
		if( mPropSetter != null ) mPropSetter = null;
		if( mPropGetter != null ) mPropGetter = null;
		if( mSuperClassGetter != null ) mSuperClassGetter = null;
		if( mCodeArea != null ) { /*mCodeArea.clear();*/ mCodeArea = null; }
		if( mDataArea != null ) {
			mDataArea.clear();
			mDataArea = null;
		}
		mBlock.remove(this);

		if( mContextType!=ContextType.TOP_LEVEL && mBlock != null ) mBlock = null;

		mNamespace.clear();

		clearNodesToDelete();

		/*
		if( mSourcePosArray != null ) {
			mSourcePosArray.clear();
			mSourcePosArray = null;
		}
		*/
		//super.finalizeObject();
	}
	public final String getName(){ return mName; }
	private final void error( final String msg ) throws CompileException {
		mBlock.error(msg);
	}
	private int getLexPos() {
		return mBlock.getLexicalAnalyzer().getCurrentPosition();
	}
	public int getNodeToDeleteVectorCount() { return mNodeToDeleteVector.size(); }

	public void pushCurrentNode( ExprNode node ) {
		mCurrentNodeVector.add( node );
	}
	public ExprNode getCurrentNode() {
		if( mCurrentNodeVector.size() == 0 ) return null;
		return mCurrentNodeVector.get( mCurrentNodeVector.size()-1 );
	}
	public void popCurrentNode() {
		final int count = mCurrentNodeVector.size();
		if( count > 0 ) {
			mCurrentNodeVector.remove( count - 1 );
		}
	}
	//private int putCode( int num ) { return putCode( num, -1 ); }
	private void expandCodeArea() {
		final int capacity = mCodeArea.length;
		final int newcount = capacity + INC_ARRAY_COUNT;
		short[] narray = new short[newcount];
		System.arraycopy(mCodeArea, 0, narray, 0, capacity );
		mCodeArea = null;
		mCodeArea = narray;
	}
	private void putSrcPos( int pos ) {
		if( pos == -1 ) return;
		if( mPrevSourcePos != pos ) {
			mPrevSourcePos = pos;
			mSourcePosArraySorted = false;
			if( mSourcePosArray == null ) mSourcePosArray = new long[INC_ARRAY_COUNT];
			else if( mSrcPosArrayPos >= mSourcePosArray.length ) {
				final int newcount = mSourcePosArray.length + INC_ARRAY_COUNT;
				long[] narray = new long[newcount];
				System.arraycopy( mSourcePosArray, 0, narray, 0, mSrcPosArrayPos );
				mSourcePosArray = narray;
			}
			mSourcePosArray[mSrcPosArrayPos] = ( ((long)mCodeAreaPos << 32) | (long)pos );
			mSrcPosArrayPos++;
		}
	}
	/*
	private int putCode( int num, int pos ) {
		final int capacity = mCodeArea.length;
		if( mCodeAreaPos >= capacity ) {
			final int newcount = capacity + INC_ARRAY_COUNT;
			short[] narray = new short[newcount];
			System.arraycopy(mCodeArea, 0, narray, 0, capacity );
			mCodeArea = null;
			mCodeArea = narray;
		}
		if( pos != -1 && CompileState.mEnableDebugCode ) {
			if( mPrevSourcePos != pos ) {
				mPrevSourcePos = pos;
				mSourcePosArraySorted = false;
				if( mSourcePosArray == null ) mSourcePosArray = new long[INC_ARRAY_COUNT];
				else if( mSrcPosArrayPos >= mSourcePosArray.length ) {
					final int newcount = mSourcePosArray.length + INC_ARRAY_COUNT;
					long[] narray = new long[newcount];
					System.arraycopy( mSourcePosArray, 0, narray, 0, mSrcPosArrayPos );
					mSourcePosArray = narray;
				}
				mSourcePosArray[mSrcPosArrayPos] = ( ((long)mCodeAreaPos << 32) | (long)pos );
				mSrcPosArrayPos++;
			}
		}
		int ret = mCodeAreaPos;
		mCodeArea[mCodeAreaPos] = (short)num; mCodeAreaPos++;
		return ret;
	}
	*/
	private int putData( final Variant val ) throws VariantException {
		// 直近の20個の中で同じものがあるか調べる TODO 別コンパイルにするのなら、全データでチェックするようにした方がいいか
		final int size = mDataArea.size();
		int count = size > SEARCH_CONST_VAL_SIZE ? SEARCH_CONST_VAL_SIZE : size;
		final int offset = size  - 1;
		for( int i = 0; i < count; i++ ) {
			int pos = offset - i;
			if( mDataArea.get( pos ).discernCompareStrictReal(val) ) {
				return pos;
			}
		}
		Variant v;
		if( val.isString() ) {
			v = new Variant( TJS.mapGlobalStringMap( val.asString() ) );
		} else {
			v = new Variant ( val );
			Object o = v.toJavaObject();
			if( o instanceof InterCodeGenerator ) {
				mInterCodeDataArea.add(v);
			}
		}
		mDataArea.add( v );
		return mDataArea.size() - 1;
	}
	/**
	 * DaraArray の中の InterCodeGenerator を InterCodeObject に差し替える
	 * @param compiler
	 */
	public void dateReplace( Compiler compiler ) {
		final int count = mInterCodeDataArea.size();
		for( int i = 0; i < count; i++ ) {
			Variant d = mInterCodeDataArea.get(i);
			Object o = d.toJavaObject();
			if( o instanceof InterCodeGenerator ) {
				int idx = compiler.getCodeIndex((InterCodeGenerator) o);
				if( idx < 0 ) {
					TJS.outputToConsole( "not found" );
				}
				d.set( compiler.getCodeObject(idx) );
			}
		}
	}

	public void addLocalVariable( final String name ) throws VariantException { addLocalVariable( name, 0 ); }
	public void addLocalVariable( final String name, int init ) throws VariantException {
		int base = mContextType == ContextType.CLASS ? 2 : 1;
		int lexPos = getLexPos();
		if( mNamespace.getLevel() >= base ) {
			mNamespace.add(name);
			if( init != 0 ) {
				int n = mNamespace.find(name);
				//putCode( VM_CP, lexPos );
				//putCode( -n-mVariableReserveCount-1, lexPos );
				//putCode( init, lexPos );
				if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(lexPos);
				mCodeArea[mCodeAreaPos] = (short)VM_CP; mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(-n-mVariableReserveCount-1); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)init; mCodeAreaPos++;
			} else {
				int n = mNamespace.find(name);
				//putCode( VM_CL, lexPos );
				//putCode( -n-mVariableReserveCount-1, lexPos );
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(lexPos);
				mCodeArea[mCodeAreaPos] = (short)VM_CL; mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(-n-mVariableReserveCount-1); mCodeAreaPos++;
			}
		} else {
			int dp = putData(new Variant(name));
			//putCode( VM_SPDS, lexPos );
			//putCode( -1, lexPos );
			//putCode( dp, lexPos );
			//putCode( init, lexPos );
			if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(lexPos);
			mCodeArea[mCodeAreaPos] = (short)VM_SPDS; mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(-1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)dp; mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)init; mCodeAreaPos++;
		}
	}
	public void initLocalVariable( final String name, ExprNode node ) throws VariantException, CompileException  {
		IntWrapper fr = new IntWrapper(mFrameBase);
		int resaddr = genNodeCode( fr, node, RT_NEEDED, 0, new SubParam() );
		addLocalVariable(name,resaddr);
		clearFrame(fr);
	}
	public void initLocalFunction( final String name, int data ) throws VariantException {
		// create a local function variable pointer by data ( in DataArea ),
		// named "name".
		int fr = mFrameBase;
		int pos = getLexPos();
		//putCode(VM_CONST, pos );
		//putCode( fr, pos );
		//putCode( data);
		if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
		if( CompileState.mEnableDebugCode ) putSrcPos(pos);
		mCodeArea[mCodeAreaPos] = (short)VM_CONST; mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)fr; mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)data; mCodeAreaPos++;
		fr++;
		addLocalVariable( name, fr-1 );
		clearFrame(fr);
	}
	public void createExprCode( ExprNode node ) throws VariantException, CompileException {
		// create code of node
		IntWrapper fr = new IntWrapper(mFrameBase);
		genNodeCode(fr, node, 0, 0, new SubParam());
		clearFrame(fr);
	}
	public void enterWhileCode( boolean doWhile ) {
		// enter to "while"
		// ( do_while = true indicates do-while syntax )
		mNestVector.add( new NestData() );
		mNestVector.lastElement().Type = doWhile ? ntDoWhile : ntWhile;
		mNestVector.lastElement().LoopStartIP = mCodeAreaPos;
	}
	public void createWhileExprCode( ExprNode node, boolean doWhile ) throws VariantException, CompileException {
		// process the condition expression "node"
		if( doWhile ) {
			doContinuePatch( mNestVector.lastElement() );
		}

		IntWrapper fr = new IntWrapper(mFrameBase);
		int resaddr = genNodeCode( fr, node, RT_NEEDED|RT_CFLAG, 0, new SubParam() );
		int nodepos = (node != null ? node.getPosition() : -1);
		boolean inv = false;
		if( !(resaddr == GNC_CFLAG || resaddr == GNC_CFLAG_I ) ) {
			//putCode( VM_TT, nodepos );
			//putCode( resaddr, nodepos );
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)VM_TT; mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)resaddr; mCodeAreaPos++;
		} else {
			if(resaddr == GNC_CFLAG_I) inv = true;
		}
		clearFrame(fr);

		if( !doWhile ) {
			mNestVector.lastElement().ExitPatchVector.add(mCodeAreaPos);
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			//putCode(inv?VM_JF:VM_JNF, nodepos);
			//putCode(0, nodepos);
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(inv?VM_JF:VM_JNF); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)0; mCodeAreaPos++;
		} else {
			int jmp_ip = mCodeAreaPos;
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			//putCode(inv?VM_JNF:VM_JF, nodepos);
			//putCode(mNestVector.lastElement().LoopStartIP - jmp_ip, nodepos);
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(inv?VM_JNF:VM_JF); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(mNestVector.lastElement().LoopStartIP - jmp_ip); mCodeAreaPos++;
		}
	}
	public void exitWhileCode( boolean doWhile ) throws CompileException {
		// exit from "while"
		if( mNestVector.size() == 0 ) {
			error(Error.SyntaxError);
			return;
		}
		if( doWhile ) {
			if( mNestVector.lastElement().Type != ntDoWhile) {
				error(Error.SyntaxError);
				return;
			}
		} else {
			if( mNestVector.lastElement().Type != ntWhile ) {
				error(Error.SyntaxError);
				return;
			}
		}

		if( !doWhile ) {
			int jmp_ip = mCodeAreaPos;
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			int pos = getLexPos();
			//putCode(VM_JMP, pos );
			//putCode( mNestVector.lastElement().LoopStartIP - jmp_ip, pos );
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(mNestVector.lastElement().LoopStartIP - jmp_ip); mCodeAreaPos++;
		}
		doNestTopExitPatch();
		mNestVector.remove( mNestVector.size()-1 );
	}
	public void enterIfCode() {
		// enter to "if"
		mNestVector.add( new NestData() );
		mNestVector.lastElement().Type = ntIf;
	}
	public void crateIfExprCode( ExprNode node ) throws VariantException, CompileException {
		// process condition expression "node"
		IntWrapper fr = new IntWrapper(mFrameBase);
		int resaddr = genNodeCode(fr, node, RT_NEEDED|RT_CFLAG, 0, new SubParam());
		int nodepos = (node != null ? node.getPosition() : -1);
		boolean inv = false;
		if(!(resaddr == GNC_CFLAG || resaddr == GNC_CFLAG_I)) {
			//putCode(VM_TT, nodepos );
			//putCode(resaddr, nodepos );
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_TT); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
		} else {
			if(resaddr == GNC_CFLAG_I) inv = true;
		}
		clearFrame(fr);
		mNestVector.lastElement().Patch1 = mCodeAreaPos;
		//addJumpList();
		mJumpList.add( mCodeAreaPos );
		//putCode(inv?VM_JF:VM_JNF, nodepos );
		//putCode(0, nodepos );
		if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
		if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
		mCodeArea[mCodeAreaPos] = (short)(inv?VM_JF:VM_JNF); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
	}
	public void exitIfCode() throws CompileException {
		// exit from if
		if( mNestVector.size() == 0 ) {
			error(Error.SyntaxError);
			return;
		}
		if( mNestVector.lastElement().Type != ntIf) {
			error(Error.SyntaxError);
			return;
		}

		mCodeArea[mNestVector.lastElement().Patch1 + 1] = (short) (mCodeAreaPos - mNestVector.lastElement().Patch1);
		mPrevIfExitPatch = mNestVector.lastElement().Patch1;
		mNestVector.remove( mNestVector.size()-1 );
	}
	public void enterElseCode() {
		// enter to "else".
		// before is "if", is clear from syntax definition.
		mNestVector.add(new NestData());
		mNestVector.lastElement().Type = ntElse;
		mNestVector.lastElement().Patch2 = mCodeAreaPos;
		//addJumpList();
		mJumpList.add( mCodeAreaPos );
		int pos = getLexPos();
		//putCode(VM_JMP, pos);
		//putCode(0, pos);
		if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
		if( CompileState.mEnableDebugCode ) putSrcPos(pos);
		mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
		mCodeArea[mPrevIfExitPatch + 1] = (short) (mCodeAreaPos - mPrevIfExitPatch);
	}
	public void exitElseCode() throws CompileException {
		// exit from else
		if( mNestVector.size() == 0 ) {
			error(Error.SyntaxError);
			return;
		}
		if( mNestVector.lastElement().Type != ntElse ) {
			error(Error.SyntaxError);
			return;
		}

		mCodeArea[mNestVector.lastElement().Patch2 + 1] = (short) (mCodeAreaPos - mNestVector.lastElement().Patch2);
		mNestVector.remove( mNestVector.size()-1 );
	}
	public void enterForCode(boolean varcreate) {
		// enter to "for".
		// ( varcreate = true, indicates that the variable is to be created in the
		//	first clause )
		mNestVector.add( new NestData() );
		mNestVector.lastElement().Type = ntFor;
		if(varcreate) enterBlock(); // create a scope
		mNestVector.lastElement().VariableCreated = varcreate;
	}
	public void createForExprCode( ExprNode node ) throws VariantException, CompileException {
		// process the "for"'s second clause; a condition expression
		mNestVector.lastElement().LoopStartIP = mCodeAreaPos;
		if( node != null ) {
			int nodepos = node.getPosition();
			IntWrapper fr = new IntWrapper( mFrameBase );
			int resaddr = genNodeCode( fr, node, RT_NEEDED|RT_CFLAG, 0, new SubParam() );
			boolean inv = false;
			if(!(resaddr == GNC_CFLAG || resaddr == GNC_CFLAG_I) ) {
				//putCode(VM_TT, nodepos );
				//putCode(resaddr, nodepos );
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
				mCodeArea[mCodeAreaPos] = (short)(VM_TT); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
			} else {
				if(resaddr == GNC_CFLAG_I) inv = true;
			}
			clearFrame(fr);
			mNestVector.lastElement().ExitPatchVector.add(mCodeAreaPos);
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			//putCode(inv?VM_JF:VM_JNF, nodepos);
			//putCode( 0, nodepos );
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(inv?VM_JF:VM_JNF); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
		}
	}
	public void setForThirdExprCode( ExprNode node ) {
		// process the "for"'s third clause; a post-loop expression
		mNestVector.lastElement().PostLoopExpr = node;
	}
	public void exitForCode() throws CompileException, VariantException {
		// exit from "for"
		int nestsize = mNestVector.size();
		if(nestsize == 0) {
			error(Error.SyntaxError);
			return;
		}
		if(mNestVector.lastElement().Type != ntFor && mNestVector.lastElement().Type != ntBlock) {
			error(Error.SyntaxError);
			return;
		}

		if( mNestVector.lastElement().Type == ntFor)
			doContinuePatch(mNestVector.lastElement());
		if(nestsize >= 2 && mNestVector.get(nestsize-2).Type == ntFor)
			doContinuePatch( mNestVector.get(nestsize-2) );

		if( mNestVector.lastElement().PostLoopExpr != null ) {
			IntWrapper fr = new IntWrapper(mFrameBase);
			genNodeCode(fr, mNestVector.lastElement().PostLoopExpr, 0, 0, new SubParam());
			clearFrame(fr);
		}
		int jmp_ip = mCodeAreaPos;
		//addJumpList();
		mJumpList.add( mCodeAreaPos );
		int lexpos = getLexPos();
		//putCode(VM_JMP, lexpos );
		//putCode( mNestVector.lastElement().LoopStartIP - jmp_ip, lexpos );
		if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
		if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
		mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(mNestVector.lastElement().LoopStartIP - jmp_ip); mCodeAreaPos++;
		doNestTopExitPatch();
		if( mNestVector.lastElement().VariableCreated ) exitBlock();
		doNestTopExitPatch();
		mNestVector.remove( mNestVector.size()-1 );
	}
	public void enterSwitchCode( ExprNode node ) throws VariantException, CompileException {
		// enter to "switch"
		// "node" indicates a reference expression
		mNestVector.add(new NestData());
		mNestVector.lastElement().Type = ntSwitch;
		mNestVector.lastElement().Patch1 = -1;
		mNestVector.lastElement().Patch2 = -1;
		//mNestVector.lastElement().IsFirstCase = true; // IsFirstCase と VariableCreated は同一に扱う
		mNestVector.lastElement().VariableCreated = true;

		IntWrapper fr = new IntWrapper(mFrameBase);
		int resaddr = genNodeCode(fr, node, RT_NEEDED, 0, new SubParam());

		if( mFrameBase != resaddr ) {
			int nodepos = (node != null ? node.getPosition() : -1);
			//putCode(VM_CP, nodepos);
			//putCode(mFrameBase, nodepos ); // FrameBase points the reference value
			//putCode(resaddr, nodepos );
			if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(mFrameBase); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
		}

		mNestVector.lastElement().RefRegister = mFrameBase;

		if(fr.value-1 > mMaxFrameCount) mMaxFrameCount = fr.value-1;

		mFrameBase++; // increment FrameBase
		if(mFrameBase-1 > mMaxFrameCount) mMaxFrameCount = mFrameBase-1;

		clearFrame(fr);

		enterBlock();
	}
	public void exitSwitchCode() throws CompileException {
		// exit from switch
		exitBlock();
		if( mNestVector.size() == 0 ) {
			error(Error.SyntaxError);
			return;
		}
		if( mNestVector.lastElement().Type != ntSwitch ) {
			error(Error.SyntaxError);
			return;
		}

		int lexpos = getLexPos();
		int patch3 = 0;
		//if( !mNestVector.lastElement().IsFirstCase ) // IsFirstCase と VariableCreated は同一に扱う
		if( !mNestVector.lastElement().VariableCreated ) {
			patch3 = mCodeAreaPos;
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			//putCode( VM_JMP, lexpos );
			//putCode( 0, lexpos );
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
			mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
		}


		if( mNestVector.lastElement().Patch1 != -1 ) {
			mCodeArea[mNestVector.lastElement().Patch1 +1] = (short) (mCodeAreaPos - mNestVector.lastElement().Patch1);
		}

		if( mNestVector.lastElement().Patch2 != -1 ) {
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			int jmp_start = mCodeAreaPos;
			//putCode( VM_JMP, lexpos);
			//putCode( mNestVector.lastElement().Patch2 - jmp_start, lexpos);
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
			mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(mNestVector.lastElement().Patch2 - jmp_start); mCodeAreaPos++;
		}

		//if( !mNestVector.lastElement().IsFirstCase ) {
		if( !mNestVector.lastElement().VariableCreated ) {
			mCodeArea[ patch3 +1 ] = (short) (mCodeAreaPos - patch3);
		}

		doNestTopExitPatch();
		mFrameBase--;
		mNestVector.remove( mNestVector.size()-1 );
	}
	public void processCaseCode( ExprNode node ) throws VariantException, CompileException {
		// process "case expression :".
		// process "default :" if node == NULL.
		int nestsize = mNestVector.size();
		if( nestsize < 3 ) {
			error(Error.MisplacedCase);
			return;
		}
		if( mNestVector.get( nestsize-1 ).Type != ntBlock ||
			mNestVector.get( nestsize-2 ).Type != ntBlock ||
			mNestVector.get( nestsize-3 ).Type != ntSwitch ) {
			// the stack layout must be ( from top )
			// ntBlock, ntBlock, ntSwitch
			error(Error.MisplacedCase);
			return;
		}

		NestData data = mNestVector.get(mNestVector.size()-3);
		int patch3 = 0;
		//if( !data.IsFirstCase ) { // IsFirstCase と VariableCreated は同一に扱う
		if( !data.VariableCreated ) {
			patch3 = mCodeAreaPos;
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			int nodepos = (node != null ? node.getPosition() : -1);
			//putCode(VM_JMP, nodepos);
			//putCode(0, nodepos);
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
		}

		exitBlock();
		if( data.Patch1 != -1 ) {
			mCodeArea[data.Patch1 +1] = (short) (mCodeAreaPos - data.Patch1);
		}

		if( node != null ) {
			IntWrapper fr = new IntWrapper(mFrameBase);
			int resaddr = genNodeCode(fr, node, RT_NEEDED, 0, new SubParam());
			int nodepos = (node != null ? node.getPosition() : -1);
			//putCode( VM_CEQ, nodepos);
			//putCode( data.RefRegister, nodepos);
				// compare to reference value with normal comparison
			//putCode( resaddr, nodepos);
			if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_CEQ); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(data.RefRegister); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
			clearFrame(fr);
			data.Patch1 = mCodeAreaPos;
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			//putCode(VM_JNF, nodepos);
			//putCode(0, nodepos);
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_JNF); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
		} else {
			data.Patch1 = mCodeAreaPos;
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			int nodepos = (node != null ? node.getPosition() : -1);
			//putCode(VM_JMP, nodepos);
			//putCode(0, nodepos);
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;

			data.Patch2 = mCodeAreaPos; // Patch2 = "default:"'s position
		}

		//if( !data.IsFirstCase ) {
		if( !data.VariableCreated ) {
			mCodeArea[patch3 +1] = (short) (mCodeAreaPos - patch3);
		}
		//data.IsFirstCase = false;
		data.VariableCreated = false;

		enterBlock();
	}
	public void enterWithCode( ExprNode node ) throws VariantException, CompileException {
		// enter to "with"
		// "node" indicates a reference expression

		// this method and ExitWithCode are very similar to switch's code.
		// (those are more simple than that...)

		IntWrapper fr = new IntWrapper(mFrameBase);
		int resaddr = genNodeCode(fr, node, RT_NEEDED, 0, new SubParam());

		int nodepos = (node != null ? node.getPosition() : -1);
		if( mFrameBase != resaddr ) {
			// bring the reference variable to frame base top
			//putCode(VM_CP, nodepos);
			//putCode( mFrameBase, nodepos); // FrameBase points the reference value
			//putCode( resaddr, nodepos);
			if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(mFrameBase); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
		}

		mNestVector.add(new NestData());
		mNestVector.lastElement().Type = ntWith;

		mNestVector.lastElement().RefRegister = mFrameBase;

		if( fr.value-1 > mMaxFrameCount) mMaxFrameCount = fr.value-1;

		mFrameBase ++; // increment FrameBase
		if(mFrameBase-1 > mMaxFrameCount) mMaxFrameCount = mFrameBase-1;

		clearFrame(fr);

		enterBlock();
	}
	public void exitWidthCode() throws CompileException {
		// exit from switch
		exitBlock();
		if( mNestVector.size() == 0 ) {
			error(Error.SyntaxError);
			return;
		}
		if( mNestVector.lastElement().Type != ntWith ) {
			error(Error.SyntaxError);
			return;
		}
		mFrameBase--;
		mNestVector.remove( mNestVector.size()-1 );
	}
	public void doBreak() throws CompileException {
		// process "break".

		// search in NestVector backwards
		//int vc = mNamespace.getCount();
		//int pvc = vc;

		int lexpos = getLexPos();
		int i = mNestVector.size() -1;
		for( ; i>=0; i-- ) {
			NestData data = mNestVector.get(i);
			if( data.Type == ntSwitch ||
				data.Type == ntWhile || data.Type == ntDoWhile ||
				data.Type == ntFor ) {
				// "break" can apply on this syntax
				data.ExitPatchVector.add( mCodeAreaPos );
				//addJumpList();
				mJumpList.add( mCodeAreaPos );
				//putCode(VM_JMP, lexpos);
				//putCode(0, lexpos); // later patches here
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
				mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
				return;
			} else if(data.Type == ntBlock) {
				//pvc = data.VariableCount;
			} else if(data.Type == ntTry) {
				//putCode(VM_EXTRY, -1 );
				if( (mCodeAreaPos) >= mCodeArea.length ) expandCodeArea();
				mCodeArea[mCodeAreaPos] = (short)(VM_EXTRY); mCodeAreaPos++;
			} else if(data.Type == ntSwitch || data.Type == ntWith) {
				// clear reference register of "switch" or "with" syntax
			}
		}

		error( Error.MisplacedBreakContinue );
	}
	public void doContinue() throws CompileException {
		// process "continue".

		// generate code that jumps before '}' ( the end of the loop ).
		// for "while" loop, the jump code immediately jumps to the condition check code.

		// search in NestVector backwards
		//int vc = mNamespace.getCount();
		//int pvc = vc;
		int i = mNestVector.size() - 1;
		int lexpos = getLexPos();
		for( ; i>=0; i-- ) {
			NestData data = mNestVector.get(i);
			if( data.Type == ntWhile ) {
				// for "while" loop
				int jmpstart = mCodeAreaPos;
				//addJumpList();
				mJumpList.add( mCodeAreaPos );
				//putCode(VM_JMP, lexpos);
				//putCode(data.LoopStartIP - jmpstart, lexpos);
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
				mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(data.LoopStartIP - jmpstart); mCodeAreaPos++;
				return;
			} else if( data.Type == ntDoWhile || data.Type == ntFor ) {
				// "do-while" or "for" loop needs forward jump
				data.ContinuePatchVector.add( mCodeAreaPos );
				//addJumpList();
				mJumpList.add( mCodeAreaPos );
				//putCode(VM_JMP, lexpos);
				//putCode(0, lexpos); // later patch this
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
				mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
				return;
			} else if( data.Type == ntBlock ) {
				// does not count variables which created at for loop's
				// first clause
				//if( i < 1 || mNestVector.get( i-1 ).Type != ntFor || !mNestVector.get(i).VariableCreated )
					//pvc = data.VariableCount;
			} else if(data.Type == ntTry) {
				//putCode(VM_EXTRY, lexpos);
				if( (mCodeAreaPos) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
				mCodeArea[mCodeAreaPos] = (short)(VM_EXTRY); mCodeAreaPos++;
			} else if(data.Type == ntSwitch || data.Type == ntWith) {
				// clear reference register of "switch" or "with" syntax
			}
		}
		error( Error.MisplacedBreakContinue );
	}
	public void doDebugger() {
		// process "debugger" statement.
		//putCode(VM_DEBUGGER, getLexPos() );
		if( (mCodeAreaPos) >= mCodeArea.length ) expandCodeArea();
		if( CompileState.mEnableDebugCode ) putSrcPos(getLexPos());
		mCodeArea[mCodeAreaPos] = (short)(VM_DEBUGGER); mCodeAreaPos++;
	}
	public void returnFromFunc( ExprNode node ) throws VariantException, CompileException {
		// precess "return"
		// note: the "return" positioned in global immediately returns without
		// execution of the remainder code.

		int nodepos = (node != null ? node.getPosition() : -1);
		if( node == null ) {
			// no return value
			//putCode( VM_SRV, nodepos );
			//putCode( 0, nodepos );  // returns register #0 = void
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_SRV); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
		} else {
			// generates return expression
			IntWrapper fr = new IntWrapper(mFrameBase);
			int resaddr = genNodeCode( fr, node, RT_NEEDED, 0, new SubParam() );
			//putCode(VM_SRV, nodepos);
			//putCode( resaddr, nodepos);
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_SRV); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
			clearFrame(fr);
		}

		// clear the frame
		int org_framebase = mFrameBase;
		clearFrame(mFrameBase, 1);
		mFrameBase = org_framebase;

		int lexpos = getLexPos();
		// check try block
		int i = mNestVector.size() -1;
		for( ; i>=0; i-- ) {
			NestData data = mNestVector.get(i);
			if(data.Type == ntTry) {
				//putCode(VM_EXTRY, lexpos); // exit from try-protected block
				if( (mCodeAreaPos) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
				mCodeArea[mCodeAreaPos] = (short)(VM_EXTRY); mCodeAreaPos++;
			}
		}
		//putCode(VM_RET, lexpos);
		if( (mCodeAreaPos) >= mCodeArea.length ) expandCodeArea();
		if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
		mCodeArea[mCodeAreaPos] = (short)(VM_RET); mCodeAreaPos++;
	}
	public void enterTryCode() {
		// enter to "try"
		mNestVector.add(new NestData());
		mNestVector.lastElement().Type = ntTry;
		mNestVector.lastElement().VariableCreated = false;

		int lexpos = getLexPos();
		mNestVector.lastElement().Patch1 = mCodeAreaPos;
		//addJumpList();
		mJumpList.add( mCodeAreaPos );
		//putCode(VM_ENTRY, lexpos);
		//putCode(0, lexpos);
		//putCode( mFrameBase, lexpos); // an exception object will be held here
		if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
		if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
		mCodeArea[mCodeAreaPos] = (short)(VM_ENTRY); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(mFrameBase); mCodeAreaPos++;

		if( mFrameBase > mMaxFrameCount ) mMaxFrameCount = mFrameBase;
	}
	public void enterCatchCode( final String name ) throws VariantException {
		// enter to "catch"
		int lexpos = getLexPos();
		//putCode(VM_EXTRY, lexpos);
		if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
		if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
		mCodeArea[mCodeAreaPos] = (short)(VM_EXTRY); mCodeAreaPos++;
		mNestVector.lastElement().Patch2 = mCodeAreaPos;
		//addJumpList();
		mJumpList.add( mCodeAreaPos );
		//putCode(VM_JMP, lexpos);
		//putCode(0, lexpos);
		mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;

		mCodeArea[mNestVector.lastElement().Patch1 + 1] = (short) (mCodeAreaPos - mNestVector.lastElement().Patch1);

		// clear frame
		int fr = mMaxFrameCount + 1;
		int base = name != null ? mFrameBase+1 : mFrameBase;
		clearFrame(fr, base);

		// change nest type to ntCatch
		mNestVector.lastElement().Type = ntCatch;

		// create variable if the catch clause has a receiver variable name
		if( name != null ) {
			mNestVector.lastElement().VariableCreated = true;
			enterBlock();
			addLocalVariable(name, mFrameBase);
				// cleate a variable that receives the exception object
		}
	}
	public void exitTryCode() throws CompileException {
		// exit from "try"
		if( mNestVector.size() >= 2 ) {
			if( mNestVector.get(mNestVector.size()-2).Type == ntCatch ) {
				if( mNestVector.get(mNestVector.size()-2).VariableCreated ) {
					exitBlock();
				}
			}
		}
		if( mNestVector.size() == 0 ) {
			error(Error.SyntaxError);
			return;
		}
		if( mNestVector.lastElement().Type != ntCatch ) {
			error(Error.SyntaxError);
			return;
		}
		int p2addr = mNestVector.lastElement().Patch2;
		mCodeArea[ p2addr + 1 ] = (short) (mCodeAreaPos - p2addr);
		mNestVector.remove( mNestVector.size()-1 );
	}
	public void processThrowCode( ExprNode node ) throws VariantException, CompileException {
		// process "throw".
		// node = expressoin to throw
		IntWrapper fr = new IntWrapper(mFrameBase);
		int resaddr = genNodeCode(fr, node, RT_NEEDED, 0, new SubParam() );
		int nodepos = (node != null ? node.getPosition() : -1);
		//putCode(VM_THROW, nodepos);
		//putCode(resaddr, nodepos);
		if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
		if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
		mCodeArea[mCodeAreaPos] = (short)(VM_THROW); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
		if(fr.value-1 > mMaxFrameCount) mMaxFrameCount = fr.value-1;
	}
	public void createExtendsExprCode( ExprNode node, boolean hold ) throws VariantException, CompileException {
		// process class extender
		IntWrapper fr = new IntWrapper(mFrameBase);
		int resaddr = genNodeCode(fr, node, RT_NEEDED, 0, new SubParam() );

		int nodepos = (node != null ? node.getPosition() : -1);
		if( (mCodeAreaPos+6) >= mCodeArea.length ) expandCodeArea();
		if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
		//putCode(VM_CHGTHIS, nodepos);
		//putCode(resaddr, nodepos);
		//putCode(-1, nodepos);
		mCodeArea[mCodeAreaPos] = (short)(VM_CHGTHIS); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(-1); mCodeAreaPos++;

		//putCode(VM_CALL, nodepos);
		//putCode(0, nodepos);
		//putCode(resaddr, nodepos);
		//putCode(0, nodepos);
		mCodeArea[mCodeAreaPos] = (short)(VM_CALL); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;

		if( hold ) {
			mSuperClassExpr = node;
		}
		mFunctionRegisterCodePoint = mCodeAreaPos; // update FunctionRegisterCodePoint

		// create a Super Class Proxy context
		if( mSuperClassGetter == null ) {
			mSuperClassGetter = new InterCodeGenerator(this, mName, mBlock, ContextType.SUPER_CLASS_GETTER );
		}
		mSuperClassGetter.createExtendsExprProxyCode(node);
	}
	public void createExtendsExprProxyCode( ExprNode node ) throws VariantException, CompileException {
		// create super class proxy to retrieve super class
		mSuperClassGetterPointer.add(mCodeAreaPos);

		IntWrapper fr = new IntWrapper(mFrameBase);
		int resaddr = genNodeCode(fr, node, RT_NEEDED, 0, new SubParam() );

		//putCode(VM_SRV);
		//putCode(resaddr);
		if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
		mCodeArea[mCodeAreaPos] = (short)(VM_SRV); mCodeAreaPos++;
		mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
		//put1OperandCode( VM_SRV, resaddr, -1 );
		clearFrame(fr);

		//putCode(VM_RET, -1 );
		mCodeArea[mCodeAreaPos] = (short)(VM_RET); mCodeAreaPos++;

		int nodepos = (node != null ? node.getPosition() : -1);
		//putCode(VM_NOP, nodepos);
		if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
		mCodeArea[mCodeAreaPos] = (short)(VM_NOP); mCodeAreaPos++;
	}
	public void enterBlock() {
		// enter to block
		mNamespace.push();
		int varcount = mNamespace.getCount();
		mNestVector.add(new NestData());
		mNestVector.lastElement().Type = ntBlock;
		mNestVector.lastElement().VariableCount = varcount;
	}
	public void exitBlock() throws CompileException {
		// exit from block
		if( mNestVector.size() == 0 ) {
			error(Error.SyntaxError);
			return;
		}
		if( mNestVector.lastElement().Type != ntBlock ) {
			error(Error.SyntaxError);
			return;
		}

		mNestVector.remove( mNestVector.size()-1 );
		//int prevcount = mNamespace.getCount();
		mNamespace.pop();
		//int curcount = mNamespace.getCount();
	}
	public void generateFuncCallArgCode() {
		int lexpos = getLexPos();
		if( mFuncArgStack.peek().IsOmit ) {
			//putCode(-1, lexpos); // omit (...) is specified
			if( (mCodeAreaPos) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
			mCodeArea[mCodeAreaPos] = (short)(-1); mCodeAreaPos++;
		} else if( mFuncArgStack.peek().HasExpand ) {
			//putCode(-2, lexpos); // arguments have argument expanding node
			ArrayList<FuncArgItem> vec = mFuncArgStack.peek().ArgVector;
			if( (mCodeAreaPos+(vec.size()*2)+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
			mCodeArea[mCodeAreaPos] = (short)(-2); mCodeAreaPos++;
			//putCode(vec.size(), lexpos); // count of the arguments
			mCodeArea[mCodeAreaPos] = (short)(vec.size()); mCodeAreaPos++;
			for(int i=0; i<vec.size(); i++) {
				//putCode(vec.get(i).Type, lexpos);
				//putCode(vec.get(i).Register, lexpos);
				FuncArgItem arg = vec.get(i);
				mCodeArea[mCodeAreaPos] = (short)(arg.Type); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(arg.Register); mCodeAreaPos++;
			}
		} else {
			ArrayList<FuncArgItem> vec = mFuncArgStack.peek().ArgVector;
			if( (mCodeAreaPos+vec.size()) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
			//putCode(vec.size(), lexpos); // count of arguments
			mCodeArea[mCodeAreaPos] = (short)(vec.size()); mCodeAreaPos++;
			for( int i=0; i<vec.size(); i++) {
				//putCode( vec.get(i).Register, lexpos);
				mCodeArea[mCodeAreaPos] = (short)(vec.get(i).Register); mCodeAreaPos++;
			}
		}
	}
	public void addFunctionDeclArg( final String varname, ExprNode node ) throws VariantException, CompileException {
		// process the function argument of declaration
		// varname = argument name
		// init = initial expression

		mNamespace.add(varname);

		if( node != null ) {
			int nodepos = (node != null ? node.getPosition() : -1);
			//putCode(VM_CDEQ, nodepos);
			//putCode(-3 - mFuncDeclArgCount, nodepos);
			//putCode(0, nodepos);
			if( (mCodeAreaPos+4) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_CDEQ); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(-3 - mFuncDeclArgCount); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;

			int jmp_ip = mCodeAreaPos;
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			//putCode(VM_JNF, nodepos);
			//putCode(0, nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_JNF); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;

			IntWrapper fr = new IntWrapper(mFrameBase);
			int resaddr = genNodeCode(fr, node, RT_NEEDED, 0, new SubParam());
			//putCode(VM_CP, nodepos);
			//putCode(-3 - mFuncDeclArgCount, nodepos);
			//putCode(resaddr, nodepos);
			if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(nodepos);
			mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(-3 - mFuncDeclArgCount); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
			clearFrame(fr);

			mCodeArea[jmp_ip+1] = (short) (mCodeAreaPos - jmp_ip);

		}
		mFuncDeclArgCount++;
	}
	public void addFunctionDeclArgCollapse( final String varname ) {
		// process the function "collapse" argument of declaration.
		// collapse argument is available to receive arguments in array object form.
		if( varname == null ) {
			// receive arguments in unnamed array
			mFuncDeclUnnamedArgArrayBase = mFuncDeclArgCount;
		} else {
			// receive arguments in named array
			mFuncDeclCollapseBase = mFuncDeclArgCount;
			mNamespace.add(varname);
		}
	}
	public void setPropertyDeclArg( final String varname ) {
		// process the setter argument
		mNamespace.add(varname);
		mFuncDeclArgCount = 1;
	}
	private void doNestTopExitPatch() {
		// process the ExitPatchList which must be in the top of NextVector
		IntVector vector = mNestVector.lastElement().ExitPatchVector;
		final int count = vector.size();
		final int codeSize = mCodeAreaPos;
		for( int i = 0; i < count; i++ ) {
			int val = vector.get(i);
			mCodeArea[ val+1] = (short) (codeSize - val);
		}
	}
	private void doContinuePatch(NestData nestdata) {
		// process the ContinuePatchList which must be in the top of NextVector
		IntVector vector = nestdata.ContinuePatchVector;
		final int count = vector.size();
		final int codeSize = mCodeAreaPos;
		for( int i = 0; i < count; i++ ) {
			int val = vector.get(i);
			mCodeArea[val+1] = (short) (codeSize - val);
		}
	}

	public ExprNode makeConstValNode( Variant val ) {
		ExprNode node = new ExprNode();
		mNodeToDeleteVector.add( node );
		node.setOpecode( Token.T_CONSTVAL );
		node.setValue( val );
		node.setPosition( getLexPos() );
		return node;
	}
	public static void characterCodeFrom( Variant val ) throws VariantException {
		char ch[] = new char[1];
		ch[0] = (char) val.asInteger();
		val.set( new String(ch) );
	}
	public static void characterCodeOf( Variant val ) throws VariantException {
		String str = val.asString();
		if( str != null ) {
			int v = str.codePointAt(0);
			val.set( v );
		} else {
			val.set( 0 );
		}
	}

	public ExprNode makeNP0( int opecode ) {
		ExprNode node = new ExprNode();
		mNodeToDeleteVector.add( node );
		node.setOpecode(opecode);
		node.setPosition( getLexPos() );
		return node;
	}
	public ExprNode makeNP1( int opecode, ExprNode node1 ) throws VariantException {
		// 定数の最適化
		if( node1 != null && node1.getOpecode() == Token.T_CONSTVAL ) {
			ExprNode ret = null;
			switch( opecode ) {
			case Token.T_EXCRAMATION:
				ret = makeConstValNode( node1.getValue().getNotValue() );
				break;
			case Token.T_TILDE:
				ret = makeConstValNode( node1.getValue().getBitNotValue() );
				break;
			case Token.T_SHARP: {
				Variant val = new Variant(node1.getValue() );
				characterCodeOf(val);
				ret = makeConstValNode( val );
				break;
			}
			case Token.T_DOLLAR: {
				Variant val = new Variant(node1.getValue() );
				characterCodeFrom(val);
				ret = makeConstValNode( val );
				break;
			}
			case Token.T_UPLUS: {
				Variant val = new Variant(node1.getValue() );
				val.toNumber();
				ret = makeConstValNode( val );
				break;
			}
			case Token.T_UMINUS: {
				Variant val = new Variant(node1.getValue() );
				val.changeSign();
				ret = makeConstValNode( val );
				break;
			}
			case Token.T_INT: {
				Variant val = new Variant(node1.getValue() );
				val.toInteger();
				ret = makeConstValNode( val );
				break;
			}
			case Token.T_REAL: {
				Variant val = new Variant(node1.getValue() );
				val.toReal();
				ret = makeConstValNode( val );
				break;
			}
			case Token.T_STRING: {
				Variant val = new Variant(node1.getValue() );
				val.toString();
				ret = makeConstValNode( val );
				break;
			}
			case Token.T_OCTET: {
				Variant val = new Variant(node1.getValue() );
				val.toOctet();
				ret = makeConstValNode( val );
				break;
			}
			} // swtich
			if( ret != null ) {
				node1.clear();
				return ret;
			}
		}
		ExprNode node = new ExprNode();
		mNodeToDeleteVector.add( node );
		node.setOpecode(opecode);
		node.setPosition( getLexPos() );
		node.add( node1 );
		return node;
	}
	public ExprNode makeNP2( int opecode, ExprNode node1, ExprNode node2 ) throws VariantException {
		// 定数の最適化
		if( node1 != null && node1.getOpecode() == Token.T_CONSTVAL && node2 != null && node2.getOpecode() == Token.T_CONSTVAL ) {
			switch( opecode ) {
			case Token.T_COMMA:
				return makeConstValNode( node2.getValue() );
			case Token.T_LOGICALOR:
				return makeConstValNode( node1.getValue().logicOr( node2.getValue() ) );
			case Token.T_LOGICALAND:
				return makeConstValNode( node1.getValue().logicAnd( node2.getValue() ) );
			case Token.T_VERTLINE:
				return makeConstValNode( node1.getValue().bitOr( node2.getValue() ) );
			case Token.T_CHEVRON:
				return makeConstValNode( node1.getValue().bitXor( node2.getValue() ) );
			case Token.T_AMPERSAND:
				return makeConstValNode( node1.getValue().bitAnd( node2.getValue() ) );
			case Token.T_NOTEQUAL:
				return makeConstValNode( node1.getValue().notEqual( node2.getValue() ) );
			case Token.T_EQUALEQUAL:
				return makeConstValNode( node1.getValue().equalEqual( node2.getValue() ) );
			case Token.T_DISCNOTEQUAL:
				return makeConstValNode( node1.getValue().discNotEqual( node2.getValue() ) );
			case Token.T_DISCEQUAL:
				return makeConstValNode( node1.getValue().discernCompare( node2.getValue() ) );
			case Token.T_LT:
				return makeConstValNode( node1.getValue().lt( node2.getValue() ) );
			case Token.T_GT:
				return makeConstValNode( node1.getValue().gt( node2.getValue() ) );
			case Token.T_LTOREQUAL:
				return makeConstValNode( node1.getValue().ltOrEqual( node2.getValue() ) );
			case Token.T_GTOREQUAL:
				return makeConstValNode( node1.getValue().gtOrEqual( node2.getValue() ) );
			case Token.T_RARITHSHIFT:
				return makeConstValNode( node1.getValue().rightShift( node2.getValue() ) );
			case Token.T_LARITHSHIFT:
				return makeConstValNode( node1.getValue().leftShift( node2.getValue() ) );
			case Token.T_RBITSHIFT:
				return makeConstValNode( node1.getValue().rightBitShift( node2.getValue() ) );
			case Token.T_PLUS:
				return makeConstValNode( node1.getValue().add( node2.getValue() ) );
			case Token.T_MINUS:
				return makeConstValNode( node1.getValue().subtract( node2.getValue() ) );
			case Token.T_PERCENT:
				return makeConstValNode( node1.getValue().residue( node2.getValue() ) );
			case Token.T_SLASH:
				return makeConstValNode( node1.getValue().divide( node2.getValue() ) );
			case Token.T_BACKSLASH:
				return makeConstValNode( node1.getValue().idiv( node2.getValue() ) );
			case Token.T_ASTERISK:
				return makeConstValNode( node1.getValue().multiply( node2.getValue() ) );
			}
		}
		ExprNode node = new ExprNode();
		mNodeToDeleteVector.add( node );
		node.setOpecode(opecode);
		node.setPosition( getLexPos() );
		node.add( node1 );
		node.add( node2 );
		return node;
	}
	public ExprNode makeNP3( int opecode, ExprNode node1, ExprNode node2, ExprNode node3 ) {
		// 三項演算子の最適化とかはしていない？
		ExprNode node = new ExprNode();
		mNodeToDeleteVector.add( node );
		node.setOpecode(opecode);
		node.setPosition( getLexPos() );
		node.add( node1 );
		node.add( node2 );
		node.add( node3 );
		return node;
	}
	private int clearFrame( IntWrapper frame ) { return clearFrame( frame, -1); }
	private int clearFrame( IntWrapper frame, int base ) {
		if( base == -1 ) base = mFrameBase;
		if( (frame.value-1) > mMaxFrameCount ) mMaxFrameCount = frame.value - 1;
		if( (frame.value-base) >= 3 ) {
			frame.value = base;
		} else {
			/*
			while( frame > base ) { frame--; }
			*/
			if( frame.value > base ) frame.value = base;
		}
		return frame.value;
	}
	private int clearFrame( int frame ) { return clearFrame( frame, -1); }
	private int clearFrame( int frame, int base ) {
		if( base == -1 ) base = mFrameBase;
		if( (frame-1) > mMaxFrameCount ) mMaxFrameCount = frame - 1;
		if( (frame-base) >= 3 ) {
			frame = base;
		} else {
			if( frame > base ) frame = base;
		}
		return frame;
	}
	private int genNodeCode( IntWrapper frame, ExprNode node, int restype, int reqresaddr, SubParam param ) throws VariantException, CompileException {
		if( node == null ) return 0;

		int resaddr;
		int node_pos = ( node != null ? node.getPosition() : -1 );
		switch( node.getOpecode() ) {
		case Token.T_CONSTVAL:	// constant value
		{
			if( param.mSubType != stNone ) error(Error.CannotModifyLHS );
			if( (restype&RT_NEEDED) == 0 ) return 0;
			int dp = putData( node.getValue() );
			//putCode( VM_CONST, node_pos );
			//putCode( frame.value, node_pos );
			//putCode( dp, node_pos );
			if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_CONST); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(dp); mCodeAreaPos++;
			int ret = frame.value;
			frame.value++;
			return ret;
		}
		case Token.T_IF:	// 'if'
		{
			if( (restype&RT_NEEDED) != 0 ) error( Error.CannotGetResult );
			int resaddr1 = genNodeCode( frame, node.getNode(1), RT_NEEDED|RT_CFLAG, 0, new SubParam() );
			boolean inv = false;
			if( !(resaddr1==GNC_CFLAG||resaddr1==GNC_CFLAG_I) ) {
				//putCode( VM_TT, node_pos );
				//putCode( resaddr1, node_pos );
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(VM_TT); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
			} else {
				if( resaddr1 == GNC_CFLAG_I ) inv = true;
			}
			int addr = mCodeAreaPos;
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			//putCode( inv ? VM_JF : VM_JNF, node_pos );
			//putCode( 0, node_pos ); // *
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(inv ? VM_JF : VM_JNF); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
			genNodeCode( frame, node.getNode(0), 0, 0, param );
			mCodeArea[addr+1] = (short) (mCodeAreaPos-addr); // patch "*"
			return 0;
		}
		case Token.T_INCONTEXTOF:	// 'incontextof'
		{
			if( (restype&RT_NEEDED) == 0 ) return 0;
			int resaddr1, resaddr2;
			resaddr1 = genNodeCode( frame, node.getNode(0), RT_NEEDED, 0, param );
			resaddr2 = genNodeCode( frame, node.getNode(1), RT_NEEDED, 0 , new SubParam() );
			if( resaddr1 <= 0 ) {
				//putCode( VM_CP, node_pos );
				//putCode( frame.value, node_pos );
				//putCode( resaddr1, node_pos );
				if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
				resaddr1 = frame.value;
				frame.value++;
			}
			//putCode( VM_CHGTHIS, node_pos );
			//putCode( resaddr1, node_pos );
			//putCode( resaddr2, node_pos );
			if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_CHGTHIS); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr2); mCodeAreaPos++;
			return resaddr1;
		}
		case Token.T_COMMA:	// ','
			genNodeCode( frame, node.getNode(0),0,0,new SubParam() );
			return genNodeCode( frame, node.getNode(1), restype, reqresaddr, param );

		case Token.T_SWAP:	// '<->'
		{
			if( (restype & RT_NEEDED) != 0 ) error( Error.CannotGetResult );
			if( param.mSubType != 0 ) error( Error.CannotModifyLHS );
			int resaddr1 = genNodeCode( frame, node.getNode(0), RT_NEEDED, 0, new SubParam() );
			if( resaddr1 <= 0 ) {
				//putCode( VM_CP, node_pos );
				//putCode( frame.value, node_pos );
				//putCode( resaddr1, node_pos );
				if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
				resaddr1 = frame.value;
				frame.value++;
			}
			int resaddr2 = genNodeCode( frame, node.getNode(1), RT_NEEDED, 0, new SubParam() );
			SubParam param2 = new SubParam();
			param2.mSubType = stEqual;
			param2.mSubAddress = resaddr2;
			genNodeCode( frame, node.getNode(0), 0, 0, param2 );

			param2.mSubType = stEqual;
			param2.mSubAddress = resaddr1;
			genNodeCode( frame, node.getNode(1), 0, 0, param2 );
			return 0;
		}
		case Token.T_EQUAL:	// '='
		{
			if( param.mSubType != 0 ) error( Error.CannotModifyLHS );
			if( (restype & RT_CFLAG) != 0 ) {
				outputWarning( Error.SubstitutionInBooleanContext, node_pos );
			}
			resaddr = genNodeCode( frame, node.getNode(1), RT_NEEDED, 0, param );
			SubParam  param2 = new SubParam();
			param2.mSubType = stEqual;
			param2.mSubAddress = resaddr;
			genNodeCode( frame, node.getNode(0), 0, 0, param2 );
			return resaddr;
		}
		case Token.T_AMPERSANDEQUAL:	// '&=' operator
		case Token.T_VERTLINEEQUAL:		// '|=' operator
		case Token.T_CHEVRONEQUAL:		// '^=' operator
		case Token.T_MINUSEQUAL:		// ^-=' operator
		case Token.T_PLUSEQUAL:			// '+=' operator
		case Token.T_PERCENTEQUAL:		// '%=' operator
		case Token.T_SLASHEQUAL:		// '/=' operator
		case Token.T_BACKSLASHEQUAL:	// '\=' operator
		case Token.T_ASTERISKEQUAL:		// '*=' operator
		case Token.T_LOGICALOREQUAL:	// '||=' operator
		case Token.T_LOGICALANDEQUAL:	// '&&=' operator
		case Token.T_RARITHSHIFTEQUAL:	// '>>=' operator
		case Token.T_LARITHSHIFTEQUAL:	// '<<=' operator
		case Token.T_RBITSHIFTEQUAL:	// '>>>=' operator
		{
			if( param.mSubType != 0 ) error( Error.CannotModifyLHS );
			resaddr = genNodeCode( frame, node.getNode(1), RT_NEEDED, 0, new SubParam() );
			SubParam param2 = new SubParam();
			switch(node.getOpecode()) // this may be sucking...
			{
			case Token.T_AMPERSANDEQUAL:	param2.mSubType = stBitAND;		break;
			case Token.T_VERTLINEEQUAL:		param2.mSubType = stBitOR;		break;
			case Token.T_CHEVRONEQUAL:		param2.mSubType = stBitXOR;		break;
			case Token.T_MINUSEQUAL:		param2.mSubType = stSub;		break;
			case Token.T_PLUSEQUAL:			param2.mSubType = stAdd;		break;
			case Token.T_PERCENTEQUAL:		param2.mSubType = stMod;		break;
			case Token.T_SLASHEQUAL:		param2.mSubType = stDiv;		break;
			case Token.T_BACKSLASHEQUAL:	param2.mSubType = stIDiv;		break;
			case Token.T_ASTERISKEQUAL:		param2.mSubType = stMul;		break;
			case Token.T_LOGICALOREQUAL:	param2.mSubType = stLogOR;		break;
			case Token.T_LOGICALANDEQUAL:	param2.mSubType = stLogAND;		break;
			case Token.T_RARITHSHIFTEQUAL:	param2.mSubType = stSAR;		break;
			case Token.T_LARITHSHIFTEQUAL:	param2.mSubType = stSAL;		break;
			case Token.T_RBITSHIFTEQUAL:	param2.mSubType = stSR;			break;
			}
			param2.mSubAddress = resaddr;
			return genNodeCode( frame, node.getNode(0), restype, reqresaddr, param2 );
		}
		case Token.T_QUESTION:	// '?' ':' operator
		{
			// three-term operator ( ? : )
			int resaddr1, resaddr2;
			int frame1, frame2;
			resaddr = genNodeCode( frame, node.getNode(0), RT_NEEDED|RT_CFLAG, 0, new SubParam() );
			boolean inv = false;
			if( !(resaddr == GNC_CFLAG || resaddr == GNC_CFLAG_I) ) {
				//putCode( VM_TT, node_pos );
				//putCode( resaddr, node_pos );
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(VM_TT); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
			} else {
				if( resaddr == GNC_CFLAG_I ) inv = true;
			}
			int cur_frame = frame.value;
			int addr1 = mCodeAreaPos;
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			//putCode( inv ? VM_JF : VM_JNF, node_pos );
			//putCode( 0, node_pos ); // patch
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(inv ? VM_JF : VM_JNF); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
			resaddr1 = genNodeCode( frame, node.getNode(1), restype, reqresaddr, param );
			if( (restype & RT_CFLAG) != 0 ) {
				if( !(resaddr1 == GNC_CFLAG || resaddr1 == GNC_CFLAG_I) ) {
					//putCode( VM_TT, node_pos );
					//putCode( resaddr1 );
					if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(VM_TT); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
				} else {
					if( resaddr1 == GNC_CFLAG_I ) {
						//putCode( VM_NF, node_pos ); // invert flag
						if( (mCodeAreaPos) >= mCodeArea.length ) expandCodeArea();
						if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
						mCodeArea[mCodeAreaPos] = (short)(VM_NF); mCodeAreaPos++;
					}
				}
			} else {
				if( (restype&RT_NEEDED) != 0 && !(resaddr1 == GNC_CFLAG || resaddr1 == GNC_CFLAG_I) && resaddr1 <= 0 ) {
					//putCode( VM_CP, node_pos );
					//putCode( frame.value, node_pos );
					//putCode( resaddr1, node_pos );
					if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
					resaddr1 = frame.value;
					frame.value++;
				}
			}
			frame1 = frame.value;

			int addr2 = mCodeAreaPos;
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			//putCode( VM_JMP, node_pos );
			//putCode( 0, node_pos ); // patch
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_JMP); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
			mCodeArea[addr1+1] = (short) (mCodeAreaPos-addr1); // patch
			frame.value = cur_frame;
			resaddr2 = genNodeCode( frame, node.getNode(2), restype, reqresaddr, param );
			if( (restype & RT_CFLAG) != 0 ) {
				// condition flag required
				if( !(resaddr2 == GNC_CFLAG || resaddr2 == GNC_CFLAG_I) ) {
					//putCode( VM_TT, node_pos );
					//putCode( resaddr2 );
					if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(VM_TT); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr2); mCodeAreaPos++;
				} else {
					if( resaddr2 == GNC_CFLAG_I ) {
						//putCode( VM_NF, node_pos ); // invert flag
						if( (mCodeAreaPos) >= mCodeArea.length ) expandCodeArea();
						if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
						mCodeArea[mCodeAreaPos] = (short)(VM_NF); mCodeAreaPos++;
					}
				}
			} else {
				if( (restype & RT_NEEDED) != 0 && !(resaddr1 == GNC_CFLAG || resaddr1 == GNC_CFLAG_I) && resaddr1 != resaddr2 ) {
					//putCode( VM_CP, node_pos );
					//putCode( resaddr1, node_pos );
					//putCode( resaddr2, node_pos );
					if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr2); mCodeAreaPos++;
					frame.value++;
				}
			}
			frame2 = frame.value;
			mCodeArea[addr2+1] = (short) (mCodeAreaPos-addr2); // patch
			frame.value = frame2 < frame1 ? frame1 : frame2;
			return (restype&RT_CFLAG) != 0 ? GNC_CFLAG : resaddr1;
		}
		case Token.T_LOGICALOR:		// '||'
		case Token.T_LOGICALAND:	// '&&'
		{
			// "logical or" and "locical and"
			// these process with th "shortcut" :
			// OR  : does not evaluate right when left results true
			// AND : does not evaluate right when left results false
			if( param.mSubType != 0 ) error( Error.CannotModifyLHS );
			int resaddr1, resaddr2;
			resaddr1 = genNodeCode( frame, node.getNode(0), RT_NEEDED|RT_CFLAG, 0, new SubParam() );
			boolean inv = false;
			if( !(resaddr1 == GNC_CFLAG || resaddr1 == GNC_CFLAG_I) ) {
				//putCode( VM_TT, node_pos );
				//putCode( resaddr1, node_pos );
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(VM_TT); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
			}
			if( resaddr1 == GNC_CFLAG_I ) inv = true;
			int addr1 =  mCodeAreaPos;
			//addJumpList();
			mJumpList.add( mCodeAreaPos );
			//putCode( node.getOpecode() == Token.T_LOGICALOR ? (inv?VM_JNF:VM_JF) : (inv?VM_JF:VM_JNF), node_pos );
			//putCode( 0, node_pos ); // *A
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(node.getOpecode() == Token.T_LOGICALOR ? (inv?VM_JNF:VM_JF) : (inv?VM_JF:VM_JNF)); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
			resaddr2 = genNodeCode( frame, node.getNode(1), RT_NEEDED|RT_CFLAG, 0, new SubParam() );
			if( !(resaddr2 == GNC_CFLAG || resaddr2 == GNC_CFLAG_I) ) {
				//putCode( inv ? VM_TF : VM_TT, node_pos );
				//putCode( resaddr2, node_pos );
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(inv ? VM_TF : VM_TT); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr2); mCodeAreaPos++;
			} else {
				if( (inv != false) != (resaddr2==GNC_CFLAG_I) ) {
					//putCode( VM_NF, node_pos ); // invert flag
					if( (mCodeAreaPos) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(VM_NF); mCodeAreaPos++;
				}
			}
			mCodeArea[addr1+1] = (short) (mCodeAreaPos-addr1); // patch
			if( (restype&RT_CFLAG) == 0 ) {
				// requested result type is not condition flag
				if( (resaddr1 == GNC_CFLAG || resaddr1 == GNC_CFLAG_I) || resaddr1 <= 0 ) {
					//putCode( inv ? VM_SETNF : VM_SETF, node_pos );
					//putCode( frame.value, node_pos );
					if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(inv ? VM_SETNF : VM_SETF); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
					resaddr1 = frame.value;
					frame.value++;
				} else {
					//putCode( inv ? VM_SETNF : VM_SETF, node_pos );
					//putCode( resaddr1, node_pos );
					if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(inv ? VM_SETNF : VM_SETF); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
				}
			}
			return (restype&RT_CFLAG) != 0 ? (inv?GNC_CFLAG_I:GNC_CFLAG) : resaddr1;
		}
		case Token.T_INSTANCEOF: // 'instanceof' operator
		{
			// instanceof operator
			int resaddr1, resaddr2;
			resaddr1 = genNodeCode( frame, node.getNode(0), RT_NEEDED, 0, new SubParam() );
			if( resaddr1 <= 0 ) {
				//putCode( VM_CP, node_pos );
				//putCode( frame.value, node_pos );
				//putCode( resaddr1, node_pos );
				if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
				resaddr1 = frame.value;
				frame.value++;
			}
			resaddr2 = genNodeCode( frame, node.getNode(1), RT_NEEDED, 0, new SubParam() );
			//putCode( VM_CHKINS, node_pos );
			//putCode( resaddr1, node_pos );
			//putCode( resaddr2, node_pos );
			if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_CHKINS); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr2); mCodeAreaPos++;
			return resaddr1;
		}
		case Token.T_VERTLINE:	// '|' operator
		case Token.T_CHEVRON:	// '^' operator
		case Token.T_AMPERSAND:	// binary '&' operator
		case Token.T_RARITHSHIFT:// '>>' operator
		case Token.T_LARITHSHIFT:// '<<' operator
		case Token.T_RBITSHIFT:	// '>>>' operator
		case Token.T_PLUS:		// binary '+' operator
		case Token.T_MINUS:		// '-' operator
		case Token.T_PERCENT:	// '%' operator
		case Token.T_SLASH:		// '/' operator
		case Token.T_BACKSLASH:	// '\' operator
		case Token.T_ASTERISK:	// binary '*' operator
		{
			// general two-term operator
			int resaddr1, resaddr2;
			if( param.mSubType != stNone ) error( Error.CannotModifyLHS );
			resaddr1 = genNodeCode( frame, node.getNode(0), RT_NEEDED, 0, new SubParam() );
			if( resaddr1 <= 0 ) {
				//putCode( VM_CP, node_pos );
				//putCode( frame.value, node_pos );
				//putCode( resaddr1, node_pos );
				if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
				resaddr1 = frame.value;
				frame.value++;
			}
			resaddr2 = genNodeCode( frame, node.getNode(1), RT_NEEDED, 0, new SubParam() );
			int code = 0;
			switch( node.getOpecode() ) { // sucking...
			case Token.T_VERTLINE:		code = VM_BOR;		break;
			case Token.T_CHEVRON:		code = VM_BXOR;		break;
			case Token.T_AMPERSAND:		code = VM_BAND;		break;
			case Token.T_RARITHSHIFT:	code = VM_SAR;		break;
			case Token.T_LARITHSHIFT:	code = VM_SAL;		break;
			case Token.T_RBITSHIFT:		code = VM_SR;		break;
			case Token.T_PLUS:			code = VM_ADD;		break;
			case Token.T_MINUS:			code = VM_SUB;		break;
			case Token.T_PERCENT:		code = VM_MOD;		break;
			case Token.T_SLASH:			code = VM_DIV;		break;
			case Token.T_BACKSLASH:		code = VM_IDIV;		break;
			case Token.T_ASTERISK:		code = VM_MUL;		break;
			}
			//putCode( code, node_pos );
			//putCode( resaddr1, node_pos );
			//putCode( resaddr2, node_pos );
			if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(code); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr2); mCodeAreaPos++;
			return resaddr1;
		}
		case Token.T_NOTEQUAL:		// '!=' operator
		case Token.T_EQUALEQUAL:	// '==' operator
		case Token.T_DISCNOTEQUAL:	// '!==' operator
		case Token.T_DISCEQUAL:		// '===' operator
		case Token.T_LT:			// '<' operator
		case Token.T_GT:			// '>' operator
		case Token.T_LTOREQUAL:		// '<=' operator
		case Token.T_GTOREQUAL:		// '>=' operator
		{
			// comparison operators
			int resaddr1, resaddr2;
			if( param.mSubType != stNone ) error( Error.CannotModifyLHS );
			resaddr1 = genNodeCode( frame, node.getNode(0), RT_NEEDED, 0 , new SubParam() );
			if( (restype&RT_CFLAG) == 0 ) {
				if( resaddr1 <= 0 ) {
					//putCode( VM_CP, node_pos );
					//putCode( frame.value, node_pos );
					//putCode( resaddr1, node_pos );
					if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
					resaddr1 = frame.value;
					frame.value++;
				}
			}
			resaddr2 = genNodeCode( frame, node.getNode(1), RT_NEEDED, 0, new SubParam() );
			int code1 = 0, code2 = 0;
			switch( node.getOpecode() ) {
			case Token.T_NOTEQUAL:		code1 = VM_CEQ;		code2 = VM_SETNF; 	break;
			case Token.T_EQUALEQUAL:	code1 = VM_CEQ;		code2 = VM_SETF;	break;
			case Token.T_DISCNOTEQUAL:	code1 = VM_CDEQ;	code2 = VM_SETNF;	break;
			case Token.T_DISCEQUAL:		code1 = VM_CDEQ;	code2 = VM_SETF;	break;
			case Token.T_LT:			code1 = VM_CLT;		code2 = VM_SETF;	break;
			case Token.T_GT:			code1 = VM_CGT;		code2 = VM_SETF;	break;
			case Token.T_LTOREQUAL:		code1 = VM_CGT;		code2 = VM_SETNF;	break;
			case Token.T_GTOREQUAL:		code1 = VM_CLT;		code2 = VM_SETNF;	break;
			}
			//putCode( code1, node_pos );
			//putCode( resaddr1, node_pos );
			//putCode( resaddr2, node_pos );
			if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(code1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr2); mCodeAreaPos++;
			if( (restype&RT_CFLAG) == 0 ) {
				//putCode( code2, node_pos );
				//putCode( resaddr1, node_pos );
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				mCodeArea[mCodeAreaPos] = (short)(code2); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr1); mCodeAreaPos++;
			}
			return (restype&RT_CFLAG) != 0 ? (code2==VM_SETNF?GNC_CFLAG_I:GNC_CFLAG) : resaddr1;
		}

		// ここから一気に
		case Token.T_EXCRAMATION: // pre-positioned '!' operator
		{
			// logical not
			if((param.mSubType != stNone)) error(Error.CannotModifyLHS);
			resaddr = genNodeCode(frame, node.getNode(0), restype, reqresaddr, new SubParam());
			if( (restype & RT_CFLAG) == 0 ) {
				// value as return value required
				if(!(resaddr>0)) {
					//putCode(VM_CP, node_pos);
					//putCode( frame.value, node_pos);
					//putCode( resaddr, node_pos);
					if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
					resaddr = frame.value;
					frame.value++;
				}
				//putCode(VM_LNOT, node_pos);
				//putCode( resaddr, node_pos);
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(VM_LNOT); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
				return resaddr;
			} else {
				// condifion flag required
				if(!(resaddr == GNC_CFLAG || resaddr == GNC_CFLAG_I)) {
					//putCode(VM_TF, node_pos);
					//putCode( resaddr);
					if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(VM_TF); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
					return GNC_CFLAG;
				}
				return resaddr == GNC_CFLAG_I ? GNC_CFLAG : GNC_CFLAG_I; // invert flag
			}
		}

		case Token.T_TILDE:		// '~' operator
		case Token.T_SHARP:		// '#' operator
		case Token.T_DOLLAR:		// '$' operator
		case Token.T_UPLUS:		// unary '+' operator
		case Token.T_UMINUS:		// unary '-' operator
		case Token.T_INVALIDATE:	// 'invalidate' operator
		case Token.T_ISVALID:		// 'isvalid' operator
		case Token.T_EVAL:		// post-positioned '!' operator
		case Token.T_INT:			// 'int' operator
		case Token.T_REAL:		// 'real' operator
		case Token.T_STRING:		// 'string' operator
		case Token.T_OCTET:		// 'octet' operator
		{
			// general unary operators
			if((param.mSubType != stNone)) error(Error.CannotModifyLHS);
			resaddr = genNodeCode(frame, node.getNode(0), RT_NEEDED, 0, new SubParam());
			if(!(resaddr>0)) {
				//putCode(VM_CP, node_pos);
				//putCode( frame.value, node_pos);
				//putCode( resaddr, node_pos);
				if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
				resaddr = frame.value;
				frame.value++;
			}
			int code = 0;
			switch(node.getOpecode()) {
				case Token.T_TILDE:			code = VM_BNOT;			break;
				case Token.T_SHARP:			code = VM_ASC;			break;
				case Token.T_DOLLAR:		code = VM_CHR;			break;
				case Token.T_UPLUS:			code = VM_NUM;			break;
				case Token.T_UMINUS:		code = VM_CHS;			break;
				case Token.T_INVALIDATE:	code = VM_INV;			break;
				case Token.T_ISVALID:		code = VM_CHKINV;		break;
				case Token.T_TYPEOF:		code = VM_TYPEOF;		break;
				case Token.T_EVAL:			code = (restype & RT_NEEDED) != 0 ? VM_EVAL:VM_EEXP;
					// warn if T_EVAL is used in non-global position
					if( TJS.mWarnOnNonGlobalEvalOperator && mContextType != ContextType.TOP_LEVEL )
						outputWarning( Error.WarnEvalOperator );
					break;

				case Token.T_INT:			code = VM_INT;			break;
				case Token.T_REAL:			code = VM_REAL;			break;
				case Token.T_STRING:		code = VM_STR;			break;
				case Token.T_OCTET:			code = VM_OCTET;		break;
			}
			//putCode(code, node_pos);
			//putCode( resaddr, node_pos);
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(code); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
			return resaddr;
		}

		case Token.T_TYPEOF:  // 'typeof' operator
		{
			// typeof
			if((param.mSubType != stNone)) error(Error.CannotModifyLHS);
			boolean haspropnode;
			ExprNode cnode = node.getNode(0);
			if( cnode.getOpecode() == Token.T_DOT || cnode.getOpecode() == Token.T_LBRACKET ||
				cnode.getOpecode() == Token.T_WITHDOT)
				haspropnode = true;
			else
				haspropnode = false;

			if(haspropnode) {
				// has property access node
				SubParam param2 = new SubParam();
				param2.mSubType = stTypeOf;
				return genNodeCode(frame, cnode, RT_NEEDED, 0, param2);
			} else {
				// normal operation
				resaddr = genNodeCode(frame, cnode, RT_NEEDED, 0, new SubParam());

				if(!(resaddr>0)) {
					//putCode(VM_CP, node_pos);
					//putCode( frame.value, node_pos);
					//putCode( resaddr, node_pos);
					if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
					resaddr = frame.value;
					frame.value++;
				}
				//putCode(VM_TYPEOF, node_pos);
				//putCode( resaddr, node_pos);
				if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(VM_TYPEOF); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
				return resaddr;
			}
		}

		case Token.T_DELETE:			// 'delete' operator
		case Token.T_INCREMENT:		// pre-positioned '++' operator
		case Token.T_DECREMENT:		// pre-positioned '--' operator
		case Token.T_POSTINCREMENT:	// post-positioned '++' operator
		case Token.T_POSTDECREMENT:	// post-positioned '--' operator
		{
			// delete, typeof, increment and decrement
			if((param.mSubType != stNone)) error(Error.CannotModifyLHS);
			SubParam param2 = new SubParam();
			switch( node.getOpecode() ) {
			case Token.T_TYPEOF:		param2.mSubType = stTypeOf;		break;
			case Token.T_DELETE:		param2.mSubType = stDelete;		break;
			case Token.T_INCREMENT:		param2.mSubType = stPreInc;		break;
			case Token.T_DECREMENT:		param2.mSubType = stPreDec;		break;
			case Token.T_POSTINCREMENT:	param2.mSubType = stPostInc;	break;
			case Token.T_POSTDECREMENT:	param2.mSubType = stPostDec;	break;
			}
			return genNodeCode(frame, node.getNode(0), restype, reqresaddr, param2);
		}

		case Token.T_LPARENTHESIS:	// '( )' operator
		case Token.T_NEW:			// 'new' operator
		{
			// function call or create-new object

			// does (*node)[0] have a node that acceesses any properties ?
			boolean haspropnode, hasnonlocalsymbol;
			ExprNode cnode = node.getNode(0);
			if(node.getOpecode() == Token.T_LPARENTHESIS &&
				(cnode.getOpecode() == Token.T_DOT || cnode.getOpecode() == Token.T_LBRACKET))
				haspropnode = true;
			else
				haspropnode = false;

			// does (*node)[0] have a node that accesses non-local functions ?
			if(node.getOpecode() == Token.T_LPARENTHESIS && cnode.getOpecode() == Token.T_SYMBOL) {
				if( mAsGlobalContextMode ) {
					hasnonlocalsymbol = true;
				} else {
					String str = cnode.getValue().asString();
					if(mNamespace.find( str ) == -1)
						hasnonlocalsymbol = true;
					else
						hasnonlocalsymbol = false;
				}
			} else {
				hasnonlocalsymbol = false;
			}

			// flag which indicates whether to do direct or indirect call access
			boolean do_direct_access = haspropnode || hasnonlocalsymbol;

			// reserve frame
			if(!do_direct_access && (restype & RT_NEEDED) != 0 )
				frame.value++; // reserve the frame for a result value

			// generate function call codes
			startFuncArg();
			int framestart = frame.value;
			int res;
			try {
				// arguments is

				if( node.getNode(1).getSize() == 1 && node.getNode(1).getNode(0) == null ) {
					// empty
				} else {
					// exist
					genNodeCode(frame, node.getNode(1), RT_NEEDED, 0, new SubParam() );
				}

				// compilation of expression that represents the function
				SubParam param2 = new SubParam();
				if(do_direct_access) {
					param2.mSubType = stFuncCall; // creates code with stFuncCall
					res = genNodeCode(frame, node.getNode(0), restype, reqresaddr, param2);
				} else {
					param2.mSubType = stNone;
					resaddr = genNodeCode(frame, node.getNode(0), RT_NEEDED, 0, param2);

					// code generatio of function calling
					/*
					putCode( node.getOpecode() == Token.T_NEW ? VM_NEW: VM_CALL, node_pos);
					putCode((
						res = (restype & RT_NEEDED) != 0 ?(framestart-1):0),
						node_pos); // result target
					putCode( resaddr, node_pos); // iTJSDispatch2 that points the function
					*/

					if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(node.getOpecode() == Token.T_NEW ? VM_NEW: VM_CALL); mCodeAreaPos++;
					res = ((restype & RT_NEEDED) != 0 ?(framestart-1):0);
					mCodeArea[mCodeAreaPos] = (short)(res); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;

					// generate argument code
					generateFuncCallArgCode();

					// clears the frame
					clearFrame( frame, framestart );
				}
			} finally {
				endFuncArg();
			}
			return res;
		}

		case Token.T_ARG:
			// a function argument
			if( node.getSize() >= 2 ) {
				if( node.getNode(1) != null ) genNodeCode( frame, node.getNode(1), RT_NEEDED, 0, new SubParam());
			}
			if( node.getNode(0) != null ) {
				ExprNode n = node.getNode(0);
				if( n.getOpecode() == Token.T_EXPANDARG ) {
					// expanding argument
					if( n.getNode(0) != null )
						addFuncArg( genNodeCode( frame, n.getNode(0), RT_NEEDED, 0, new SubParam()), fatExpand);
					else
						addFuncArg(0, fatUnnamedExpand);
				}
				else
				{
					addFuncArg( genNodeCode( frame, node.getNode(0), RT_NEEDED, 0, new SubParam()), fatNormal);
				}
			} else {
				addFuncArg(0, fatNormal);
			}
			return 0;

		case Token.T_OMIT:
			// omitting of the function arguments
			addOmitArg();
	        return 0;

		case Token.T_DOT:			// '.' operator
		case Token.T_LBRACKET:	// '[ ]' operator
		{
			// member access ( direct or indirect )
			boolean direct = node.getOpecode() == Token.T_DOT;
			int dp;

			SubParam param2 = new SubParam();
			param2.mSubType = stNone;
			resaddr = genNodeCode( frame, node.getNode(0), RT_NEEDED, 0, param2);

			if(direct)
				dp = putData( node.getNode(1).getValue() );
			else
				dp = genNodeCode( frame, node.getNode(1), RT_NEEDED, 0, new SubParam() );

			switch(param.mSubType) {
			case stNone:
			case stIgnorePropGet:
				if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				if(param.mSubType == stNone) {
					//putCode(direct ? VM_GPD : VM_GPI, node_pos);
					mCodeArea[mCodeAreaPos] = (short)(direct ? VM_GPD : VM_GPI); mCodeAreaPos++;
				} else {
					//putCode(direct ? VM_GPDS : VM_GPIS, node_pos);
					mCodeArea[mCodeAreaPos] = (short)(direct ? VM_GPDS : VM_GPIS); mCodeAreaPos++;
				}
				//putCode( frame.value, node_pos);
				//putCode( resaddr, node_pos);
				//putCode( dp, node_pos);
				mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(dp); mCodeAreaPos++;
				frame.value++;
				return frame.value-1;

			case stEqual:
			case stIgnorePropSet:
				if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				if(param.mSubType == stEqual) {
					if( node.getNode(0).getOpecode() == Token.T_THIS_PROXY ) {
						//putCode(direct ? VM_SPD : VM_SPI, node_pos);
						mCodeArea[mCodeAreaPos] = (short)(direct ? VM_SPD : VM_SPI); mCodeAreaPos++;
					} else {
						//putCode(direct ? VM_SPDE : VM_SPIE, node_pos);
						mCodeArea[mCodeAreaPos] = (short)(direct ? VM_SPDE : VM_SPIE); mCodeAreaPos++;
					}
				} else {
					//putCode(direct ? VM_SPDS : VM_SPIS, node_pos);
					mCodeArea[mCodeAreaPos] = (short)(direct ? VM_SPDS : VM_SPIS); mCodeAreaPos++;
				}
				//putCode( resaddr, node_pos);
				//putCode( dp, node_pos);
				//putCode( param.mSubAddress, node_pos);
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(dp); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(param.mSubAddress); mCodeAreaPos++;
				return param.mSubAddress;

			case stBitAND:
			case stBitOR:
			case stBitXOR:
			case stSub:
			case stAdd:
			case stMod:
			case stDiv:
			case stIDiv:
			case stMul:
			case stLogOR:
			case stLogAND:
			case stSAR:
			case stSAL:
			case stSR:
				//putCode( param.mSubType + (direct?1:2), node_pos);
					// here adds 1 or 2 to the ope-code
					// ( see the ope-code's positioning order )
				//putCode(( (restype & RT_NEEDED) != 0 ? frame.value : 0), node_pos);
				//putCode( resaddr, node_pos);
				//putCode( dp, node_pos);
				//putCode( param.mSubAddress, node_pos);
				if( (mCodeAreaPos+4) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(param.mSubType + (direct?1:2)); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(( (restype & RT_NEEDED) != 0 ? frame.value : 0)); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(dp); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(param.mSubAddress); mCodeAreaPos++;
				if( (restype & RT_NEEDED) != 0 ) frame.value++;
				return (restype & RT_NEEDED) != 0 ?frame.value-1:0;

			case stPreInc:
			case stPreDec:
				//putCode((param.mSubType == stPreInc ? VM_INC : VM_DEC) + (direct? 1:2), node_pos);
				//putCode(((restype & RT_NEEDED) != 0 ? frame.value : 0), node_pos);
				//putCode( resaddr, node_pos);
				//putCode( dp, node_pos);
				if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)((param.mSubType == stPreInc ? VM_INC : VM_DEC) + (direct? 1:2)); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(((restype & RT_NEEDED) != 0 ? frame.value : 0)); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(dp); mCodeAreaPos++;
				if( (restype & RT_NEEDED) != 0 ) frame.value++;
				return (restype & RT_NEEDED) != 0 ?frame.value-1:0;

			case stPostInc:
			case stPostDec:
			{
				int retresaddr = 0;
				if( (restype & RT_NEEDED) != 0 ) {
					// need result ...
					//putCode(direct ? VM_GPD : VM_GPI, node_pos);
					//putCode( frame.value, node_pos);
					//putCode( resaddr, node_pos);
					//putCode( dp, node_pos);
					if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(direct ? VM_GPD : VM_GPI); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(dp); mCodeAreaPos++;
					retresaddr = frame.value;
					frame.value++;
				}
				//putCode( (param.mSubType == stPostInc ? VM_INC : VM_DEC) + (direct? 1:2), node_pos);
				//putCode( 0, node_pos );
				//putCode( resaddr, node_pos );
				//putCode( dp, node_pos );
				if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)((param.mSubType == stPostInc ? VM_INC : VM_DEC) + (direct? 1:2)); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(dp); mCodeAreaPos++;
				return retresaddr;
			}
			case stTypeOf:
			{
				// typeof
				//putCode(direct? VM_TYPEOFD:VM_TYPEOFI, node_pos);
				//putCode(( (restype & RT_NEEDED) != 0 ? frame.value:0), node_pos);
				//putCode( resaddr, node_pos);
				//putCode( dp, node_pos);
				if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(direct? VM_TYPEOFD:VM_TYPEOFI); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(( (restype & RT_NEEDED) != 0 ? frame.value:0)); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(dp); mCodeAreaPos++;
				if( (restype & RT_NEEDED) != 0 ) frame.value++;
				return (restype & RT_NEEDED) != 0 ? frame.value-1:0;
			}
			case stDelete:
			{
				// deletion
				//putCode(direct? VM_DELD:VM_DELI, node_pos);
				//putCode(( (restype & RT_NEEDED) != 0 ? frame.value:0), node_pos);
				//putCode( resaddr, node_pos);
				//putCode( dp, node_pos);
				if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(direct? VM_DELD:VM_DELI); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(( (restype & RT_NEEDED) != 0 ? frame.value:0)); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(dp); mCodeAreaPos++;
				if( (restype & RT_NEEDED) != 0 ) frame.value++;
				return (restype & RT_NEEDED) != 0 ? frame.value-1:0;
			}
			case stFuncCall:
			{
				// function call
				//putCode(direct ? VM_CALLD:VM_CALLI, node_pos);
				//putCode(( (restype & RT_NEEDED) != 0 ? frame.value:0), node_pos); // result target
				//putCode( resaddr, node_pos); // the object
				//putCode( dp, node_pos); // function name
				if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
				if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
				mCodeArea[mCodeAreaPos] = (short)(direct ? VM_CALLD:VM_CALLI); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(( (restype & RT_NEEDED) != 0 ? frame.value:0)); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
				mCodeArea[mCodeAreaPos] = (short)(dp); mCodeAreaPos++;

				// generate argument code
				generateFuncCallArgCode();

				// extend frame and return
				if( (restype & RT_NEEDED) != 0 ) frame.value++;
				return (restype & RT_NEEDED) != 0 ? frame.value-1:0;
			}

			default:
				error(Error.CannotModifyLHS);
				return 0;
			}
		}


		case Token.T_SYMBOL:	// symbol
		{
			// accessing to a variable
			int n;
			if( mAsGlobalContextMode ) {
				n = -1; // global mode cannot access local variables
			} else {
				String str = node.getValue().asString();
				n = mNamespace.find( str );
			}

			if(n!=-1) {
				boolean isstnone = !(param.mSubType != stNone);

				if(!isstnone) {
					// substitution, or like it
					switch(param.mSubType){
					case stEqual:
						//putCode(VM_CP, node_pos);
						//putCode((-n-mVariableReserveCount-1), node_pos);
						//putCode( param.mSubAddress, node_pos);
						if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
						if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
						mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
						mCodeArea[mCodeAreaPos] = (short)(-n-mVariableReserveCount-1); mCodeAreaPos++;
						mCodeArea[mCodeAreaPos] = (short)(param.mSubAddress); mCodeAreaPos++;
						break;

					case stBitAND:
					case stBitOR:
					case stBitXOR:
					case stSub:
					case stAdd:
					case stMod:
					case stDiv:
					case stIDiv:
					case stMul:
					case stLogOR:
					case stLogAND:
					case stSAR:
					case stSAL:
					case stSR:
						//putCode(param.mSubType, node_pos);
						//putCode((-n-mVariableReserveCount-1), node_pos);
						//putCode( param.mSubAddress, node_pos);
						if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
						if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
						mCodeArea[mCodeAreaPos] = (short)(param.mSubType); mCodeAreaPos++;
						mCodeArea[mCodeAreaPos] = (short)(-n-mVariableReserveCount-1); mCodeAreaPos++;
						mCodeArea[mCodeAreaPos] = (short)(param.mSubAddress); mCodeAreaPos++;
						return (restype & RT_NEEDED) != 0 ? -n-mVariableReserveCount-1:0;

					case stPreInc: // pre-positioning
						//putCode(VM_INC, node_pos);
						//putCode((-n-mVariableReserveCount-1), node_pos);
						if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
						if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
						mCodeArea[mCodeAreaPos] = (short)(VM_INC); mCodeAreaPos++;
						mCodeArea[mCodeAreaPos] = (short)(-n-mVariableReserveCount-1); mCodeAreaPos++;
						return (restype & RT_NEEDED) != 0 ?-n-mVariableReserveCount-1:0;

					case stPreDec: // pre-
						//putCode(VM_DEC, node_pos);
						//putCode((-n-mVariableReserveCount-1), node_pos);
						if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
						if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
						mCodeArea[mCodeAreaPos] = (short)(VM_DEC); mCodeAreaPos++;
						mCodeArea[mCodeAreaPos] = (short)(-n-mVariableReserveCount-1); mCodeAreaPos++;
						return (restype & RT_NEEDED) != 0 ? -n-mVariableReserveCount-1:0;

					case stPostInc: // post-
						if( (restype & RT_NEEDED) != 0 ) {
							//putCode(VM_CP, node_pos);
							//putCode( frame.value, node_pos);
							//putCode((-n-mVariableReserveCount-1), node_pos);
							if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
							if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
							mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
							mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
							mCodeArea[mCodeAreaPos] = (short)(-n-mVariableReserveCount-1); mCodeAreaPos++;
							frame.value++;
						}
						//putCode(VM_INC, node_pos);
						//putCode((-n-mVariableReserveCount-1), node_pos);
						if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
						if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
						mCodeArea[mCodeAreaPos] = (short)(VM_INC); mCodeAreaPos++;
						mCodeArea[mCodeAreaPos] = (short)(-n-mVariableReserveCount-1); mCodeAreaPos++;
						return (restype & RT_NEEDED) != 0 ? frame.value-1:0;

					case stPostDec: // post-
						if( (restype & RT_NEEDED) != 0 ) {
							//putCode(VM_CP, node_pos);
							//putCode( frame.value, node_pos);
							//putCode((-n-mVariableReserveCount-1), node_pos);
							if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
							if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
							mCodeArea[mCodeAreaPos] = (short)(VM_CP); mCodeAreaPos++;
							mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
							mCodeArea[mCodeAreaPos] = (short)(-n-mVariableReserveCount-1); mCodeAreaPos++;
							frame.value++;
						}
						//putCode(VM_DEC, node_pos);
						//putCode((-n-mVariableReserveCount-1), node_pos);
						if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
						if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
						mCodeArea[mCodeAreaPos] = (short)(VM_DEC); mCodeAreaPos++;
						mCodeArea[mCodeAreaPos] = (short)(-n-mVariableReserveCount-1); mCodeAreaPos++;
						return (restype & RT_NEEDED) != 0 ? frame.value-1:0;

					case stDelete: // deletion
					{
						String str = node.getValue().asString();
						mNamespace.remove(str);
						if( (restype & RT_NEEDED) != 0 ) {
							int dp = putData( new Variant(1) ); // true
							//putCode(VM_CONST, node_pos);
							//putCode( frame.value, node_pos);
							//putCode( dp, node_pos);
							if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
							if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
							mCodeArea[mCodeAreaPos] = (short)(VM_CONST); mCodeAreaPos++;
							mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
							mCodeArea[mCodeAreaPos] = (short)(dp); mCodeAreaPos++;
							return frame.value-1;
						}
						return 0;
					}
					default:
						error(Error.CannotModifyLHS);
					}
					return 0;
				} else {
					// read
					String str = node.getValue().asString();
					int n1 = mNamespace.find( str );
					return -n1-mVariableReserveCount-1;
				}
			} else {
				// n==-1 ( indicates the variable is not found in the local  )
				// assume the variable is in "this".
				// make nodes that refer "this" and process it
				ExprNode nodep = new ExprNode();
				nodep.setOpecode(Token.T_DOT);
				nodep.setPosition(node_pos);
				ExprNode node1 = new ExprNode();
				mNodeToDeleteVector.add(node1);
				nodep.add(node1);
				node1.setOpecode(mAsGlobalContextMode ? Token.T_GLOBAL:Token.T_THIS_PROXY);
				node1.setPosition(node_pos);
				ExprNode node2 = new ExprNode();
				mNodeToDeleteVector.add(node2);
				nodep.add(node2);
				node2.setOpecode(Token.T_SYMBOL);
				node2.setPosition(node_pos);
				node2.setValue(node.getValue());
				return genNodeCode( frame, nodep, restype, reqresaddr, param );
			}
		}

		case Token.T_IGNOREPROP: // unary '&' operator
		case Token.T_PROPACCESS: // unary '*' operator
			if( node.getOpecode() == (TJS.mUnaryAsteriskIgnoresPropAccess?Token.T_PROPACCESS:Token.T_IGNOREPROP)) {
				// unary '&' operator
				// substance accessing (ignores property operation)
			  	SubParam sp = new SubParam(param);
				if( sp.mSubType == stNone) sp.mSubType = stIgnorePropGet;
				else if(sp.mSubType == stEqual) sp.mSubType = stIgnorePropSet;
				else error(Error.CannotModifyLHS);
				return genNodeCode(frame, node.getNode(0), restype, reqresaddr, sp);
			} else {
				// unary '*' operator
				// force property access
				resaddr = genNodeCode(frame, node.getNode(0), RT_NEEDED, 0, new SubParam() );
				switch(param.mSubType) {
				case stNone: // read from property object
					//putCode(VM_GETP, node_pos);
					//putCode( frame.value, node_pos);
					//putCode( resaddr, node_pos);
					if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(VM_GETP); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
					frame.value++;
					return frame.value - 1;

				case stEqual: // write to property object
					//putCode(VM_SETP, node_pos);
					//putCode( resaddr, node_pos);
					//putCode( param.mSubAddress, node_pos);
					if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(VM_SETP); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(param.mSubAddress); mCodeAreaPos++;
					return param.mSubAddress;

				case stBitAND:
				case stBitOR:
				case stBitXOR:
				case stSub:
				case stAdd:
				case stMod:
				case stDiv:
				case stIDiv:
				case stMul:
				case stLogOR:
				case stLogAND:
				case stSAR:
				case stSAL:
				case stSR:
					//putCode(param.mSubType + 3, node_pos);
						// +3 : property access
						// ( see the ope-code's positioning order )
					//putCode(((restype & RT_NEEDED) != 0 ? frame.value: 0), node_pos);
					//putCode( resaddr, node_pos);
					//putCode( param.mSubAddress, node_pos);
					if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)(param.mSubType + 3); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(((restype & RT_NEEDED) != 0 ? frame.value: 0)); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(param.mSubAddress); mCodeAreaPos++;
					if( (restype & RT_NEEDED) != 0 ) frame.value++;
					return (restype & RT_NEEDED) != 0 ? frame.value-1:0;

				case stPreInc:
				case stPreDec:
					//putCode((param.mSubType == stPreInc ? VM_INC : VM_DEC) + 3, node_pos);
					//putCode(((restype & RT_NEEDED) != 0 ? frame.value : 0), node_pos);
					//putCode( resaddr, node_pos);
					if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)((param.mSubType == stPreInc ? VM_INC : VM_DEC) + 3); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(((restype & RT_NEEDED) != 0 ? frame.value : 0)); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
					if( (restype & RT_NEEDED) != 0 ) frame.value++;
					return (restype & RT_NEEDED) != 0 ? frame.value-1:0;

				case stPostInc:
				case stPostDec:
				{
					int retresaddr = 0;
					if( (restype & RT_NEEDED) != 0 ) {
						// need result ...
						//putCode(VM_GETP, node_pos);
						//putCode( frame.value, node_pos);
						//putCode( resaddr, node_pos);
						if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
						if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
						mCodeArea[mCodeAreaPos] = (short)(VM_GETP); mCodeAreaPos++;
						mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
						mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
						retresaddr = frame.value;
						frame.value++;
					}
					//putCode((param.mSubType == stPostInc ? VM_INC : VM_DEC) + 3, node_pos);
					//putCode( 0, node_pos);
					//putCode( resaddr, node_pos);
					if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
					if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
					mCodeArea[mCodeAreaPos] = (short)((param.mSubType == stPostInc ? VM_INC : VM_DEC) + 3); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
					mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
					return retresaddr;
				}

				default:
					error(Error.CannotModifyLHS);
					return 0;
				}
			}


		case Token.T_SUPER: // 'super'
		{
			// refer super class
			//int dp;
			ExprNode node1;
			if( mParent != null && mParent.mContextType == ContextType.PROPERTY ) {
				if( (node1 = mParent.mParent.mSuperClassExpr) == null ) {
					error(Error.CannotGetSuper);
					return 0;
				}
			} else {
				if( mParent == null || (node1 = mParent.mSuperClassExpr) == null ) {
					error(Error.CannotGetSuper);
					return 0;
				}
			}

			mAsGlobalContextMode = true;
			// the code must be generated in global context

			try {
				resaddr = genNodeCode(frame, node1, restype, reqresaddr, param);
			} finally {
				mAsGlobalContextMode = false;
			}

			return resaddr;
		}

		case Token.T_THIS:
			if( param.mSubType != 0 ) error(Error.CannotModifyLHS);
			return -1;

		case Token.T_THIS_PROXY:
			// this-proxy is a special register that points
			// both "objthis" and "global"
			// if refering member is not in "objthis", this-proxy
			// refers "global".
			return -mVariableReserveCount;

		case Token.T_WITHDOT: // unary '.' operator
		{
			// dot operator omitting object name
			ExprNode nodep = new ExprNode();
			nodep.setOpecode(Token.T_DOT);
			nodep.setPosition(node_pos);
			ExprNode node1 = new ExprNode();
			mNodeToDeleteVector.add(node1);
			nodep.add(node1);
			node1.setOpecode(Token.T_WITHDOT_PROXY);
			node1.setPosition(node_pos);
			nodep.add( node.getNode(0) );
			return genNodeCode(frame, nodep, restype, reqresaddr, param);
	 	}

		case Token.T_WITHDOT_PROXY:
		{
			// virtual left side of "." operator which omits object

			// search in NestVector
			int i = mNestVector.size() -1;
			for(; i>=0; i--) {
				NestData data = mNestVector.get(i);
				if( data.Type == ntWith ) {
					// found
					return data.RefRegister;
				}
			}

			// not found in NestVector ...
		}
		// NO "break" HERE!!!!!! (pass thru to global)

		case Token.T_GLOBAL:
		{
			if( param.mSubType != 0 ) error(Error.CannotModifyLHS);
			if( (restype & RT_NEEDED) == 0 ) return 0;
			//putCode(VM_GLOBAL, node_pos);
			//putCode( frame.value, node_pos);
			if( (mCodeAreaPos+1) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_GLOBAL); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
			frame.value++;
			return frame.value-1;
		}

		case Token.T_INLINEARRAY:
		{
			// inline array
			int arraydp = putData( new Variant( "Array") );
			//	global %frame0
			//	gpd %frame1, %frame0 . #arraydp // #arraydp = Array
			int frame0 = frame.value;
			if( (mCodeAreaPos+12) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			//putCode(VM_GLOBAL, node_pos);
			//putCode((frame.value+0), node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_GLOBAL); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
			//putCode(VM_GPD, node_pos);
			//putCode((frame.value+1), node_pos);
			//putCode((frame.value+0), node_pos);
			//putCode( arraydp, node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_GPD); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value+1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(arraydp); mCodeAreaPos++;
			//	new %frame0, %frame1()
			//putCode(VM_NEW, node_pos);
			//putCode((frame.value+0), node_pos);
			//putCode((frame.value+1), node_pos);
			//putCode(0);  // argument count for "new Array"
			mCodeArea[mCodeAreaPos] = (short)(VM_NEW); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value+1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
			//	const %frame1, #zerodp
			int zerodp = putData( new Variant(0) );
			//putCode(VM_CONST, node_pos);
			//putCode((frame.value+1), node_pos);
			//putCode( zerodp, node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_CONST); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value+1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(zerodp); mCodeAreaPos++;
			frame.value += 2;

			mArrayArgStack.push( new ArrayArg() );
			mArrayArgStack.peek().Object = frame0;
			mArrayArgStack.peek().Counter = frame0 + 1;

			int nodesize = node.getSize();
			if( node.getSize() == 1 && node.getNode(0).getNode(0) == null ) {
				// the element is empty
			} else {
				for( int i = 0; i<nodesize; i++) {
					genNodeCode(frame, node.getNode(i), RT_NEEDED, 0, new SubParam() ); // elements
				}
			}

			mArrayArgStack.pop();
			return (restype & RT_NEEDED) != 0 ? (frame0):0;
		}

		case Token.T_ARRAYARG:
		{
			// an element of inline array
			int framestart = frame.value;

			resaddr = node.getNode(0) != null ? genNodeCode( frame, node.getNode(0), RT_NEEDED, 0, new SubParam()):0;

			// spis %object.%count, %resaddr
			//putCode(VM_SPIS, node_pos);
			//putCode((mArrayArgStack.peek().Object));
			//putCode((mArrayArgStack.peek().Counter));
			//putCode( resaddr);
			if( (mCodeAreaPos+5) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_SPIS); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(mArrayArgStack.peek().Object); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(mArrayArgStack.peek().Counter); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(resaddr); mCodeAreaPos++;
			// inc %count
			//putCode(VM_INC);
			//putCode((mArrayArgStack.peek().Counter));
			mCodeArea[mCodeAreaPos] = (short)(VM_INC); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(mArrayArgStack.peek().Counter); mCodeAreaPos++;

			clearFrame(frame, framestart);

			return 0;
		}


		case Token.T_INLINEDIC:
		{
			// inline dictionary
			int dicdp = putData( new Variant( "Dictionary" ) );
			//	global %frame0
			//	gpd %frame1, %frame0 . #dicdp // #dicdp = Dictionary
			int frame0 = frame.value;
			if( (mCodeAreaPos+9) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			//putCode(VM_GLOBAL, node_pos);
			//putCode((frame.value+0), node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_GLOBAL); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
			//putCode(VM_GPD, node_pos);
			//putCode((frame.value+1), node_pos);
			//putCode((frame.value+0), node_pos);
			//putCode( dicdp, node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_GPD); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value+1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(dicdp); mCodeAreaPos++;
			//	new %frame0, %frame1()
			//putCode(VM_NEW, node_pos);
			//putCode((frame.value+0), node_pos);
			//putCode((frame.value+1), node_pos);
			//putCode(0);  // argument count for "Dictionary" class
			mCodeArea[mCodeAreaPos] = (short)(VM_NEW); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value+1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
			frame.value += 2;
			clearFrame(frame, frame0 + 1);  // clear register at frame+1

			mArrayArgStack.push(new ArrayArg());
			mArrayArgStack.peek().Object = frame0;

			int nodesize = node.getSize();
			for( int i = 0; i < nodesize; i++) {
				genNodeCode(frame, node.getNode(i), RT_NEEDED, 0, new SubParam()); // element
			}

			mArrayArgStack.pop();
			return (restype & RT_NEEDED) != 0 ? (frame0): 0;
		}

		case Token.T_DICELM:
		{
			// an element of inline dictionary
			int framestart = frame.value;
			int name;
			int value;
			name = genNodeCode(frame, node.getNode(0), RT_NEEDED, 0, new SubParam());
			value = genNodeCode(frame, node.getNode(1), RT_NEEDED, 0, new SubParam());
			// spis %object.%name, %value
			//putCode(VM_SPIS, node_pos);
			//putCode((mArrayArgStack.peek().Object));
			//putCode( name);
			//putCode( value);
			if( (mCodeAreaPos+3) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			mCodeArea[mCodeAreaPos] = (short)(VM_SPIS); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(mArrayArgStack.peek().Object); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(name); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(value); mCodeAreaPos++;

			clearFrame(frame, framestart);

			return 0;
		}

		case Token.T_REGEXP:
		{
			// constant regular expression
			if( (restype & RT_NEEDED) == 0 ) return 0;
			int regexpdp = putData( new Variant( "RegExp" ));
			int patdp = putData(node.getValue());
			int compiledp = putData( new Variant( "_compile" ) );
			// global %frame0
			//	gpd %frame1, %frame0 . #regexpdp // #regexpdp = RegExp
			int frame0 = frame.value;
			if( (mCodeAreaPos+18) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(node_pos);
			//putCode(VM_GLOBAL, node_pos);
			//putCode( frame.value);
			mCodeArea[mCodeAreaPos] = (short)(VM_GLOBAL); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
			//putCode(VM_GPD);
			//putCode((frame.value + 1));
			//putCode( frame.value);
			//putCode( regexpdp);
			mCodeArea[mCodeAreaPos] = (short)(VM_GPD); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value + 1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(regexpdp); mCodeAreaPos++;
			// const frame2, patdp;
			//putCode(VM_CONST);
			//putCode((frame.value + 2));
			//putCode( patdp);
			mCodeArea[mCodeAreaPos] = (short)(VM_CONST); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value + 2); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(patdp); mCodeAreaPos++;
			// new frame0 , frame1();
			//putCode(VM_NEW);
			//putCode( frame.value);
			//putCode((frame.value+1));
			//putCode(0);
			mCodeArea[mCodeAreaPos] = (short)(VM_NEW); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value+1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
			// calld 0, frame0 . #compiledp(frame2)
			//putCode(VM_CALLD);
			//putCode( 0);
			//putCode( frame0);
			//putCode( compiledp);
			//putCode(1);
			//putCode((frame.value+2));
			mCodeArea[mCodeAreaPos] = (short)(VM_CALLD); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame0); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(compiledp); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(1); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(frame.value+2); mCodeAreaPos++;
			frame.value+=3;
			clearFrame(frame, frame0 + 1);

			return frame0;
		}

		case Token.T_VOID:
			if( param.mSubType != 0 ) error( Error.CannotModifyLHS );
			if( (restype & RT_NEEDED) == 0 ) return 0;
			return 0; // 0 is always void
		}
		return 0;
	}
	private void addOmitArg() throws CompileException {
		// omit of the function arguments
		if( mContextType != ContextType.FUNCTION && mContextType != ContextType.EXPR_FUNCTION ) {
			error( Error.CannotOmit );
		}
		mFuncArgStack.peek().IsOmit = true;
	}
	private void endFuncArg() {
		// notify the end of function arguments
		mFuncArgStack.pop();

	}
	private void startFuncArg() {
		// notify the start of function arguments
		// create a stack for function arguments
		FuncArg arg = new FuncArg();
		mFuncArgStack.push(arg);
	}
	private void outputWarning( String mes ) {
		outputWarning( mes,-1 );
	}
	private void outputWarning( String mes, int pos ) {
		int errpos = pos == -1 ? mBlock.getLexicalAnalyzer().getCurrentPosition(): pos;

		StringBuilder strBuilder = new StringBuilder(512);
		strBuilder.append(Error.Warning);
		strBuilder.append(mes);
		strBuilder.append(" at ");
		strBuilder.append(mBlock.getName());
		strBuilder.append(" line ");
		strBuilder.append(String.valueOf( 1 + mBlock.srcPosToLine(errpos) ) );

		//mBlock.getTJS().outputToConsole( strBuilder.toString() );
		TJS.outputToConsole( strBuilder.toString() );
		strBuilder = null;
	}
	private void addFuncArg(int addr, int type ) {
		// add a function argument
		// addr = register address to add
		mFuncArgStack.peek().ArgVector.add( new FuncArgItem(addr, type) );
		if( type == fatExpand || type == fatUnnamedExpand )
			mFuncArgStack.peek().HasExpand = true; // has expanding node
	}
	//void addJumpList() { mJumpList.add( mCodeAreaPos ); }
	public void commit() throws VariantException, TJSException {
		// some context-related processing at final, and commits it
		if( mContextType == ContextType.CLASS ) {
			// clean up super class proxy
			if( mSuperClassGetter != null ) mSuperClassGetter.commit();
		}

		if(mContextType != ContextType.PROPERTY && mContextType != ContextType.SUPER_CLASS_GETTER ) {
			int lexpos = getLexPos();
			//putCode( VM_SRV, lexpos );
			//putCode( 0 );
			if( (mCodeAreaPos+2) >= mCodeArea.length ) expandCodeArea();
			if( CompileState.mEnableDebugCode ) putSrcPos(lexpos);
			mCodeArea[mCodeAreaPos] = (short)(VM_SRV); mCodeAreaPos++;
			mCodeArea[mCodeAreaPos] = (short)(0); mCodeAreaPos++;
			//putCode( VM_RET, -1 );
			mCodeArea[mCodeAreaPos] = (short)(VM_RET); mCodeAreaPos++;
		}

		registerFunction();

		if( mContextType != ContextType.PROPERTY && mContextType != ContextType.SUPER_CLASS_GETTER ) fixCode();

		/*
		if( !DataArea ) {
			DataArea = new tTJSVariant[_DataAreaSize];
			DataAreaSize = _DataAreaSize;

			for(tjs_int i = 0; i<_DataAreaSize; i++) {
				DataArea[i].CopyRef( *_DataArea[i]);
			}

			if(_DataArea) {
				for(tjs_int i = 0; i<_DataAreaSize; i++) delete _DataArea[i];
				TJS_free(_DataArea);
				_DataArea = NULL;
			}
		}
		*/
		mDataArray = new Variant[mDataArea.size()];
		mDataArray = mDataArea.toArray(mDataArray);
		mDataArea.clear();
		mDataArea = null;

		if( mContextType == ContextType.SUPER_CLASS_GETTER )
			mMaxVariableCount = 2; // always 2
		else
			mMaxVariableCount = mNamespace.getMaxCount();

		mSuperClassExpr = null;

		clearNodesToDelete();

		/*
		// compact SourcePosArray to just size
		if( mSourcePosArraySize != 0 && mSourcePosArray != null ) {
			SourcePosArray = (tSourcePos*)TJS_realloc(SourcePosArray, SourcePosArraySize * sizeof(tSourcePos));
			if(!SourcePosArray) TJS_eTJSScriptError(TJSInsufficientMem, Block, 0);
			SourcePosArrayCapa = SourcePosArraySize;
		} */

		/*
		// compact CodeArea to just size
		if(CodeAreaSize && CodeArea) {
			// must inflate the code area
			CodeArea = (tjs_int32*)TJS_realloc(CodeArea,sizeof(tjs_int32)*CodeAreaSize);
			if(!CodeArea) TJS_eTJSScriptError(TJSInsufficientMem, Block, 0);
			CodeAreaCapa = CodeAreaSize;
		}
		*/
		/*
		if( mCodeArea.hasArray() ) {
			mCode = mCodeArea.array();
		} else {
			final int count = mCodeArea.position();
			mCode = new short[count];
			mCodeArea.flip();
			mCodeArea.get(mCode);
		}
		*/
		mCode = new short[mCodeAreaPos];
		System.arraycopy(mCodeArea, 0, mCode, 0, mCodeAreaPos );


		// set object type info for debugging
		/* デバッグ用の機能はとりあえず省略
		if( Debug.objectHashMapEnabled() )
			TJSObjectHashSetType( this, getShortDescriptionWithClassName() );
		*/


		// we do thus nasty thing because the std::vector does not free its storage
		// even we call 'clear' method...

		//mNodeToDeleteVector = null; mNodeToDeleteVector = new VectorWrap<ExprNode>(); 直前でクリアされているはず
		mCurrentNodeVector.clear(); // mCurrentNodeVector = null; mCurrentNodeVector = new VectorWrap<ExprNode>();
		mFuncArgStack = null; mFuncArgStack = new Stack<FuncArg>();
		mArrayArgStack = null; mArrayArgStack = new Stack<ArrayArg>();
		mNestVector = null; mNestVector = new VectorWrap<NestData>();
		mJumpList = null; mJumpList = new IntVector();
		mFixList = null; mFixList = new ArrayList<FixData>();
		mNonLocalFunctionDeclVector = null; mNonLocalFunctionDeclVector = new VectorWrap<NonLocalFunctionDecl>();
	}
	private void clearNodesToDelete() {
		if( mNodeToDeleteVector.size() > 0 ) {
			int count = mNodeToDeleteVector.size();
			for( int i = count-1; i >= 0; i-- ) {
				mNodeToDeleteVector.get(i).clear();
			}
		}
		mNodeToDeleteVector.clear();
	}
	private void fixCode() {
		// code re-positioning and patch processing
		// OriginalTODO: InterCodeContext::fixCode fasten the algorithm

		// create 'regmember' instruction to register class members to
		// newly created object
		if( mContextType == ContextType.CLASS ) {
			// generate a code
			//ByteBuffer buff = ByteBuffer.allocate(2);
			//buff.order( ByteOrder.nativeOrder() );
			//ShortBuffer code = buff.asShortBuffer();
			//code.clear();
			//code.put( (short) VM_REGMEMBER );
			short[] newbuff = new short[1];
			ShortBuffer code = ShortBuffer.wrap(newbuff);
			code.clear();
			code.put( (short) VM_REGMEMBER );

			// make a patch information
			// use FunctionRegisterCodePoint for insertion point
			mFixList.add( new FixData(mFunctionRegisterCodePoint, 0, 1, code, true));
		}

		// process funtion reservation to enable backward reference of
		// global/method functions
		if( mNonLocalFunctionDeclVector.size() >= 1 ) {
			if( mMaxFrameCount < 1) mMaxFrameCount = 1;

			//std::vector<tNonLocalFunctionDecl>::iterator func;
			Iterator<NonLocalFunctionDecl> func;

			// make function registration code to objthis

			// compute codesize
			int codesize = 2;
			func = mNonLocalFunctionDeclVector.iterator();
			while( func.hasNext() ){
				NonLocalFunctionDecl dec = (NonLocalFunctionDecl)func.next();
				if( dec.ChangeThis ) codesize += 10;
				else codesize += 7;
		    }
			short[] newbuff = new short[codesize];
			//ByteBuffer buff = ByteBuffer.allocate(codesize*2);
			//buff.order( ByteOrder.nativeOrder() );
			//ShortBuffer code = buff.asShortBuffer();
			ShortBuffer code = ShortBuffer.wrap(newbuff);
			code.clear();

			// generate code
			func = mNonLocalFunctionDeclVector.iterator();
			while( func.hasNext() ){
				NonLocalFunctionDecl dec = func.next();

				// const %1, #funcdata
				code.put( VM_CONST );
				code.put( (short) 1);
				code.put( (short) dec.DataPos );

				// chgthis %1, %-1
				if( dec.ChangeThis ) {
					code.put( VM_CHGTHIS );
					code.put( (short) 1 );
					code.put( (short) -1 );
				}

				// spds %-1.#funcname, %1
				code.put( VM_SPDS );
				code.put( (short) -1 ); // -1 =  objthis
				code.put( (short) dec.NameDataPos);
				code.put( (short) 1 );
			}

			// cl %1
			code.put( VM_CL );
			code.put( (short) 1 );
			code.flip();

			// make a patch information
			mFixList.add( new FixData(mFunctionRegisterCodePoint, 0, codesize, code, true) );

			mNonLocalFunctionDeclVector.clear();
		}

		// sort SourcePosVector
		sortSourcePos();

		// re-position patch
		int count = mFixList.size();
		for( int i = 0; i < count; i++ ) {
			FixData fix = mFixList.get(i);

			int jcount = mJumpList.size();
			for( int j = 0; j < jcount; j++ ) {
				int jmp = mJumpList.get(j);
				int jmptarget = mCodeArea[ jmp + 1 ] + jmp;
				if( jmp >= fix.StartIP && jmp < fix.Size + fix.StartIP ) {
					// jmp is in the re-positioning target -> delete
					mJumpList.remove(j);
					if( (j+1) < jcount ) {
						j++;
						jmp = mJumpList.get(j);
					} else {
						jmp = 0;
					}
				} else if( fix.BeforeInsertion ?
					(jmptarget < fix.StartIP):(jmptarget <= fix.StartIP)
					&& jmp > fix.StartIP + fix.Size ||
					jmp < fix.StartIP && jmptarget >= fix.StartIP + fix.Size)
				{
					// jmp and its jumping-target is in the re-positioning target
					int v = mCodeArea[ jmp + 1 ];
					v += fix.NewSize - fix.Size;
					mCodeArea[ jmp+1] = (short) v;
				}

				if( jmp >= fix.StartIP + fix.Size) {
					// fix up jmp
					jmp += fix.NewSize - fix.Size;
					mJumpList.set( j, Integer.valueOf(jmp) );
				}
			}

			// move the code
			if( fix.NewSize > fix.Size ) {
				// when code inflates on fixing
				//final int newBufferSize = 2 * (mCodeArea.position() + fix.NewSize - fix.Size);
				//ByteBuffer buff = ByteBuffer.allocate( newBufferSize );
				//buff.order( ByteOrder.nativeOrder() );
				//ShortBuffer ibuff = buff.asShortBuffer();
				final int newBufferSize = (mCodeAreaPos + fix.NewSize - fix.Size);
				short[] newbuff = new short[newBufferSize];
				//ShortBuffer ibuff = ShortBuffer.wrap(newbuff);
				System.arraycopy(mCodeArea, 0, newbuff, 0, mCodeAreaPos);
				mCodeArea = null;
				mCodeArea = newbuff;

				//ibuff.clear();
				//mCodeArea.flip();
				//ibuff.put( mCodeArea );
				//mCodeArea = null;
				//mCodeArea = ibuff;
			}

			if( mCodeAreaPos - (fix.StartIP + fix.Size) > 0 ) {
				// move the existing code
				int dst = fix.StartIP + fix.NewSize;
				int src = fix.StartIP + fix.Size;
				int size = mCodeAreaPos - (fix.StartIP + fix.Size);

				//ByteBuffer buff = ByteBuffer.allocate(size*2);
				//buff.order( ByteOrder.nativeOrder() );
				//ShortBuffer ibuff = buff.asShortBuffer();
				short[] newbuff = new short[size];
				//ShortBuffer ibuff = ShortBuffer.wrap(newbuff);
				//ibuff.clear();
				//for( int j = 0; j < size; j++ ) { // テンポラリへコピー
					//ibuff.put( j, mCodeArea[src+j] );
				//}
				//for( int j = 0; j < size; j++ ) {
				///	mCodeArea[dst+j] = ibuff.get(j);
				//}
				//ibuff = null;
				System.arraycopy( mCodeArea, src, newbuff, 0, size );
				System.arraycopy( newbuff, 0, mCodeArea, dst, size );

				// move sourcepos
				if( CompileState.mEnableDebugCode ) {
					final int srcSize = mSrcPosArrayPos;
					long[] srcPos = mSourcePosArray;
					for( int j = 0; j < srcSize; j++) {
						long val = srcPos[j];
						val >>>= 32;
						if( val >= fix.StartIP + fix.Size ) {
							val += fix.NewSize - fix.Size;
							val = (val << 32) | (srcPos[j]&0xFFFFFFFFL);
							srcPos[j] = val;
						}
					}
				}
			}

			if( fix.NewSize > 0 && fix.Code != null ) {
				// copy the new code
				int size = fix.NewSize;
				int dst = fix.StartIP;
				for( int j = 0; j < size; j++ ) {
					mCodeArea[ dst+j] = fix.Code.get(j);
				}
			}

			mCodeAreaPos = mCodeAreaPos + fix.NewSize-fix.Size;
		}

		// eliminate redundant jump codes
		int jcount = mJumpList.size();
		for( int i = 0; i < jcount; i++ ) {
			int jmp = mJumpList.get(i);

			int jumptarget = mCodeArea[ jmp + 1 ] + jmp;
			int jumpcode = mCodeArea[jmp];
			int addr = jmp;
			addr += mCodeArea[addr + 1];
			for(;;) {
				if( mCodeArea[addr] == VM_JMP ||
					(mCodeArea[addr] == jumpcode && (jumpcode == VM_JF || jumpcode == VM_JNF))) {
					// simple jump code or
					// JF after JF or JNF after JNF
					jumptarget = mCodeArea[addr + 1] + addr; // skip jump after jump
					if(mCodeArea[addr + 1] != 0)
						addr += mCodeArea[addr + 1];
					else
						break; // must be an error
				} else if(mCodeArea[addr] == VM_JF && jumpcode == VM_JNF || mCodeArea[addr] == VM_JNF && jumpcode == VM_JF) {
					// JF after JNF or JNF after JF
					jumptarget = addr + 2;
						// jump code after jump will not jump
					addr += 2;
				}
				else
				{
					// other codes
					break;
				}
			}
			mCodeArea[jmp + 1] = (short) (jumptarget - jmp);
		}

		/* アドレス変換は不要
		// convert jump addresses to VM address
		for(std::list<tjs_int>::iterator jmp = JumpList.begin(); jmp!=JumpList.end(); jmp++) {
			CodeArea[*jmp + 1] = TJS_TO_VM_CODE_ADDR(CodeArea[*jmp + 1]);
		}
		*/

		mJumpList.clear();
		mFixList.clear();
	}
	/*
	// クイックソート
	private static int pivot( LongBuffer a, int i, int j ) {
		int k = i + 1;
		while( k <= j && a.get(i) == a.get(k) ) k++;
		if( k > j ) return -1;
		if( a.get(i) >= a.get(k) ) return i;
		return k;
	}
	private static int partition( LongBuffer a, int i, int j, long x ) {
		int l = i, r = j;
		while( l <= r ) {
			while( l <= j && a.get(l) < x )  l++;
			while( r >= i && a.get(r) >= x ) r--;
			if( l > r ) break;
			// swap
			long t = a.get(l);
			a.put( l, a.get(r) );
			a.put( r, t );
			l++; r--;
		}
		return l;
	}
	private static void quickSort( LongBuffer a, int i, int j ){
		if( i == j ) return;
		int p = pivot( a, i, j );
		if( p != -1 ) {
			int k = partition( a, i, j, a.get(p) );
			quickSort( a, i, k-1 );
			quickSort( a, k, j );
		}
	}
	*/
	//private static void bubbleSort( LongBuffer a, int n ) {
	//	long[] array = a.array();
	//	Arrays.sort(array,0,n);
		/*
		for( int i = 0; i < n; i++ ) {
			System.out.print("v: " + array[i] + "\n" );
		}

		long[] copya = new long[n+1];
		System.arraycopy(array,0,copya,0,n);
		Arrays.sort(copya,0,n+1);
		for( int i = 0; i < n; i++ ) {
			if( array[i] != copya[i] ) {
				System.out.print("v: " + array[i] + "\n" );
			}
		}
		// 元々のソート処理
		for( int i = 0; i < n - 1; i++) {
			for( int j = n - 1; j > i; j--) {
				long prev = a.get(j-1);
				long cur = a.get(j);
				if( prev > cur ) {	// 前の要素の方が大きかったら
					// 交換する
					a.put( j, prev );
					a.put( j - 1, cur );
				}
			}
		}
		for( int i = 0; i < n; i++ ) {
			if( array[i] != copya[i] ) {
				System.out.print("v: " + array[i] + "\n" );
			}
		}
		*/
	//}

	private void sortSourcePos() {
		// 上位をcodePos, 下位をsourcePos とする, codePos でのソートなので下位は気にせずソートししまう
		if( !mSourcePosArraySorted && mSourcePosArray != null ) {
			//quickSort( mSourcePosArray, 0, mSourcePosArray.position()-1 );
			//bubbleSort( mSourcePosArray, mSourcePosArray.position() );
			if( CompileState.mEnableDebugCode ) {
				Arrays.sort( mSourcePosArray, 0, mSrcPosArrayPos );
			}
			mSourcePosArraySorted = true;
		}
	}
	private void registerFunction() throws VariantException, TJSException {
		// registration of function to the parent's context
		if( mParent == null ) return;

		if( mContextType == ContextType.PROPERTY_SETTER ) {
			mParent.mPropSetter = this;
			return;
		}
		if( mContextType == ContextType.PROPERTY_GETTER ) {
			mParent.mPropGetter = this;
			return;
		}
		if( mContextType == ContextType.SUPER_CLASS_GETTER ) {
			return; // these are already registered to parent context
		}

		if( mContextType != ContextType.FUNCTION &&  // ctExprFunction is not concerned here
			mContextType != ContextType.PROPERTY &&
			mContextType != ContextType.CLASS ) {
			return;
		}

		int data = -1;
		if( mParent.mContextType == ContextType.TOP_LEVEL ) {
			Variant val;
			val = new Variant( this );
			data = mParent.putData(val);
			val = new Variant(mName);
			int name = mParent.putData(val);
			boolean changethis = mContextType == ContextType.FUNCTION || mContextType == ContextType.PROPERTY;
			mParent.mNonLocalFunctionDeclVector.add( new NonLocalFunctionDecl(data, name, changethis) );
		}

		if( mContextType == ContextType.FUNCTION && mParent.mContextType == ContextType.FUNCTION ) {
			// local functions
			// adds the function as a parent's local variable
			if( data == -1 ) {
				Variant val;
				val = new Variant( this );
				data = mParent.putData(val);
			}
			mParent.initLocalFunction( mName, data );
		}

		if( mParent.mContextType == ContextType.FUNCTION || mParent.mContextType == ContextType.CLASS ) {
			// register members to the parent object
			//Variant val = new Variant( this );
			//mParent.propSet( MEMBERENSURE|IGNOREPROP, mName, val, mParent );
			if( mProperties == null ) mProperties = new ArrayList<Property>();
			addProperty( mName, this );
		}
	}
	static public class Property {
		public String Name;
		public InterCodeGenerator Value;
		public Property( String name, InterCodeGenerator val ) {
			Name = name;
			Value = val;
		}
	}
	private ArrayList<Property> mProperties;
	private void addProperty( String name, InterCodeGenerator val ) {
		mProperties.add( new Property(name,val) );
	}
	ArrayList<Property> getProp() {
		return mProperties;
	}
	public boolean isClass() {
		return( mContextType == ContextType.CLASS || mContextType == ContextType.TOP_LEVEL );
	}
	public void dumpClassStructure( int nest ) {
		StringBuilder builder = new StringBuilder();
		for( int i = 0; i < nest; i++ ) {
			builder.append("  ");
		}
		String space = builder.toString();
		switch(mContextType) {
			case ContextType.TOP_LEVEL:
				TJS.outputToConsole(space+ "Top level Name: " + mName);
				break;
			case ContextType.FUNCTION:
				TJS.outputToConsole(space+ "Function Name: " + mName);
				break;
			case ContextType.EXPR_FUNCTION:
				TJS.outputToConsole(space+ "Expr function Name: " + mName);
				break;
			case ContextType.PROPERTY:
				TJS.outputToConsole(space+ "Property Name: " + mName);
				break;
			case ContextType.PROPERTY_SETTER:
				TJS.outputToConsole(space+ "Property setter Name: " + mName);
				break;
			case ContextType.PROPERTY_GETTER:
				TJS.outputToConsole(space+ "Property getter Name: " + mName);
				break;
			case ContextType.CLASS:
				TJS.outputToConsole(space+ "Class Name: " + mName);
				break;
			case ContextType.SUPER_CLASS_GETTER:
				TJS.outputToConsole(space+ "Super class getter Name: " + mName);
				break;
		}
		TJS.outputToConsole(space+ "  Max variable count: " + mMaxVariableCount);
		TJS.outputToConsole(space+ "  Variable reserve count: " + mVariableReserveCount);
		TJS.outputToConsole(space+ "  Max frame count: " + mMaxFrameCount);
		TJS.outputToConsole(space+ "  Func decl arg count: " + mFuncDeclArgCount);
		TJS.outputToConsole(space+ "  Func decl unnamed arg array base: " + mFuncDeclUnnamedArgArrayBase);
		TJS.outputToConsole(space+ "  Func decl collapse base: " + mFuncDeclCollapseBase);
		if( mPropSetter != null ) {
			TJS.outputToConsole(space+ "  Prop setter:");
			mPropSetter.dumpClassStructure(nest+1);
		}
		else TJS.outputToConsole(space+ "  Prop setter: not found");
		if( mPropGetter != null ) {
			TJS.outputToConsole(space+ "  Prop getter: true");
			mPropGetter.dumpClassStructure(nest+1);
		}
		else TJS.outputToConsole(space+ "  Prop getter: not found");
		if( mSuperClassGetter != null ) {
			TJS.outputToConsole(space+ "  Super class getter:");
			mSuperClassGetter.dumpClassStructure(nest+1);
		} else {
			TJS.outputToConsole(space+ "  Super class getter: not found");
		}
		if( mProperties != null ) {
			final int count = mProperties.size();
			if( count > 0 ) {
				TJS.outputToConsole(space+ "  Members:");
				for( int i = 0; i < count; i++ ) {
					mProperties.get(i).Value.dumpClassStructure(nest+1);
				}
			} else {
				TJS.outputToConsole(space+ "  Members: not found");
			}
		} else {
			TJS.outputToConsole(space+ "  Members: not found");
		}
		{
			TJS.outputToConsole(space+ "  Data array members:");
			final int count = mDataArray.length;
			Variant[] da = mDataArray;
			for( int i = 0; i < count; i++ ) {
				Variant d = da[i];
				Object o = d.toJavaObject();
				if( o instanceof InterCodeGenerator ) {
					((InterCodeGenerator)o).dumpClassStructure(nest+1);
				}
			}
		}
		/*
		if( mContextType == ContextType.CLASS ) {
			if( mParent != null ) {
				TJS.outputToConsole(space+ "  Parent:");
				mParent.dumpClassStructure(nest+1);
			} else {
				TJS.outputToConsole(space+ "  Parent: not found");
			}
		}
		*/
	}


	// VMCodes
	private static final short
		VM_NOP		= 0,
		VM_CONST	= 1,
		VM_CP		= 2,
		VM_CL		= 3,
		VM_CCL		= 4,
		VM_TT		= 5,
		VM_TF		= 6,
		VM_CEQ		= 7,
		VM_CDEQ		= 8,
		VM_CLT		= 9,
		VM_CGT		= 10,
		VM_SETF		= 11,
		VM_SETNF	= 12,
		VM_LNOT		= 13,
		VM_NF		= 14,
		VM_JF		= 15,
		VM_JNF		= 16,
		VM_JMP		= 17,

		VM_INC		= 18,
		VM_INCPD	= 19,
		VM_INCPI	= 20,
		VM_INCP		= 21,
		VM_DEC		= 22,
		VM_DECPD	= 23,
		VM_DECPI	= 24,
		VM_DECP		= 25,
		VM_LOR		= 26,
		VM_LORPD	= 27,
		VM_LORPI	= 28,
		VM_LORP		= 29,
		VM_LAND		= 30,
		VM_LANDPD	= 31,
		VM_LANDPI	= 32,
		VM_LANDP	= 33,
		VM_BOR		= 34,
		VM_BORPD	= 35,
		VM_BORPI	= 36,
		VM_BORP		= 37,
		VM_BXOR		= 38,
		VM_BXORPD	= 39,
		VM_BXORPI	= 40,
		VM_BXORP	= 41,
		VM_BAND		= 42,
		VM_BANDPD	= 43,
		VM_BANDPI	= 44,
		VM_BANDP	= 45,
		VM_SAR		= 46,
		VM_SARPD	= 47,
		VM_SARPI	= 48,
		VM_SARP		= 49,
		VM_SAL		= 50,
		VM_SALPD	= 51,
		VM_SALPI	= 52,
		VM_SALP		= 53,
		VM_SR		= 54,
		VM_SRPD		= 55,
		VM_SRPI		= 56,
		VM_SRP		= 57,
		VM_ADD		= 58,
		VM_ADDPD	= 59,
		VM_ADDPI	= 60,
		VM_ADDP		= 61,
		VM_SUB		= 62,
		VM_SUBPD	= 63,
		VM_SUBPI	= 64,
		VM_SUBP		= 65,
		VM_MOD		= 66,
		VM_MODPD	= 67,
		VM_MODPI	= 68,
		VM_MODP		= 69,
		VM_DIV		= 70,
		VM_DIVPD	= 71,
		VM_DIVPI	= 72,
		VM_DIVP		= 73,
		VM_IDIV		= 74,
		VM_IDIVPD	= 75,
		VM_IDIVPI	= 76,
		VM_IDIVP	= 77,
		VM_MUL		= 78,
		VM_MULPD	= 79,
		VM_MULPI	= 80,
		VM_MULP		= 81,

		VM_BNOT		= 82,
		VM_TYPEOF	= 83,
		VM_TYPEOFD	= 84,
		VM_TYPEOFI	= 85,
		VM_EVAL		= 86,
		VM_EEXP		= 87,
		VM_CHKINS	= 88,
		VM_ASC		= 89,
		VM_CHR		= 90,
		VM_NUM		= 91,
		VM_CHS		= 92,
		VM_INV		= 93,
		VM_CHKINV	= 94,
		VM_INT		= 95,
		VM_REAL		= 96,
		VM_STR		= 97,
		VM_OCTET	= 98,
		VM_CALL		= 99,
		VM_CALLD	= 100,
		VM_CALLI	= 101,
		VM_NEW		= 102,
		VM_GPD		= 103,
		VM_SPD		= 104,
		VM_SPDE		= 105,
		VM_SPDEH	= 106,
		VM_GPI		= 107,
		VM_SPI		= 108,
		VM_SPIE		= 109,
		VM_GPDS		= 110,
		VM_SPDS		= 111,
		VM_GPIS		= 112,
		VM_SPIS		= 113,
		VM_SETP		= 114,
		VM_GETP		= 115,
		VM_DELD		= 116,
		VM_DELI		= 117,
		VM_SRV		= 118,
		VM_RET		= 119,
		VM_ENTRY	= 120,
		VM_EXTRY	= 121,
		VM_THROW	= 122,
		VM_CHGTHIS	= 123,
		VM_GLOBAL	= 124,
		VM_ADDCI	= 125,
		VM_REGMEMBER= 126,
		VM_DEBUGGER	= 127,
		__VM_LAST	= 128;
	// SubType
	static private final short
		stNone		=VM_NOP,
		stEqual		=VM_CP,
		stBitAND	=VM_BAND,
		stBitOR		=VM_BOR,
		stBitXOR	=VM_BXOR,
		stSub		=VM_SUB,
		stAdd		=VM_ADD,
		stMod		=VM_MOD,
		stDiv		=VM_DIV,
		stIDiv		=VM_IDIV,
		stMul		=VM_MUL,
		stLogOR		=VM_LOR,
		stLogAND	=VM_LAND,
		stSAR		=VM_SAR,
		stSAL		=VM_SAL,
		stSR		=VM_SR,

		stPreInc	= __VM_LAST,
		stPreDec	=129,
		stPostInc	=130,
		stPostDec	=131,
		stDelete	=132,
		stFuncCall	=133,
		stIgnorePropGet	=134,
		stIgnorePropSet	=135,
		stTypeOf	=136;
	// FuncArgType
	static private final byte
		fatNormal = 0,
		fatExpand = 1,
		fatUnnamedExpand = 2;

	static private final int RT_NEEDED = 0x0001;   // result needed
	static private final int RT_CFLAG = 0x0002;   // condition flag needed
	static private final int GNC_CFLAG = (1<<(4*8-1)); // true logic
	static private final int GNC_CFLAG_I = (GNC_CFLAG+1); // inverted logic

	/**
	 * バイトコードを出力する
	 * @return
	 */
	public ByteBuffer exportByteCode( Compiler block, ConstArrayData constarray ) {
		int parent = -1;
		if( mParent != null ) {
			parent = block.getCodeIndex(mParent);
		}
		int propSetter = -1;
		if( mPropSetter != null ) {
			propSetter = block.getCodeIndex(mPropSetter);
		}
		int propGetter = -1;
		if( mPropGetter != null ) {
			propGetter = block.getCodeIndex(mPropGetter);
		}
		int superClassGetter = -1;
		if( mSuperClassGetter != null ) {
			superClassGetter = block.getCodeIndex(mSuperClassGetter);
		}
		int name = -1;
		if( mName != null ) {
			name = constarray.putString(mName);
		}
		// 13 * 4 データ部分のサイズ
		//int srcpossize = mSourcePosArray != null ? mSourcePosArray.position() * 8 : 0; // mSourcePosArray は配列の方がいいかな, codepos, sorcepos の順で、int型で登録した方がいいかも
		int srcpossize = 0; // 主にデバッグ用の情報なので出力抑止
		int codesize = (mCode.length%2) == 1 ? mCode.length * 2+2 : mCode.length * 2;
		int datasize = mDataArray.length * 4;
		int scgpsize = mSuperClassGetterPointer != null ? mSuperClassGetterPointer.size() * 4 : 0;
		int propsize = (mProperties != null ? mProperties.size() * 8 : 0)+4;
		int size = 12*4 + srcpossize + codesize + datasize + scgpsize + propsize + 4*4;
		ByteBuffer result = ByteBuffer.allocate(size);
		result.order(ByteOrder.LITTLE_ENDIAN);
		result.clear();
		IntBuffer buf = result.asIntBuffer();
		buf.clear();
		buf.put(parent);
		buf.put(name);
		buf.put(mContextType);
		buf.put(mMaxVariableCount);
		buf.put(mVariableReserveCount);
		buf.put(mMaxFrameCount);
		buf.put(mFuncDeclArgCount);
		buf.put(mFuncDeclUnnamedArgArrayBase);
		buf.put(mFuncDeclCollapseBase);
		buf.put(propSetter);
		buf.put(propGetter);
		buf.put(superClassGetter);

		//int count = mSourcePosArray != null ? mSourcePosArray.position() : 0;
		int count = 0;
		buf.put(count); // 主にデバッグ用の情報なので出力抑止
		/*
		for( int i = 0; i < count ; i++ ) {
			int v = (int) (mSourcePosArray.get(i) >>> 32);
			buf.put(v);
		}
		for( int i = 0; i < count ; i++ ) {
			int v = (int) (mSourcePosArray.get(i) & 0xFFFFFFFFL);
			buf.put(v);
		}
		*/
		count = mCode.length;
		buf.put(count);
		ShortBuffer sbuf = result.asShortBuffer();
		sbuf.clear();
		sbuf.position( buf.position() * 2);
		sbuf.put(mCode);
		if( (count%2) == 1 ) { // アライメント
			sbuf.put((short) 0);
		}
		buf.position(sbuf.position()/2);
		count = mDataArray.length;
		buf.put(count);
		sbuf.position( buf.position() * 2);
		for( int i = 0; i < count ; i++ ) {
			Variant val = mDataArray[i];
			short type = constarray.getType(val);
			short v = (short) constarray.putVariant( val, block );
			sbuf.put(type);
			sbuf.put(v);
		}
		buf.position(sbuf.position()/2);
		count = mSuperClassGetterPointer != null ? mSuperClassGetterPointer.size() : 0;
		buf.put(count);
		for( int i = 0; i < count ; i++ ) {
			int v = mSuperClassGetterPointer.get(i);
			buf.put(v);
		}
		count = 0;
		if( mProperties != null ) {
			count = mProperties.size();
			buf.put(count);
			if( count > 0 ) {
				for( int i = 0; i < count; i++ ) {
					Property prop = mProperties.get(i); // .Value.dumpClassStructure(nest+1);
					int propname = constarray.putString(prop.Name);
					int propobj = -1;
					if( prop.Value != null ) {
						propobj = block.getCodeIndex(prop.Value);
					}
					buf.put(propname);
					buf.put(propobj);
				}
			}
		} else {
			buf.put(count);
		}
		result.limit(result.capacity());
		result.position(0);
		return result;
	}
	public ArrayList<String> toJavaCode( int start, int end ) throws CompileException, VariantException {
		JavaCodeGenerator gen = new JavaCodeGenerator(mCode,mDataArray,this);
		gen.genFunCall( mMaxVariableCount, mMaxFrameCount, mFuncDeclArgCount, mFuncDeclCollapseBase );
		gen.generate( start, end, mFuncDeclUnnamedArgArrayBase, mMaxFrameCount );
		ArrayList<String> ret = gen.getSourceCode();
		gen = null;
		return ret;
	}
	/**
	 * 生成一段階目
	 * @return
	 */
	public InterCodeObject creteCodeObject( ScriptBlock block ) {
		sortSourcePos(); // 常にソートして渡す
		LongBuffer srcPos = null;
		if( mSourcePosArray != null ) {
			srcPos = LongBuffer.wrap(mSourcePosArray);
			srcPos.position(mSrcPosArrayPos);
		}
		return new InterCodeObject( block, mName, mContextType, mCode, mDataArray, mMaxVariableCount, mVariableReserveCount,
				mMaxFrameCount, mFuncDeclArgCount, mFuncDeclUnnamedArgArrayBase, mFuncDeclCollapseBase, mSourcePosArraySorted,
				srcPos, mSuperClassGetterPointer.toArray() );
	}
	public void createSecond( InterCodeObject obj ) {
		obj.setCodeObject(
				mParent != null ? mBlock.getCodeObject(mBlock.getCodeIndex(mParent)) : null,
				mPropSetter != null ? mBlock.getCodeObject(mBlock.getCodeIndex(mPropSetter)) : null,
				mPropGetter != null ? mBlock.getCodeObject(mBlock.getCodeIndex(mPropGetter)) : null,
				mSuperClassGetter != null ? mBlock.getCodeObject(mBlock.getCodeIndex(mSuperClassGetter)) : null );
	}

	// SourceCodeAccessor
	@Override
	public int codePosToSrcPos(int codepos) {
		// converts from
		// CodeArea oriented position to source oriented position
		if( mSourcePosArray == null ) return 0;

		int s = 0;
		int e = mSrcPosArrayPos;
		if(e==0) return 0;
		while( true ) {
			if( e-s <= 1 ) return (int) (mSourcePosArray[s] & 0xFFFFFFFFL);
			int m = s + (e-s)/2;
			if( (mSourcePosArray[m] >>> 32) > codepos)
				e = m;
			else
				s = m;
		}
	}
	@Override
	public int srcPosToLine(int srcpos) {
		return mBlock.srcPosToLine(srcpos);
	}
	@Override
	public String getLine(int line) {
		return mBlock.getLine(line);
	}
	@Override
	public String getScript() {
		return mBlock.getScript();
	}
	@Override
	public int getLineOffset() {
		return mBlock.getLineOffset();
	}

	public int getContextType() { return mContextType; }
	public InterCodeGenerator getParent() { return mParent; }
}
