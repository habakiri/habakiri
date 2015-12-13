package jp.kirikiri.tjs2.translate;

import java.util.ArrayList;
import java.util.HashMap;

import jp.kirikiri.tjs2.ContextType;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.TextWriteStreamInterface;

public class JavaCodeIntermediate {

	public static class ClosureCode {
		public ArrayList<String> mCode;
		public int mType; // context type
		public String mName;
		public ClosureCode( String name, int type, ArrayList<String> code ) {
			mName = name;
			mType = type;
			mCode = code;
		}
		public void write( final String classname, TextWriteStreamInterface stream ) throws TJSException {
			if( mType == ContextType.FUNCTION ) {
				if( mName.equals(classname) ) {
					stream.write("registerNCM( \""+mName+"\", new NativeConvertedClassConstructor(engine) {\n@Override\n" );
				} else {
					stream.write("registerNCM( \""+mName+"\", new NativeConvertedClassMethod(engine) {\n@Override\n" );
				}
			}
			ArrayList<String> ca = mCode;
			final int count = ca.size();
			for( int i = 0; i < count; i++ ) {
				String line = ca.get(i);
				if( line != null ) {
					stream.write( ca.get(i) );
					stream.write("\n");
				}
			}

			if( mType == ContextType.FUNCTION ) {
				stream.write("}, CLASS_NAME, Interface.nitMethod, 0 );\n\n" );
			}
		}
	}
	public static class Property {
		public ArrayList<String> mSetter;
		public ArrayList<String> mGetter;
		public String mName;
		public Property( String name ) {
			mName = name;
		}
		public void setGetter( ArrayList<String> code ) {
			mGetter = code;
		}
		public void setSetter( ArrayList<String> code ) {
			mSetter = code;
		}
	}
	private ArrayList<String> mInitializer;
	private ArrayList<ClosureCode> mMembers;
	private HashMap<String,Property> mProps;
	private String mName;

	public JavaCodeIntermediate( String classname ) {
		mName = classname;
		mMembers = new ArrayList<ClosureCode>();
		mProps = new HashMap<String,Property>();
	}
	public void addMember( ClosureCode code ) {
		mMembers.add( code );
	}
	public void addProperty( final String name, Property prop ) {
		mProps.put( name, prop );
	}
	public Property getProperty( final String name ) {
		return mProps.get(name);
	}
	public void setInitializer( ArrayList<String> code ) {
		mInitializer = code;
	}
	public String getName() { return mName; }

	public void write() throws TJSException {
		String classname = mName + "Class";
		String filename = mName + "Class.java";
		TextWriteStreamInterface stream = TJS.mStorage.createTextWriteStream(filename, "utf-8");
		stream.write("package jp.kirikiri.tjs2java;\n");
		stream.write("import jp.kirikiri.tjs2.*;\n");
		stream.write("import jp.kirikiri.tjs2.Error;\n");
		stream.write("import jp.kirikiri.tvp2.base.ScriptsClass;\n");
		stream.write("import jp.kirikiri.tvp2.msg.Message;\n");


		stream.write("public class "+classname+" extends ExtendableNativeClass {\n");
		stream.write("static public int ClassID = -1;\n");
		stream.write("static public final String CLASS_NAME = \""+mName+"\";\n");

		stream.write("public "+classname+"() throws VariantException, TJSException {\n");
		stream.write("super( CLASS_NAME );\n");
		stream.write("final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);\n");
		stream.write("setClassID( NCM_CLASSID );\n");
		stream.write("ClassID = NCM_CLASSID;\n");
		stream.write("TJS engine = ScriptsClass.getEngine();\n" );

		final int count = mMembers.size();
		for( int i = 0; i < count; i++ ) {
			mMembers.get(i).write(mName,stream);
		}

		stream.write("}\n}\n");
		stream.destruct();
	}
}
