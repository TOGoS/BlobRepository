package togos.repository.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bitpedia.util.Base32;

import togos.blob.ByteBlob;
import togos.blob.file.FileBlob;
import togos.repository.HashMismatchException;
import togos.repository.Repository;
import togos.repository.ResourceNotFoundException;
import togos.repository.StoreException;
import togos.repository.UnsupportedSchemeException;

public class SHA1FileRepository implements Repository<ByteBlob>
{
	protected final File dataDir;
	protected final String storeSector;
	
	Random r = new Random();
	
	public SHA1FileRepository( File dataDir, String storeSector ) {
		this.dataDir = dataDir;
		this.storeSector = storeSector;
	}
	
	Pattern SHA1EXTRACTOR = Pattern.compile("^urn:(?:sha1|bitprint):([A-Z0-9]{32})");
	
	@Override public FileBlob get( String urn ) throws ResourceNotFoundException {
		FileBlob fb = findFile( urn );
		if( fb != null ) return fb;
		throw new ResourceNotFoundException(urn);
	}
	
	protected FileBlob findFile( String urn ) {
		Matcher m = SHA1EXTRACTOR.matcher(urn);
		if( !m.find() ) return null;
		if( !dataDir.exists() ) return null;
		
		String sha1Base32 = m.group(1);
		
		String postSectorPath = sha1Base32.substring(0,2) + "/" + sha1Base32;
		
		File[] sectorFileList = dataDir.listFiles();
		if( sectorFileList == null ) return null;
		
		for( File sector : sectorFileList ) {
			FileBlob blobFile = new FileBlob(sector, postSectorPath); 
			if( blobFile.exists() ) return blobFile;
		}
		return null;
	}
	
	@Override public boolean contains(String urn) {
		return findFile( urn ) != null; 
	}
	
	public void put(String urn, InputStream is)
		throws UnsupportedSchemeException, HashMismatchException, StoreException
	{
		try {
			if( !contains(urn) ) {
				Matcher m = SHA1EXTRACTOR.matcher(urn); 
				if( !m.find() ) {
					throw new UnsupportedSchemeException("Unsupported URN Scheme: "+urn);
				}
				String sha1Base32 = m.group(1);
				
				File tempFile = new File(dataDir + "/" + storeSector + "/." + sha1Base32 + "-" + r.nextInt(Integer.MAX_VALUE) + ".temp" );
				try {
					FileUtil.mkParentDirs( tempFile );
					FileOutputStream fos = new FileOutputStream( tempFile );
					MessageDigest digestor;
					try {
						digestor = MessageDigest.getInstance("SHA-1");
					} catch( NoSuchAlgorithmException e ) {
						throw new StoreException( "sha1-not-found-which-is-ridiculous", e );
					}
					byte[] buffer = new byte[65536];
					int z;
					while( (z = is.read(buffer)) > 0 ) {
						digestor.update( buffer, 0, z );
						fos.write( buffer, 0, z );
					}
					fos.close();
					byte[] digest = digestor.digest();
					String calculatedSha1Base32 = Base32.encode(digest);
					if( !calculatedSha1Base32.equals(sha1Base32) ) {
						throw new HashMismatchException( "Given and calculated hashes do not match" );
					}
					File finalFile = new File(dataDir + "/" + storeSector + "/" + sha1Base32.substring(0,2) + "/" + sha1Base32);
					FileUtil.mkParentDirs( finalFile );
					if( !tempFile.renameTo(finalFile) ) {
						throw new StoreException( "Failed to move temp file to final location" );
					}
				} finally {
					if( tempFile.exists() )	tempFile.delete();
				}
			}
		} catch( IOException e ) {
			throw new StoreException( "IOException while storing", e );
		} finally {
			try {
				is.close();
			} catch( IOException e ) {
			}
		}
	};
	
	@Override public void put(String urn, ByteBlob blob)
		throws UnsupportedSchemeException, HashMismatchException, StoreException
	{
		try {
			put( urn, blob.openInputStream() );
		} catch( IOException e ) {
			throw new StoreException(e);
		}
	}
	
	public String toString() {
		return getClass().getName()+"( dataDir @ '"+dataDir+"' )";
	}
}
