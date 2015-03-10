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
import togos.repository.UnsuitablePayloadException;
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
	
	protected String _put(String urn, InputStream is)
		throws UnsupportedSchemeException, HashMismatchException, StoreException
	{
		try {
			if( urn != null && contains(urn) ) return urn;
			
			String tempName;
			String givenBase32Hash;
			if( urn != null ) {
				Matcher m = SHA1EXTRACTOR.matcher(urn); 
				if( !m.find() ) {
					throw new UnsupportedSchemeException("Unsupported URN Scheme: "+urn);
				}
				tempName = givenBase32Hash = m.group(1);
			} else {
				givenBase32Hash = null;
				tempName = String.valueOf(is.hashCode());
			}
			
			String base32Hash;
			File tempFile = new File(dataDir + "/" + storeSector + "/." + tempName + "-" + System.currentTimeMillis() + "-" + r.nextInt(Integer.MAX_VALUE) + ".temp" );
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
				byte[] hash = digestor.digest();
				base32Hash = Base32.encode(hash);
				
				if( givenBase32Hash != null && !base32Hash.equals(givenBase32Hash) ) {
					throw new HashMismatchException("Given and calculated hashes do not match: "+givenBase32Hash+", "+base32Hash);
				}
				
				File finalFile = new File(dataDir + "/" + storeSector + "/" + base32Hash.substring(0,2) + "/" + base32Hash);
				FileUtil.mkParentDirs( finalFile );
				if( !tempFile.renameTo(finalFile) ) {
					throw new StoreException( "Failed to move temp file to final location" );
				}
			} finally {
				if( tempFile.exists() )	tempFile.delete();
			}
			
			return "urn:sha1:"+base32Hash;
		} catch( IOException e ) {
			throw new StoreException( "IOException while storing", e );
		} finally {
			try {
				is.close();
			} catch( IOException e ) {
			}
		}
	};
	
	@Override
	public String store(ByteBlob blob)
		throws UnsuitablePayloadException, StoreException
	{
		try {
			return _put( null, blob.openInputStream() );
		} catch( HashMismatchException e ) {
			// Well this just shouldn't happen
			throw new StoreException(e);
		} catch( UnsupportedSchemeException e ) {
			// This either
			throw new StoreException(e);
		} catch( IOException e ) {
			throw new StoreException(e);
		}
	}
	
	@Override public void put(String urn, ByteBlob blob)
		throws UnsupportedSchemeException, HashMismatchException, StoreException
	{
		try {
			_put( urn, blob.openInputStream() );
		} catch( IOException e ) {
			throw new StoreException(e);
		}
	}
	
	public String toString() {
		return getClass().getName()+"( dataDir @ '"+dataDir+"' )";
	}
}
