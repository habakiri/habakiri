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


public class VariantClosure /*implements Cloneable*/ {

	public Dispatch2	mObject;
	public Dispatch2	mObjThis;

	public VariantClosure( Dispatch2 obj ) {
		mObject = obj;
		//mObjThis = null;
	}
	public VariantClosure( Dispatch2 obj, Dispatch2 objthis ) {
		mObject = obj;
		mObjThis = objthis;
	}
	public void set( Dispatch2 obj ) {
		set( obj, null );
	}
	public void set( Dispatch2 obj, Dispatch2 objthis ) {
		mObject = obj;
		mObjThis = objthis;
	}
	public void set(VariantClosure clo) {
		mObject = clo.mObject;
		mObjThis = clo.mObjThis;
	}
	public Dispatch2 selectObject() {
		if( mObjThis != null ) return mObjThis;
		else return mObject;
	}
	public boolean equals( Object o ) {
		if( o instanceof VariantClosure ) {
			VariantClosure vc = (VariantClosure)o;
			return mObject == vc.mObject && mObjThis == vc.mObjThis;
		} else {
			return false ;
		}
	}
	/*
	public Object clone() {
		VariantClosure r;
		try {
			r = (VariantClosure)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
		Object o = null;
		if( mObject != null ) o = mObject.clone();
		Object t = null;
		if( mObjThis != null ) t = mObjThis.clone();
		r.set( o, t );
		return r;
	}
	*/
	public int funcCall( int flag, final String memberName, Variant result, Variant[] param, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.funcCall( flag, memberName, result, param,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int funcCallByNum( int flag, int num, Variant result, Variant[] param, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.funcCallByNum(flag, num, result, param,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int propGet( int flag, final String mumberName, Variant result, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.propGet(flag, mumberName, result,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int propGetByNum( int flag, int num, Variant result, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.propGetByNum(flag, num, result,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int propSet( int flag, String mumberName, final Variant param, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.propSet(flag, mumberName, param,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int propSetByNum( int flag, int num, final Variant param, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.propSetByNum(flag, num, param,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int getCount( IntWrapper result, final String memberName, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.getCount(result, memberName,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int getCountByNum( IntWrapper result, int num, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.getCountByNum(result, num,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	/*
	public int enumMembers( int flag, VariantClosure callback, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.enumMembers(flag, callback,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}
	*/
	public int enumMembers( int flags, EnumMembersCallback callback, Dispatch2 objthis ) throws VariantException, TJSException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.enumMembers(flags, callback,
				mObjThis != null ? mObjThis : (objthis != null ? objthis : mObject) );
	}

	public int deleteMember( int flag, String memberName, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.deleteMember(flag, memberName,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int deleteMemberByNum( int flag, int num, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.deleteMemberByNum(flag, num,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int invalidate( int flag, String memberName, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.invalidate(flag, memberName,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int invalidateByNum( int flag, int num, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.invalidateByNum(flag, num,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int isValid( int flag, String memberName, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.isValid(flag, memberName,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int isValidByNum( int flag, int num, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.isValidByNum(flag, num,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int createNew( int flag, String memberName, Holder<Dispatch2> result, Variant[] param, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.createNew(flag, memberName, result, param,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int createNewByNum( int flag, int num, Holder<Dispatch2> result, Variant[] param, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.createNewByNum(flag, num, result, param,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int isInstanceOf( int flag, String memberName, String className, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.isInstanceOf(flag, memberName, className,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	// オリジナルはバグ？ 関数名が一致していない
	//tjs_error IsInstanceOf(tjs_uint32 flag, tjs_int num, tjs_char *classname, iTJSDispatch2 *objthis) const {
	public int isInstanceOfByNum( int flag, int num, String className, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.isInstanceOfByNum(flag, num, className,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int operation( int flag, String memberName, Variant result, final Variant param, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.operation(flag, memberName, result, param,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}

	public int operationByNum( int flag, int num, Variant result, final Variant param, Dispatch2 objThis ) throws TJSException, VariantException {
		if( mObject == null ) throw new TJSException(Error.NullAccess);
		return mObject.operationByNum(flag, num, result, param,
				mObjThis != null ? mObjThis : (objThis != null ? objThis : mObject) );
	}
	@Override
	public final String toString() {
		StringBuilder str = new StringBuilder(128);
		str.append( "(object)" );
		str.append( '(' );
		if( mObject != null ) {
			str.append( '[' );
			if( mObject instanceof NativeClass ) {
				str.append( ((NativeClass)mObject).getClassName() );
			} else if( mObject instanceof InterCodeObject ) {
				str.append( ((InterCodeObject)mObject).getName() );
			} else if( mObject instanceof CustomObject ) {
				String name = ((CustomObject)mObject).getClassNames();
				if( name != null ) str.append( name );
				else str.append( mObject.getClass().getName() );
			} else {
				str.append( mObject.getClass().getName() );
			}
			str.append( ']' );
		} else {
			str.append("0x00000000");
		}
		if( mObjThis != null ) {
			str.append( '[' );
			if( mObjThis instanceof NativeClass ) {
				str.append( ((NativeClass)mObjThis).getClassName() );
			} else if( mObjThis instanceof InterCodeObject ) {
				str.append( ((InterCodeObject)mObjThis).getName() );
			} else if( mObjThis instanceof CustomObject ) {
				String name = ((CustomObject)mObjThis).getClassNames();
				if( name != null ) str.append( name );
				else str.append( mObjThis.getClass().getName() );
			} else {
				str.append( mObjThis.getClass().getName() );
			}
			str.append( ']' );
		} else {
			str.append(":0x00000000");
		}
		str.append( ')' );
		return str.toString();
	}
}
