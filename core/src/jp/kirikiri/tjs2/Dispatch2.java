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

public interface Dispatch2 {

	// function invocation
	public int funcCall( int flag, final String memberName, Variant result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException;

	// function invocation by index number
	public int funcCallByNum( int flag, int num, Variant result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException;

	// property get
	public int propGet( int flag, final String memberName, Variant result, Dispatch2 objThis ) throws VariantException, TJSException;

	// property get by index number
	public int propGetByNum( int flag, int num, Variant result, Dispatch2 objThis ) throws VariantException, TJSException;

	// property set
	public int propSet( int flag, String memberName, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException;

	// property set by index number
	public int propSetByNum( int flag, int num, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException;

	// get member count
	public int getCount( IntWrapper result, final String memberName, Dispatch2 objThis ) throws VariantException, TJSException;

	// get member count by index number ( result is Integer )
	public int getCountByNum( IntWrapper result, int num, Dispatch2 objThis ) throws VariantException, TJSException;

	// enumerate members
	//public int enumMembers( int flag, VariantClosure callback, Dispatch2 objThis ) throws VariantException, TJSException;
	public int enumMembers( int flags, EnumMembersCallback callback, Dispatch2 objthis ) throws VariantException, TJSException;

	// delete member
	public int deleteMember( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException;

	// delete member by index number
	public int deleteMemberByNum( int flag, int num, Dispatch2 objThis ) throws VariantException, TJSException;

	// invalidation
	public int invalidate( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException;

	// invalidation by index number
	public int invalidateByNum( int flag, int num, Dispatch2 objThis ) throws VariantException, TJSException;

	// get validation, returns true or false
	public int isValid( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException;

	// get validation by index number, returns true or false
	public int isValidByNum( int flag, int num, Dispatch2 objThis ) throws VariantException, TJSException;

	// create new object
	public int createNew( int flag, String memberName, Holder<Dispatch2> result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException;

	// create new object by index number
	public int createNewByNum( int flag, int num, Holder<Dispatch2> result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException;

	// reserved1 not use

	// class instance matching returns false or true
	public int isInstanceOf( int flag, String memberName, String className, Dispatch2 objThis ) throws VariantException, TJSException;

	// class instance matching by index number
	public int isInstanceOfByNum( int flag, int num, String className, Dispatch2 objThis ) throws VariantException, TJSException;

	// operation with member
	public int operation( int flag, String memberName, Variant result, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException;

	// operation with member by index number
	public int operationByNum( int flag, int num, Variant result, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException;

	// support for native instance
	public int nativeInstanceSupport( int flag, int classid, Holder<NativeInstance> pointer );

	// support for class instance infomation
	public int classInstanceInfo( int flag, int num, Variant value ) throws VariantException;

	// special funcsion
	public int addClassInstanveInfo( final String name );

	// special funcsion
	public NativeInstance getNativeInstance( int classid );

	// special funcsion
	public int setNativeInstance(int classid, NativeInstance ni);
	// reserved2
	// reserved3

}
