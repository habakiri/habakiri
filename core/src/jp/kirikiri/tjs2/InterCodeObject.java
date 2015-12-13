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

/**
 * TJS2 バイトコードを持ったオブジェクト
 */
public class InterCodeObject extends CustomObject implements SourceCodeAccessor {

	static private final String mStrFuncs[] = {
		"charAt",
		"indexOf",
		"toUpperCase",
		"toLowerCase",
		"substring",
		"substr",
		"sprintf",
		"replace",
		"escape",
		"split",
		"trim",
		"reverse",
		"repeat"
	};
	static private final int
		StrMethod_charAt = 0,
		StrMethod_indexOf = 1,
		StrMethod_toUpperCase = 2,
		StrMethod_toLowerCase = 3,
		StrMethod_substring = 4,
		StrMethod_substr = 5,
		StrMethod_sprintf = 6,
		StrMethod_replace = 7,
		StrMethod_escape = 8,
		StrMethod_split = 9,
		StrMethod_trim = 10,
		StrMethod_reverse = 11,
		StrMethod_repeat = 12;

	private ScriptBlock mBlock;
	InterCodeObject mParent;
	private String mName;
	private int mContextType;
	private short[] mCode;
	private Variant[] mDataArray;
	/** 引数の数 */
	private int mMaxVariableCount;
	/** 予約領域(this,proxy用) */
	private int mVariableReserveCount;
	/** 関数内レジスタ数(ローカル変数の数) */
	private int mMaxFrameCount;
	private int mFuncDeclArgCount;
	private int mFuncDeclUnnamedArgArrayBase;
	private int mFuncDeclCollapseBase;

	//private boolean mSourcePosArraySorted;
	private LongBuffer mSourcePosArray; // 上位をcodePos, 下位をsourcePos とする

	private InterCodeObject mPropSetter;
	private InterCodeObject mPropGetter;
	private InterCodeObject mSuperClassGetter;
	//private IntVector mSuperClassGetterPointer; // int[] に書き換えた方がいいかも
	private int[] mSuperClassGetterPointer;

	/** ディスアセンブラ */
	private Disassembler mDisassembler;

	private static final int NAMESPACE_DEFAULT_HASH_BITS = 3;
	private static int getContextHashSize( int type ) {
		switch(type) {
		case ContextType.TOP_LEVEL:				return 0;
		case ContextType.FUNCTION:				return 1;
		case ContextType.EXPR_FUNCTION: 		return 1;
		case ContextType.PROPERTY:				return 1;
		case ContextType.PROPERTY_SETTER:		return 0;
		case ContextType.PROPERTY_GETTER:		return 0;
		case ContextType.CLASS:					return NAMESPACE_DEFAULT_HASH_BITS;
		case ContextType.SUPER_CLASS_GETTER:	return 0;
		default:								return NAMESPACE_DEFAULT_HASH_BITS;
		}
	}

	public InterCodeObject( ScriptBlock block, String name, int type, short[] code, Variant[] da, int varcount, int verrescount,
			int maxframe, int argcount, int arraybase, int colbase, boolean srcsorted, LongBuffer srcpos, int[] superpointer ) {
		super(getContextHashSize(type));
		super.mCallFinalize = false;

		mBlock = block;
		//mBlock.add( this );
		mName = name;
		mContextType = type;
		mCode = code;
		mDataArray = da;
		mMaxVariableCount = varcount;
		mVariableReserveCount = verrescount;
		mMaxFrameCount = maxframe;
		mFuncDeclArgCount = argcount;
		mFuncDeclUnnamedArgArrayBase = arraybase;
		mFuncDeclCollapseBase = colbase;
		//mSourcePosArraySorted = srcsorted;
		mSourcePosArray = srcpos;
		mSuperClassGetterPointer = superpointer;
	}
	public void setCodeObject( InterCodeObject parent, InterCodeObject setter, InterCodeObject getter, InterCodeObject superclass ) {
		mParent = parent;
		mPropSetter = setter;
		mPropGetter = getter;
		mSuperClassGetter = superclass;
	}
	protected void finalizeObject() throws VariantException, TJSException {
		if( mPropSetter != null ) mPropSetter = null;
		if( mPropGetter != null ) mPropGetter = null;
		if( mSuperClassGetter != null ) mSuperClassGetter = null;

		mBlock.remove(this);
		if( mContextType!=ContextType.TOP_LEVEL && mBlock != null ) mBlock = null;

		super.finalizeObject();
	}
	public void compact() {
		if( TJS.IsLowMemory ) {
			mSourcePosArray = null;
		}
	}

	private void executeAsFunction(Dispatch2 objthis, Variant[] args, Variant result, int start_ip ) throws VariantException, TJSException {
		int num_alloc = mMaxVariableCount + mVariableReserveCount + 1 + mMaxFrameCount;
		// TJSVariantArrayStackAddRef();

		Variant[] regs = null;
		//int offset = -1;
		try {
			/*
			VariantArrayList.Memory mem = TJS.allocateVA(num_alloc);
			offset = mem.mOffset;
			regs = mem.mArray;
			int arrayOffset = offset + mMaxVariableCount + mVariableReserveCount; // register area
			*/
			regs = new Variant[num_alloc];
			for( int i = 0; i < num_alloc; i++ ) {
				//regs[i] = TJS.allocateVariant();
				regs[i] = new Variant();
			}
			int arrayOffset = mMaxVariableCount + mVariableReserveCount; // register area

			// objthis-proxy
			if( objthis != null ) {
				ObjectProxy proxy = new ObjectProxy();
				proxy.setObjects( objthis, mBlock.getTJS().getGlobal() );
				// OriginalTODO: caching of objthis-proxy

				//ra[-2] = proxy;
				regs[arrayOffset-2].set(proxy);
			} else {
				//proxy.setObjects( null, null );

				Dispatch2 global = mBlock.getTJS().getGlobal();

				//ra[-2].setObject( global, global );
				regs[arrayOffset-2].set(global,global);
			}

//			if( TJSStackTracerEnabled() ) TJSStackTracerPush( this, false );

//			// check whether the objthis is deleting
//			if( TJSWarnOnExecutionOnDeletingObject && TJSObjectFlagEnabled() && mBlock.getTJS().getConsoleOutput() )
//				TJSWarnIfObjectIsDeleting( mBlock.getTJS().getConsoleOutput(), objthis);


			try {
				//ra[-1].SetObject(objthis, objthis);
				regs[arrayOffset-1].set(objthis,objthis);
				//ra[0].Clear();
				//regs[arrayOffset].clear();

				// transfer arguments
				final int numargs = args != null ? args.length : 0;
				if( numargs >= mFuncDeclArgCount ) {
					// given arguments are greater than or equal to desired arguments
					if( mFuncDeclArgCount != 0 ) {
						//Variant *r = ra - 3;
						int r = arrayOffset - 3;
						//Variant **a = args;
						int n = mFuncDeclArgCount;
						int argOffset = 0;
						while( true ) {
							regs[r].set( args[argOffset] );
							argOffset++;
							//*r = **(a++);
							n--;
							if( n == 0 ) break;
							r--;
						}
					}
				} else {
					// given arguments are less than desired arguments
					//Variant *r = ra - 3;
					int r = arrayOffset - 3;
					//Variant **a = args;
					int argOffset = 0;
					int i;
					for(i = 0; i < numargs; i++) {
						regs[r].set( args[argOffset] );
						argOffset++;
						//*(r--) = **(a++);
						r--;
					}
					for(; i < mFuncDeclArgCount; i++) {
						//(r--)->Clear();
						regs[r].clear();
						r--;
					}
				}

				// collapse into array when FuncDeclCollapseBase >= 0
				if( mFuncDeclCollapseBase >= 0 ) {
					//Variant *r = ra - 3 - mFuncDeclCollapseBase; // target variant
					int r = arrayOffset - 3 - mFuncDeclCollapseBase; // target variant
					Dispatch2 dsp = TJS.createArrayObject();
					//*r = new Variant(dsp, dsp);
					regs[r].set(dsp, dsp);
					//dsp->Release();

					if( numargs > mFuncDeclCollapseBase ) {
						// there are arguments to store
						for( int c = 0, i = mFuncDeclCollapseBase; i < numargs; i++, c++) {
							dsp.propSetByNum(0, c, args[i], dsp);
						}
					}
				}

				// execute
				executeCode( regs, arrayOffset, start_ip, args, result);
			} finally {
				//regs[arrayOffset-2].clear();
				//TJS.releaseVariant(regs);
				//TJS.releaseVA( offset, regs );
				regs = null;
				//ra[-2].Clear(); // at least we must clear the object placed at local stack
				//TJSVariantArrayStack->Deallocate(num_alloc, regs);
//				if(TJSStackTracerEnabled()) TJSStackTracerPop();
			}
			//ra[-2].Clear(); // at least we must clear the object placed at local stack
			// TJSVariantArrayStack->Deallocate(num_alloc, regs);
//			if(TJSStackTracerEnabled()) TJSStackTracerPop();
		} finally {
			// TJSVariantArrayStackRelease();
			regs = null;
		}
	}

	private int executeCode( Variant[] ra_org, int ra_offset, int startip, Variant[] args, Variant result) throws TJSScriptError, VariantException {

		// execute VM codes
		int codesave = startip;
		try {
			int code = startip;//mCodeArea.get(startip);

			//if(TJSStackTracerEnabled()) TJSStackTracerSetCodePointer(CodeArea, &codesave);


			Variant[] ra = ra_org;
			Variant[] da = mDataArray;
			short[] ca = mCode;

			int ri;
			boolean flag = false;
			//int op;
			while( true ) {
				codesave = code;
				//op = ca[code];
				switch( ca[code] ) {
				case VM_NOP:
					code++;
					break;

				case VM_CONST:
					//TJS_GET_VM_REG(ra, code[1]).CopyRef(TJS_GET_VM_REG(da, code[2]));
					ra[ra_offset+ca[code+1]].set(da[ca[code+2]]);
					code += 3;
					break;

				case VM_CP:
					//TJS_GET_VM_REG(ra, code[1]).CopyRef(TJS_GET_VM_REG(ra, code[2]));
					ra[ra_offset+ca[code+1]].set(ra[ra_offset+ca[code+2]]);
					code += 3;
					break;

				case VM_CL:
					//TJS_GET_VM_REG(ra, code[1]).Clear();
					ra[ra_offset+ca[code+1]].clear();
					code += 2;
					break;

				case VM_CCL:
					continuousClear(ra, ra_offset, code);
					code += 3;
					break;

				case VM_TT:
					//flag = TJS_GET_VM_REG(ra, code[1]).operator bool();
					flag = ra[ra_offset+ca[code+1]].asBoolean();
					code += 2;
					break;

				case VM_TF:
					//flag = !(TJS_GET_VM_REG(ra, code[1]).operator bool());
					flag = !ra[ra_offset+ca[code+1]].asBoolean();
					code += 2;
					break;

				case VM_CEQ:
					//flag = TJS_GET_VM_REG(ra, code[1]).NormalCompare( TJS_GET_VM_REG(ra, code[2]));
					flag = ra[ra_offset+ca[code+1]].normalCompare( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;

				case VM_CDEQ:
					//flag = TJS_GET_VM_REG(ra, code[1]).DiscernCompare( TJS_GET_VM_REG(ra, code[2]));
					ri = ca[code+2];
					if( ri == 0 ) {
						flag = ra[ra_offset+ca[code+1]].isVoid();
					} else {
						flag = ra[ra_offset+ca[code+1]].discernCompare( ra[ra_offset+ca[code+2]] ).asBoolean();
					}
					code += 3;
					break;

				case VM_CLT:
					//flag = TJS_GET_VM_REG(ra, code[1]).GreaterThan( TJS_GET_VM_REG(ra, code[2]));
					flag = ra[ra_offset+ca[code+1]].greaterThan( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;

				case VM_CGT:
					//flag = TJS_GET_VM_REG(ra, code[1]).LittlerThan( TJS_GET_VM_REG(ra, code[2]));
					flag = ra[ra_offset+ca[code+1]].littlerThan( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;

				case VM_SETF:
					//TJS_GET_VM_REG(ra, code[1]) = flag;
					ra[ra_offset+ca[code+1]].set(flag?1:0);
					code += 2;
					break;

				case VM_SETNF:
					//TJS_GET_VM_REG(ra, code[1]) = !flag;
					ra[ra_offset+ca[code+1]].set(flag?0:1);
					code += 2;
					break;

				case VM_LNOT:
					//TJS_GET_VM_REG(ra, code[1]).logicalnot();
					ra[ra_offset+ca[code+1]].logicalnot();
					code += 2;
					break;

				case VM_NF:
					flag = !flag;
					code ++;
					break;

				case VM_JF:
					// TJS_ADD_VM_CODE_ADDR(dest, x)  ((*(char **)&(dest)) += (x))
					if(flag)
						//TJS_ADD_VM_CODE_ADDR(code, code[1]);
						//code += ra[ra_offset+ca[code+1]).asInteger();
						code += ca[code+1];
					else
						code += 2;
					break;

				case VM_JNF:
					if(!flag)
						//TJS_ADD_VM_CODE_ADDR(code, code[1]);
						//code += ra[ra_offset+ca[code+1]).asInteger();
						code += ca[code+1];
					else
						code += 2;
					break;

				case VM_JMP:
					//TJS_ADD_VM_CODE_ADDR(code, code[1]);
					//code += ra[ra_offset+ca[code+1]).asInteger();
					code += ca[code+1];
					break;

				case VM_INC:
					//TJS_GET_VM_REG(ra, code[1]).increment();
					ra[ra_offset+ca[code+1]].increment();
					code += 2;
					break;

				case VM_INCPD:
					operatePropertyDirect0(ra, ra_offset, code, OP_INC);
					code += 4;
					break;

				case VM_INCPI:
					operatePropertyIndirect0(ra, ra_offset, code, OP_INC);
					code += 4;
					break;

				case VM_INCP:
					operateProperty0(ra, ra_offset, code, OP_INC);
					code += 3;
					break;

				case VM_DEC:
					//TJS_GET_VM_REG(ra, code[1]).decrement();
					ra[ra_offset+ca[code+1]].decrement();
					code += 2;
					break;

				case VM_DECPD:
					operatePropertyDirect0(ra, ra_offset, code, OP_DEC);
					code += 4;
					break;

				case VM_DECPI:
					operatePropertyIndirect0(ra, ra_offset, code, OP_DEC);
					code += 4;
					break;

				case VM_DECP:
					operateProperty0(ra, ra_offset, code, OP_DEC);
					code += 3;
					break;


				// TJS_DEF_VM_P
				case VM_LOR:
					ra[ra_offset+ca[code+1]].logicalorequal( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_LORPD:
					operatePropertyDirect(ra, ra_offset, code, OP_LOR);
					code += 5;
					break;
				case VM_LORPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_LOR );
					code += 5;
					break;
				case VM_LORP:
					operateProperty(ra, ra_offset, code, OP_LOR );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_LAND:
					ra[ra_offset+ca[code+1]].logicalandequal( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_LANDPD:
					operatePropertyDirect(ra, ra_offset, code, OP_LAND );
					code += 5;
					break;
				case VM_LANDPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_LAND );
					code += 5;
					break;
				case VM_LANDP:
					operateProperty(ra, ra_offset, code, OP_LAND );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_BOR:
					ra[ra_offset+ca[code+1]].orEqual( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_BORPD:
					operatePropertyDirect(ra, ra_offset, code, OP_BOR );
					code += 5;
					break;
				case VM_BORPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_BOR );
					code += 5;
					break;
				case VM_BORP:
					operateProperty(ra, ra_offset, code, OP_BOR );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_BXOR:
					ra[ra_offset+ca[code+1]].bitXorEqual( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_BXORPD:
					operatePropertyDirect(ra, ra_offset, code, OP_BXOR );
					code += 5;
					break;
				case VM_BXORPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_BXOR );
					code += 5;
					break;
				case VM_BXORP:
					operateProperty(ra, ra_offset, code, OP_BXOR );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_BAND:
					ra[ra_offset+ca[code+1]].andEqual( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_BANDPD:
					operatePropertyDirect(ra, ra_offset, code, OP_BAND );
					code += 5;
					break;
				case VM_BANDPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_BAND );
					code += 5;
					break;
				case VM_BANDP:
					operateProperty(ra, ra_offset, code, OP_BAND );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_SAR:
					ra[ra_offset+ca[code+1]].rightShiftEqual( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_SARPD:
					operatePropertyDirect(ra, ra_offset, code, OP_SAR );
					code += 5;
					break;
				case VM_SARPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_SAR );
					code += 5;
					break;
				case VM_SARP:
					operateProperty(ra, ra_offset, code, OP_SAR );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_SAL:
					ra[ra_offset+ca[code+1]].leftShiftEqual( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_SALPD:
					operatePropertyDirect(ra, ra_offset, code, OP_SAL );
					code += 5;
					break;
				case VM_SALPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_SAL );
					code += 5;
					break;
				case VM_SALP:
					operateProperty(ra, ra_offset, code, OP_SAL );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_SR:
					ra[ra_offset+ca[code+1]].rbitshiftequal( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_SRPD:
					operatePropertyDirect(ra, ra_offset, code, OP_SR );
					code += 5;
					break;
				case VM_SRPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_SR );
					code += 5;
					break;
				case VM_SRP:
					operateProperty(ra, ra_offset, code, OP_SR );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_ADD:
					ra[ra_offset+ca[code+1]].addEqual( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_ADDPD:
					operatePropertyDirect(ra, ra_offset, code, OP_ADD );
					code += 5;
					break;
				case VM_ADDPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_ADD );
					code += 5;
					break;
				case VM_ADDP:
					operateProperty(ra, ra_offset, code, OP_ADD );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_SUB:
					ra[ra_offset+ca[code+1]].subtractEqual( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_SUBPD:
					operatePropertyDirect(ra, ra_offset, code, OP_SUB );
					code += 5;
					break;
				case VM_SUBPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_SUB );
					code += 5;
					break;
				case VM_SUBP:
					operateProperty(ra, ra_offset, code, OP_SUB );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_MOD:
					ra[ra_offset+ca[code+1]].residueEqual( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_MODPD:
					operatePropertyDirect(ra, ra_offset, code, OP_MOD );
					code += 5;
					break;
				case VM_MODPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_MOD );
					code += 5;
					break;
				case VM_MODP:
					operateProperty(ra, ra_offset, code, OP_MOD );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_DIV:
					ra[ra_offset+ca[code+1]].divideEqual( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_DIVPD:
					operatePropertyDirect(ra, ra_offset, code, OP_DIV );
					code += 5;
					break;
				case VM_DIVPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_DIV );
					code += 5;
					break;
				case VM_DIVP:
					operateProperty(ra, ra_offset, code, OP_DIV );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_IDIV:
					ra[ra_offset+ca[code+1]].idivequal( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_IDIVPD:
					operatePropertyDirect(ra, ra_offset, code, OP_IDIV );
					code += 5;
					break;
				case VM_IDIVPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_IDIV );
					code += 5;
					break;
				case VM_IDIVP:
					operateProperty(ra, ra_offset, code, OP_IDIV );
					code += 4;
					break;
				// TJS_DEF_VM_P
				case VM_MUL:
					ra[ra_offset+ca[code+1]].multiplyEqual( ra[ra_offset+ca[code+2]] );
					code += 3;
					break;
				case VM_MULPD:
					operatePropertyDirect(ra, ra_offset, code, OP_MUL );
					code += 5;
					break;
				case VM_MULPI:
					operatePropertyIndirect(ra, ra_offset, code, OP_MUL );
					code += 5;
					break;
				case VM_MULP:
					operateProperty(ra, ra_offset, code, OP_MUL );
					code += 4;
					break;
				// TJS_DEF_VM_P


				case VM_BNOT:
					//TJS_GET_VM_REG(ra, code[1]).bitnot();
					ra[ra_offset+ca[code+1]].bitnot();
					code += 2;
					break;

				case VM_ASC:
					//CharacterCodeOf(TJS_GET_VM_REG(ra, code[1]));
					characterCodeOf( ra[ra_offset+ca[code+1]] );
					code += 2;
					break;

				case VM_CHR:
					//CharacterCodeFrom(TJS_GET_VM_REG(ra, code[1]));
					characterCodeFrom( ra[ra_offset+ca[code+1]] );
					code += 2;
					break;

				case VM_NUM:
					//TJS_GET_VM_REG(ra, code[1]).tonumber();
					ra[ra_offset+ca[code+1]].tonumber();
					code += 2;
					break;

				case VM_CHS:
					//TJS_GET_VM_REG(ra, code[1]).changesign();
					ra[ra_offset+ca[code+1]].changesign();
					code += 2;
					break;

				case VM_INV: {
					int offset = ra_offset+ca[code+1];
					boolean tmp = ra[offset].isObject() == false ? false :
						ra[offset].asObjectClosure().invalidate(0, null, ra[ra_offset-1].asObject()) == Error.S_TRUE;
					ra[offset].set(tmp?1:0);
					/*
					TJS_GET_VM_REG(ra, code[1]) =
						TJS_GET_VM_REG(ra, code[1]).Type() != tvtObject ? false :
						(TJS_GET_VM_REG(ra, code[1]).AsObjectClosureNoAddRef().Invalidate(0,
						NULL, NULL, ra[-1].AsObjectNoAddRef()) == TJS_S_TRUE);
					*/
					code += 2;
					break;
				}

				case VM_CHKINV: {
					int offset = ra_offset+ca[code+1];
					boolean tmp;
					if( ra[offset].isObject() == false ) {
						tmp = true;
					} else {
						int ret = ra[offset].asObjectClosure().isValid(0, null, ra[ra_offset-1].asObject());
						tmp = ret == Error.S_TRUE || ret == Error.E_NOTIMPL;
					}
					ra[offset].set(tmp?1:0);
					/*
					TJS_GET_VM_REG(ra, code[1]) =
						TJS_GET_VM_REG(ra, code[1]).Type() != tvtObject ? true :
						TJSIsObjectValid(TJS_GET_VM_REG(ra, code[1]).AsObjectClosureNoAddRef().IsValid(0,
						NULL, NULL, ra[-1].AsObjectNoAddRef()));
					*/
					code += 2;
					break;
				}

				case VM_INT:
					//TJS_GET_VM_REG(ra, code[1]).ToInteger();
					ra[ra_offset+ca[code+1]].toInteger();
					code += 2;
					break;

				case VM_REAL:
					//TJS_GET_VM_REG(ra, code[1]).ToReal();
					ra[ra_offset+ca[code+1]].toReal();
					code += 2;
					break;

				case VM_STR:
					//TJS_GET_VM_REG(ra, code[1]).ToString();
					ra[ra_offset+ca[code+1]].selfToString();
					code += 2;
					break;

				case VM_OCTET:
					//TJS_GET_VM_REG(ra, code[1]).ToOctet();
					ra[ra_offset+ca[code+1]].toOctet();
					code += 2;
					break;

				case VM_TYPEOF:
					//TypeOf(TJS_GET_VM_REG(ra, code[1]));
					typeOf( ra[ra_offset+ca[code+1]] );
					code += 2;
					break;

				case VM_TYPEOFD:
					typeOfMemberDirect(ra, ra_offset, code, Interface.MEMBERMUSTEXIST);
					code += 4;
					break;

				case VM_TYPEOFI:
					typeOfMemberIndirect(ra, ra_offset, code, Interface.MEMBERMUSTEXIST);
					code += 4;
					break;

				case VM_EVAL:
					eval( ra[ra_offset+ca[code+1]],
						TJS.mEvalOperatorIsOnGlobal ? null : ra[ra_offset-1].asObject(),
						true);
					code += 2;
					break;

				case VM_EEXP:
					eval( ra[ra_offset+ca[code+1]],
						TJS.mEvalOperatorIsOnGlobal ? null : ra[ra_offset-1].asObject(),
						false);
					code += 2;
					break;

				case VM_CHKINS:
					instanceOf( ra[ra_offset+ca[code+2]], ra[ra_offset+ca[code+1]] );
					code += 3;
					break;

				case VM_CALL:
				case VM_NEW:
					code += callFunction(ra, ra_offset, code, args );
					break;

				case VM_CALLD:
					code += callFunctionDirect(ra, ra_offset, code, args );
					break;

				case VM_CALLI:
					code += callFunctionIndirect(ra, ra_offset, code, args );
					break;

				case VM_GPD:
					getPropertyDirect(ra, ra_offset, code, 0);
					code += 4;
					break;

				case VM_GPDS:
					getPropertyDirect(ra, ra_offset, code, Interface.IGNOREPROP);
					code += 4;
					break;

				case VM_SPD:
					setPropertyDirect(ra, ra_offset, code, 0);
					code += 4;
					break;

				case VM_SPDE:
					setPropertyDirect(ra, ra_offset, code, Interface.MEMBERENSURE);
					code += 4;
					break;

				case VM_SPDEH:
					setPropertyDirect(ra, ra_offset, code, Interface.MEMBERENSURE|Interface.HIDDENMEMBER);
					code += 4;
					break;

				case VM_SPDS:
					setPropertyDirect(ra, ra_offset, code, Interface.MEMBERENSURE|Interface.IGNOREPROP);
					code += 4;
					break;

				case VM_GPI:
					getPropertyIndirect(ra, ra_offset, code, 0);
					code += 4;
					break;

				case VM_GPIS:
					getPropertyIndirect(ra, ra_offset, code, Interface.IGNOREPROP);
					code += 4;
					break;

				case VM_SPI:
					setPropertyIndirect(ra, ra_offset, code, 0);
					code += 4;
					break;

				case VM_SPIE:
					setPropertyIndirect(ra, ra_offset, code, Interface.MEMBERENSURE);
					code += 4;
					break;

				case VM_SPIS:
					setPropertyIndirect(ra, ra_offset, code, Interface.MEMBERENSURE|Interface.IGNOREPROP);
					code += 4;
					break;

				case VM_GETP:
					getProperty(ra, ra_offset, code);
					code += 3;
					break;

				case VM_SETP:
					setProperty(ra, ra_offset, code);
					code += 3;
					break;

				case VM_DELD:
					deleteMemberDirect(ra, ra_offset, code);
					code += 4;
					break;

				case VM_DELI:
					deleteMemberIndirect(ra, ra_offset, code);
					code += 4;
					break;

				case VM_SRV:
					if( result != null ) result.copyRef( ra[ra_offset+ca[code+1]] );
					code += 2;
					break;

				case VM_RET:
					return code + 1;

				case VM_ENTRY:
					// TJS_FROM_VM_REG_ADDR(x) ((tjs_int)(x) / (tjs_int)sizeof(tTJSVariant))
					// TJS_FROM_VM_CODE_ADDR(x)  ((tjs_int)(x) / (tjs_int)sizeof(tjs_uint32))
					/*
					code = CodeArea + ExecuteCodeInTryBlock(ra, code-CodeArea + 3, args,
						numargs, result, TJS_FROM_VM_CODE_ADDR(code[1])+code-CodeArea,
						TJS_FROM_VM_REG_ADDR(code[2]));
					*/
					code = executeCodeInTryBlock( ra, ra_offset, code+3, args,
							result, ca[code+1]+code, ca[code+2]);
					break;

				case VM_EXTRY:
					return code+1;  // same as ret

				case VM_THROW:
					throwScriptException( ra[ra_offset+ca[code+1]],
						mBlock, codePosToSrcPos(code) );
					code += 2; // actually here not proceed...
					break;

				case VM_CHGTHIS:
					 ra[ra_offset+ca[code+1]].changeClosureObjThis(
						 ra[ra_offset+ca[code+2]].asObject());
					code += 3;
					break;

				case VM_GLOBAL:
					ra[ra_offset+ca[code+1]].set(mBlock.getTJS().getGlobal());
					code += 2;
					break;

				case VM_ADDCI:
					addClassInstanceInfo(ra, ra_offset,code);
					code+=3;
					break;

				case VM_REGMEMBER:
					//registerObjectMember( ra[ra_offset-1].asObject() );
					copyAllMembers( (CustomObject)ra[ra_offset-1].asObject() );
					code ++;
					break;

				case VM_DEBUGGER:
					//TJSNativeDebuggerBreak();
					code++;
					break;

				default:
					throwInvalidVMCode();
				}
			}/*
		} catch( TJSSilentException e ) {
			throw e;
			*/
		} catch( TJSScriptException e ) {
			e.addTrace( this, codesave );
			//e.printStackTrace();
			throw e;
		} catch( TJSScriptError e ) {
			e.addTrace( this, codesave );
			//e.printStackTrace();
			throw e;
		} catch( TJSException e ) {
			displayExceptionGeneratedCode( codesave, ra_org, ra_offset );
			//TJS_eTJSScriptError( e.getMessage(), this, codesave );
			//e.printStackTrace();
			Error.reportExceptionSource( e.getMessage(), this, codePosToSrcPos(codesave) );
			throw new TJSScriptError(e.getMessage(), mBlock, codePosToSrcPos(codesave) );
		} catch( Exception e ) {
			e.printStackTrace(); /* TODO 組み込み例外位置がわからなくなってしまうのを防ぐためにここで吐く */
			displayExceptionGeneratedCode( codesave, ra_org, ra_offset );
			//TJS_eTJSScriptError( e.getMessage(), this, codesave );
			Error.reportExceptionSource( e.getMessage(), this, codePosToSrcPos(codesave) );
			throw new TJSScriptError(e.getMessage(), mBlock, codePosToSrcPos(codesave) );
		}

		//return codesave;
	}

	private final void operateProperty(Variant[] ra, int ra_offset, int code, int ope) throws TJSException, VariantException {
		short[] ca = mCode;
		Variant ra_code2 = ra[ra_offset+ca[code+2]];
		Variant ra_code3 = ra[ra_offset+ca[code+3]];
		VariantClosure clo =  ra_code2.asObjectClosure();
		final int offset = ca[code+1];
		Variant result = offset != 0 ? ra[ra_offset+offset] : null;
		Dispatch2 objThis = clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject();
		int hr = clo.operation(ope, null, result, ra_code3, objThis );
		if( hr < 0 ) throwFrom_tjs_error(hr, null);
	}
	private final void operatePropertyIndirect(Variant[] ra, int ra_offset, int code, int ope ) throws TJSException, VariantException {
		short[] ca = mCode;
		Variant ra_code2 = ra[ra_offset+ca[code+2]];
		Variant ra_code3 = ra[ra_offset+ca[code+3]];
		Variant ra_code4 = ra[ra_offset+ca[code+4]];
		VariantClosure clo = ra_code2.asObjectClosure();
		final int offset = ca[code+1];
		Variant result = offset!=0 ? ra[ra_offset+offset] : null;
		Dispatch2 objThis = clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject();
		if( ra_code3.isInteger() != true ) {
			String str = ra_code3.asString();
			int hr = clo.operation( ope, str, result, ra_code4, objThis );
			if( hr < 0 ) throwFrom_tjs_error( hr, str );
		} else {
			int num = ra_code3.asInteger();
			int hr = clo.operationByNum( ope, num, result, ra_code4, objThis );
			if( hr < 0 ) throwFrom_tjs_error_num( hr, num );
		}
	}
	private final void operatePropertyDirect(Variant[] ra, int ra_offset, int code, int ope ) throws TJSException, VariantException {
		short[] ca = mCode;
		Variant ra_code2 = ra[ra_offset+ca[code+2]];
		Variant da_code3 = mDataArray[ca[code+3]];
		Variant ra_code4 = ra[ra_offset+ca[code+4]];
		VariantClosure clo =  ra_code2.asObjectClosure();
		String nameStr = da_code3.getString();
		final int offset = ca[code+1];
		Variant result = offset != 0 ? ra[ra_offset+offset] : null;
		Dispatch2 objThis = clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject();
		int hr = clo.operation(ope, nameStr, result, ra_code4, objThis );
		if( hr < 0 ) throwFrom_tjs_error( hr, nameStr );
	}
	private final void displayExceptionGeneratedCode(int codepos, Variant[] ra, int ra_offset) throws VariantException {
		StringBuilder builder = new StringBuilder(128);
		builder.append("==== An exception occured at ");
		builder.append(getPositionDescriptionString(codepos));
		builder.append(", VM ip = ");
		builder.append(codepos);
		builder.append(" ====");
		final int info_len = builder.length();

		TJS.outputToConsole( builder.toString() );
		builder.delete(0, builder.length());
		TJS.outputToConsole( "-- Disassembled VM code --" );
		disassenbleSrcLine(codepos);

		TJS.outputToConsole( "-- Register dump --" );

		final int ra_start = ra_offset - (mMaxVariableCount + mVariableReserveCount);
		int ra_count = mMaxVariableCount + mVariableReserveCount + 1 + mMaxFrameCount;
		StringBuilder line = new StringBuilder(128);
		// if( (ra_count + ra_start) > ra.size() ) ra_count = ra.size() - ra_start;
		for( int i = 0; i < ra_count; i ++ ) {
			builder.append("%");
			builder.append(( i - (mMaxVariableCount + mVariableReserveCount)));
			builder.append('=');
			builder.append(Utils.VariantToReadableString( ra[ ra_start + i ] ));
			if( line.length() + builder.length() + 2 > info_len ) {
				TJS.outputToConsole( line.toString() );
				line.delete(0, line.length());
				line.append( builder );
			} else {
				if( line.length() > 0 ) line.append( "  " );
				line.append( builder );
			}
			builder.delete(0, builder.length());
		}

		if( line.length() > 0 ) {
			TJS.outputToConsole( line.toString() );
		}
		/* デバッグのため追加 start TODO */
		/*
		TJS.outputToConsole( "-- Data dump --" );
		final int dataCount = mDataArea.size();
		for( int i = 0; i < dataCount; i++ ) {
			Variant v = mDataArea.get(i);
			String data_info = "*" + String.valueOf(i) + "=" +  Utils.VariantToReadableString(v);
			TJS.outputToConsole( data_info );
		}
		*/
		/* デバッグのため追加 end */
		TJS.outputToConsoleSeparator( "-", info_len );
	}
	/*
	private void dumpCodePosSrcPos() {
		final int count = mSourcePosArray.position();
		for( int i = 0; i < count; i++ ) {
			long pos = mSourcePosArray.get(i);
			String buf = String.format("code:%d,src:%d", pos>>>32, pos&0xFFFFFFFFL );
			TJS.outputToConsole(buf);
		}
	}
	*/

	private void disassenbleSrcLine(int codepos) throws VariantException {
		int start = findSrcLineStartCodePos(codepos);
		disassemble(start, codepos + 1);
	}
	private void disassemble(int start, int end ) throws VariantException {
		if( mDisassembler == null ) {
			mDisassembler = new Disassembler(mCode,mDataArray,this);
		} else {
			mDisassembler.set(mCode,mDataArray,this);
		}
		mDisassembler.disassemble( mBlock, start, end);
	}
	public void disassemble(ScriptBlock data, int start, int end) throws VariantException {
		if( mDisassembler == null ) {
			mDisassembler = new Disassembler(mCode,mDataArray,this);
		} else {
			mDisassembler.set(mCode,mDataArray,this);
		}
		mDisassembler.disassemble( data, start, end);
	}

	/**
	 * 同一行の最初のコード位置を得る
	 * @param codepos 検索するコード位置
	 * @return 同一行の最初のコード位置
	 */
	public int findSrcLineStartCodePos(int codepos) {
		// find code address which is the first instruction of the source line
		if( mSourcePosArray == null ) return 0;

		int srcpos = codePosToSrcPos(codepos);
		int line = mBlock.srcPosToLine(srcpos);
		srcpos = mBlock.lineToSrcPos(line);

		int codeposmin = -1;
		final int count = mSourcePosArray.position();
		for( int i = 0; i < count; i++) {
			// 上位をcodePos, 下位をsourcePos とする
			long sourcePosArray = mSourcePosArray.get(i);
			int sourcePos = (int) ( sourcePosArray & 0xFFFFFFFFL);
			if( sourcePos >= srcpos ) {
				int codePos = (int)((sourcePosArray >> 32) & 0xFFFFFFFFL);
				if(codeposmin == -1 || codePos < codeposmin )
					codeposmin = codePos;
			}
		}
		if(codeposmin < 0) codeposmin = 0;
		return codeposmin;
	}
	static private void throwInvalidVMCode() throws TJSException {
		throw new TJSException(Error.InvalidOpecode);

	}

	/*
	private void registerObjectMember(Dispatch2 dest) throws VariantException, TJSException {
		if( dest instanceof CustomObject ) {
			copyAllMembers( (CustomObject)dest );
		} else {
			// register this object member to 'dest' (destination object).
			// called when new object is to be created.
			// a class to receive member callback from class
			class Callback extends Dispatch {
				public Dispatch2 mDest; // destination object
				public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
					// *param[0] = name   *param[1] = flags   *param[2] = value
					int flags = param[1].asInteger();
					if( (flags & Interface.STATICMEMBER) == 0 ) {
						Variant val = param[2];
						if( val.isObject() ) {
							// change object's objthis if the object's objthis is null
							val.changeClosureObjThis(mDest);
						}
						mDest.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP|flags, param[0].asString(), val, mDest);
					}
					if( result != null ) result.set(1); // returns true
					return Error.S_OK;
				}
			};

			Callback callback = new Callback();
			callback.mDest = dest;

			// enumerate members
			enumMembers( Interface.IGNOREPROP, new VariantClosure( callback, null), this);
		}
	}
	*/
	private void addClassInstanceInfo(Variant[] ra, int ra_offset, int code) throws VariantException {
		short[] ca = mCode;
		Dispatch2 dsp;
		dsp = ra[ ra_offset+ca[code+1] ].asObject();
		if( dsp != null ) {
			//dsp.classInstanceInfo( Interface.CII_ADD, 0, ra[ra_offset+mCode[code+2]] );
			dsp.addClassInstanveInfo( ra[ra_offset+ca[code+2]].asString() );
		} else {
			// ?? must be an error
		}
	}
	private void throwScriptException( Variant val, ScriptBlock block, int srcpos ) throws TJSException, VariantException {
		String msg = null;
		if( val.isObject() ) {
			VariantClosure clo = val.asObjectClosure();
			if( clo.mObject != null ) {
				Variant v2 = new Variant();
				String message_name = "message";
				int hr = clo.propGet( 0, message_name, v2, null );
				if( hr >= 0 ) {
					msg = "script exception : " + v2.asString();
				}
			}
		}
		if(msg == null || msg.length() == 0 ) {
			msg = "script exception";
		}

		//TJSReportExceptionSource(msg, block, srcpos);
		throw new TJSScriptException(msg, block, srcpos, val);
	}
	@Override
	public int codePosToSrcPos( int codepos ) {
		// converts from
		// CodeArea oriented position to source oriented position
		if( mSourcePosArray == null ) return 0;

		int s = 0;
		int e = mSourcePosArray.position();
		if(e==0) return 0;
		while( true ) {
			if( e-s <= 1 ) return (int) (mSourcePosArray.get(s) & 0xFFFFFFFFL);
			int m = s + (e-s)/2;
			if( (mSourcePosArray.get(m) >>> 32) > codepos)
				e = m;
			else
				s = m;
		}
	}
	private int executeCodeInTryBlock(Variant[] ra, int ra_offset, int startip, Variant[] args, Variant result, int catchip, int exobjreg) throws VariantException, TJSException {
		// execute codes in a try-protected block
		try {
			//if( TJS.stackTracerEnabled() ) TJS.stackTracerPush(this, true);
			int ret;
			try {
				ret = executeCode( ra, ra_offset, startip, args, result );
			} finally {
				//if(TJSStackTracerEnabled()) TJSStackTracerPop();
			}
			return ret; /*
		} catch( TJSSilentException e ) {
			throw e; */
		} catch( TJSScriptException e ) {
			if( exobjreg != 0 ) ra[ra_offset+exobjreg].set( e.getValue() );
			return catchip;
		} catch( TJSScriptError e ) {
			if(exobjreg != 0) {
				Variant msg = new Variant(e.getMessage());
				Variant trace = new Variant(e.getTrace());
				Variant ret = new Variant();
				Error.getExceptionObject( mBlock.getTJS(), ret, msg, trace );
				ra[ra_offset+exobjreg].set( ret );
			}
			return catchip;
		} catch( TJSException e ) {
			if( exobjreg != 0 ){
				Variant msg = new Variant( e.getMessage() );
				Variant ret = new Variant();
				Error.getExceptionObject( mBlock.getTJS(), ret, msg, null );
				ra[ra_offset+exobjreg].set( ret );
			}
			return catchip;
		} catch( Exception e ) {
			if( exobjreg != 0 ) {
				Variant msg = new Variant( e.getMessage() );
				Variant ret = new Variant();
				Error.getExceptionObject( mBlock.getTJS(), ret, msg, null );
				ra[ra_offset+exobjreg].set( ret );
			}
			return catchip;
		}
	}
	private void deleteMemberIndirect(Variant[] ra, int ra_offset, int code) throws TJSException, VariantException {
		short[] ca = mCode;
		VariantClosure clo = ra[ra_offset+ca[code+2]].asObjectClosure();
		String str = ra[ra_offset+ca[code+3]].asString();
		int hr;
		try {
			hr = clo.deleteMember( 0, str, clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject() );
			final int offset = ca[code+1];
			if( offset != 0  ) {
				if( hr < 0 )
					ra[ra_offset+offset].set(0);
				else
					ra[ra_offset+offset].set(1);
			}
		} finally {
			str = null;
			clo = null;
		}
	}
	private void deleteMemberDirect(Variant[] ra, int ra_offset, int code) throws TJSException, VariantException {
		short[] ca = mCode;
		VariantClosure clo = ra[ra_offset+ca[code+2]].asObjectClosure();
		int hr;
		try {
			Variant name = mDataArray[ca[code+3]];
			String nameStr = name.getString();
			hr = clo.deleteMember(0, nameStr, ra[ra_offset-1].asObject() );
		} finally {
			clo = null;
		}

		final int offset = ca[code+1];
		if( offset != 0 ) {
			if( hr < 0 )
				ra[ra_offset+offset].set(0);
			else
				ra[ra_offset+offset].set(1);
		}
	}
	private void setProperty(Variant[] ra, int ra_offset, int code) throws TJSException, VariantException {
		short[] ca = mCode;
		VariantClosure clo = ra[ ra_offset+ca[code+1] ].asObjectClosure();
		int hr;
		hr = clo.propSet(0, null, ra[ ra_offset+ca[code+2] ],
			clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject() );
		if( hr < 0 )
			throwFrom_tjs_error( hr, null );
	}
	static private void throwFrom_tjs_error_num( int hr, int num ) throws TJSException {
		Error.throwFrom_tjs_error( hr, String.valueOf(num) );
	}
	static private void throwFrom_tjs_error(int hr, final String name) throws TJSException {
		Error.throwFrom_tjs_error(hr, name);
	}
	private void getProperty(Variant[] ra, int ra_offset, int code) throws TJSException, VariantException {
		short[] ca = mCode;
		VariantClosure clo = ra[ra_offset+ca[code+2]].asObjectClosure();
		int hr = clo.propGet(0, null, ra[ra_offset+ca[code+1]],
			clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject() );
		if( hr < 0 ) throwFrom_tjs_error(hr, null);
	}
	private void setPropertyIndirect(Variant[] ra, int ra_offset, int code, int flags ) throws TJSException, VariantException {
		short[] ca = mCode;
		Variant ra_code1 = ra[ra_offset+ca[code+1]];
		Variant ra_code2 = ra[ra_offset+ca[code+2]];
		Variant ra_code3 = ra[ra_offset+ca[code+3]];
		if( ra_code1.isObject() ) {
			VariantClosure clo = ra_code1.asObjectClosure();
			if( ra_code2.isInteger() != true ) {
				String str;
				str = ra_code2.asString();
				int hr;
				hr = clo.propSet( flags, str, ra_code3, clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject() );
				/*
				hr = clo.propSetByVS( flags, str, ra_code3, clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject() );
				if( hr == Error.E_NOTIMPL )
					hr = clo.propSet( flags, str, ra_code3, clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject() );
				*/
				if( hr < 0 ) throwFrom_tjs_error( hr, str );
			} else {
				int hr;
				hr = clo.propSetByNum(flags, ra_code2.asInteger(), ra_code3, clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject() );
				if( hr < 0 ) throwFrom_tjs_error_num( hr, ra_code2.asInteger() );
			}
		} else if( ra_code1.isString() ) {
			setStringProperty( ra_code3, ra_code1, ra_code2 );
		} else if( ra_code1.isOctet() ) {
			setOctetProperty( ra_code3, ra_code1, ra_code2 );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(ra_code1) );
			throw new VariantException( mes );
		}
	}
	private void getOctetProperty(Variant result, final Variant octet, final Variant member) throws TJSException, VariantException {
		// processes properties toward octets.
		if( member.isNumber() != true  ) {
			final String name = member.getString();
			if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

			if( name.equals("length") ) {
				// get string length
				ByteBuffer o = octet.asOctet();
				if( o != null )
					result.set( o.capacity() );
				else
					result.set( 0 );
				return;
			} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
				ByteBuffer o = octet.asOctet();
				int n = Integer.valueOf( name );
				int len = o != null ? o.capacity() : 0;
				if(n<0 || n>=len)
					throw new TJSException(Error.RangeError);
				result.set( o.get(n) );
				return;
			}
			throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
		} else { // member.Type() == tvtInteger || member.Type() == tvtReal
			ByteBuffer o = octet.asOctet();
			int n = member.asInteger();
			int len = o != null ? o.capacity() : 0;
			if(n<0 || n>=len)
				throw new TJSException( Error.RangeError);
			result.set( o.get(n) );
			return;
		}
	}
	private void setOctetProperty(Variant param, final Variant octet, final Variant member) throws TJSException, VariantException {
		// processes properties toward octets.
		if( member.isNumber() != true  ) {
			final String name = member.getString();
			if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

			if( name.equals("length") ) {
				throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
			} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
				throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
			}
			throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
		} else { // member.Type() == tvtInteger || member.Type() == tvtReal
			throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
		}
	}
	private void getStringProperty(Variant result, final Variant str, final Variant member) throws TJSException, VariantException {
		// processes properties toward strings.
		if( member.isNumber() != true ) {
			final String name = member.getString();
			if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

			if( name.equals("length") ) {
				// get string length
				final String s = str.asString();
				if( s == null )
					result.set(0); // tTJSVariantString::GetLength can return zero if 'this' is NULL
				else
					result.set( s.length() );
				return;
			} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
				final String s = str.asString();
				int n = Integer.valueOf(name);
				int len = s.length();
				if(n == len) { result.set( new String() ); return; }
				if(n<0 || n>len)
					throw new TJSException(Error.RangeError);

				result.set( s.substring(n,n+1) );
				return;
			}
			throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
		} else { // member.Type() == tvtInteger || member.Type() == tvtReal
			final String s = str.asString();
			int n = member.asInteger();
			int len = s.length();
			if(n == len) { result.set( new String() ); return; }
			if(n<0 || n>len)
				throw new TJSException(Error.RangeError);
			result.set( s.substring(n,n+1) );
			return;
		}
	}
	private void setStringProperty(Variant param, final Variant str, final Variant member) throws TJSException, VariantException {
		// processes properties toward strings.
		if( member.isNumber() != true  ) {
			final String name = member.getString();
			if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

			if( name.equals("length") ) {
				throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
			} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
				throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
			}
			throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
		} else { // member.Type() == tvtInteger || member.Type() == tvtReal
			throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
		}
	}
	private void getPropertyIndirect(Variant[] ra, int ra_offset, int code, int flags ) throws TJSException, VariantException {
		short[] ca = mCode;
		Variant ra_code1 = ra[ra_offset+ca[code+1]];
		Variant ra_code2 = ra[ra_offset+ca[code+2]];
		Variant ra_code3 = ra[ra_offset+ca[code+3]];
		if( ra_code2.isObject() ) {
			int hr;
			VariantClosure clo = ra_code2.asObjectClosure();
			if( ra_code3.isInteger() != true  ){
				String str = ra_code3.asString();
				hr = clo.propGet(flags, str, ra_code1, clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject() );
				if( hr < 0 ) throwFrom_tjs_error( hr, str );
			} else {
				hr = clo.propGetByNum(flags, ra_code3.asInteger(), ra_code1, clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject() );
				if( hr < 0 ) throwFrom_tjs_error_num( hr, ra_code3.asInteger() );
			}
		} else if( ra_code2.isString() ) {
			getStringProperty( ra_code1, ra_code2, ra_code3 );
		} else if( ra_code2.isOctet() ) {
			getOctetProperty( ra_code1, ra_code2, ra_code3 );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(ra_code2) );
			throw new VariantException( mes );
		}
	}
	private void setPropertyDirect(Variant[] ra, int ra_offset, int code, int flags ) throws TJSException, VariantException {
		short[] ca = mCode;
		Variant ra_code1 = ra[ra_offset+ca[code+1]];
		Variant da_code2 = mDataArray[ca[code+2]];
		Variant ra_code3 = ra[ra_offset+ca[code+3]];
		if( ra_code1.isObject() ) {
			VariantClosure clo = ra_code1.asObjectClosure();
			String name = da_code2.getString();
			Dispatch2 objThis = clo.mObjThis;
			if( objThis == null ) objThis = ra[ra_offset-1].asObject();
			int hr = clo.propSet(flags, name, ra_code3, objThis );
			if( hr < 0 ) throwFrom_tjs_error( hr, name );
		} else if( ra_code1.isString() ) {
			setStringProperty( ra_code3, ra_code1, da_code2 );
		} else if( ra_code1.isOctet() ) {
			setOctetProperty( ra_code3, ra_code1, da_code2 );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(ra_code1) );
			throw new VariantException( mes );
		}
	}
	private void getPropertyDirect(Variant[] ra, int ra_offset, int code, int flags ) throws TJSException, VariantException {
		short[] ca = mCode;
		Variant ra_code1 = ra[ra_offset+ca[code+1]];
		Variant ra_code2 = ra[ra_offset+ca[code+2]];
		Variant da_code3 = mDataArray[ca[code+3]];
		if( ra_code2.isObject() ) {
			VariantClosure clo = ra_code2.asObjectClosure();
			String nameStr = da_code3.getString();
			int hr = clo.propGet( flags, nameStr, ra_code1, clo.mObjThis != null ? clo.mObjThis : ra[ra_offset-1].asObject() );
			if( hr < 0 ) throwFrom_tjs_error(hr, nameStr );
		} else if( ra_code2.isString() ) {
			getStringProperty( ra_code1, ra_code2, da_code3 );
		} else if( ra_code2.isOctet() ) {
			getOctetProperty( ra_code1, ra_code2, da_code3 );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(ra_code2) );
			throw new VariantException( mes );
		}
	}
	private void processOctetFunction( final String member, final String target, Variant[] args, Variant result) throws TJSException {
		// OrigianlTODO: unpack/pack implementation
		throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, member );
	}
	static public void processStringFunction( final String member, final String target, Variant[] args, Variant result) throws TJSException, VariantException {
		if( member == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

		final int hash = member.hashCode();
		String s = new String(target); // target string
		final int s_len = target.length();
		if((hash == mStrFuncs[StrMethod_charAt].hashCode() && mStrFuncs[StrMethod_charAt].equals(member))) {
			if( args.length != 1) throwFrom_tjs_error(Error.E_BADPARAMCOUNT,null);
			if( s_len == 0 ) {
				if( result != null ) result.set("");
				return;
			}
			int i = args[0].asInteger();
			if( i < 0 || i >= s_len ) {
				if( result != null ) result.set("");
				return;
			}
			if(result != null) result.set( s.substring(i,i+1) );
			return;
		} else if((hash == mStrFuncs[StrMethod_indexOf].hashCode() && mStrFuncs[StrMethod_indexOf].equals(member))) {
			if( args.length != 1 && args.length != 2) throwFrom_tjs_error(Error.E_BADPARAMCOUNT,null);
			String pstr = args[0].asString(); // sub string
			if( s == null || pstr == null ) {
				if( result != null ) result.set( -1 );
				return;
			}
			int start;
			if( args.length == 1) {
				start = 0;
			} else {
				// integer convertion may raise an exception
				start = args[1].asInteger();
			}
			if( start >= s_len ) {
				if( result != null ) result.set( -1 );
				return;
			}
			int found = s.indexOf( pstr, start);
			if( result != null ) result.set(found);
			return;
		} else if((hash == mStrFuncs[StrMethod_toUpperCase].hashCode() && mStrFuncs[StrMethod_toUpperCase].equals(member))) {
			if( args.length != 0) throwFrom_tjs_error( Error.E_BADPARAMCOUNT, null );
			if( result != null ) {
				result.set( s.toUpperCase() );
			}
			return;
		} else if((hash == mStrFuncs[StrMethod_toLowerCase].hashCode() && mStrFuncs[StrMethod_toLowerCase].equals(member))) {
			if( args.length != 0) throwFrom_tjs_error(Error.E_BADPARAMCOUNT,null);
			if( result != null ){
				result.set( s.toLowerCase() );
			}
			return;
		} else if((hash == mStrFuncs[StrMethod_substring].hashCode() && mStrFuncs[StrMethod_substring].equals(member)) || (hash == mStrFuncs[StrMethod_substr].hashCode() && mStrFuncs[StrMethod_substr].equals(member))) {
			if( args.length != 1 && args.length != 2) throwFrom_tjs_error(Error.E_BADPARAMCOUNT,null);
			int start = args[0].asInteger();
			if( start < 0 || start >= s_len ) {
				if( result != null ) result.set("");
				return;
			}
			int count;
			if( args.length == 2 ) {
				count = args[1].asInteger();
				if( count < 0 ) {
					if( result != null ) result.set("");
					return;
				}
				if(start + count > s_len) count = s_len - start;
				if( result != null ) result.set( s.substring( start, start+count ) );
				return;
			} else {
				if( result != null ) result.set( s.substring( start ) );
			}
			return;
		} else if((hash == mStrFuncs[StrMethod_sprintf].hashCode() && mStrFuncs[StrMethod_sprintf].equals(member))) {
			if( result != null ) {
				String res = Utils.formatString( s, args );
				result.set( res );
			}
			return;
		} else if((hash == mStrFuncs[StrMethod_replace].hashCode() && mStrFuncs[StrMethod_replace].equals(member))) {
			// string.replace(pattern, replacement-string)  -->
			// pattern.replace(string, replacement-string)
			if( args.length < 2 ) throwFrom_tjs_error(Error.E_BADPARAMCOUNT,null);

			VariantClosure clo = args[0].asObjectClosure();
			Variant str = new Variant(target);
			Variant[] params = new Variant[2];
			params[0] = str;
			params[1] = args[1];
			final String replace_name = "replace";
			clo.funcCall(0, replace_name, result, params, null );
			return;
		} else if((hash == mStrFuncs[StrMethod_escape].hashCode() && mStrFuncs[StrMethod_escape].equals(member))) {
			if( result != null ) result.set( LexBase.escapeC(target) );
			return;
		} else if((hash == mStrFuncs[StrMethod_split].hashCode() && mStrFuncs[StrMethod_split].equals(member))) {
			// string.split(pattern, reserved, purgeempty) -->
			// Array.split(pattern, string, reserved, purgeempty)
			if( args.length < 1) throwFrom_tjs_error(Error.E_BADPARAMCOUNT,null);

			Dispatch2 array = TJS.createArrayObject();
			try {
				Variant str = new Variant(target);
				Variant[] params;
				if( args.length >= 3 ) {
					params = new Variant[4];
					params[0] = args[0];
					params[1] = str;
					params[2] = args[1];
					params[3] = args[2];
				} else if( args.length >= 2 ) {
					params = new Variant[3];
					params[0] = args[0];
					params[1] = str;
					params[2] = args[1];
				} else {
					params = new Variant[2];
					params[0] = args[0];
					params[1] = str;
				}
				final String split_name = "split";
				array.funcCall( 0, split_name, null, params, array );
				if( result != null ) result.set( new Variant(array, array) );
			} finally {
				array = null;
			}
			return;
		} else if((hash == mStrFuncs[StrMethod_trim].hashCode() && mStrFuncs[StrMethod_trim].equals(member))) {
			if( args.length != 0 ) throwFrom_tjs_error(Error.E_BADPARAMCOUNT,null);
			if( result == null ) return;
			result.set( s.trim() );
			return;
		} else if((hash == mStrFuncs[StrMethod_reverse].hashCode() && mStrFuncs[StrMethod_reverse].equals(member))) {
			if( args.length != 0) throwFrom_tjs_error(Error.E_BADPARAMCOUNT,null);
	 		if( result == null ) return;

	 		StringBuilder builder = new StringBuilder(s_len);
	 		for( int i = 0; i < s_len; i++ ) {
	 			builder.append( s.charAt(s_len-i-1) );
			}
	 		result.set( builder.toString() );
			return;
		} else if((hash == mStrFuncs[StrMethod_repeat].hashCode() && mStrFuncs[StrMethod_repeat].equals( member))) {
			if( args.length != 1) throwFrom_tjs_error(Error.E_BADPARAMCOUNT,null);
			if( result == null ) return;
			int count = args[0].asInteger();
			if( count <= 0 || s_len <= 0 ) {
				result.set("");
				return;
			}
			final int destLength = s_len * count;
	 		StringBuilder builder = new StringBuilder(destLength);
			while( count > 0 ) {
				builder.append( s );
				count--;
			}
			result.set( builder.toString() );
			return;
		}
		throwFrom_tjs_error(Error.E_MEMBERNOTFOUND, member);
	}

	private int callFunctionIndirect(Variant[] ra, int ra_offset, int code, Variant[] args) throws VariantException, TJSException {
		int hr;
		short[] ca = mCode;
		String name = ra[ ra_offset+ca[code+3] ].asString();
		//TJS_BEGIN_FUNC_CALL_ARGS(code + 4)
		Variant[] pass_args;
		int code_size;
		try {
			final int code_offset = code + 4;
			int pass_args_count = ca[ code_offset ];
			if( pass_args_count == -1 ) {
				/* omitting args; pass intact aguments from the caller */
				pass_args = args;
				pass_args_count = args.length;
				code_size = 1;
			} else if(pass_args_count == -2) {
				int args_v_count = 0;
				/* count total argument count */
				pass_args_count = 0;
				int arg_written_count = ca[ code_offset+1 ];
				code_size = (arg_written_count << 1) + 2;
				for( int i = 0; i < arg_written_count; i++) {
					int pos = code_offset+(i<<1)+2;
					switch( ca[ pos ] ) {
					case fatNormal:
						pass_args_count ++;
						break;
					case fatExpand:
						args_v_count +=
							ArrayClass.getArrayElementCount( ra[ ra_offset+ca[ pos+1 ] ].asObject() );
						break;
					case fatUnnamedExpand:
						pass_args_count += (args.length > mFuncDeclUnnamedArgArrayBase) ? (args.length - mFuncDeclUnnamedArgArrayBase) : 0;
						break;
					}
				}
				pass_args_count += args_v_count;
				/* allocate temporary variant array for Array object */
				Variant[] pass_args_v = new Variant[args_v_count];
				/* allocate pointer array */
				pass_args = new Variant[pass_args_count];

				/* create pointer array to pass to callee function */
				args_v_count = 0;
				pass_args_count = 0;
				for( int i = 0; i < arg_written_count; i++ ) {
					int pos = code_offset+(i<<1)+2;
					switch( ca[ pos ] ) {
					case fatNormal:
						pass_args[pass_args_count++] = ra[ ra_offset+ca[ pos+1 ] ];
						break;
					case fatExpand: {
						int count = ArrayClass.copyArrayElementTo( ra[ ra_offset+ca[ pos+1] ].asObject(), pass_args_v, args_v_count, 0, -1);
						for( int j = 0; j < count; j++ ) {
							pass_args[pass_args_count++] = pass_args_v[j + args_v_count];
						}
						args_v_count += count;
						break;
					}
					case fatUnnamedExpand: {
						int count = (args.length > mFuncDeclUnnamedArgArrayBase) ? (args.length - mFuncDeclUnnamedArgArrayBase) : 0;
						for( int j = 0; j < count; j++ ) {
							pass_args[pass_args_count++] = args[mFuncDeclUnnamedArgArrayBase + j];
						}
						break;
					}
					}
				}
				pass_args_v = null;
			} else {
				code_size = pass_args_count + 1;
				pass_args = new Variant[pass_args_count];
				for( int i = 0; i < pass_args_count; i++)
					pass_args[i] = ra[ ra_offset+ca[code_offset+1+i] ];
			}
			//TJS_BEGIN_FUNC_CALL_ARGS(code + 4)

			Variant ra_code2 = ra[ra_offset+ca[code+2]];
			final int offset = ca[code+1];
			if( ra_code2.isObject() ) {
				VariantClosure clo = ra_code2.asObjectClosure();
				hr = clo.funcCall(0, name, offset != 0 ? ra[ra_offset+offset] : null,
						pass_args, clo.mObjThis != null ? clo.mObjThis:ra[ ra_offset-1 ].asObject());
			} else if( ra_code2.isString() ) {
				processStringFunction( name, ra_code2.asString(),
					pass_args, offset != 0 ? ra[ra_offset+offset]:null);
				hr = Error.S_OK;
			} else if( ra_code2.isOctet() ) {
				processOctetFunction( name, ra_code2.asString(),
					pass_args, offset != 0 ? ra[ra_offset+offset]:null);
				hr = Error.S_OK;
			} else {
				String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(ra_code2) );
				throw new VariantException( mes );
			}
		} finally {
			pass_args = null;
		}

		if( hr < 0 ) throwFrom_tjs_error( hr, name );

		return code_size + 4;
	}
	private int callFunctionDirect(Variant[] ra, int ra_offset, int code, Variant[] args) throws TJSException, VariantException {
		int hr;
		short[] ca = mCode;
		//TJS_BEGIN_FUNC_CALL_ARGS(code + 4)
		Variant[] pass_args;
		//Variant[] pass_args_p = new Variant[PASS_ARGS_PREPARED_ARRAY_COUNT];
		//Variant[] pass_args_v = null;
		int code_size;
		try {
			final int code_offset = code + 4;
			int pass_args_count = ca[ code_offset ];
			if( pass_args_count == -1 ) {
				/* omitting args; pass intact aguments from the caller */
				pass_args = args;
				pass_args_count = args.length;
				code_size = 1;
			} else if(pass_args_count == -2) {
				int args_v_count = 0;
				/* count total argument count */
				pass_args_count = 0;
				int arg_written_count = ca[ code_offset+1 ];
				code_size = (arg_written_count << 1) + 2;
				for( int i = 0; i < arg_written_count; i++) {
					int pos = code_offset+(i<<1)+2;
					switch( ca[ pos ] ) {
					case fatNormal:
						pass_args_count ++;
						break;
					case fatExpand:
						args_v_count +=
							ArrayClass.getArrayElementCount( ra[ ra_offset+ca[ pos+1 ] ].asObject() );
						break;
					case fatUnnamedExpand:
						pass_args_count += (args.length > mFuncDeclUnnamedArgArrayBase) ? (args.length - mFuncDeclUnnamedArgArrayBase) : 0;
						break;
					}
				}
				pass_args_count += args_v_count;
				/* allocate temporary variant array for Array object */
				Variant[] pass_args_v = new Variant[args_v_count];
				/* allocate pointer array */
				pass_args = new Variant[pass_args_count];

				/* create pointer array to pass to callee function */
				args_v_count = 0;
				pass_args_count = 0;
				for( int i = 0; i < arg_written_count; i++ ) {
					int pos = code_offset+(i<<1)+2;
					switch( ca[ pos ] ) {
					case fatNormal:
						pass_args[pass_args_count++] = ra[ ra_offset+ca[ pos+1 ] ];
						break;
					case fatExpand: {
						int count = ArrayClass.copyArrayElementTo( ra[ ra_offset+ca[ pos+1] ].asObject(), pass_args_v, args_v_count, 0, -1);
						for( int j = 0; j < count; j++ ) {
							pass_args[pass_args_count++] = pass_args_v[j + args_v_count];
						}
						args_v_count += count;
						break;
					}
					case fatUnnamedExpand: {
						int count = (args.length > mFuncDeclUnnamedArgArrayBase) ? (args.length - mFuncDeclUnnamedArgArrayBase) : 0;
						for( int j = 0; j < count; j++ ) {
							pass_args[pass_args_count++] = args[mFuncDeclUnnamedArgArrayBase + j];
						}
						break;
					}
					}
				}
				pass_args_v = null;
			} else {
				code_size = pass_args_count + 1;
				pass_args = new Variant[pass_args_count];
				for( int i = 0; i < pass_args_count; i++)
					pass_args[i] = ra[ ra_offset+ca[code_offset+1+i] ];
			}
			//TJS_BEGIN_FUNC_CALL_ARGS(code + 4)

			Variant ra_code2 = ra[ra_offset+ca[code+2]];
			Variant name = mDataArray[ca[code+3]];
			final int offset = ca[code+1];
			if( ra_code2.isObject() ) {
				VariantClosure clo = ra_code2.asObjectClosure();
				String nameStr = name.getString();
				hr = clo.funcCall(0, nameStr, offset != 0 ? ra[ra_offset+offset]:null,
						pass_args, clo.mObjThis != null ?clo.mObjThis:ra[ ra_offset-1 ].asObject());
			} else if( ra_code2.isString() ) {
				processStringFunction( name.getString(), ra_code2.asString(),
					pass_args, offset != 0 ? ra[ra_offset+offset]:null);
				hr = Error.S_OK;
			} else if( ra_code2.isOctet() ) {
				processOctetFunction( name.getString(), ra_code2.asString(),
					pass_args, offset != 0 ? ra[ra_offset+offset]:null);
				hr = Error.S_OK;
			} else {
				String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(ra_code2) );
				throw new VariantException( mes );
			}
		} finally {
			pass_args = null;
		}

		if( hr < 0 ) throwFrom_tjs_error( hr, mDataArray[ca[code+3]].asString() );

		return code_size + 4;
	}
	private int callFunction(Variant[] ra, int ra_offset, int code, Variant[] args) throws TJSException, VariantException {
		// function calling / create new object
		int hr;
		short[] ca = mCode;
		//TJS_BEGIN_FUNC_CALL_ARGS(code + 3)
		Variant[] pass_args;
		int code_size;
		try {
			final int code_offset = code + 3;
			int pass_args_count = ca[ code_offset ];
			if( pass_args_count == -1 ) {
				/* omitting args; pass intact aguments from the caller */
				pass_args = args;
				pass_args_count = args.length;
				code_size = 1;
			} else if(pass_args_count == -2) {
				int args_v_count = 0;
				/* count total argument count */
				pass_args_count = 0;
				int arg_written_count = ca[ code_offset+1 ];
				code_size = (arg_written_count << 1) + 2;
				for( int i = 0; i < arg_written_count; i++) {
					int pos = code_offset+(i<<1)+2;
					switch( ca[ pos ] ) {
					case fatNormal:
						pass_args_count ++;
						break;
					case fatExpand:
						args_v_count +=
							ArrayClass.getArrayElementCount( ra[ ra_offset+ca[ pos+1 ] ].asObject() );
						break;
					case fatUnnamedExpand:
						pass_args_count += (args.length > mFuncDeclUnnamedArgArrayBase) ? (args.length - mFuncDeclUnnamedArgArrayBase) : 0;
						break;
					}
				}
				pass_args_count += args_v_count;
				/* allocate temporary variant array for Array object */
				Variant[] pass_args_v = new Variant[args_v_count];
				/* allocate pointer array */
				pass_args = new Variant[pass_args_count];

				/* create pointer array to pass to callee function */
				args_v_count = 0;
				pass_args_count = 0;
				for( int i = 0; i < arg_written_count; i++ ) {
					int pos = code_offset+(i<<1)+2;
					switch( ca[ pos ] ) {
					case fatNormal:
						pass_args[pass_args_count++] = ra[ ra_offset+ca[ pos+1 ] ];
						break;
					case fatExpand: {
						int count = ArrayClass.copyArrayElementTo( ra[ ra_offset+ca[ pos+1 ] ].asObject(), pass_args_v, args_v_count, 0, -1);
						for( int j = 0; j < count; j++ ) {
							pass_args[pass_args_count++] = pass_args_v[j + args_v_count];
						}
						args_v_count += count;
						break;
					}
					case fatUnnamedExpand: {
						int count = (args.length > mFuncDeclUnnamedArgArrayBase) ? (args.length - mFuncDeclUnnamedArgArrayBase) : 0;
						for( int j = 0; j < count; j++ ) {
							pass_args[pass_args_count++] = args[mFuncDeclUnnamedArgArrayBase + j];
						}
						break;
					}
					}
				}
				pass_args_v = null;
			} else {
				code_size = pass_args_count + 1;
				pass_args = new Variant[pass_args_count];
				for( int i = 0; i < pass_args_count; i++)
					pass_args[i] = ra[ ra_offset+ca[code_offset+1+i] ];
			}
			//TJS_BEGIN_FUNC_CALL_ARGS(code + 3)

			VariantClosure clo = ra[ ra_offset+ca[code+2] ].asObjectClosure();
			final int offset = ca[code+1];
			int op = ca[code];
			if( op == VM_CALL ) {
				hr = clo.funcCall(0, null,
						offset != 0 ? ra[ ra_offset+offset ] :null, pass_args,
						clo.mObjThis != null ?clo.mObjThis:ra[ ra_offset-1 ].asObject());
			} else {
				Holder<Dispatch2> dsp = new Holder<Dispatch2>(null);
				hr = clo.createNew(0, null, dsp, pass_args,
						clo.mObjThis != null ?clo.mObjThis:ra[ ra_offset-1 ].asObject());
				if( hr >= 0 ) {
					if( dsp.mValue != null  ) {
						if( offset != 0 ) ra[ ra_offset+offset].set(dsp.mValue, dsp.mValue);
					}
				}
			}
			// OriginalTODO: Null Check
/* TODO for debug 組み込み系の例外は主にここでとらえられる */
		} catch( TJSScriptException e ) {
			e.printStackTrace();
			throw e;
		} catch( TJSScriptError e ) {
			e.printStackTrace();
			throw e;
		} catch( TJSException e ) {
			e.printStackTrace();
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
			throw new TJSException( e.getMessage() );
/* TODO for debug */
		} finally {
			pass_args = null;
		}

		if( hr < 0 ) throwFrom_tjs_error(hr, "" );

		return code_size + 3;
	}
	private void instanceOf( final Variant name, Variant targ) throws VariantException, TJSException {
		// checks instance inheritance.
		String str = name.asString();
		if( str != null ) {
			int hr = CustomObject.defaultIsInstanceOf(0, targ, str, null);
			if( hr < 0 ) throwFrom_tjs_error(hr,null);

			targ.set( (hr == Error.S_TRUE) ? 1 : 0 );
			return;
		}
		targ.set(0);
	}
	private void eval(Variant val, Dispatch2 objthis, boolean resneed) throws VariantException, TJSException, CompileException {
		Variant res = new Variant();
		String str = val.asString();
		if( str.length() > 0 ) {
			if( resneed )
				mBlock.getTJS().evalExpression( str, res, objthis, null, 0 );
			else
				mBlock.getTJS().evalExpression( str, null, objthis, null, 0);
		}
		if(resneed) val.set( res );
	}
	private void typeOfMemberIndirect(Variant[] ra, int ra_offset, int code, int flags) throws TJSException, VariantException {
		short[] ca = mCode;
		Variant ra1 = ra[ra_offset+ca[code+1]];
		Variant ra2 = ra[ra_offset+ca[code+2]];
		Variant ra3 = ra[ra_offset+ca[code+3]];
		if( ra2.isObject() ) {
			VariantClosure clo = ra2.asObjectClosure();
			if( ra3.isInteger() != true ) {
				String str = ra3.asString();
				int hr = clo.propGet( flags, str, ra1,
					clo.mObjThis != null ?clo.mObjThis:ra[ ra_offset-1 ].asObject());
				if( hr == Error.S_OK ) {
					typeOf( ra1 );
				} else if( hr == Error.E_MEMBERNOTFOUND ) {
					// ra[ra_offset+mCode[code+1]] = new Variant("undefined");
					ra1.set("undefined");
				} else if( hr < 0 ) throwFrom_tjs_error(hr, str);
			} else {
				int hr = clo.propGetByNum(flags, ra3.asInteger(), ra1,
					clo.mObjThis != null ?clo.mObjThis:ra[ ra_offset-1 ].asObject());
				if( hr == Error.S_OK ) {
					typeOf( ra1 );
				} else if( hr == Error.E_MEMBERNOTFOUND ) {
					ra1.set("undefined");
				} else if( hr < 0 ) throwFrom_tjs_error_num( hr, ra3.asInteger() );
			}
		} else if( ra2.isString() ) {
			getStringProperty( ra1, ra2, ra3 );
			typeOf( ra1 );
		} else if( ra2.isOctet()) {
			getOctetProperty( ra1, ra2, ra3 );
			typeOf(ra1);
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(ra2) );
			throw new VariantException( mes );
		}
	}
	private void typeOfMemberDirect(Variant[] ra, int ra_offset, int code, int flags) throws TJSException, VariantException {
		short[] ca = mCode;
		Variant ra1 = ra[ra_offset+ca[code+1]];
		Variant ra2 = ra[ra_offset+ca[code+2]];
		Variant da3 = mDataArray[ca[code+3]];
		if( ra2.isObject() ) {
			int hr;
			VariantClosure clo = ra2.asObjectClosure();
			String name = da3.getString();
			hr = clo.propGet(flags, name, ra1, clo.mObjThis != null ?clo.mObjThis:ra[ ra_offset-1 ].asObject() );
			if( hr == Error.S_OK ) {
				typeOf( ra1 );
			} else if( hr == Error.E_MEMBERNOTFOUND ) {
				ra1.set("undefined");
			} else if( hr < 0 ) throwFrom_tjs_error(hr, name );
		} else if( ra2.isString() ) {
			getStringProperty( ra1, ra2, da3 );
			typeOf( ra1 );
		} else if( ra2.isOctet() ){
			getOctetProperty( ra1, ra2, da3 );
			typeOf( ra1 );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(ra2) );
			throw new VariantException( mes );
		}
	}
	private void typeOf( Variant val ) {
		// processes TJS2's typeof operator.
		String name = val.getTypeName();
		if( name != null ) {
			val.set( name );
		}
	}
	private void operateProperty0(Variant[] ra, int ra_offset, int code, int ope) throws TJSException, VariantException {
		short[] ca = mCode;
		VariantClosure clo =  ra[ ra_offset+ca[code+2] ].asObjectClosure();
		final int offset = ca[code+1];
		int hr = clo.operation(ope, null,
				offset != 0 ? ra[ ra_offset+offset ] : null, null,
				clo.mObjThis != null ?clo.mObjThis:ra[ ra_offset-1 ].asObject());
		if( hr < 0 ) throwFrom_tjs_error(hr, null);
	}
	private void operatePropertyIndirect0(Variant[] ra, int ra_offset, int code, int ope ) throws TJSException, VariantException {
		short[] ca = mCode;
		VariantClosure clo = ra[ ra_offset+ca[code+2] ].asObjectClosure();
		Variant ra_code3 = ra[ ra_offset+ca[code+3] ];
		final int offset = ca[code+1];
		if( ra_code3.isInteger() != true ) {
			String str = ra_code3.asString();
			int hr = clo.operation(ope, str, offset != 0 ? ra[ra_offset+offset]:null, null,
					clo.mObjThis != null ?clo.mObjThis:ra[ ra_offset-1 ].asObject());
			if( hr < 0 ) throwFrom_tjs_error(hr, str);
		} else {
			int hr = clo.operationByNum(ope, ra_code3.asInteger(),
					offset != 0 ? ra[ ra_offset+offset]:null, null,
					clo.mObjThis != null ?clo.mObjThis:ra[ ra_offset-1 ].asObject());
			if( hr < 0 ) throwFrom_tjs_error_num(hr, ra_code3.asInteger());
		}
	}
	private void operatePropertyDirect0(Variant[] ra, int ra_offset, int code, int ope) throws TJSException, VariantException {
		short[] ca = mCode;
		VariantClosure clo = ra[ ra_offset+ca[code+2] ].asObjectClosure();
		Variant name = mDataArray[ca[code+3]];
		String nameStr = name.getString();
		final int offset = ca[code+1];
		int hr = clo.operation( ope, nameStr, offset != 0 ? ra[ra_offset+offset]:null, null,
				ra[ ra_offset-1 ].asObject());
		if( hr < 0 ) throwFrom_tjs_error( hr, nameStr );
	}
	private void continuousClear(Variant[] ra, int ra_offset, int code) {
		short[] ca = mCode;
		int start = ra_offset+ca[code+1];
		int end = start+ca[code+2];
		while( start < end ) {
			ra[start].clear();
			start++;
		}
	}
	public String getPositionDescriptionString(int codepos) {
		return mBlock.getLineDescriptionString( codePosToSrcPos(codepos) ) + "[" + getShortDescription() + "]";
	}
	private String getShortDescription() {
		String ret = "(" + getContextTypeName() + ")";
		String name;
		if( mContextType == ContextType.PROPERTY_SETTER || mContextType == ContextType.PROPERTY_GETTER) {
			if( mParent != null )
				name = mParent.mName;
			else
				name = null;
		} else {
			name = mName;
		}
		if( name != null ) ret += " " + name;
		return ret;
	}
	public String getContextTypeName() {
		switch(mContextType)
		{
		case ContextType.TOP_LEVEL:				return "top level script";
		case ContextType.FUNCTION:				return "function";
		case ContextType.EXPR_FUNCTION:			return "function expression";
		case ContextType.PROPERTY:				return "property";
		case ContextType.PROPERTY_SETTER:		return "property setter";
		case ContextType.PROPERTY_GETTER:		return "property getter";
		case ContextType.CLASS:					return "class";
		case ContextType.SUPER_CLASS_GETTER:	return "super class getter proxy";
		default:								return "unknown";
		}
	}
	// Dispatch2 implementation
	// function invocation
	@Override
	public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() )
			return Error.E_INVALIDOBJECT;

		if( membername == null ) {
			switch( mContextType ) {
			case ContextType.TOP_LEVEL:
				executeAsFunction( objthis != null ? objthis : mBlock.getTJS().getGlobal(), null, result, 0);
				break;

			case ContextType.FUNCTION:
			case ContextType.EXPR_FUNCTION:
			case ContextType.PROPERTY_GETTER:
			case ContextType.PROPERTY_SETTER:
				executeAsFunction( objthis, param, result, 0 );
				break;

			case ContextType.CLASS: // on super class' initialization
				executeAsFunction( objthis, param, result, 0 );
				break;

			case ContextType.PROPERTY:
				return Error.E_INVALIDTYPE;
			}

			return Error.S_OK;
		}

		int hr = super.funcCall( flag, membername, result, param, objthis);

		if( membername != null && hr == Error.E_MEMBERNOTFOUND && mContextType == ContextType.CLASS && mSuperClassGetter != null ) {
			// look up super class
			int[] pointer = mSuperClassGetter.mSuperClassGetterPointer;
			final int count = pointer.length;
			if( count > 0 ) {
				Variant res = new Variant();
				for( int i = count-1; i >= 0; i-- ) {
					int v = pointer[i];
					mSuperClassGetter.executeAsFunction( null, null, res, v );
					VariantClosure clo = res.asObjectClosure();
					hr = clo.funcCall(flag, membername, result, param, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// property get
	public int propGet( int flag, final String membername, Variant result, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;

		if( membername == null ) {
			if( mContextType == ContextType.PROPERTY ) {
				// executed as a property getter
				if( mPropGetter != null )
					return mPropGetter.funcCall(0, null, result, null, objthis);
				else
					return Error.E_ACCESSDENYED;
			}
		}

		int hr = super.propGet(flag, membername, result, objthis);

		if( membername != null && hr == Error.E_MEMBERNOTFOUND && mContextType == ContextType.CLASS && mSuperClassGetter != null ) {
			// look up super class
			int[] pointer = mSuperClassGetter.mSuperClassGetterPointer;
			final int count = pointer.length;
			if( count != 0 ) {
				Variant res = new Variant();
				for( int i = count-1; i >= 0; i--) {
					mSuperClassGetter.executeAsFunction( null, null, res, pointer[i] );
					VariantClosure clo = res.asObjectClosure();
					hr = clo.propGet(flag, membername, result, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// property set
	public int propSet( int flag, String membername, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;

		if( membername == null ) {
			if( mContextType == ContextType.PROPERTY ) {
				// executed as a property setter
				if( mPropSetter != null ) {
					Variant[] params = new Variant[1];
					params[0] = param;
					return mPropSetter.funcCall(0, null, null, params, objthis );
				} else
					return Error.E_ACCESSDENYED;

					// WARNING!! const tTJSVariant ** -> tTJSVariant** force casting
			}
		}

		int hr;
		if( membername != null && mContextType == ContextType.CLASS && mSuperClassGetter != null ) {
			int pseudo_flag = (flag & Interface.IGNOREPROP) != 0 ? flag : (flag &~ Interface.MEMBERENSURE);
			// member ensuring is temporarily disabled unless Interface.IGNOREPROP
			hr = super.propSet(pseudo_flag, membername, param,objthis);
			if(hr == Error.E_MEMBERNOTFOUND) {
				int[] pointer = mSuperClassGetter.mSuperClassGetterPointer;
				final int count = pointer.length;
				if( count != 0 ) {
					Variant res = new Variant();
					for( int i = count-1; i >= 0; i--) {
						mSuperClassGetter.executeAsFunction( null, null, res, pointer[i] );
						VariantClosure clo = res.asObjectClosure();
						hr = clo.propSet(pseudo_flag, membername, param, objthis);
						if( hr != Error.E_MEMBERNOTFOUND ) break;
					}
				}
			}

			if( hr == Error.E_MEMBERNOTFOUND && (flag & Interface.MEMBERENSURE) != 0 ) {
				// re-ensure the member for "this" object
				hr = super.propSet(flag, membername, param, objthis);
			}
		} else {
			hr = super.propSet(flag, membername, param, objthis);
		}
		return hr;
	}

	// create new object
	public int createNew( int flag, String membername, Holder<Dispatch2> result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;

		if( membername == null ) {
			if( mContextType != ContextType.CLASS ) return Error.E_INVALIDTYPE;

			Dispatch2 dsp = new CustomObject();

			executeAsFunction(dsp, null, null, 0);
			funcCall(0, mName, null, param, dsp);
			result.set( dsp );
			return Error.S_OK;
		}

		int hr = super.createNew(flag, membername, result, param, objthis);

		if( membername != null && hr == Error.E_MEMBERNOTFOUND && mContextType == ContextType.CLASS && mSuperClassGetter != null ) {
			// look up super class
			int[] pointer = mSuperClassGetter.mSuperClassGetterPointer;
			final int count = pointer.length;
			if( count != 0 ) {
				Variant res = new Variant();
				for( int i = count-1; i >= 0; i--) {
					mSuperClassGetter.executeAsFunction( null, null, res, pointer[i] );
					VariantClosure clo = res.asObjectClosure();
					hr = clo.createNew(flag, membername, result, param, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// class instance matching returns false or true
	public int isInstanceOf( int flag, String membername, String classname, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity()) return Error.E_INVALIDOBJECT;

		if( membername == null ) {
			switch(mContextType)
			{
			case ContextType.TOP_LEVEL:
			case ContextType.PROPERTY_SETTER:
			case ContextType.PROPERTY_GETTER:
			case ContextType.SUPER_CLASS_GETTER:
				break;

			case ContextType.FUNCTION:
			case ContextType.EXPR_FUNCTION:
				if( "Function".equals(classname) ) return Error.S_TRUE;
				break;

			case ContextType.PROPERTY:
				if( "Property".equals(classname) ) return Error.S_TRUE;
				break;

			case ContextType.CLASS:
				if( "Class".equals(classname) ) return Error.S_TRUE;
				break;
			}
		}

		int hr = super.isInstanceOf(flag, membername, classname, objthis);

		if( membername != null && hr == Error.E_MEMBERNOTFOUND && mContextType == ContextType.CLASS && mSuperClassGetter != null ) {
			// look up super class
			int[] pointer = mSuperClassGetter.mSuperClassGetterPointer;
			final int count = pointer.length;
			if( count != 0 ) {
				Variant res = new Variant();
				for( int i = count-1; i >= 0; i--) {
					mSuperClassGetter.executeAsFunction( null, null, res, pointer[i] );
					VariantClosure clo = res.asObjectClosure();
					hr = clo.isInstanceOf(flag, membername, classname, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// get member count
	public int getCount( IntWrapper result, final String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.getCount(result, membername, objthis);

		if(membername != null && hr == Error.E_MEMBERNOTFOUND && mContextType == ContextType.CLASS && mSuperClassGetter != null )
		{
			// look up super class
			int[] pointer = mSuperClassGetter.mSuperClassGetterPointer;
			final int count = pointer.length;
			if( count != 0 ) {
				Variant res = new Variant();
				for( int i = count-1; i >= 0; i--) {
					mSuperClassGetter.executeAsFunction( null, null, res, pointer[i] );
					VariantClosure clo = res.asObjectClosure();
					hr = clo.getCount(result, membername, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// delete member
	public int deleteMember( int flag, String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.deleteMember(flag, membername, objthis);

		if(membername != null && hr == Error.E_MEMBERNOTFOUND && mContextType == ContextType.CLASS && mSuperClassGetter != null ) {
			// look up super class
			int[] pointer = mSuperClassGetter.mSuperClassGetterPointer;
			final int count = pointer.length;
			if( count != 0 ) {
				Variant res = new Variant();
				for( int i = count-1; i >= 0; i--) {
					mSuperClassGetter.executeAsFunction( null, null, res, pointer[i] );
					VariantClosure clo = res.asObjectClosure();
					hr = clo.deleteMember(flag, membername, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// invalidation
	public int invalidate( int flag, String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.invalidate(flag, membername, objthis);

		if( membername != null && hr == Error.E_MEMBERNOTFOUND && mContextType == ContextType.CLASS && mSuperClassGetter != null ) {
			// look up super class
			int[] pointer = mSuperClassGetter.mSuperClassGetterPointer;
			final int count = pointer.length;
			if( count != 0 ) {
				Variant res = new Variant();
				for( int i = count-1; i >= 0; i--) {
					mSuperClassGetter.executeAsFunction( null, null, res, pointer[i] );
					VariantClosure clo = res.asObjectClosure();
					hr = clo.invalidate(flag, membername, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// get validation, returns true or false
	public int isValid( int flag, String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.isValid(flag, membername, objthis);

		if(membername != null && hr == Error.E_MEMBERNOTFOUND && mContextType == ContextType.CLASS && mSuperClassGetter != null ) {
			// look up super class
			int[] pointer = mSuperClassGetter.mSuperClassGetterPointer;
			final int count = pointer.length;
			if( count != 0 ) {
				Variant res = new Variant();
				for( int i = count-1; i >= 0; i--) {
					mSuperClassGetter.executeAsFunction( null, null, res, pointer[i] );
					VariantClosure clo = res.asObjectClosure();
					hr = clo.isValid(flag, membername, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// operation with member
	public int operation( int flag, String membername, Variant result, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		if(membername == null) {
			if( mContextType == ContextType.PROPERTY ) {
				// operation for property object
				return super.dispatchOperation(flag, membername, result, param, objthis);
			} else {
				return super.operation(flag, membername, result, param, objthis);
			}
		}

		int hr;
		if( membername != null && mContextType == ContextType.CLASS && mSuperClassGetter != null ) {
			int pseudo_flag = (flag & Interface.IGNOREPROP) !=  0 ? flag : (flag &~ Interface.MEMBERENSURE);
			hr = super.operation(pseudo_flag, membername, result, param, objthis);
			if( hr == Error.E_MEMBERNOTFOUND ) {
				// look up super class
				int[] pointer = mSuperClassGetter.mSuperClassGetterPointer;
				final int count = pointer.length;
				if( count != 0 ) {
					Variant res = new Variant();
					for( int i = count-1; i >= 0; i--) {
						mSuperClassGetter.executeAsFunction( null, null, res, pointer[i] );
						VariantClosure clo = res.asObjectClosure();
						hr = clo.operation(pseudo_flag, membername, result, param,
						objthis);
						if( hr != Error.E_MEMBERNOTFOUND ) break;
					}
				}
			}
			if(hr == Error.E_MEMBERNOTFOUND) hr = super.operation(flag, membername, result, param, objthis);
			return hr;
		} else {
			return super.operation(flag, membername, result, param, objthis);
		}
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
	// VMCodes
	private static final int
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
		VM_DEBUGGER	= 127;
//		__VM_LAST	= 128;


	// FuncArgType
	static private final int
		fatNormal = 0,
		fatExpand = 1,
		fatUnnamedExpand = 2;

	static private final int
		OP_BAND	= 0x0001,
		OP_BOR	= 0x0002,
		OP_BXOR	= 0x0003,
		OP_SUB	= 0x0004,
		OP_ADD	= 0x0005,
		OP_MOD	= 0x0006,
		OP_DIV	= 0x0007,
		OP_IDIV	= 0x0008,
		OP_MUL	= 0x0009,
		OP_LOR	= 0x000a,
		OP_LAND	= 0x000b,
		OP_SAR	= 0x000c,
		OP_SAL	= 0x000d,
		OP_SR	= 0x000e,
		OP_INC	= 0x000f,
		OP_DEC	= 0x0010;
//		OP_MASK	= 0x001f,
//		OP_MIN	= OP_BAND,
//		OP_MAX	= OP_DEC;


	public int getCodeSize() { return mCode.length; }
	public int getDataSize() { return mDataArray.length; }

	@Override
	public int srcPosToLine(int srcpos) {
		return mBlock.srcPosToLine(srcpos);
	}

	@Override
	public String getLine(int line) {
		return mBlock.getLine(line);
	}
	public final String getName(){ return mName; }
	/**
	 * DaraArray の中の InterCodeGenerator を InterCodeObject に差し替える
	 * @param compiler
	 */
	public void dateReplace( Compiler compiler ) {
		final int count = mDataArray.length;
		Variant[] da = mDataArray;
		for( int i = 0; i < count; i++ ) {
			Variant d = da[i];
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

	@Override
	public String getScript() {
		return mBlock.getScript();
	}

	@Override
	public int getLineOffset() {
		return mBlock.getLineOffset();
	}
}
