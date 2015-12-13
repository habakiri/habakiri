/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * �� software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * �\�[�X�R�[�h�`�����o�C�i���`�����A�ύX���邩���Ȃ������킸�A�ȉ��̏�����
 * �����ꍇ�Ɍ���A�ĔЕz����юg�p��������܂��B
 *
 * �E�\�[�X�R�[�h���ĔЕz����ꍇ�A��L�̒��쌠�\���A�{�����ꗗ�A����щ��L�Ɛ�
 *   �������܂߂邱�ƁB
 * �E�o�C�i���`���ōĔЕz����ꍇ�A�Еz���ɕt���̃h�L�������g���̎����ɁA��L��
 *   ���쌠�\���A�{�����ꗗ�A����щ��L�Ɛӏ������܂߂邱�ƁB
 * �E���ʂɂ����ʂ̋��Ȃ��ɁA�{�\�t�g�E�F�A����h���������i�̐�`�܂��͔̔�
 *   ���i�ɁA�g�D�̖��O�܂��̓R���g���r���[�^�[�̖��O���g�p���Ă͂Ȃ�Ȃ��B
 *
 * �{�\�t�g�E�F�A�́A���쌠�҂���уR���g���r���[�^�[�ɂ���āu����̂܂܁v��
 * ����Ă���A�����َ����킸�A���ƓI�Ȏg�p�\���A����ѓ���̖ړI�ɑ΂���K
 * �����Ɋւ���Öق̕ۏ؂��܂߁A�܂�����Ɍ��肳��Ȃ��A�����Ȃ�ۏ؂�����܂�
 * ��B���쌠�҂��R���g���r���[�^�[���A���R�̂�������킸�A���Q�����̌�������
 * ����킸�A���ӔC�̍������_��ł��邩���i�ӔC�ł��邩�i�ߎ����̑��́j�s�@
 * �s�ׂł��邩���킸�A���ɂ��̂悤�ȑ��Q����������\����m�炳��Ă����Ƃ�
 * �Ă��A�{�\�t�g�E�F�A�̎g�p�ɂ���Ĕ��������i��֕i�܂��͑�p�T�[�r�X�̒��B�A
 * �g�p�̑r���A�f�[�^�̑r���A���v�̑r���A�Ɩ��̒��f���܂߁A�܂�����Ɍ��肳���
 * ���j���ڑ��Q�A�Ԑڑ��Q�A�����I�ȑ��Q�A���ʑ��Q�A�����I���Q�A�܂��͌��ʑ��Q��
 * ���āA��ؐӔC�𕉂�Ȃ����̂Ƃ��܂��B
 ******************************************************************************
 * �{�\�t�g�E�F�A�́A�g���g��2 ( http://kikyou.info/tvp/ ) �̃\�[�X�R�[�h��Java
 * �ɏ������������̂��ꕔ�g�p���Ă��܂��B
 * �g���g��2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
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
