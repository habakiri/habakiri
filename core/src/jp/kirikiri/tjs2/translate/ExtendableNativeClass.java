package jp.kirikiri.tjs2.translate;


import jp.kirikiri.tjs2.CustomObject;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Holder;
import jp.kirikiri.tjs2.IntWrapper;
import jp.kirikiri.tjs2.InterCodeObject;
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.NativeClass;
import jp.kirikiri.tjs2.NativeInstance;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.base.ScriptsClass;
import jp.kirikiri.tvp2.msg.Message;

/**
 * ネイティブで、他のクラスを継承可能なクラス定義用クラス
 *
 */
public class ExtendableNativeClass extends NativeClass {
	protected Dispatch2[] mSuperClasses;

	/**
	 * global を得る
	 * @return global
	 */
	public static Dispatch2 getGlobal() {
		return ScriptsClass.getGlobal();
	}

	public ExtendableNativeClass(String name) {
		super(name);
	}
	protected void extendsClass( final String classname ) throws TJSException {
		Dispatch2 global = ScriptsClass.getGlobal();

		Variant result = new Variant();
		int hr = global.propGet( Interface.MEMBERMUSTEXIST, classname, result, global );
		if( hr < 0 ) Error.throwFrom_tjs_error( hr, classname );

		if( mSuperClasses == null ) {
			mSuperClasses = new Dispatch2[1];
		} else {
			final int count = mSuperClasses.length;
			Dispatch2[] newsuper = new Dispatch2[count+1];
			for( int i = 0; i < count; i++ ) {
				newsuper[i+1] = mSuperClasses[i];
			}
			mSuperClasses = null;
			mSuperClasses = newsuper;
		}
		mSuperClasses[0] = result.asObject();
	}

	public Dispatch2 getSuper() throws TJSException {
		final int count = mSuperClasses.length;
		if( count == 0 ) return null;
		if( count != 1 ) Message.throwExceptionMessage(Message.InternalError);
		return mSuperClasses[0];
	}
	/**
	 * dest へクラスのメンバ変数を登録する
	 * @param dest
	 * @return
	 * @throws TJSException
	 * @throws VariantException
	 */
	public int registerVariable( Dispatch2 dest ) throws VariantException, TJSException {
		return Error.S_OK;
	}
	public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername != null ) {
			int hr = super.funcCall(flag, membername, result, param, objthis);
			if( hr == Error.E_MEMBERNOTFOUND && mSuperClasses != null && mSuperClasses.length > 0 ) {
				final int count = mSuperClasses.length;
				for( int i = 0; i < count; i++ ) {
					hr = mSuperClasses[i].funcCall(flag, membername, result, param, objthis);
					if( hr == Error.S_OK ) break;
					if( hr != Error.E_MEMBERNOTFOUND && hr < 0 ) return hr;
				}
			}
			return hr;
		}

		// 生成を高速化するためにメンバコピーを特別処理する形で実装
		objthis.addClassInstanveInfo( mClassName );
		//NativeInstance nativeptr = createNativeInstance();
		//objthis.setNativeInstance( mClassIDInternal, nativeptr );

		int hr;
		if( mSuperClasses != null && mSuperClasses.length > 0 ) {
			final int count = mSuperClasses.length;
			for( int i = 0; i < count; i++ ) {
				hr = mSuperClasses[i].funcCall( flag, null, null, TJS.NULL_ARG, objthis );
				if( hr < 0 ) return hr;
			}
		}

		hr = copyAllMembers( (CustomObject)objthis );
		if( hr < 0 ) return hr;
		hr = registerVariable( objthis );
		if( hr < 0 ) return hr;
		return Error.S_OK;
	}

	// create new object
	@Override
	public int createNew( int flag, String membername, Holder<Dispatch2> result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr;
		CustomObject dsp = (CustomObject)createBaseTJSObject();
		dsp.addClassInstanveInfo( mClassName );
		//NativeInstance nativeptr = createNativeInstance();
		//dsp.setNativeInstance( mClassIDInternal, nativeptr );

		if( mSuperClasses != null && mSuperClasses.length > 0 ) {
			final int count = mSuperClasses.length;
			for( int i = 0; i < count; i++ ) {
				hr = mSuperClasses[i].funcCall( flag, null, null, TJS.NULL_ARG, dsp );
				if( hr < 0 ) return hr;
			}
		}

		hr = copyAllMembers( dsp );
		if( hr < 0 ) return hr;
		hr = registerVariable( dsp );
		if( hr < 0 ) return hr;

		// メンバ変数の初期化 (ネイティブクラスの初期化済みメンバーをコピーしているので不要かな？)

		// コンストラクタ呼び出し
		final int param_len = param.length;
		Variant[] arg = new Variant[param_len];
		for( int i = 0; i < param_len; i++ ) {
			arg[i] = new Variant(param[i]);
		}
		hr = super.funcCall( 0, mClassName, null, arg, dsp); // call constructor
			// call the constructor
		if( hr == Error.E_MEMBERNOTFOUND ) hr = Error.S_OK;
			// missing constructor is OK ( is this ugly ? )
		if( hr >= 0 ) result.set( dsp );

		return hr;
	}
	// property get
	@Override
	public int propGet( int flag, final String membername, Variant result, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;

		int hr = super.propGet(flag, membername, result, objthis);
		if( membername != null && hr == Error.E_MEMBERNOTFOUND ) {
			// look up super class
			final int count = mSuperClasses.length;
			if( count != 0 ) {
				for( int i = count-1; i >= 0; i--) {
					hr = mSuperClasses[i].propGet(flag, membername, result, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// property set
	@Override
	public int propSet( int flag, String membername, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() ) return Error.E_INVALIDOBJECT;

		int hr = super.propSet(flag, membername, param, objthis);
		if( membername != null && hr == Error.E_MEMBERNOTFOUND ) {
			// look up super class
			final int count = mSuperClasses.length;
			if( count != 0 ) {
				for( int i = count-1; i >= 0; i--) {
					hr = mSuperClasses[i].propSet(flag, membername, param, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}
	// class instance matching returns false or true
	@Override
	public int isInstanceOf( int flag, String membername, String classname, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity()) return Error.E_INVALIDOBJECT;

		int hr = super.isInstanceOf(flag, membername, classname, objthis);
		if( membername != null && hr == Error.E_MEMBERNOTFOUND ) {
			// look up super class
			final int count = mSuperClasses.length;
			if( count != 0 ) {
				for( int i = count-1; i >= 0; i--) {
					hr = mSuperClasses[i].isInstanceOf(flag, membername, classname, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// get member count
	@Override
	public int getCount( IntWrapper result, final String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.getCount( result, membername, objthis);
		if( membername != null && hr == Error.E_MEMBERNOTFOUND ) {
			// look up super class
			final int count = mSuperClasses.length;
			if( count != 0 ) {
				for( int i = count-1; i >= 0; i--) {
					hr = mSuperClasses[i].getCount( result, membername, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// delete member
	@Override
	public int deleteMember( int flag, String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.deleteMember(flag, membername, objthis);
		if( membername != null && hr == Error.E_MEMBERNOTFOUND ) {
			// look up super class
			final int count = mSuperClasses.length;
			if( count != 0 ) {
				for( int i = count-1; i >= 0; i--) {
					hr = mSuperClasses[i].deleteMember( flag, membername, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// invalidation
	@Override
	public int invalidate( int flag, String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.invalidate(flag, membername, objthis);
		if( membername != null && hr == Error.E_MEMBERNOTFOUND ) {
			// look up super class
			final int count = mSuperClasses.length;
			if( count != 0 ) {
				for( int i = count-1; i >= 0; i--) {
					hr = mSuperClasses[i].invalidate( flag, membername, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// get validation, returns true or false
	@Override
	public int isValid( int flag, String membername, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.isValid(flag, membername, objthis);
		if( membername != null && hr == Error.E_MEMBERNOTFOUND ) {
			// look up super class
			final int count = mSuperClasses.length;
			if( count != 0 ) {
				for( int i = count-1; i >= 0; i--) {
					hr = mSuperClasses[i].isValid( flag, membername, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

	// operation with member
	@Override
	public int operation( int flag, String membername, Variant result, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.operation(flag, membername, result, param, objthis);
		if( membername != null && hr == Error.E_MEMBERNOTFOUND ) {
			// look up super class
			final int count = mSuperClasses.length;
			if( count != 0 ) {
				for( int i = count-1; i >= 0; i--) {
					hr = mSuperClasses[i].operation( flag, membername, result, param, objthis);
					if( hr != Error.E_MEMBERNOTFOUND ) break;
				}
			}
		}
		return hr;
	}

}
