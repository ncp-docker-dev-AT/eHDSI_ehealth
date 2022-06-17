import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class EhdsiErrorCodeTest {

    @Test
    public void testUniqueCode(){

        List<String> codes = Arrays.stream(EhdsiErrorCode.values()).map(EhdsiErrorCode::getCode).collect(Collectors.toList());
        Set<String> codesSet = new HashSet<>(codes);

        List<String> duplicateCode = codesSet.stream().filter(code -> Collections.frequency(codes, code) > 1).collect(Collectors.toList());
        Assert.assertEquals(" EHDSI error code should not have any duplicate : " + String.join(", ", duplicateCode), 0, duplicateCode.size());


    }
}
