package idc.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ Base64ConverterTest.class, Base64InputStreamTest.class,
    Base64OutputStreamTest.class, DesTest.class, UtilsTest.class,
    FeistelNetworkDataCipherTest.class, HexInputStreamTest.class })
public class AllTests {

}
