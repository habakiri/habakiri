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

public class NativeClass extends CustomObject {

	protected int mClassIDInternal;
	protected String mClassName;
	private Variant mWorkParam;
	//private Callback mCallback;
	//private VariantClosure mCallbackClosure;

	/*
	// a class to receive member callback from class
	static class Callback extends Dispatch {
		public Dispatch2 mDest; // destination object
		public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
			// *param[0] = name   *param[1] = flags   *param[2] = value
			int flags = param[1].asInteger();
			if( (flags & Interface.STATICMEMBER) == 0 ) {
				Variant val = new Variant( param[2] );
				if( val.isObject() ) {
					// change object's objthis if the object's objthis is null
					if( val.asObjectThis() == null ) val.changeClosureObjThis(mDest);
				}

				mDest.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP|flags, param[0].asString(), val, mDest );
			}
			if( result != null ) result.set(1); // returns true
			return Error.S_OK;
		}
	};
	*/

	public NativeClass( String name ) {
		super();
		mCallFinalize = false;
		mClassName = TJS.mapGlobalStringMap(name);
		mWorkParam = new Variant();
		//mCallback = new Callback();
		//mCallbackClosure = new VariantClosure(mCallback,null);
	}
	public void registerNCM( final String name, Dispatch2 dsp, final String className, int type ) throws VariantException, TJSException {
		registerNCM( name, dsp, className, type, 0 );
	}
	public void registerNCM( final String name, Dispatch2 dsp, final String className, int type, int flags ) throws VariantException, TJSException {
		String tname = TJS.mapGlobalStringMap(name);
		/* デバッグ機能は未実装
		// set object type for debugging
		if(TJSObjectHashMapEnabled()) {
			switch(type) {
			case nitMethod:
				TJSObjectHashSetType(dsp, ttstr(TJS_W("(native function) ")) + classname + TJS_W(".") + name);
				break;
			case nitProperty:
				TJSObjectHashSetType(dsp, ttstr(TJS_W("(native property) ")) + classname + TJS_W(".") + name);
				break;
			/*
			case nitClass:
				The information is not set here
				(is to be set in tTJSNativeClass::tTJSNativeClass)
			*//*
			}
		}
		*/
		// add to this
		//Variant val = new Variant(dsp);
		//propSet( (Interface.MEMBERENSURE | Interface.IGNOREPROP) | flags, tname, val, this);
		mWorkParam.set(dsp);
		try {
			propSet( (Interface.MEMBERENSURE | Interface.IGNOREPROP) | flags, tname, mWorkParam, this);
		} finally {
			mWorkParam.clear();
		}
	}
	protected void finalizeObject() throws VariantException, TJSException {
		super.finalizeObject();
	}
	protected Dispatch2 createBaseTJSObject() { return new CustomObject(); }
	protected NativeInstance createNativeInstance() throws TJSException { return null; }
	public final String getClassName() { return mClassName; }
	public void setClassID( int classid ) { mClassIDInternal = classid; }

	public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( !getValidity() )
			return Error.E_INVALIDOBJECT;

		if( membername != null ) return super.funcCall(flag, membername, result, param, objthis);

		// 生成を高速化するためにメンバコピーを特別処理する形で実装
		objthis.addClassInstanveInfo( mClassName );
		NativeInstance nativeptr = createNativeInstance();
		objthis.setNativeInstance( mClassIDInternal, nativeptr );
		int hr = copyAllMembers( (CustomObject)objthis );
		if( hr < 0 ) return hr;
		/*
		//Variant name = new Variant( mClassName );
		//objthis.classInstanceInfo( Interface.CII_ADD, 0, name ); // add class name
		objthis.addClassInstanveInfo( mClassName );

		// create base native object
		NativeInstance nativeptr = createNativeInstance();

		// register native instance information to the object;
		// even if "nativeptr" is null
		objthis.setNativeInstance( mClassIDInternal, nativeptr );

		// register members to "objthis"
		//Callback callback = new Callback();
		mCallback.mDest = objthis;

		// enumerate members
		try {
			enumMembers( Interface.IGNOREPROP, mCallbackClosure, this);
		} finally {
			mCallback.mDest = null;
		}
		*/
		return Error.S_OK;
	}
	public int createNew( int flag, final String membername, Holder<Dispatch2> result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		// CreateNew
		Dispatch2 dsp = createBaseTJSObject();
		/* デバッグ機能は省く
		// set object type for debugging
		if(TJSObjectHashMapEnabled())
			TJSObjectHashSetType(dsp, TJS_W("instance of class ") + ClassName);
		*/

		// instance initialization
		//int hr = funcCall( 0, null, null, null, dsp); // add member to dsp
		// 生成を高速化するためにメンバコピーを特別処理する形で実装
		dsp.addClassInstanveInfo( mClassName );
		NativeInstance nativeptr = createNativeInstance();
		dsp.setNativeInstance( mClassIDInternal, nativeptr );
		int hr = copyAllMembers( (CustomObject)dsp );
		if( hr < 0 ) return hr;

		hr = super.funcCall( 0, mClassName, null, param, dsp); // call constructor
			// call the constructor
		if( hr == Error.E_MEMBERNOTFOUND ) hr = Error.S_OK;
			// missing constructor is OK ( is this ugly ? )
		if( hr >= 0 ) result.set( dsp );

		return hr;
	}
	public int isInstanceOf( int flag, final String membername, final String classname, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername == null ) {
			if( "Class".equals(classname) ) return Error.S_TRUE;
			if( mClassName != null && mClassName.equals(classname) ) return Error.S_TRUE;
		}
		return super.isInstanceOf(flag, membername, classname, objthis);
	}
	/*
	protected void registerMethods( Class<?> c, final String classname ) throws VariantException, TJSException {
		//クラスに存在するメソッドをすべて取得し登録する
		@SuppressWarnings("rawtypes")
		Class[] propArgClass = new Class[]{ Variant.class, Dispatch2.class };
		try {
			Method dennySetMethod;
			Method dennyGetMethod;
			try {
				dennyGetMethod = c.getMethod( "dennyPropGet", propArgClass );
				dennySetMethod = c.getMethod( "dennyPropSet", propArgClass );
			} catch (NoSuchMethodException e1) {
				throw new TJSException(Error.InternalError);
			}
			HashSet<String> registProp = new HashSet<String>(); // set/getで重複しないようにチェック

			Method[] methods = c.getMethods();
			for( Method m : methods ) {
				final String methodName = m.getName();
				final Annotation[] a = m.getDeclaredAnnotations();
				int flag = 0;
				if( a != null ) {
					final int acount = a.length;
					for( int i = 0; i < acount; i++ ) {
						if( a[i] instanceof TJSStatic ) {
							flag |= STATICMEMBER;
						}
					}
				}
				if( "constructor" .equals( methodName ) ) {
					// コンストラクタ
					// パラメータチェック
					@SuppressWarnings("rawtypes")
					Class[] params = m.getParameterTypes();
					if( params.length == 3 ) {
						if( params[0] == Variant.class &&
							params[1] == Variant[].class &&
							params[2] == Dispatch2.class ) {
							registerNCM( classname, new NativeClassConstructor(m), classname, nitMethod, flag );
						}
					}
				} else if( methodName.startsWith("prop_") ) {
					// プロパティ
					@SuppressWarnings("rawtypes")
					Class[] params = m.getParameterTypes();
					if( params.length == 2 ) {
						if( params[0] == Variant.class && params[1] == Dispatch2.class ) {
							Method setMethod = null;
							Method getMethod = null;
							String propName = null;
							if( methodName.startsWith("prop_set_") ) {
								setMethod = m;
								propName = methodName.substring( "prop_set_".length() );
								if( registProp.contains(propName) == false ) {
									try {
										getMethod = c.getMethod( "prop_get_" + propName, propArgClass );
									} catch (NoSuchMethodException e) {
										getMethod = null;
									}
								}
							} else if( methodName.startsWith("prop_get_") ) {
								getMethod = m;
								propName = methodName.substring( "prop_get_".length() );
								if( registProp.contains(propName) == false ) {
									try {
										setMethod = c.getMethod( "prop_set_" + propName, propArgClass );
									} catch (NoSuchMethodException e) {
										setMethod = null;
									}
								}
							}
							if( registProp.contains(propName) == false ) {
								if( setMethod != null || getMethod != null ) {
									if( setMethod == null ) setMethod = dennySetMethod;
									if( getMethod == null ) getMethod = dennyGetMethod;
									registerNCM( propName, new NativeClassProperty(getMethod, setMethod), classname, nitProperty, flag );
									registProp.add(propName);
								}
							}
						}
					}
				} else {
					// 通常メソッド
					// パラメータチェック
					@SuppressWarnings("rawtypes")
					Class[] params = m.getParameterTypes();
					if( params.length == 3 ) {
						if( params[0] == Variant.class &&
							params[1] == Variant[].class &&
							params[2] == Dispatch2.class ) {
							registerNCM( methodName, new NativeClassMethod(m), classname, nitMethod, flag );
						}
					}
				}
			}
			registProp = null;
		} catch (SecurityException e) {
			throw new TJSException(Error.InternalError + e.toString());
		}
	}
	static private final int E_ACCESSDENYED = -1007;
	public static int dennyPropGet( Variant result, Dispatch2 objthis ) { return E_ACCESSDENYED; }
	public static int dennyPropSet( Variant param, Dispatch2 objthis ) { return E_ACCESSDENYED; }
	*/
}
