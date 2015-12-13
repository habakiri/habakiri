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

import java.util.ArrayList;

public class ExprNode {

	private int mOp;
	private int mPosition;
	private ArrayList<ExprNode> mNodes;
	private Variant mVal;

	public ExprNode() {
		//mOp = 0;
		//mNodes = null;
		//mVal = null;
		mPosition = -1;
	}
	protected void finalize() {
		if( mNodes != null ) {
			final int count = mNodes.size();
			for( int i = 0; i < count; i++ ) {
				ExprNode node = mNodes.get(i);
				if( node != null ) node.clear();
			}
			mNodes.clear();
			mNodes = null;
		}
		if( mVal != null ) {
			mVal.clear();
			mVal = null;
		}
	}

	public final void setOpecode( int op ) { mOp = op; }
	public final void setPosition( int pos ) { mPosition = pos; }
	public final void setValue( Variant val ) {
		if( mVal == null ) {
			mVal = new Variant(val);
		} else {
			mVal.copyRef( val );
		}
	}
	public final void add( ExprNode node ) {
		if( mNodes == null ) mNodes = new ArrayList<ExprNode>();
		mNodes.add( node );
	}

	public final int getOpecode() { return mOp; }
	public final int getPosition() { return mPosition; }
	public final Variant getValue() { return mVal; }
	public final ExprNode getNode( int index ) {
		if( mNodes == null ) {
			return null;
		} else if( index < mNodes.size() ) {
			return mNodes.get( index );
		} else {
			return null;
		}
	}
	public final int getSize() {
		if( mNodes == null ) return 0;
		else return mNodes.size();
	}
	public final void addArrayElement( Variant val ) throws TJSException, VariantException {
		final String ss_add = "add";
		Variant[] args = new Variant[1];
		args[0] = val;
		mVal.asObjectClosure().funcCall(0, ss_add, null, args, null );
	}
	public final void addDictionaryElement( String name, Variant val ) throws TJSException, VariantException {
		mVal.asObjectClosure().propSet(Interface.MEMBERENSURE, name, val, null);
	}

	public final void clear() {
		if( mNodes != null ) {
			final int count = mNodes.size();
			for( int i = 0; i < count; i++ ) {
				ExprNode node = mNodes.get(i);
				if( node != null ) node.clear();
			}
			mNodes.clear();
			mNodes = null;
		}
		if( mVal != null ) {
			mVal.clear();
			mVal = null;
		}
	}
}
