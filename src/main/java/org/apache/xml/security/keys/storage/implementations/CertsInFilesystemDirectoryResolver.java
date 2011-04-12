/*
 * Copyright  1999-2010 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.xml.security.keys.storage.implementations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.xml.security.keys.storage.StorageResolverException;
import org.apache.xml.security.keys.storage.StorageResolverSpi;
import org.apache.xml.security.utils.Base64;

/**
 * This {@link StorageResolverSpi} makes all raw (binary) {@link X509Certificate}s
 * which reside as files in a single directory available to the {@link org.apache.xml.security.keys.storage.StorageResolver}.
 */
public class CertsInFilesystemDirectoryResolver extends StorageResolverSpi {

    /** {@link org.apache.commons.logging} logging facility */
    private static org.apache.commons.logging.Log log = 
        org.apache.commons.logging.LogFactory.getLog(
            CertsInFilesystemDirectoryResolver.class
        );

    /** Field merlinsCertificatesDir */
    String merlinsCertificatesDir = null;

    /** Field certs */
    private List<X509Certificate> certs = new ArrayList<X509Certificate>();

    /**
     * @param directoryName
     * @throws StorageResolverException
     */
    public CertsInFilesystemDirectoryResolver(String directoryName) 
        throws StorageResolverException {

        this.merlinsCertificatesDir = directoryName;

        this.readCertsFromHarddrive();
    }

    /**
     * Method readCertsFromHarddrive
     *
     * @throws StorageResolverException
     */
    private void readCertsFromHarddrive() throws StorageResolverException {

        File certDir = new File(this.merlinsCertificatesDir);
        List<String> al = new ArrayList<String>();
        String[] names = certDir.list();

        for (int i = 0; i < names.length; i++) {
            String currentFileName = names[i];

            if (currentFileName.endsWith(".crt")) {
                al.add(names[i]);
            }
        }

        CertificateFactory cf = null;

        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException ex) {
            throw new StorageResolverException("empty", ex);
        }

        if (cf == null) {
            throw new StorageResolverException("empty");
        }

        for (int i = 0; i < al.size(); i++) {
            String filename = certDir.getAbsolutePath() + File.separator + (String) al.get(i);
            File file = new File(filename);
            boolean added = false;
            String dn = null;

            try {
                FileInputStream fis = new FileInputStream(file);
                X509Certificate cert =
                    (X509Certificate) cf.generateCertificate(fis);

                fis.close();

                //add to ArrayList
                cert.checkValidity();
                this.certs.add(cert);

                dn = cert.getSubjectDN().getName();
                added = true;
            } catch (FileNotFoundException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not add certificate from file " + filename, ex);
                }
            } catch (IOException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not add certificate from file " + filename, ex);
                }
            } catch (CertificateNotYetValidException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not add certificate from file " + filename, ex);
                }
            } catch (CertificateExpiredException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not add certificate from file " + filename, ex);
                }
            } catch (CertificateException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not add certificate from file " + filename, ex);
                }
            }

            if (added && log.isDebugEnabled()) {
                log.debug("Added certificate: " + dn);
            }
        }
    }

    /** @inheritDoc */
    public Iterator<Certificate> getIterator() {
        return new FilesystemIterator(this.certs);
    }

    /**
     * Class FilesystemIterator
     */
    private static class FilesystemIterator implements Iterator<Certificate> {

        /** Field certs */
        List<X509Certificate> certs = null;

        /** Field i */
        int i;

        /**
         * Constructor FilesystemIterator
         *
         * @param certs
         */
        public FilesystemIterator(List<X509Certificate> certs) {
            this.certs = certs;
            this.i = 0;
        }

        /** @inheritDoc */
        public boolean hasNext() {
            return (this.i < this.certs.size());
        }

        /** @inheritDoc */
        public Certificate next() {
            return this.certs.get(this.i++);
        }

        /**
         * Method remove
         *
         */
        public void remove() {
            throw new UnsupportedOperationException("Can't remove keys from KeyStore");
        }
    }

    /**
     * Method main
     *
     * @param unused
     * @throws Exception
     */
    public static void main(String unused[]) throws Exception {

        CertsInFilesystemDirectoryResolver krs =
            new CertsInFilesystemDirectoryResolver(
                "data/ie/baltimore/merlin-examples/merlin-xmldsig-eighteen/certs");

        for (Iterator<Certificate> i = krs.getIterator(); i.hasNext(); ) {
            X509Certificate cert = (X509Certificate) i.next();
            byte[] ski =
                org.apache.xml.security.keys.content.x509.XMLX509SKI.getSKIBytesFromCert(cert);

            System.out.println();
            System.out.println("Base64(SKI())=                 \""
                               + Base64.encode(ski) + "\"");
            System.out.println("cert.getSerialNumber()=        \""
                               + cert.getSerialNumber().toString() + "\"");
            System.out.println("cert.getSubjectDN().getName()= \""
                               + cert.getSubjectDN().getName() + "\"");
            System.out.println("cert.getIssuerDN().getName()=  \""
                               + cert.getIssuerDN().getName() + "\"");
        }
    }
}
