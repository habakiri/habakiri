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

import java.util.ArrayList;

public class CustomObject extends Dispatch {
	//private static final boolean LOGD = false;

	private static final boolean AUTO_REBUILD_HASH = false;
	private static final int AUTO_REBUILD_HASH_THRESHOLD = 2;

	private static final int MAX_NATIVE_CLASS = 4;
	private static final int OBJECT_HASH__BITS_LIMITS = 32;

	private static final int NAMESPACE_DEFAULT_HASH_BITS = 3;
	private static final int SYMBOL_USING	= 0x1;
	private static final int SYMBOL_INIT	= 0x2;
	private static final int SYMBOL_HIDDEN	= 0x8;
	private static final int SYMBOL_STATIC	= 0x10;


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
		OP_DEC	= 0x0010,
		OP_MASK	= 0x001f,
		OP_MIN	= OP_BAND,
		OP_MAX	= OP_DEC;

	private static String mFinalizeName;
	private static String mMissingName;
	private static int mGlobalRebuildHashMagic = 0;
	public static void doRehash() { mGlobalRebuildHashMagic++; }

	public int mCount;
	public int mHashMask;
	public int mHashSize;
	public SymbolData[] mSymbols;
	public int mRebuildHashMagic;
	public boolean mIsInvalidated;
	public boolean mIsInvalidating;
	public NativeInstance[] mClassInstances;	//[MAX_NATIVE_CLASS];
	public int[] mClassIDs;	// [MAX_NATIVE_CLASS];

	private NativeInstance mPrimaryClassInstances;
	private int mPrimaryClassID;

	protected boolean mCallFinalize; // set false if this object does not need to call "finalize"
	protected String mfinalize_name; // name of the 'finalize' method
	protected boolean mCallMissing; // set true if this object should call 'missing' method
	protected boolean mProsessingMissing; // true if 'missing' method is being called
	protected String mmissing_name; // name of the 'missing' method
	protected ArrayList<String> mClassNames;

	public static void initialize() {
		mFinalizeName = null;
		mMissingName = null;
		mGlobalRebuildHashMagic = 0;
	}
	public static void finalizeApplication() {
		mFinalizeName = null;
		mMissingName = null;
	}
	/*
	private SymbolData mLastPropGetSymbol;
	private String mLastPropGetName;
	private SymbolData mLastPropSetSymbol;
	private String mLastPropSetName;
	*/

	protected final boolean getValidity() { return !mIsInvalidated; }
	//protected void finalize() {
		//try {
		//	super.finalize();
		//} catch (Throwable e) {
		//}
		/* destruct は実装されていないのでコールしない
		for( int i = MAX_NATIVE_CLASS-1; i>=0; i--) {
			if( mClassIDs[i] != -1 ){
				if( mClassInstances[i] != null ) mClassInstances[i].destruct();
			}
		}
		*/
		//mSymbols = null;
	//}
	protected void finalizeObject() throws VariantException, TJSException {
		// call this object's "finalize"
		if( mCallFinalize && TJS.IsTarminating == false ) {
			//funcCall( 0, mfinalize_name, mfinalize_name.hashCode(), null, 0, null, this );
			funcCall( 0, mfinalize_name, null, TJS.NULL_ARG, this );
		}

		for( int i = MAX_NATIVE_CLASS-1; i >= 0; i-- ) {
			if( mClassIDs[i] != -1 ) {
				if( mClassInstances[i] != null ) mClassInstances[i].invalidate();
			}
		}
		mPrimaryClassInstances = null;
		deleteAllMembers();
	}

	private void finalizeInternal() throws VariantException, TJSException {
		if( mIsInvalidating ) return; // to avoid re-entrance
		mIsInvalidating = true;
		try {
			if( !mIsInvalidated ) {
				finalizeObject();
				mIsInvalidated = true;
			}
		} finally {
			mIsInvalidating = false;
		}
	}

	public CustomObject() {
		this(NAMESPACE_DEFAULT_HASH_BITS);
	}
	public CustomObject( int hashbits ) {
		super();
		// デバッグ系はなし
		// if(TJSObjectHashMapEnabled()) TJSAddObjectHashRecord(this);

		mRebuildHashMagic = mGlobalRebuildHashMagic;
		if( hashbits > OBJECT_HASH__BITS_LIMITS ) hashbits = OBJECT_HASH__BITS_LIMITS;

		mHashSize = (1 << hashbits);
		mHashMask = mHashSize - 1;
		mSymbols = new SymbolData[mHashSize];
		for( int i = 0; i < mHashSize; i++ ) {
			mSymbols[i] = new SymbolData();
		}

		//mIsInvalidated = false;
		//mIsInvalidating = false;
		mCallFinalize = true;
		//mCallMissing = false;
		//mProsessingMissing = false;
		if( mFinalizeName == null ) {
			// first time; initialize 'finalize' name and 'missing' name
			mFinalizeName = TJS.mapGlobalStringMap( "finalize" );
			mMissingName  = TJS.mapGlobalStringMap( "missing" );
		}
		mfinalize_name = mFinalizeName;
		mmissing_name = mMissingName;
		mClassInstances = new NativeInstance[MAX_NATIVE_CLASS];
		mClassIDs = new int[MAX_NATIVE_CLASS];
		for( int i = 0; i < MAX_NATIVE_CLASS; i++ ) {
			mClassIDs[i] = -1;
		}
		mClassNames = new ArrayList<String>();
		//mPrimaryClassInstances;
		mPrimaryClassID = -1;
	}
	protected void beforeDestruction() throws VariantException, TJSException {
		// デバッグ系はなし
		// if(TJSObjectHashMapEnabled()) TJSSetObjectHashFlag(this, TJS_OHMF_DELETING, TJS_OHMF_SET);
		finalizeInternal();
		//super.beforeDestruction();
	}

	//private void checkObjectClosureAdd( final Variant val ) {
		// adjust the reference counter when the object closure's "objthis" is
		// referring to the object itself.
		/* 使われない
		if( val.isObject() ) {
			Dispatch2 dsp = (Dispatch2) val.asObjectClosure().mObjThis;
			// if( dsp == this ) this.Release();
		}
		*/
	//}
	//private void checkObjectClosureRemove( final Variant val ) {
		/* 使われない
		if( val.val.isObject() ) {
			Dispatch2 dsp = val.val.asObjectClosure().mObjThis;
			if( dsp == this ) this.AddRef();
		}
		*/
	//}
	private boolean callGetMissing( final String name, Variant result ) throws VariantException, TJSException {
		// call 'missing' method for PopGet
		if( mProsessingMissing ) return false;
		mProsessingMissing = true;
		boolean res = false;
		try {
			Variant val = new Variant();
			SimpleGetSetProperty prop = new SimpleGetSetProperty(val);
			try {
				Variant[] args = new Variant[3];
				args[0] = new Variant(0);		// false: get
				args[1] = new Variant(new String(name) );	// member name
				args[2] = new Variant(prop);
				//tTJSVariant *pargs[3] = {args +0, args +1, args +2};
				Variant funcresult = new Variant();
				int er = funcCall( 0, mmissing_name, funcresult, args, this );
				if( er < 0 ) {
					res = false;
				} else {
					res = funcresult.asInteger() != 0;
					result.set( val );
				}
			} finally {
				// prop.Release();
			}
		} finally {
			mProsessingMissing = false;
		}
		return res;
	}
	private boolean callSetMissing( final String name, final Variant value ) throws VariantException, TJSException {
		// call 'missing' method for PopSet
		if( mProsessingMissing ) return false;
		mProsessingMissing = true;
		boolean res = false;
		try {
			Variant val = new Variant(value);
			SimpleGetSetProperty prop = new SimpleGetSetProperty(val);
			try {
				Variant[] args = new Variant[3];
				args[0] = new Variant(1);					// true: set
				args[1] = new Variant(new String(name) );	// member name
				args[2] = new Variant(prop);
				//tTJSVariant *pargs[3] = {args +0, args +1, args +2};
				Variant funcresult = new Variant();
				int er = funcCall( 0, mmissing_name, funcresult, args, this );
				if( er < 0 ) {
					res = false;
				} else {
					res = funcresult.asInteger() != 0;
				}
			} finally {
				//prop.Release();
			}
		} finally {
			mProsessingMissing = false;
		}
		return res;
	}

	// Adds the symbol, returns the newly created data;
	// if already exists, returns the data.
	private SymbolData add( final String name ) throws TJSException {
		// add a data element named "name".
		// return existing element if the element named "name" is already alive.
		if( name == null ) return null;

		SymbolData data;
		data = find( name );
		if( data != null ) {
			// the element is already alive
			return data;
		}

		if( AUTO_REBUILD_HASH ) {
			if( mCount >= (mHashSize * AUTO_REBUILD_HASH_THRESHOLD) ) {
				rebuildHash();
			}
		}

		final int hash = name.hashCode();
		final int pos = (hash & mHashMask);
		//if( LOGD ) Logger.log("Symbol Pos:"+pos);
		SymbolData lv1 = mSymbols[pos];
		if( (lv1.mSymFlags & SYMBOL_USING) != 0 ) {
			// lv1 is using
			// make a chain and insert it after lv1
			data = new SymbolData();
			data.selfClear();
			data.mNext = lv1.mNext;
			lv1.mNext = data;
			data.setName(name, hash);
			data.mSymFlags |= SYMBOL_USING;
		} else {
			// lv1 is unused
			if( (lv1.mSymFlags & SYMBOL_INIT) == 0 ) {
				lv1.selfClear();
			}
			lv1.setName(name, hash);
			lv1.mSymFlags |= SYMBOL_USING;
			data = lv1;
		}
		mCount++;
		return data;
	}
	private SymbolData addTo( final String name, SymbolData[] newdata, int newhashmask ) throws TJSException {
		// similar to Add, except for adding member to new hash space.
		if( name == null) return null;

		// at this point, the member must not exist in destination hash space

		int hash;
		hash = name.hashCode();

		SymbolData lv1 = newdata[hash & newhashmask];
		SymbolData data;

		if( (lv1.mSymFlags & SYMBOL_USING) != 0 ) {
			// lv1 is using
			// make a chain and insert it after lv1
			data = new SymbolData();
			data.selfClear();
			data.mNext = lv1.mNext;
			lv1.mNext = data;
			data.setName(name, hash);
			data.mSymFlags |= SYMBOL_USING;
		} else {
			// lv1 is unused
			if( (lv1.mSymFlags & SYMBOL_INIT) == 0 ) {
				lv1.selfClear();
			}
			lv1.setName(name, hash);
			lv1.mSymFlags |= SYMBOL_USING;
			data = lv1;
		}

		// count is not incremented

		return data;
	}
	private void rebuildHash() throws TJSException {
		// rebuild hash table
		mRebuildHashMagic = mGlobalRebuildHashMagic;

		/*
		mLastPropGetSymbol = null;
		mLastPropGetName = null;
		mLastPropSetSymbol = null;
		mLastPropSetName = null;
		*/

		// decide new hash table size

		int r, v = mCount;
		if( (v & 0xffff0000) != 0 ) { r = 16; v >>= 16; } else r = 0;
		if( (v & 0xff00) != 0 ) { r += 8; v >>= 8; }
		if( (v & 0xf0) != 0 ) { r += 4; v >>= 4; }
		v<<=1;
		int newhashbits = r + ((0xffffaa50 >> v) &0x03) + 2;
		if(newhashbits > OBJECT_HASH__BITS_LIMITS) newhashbits = OBJECT_HASH__BITS_LIMITS;
		int newhashsize = (1 << newhashbits);


		if(newhashsize == mHashSize) return;

		int newhashmask = newhashsize - 1;
		int orgcount = mCount;

		// allocate new hash space
		SymbolData[] newsymbols = new SymbolData[newhashsize];
		for( int i = 0; i < newhashsize; i++ ) {
			newsymbols[i] = new SymbolData();
		}

		// enumerate current symbol and push to new hash space
		try {
			//memset(newsymbols, 0, sizeof(tTJSSymbolData) * newhashsize);
			//int i;
			//SymbolData lv1 = mSymbols[0];
			//SymbolData lv1lim = mSymbols[mHashSize]; // 末尾か、iterator のように処理してるんだな
			for( int i = 0; i < mHashSize; i++ ) {
			//for( ; lv1 < lv1lim; lv1++ ) {
				SymbolData lv1 = mSymbols[i];
				SymbolData d = lv1.mNext;
				while( d != null ) {
					SymbolData nextd = d.mNext;
					if( (d.mSymFlags & SYMBOL_USING) != 0 ) {
//						d->ReShare();
						SymbolData data = addTo( d.mName, newsymbols, newhashmask );
						if( data != null ) {
							//data.mValue = d.mValue;
							data.mValue.copyRef( d.mValue );
							//GetValue(data).CopyRef(*(tTJSVariant*)(&(d->Value)));
							data.mSymFlags &= ~ (SYMBOL_HIDDEN | SYMBOL_STATIC);
							data.mSymFlags |= d.mSymFlags & (SYMBOL_HIDDEN | SYMBOL_STATIC);
							//checkObjectClosureAdd( (Variant)data.mValue );
						}
					}
					d = nextd;
				}

				if( (lv1.mSymFlags & SYMBOL_USING) != 0 ) {
//					lv1->ReShare();
					SymbolData data = addTo( lv1.mName, newsymbols, newhashmask );
					if( data != null ) {
						//data.mValue = lv1.mValue;
						data.mValue.copyRef(lv1.mValue);
						//GetValue(data).CopyRef(*(tTJSVariant*)(&(lv1->Value)));
						data.mSymFlags &= ~ (SYMBOL_HIDDEN | SYMBOL_STATIC);
						data.mSymFlags |= lv1.mSymFlags & (SYMBOL_HIDDEN | SYMBOL_STATIC);
						//checkObjectClosureAdd( (Variant)data.mValue );
					}
				}
			}
		} catch( TJSException e ) {
			// recover
			int _HashMask = mHashMask;
			int _HashSize = mHashSize;
			SymbolData[] _Symbols = mSymbols;

			mSymbols = newsymbols;
			mHashSize = newhashsize;
			mHashMask = newhashmask;

			deleteAllMembers();
			mSymbols = null;

			mHashMask = _HashMask;
			mHashSize = _HashSize;
			mSymbols = _Symbols;
			mCount = orgcount;

			throw e;
		}

		// delete all current members
		deleteAllMembers();
		mSymbols = null;

		// assign new members
		mSymbols = newsymbols;
		mHashSize = newhashsize;
		mHashMask = newhashmask;
		mCount = orgcount;
	}
	private boolean deleteByName( final String name ) {
		// find an element named "name" and deletes it
		int hash = name.hashCode();
		SymbolData lv1 = mSymbols[hash & mHashMask];

		if( (lv1.mSymFlags & SYMBOL_USING) == 0 && lv1.mNext == null )
			return false; // not found

		if( (lv1.mSymFlags & SYMBOL_USING) != 0 && lv1.nameMatch(name) ) {
			// mark the element place as "unused"
			//checkObjectClosureRemove( (Variant)lv1.mValue );
			lv1.postClear();
			mCount--;
			return true;
		}

		// chain processing
		SymbolData d = lv1.mNext;
		SymbolData prevd = lv1;
		while( d != null ) {
			if((d.mSymFlags & SYMBOL_USING) != 0 && d.mHash == hash ) {
				if( d.nameMatch(name) ) {
					// sever from the chain
					prevd.mNext = d.mNext;
					//checkObjectClosureRemove( (Variant)d.mValue );
					d.destory();
					d = null;
					mCount--;
					return true;
				}
			}
			prevd = d;
			d = d.mNext;
		}

		return false;
	}
	private void deleteAllMembers() {
		// delete all members
		/* メンバ数が少ない時の効率化用にあるみたいだけど、Javaでは使わない。
		if( mCount <= 10 ) {
			deleteAllMembersInternal();
			return;
		}
		*/

		//Vector<Dispatch2> vector = new Vector<Dispatch2>();
		//try {
			SymbolData lv1;//, lv1lim;

			// list all members up that hold object
			for( int i = 0; i < mHashSize; i++ ) {
				lv1 = mSymbols[i];
				SymbolData d = lv1.mNext;
				while( d != null ) {
					SymbolData nextd = d.mNext;
					if( (d.mSymFlags & SYMBOL_USING) != 0 ) {
						Variant val = (Variant)d.mValue;
						if( val.isObject() ) {
							//checkObjectClosureRemove( val );
							//VariantClosure clo = val.asObjectClosure();
							//if( clo.mObject != null ) vector.add( clo.mObject );
							//if( clo.mObjThis != null ) vector.add( clo.mObjThis );
							val.clear();
						}
					}
					d = nextd;
				}

				if( (lv1.mSymFlags & SYMBOL_USING) != 0 ) {
					Variant val = (Variant)lv1.mValue;
					if( val.isObject() ) {
						//checkObjectClosureRemove( val );
						//VariantClosure clo = val.asObjectClosure();
						//if( clo.mObject != null ) vector.add( clo.mObject );
						//if( clo.mObjThis != null ) vector.add( clo.mObjThis );
						val.clear();
					}
				}
			}

			// delete all members
			for( int i = 0; i < mHashSize; i++ ) {
				lv1 = mSymbols[i];
				SymbolData d = lv1.mNext;
				while( d != null ) {
					SymbolData nextd = d.mNext;
					if( (d.mSymFlags & SYMBOL_USING) != 0 ) {
						d.destory();
					}
					d = null;
					d = nextd;
				}
				if( (lv1.mSymFlags & SYMBOL_USING) != 0 ) {
					lv1.postClear();
				}
				lv1.mNext = null;
			}
			mCount = 0;
		//} finally {}

		// release all objects
		/*
		std::vector<iTJSDispatch2*>::iterator i;
		for(i = vector.begin(); i != vector.end(); i++)
		{
			(*i)->Release();
		}
		*/
	}

	private SymbolData find(String name ) {
		// searche an element named "name" and return its "SymbolData".
		// return NULL if the element is not found.
		if( name == null ) return null;

		final int hash = name.hashCode();
		final int pos = (hash & mHashMask);
		// if( LOGD ) Logger.log("Symbol Pos:"+pos);
		SymbolData lv1 = mSymbols[pos];

		if( (lv1.mSymFlags & SYMBOL_USING) == 0 && lv1.mNext == null )
			return null; // lv1 is unused and does not have any chains

		// search over the chain
		int cnt = 0;
		SymbolData prevd = lv1;
		SymbolData d = lv1.mNext;
		for(; d != null; prevd = d, d=d.mNext, cnt++) {
			if( d.mHash == hash && (d.mSymFlags & SYMBOL_USING) != 0 ) {
				if( d.nameMatch(name) ) {
					if( cnt > 2 ) {
						// move to first
						prevd.mNext = d.mNext;
						d.mNext = lv1.mNext;
						lv1.mNext = d;
					}
					return d;
				}
			}
		}

		if( lv1.mHash == hash && (lv1.mSymFlags & SYMBOL_USING) != 0 ) {
			if( lv1.nameMatch(name) ) {
				return lv1;
			}
		}

		return null;
	}

	private static boolean enumCallback( int flags, EnumMembersCallback callback, Variant value, Dispatch2 objthis, final SymbolData data ) throws TJSException {
		int newflags = 0;
		if( (data.mSymFlags & SYMBOL_HIDDEN) != 0 ) newflags |= Interface.HIDDENMEMBER;
		if( (data.mSymFlags & SYMBOL_STATIC) != 0 ) newflags |= Interface.STATICMEMBER;

		value.clear();
		if( (flags & Interface.ENUM_NO_VALUE) == 0 ) {
			boolean getvalues = false;
			if( (flags & Interface.IGNOREPROP) == 0 ) {
				Variant targ = data.mValue;
				if( targ.isObject() ) {
					VariantClosure tvclosure = targ.asObjectClosure();
					int hr = Error.E_NOTIMPL;
					if( tvclosure.mObject != null ) {
						Dispatch2 disp = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
						hr = tvclosure.mObject.propGet(0, null, value, disp );
					}
					if( hr >= 0 ) getvalues = true;
					else if( hr != Error.E_NOTIMPL && hr != Error.E_INVALIDTYPE && hr != Error.E_INVALIDOBJECT ) return false;
				}
			}
			if( getvalues == false ) value.copyRef( data.mValue );
		}
		return callback.callback( data.mName, newflags, value );
	}
	public int enumMembers( int flags, EnumMembersCallback callback, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;
		if( callback == null ) return Error.S_OK;

		Variant value = new Variant();
		for( int i = 0; i < mHashSize; i++ ) {
			final SymbolData lv1 = mSymbols[i];
			SymbolData d = lv1.mNext;
			while( d != null ) {
				final SymbolData nextd = d.mNext;
				if( (d.mSymFlags & SYMBOL_USING) != 0 ) {
					if( enumCallback( flags, callback, value, objthis, d) == false) return Error.S_OK;
				}
				d = nextd;
			}
			if( (lv1.mSymFlags & SYMBOL_USING) != 0 ) {
				if( enumCallback(flags, callback, value, objthis, lv1) == false ) return Error.S_OK;
			}
		}

		return Error.S_OK;
	}
	/*
	private static boolean callEnumCallbackForData( int flags, Variant[] params, VariantClosure callback, Dispatch2 objthis, final SymbolData data) throws VariantException, TJSException {
		int newflags = 0;
		if( (data.mSymFlags & SYMBOL_HIDDEN) != 0 ) newflags |= Interface.HIDDENMEMBER;
		if( (data.mSymFlags & SYMBOL_STATIC) != 0 ) newflags |= Interface.STATICMEMBER;
		params[0].set( data.mName );
		params[1].set( newflags );

		if( (flags & Interface.ENUM_NO_VALUE) == 0 ) {
			// get value
			int ret = defaultPropGet( flags, (Variant)data.mValue, params[2], objthis );
			if( ret < 0 ) return false;
		}

		Variant res = new Variant();
		int ret;
		if( (flags & Interface.ENUM_NO_VALUE) != 0 ) {
			Variant[] args = new Variant[2];
			args[0] = params[0];
			args[1] = params[1];
			ret = callback.funcCall( 0, null, res, args, null );
		} else {
			ret = callback.funcCall( 0, null, res, params, null );
		}
		if( ret < 0 ) return false;
		return res.asInteger() != 0;
	}
	*/
	protected static int defaultPropGet( int flag, Variant targ, Variant result, Dispatch2 objthis ) throws VariantException, TJSException {
		if( (flag & Interface.IGNOREPROP) == 0 ) {
			// if IGNOREPROP is not specified

			// if member's type is tvtObject, call the object's PropGet with "member=NULL"
			//  ( default member invocation ). if it is succeeded, return its return value.
			// if the PropGet's return value is TJS_E_ACCESSDENYED,
			// return as an error, otherwise return the member itself.
			if( targ.isObject() ) {
				VariantClosure tvclosure = targ.asObjectClosure();
				int hr = Error.E_NOTIMPL;
				if( tvclosure.mObject != null ) {
					Dispatch2 disp = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
					hr = tvclosure.mObject.propGet(0, null, result, disp );
				}
				if( hr >= 0 ) return hr;
				if( hr != Error.E_NOTIMPL && hr != Error.E_INVALIDTYPE && hr != Error.E_INVALIDOBJECT ) return hr;
			}
		}

		// return the member itself
		if( result == null ) return Error.E_INVALIDPARAM;

		result.copyRef( targ );
		return Error.S_OK;
	}


	/**
	 * new する時のメンバコピー
	 * @param dest コピー先
	 * @return エラーコード
	 * @throws TJSException
	 */
	protected int copyAllMembers( CustomObject dest ) throws TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;
		if( !dest.getValidity() ) return Error.E_INVALIDOBJECT;

		Variant result = new Variant();
		for( int i = 0; i < mHashSize; i++ ) {
			final SymbolData lv1 = mSymbols[i];
			SymbolData d = lv1.mNext;
			while( d != null ) {
				final SymbolData nextd = d.mNext;
				if( ((d.mSymFlags & SYMBOL_USING) != 0) && ((d.mSymFlags & SYMBOL_STATIC) == 0) ) {
					//if( d.mValue.isObject() )
					{
						result.set(d.mValue);
						//if( result.asObjectThis() == null ) result.changeClosureObjThis(dest);
						if( result.isObject() ) result.changeClosureObjThis(dest);
						SymbolData data = dest.add(d.mName);
						if( (d.mSymFlags & SYMBOL_HIDDEN) != 0 )
							data.mSymFlags |= SYMBOL_HIDDEN;
						else
							data.mSymFlags &= ~SYMBOL_HIDDEN;
						data.mValue.copyRef(result);
					}
				}
				d = nextd;
			}

			if( ((lv1.mSymFlags & SYMBOL_USING) != 0) && ((lv1.mSymFlags & SYMBOL_STATIC) == 0) ) {
				//if( lv1.mValue.isObject() )
				{
					result.set(lv1.mValue);
					//if( result.asObjectThis() == null ) result.changeClosureObjThis(dest);
					if( result.isObject() ) result.changeClosureObjThis(dest);
					SymbolData data = dest.add(lv1.mName);
					if( (lv1.mSymFlags & SYMBOL_HIDDEN) != 0 )
						data.mSymFlags |= SYMBOL_HIDDEN;
					else
						data.mSymFlags &= ~SYMBOL_HIDDEN;
					data.mValue.copyRef(result);
				}
			}
		}

		return Error.S_OK;
	}

	/*
	private void internalEnumMembers( int flags, VariantClosure callback, Dispatch2 objthis ) throws VariantException, TJSException {
		// enumlate members by calling callback.
		// note that member changes(delete or insert) through this function is not guaranteed.
		if( callback == null ) return;

		Variant name = new Variant();
		Variant newflags = new Variant();
		Variant value = new Variant();
		Variant[] params = new Variant[3];
		params[0] = name;
		params[1] = newflags;
		params[2] = value;

		//const tTJSSymbolData * lv1 = Symbols;
		//const tTJSSymbolData * lv1lim = lv1 + HashSize;
		//for(; lv1 < lv1lim; lv1++)
		for( int i = 0; i < mHashSize; i++ ) {
			final SymbolData lv1 = mSymbols[i];
			SymbolData d = lv1.mNext;
			while( d != null ) {
				final SymbolData nextd = d.mNext;

				if( (d.mSymFlags & SYMBOL_USING) != 0 ) {
					if( callEnumCallbackForData(flags, params, callback, objthis, d) == false) return;
				}
				d = nextd;
			}

			if( (lv1.mSymFlags & SYMBOL_USING) != 0 ) {
				if( callEnumCallbackForData(flags, params, callback, objthis, lv1) == false ) return;
			}
		}
	}
	*/
	public void clear() { deleteAllMembers(); }

	// service function for lexical analyzer
	public int getValueInteger( final String name ) throws VariantException {
		SymbolData data = find( name );
		if( data == null ) return -1;
		Variant val = (Variant)data.mValue;
		return val.asInteger(); // オリジナルでは、強制的に int で返す(アドレスになるかもしれない)もののようだが……
	}
	private static int tryFuncCallViaPropGet( VariantClosure tvclosure, int flag, Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
		// retry using PropGet
		Variant tmp = new Variant();
		Dispatch2 disp = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
		int er = tvclosure.mObject.propGet( 0, null, tmp, disp );
		if( er >= 0 ) {
			tvclosure = tmp.asObjectClosure();
			disp = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
			er = tvclosure.mObject.funcCall( flag, null, result, param, disp );
		}
		return er;
	}
	protected static int defaultFuncCall( int flag, Variant targ, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( targ.isObject() ) {
			int er = Error.E_INVALIDTYPE;
			VariantClosure tvclosure = targ.asObjectClosure();
			if( tvclosure.mObject != null ) {
				// bypass
				Dispatch2 disp = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
				er = tvclosure.mObject.funcCall(flag, null, result, param, disp );
				if(er == Error.E_INVALIDTYPE ) {
					// retry using PropGet
					er = tryFuncCallViaPropGet( tvclosure, flag, result, param, objthis );
				}
			}
			return er;
		}
		return Error.E_INVALIDTYPE;
	}
	public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity())
			return Error.E_INVALIDOBJECT;

		if( membername == null ) {
			// this function is called as to call a default method,
			// but this object is not a function.
			return Error.E_INVALIDTYPE; // so returns TJS_E_INVALIDTYPE
		}

		SymbolData data =  find(membername );
		if(data == null ) {
			if(mCallMissing) {
				// call 'missing' method
				Variant value_func = new Variant();
				if( callGetMissing(membername, value_func))
					return defaultFuncCall(flag, value_func, result, param, objthis);
			}

			return Error.E_MEMBERNOTFOUND; // member not found
		}

		return defaultFuncCall(flag, data.mValue, result, param, objthis);
	}
	public int propGet( int flag, final String membername, Variant result, Dispatch2 objthis) throws VariantException, TJSException {
		if( mRebuildHashMagic != mGlobalRebuildHashMagic ) {
			rebuildHash();
		}

		if( !getValidity() )
			return Error.E_INVALIDOBJECT;

		if( membername == null ) {
			// this object itself has no information on PropGet with membername == NULL
			return Error.E_INVALIDTYPE;
		}

		SymbolData data;
		data = find(membername);
		if( data == null ) {
			if( mCallMissing ) {
				// call 'missing' method
				Variant value = new Variant();
				if( callGetMissing(membername, value) )
					return defaultPropGet(flag, value, result, objthis);
			}
		}

		if( data == null && (flag & Interface.MEMBERENSURE) != 0 ) {
			// create a member when TJS_MEMBERENSURE is specified
			data = add(membername);
		}

		if( data == null ) return Error.E_MEMBERNOTFOUND; // not found

		//mLastPropGetSymbol = data;
		//mLastPropGetName = membername;
		return defaultPropGet(flag, data.mValue, result, objthis);
	}
	static protected int defaultPropSet( int flag, Variant targ, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( (flag & Interface.IGNOREPROP) == 0) {
			if( targ.isObject() ) {
				// roughly the same as TJSDefaultPropGet
				VariantClosure tvclosure = targ.asObjectClosure();
				int hr = Error.E_NOTIMPL;
				if( tvclosure.mObject != null ) {
					Dispatch2 disp = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
					hr = tvclosure.mObject.propSet(0, null, param, disp );
				}
				if( hr >= 0 ) return hr;
				if( hr != Error.E_NOTIMPL && hr != Error.E_INVALIDTYPE && hr != Error.E_INVALIDOBJECT) return hr;
			}
		}

		// normal substitution
		if( param == null ) return Error.E_INVALIDPARAM;

		targ.copyRef( param );
		return Error.S_OK;
	}
	public int propSet( int flag, final String membername, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;

		if( membername == null ) {
			// no action is defined with the default member
			return Error.E_INVALIDTYPE;
		}

		SymbolData data = null;
		if( mCallMissing ) {
			data = find(membername);
			if( data == null ) {
				// call 'missing' method
				if( callSetMissing(membername, param) ) return Error.S_OK;
			}
		}

		if( (flag & Interface.MEMBERENSURE) != 0 )
			data = add(membername); // create a member when MEMBERENSURE is specified
		else
			data = find(membername);

		if( data == null ) return Error.E_MEMBERNOTFOUND; // not found

		if( (flag & Interface.HIDDENMEMBER) != 0 )
			data.mSymFlags |= SYMBOL_HIDDEN;
		else
			data.mSymFlags &= ~SYMBOL_HIDDEN;

		if( (flag & Interface.STATICMEMBER) != 0 )
			data.mSymFlags |= SYMBOL_STATIC;
		else
			data.mSymFlags &= ~SYMBOL_STATIC;

		//-- below is mainly the same as defaultPropSet

		if( (flag & Interface.IGNOREPROP) == 0 ) {
			if( data.mValue.isObject() ) {
				VariantClosure tvclosure = data.mValue.asObjectClosure();
				if( tvclosure.mObject != null ) {
					Dispatch2 disp = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
					int hr = tvclosure.mObject.propSet(0, null, param, disp );
					if( hr >= 0 ) return hr;
					if(hr != Error.E_NOTIMPL && hr != Error.E_INVALIDTYPE && hr != Error.E_INVALIDOBJECT) return hr;
				}
				data = find(membername);
			}
		}

		if( param == null ) return Error.E_INVALIDPARAM;

		//mLastPropSetName = membername;
		//mLastPropSetSymbol = data;
		data.mValue.copyRef(param);
		return Error.S_OK;
	}
	public int getCount( IntWrapper result, final String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;
		if( result == null ) return Error.E_INVALIDPARAM;
		result.value = mCount;
		return Error.S_OK;
	}
	/*
	public int enumMembers( int flag, VariantClosure callback, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;
		internalEnumMembers(flag, callback, objthis);
		return Error.S_OK;
	}
	*/
	public int deleteMember( int flag, final String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;
		if( membername == null ) return Error.E_MEMBERNOTFOUND;
		if( !deleteByName(membername) ) return Error.E_MEMBERNOTFOUND;
		return Error.S_OK;
	}
	protected static int defaultInvalidate( int flag, Variant targ, Dispatch2 objthis ) throws VariantException, TJSException {
		if( targ.isObject() ) {
			VariantClosure tvclosure = targ.asObjectClosure();
			if( tvclosure.mObject != null ) {
				// bypass
				Dispatch2 disp = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
				return tvclosure.mObject.invalidate( flag, null, disp );
			}
		}
		return Error.S_FALSE;
	}
	public int invalidate( int flag, final String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;

		if( membername == null ) {
			if( mIsInvalidated ) return Error.S_FALSE;
			finalizeInternal();
			return Error.S_TRUE;
		}

		SymbolData data = find(membername);
		if( data == null ) {
			if( mCallMissing ) {
				// call 'missing' method
				Variant value = new Variant();
				if( callGetMissing(membername, value) )
					return defaultInvalidate(flag, value, objthis);
			}
		}

		if( data == null ) return Error.E_MEMBERNOTFOUND; // not found

		return defaultInvalidate( flag, data.mValue, objthis);
	}
	protected static int defaultIsValid( int flag, Variant targ, Dispatch2 objthis ) throws VariantException, TJSException {
		if( targ.isObject() ) {
			VariantClosure tvclosure = targ.asObjectClosure();
			if( tvclosure.mObject != null ) {
				// bypass
				Dispatch2 disp = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
				return tvclosure.mObject.isValid( flag, null, disp );
			}
		}
		// the target type is not tvtObject
		return Error.S_TRUE;
	}
	public int isValid( int flag, final String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername == null ) {
			if( mIsInvalidated ) return Error.S_FALSE;
			return Error.S_TRUE;
		}

		SymbolData data = find(membername);
		if( data == null ) {
			if( mCallMissing ) {
				// call 'missing' method
				Variant value = new Variant();
				if( callGetMissing(membername, value) )
					return defaultIsValid(flag, value, objthis);
			}
		}
		if( data == null ) return Error.E_MEMBERNOTFOUND; // not found
		return defaultIsValid(flag, data.mValue, objthis);
	}
	protected static int defaultCreateNew( int flag, Variant targ, Holder<Dispatch2> result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( targ.isObject() ) {
			VariantClosure tvclosure = targ.asObjectClosure();
			if( tvclosure.mObject != null ) {
				// bypass
				Dispatch2 disp = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
				return tvclosure.mObject.createNew(flag, null, result, param, disp );
			}
		}
		return Error.E_INVALIDTYPE;
	}
	public int createNew( int flag, final String membername, Holder<Dispatch2> result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;
		if( membername == null ) {
			// as an action of the default member, this object cannot create an object
			// because this object is not a class
			return Error.E_INVALIDTYPE;
		}
		SymbolData data = find(membername);
		if( data == null ) {
			if( mCallMissing ) {
				// call 'missing' method
				Variant value = new Variant();
				if( callGetMissing(membername, value) )
					return defaultCreateNew(flag, value, result, param, objthis);
			}
		}
		if( data == null ) return Error.E_MEMBERNOTFOUND; // not found
		return defaultCreateNew( flag, data.mValue, result, param, objthis);
	}
	public static int defaultIsInstanceOf( int flag, Variant targ, final String name, Dispatch2 objthis ) throws VariantException, TJSException {
		if( targ.isVoid() ) return Error.S_FALSE;

		if( "Object".equals(name) ) return Error.S_TRUE;

		if( targ.isNumber() ) {
			if( "Number".equals(name) ) return Error.S_TRUE;
			else return Error.S_FALSE;
		} else if( targ.isString() ) {
			if( "String".equals(name) ) return Error.S_TRUE;
			else return Error.S_FALSE;
		} else if( targ.isOctet() ) {
			if( "Octet".equals(name) ) return Error.S_TRUE;
			else return Error.S_FALSE;
		} else if( targ.isObject() ) {
			VariantClosure tvclosure = targ.asObjectClosure();
			if( tvclosure.mObject != null ) {
				// bypass
				Dispatch2 disp = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
				return tvclosure.mObject.isInstanceOf( flag, null, name, disp );
			}
			return Error.S_FALSE;
		}
		return Error.S_FALSE;
	}
	public int isInstanceOf( int flag, final String membername, final String classname, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;

		if( membername == null ) {
			// always returns true if "Object" is specified
			if( "Object".equals(classname) ) return Error.S_TRUE;

			// look for the class instance information
			int count = mClassNames.size();
			for( int i = 0; i < count; i++ ) {
				if( mClassNames.get(i).equals(classname) ) return Error.S_TRUE;
			}
			return Error.S_FALSE;
		}
		SymbolData data = find(membername);
		if( data == null ) {
			if( mCallMissing ) {
				// call 'missing' method
				Variant value = new Variant();
				if( callGetMissing(membername, value) )
					return defaultIsInstanceOf( flag, value, classname, objthis );
			}
		}
		if( data== null ) return Error.E_MEMBERNOTFOUND; // not found
		return defaultIsInstanceOf(flag, data.mValue, classname, objthis);
	}
	protected static int defaultOperation( int flag, Variant targ, Variant result, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		int op = flag & OP_MASK;

		if( op != OP_INC && op != OP_DEC && param == null ) return Error.E_INVALIDPARAM;

		if( op < OP_MIN || op > OP_MAX) return Error.E_INVALIDPARAM;

		if( targ.isObject() ) {
			// the member may be a property handler if the member's type is "tvtObject"
			// so here try to access the object.
			int hr;
			VariantClosure tvclosure = targ.asObjectClosure();
			if( tvclosure.mObject != null ) {
				Dispatch2 ot = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;
				Variant tmp = new Variant();
				hr = tvclosure.mObject.propGet( 0, null, tmp, ot );
				if( hr >= 0 ) {
					doVariantOperation( op, tmp, param );

					hr = tvclosure.mObject.propSet( 0, null, tmp, ot );
					if( hr < 0 ) return hr;
					if( result != null ) result.copyRef(tmp);
					return Error.S_OK;
				} else if( hr != Error.E_NOTIMPL && hr != Error.E_INVALIDTYPE && hr != Error.E_INVALIDOBJECT) {
					return hr;
				}
				// normal operation is proceeded if "PropGet" is failed.
			}
		}
		doVariantOperation( op, targ, param );
		if( result != null ) result.copyRef(targ);
		return Error.S_OK;
	}
	public int operation( int flag, final String membername, Variant result, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;

		// operation about the member
		// processing line is the same as above function
		if( membername == null ) {
			return Error.E_INVALIDTYPE;
		}
		int op = flag & OP_MASK;
		if( op != OP_INC && op != OP_DEC && param == null ) return Error.E_INVALIDPARAM;
		if( op < OP_MIN || op > OP_MAX) return Error.E_INVALIDPARAM;
		SymbolData data = find(membername);
		if( data == null ) {
			if( mCallMissing ) {
				// call default operation
				return super.operation(flag, membername, result, param, objthis);
			}
		}
		if( data == null ) return Error.E_MEMBERNOTFOUND; // not found

		if( data.mValue.isObject() ) {
			int hr;
			VariantClosure tvclosure;
			tvclosure = data.mValue.asObjectClosure();
			if( tvclosure.mObject != null ) {
				Dispatch2 ot = tvclosure.mObjThis != null ? tvclosure.mObjThis : objthis;;
				Variant tmp = new Variant();
				hr = tvclosure.mObject.propGet(0, null, tmp, ot);
				if( hr>= 0 ) {
					doVariantOperation(op, tmp, param);
					hr = tvclosure.mObject.propSet(0, null, tmp, ot);
					if( hr < 0 ) return hr;
					if( result != null ) result.copyRef(tmp);
					return Error.S_OK;
				} else if( hr != Error.E_NOTIMPL && hr != Error.E_INVALIDTYPE && hr != Error.E_INVALIDOBJECT ) {
					return hr;
				}
			}
		}

		//checkObjectClosureRemove( data.mValue );

		Variant tmp = data.mValue ;
		try {
			doVariantOperation(op, tmp, param);
		} finally {
			//checkObjectClosureAdd( data.mValue );
		}
		if( result != null ) result.copyRef(tmp);
		return Error.S_OK;
	}
	// Dispatch クラスのメソッドを呼び出すため
	public int dispatchOperation( int flag, final String membername, Variant result, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		return super.operation( flag, membername, result, param, objthis );
	}
	public int nativeInstanceSupport( int flag, int classid, Holder<NativeInstance> pointer ) {
		if( flag == Interface.NIS_GETINSTANCE ) {
			// search "classid"
			for( int i = 0; i < MAX_NATIVE_CLASS; i++ ) {
				if( mClassIDs[i] == classid ) {
					pointer.mValue = mClassInstances[i];
					return Error.S_OK;
				}
			}
			return Error.E_FAIL;
		} else if( flag == Interface.NIS_REGISTER ) {
			// search for the empty place
			if( mPrimaryClassID == -1 ) {
				mPrimaryClassID = classid;
				mPrimaryClassInstances = pointer.mValue;
			}
			for( int i = 0; i < MAX_NATIVE_CLASS; i++ ) {
				if( mClassIDs[i] == -1 ) {
					// found... writes there
					mClassIDs[i] = classid;
					mClassInstances[i] = pointer.mValue;
					return Error.S_OK;
				}
			}
			return Error.E_FAIL;
		}
		return Error.E_NOTIMPL;
	}
	public int addClassInstanveInfo( final String name ) {
		mClassNames.add(name);
		return Error.S_OK;
	}
	public int classInstanceInfo( int flag, int num, Variant value ) throws VariantException {
		switch( flag ) {
		case Interface.CII_ADD:
		{
			// add value
			String name = value.asString();
			// デバッグ系はなし
			//if( objectHashMapEnabled() && mClassNames.size() == 0)
			//	objectHashSetType( this, "instance of class " + name );
					// First class name is used for the object classname
					// because the order of the class name
					// registration is from descendant to ancestor.
			mClassNames.add(name);
			return Error.S_OK;
		}

		case Interface.CII_GET:
		{
			// get value
			if( num >= mClassNames.size() ) return Error.E_FAIL;
			value.set( mClassNames.get(num) );
			return Error.S_OK;
		}

		case Interface.CII_SET_FINALIZE:
		{
			// set 'finalize' method name
			mfinalize_name = value.asString();
			mCallFinalize = mfinalize_name.length() > 0;
			return Error.S_OK;
		}

		case Interface.CII_SET_MISSING:
		{
			// set 'missing' method name
			mmissing_name = value.asString();
			mCallMissing = mmissing_name.length() > 0;
			return Error.S_OK;
		}
		}
		return Error.E_NOTIMPL;
	}
	// special funcsion
	@Override
	public NativeInstance getNativeInstance( int classid ) {
		if( mPrimaryClassID == classid ) {
			return mPrimaryClassInstances;
		} else {
			for( int i = 0; i < MAX_NATIVE_CLASS; i++ ) {
				if( mClassIDs[i] == classid ) {
					return mClassInstances[i];
				}
			}
			return null;
		}
	}
	@Override
	public int setNativeInstance( int classid, NativeInstance ni ) {
		// search for the empty place
		if( mPrimaryClassID == -1 ) {
			mPrimaryClassID = classid;
			mPrimaryClassInstances = ni;
		}
		for( int i = 0; i < MAX_NATIVE_CLASS; i++ ) {
			if( mClassIDs[i] == -1 ) {
				// found... writes there
				mClassIDs[i] = classid;
				mClassInstances[i] = ni;
				return Error.S_OK;
			}
		}
		return Error.E_FAIL;
	}
	public final String getClassNames() {
		if( mClassNames != null && mClassNames.size() > 0 ) {
			StringBuilder builder = new StringBuilder(512);
			final int count = mClassNames.size();
			for( int i = 0; i < count; i++ ) {
				if( i != 0 ) builder.append( ',' );
				builder.append( mClassNames.get(i) );
			}
			return builder.toString();
		} else {
			return null;
		}
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(1024);
		for( int i = 0; i < mHashSize; i++ ) {
			final SymbolData lv1 = mSymbols[i];
			SymbolData d = lv1.mNext;
			while( d != null ) {
				final SymbolData nextd = d.mNext;

				if( (d.mSymFlags & SYMBOL_USING) != 0 ) {
					builder.append( d.mName );
					builder.append( " : " );
					builder.append( d.mValue.toString() );
					builder.append( ", " );
				}
				d = nextd;
			}
			if( (lv1.mSymFlags & SYMBOL_USING) != 0 ) {
				builder.append( lv1.mName );
				builder.append( " : " );
				builder.append( lv1.mValue.toString() );
				builder.append( ", " );
			}
		}
		if( builder.length() == 0 ) return "empty";
		return builder.toString();
	}


	/**
	 * 最初に一気に定数値を登録する
	 * @param membername
	 * @param param
	 * @param objthis
	 * @return
	 * @throws VariantException
	 * @throws TJSException
	 */
	public int propSetConstArray( final String[] membername, final int[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;

		if( membername.length != param.length ) {
			throw new TJSException( Error.InternalError );
		}
		final int count = membername.length;
		for( int i = 0; i < count; i++ ) {
			if( membername[i] == null )
				throw new TJSException( Error.InternalError );
			String name = TJS.mapGlobalStringMap(membername[i]);

			SymbolData data = add(name);
			data.mSymFlags &= ~SYMBOL_HIDDEN;
			data.mSymFlags &= ~SYMBOL_STATIC;

			if( data.mValue.isObject() ) {
				throw new TJSException( Error.InternalError );
			}
			data.mValue.set(param[i]);
		}
		return Error.S_OK;
	}
}

