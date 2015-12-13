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
package jp.kirikiri.tvp2.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.SystemInitializer;

public class LogStreamHolder {

	PrintWriter mStream;
	boolean mAlive;
	boolean mOpenFailed;

	public LogStreamHolder() {
		//mStream = null;
		mAlive = true;
		//mOpenFailed = false;
	}

	protected void finalize() {
		if(mStream!=null){
			mStream.close();
		}
		mAlive = false;
	}
	 private static boolean checkBeforeWritefile(File file){
		  if( file.exists() ){
			  if( file.isFile() && file.canWrite() ){
				  return true;
			  }
			  return false;
		  }
		  return true;
	}
	private static final String SEPARATOR = "\n\n\n==============================================================================\n==============================================================================\n";
	private void open( boolean append ) {
		if(mOpenFailed) return; // no more try

		try {
			String filename = null;
			if( TVP.DebugLog.enableLogLoacation() == false) {
				mStream = null;
				mOpenFailed = true;
			} else {
				// no log location specified
				filename = TVP.DebugLog.getNativeLogLocation() + "\\krkr.console.log";
				SystemInitializer.ensureDataPathDirectory();

				File file = new File(filename);

				if( checkBeforeWritefile(file) ) {
					boolean appendbom = file.exists() == false;
					mStream = new PrintWriter(new BufferedWriter(new FileWriter(file,append)));
					if( mStream != null && appendbom ) {
						mStream.write(0xff);
						mStream.write(0xfe);
					}
				}
				if(mStream==null) mOpenFailed = true;
			}

			if(mStream!=null) {
				log(SEPARATOR);

				Calendar cal = Calendar.getInstance();
				Date date = cal.getTime();
				DateFormat formatter = DateFormat.getDateInstance();
				String timebuf = formatter.format(date);
				formatter = null;
				date = null;
				cal = null;

				StringBuffer buffer = new StringBuffer(32);
				buffer.append("Logging to ");
				buffer.append( filename );
				buffer.append(" started on ");
				buffer.append( timebuf );
				log( buffer.toString() );
				buffer = null;

			}
		} catch( IOException e ) {
			mOpenFailed = true;
		}
	}

	/**
	 * clear log text
	 */
	public void clear() {
		if( mStream != null ) mStream.close();
		open( false );
	}
	public void log( final String text ) {
		if(mStream==null) open(true);

		if(mStream!=null) {
			mStream.print(text);
			mStream.print('\n');
			mStream.flush();
		}
	}

	/**
	 * reopen log stream
	 */
	public void reopen() {
		if(mStream!=null) mStream.close();
		mStream = null;
		mAlive = false;
		mOpenFailed = false;
	}
}
