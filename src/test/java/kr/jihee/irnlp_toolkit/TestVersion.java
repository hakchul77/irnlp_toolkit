/**
 * Base package
 */
package kr.jihee.irnlp_toolkit;

import junit.framework.TestCase;
import kr.jihee.irnlp_toolkit.Version;

/**
 * Unit test for version
 * 
 * @author Jihee
 */
public class TestVersion extends TestCase {

	/**
	 * Version Test
	 */
	public void testVersion() {
		System.out.println("\n----- testVersion() ------------------------------");

		assertEquals("0.8", Version.CURRENT.toString());
		System.out.println(Version.CURRENT.getProjectVersion());
	}
}
