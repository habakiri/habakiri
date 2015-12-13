/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * 楓 software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * ソースコード形式かバイナリ形式か、変更するかしないかを問わず、以下の条件を満
 * たす場合に限り、再頒布および使用が許可されます。
 *
 * ・ソースコードを再頒布する場合、上記の著作権表示、本条件一覧、および下記免責
 *   条項を含めること。
 * ・バイナリ形式で再頒布する場合、頒布物に付属のドキュメント等の資料に、上記の
 *   著作権表示、本条件一覧、および下記免責条項を含めること。
 * ・書面による特別の許可なしに、本ソフトウェアから派生した製品の宣伝または販売
 *   促進に、組織の名前またはコントリビューターの名前を使用してはならない。
 *
 * 本ソフトウェアは、著作権者およびコントリビューターによって「現状のまま」提供
 * されており、明示黙示を問わず、商業的な使用可能性、および特定の目的に対する適
 * 合性に関する暗黙の保証も含め、またそれに限定されない、いかなる保証もありませ
 * ん。著作権者もコントリビューターも、事由のいかんを問わず、損害発生の原因いか
 * んを問わず、かつ責任の根拠が契約であるか厳格責任であるか（過失その他の）不法
 * 行為であるかを問わず、仮にそのような損害が発生する可能性を知らされていたとし
 * ても、本ソフトウェアの使用によって発生した（代替品または代用サービスの調達、
 * 使用の喪失、データの喪失、利益の喪失、業務の中断も含め、またそれに限定されな
 * い）直接損害、間接損害、偶発的な損害、特別損害、懲罰的損害、または結果損害に
 * ついて、一切責任を負わないものとします。
 ******************************************************************************
 * 本ソフトウェアは、吉里吉里2 ( http://kikyou.info/tvp/ ) のソースコードをJava
 * に書き換えたものを一部使用しています。
 * 吉里吉里2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
 ******************************************************************************
 */
package jp.kirikiri.tjs2;

/**
 * ディスアセンブラ
 * TJS2 のバイトコードを可読可能なアセンブリソースに変換する
  */
public class Disassembler {

	private short[] mCode;
	private Variant[] mData; // バイナリの固まりのバイトコードからも取得できるように、クラスでラップした方がいいかも
	private SourceCodeAccessor mAccessor;

	public Disassembler( short[] ca, Variant[] da, SourceCodeAccessor a ) {
		mCode = ca;
		mData = da;
		mAccessor = a;
	}
	public void set( short[] ca, Variant[] da, SourceCodeAccessor a  ) {
		mCode = ca;
		mData = da;
		mAccessor = a;
	}
	public void clear() {
		mCode = null;
		mData = null;
		mAccessor = null;
	}

	private void outputFuncSrc( final String msg, final String name, int line, ScriptBlock data) {
		String buf;
		if(line >= 0)
			buf = String.format("#%s(%d) %s", name, line+1, msg );
		else
			buf = String.format("#%s %s", name, msg);

		TJS.outputToConsole(buf);
	}
	private void outputFunc( final String msg, final String comment, int addr, int codestart, int size, ScriptBlock data ) {
		String buf = String.format( "%08d %s", addr, msg );
		if( comment != null ) {
			buf += "\t// " + comment;
		}
		TJS.outputToConsole(buf);
	}
	public void disassemble(ScriptBlock data, int start, int end) throws VariantException {
		// dis-assemble the intermediate code.
		// "output_func" points a line output function.
		//String  s;

		String msg;
		String com;

		int prevline = -1;
		int curline = -1;

		if(end <= 0) end = mCode.length;
		if(end > mCode.length ) end = mCode.length;

		for( int i = start; i < end; ) {
			msg = null;
			com = null;
			int size;
			int srcpos = mAccessor.codePosToSrcPos(i);
			int line = mAccessor.srcPosToLine(srcpos);

			// output source lines as comment
			if( curline == -1 || curline <= line ) {
				if(curline == -1) curline = line;
				int nl = line - curline;
				while( curline <= line ) {
					if( nl<3 || nl >= 3 && line-curline <= 2) {
						//int len;
						String src = mAccessor.getLine(curline);
						outputFuncSrc( src, "", curline, data);
						curline++;
					} else {
						curline = line - 2;
					}
				}
			} else if(prevline != line) {
				String src = mAccessor.getLine(line);
				outputFuncSrc( src, "", line, data);
			}
			prevline = line;

			// decode each instructions
			switch( mCode[i] ) {
			case VM_NOP:
				msg = "nop";
				size = 1;
				break;

			case VM_NF:
				msg = "nf";
				size = 1;
				break;

			case VM_CONST:
				msg = String.format( "const %%%d, *%d", mCode[i+1], mCode[i+2] );
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+2], Utils.VariantToReadableString( mData[ mCode[i+2] ] ) );
				}
				size = 3;
				break;

				// instructions that
				// 1. have two operands that represent registers.
				// 2. do not have property access variants.
			case VM_CP:
				msg = String.format( "cp %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_CEQ:
				msg = String.format( "ceq %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_CDEQ:
				msg = String.format( "cdeq %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_CLT:
				msg = String.format( "clt %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_CGT:
				msg = String.format( "cgt %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_CHKINS:
				msg = String.format( "chkins %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;

			// instructions that
			// 1. have two operands that represent registers.
			// 2. have property access variants
			case VM_LOR:
				msg = String.format( "lor %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_LOR+1:
				msg = String.format( "lorpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_LOR+2:
				msg = String.format( "lorpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_LOR+3:
				msg = String.format( "lorp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_LAND:
				msg = String.format( "land %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_LAND+1:
				msg = String.format( "landpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_LAND+2:
				msg = String.format( "landpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_LAND+3:
				msg = String.format( "landp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_BOR:
				msg = String.format( "bor %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_BOR+1:
				msg = String.format( "borpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_BOR+2:
				msg = String.format( "borpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_BOR+3:
				msg = String.format( "borp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_BXOR:
				msg = String.format( "bxor %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_BXOR+1:
				msg = String.format( "bxorpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_BXOR+2:
				msg = String.format( "bxorpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_BXOR+3:
				msg = String.format( "bxorp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_BAND:
				msg = String.format( "band %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_BAND+1:
				msg = String.format( "bandpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_BAND+2:
				msg = String.format( "bandpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_BAND+3:
				msg = String.format( "bandp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_SAR:
				msg = String.format( "sar %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_SAR+1:
				msg = String.format( "sarpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_SAR+2:
				msg = String.format( "sarpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_SAR+3:
				msg = String.format( "sarp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_SAL:
				msg = String.format( "sal %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_SAL+1:
				msg = String.format( "salpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_SAL+2:
				msg = String.format( "salpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_SAL+3:
				msg = String.format( "salp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_SR:
				msg = String.format( "sr %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_SR+1:
				msg = String.format( "srpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_SR+2:
				msg = String.format( "srpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_SR+3:
				msg = String.format( "srp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_ADD:
				msg = String.format( "add %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_ADD+1:
				msg = String.format( "addpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_ADD+2:
				msg = String.format( "addpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_ADD+3:
				msg = String.format( "addp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_SUB:
				msg = String.format( "sub %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_SUB+1:
				msg = String.format( "subpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_SUB+2:
				msg = String.format( "subpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_SUB+3:
				msg = String.format( "subp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_MOD:
				msg = String.format( "mod %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_MOD+1:
				msg = String.format( "modpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_MOD+2:
				msg = String.format( "modpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_MOD+3:
				msg = String.format( "modp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_DIV:
				msg = String.format( "div %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_DIV+1:
				msg = String.format( "divpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_DIV+2:
				msg = String.format( "divpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_DIV+3:
				msg = String.format( "divp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_IDIV:
				msg = String.format( "idiv %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_IDIV+1:
				msg = String.format( "idivpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_IDIV+2:
				msg = String.format( "idivpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_IDIV+3:
				msg = String.format( "idivp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM
			case VM_MUL:
				msg = String.format( "mul %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;
			case VM_MUL+1:
				msg = String.format( "mulpd %%%d, %%%d.*%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				if( mData != null ) {
					com = String.format("*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				size = 5;
				break;
			case VM_MUL+2:
				msg = String.format( "mulpi %%%d, %%%d.%%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3], mCode[i+4]);
				size = 5;
				break;
			case VM_MUL+3:
				msg = String.format( "mulp %%%d, %%%d, %%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			// OP2_DISASM

			// instructions that have one operand which represent a register,
			// except for inc, dec
			case VM_TT:			msg = String.format( "tt %%%d", mCode[i+1] ); size = 2;		break;
			case VM_TF:			msg = String.format( "tf %%%d", mCode[i+1] ); size = 2;		break;
			case VM_SETF:		msg = String.format( "setf %%%d", mCode[i+1] ); size = 2;	break;
			case VM_SETNF:		msg = String.format( "setnf %%%d", mCode[i+1] ); size = 2;	break;
			case VM_LNOT:		msg = String.format( "lnot %%%d", mCode[i+1] ); size = 2;	break;
			case VM_BNOT:		msg = String.format( "bnot %%%d", mCode[i+1] ); size = 2;	break;
			case VM_ASC:		msg = String.format( "asc %%%d", mCode[i+1] ); size = 2;	break;
			case VM_CHR:		msg = String.format( "chr %%%d", mCode[i+1] ); size = 2;	break;
			case VM_NUM:		msg = String.format( "num %%%d", mCode[i+1] ); size = 2;	break;
			case VM_CHS:		msg = String.format( "chs %%%d", mCode[i+1] ); size = 2;	break;
			case VM_CL:			msg = String.format( "cl %%%d", mCode[i+1] ); size = 2;		break;
			case VM_INV:		msg = String.format( "inv %%%d", mCode[i+1] ); size = 2;	break;
			case VM_CHKINV:		msg = String.format( "chkinv %%%d", mCode[i+1] ); size = 2;	break;
			case VM_TYPEOF:		msg = String.format( "typeof %%%d", mCode[i+1] ); size = 2;	break;
			case VM_EVAL:		msg = String.format( "eval %%%d", mCode[i+1] ); size = 2;	break;
			case VM_EEXP:		msg = String.format( "eexp %%%d", mCode[i+1] ); size = 2;	break;
			case VM_INT:		msg = String.format( "int %%%d", mCode[i+1] ); size = 2;	break;
			case VM_REAL:		msg = String.format( "real %%%d", mCode[i+1] ); size = 2;	break;
			case VM_STR:		msg = String.format( "str %%%d", mCode[i+1] ); size = 2;	break;
			case VM_OCTET:		msg = String.format( "octet %%%d", mCode[i+1] ); size = 2;	break;

			case VM_CCL:
				msg = String.format( "ccl %%%d-%%%d", mCode[i+1], mCode[i+1] + mCode[i+2] -1 );
				size = 3;
				break;

			// inc and dec
			case VM_INC:
				msg = String.format( "inc %%%d", mCode[i+1] );
				size = 2;
				break;
			case VM_INC+1:
				msg = String.format("incpd %%%d, %%%d.*%d", mCode[i+1], mCode[i+2], mCode[i+3] );
				if( mData != null ) {
					com = String.format( "*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[mCode[i+3]] ) );
				}
				size = 4;
				break;
			case VM_INC+2:
				msg = String.format( "incpi %%%d, %%%d.%%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			case VM_INC+3:
				msg = String.format( "incp %%%d, %%%d", mCode[i+1], mCode[i+2]);
				size = 3;
				break;
			// inc and dec
			case VM_DEC:
				msg = String.format( "dec %%%d", mCode[i+1] );
				size = 2;
				break;
			case VM_DEC+1:
				msg = String.format( "decpd %%%d, %%%d.*%d", mCode[i+1], mCode[i+2], mCode[i+3] );
				if( mData != null ) {
					com = String.format( "*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[mCode[i+3]] ) );
				}
				size = 4;
				break;
			case VM_DEC+2:
				msg = String.format( "decpi %%%d, %%%d.%%%d", mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;
			case VM_DEC+3:
				msg = String.format( "decp %%%d, %%%d", mCode[i+1], mCode[i+2]);
				size = 3;
				break;

			// instructions that have one operand which represents code area
			case VM_JF:		msg = String.format( "jf %09d", mCode[i+1] + i); size = 2;		break;
			case VM_JNF:	msg = String.format( "jnf %09d", mCode[i+1] + i); size = 2;		break;
			case VM_JMP:	msg = String.format( "jmp %09d", mCode[i+1] + i); size = 2;		break;

			case VM_CALL:
			case VM_CALLD:
			case VM_CALLI:
			case VM_NEW:
			{
				// function call variants
				msg = String.format(
					mCode[i] == VM_CALL  ? "call %%%d, %%%d(" :
					mCode[i] == VM_CALLD ? "calld %%%d, %%%d.*%d(" :
					mCode[i] == VM_CALLI ? "calli %%%d, %%%d.%%%d(" :
											 "new %%%d, %%%d(",
					mCode[i+1], mCode[i+2], mCode[i+3]);
				int st; // start of arguments
				if( mCode[i] == VM_CALLD || mCode[i] == VM_CALLI)
					st = 5;
				else
					st = 4;
				int num = mCode[i+st-1];     // st-1 = argument count
				boolean first = true;
				String buf = null;
				int c = 0;
				if(num == -1) {
					// omit arg
					size = st;
					msg += "...";
				} else if(num == -2) {
					// expand arg
					st++;
					num = mCode[i+st-1];
					size = st + num * 2;
					for( int j = 0; j < num; j++ ) {
						if(!first) msg += ", ";
						first = false;
						switch(mCode[i+st+j*2] )
						{
						case fatNormal:
							buf = String.format( "%%%d", mCode[i+st+j*2+1] );
							break;
						case fatExpand:
							buf = String.format( "%%%d*", mCode[i+st+j*2+1] );
							break;
						case fatUnnamedExpand:
							buf = "*";
							break;
						}
						msg += buf;
					}
				} else {
					// normal operation
					size = st + num;
					while( num > 0 ) {
						if(!first) msg += ", ";
						first = false;
						buf = String.format( "%%%d", mCode[i+c+st] );
						c++;
						msg += buf;
						num--;
					}
				}

				msg += ")";
				if( mData != null && mCode[i] == VM_CALLD ) {
					com = String.format( "*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[ mCode[i+3] ] ) );
				}
				break;
			}

			case VM_GPD:
			case VM_GPDS:
				// property get direct
				msg = String.format(
					mCode[i] == VM_GPD ? "gpd %%%d, %%%d.*%d" : "gpds %%%d, %%%d.*%d",
					mCode[i+1], mCode[i+2], mCode[i+3]);
				if( mData != null ) {
					com = String.format( "*%d = %s", mCode[i+3], Utils.VariantToReadableString(mData[mCode[i+3]]) );
				}
				size = 4;
				break;

			case VM_SPD:
			case VM_SPDE:
			case VM_SPDEH:
			case VM_SPDS:
				// property set direct
				msg = String.format(
					mCode[i] == VM_SPD ? "spd %%%d.*%d, %%%d":
					mCode[i] == VM_SPDE? "spde %%%d.*%d, %%%d":
					mCode[i] == VM_SPDEH?"spdeh %%%d.*%d, %%%d":
												"spds %%%d.*%d, %%%d",
					mCode[i+1], mCode[i+2], mCode[i+3]);
				if( mData != null ) {
					com = String.format( "*%d = %s", mCode[i+2], Utils.VariantToReadableString( mData[mCode[i+2]]) );
				}

				size = 4;
				break;


			case VM_GPI:
			case VM_GPIS:
				// property get indirect
				msg = String.format(
					mCode[i] == VM_GPI ?  "gpi %%%d, %%%d.%%%d":
											 "gpis %%%d, %%%d.%%%d",
					mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;


			case VM_SPI:
			case VM_SPIE:
			case VM_SPIS:
				// property set indirect
				msg = String.format(
					mCode[i] == VM_SPI  ?"spi %%%d.%%%d, %%%d":
					mCode[i] == VM_SPIE ?"spie %%%d.%%%d, %%%d":
											"spis %%%d.%%%d, %%%d",
					mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;


			case VM_SETP:
				// property set
				msg = String.format( "setp %%%d, %%%d", mCode[i+1], mCode[i+2]);
				size = 3;
				break;

			case VM_GETP:
				// property get
				msg = String.format( "getp %%%d, %%%d", mCode[i+1], mCode[i+2]);
				size = 3;
				break;


			case VM_DELD:
			case VM_TYPEOFD:
				// member delete direct / typeof direct
				msg = String.format(
					mCode[i] == VM_DELD   ?"deld %%%d, %%%d.*%d":
											  "typeofd %%%d, %%%d.*%d",
					mCode[i+1], mCode[i+2], mCode[i+3]);
				if( mData != null ) {
					com= String.format( "*%d = %s", mCode[i+3], Utils.VariantToReadableString( mData[mCode[i+3]]));
				}
				size = 4;
				break;

			case VM_DELI:
			case VM_TYPEOFI:
				// member delete indirect / typeof indirect
				msg = String.format(
					mCode[i] == VM_DELI ? "deli %%%d, %%%d.%%%d" : "typeofi %%%d, %%%d.%%%d",
					mCode[i+1], mCode[i+2], mCode[i+3]);
				size = 4;
				break;

			case VM_SRV:
				// set return value
				msg = String.format("srv %%%d", mCode[i+1] );
				size = 2;
				break;

			case VM_RET:
				// return
				msg = "ret";
				size = 1;
				break;

			case VM_ENTRY:
				// enter try-protected block
				msg = String.format("entry %09d, %%%d", mCode[i+1] + i, mCode[i+2] );
				size = 3;
				break;

			case VM_EXTRY:
				// exit from try-protected block
				msg = "extry";
				size = 1;
				break;

			case VM_THROW:
				msg = String.format( "throw %%%d", mCode[i+1] );
				size = 2;
				break;

			case VM_CHGTHIS:
				msg = String.format( "chgthis %%%d, %%%d", mCode[i+1], mCode[i+2]);
				size = 3;
				break;

			case VM_GLOBAL:
				msg = String.format("global %%%d", mCode[i+1]);
				size = 2;
				break;

			case VM_ADDCI:
				msg = String.format("addci %%%d, %%%d", mCode[i+1], mCode[i+2] );
				size = 3;
				break;

			case VM_REGMEMBER:
				msg = "regmember";
				size = 1;
				break;

			case VM_DEBUGGER:
				msg = "debugger";
				size = 1;
				break;

			default:
				msg = String.format("unknown instruction %d", mCode[i] );
				size = 1;
				break;
			} /* switch */

			outputFunc( msg, com, i, i, size, data);  // call the callback

			i+=size;
		}
	}

	// VMCodes
	private static final int
		VM_NOP		= 0,
		VM_CONST	= 1,
		VM_CP		= 2,
		VM_CL		= 3,
		VM_CCL		= 4,
		VM_TT		= 5,
		VM_TF		= 6,
		VM_CEQ		= 7,
		VM_CDEQ		= 8,
		VM_CLT		= 9,
		VM_CGT		= 10,
		VM_SETF		= 11,
		VM_SETNF	= 12,
		VM_LNOT		= 13,
		VM_NF		= 14,
		VM_JF		= 15,
		VM_JNF		= 16,
		VM_JMP		= 17,

		VM_INC		= 18,
		VM_INCPD	= 19,
		VM_INCPI	= 20,
		VM_INCP		= 21,
		VM_DEC		= 22,
		VM_DECPD	= 23,
		VM_DECPI	= 24,
		VM_DECP		= 25,
		VM_LOR		= 26,
		VM_LORPD	= 27,
		VM_LORPI	= 28,
		VM_LORP		= 29,
		VM_LAND		= 30,
		VM_LANDPD	= 31,
		VM_LANDPI	= 32,
		VM_LANDP	= 33,
		VM_BOR		= 34,
		VM_BORPD	= 35,
		VM_BORPI	= 36,
		VM_BORP		= 37,
		VM_BXOR		= 38,
		VM_BXORPD	= 39,
		VM_BXORPI	= 40,
		VM_BXORP	= 41,
		VM_BAND		= 42,
		VM_BANDPD	= 43,
		VM_BANDPI	= 44,
		VM_BANDP	= 45,
		VM_SAR		= 46,
		VM_SARPD	= 47,
		VM_SARPI	= 48,
		VM_SARP		= 49,
		VM_SAL		= 50,
		VM_SALPD	= 51,
		VM_SALPI	= 52,
		VM_SALP		= 53,
		VM_SR		= 54,
		VM_SRPD		= 55,
		VM_SRPI		= 56,
		VM_SRP		= 57,
		VM_ADD		= 58,
		VM_ADDPD	= 59,
		VM_ADDPI	= 60,
		VM_ADDP		= 61,
		VM_SUB		= 62,
		VM_SUBPD	= 63,
		VM_SUBPI	= 64,
		VM_SUBP		= 65,
		VM_MOD		= 66,
		VM_MODPD	= 67,
		VM_MODPI	= 68,
		VM_MODP		= 69,
		VM_DIV		= 70,
		VM_DIVPD	= 71,
		VM_DIVPI	= 72,
		VM_DIVP		= 73,
		VM_IDIV		= 74,
		VM_IDIVPD	= 75,
		VM_IDIVPI	= 76,
		VM_IDIVP	= 77,
		VM_MUL		= 78,
		VM_MULPD	= 79,
		VM_MULPI	= 80,
		VM_MULP		= 81,

		VM_BNOT		= 82,
		VM_TYPEOF	= 83,
		VM_TYPEOFD	= 84,
		VM_TYPEOFI	= 85,
		VM_EVAL		= 86,
		VM_EEXP		= 87,
		VM_CHKINS	= 88,
		VM_ASC		= 89,
		VM_CHR		= 90,
		VM_NUM		= 91,
		VM_CHS		= 92,
		VM_INV		= 93,
		VM_CHKINV	= 94,
		VM_INT		= 95,
		VM_REAL		= 96,
		VM_STR		= 97,
		VM_OCTET	= 98,
		VM_CALL		= 99,
		VM_CALLD	= 100,
		VM_CALLI	= 101,
		VM_NEW		= 102,
		VM_GPD		= 103,
		VM_SPD		= 104,
		VM_SPDE		= 105,
		VM_SPDEH	= 106,
		VM_GPI		= 107,
		VM_SPI		= 108,
		VM_SPIE		= 109,
		VM_GPDS		= 110,
		VM_SPDS		= 111,
		VM_GPIS		= 112,
		VM_SPIS		= 113,
		VM_SETP		= 114,
		VM_GETP		= 115,
		VM_DELD		= 116,
		VM_DELI		= 117,
		VM_SRV		= 118,
		VM_RET		= 119,
		VM_ENTRY	= 120,
		VM_EXTRY	= 121,
		VM_THROW	= 122,
		VM_CHGTHIS	= 123,
		VM_GLOBAL	= 124,
		VM_ADDCI	= 125,
		VM_REGMEMBER= 126,
		VM_DEBUGGER	= 127,
		__VM_LAST	= 128;

	// FuncArgType
	static private final int
		fatNormal = 0,
		fatExpand = 1,
		fatUnnamedExpand = 2;
}
