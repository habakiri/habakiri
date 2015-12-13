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

public class ObjectProxy implements Dispatch2 {
/*
	a class that do:
	1. first access to the Dispatch1
	2. if failed, then access to the Dispatch2
*/

	public ObjectProxy() {}

	private Dispatch2 mDispatch1;
	private Dispatch2 mDispatch2;

	public void setObjects( Dispatch2 dsp1, Dispatch2 dsp2 )
	{
		mDispatch1 = dsp1;
		mDispatch2 = dsp2;
	}

	@Override
	public int funcCall(int flag, String membername, Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.funcCall(flag, membername, result, param, OBJ1);
		if( hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2 ) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.funcCall(flag, membername, result, param, OBJ2);
		}
		return hr;
	}

	@Override
	public int funcCallByNum(int flag, int num, Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.funcCallByNum(flag, num, result, param, OBJ1);
		if( hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2 ) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.funcCallByNum(flag, num, result, param, OBJ2);
		}
		return hr;
	}

	@Override
	public int propGet(int flag, String membername, Variant result, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr =mDispatch1.propGet(flag, membername, result, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.propGet(flag, membername, result, OBJ2);
		}
		return hr;
	}

	@Override
	public int propGetByNum(int flag, int num, Variant result, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.propGetByNum(flag, num, result, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.propGetByNum(flag, num, result, OBJ2);
		}
		return hr;
	}

	@Override
	public int propSet(int flag, String membername, Variant param, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.propSet(flag, membername, param, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.propSet(flag, membername, param, OBJ2);
		}
		return hr;
	}

	@Override
	public int propSetByNum(int flag, int num, Variant param, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.propSetByNum(flag, num, param, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.propSetByNum(flag, num, param, OBJ2);
		}
		return hr;
	}

	@Override
	public int getCount(IntWrapper result, String membername, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.getCount(result, membername, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.getCount(result, membername, OBJ2);
		}
		return hr;
	}

	@Override
	public int getCountByNum(IntWrapper result, int num, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.getCountByNum(result, num, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.getCountByNum(result, num, OBJ2);
		}
		return hr;
	}

	/*
	@Override
	public int enumMembers(int flag, VariantClosure callback, Dispatch2 objthis) throws VariantException, TJSException {
		return Error.E_NOTIMPL;
	}
	*/
	@Override
	public int enumMembers( int flags, EnumMembersCallback callback, Dispatch2 objthis ) throws VariantException, TJSException {
		return Error.E_NOTIMPL;
	}

	@Override
	public int deleteMember(int flag, String membername, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.deleteMember(flag, membername, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.deleteMember(flag, membername, OBJ2);
		}
		return hr;
	}

	@Override
	public int deleteMemberByNum(int flag, int num, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.deleteMemberByNum(flag, num, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.deleteMemberByNum(flag, num, OBJ2);
		}
		return hr;
	}

	@Override
	public int invalidate(int flag, String membername, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.invalidate(flag, membername, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.invalidate(flag, membername, OBJ2);
		}
		return hr;
	}

	@Override
	public int invalidateByNum(int flag, int num, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.invalidateByNum(flag, num, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.invalidateByNum(flag, num, OBJ2);
		}
		return hr;
	}

	@Override
	public int isValid(int flag, String membername, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.isValid(flag, membername, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.isValid(flag, membername, OBJ2);
		}
		return hr;
	}

	@Override
	public int isValidByNum(int flag, int num, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.isValidByNum(flag, num, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.isValidByNum(flag, num, OBJ2);
		}
		return hr;
	}

	@Override
	public int createNew(int flag, String membername, Holder<Dispatch2> result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.createNew(flag, membername, result, param, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.createNew(flag, membername, result, param, OBJ2);
		}
		return hr;
	}

	@Override
	public int createNewByNum(int flag, int num, Holder<Dispatch2> result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.createNewByNum(flag, num, result, param, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.createNewByNum(flag, num, result, param, OBJ2);
		}
		return hr;
	}

	@Override
	public int isInstanceOf(int flag, String membername, String classname, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.isInstanceOf(flag, membername, classname, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.isInstanceOf(flag, membername, classname, OBJ2);
		}
		return hr;
	}

	@Override
	public int isInstanceOfByNum(int flag, int num, String classname, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.isInstanceOfByNum(flag, num, classname, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.isInstanceOfByNum(flag, num, classname, OBJ2);
		}
		return hr;
	}

	@Override
	public int operation(int flag, String membername, Variant result, Variant param, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.operation(flag, membername, result, param, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.operation(flag, membername, result, param, OBJ2);
		}
		return hr;
	}

	@Override
	public int operationByNum(int flag, int num, Variant result, Variant param, Dispatch2 objthis) throws VariantException, TJSException {
		Dispatch2 OBJ1 = ((objthis != null) ? (objthis) : (mDispatch1));
		int hr = mDispatch1.operationByNum(flag, num, result, param, OBJ1);
		if(hr == Error.E_MEMBERNOTFOUND && mDispatch1 != mDispatch2) {
			Dispatch2 OBJ2 = ((objthis != null) ? (objthis) : (mDispatch2));
			return mDispatch2.operationByNum(flag, num, result, param, OBJ2);
		}
		return hr;
	}

	@Override
	public int nativeInstanceSupport(int flag, int classid, Holder<NativeInstance> pointer) {
		return Error.E_NOTIMPL;
	}

	@Override
	public int classInstanceInfo(int flag, int num, Variant value) throws VariantException {
		return Error.E_NOTIMPL;
	}
	@Override
	public int addClassInstanveInfo( final String name) {
		return Error.E_NOTIMPL;
	}

	@Override
	public NativeInstance getNativeInstance(int classid) {
		return null;
	}

	@Override
	public int setNativeInstance(int classid, NativeInstance ni) {
		return Error.E_NOTIMPL;
	}

}
