/*
 * Copyright 2009-2010 The Apache Software Foundation.
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
package org.apache.xml.security.test.signature;

import java.io.File;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.Constants;

public class HMACOutputLengthTest extends org.junit.Assert {
    
    /** {@link org.apache.commons.logging} logging facility */
    static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog
            (HMACOutputLengthTest.class.getName());
    
    private static DocumentBuilderFactory dbf = null;

    private static final String BASEDIR = 
        System.getProperty("basedir") == null ? "./": System.getProperty("basedir");
    private static final String SEP = System.getProperty("file.separator");

    public HMACOutputLengthTest() throws Exception {
        Init.init();
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
    }
    
    @org.junit.Test
    public void test_signature_enveloping_hmac_sha1_trunclen_0() throws Exception {
        try {
            validate("signature-enveloping-hmac-sha1-trunclen-0-attack.xml");
            fail("Expected HMACOutputLength exception");
        } catch (XMLSignatureException xse) {
            // System.out.println(xse.getMessage());
            if (xse.getMsgID().equals("algorithms.HMACOutputLengthMin")) {
                // pass
            } else {
                fail(xse.getMessage());
            }
        }
    }
    
    @org.junit.Test
    public void test_signature_enveloping_hmac_sha1_trunclen_8() throws Exception {
        try {
            validate("signature-enveloping-hmac-sha1-trunclen-8-attack.xml");
        } catch (XMLSignatureException xse) {
            // System.out.println(xse.getMessage());
            if (xse.getMsgID().equals("algorithms.HMACOutputLengthMin")) {
                // pass
            } else {
                fail(xse.getMessage());
            }
        }
    }
    
    @org.junit.Test
    public void test_generate_hmac_sha1_40() throws Exception {
        Document doc = dbf.newDocumentBuilder().newDocument();
        XMLSignature sig = 
            new XMLSignature(
                doc, null, XMLSignature.ALGO_ID_MAC_HMAC_SHA1, 
                40, Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS
            );
        try {
            sig.sign(getSecretKey("secret".getBytes("ASCII")));
            fail("Expected HMACOutputLength Exception");
        } catch (XMLSignatureException xse) {
            // System.out.println(xse.getMessage());
            if (xse.getMsgID().equals("algorithms.HMACOutputLengthMin")) {
                // pass
            } else {
                fail(xse.getMessage());
            }
        }
    }

    private boolean validate(String data) throws Exception {
        File file = 
            new File(BASEDIR + SEP + "src/test/resources" + SEP + "javax" + SEP + "xml" 
                     + SEP + "crypto" + SEP + "dsig" + SEP, data);

        Document doc = dbf.newDocumentBuilder().parse(file);
        NodeList nl =
            doc.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
        if (nl.getLength() == 0) {
            throw new Exception("Couldn't find signature Element");
        }
        Element sigElement = (Element) nl.item(0);
        XMLSignature signature = new XMLSignature(sigElement, file.toURI().toString());
        SecretKey sk = signature.createSecretKey("secret".getBytes("ASCII"));
        return signature.checkSignatureValue(sk);
    }

    private SecretKey getSecretKey(final byte[] secret) {
        return new SecretKey() {
            public String getFormat()   { return "RAW"; }
            public byte[] getEncoded()  { return secret; }
            public String getAlgorithm(){ return "SECRET"; }
        };
    }
    
}
