package jp.kirikiri.tjs2.translate;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import jp.kirikiri.tjs2.CompileException;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.IntVector;
import jp.kirikiri.tjs2.InterCodeGenerator;
import jp.kirikiri.tjs2.InterCodeObject;
import jp.kirikiri.tjs2.SourceCodeAccessor;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tjs2.VariantException;


/**
 * ディスJavaコンパイラ
 * TJS2 のバイトコードをコンパイル可能なJavaソースに変換する
  */
public class JavaCodeGenerator {

	private static final byte TYPE_VOID = 0;
	private static final byte TYPE_OBJECT = 1;
	private static final byte TYPE_INTER_OBJECT = 2;
	private static final byte TYPE_STRING = 3;
	private static final byte TYPE_OCTET = 4;
	private static final byte TYPE_REAL = 5;
	private static final byte TYPE_INTEGER = 8;
	private static final byte TYPE_INTER_GENERATOR = 10; // temporary
	private static final byte TYPE_NULL_CLOSURE = 11;
	private static final byte TYPE_UNKNOWN = -1;

	static class ExceptionData {
		public int mCatchIp;
		public int mExobjReg;
		public ExceptionData( int catchip, int reg ) {
			mCatchIp = catchip;
			mExobjReg = reg;
		}
	}
	private ArrayList<String> mSourceCodes;
	private ArrayList<ExceptionData> mExceptionDataStack;
	private short[] mCode;
	private Variant[] mData;
	private SourceCodeAccessor mAccessor;
	private int mFuncDeclUnnamedArgArrayBase;

	public JavaCodeGenerator( short[] ca, Variant[] da, SourceCodeAccessor a ) {
		mCode = ca;
		mData = da;
		mAccessor = a;
		mExceptionDataStack = new ArrayList<ExceptionData>();
		mSourceCodes = new ArrayList<String>();
	}
	public void set( short[] ca, Variant[] da, SourceCodeAccessor a  ) {
		mCode = ca;
		mData = da;
		mAccessor = a;
		mExceptionDataStack = new ArrayList<ExceptionData>();
		mSourceCodes = new ArrayList<String>();
	}
	/*
	public void clear() {
		mCode = null;
		mData = null;
		mAccessor = null;
	}
	*/
	private int findJumpTarget( int[] array, int pos ) {
		final int count = array.length;
		for( int i = 0; i < count; i++ ) {
			if( array[i] == pos ) {
				return i + 1;
			}
		}
		return -1;
	}

	private void outputFuncSrc( final String msg, final String name, int line ) {
		String buf;
		if(line >= 0)
			buf = String.format("// #%s(%d) %s", name, line+1, msg );
		else
			buf = String.format("// #%s %s", name, msg);

		mSourceCodes.add( buf );
	}
	private final byte getType( Variant v ) {
		Object o = v.toJavaObject();
		if( o == null ) {
			return TYPE_VOID;
		} else if( o instanceof String ) {
			return TYPE_STRING;
		} else if( o instanceof Integer ) {
			return TYPE_INTEGER;
		} else if( o instanceof Double ) {
			return TYPE_REAL;
		} else if( o instanceof VariantClosure ) {
			VariantClosure clo = (VariantClosure)o;
			Dispatch2 dsp = clo.mObject;
			if( dsp instanceof InterCodeObject ) {
				return TYPE_INTER_OBJECT;
			} else {
				if( dsp == null ) return TYPE_NULL_CLOSURE;
				return TYPE_OBJECT;
			}
		} else if( o instanceof InterCodeGenerator ) {
			return TYPE_INTER_GENERATOR;
		} else if( o instanceof ByteBuffer ) {
			return TYPE_OCTET;
		} else if( o instanceof Long ) {
			return TYPE_INTEGER;
		}
		return TYPE_UNKNOWN;
	}
	private String getRegisterName( int v ) {
		if( v < 0 ) {
			if( v == -1 ) {
				return "varthis";
			} else if( v == -2 ) {
				return "this_proxy";
			} else {
				v += 3;
				v = -v;
				return "args[" + v + "]";
			}
		} else {
			return "r" + v;
		}
	}
	/*
	private String getDataToVariant( Variant v ) throws CompileException {
		Object o = v.toJavaObject();
		if( o == null ) {
			return "new Variant()";
		} else if( o instanceof String ) {
			return "new Variant( \""+((String)o).toString()+"\")";
		} else if( o instanceof Integer ) {
			return "new Variant( "+((Integer)o).toString()+" )";
		} else if( o instanceof Double ) {
			return "new Variant( "+((Double)o).toString()+" )";
		} else if( o instanceof VariantClosure ) {
			VariantClosure clo = (VariantClosure)o;
			Dispatch2 dsp = clo.mObject;
			if( dsp instanceof InterCodeObject ) {
				throw new CompileException("非サポートの定数形式");
			} else {
				throw new CompileException("非サポートの定数形式");
			}
		} else if( o instanceof InterCodeGenerator ) {
			throw new CompileException("非サポートの定数形式");
		} else if( o instanceof ByteBuffer ) {
			throw new CompileException("非サポートの定数形式");
		} else if( o instanceof Long ) {
			return "new Variant( "+((Long)o).toString()+" )";
		}
		throw new CompileException("非サポートの定数形式");
	}*/
	private String getDataToStrOrNum( Variant v ) throws CompileException {
		Object o = v.toJavaObject();
		if( o == null ) {
			return "null";
		} else if( o instanceof String ) {
			return "\"" + (String)o + "\"";
		} else if( o instanceof Integer ) {
			return ((Integer)o).toString();
		} else if( o instanceof Double ) {
			return ((Double)o).toString();
		} else if( o instanceof VariantClosure ) {
			throw new CompileException("非サポートの定数形式");
		} else if( o instanceof InterCodeGenerator ) {
			throw new CompileException("非サポートの定数形式");
		} else if( o instanceof ByteBuffer ) {
			throw new CompileException("非サポートの定数形式");
		} else if( o instanceof Long ) {
			return ((Long)o).toString();
		}
		throw new CompileException("非サポートの定数形式");
	}
	public void genFunCall( final int variable, final int frame, final int declArgCount, final int declCollapseBase ) {
		mSourceCodes.add( "protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {" );
		int num_alloc = variable;
		if( num_alloc == 0 ) {
			mSourceCodes.add( "Variant[] args = TJS.NULL_ARG;" );
		} else {
			mSourceCodes.add( "Variant[] args = new Variant["+num_alloc+"];" );
			for( int i = 0; i < num_alloc; i++ ) {
				mSourceCodes.add( "args["+i+"] = new Variant();" );
			}
		}
		mSourceCodes.add( "Variant this_proxy;" );
		mSourceCodes.add( "if( objthis != null ) {" );
		mSourceCodes.add( "ObjectProxy proxy = new ObjectProxy();" );
		mSourceCodes.add( "proxy.setObjects( objthis, ScriptsClass.getGlobal() );" );
		mSourceCodes.add( "this_proxy = new Variant( proxy );" );
		mSourceCodes.add( "} else {" );
		mSourceCodes.add( "Dispatch2 global = ScriptsClass.getGlobal();" );
		mSourceCodes.add( "this_proxy = new Variant(global,global);" );
		mSourceCodes.add( "}" );

		mSourceCodes.add( "Variant varthis = new Variant(objthis,objthis);" );
		if( num_alloc > 0 ) {
			mSourceCodes.add( "final int numargs = param != null ? param.length : 0;" );
			mSourceCodes.add( "if( numargs >= "+declArgCount+" ) {" );
			if( declArgCount != 0 ) {
				int r = 0;
				int n = declArgCount;
				int argOffset = 0;
				while( true ) {
					mSourceCodes.add( "args["+r+"].set( param["+argOffset+"] );" );
					argOffset++;
					n--;
					if( n == 0 ) break;
					r++;
				}
			}
			mSourceCodes.add( "} else {" );
			mSourceCodes.add( "int i;" );
			mSourceCodes.add( "for( i = 0; i < numargs; i++ ) {" );
			mSourceCodes.add( "args[i].set( param[i] );" );
			mSourceCodes.add( "}" );
			mSourceCodes.add( "for( ; i < "+declArgCount+"; i++ ) {" );
			mSourceCodes.add( "args[i].clear();" );
			mSourceCodes.add( "}" );
			mSourceCodes.add( "}" );

			if( declCollapseBase >= 0 ) {
				int r = declCollapseBase;
				mSourceCodes.add( "{" );
				mSourceCodes.add( "Dispatch2 dsp = TJS.createArrayObject();" );
				mSourceCodes.add( "args["+r+"].set(dsp, dsp);" );
				mSourceCodes.add( "if( numargs > "+declCollapseBase+" ) {" );
				mSourceCodes.add( "for( int c = 0, i = "+declCollapseBase+"; i < numargs; i++, c++) {" );
				mSourceCodes.add( "dsp.propSetByNum(0, c, param[i], dsp);" );
				mSourceCodes.add( "}" );
				mSourceCodes.add( "}" );
			}
		}
		mSourceCodes.add( "executeCode( varthis, this_proxy, args, result, objthis );" );
		mSourceCodes.add( "return Error.S_OK;" );
		mSourceCodes.add( "}" );
	}
	private void genReg( int count ) {
		mSourceCodes.add( "private void executeCode( Variant varthis, Variant this_proxy, Variant[] args, Variant result, Dispatch2 objthis) throws VariantException, TJSException {" );
		count++;
		for( int i = 0; i < count; i++ ) {
			mSourceCodes.add( "Variant " + getRegisterName(i) + " = new Variant();" );
		}
	}
	public void generate( int start, int end, int funcbase, int framecount ) throws VariantException, CompileException {
		genReg( framecount );
		// dis-assemble the intermediate code.
		// "output_func" points a line output function.
		//String  s;
		mFuncDeclUnnamedArgArrayBase = funcbase;

		String msg;

		int prevline = -1;
		int curline = -1;

		short[] ca = mCode;
		if(end <= 0) end = ca.length;
		if(end > ca.length ) end = ca.length;

		IntVector target = checkJumpCode( start, end );
		int[] jumpaddr = null;
		int jumpcount = 0;
		if( target.size() > 0 ) {
			//target.add( Integer.MAX_VALUE );
			jumpaddr = target.toArray();
			jumpcount = jumpaddr.length;
			// ジャンプする可能性がある
			mSourceCodes.add( "boolean flag = false;");
			mSourceCodes.add( "boolean loop = true;");
			mSourceCodes.add( "int goto_target = 0;");
			mSourceCodes.add( "do {");
			mSourceCodes.add( "switch(goto_target) {");
			mSourceCodes.add( "case 0:");
			Arrays.sort(jumpaddr);
		}

		boolean outputmask = false;
		boolean forcejump = false;
		boolean alreadyreturn = false;
		int[] interret = new int[1];
		int type;
		int v, v1;
		Variant[] da = mData;
		for( int code = start; code < end; ) {
			msg = null;
			int srcpos = mAccessor.codePosToSrcPos(code);
			int line = mAccessor.srcPosToLine(srcpos);

			// output source lines as comment
			if( curline == -1 || curline <= line ) {
				if(curline == -1) curline = line;
				int nl = line - curline;
				while( curline <= line ) {
					if( nl<3 || nl >= 3 && line-curline <= 2) {
						//int len;
						String src = mAccessor.getLine(curline);
						outputFuncSrc( src, "", curline );
						curline++;
					} else {
						curline = line - 2;
					}
				}
			} else if(prevline != line) {
				String src = mAccessor.getLine(line);
				outputFuncSrc( src, "", line );
			}
			prevline = line;

			// decode each instructions
			for( int i = 0; i < jumpcount; i++ ) {
				if( jumpaddr[i] == code ) {
					// ジャンプターゲットの場合、case を挿入する
					int addr = i + 1;
					mSourceCodes.add( "\ncase "+addr+":" );
					alreadyreturn = false;
					forcejump = false;
					break;
				}
			}
			if( alreadyreturn == true || forcejump == true ) {
				outputmask = true;
			} else {
				outputmask = false;
			}

			int op = ca[code];
			switch( op ) {
			case VM_NOP:
				code++;
				break;

			case VM_CONST:
				type = getType( da[ca[code+2]] );
				v = ca[code+1];
				switch( type ) {
				case TYPE_INTEGER:
					msg = getRegisterName(v) + ".set( " + da[ca[code+2]].asInteger() + " );";
					break;
				case TYPE_REAL:
					msg = getRegisterName(v) + ".set( " + da[ca[code+2]].asDouble() + " );";
					break;
				case TYPE_STRING:
					msg = getRegisterName(v) + ".set( \"" + da[ca[code+2]].asString() + "\" );";
					break;
				case TYPE_VOID:
					msg = getRegisterName(v) + ".set( null );";
					break;
				case TYPE_INTER_OBJECT: {
					Object o = da[ca[code+2]].toJavaObject();
					VariantClosure clo = (VariantClosure)o;
					Dispatch2 dsp = clo.mObject;
					InterCodeObject obj = (InterCodeObject)dsp;
					msg = getRegisterName(v) + ".set( (Dispatch2)new "+obj.getName()+"Class() );";
					break;
				}
				case TYPE_INTER_GENERATOR: {
					InterCodeGenerator obj = (InterCodeGenerator) da[ca[code+2]].toJavaObject();
					msg = getRegisterName(v) + ".set( (Dispatch2)new "+obj.getName()+"Class() );";
				}
				case TYPE_NULL_CLOSURE:
					msg = getRegisterName(v) + ".set( null, null );";
					break;
				default:
					throw new CompileException( "非サポートの定数形式" );
				}
				code += 3;
				break;

			case VM_CP:
				msg = getRegisterName(ca[code+1]) + ".set( " + getRegisterName(ca[code+2]) + " );";
				code += 3;
				break;

			case VM_CL:
				msg = getRegisterName(ca[code+1]) + ".clear();";
				code += 2;
				break;

			case VM_CCL:
				// 展開してしまう
				v = ca[code+1];
				v1 = v + ca[code+2];
				msg = "";
				while( v < v1 ) {
					msg += getRegisterName(v) + ".clear();\n";
					v++;
				}
				code += 3;
				break;

			case VM_TT:
				msg = "flag = " + getRegisterName(ca[code+1]) + ".asBoolean();";
				code += 2;
				break;

			case VM_TF:
				msg = "flag = !" + getRegisterName(ca[code+1]) + ".asBoolean();";
				code += 2;
				break;

			case VM_CEQ:
				msg = "flag = " + getRegisterName(ca[code+1]) + ".normalCompare( "+ getRegisterName(ca[code+2]) + " );";
				code += 3;
				break;

			case VM_CDEQ:
				if( ca[code+2] == 0 ) {
					msg = "flag = " + getRegisterName(ca[code+1]) + ".isVoid();";
				} else {
					msg = "flag = " + getRegisterName(ca[code+1]) + ".discernCompare( "+ getRegisterName(ca[code+2]) + " ).asBoolean();";
				}
				code += 3;
				break;

			case VM_CLT:
				msg = "flag = " + getRegisterName(ca[code+1]) + ".greaterThan( "+ getRegisterName(ca[code+2]) + " );";
				code += 3;
				break;

			case VM_CGT:
				msg = "flag = " + getRegisterName(ca[code+1]) + ".littlerThan( "+ getRegisterName(ca[code+2]) + " );";
				code += 3;
				break;

			case VM_SETF:
				msg = getRegisterName(ca[code+1]) + ".set(flag?1:0);";
				code += 2;
				break;

			case VM_SETNF:
				msg = getRegisterName(ca[code+1]) + ".set(flag?0:1);";
				code += 2;
				break;

			case VM_LNOT:
				msg = getRegisterName(ca[code+1]) + ".logicalnot();";
				code += 2;
				break;

			case VM_NF:
				msg = "flag = !flag;";
				code ++;
				break;

			case VM_JF:
				v = findJumpTarget( jumpaddr, (code + ca[code+1]) );
				msg = "if( flag ) {\ngoto_target = " + v + ";\nbreak;\n}";
				code += 2;
				break;

			case VM_JNF:
				v = findJumpTarget( jumpaddr, (code + ca[code+1]) );
				msg = "if( !flag ) {\ngoto_target = " + v + ";\nbreak;\n}";
				code += 2;
				break;

			case VM_JMP:
				v = findJumpTarget( jumpaddr, (code + ca[code+1]) );
				msg = "goto_target = " + v + ";\nbreak;";
				forcejump = true;
				code += 2;
				break;

			case VM_INC:
				msg = getRegisterName(ca[code+1]) + ".increment();";
				code += 2;
				break;

			case VM_INCPD:
				msg = "{\n";
				msg += "VariantClosure clo = " + getRegisterName(ca[code+2]) + ".asObjectClosure();\n";
				if( ca[code+1] != 0 ) { // result
					msg += "int hr = clo.operation( OP_INC, \""+ da[ca[code+3]].getString() + "\", "+getRegisterName(ca[code+1])+", null, objthis );\n";
				} else {
					msg += "int hr = clo.operation( OP_INC, \""+ da[ca[code+3]].getString() + "\", null, null, objthis );\n";
				}
				msg += "if( hr < 0 ) throwFrom_tjs_error( hr, \""+ da[ca[code+3]].getString() + "\" );\n";
				msg += "}";
				//operatePropertyDirect0(ra, ra_offset, code, OP_INC);
				code += 4;
				break;

			case VM_INCPI:
				msg = "{\n";
				msg += "VariantClosure clo = " + getRegisterName(ca[code+2]) + ".asObjectClosure();\n";
				msg += "Variant name = " + getRegisterName(ca[code+3])+";\n";
				if( ca[code+1] != 0 ) { // result
					msg += "operatePropertyIndirect0( clo, name, "+getRegisterName(ca[code+1])+", objthis, OP_INC );\n";
				} else {
					msg += "operatePropertyIndirect0( clo, name, null, objthis, OP_INC );\n";
				}
				msg += "}";
				//operatePropertyIndirect0(ra, ra_offset, code, OP_INC);
				code += 4;
				break;

			case VM_INCP:
				msg = "{\n";
				msg += "VariantClosure clo = " + getRegisterName(ca[code+2]) + ".asObjectClosure();\n";
				if( ca[code+1] != 0 ) { // result
					msg += "int hr = clo.operation( OP_INC, null, "+getRegisterName(ca[code+1])+", null, clo.mObjThis != null ?clo.mObjThis:objthis );\n";
				} else {
					msg += "int hr = clo.operation( OP_INC, null, null, null, clo.mObjThis != null ?clo.mObjThis:objthis );\n";
				}
				msg += "if( hr < 0 ) throwFrom_tjs_error( hr, null );\n";
				msg += "}";
				//operateProperty0(ra, ra_offset, code, OP_INC);
				code += 3;
				break;

			case VM_DEC:
				msg = getRegisterName(ca[code+1]) + ".decrement();";
				code += 2;
				break;

			case VM_DECPD:
				msg = "{\n";
				msg += "VariantClosure clo = " + getRegisterName(ca[code+2]) + ".asObjectClosure();\n";
				if( ca[code+1] != 0 ) { // result
					msg += "int hr = clo.operation( OP_DEC, \""+ da[ca[code+3]].getString() + "\", "+getRegisterName(ca[code+1])+", null, objthis );\n";
				} else {
					msg += "int hr = clo.operation( OP_DEC, \""+ da[ca[code+3]].getString() + "\", null, null, objthis );\n";
				}
				msg += "if( hr < 0 ) throwFrom_tjs_error( hr, \""+ da[ca[code+3]].getString() + "\" );\n";
				msg += "}";
				//operatePropertyDirect0(ra, ra_offset, code, OP_DEC);
				code += 4;
				break;

			case VM_DECPI:
				msg = "{\n";
				msg += "VariantClosure clo = " + getRegisterName(ca[code+2]) + ".asObjectClosure();\n";
				msg += "Variant name = " + getRegisterName(ca[code+3])+";\n";
				if( ca[code+1] != 0 ) { // result
					msg += "operatePropertyIndirect0( clo, name, "+getRegisterName(ca[code+1])+", objthis, OP_DEC );\n";
				} else {
					msg += "operatePropertyIndirect0( clo, name, null, objthis, OP_DEC );\n";
				}
				msg += "}";
				//operatePropertyIndirect0(ra, ra_offset, code, OP_DEC);
				code += 4;
				break;

			case VM_DECP:
				msg = "{\n";
				msg += "VariantClosure clo = " + getRegisterName(ca[code+2]) + ".asObjectClosure();\n";
				if( ca[code+1] != 0 ) { // result
					msg += "int hr = clo.operation( OP_DEC, null, "+getRegisterName(ca[code+1])+", null, clo.mObjThis != null ?clo.mObjThis:objthis );\n";
				} else {
					msg += "int hr = clo.operation( OP_DEC, null, null, null, clo.mObjThis != null ?clo.mObjThis:objthis );\n";
				}
				msg += "if( hr < 0 ) throwFrom_tjs_error( hr, null );\n";
				msg += "}";
				//operateProperty0(ra, ra_offset, code, OP_DEC);
				code += 3;
				break;


			// TJS_DEF_VM_P
			case VM_LOR:
				msg = getRegisterName(ca[code+1]) + ".logicalorequal("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_LORPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_LOR );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_LOR );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_LOR);
				code += 5;
				break;
			case VM_LORPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_LOR );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_LOR );";
				}
				// operatePropertyIndirect(ra, ra_offset, code, OP_LOR );
				code += 5;
				break;
			case VM_LORP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_LOR );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_LOR );";
				}
				// operateProperty(ra, ra_offset, code, OP_LOR );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_LAND:
				msg = getRegisterName(ca[code+1]) + ".logicalandequal("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_LANDPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_LAND );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_LAND );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_LAND );
				code += 5;
				break;
			case VM_LANDPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_LAND );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_LAND );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_LAND );
				code += 5;
				break;
			case VM_LANDP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_LAND );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_LAND );";
				}
				//operateProperty(ra, ra_offset, code, OP_LAND );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_BOR:
				msg = getRegisterName(ca[code+1]) + ".orEqual("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_BORPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_BOR );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_BOR );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_BOR );
				code += 5;
				break;
			case VM_BORPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_BOR );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_BOR );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_BOR );
				code += 5;
				break;
			case VM_BORP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_BOR );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_BOR );";
				}
				//operateProperty(ra, ra_offset, code, OP_BOR );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_BXOR:
				msg = getRegisterName(ca[code+1]) + ".bitXorEqual("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_BXORPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_BXOR );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_BXOR );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_BXOR );
				code += 5;
				break;
			case VM_BXORPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_BXOR );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_BXOR );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_BXOR );
				code += 5;
				break;
			case VM_BXORP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_BXOR );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_BXOR );";
				}
				//operateProperty(ra, ra_offset, code, OP_BXOR );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_BAND:
				msg = getRegisterName(ca[code+1]) + ".andEqual("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_BANDPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_BAND );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_BAND );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_BAND );
				code += 5;
				break;
			case VM_BANDPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_BAND );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_BAND );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_BAND );
				code += 5;
				break;
			case VM_BANDP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_BAND );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_BAND );";
				}
				//operateProperty(ra, ra_offset, code, OP_BAND );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_SAR:
				msg = getRegisterName(ca[code+1]) + ".rightShiftEqual("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_SARPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_SAR );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_SAR );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_SAR );
				code += 5;
				break;
			case VM_SARPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_SAR );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_SAR );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_SAR );
				code += 5;
				break;
			case VM_SARP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_SAR );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_SAR );";
				}
				//operateProperty(ra, ra_offset, code, OP_SAR );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_SAL:
				msg = getRegisterName(ca[code+1]) + ".leftShiftEqual("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_SALPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_SAL );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_SAL );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_SAL );
				code += 5;
				break;
			case VM_SALPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_SAL );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_SAL );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_SAL );
				code += 5;
				break;
			case VM_SALP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_SAL );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_SAL );";
				}
				//operateProperty(ra, ra_offset, code, OP_SAL );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_SR:
				msg = getRegisterName(ca[code+1]) + ".rbitshiftequal("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_SRPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_SR );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_SR );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_SR );
				code += 5;
				break;
			case VM_SRPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_SR );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_SR );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_SR );
				code += 5;
				break;
			case VM_SRP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_SR );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_SR );";
				}
				//operateProperty(ra, ra_offset, code, OP_SR );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_ADD:
				msg = getRegisterName(ca[code+1]) + ".addEqual("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_ADDPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_ADD );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_ADD );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_ADD );
				code += 5;
				break;
			case VM_ADDPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_ADD );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_ADD );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_ADD );
				code += 5;
				break;
			case VM_ADDP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_ADD );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_ADD );";
				}
				//operateProperty(ra, ra_offset, code, OP_ADD );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_SUB:
				msg = getRegisterName(ca[code+1]) + ".subtractEqual("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_SUBPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_SUB );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_SUB );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_SUB );
				code += 5;
				break;
			case VM_SUBPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_SUB );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_SUB );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_SUB );
				code += 5;
				break;
			case VM_SUBP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_SUB );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_SUB );";
				}
				//operateProperty(ra, ra_offset, code, OP_SUB );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_MOD:
				msg = getRegisterName(ca[code+1]) + ".residueEqual("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_MODPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_MOD );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_MOD );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_MOD );
				code += 5;
				break;
			case VM_MODPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_MOD );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_MOD );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_MOD );
				code += 5;
				break;
			case VM_MODP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_MOD );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_MOD );";
				}
				//operateProperty(ra, ra_offset, code, OP_MOD );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_DIV:
				msg = getRegisterName(ca[code+1]) + ".divideEqual("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_DIVPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_DIV );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_DIV );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_DIV );
				code += 5;
				break;
			case VM_DIVPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_DIV );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_DIV );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_DIV );
				code += 5;
				break;
			case VM_DIVP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_DIV );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_DIV );";
				}
				//operateProperty(ra, ra_offset, code, OP_DIV );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_IDIV:
				msg = getRegisterName(ca[code+1]) + ".idivequal("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_IDIVPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_IDIV );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_IDIV );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_IDIV );
				code += 5;
				break;
			case VM_IDIVPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_IDIV );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_IDIV );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_IDIV );
				code += 5;
				break;
			case VM_IDIVP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_IDIV );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_IDIV );";
				}
				//operateProperty(ra, ra_offset, code, OP_IDIV );
				code += 4;
				break;
			// TJS_DEF_VM_P
			case VM_MUL:
				msg = getRegisterName(ca[code+1]) + ".multiplyEqual("+getRegisterName(ca[code+2])+");";
				code += 3;
				break;
			case VM_MULPD:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_MUL );";
				} else {
					msg = "operatePropertyDirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), \""+
						da[ca[code+3]].getString() + "\", null, " + getRegisterName(ca[code+4]) +", objthis, OP_MUL );";
				}
				//operatePropertyDirect(ra, ra_offset, code, OP_MUL );
				code += 5;
				break;
			case VM_MULPI:
				if( ca[code+1] != 0 ) { // result
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", " + getRegisterName(ca[code+1]) + ", " +
						getRegisterName(ca[code+4]) +", objthis, OP_MUL );";
				} else {
					msg = "operatePropertyIndirect( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+3]) + ", null, " +
						getRegisterName(ca[code+4]) +", objthis, OP_MUL );";
				}
				//operatePropertyIndirect(ra, ra_offset, code, OP_MUL );
				code += 5;
				break;
			case VM_MULP:
				if( ca[code+1] != 0 ) { // result
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+3]) + ", objthis, OP_MUL );";
				} else {
					msg = "operateProperty( "+getRegisterName(ca[code+2]) + ".asObjectClosure(), "+
						"null, " + getRegisterName(ca[code+3]) + ", objthis, OP_MUL );";
				}
				//operateProperty(ra, ra_offset, code, OP_MUL );
				code += 4;
				break;
			// TJS_DEF_VM_P


			case VM_BNOT:
				msg = getRegisterName(ca[code+1]) + ".bitnot();";
				code += 2;
				break;

			case VM_ASC:
				msg = "characterCodeOf( " + getRegisterName(ca[code+1]) + " );";
				code += 2;
				break;

			case VM_CHR:
				msg = "characterCodeFrom( " + getRegisterName(ca[code+1]) + " );";
				code += 2;
				break;

			case VM_NUM:
				msg = getRegisterName(ca[code+1]) + ".tonumber();";
				code += 2;
				break;

			case VM_CHS:
				msg = getRegisterName(ca[code+1]) + ".changesign();";
				code += 2;
				break;

			case VM_INV: {
				int offset = ca[code+1];
				msg = "boolean tmp = " + getRegisterName(offset) + ".isObject() == false ? false : " +
					getRegisterName(offset) + ".asObjectClosure().invalidate(0, null, objthis) == Error.S_TRUE;\n";
				msg += getRegisterName(offset) + ".set(tmp?1:0);";
				code += 2;
				break;
			}

			case VM_CHKINV: {
				int offset = ca[code+1];
				msg = "{\nboolean tmp;\n";
				msg += "if( " + getRegisterName(offset) + ".isObject() == false ) {\n";
				msg += "tmp = true;\n";
				msg += "} else {\n";
				msg += "int ret = " + getRegisterName(offset) + ".asObjectClosure().isValid(0, null, objthis );\n";
				msg += "tmp = ret == Error.S_TRUE || ret == Error.E_NOTIMPL;\n";
				msg += "}\n";
				msg += getRegisterName(offset) + ".set(tmp?1:0);\n";
				msg += "}";
				code += 2;
				break;
			}

			case VM_INT:
				msg = getRegisterName(ca[code+1]) + ".toInteger();";
				code += 2;
				break;

			case VM_REAL:
				msg = getRegisterName(ca[code+1]) + ".toReal();";
				code += 2;
				break;

			case VM_STR:
				msg = getRegisterName(ca[code+1]) + ".selfToString();";
				code += 2;
				break;

			case VM_OCTET:
				msg = getRegisterName(ca[code+1]) + ".toOctet();";
				code += 2;
				break;

			case VM_TYPEOF:
				msg = "typeOf( " + getRegisterName(ca[code+1]) + " );";
				code += 2;
				break;

			case VM_TYPEOFD:
				msg = "typeOfMemberDirect( "+getRegisterName(ca[code+1])+", "+
					getRegisterName(ca[code+2])+", "+
					getDataToStrOrNum(da[ca[code+3]])+", objthis, Interface.MEMBERMUSTEXIST );";
				code += 4;
				break;

			case VM_TYPEOFI:
				msg = "typeOfMemberIndirect( "+getRegisterName(ca[code+1])+", "+getRegisterName(ca[code+2])+
					", "+getRegisterName(ca[code+3])+", objthis, Interface.MEMBERMUSTEXIST );\n";
				code += 4;
				break;

			case VM_EVAL:
				msg = "eval( "+getRegisterName(ca[code+1])+", TJS.mEvalOperatorIsOnGlobal ? null : objthis, true);";
				code += 2;
				break;

			case VM_EEXP:
				msg = "eval( "+getRegisterName(ca[code+1])+", TJS.mEvalOperatorIsOnGlobal ? null : objthis, false);";
				code += 2;
				break;

			case VM_CHKINS:
				msg = "instanceOf( " + getRegisterName(ca[code+2]) + ", " + getRegisterName(ca[code+1]) + " );";
				code += 3;
				break;

			case VM_CALL:
			case VM_NEW:
				msg = callFunction( ca, interret, code, 3, FUNC_NORMAL );
				code += interret[0];
				break;

			case VM_CALLD:
				msg = callFunction( ca, interret, code, 4, FUNC_DIRECT );
				code += interret[0];
				//code += callFunctionDirect(ra, ra_offset, code, args );
				break;

			case VM_CALLI:
				msg = callFunction( ca, interret, code, 4, FUNC_INDIRECT );
				code += interret[0];
				//code += callFunctionIndirect(ra, ra_offset, code, args );
				break;

			case VM_GPD:
				msg = "getPropertyDirect( " + getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+2]) + ", "+getDataToStrOrNum(da[ca[code+3]])+", objthis, 0 );";
				//getPropertyDirect(ra, ra_offset, code, 0);
				code += 4;
				break;

			case VM_GPDS:
				msg = "getPropertyDirect( " + getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+2]) + ", "+getDataToStrOrNum(da[ca[code+3]])+", objthis, Interface.IGNOREPROP );";
				//getPropertyDirect(ra, ra_offset, code, Interface.IGNOREPROP);
				code += 4;
				break;

			case VM_SPD:
				msg = "setPropertyDirect( " + getRegisterName(ca[code+1]) + ", "+getDataToStrOrNum(da[ca[code+2]])+", " + getRegisterName(ca[code+3]) + ", objthis, 0 );";
				//setPropertyDirect(ra, ra_offset, code, 0);
				code += 4;
				break;

			case VM_SPDE:
				msg = "setPropertyDirect( " + getRegisterName(ca[code+1]) + ", "+getDataToStrOrNum(da[ca[code+2]])+", " + getRegisterName(ca[code+3]) + ", objthis, Interface.MEMBERENSURE );";
				//setPropertyDirect(ra, ra_offset, code, Interface.MEMBERENSURE);
				code += 4;
				break;

			case VM_SPDEH:
				msg = "setPropertyDirect( " + getRegisterName(ca[code+1]) + ", "+getDataToStrOrNum(da[ca[code+2]])+", " + getRegisterName(ca[code+3]) + ", objthis, Interface.MEMBERENSURE|Interface.HIDDENMEMBER );";
				//setPropertyDirect(ra, ra_offset, code, Interface.MEMBERENSURE|Interface.HIDDENMEMBER);
				code += 4;
				break;

			case VM_SPDS:
				msg = "setPropertyDirect( " + getRegisterName(ca[code+1]) + ", "+getDataToStrOrNum(da[ca[code+2]])+", " + getRegisterName(ca[code+3]) + ", objthis, Interface.MEMBERENSURE|Interface.IGNOREPROP );";
				//setPropertyDirect(ra, ra_offset, code, Interface.MEMBERENSURE|Interface.IGNOREPROP);
				code += 4;
				break;

			case VM_GPI:
				msg = "getPropertyIndirect( " + getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+2]) + ", " + getRegisterName(ca[code+3]) + ", objthis, 0 );";
				//getPropertyIndirect(ra, ra_offset, code, 0);
				code += 4;
				break;

			case VM_GPIS:
				msg = "getPropertyIndirect( " + getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+2]) + ", " + getRegisterName(ca[code+3]) + ", objthis, Interface.IGNOREPROP );";
				//getPropertyIndirect(ra, ra_offset, code, Interface.IGNOREPROP);
				code += 4;
				break;

			case VM_SPI:
				msg = "setPropertyIndirect( " + getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+2]) + ", " + getRegisterName(ca[code+3]) + ", objthis, 0 );";
				//setPropertyIndirect(ra, ra_offset, code, 0);
				code += 4;
				break;

			case VM_SPIE:
				msg = "setPropertyIndirect( " + getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+2]) + ", " + getRegisterName(ca[code+3]) + ", objthis, Interface.MEMBERENSURE );";
				//setPropertyIndirect(ra, ra_offset, code, Interface.MEMBERENSURE);
				code += 4;
				break;

			case VM_SPIS:
				msg = "setPropertyIndirect( " + getRegisterName(ca[code+1]) + ", " + getRegisterName(ca[code+2]) + ", " + getRegisterName(ca[code+3]) + ", objthis, Interface.MEMBERENSURE|Interface.IGNOREPROP );";
				//setPropertyIndirect(ra, ra_offset, code, Interface.MEMBERENSURE|Interface.IGNOREPROP);
				code += 4;
				break;

			case VM_GETP:
				msg = "{\nVariantClosure clo = "+getRegisterName(ca[code+2])+".asObjectClosure();\n";
				msg += "int hr = clo.propGet( 0, null, "+getRegisterName(ca[code+1])+", clo.mObjThis != null ? clo.mObjThis : objthis );\n";
				msg += "if( hr < 0 ) throwFrom_tjs_error(hr, null);\n}";
				//getProperty(ra, ra_offset, code);
				code += 3;
				break;

			case VM_SETP:
				msg = "{\nVariantClosure clo = "+getRegisterName(ca[code+1])+".asObjectClosure();\n";
				msg += "int hr = clo.propSet(0, null, "+getRegisterName(ca[code+2])+", clo.mObjThis != null ? clo.mObjThis : objthis );\n";
				msg += "if( hr < 0 ) throwFrom_tjs_error( hr, null );\n}";
				//setProperty(ra, ra_offset, code);
				code += 3;
				break;

			case VM_DELD:
				msg = "{\nVariantClosure clo = "+getRegisterName(ca[code+2])+".asObjectClosure();\n";
				final String name = da[ca[code+3]].getString();
				msg += "int hr = clo.deleteMember(0, \""+name+"\", objthis );\n";
				if( ca[code+1] != 0 ) {
					msg += "if( hr < 0 ) "+getRegisterName(ca[code+1])+".set(0);\n";
					msg += "else "+getRegisterName(ca[code+1])+".set(1);\n";
				}
				msg += "}";
				//deleteMemberDirect(ra, ra_offset, code);
				code += 4;
				break;

			case VM_DELI:
				msg = "{\nVariantClosure clo = "+getRegisterName(ca[code+2])+".asObjectClosure();\n";
				msg += "final String name = "+getRegisterName(ca[code+3])+".asString();\n";
				msg += "int hr = clo.deleteMember( 0, name, clo.mObjThis != null ? clo.mObjThis : objthis );\n";
				if( ca[code+1] != 0 ) {
					msg += "if( hr < 0 ) "+getRegisterName(ca[code+1])+".set(0);\n";
					msg += "else "+getRegisterName(ca[code+1])+".set(1);\n";
				}
				msg += "}";
				//deleteMemberIndirect(ra, ra_offset, code);
				code += 4;
				break;

			case VM_SRV:
				msg = "if( result != null ) result.copyRef( "+getRegisterName(ca[code+1])+" );";
				code += 2;
				break;

			case VM_RET:
				if( alreadyreturn == false ) {
					msg = "return;";// + (code + 1) + ";";
					alreadyreturn = true;
				}
				code += 1;
				break;

			case VM_ENTRY:
				mExceptionDataStack.add( new ExceptionData(ca[code+1]+code, ca[code+2]) );
				msg = "try {";
				//code = executeCodeInTryBlock( ra, ra_offset, code+3, args, result, ca[code+1]+code, ca[code+2] );
				code += 3;
				break;

			case VM_EXTRY:
				if( mExceptionDataStack.size() > 0 ) {
					int last = mExceptionDataStack.size()-1;
					ExceptionData ex = mExceptionDataStack.get(last);
					mExceptionDataStack.remove(last);
					int exobjreg = ex.mExobjReg;
					int catchip = findJumpTarget( jumpaddr, ex.mCatchIp );
					msg = "} catch( TJSScriptException e ) {\n";
					if( exobjreg != 0 ) {
						msg += getRegisterName(exobjreg)+".set( e.getValue() );\n";
					}
					msg += "goto_target = "+catchip+";\n";
					msg += "break;\n";
					msg += "} catch( TJSScriptError e ) {\n";
					if(exobjreg != 0) {
						msg += "Variant msg = new Variant(e.getMessage());\n";
						msg += "Variant trace = new Variant(e.getTrace());\n";
						msg += "Variant ret = new Variant();\n";
						msg += "Error.getExceptionObject( mBlock.getTJS(), ret, msg, trace );\n";
						msg += getRegisterName(exobjreg)+".set( ret );\n";
					}
					msg += "goto_target = "+catchip+";\n";
					msg += "break;\n";
					msg += "} catch( TJSException e ) {\n";
					if( exobjreg != 0 ){
						msg += "Variant msg = new Variant( e.getMessage() );\n";
						msg += "Variant ret = new Variant();\n";
						msg += "Error.getExceptionObject( mBlock.getTJS(), ret, msg, null );\n";
						msg += getRegisterName(exobjreg)+".set( ret );\n";
					}
					msg += "goto_target = "+catchip+";\n";
					msg += "break;\n";
					msg += "} catch( Exception e ) {\n";
					if( exobjreg != 0 ) {
						msg += "Variant msg = new Variant( e.getMessage() );\n";
						msg += "Variant ret = new Variant();\n";
						msg += "Error.getExceptionObject( mBlock.getTJS(), ret, msg, null );\n";
						msg += getRegisterName(exobjreg)+".set( ret );\n";
					}
					msg += "goto_target = "+catchip+";\n";
					msg += "break;\n";
					msg += "}\n";
				}
				code += 1;

			case VM_THROW:
				msg = "throwScriptException( "+getRegisterName(ca[code+1])+", mBlock, "+mAccessor.codePosToSrcPos(code)+" );\n";
				code += 2; // actually here not proceed...
				break;

			case VM_CHGTHIS:
				msg = getRegisterName(ca[code+1])+".changeClosureObjThis( " +getRegisterName(ca[code+2])+ ".asObject() );";
				//ra[ra_offset+ca[code+1]].changeClosureObjThis(ra[ra_offset+ca[code+2]].asObject());
				code += 3;
				break;

			case VM_GLOBAL:
				msg = getRegisterName(ca[code+1])+".set( ScriptsClass.getGlobal() );";
				code += 2;
				break;

			case VM_ADDCI:
				msg = "addClassInstanceInfo( "+getRegisterName(ca[code+1])+".asObject(), "+getRegisterName(ca[code+2])+".asString() );";
				//addClassInstanceInfo(ra, ra_offset,code);
				code+=3;
				break;

			case VM_REGMEMBER:
				msg = "copyAllMembers( (CustomObject)objthis );";
				//copyAllMembers( (CustomObject)ra[ra_offset-1].asObject() );
				code++;
				break;

			case VM_DEBUGGER:
				code++;
				break;

			default:
				throw new CompileException("未定義のVMオペコードです。");
			} /* switch */

			if( outputmask == false ) {
				mSourceCodes.add( msg );
			}
		}

		if( target.size() > 0 ) {
			mSourceCodes.add( "default: loop = false;" );
			mSourceCodes.add( "}" );
			mSourceCodes.add( "} while(loop);" );
			mSourceCodes.add( "return;" );
		}
		mSourceCodes.add( "}" );
	}
	public IntVector checkJumpCode( int start, int end ) throws VariantException {
		IntVector ret = new IntVector();
		short[] ca = mCode;
		if(end <= 0) end = ca.length;
		if(end > ca.length ) end = ca.length;
		int size = 0;
		for( int i = start; i < end; ) {
			switch( ca[i] ) {
			case VM_NOP: size = 1; break;
			case VM_NF: size = 1; break;
			case VM_CONST: size = 3; break;
			case VM_CP: size = 3; break;
			case VM_CEQ: size = 3; break;
			case VM_CDEQ: size = 3; break;
			case VM_CLT: size = 3; break;
			case VM_CGT: size = 3; break;
			case VM_CHKINS: size = 3; break;
			case VM_LOR: size = 3; break;
			case VM_LOR+1: size = 5; break;
			case VM_LOR+2: size = 5; break;
			case VM_LOR+3: size = 4; break;
			case VM_LAND: size = 3; break;
			case VM_LAND+1: size = 5; break;
			case VM_LAND+2: size = 5; break;
			case VM_LAND+3: size = 4; break;
			case VM_BOR: size = 3; break;
			case VM_BOR+1: size = 5; break;
			case VM_BOR+2: size = 5; break;
			case VM_BOR+3: size = 4; break;
			case VM_BXOR: size = 3; break;
			case VM_BXOR+1: size = 5; break;
			case VM_BXOR+2: size = 5; break;
			case VM_BXOR+3: size = 4; break;
			case VM_BAND: size = 3; break;
			case VM_BAND+1: size = 5; break;
			case VM_BAND+2: size = 5; break;
			case VM_BAND+3: size = 4; break;
			case VM_SAR: size = 3; break;
			case VM_SAR+1: size = 5; break;
			case VM_SAR+2: size = 5; break;
			case VM_SAR+3: size = 4; break;
			case VM_SAL: size = 3; break;
			case VM_SAL+1: size = 5; break;
			case VM_SAL+2: size = 5; break;
			case VM_SAL+3: size = 4; break;
			case VM_SR: size = 3; break;
			case VM_SR+1: size = 5; break;
			case VM_SR+2: size = 5; break;
			case VM_SR+3: size = 4; break;
			case VM_ADD:  size = 3; break;
			case VM_ADD+1: size = 5; break;
			case VM_ADD+2: size = 5; break;
			case VM_ADD+3: size = 4; break;
			case VM_SUB: size = 3; break;
			case VM_SUB+1: size = 5; break;
			case VM_SUB+2: size = 5; break;
			case VM_SUB+3: size = 4; break;
			case VM_MOD:  size = 3; break;
			case VM_MOD+1: size = 5; break;
			case VM_MOD+2: size = 5; break;
			case VM_MOD+3: size = 4; break;
			case VM_DIV:  size = 3; break;
			case VM_DIV+1: size = 5; break;
			case VM_DIV+2: size = 5; break;
			case VM_DIV+3: size = 4; break;
			case VM_IDIV:  size = 3; break;
			case VM_IDIV+1: size = 5; break;
			case VM_IDIV+2: size = 5; break;
			case VM_IDIV+3: size = 4; break;
			case VM_MUL: size = 3; break;
			case VM_MUL+1: size = 5; break;
			case VM_MUL+2: size = 5; break;
			case VM_MUL+3: size = 4; break;
			case VM_TT:  size = 2; break;
			case VM_TF: size = 2; break;
			case VM_SETF: size = 2; break;
			case VM_SETNF: size = 2; break;
			case VM_LNOT: size = 2; break;
			case VM_BNOT: size = 2; break;
			case VM_ASC: size = 2; break;
			case VM_CHR: size = 2; break;
			case VM_NUM: size = 2; break;
			case VM_CHS: size = 2; break;
			case VM_CL: size = 2; break;
			case VM_INV: size = 2; break;
			case VM_CHKINV: size = 2; break;
			case VM_TYPEOF: size = 2; break;
			case VM_EVAL: size = 2; break;
			case VM_EEXP: size = 2; break;
			case VM_INT: size = 2; break;
			case VM_REAL: size = 2; break;
			case VM_STR: size = 2; break;
			case VM_OCTET: size = 2; break;
			case VM_CCL: size = 3; break;
			case VM_INC: size = 2; break;
			case VM_INC+1: size = 4; break;
			case VM_INC+2: size = 4; break;
			case VM_INC+3: size = 3; break;
			case VM_DEC: size = 2; break;
			case VM_DEC+1: size = 4; break;
			case VM_DEC+2: size = 4; break;
			case VM_DEC+3: size = 3; break;

			case VM_JF: ret.add(ca[i+1] + i); size = 2; break;
			case VM_JNF: ret.add(ca[i+1] + i); size = 2; break;
			case VM_JMP: ret.add(ca[i+1] + i); size = 2; break;

			case VM_CALL:
			case VM_CALLD:
			case VM_CALLI:
			case VM_NEW:
			{
				int st; // start of arguments
				if( ca[i] == VM_CALLD || ca[i] == VM_CALLI)
					st = 5;
				else
					st = 4;
				int num = ca[i+st-1];     // st-1 = argument count
				if(num == -1) {
					// omit arg
					size = st;
				} else if(num == -2) {
					// expand arg
					st++;
					num = ca[i+st-1];
					size = st + num * 2;
				} else {
					// normal operation
					size = st + num;
				}
				break;
			}

			case VM_GPD:
			case VM_GPDS:
				size = 4;
				break;

			case VM_SPD:
			case VM_SPDE:
			case VM_SPDEH:
			case VM_SPDS:
				size = 4;
				break;

			case VM_GPI:
			case VM_GPIS:
				size = 4;
				break;


			case VM_SPI:
			case VM_SPIE:
			case VM_SPIS:
				size = 4;
				break;


			case VM_SETP: size = 3; break;
			case VM_GETP: size = 3; break;

			case VM_DELD:
			case VM_TYPEOFD:
				size = 4;
				break;

			case VM_DELI:
			case VM_TYPEOFI:
				size = 4;
				break;
			case VM_SRV: size = 2; break;
			case VM_RET: size = 1; break;

			case VM_ENTRY:
				ret.add(ca[i+1]+i); // catch アドレス
				size = 3;
				break;

			case VM_EXTRY: size = 1; break;
			case VM_THROW: size = 2; break;
			case VM_CHGTHIS: size = 3; break;
			case VM_GLOBAL: size = 2; break;
			case VM_ADDCI: size = 3; break;
			case VM_REGMEMBER: size = 1; break;
			case VM_DEBUGGER: size = 1; break;
			default: size = 1; break;
			} /* switch */

			i+=size;
		}
		return ret;
	}
	private String callFunction( short[] ca, int[] ret, int code, int offset, int style ) throws VariantException, CompileException {
		// function calling / create new object
		final int code_offset = code + offset;
		int pass_args_count = ca[ code_offset ];
		if( pass_args_count == -1 ) {
			/* omitting args; pass intact aguments from the caller */
			// ... の時、arg をそのまま渡す
			ret[0] = 1 + offset;
			switch( style ) {
				case FUNC_NORMAL:
					return callFunctionInternalString( ca, code, "args" );
				case FUNC_INDIRECT:
					return callFunctionIndirectInternalString( ca, code, "args" );
				case FUNC_DIRECT:
					return callFunctionDirectInternalString( ca, code, "args" );
			}
			throw new CompileException(Error.InternalError);
		} else if(pass_args_count == -2) {
			// 全引数の数をカウント
			int arg_written_count = ca[ code_offset+1 ];
			ret[0] = arg_written_count * 2 + 2 + offset;
			StringBuilder builder = new StringBuilder();
			builder.append( "{\n");
			builder.append( "int args_v_count = 0;\n");
			builder.append( "int pass_args_count = 0;\n");
			for( int i = 0; i < arg_written_count; i++) {
				switch( ca[ code_offset+i*2+2 ] ) {
				case fatNormal:
					builder.append( "pass_args_count++;\n");
					break;
				case fatExpand:
					builder.append( "args_v_count += ");
					builder.append( "ArrayClass.getArrayElementCount( "+getRegisterName(ca[code_offset+i*2+1+2])+".asObject();\n" );
					break;
				case fatUnnamedExpand:
					builder.append( "pass_args_count += ");
					builder.append( "(args.length > " + mFuncDeclUnnamedArgArrayBase + ") ? (args.length - "+mFuncDeclUnnamedArgArrayBase+") : 0;\n");
					break;
				}
			}
			builder.append( "pass_args_count += args_v_count;\n");
			// Array 用のテンポラリ配列を確保する
			builder.append( "Variant[] pass_args_v = new Variant[args_v_count];\n");
			// 実際の引数配列を確保する
			builder.append( "pass_args = new Variant[pass_args_count];\n");
			// 実際の引数配列に値(参照)を入れていく
			builder.append( "args_v_count = 0;\n");
			builder.append( "pass_args_count = 0;\n");
			for( int i = 0; i < arg_written_count; i++ ) {
				switch( ca[ code_offset+i*2+2 ] ) {
				case fatNormal:
					builder.append( "pass_args[pass_args_count++] = "+getRegisterName(ca[code_offset+i*2+1+2])+";");
					break;
				case fatExpand: {
					builder.append( "int count = ArrayClass.copyArrayElementTo( "+getRegisterName(ca[code_offset+i*2+1+2])+".asObject(), pass_args_v, args_v_count, 0, -1);\n");
					builder.append( "for( int j = 0; j < count; j++ ) {\n" );
					builder.append( "pass_args[pass_args_count++] = pass_args_v[j + args_v_count];\n" );
					builder.append( "}\n" );
					builder.append( "args_v_count += count;\n" );
					break;
				}
				case fatUnnamedExpand: {
					builder.append( "int count = (args.length > "+mFuncDeclUnnamedArgArrayBase+") ? (args.length - "+mFuncDeclUnnamedArgArrayBase+") : 0;\n" );
					builder.append( "for( int j = 0; j < count; j++ ) {\n" );
					builder.append( "pass_args[pass_args_count++] = args["+mFuncDeclUnnamedArgArrayBase+" + j];\n");
					builder.append( "}\n" );
					break;
				}
				}
			}
			switch( style ) {
			case FUNC_NORMAL:
				builder.append( callFunctionInternalString( ca, code, "pass_args" ) );
				break;
			case FUNC_INDIRECT:
				builder.append( callFunctionIndirectInternalString( ca, code, "pass_args" ) );
				break;
			case FUNC_DIRECT:
				builder.append( callFunctionDirectInternalString( ca, code, "pass_args" ) );
				break;
			}
			builder.append( "\npass_args_v = null;\n" );
			builder.append( "}" );
			return builder.toString();
		} else {
			// 通常の引数を持つ関数呼び出し
			ret[0] = pass_args_count + 1 + offset;
			StringBuilder builder = new StringBuilder();
			builder.append( "{\n" );
			String arg_name = "TJS.NULL_ARG";
			if( pass_args_count > 0 ) {
				builder.append( "Variant[] pass_args = new Variant["+pass_args_count+"];\n" );
				arg_name = "pass_args";
			}
			for( int i = 0; i < pass_args_count; i++) {
				builder.append( "pass_args["+i+"] = "+getRegisterName(ca[code_offset+1+i]) +";\n" );
			}
			switch( style ) {
			case FUNC_NORMAL:
				builder.append( callFunctionInternalString( ca, code, arg_name ) );
				break;
			case FUNC_INDIRECT:
				builder.append( callFunctionIndirectInternalString( ca, code, arg_name ) );
				break;
			case FUNC_DIRECT:
				builder.append( callFunctionDirectInternalString( ca, code, arg_name ) );
				break;
			}
			builder.append( "\n}" );
			return builder.toString();
		}
	}
	private String callFunctionInternalString( short[] ca, int code, final String pass_args ) {
		StringBuilder builder = new StringBuilder();
		builder.append( "{\n" );
		builder.append( "VariantClosure clo = "+getRegisterName(ca[code+2]) +".asObjectClosure();\n" );
		final int offset = ca[code+1];
		int op = ca[code];
		if( op == VM_CALL ) {
			if( offset != 0 ) {
				builder.append( "int hr = clo.funcCall(0, null, "+getRegisterName(offset) +", "+pass_args+","+
					"clo.mObjThis != null ?clo.mObjThis:objthis);\n" );
			} else {
				builder.append( "int hr = clo.funcCall(0, null, null, "+pass_args+","+
					"clo.mObjThis != null ?clo.mObjThis:objthis);\n" );
			}
		} else {
			builder.append( "Holder<Dispatch2> dsp = new Holder<Dispatch2>(null);\n" );
			builder.append( "int hr = clo.createNew(0, null, dsp, "+pass_args+", clo.mObjThis != null ?clo.mObjThis:objthis);\n");
			if( offset != 0 ) {
				builder.append( "if( hr >= 0 ) {\n" );
				builder.append( "if( dsp.mValue != null  ) {\n" );
				builder.append( getRegisterName(offset)+".set(dsp.mValue, dsp.mValue);\n" );
				builder.append( "}\n" );
				builder.append( "}\n" );
			}
		}
		builder.append( "if( hr < 0 ) throwFrom_tjs_error(hr, \"\" );\n");
		builder.append( "}" );
		return builder.toString();
	}
	private String callFunctionDirectInternalString( short[] ca, int code, final String pass_args ) throws VariantException {
		StringBuilder builder = new StringBuilder();
		builder.append( "{\n" );
		builder.append( "int hr;\n" );
		final String name = mData[ca[code+3]].getString();
		final int offset = ca[code+1];
		final String ra_code2 = getRegisterName(ca[code+2]);
		builder.append( "if( "+ra_code2+".isObject() ) {\n" );
		builder.append( "VariantClosure clo = "+ra_code2+".asObjectClosure();\n" );
		if( offset != 0 ) {
			builder.append( "hr = clo.funcCall(0, \""+name+"\", "+getRegisterName(offset)+", ");
			builder.append( pass_args+", clo.mObjThis != null ?clo.mObjThis:objthis);\n" );
		} else {
			builder.append( "hr = clo.funcCall(0, \""+name+"\", null, ");
			builder.append( pass_args+", clo.mObjThis != null ?clo.mObjThis:objthis);\n" );
		}
		if( isStringFunctionName(name) ) { // 呼び出し名が文字列用関数で無い時は出力をスキップ
			builder.append( "} else if( "+ra_code2+".isString() ) {\n" );
			builder.append( "processStringFunction( \""+name+"\", "+ra_code2+".asString(),"+pass_args+", " );
			if( offset != 0 ) {
				builder.append( getRegisterName(offset) );
			} else {
				builder.append( "null" );
			}
			builder.append( ");\n" );
			builder.append( "hr = Error.S_OK;\n" );
		}
		/* octet のメソッドはまだ存在しないので、スキップ
		builder.append( "} else if( "+ra_code2+".isOctet() ) {\n" );
		builder.append( "processOctetFunction( \""+name+"\", "+ra_code2+".asString(),"+pass_args+", " );
		if( offset != 0 ) {
			builder.append( getRegisterName(offset) );
		} else {
			builder.append( "null" );
		}
		builder.append( ");\n" );
		builder.append( "hr = Error.S_OK;\n" );
		*/
		builder.append( "} else {\n" );
		builder.append( "String mes = Error.VariantConvertErrorToObject.replace( \"%1\", Utils.VariantToReadableString("+ra_code2+") );\n");
		builder.append( "throw new VariantException( mes );\n");
		builder.append( "}\n");
		builder.append( "if( hr < 0 ) throwFrom_tjs_error(hr, \""+name+"\" );\n");
		builder.append( "}" );
		return builder.toString();
	}
	private String callFunctionIndirectInternalString( short[] ca, int code, final String pass_args ) {
		StringBuilder builder = new StringBuilder();
		builder.append( "{\n" );
		builder.append( "int hr;\n" );
		final String ra_code2 = getRegisterName(ca[code+2]);
		final String name = getRegisterName(ca[code+3])+".asString()";
		final int offset = ca[code+1];
		builder.append( "if( "+ra_code2+".isObject() ) {\n" );
		builder.append( "VariantClosure clo = "+ra_code2+".asObjectClosure();\n" );
		builder.append( "hr = clo.funcCall(0, "+name+", " );
		if( offset != 0 ) {
			builder.append( getRegisterName(offset) );
		} else {
			builder.append( "null" );
		}
		builder.append( pass_args );
		builder.append( ", clo.mObjThis != null ? clo.mObjThis:objthis);\n" );
		if( isStringFunctionName(name) ) { // 呼び出し名が文字列用関数で無い時は出力をスキップ
			builder.append( "} else if( "+ra_code2+".isString() ) {\n" );
			builder.append( "processStringFunction( "+name+", "+ra_code2+".asString(),");
			builder.append(pass_args);
			builder.append(", ");
			if( offset != 0 ) {
				builder.append( getRegisterName(offset) );
			} else {
				builder.append( "null" );
			}
			builder.append( ");\n" );
			builder.append( "hr = Error.S_OK;\n" );
		}
		/* octet のメソッドはまだ存在しないので、スキップ
		builder.append( "} else if( "+ra_code2+".isOctet() ) {\n");
		builder.append( "processOctetFunction( "+name+", "+ra_code2+".asString(),");
		builder.append( pass_args );
		builder.append(", ");
		if( offset != 0 ) {
			builder.append( getRegisterName(offset) );
		} else {
			builder.append( "null" );
		}
		builder.append( ");\n" );
		builder.append( "hr = Error.S_OK;\n" );
		*/
		builder.append( "} else {\n" );
		builder.append( "String mes = Error.VariantConvertErrorToObject.replace( \"%1\", Utils.VariantToReadableString("+ra_code2+") );\n" );
		builder.append( "throw new VariantException( mes );\n" );
		builder.append( "}\n" );
		builder.append( "if( hr < 0 ) throwFrom_tjs_error(hr, \"\" );\n");
		builder.append( "}" );
		return builder.toString();
	}
	public ArrayList<String> getSourceCode() { return mSourceCodes; }

	private static boolean isStringFunctionName( final String name ) {
		if( name == null ) return false;
		if( name.length() == 0 ) return false;
		final int count = STR_FUNC.length;
		for( int i = 0; i < count; i++ ) {
			if( name.equals(STR_FUNC[i]) ) {
				return true;
			}
		}
		return false;
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
		VM_DEBUGGER	= 127;
		//__VM_LAST	= 128;

	// FuncArgType
	static private final int
		fatNormal = 0,
		fatExpand = 1,
		fatUnnamedExpand = 2;
	// 関数呼び出しスタイル
	static private final int
		FUNC_NORMAL = 0,
		FUNC_INDIRECT = 1,
		FUNC_DIRECT = 2;
	static private final String STR_FUNC[] = {
		"charAt",
		"indexOf",
		"toUpperCase",
		"toLowerCase",
		"substring",
		"substr",
		"sprintf",
		"replace",
		"escape",
		"split",
		"trim",
		"reverse",
		"repeat"
	};
}
