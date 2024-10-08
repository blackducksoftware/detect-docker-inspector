package com.blackduck.integration.blackduck.dockerinspector.unit.httpclient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.blackduck.dockerinspector.output.BdioFilename;
import com.blackduck.integration.exception.IntegrationException;

public class OutputFilenameTest {

    @Test
    public void testAlpine() throws IntegrationException {
        BdioFilename outputFilename = new BdioFilename("alpine_latest_APK");
        assertEquals("alpine_latest_APK_bdio.jsonld", outputFilename.getBdioFilename());
    }

    @Test
    public void testCentos() throws IntegrationException {
        BdioFilename outputFilename = new BdioFilename("blackducksoftware_centos_minus_vim_plus_bacula_1.0_RPM");
        assertEquals("blackducksoftware_centos_minus_vim_plus_bacula_1.0_RPM_bdio.jsonld", outputFilename.getBdioFilename());
    }

    @Test
    public void testUbuntu() throws IntegrationException {
        BdioFilename outputFilename = new BdioFilename("ubuntu_latest_DPKG");
        assertEquals("ubuntu_latest_DPKG_bdio.jsonld", outputFilename.getBdioFilename());
    }

    @Test
    public void testBusybox() throws IntegrationException {
        BdioFilename outputFilename = new BdioFilename("busybox_latest_noPkgMgr");
        assertEquals("busybox_latest_noPkgMgr_bdio.jsonld", outputFilename.getBdioFilename());
    }
}
