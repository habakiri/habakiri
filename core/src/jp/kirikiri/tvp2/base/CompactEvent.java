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
/**
 * Compact events are to be delivered when:
 * 1. the application is in idle state for long duration
 * 2. the application had been deactivated ( application has lost the focus )
 * 3. the application had been minimized
 * these are to reduce memory usage, like garbage collection, cache cleaning,
 * or etc ...
 */
package jp.kirikiri.tvp2.base;

import java.util.ArrayList;

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.TJSScriptError;
import jp.kirikiri.tjs2.TJSScriptException;

/**
 *
 */
public class CompactEvent {
	private ArrayList<CompactEventCallbackInterface> mCompactEventVector;
	public CompactEvent() {
		mCompactEventVector = new ArrayList<CompactEventCallbackInterface>();
	}
	public void addCompactEventHook(CompactEventCallbackInterface callback) {
		mCompactEventVector.add(callback);
	}
	public void removeCompactEventHook(CompactEventCallbackInterface callback) {
		final int count = mCompactEventVector.size();
		for( int i = 0; i < count; i++ ) {
			CompactEventCallbackInterface itf = mCompactEventVector.get(i);
			if( itf == callback ) {
				mCompactEventVector.set( i, null );
			}
		}
	}
	public void deliverCompactEvent( int level ) {
		// must be called by each platforms's implementation
		final int count = mCompactEventVector.size();
		if( count > 0 ) {
			boolean emptyflag = false;
			for( int i = 0; i < count; i ++) {
				// note that the handler can remove itself while the event
				try {
					try {
						CompactEventCallbackInterface itf = mCompactEventVector.get(i);
						if( itf != null )
							itf.onCompact(level);
						else
							emptyflag = true;
					} catch( TJSScriptException e ) {
						throw e;
					} catch( TJSScriptError e ) {
						throw e;
					} catch( TJSException e) {
						throw e;
					} catch( Exception e ) {
						throw new TJSException(e.getMessage());
					}
				} catch(TJSScriptError e) {
					e.addTrace( "Compact Event" );
					ScriptsClass.showScriptException(e);
				} catch( TJSException e ) {
					ScriptsClass.showScriptException(e);
				}
			}

			if( emptyflag ) {
				// the array has empty cell
				// eliminate empty
				for( int i = count-1; i >= 0; i-- ) {
					CompactEventCallbackInterface itf = mCompactEventVector.get(i);
					if( itf == null ) mCompactEventVector.remove(i);
				}
			}
		}
	}
}
