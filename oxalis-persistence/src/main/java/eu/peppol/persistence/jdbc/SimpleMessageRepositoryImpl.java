/*
 * Copyright (c) 2010 - 2017 Norwegian Agency for Public Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.persistence.jdbc;

import eu.peppol.PeppolTransmissionMetaData;
import eu.peppol.evidence.TransmissionEvidence;
import eu.peppol.identifier.MessageId;
import eu.peppol.persistence.MessageMetaData;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.OxalisMessagePersistenceException;
import eu.peppol.persistence.TransferDirection;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * @author steinar
 *         Date: 11.01.2017
 *         Time: 17.58
 */
public class SimpleMessageRepositoryImpl implements MessageRepository {

    public static final String VAR_PEPPOL_IN = "/var/peppol/IN";

    Path computeFileName(PeppolTransmissionMetaData peppolTransmissionMetaData, String extension) {
        Path fileName = Paths.get(VAR_PEPPOL_IN,
                peppolTransmissionMetaData.getRecipientId().toString().replaceAll("[^a-zA-Z0-9.-]", "_"),
                peppolTransmissionMetaData.getRecipientId().toString().replaceAll("[^a-zA-Z0-9.-]", "_"),
                peppolTransmissionMetaData.getMessageId().toString() + extension);

        Path directory = fileName.getParent();
        if (Files.notExists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create directories for path " + directory);
            }
        }

        return fileName;
    }

    @Override
    public Long saveInboundMessage(PeppolTransmissionMetaData peppolTransmissionMetaData, InputStream payload) throws OxalisMessagePersistenceException {


        Path fileName = computeFileName(peppolTransmissionMetaData, ".xml");
        try {
            Files.copy(payload, fileName);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to persist payload to " + fileName.toString());
        }
        return null;
    }

    @Override
    public Long saveOutboundMessage(MessageMetaData messageMetaData, InputStream payloadDocument) throws OxalisMessagePersistenceException {
        return null;
    }

    @Override
    public Long saveOutboundMessage(MessageMetaData messageMetaData, Document payloadDocument) throws OxalisMessagePersistenceException {
        return null;
    }

    @Override
    public Long saveInboundMessage(MessageMetaData messageMetaData, InputStream payload) throws OxalisMessagePersistenceException {
        return null;
    }

    @Override
    public void saveInboundTransportReceipt(TransmissionEvidence transmissionEvidence, PeppolTransmissionMetaData peppolTransmissionMetaData) throws OxalisMessagePersistenceException {

    }


    @Override
    public void saveOutboundTransportReceipt(TransmissionEvidence transmissionEvidence, MessageId messageId) throws OxalisMessagePersistenceException {

    }

    @Override
    public MessageMetaData findByMessageNo(Long msgNo) {
        return null;
    }

    @Override
    public Optional<MessageMetaData> findByMessageId(TransferDirection transferDirection, MessageId messageId) throws IllegalStateException {
        return null;
    }

    @Override
    public List<MessageMetaData> findByMessageId(MessageId messageId) {
        return null;
    }

    @Override
    public void saveNativeInboundTransmissionEvidence(PeppolTransmissionMetaData peppolTransmissionMetaData, byte[] receiptBytes) {
        Path fileName = computeFileName(peppolTransmissionMetaData, ".smime");
        try {
            Files.write(fileName, receiptBytes);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to persist payload to " + fileName.toString());
        }

    }
}
