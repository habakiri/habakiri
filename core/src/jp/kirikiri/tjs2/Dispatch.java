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

public class Dispatch implements Dispatch2 {

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

	protected void beforeDestruction() throws VariantException, TJSException {}
	private boolean mBeforeDestructionCalled;
	//public Dispatch() {
		//mBeforeDestructionCalled = false;
		//TJS.pushObject(this);
	//}
	public void doFinalize() {
		// object destruction
		if( !mBeforeDestructionCalled ) {
			mBeforeDestructionCalled = true;
			try {
				beforeDestruction();
			} catch (VariantException e) {
			} catch (TJSException e) {
			}
		}
	}
	protected void finalize() {
		TJS.pushObject(this);
	}

	/*
	protected void finalize() {
		// object destruction
		if( !mBeforeDestructionCalled ) {
			mBeforeDestructionCalled = true;
			try {
				beforeDestruction();
			} catch (VariantException e) {
			} catch (TJSException e) {
			}
		}
		try {
			super.finalize();
		} catch (Throwable e) {
		}
	}
	*/

	// function invocation
	public int funcCall( int flag, final String memberName, Variant result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException {
		return memberName != null ? Error.E_MEMBERNOTFOUND : Error.E_NOTIMPL;
	}

	// function invocation by index number
	public int funcCallByNum( int flag, int num, Variant result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException {
		return funcCall( flag, String.valueOf( num ), result, param, objThis );
	}

	// property get
	public int propGet( int flag, final String memberName, Variant result, Dispatch2 objThis ) throws VariantException, TJSException {
		return memberName != null ? Error.E_MEMBERNOTFOUND : Error.E_NOTIMPL;
	}

	// property get by index number
	public int propGetByNum( int flag, int num, Variant result, Dispatch2 objThis ) throws VariantException, TJSException {
		return propGet( flag, String.valueOf(num), result, objThis );
	}

	// property set
	public int propSet( int flag, String memberName, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException {
		return memberName != null ? Error.E_MEMBERNOTFOUND : Error.E_NOTIMPL;
	}

	// property set by index number
	public int propSetByNum( int flag, int num, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException {
		return propSet( flag, String.valueOf(num), param, objThis );
	}

	// get member count
	public int getCount( IntWrapper result, final String memberName, Dispatch2 objThis ) throws VariantException, TJSException {
		return Error.E_NOTIMPL;
	}

	// get member count by index number ( result is Integer )
	public int getCountByNum( IntWrapper result, int num, Dispatch2 objThis ) throws VariantException, TJSException {
		return getCount( result, String.valueOf(num), objThis );
	}

	// enumerate members
	/*
	public int enumMembers( int flag, VariantClosure callback, Dispatch2 objThis ) throws VariantException, TJSException {
		return Error.E_NOTIMPL;
	}
	*/
	public int enumMembers( int flags, EnumMembersCallback callback, Dispatch2 objthis ) throws VariantException, TJSException {
		return Error.E_NOTIMPL;
	}

	// delete member
	public int deleteMember( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException {
		return memberName != null ? Error.E_MEMBERNOTFOUND : Error.E_NOTIMPL;
	}

	// delete member by index number
	public int deleteMemberByNum( int flag, int num, Dispatch2 objThis ) throws VariantException, TJSException {
		return deleteMember( flag, String.valueOf(num), objThis );
	}

	// invalidation
	public int invalidate( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException {
		return memberName != null ? Error.E_MEMBERNOTFOUND : Error.E_NOTIMPL;
	}

	// invalidation by index number
	public int invalidateByNum( int flag, int num, Dispatch2 objThis ) throws VariantException, TJSException {
		return invalidate( flag, String.valueOf(num), objThis );
	}

	// get validation, returns true or false
	public int isValid( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException {
		return memberName != null ? Error.E_MEMBERNOTFOUND : Error.E_NOTIMPL;
	}

	// get validation by index number, returns true or false
	public int isValidByNum( int flag, int num, Dispatch2 objThis ) throws VariantException, TJSException {
		return isValid( flag, String.valueOf(num), objThis );
	}

	// create new object
	public int createNew( int flag, String memberName, Holder<Dispatch2> result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException {
		return memberName != null ? Error.E_MEMBERNOTFOUND : Error.E_NOTIMPL;
	}

	// create new object by index number
	public int createNewByNum( int flag, int num, Holder<Dispatch2> result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException {
		return createNew( flag, String.valueOf(num), result, param, objThis );
	}

	// reserved1 not use

	// class instance matching returns false or true
	public int isInstanceOf( int flag, String memberName, String className, Dispatch2 objThis ) throws VariantException, TJSException {
		return memberName != null ? Error.E_MEMBERNOTFOUND : Error.E_NOTIMPL;
	}

	// class instance matching by index number
	public int isInstanceOfByNum( int flag, int num, String className, Dispatch2 objThis ) throws VariantException, TJSException {
		return isInstanceOf( flag, String.valueOf(num), className, objThis );
	}

	// operation with member
	public int operation( int flag, String memberName, Variant result, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException {
		int op = flag & OP_MASK;
		if( op != OP_INC && op != OP_DEC && param == null )
			return Error.E_INVALIDPARAM;
		if( op < OP_MIN || op > OP_MAX )
			return Error.E_INVALIDPARAM;

		Variant tmp = new Variant();
		int hr = propGet( 0, memberName, tmp, objThis );
		if( hr < 0 ) return hr;	// #define TJS_FAILED(x) ((x)<0)

		doVariantOperation( op, tmp, param );

		hr = propSet( 0, memberName, tmp, objThis );
		if( hr < 0 ) return hr;

		if( result != null ) result.copyRef(tmp);

		return Error.S_OK;
	}

	// operation with member by index number
	public int operationByNum( int flag, int num, Variant result, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException {
		return operation( flag, String.valueOf(num), result, param, objThis );
	}

	// support for native instance
	public int nativeInstanceSupport( int flag, int classid, Holder<NativeInstance> pointer ) {
		return Error.E_NOTIMPL;
	}

	// support for class instance infomation
	public int classInstanceInfo( int flag, int num, Variant value ) throws VariantException {
		return Error.E_NOTIMPL;
	}
	@Override
	public int addClassInstanveInfo(String name) {
		return Error.E_NOTIMPL;
	}

	// special funcsion
	public NativeInstance getNativeInstance( int classid ) {
		Holder<NativeInstance> holder = new Holder<NativeInstance>(null);
		int hr = nativeInstanceSupport( Interface.NIS_GETINSTANCE, classid, holder );
		if( hr >= 0 ) {
			return holder.mValue;
		} else {
			return null;
		}
	}
	@Override
	public int setNativeInstance(int classid, NativeInstance ni) {
		Holder<NativeInstance> holder = new Holder<NativeInstance>(ni);
		return  nativeInstanceSupport( Interface.NIS_REGISTER, classid, holder );
	}

	public static void doVariantOperation( int op, Variant target, final Variant param) throws VariantException {
		switch(op) {
		case OP_BAND:
			target.andEqual( param );
			return;
		case OP_BOR:
			target.orEqual( param );
			return;
		case OP_BXOR:
			target.bitXorEqual( param );
			return;
		case OP_SUB:
			target.subtractEqual( param );
			return;
		case OP_ADD:
			target.addEqual( param );
			return;
		case OP_MOD:
			target.residueEqual( param );
			return;
		case OP_DIV:
			target.divideEqual( param );
			return;
		case OP_IDIV:
			target.idivequal( param );
			return;
		case OP_MUL:
			target.multiplyEqual( param );
			return;
		case OP_LOR:
			target.logicalorequal( param );
			return;
		case OP_LAND:
			target.logicalandequal( param );
			return;
		case OP_SAR:
			target.rightShiftEqual( param );
			return;
		case OP_SAL:
			target.leftShiftEqual( param );
			return;
		case OP_SR:
			target.rbitshiftequal( param );
			return;
		case OP_INC:
			target.increment();
			return;
		case OP_DEC:
			target.decrement();
			return;
		}
	}
	// reserved2
	// reserved3
}
