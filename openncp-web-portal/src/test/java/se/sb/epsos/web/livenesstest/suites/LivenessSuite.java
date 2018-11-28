package se.sb.epsos.web.livenesstest.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import se.sb.epsos.web.livenesstest.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AustriaTest.class,
        CzechTest.class,
        DenmarkTest.class,
        FranceTest.class,
        ItalyTest.class,
        SpainTest.class,
        SwedenTest.class
})
public class LivenessSuite {

}
