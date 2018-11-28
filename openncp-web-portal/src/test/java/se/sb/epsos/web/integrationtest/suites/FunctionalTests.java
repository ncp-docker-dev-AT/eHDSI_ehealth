package se.sb.epsos.web.integrationtest.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import se.sb.epsos.web.integrationtest.webtests.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        IEHack.class,
        LoginTest.class,
        QueryPersonAsDoctorTest.class,
        QueryPersonAsPharmacistTest.class,
        TrcAsDoctorTest.class,
        TrcAsPharmacistTest.class,
        DispensePrescriptionTest.class,
        PdfPageTest.class

})
public class FunctionalTests {

}
