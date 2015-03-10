package togos.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import junit.framework.TestCase;
import togos.blob.ByteBlob;
import togos.blob.ByteChunk;
import togos.blob.file.FileBlob;
import togos.blob.util.SimpleByteChunk;

public abstract class BlobRepositoryTest extends TestCase
{
	abstract Object[] getIdentificationSchemes();
	abstract String identify(ByteBlob b, Object idScheme);
	abstract Repository<ByteBlob> createRepository();
	
	protected Repository<ByteBlob> repo;
	protected Random rand;

	public void setUp() {
		this.repo = createRepository();
		rand = new Random(90210);
	}
	
	protected void assertEquals( ByteBlob b1, ByteBlob b2 ) throws IOException {
		InputStream i1 = b1.openInputStream();
		try {
			InputStream i2 = b2.openInputStream();
			try {
				if( i1.read() != i2.read() ) {
					fail("Blobs didn't match");
				}
			} finally {
				i2.close();
			}
		} finally {
			i1.close();
		}
	}
	
	protected void testPutGet(ByteBlob b, boolean provideHash) {
		for( Object idScheme : getIdentificationSchemes() ) {
			String id = identify(b, idScheme);
			try {
				if( provideHash ) {
					repo.put(id, b);
				} else {
					String storedId = repo.store(b);
					assertEquals(id, storedId);
				}
				
				ByteBlob stored = repo.get(id);
				assertEquals(b, stored);
			} catch( Exception e ) {
				throw new RuntimeException(e);
			}
		}
	}
	
	protected ByteChunk randomByteChunk(int size) {
		byte[] dat = new byte[size];
		rand.nextBytes(dat);
		return SimpleByteChunk.get(dat);
	}
	
	protected void testStoreRandomChunks(boolean provideHash) {
		for( int i=0; i<17; ++i ) {
			for( int j=-1; j<=1; ++j ) {
				int size = 1<<i + j;
				if( size >= 0 ) {
					testPutGet(randomByteChunk(size), provideHash);
				}
			}
		}
	}
	
	public void testPutRandomChunks() {
		testStoreRandomChunks(true);
	}
	public void testStoreRandomChunks() {
		testStoreRandomChunks(false);
	}
	
	public void testStoreBiggishFile(boolean provideHash) throws IOException {
		File tempFile = File.createTempFile(".rando", ".dat", new File("temp"));
		FileOutputStream fos = new FileOutputStream(tempFile);
		try {
			byte[] buffer = new byte[1<<16+5];
			int written = 0;
			while( written < 1024*1024*10 ) {
				rand.nextBytes(buffer);
				fos.write(buffer);
				written += buffer.length;
			}
		} finally {
			fos.close();
		}
		FileBlob fb = new FileBlob(tempFile);
		testPutGet(fb, provideHash);
	}
	
	public void testPutBiggishFile() throws IOException {
		testStoreBiggishFile(true);
	}
	public void testStoreBiggishFile() throws IOException {
		testStoreBiggishFile(false);
	}
}
