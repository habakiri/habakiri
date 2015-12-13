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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessageMapper {
	static public class MessageHolder {
		final String mName;
		final String mDefaultMessage;
		String mAssignedMessage;

		public MessageHolder( final String name, final String defmsg ) {
			//mAssignedMessage = null;
			mDefaultMessage = defmsg;
			mName = name;
			TJS.registerMessageMap(mName, this);
		}

		public MessageHolder( final String name, final String defmsg, boolean regist ) {
			/* "name" and "defmsg" must point static area */
			//mAssignedMessage = null;
			mDefaultMessage = defmsg;
			if( regist ) {
				mName = name;
				TJS.registerMessageMap(mName, this);
			} else {
				mName = null;
			}
		}

		protected void finalize() {
			if(mName!=null) TJS.unregisterMessageMap(mName);
			if(mAssignedMessage!=null) mAssignedMessage = null;
		}

		public void assignMessage( final String msg ) {
			if(mAssignedMessage!=null) mAssignedMessage = null;
			mAssignedMessage = msg;
		}
		public String getMessage() {
			 return mAssignedMessage != null ? mAssignedMessage : mDefaultMessage;
		}
	}

	HashMap<String, MessageHolder> mHash;


	public MessageMapper() {
		mHash = new HashMap<String, MessageHolder>();
	}

	public void register( final String name, MessageHolder holder ) {
		mHash.put( name, holder);
	}

	public void unregister( final String name ) {
		mHash.remove(name);
	}

	public boolean assignMessage( final String name, final String newmsg ) {
		MessageHolder holder = mHash.get(name);
		if(holder!=null) {
			holder.assignMessage(newmsg);
			return true;
		}
		return false;
	}

	public String get( final String name ) {
		MessageHolder holder = mHash.get(name);
		if(holder!=null) {
			return holder.getMessage();
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public String createMessageMapString() {
		StringBuilder script = new StringBuilder();
		Collection ite = mHash.entrySet();
		for( Iterator i = ite.iterator(); i.hasNext(); ) {
		    Map.Entry entry = (Map.Entry)i.next();
		    String name = (String)entry.getKey();

			MessageHolder h = (MessageHolder)entry.getValue();
			script.append("\tr(\"");
			script.append(  LexBase.escapeC(name) );
			script.append("\", \"");
			script.append( LexBase.escapeC(h.getMessage()) );
			script.append("\");\n");
		}
		return script.toString();
	}
}
