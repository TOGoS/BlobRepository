package togos.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bitpedia.util.Base32;

import togos.blob.ByteBlob;
import togos.repository.file.FileUtil;
import togos.repository.file.SHA1FileRepository;

public class SHA1FileRepositoryTest extends BlobRepositoryTest
{
	File repoDir;
	
	public void setUp() {
		repoDir = new File("temp/test-sha1-repo");
		FileUtil.rmR(repoDir);
		super.setUp();
	}
	
	@Override Repository<ByteBlob> createRepository() {
		return new SHA1FileRepository(repoDir, "tets");
	}
	
	@Override Object[] getIdentificationSchemes() {
		return new Object[] { "sha1" };
	}
	
	@Override String identify(ByteBlob b, Object idScheme) {
		try {
			InputStream is = b.openInputStream();
			try {
				MessageDigest digestor;
				try {
					digestor = MessageDigest.getInstance("SHA-1");
				} catch( NoSuchAlgorithmException e ) {
					throw new RuntimeException( "sha1-not-found-which-is-ridiculous", e );
				}
				byte[] buffer = new byte[65536];
				int z;
				while( (z = is.read(buffer)) > 0 ) {
					digestor.update( buffer, 0, z );
				}
				byte[] digest = digestor.digest();
				return "urn:sha1:"+Base32.encode(digest);
			} finally {
				is.close();
			}
		} catch( IOException e ) {
			throw new RuntimeException(e);
		}
	}
}
