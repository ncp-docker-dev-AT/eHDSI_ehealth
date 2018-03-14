package eu.europa.ec.sante.ehdsi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
@Configurable
@EnableConfigurationProperties
@EntityScan(basePackages = {"eu.europa.ec.sante.ehdsi"})
public class OpenNCPInitializeApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNCPInitializeApplication.class);

    public OpenNCPInitializeApplication() {
    }

    public static void main(String[] args) throws IOException {

        LOGGER.info("Starting Import process...");
        ApplicationContext context = SpringApplication.run(OpenNCPInitializeApplication.class, args);
        LOGGER.info("OS: {} ({}, {})", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        LOGGER.info("JRE: {} ({})", System.getProperty("java.version"), System.getProperty("java.vendor"));
        LOGGER.info("JVM: {} ({})", System.getProperty("java.vm.version"), System.getProperty("java.vm.name"));

        String path = "/opt/test/";
        LOGGER.info("OpenNCP root directory: '{}')", path);

        if (StringUtils.isNotBlank(path)) {

            File file = new File(path);

            if (file.isDirectory() && file.list().length > 0) {

                LOGGER.info("'{}' is not empty", path);
            } else {

                LOGGER.info("Initialization of the root directory");

                Resource test = new ClassPathResource("config");

//                Files.walk(Paths.get(test.getURI()))
//                        .filter(Files::isRegularFile)
//                        .forEach(path1 -> {
//
//                            File file1 = new File(path1);
//                            FileUtils.copyFile(source, dest);
//
//                        });

//                List<File> filesInFolder = Files.walk(Paths.get(test.getURI()))
//                        .filter(Files::isRegularFile)
//                        .map(Path::toFile)
//                        .collect(Collectors.toList());

                File file1 = new File(test.getURI());
                FileUtils.copyDirectory(file1, file);
                LOGGER.info("OK");
            }
        }
    }
}
