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
package jp.kirikiri.tvp2env;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tvp2.TVP;

public class FileSelector {

	public static boolean selectFile( Dispatch2 params ) throws TJSException {
		Variant val = new Variant();

		JFileChooser filechooser = new JFileChooser();

		int hr;
		hr = params.propGet(Interface.MEMBERMUSTEXIST, "filter", val, params );
		if( hr >= 0 ) {
			ArrayList<String> filtterlist = new ArrayList<String>();
			if( val.isObject() != true ) {
				filtterlist.add( val.asString() );
			} else {
				Dispatch2 array = val.asObject();
				int count;
				Variant tmp = new Variant();
				hr = array.propGet( Interface.MEMBERMUSTEXIST, "count", tmp, array );
				if( hr >= 0 )
					count = tmp.asInteger();
				else
					count = 0;

				for( int i = 0; i < count; i++ ) {
					hr = array.propGetByNum( Interface.MEMBERMUSTEXIST, i, tmp, array );
					if( hr >= 0 ) {
						filtterlist.add( tmp.asString() );
					}
				}
			}
			final int count = filtterlist.size();
			StringBuilder builder = new StringBuilder(512); // description
			ArrayList<String> extlist = new ArrayList<String>();
			for( int i = 0; i < count; i++ ) {
				String fileter = filtterlist.get(i);
				String[] desc = fileter.split("|");
				if( desc.length >= 2 ) {
					builder.append(desc[0]);
					String[] ext = desc[1].split(";");
					final int extcount = ext.length;
					for( int j = 0; j < extcount; j++ ) {
						int last = ext[j].lastIndexOf('.');
						if( last != -1 ) {
							String e = ext[j].substring(last);
							if( e.indexOf('*') == -1 ) {
								extlist.add(e);
							}
						}
					}
				}
			}
			if( extlist.size() > 0 ) {
				FileNameExtensionFilter filefilter = new FileNameExtensionFilter(builder.toString(),(String[]) extlist.toArray());
				filechooser.setFileFilter(filefilter);
			}
			builder = null;
			filtterlist = null;
			extlist = null;
		}
		/*
		int filterIndex = 0;
		hr = params.propGet(Interface.MEMBERMUSTEXIST, "filterIndex", val, params );
		if( hr >= 0 ) {
			filterIndex = val.asInteger();
		}
		*/

		String filename;
		hr = params.propGet(Interface.MEMBERMUSTEXIST, "name", val, params );
		if( hr >= 0 ) {
			String lname = val.asString();
			if( lname != null && lname.length() != 0 ) {
				lname = TVP.StorageMediaManager.normalizeStorageName(lname,null);
				filename = TVP.StorageMediaManager.getLocalName(lname);
				filechooser.setSelectedFile( new File(filename) );
			}
		}

		hr = params.propGet(Interface.MEMBERMUSTEXIST, "initialDir", val, params );
		if( hr >= 0 ) {
			String lname = val.asString();
			if( lname != null && lname.length() != 0 ) {
				lname = TVP.StorageMediaManager.normalizeStorageName(lname,null);
				String initDir = TVP.StorageMediaManager.getLocalName(lname);
				filechooser.setCurrentDirectory( new File(initDir) );
			}
		}

		hr = params.propGet(Interface.MEMBERMUSTEXIST, "initialDir", val, params );
		if( hr >= 0 ) {
			String title = val.asString();
			filechooser.setDialogTitle(title);
		}
		boolean issave = false;
		hr = params.propGet(Interface.MEMBERMUSTEXIST, "save", val, params );
		if( hr >= 0 ) {
			issave = val.asBoolean();
		}
		/*
		hr = params.propGet(Interface.MEMBERMUSTEXIST, "defaultExt", val, params );
		if( hr >= 0 ) {
			String ext = val.asString();
		}
		*/


		int selected;
		if( issave ) {
			selected = filechooser.showSaveDialog(null);
		} else {
			selected = filechooser.showOpenDialog(null);
		}
		if( selected == JFileChooser.APPROVE_OPTION ) {
			File file = filechooser.getSelectedFile();

			// file name
			filename = TVP.StorageMediaManager.normalizeStorageName( file.getAbsolutePath(), null );
			val.set(filename);
			params.propSet( Interface.MEMBERENSURE, "name", val, params);
			return true;
		}
		return false;
	}
}
