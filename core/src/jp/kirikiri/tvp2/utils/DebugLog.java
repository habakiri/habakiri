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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.ListIterator;

import jp.kirikiri.tjs2.ConsoleOutput;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.TVPSystem;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2env.ApplicationSystem;
import jp.kirikiri.tvp2env.DefaultLogger;

public class DebugLog {
	static private boolean QUEUEING_LOG = true;
	static private boolean LOG_TIMESTAMP = true;

	static class LogItem {
		public String Log;
		public String Time;
		public LogItem( final String log, final String time ) {
			Log = log;
			Time = time;
		}
	}
	private LinkedList<LogItem> mLogDeque;
	private int mLogMaxLines;

	private boolean mAutoLogToFileOnError;
	private boolean mAutoClearLogOnError;
	private boolean mLoggingToFile;
	private String mLogLocation;
	private String mNativeLogLocation;
	private String mDumpOutFileName;

	private LogStreamHolder mLogStreamHolder;

	private int mLogToFileRollBack;
	private StringBuilder mImportantLogs;
	private StringBuilder mWorkBuilder;
	private StringBuilder mLastLogWorkBuilder;
	private ConsoleOutput mOnLog;

	private Calendar mCalendar;
	private int mPrevHour;
	private int mPrevMinute;
	private int mPrevSecond;
	private String mTimeBuf;

	public DebugLog() {
		String tmp;
		tmp = TVP.Properties.getProperty("log_queueing", "true");
		if( "true".equalsIgnoreCase(tmp) || "yes".equalsIgnoreCase(tmp) ) {
			QUEUEING_LOG = true;
		} else {
			QUEUEING_LOG = false;
		}

		tmp = TVP.Properties.getProperty("log_timestamp", "true");
		if( "true".equalsIgnoreCase(tmp) || "yes".equalsIgnoreCase(tmp) ) {
			LOG_TIMESTAMP = true;
		} else {
			LOG_TIMESTAMP = false;
		}

		mLogDeque = new LinkedList<LogItem>();
		mAutoLogToFileOnError = true;
		tmp = TVP.Properties.getProperty("log_to_file_roll_back", "100");
		mLogToFileRollBack = Integer.valueOf(tmp);
		tmp = TVP.Properties.getProperty("log_max_lines", "2048");
		mLogMaxLines = Integer.valueOf(tmp);
		mImportantLogs = new StringBuilder(128);
		mWorkBuilder = new StringBuilder(128);
		mLastLogWorkBuilder = new StringBuilder(128);

		mLogStreamHolder = new LogStreamHolder();

		mCalendar = Calendar.getInstance();
		mPrevHour = -1;
		mPrevMinute = -1;
		mPrevSecond = -1;

		mOnLog = new DefaultLogger();
	}
	public boolean enableLogLoacation() {
		return mLogLocation != null && mLogLocation.length() != 0;
	}
	public String getNativeLogLocation() { return mNativeLogLocation; }

	public void addLog( final String line ) {
		addLog( line, false );
	}
	public void addImportantLog( final String line ) {
		addLog( line, true );
	}
	public String getImportantLog() {
		if(mImportantLogs==null) return "";
		return mImportantLogs.toString();
	}
	public void addLog( final String line, boolean appendtoimportant ) {
		// add a line to the log.
		// exceeded lines over TVPLogMaxLines are eliminated.
		// this function is not thread-safe ...

		if(mLogDeque==null) return; // log system is shuttingdown
		if(mImportantLogs==null) return; // log system is shuttingdown

		if( LOG_TIMESTAMP ) {
			mCalendar.setTimeInMillis( System.currentTimeMillis() );

			int h = mCalendar.get( Calendar.HOUR_OF_DAY );
			int m = mCalendar.get( Calendar.MINUTE );
			int s = mCalendar.get( Calendar.SECOND );
			if( mPrevSecond != s || mPrevMinute != m || mPrevHour != h ) {
				mTimeBuf = String.format("%02d:%02d:%02d", h, m, s );
				mPrevHour = h;
				mPrevMinute = m;
				mPrevSecond = s;
			}
		}
		if( QUEUEING_LOG ) mLogDeque.addLast(new LogItem(line, mTimeBuf) );

		if( appendtoimportant ) {
			if( LOG_TIMESTAMP ) mImportantLogs.append(mTimeBuf);
			mImportantLogs.append(" ! ");
			mImportantLogs.append(line);
			mImportantLogs.append("\n");
		}

		if( QUEUEING_LOG )  {
			while( mLogDeque.size() >= mLogMaxLines+100 ) {
				for( int i = 0; i < 100; i++ ) {
					mLogDeque.removeFirst();
				}
			}
		}

		if( LOG_TIMESTAMP ) {
			mWorkBuilder.append(mTimeBuf);
			mWorkBuilder.append(' ');
		}
		mWorkBuilder.append(line);
		String buf = mWorkBuilder.toString();
		mWorkBuilder.delete( 0, mWorkBuilder.length() );

		if( mOnLog != null ) {
			mOnLog.print(buf);
		}

		if(mLoggingToFile) TVP.LogStreamHolder.log(buf);
	}

	public void setLogLocation( final String loc ) throws TJSException {
		mLogLocation = TVP.StorageMediaManager.normalizeStorageName(loc,null);

		String nativepath = TVP.StorageMediaManager.getLocallyAccessibleName(mLogLocation);
		if( nativepath.length() == 0 ) {
			mNativeLogLocation = null;
			mLogLocation = null;
		} else {
			char last = nativepath.charAt(nativepath.length()-1);
			if( last != File.separatorChar ) {
				mNativeLogLocation = nativepath + File.separatorChar;
			} else {
				mNativeLogLocation = nativepath;
			}
		}
		mLogStreamHolder.reopen();

		// check force logging option
		String forcelog = TVP.Properties.getProperty("forcelog", "no" );
		if( "no".equals(forcelog) == false ) {
			if("yes".equals(forcelog)) {
				mLoggingToFile = false;
				startLogToFile(false);
			} else if( "clear".equals(forcelog) ) {
				mLoggingToFile = false;
				startLogToFile(true);
			}
		}
		String logerror = TVP.Properties.getProperty("logerror", "yes" );
		if( "yes".equals(logerror) == false ) {
			if( "no".equals(logerror) ) {
				mAutoClearLogOnError = false;
				mAutoLogToFileOnError = false;
			} else if( "clear".equals(logerror) ) {
				mAutoClearLogOnError = true;
				mAutoLogToFileOnError = true;
			}
		}
	}
	private static final String SEPARATOR = "\n------------------------------------------------------------------------------\n";
	void startLogToFile(boolean clear) {
		if( mImportantLogs == null ) return; // log system is shuttingdown

		if(mLoggingToFile) return; // already logging
		if(clear) mLogStreamHolder.clear();

		// log last lines
		mLogStreamHolder.log( mImportantLogs.toString() );
		mLogStreamHolder.log( SEPARATOR );

		String content = getLastLog(mLogToFileRollBack);
		mLogStreamHolder.log(content);

		mLoggingToFile = true;
	}

	private String getLastLog( int n ) {
		if(mLogDeque==null) return ""; // log system is shuttingdown

		int size = mLogDeque.size();
		if(n > size) n = size;
		if(n==0) return "";

		ListIterator<LogItem> iter = mLogDeque.listIterator(size-n);
		while( iter.hasNext() ) {
			LogItem item = iter.next();
			mLastLogWorkBuilder.append( item.Time );
			mLastLogWorkBuilder.append( ' ' );
			mLastLogWorkBuilder.append( item.Log );
			mLastLogWorkBuilder.append( '\n' );
		}
		String buf = mLastLogWorkBuilder.toString();
		mLastLogWorkBuilder.delete(0, mLastLogWorkBuilder.length() );

		return buf;
	}
	public void startDump() throws TJSException {
		String path = ApplicationSystem.getNativeAppPath();
		char last = path.charAt(path.length()-1);
		if( last != '/' && last != '\\' ) {
			path += File.separatorChar;
		}
		path += "dump.txt";
		mDumpOutFileName = path;
		try {
			TVP.DumpOutputStream = new FileOutputStream( path, true );
			byte[] buf = {(byte)0xff,(byte)0xfe};
			TVP.DumpOutputStream.write(buf);
		} catch( FileNotFoundException e ) {
			Message.throwExceptionMessage( Message.CannotFindStorage, e.getMessage() );
		} catch (IOException e) {
		}
	}
	public void endDump() {
		if( TVP.DumpOutputStream != null ) {
			try {
				TVP.DumpOutputStream.close();
			} catch (IOException e) {
			}
			TVP.DumpOutputStream = null;

			addLog( "Dumped to " + mDumpOutFileName );
		}
	}
	public void onError() {
		if( mAutoLogToFileOnError) startLogToFile(mAutoClearLogOnError);
		// if(TVP.MainForm!=null) TVP.MainForm.notifySystemError();
	}
	public String getLogLocation() {
		return mLogLocation;
	}
	public boolean getAutoLogToFileOnError() {
		return mAutoLogToFileOnError;
	}
	public void setAutoLogToFileOnError(boolean b) {
		mAutoLogToFileOnError = b;
	}
	public boolean getAutoClearLogOnError() {
		return mAutoClearLogOnError;
	}
	public void setAutoClearLogOnError(boolean b) {
		mAutoClearLogOnError = b;
	}
}
