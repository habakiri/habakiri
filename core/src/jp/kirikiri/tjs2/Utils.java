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

public class Utils {

	public static String VariantToReadableString( final Variant val ) throws VariantException {
		return VariantToReadableString( val, 512 );
	}
	public static String VariantToReadableString( final Variant val, int maxlen ) throws VariantException {
		String ret = null;
		if( val == null || val.isVoid() ) {
			ret = new String( "(void)" );
		} else if( val.isInteger() ) {
			ret = new String( "(int)" + val.asString() );
		} else if( val.isReal() ) {
			ret = new String( "(real)" + val.asString() );
		} else if( val.isString() ) {
			ret = new String( "(string)\"" + LexBase.escapeC( val.asString() ) + "\"" );
		} else if( val.isOctet() ) {
			ret = new String( "(octet)<% " + Variant.OctetToListString( val.asOctet() ) + " %>" );
		} else if( val.isObject() ) {
			VariantClosure c = (VariantClosure)val.asObjectClosure();
			StringBuilder str = new StringBuilder(128);
			str.append( "(object)" );
			str.append( '(' );
			if( c.mObject != null ) {
				str.append( '[' );
				if( c.mObject instanceof NativeClass ) {
					str.append( ((NativeClass)c.mObject).getClassName() );
				} else if( c.mObject instanceof InterCodeObject ) {
					str.append( ((InterCodeObject)c.mObject).getName() );
				} else if( c.mObject instanceof CustomObject ) {
					String name = ((CustomObject)c.mObject).getClassNames();
					if( name != null ) str.append( name );
					else str.append( c.mObject.getClass().getName() );
				} else {
					str.append( c.mObject.getClass().getName() );
				}
				str.append( ']' );
			} else {
				str.append("0x00000000");
			}
			if( c.mObjThis != null ) {
				str.append( '[' );
				if( c.mObjThis instanceof NativeClass ) {
					str.append( ((NativeClass)c.mObjThis).getClassName() );
				} else if( c.mObjThis instanceof InterCodeObject ) {
					str.append( ((InterCodeObject)c.mObjThis).getName() );
				} else if( c.mObjThis instanceof CustomObject ) {
					String name = ((CustomObject)c.mObjThis).getClassNames();
					if( name != null ) str.append( name );
					else str.append( c.mObjThis.getClass().getName() );
				} else {
					str.append( c.mObjThis.getClass().getName() );
				}
				str.append( ']' );
			} else {
				str.append(":0x00000000");
			}
			str.append( ')' );
			ret = str.toString();
		} else { // native object ?
			ret = new String( "(octet) [" + val.getClass().getName() + "]" );
		}

		if( ret != null ) {
			if( ret.length() > maxlen ) {
				return ret.substring( 0, maxlen );
			} else {
				return ret;
			}
		}
		return new String("");
	}
	public static final String variantToExpressionString( final Variant val ) throws VariantException {
		// convert given variant to string which can be interpret as an expression.
		// this function does not convert objects ( returns empty string )
		if( val.isVoid() ) {
			return "void";
		} else if( val.isInteger() ) {
			return val.asString();
		} else if( val.isReal() ) {
			String s = Variant.realToHexString( val.asDouble() );
			return s + " /* " + val.asString() + " */";
		} else if( val.isString() ) {
			String s = LexBase.escapeC(val.asString());
			return "\"" + s + "\"";
		} else if( val.isOctet() ) {
			String s = Variant.octetToListString(val.asOctet());
			return "<%" + s + "%>";
		} else {
			return new String();
		}
	}
	public static String formatString(String format, Variant[] params) {
		final int count = params.length;
		Object[] args = new Object[count];
		for( int i = 0; i < count; i++ ) {
			args[i] = params[i].toJavaObject();
			if( args[i] instanceof String ) {
				final int length = ((String)args[i]).length();
				if( length == 1 ) {
					args[i] = Character.valueOf(((String)args[i]).charAt(0));
				}
			}
		}
		return String.format(format, args);
	}
}

