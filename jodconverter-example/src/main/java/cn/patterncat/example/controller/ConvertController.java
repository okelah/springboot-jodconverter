package cn.patterncat.example.controller;

import org.apache.commons.io.FilenameUtils;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.job.ConversionJobWithOptionalTargetFormatUnspecified;
import org.jodconverter.job.ConversionJobWithRequiredSourceFormatUnspecified;
import org.jodconverter.job.ConversionJobWithSourceSpecified;
import org.jodconverter.office.OfficeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

/**
 * Created by patterncat on 2017-10-22.
 */
@RestController
@RequestMapping("/convert")
public class ConvertController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertController.class);

    @Autowired
    DocumentConverter jodConverter;

    @RequestMapping(value = "/to-pdf",method = RequestMethod.POST)
    public String convert(@RequestParam(value = "file",required = true) MultipartFile file) throws IOException, OfficeException, ServletException {
        ConversionJobWithRequiredSourceFormatUnspecified unspecified = jodConverter.convert(file.getInputStream());

        String inputExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        DocumentFormat inputFormat = jodConverter.getFormatRegistry().getFormatByExtension(inputExtension);
        ConversionJobWithSourceSpecified sourceSpecified = unspecified.as(inputFormat);
        File outputFile = File.createTempFile("tmp", ".pdf");
        LOGGER.info("output file :{}",outputFile.getAbsolutePath());
        ConversionJobWithOptionalTargetFormatUnspecified targetFormatUnspecified = sourceSpecified.to(outputFile);

//        DocumentFormat outputFormat = jodConverter.getFormatRegistry().getFormatByExtension("pdf");
        targetFormatUnspecified.as(DefaultDocumentFormatRegistry.PDF)
                .execute();

        LOGGER.info("return execution");

//        jodConverter.convert(file.getInputStream())
//                .as(inputFormat)
//                .to(outputFile)
//                .as(DefaultDocumentFormatRegistry.PDF)
//                .execute();
        return outputFile.getAbsolutePath();
    }

}
