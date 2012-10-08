package eu.peppol.outbound.smp;

import eu.peppol.outbound.util.JaxbContextCache;
import eu.peppol.outbound.util.Log;
import eu.peppol.smp.SmpLookupException;
import eu.peppol.start.identifier.*;
import eu.peppol.util.Util;
import eu.peppol.security.SmpResponseValidator;
import org.busdox.smp.EndpointType;
import org.busdox.smp.ProcessIdentifierType;
import org.busdox.smp.ServiceGroupType;
import org.busdox.smp.SignedServiceMetadataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 9:01:53 AM
 *
 * @author Nigel Parker
 * @author Steinar O. Cook
 */
public class SmpLookupManager {

    protected static final String SML_PEPPOLCENTRAL_ORG = "sml.peppolcentral.org";

    KeystoreManager keystoreManager = new KeystoreManager();

    /**
     * 
     *
     * @param participant
     * @param documentTypeIdentifier
     * @return The endpoint address for the participant and DocumentId
     * @throws RuntimeException If the end point address cannot be resolved for the participant. This is caused by a {@link java.net.UnknownHostException}
     */
    public URL getEndpointAddress(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {

        EndpointType endpointType = getEndpointType(participant, documentTypeIdentifier);
        String address = endpointType.getEndpointReference().getAddress().getValue();
        Log.info("Found endpoint address for " + participant.stringValue() + " from SMP: " + address);

        try {
            return new URL(address);
        } catch (Exception e) {
            throw new RuntimeException("SMP returned invalid URL", e);
        }
    }

    /**
     * Retrieves the end point certificate for the given combination of receiving participant id and document type identifer.
     *
     * @param participant receiving participant
     * @param documentTypeIdentifier document type to be sent
     * @return The X509Certificate for the given ParticipantId and DocumentId
     * @throws RuntimeException If the end point address cannot be resolved for the participant. This is caused by a {@link java.net.UnknownHostException}
     */
    public X509Certificate getEndpointCertificate(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {

        try {
            String body = getEndpointType(participant, documentTypeIdentifier).getCertificate();
            String endpointCertificate = "-----BEGIN CERTIFICATE-----\n" + body + "\n-----END CERTIFICATE-----";
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(endpointCertificate.getBytes()));

        } catch (CertificateException e) {
            throw new RuntimeException("Failed to get certificate from SMP for " + ParticipantId.getScheme() + ":" + participant.stringValue());
        }
    }

    private EndpointType getEndpointType(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {

        try {
            SignedServiceMetadataType serviceMetadata = getServiceMetaData(participant, documentTypeIdentifier);

            return serviceMetadata
                    .getServiceMetadata()
                    .getServiceInformation()
                    .getProcessList()
                    .getProcess()
                    .get(0)
                    .getServiceEndpointList()
                    .getEndpoint()
                    .get(0);

        } catch (Exception e) {
            throw new RuntimeException("Problem with SMP lookup", e);
        }
    }


    private static SignedServiceMetadataType getServiceMetaData(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) throws SmpSignedServiceMetaDataException {

        URL smpUrl = null;
        try {
            smpUrl = getSmpUrl(participant, documentTypeIdentifier);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to construct URL for " + participant + ", documentType" + documentTypeIdentifier + "; "+e.getMessage(), e);
        }

        InputSource smpContents = null;
        try {
            Log.debug("Constructed SMP url: " + smpUrl.toExternalForm());
            smpContents = Util.getUrlContent(smpUrl);
        } catch (Exception e) {
            throw new SmpSignedServiceMetaDataException(participant, documentTypeIdentifier, smpUrl,e);
        }

        try {

            // Parses the XML response from the SMP
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(smpContents);

            // Validates the signature
            SmpResponseValidator smpResponseValidator = new SmpResponseValidator(document);
            if (!smpResponseValidator.isSmpSignatureValid()) {
                throw new IllegalStateException("SMP response contained invalid signature");
            }

/**
 * TODO: Ucomment this once PEPPOL has decided how we can follow the chain of trust for the SMP certificate.
            // Validates the certificate supplied with the signature
            if (!keystoreManager.validate(smpResponseValidator.getCertificate())) {
                throw new IllegalStateException("SMP Certificate not valid for " + smpUrl);
            }
*/
            Unmarshaller unmarshaller = JaxbContextCache.getInstance(SignedServiceMetadataType.class).createUnmarshaller();

            return unmarshaller.unmarshal(document, SignedServiceMetadataType.class).getValue();
        } catch (Exception e) {
            throw new SmpSignedServiceMetaDataException(participant, documentTypeIdentifier, smpUrl, e);
        }
    }


    private static URL getSmpUrl(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier) throws Exception {

        String scheme = ParticipantId.getScheme();
        String value = participantId.stringValue();
        String hostname = "B-" + Util.calculateMD5(value.toLowerCase()) + "." + scheme + "." + SML_PEPPOLCENTRAL_ORG;
        String encodedParticipant = URLEncoder.encode(scheme + "::" + value, "UTF-8");
        String encodedDocumentId = URLEncoder.encode(PeppolDocumentTypeIdAcronym.getScheme() + "::" + documentTypeIdentifier.toString(), "UTF-8");

        return new URL("http://" + hostname + "/" + encodedParticipant + "/services/" + encodedDocumentId);
    }

    static URL getServiceGroupURL(ParticipantId participantId) throws SmpLookupException {
        String scheme = ParticipantId.getScheme();
        String value = participantId.stringValue();

        try {
            String hostname = "B-" + Util.calculateMD5(value.toLowerCase()) + "." + scheme + "." + SML_PEPPOLCENTRAL_ORG;

            // Example: iso6523-actorid-upis%3A%3A9908:810017902
            String encodedParticipant = URLEncoder.encode(scheme + "::", "UTF-8") + value;

            return new URL("http://" + hostname + "/" + encodedParticipant);
        } catch (Exception e) {
            throw new SmpLookupException(participantId, e);
        }
    }

    /**
     * Retrieves a group of URLs representing the documents accepted by the given participant id
     *
     * @param participantId participant id to look up
     * @return list of URLs representing each document type accepted
     */
    public static List<PeppolDocumentTypeId> getServiceGroups(ParticipantId participantId) throws SmpLookupException {
        URL serviceGroupURL = getServiceGroupURL(participantId);
        InputSource smpContents = Util.getUrlContent(serviceGroupURL);

        // Parses the XML response from the SMP
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = null;
        Document document;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(smpContents);


            // Locates the namespace URI of the root element
            String nameSpaceURI = document.getDocumentElement().getNamespaceURI();
            NodeList nodes = document.getElementsByTagNameNS(nameSpaceURI,"ServiceMetadataReference");

            List<PeppolDocumentTypeId> result = new ArrayList<PeppolDocumentTypeId>();

            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                String hrefAsString = element.getAttribute("href");
                // Gets rid of all the funnty %3A's...
                hrefAsString = URLDecoder.decode(hrefAsString, "UTF-8");
                // Grabs the entire text string after "busdox-docid-qns::"
                String docTypeAsString = hrefAsString.substring(hrefAsString.indexOf("busdox-docid-qns::")+"busdox-docid-qns::".length());

                // Parses and creates the document type id
                PeppolDocumentTypeId peppolDocumentTypeId = PeppolDocumentTypeId.valueOf(docTypeAsString);

                result.add(peppolDocumentTypeId);
            }

            return result;

        } catch (Exception e) {
            throw new IllegalStateException("Unable to obtain list of document types via ServiceGroup lookup at " + serviceGroupURL + "; " + e.getMessage(), e);
        }
    }


    public static PeppolProcessTypeId getProcessIdentifierForDocumentType(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier) throws SmpSignedServiceMetaDataException {
        SignedServiceMetadataType serviceMetaData = getServiceMetaData(participantId, documentTypeIdentifier);
        // SOAP generated type...
        ProcessIdentifierType processIdentifier = serviceMetaData.getServiceMetadata().getServiceInformation().getProcessList().getProcess().get(0).getProcessIdentifier();

        // Converts SOAP generated type into something nicer
        return PeppolProcessTypeId.valueOf(processIdentifier.getValue());
    }
}
