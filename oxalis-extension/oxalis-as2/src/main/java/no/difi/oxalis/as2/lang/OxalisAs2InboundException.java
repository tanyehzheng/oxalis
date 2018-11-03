/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.as2.lang;

import no.difi.oxalis.as2.code.Disposition;

/**
 * @author erlend
 */
public class OxalisAs2InboundException extends OxalisAs2Exception {

    private Disposition disposition;

    public OxalisAs2InboundException(String message, Disposition disposition) {
        super(message);
        this.disposition = disposition;
    }

    public OxalisAs2InboundException(Disposition disposition, String message, Throwable cause) {
        super(message, cause);
        this.disposition = disposition;
    }

    public Disposition getDisposition() {
        return disposition;
    }
}