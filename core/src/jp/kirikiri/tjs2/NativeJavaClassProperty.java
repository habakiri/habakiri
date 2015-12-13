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
/*
 * TODO 未テスト
 */
package jp.kirikiri.tjs2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class NativeJavaClassProperty extends Dispatch {

	private Method mGet;
	private Method mSet;
	private Class<?> mParamType;
	private Class<?> mReturnType;
	private boolean mIsStaticGet;
	private boolean mIsStaticSet;
	private int mClassID;

	public NativeJavaClassProperty( Method get, Method set, int classID ) throws TJSException {
		mClassID = classID;
		mGet = get;
		if( get != null ) {
			mReturnType = get.getReturnType();
			if( mReturnType.equals(void.class) ) {
				throw new TJSException( Error.InternalError );
			}
			if( Modifier.isStatic( get.getModifiers() ) ) mIsStaticGet = true;
		}
		mSet = set;
		if( set != null ) {
			@SuppressWarnings("rawtypes")
			Class[] params = set.getParameterTypes();
			if( params.length == 1 ) {
				mParamType = params[0];
			} else {
				throw new TJSException( Error.InternalError );
			}
			if( Modifier.isStatic( set.getModifiers() ) ) mIsStaticSet = true;
		}
	}
	public int isInstanceOf( int flag, final String membername, final String classname, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername == null ) {
			if( "Property".equals(classname) ) return Error.S_TRUE;
		}
		return super.isInstanceOf(flag, membername, classname, objthis);
	}
	public int propGet( int flag, final String membername, Variant result, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername != null ) return super.propGet( flag, membername, result, objthis );

		if( result == null ) return Error.E_FAIL;
		if( mGet == null ) return Error.E_ACCESSDENYED;

		Object self;
		if( mIsStaticGet ) {
			self = null; // static 時は null
		} else {
			if( objthis == null ) return Error.E_NATIVECLASSCRASH;
			NativeJavaInstance ni = (NativeJavaInstance)objthis.getNativeInstance( mClassID );
			if( ni == null  ) return Error.E_FAIL;
			self = ni.getNativeObject();
			if( self == null ) return Error.E_NATIVECLASSCRASH;
		}
		int er = Error.S_OK;
		try {
			Object ret = mGet.invoke( self );
			NativeJavaClass.javaObjectToVariant( result, mReturnType, ret );
		} catch (IllegalArgumentException e) {
			er = Error.E_INVALIDPARAM;
		} catch (IllegalAccessException e) {
			er = Error.E_ACCESSDENYED;
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if( t instanceof VariantException ) {
				throw (VariantException)t;
			} else if( t instanceof TJSException ) {
				throw (TJSException)t;
			} else {
				throw new TJSException( t.toString() );
			}
		}
		return er;
	}
	public int propSet( int flag, final String membername, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername != null ) return super.propSet(flag, membername, param, objthis);
		if( objthis == null ) return Error.E_NATIVECLASSCRASH;
		if( param == null ) return Error.E_FAIL;
		if( mSet == null ) return Error.E_ACCESSDENYED;

		Object self;
		if( mIsStaticSet ) {
			self = null; // static 時は null
		} else {
			NativeJavaInstance ni = (NativeJavaInstance)objthis.getNativeInstance( mClassID );
			if( ni == null ) return Error.E_FAIL;
			self = ni.getNativeObject();
			if( self == null ) return Error.E_NATIVECLASSCRASH;
		}
		int er = Error.S_OK;
		Object arg = NativeJavaClass.variantToJavaObject( param, mParamType );
		if( arg == null ) return Error.E_INVALIDPARAM;
		Object[] args = new Object[1];
		args[0] = arg;
		try {
			mSet.invoke( self, args );
		} catch (IllegalArgumentException e) {
			er = Error.E_INVALIDPARAM;
		} catch (IllegalAccessException e) {
			er = Error.E_ACCESSDENYED;
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if( t instanceof VariantException ) {
				throw (VariantException)t;
			} else if( t instanceof TJSException ) {
				throw (TJSException)t;
			} else {
				throw new TJSException( t.toString() );
			}
		}
		return er;
	}
}
