package togos.repository.file;

import java.io.File;

public class FileUtil
{
	public static void mkParentDirs( File f ) {
		File p = f.getParentFile();
		if( p == null ) return;
		if( p.exists() ) return;
		p.mkdirs();
	}
	
	public static boolean rmR( File f ) {
		if( !f.exists() ) return true;
		if( f.isDirectory() ) {
			File[] subs = f.listFiles();
			if( subs == null ) return false;
			boolean worked = true;
			for( File sub : subs ) {
				worked &= rmR(sub);
			}
			return worked;
		}
		return f.delete();
	}
}
